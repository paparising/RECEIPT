package com.example.receipt.service;

import com.example.receipt.entity.FailureReport;
import com.example.receipt.repository.FailureReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FailureReportService {
    
    @Autowired
    private FailureReportRepository failureReportRepository;
    
    /**
     * Create and save a new failure report from DLQ message
     */
    public FailureReport createFailureReport(String propertyName, Integer year, String errorMessage, LocalDateTime failedTimestamp) {
        FailureReport report = new FailureReport(propertyName, year, errorMessage, failedTimestamp);
        return failureReportRepository.save(report);
    }
    
    /**
     * Get all pending failure reports
     */
    public List<FailureReport> getPendingReports() {
        return failureReportRepository.findByStatus("PENDING");
    }
    
    /**
     * Get failure reports by property name
     */
    public List<FailureReport> getReportsByPropertyName(String propertyName) {
        return failureReportRepository.findByPropertyName(propertyName);
    }
    
    /**
     * Get failure reports by property name and year
     */
    public List<FailureReport> getReportsByPropertyAndYear(String propertyName, Integer year) {
        return failureReportRepository.findByPropertyNameAndYear(propertyName, year);
    }
    
    /**
     * Get failure reports by date range
     */
    public List<FailureReport> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return failureReportRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    /**
     * Get all failure reports
     */
    public List<FailureReport> getAllReports() {
        return failureReportRepository.findAll();
    }
    
    /**
     * Get failure report by ID
     */
    public FailureReport getReportById(Long id) {
        Optional<FailureReport> report = failureReportRepository.findById(id);
        return report.orElse(null);
    }
    
    /**
     * Get failure reports by status
     */
    public List<FailureReport> getReportsByStatus(String status) {
        return failureReportRepository.findByStatus(status);
    }
    
    /**
     * Mark failure report as resolved
     */
    public FailureReport markAsResolved(Long id, String resolution) {
        Optional<FailureReport> report = failureReportRepository.findById(id);
        if (report.isPresent()) {
            FailureReport failureReport = report.get();
            failureReport.setStatus("RESOLVED");
            failureReport.setResolution(resolution);
            failureReport.setResolvedAt(LocalDateTime.now());
            return failureReportRepository.save(failureReport);
        }
        return null;
    }
    
    /**
     * Archive failure report
     */
    public FailureReport archiveReport(Long id) {
        Optional<FailureReport> report = failureReportRepository.findById(id);
        if (report.isPresent()) {
            FailureReport failureReport = report.get();
            failureReport.setStatus("ARCHIVED");
            return failureReportRepository.save(failureReport);
        }
        return null;
    }
    
    /**
     * Get count of pending failure reports
     */
    public long getPendingReportCount() {
        return failureReportRepository.countByStatus("PENDING");
    }
    
    /**
     * Delete failure report
     */
    public void deleteReport(Long id) {
        failureReportRepository.deleteById(id);
    }
}
