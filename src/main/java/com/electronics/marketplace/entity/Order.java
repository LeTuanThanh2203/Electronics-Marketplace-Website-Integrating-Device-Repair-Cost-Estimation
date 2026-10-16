package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.OrderStatus;
import com.electronics.marketplace.entity.enums.OrderType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Don hang (SDD 13.7). Hai kenh qua orderType:
 * MARKETPLACE = Customer <-> Seller qua Listing, tien mat offline, khong Payment.
 * NEW_DEVICE  = Customer mua may moi tu Admin qua Product, co Payment (BR-43, BR-44).
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private User buyer;
    private OrderType orderType;
    private OrderStatus status;
    private Date createdAt;
    private Date updatedAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    /**
     * SUM(OrderItem.price x quantity) tren toan bo OrderItem.
     * Tinh toan thay vi luu truong totalAmount denormalized (SDD 13.7).
     * MARKETPLACE: dung mot OrderItem voi quantity = 1 nen bang chinh OrderItem.price.
     */
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
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

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
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

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
