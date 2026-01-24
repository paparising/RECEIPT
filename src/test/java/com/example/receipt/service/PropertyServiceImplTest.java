package com.example.receipt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.receipt.entity.Property;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.entity.Receipt;
import com.example.receipt.repository.PropertyRepository;
import com.example.receipt.service.impl.PropertyServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private Property testProperty;
    private Receipt testReceipt;
    private PropertyReceipt testPropertyReceipt;

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

        testPropertyReceipt = new PropertyReceipt();
        testPropertyReceipt.setId(1L);
        testPropertyReceipt.setProperty(testProperty);
        testPropertyReceipt.setReceipt(testReceipt);
        testPropertyReceipt.setPortion(100.0);

        List<PropertyReceipt> propertyReceipts = new ArrayList<>();
        propertyReceipts.add(testPropertyReceipt);
        testProperty.setPropertyReceipts(propertyReceipts);
    }

    @Test
    public void testGetPropertyWithReceiptsWithYear() {
        // Arrange
        when(propertyRepository.findPropertyWithReceipts(1L, 2024))
                .thenReturn(Optional.of(testProperty));

        // Act
        Optional<Property> result = propertyService.getPropertyWithReceipts(1L, 2024);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Property", result.get().getName());
        assertEquals(1, result.get().getPropertyReceipts().size());
        verify(propertyRepository, times(1)).findPropertyWithReceipts(1L, 2024);
        verify(propertyRepository, never()).findPropertyWithAllReceipts(any());
    }

    @Test
    public void testGetPropertyWithReceiptsWithoutYear() {
        // Arrange
        when(propertyRepository.findPropertyWithAllReceipts(1L))
                .thenReturn(Optional.of(testProperty));

        // Act
        Optional<Property> result = propertyService.getPropertyWithReceipts(1L, null);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Property", result.get().getName());
        verify(propertyRepository, times(1)).findPropertyWithAllReceipts(1L);
        verify(propertyRepository, never()).findPropertyWithReceipts(any(), any());
    }

    @Test
    public void testGetPropertyNotFound() {
        // Arrange
        when(propertyRepository.findPropertyWithAllReceipts(999L))
                .thenReturn(Optional.empty());

        // Act
        Optional<Property> result = propertyService.getPropertyWithReceipts(999L, null);

        // Assert
        assertFalse(result.isPresent());
    }
}
