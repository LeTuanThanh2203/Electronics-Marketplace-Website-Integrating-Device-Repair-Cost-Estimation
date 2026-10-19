package com.electronics.marketplace.controller.auth;

import com.electronics.marketplace.DBConnection.AuthDB;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.UserStatus;
import com.electronics.marketplace.filter.AuthenticationFilter;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute(AuthenticationFilter.SESSION_KEY) != null) {
            resp.sendRedirect(req.getContextPath() + "/profile");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String error = null;
        try {
            User u = (email == null || password == null) ? null : AuthDB.authenticate(email.trim(), password);
            if (u == null) {
                error = "Email hoặc mật khẩu không đúng.";
            } else if (u.getStatus() == UserStatus.SUSPENDED) {
                error = "Tài khoản của bạn đang bị khóa. Vui lòng liên hệ quản trị viên.";
            } else {
                req.changeSessionId();                       // chống session fixation
                req.getSession().setAttribute(AuthenticationFilter.SESSION_KEY, u);
                resp.sendRedirect(req.getContextPath() + (u.isAdmin() ? "/admin" : "/profile"));
                return;
            }
        } catch (PersistenceException e) {
            log("Loi dang nhap", e);
            error = "Lỗi hệ thống, vui lòng thử lại.";
        }
        req.setAttribute("error", error);
        req.setAttribute("email", email);
        req.getRequestDispatcher("/WEB-INF/auth/login.jsp").forward(req, resp);
    }
}

