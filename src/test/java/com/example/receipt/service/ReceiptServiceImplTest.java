package com.example.receipt.service;

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

import com.example.receipt.dto.ReceiptDto;
import com.example.receipt.dto.PropertyAllocationDto;
import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.ReceiptSource;
import com.example.receipt.entity.PropertyReceipt;
import com.example.receipt.entity.Property;
import com.example.receipt.repository.ReceiptRepository;
import com.example.receipt.repository.ReceiptSourceRepository;
import com.example.receipt.service.impl.ReceiptServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReceiptServiceImplTest {

    @Mock
    private ReceiptRepository receiptRepository;
    
    @Mock
    private ReceiptSourceRepository receiptSourceRepository;

    @InjectMocks
    private ReceiptServiceImpl receiptService;

    private Receipt testReceipt;
    private ReceiptDto testReceiptDto;
    private ReceiptSource testReceiptSource;

    @BeforeEach
    public void setUp() {
        testReceiptSource = new ReceiptSource();
        testReceiptSource.setId(1);
        testReceiptSource.setRetailerName("Test Retailer");
        testReceiptSource.setDescription("Test Retailer Description");

        testReceipt = new Receipt();
        testReceipt.setId(1L);
        testReceipt.setDescription("Test Receipt");
        testReceipt.setAmount(100.0);
        testReceipt.setReceiptDate("2024-01-15 10:30:00");
        testReceipt.setYear(2024);
        testReceipt.setReceiptSource(testReceiptSource);

        testReceiptDto = new ReceiptDto();
        testReceiptDto.setId(1L);
        testReceiptDto.setDescription("Test Receipt");
        testReceiptDto.setAmount(100.0);
        testReceiptDto.setReceiptDate("2024-01-15 10:30:00");
        testReceiptDto.setYear(2024);
        testReceiptDto.setReceiptSourceId(1);
    }

    @Test
    public void testUpsertReceiptCreate() {
        // Arrange
        ReceiptDto newReceiptDto = new ReceiptDto();
        newReceiptDto.setDescription("New Receipt");
        newReceiptDto.setAmount(50.0);
        newReceiptDto.setReceiptDate("2024-01-20 14:00:00");
        newReceiptDto.setYear(2024);
        newReceiptDto.setReceiptSourceId(1);

        Receipt newReceipt = new Receipt();
        newReceipt.setId(2L);
        newReceipt.setDescription("New Receipt");
        newReceipt.setAmount(50.0);
        newReceipt.setReceiptDate("2024-01-20 14:00:00");
        newReceipt.setYear(2024);
        newReceipt.setReceiptSource(testReceiptSource);

        when(receiptSourceRepository.findById(1)).thenReturn(Optional.of(testReceiptSource));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(newReceipt);

        // Act
        ReceiptDto result = receiptService.upsertReceipt(newReceiptDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Receipt", result.getDescription());
        assertEquals(50.0, result.getAmount());
        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }

    @Test
    public void testUpsertReceiptUpdate() {
        // Arrange
        testReceiptDto.setDescription("Updated Receipt");
        testReceiptDto.setAmount(150.0);

        when(receiptSourceRepository.findById(1)).thenReturn(Optional.of(testReceiptSource));
        when(receiptRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);

        // Act
        ReceiptDto result = receiptService.upsertReceipt(testReceiptDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Receipt", result.getDescription());
        assertEquals(150.0, result.getAmount());
        verify(receiptRepository, times(1)).findById(1L);
        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }

    @Test
    public void testGetReceiptById() {
        // Arrange
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(testReceipt));

        // Act
        Optional<ReceiptDto> result = receiptService.getReceiptById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Receipt", result.get().getDescription());
        assertEquals(100.0, result.get().getAmount());
        assertNull(result.get().getPropertyAllocations());
        verify(receiptRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetReceiptByIdWithRelatedProperties() {
        // Arrange
        Property property1 = new Property();
        property1.setId(10L);
        property1.setName("Property 1");
        property1.setStreetNumber("123");
        property1.setStreetName("Main St");
        property1.setCity("New York");
        property1.setState("NY");
        property1.setZipCode("10001");

        Property property2 = new Property();
        property2.setId(11L);
        property2.setName("Property 2");
        property2.setStreetNumber("456");
        property2.setStreetName("Oak Ave");
        property2.setCity("Boston");
        property2.setState("MA");
        property2.setZipCode("02101");

        PropertyReceipt pr1 = new PropertyReceipt();
        pr1.setId(1L);
        pr1.setProperty(property1);
        pr1.setReceipt(testReceipt);
        pr1.setPortion(50.0);

        PropertyReceipt pr2 = new PropertyReceipt();
        pr2.setId(2L);
        pr2.setProperty(property2);
        pr2.setReceipt(testReceipt);
        pr2.setPortion(50.0);

        List<PropertyReceipt> propertyReceipts = new ArrayList<>();
        propertyReceipts.add(pr1);
        propertyReceipts.add(pr2);

        testReceipt.setPropertyReceipts(propertyReceipts);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(testReceipt));

        // Act
        Optional<ReceiptDto> result = receiptService.getReceiptById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Receipt", result.get().getDescription());
        assertEquals(100.0, result.get().getAmount());
        assertNotNull(result.get().getPropertyAllocations());
        assertEquals(2, result.get().getPropertyAllocations().size());

        PropertyAllocationDto relatedProp1 = result.get().getPropertyAllocations().get(0);
        assertEquals("Property 1", relatedProp1.getPropertyName());
        assertEquals(50, relatedProp1.getPropertyPercentage());
        
        PropertyAllocationDto relatedProp2 = result.get().getPropertyAllocations().get(1);
        assertEquals("Property 2", relatedProp2.getPropertyName());
        assertEquals(50, relatedProp2.getPropertyPercentage());
        
        verify(receiptRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetReceiptByIdNotFound() {
        // Arrange
        when(receiptRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ReceiptDto> result = receiptService.getReceiptById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(receiptRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetAllReceipts() {
        // Arrange
        Receipt receipt2 = new Receipt();
        receipt2.setId(2L);
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);

        List<Receipt> receiptList = new ArrayList<>();
        receiptList.add(testReceipt);
        receiptList.add(receipt2);

        Page<Receipt> page = new PageImpl<>(receiptList);
        Pageable pageable = PageRequest.of(0, 100);

        when(receiptRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<ReceiptDto> result = receiptService.getAllReceipts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(receiptRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testDeleteReceipt() {
        // Arrange
        when(receiptRepository.existsById(1L)).thenReturn(true);

        // Act
        receiptService.deleteReceipt(1L);

        // Assert
        verify(receiptRepository, times(1)).existsById(1L);
        verify(receiptRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteReceiptNotFound() {
        // Arrange
        when(receiptRepository.existsById(999L)).thenReturn(false);

        // Act
        receiptService.deleteReceipt(999L);

        // Assert
        verify(receiptRepository, times(1)).existsById(999L);
        verify(receiptRepository, never()).deleteById(999L);
    }

    @Test
    public void testGetReceiptsByYear() {
        // Arrange
        Receipt receipt2 = new Receipt();
        receipt2.setId(2L);
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);

        List<Receipt> receiptList = new ArrayList<>();
        receiptList.add(testReceipt);
        receiptList.add(receipt2);

        when(receiptRepository.findByYear(2024)).thenReturn(receiptList);

        // Act
        List<ReceiptDto> result = receiptService.getReceiptsByYear(2024);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Receipt", result.get(0).getDescription());
        assertEquals("Receipt 2", result.get(1).getDescription());
        verify(receiptRepository, times(1)).findByYear(2024);
    }

    @Test
    public void testGetReceiptsBySource() {
        // Arrange
        Receipt receipt2 = new Receipt();
        receipt2.setId(2L);
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);
        receipt2.setReceiptSource(testReceiptSource);

        List<Receipt> receiptList = new ArrayList<>();
        receiptList.add(testReceipt);
        receiptList.add(receipt2);

        when(receiptRepository.findByReceiptSourceId(1)).thenReturn(receiptList);

        // Act
        List<ReceiptDto> result = receiptService.getReceiptsBySource(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getReceiptSourceId());
        verify(receiptRepository, times(1)).findByReceiptSourceId(1);
    }

    @Test
    public void testGetReceiptsBySourceAndYear() {
        // Arrange
        List<Receipt> receiptList = new ArrayList<>();
        receiptList.add(testReceipt);

        when(receiptRepository.findByReceiptSourceIdAndYear(1, 2024)).thenReturn(receiptList);

        // Act
        List<ReceiptDto> result = receiptService.getReceiptsBySourceAndYear(1, 2024);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getReceiptSourceId());
        assertEquals(2024, result.get(0).getYear());
        verify(receiptRepository, times(1)).findByReceiptSourceIdAndYear(1, 2024);
    }

    @Test
    public void testGetReceiptsByYearEmpty() {
        // Arrange
        when(receiptRepository.findByYear(2025)).thenReturn(new ArrayList<>());

        // Act
        List<ReceiptDto> result = receiptService.getReceiptsByYear(2025);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(receiptRepository, times(1)).findByYear(2025);
    }
}
