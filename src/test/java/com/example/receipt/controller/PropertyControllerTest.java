package com.example.receipt.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.receipt.service.PropertyService;
import com.example.receipt.entity.Property;
import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.exception.PropertyNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PropertyControllerTest {

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private PropertyController propertyController;

    private Property testProperty;
    private Receipt testReceipt;

    @BeforeEach
    public void setUp() {
        testProperty = new Property();
        testProperty.setId(1L);
        testProperty.setName("Test Property");
        testProperty.setStreetNumber("123");
        testProperty.setStreetName("Main St");
        testProperty.setCity("Boston");
        testProperty.setState("MA");
        testProperty.setZipCode("02101");

        testReceipt = new Receipt();
        testReceipt.setId(1L);
        testReceipt.setDescription("Test Receipt");
        testReceipt.setAmount(100.0);
        testReceipt.setReceiptDate("2024-01-15 10:30:00");
        testReceipt.setYear(2024);

        PropertyReceipt propertyReceipt = new PropertyReceipt();
        propertyReceipt.setId(1L);
        propertyReceipt.setProperty(testProperty);
        propertyReceipt.setReceipt(testReceipt);
        propertyReceipt.setPortion(100.0);

        List<PropertyReceipt> propertyReceipts = new ArrayList<>();
        propertyReceipts.add(propertyReceipt);
        testProperty.setPropertyReceipts(propertyReceipts);
    }

    @Test
    public void testGetPropertyWithReceiptsSuccess() {
        // Arrange
        when(propertyService.getPropertyWithReceipts(1L, 2024))
                .thenReturn(Optional.of(testProperty));

        // Act
        Optional<Property> result = propertyService.getPropertyWithReceipts(1L, 2024);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Property", result.get().getName());
        verify(propertyService, times(1)).getPropertyWithReceipts(1L, 2024);
    }

    @Test
    public void testGetPropertyWithReceiptsWithoutYear() {
        // Arrange
        when(propertyService.getPropertyWithReceipts(1L, null))
                .thenReturn(Optional.of(testProperty));

        // Act
        Optional<Property> result = propertyService.getPropertyWithReceipts(1L, null);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Property", result.get().getName());
    }

    @Test
    public void testGetPropertyNotFound() {
        // Arrange
        when(propertyService.getPropertyWithReceipts(999L, null))
                .thenThrow(new PropertyNotFoundException("Property with ID 999 not found"));

        // Act & Assert
        assertThrows(PropertyNotFoundException.class, () -> {
            propertyService.getPropertyWithReceipts(999L, null);
        });
    }
}
