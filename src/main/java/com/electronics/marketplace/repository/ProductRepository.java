package com.electronics.marketplace.repository;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.enums.ProductStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Truy cap Product (SDD 13.5).
 *
 * ponytail: moi method tu mo EntityManager + transaction roi dong ngay, KHONG
 * dung chung mot EM dai han. Du an chay 1 JVM, EM ngan han la cach re nhat de
 * tranh stale cache va tranh ro ri connection trong pool. Khi nao co nhieu
 * thao tac lien tiep trong cung mot request thi chuyen sang EM per-request
 * (filter mo/dong) - luc do moi can.
 */
public class ProductRepository {

    public Product findById(Long id) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    /** Availability filter cua SDD 8.3: chi Product AVAILABLE moi ban duoc. */
    public List<Product> findAvailable() {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.id",
                            Product.class)
                    .setParameter("status", ProductStatus.AVAILABLE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(Product product) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (product.getId() == null) {
                em.persist(product);
            } else {
                em.merge(product);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
