package com.example.receipt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ReceiptUpsertRequest {
    
    @NotBlank(message = "Receipt date is required and cannot be blank")
    private String receiptDate;  // ISO format datetime string
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private Double total;
    
    @NotBlank(message = "Store name is required and cannot be blank")
    @Size(min = 1, max = 255, message = "Store name must be between 1 and 255 characters")
    private String storeName;
    
    @Size(max = 500, message = "Receipt description must not exceed 500 characters")
    private String receiptDescription;
    
    @Valid
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
