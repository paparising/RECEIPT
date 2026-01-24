package com.example.receipt.entity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "properties")
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true, nullable = true)
    private String Alias;
    @Column(nullable = false)
    private String streetNumber;
    @Column(nullable = false)
    private String streetName;
    @Column(nullable = true)
    private String Unit;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String zipCode;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyReceipt> propertyReceipts;

    // Getters and Setters
    public Long getId() {
        return id;
    }   
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAlias() {
        return Alias;
    }
    public void setAlias(String alias) {
        Alias = alias;
    }
    public String getStreetNumber() {
        return streetNumber;
    }
    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }
    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName; 
    }
    public String getUnit() {
        return Unit;
    }
    public void setUnit(String unit) {
        Unit = unit;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public List<PropertyReceipt> getPropertyReceipts() {
        return propertyReceipts;
    }

    public void setPropertyReceipts(List<PropertyReceipt> propertyReceipts) {
        this.propertyReceipts = propertyReceipts;
    }

    @Override
    public String toString() {
        return "Property [id=" + id + ", name=" + name + ", Alias=" + Alias + ", streetNumber=" + streetNumber
                + ", streetName=" + streetName + ", Unit=" + Unit + ", city=" + city + ", state=" + state
                + ", zipCode=" + zipCode + "]";
    }
}

