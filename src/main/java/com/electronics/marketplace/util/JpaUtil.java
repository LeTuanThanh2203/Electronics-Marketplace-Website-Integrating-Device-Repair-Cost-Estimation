package com.electronics.marketplace.util;

import com.electronics.marketplace.config.JpaConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Điểm truy cập EntityManagerFactory cho module Repair.
 *
 * <p>Đây là lớp vỏ mỏng chuyển tiếp sang {@link JpaConfig} của module Auth.
 * <b>Không</b> tự gọi {@code Persistence.createEntityManagerFactory()}: cấu hình
 * kết nối (DB_URL / DB_USER / DB_PASSWORD) nằm trong {@code JpaConfig}, và
 * {@code persistence.xml} dùng placeholder {@code ${DB_URL}} mà Hibernate 6.4.4
 * không tự resolve được khi tạo EMF trực tiếp.</p>
 *
 * <p>Nhờ vậy cả hai module dùng chung một EntityManagerFactory và một connection
 * pool HikariCP. Việc đóng pool do
 * {@link com.electronics.marketplace.listener.AppShutdownListener} đảm nhận.</p>
 */
public final class JpaUtil {

    private JpaUtil() {
    }

    /**
     * Trả về EMF dùng chung.
     *
     * @throws IllegalStateException nếu thiếu biến môi trường kết nối DB
     *         (do {@code JpaConfig} ném ra)
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return JpaConfig.getEntityManagerFactory();
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Đóng EMF dùng chung khi context bị destroy. Chỉ
     * {@link com.electronics.marketplace.listener.AppShutdownListener} nên gọi;
     * các nơi khác gọi sẽ làm hỏng pool của toàn app.
     */
    public static void shutdown() {
        JpaConfig.shutdown();
    }
}
