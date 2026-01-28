package com.example.receipt.messaging;

import com.example.receipt.config.RabbitMQConfig;
import com.example.receipt.dto.YearlyReportRequest;
import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.enums.ReportType;
import com.example.receipt.factory.ReportGeneratorFactory;
import com.example.receipt.repository.PropertyRepository;
import com.example.receipt.service.EmailService;
import com.example.receipt.service.FailureReportService;
import com.example.receipt.service.ReportGenerator;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportMessageConsumer {

    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    @Value("${app.messaging.max-retries:3}")
    private int maxRetries;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ReportGeneratorFactory reportGeneratorFactory;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FailureReportService failureReportService;

    @RabbitListener(queues = RabbitMQConfig.REPORT_QUEUE)
    public void processReportRequest(YearlyReportRequest reportRequest, Message message, 
                                    @Header(name = RETRY_COUNT_HEADER, required = false) Integer retryCount) {
        int currentRetryCount = retryCount != null ? retryCount : 0;
        
        try {
            System.out.println("Processing report request for property: " + reportRequest.getPropertyName() + 
                             " with report type: " + reportRequest.getReportType() + 
                             " (Attempt " + (currentRetryCount + 1) + "/" + maxRetries + ")");
            
            // Find property by name
            List<Property> properties = propertyRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(reportRequest.getPropertyName()))
                .collect(Collectors.toList());

            if (properties.isEmpty()) {
                sendErrorEmail(reportRequest.getUserEmail(), reportRequest.getPropertyName(), 
                    "Property not found with name: " + reportRequest.getPropertyName());
                return;
            }

            Property property = properties.getFirst();
            
            // Filter receipts by year
            List<PropertyReceipt> yearlyReceipts = property.getPropertyReceipts().stream()
                .filter(pr -> pr.getReceipt().getYear().equals(reportRequest.getYear()))
                .collect(Collectors.toList());

            if (yearlyReceipts.isEmpty()) {
                sendErrorEmail(reportRequest.getUserEmail(), reportRequest.getPropertyName(),
                    "No receipts found for year " + reportRequest.getYear());
                return;
            }

            // Get the appropriate report generator based on report type
            ReportType reportType = ReportType.fromCode(reportRequest.getReportType());
            ReportGenerator reportGenerator = reportGeneratorFactory.getGenerator(reportType);

            // Generate report
            byte[] reportContent = reportGenerator.generateReport(
                property, 
                reportRequest.getYear(), 
                yearlyReceipts
            );

            // Create email HTML content
            String htmlContent = createEmailHtml(property, reportRequest.getYear(), yearlyReceipts, reportType);
            
            // Send email with report attachment
            String reportFileName = property.getName().replaceAll(" ", "_") + "_Report_" + reportRequest.getYear() + 
                                   "." + reportGenerator.getFileExtension();
            emailService.sendReportEmail(
                reportRequest.getUserEmail(),
                "Yearly Receipt Report (" + reportType.getCode() + ") - " + property.getName() + " (" + reportRequest.getYear() + ")",
                htmlContent,
                reportContent,
                reportFileName
            );

            System.out.println("Report sent successfully to " + reportRequest.getUserEmail());

        } catch (Exception ex) {
            System.err.println("Error processing report request (Attempt " + (currentRetryCount + 1) + "): " + ex.getMessage());
            ex.printStackTrace();
            
            if (currentRetryCount < maxRetries - 1) {
                // Retry logic - send back to queue with incremented retry count
                System.out.println("Retrying report request for property: " + reportRequest.getPropertyName() + 
                                 " (Retry " + (currentRetryCount + 1) + "/" + (maxRetries - 1) + ")");
                retryReportRequest(reportRequest, currentRetryCount + 1);
            } else {
                // Max retries exceeded - send to DLQ
                System.err.println("Max retries exceeded for report request. Sending to Dead Letter Queue.");
                sendToDLQ(reportRequest, ex.getMessage());
                
                try {
                    sendErrorEmail(reportRequest.getUserEmail(), reportRequest.getPropertyName(), 
                        "Error generating report after " + maxRetries + " attempts: " + ex.getMessage());
                } catch (Exception emailEx) {
                    System.err.println("Failed to send error email: " + emailEx.getMessage());
                }
            }
        }
    }

    private void retryReportRequest(YearlyReportRequest reportRequest, int retryCount) {
        try {
            // Send message back to queue with retry count header
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.REPORT_EXCHANGE,
                RabbitMQConfig.REPORT_ROUTING_KEY,
                reportRequest,
                message -> {
                    message.getMessageProperties().setHeader(RETRY_COUNT_HEADER, retryCount);
                    return message;
                }
            );
            System.out.println("Report request requeued with retry count: " + retryCount);
        } catch (Exception retryEx) {
            System.err.println("Failed to retry report request: " + retryEx.getMessage());
        }
    }

    private void sendToDLQ(YearlyReportRequest reportRequest, String errorMessage) {
        try {
            LocalDateTime failedTimestamp = LocalDateTime.now();
            String dlqMessage = "Failed report request for property: " + reportRequest.getPropertyName() + 
                              ", Year: " + reportRequest.getYear() + 
                              ", Error: " + errorMessage + 
                              ", Timestamp: " + failedTimestamp;
            
            // Save to database
            failureReportService.createFailureReport(
                reportRequest.getPropertyName(),
                reportRequest.getYear(),
                errorMessage,
                failedTimestamp
            );
            
            // Also send to DLQ for legacy compatibility
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.REPORT_DLQ_EXCHANGE,
                RabbitMQConfig.REPORT_DLQ_ROUTING_KEY,
                dlqMessage,
                message -> {
                    message.getMessageProperties().setHeader("original-property", reportRequest.getPropertyName().getBytes());
                    message.getMessageProperties().setHeader("original-year", reportRequest.getYear().toString().getBytes());
                    message.getMessageProperties().setHeader("failed-timestamp", failedTimestamp.toString().getBytes());
                    return message;
                }
            );
            
            System.out.println("Message sent to Dead Letter Queue and saved to failure_reports table for property: " + reportRequest.getPropertyName());
        } catch (Exception dlqEx) {
            System.err.println("Failed to send message to DLQ or save to database: " + dlqEx.getMessage());
            dlqEx.printStackTrace();
        }
    }

    @RabbitListener(queues = RabbitMQConfig.REPORT_DLQ_QUEUE)
    public void processDLQMessage(String message) {
        System.err.println("[DLQ] Processing dead letter message: " + message);
        // Messages are already saved to database via sendToDLQ()
        // This listener is for monitoring/alerting purposes
        // TODO: Integrate with monitoring/alerting system
    }

    private String createEmailHtml(Property property, Integer year, List<PropertyReceipt> receipts, ReportType reportType) {
        double totalAmount = receipts.stream().mapToDouble(PropertyReceipt::getPortion).sum();
        
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto;'>");
        
        // Header
        html.append("<h2 style='color: #2980b9;'>Yearly Receipt Report (").append(reportType.getCode()).append(")</h2>");
        html.append("<p><strong>Property:</strong> ").append(property.getName()).append("</p>");
        html.append("<p><strong>Address:</strong> ").append(property.getStreetNumber()).append(" ")
            .append(property.getStreetName()).append(", ").append(property.getCity()).append(", ")
            .append(property.getState()).append(" ").append(property.getZipCode()).append("</p>");
        html.append("<p><strong>Year:</strong> ").append(year).append("</p>");
        html.append("<p><strong>Report Type:</strong> ").append(reportType.getCode()).append("</p>");
        html.append("<p><strong>Generated:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        
        // Summary
        html.append("<hr>");
        html.append("<h3>Summary</h3>");
        html.append("<p><strong>Total Receipts:</strong> ").append(receipts.size()).append("</p>");
        html.append("<p><strong>Total Amount:</strong> $").append("%.2f".formatted(totalAmount)).append("</p>");
        
        // Table
        html.append("<hr>");
        html.append("<h3>Receipt Details</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
        html.append("<thead style='background-color: #2980b9; color: white;'>");
        html.append("<tr>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Date</th>");
        html.append("<th style='padding: 10px; text-align: left; border: 1px solid #ddd;'>Description</th>");
        html.append("<th style='padding: 10px; text-align: right; border: 1px solid #ddd;'>Amount</th>");
        html.append("<th style='padding: 10px; text-align: right; border: 1px solid #ddd;'>Portion</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (PropertyReceipt receipt : receipts) {
            html.append("<tr>");
            html.append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(receipt.getReceipt().getReceiptDate()).append("</td>");
            html.append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(receipt.getReceipt().getDescription()).append("</td>");
            html.append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>$").append("%.2f".formatted(receipt.getReceipt().getAmount())).append("</td>");
            html.append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>$").append("%.2f".formatted(receipt.getPortion())).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");
        
        // Footer
        html.append("<hr>");
        html.append("<p style='font-size: 12px; color: #666;'><em>This is an automated report generated by the Receipt System. Please see attached ").append(reportType.getCode().toUpperCase()).append(" for detailed report.</em></p>");
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }

    private void sendErrorEmail(String toEmail, String propertyName, String errorMessage) throws Exception {
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>";
        htmlContent += "<div style='max-width: 600px; margin: 0 auto;'>";
        htmlContent += "<h2 style='color: #e74c3c;'>Report Generation Error</h2>";
        htmlContent += "<p><strong>Property:</strong> " + propertyName + "</p>";
        htmlContent += "<p><strong>Error:</strong> " + errorMessage + "</p>";
        htmlContent += "<p><em>Please verify the property name and try again.</em></p>";
        htmlContent += "</div>";
        htmlContent += "</body></html>";
        
        emailService.sendReportEmail(toEmail, "Report Generation Failed - " + propertyName, htmlContent);
    }
}
