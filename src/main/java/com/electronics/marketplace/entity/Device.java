package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Danh mục dòng thiết bị, dùng chung cho cả 3 module:
 * Repair (Quốc), Listing (Thành) và Product (Anh).
 *
 * <p>Các trường hợp nhất từ 3 nhánh:
 * <ul>
 *   <li>Quốc: {@code brand}, {@code series}, {@code model} + unique constraint</li>
 *   <li>Thành: {@code name}</li>
 *   <li>Anh: {@code name}, {@code deviceType}, quan hệ {@code products}</li>
 * </ul>
 * {@code name} và {@code series} đều để nullable/giá trị mặc định để mỗi module
 * dùng đúng trường của mình mà không phá dữ liệu của module khác.</p>
 */
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

    // Dùng bởi module Product/Listing (Anh, Thành). Nullable để module Repair
    // vẫn tạo được Device chỉ với brand + series + model.
    @Column(name = "name", length = 150)
    private String name;

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

    // Module Product (Anh) dùng để phân loại LAPTOP / SMARTPHONE...
    @Column(name = "device_type", length = 50)
    private String deviceType = "LAPTOP";

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairPricing> pricings = new ArrayList<>();

    // Không khai báo quan hệ ngược tới Product ở đây: Product (nhánh Anh) đã có
    // @ManyToOne device, và class Product trên nhánh này còn rỗng nên Hibernate
    // sẽ không resolve được mappedBy. Thêm lại khi merge module Product.

    public Device() {
    }

    /** Constructor của module Product (Anh): name + brand + model. */
    public Device(String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.releaseYear = releaseYear;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
    }

    /**
     * Constructor của module Repair (Quốc): brand + series + model.
     *
     * <p>Không dùng constructor 7 tham số vì chữ ký kiểu
     * {@code (String, String, String, Integer, String, String, String)} trùng hoàn
     * toàn với constructor {@code (name, brand, model, ...)} của module Product —
     * gọi nhầm sẽ compile được nhưng gán sai trường. Factory method có tên riêng
     * nên không thể nhầm.</p>
     */
    public static Device forRepair(String brand, String series, String model,
                                   Integer releaseYear, String cpu, String ram, String storage) {
        Device device = new Device();
        device.brand = brand;
        device.series = series != null ? series : "";
        device.model = model;
        device.releaseYear = releaseYear;
        device.cpu = cpu;
        device.ram = ram;
        device.storage = storage;
        return device;
    }

    /**
     * Constructor của module Listing (Thành) — có {@code name} và {@code id}.
     * Giữ lại để code bên Thành compile được khi merge.
     */
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<RepairPricing> getPricings() {
        return pricings;
    }

    public void setPricings(List<RepairPricing> pricings) {
        this.pricings = pricings;
    }

    /**
     * Tên hiển thị thống nhất cho cả 3 module: ưu tiên {@code name} (Anh/Thành),
     * nếu không có thì ghép {@code brand + series + model} (Quốc).
     */
    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
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
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", series='" + series + '\'' +
                ", model='" + model + '\'' +
                ", releaseYear=" + releaseYear +
                '}';
    }
}
