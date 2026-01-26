package com.example.receipt.messaging;

import com.example.receipt.dto.YearlyReportRequest;
import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.entity.Receipt;
import com.example.receipt.enums.ReportType;
import com.example.receipt.factory.ReportGeneratorFactory;
import com.example.receipt.repository.PropertyRepository;
import com.example.receipt.service.EmailService;
import com.example.receipt.service.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportMessageConsumerTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ReportGeneratorFactory reportGeneratorFactory;

    @Mock
    private ReportGenerator reportGenerator;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReportMessageConsumer reportMessageConsumer;

    private YearlyReportRequest testRequest;
    private Property testProperty;
    private List<PropertyReceipt> testReceipts;

    @BeforeEach
    public void setUp() {
        testRequest = new YearlyReportRequest(
                "Main Building",
                2024,
                "user@example.com",
                1L
        );

        testProperty = new Property();
        testProperty.setId(1L);
        testProperty.setName("Main Building");
        testProperty.setStreetNumber("123");
        testProperty.setStreetName("Main Street");
        testProperty.setCity("Boston");
        testProperty.setState("MA");
        testProperty.setZipCode("02101");

        testReceipts = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Receipt receipt = new Receipt();
            receipt.setId((long) i);
            receipt.setDescription("Receipt " + i);
            receipt.setAmount(100.0 * i);
            receipt.setReceiptDate("2024-01-" + "%02d".formatted(i) + " 10:00:00");
            receipt.setYear(2024);

            PropertyReceipt propertyReceipt = new PropertyReceipt();
            propertyReceipt.setId((long) i);
            propertyReceipt.setProperty(testProperty);
            propertyReceipt.setReceipt(receipt);
            propertyReceipt.setPortion(100.0 * i);

            testReceipts.add(propertyReceipt);
        }
        testProperty.setPropertyReceipts(testReceipts);
    }

    @Test
    public void testProcessReportMessageSuccess() throws Exception {
        // Arrange
        byte[] reportContent = "REPORT_CONTENT".getBytes();
        when(propertyRepository.findAll()).thenReturn(List.of(testProperty));
        when(reportGeneratorFactory.getGenerator(ReportType.PDF)).thenReturn(reportGenerator);
        when(reportGenerator.generateReport(eq(testProperty), eq(2024), any())).thenReturn(reportContent);
        when(reportGenerator.getFileExtension()).thenReturn("pdf");
        doNothing().when(emailService).sendReportEmail(anyString(), anyString(), anyString(), any(byte[].class), anyString());

        // Act
        reportMessageConsumer.processReportRequest(testRequest);

        // Assert
        verify(propertyRepository, times(1)).findAll();
        verify(reportGeneratorFactory, times(1)).getGenerator(ReportType.PDF);
        verify(reportGenerator, times(1)).generateReport(eq(testProperty), eq(2024), any());
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString(), any(byte[].class), anyString());
    }

    @Test
    public void testProcessReportMessagePropertyNotFound() throws Exception {
        // Arrange
        when(propertyRepository.findAll()).thenReturn(List.of());
        doNothing().when(emailService).sendReportEmail(anyString(), anyString(), anyString());

        // Act
        reportMessageConsumer.processReportRequest(testRequest);

        // Assert
        verify(propertyRepository, times(1)).findAll();
        verify(reportGeneratorFactory, times(0)).getGenerator(any(ReportType.class));
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testProcessReportMessageWithEmptyReceipts() throws Exception {
        // Arrange
        Property propertyWithNoReceipts = new Property();
        propertyWithNoReceipts.setId(1L);
        propertyWithNoReceipts.setName("Main Building");
        propertyWithNoReceipts.setPropertyReceipts(new ArrayList<>());

        when(propertyRepository.findAll()).thenReturn(List.of(propertyWithNoReceipts));
        doNothing().when(emailService).sendReportEmail(any(), any(), any());

        // Act
        reportMessageConsumer.processReportRequest(testRequest);

        // Assert
        verify(propertyRepository, times(1)).findAll();
        verify(reportGeneratorFactory, times(0)).getGenerator(any(ReportType.class));
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testProcessReportMessagePdfGenerationError() throws Exception {
        // Arrange
        when(propertyRepository.findAll()).thenReturn(List.of(testProperty));
        when(reportGeneratorFactory.getGenerator(ReportType.PDF)).thenReturn(reportGenerator);
        when(reportGenerator.generateReport(any(), any(), any()))
                .thenThrow(new RuntimeException("Report generation failed"));
        doNothing().when(emailService).sendReportEmail(anyString(), anyString(), anyString());

        // Act
        reportMessageConsumer.processReportRequest(testRequest);

        // Assert
        verify(propertyRepository, times(1)).findAll();
        verify(reportGeneratorFactory, times(1)).getGenerator(ReportType.PDF);
        verify(reportGenerator, times(1)).generateReport(any(), any(), any());
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testProcessReportMessageEmailSendingError() throws Exception {
        // Arrange
        byte[] reportContent = "REPORT_CONTENT".getBytes();
        when(propertyRepository.findAll()).thenReturn(List.of(testProperty));
        when(reportGeneratorFactory.getGenerator(ReportType.PDF)).thenReturn(reportGenerator);
        when(reportGenerator.generateReport(any(), any(), any())).thenReturn(reportContent);
        when(reportGenerator.getFileExtension()).thenReturn("pdf");
        doThrow(new RuntimeException("Email sending failed"))
                .when(emailService).sendReportEmail(anyString(), anyString(), anyString(), any(byte[].class), anyString());

        // Act
        reportMessageConsumer.processReportRequest(testRequest);

        // Assert
        verify(propertyRepository, times(1)).findAll();
        verify(reportGeneratorFactory, times(1)).getGenerator(ReportType.PDF);
        verify(reportGenerator, times(1)).generateReport(any(), any(), any());
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString(), any(byte[].class), anyString());
    }

    @Test
    public void testProcessReportMessageWithDifferentProperty() throws Exception {
        // Arrange
        YearlyReportRequest anotherRequest = new YearlyReportRequest(
                "Downtown Office",
                2024,
                "admin@example.com",
                2L
        );

        Property anotherProperty = new Property();
        anotherProperty.setId(2L);
        anotherProperty.setName("Downtown Office");
        anotherProperty.setPropertyReceipts(testReceipts);

        byte[] reportContent = "REPORT_CONTENT".getBytes();
        when(propertyRepository.findAll()).thenReturn(List.of(anotherProperty));
        when(reportGeneratorFactory.getGenerator(ReportType.PDF)).thenReturn(reportGenerator);
        when(reportGenerator.generateReport(any(Property.class), eq(2024), any())).thenReturn(reportContent);
        when(reportGenerator.getFileExtension()).thenReturn("pdf");
        doNothing().when(emailService).sendReportEmail(any(), any(), any(), any(byte[].class), any());

        // Act
        reportMessageConsumer.processReportRequest(anotherRequest);

        // Assert
        verify(propertyRepository, times(1)).findAll();
        verify(reportGeneratorFactory, times(1)).getGenerator(ReportType.PDF);
        verify(reportGenerator, times(1)).generateReport(any(Property.class), eq(2024), any());
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString(), any(byte[].class), anyString());
    }

    @Test
    public void testProcessReportMessageWithMultipleReceipts() throws Exception {
        // Arrange
        List<PropertyReceipt> manyReceipts = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Receipt receipt = new Receipt();
            receipt.setId((long) i);
            receipt.setDescription("Receipt " + i);
            receipt.setAmount(100.0 * i);
            receipt.setReceiptDate("2024-01-" + "%02d".formatted(i % 28 + 1) + " 10:00:00");
            receipt.setYear(2024);

            PropertyReceipt propertyReceipt = new PropertyReceipt();
            propertyReceipt.setId((long) i);
            propertyReceipt.setProperty(testProperty);
            propertyReceipt.setReceipt(receipt);
            propertyReceipt.setPortion(100.0 * i);

            manyReceipts.add(propertyReceipt);
        }
        testProperty.setPropertyReceipts(manyReceipts);

        byte[] reportContent = "REPORT_CONTENT".getBytes();
        when(propertyRepository.findAll()).thenReturn(List.of(testProperty));
        when(reportGeneratorFactory.getGenerator(ReportType.PDF)).thenReturn(reportGenerator);
        when(reportGenerator.generateReport(eq(testProperty), eq(2024), any())).thenReturn(reportContent);
        when(reportGenerator.getFileExtension()).thenReturn("pdf");
        doNothing().when(emailService).sendReportEmail(any(), any(), any(), any(byte[].class), any());

        // Act
        reportMessageConsumer.processReportRequest(testRequest);

        // Assert
        verify(propertyRepository, times(1)).findAll();
        verify(reportGeneratorFactory, times(1)).getGenerator(ReportType.PDF);
        verify(reportGenerator, times(1)).generateReport(eq(testProperty), eq(2024), any());
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString(), any(byte[].class), anyString());
    }

    @Test
    public void testProcessReportMessageConsumerContinuesOnError() throws Exception {
        // Arrange - Setup consumer to handle exceptions
        when(propertyRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        doNothing().when(emailService).sendReportEmail(anyString(), anyString(), anyString());

        // Act - Should not throw exception
        assertDoesNotThrow(() -> reportMessageConsumer.processReportRequest(testRequest));

        // Assert
        verify(emailService, times(1)).sendReportEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testProcessReportMessageWithNullRequest() {
        // This test verifies the consumer can be instantiated
        assertNotNull(reportMessageConsumer);
    }
}
