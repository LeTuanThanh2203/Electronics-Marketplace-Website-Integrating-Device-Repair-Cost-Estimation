package com.electronics.marketplace.config;

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

    private static volatile EntityManagerFactory emf;

    private JpaConfig() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = build();
        }
        return emf;
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
        overrides.put("jakarta.persistence.jdbc.url", getEnvOrDefault(ENV_URL, "jdbc:postgresql://localhost:5432/electronics_marketplace"));
        overrides.put("jakarta.persistence.jdbc.user", getEnvOrDefault(ENV_USER, "postgres"));
        overrides.put("jakarta.persistence.jdbc.password", getEnvOrDefault(ENV_PASSWORD, "02052010"));
        return overrides;
    }

    /** Lay bien moi truong, hoac System property, hoac gia tri mac dinh an toan cho local dev. */
    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(name);
        }
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
