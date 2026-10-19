package com.electronics.marketplace.config;

import com.electronics.marketplace.util.JpaUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Quản lý vòng đời tài nguyên dùng chung của ứng dụng.
 *
 * <p>Hiện tại chỉ giải phóng {@code EntityManagerFactory} (và connection pool
 * HikariCP bên trong) khi ứng dụng bị undeploy hoặc reload. Không có bước này,
 * Tomcat giữ lại pool của lần deploy cũ, và sau vài lần reload PostgreSQL sẽ
 * hết connection.</p>
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().log("Electronics Marketplace đang khởi động.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.shutdown();
        sce.getServletContext().log("Electronics Marketplace đã dừng, EntityManagerFactory đã đóng.");
    }
}
