package com.example.receipt.dto;

public class PropertyReceiptDto {
    
    private Long propertyId;
    private String propertyName;
    private String propertyAddress;
    private Double portion;
    
    // Constructors
    public PropertyReceiptDto() {
    }
    
    public PropertyReceiptDto(Long propertyId, String propertyName, String propertyAddress, Double portion) {
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.propertyAddress = propertyAddress;
        this.portion = portion;
    }
    
    // Getters and Setters
    public Long getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public String getPropertyAddress() {
        return propertyAddress;
    }
    
    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }
    
    public Double getPortion() {
        return portion;
    }
    
    public void setPortion(Double portion) {
        this.portion = portion;
    }
}
