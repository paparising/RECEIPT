package com.example.receipt.service;

import com.example.receipt.entity.Property;
import java.util.Optional;

public interface PropertyService {
    Optional<Property> getPropertyWithReceipts(Long propertyId, Integer year);
}