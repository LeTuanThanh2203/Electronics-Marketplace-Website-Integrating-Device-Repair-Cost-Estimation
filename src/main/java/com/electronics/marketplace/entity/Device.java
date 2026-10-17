package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Thực thể Device (Bảng devices) - Danh mục thiết bị laptop.
 * Field-based JPA mapping theo Rule 2.
 * Dùng Long id theo thỏa thuận chung.
 */
@Entity
@Table(name = "devices")
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;
    private String model;

    @Column(name = "release_year")
    private Integer releaseYear;

    private String cpu;
    private String ram;
    private String storage;

    public Device() {
    }

    public Device(Long id, String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.releaseYear = releaseYear;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
    }

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
}
