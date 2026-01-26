package com.example.receipt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.receipt.service.ReceiptService;
import com.example.receipt.dto.ReceiptDto;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {
    
    @Autowired
    private ReceiptService receiptService;
    
    // Upsert receipt (create or update)
    @PostMapping("/upsert")
    public ResponseEntity<ReceiptDto> upsertReceipt(@RequestBody ReceiptDto receiptDto) {
        ReceiptDto savedReceipt = receiptService.upsertReceipt(receiptDto);
        return ResponseEntity.ok(savedReceipt);
    }
    
    // Get all receipts with pagination (default 100 per page)
    @GetMapping
    public ResponseEntity<Page<ReceiptDto>> getAllReceipts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReceiptDto> receipts = receiptService.getAllReceipts(pageable);
        return ResponseEntity.ok(receipts);
    }
    
    // Get receipt by ID
    @GetMapping("/{id}")
    public ResponseEntity<ReceiptDto> getReceiptById(@PathVariable Long id) {
        Optional<ReceiptDto> receipt = receiptService.getReceiptById(id);
        return receipt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // Search by year
    @GetMapping("/search/year/{year}")
    public ResponseEntity<List<ReceiptDto>> getReceiptsByYear(@PathVariable Integer year) {
        List<ReceiptDto> receipts = receiptService.getReceiptsByYear(year);
        return ResponseEntity.ok(receipts);
    }
    
    // Search by source
    @GetMapping("/search/source/{sourceId}")
    public ResponseEntity<List<ReceiptDto>> getReceiptsBySource(@PathVariable Long sourceId) {
        List<ReceiptDto> receipts = receiptService.getReceiptsBySource(sourceId);
        return ResponseEntity.ok(receipts);
    }
    
    // Search by source and year
    @GetMapping("/search/source/{sourceId}/year/{year}")
    public ResponseEntity<List<ReceiptDto>> getReceiptsBySourceAndYear(
            @PathVariable Long sourceId,
            @PathVariable Integer year) {
        List<ReceiptDto> receipts = receiptService.getReceiptsBySourceAndYear(sourceId, year);
        return ResponseEntity.ok(receipts);
    }
    
    // Delete receipt
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceipt(@PathVariable Long id) {
        Optional<ReceiptDto> receipt = receiptService.getReceiptById(id);
        if (receipt.isPresent()) {
            receiptService.deleteReceipt(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
