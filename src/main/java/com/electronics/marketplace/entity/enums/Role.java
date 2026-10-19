package com.electronics.marketplace.entity.enums;

/**
 * Quyen o cap HE THONG (SDD 13.2, BR-06).
 * CHI co USER va ADMIN. Buyer/Seller KHONG phai gia tri enum: do la vai tro
 * nghiep vu suy ra tu ngu canh (ai tao Listing = Seller, ai dat Order = Buyer),
 * khong phai cot rieng trong DB.
 */
public enum Role {
    USER,
    ADMIN
}
