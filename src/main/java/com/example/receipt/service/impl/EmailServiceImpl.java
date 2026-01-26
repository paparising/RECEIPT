package com.example.receipt.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.receipt.service.EmailService;
import java.io.IOException;
import java.util.Base64;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:Receipt System}")
    private String fromName;

    @Override
    public void sendReportEmail(String toEmail, String subject, String htmlContent, 
                               byte[] pdfContent, String pdfFileName) throws IOException {
        
        try {
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            // Build email JSON manually
            String emailJson = buildEmailJson(toEmail, subject, htmlContent, pdfContent, pdfFileName);
            request.setBody(emailJson);

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Email sent successfully to " + toEmail);
            } else {
                throw new IOException("SendGrid API error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Failed to send email: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void sendReportEmail(String toEmail, String subject, String htmlContent) throws IOException {
        sendReportEmail(toEmail, subject, htmlContent, null, null);
    }

    private String buildEmailJson(String toEmail, String subject, String htmlContent, 
                                  byte[] pdfContent, String pdfFileName) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"personalizations\":[{");
        json.append("\"to\":[{\"email\":\"").append(toEmail).append("\"}]");
        json.append("}],");
        json.append("\"from\":{\"email\":\"").append(fromEmail).append("\",\"name\":\"").append(fromName).append("\"},");
        json.append("\"subject\":\"").append(escapeJson(subject)).append("\",");
        json.append("\"content\":[{\"type\":\"text/html\",\"value\":\"").append(escapeJson(htmlContent)).append("\"}]");

        if (pdfContent != null && pdfContent.length > 0) {
            String base64Content = Base64.getEncoder().encodeToString(pdfContent);
            json.append(",\"attachments\":[{");
            json.append("\"content\":\"").append(base64Content).append("\",");
            json.append("\"type\":\"application/pdf\",");
            json.append("\"filename\":\"").append(pdfFileName).append("\",");
            json.append("\"disposition\":\"attachment\"");
            json.append("}]");
        }

        json.append("}");
        return json.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
