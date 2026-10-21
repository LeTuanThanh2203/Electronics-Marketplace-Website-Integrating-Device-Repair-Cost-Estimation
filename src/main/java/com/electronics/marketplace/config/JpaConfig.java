package com.electronics.marketplace.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Khởi tạo EntityManagerFactory dùng chung cho toàn ứng dụng.
 */
public final class JpaConfig {

    public static final String PU_NAME = "Electronics_MarketplacePU";
    private static volatile EntityManagerFactory emf;

    private JpaConfig() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JpaConfig.class) {
                if (emf == null) {
                    try {
                        emf = Persistence.createEntityManagerFactory(PU_NAME);
                    } catch (Exception e) {
                        System.err.println("[JpaConfig] Không thể khởi tạo EntityManagerFactory: " + e.getMessage());
                    }
                }
            }
        }
        return emf;
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
