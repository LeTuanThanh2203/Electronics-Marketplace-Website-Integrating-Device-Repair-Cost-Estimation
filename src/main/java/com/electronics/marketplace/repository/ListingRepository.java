package com.electronics.marketplace.repository;

import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.enums.ListingCondition;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface Repository quản lý các tác vụ truy xuất dữ liệu cho bài đăng Chợ đồ cũ.
 * Tuân thủ Rule 2: Cấu trúc hóa truy xuất CSDL qua tầng DAO/Repository.
 * Dùng Long id theo quy định chuẩn của dự án.
 */
public interface ListingRepository {

    /**
     * Lấy toàn bộ các bài đăng khả dụng trên sàn theo BR-15:
     * Điều kiện: status == AVAILABLE && isApproved == true && isRemovedByAdmin == false.
     */
    List<Listing> findAllAvailable();

    /**
     * Tìm kiếm và lọc danh sách bài đăng theo nhiều tiêu chí kết hợp.
     *
     * @param keyword    Từ khóa tìm kiếm (theo tên máy, model hoặc mô tả)
     * @param brand      Thương hiệu (thinkpad, dell, apple, asus, hp...)
     * @param condition  Tình trạng (DAMAGED, USED_FAIR, USED_GOOD)
     * @param minPrice   Giá tối thiểu
     * @param maxPrice   Giá tối đa
     * @param ram        Dung lượng RAM lọc (8, 16, 32...)
     * @return Danh sách bài đăng thỏa mãn
     */
    List<Listing> searchAndFilter(String keyword, String brand, ListingCondition condition, 
                                  BigDecimal minPrice, BigDecimal maxPrice, String ram);

    /**
     * Tìm bài đăng theo ID
     */
    Listing findById(Long id);

    /**
     * Tìm danh sách các bài đăng của một người bán cụ thể
     */
    List<Listing> findBySellerId(Long sellerId);

    /**
     * Lưu mới một bài đăng
     */
    Listing save(Listing listing);

    /**
     * Cập nhật thông tin bài đăng
     */
    Listing update(Listing listing);

    /**
     * Xóa bài đăng theo ID
     */
    boolean delete(Long id);
}
