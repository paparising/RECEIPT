package com.example.receipt.service;

import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {

    public byte[] generateYearlyReportPdf(Property property, Integer year, List<PropertyReceipt> receipts) throws DocumentException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
        Paragraph title = new Paragraph("Yearly Receipt Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Report Header
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font regularFont = new Font(Font.FontFamily.HELVETICA, 11);

        Paragraph reportHeader = new Paragraph();
        reportHeader.add(new Chunk("Property: ", headerFont));
        reportHeader.add(new Chunk(property.getName() + "\n", regularFont));
        reportHeader.add(new Chunk("Address: ", headerFont));
        reportHeader.add(new Chunk(property.getStreetNumber() + " " + property.getStreetName() + 
                ", " + property.getCity() + ", " + property.getState() + " " + property.getZipCode() + "\n", regularFont));
        reportHeader.add(new Chunk("Year: ", headerFont));
        reportHeader.add(new Chunk(year.toString() + "\n", regularFont));
        reportHeader.add(new Chunk("Report Generated: ", headerFont));
        reportHeader.add(new Chunk(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n", regularFont));
        reportHeader.setSpacingAfter(20);
        document.add(reportHeader);

        // Summary Section
        double totalAmount = receipts.stream().mapToDouble(PropertyReceipt::getPortion).sum();
        
        Paragraph summary = new Paragraph();
        summary.add(new Chunk("Summary:\n", headerFont));
        summary.add(new Chunk("Total Receipts: " + receipts.size() + "\n", regularFont));
        summary.add(new Chunk("Total Amount: $" + String.format("%.2f", totalAmount) + "\n", regularFont));
        summary.setSpacingAfter(20);
        document.add(summary);

        // Table Header
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        BaseColor headerBackgroundColor = new BaseColor(41, 128, 185);

        String[] headers = {"Date", "Description", "Amount", "Portion", "Receipt ID"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
            cell.setBackgroundColor(headerBackgroundColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(10);
            table.addCell(cell);
        }

        // Table Data
        Font tableDataFont = new Font(Font.FontFamily.HELVETICA, 10);
        for (PropertyReceipt receipt : receipts) {
            addTableRow(table, 
                receipt.getReceipt().getReceiptDate(),
                receipt.getReceipt().getDescription(),
                String.format("$%.2f", receipt.getReceipt().getAmount()),
                String.format("$%.2f", receipt.getPortion()),
                receipt.getReceipt().getId().toString(),
                tableDataFont
            );
        }

        document.add(table);

        // Footer
        document.addCreationDate();
        document.close();

        return byteArrayOutputStream.toByteArray();
    }

    private void addTableRow(PdfPTable table, String date, String description, String amount, String portion, String receiptId, Font font) {
        table.addCell(createCell(date, font, Element.ALIGN_LEFT));
        table.addCell(createCell(description, font, Element.ALIGN_LEFT));
        table.addCell(createCell(amount, font, Element.ALIGN_RIGHT));
        table.addCell(createCell(portion, font, Element.ALIGN_RIGHT));
        table.addCell(createCell(receiptId, font, Element.ALIGN_CENTER));
    }

    private PdfPCell createCell(String content, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(8);
        return cell;
    }
}
