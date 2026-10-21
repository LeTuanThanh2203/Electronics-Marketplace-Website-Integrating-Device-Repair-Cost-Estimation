package com.electronics.marketplace.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích băm và kiểm tra mật khẩu bằng BCrypt (tuân thủ Rule bảo mật).
 */
public class PasswordUtil {

    /**
     * Băm mật khẩu thô bằng BCrypt với work factor 10.
     * @param plain mật khẩu thô
     * @return chuỗi đã băm
     */
    public static String hash(String plain) {
        if (plain == null) {
            return null;
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    /**
     * Kiểm tra mật khẩu thô có khớp với chuỗi băm hay không.
     * Hỗ trợ BCrypt và fallback plain-text cho dữ liệu mẫu dev/test.
     * @param plain mật khẩu thô người dùng nhập
     * @param hash chuỗi băm trong CSDL
     * @return true nếu hợp lệ
     */
    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null) {
            return false;
        }
        try {
            if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
                return BCrypt.checkpw(plain, hash);
            }
            return plain.equals(hash);
        } catch (Exception e) {
            return false;
        }
    }
}
