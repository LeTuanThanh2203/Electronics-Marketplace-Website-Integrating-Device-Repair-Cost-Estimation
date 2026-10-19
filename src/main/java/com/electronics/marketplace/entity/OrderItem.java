package com.electronics.marketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * LineItem cho ca hai kenh (SDD 13.7).
 * Tham chieu dung mot trong hai: Listing (MARKETPLACE) hoac Product (NEW_DEVICE),
 * loai tru lan nhau. quantity luon = 1.
 * price la snapshot tai thoi diem tao Order, bat bien sau do (BR-41).
 */
@Entity
@Table(name = "order_items")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Order MARKETPLACE tro toi Listing da mua. Cot van la "listing_id" nen du
     * lieu cu khong doi; khac biet la gio doc duoc listing.device.name cho
     * history.jsp (truoc day chi co ID tho).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    /** nullable: Order NEW_DEVICE dung product, MARKETPLACE dung listing (XOR). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /** updatable = false: BR-41 - snapshot gia, bat bien sau khi tao. */
    @Column(nullable = false, precision = 14, scale = 2, updatable = false)
    private BigDecimal price;

    @Column(nullable = false)
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