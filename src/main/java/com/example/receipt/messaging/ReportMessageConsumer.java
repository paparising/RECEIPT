package com.example.receipt.messaging;

import com.example.receipt.config.RabbitMQConfig;
import com.example.receipt.dto.YearlyReportRequest;
import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.enums.ReportType;
import com.example.receipt.factory.ReportGeneratorFactory;
import com.example.receipt.repository.PropertyRepository;
import com.example.receipt.service.EmailService;
import com.example.receipt.service.ReportGenerator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportMessageConsumer {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ReportGeneratorFactory reportGeneratorFactory;

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.REPORT_QUEUE)
    public void processReportRequest(YearlyReportRequest reportRequest) {
        try {
            System.out.println("Processing report request for property: " + reportRequest.getPropertyName() + 
                             " with report type: " + reportRequest.getReportType());
            
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
            System.err.println("Error processing report request: " + ex.getMessage());
            ex.printStackTrace();
            try {
                sendErrorEmail(reportRequest.getUserEmail(), reportRequest.getPropertyName(), 
                    "Error generating report: " + ex.getMessage());
            } catch (Exception emailEx) {
                System.err.println("Failed to send error email: " + emailEx.getMessage());
            }
        }
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
