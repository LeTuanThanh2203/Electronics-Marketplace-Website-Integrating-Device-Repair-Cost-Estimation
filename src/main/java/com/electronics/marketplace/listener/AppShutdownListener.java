package com.electronics.marketplace.listener;

import com.electronics.marketplace.util.MailUtilGmail;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppShutdownListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MailUtilGmail.shutdown();
        // Nếu JpaConfig có hàm đóng EntityManagerFactory thì gọi thêm ở đây,
        // ví dụ JpaConfig.close(); để pool kết nối được trả lại khi redeploy.
    }
}