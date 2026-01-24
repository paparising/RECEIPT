package com.example.receipt.dto;

public class YearlyReportResponse {
    private String message;
    private String status;
    private String reportId;

    public YearlyReportResponse() {
    }

    public YearlyReportResponse(String message, String status, String reportId) {
        this.message = message;
        this.status = status;
        this.reportId = reportId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
