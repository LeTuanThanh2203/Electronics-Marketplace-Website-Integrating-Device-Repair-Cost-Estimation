package com.electronics.marketplace.util;


/** Hàm tiện ích nhỏ. Bảng được Hibernate tự tạo từ Entity nên không cần SQL ở đây. */
public class SQLUtil {
    /** Chuỗi rỗng -> null (để lưu cột nullable).
     * @param s
     * @return  */
    public static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}