package com.example.receipt.service;

import com.example.receipt.dto.ReceiptDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ReceiptService {
    
    // Upsert a receipt (create or update)
    ReceiptDto upsertReceipt(ReceiptDto receiptDto);
    
    // Get receipt by ID
    Optional<ReceiptDto> getReceiptById(Long id);
    
    // Get all receipts with pagination (default 100 per page)
    Page<ReceiptDto> getAllReceipts(Pageable pageable);
    
    // Delete receipt by ID
    void deleteReceipt(Long id);
    
    // Search receipts by year
    List<ReceiptDto> getReceiptsByYear(Integer year);
    
    // Search receipts by receipt source ID
    List<ReceiptDto> getReceiptsBySource(Long sourceId);
    
    // Search receipts by source ID and year
    List<ReceiptDto> getReceiptsBySourceAndYear(Long sourceId, Integer year);
}
