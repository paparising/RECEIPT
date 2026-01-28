package com.example.receipt.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReceiptUpsertRequestValidationTest {

    @Autowired
    private Validator validator;

    private ReceiptUpsertRequest validRequest;

    @BeforeEach
    public void setUp() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 60));
        properties.add(new PropertyAllocationDto("Property 2", 40));

        validRequest = new ReceiptUpsertRequest(
                "2026-01-28T10:30:00Z",
                150.50,
                "WALMART",
                "Grocery items",
                properties
        );
    }

    @Test
    public void testValidRequest() {
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    public void testMissingReceiptDate() {
        validRequest.setReceiptDate(null);
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Receipt date is required")));
    }

    @Test
    public void testBlankReceiptDate() {
        validRequest.setReceiptDate("");
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Receipt date is required")));
    }

    @Test
    public void testNullTotal() {
        validRequest.setTotal(null);
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Total amount is required")));
    }

    @Test
    public void testNegativeTotal() {
        validRequest.setTotal(-50.0);
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Total amount must be greater than 0")));
    }

    @Test
    public void testZeroTotal() {
        validRequest.setTotal(0.0);
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Total amount must be greater than 0")));
    }

    @Test
    public void testMissingStoreName() {
        validRequest.setStoreName(null);
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Store name is required")));
    }

    @Test
    public void testBlankStoreName() {
        validRequest.setStoreName("   ");
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Store name is required")));
    }

    @Test
    public void testStoreNameTooLong() {
        validRequest.setStoreName("A".repeat(256));
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Store name must be between")));
    }

    @Test
    public void testReceiptDescriptionTooLong() {
        validRequest.setReceiptDescription("A".repeat(501));
        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Receipt description must not exceed")));
    }

    @Test
    public void testInvalidPropertyPercentage() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 150)); // Invalid: > 100
        validRequest.setProperties(properties);

        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Property percentage cannot exceed 100")));
    }

    @Test
    public void testNullPropertyPercentage() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        PropertyAllocationDto prop = new PropertyAllocationDto();
        prop.setPropertyName("Property 1");
        prop.setPropertyPercentage(null);
        properties.add(prop);
        validRequest.setProperties(properties);

        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Property percentage is required")));
    }

    @Test
    public void testNullPropertyName() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        PropertyAllocationDto prop = new PropertyAllocationDto();
        prop.setPropertyName(null);
        prop.setPropertyPercentage(50);
        properties.add(prop);
        validRequest.setProperties(properties);

        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Property name is required")));
    }

    @Test
    public void testNegativePropertyPercentage() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", -10));
        validRequest.setProperties(properties);

        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Property percentage must be at least 0")));
    }

    @Test
    public void testZeroPropertyPercentage() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 0));
        properties.add(new PropertyAllocationDto("Property 2", 100));
        validRequest.setProperties(properties);

        Set<ConstraintViolation<ReceiptUpsertRequest>> violations = validator.validate(validRequest);
        // 0 is allowed (min is 0), so should be valid
        assertTrue(violations.isEmpty());
    }
}
