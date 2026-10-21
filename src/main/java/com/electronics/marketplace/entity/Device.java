package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devices", uniqueConstraints = {
    @UniqueConstraint(name = "uq_devices_brand_series_model",
                      columnNames = {"brand", "series", "model"})
})
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "series", nullable = false, length = 100)
    private String series = "";

    @Column(name = "model", nullable = false, length = 150)
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

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairPricing> pricings = new ArrayList<>();

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Device() {}

    public Device(Long id, String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.series = "";
        this.releaseYear = releaseYear;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
    }

    

    public Device(String brand, String series, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.brand = brand;
        this.series = series != null ? series : "";
        this.model = model;
        this.name = brand + " " + (series != null ? series + " " : "") + model;
        this.releaseYear = releaseYear;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return getDisplayName();
    }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series != null ? series : ""; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public List<RepairPricing> getPricings() { return pricings; }
    public void setPricings(List<RepairPricing> pricings) { this.pricings = pricings; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (brand != null && !brand.isEmpty()) sb.append(brand).append(" ");
        if (series != null && !series.isEmpty()) sb.append(series).append(" ");
        if (model != null && !model.isEmpty()) sb.append(model);
        return sb.toString().trim();
    }
}
