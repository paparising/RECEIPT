package com.example.receipt.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "receipt_sources")
public class ReceiptSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String retailerName;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "receiptSource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Receipt> receipts;

    // Constructors
    public ReceiptSource() {
    }

    public ReceiptSource(String retailerName, String description) {
        this.retailerName = retailerName;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }
}
