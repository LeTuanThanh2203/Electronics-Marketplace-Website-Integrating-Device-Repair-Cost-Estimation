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
import java.util.Arrays;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/auth/profile"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,        // 1 MB
        maxFileSize = 5 * 1024 * 1024,          // 5 MB
        maxRequestSize = 10 * 1024 * 1024       // 10 MB
)
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/auth/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        User sessionUser = (User) session.getAttribute(AuthenticationFilter.SESSION_KEY);
        String action = getFormField(req, "action");
        String error = null, success = null;

        try {
            User u = UserDB.selectById(sessionUser.getId());   // lấy dữ liệu mới nhất
            if ("info".equals(action)) {
                String name = getFormField(req, "name");
                String phone = getFormField(req, "phone");
                String location = getFormField(req, "location");
                String removeAvatar = getFormField(req, "removeAvatar");

                if (name == null || name.isEmpty() || name.length() > 100) {
                    error = "Họ tên không hợp lệ.";
                } else if (phone != null && !phone.matches("^[0-9]{9,11}$")) {
                    error = "Số điện thoại phải có 9-11 chữ số.";
                } else {
                    u.setName(name);
                    u.setPhone(SQLUtil.blankToNull(phone));
                    u.setLocation(SQLUtil.blankToNull(location));

                    // Xử lý tệp ảnh đại diện tải lên từ máy tính
                    Part avatarPart = null;
                    try {
                        avatarPart = req.getPart("avatarFile");
                    } catch (Exception ignored) {}

                    if (avatarPart != null && avatarPart.getSize() > 0) {
                        String submittedFileName = avatarPart.getSubmittedFileName();
                        if (submittedFileName != null && !submittedFileName.trim().isEmpty()) {
                            String ext = "";
                            int dotIdx = submittedFileName.lastIndexOf('.');
                            if (dotIdx >= 0) {
                                ext = submittedFileName.substring(dotIdx).toLowerCase();
                            }
                            if (!Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(ext)) {
                                error = "Định dạng ảnh không được hỗ trợ. Vui lòng chọn tệp JPG, JPEG, PNG, WEBP hoặc GIF.";
                            } else if (avatarPart.getSize() > 5 * 1024 * 1024) {
                                error = "Kích thước ảnh đại diện không được vượt quá 5MB.";
                            } else {
                                String uniqueFileName = "avatar_" + u.getId() + "_" + System.currentTimeMillis() + ext;
                                String uploadDirPath = req.getServletContext().getRealPath("/uploads/avatars");
                                File uploadDir;
                                if (uploadDirPath != null) {
                                    uploadDir = new File(uploadDirPath);
                                } else {
                                    String catalinaBase = System.getProperty("catalina.base", System.getProperty("user.dir"));
                                    uploadDir = new File(catalinaBase, "webapps" + req.getContextPath() + "/uploads/avatars");
                                }
                                if (!uploadDir.exists()) {
                                    uploadDir.mkdirs();
                                }
                                File targetFile = new File(uploadDir, uniqueFileName);
                                avatarPart.write(targetFile.getAbsolutePath());

                                u.setAvatar(req.getContextPath() + "/uploads/avatars/" + uniqueFileName);
                            }
                        }
                    } else if ("true".equalsIgnoreCase(removeAvatar)) {
                        u.setAvatar(null);
                    }

                    if (error == null) {
                        UserDB.updateProfile(u);
                        success = "Đã cập nhật thông tin cá nhân.";
                    }
                }
            } else if ("password".equals(action)) {
                String oldPw = getFormField(req, "oldPassword");
                String newPw = getFormField(req, "newPassword");
                String confirmPw = getFormField(req, "confirm");
                if (oldPw == null || !PasswordUtil.verify(oldPw, u.getPasswordHash())) error = "Mật khẩu hiện tại không đúng.";
                else if (newPw == null || newPw.length() < 6) error = "Mật khẩu mới tối thiểu 6 ký tự.";
                else if (!newPw.equals(confirmPw)) error = "Mật khẩu nhập lại không khớp.";
                else {
                    UserDB.updatePassword(u.getId(), PasswordUtil.hash(newPw));
                    success = "Đã đổi mật khẩu.";
                }
            }
            session.setAttribute(AuthenticationFilter.SESSION_KEY, UserDB.selectById(u.getId()));
        } catch (PersistenceException e) {
            log("Loi cap nhat profile", e);
            error = "Lỗi hệ thống, vui lòng thử lại.";
        }
        req.setAttribute("error", error);
        req.setAttribute("success", success);
        req.getRequestDispatcher("/WEB-INF/auth/profile.jsp").forward(req, resp);
    }

    private String getFormField(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val != null) {
            String trimmed = val.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
        try {
            Part p = req.getPart(name);
            if (p != null) {
                try (var is = p.getInputStream()) {
                    String str = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                    return str.isEmpty() ? null : str;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}