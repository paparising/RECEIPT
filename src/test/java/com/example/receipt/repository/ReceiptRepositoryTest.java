package com.example.receipt.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.ReceiptSource;

import java.util.List;
import java.util.Optional;

@DataJpaTest
public class ReceiptRepositoryTest {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Receipt testReceipt;
    private ReceiptSource testReceiptSource;

    @BeforeEach
    public void setUp() {
        testReceiptSource = new ReceiptSource();
        testReceiptSource.setRetailerName("Test Retailer");
        testReceiptSource.setDescription("Test Retailer Description");
        entityManager.persist(testReceiptSource);

        testReceipt = new Receipt();
        testReceipt.setDescription("Test Receipt");
        testReceipt.setAmount(100.0);
        testReceipt.setReceiptDate("2024-01-15 10:30:00");
        testReceipt.setYear(2024);
        testReceipt.setReceiptSource(testReceiptSource);
        entityManager.persist(testReceipt);
        entityManager.flush();
    }

    @Test
    public void testFindByYear() {
        // Arrange
        Receipt receipt2 = new Receipt();
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);
        receipt2.setReceiptSource(testReceiptSource);
        entityManager.persist(receipt2);

        Receipt receipt3 = new Receipt();
        receipt3.setDescription("Receipt 3");
        receipt3.setAmount(300.0);
        receipt3.setReceiptDate("2023-01-16 11:00:00");
        receipt3.setYear(2023);
        receipt3.setReceiptSource(testReceiptSource);
        entityManager.persist(receipt3);
        entityManager.flush();

        // Act
        List<Receipt> receipts2024 = receiptRepository.findByYear(2024);
        List<Receipt> receipts2023 = receiptRepository.findByYear(2023);

        // Assert
        assertEquals(2, receipts2024.size());
        assertEquals(1, receipts2023.size());
    }

    @Test
    public void testFindByReceiptSourceId() {
        // Arrange
        ReceiptSource source2 = new ReceiptSource();
        source2.setRetailerName("Retailer 2");
        source2.setDescription("Retailer 2 Description");
        entityManager.persist(source2);

        Receipt receipt2 = new Receipt();
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);
        receipt2.setReceiptSource(source2);
        entityManager.persist(receipt2);
        entityManager.flush();

        // Act
        List<Receipt> receiptsSource1 = receiptRepository.findByReceiptSourceId(testReceiptSource.getId());
        List<Receipt> receiptsSource2 = receiptRepository.findByReceiptSourceId(source2.getId());

        // Assert
        assertEquals(1, receiptsSource1.size());
        assertEquals(1, receiptsSource2.size());
    }

    @Test
    public void testFindByReceiptSourceIdAndYear() {
        // Arrange
        Receipt receipt2 = new Receipt();
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);
        receipt2.setReceiptSource(testReceiptSource);
        entityManager.persist(receipt2);

        Receipt receipt3 = new Receipt();
        receipt3.setDescription("Receipt 3");
        receipt3.setAmount(300.0);
        receipt3.setReceiptDate("2023-01-16 11:00:00");
        receipt3.setYear(2023);
        receipt3.setReceiptSource(testReceiptSource);
        entityManager.persist(receipt3);
        entityManager.flush();

        // Act
        List<Receipt> receipts = receiptRepository.findByReceiptSourceIdAndYear(testReceiptSource.getId(), 2024);

        // Assert
        assertEquals(2, receipts.size());
    }

    @Test
    public void testFindAllWithPagination() {
        // Arrange
        Receipt receipt2 = new Receipt();
        receipt2.setDescription("Receipt 2");
        receipt2.setAmount(200.0);
        receipt2.setReceiptDate("2024-01-16 11:00:00");
        receipt2.setYear(2024);
        receipt2.setReceiptSource(testReceiptSource);
        entityManager.persist(receipt2);

        Receipt receipt3 = new Receipt();
        receipt3.setDescription("Receipt 3");
        receipt3.setAmount(300.0);
        receipt3.setReceiptDate("2024-01-17 12:00:00");
        receipt3.setYear(2024);
        receipt3.setReceiptSource(testReceiptSource);
        entityManager.persist(receipt3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Receipt> page = receiptRepository.findAll(pageable);

        // Assert
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    public void testExistsById() {
        // Act & Assert
        assertTrue(receiptRepository.existsById(testReceipt.getId()));
        assertFalse(receiptRepository.existsById(999L));
    }

    @Test
    public void testFindByYearEmpty() {
        // Act
        List<Receipt> receipts = receiptRepository.findByYear(2025);

        // Assert
        assertEquals(0, receipts.size());
    }

    @Test
    public void testFindById() {
        // Act
        Optional<Receipt> receipt = receiptRepository.findById(testReceipt.getId());

        // Assert
        assertTrue(receipt.isPresent());
        assertEquals("Test Receipt", receipt.get().getDescription());
        assertEquals(100.0, receipt.get().getAmount());
    }

    @Test
    public void testSaveReceipt() {
        // Arrange
        Receipt newReceipt = new Receipt();
        newReceipt.setDescription("New Receipt");
        newReceipt.setAmount(150.0);
        newReceipt.setReceiptDate("2024-01-20 14:00:00");
        newReceipt.setYear(2024);
        newReceipt.setReceiptSource(testReceiptSource);

        // Act
        Receipt savedReceipt = receiptRepository.save(newReceipt);

        // Assert
        assertNotNull(savedReceipt.getId());
        assertEquals("New Receipt", savedReceipt.getDescription());
        assertTrue(receiptRepository.existsById(savedReceipt.getId()));
    }

    @Test
    public void testDeleteReceipt() {
        // Arrange
        Long receiptId = testReceipt.getId();

        // Act
        receiptRepository.delete(testReceipt);

        // Assert
        assertFalse(receiptRepository.existsById(receiptId));
    }

    @Test
    public void testUpdateReceipt() {
        // Arrange
        testReceipt.setDescription("Updated Description");
        testReceipt.setAmount(250.0);

        // Act
        Receipt updatedReceipt = receiptRepository.save(testReceipt);

        // Assert
        assertEquals("Updated Description", updatedReceipt.getDescription());
        assertEquals(250.0, updatedReceipt.getAmount());
    }
}
