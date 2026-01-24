package com.example.receipt.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.receipt.service.PropertyService;
import com.example.receipt.repository.PropertyRepository;
import com.example.receipt.entity.Property;
import java.util.Optional;

@Service
public class PropertyServiceImpl implements PropertyService {
    
    @Autowired
    private PropertyRepository propertyRepository;
    
    @Override
    public Optional<Property> getPropertyWithReceipts(Long propertyId, Integer year) {
        if (year != null) {
            return propertyRepository.findPropertyWithReceipts(propertyId, year);
        } else {
            return propertyRepository.findPropertyWithAllReceipts(propertyId);
        }
    }
}