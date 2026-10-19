package com.electronics.marketplace.controller.pub;

/**
 * Tên package {@code public} không phải là định danh Java hợp lệ nên file cũ
 * {@code controller/public/ListingServlet.java} chỉ là scaffolding rỗng, không
 * map URL nào và chặn toàn bộ {@code mvn compile}.
 *
 * <p>Servlet chợ đồ cũ thật nằm ở nhánh Thành
 * ({@code controller/pub/ListingServlet}, URL {@code /listing/*}) và sẽ được
 * thêm vào khi merge. File này chỉ giữ package {@code controller.pub} tồn tại
 * để bản vá của module Repair không phải đợi nhánh đó.</p>
 */
final class PackageMarker {
    private PackageMarker() {
    }
}
