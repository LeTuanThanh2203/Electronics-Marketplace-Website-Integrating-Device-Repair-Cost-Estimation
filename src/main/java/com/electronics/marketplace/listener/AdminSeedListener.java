package com.electronics.marketplace.listener;

import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Tạo tài khoản admin đầu tiên từ biến môi trường, chỉ khi chưa có admin nào. */
@WebListener
public class AdminSeedListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String email = System.getenv("ADMIN_EMAIL");
            String password = System.getenv("ADMIN_PASSWORD");
            if (email == null || email.trim().isEmpty()) {
                email = "admin@marketplace.com";
            }
            if (password == null || password.trim().isEmpty()) {
                password = "admin123";
            }

            if (!UserDB.existsAdmin()) {
                User a = new User();
                a.setName("Quản trị viên Hệ thống");
                a.setEmail(email.trim());
                a.setPasswordHash(PasswordUtil.hash(password));
                a.setRole(Role.ADMIN);
                UserDB.insert(a);
                sce.getServletContext().log("Đã tạo tài khoản admin mặc định: " + email.trim() + " / " + password);
            }

            if (UserDB.getByEmail("user@marketplace.com") == null) {
                User u = new User();
                u.setName("Người dùng Mẫu");
                u.setEmail("user@marketplace.com");
                u.setPasswordHash(PasswordUtil.hash("user123"));
                u.setRole(Role.USER);
                UserDB.insert(u);
                sce.getServletContext().log("Đã tạo tài khoản user mẫu: user@marketplace.com / user123");
            }
        } catch (Exception e) {
            sce.getServletContext().log("Lỗi khởi tạo tài khoản seed: " + e.getMessage());
        }
    }
}