package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an electronic device model line (e.g. Device / Laptop).
 * Defined per Lucidchart Class Diagram:
 * - id: int/Long
 * - name: string
 * - brand: string
 * - model: string
 * - releaseYear: int
 * - cpu: string
 * - ram: string / int
 * - storage: string / int
 * - products: 1 Device to N Products
 */
@Entity
@Table(name = "devices")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "cpu", length = 100)
    private String cpu;

    @Column(name = "ram", length = 50)
    private String ram;

    @Column(name = "storage", length = 50)
    private String storage;

    @Column(name = "device_type", length = 50)
    private String deviceType = "LAPTOP";

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Device() {
    }

    public Device(String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.releaseYear = releaseYear;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.deviceType = "LAPTOP";
    }

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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
