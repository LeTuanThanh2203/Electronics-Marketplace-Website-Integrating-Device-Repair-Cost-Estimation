package com.electronics.marketplace.entity.enums;

/**
 * Tình trạng thiết bị đồ cũ (Theo BR-08 đặc tả hệ thống)
 * DAMAGED: Thiết bị có lỗi phần cứng hoặc chức năng cần sửa chữa
 * USED_FAIR: Ngoại hình trầy xước nhẹ hoặc hao mòn theo thời gian (95%)
 * USED_GOOD: Ngoại hình đẹp như mới, chức năng hoàn hảo (99%)
 */
public enum ListingCondition {
    DAMAGED("Hư hỏng", "Thiết bị có lỗi phần cứng hoặc linh kiện cần sửa chữa"),
    USED_FAIR("Tạm ổn (95%)", "Ngoại hình có trầy xước nhẹ theo thời gian"),
    USED_GOOD("Như mới (99%)", "Ngoại hình đẹp, mọi chức năng hoạt động hoàn hảo");

    private final String displayName;
    private final String description;

    ListingCondition(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
