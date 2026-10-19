package com.electronics.marketplace.entity.enums;

public enum ListingStatus {
    PENDING_APPROVAL, // Mới đăng, chờ Admin (Tân) duyệt
    APPROVED,         // Admin đã duyệt -> Hiển thị công khai trên chợ
    REJECTED,         // Vi phạm / Từ chối duyệt
    SOLD,             // Đã bán
    HIDDEN            // Người bán tự ẩn
}

