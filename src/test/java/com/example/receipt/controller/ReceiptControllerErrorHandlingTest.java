package com.example.receipt.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.receipt.service.ReceiptService;
import com.example.receipt.dto.ReceiptDto;
import com.example.receipt.dto.ReceiptUpsertRequest;
import com.example.receipt.dto.PropertyAllocationDto;
import com.example.receipt.dto.ReceiptDtoMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(ReceiptController.class)
@AutoConfigureMockMvc
@Disabled("Integration tests - requires full Spring context")
public class ReceiptControllerErrorHandlingTest {
    
    @Configuration
    public static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
            return http.build();
        }
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ReceiptService receiptService;
    
    @MockBean
    private ReceiptDtoMapper receiptDtoMapper;
    
    private ReceiptUpsertRequest validRequest;
    
    @BeforeEach
    public void setUp() {
        validRequest = new ReceiptUpsertRequest();
        validRequest.setReceiptDate("2026-01-26T10:30:00Z");
        validRequest.setTotal(150.50);
        validRequest.setStoreName("Walmart");
        validRequest.setReceiptDescription("Grocery items");
        
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 60));
        properties.add(new PropertyAllocationDto("Property 2", 40));
        validRequest.setProperties(properties);
    }
    
    @Test
    public void testUpsertReceipt_Success() throws Exception {
        ReceiptDto expectedDto = new ReceiptDto();
        expectedDto.setId(1L);
        expectedDto.setAmount(150.50);
        expectedDto.setDescription("Walmart - Grocery items");
        
        when(receiptService.upsertReceipt(any(ReceiptDto.class))).thenReturn(expectedDto);
        
        mockMvc.perform(post("/api/receipts/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(150.50));
    }
    
    @Test
    public void testUpsertReceipt_InvalidPropertyPercentages() throws Exception {
        ReceiptUpsertRequest invalidRequest = new ReceiptUpsertRequest();
        invalidRequest.setReceiptDate("2026-01-26T10:30:00Z");
        invalidRequest.setTotal(150.50);
        invalidRequest.setStoreName("Walmart");
        invalidRequest.setReceiptDescription("Grocery items");
        
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 60));
        properties.add(new PropertyAllocationDto("Property 2", 30)); // Sum = 90, not 100
        invalidRequest.setProperties(properties);
        
        mockMvc.perform(post("/api/receipts/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid receipt data"))
                .andExpect(jsonPath("$.details").exists());
    }
    
    @Test
    public void testUpsertReceipt_CorrectPropertyPercentages() throws Exception {
        ReceiptUpsertRequest request = new ReceiptUpsertRequest();
        request.setReceiptDate("2026-01-26T10:30:00Z");
        request.setTotal(200.0);
        request.setStoreName("Target");
        request.setReceiptDescription("Electronics");
        
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Warehouse A", 50));
        properties.add(new PropertyAllocationDto("Warehouse B", 25));
        properties.add(new PropertyAllocationDto("Warehouse C", 25));
        request.setProperties(properties);
        
        ReceiptDto expectedDto = new ReceiptDto();
        expectedDto.setId(2L);
        expectedDto.setAmount(200.0);
        
        when(receiptService.upsertReceipt(any(ReceiptDto.class))).thenReturn(expectedDto);
        
        mockMvc.perform(post("/api/receipts/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }
    
    @Test
    public void testUpsertReceipt_NoProperties() throws Exception {
        ReceiptUpsertRequest request = new ReceiptUpsertRequest();
        request.setReceiptDate("2026-01-26T10:30:00Z");
        request.setTotal(100.0);
        request.setStoreName("Store");
        request.setReceiptDescription("Items");
        request.setProperties(null);
        
        ReceiptDto expectedDto = new ReceiptDto();
        expectedDto.setId(3L);
        expectedDto.setAmount(100.0);
        
        when(receiptService.upsertReceipt(any(ReceiptDto.class))).thenReturn(expectedDto);
        
        mockMvc.perform(post("/api/receipts/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testUpsertReceipt_ServiceException() throws Exception {
        when(receiptService.upsertReceipt(any(ReceiptDto.class)))
                .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(post("/api/receipts/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error processing receipt"))
                .andExpect(jsonPath("$.details").value("Database error"));
    }
    
    @Test
    public void testUpsertReceipt_PropertyAllocationMapping() throws Exception {
        ReceiptUpsertRequest request = new ReceiptUpsertRequest();
        request.setReceiptDate("2026-01-26T10:30:00Z");
        request.setTotal(500.0);
        request.setStoreName("BigStore");
        request.setReceiptDescription("Mix of items");
        
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Main Office", 75));
        properties.add(new PropertyAllocationDto("Branch Office", 25));
        request.setProperties(properties);
        
        ReceiptDto expectedDto = new ReceiptDto();
        expectedDto.setId(4L);
        expectedDto.setAmount(500.0);
        List<PropertyAllocationDto> returnedAllocations = new ArrayList<>();
        returnedAllocations.add(new PropertyAllocationDto("Main Office", 75));
        returnedAllocations.add(new PropertyAllocationDto("Branch Office", 25));
        expectedDto.setPropertyAllocations(returnedAllocations);
        
        when(receiptService.upsertReceipt(any(ReceiptDto.class))).thenReturn(expectedDto);
        
        mockMvc.perform(post("/api/receipts/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyAllocations").isArray())
                .andExpect(jsonPath("$.propertyAllocations[0].propertyName").value("Main Office"))
                .andExpect(jsonPath("$.propertyAllocations[0].propertyPercentage").value(75));
    }
}
