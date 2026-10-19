package com.electronics.marketplace.listener;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.util.MailUtilGmail;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppShutdownListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MailUtilGmail.shutdown();
        // Đóng EMF + pool HikariCP khi redeploy/undeploy.
        JpaConfig.shutdown();
    }
}