package com.example.receipt.service;

import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.entity.Receipt;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PdfGeneratorServiceTest {

    @InjectMocks
    private PdfGeneratorService pdfGeneratorService;

    private Property testProperty;
    private List<PropertyReceipt> testReceipts;

    @BeforeEach
    public void setUp() {
        testProperty = new Property();
        testProperty.setId(1L);
        testProperty.setName("Main Building");
        testProperty.setStreetNumber("123");
        testProperty.setStreetName("Main Street");
        testProperty.setCity("Boston");
        testProperty.setState("MA");
        testProperty.setZipCode("02101");

        testReceipts = new ArrayList<>();

        // Create test receipts
        for (int i = 1; i <= 3; i++) {
            Receipt receipt = new Receipt();
            receipt.setId((long) i);
            receipt.setDescription("Receipt " + i);
            receipt.setAmount(100.0 * i);
            receipt.setReceiptDate("2024-01-" + String.format("%02d", i) + " 10:00:00");
            receipt.setYear(2024);

            PropertyReceipt propertyReceipt = new PropertyReceipt();
            propertyReceipt.setId((long) i);
            propertyReceipt.setProperty(testProperty);
            propertyReceipt.setReceipt(receipt);
            propertyReceipt.setPortion(100.0 * i);

            testReceipts.add(propertyReceipt);
        }
    }

    @Test
    public void testGenerateYearlyReportPdfSuccess() throws DocumentException {
        // Act
        byte[] pdfBytes = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, testReceipts);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF files start with %PDF
        assertTrue(new String(pdfBytes, 0, 4).contains("%PDF") || pdfBytes[0] == '%');
    }

    @Test
    public void testGenerateYearlyReportPdfWithEmptyReceipts() throws DocumentException {
        // Act
        byte[] pdfBytes = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, new ArrayList<>());

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    public void testGenerateYearlyReportPdfWithSingleReceipt() throws DocumentException {
        // Arrange
        List<PropertyReceipt> singleReceipt = new ArrayList<>();
        singleReceipt.add(testReceipts.get(0));

        // Act
        byte[] pdfBytes = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, singleReceipt);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    public void testGenerateYearlyReportPdfWithMultipleReceipts() throws DocumentException {
        // Act
        byte[] pdfBytes = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, testReceipts);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Multiple receipts should generate larger PDF
        assertTrue(pdfBytes.length > 1000);
    }

    @Test
    public void testGenerateYearlyReportPdfDifferentYears() throws DocumentException {
        // Act & Assert for different years
        byte[] pdf2024 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, testReceipts);
        byte[] pdf2023 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2023, testReceipts);
        byte[] pdf2025 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2025, testReceipts);

        assertNotNull(pdf2024);
        assertNotNull(pdf2023);
        assertNotNull(pdf2025);
        assertTrue(pdf2024.length > 0);
        assertTrue(pdf2023.length > 0);
        assertTrue(pdf2025.length > 0);
    }

    @Test
    public void testGenerateYearlyReportPdfWithDifferentProperties() throws DocumentException {
        // Arrange
        Property anotherProperty = new Property();
        anotherProperty.setId(2L);
        anotherProperty.setName("Downtown Office");
        anotherProperty.setStreetNumber("456");
        anotherProperty.setStreetName("Oak Street");
        anotherProperty.setCity("New York");
        anotherProperty.setState("NY");
        anotherProperty.setZipCode("10001");

        // Act
        byte[] pdf1 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, testReceipts);
        byte[] pdf2 = pdfGeneratorService.generateYearlyReportPdf(anotherProperty, 2024, testReceipts);

        // Assert
        assertNotNull(pdf1);
        assertNotNull(pdf2);
        assertTrue(pdf1.length > 0);
        assertTrue(pdf2.length > 0);
    }

    @Test
    public void testGenerateYearlyReportPdfWithLargeAmounts() throws DocumentException {
        // Arrange
        List<PropertyReceipt> largeAmountReceipts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Receipt receipt = new Receipt();
            receipt.setId((long) i);
            receipt.setDescription("Large Receipt " + i);
            receipt.setAmount(50000.0 * i);
            receipt.setReceiptDate("2024-01-" + String.format("%02d", i) + " 10:00:00");
            receipt.setYear(2024);

            PropertyReceipt propertyReceipt = new PropertyReceipt();
            propertyReceipt.setId((long) i);
            propertyReceipt.setProperty(testProperty);
            propertyReceipt.setReceipt(receipt);
            propertyReceipt.setPortion(50000.0 * i);

            largeAmountReceipts.add(propertyReceipt);
        }

        // Act
        byte[] pdfBytes = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, largeAmountReceipts);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    public void testGenerateYearlyReportPdfConsistency() throws DocumentException {
        // Act - Generate same report twice
        byte[] pdf1 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, testReceipts);
        byte[] pdf2 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2024, testReceipts);

        // Assert - PDFs should have same length (may differ due to timestamps, but general structure same)
        assertNotNull(pdf1);
        assertNotNull(pdf2);
        assertTrue(pdf1.length > 0);
        assertTrue(pdf2.length > 0);
        // Allow small variance due to generation timestamp
        assertTrue(Math.abs(pdf1.length - pdf2.length) < 1000);
    }

    @Test
    public void testGenerateYearlyReportPdfWithSpecialCharactersInPropertyName() throws DocumentException {
        // Arrange
        Property specialProperty = new Property();
        specialProperty.setId(3L);
        specialProperty.setName("Property & Co. - Special/Building #1");
        specialProperty.setStreetNumber("100");
        specialProperty.setStreetName("Special Lane");
        specialProperty.setCity("San Francisco");
        specialProperty.setState("CA");
        specialProperty.setZipCode("94105");

        // Act
        byte[] pdfBytes = pdfGeneratorService.generateYearlyReportPdf(specialProperty, 2024, testReceipts);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    public void testGenerateYearlyReportPdfWithBoundaryYear() throws DocumentException {
        // Act
        byte[] pdf1900 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 1900, testReceipts);
        byte[] pdf2100 = pdfGeneratorService.generateYearlyReportPdf(testProperty, 2100, testReceipts);

        // Assert
        assertNotNull(pdf1900);
        assertNotNull(pdf2100);
        assertTrue(pdf1900.length > 0);
        assertTrue(pdf2100.length > 0);
    }
}
