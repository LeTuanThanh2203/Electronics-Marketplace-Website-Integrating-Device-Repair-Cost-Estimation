package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devices", uniqueConstraints = {
    // Chặn admin thêm trùng một dòng máy. Cột series được chuẩn hóa về "" (không NULL)
    // trong RepairService để unique constraint thực sự có tác dụng trên PostgreSQL.
    @UniqueConstraint(name = "uq_devices_brand_series_model",
                      columnNames = {"brand", "series", "model"})
})
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    // Không để NULL: PostgreSQL coi mỗi NULL là khác nhau nên unique constraint
    // (brand, series, model) sẽ không chặn được dòng máy trùng khi series bị NULL.
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

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairPricing> pricings = new ArrayList<>();

    public Device() {
    }

    public Device(String brand, String series, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.brand = brand;
        this.series = series != null ? series : "";
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series != null ? series : "";
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

    public List<RepairPricing> getPricings() {
        return pricings;
    }

    public void setPricings(List<RepairPricing> pricings) {
        this.pricings = pricings;
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (brand != null && !brand.isEmpty()) sb.append(brand).append(" ");
        if (series != null && !series.isEmpty()) sb.append(series).append(" ");
        if (model != null && !model.isEmpty()) sb.append(model);
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", series='" + series + '\'' +
                ", model='" + model + '\'' +
                ", releaseYear=" + releaseYear +
                '}';
    }
}
