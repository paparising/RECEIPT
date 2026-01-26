package com.example.receipt.service;

import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import java.util.List;

/**
 * Interface for generating reports in different formats
 */
public interface ReportGenerator {
    
    /**
     * Generate a report in the specific format
     * @param property the property entity
     * @param year the year for the report
     * @param receipts the list of property receipts
     * @return byte array containing the report content
     * @throws Exception if report generation fails
     */
    byte[] generateReport(Property property, Integer year, List<PropertyReceipt> receipts) throws Exception;
    
    /**
     * Get the file extension for this report type
     * @return file extension (e.g., "pdf", "csv")
     */
    String getFileExtension();
    
    /**
     * Get the MIME type for this report
     * @return MIME type (e.g., "application/pdf", "text/csv")
     */
    String getMimeType();
}
