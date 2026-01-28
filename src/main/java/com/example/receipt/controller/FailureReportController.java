package com.example.receipt.controller;

import com.example.receipt.entity.FailureReport;
import com.example.receipt.service.FailureReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for managing failure report messages
 * Provides endpoints to query, track, and resolve DLQ messages
 */
@RestController
@RequestMapping("/api/failure-reports")
public class FailureReportController {

    @Autowired
    private FailureReportService failureReportService;

    /**
     * Get all pending (unresolved) failure reports
     * @return List of pending failure reports
     */
    @GetMapping("/pending")
    public ResponseEntity<List<FailureReport>> getPendingReports() {
        List<FailureReport> reports = failureReportService.getPendingReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Get count of pending failure reports
     * @return Count of pending reports
     */
    @GetMapping("/pending/count")
    public ResponseEntity<Long> getPendingCount() {
        Long count = failureReportService.getPendingReportCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Get all failure reports for a specific property
     * @param propertyName Name of the property
     * @return List of failure reports for the property
     */
    @GetMapping("/property/{propertyName}")
    public ResponseEntity<List<FailureReport>> getByProperty(@PathVariable String propertyName) {
        List<FailureReport> reports = failureReportService.getReportsByPropertyName(propertyName);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get failure reports for a specific property and year
     * @param propertyName Name of the property
     * @param year Report year
     * @return List of failure reports
     */
    @GetMapping("/property/{propertyName}/year/{year}")
    public ResponseEntity<List<FailureReport>> getByPropertyAndYear(
            @PathVariable String propertyName,
            @PathVariable Integer year) {
        List<FailureReport> reports = failureReportService.getReportsByPropertyAndYear(propertyName, year);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get failure reports within a date range
     * @param start Start date (format: yyyy-MM-dd'T'HH:mm:ss)
     * @param end End date (format: yyyy-MM-dd'T'HH:mm:ss)
     * @return List of failure reports in date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<FailureReport>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<FailureReport> reports = failureReportService.getReportsByDateRange(start, end);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get all failure reports with specific status
     * @param status Report status (e.g., PENDING, RESOLVED, ARCHIVED)
     * @return List of reports with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FailureReport>> getByStatus(@PathVariable String status) {
        List<FailureReport> reports = failureReportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get a specific failure report by ID
     * @param id Report ID
     * @return The failure report if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<FailureReport> getById(@PathVariable Long id) {
        FailureReport report = failureReportService.getReportById(id);
        if (report != null) {
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get all failure reports
     * @return List of all failure reports
     */
    @GetMapping
    public ResponseEntity<List<FailureReport>> getAllReports() {
        List<FailureReport> reports = failureReportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Mark a failure report as resolved
     * @param id Report ID
     * @param resolution Resolution notes
     * @return Updated failure report
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<FailureReport> markAsResolved(
            @PathVariable Long id,
            @RequestParam String resolution) {
        FailureReport report = failureReportService.markAsResolved(id, resolution);
        if (report != null) {
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Archive a failure report
     * @param id Report ID
     * @return Updated failure report
     */
    @PutMapping("/{id}/archive")
    public ResponseEntity<FailureReport> archiveReport(@PathVariable Long id) {
        FailureReport report = failureReportService.archiveReport(id);
        if (report != null) {
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete a failure report
     * @param id Report ID
     * @return Response indicating deletion success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        failureReportService.deleteReport(id);
        return ResponseEntity.ok().build();
    }
}
