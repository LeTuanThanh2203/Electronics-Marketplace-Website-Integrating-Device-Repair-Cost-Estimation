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
    private static final String ENV_DATABASE_URL = "DATABASE_URL";
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
     * Override map luon thang gia tri khai bao trong persistence.xml,
     * ho tro tu dong nhan dien DATABASE_URL tu Render / Cloud hoac DB_URL local.
     */
    private static Map<String, Object> connectionOverrides() {
        Map<String, Object> overrides = new HashMap<>();

        String rawUrl = getEnvOrDefault(ENV_URL, null);
        if (rawUrl == null) {
            rawUrl = getEnvOrDefault(ENV_DATABASE_URL, "jdbc:postgresql://localhost:5432/electronics_marketplace");
        }

        String user = getEnvOrDefault(ENV_USER, null);
        String password = getEnvOrDefault(ENV_PASSWORD, null);

        // Chuan hoa neu la URL dang URI (postgres://user:pass@host:port/dbname)
        if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
            try {
                java.net.URI uri = new java.net.URI(rawUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    if (user == null && parts.length > 0 && !parts[0].isEmpty()) {
                        user = parts[0];
                    }
                    if (password == null && parts.length > 1 && !parts[1].isEmpty()) {
                        password = parts[1];
                    }
                }
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                rawUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path;
                if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                    rawUrl += "?" + uri.getQuery();
                }
            } catch (Exception ignored) {
                if (rawUrl.startsWith("postgres://")) {
                    rawUrl = "jdbc:postgresql://" + rawUrl.substring("postgres://".length());
                } else {
                    rawUrl = "jdbc:postgresql://" + rawUrl.substring("postgresql://".length());
                }
            }
        }

        if (user == null) {
            user = "postgres";
        }
        if (password == null) {
            password = "02052010";
        }

        overrides.put("jakarta.persistence.jdbc.url", rawUrl);
        overrides.put("jakarta.persistence.jdbc.user", user);
        overrides.put("jakarta.persistence.jdbc.password", password);
        return overrides;
    }

    /** Lay bien moi truong, hoac System property, hoac gia tri mac dinh. */
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
