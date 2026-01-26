package com.example.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.example.receipt.entity.Receipt;
import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    
    // Search by year
    List<Receipt> findByYear(Integer year);
    
    // Search by receipt source ID
    List<Receipt> findByReceiptSourceId(Long receiptSourceId);
    
    // Search by receipt source ID and year
    List<Receipt> findByReceiptSourceIdAndYear(Long receiptSourceId, Integer year);
    
    // Check if receipt exists by ID
    boolean existsById(Long id);
    
    // Get all receipts with pagination
    Page<Receipt> findAll(Pageable pageable);
}
