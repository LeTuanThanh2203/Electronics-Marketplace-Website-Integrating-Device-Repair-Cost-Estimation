package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.PaymentMethod;
import com.electronics.marketplace.entity.enums.PaymentStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Ban ghi yeu cau thanh toan cho Order NEW_DEVICE (SDD 13.8).
 * Moi Order co toi da mot Payment (orderId unique); Order MARKETPLACE khong co Payment.
 * MVP khong tich hop payment gateway: he thong sinh Payment o PENDING,
 * Admin xac nhan thu cong sang PAID (BR-44).
 */
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Order order;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String transactionCode;
    private Date createdAt;
    private Date paidAt;

    public Payment() {
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

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
    }
}
