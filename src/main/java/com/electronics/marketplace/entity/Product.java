package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a new Product catalog item (B2C).
 * Defined per Lucidchart Class Diagram:
 * - adminId: int (or Long)
 * - productId / id: int (or Long)
 * - name: string
 * - brand: string
 * - price: float / BigDecimal
 * - status: ProductStatus (AVAILABLE, OUT_OF_STOCK, DISCONTINUED)
 * - specifications: 0..N ProductSpecification
 * - device: Device
 * Plus UI supporting fields for the official storefront design.
 */
@Entity
@Table(name = "products")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // maps to productId in UML

    @Column(name = "admin_id")
    private Long adminId; // maps to adminId in UML

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "price", nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductStatus status = ProductStatus.AVAILABLE;

    // UI & Storefront display fields from template
    @Column(name = "sku", length = 50)
    private String sku;

    @Column(name = "series", length = 100)
    private String series;

    @Column(name = "original_price", precision = 14, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "discount_tag", length = 100)
    private String discountTag;

    @Column(name = "badge", length = 50)
    private String badge = "SEAL 100%";

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "warranty", length = 150)
    private String warranty = "Bảo hành 24 tháng chính hãng";

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "target_need", length = 50)
    private String targetNeed; // OFFICE, GAMING, GRAPHICS (used by Recommendation Engine)

    @Column(name = "stock_quantity", nullable = false, columnDefinition = "integer default 10")
    private Integer stockQuantity = 10;

    @Column(name = "store_name", length = 150)
    private String storeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductSpecification> specifications = new ArrayList<>();

    // ponytail: default de hbm2ddl.auto=update them duoc cot NOT NULL vao bang
    // products dang co du lieu. Hibernate sinh "ADD COLUMN ... NOT NULL" khong co
    // DEFAULT se bi PostgreSQL tu choi.
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp default now()")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product() {
    }

    public Product(Long adminId, String name, String brand, BigDecimal price, ProductStatus status) {
        this.adminId = adminId;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.status = status != null ? status : ProductStatus.AVAILABLE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Specification helper methods
    public void addSpecification(ProductSpecification spec) {
        specifications.add(spec);
        spec.setProduct(this);
    }

    public void removeSpecification(ProductSpecification spec) {
        specifications.remove(spec);
        spec.setProduct(null);
    }

    public ProductSpecification getSpecification(String key) {
        if (specifications == null || key == null) return null;
        for (ProductSpecification spec : specifications) {
            if (spec.getSpecKey() != null && spec.getSpecKey().equalsIgnoreCase(key)) {
                return spec;
            }
        }
        return null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getDiscountTag() {
        return discountTag;
    }

    public void setDiscountTag(String discountTag) {
        this.discountTag = discountTag;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetNeed() {
        return targetNeed;
    }

    public void setTargetNeed(String targetNeed) {
        this.targetNeed = targetNeed;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @JsonIgnore
    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public List<ProductSpecification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<ProductSpecification> specifications) {
        this.specifications = specifications;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Convenience getters for EL expressions in JSP.
    // ponytail: tra null khi thieu spec - JSP dung <c:if> de an dong, khong bia
    // so lieu (truoc day mac dinh 16GB/512GB/14.0).
    public String getCpu() {
        if (device != null && device.getCpu() != null && !device.getCpu().isEmpty()) {
            return device.getCpu();
        }
        ProductSpecification spec = getSpecification("CPU");
        return spec != null ? spec.getTextValue() : null;
    }

    public Integer getRamGb() {
        ProductSpecification spec = getSpecification("RAM");
        if (spec != null && spec.getNumericValue() != null) {
            return spec.getNumericValue();
        }
        if (device != null && device.getRam() != null) {
            try {
                return Integer.parseInt(device.getRam().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Integer getStorageGb() {
        ProductSpecification spec = getSpecification("STORAGE");
        if (spec != null && spec.getNumericValue() != null) {
            return spec.getNumericValue();
        }
        if (device != null && device.getStorage() != null) {
            try {
                return Integer.parseInt(device.getStorage().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Double getScreenSize() {
        ProductSpecification spec = getSpecification("SCREEN_SIZE");
        return spec != null && spec.getNumericValue() != null
                ? spec.getNumericValue().doubleValue() : null;
    }

    public String getScreenResolution() {
        ProductSpecification spec = getSpecification("SCREEN_DETAIL");
        return spec != null ? spec.getTextValue() : null;
    }

    public String getGpu() {
        ProductSpecification spec = getSpecification("GPU");
        return spec != null ? spec.getTextValue() : null;
    }
}
