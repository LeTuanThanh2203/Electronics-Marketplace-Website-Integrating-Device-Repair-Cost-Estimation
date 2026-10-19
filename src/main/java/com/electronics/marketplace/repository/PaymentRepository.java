package com.electronics.marketplace.repository;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Payment;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Doc Payment. Xac nhan PAID do Admin lam thu cong (BR-44) nen repository nay
 * khong co thao tac ghi.
 */
public class PaymentRepository {

    /**
     * Payment kem order + buyer (de check IDOR) va items (payment.jsp doc
     * order.totalAmount). Khong fetch i.product: getTotalAmount() chi dung
     * OrderItem.price.
     */
    public Payment findByOrderId(Long orderId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            List<Payment> found = em.createQuery(
                            "SELECT DISTINCT p FROM Payment p " +
                                    "JOIN FETCH p.order o " +
                                    "JOIN FETCH o.buyer " +
                                    "LEFT JOIN FETCH o.items " +
                                    "WHERE o.id = :orderId",
                            Payment.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
            return found.isEmpty() ? null : found.get(0);
        } finally {
            em.close();
        }
    }

    /** Map orderId -> Payment cho history.jsp (Order khong co truong payment). */
    public Map<Long, Payment> findByOrderIds(Collection<Long> orderIds) {
        Map<Long, Payment> byOrderId = new HashMap<>();
        if (orderIds == null || orderIds.isEmpty()) {
            return byOrderId;
        }
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            List<Payment> payments = em.createQuery(
                            "SELECT p FROM Payment p WHERE p.order.id IN :ids",
                            Payment.class)
                    .setParameter("ids", orderIds)
                    .getResultList();
            for (Payment payment : payments) {
                // getId() tren proxy khong kich hoat lazy load.
                byOrderId.put(payment.getOrder().getId(), payment);
            }
            return byOrderId;
        } finally {
            em.close();
        }
    }
}
