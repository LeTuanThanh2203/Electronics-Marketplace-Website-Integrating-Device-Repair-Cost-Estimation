package com.electronics.marketplace.DBConnection;

import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.UserStatus;
import com.electronics.marketplace.util.PasswordUtil;

public class AuthDB {
    /**
     * Trả về User nếu email + mật khẩu đúng (kể cả SUSPENDED để servlet báo lỗi riêng).
     * Trả về null nếu sai thông tin hoặc tài khoản đã DELETED.
     */
    public static User authenticate(String email, String password) {
        User u = UserDB.selectByEmail(email);
        if (u == null || u.getStatus() == UserStatus.DELETED) return null;
        return PasswordUtil.verify(password, u.getPasswordHash()) ? u : null;
    }
}