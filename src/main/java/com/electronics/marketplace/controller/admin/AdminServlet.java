package com.electronics.marketplace.controller.admin;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import com.electronics.marketplace.entity.enums.Status;
import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.entity.User;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("users", UserDB.selectAll());
            req.setAttribute("totalUsers", UserDB.countAll());
            req.setAttribute("activeUsers", UserDB.countByStatus(Status.ACTIVE));
            req.setAttribute("suspendedUsers", UserDB.countByStatus(Status.SUSPENDED));
        } catch (PersistenceException e) {
            log("Loi tai dashboard", e);
            req.setAttribute("error", "Không tải được dữ liệu.");
        }
        req.getRequestDispatcher("/WEB-INF/admin/dashboard.jsp").forward(req, resp);
    }

    /** Nhận action=lock|unlock|delete&userId=... từ AJAX và trả về JSON. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User me = (User) req.getSession().getAttribute("user");
        String action = req.getParameter("action") == null ? "" : req.getParameter("action");
        try {
            int id = Integer.parseInt(req.getParameter("userId"));
            User target = UserDB.selectById(id);
            if (target == null) {
                writeJson(resp, false, "Không tìm thấy người dùng.", null);
                return;
            }
            if (target.getUserID() == me.getUserID() || target.isAdmin()) {
                writeJson(resp, false, "Không thể thao tác với chính bạn hoặc tài khoản admin.", null);
                return;
            }
            Status newStatus;
            String message;
            switch (action) {
                case "lock":   newStatus = Status.SUSPENDED; message = "Đã khóa tài khoản."; break;
                case "unlock": newStatus = Status.ACTIVE;    message = "Đã mở khóa tài khoản."; break;
                case "delete": newStatus = Status.DELETED;   message = "Đã xóa tài khoản."; break;
                default:
                    writeJson(resp, false, "Thao tác không hợp lệ.", null);
                    return;
            }
            UserDB.updateStatus(id, newStatus);
            writeJson(resp, true, message, newStatus);
        } catch (NumberFormatException e) {
            writeJson(resp, false, "Mã người dùng không hợp lệ.", null);
        } catch (PersistenceException e) {
            log("Loi thao tac admin", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, false, "Lỗi hệ thống, vui lòng thử lại.", null);
        }
    }

    private void writeJson(HttpServletResponse resp, boolean ok, String message, Status status) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":").append(ok)
          .append(",\"message\":\"").append(message).append("\"");
        if (status != null) {
            sb.append(",\"status\":\"").append(status.name()).append("\"")
              .append(",\"total\":").append(UserDB.countAll())
              .append(",\"active\":").append(UserDB.countByStatus(Status.ACTIVE))
              .append(",\"suspended\":").append(UserDB.countByStatus(Status.SUSPENDED));
        }
        sb.append("}");
        resp.getWriter().write(sb.toString());
    }
}

