package com.electronics.marketplace.controller.admin;

import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.UserStatus;
import com.electronics.marketplace.filter.AuthenticationFilter;
import com.electronics.marketplace.util.HttpUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("users", UserDB.selectAll());
            req.setAttribute("totalUsers", UserDB.countAll());
            req.setAttribute("activeUsers", UserDB.countByStatus(UserStatus.ACTIVE));
            req.setAttribute("suspendedUsers", UserDB.countByStatus(UserStatus.SUSPENDED));
        } catch (PersistenceException e) {
            log("Loi tai dashboard", e);
            req.setAttribute("error", "Không tải được dữ liệu.");
        }
        req.getRequestDispatcher("/WEB-INF/admin/dashboard.jsp").forward(req, resp);
    }

    /** Nhận action=lock|unlock|delete&userId=... từ AJAX và trả về JSON. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User me = (User) req.getSession().getAttribute(AuthenticationFilter.SESSION_KEY);
        String action = req.getParameter("action") == null ? "" : req.getParameter("action");
        try {
            Long id = Long.valueOf(req.getParameter("userId"));
            User target = UserDB.selectById(id);
            if (target == null) {
                fail(resp, "Không tìm thấy người dùng.");
                return;
            }
            if (target.getId().equals(me.getId()) || target.isAdmin()) {
                fail(resp, "Không thể thao tác với chính bạn hoặc tài khoản admin.");
                return;
            }
            UserStatus newStatus;
            String message;
            switch (action) {
                case "lock":   newStatus = UserStatus.SUSPENDED; message = "Đã khóa tài khoản."; break;
                case "unlock": newStatus = UserStatus.ACTIVE;    message = "Đã mở khóa tài khoản."; break;
                case "delete": newStatus = UserStatus.DELETED;   message = "Đã xóa tài khoản."; break;
                default:
                    fail(resp, "Thao tác không hợp lệ.");
                    return;
            }
            UserDB.updateStatus(id, newStatus);
            HttpUtil.writeJson(resp, HttpServletResponse.SC_OK,
                "{\"ok\":true,\"message\":\"" + message + "\",\"status\":\"" + newStatus.name() + "\""
                + ",\"total\":" + UserDB.countAll()
                + ",\"active\":" + UserDB.countByStatus(UserStatus.ACTIVE)
                + ",\"suspended\":" + UserDB.countByStatus(UserStatus.SUSPENDED) + "}");
        } catch (NumberFormatException e) {
            fail(resp, "Mã người dùng không hợp lệ.");
        } catch (PersistenceException e) {
            log("Loi thao tac admin", e);
            HttpUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "{\"ok\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại.\"}");
        }
    }

    private void fail(HttpServletResponse resp, String message) throws IOException {
        HttpUtil.writeJson(resp, HttpServletResponse.SC_OK, "{\"ok\":false,\"message\":\"" + message + "\"}");
    }
}

