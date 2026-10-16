package com.electronics.marketplace.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Gio hang cua Customer cho kenh NEW_DEVICE (SDD 13.8).
 * Moi Customer co dung mot Cart (customerId unique).
 * Cart khong reserve Product va khong thay doi Product.status (BR-45).
 */
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private User customer;
    private Date createdAt;
    private Date updatedAt;
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    /**
     * SUM(CartItem.product.price x quantity). Tinh toan thay vi luu truong denormalized,
     * cung kieu voi Order.getTotalAmount(). EL khong duyet tinh duoc tong nen JSP can ham nay.
     */
    public BigDecimal getSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            BigDecimal lineTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
        }
        return total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
