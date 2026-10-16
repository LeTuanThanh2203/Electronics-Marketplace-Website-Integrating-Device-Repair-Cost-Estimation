package com.electronics.marketplace.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * LineItem cho ca hai kenh (SDD 13.7).
 * Tham chieu dung mot trong hai: Listing (MARKETPLACE) hoac Product (NEW_DEVICE),
 * loai tru lan nhau. quantity luon = 1.
 * price la snapshot tai thoi diem tao Order, bat bien sau do (BR-41).
 */
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Order order;
    private Listing listing;
    private Product product;
    private BigDecimal price;
    private int quantity = 1;

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
