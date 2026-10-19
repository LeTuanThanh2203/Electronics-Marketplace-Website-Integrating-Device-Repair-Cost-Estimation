package com.electronics.marketplace.entity.enums;

/**
 * Trạng thái của bài đăng tin đồ cũ (Theo BR-15, BR-20, BR-24)
 * AVAILABLE: Đang mở bán công khai trên chợ
 * RESERVED: Đã có người mua đặt đơn (chờ xác nhận/giao nhận)
 * SOLD: Đã hoàn tất giao dịch thanh toán
 * HIDDEN: Người bán chủ động tạm ẩn bài đăng
 */
public enum ListingStatus {
    AVAILABLE("Đang bán"),
    RESERVED("Đã giữ chỗ"),
    SOLD("Đã bán"),
    HIDDEN("Đã ẩn");

    private final String displayName;

    ListingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
