package com.example.receipt.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.example.receipt.service.impl.EmailServiceImpl;

import java.io.IOException;

@SpringBootTest
@TestPropertySource(properties = {
    "sendgrid.api.key=test-api-key",
    "sendgrid.from.email=noreply@example.com",
    "sendgrid.from.name=Receipt System"
})
public class EmailServiceImplTest {

    @Autowired
    private EmailServiceImpl emailService;

    private String testEmail = "test@example.com";
    private String testSubject = "Test Subject";
    private String testHtmlContent = "<html><body>Test Content</body></html>";
    private byte[] testPdfContent = "PDF Content".getBytes();
    private String testPdfFileName = "report.pdf";

    @Test
    public void testEmailServiceInterfaceImplemented() {
        // Assert that EmailServiceImpl implements EmailService interface
        assertTrue(emailService instanceof EmailService, 
                   "EmailServiceImpl should implement EmailService interface");
    }

    @Test
    public void testSendReportEmailMethodSignatureWithPdf() {
        // Verify that the send method with PDF parameters exists
        try {
            EmailServiceImpl.class.getMethod("sendReportEmail", 
                String.class, String.class, String.class, byte[].class, String.class);
            assertTrue(true, "Method sendReportEmail with PDF parameters exists");
        } catch (NoSuchMethodException e) {
            fail("Method sendReportEmail with PDF parameters not found");
        }
    }

    @Test
    public void testSendReportEmailMethodSignatureWithoutPdf() {
        // Verify that the send method without PDF parameters exists
        try {
            EmailServiceImpl.class.getMethod("sendReportEmail", 
                String.class, String.class, String.class);
            assertTrue(true, "Method sendReportEmail without PDF parameters exists");
        } catch (NoSuchMethodException e) {
            fail("Method sendReportEmail without PDF parameters not found");
        }
    }

    @Test
    public void testEmailServiceIsNotNull() {
        // Verify that the service is properly injected
        assertNotNull(emailService, "EmailServiceImpl should be autowired");
    }

    @Test
    public void testEmailServiceImplementsInterface() {
        // Verify that service implements the interface
        assertTrue(emailService instanceof EmailService, 
                   "EmailServiceImpl must implement EmailService interface");
    }
}
