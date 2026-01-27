package com.example.receipt.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.receipt.entity.ReceiptSource;
import java.util.Optional;

@DataJpaTest
public class ReceiptSourceRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ReceiptSourceRepository receiptSourceRepository;
    
    @BeforeEach
    public void setUp() {
        ReceiptSource source1 = new ReceiptSource();
        source1.setRetailerName("WALMART");
        source1.setDescription("Walmart stores");
        entityManager.persistAndFlush(source1);
        
        ReceiptSource source2 = new ReceiptSource();
        source2.setRetailerName("TARGET");
        source2.setDescription("Target stores");
        entityManager.persistAndFlush(source2);
    }
    
    @Test
    public void testFindByRetailerName_Found() {
        Optional<ReceiptSource> result = receiptSourceRepository.findByRetailerName("WALMART");
        
        assertTrue(result.isPresent());
        assertEquals("WALMART", result.get().getRetailerName());
        assertEquals("Walmart stores", result.get().getDescription());
    }
    
    @Test
    public void testFindByRetailerName_NotFound() {
        Optional<ReceiptSource> result = receiptSourceRepository.findByRetailerName("COSTCO");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    public void testFindById() {
        Optional<ReceiptSource> result = receiptSourceRepository.findByRetailerName("TARGET");
        assertTrue(result.isPresent());
        
        Long id = result.get().getId();
        Optional<ReceiptSource> foundById = receiptSourceRepository.findById(id);
        
        assertTrue(foundById.isPresent());
        assertEquals("TARGET", foundById.get().getRetailerName());
    }
    
    @Test
    public void testSaveNewReceiptSource() {
        ReceiptSource newSource = new ReceiptSource();
        newSource.setRetailerName("AMAZON");
        newSource.setDescription("Amazon stores");
        
        ReceiptSource saved = receiptSourceRepository.save(newSource);
        entityManager.flush();
        
        assertNotNull(saved.getId());
        
        Optional<ReceiptSource> found = receiptSourceRepository.findByRetailerName("AMAZON");
        assertTrue(found.isPresent());
        assertEquals("AMAZON", found.get().getRetailerName());
    }
    
    @Test
    public void testReceiptSourceRetailerNameIsUnique() {
        // Verify that two different retailers are stored separately
        Optional<ReceiptSource> walmart = receiptSourceRepository.findByRetailerName("WALMART");
        Optional<ReceiptSource> target = receiptSourceRepository.findByRetailerName("TARGET");
        
        assertTrue(walmart.isPresent());
        assertTrue(target.isPresent());
        assertNotEquals(walmart.get().getId(), target.get().getId());
    }
}
