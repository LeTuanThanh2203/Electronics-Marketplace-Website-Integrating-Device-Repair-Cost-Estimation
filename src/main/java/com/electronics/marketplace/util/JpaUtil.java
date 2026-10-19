package com.electronics.marketplace.util;

import com.electronics.marketplace.config.AppLifecycleListener;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Điểm truy cập duy nhất tới {@link EntityManagerFactory} của ứng dụng.
 *
 * <p>EMF được tạo lười ở lần dùng đầu tiên và được đóng bởi
 * {@link AppLifecycleListener} khi Tomcat undeploy/reload. Nếu không đóng,
 * HikariCP giữ connection pool mở và mỗi lần redeploy sẽ rò một pool, dần cạn
 * connection của PostgreSQL.</p>
 */
public final class JpaUtil {

    private static final String PERSISTENCE_UNIT_NAME = "Electronics_MarketplacePU";

    private static volatile EntityManagerFactory emf;

    private JpaUtil() {
    }

    /**
     * Trả về EMF, tạo mới nếu chưa có hoặc đã bị đóng.
     *
     * @throws IllegalStateException nếu không khởi tạo được (thiếu driver, sai cấu hình DB...)
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory local = emf;
        if (local == null || !local.isOpen()) {
            synchronized (JpaUtil.class) {
                local = emf;
                if (local == null || !local.isOpen()) {
                    try {
                        local = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    } catch (Throwable ex) {
                        // Ném lỗi rõ ràng thay vì trả null rồi NPE ở tầng repository.
                        throw new IllegalStateException(
                                "Không khởi tạo được EntityManagerFactory '" + PERSISTENCE_UNIT_NAME
                                        + "'. Kiểm tra PostgreSQL đang chạy và cấu hình trong persistence.xml. "
                                        + "Nguyên nhân: " + ex.getMessage(), ex);
                    }
                    emf = local;
                }
            }
        }
        return local;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Đóng EMF và giải phóng connection pool. Được gọi tự động khi ứng dụng dừng.
     */
    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            try {
                emf.close();
            } catch (Exception e) {
                System.err.println("Lỗi khi đóng EntityManagerFactory: " + e.getMessage());
            } finally {
                emf = null;
            }
        }
    }
}
