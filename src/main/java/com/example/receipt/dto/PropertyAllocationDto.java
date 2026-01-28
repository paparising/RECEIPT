package com.example.receipt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PropertyAllocationDto {
    
    @NotBlank(message = "Property name is required and cannot be blank")
    private String propertyName;
    
    @NotNull(message = "Property percentage is required")
    @Min(value = 0, message = "Property percentage must be at least 0")
    @Max(value = 100, message = "Property percentage cannot exceed 100")
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
