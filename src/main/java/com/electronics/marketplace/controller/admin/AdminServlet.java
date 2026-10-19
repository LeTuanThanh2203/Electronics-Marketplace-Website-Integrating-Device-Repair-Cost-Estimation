package com.electronics.marketplace.controller.admin;

import com.electronics.marketplace.DBConnection.DBUtil;
import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.ListingStatus;
import com.electronics.marketplace.entity.enums.UserStatus;
import com.electronics.marketplace.filter.AuthenticationFilter;
import com.electronics.marketplace.util.HttpUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminServlet", urlPatterns = {"/admin", "/admin/dashboard", "/admin/users"})
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 1. Thống kê & Quản lý User (phần cũ của Tân)
            req.setAttribute("users", UserDB.selectAll());
            req.setAttribute("totalUsers", UserDB.countAll());
            req.setAttribute("activeUsers", UserDB.countByStatus(UserStatus.ACTIVE));
            req.setAttribute("suspendedUsers", UserDB.countByStatus(UserStatus.SUSPENDED));

            // 2. TÍCH HỢP VỚI THÀNH: Lấy danh sách tin chợ đồ cũ chờ duyệt (isApproved = false, isRemovedByAdmin = false)
            List<Listing> pendingListings = DBUtil.query(em -> em.createQuery(
                    "SELECT l FROM Listing l JOIN FETCH l.seller LEFT JOIN FETCH l.device WHERE (l.isApproved = false OR l.isApproved IS NULL) AND (l.isRemovedByAdmin = false OR l.isRemovedByAdmin IS NULL) ORDER BY l.createdAt DESC", Listing.class)
                    .getResultList());
            req.setAttribute("pendingListings", pendingListings);
            req.setAttribute("pendingListingsCount", pendingListings.size());

        } catch (PersistenceException e) {
            log("Loi tai dashboard", e);
            req.setAttribute("error", "Không tải được dữ liệu.");
        }
        req.getRequestDispatcher("/WEB-INF/admin/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User me = (User) req.getSession().getAttribute(AuthenticationFilter.SESSION_KEY);
        String action = req.getParameter("action") == null ? "" : req.getParameter("action");

        try {
            if ("approveListing".equals(action) || "rejectListing".equals(action)) {
                Long listingId = Long.valueOf(req.getParameter("listingId"));
                boolean isApprove = "approveListing".equals(action);
                String reason = req.getParameter("reason");
                String msg = isApprove ? "Đã duyệt bài đăng thành công." : "Đã từ chối bài đăng.";

                DBUtil.execute(em -> {
                    Listing l = em.find(Listing.class, listingId);
                    if (l != null) {
                        if (isApprove) {
                            l.setIsApproved(true);
                            l.setIsRemovedByAdmin(false);
                            l.setRejectionReason(null);
                        } else {
                            l.setIsApproved(false);
                            l.setIsRemovedByAdmin(true);
                            l.setRejectionReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Không đạt tiêu chuẩn kiểm duyệt");
                        }
                        l.setUpdatedAt(new java.util.Date());
                    }
                });

                Long remainingCount = DBUtil.query(em -> em.createQuery(
                        "SELECT COUNT(l) FROM Listing l WHERE (l.isApproved = false OR l.isApproved IS NULL) AND (l.isRemovedByAdmin = false OR l.isRemovedByAdmin IS NULL)", Long.class)
                        .getSingleResult());

                HttpUtil.writeJson(resp, HttpServletResponse.SC_OK,
                    "{\"ok\":true,\"message\":\"" + msg + "\",\"listingId\":" + listingId + ",\"pendingCount\":" + remainingCount + "}");
                return;
            }

            
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
            fail(resp, "Mã đối tượng không hợp lệ.");
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