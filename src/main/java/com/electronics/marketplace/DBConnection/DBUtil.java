package com.electronics.marketplace.DBConnection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Consumer;
import java.util.function.Function;

/** Tiện ích mở/đóng EntityManager và transaction để các lớp DAO gọn hơn. */
public class DBUtil {
    /** Chạy thao tác đọc (không cần transaction), tự đóng EntityManager. */
    public static <T> T query(Function<EntityManager, T> work) {
        EntityManager em = DBConnection.createEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    /** Chạy thao tác ghi trong transaction, trả về kết quả. Lỗi thì rollback. */
    public static <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager em = DBConnection.createEntityManager();
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
