package com.example.receipt.repository;

import com.example.receipt.entity.FailureReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FailureReportRepository extends JpaRepository<FailureReport, Long> {
    
    List<FailureReport> findByStatus(String status);
    
    List<FailureReport> findByPropertyName(String propertyName);
    
    List<FailureReport> findByPropertyNameAndYear(String propertyName, Integer year);
    
    List<FailureReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<FailureReport> findByStatusAndPropertyName(String status, String propertyName);
    
    long countByStatus(String status);
}
