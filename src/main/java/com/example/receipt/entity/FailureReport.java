package com.example.receipt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "failure_reports")
public class FailureReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String propertyName;
    
    @Column(nullable = true)
    private Integer year;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String errorMessage;
    
    @Column(nullable = false)
    private LocalDateTime failedTimestamp;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String status; // PENDING, RESOLVED, ARCHIVED
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String resolution;
    
    @Column(nullable = true)
    private LocalDateTime resolvedAt;
    
    public FailureReport() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public FailureReport(String propertyName, Integer year, String errorMessage, LocalDateTime failedTimestamp) {
        this();
        this.propertyName = propertyName;
        this.year = year;
        this.errorMessage = errorMessage;
        this.failedTimestamp = failedTimestamp;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getFailedTimestamp() {
        return failedTimestamp;
    }
    
    public void setFailedTimestamp(LocalDateTime failedTimestamp) {
        this.failedTimestamp = failedTimestamp;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    @Override
    public String toString() {
        return "FailureReport{" +
                "id=" + id +
                ", propertyName='" + propertyName + '\'' +
                ", year=" + year +
                ", errorMessage='" + errorMessage + '\'' +
                ", failedTimestamp=" + failedTimestamp +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }
}
