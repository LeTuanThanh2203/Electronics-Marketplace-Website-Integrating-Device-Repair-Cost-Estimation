package com.electronics.marketplace.controller.auth;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import com.electronics.marketplace.util.SQLUtil;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.util.PasswordUtil;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/auth/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        User sessionUser = (User) session.getAttribute("user");
        String action = req.getParameter("action");
        String error = null, success = null;

        try {
            User u = UserDB.selectById(sessionUser.getUserID());   // lấy dữ liệu mới nhất
            if ("info".equals(action)) {
                String name = req.getParameter("name") == null ? "" : req.getParameter("name").trim();
                String phone = req.getParameter("phone") == null ? "" : req.getParameter("phone").trim();
                if (name.isEmpty() || name.length() > 100) error = "Họ tên không hợp lệ.";
                else if (!phone.matches("^[0-9]{9,11}$")) error = "Số điện thoại phải có 9-11 chữ số.";
                else {
                    u.setName(name);
                    u.setPhone(phone);
                    u.setLocation(SQLUtil.blankToNull(req.getParameter("location")));
                    u.setAvatar(SQLUtil.blankToNull(req.getParameter("avatar")));
                    UserDB.updateProfile(u);
                    success = "Đã cập nhật thông tin cá nhân.";
                }
            } else if ("password".equals(action)) {
                String oldPw = req.getParameter("oldPassword");
                String newPw = req.getParameter("newPassword");
                if (oldPw == null || !PasswordUtil.verify(oldPw, u.getPasswordHash())) error = "Mật khẩu hiện tại không đúng.";
                else if (newPw == null || newPw.length() < 6) error = "Mật khẩu mới tối thiểu 6 ký tự.";
                else if (!newPw.equals(req.getParameter("confirm"))) error = "Mật khẩu nhập lại không khớp.";
                else {
                    UserDB.updatePassword(u.getUserID(), PasswordUtil.hash(newPw));
                    success = "Đã đổi mật khẩu.";
                }
            }
            session.setAttribute("user", UserDB.selectById(u.getUserID()));
        } catch (PersistenceException e) {
            log("Loi cap nhat profile", e);
            error = "Lỗi hệ thống, vui lòng thử lại.";
        }
        req.setAttribute("error", error);
        req.setAttribute("success", success);
        req.getRequestDispatcher("/WEB-INF/auth/profile.jsp").forward(req, resp);
    }
}
