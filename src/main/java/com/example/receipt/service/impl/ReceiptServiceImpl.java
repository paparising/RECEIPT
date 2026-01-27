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
import com.example.receipt.repository.ReceiptRepository;
import com.example.receipt.repository.ReceiptSourceRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;
    
    @Autowired
    private ReceiptSourceRepository receiptSourceRepository;

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
                    source.setId(receiptDto.getReceiptSourceId());
                    source.setRetailerName(receiptDto.getDescription().toUpperCase());
                    ReceiptSource savedSource = receiptSourceRepository.save(source);
                    receipt.setReceiptSource(savedSource);
                }
            }
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
                    source.setId(receiptDto.getReceiptSourceId());
                    source.setRetailerName(receiptDto.getDescription().toUpperCase());
                    ReceiptSource savedSource = receiptSourceRepository.save(source);
                    receipt.setReceiptSource(savedSource);
                }
            }
        }
        
        receipt = receiptRepository.save(receipt);
        return convertToDto(receipt);
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
    public List<ReceiptDto> getReceiptsBySource(Long sourceId) {
        return receiptRepository.findByReceiptSourceId(sourceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptDto> getReceiptsBySourceAndYear(Long sourceId, Integer year) {
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