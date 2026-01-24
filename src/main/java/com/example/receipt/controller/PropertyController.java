package com.example.receipt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.receipt.service.PropertyService;
import com.example.receipt.entity.Property;
import com.example.receipt.exception.PropertyNotFoundException;
import java.util.Optional;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {
    
    @Autowired
    private PropertyService propertyService;
    
    @GetMapping("/{propertyId}/receipts")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Property> getPropertyWithReceipts(
            @PathVariable Long propertyId,
            @RequestParam(required = false) Integer year) {
        
        try {
            // Validate property ID
            if (propertyId == null || propertyId <= 0) {
                throw new IllegalArgumentException("Property ID must be a positive number");
            }
            
            // Validate year if provided
            if (year != null && year < 1900) {
                throw new IllegalArgumentException("Year must be 1900 or later");
            }
            
            Optional<Property> property = propertyService.getPropertyWithReceipts(propertyId, year);
            
            if (property.isPresent()) {
                return ResponseEntity.ok(property.get());
            } else {
                throw new PropertyNotFoundException("Property with ID " + propertyId + " not found");
            }
        } catch (PropertyNotFoundException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error retrieving property with receipts: " + ex.getMessage(), ex);
        }
    }
}
