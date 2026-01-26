package com.example.receipt.service;

import java.io.IOException;

public interface EmailService {

    /**
     * Send an email with HTML content and optional PDF attachment
     * @param toEmail recipient email address
     * @param subject email subject
     * @param htmlContent HTML content of the email
     * @param pdfContent PDF file content as byte array (optional)
     * @param pdfFileName name of the PDF file (required if pdfContent is provided)
     * @throws IOException if email sending fails
     */
    void sendReportEmail(String toEmail, String subject, String htmlContent, 
                        byte[] pdfContent, String pdfFileName) throws IOException;

    /**
     * Send an email with HTML content only (no attachment)
     * @param toEmail recipient email address
     * @param subject email subject
     * @param htmlContent HTML content of the email
     * @throws IOException if email sending fails
     */
    void sendReportEmail(String toEmail, String subject, String htmlContent) throws IOException;
}

