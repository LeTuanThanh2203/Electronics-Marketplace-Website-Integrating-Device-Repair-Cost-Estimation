package com.electronics.marketplace.controller.auth;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.util.PasswordUtil;
import com.electronics.marketplace.util.MailUtilGmail;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email"));
        String phone = trim(req.getParameter("phone"));
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");

        String error = null;
        if (name.isEmpty() || name.length() > 100) error = "Họ tên không hợp lệ.";
        else if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) error = "Email không hợp lệ.";
        else if (!phone.matches("^[0-9]{9,11}$")) error = "Số điện thoại phải có 9-11 chữ số.";
        else if (password == null || password.length() < 6) error = "Mật khẩu tối thiểu 6 ký tự.";
        else if (!password.equals(confirm)) error = "Mật khẩu nhập lại không khớp.";

        try {
            if (error == null && UserDB.emailExists(email)) {
                error = "Email này đã được đăng ký.";
            }
            // Kiểm tra tên miền email có tồn tại không (nhanh, chạy đồng bộ)
            if (error == null && !MailUtilGmail.domainHasMailServer(email)) {
                error = "Email không tồn tại. Vui lòng kiểm tra lại.";
            }
            if (error == null) {
                User u = new User();
                u.setName(name);
                u.setEmail(email);
                u.setPhone(phone);
                u.setPasswordHash(PasswordUtil.hash(password));
                UserDB.insert(u);

                MailUtilGmail.sendWelcomeAsync(email, name);   // gửi mail ngầm, không chờ
                resp.sendRedirect(req.getContextPath() + "/login?registered=1");
                return;
            }
        } catch (PersistenceException e) {
            log("Loi dang ky", e);
            error = "Lỗi hệ thống, vui lòng thử lại.";
        }
        req.setAttribute("error", error);
        req.setAttribute("name", name);
        req.setAttribute("email", email);
        req.setAttribute("phone", phone);
        req.getRequestDispatcher("/WEB-INF/auth/register.jsp").forward(req, resp);
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}