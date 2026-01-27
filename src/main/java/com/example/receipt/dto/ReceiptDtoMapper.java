package com.example.receipt.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.receipt.repository.ReceiptSourceRepository;
import com.example.receipt.entity.ReceiptSource;
import java.util.Optional;

@Component
public class ReceiptDtoMapper {
    
    @Autowired
    private ReceiptSourceRepository receiptSourceRepository;
    
    /**
     * Maps ReceiptUpsertRequest to ReceiptDto
     * 
     * @param request the receipt upsert request containing receipt details and properties
     * @return ReceiptDto ready to be passed to the service layer
     * @throws IllegalArgumentException if property percentages don't sum to 100
     */
    public ReceiptDto mapRequestToDto(ReceiptUpsertRequest request) {
        ReceiptDto dto = new ReceiptDto();
        
        // Set receipt date (ISO format)
        dto.setReceiptDate(request.getReceiptDate());
        
        // Set amount (total)
        dto.setAmount(request.getTotal());
        
        // Set description (store name + receipt description)
        String description = buildDescription(request.getStoreName(), request.getReceiptDescription());
        dto.setDescription(description);
        
        // Extract year from ISO datetime string
        int year = extractYear(request.getReceiptDate());
        dto.setYear(year);
        
        // Search for existing ReceiptSource by storeName and set ID if found, or create new
        if (request.getStoreName() != null && !request.getStoreName().isEmpty()) {
            String upperCaseStoreName = request.getStoreName().toUpperCase();
            Optional<ReceiptSource> existingSource = receiptSourceRepository.findByRetailerName(upperCaseStoreName);
            
            if (existingSource.isPresent()) {
                dto.setReceiptSourceId(existingSource.get().getId());
            } else {
                // Create new ReceiptSource if not found
                ReceiptSource newSource = new ReceiptSource();
                newSource.setRetailerName(upperCaseStoreName);
                newSource.setDescription(request.getReceiptDescription() != null ? 
                    request.getReceiptDescription() : "Auto-created receipt source");
                
                ReceiptSource savedSource = receiptSourceRepository.save(newSource);
                dto.setReceiptSourceId(savedSource.getId());
            }
        }
        
        // Validate and map properties
        if (request.getProperties() != null && !request.getProperties().isEmpty()) {
            validatePropertyPercentages(request.getProperties());
            dto.setPropertyAllocations(request.getProperties());
        }
        
        return dto;
    }
    
    /**
     * Validates that the sum of property percentages equals 100
     * 
     * @param properties the list of property allocations
     * @throws IllegalArgumentException if percentages don't sum to 100
     */
    private void validatePropertyPercentages(java.util.List<PropertyAllocationDto> properties) {
        int totalPercentage = properties.stream()
                .mapToInt(p -> p.getPropertyPercentage() != null ? p.getPropertyPercentage() : 0)
                .sum();
        
        if (totalPercentage != 100) {
            throw new IllegalArgumentException(
                String.format("Property percentages must sum to 100, but got %d", totalPercentage)
            );
        }
    }
    
    /**
     * Builds a combined description from store name and receipt description
     * 
     * @param storeName the store name
     * @param receiptDescription the receipt description
     * @return combined description
     */
    private String buildDescription(String storeName, String receiptDescription) {
        StringBuilder description = new StringBuilder();
        
        if (storeName != null && !storeName.isEmpty()) {
            description.append(storeName);
        }
        
        if (receiptDescription != null && !receiptDescription.isEmpty()) {
            if (description.length() > 0) {
                description.append(" - ");
            }
            description.append(receiptDescription);
        }
        
        return description.toString();
    }
    
    /**
     * Extracts year from ISO datetime string
     * 
     * @param isoDateTime the ISO formatted datetime string
     * @return the year extracted from the datetime, or current year if parsing fails
     */
    private int extractYear(String isoDateTime) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(isoDateTime);
            return zdt.getYear();
        } catch (Exception e) {
            // If parsing fails, return current year
            return LocalDate.now().getYear();
        }
    }
}
