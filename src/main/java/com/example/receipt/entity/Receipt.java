package com.example.receipt.entity;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

@Entity
@Table(name = "receipts")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String description;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String receiptDate;

    @Column(nullable = false, name = "receipt_year")
    private Integer year;

    @NotNull(message = "Receipt source is required")
    @ManyToOne
    @JoinColumn(name = "receipt_source_id")
    private ReceiptSource receiptSource;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyReceipt> propertyReceipts;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ReceiptSource getReceiptSource() {
        return receiptSource;
    }

    public void setReceiptSource(ReceiptSource receiptSource) {
        this.receiptSource = receiptSource;
    }

    public List<PropertyReceipt> getPropertyReceipts() {
        return propertyReceipts;
    }

    public void setPropertyReceipts(List<PropertyReceipt> propertyReceipts) {
        this.propertyReceipts = propertyReceipts;
    }
}
