package com.example.receipt.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.receipt.dto.ReceiptDto;
import com.example.receipt.dto.ReceiptUpsertRequest;
import com.example.receipt.dto.PropertyAllocationDto;
import com.example.receipt.dto.ReceiptDtoMapper;
import com.example.receipt.service.ReceiptService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReceiptControllerIntegrationTest {

    @Mock
    private ReceiptService receiptService;
    
    @Mock
    private ReceiptDtoMapper receiptDtoMapper;

    @InjectMocks
    private ReceiptController receiptController;

    private ReceiptDto testReceiptDto;

    @BeforeEach
    public void setUp() {
        testReceiptDto = new ReceiptDto();
        testReceiptDto.setId(1L);
        testReceiptDto.setDescription("Test Receipt");
        testReceiptDto.setAmount(100.0);
        testReceiptDto.setReceiptDate("2024-01-15 10:30:00");
        testReceiptDto.setYear(2024);
        testReceiptDto.setReceiptSourceId(1);
    }

    @Test
    public void testUpsertReceipt() {
        // Arrange
        ReceiptUpsertRequest request = new ReceiptUpsertRequest("2024-01-15", 100.0, "Test Store", "Test Receipt", new ArrayList<>());
        when(receiptDtoMapper.mapRequestToDto(any(ReceiptUpsertRequest.class))).thenReturn(testReceiptDto);
        when(receiptService.upsertReceipt(any(ReceiptDto.class))).thenReturn(testReceiptDto);

        // Act
        ResponseEntity<?> response = receiptController.upsertReceipt(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ReceiptDto body = (ReceiptDto) response.getBody();
        assertEquals("Test Receipt", body.getDescription());
        assertEquals(100.0, body.getAmount());
        verify(receiptService, times(1)).upsertReceipt(any(ReceiptDto.class));
    }

    @Test
    public void testGetAllReceiptsWithDefaultPagination() {
        // Arrange
        List<ReceiptDto> receiptList = new ArrayList<>();
        receiptList.add(testReceiptDto);

        ReceiptDto receipt2 = new ReceiptDto();
        receipt2.setId(2L);
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);
        receipt2.setReceiptSourceId(1);
        receiptList.add(receipt2);

        Page<ReceiptDto> page = new PageImpl<>(receiptList);
        Pageable pageable = PageRequest.of(0, 100);

        when(receiptService.getAllReceipts(pageable)).thenReturn(page);

        // Act
        ResponseEntity<Page<ReceiptDto>> response = receiptController.getAllReceipts(0, 100);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        verify(receiptService, times(1)).getAllReceipts(any(Pageable.class));
    }

    @Test
    public void testGetAllReceiptsWithCustomPagination() {
        // Arrange
        List<ReceiptDto> receiptList = new ArrayList<>();
        receiptList.add(testReceiptDto);

        Page<ReceiptDto> page = new PageImpl<>(receiptList, PageRequest.of(1, 50), 150);
        Pageable pageable = PageRequest.of(1, 50);

        when(receiptService.getAllReceipts(pageable)).thenReturn(page);

        // Act
        ResponseEntity<Page<ReceiptDto>> response = receiptController.getAllReceipts(1, 50);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalPages());
        verify(receiptService, times(1)).getAllReceipts(any(Pageable.class));
    }

    @Test
    public void testGetReceiptById() {
        // Arrange
        when(receiptService.getReceiptById(1L)).thenReturn(Optional.of(testReceiptDto));

        // Act
        ResponseEntity<ReceiptDto> response = receiptController.getReceiptById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Receipt", response.getBody().getDescription());
        verify(receiptService, times(1)).getReceiptById(1L);
    }

    @Test
    public void testGetReceiptByIdWithRelatedProperties() {
        // Arrange
        List<PropertyAllocationDto> propertyAllocations = new ArrayList<>();
        propertyAllocations.add(new PropertyAllocationDto("Property 1", 50));
        propertyAllocations.add(new PropertyAllocationDto("Property 2", 50));
        testReceiptDto.setPropertyAllocations(propertyAllocations);

        when(receiptService.getReceiptById(1L)).thenReturn(Optional.of(testReceiptDto));

        // Act
        ResponseEntity<ReceiptDto> response = receiptController.getReceiptById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Receipt", response.getBody().getDescription());
        assertNotNull(response.getBody().getPropertyAllocations());
        assertEquals(2, response.getBody().getPropertyAllocations().size());
        
        PropertyAllocationDto prop1 = response.getBody().getPropertyAllocations().get(0);
        assertEquals("Property 1", prop1.getPropertyName());
        assertEquals(50, prop1.getPropertyPercentage());
        
        verify(receiptService, times(1)).getReceiptById(1L);
    }

    @Test
    public void testGetReceiptByIdNotFound() {
        // Arrange
        when(receiptService.getReceiptById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ReceiptDto> response = receiptController.getReceiptById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(receiptService, times(1)).getReceiptById(999L);
    }

    @Test
    public void testGetReceiptsByYear() {
        // Arrange
        List<ReceiptDto> receiptList = new ArrayList<>();
        receiptList.add(testReceiptDto);

        ReceiptDto receipt2 = new ReceiptDto();
        receipt2.setId(2L);
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);
        receipt2.setReceiptSourceId(1);
        receiptList.add(receipt2);

        when(receiptService.getReceiptsByYear(2024)).thenReturn(receiptList);

        // Act
        ResponseEntity<List<ReceiptDto>> response = receiptController.getReceiptsByYear(2024);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(receiptService, times(1)).getReceiptsByYear(2024);
    }

    @Test
    public void testGetReceiptsByYearEmpty() {
        // Arrange
        when(receiptService.getReceiptsByYear(2025)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<ReceiptDto>> response = receiptController.getReceiptsByYear(2025);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(receiptService, times(1)).getReceiptsByYear(2025);
    }

    @Test
    public void testGetReceiptsBySource() {
        // Arrange
        List<ReceiptDto> receiptList = new ArrayList<>();
        receiptList.add(testReceiptDto);

        when(receiptService.getReceiptsBySource(1)).thenReturn(receiptList);

        // Act
        ResponseEntity<List<ReceiptDto>> response = receiptController.getReceiptsBySource(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1, response.getBody().get(0).getReceiptSourceId());
        verify(receiptService, times(1)).getReceiptsBySource(1);
    }

    @Test
    public void testGetReceiptsBySourceAndYear() {
        // Arrange
        List<ReceiptDto> receiptList = new ArrayList<>();
        receiptList.add(testReceiptDto);

        when(receiptService.getReceiptsBySourceAndYear(1, 2024)).thenReturn(receiptList);

        // Act
        ResponseEntity<List<ReceiptDto>> response = receiptController.getReceiptsBySourceAndYear(1, 2024);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1, response.getBody().get(0).getReceiptSourceId());
        assertEquals(2024, response.getBody().get(0).getYear());
        verify(receiptService, times(1)).getReceiptsBySourceAndYear(1, 2024);
    }

    @Test
    public void testDeleteReceipt() {
        // Arrange
        when(receiptService.getReceiptById(1L)).thenReturn(Optional.of(testReceiptDto));

        // Act
        ResponseEntity<Void> response = receiptController.deleteReceipt(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(receiptService, times(1)).getReceiptById(1L);
        verify(receiptService, times(1)).deleteReceipt(1L);
    }

    @Test
    public void testDeleteReceiptNotFound() {
        // Arrange
        when(receiptService.getReceiptById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = receiptController.deleteReceipt(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(receiptService, times(1)).getReceiptById(999L);
        verify(receiptService, never()).deleteReceipt(999L);
    }

    @Test
    public void testUpsertReceiptCreateNew() {
        // Arrange
        ReceiptUpsertRequest request = new ReceiptUpsertRequest("2024-01-20", 50.0, "New Store", "New Receipt", new ArrayList<>());
        
        ReceiptDto savedReceipt = new ReceiptDto();
        savedReceipt.setId(3L);
        savedReceipt.setDescription("New Receipt");
        savedReceipt.setAmount(50.0);
        savedReceipt.setReceiptDate("2024-01-20 14:00:00");
        savedReceipt.setYear(2024);
        savedReceipt.setReceiptSourceId(1);

        when(receiptDtoMapper.mapRequestToDto(any(ReceiptUpsertRequest.class))).thenReturn(savedReceipt);
        when(receiptService.upsertReceipt(any(ReceiptDto.class))).thenReturn(savedReceipt);

        // Act
        ResponseEntity<?> response = receiptController.upsertReceipt(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ReceiptDto body = (ReceiptDto) response.getBody();
        assertEquals("New Receipt", body.getDescription());
        assertNotNull(body.getId());
        verify(receiptService, times(1)).upsertReceipt(any(ReceiptDto.class));
    }
}
