package com.electronics.marketplace.entity.enums;

/**
 * SDD 13.5 / 13.11. Admin quan ly thu cong.
 * Hard constraint cua Availability filter (SDD 8.3) va cua checkout (BR-45):
 * chi CartItem co Product.status = AVAILABLE moi duoc dat hang.
 */
public enum ProductStatus {
    AVAILABLE,
    OUT_OF_STOCK,
    DISCONTINUED
}
