# HỒ SƠ PHỐI HỢP DỰ ÁN — NAM (GIỎ HÀNG, ĐƠN HÀNG & CỔNG MOMO)

> **Thành viên:** Huỳnh Thuyên Nam  
> **Phân hệ phụ trách:** Giỏ Hàng Điện Tử (`Cart`), Quy trình Đặt Hàng & Quản Lý Đơn Hàng (`Order`), Thuật toán Chống Tranh Mua Hàng C2C (Concurrency BR-20), Tích hợp Cổng Thanh Toán MoMo Sandbox.  
> **Nhánh Git:** `origin/Nam`  
> **Trạng thái:** Tích hợp hoàn tất, 3/3 Unit Tests `MomoGatewayTest` PASS, luồng mua sắm thông suốt.

---

## 1. CÁC ĐIỀU CHỈNH PHỐI HỢP (ĐÃ HOÀN TẤT & ĐỒNG BỘ)

* **Giải thuật Chống Tranh Mua Hàng C2C Độc Bản (Concurrency Control — BR-20):**
  - Khi nhận yêu cầu "Chốt mua" từ bài đăng Chợ Đồ Cũ của Thành, `OrderService` thực thi câu lệnh conditional update nguyên tử:
    ```sql
    UPDATE listings 
    SET status = 'RESERVED', reserved_by_order_id = ?, reserved_at = ?
    WHERE id = ? AND status = 'AVAILABLE' AND is_approved = true AND is_removed_by_admin = false
    ```
  - Nếu `affected_rows == 0` (đã có người khác giữ chỗ trước hoặc tin chưa duyệt) $\to$ Trả ngay mã lỗi HTTP **409 Conflict** (`LISTING_NOT_AVAILABLE`).
  - Kiểm soát hạn ngạch người mua (BR-31/BR-32): Nếu buyer đang có $\ge 3$ đơn hàng C2C chưa hoàn tất $\to$ Trả HTTP **409 Conflict** (`MAX_ACTIVE_LISTING_ORDERS_REACHED`).
* **Đồng bộ Vòng đời Trạng thái Bài Đăng với Thành Lê:**
  - Khi đơn hàng hoàn tất thanh toán/giao nhận (`OrderStatus.COMPLETED`) $\to$ Tự động cập nhật `Listing.status = ListingStatus.SOLD` (BR-18).
  - Khi đơn hàng bị hủy (`OrderStatus.CANCELLED`) $\to$ Tự động hoàn lại `Listing.status = ListingStatus.AVAILABLE` và xóa cờ `reserved_by_order_id = null` (BR-21).
* **Kết nối Luồng Thêm Giỏ Hàng với Phước Anh (Máy Mới B2C):**
  - `CartServlet` tiếp nhận request `POST /cart?action=add` kèm `productId` từ `detail.jsp` của Phước Anh.
  - Áp dụng quy tắc BR-45: Mỗi máy tính xách tay trong giỏ luôn có `quantity = 1`; nếu sản phẩm đã có trong giỏ thì không cộng dồn.
  - Thêm thành công tự động redirect về trang xem giỏ `${ctx}/cart`.
* **Tích hợp Cổng Thanh Toán MoMo Sandbox Thật:**
  - `MomoGateway` triển khai ký số HMAC-SHA256, gửi request thanh toán sang API MoMo, nhận `payUrl` và điều hướng khách hàng quét mã QR MoMo an toàn.
  - Phục hồi đầy đủ Enum `PaymentMethod`: `QR`, `MOMO`, `BANK`.
* **Cải tiến Trải nghiệm Khách vãng lai:**
  - Khách chưa đăng nhập bấm *"Thêm vào giỏ hàng"* hoặc *"Mua ngay"* trên trang máy mới sẽ được tự động chuyển hướng sang `/login` thay vì văng trang trắng lỗi `401 Unauthorized`.

---

## 2. CÁC TÌNH TRẠNG CONFLICT CÓ THỂ XẢY RA & GIẢI PHÁP

| Điểm Xung Đột | Nguyên Nhân Phát Sinh | Giải Pháp Đã Thực Thi |
|---|---|---|
| **Conflict Modify/Delete làm rỗng 14 file lõi** | Commit `3782371` của Tân từng chạy `git revert` làm mất toàn bộ mã nguồn của Nam (`CartService`, `OrderService`, `MomoGateway`, `cart.jsp`...). | **Khôi phục nguyên vẹn 100%:** Kéo lại toàn bộ các file từ `origin/Nam`. |
| **Enum `PaymentMethod.java` bị rỗng** | Nhánh khác vô tình xóa hết các hằng số trong Enum này khiến `OrderServiceTest` không compile được. | Khôi phục đầy đủ 3 hằng số: `QR`, `MOMO`, `BANK`. |
| **Xung đột kiểu liên kết trong `OrderItem.java`** | Bản ban đầu dùng `Long listingId`, sau đổi sang `@ManyToOne Listing listing`. | Giữ quan hệ `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "listing_id") private Listing listing;` chuẩn JPA. |
| **Xung đột `User.java` (String vs Enum)** | Thành ban đầu dùng `String role/status`; Nam dùng Enum `Role` và `UserStatus`. | Giữ nguyên các Enum của Nam; nạp chồng thêm constructor có tham số cho Thành để tương thích bộ test. |
| **Lỗi 500 khi click vào Giỏ Hàng (`/cart`)** | Lệch khoảng trắng `contentType="text/html; charset=UTF-8"` giữa `header.jspf` và `cart.jsp` khiến Jasper compiler ném lỗi. | Xóa bỏ directive `contentType` trong `header.jspf`. |

---

## 3. YÊU CẦU PHỐI HỢP & CHECKLIST HÀNH ĐỘNG

### Đường dẫn kiểm thử luồng Thương mại:
* Xem giỏ hàng: `http://localhost:8080/cart`
* Tiến hành đặt hàng: `http://localhost:8080/checkout`
* Cổng thanh toán đơn: `http://localhost:8080/payment?orderId=...`
* Lịch sử đơn hàng: `http://localhost:8080/order`

### Đối với các thành viên khác:
- [ ] **Thành Lê (C2C):** Nút "CHỐT MUA MÁY" trên `listing/detail.jsp` gửi form POST chính xác tới `${ctx}/order?action=create` kèm `<input type="hidden" name="listingId" value="${listing.id}">`.
- [ ] **Phước Anh (Catalog):** Nút "THÊM GIỎ HÀNG" trên `product/detail.jsp` gửi form POST chính xác tới `${ctx}/cart?action=add` kèm `productId`.
- [ ] **Tân (Auth):** Lưu đối tượng `User` trong session key `"currentUser"` để `CartServlet` và `OrderServlet` lấy ID người mua gán vào đơn.

### Việc Nam cần duy trì:
- [x] Đảm bảo cập nhật trạng thái `Listing` (`RESERVED` $\to$ `SOLD` hoặc `AVAILABLE`) đồng bộ với trạng thái đơn hàng.
- [x] Không sửa đổi các thuộc tính của `Product` và `Listing` của các bạn khác.
- [x] Duy trì 3/3 Unit Tests `MomoGatewayTest` PASS trước mọi lần hợp nhất code.
