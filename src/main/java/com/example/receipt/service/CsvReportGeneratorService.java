package com.example.receipt.service;

import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating yearly receipt reports in CSV format
 */
@Service
public class CsvReportGeneratorService implements ReportGenerator {

    @Override
    public byte[] generateReport(Property property, Integer year, List<PropertyReceipt> receipts) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8);
        PrintWriter printWriter = new PrintWriter(writer);

        try {
            // Add BOM for UTF-8 to ensure proper handling in Excel
            printWriter.write("\ufeff");

            // Add header information
            printWriter.println("YEARLY RECEIPT REPORT");
            printWriter.println();

            // Add property details
            printWriter.println("Property Details");
            printWriter.println("Property Name," + escapeCSV(property.getName()));
            printWriter.println("Address," + escapeCSV(property.getStreetNumber() + " " + property.getStreetName() + 
                    ", " + property.getCity() + ", " + property.getState() + " " + property.getZipCode()));
            printWriter.println("Year," + year);
            printWriter.println("Report Generated," + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            printWriter.println();

            // Add summary section
            double totalAmount = receipts.stream().mapToDouble(PropertyReceipt::getPortion).sum();
            printWriter.println("Summary");
            printWriter.println("Total Receipts," + receipts.size());
            printWriter.println("Total Amount," + String.format("%.2f", totalAmount));
            printWriter.println();

            // Add column headers
            printWriter.println("Receipt Details");
            printWriter.println("Date,Description,Amount,Portion,Receipt ID,Receipt Source");

            // Add receipt data
            for (PropertyReceipt receipt : receipts) {
                String date = receipt.getReceipt().getReceiptDate() != null ? 
                    receipt.getReceipt().getReceiptDate().toString() : "";
                String description = escapeCSV(receipt.getReceipt().getDescription());
                String amount = String.format("%.2f", receipt.getReceipt().getAmount());
                String portion = String.format("%.2f", receipt.getPortion());
                String receiptId = receipt.getReceipt().getId().toString();
                String source = receipt.getReceipt().getReceiptSource() != null ? 
                    escapeCSV(receipt.getReceipt().getReceiptSource().getRetailerName()) : "";

                printWriter.println(date + "," + description + "," + amount + "," + portion + "," + receiptId + "," + source);
            }

            printWriter.flush();
            return byteArrayOutputStream.toByteArray();

        } finally {
            printWriter.close();
            writer.close();
            byteArrayOutputStream.close();
        }
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }

    @Override
    public String getMimeType() {
        return "text/csv";
    }

    /**
     * Escape CSV special characters in a field value
     * @param value the value to escape
     * @return escaped value safe for CSV format
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // If the value contains comma, quote, or newline, wrap it in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
