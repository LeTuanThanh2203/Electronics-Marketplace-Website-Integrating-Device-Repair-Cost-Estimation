package com.electronics.marketplace.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Mot dong trong Cart (SDD 13.8).
 * quantity luon = 1: moi Product la mot don vi duy nhat.
 * (cartId, productId) la duy nhat.
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Cart cart;
    private Product product;
    private int quantity = 1;
    private Date addedAt;

    public CartItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }
}
