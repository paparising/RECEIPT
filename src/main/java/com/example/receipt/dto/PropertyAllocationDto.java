package com.example.receipt.dto;

public class PropertyAllocationDto {
    
    private String propertyName;
    private Integer propertyPercentage;
    
    // Constructors
    public PropertyAllocationDto() {
    }
    
    public PropertyAllocationDto(String propertyName, Integer propertyPercentage) {
        this.propertyName = propertyName;
        this.propertyPercentage = propertyPercentage;
    }
    
    // Getters and Setters
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public Integer getPropertyPercentage() {
        return propertyPercentage;
    }
    
    public void setPropertyPercentage(Integer propertyPercentage) {
        this.propertyPercentage = propertyPercentage;
    }
}
