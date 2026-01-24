package com.example.receipt.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import com.example.receipt.entity.Property;
import com.example.receipt.entity.Receipt;
import com.example.receipt.entity.PropertyReceipt;
import java.util.Optional;

@DataJpaTest
public class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindPropertyWithAllReceipts() {
        // Arrange
        Property property = new Property();
        property.setName("Test Property");
        property.setStreetNumber("123");
        property.setStreetName("Main St");
        property.setCity("Boston");
        property.setState("MA");
        property.setZipCode("02101");
        entityManager.persistAndFlush(property);

        Receipt receipt = new Receipt();
        receipt.setDescription("Test Receipt");
        receipt.setAmount(100.0);
        receipt.setReceiptDate("2024-01-15 10:30:00");
        receipt.setYear(2024);
        entityManager.persistAndFlush(receipt);

        PropertyReceipt propertyReceipt = new PropertyReceipt();
        propertyReceipt.setProperty(property);
        propertyReceipt.setReceipt(receipt);
        propertyReceipt.setPortion(100.0);
        entityManager.persistAndFlush(propertyReceipt);

        entityManager.clear();

        // Act
        Optional<Property> result = propertyRepository.findPropertyWithAllReceipts(property.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Property", result.get().getName());
        assertFalse(result.get().getPropertyReceipts().isEmpty());
        assertEquals(1, result.get().getPropertyReceipts().size());
    }

    @Test
    public void testFindPropertyWithReceiptsByYear() {
        // Arrange
        Property property = new Property();
        property.setName("Test Property");
        property.setStreetNumber("456");
        property.setStreetName("Oak Ave");
        property.setCity("Cambridge");
        property.setState("MA");
        property.setZipCode("02142");
        entityManager.persistAndFlush(property);

        Receipt receipt2024 = new Receipt();
        receipt2024.setDescription("2024 Receipt");
        receipt2024.setAmount(150.0);
        receipt2024.setReceiptDate("2024-06-20 14:00:00");
        receipt2024.setYear(2024);
        entityManager.persistAndFlush(receipt2024);

        Receipt receipt2023 = new Receipt();
        receipt2023.setDescription("2023 Receipt");
        receipt2023.setAmount(200.0);
        receipt2023.setReceiptDate("2023-05-10 09:15:00");
        receipt2023.setYear(2023);
        entityManager.persistAndFlush(receipt2023);

        PropertyReceipt pr1 = new PropertyReceipt();
        pr1.setProperty(property);
        pr1.setReceipt(receipt2024);
        pr1.setPortion(150.0);
        entityManager.persistAndFlush(pr1);

        PropertyReceipt pr2 = new PropertyReceipt();
        pr2.setProperty(property);
        pr2.setReceipt(receipt2023);
        pr2.setPortion(200.0);
        entityManager.persistAndFlush(pr2);

        entityManager.clear();

        // Act
        Optional<Property> result = propertyRepository.findPropertyWithReceipts(property.getId(), 2024);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Property", result.get().getName());
        assertEquals(1, result.get().getPropertyReceipts().size());
        assertEquals(2024, result.get().getPropertyReceipts().getFirst().getReceipt().getYear());
    }

    @Test
    public void testFindPropertyNotFound() {
        // Act
        Optional<Property> result = propertyRepository.findPropertyWithAllReceipts(999L);

        // Assert
        assertFalse(result.isPresent());
    }
}
