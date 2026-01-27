package com.example.receipt.dto;

import java.util.List;

public class ReceiptDto {
    
    private Long id;
    private String description;
    private Double amount;
    private String receiptDate;
    private Integer year;
    private Long receiptSourceId;
    private List<PropertyAllocationDto> propertyAllocations;
    
    // Constructors
    public ReceiptDto() {
    }
    
    public ReceiptDto(String description, Double amount, String receiptDate, Integer year) {
        this.description = description;
        this.amount = amount;
        this.receiptDate = receiptDate;
        this.year = year;
    }
    
    public ReceiptDto(String description, Double amount, String receiptDate, Integer year, Long receiptSourceId) {
        this.description = description;
        this.amount = amount;
        this.receiptDate = receiptDate;
        this.year = year;
        this.receiptSourceId = receiptSourceId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String getReceiptDate() {
        return receiptDate;
    }
    
    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Long getReceiptSourceId() {
        return receiptSourceId;
    }
    
    public void setReceiptSourceId(Long receiptSourceId) {
        this.receiptSourceId = receiptSourceId;
    }

    public List<PropertyAllocationDto> getPropertyAllocations() {
        return propertyAllocations;
    }

    public void setPropertyAllocations(List<PropertyAllocationDto> propertyAllocations) {
        this.propertyAllocations = propertyAllocations;
    }
}
