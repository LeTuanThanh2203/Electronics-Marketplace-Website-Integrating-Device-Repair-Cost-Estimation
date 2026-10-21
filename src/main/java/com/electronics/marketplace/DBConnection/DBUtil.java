package com.electronics.marketplace.DBConnection;

import com.electronics.marketplace.config.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tiện ích quản lý vòng đời EntityManager và Transaction cho các DAO/DB classes.
 */
public class DBUtil {

    private static EntityManager em() {
        EntityManagerFactory emf = JpaConfig.getEntityManagerFactory();
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory chưa sẵn sàng");
        }
        return emf.createEntityManager();
    }

    /**
     * Thao tác đọc dữ liệu (query), tự động đóng EntityManager.
     */
    public static <T> T query(Function<EntityManager, T> work) {
        EntityManager em = em();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    /**
     * Thao tác ghi dữ liệu trong transaction, tự động commit/rollback và đóng EntityManager.
     */
    public static <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Thao tác ghi dữ liệu không cần giá trị trả về.
     */
    public static void execute(Consumer<EntityManager> work) {
        inTransaction(em -> {
            work.accept(em);
            return null;
        });
    }
}
