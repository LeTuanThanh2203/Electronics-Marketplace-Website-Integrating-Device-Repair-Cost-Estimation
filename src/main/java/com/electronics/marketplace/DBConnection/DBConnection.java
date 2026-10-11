package com.electronics.marketplace.DBConnection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Đọc cấu hình (biến môi trường > db.properties) và quản lý EntityManagerFactory dùng chung. */
public class DBConnection {

    private static final Properties PROPS = new Properties();
    private static EntityManagerFactory emf;

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String get(String env, String key, String def) {
        String v = System.getenv(env);
        if (v != null && !v.isBlank()) return v.trim();
        return PROPS.getProperty(key, def).trim();
    }

    public static String host()     { return get("DB_HOST", "db.host", "localhost"); }
    public static String port()     { return get("DB_PORT", "db.port", "5432"); }
    public static String dbName()   { return get("DB_NAME", "db.name", "chodocu"); }
    public static String user()     { return get("DB_USER", "db.user", "postgres"); }
    public static String password() { return get("DB_PASSWORD", "db.password", "postgres"); }

    private static Map<String, Object> props(String database) {
        Map<String, Object> m = new HashMap<>();
        m.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://" + host() + ":" + port() + "/" + database);
        m.put("jakarta.persistence.jdbc.user", user());
        m.put("jakarta.persistence.jdbc.password", password());
        return m;
    }

    /** EntityManagerFactory chính (tạo 1 lần, có connection pool). */
    public static synchronized EntityManagerFactory getEmf() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("chodocuPU", props(dbName()));
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEmf().createEntityManager();
    }

    /** Factory tạm trỏ vào database "postgres" để tạo database của project lần đầu. */
    public static EntityManagerFactory createAdminEmf() {
        return Persistence.createEntityManagerFactory("adminPU", props("postgres"));
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}

