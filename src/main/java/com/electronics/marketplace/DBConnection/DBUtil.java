package com.electronics.marketplace.DBConnection;

import com.electronics.marketplace.config.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Consumer;
import java.util.function.Function;

/** Tiện ích mở/đóng EntityManager và transaction cho các lớp DAO. */
public class DBUtil {

    private static EntityManager em() {
        return JpaConfig.getEntityManagerFactory().createEntityManager();
    }

    /** Thao tác đọc (không cần transaction), tự đóng EntityManager. */
    public static <T> T query(Function<EntityManager, T> work) {
        EntityManager em = em();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    /** Thao tác ghi trong transaction, trả về kết quả. Lỗi thì rollback. */
    public static <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Giống inTransaction nhưng không trả kết quả. */
    public static void execute(Consumer<EntityManager> work) {
        inTransaction(em -> {
            work.accept(em);
            return null;
        });
    }
}