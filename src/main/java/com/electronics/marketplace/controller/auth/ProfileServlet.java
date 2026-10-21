package com.electronics.marketplace.controller.auth;

import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.filter.AuthenticationFilter;
import com.electronics.marketplace.util.PasswordUtil;
import com.electronics.marketplace.util.SQLUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/auth/profile"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,        // 1 MB
        maxFileSize = 10 * 1024 * 1024,         // 10 MB
        maxRequestSize = 25 * 1024 * 1024       // 25 MB
)
public class ProfileServlet extends HttpServlet {

    private String getFormField(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        try {
            Part part = req.getPart(name);
            if (part != null) {
                try (var is = part.getInputStream()) {
                    String str = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                    if (!str.isEmpty()) return str;
                }
            }
        } catch (Exception ignored) {}
        return val != null ? val.trim() : null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/auth/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        User sessionUser = (User) session.getAttribute(AuthenticationFilter.SESSION_KEY);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = getFormField(req, "action");
        String error = null, success = null;

        try {
            User u = UserDB.selectById(sessionUser.getId());   // lấy dữ liệu mới nhất
            if (u == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            if ("info".equals(action)) {
                String name = getFormField(req, "name");
                String phone = getFormField(req, "phone");
                String location = getFormField(req, "location");
                String clearAvatar = getFormField(req, "clearAvatar");

                if (name == null || name.isEmpty() || name.length() > 100) {
                    error = "Họ tên không hợp lệ.";
                } else if (phone != null && !phone.isEmpty() && !phone.matches("^[0-9]{9,11}$")) {
                    error = "Số điện thoại phải có 9-11 chữ số.";
                } else {
                    u.setName(name);
                    u.setPhone(SQLUtil.blankToNull(phone));
                    u.setLocation(SQLUtil.blankToNull(location));

                    // Xử lý upload ảnh đại diện từ file máy tính (thư mục)
                    Part avatarPart = null;
                    try {
                        avatarPart = req.getPart("avatarFile");
                    } catch (Exception ignored) {}

                    if (avatarPart != null && avatarPart.getSize() > 0) {
                        String submittedName = avatarPart.getSubmittedFileName();
                        if (submittedName != null && !submittedName.trim().isEmpty()) {
                            String ext = ".jpg";
                            int dot = submittedName.lastIndexOf('.');
                            if (dot >= 0) {
                                String rawExt = submittedName.substring(dot).toLowerCase();
                                if (rawExt.equals(".png") || rawExt.equals(".jpeg") || rawExt.equals(".jpg") || rawExt.equals(".webp") || rawExt.equals(".gif")) {
                                    ext = rawExt;
                                }
                            }
                            String fileName = "avatar_user_" + u.getId() + "_" + System.currentTimeMillis() + ext;
                            String uploadPath = req.getServletContext().getRealPath("/uploads/avatars");
                            File uploadDir = new File(uploadPath);
                            if (!uploadDir.exists()) {
                                uploadDir.mkdirs();
                            }
                            File targetFile = new File(uploadDir, fileName);
                            avatarPart.write(targetFile.getAbsolutePath());
                            u.setAvatar(req.getContextPath() + "/uploads/avatars/" + fileName);
                        }
                    } else if ("true".equalsIgnoreCase(clearAvatar)) {
                        u.setAvatar(null);
                    }

                    UserDB.updateProfile(u);
                    success = "Đã cập nhật thông tin cá nhân và ảnh đại diện thành công.";
                }
            } else if ("password".equals(action)) {
                String oldPw = getFormField(req, "oldPassword");
                String newPw = getFormField(req, "newPassword");
                String confirm = getFormField(req, "confirm");

                if (oldPw == null || !PasswordUtil.verify(oldPw, u.getPasswordHash())) {
                    error = "Mật khẩu hiện tại không đúng.";
                } else if (newPw == null || newPw.length() < 6) {
                    error = "Mật khẩu mới tối thiểu 6 ký tự.";
                } else if (!newPw.equals(confirm)) {
                    error = "Mật khẩu nhập lại không khớp.";
                } else {
                    UserDB.updatePassword(u.getId(), PasswordUtil.hash(newPw));
                    success = "Đã đổi mật khẩu thành công.";
                }
            }

            User freshUser = UserDB.selectById(u.getId());
            session.setAttribute(AuthenticationFilter.SESSION_KEY, freshUser);
        } catch (PersistenceException e) {
            log("Loi cap nhat profile", e);
            error = "Lỗi hệ thống, vui lòng thử lại.";
        }

        req.setAttribute("error", error);
        req.setAttribute("success", success);
        req.getRequestDispatcher("/WEB-INF/auth/profile.jsp").forward(req, resp);
    }
}