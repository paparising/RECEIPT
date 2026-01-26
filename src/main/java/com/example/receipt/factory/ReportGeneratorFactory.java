package com.example.receipt.factory;

import com.example.receipt.enums.ReportType;
import com.example.receipt.service.ReportGenerator;
import com.example.receipt.service.CsvReportGeneratorService;
import com.example.receipt.service.PdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory pattern implementation for creating report generators.
 * 
 * This factory uses the Factory Pattern to create and return appropriate
 * ReportGenerator strategies based on the requested report type.
 * 
 * Combined Design Patterns:
 * 1. FACTORY PATTERN: This class encapsulates the creation logic for report generators,
 *    allowing clients to request generators by type without knowing creation details.
 * 2. STRATEGY PATTERN: The ReportGenerator interface and its implementations (PDF, CSV)
 *    represent different strategies for report generation that are interchangeable
 *    at runtime.
 * 
 * Example Usage:
 *     ReportGeneratorFactory factory = applicationContext.getBean(ReportGeneratorFactory.class);
 *     ReportGenerator pdfGenerator = factory.getGenerator(ReportType.PDF);
 *     byte[] pdfReport = pdfGenerator.generateReport(property, year, receipts);
 */
@Component
public class ReportGeneratorFactory {

    private final Map<ReportType, ReportGenerator> generators;

    @Autowired
    public ReportGeneratorFactory(PdfGeneratorService pdfGeneratorService, 
                                  CsvReportGeneratorService csvGeneratorService) {
        generators = new HashMap<>();
        generators.put(ReportType.PDF, pdfGeneratorService);
        generators.put(ReportType.CSV, csvGeneratorService);
    }

    /**
     * Get the appropriate report generator for the given report type
     * @param reportType the type of report to generate
     * @return the corresponding ReportGenerator implementation
     * @throws IllegalArgumentException if the report type is not supported
     */
    public ReportGenerator getGenerator(ReportType reportType) {
        if (reportType == null) {
            reportType = ReportType.PDF;
        }
        
        ReportGenerator generator = generators.get(reportType);
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }
        return generator;
    }

    /**
     * Get the appropriate report generator for the given report type code
     * @param reportTypeCode the code of the report type
     * @return the corresponding ReportGenerator implementation
     */
    public ReportGenerator getGenerator(String reportTypeCode) {
        ReportType reportType = ReportType.fromCode(reportTypeCode);
        return getGenerator(reportType);
    }
}
