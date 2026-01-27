package com.example.receipt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.receipt.service.impl.ReceiptServiceImpl;
import com.example.receipt.dto.ReceiptDto;
import com.example.receipt.dto.PropertyAllocationDto;
import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.ReceiptSource;
import com.example.receipt.repository.ReceiptRepository;
import com.example.receipt.repository.ReceiptSourceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReceiptServiceImplReceiptSourceTest {
    
    @Mock
    private ReceiptRepository receiptRepository;
    
    @Mock
    private ReceiptSourceRepository receiptSourceRepository;
    
    @InjectMocks
    private ReceiptServiceImpl receiptService;
    
    @BeforeEach
    public void setUp() {
    }
    
    @Test
    public void testUpsertReceipt_WithExistingReceiptSource() {
        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setDescription("Store - Items");
        receiptDto.setAmount(100.0);
        receiptDto.setReceiptDate("2026-01-26T10:30:00Z");
        receiptDto.setYear(2026);
        receiptDto.setReceiptSourceId(1L);
        
        ReceiptSource existingSource = new ReceiptSource();
        existingSource.setId(1L);
        existingSource.setRetailerName("STORE");
        
        Receipt savedReceipt = new Receipt();
        savedReceipt.setId(100L);
        savedReceipt.setDescription("Store - Items");
        savedReceipt.setAmount(100.0);
        savedReceipt.setReceiptSource(existingSource);
        
        when(receiptSourceRepository.findById(1L)).thenReturn(Optional.of(existingSource));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(savedReceipt);
        
        ReceiptDto result = receiptService.upsertReceipt(receiptDto);
        
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getReceiptSourceId());
        verify(receiptSourceRepository).findById(1L);
    }
    
    @Test
    public void testUpsertReceipt_CreateNewReceiptSource() {
        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setDescription("NewStore - Items");
        receiptDto.setAmount(150.0);
        receiptDto.setReceiptDate("2026-01-26T10:30:00Z");
        receiptDto.setYear(2026);
        receiptDto.setReceiptSourceId(2L);
        
        Receipt savedReceipt = new Receipt();
        savedReceipt.setId(101L);
        savedReceipt.setDescription("NewStore - Items");
        savedReceipt.setAmount(150.0);
        
        ReceiptSource newSource = new ReceiptSource();
        newSource.setId(2L);
        newSource.setRetailerName("NEWSTORE - ITEMS");
        savedReceipt.setReceiptSource(newSource);
        
        when(receiptSourceRepository.findById(2L)).thenReturn(Optional.empty());
        when(receiptSourceRepository.save(any(ReceiptSource.class))).thenReturn(newSource);
        when(receiptRepository.save(any(Receipt.class))).thenReturn(savedReceipt);
        
        ReceiptDto result = receiptService.upsertReceipt(receiptDto);
        
        assertNotNull(result);
        assertEquals(101L, result.getId());
        verify(receiptSourceRepository).findById(2L);
        verify(receiptSourceRepository).save(any(ReceiptSource.class));
    }
    
    @Test
    public void testUpsertReceipt_UpdateWithExistingSource() {
        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setId(50L);
        receiptDto.setDescription("UpdatedStore - Items");
        receiptDto.setAmount(200.0);
        receiptDto.setReceiptDate("2026-01-26T10:30:00Z");
        receiptDto.setYear(2026);
        receiptDto.setReceiptSourceId(3L);
        
        Receipt existingReceipt = new Receipt();
        existingReceipt.setId(50L);
        existingReceipt.setDescription("OldStore - Items");
        existingReceipt.setAmount(100.0);
        
        ReceiptSource source = new ReceiptSource();
        source.setId(3L);
        source.setRetailerName("STORE");
        
        Receipt updatedReceipt = new Receipt();
        updatedReceipt.setId(50L);
        updatedReceipt.setDescription("UpdatedStore - Items");
        updatedReceipt.setAmount(200.0);
        updatedReceipt.setReceiptSource(source);
        
        when(receiptRepository.existsById(50L)).thenReturn(true);
        when(receiptRepository.findById(50L)).thenReturn(Optional.of(existingReceipt));
        when(receiptSourceRepository.findById(3L)).thenReturn(Optional.of(source));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(updatedReceipt);
        
        ReceiptDto result = receiptService.upsertReceipt(receiptDto);
        
        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals(3L, result.getReceiptSourceId());
        verify(receiptRepository).findById(50L);
        verify(receiptSourceRepository).findById(3L);
    }
    
    @Test
    public void testUpsertReceipt_NoReceiptSource() {
        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setDescription("Items");
        receiptDto.setAmount(75.0);
        receiptDto.setReceiptDate("2026-01-26T10:30:00Z");
        receiptDto.setYear(2026);
        receiptDto.setReceiptSourceId(null);
        
        Receipt savedReceipt = new Receipt();
        savedReceipt.setId(102L);
        savedReceipt.setDescription("Items");
        savedReceipt.setAmount(75.0);
        savedReceipt.setReceiptSource(null);
        
        when(receiptRepository.save(any(Receipt.class))).thenReturn(savedReceipt);
        
        ReceiptDto result = receiptService.upsertReceipt(receiptDto);
        
        assertNotNull(result);
        assertEquals(102L, result.getId());
        assertNull(result.getReceiptSourceId());
        verify(receiptSourceRepository, never()).findById(any());
    }
}
