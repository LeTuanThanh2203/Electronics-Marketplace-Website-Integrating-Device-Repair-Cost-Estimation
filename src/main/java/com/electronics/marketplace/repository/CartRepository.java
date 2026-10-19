package com.electronics.marketplace.repository;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Cart;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Doc gio hang. Cac thao tac ghi (add/remove) nam trong CartService vi chung
 * can mot transaction rieng, khong the ghep tu nhieu method EM ngan han.
 */
public class CartRepository {

    /**
     * Cart cua user kem items + product da fetch: JSP doc
     * item.product.price de tinh subtotal SAU khi EM da dong, nen khong the
     * de LAZY.
     *
     * Khong dat setMaxResults: fetch join collection lam so dong SQL lon hon so
     * Cart, cat theo dong se mat bot CartItem. customer_id la UNIQUE nen ket qua
     * DISTINCT toi da mot phan tu - lay phan tu dau la du.
     */
    public Cart findByUserId(Long userId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            List<Cart> carts = em.createQuery(
                            "SELECT DISTINCT c FROM Cart c " +
                                    "LEFT JOIN FETCH c.items i " +
                                    "LEFT JOIN FETCH i.product " +
                                    "WHERE c.customer.id = :uid",
                            Cart.class)
                    .setParameter("uid", userId)
                    .getResultList();
            return carts.isEmpty() ? null : carts.get(0);
        } finally {
            em.close();
        }
    }
}
