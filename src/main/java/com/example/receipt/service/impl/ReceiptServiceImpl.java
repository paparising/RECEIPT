package com.example.receipt.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.receipt.service.ReceiptService;
import com.example.receipt.dto.ReceiptDto;
import com.example.receipt.dto.PropertyReceiptDto;
import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.ReceiptSource;
import com.example.receipt.entity.Property;
import com.example.receipt.repository.ReceiptRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

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
                ReceiptSource source = new ReceiptSource();
                source.setId(receiptDto.getReceiptSourceId());
                receipt.setReceiptSource(source);
            }
        } else {
            // Create new receipt
            receipt = new Receipt();
            receipt.setDescription(receiptDto.getDescription());
            receipt.setAmount(receiptDto.getAmount());
            receipt.setReceiptDate(receiptDto.getReceiptDate());
            receipt.setYear(receiptDto.getYear());
            
            if (receiptDto.getReceiptSourceId() != null) {
                ReceiptSource source = new ReceiptSource();
                source.setId(receiptDto.getReceiptSourceId());
                receipt.setReceiptSource(source);
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
                    // Include related properties for detailed view
                    if (receipt.getPropertyReceipts() != null && !receipt.getPropertyReceipts().isEmpty()) {
                        List<PropertyReceiptDto> relatedProperties = receipt.getPropertyReceipts()
                                .stream()
                                .map(pr -> {
                                    Property property = pr.getProperty();
                                    String address = buildAddress(property);
                                    return new PropertyReceiptDto(
                                            property.getId(),
                                            property.getName(),
                                            address,
                                            pr.getPortion()
                                    );
                                })
                                .collect(Collectors.toList());
                        dto.setRelatedProperties(relatedProperties);
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

    private String buildAddress(Property property) {
        StringBuilder address = new StringBuilder();
        if (property.getStreetNumber() != null) {
            address.append(property.getStreetNumber()).append(" ");
        }
        if (property.getStreetName() != null) {
            address.append(property.getStreetName()).append(", ");
        }
        if (property.getUnit() != null && !property.getUnit().isEmpty()) {
            address.append(property.getUnit()).append(", ");
        }
        if (property.getCity() != null) {
            address.append(property.getCity()).append(", ");
        }
        if (property.getState() != null) {
            address.append(property.getState()).append(" ");
        }
        if (property.getZipCode() != null) {
            address.append(property.getZipCode());
        }
        return address.toString().trim();
    }
}