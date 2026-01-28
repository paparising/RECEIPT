package com.example.receipt.controller;

import com.example.receipt.dto.ReceiptDto;
import com.example.receipt.dto.ReceiptUpsertRequest;
import com.example.receipt.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReceiptControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    private ReceiptDto validReceipt;

    @BeforeEach
    public void setUp() {
        validReceipt = new ReceiptDto();
        validReceipt.setId(1L);
        validReceipt.setDescription("Grocery items");
        validReceipt.setReceiptDate("2026-01-28T10:30:00Z");
        validReceipt.setAmount(150.50);
    }

    // ==================== GET ALL RECEIPTS TESTS ====================

    @Test
    public void testGetAllReceiptsWithValidPaginationParams() throws Exception {
        when(receiptService.getAllReceipts(any())).thenReturn(org.springframework.data.domain.Page.empty());
        
        mockMvc.perform(get("/api/receipts?page=0&size=100"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllReceiptsWithNegativePage() throws Exception {
        mockMvc.perform(get("/api/receipts?page=-1&size=100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Page must be >= 0")));
    }

    @Test
    public void testGetAllReceiptsWithZeroSize() throws Exception {
        mockMvc.perform(get("/api/receipts?page=0&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Size must be >= 1")));
    }

    @Test
    public void testGetAllReceiptsWithNegativeSize() throws Exception {
        mockMvc.perform(get("/api/receipts?page=0&size=-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Size must be >= 1")));
    }

    @Test
    public void testGetAllReceiptsDefaultPaginationParams() throws Exception {
        when(receiptService.getAllReceipts(any())).thenReturn(org.springframework.data.domain.Page.empty());
        
        mockMvc.perform(get("/api/receipts"))
                .andExpect(status().isOk());
    }

    // ==================== GET RECEIPT BY ID TESTS ====================

    @Test
    public void testGetReceiptByIdWithValidId() throws Exception {
        when(receiptService.getReceiptById(1L)).thenReturn(Optional.of(validReceipt));
        
        mockMvc.perform(get("/api/receipts/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetReceiptByIdWithZeroId() throws Exception {
        mockMvc.perform(get("/api/receipts/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Receipt ID must be > 0")));
    }

    @Test
    public void testGetReceiptByIdWithNegativeId() throws Exception {
        mockMvc.perform(get("/api/receipts/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Receipt ID must be > 0")));
    }

    @Test
    public void testGetReceiptByIdNotFound() throws Exception {
        when(receiptService.getReceiptById(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/receipts/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== SEARCH BY YEAR TESTS ====================

    @Test
    public void testGetReceiptsByYearWithValidYear() throws Exception {
        when(receiptService.getReceiptsByYear(2026)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/year/2026"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetReceiptsByYearWithYearBefore1900() throws Exception {
        mockMvc.perform(get("/api/receipts/search/year/1899"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Year must be >= 1900")));
    }

    @Test
    public void testGetReceiptsByYearWithVeryOldYear() throws Exception {
        mockMvc.perform(get("/api/receipts/search/year/1500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Year must be >= 1900")));
    }

    @Test
    public void testGetReceiptsByYearWithRecentYear() throws Exception {
        when(receiptService.getReceiptsByYear(2024)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/year/2024"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetReceiptsByYearWithFutureYear() throws Exception {
        when(receiptService.getReceiptsByYear(2050)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/year/2050"))
                .andExpect(status().isOk());
    }

    // ==================== SEARCH BY SOURCE TESTS ====================

    @Test
    public void testGetReceiptsBySourceWithValidSourceId() throws Exception {
        when(receiptService.getReceiptsBySource(1)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/source/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetReceiptsBySourceWithZeroSourceId() throws Exception {
        mockMvc.perform(get("/api/receipts/search/source/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Source ID must be > 0")));
    }

    @Test
    public void testGetReceiptsBySourceWithNegativeSourceId() throws Exception {
        mockMvc.perform(get("/api/receipts/search/source/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Source ID must be > 0")));
    }

    @Test
    public void testGetReceiptsBySourceWithLargeSourceId() throws Exception {
        when(receiptService.getReceiptsBySource(999)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/source/999"))
                .andExpect(status().isOk());
    }

    // ==================== SEARCH BY SOURCE AND YEAR TESTS ====================

    @Test
    public void testGetReceiptsBySourceAndYearWithValidParams() throws Exception {
        when(receiptService.getReceiptsBySourceAndYear(1, 2026)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/source/1/year/2026"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetReceiptsBySourceAndYearWithZeroSourceId() throws Exception {
        mockMvc.perform(get("/api/receipts/search/source/0/year/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Source ID must be > 0")));
    }

    @Test
    public void testGetReceiptsBySourceAndYearWithNegativeSourceId() throws Exception {
        mockMvc.perform(get("/api/receipts/search/source/-1/year/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Source ID must be > 0")));
    }

    @Test
    public void testGetReceiptsBySourceAndYearWithYearBefore1900() throws Exception {
        mockMvc.perform(get("/api/receipts/search/source/1/year/1899"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Year must be >= 1900")));
    }

    @Test
    public void testGetReceiptsBySourceAndYearWithBothInvalid() throws Exception {
        mockMvc.perform(get("/api/receipts/search/source/-1/year/1899"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetReceiptsBySourceAndYearWithValidSourceAndYear() throws Exception {
        when(receiptService.getReceiptsBySourceAndYear(5, 2024)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/source/5/year/2024"))
                .andExpect(status().isOk());
    }

    // ==================== DELETE RECEIPT TESTS ====================

    @Test
    public void testDeleteReceiptWithValidId() throws Exception {
        when(receiptService.getReceiptById(1L)).thenReturn(Optional.of(validReceipt));
        doNothing().when(receiptService).deleteReceipt(1L);
        
        mockMvc.perform(delete("/api/receipts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteReceiptWithZeroId() throws Exception {
        mockMvc.perform(delete("/api/receipts/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Receipt ID must be > 0")));
    }

    @Test
    public void testDeleteReceiptWithNegativeId() throws Exception {
        mockMvc.perform(delete("/api/receipts/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..message").value(containsString("Receipt ID must be > 0")));
    }

    @Test
    public void testDeleteReceiptNotFound() throws Exception {
        when(receiptService.getReceiptById(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(delete("/api/receipts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteReceiptWithLargeValidId() throws Exception {
        ReceiptDto largeIdReceipt = new ReceiptDto();
        largeIdReceipt.setId(999999L);
        
        when(receiptService.getReceiptById(999999L)).thenReturn(Optional.of(largeIdReceipt));
        doNothing().when(receiptService).deleteReceipt(999999L);
        
        mockMvc.perform(delete("/api/receipts/999999"))
                .andExpect(status().isNoContent());
    }

    // ==================== BOUNDARY TESTS ====================

    @Test
    public void testPaginationBoundaryPage() throws Exception {
        when(receiptService.getAllReceipts(any())).thenReturn(org.springframework.data.domain.Page.empty());
        
        mockMvc.perform(get("/api/receipts?page=0&size=1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testYearBoundaryExactly1900() throws Exception {
        when(receiptService.getReceiptsByYear(1900)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/year/1900"))
                .andExpect(status().isOk());
    }

    @Test
    public void testIdBoundaryExactly1() throws Exception {
        when(receiptService.getReceiptById(1L)).thenReturn(Optional.of(validReceipt));
        
        mockMvc.perform(get("/api/receipts/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSourceIdBoundaryExactly1() throws Exception {
        when(receiptService.getReceiptsBySource(1)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/source/1"))
                .andExpect(status().isOk());
    }

    // ==================== COMBINED VALIDATION TESTS ====================

    @Test
    public void testMultiplePaginationErrors() throws Exception {
        mockMvc.perform(get("/api/receipts?page=-1&size=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSourceAndYearBothAtBoundary() throws Exception {
        when(receiptService.getReceiptsBySourceAndYear(1, 1900)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/receipts/search/source/1/year/1900"))
                .andExpect(status().isOk());
    }
}
