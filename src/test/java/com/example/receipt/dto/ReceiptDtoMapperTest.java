package com.example.receipt.dto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.receipt.entity.ReceiptSource;
import com.example.receipt.repository.ReceiptSourceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReceiptDtoMapperTest {
    
    @Mock
    private ReceiptSourceRepository receiptSourceRepository;
    
    @InjectMocks
    private ReceiptDtoMapper receiptDtoMapper;
    
    private ReceiptUpsertRequest request;
    
    @BeforeEach
    public void setUp() {
        request = new ReceiptUpsertRequest();
        request.setReceiptDate("2026-01-26T10:30:00Z");
        request.setTotal(150.50);
        request.setStoreName("Walmart");
        request.setReceiptDescription("Grocery items");
    }
    
    @Test
    public void testMapRequestToDto_ValidRequest() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 60));
        properties.add(new PropertyAllocationDto("Property 2", 40));
        request.setProperties(properties);
        
        ReceiptSource savedSource = new ReceiptSource();
        savedSource.setId(1);
        savedSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(savedSource);
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        assertNotNull(dto);
        assertEquals("2026-01-26T10:30:00Z", dto.getReceiptDate());
        assertEquals(150.50, dto.getAmount());
        assertEquals("Walmart - Grocery items", dto.getDescription());
        assertEquals(2026, dto.getYear());
        assertNotNull(dto.getPropertyAllocations());
        assertEquals(2, dto.getPropertyAllocations().size());
    }
    
    @Test
    public void testMapRequestToDto_InvalidPropertyPercentages() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 60));
        properties.add(new PropertyAllocationDto("Property 2", 30));
        request.setProperties(properties);
        
        ReceiptSource savedSource = new ReceiptSource();
        savedSource.setId(1);
        savedSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(savedSource);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> receiptDtoMapper.mapRequestToDto(request));
        
        assertTrue(exception.getMessage().contains("Property percentages must sum to 100"));
        assertTrue(exception.getMessage().contains("90"));
    }
    
    @Test
    public void testMapRequestToDto_ReceiptSourceFound() {
        ReceiptSource existingSource = new ReceiptSource();
        existingSource.setId(1);
        existingSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.of(existingSource));
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        assertNotNull(dto);
        assertNotNull(dto.getReceiptSourceId());
        assertEquals(1, dto.getReceiptSourceId());
    }
    
    @Test
    public void testMapRequestToDto_ReceiptSourceNotFound() {
        ReceiptSource savedSource = new ReceiptSource();
        savedSource.setId(1);
        savedSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(savedSource);
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        assertNotNull(dto);
        assertNotNull(dto.getReceiptSourceId());
        assertEquals(1, (int) dto.getReceiptSourceId());
        verify(receiptSourceRepository).save(any(ReceiptSource.class));
    }
    
    @Test
    public void testMapRequestToDto_StoreNameCaseInsensitive() {
        request.setStoreName("walmart");
        ReceiptSource existingSource = new ReceiptSource();
        existingSource.setId(2);
        existingSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.of(existingSource));
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        assertEquals(2, dto.getReceiptSourceId());
        verify(receiptSourceRepository).findByRetailerName("WALMART");
    }
    
    @Test
    public void testMapRequestToDto_ExtractYear() {
        request.setReceiptDate("2023-06-15T14:30:00Z");
        
        ReceiptSource savedSource = new ReceiptSource();
        savedSource.setId(1);
        savedSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(savedSource);
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        assertEquals(2023, dto.getYear());
    }
    
    @Test
    public void testMapRequestToDto_InvalidDateFormat() {
        request.setReceiptDate("invalid-date");
        
        ReceiptSource savedSource = new ReceiptSource();
        savedSource.setId(1);
        savedSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(savedSource);
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        // Should default to current year on parse error
        assertTrue(dto.getYear() > 0);
    }
    
    @Test
    public void testMapRequestToDto_NoProperties() {
        request.setProperties(null);
        
        ReceiptSource savedSource = new ReceiptSource();
        savedSource.setId(1);
        savedSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(savedSource);
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        assertNotNull(dto);
        assertNull(dto.getPropertyAllocations());
    }
    
    @Test
    public void testMapRequestToDto_PropertyPercentagesSum100() {
        List<PropertyAllocationDto> properties = new ArrayList<>();
        properties.add(new PropertyAllocationDto("Property 1", 50));
        properties.add(new PropertyAllocationDto("Property 2", 25));
        properties.add(new PropertyAllocationDto("Property 3", 25));
        request.setProperties(properties);
        
        ReceiptSource savedSource = new ReceiptSource();
        savedSource.setId(1);
        savedSource.setRetailerName("WALMART");
        
        when(receiptSourceRepository.findByRetailerName("WALMART")).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(savedSource);
        
        ReceiptDto dto = receiptDtoMapper.mapRequestToDto(request);
        
        assertNotNull(dto);
        assertEquals(3, dto.getPropertyAllocations().size());
    }
}
