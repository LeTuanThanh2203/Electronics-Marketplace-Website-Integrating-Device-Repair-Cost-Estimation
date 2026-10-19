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
        String email = System.getenv("ADMIN_EMAIL");
        String password = System.getenv("ADMIN_PASSWORD");
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            sce.getServletContext().log("Khong tao admin: chua dat ADMIN_EMAIL / ADMIN_PASSWORD.");
            return;
        }
        if (UserDB.existsAdmin()) return;

        User a = new User();
        a.setName("Quản trị viên");
        a.setEmail(email.trim());
        a.setPasswordHash(PasswordUtil.hash(password));
        a.setRole(Role.ADMIN);
        UserDB.insert(a);
        sce.getServletContext().log("Da tao tai khoan admin: " + email.trim());
    }
}