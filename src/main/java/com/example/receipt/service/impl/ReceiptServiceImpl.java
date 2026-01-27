package com.example.receipt.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.receipt.service.ReceiptService;
import com.example.receipt.dto.ReceiptDto;
import com.example.receipt.dto.PropertyAllocationDto;
import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.ReceiptSource;
import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.repository.ReceiptRepository;
import com.example.receipt.repository.ReceiptSourceRepository;
import com.example.receipt.repository.PropertyRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;
    
    @Autowired
    private ReceiptSourceRepository receiptSourceRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Override
    public ReceiptDto upsertReceipt(ReceiptDto receiptDto) {
        Receipt receipt;
        
        if (receiptDto.getId() != null && receiptRepository.existsById(receiptDto.getId())) {
            // Update existing receipt
            receipt = receiptRepository.findById(receiptDto.getId()).get();
            receipt.setDescription(receiptDto.getDescription());
            receipt.setAmount(receiptDto.getAmount());
            receipt.setReceiptDate(receiptDto.getReceiptDate());
            receipt.setYear(receiptDto.getYear());
            
            // Update receipt source if provided
            if (receiptDto.getReceiptSourceId() != null) {
                Optional<ReceiptSource> existingSource = receiptSourceRepository.findById(receiptDto.getReceiptSourceId());
                if (existingSource.isPresent()) {
                    receipt.setReceiptSource(existingSource.get());
                } else {
                    // Create and persist new receipt source if not found
                    ReceiptSource source = new ReceiptSource();
                    source.setRetailerName(receiptDto.getDescription().toUpperCase());
                    ReceiptSource savedSource = receiptSourceRepository.save(source);
                    receipt.setReceiptSource(savedSource);
                }
            }
            
            // Update property allocations
            updatePropertyAllocations(receipt, receiptDto.getPropertyAllocations());
        } else {
            // Create new receipt
            receipt = new Receipt();
            receipt.setDescription(receiptDto.getDescription());
            receipt.setAmount(receiptDto.getAmount());
            receipt.setReceiptDate(receiptDto.getReceiptDate());
            receipt.setYear(receiptDto.getYear());
            
            if (receiptDto.getReceiptSourceId() != null) {
                Optional<ReceiptSource> existingSource = receiptSourceRepository.findById(receiptDto.getReceiptSourceId());
                if (existingSource.isPresent()) {
                    receipt.setReceiptSource(existingSource.get());
                } else {
                    // Create and persist new receipt source if not found
                    ReceiptSource source = new ReceiptSource();
                    source.setRetailerName(receiptDto.getDescription().toUpperCase());
                    ReceiptSource savedSource = receiptSourceRepository.save(source);
                    receipt.setReceiptSource(savedSource);
                }
            }
            
            // Set property allocations for new receipt
            updatePropertyAllocations(receipt, receiptDto.getPropertyAllocations());
        }
        
        receipt = receiptRepository.save(receipt);
        return convertToDto(receipt);
    }

    private void updatePropertyAllocations(Receipt receipt, List<PropertyAllocationDto> allocations) {
        // Clear existing property allocations
        if (receipt.getPropertyReceipts() == null) {
            receipt.setPropertyReceipts(new ArrayList<>());
        } else {
            receipt.getPropertyReceipts().clear();
        }
        
        // Add new property allocations if provided
        if (allocations != null && !allocations.isEmpty()) {
            for (PropertyAllocationDto allocation : allocations) {
                // Find property by name
                List<Property> properties = propertyRepository.findAll().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(allocation.getPropertyName()))
                        .collect(Collectors.toList());
                
                if (!properties.isEmpty()) {
                    Property property = properties.get(0);
                    PropertyReceipt propertyReceipt = new PropertyReceipt();
                    propertyReceipt.setReceipt(receipt);
                    propertyReceipt.setProperty(property);
                    
                    // Set percentage from allocation
                    Integer percentage = allocation.getPropertyPercentage();
                    propertyReceipt.setPercentage(percentage);
                    
                    // Calculate portion from percentage
                    Double portion = (receipt.getAmount() * percentage) / 100.0;
                    propertyReceipt.setPortion(portion);
                    
                    receipt.getPropertyReceipts().add(propertyReceipt);
                }
            }
        }
    }

    @Override
    public Optional<ReceiptDto> getReceiptById(Long id) {
        return receiptRepository.findById(id)
                .map(receipt -> {
                    ReceiptDto dto = convertToDto(receipt);
                    // Include property allocations for detailed view
                    if (receipt.getPropertyReceipts() != null && !receipt.getPropertyReceipts().isEmpty()) {
                        List<PropertyAllocationDto> allocations = receipt.getPropertyReceipts()
                                .stream()
                                .map(pr -> {
                                    Property property = pr.getProperty();
                                    // Calculate percentage based on portion
                                    Integer percentage = (int) Math.round((pr.getPortion() / receipt.getAmount()) * 100);
                                    return new PropertyAllocationDto(
                                            property.getName(),
                                            percentage
                                    );
                                })
                                .collect(Collectors.toList());
                        dto.setPropertyAllocations(allocations);
                    }
                    return dto;
                });
    }

    @Override
    public Page<ReceiptDto> getAllReceipts(Pageable pageable) {
        return receiptRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    public void deleteReceipt(Long id) {
        if (receiptRepository.existsById(id)) {
            receiptRepository.deleteById(id);
        }
    }

    @Override
    public List<ReceiptDto> getReceiptsByYear(Integer year) {
        return receiptRepository.findByYear(year)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptDto> getReceiptsBySource(Integer sourceId) {
        return receiptRepository.findByReceiptSourceId(sourceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptDto> getReceiptsBySourceAndYear(Integer sourceId, Integer year) {
        return receiptRepository.findByReceiptSourceIdAndYear(sourceId, year)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ReceiptDto convertToDto(Receipt receipt) {
        ReceiptDto dto = new ReceiptDto();
        dto.setId(receipt.getId());
        dto.setDescription(receipt.getDescription());
        dto.setAmount(receipt.getAmount());
        dto.setReceiptDate(receipt.getReceiptDate());
        dto.setYear(receipt.getYear());
        
        if (receipt.getReceiptSource() != null) {
            dto.setReceiptSourceId(receipt.getReceiptSource().getId());
        }
        
        return dto;
    }
}