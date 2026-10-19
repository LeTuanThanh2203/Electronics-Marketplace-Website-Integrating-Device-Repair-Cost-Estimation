package com.electronics.marketplace.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Khoi tao EntityManagerFactory dung chung cho toan app.
 *
 * Thong tin ket noi DB lay tu bien moi truong DB_URL / DB_USER / DB_PASSWORD,
 * KHONG hardcode trong persistence.xml (khong commit mat khau vao git).
 *
 * Vi sao phai lam o Java: Hibernate 6.4.4 chi thay the ${TEN_SYSTEM_PROPERTY}
 * trong persistence.xml, khong ho tro ${env.TEN}. Da kiem chung: dung
 * ${env.DB_URL} thi Hibernate nem "HHH10001004: No JDBC URL specified".
 *
 * ponytail: EMF la singleton nghia la moi JVM chi tao mot lan, KHONG close -
 * dong EMF se pha huy connection pool dung chung. Du an chay 1 JVM nen khong
 * can lifecycle listener. Khi nao hot-reload / nhieu context thi them
 * ServletContextListener goi close().
 */
public final class JpaConfig {

    public static final String PU_NAME = "Electronics_MarketplacePU";

    private static final String ENV_URL = "DB_URL";
    private static final String ENV_USER = "DB_USER";
    private static final String ENV_PASSWORD = "DB_PASSWORD";

    /** Holder: JVM chi dong bo hoa mot lan, khong can synchronized thu cong. */
    private static class Holder {
        private static final EntityManagerFactory EMF = build();
    }

    private JpaConfig() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return Holder.EMF;
    }

    /**
     * Tạo EntityManager mới từ EMF dùng chung.
     * Giữ để module Product (Anh) đang gọi {@code JpaConfig.getEntityManager()} compile được.
     * Caller chịu trách nhiệm {@code close()} EntityManager.
     */
    public static EntityManager getEntityManager() {
        return Holder.EMF.createEntityManager();
    }

    /** Đóng EMF và pool khi context bị destroy (gọi từ AppShutdownListener). */
    public static void shutdown() {
        if (Holder.EMF.isOpen()) {
            Holder.EMF.close();
        }
    }

    private static EntityManagerFactory build() {
        return Persistence.createEntityManagerFactory(PU_NAME, connectionOverrides());
    }

    /**
     * Override map luon thang gia tri khai bao trong persistence.xml, nen
     * placeholder ${DB_URL} trong file khong bat buoc phai resolve duoc.
     */
    private static Map<String, Object> connectionOverrides() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", requireEnv(ENV_URL));
        overrides.put("jakarta.persistence.jdbc.user", requireEnv(ENV_USER));
        overrides.put("jakarta.persistence.jdbc.password", requireEnv(ENV_PASSWORD));
        return overrides;
    }

    /** Fail fast: thieu bien moi truong thi bao ngay, khong de Hibernate bao loi kho hieu. */
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Thieu bien moi truong " + name + ". Dat DB_URL, DB_USER, DB_PASSWORD truoc khi chay.");
        }
        return value;
    }
}
