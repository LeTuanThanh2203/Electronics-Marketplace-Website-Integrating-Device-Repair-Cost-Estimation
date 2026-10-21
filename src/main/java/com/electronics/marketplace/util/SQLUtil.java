package com.electronics.marketplace.util;

/**
 * Tiện ích xử lý chuỗi và dữ liệu SQL / Form.
 */
public class SQLUtil {

    /**
     * Chuyển chuỗi rỗng thành null (hỗ trợ lưu trường nullable vào CSDL).
     * @param s chuỗi đầu vào
     * @return chuỗi đã trim hoặc null nếu rỗng
     */
    public static String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
