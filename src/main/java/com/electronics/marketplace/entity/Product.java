package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.ProductStatus;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Catalog thiet bi MOI do Admin quan ly (SDD 13.5).
 * Nguon du lieu cho recommendation (SDD 8), Product Detail, va kenh NEW_DEVICE
 * (Customer them vao Cart, checkout tao Order NEW_DEVICE + Payment).
 * Product KHONG tham gia vong doi reservation cua Marketplace (Listing moi la
 * doi tuong cua kenh do).
 *
 * ponytail: chi khai bao cac field ma Cart/CartItem va view dang dung. Cac FK
 * (device, deviceModel, admin) va avgRating thuoc pham vi Product module cua
 * Phuoc Anh - them kem annotation JPA khi ban do entity, tranh dung do o day.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private ProductStatus status;
    /** Display-only (FR-BUY-03). MVP khong co entity Store rieng. */
    private String storeName;

    public Product() {
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

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
