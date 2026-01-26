package com.example.receipt.dto;

import java.io.Serial;
import java.io.Serializable;

public class YearlyReportRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String propertyName;
    private Integer year;
    private String userEmail;
    private Long userId;
    private String reportType;  // "pdf" or "csv"

    public YearlyReportRequest() {
    }

    public YearlyReportRequest(String propertyName, Integer year, String userEmail, Long userId) {
        this.propertyName = propertyName;
        this.year = year;
        this.userEmail = userEmail;
        this.userId = userId;
        this.reportType = "pdf";  // default to PDF
    }

    public YearlyReportRequest(String propertyName, Integer year, String userEmail, Long userId, String reportType) {
        this.propertyName = propertyName;
        this.year = year;
        this.userEmail = userEmail;
        this.userId = userId;
        this.reportType = reportType != null ? reportType : "pdf";
    }

    // Getters and Setters
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReportType() {
        return reportType != null ? reportType : "pdf";
    }

    public void setReportType(String reportType) {
        this.reportType = reportType != null ? reportType : "pdf";
    }
}
