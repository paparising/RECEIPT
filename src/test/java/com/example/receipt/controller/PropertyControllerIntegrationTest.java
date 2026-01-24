package com.example.receipt.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.receipt.service.PropertyService;
import com.example.receipt.entity.Property;
import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.PropertyReceipt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PropertyControllerIntegrationTest {

    @Mock
    private PropertyService propertyService;

    private Property testProperty;
    private Receipt testReceipt2024;
    private Receipt testReceipt2023;

    @BeforeEach
    public void setUp() {
        testProperty = new Property();
        testProperty.setId(1L);
        testProperty.setName("Integration Test Property");
        testProperty.setStreetNumber("789");
        testProperty.setStreetName("Elm Street");
        testProperty.setCity("New York");
        testProperty.setState("NY");
        testProperty.setZipCode("10001");

        testReceipt2024 = new Receipt();
        testReceipt2024.setId(1L);
        testReceipt2024.setDescription("2024 Expense");
        testReceipt2024.setAmount(250.50);
        testReceipt2024.setReceiptDate("2024-03-15 11:00:00");
        testReceipt2024.setYear(2024);

        testReceipt2023 = new Receipt();
        testReceipt2023.setId(2L);
        testReceipt2023.setDescription("2023 Expense");
        testReceipt2023.setAmount(175.75);
        testReceipt2023.setReceiptDate("2023-07-20 14:30:00");
        testReceipt2023.setYear(2023);

        PropertyReceipt pr2024 = new PropertyReceipt();
        pr2024.setId(1L);
        pr2024.setProperty(testProperty);
        pr2024.setReceipt(testReceipt2024);
        pr2024.setPortion(250.50);

        PropertyReceipt pr2023 = new PropertyReceipt();
        pr2023.setId(2L);
        pr2023.setProperty(testProperty);
        pr2023.setReceipt(testReceipt2023);
        pr2023.setPortion(175.75);

        List<PropertyReceipt> propertyReceipts = new ArrayList<>();
        propertyReceipts.add(pr2024);
        propertyReceipts.add(pr2023);
        testProperty.setPropertyReceipts(propertyReceipts);
    }

    @Test
    public void testGetPropertyWithAllReceipts() {
        when(propertyService.getPropertyWithReceipts(1L, null))
                .thenReturn(Optional.of(testProperty));

        Optional<Property> result = propertyService.getPropertyWithReceipts(1L, null);

        assertTrue(result.isPresent());
        assertEquals("Integration Test Property", result.get().getName());
        assertEquals(2, result.get().getPropertyReceipts().size());
    }

    @Test
    public void testGetPropertyWithReceiptsByYear() {
        Property propertyWith2024Only = new Property();
        propertyWith2024Only.setId(1L);
        propertyWith2024Only.setName("Integration Test Property");
        List<PropertyReceipt> receipts = new ArrayList<>();
        PropertyReceipt pr = new PropertyReceipt();
        pr.setId(1L);
        pr.setReceipt(testReceipt2024);
        receipts.add(pr);
        propertyWith2024Only.setPropertyReceipts(receipts);

        when(propertyService.getPropertyWithReceipts(1L, 2024))
                .thenReturn(Optional.of(propertyWith2024Only));

        Optional<Property> result = propertyService.getPropertyWithReceipts(1L, 2024);

        assertTrue(result.isPresent());
        assertEquals("Integration Test Property", result.get().getName());
        assertEquals(1, result.get().getPropertyReceipts().size());
    }

    @Test
    public void testGetNonExistentProperty() {
        when(propertyService.getPropertyWithReceipts(9999L, null))
                .thenReturn(Optional.empty());

        Optional<Property> result = propertyService.getPropertyWithReceipts(9999L, null);

        assertFalse(result.isPresent());
    }
}
