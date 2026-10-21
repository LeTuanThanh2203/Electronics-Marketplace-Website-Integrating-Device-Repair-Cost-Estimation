package com.electronics.marketplace.repository;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Order;
import com.electronics.marketplace.entity.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;

/**
 * Doc don hang cho history.jsp. Ghi trang thai don nam trong OrderService.
 */
public class OrderRepository {

    private static final String FETCH_ITEMS =
            "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.items i " +
                    "LEFT JOIN FETCH i.product " +
                    "LEFT JOIN FETCH i.listing l " +
                    "LEFT JOIN FETCH l.device " +
                    "LEFT JOIN FETCH l.seller ";

    /**
     * Trang don cua buyer. Hai query thay vi mot:
     * phan trang tren bang orders truoc, roi fetch join theo id.
     *
     * Ly do: fetch join collection lam so dong SQL lon hon so Order, nen
     * setMaxResults tren cau co join se cat giua chung va tra ve it don hon
     * pageSize (HHH000104). Don NEW_DEVICE nhieu OrderItem la truong hop that.
     */
    public List<Order> findHistory(Long buyerId, OrderStatus status, int page, int pageSize) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT o.id FROM Order o WHERE o.buyer.id = :buyerId");
            if (status != null) {
                jpql.append(" AND o.status = :status");
            }
            jpql.append(" ORDER BY o.createdAt DESC, o.id DESC");

            TypedQuery<Long> idQuery = em.createQuery(jpql.toString(), Long.class)
                    .setParameter("buyerId", buyerId)
                    .setFirstResult((page - 1) * pageSize)
                    .setMaxResults(pageSize);
            if (status != null) {
                idQuery.setParameter("status", status);
            }

            List<Long> ids = idQuery.getResultList();
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            return em.createQuery(
                            FETCH_ITEMS + "WHERE o.id IN :ids ORDER BY o.createdAt DESC, o.id DESC",
                            Order.class)
                    .setParameter("ids", ids)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long countHistory(Long buyerId, OrderStatus status) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(o) FROM Order o WHERE o.buyer.id = :buyerId");
            if (status != null) {
                jpql.append(" AND o.status = :status");
            }
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                    .setParameter("buyerId", buyerId);
            if (status != null) {
                query.setParameter("status", status);
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
