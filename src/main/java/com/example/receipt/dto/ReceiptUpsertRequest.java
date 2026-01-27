package com.example.receipt.dto;

import java.util.List;

public class ReceiptUpsertRequest {
    
    private String receiptDate;  // ISO format datetime string
    private Double total;
    private String storeName;
    private String receiptDescription;
    private List<PropertyAllocationDto> properties;
    
    // Constructors
    public ReceiptUpsertRequest() {
    }
    
    public ReceiptUpsertRequest(String receiptDate, Double total, String storeName, 
                                String receiptDescription, List<PropertyAllocationDto> properties) {
        this.receiptDate = receiptDate;
        this.total = total;
        this.storeName = storeName;
        this.receiptDescription = receiptDescription;
        this.properties = properties;
    }
    
    // Getters and Setters
    public String getReceiptDate() {
        return receiptDate;
    }
    
    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }
    
    public Double getTotal() {
        return total;
    }
    
    public void setTotal(Double total) {
        this.total = total;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public String getReceiptDescription() {
        return receiptDescription;
    }
    
    public void setReceiptDescription(String receiptDescription) {
        this.receiptDescription = receiptDescription;
    }
    
    public List<PropertyAllocationDto> getProperties() {
        return properties;
    }
    
    public void setProperties(List<PropertyAllocationDto> properties) {
        this.properties = properties;
    }
}
