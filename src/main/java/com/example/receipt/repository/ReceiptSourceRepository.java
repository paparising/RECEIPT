package com.example.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.receipt.entity.ReceiptSource;
import java.util.Optional;

@Repository
public interface ReceiptSourceRepository extends JpaRepository<ReceiptSource, Integer> {
    Optional<ReceiptSource> findByRetailerName(String retailerName);
}
