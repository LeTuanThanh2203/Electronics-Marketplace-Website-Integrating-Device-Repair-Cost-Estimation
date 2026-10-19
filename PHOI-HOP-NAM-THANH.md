# Đồng bộ & Hợp đồng phối hợp: Listing (Thành) ↔ Order (Nam)
> **Tài liệu đối chiếu:** Đã đối soát chéo với `PHOI-HOP-THANH.md` và các commit mới nhất trên `origin/Nam` (`68eb932`, `7f16b28`, `a86e2d2`, `3664f24`).  
> **Phiên bản:** v3.0 (Cập nhật ngày 2026-10-19 — Bổ sung Lịch sử sửa đổi & Đối soát tiến độ thực tế).  
> **Nguyên tắc:** Mỗi người một Servlet và Service riêng. Giao tiếp qua URL, trạng thái Database và Entity chuẩn hóa, không sửa đè file của nhau.

---

## 1. LỊCH SỬ SỬA ĐỔI & TRẠNG THÁI CODE CỦA THÀNH (CHANGELOG / AUDIT LOG)

Để lưu lại toàn bộ lịch sử các mục Thành đã xử lý dứt điểm theo góp ý từ Nam và Agent Rules:

| STT | Hạng mục đã sửa | Chi tiết thay đổi | Trạng thái hiện tại |
|---|---|---|---|
| **01** | **Sửa 8 lỗi compile trong `MarketplaceServiceTest.java`** | Đổi toàn bộ các tham số ID dạng số nguyên literal (`1`, `999`, `10`) sang kiểu `Long` (`1L`, `999L`, `10L`) tại các dòng 58, 60, 68, 70, 87, 95, 98, 99. | **ĐÃ HOÀN TẤT**<br>`mvn test` đạt **10/10 PASS**, 0 failure, 0 error. |
| **02** | **Xóa Scriptlet trong `index.jsp`** | Gỡ bỏ đoạn scriptlet Java `<% response.sendRedirect(...); %>`. Thay thế bằng thẻ JSTL chuẩn `<c:redirect url="/home" />` theo đúng **Rule 1** và mục 7 của `PHOI-HOP-THANH.md`. | **ĐÃ HOÀN TẤT**<br>Tuân thủ nghiêm ngặt chuẩn JSTL/EL. |
| **03** | **Mở khóa toàn bộ Blocker Entity cho Nam** | Khởi tạo và ánh xạ JPA Field-based hoàn chỉnh: `Listing.java`, `Device.java`, `ListingImage.java`, `ListingProblem.java`, `RepairProblem.java`, enum `ListingStatus.java`, enum `ListingCondition.java`. | **ĐÃ HOÀN TẤT**<br>Nam đã nhận vào commit `68eb932`. |
| **04** | **Cắm nút "Chốt mua" trên View** | Thêm form POST chuẩn sang `${pageContext.request.contextPath}/order?action=create` kèm `<input type="hidden" name="listingId" ...>` trong `views/listing/detail.jsp:L338-L345`. | **ĐÃ HOÀN TẤT**<br>Sẵn sàng cho `OrderServlet` tiếp nhận. |
| **05** | **Khớp tuyến đường điều hướng Menu** | `ListingServlet.java` đã bổ sung nhánh bắt `action=marketplace` và đường dẫn `/marketplace` để khớp với `_header.jspf` của Nam, chống lỗi 404. | **ĐÃ HOÀN TẤT**<br>Khớp 100% với header của Nam. |
| **06** | **Thống nhất `User.java` (Enum thay vì String)** | Đồng ý kế thừa cách khai báo Enum của Nam: `@Enumerated(EnumType.STRING) private Role role;` và `@Enumerated(EnumType.STRING) private UserStatus status;`. | **ĐÃ ĐỒNG THUẬN**<br>Sẽ lấy bản của Nam khi merge. |
| **07** | **Thống nhất `persistence.xml` & `JpaConfig.java`** | Đồng ý chuyển sang cơ chế tự quét classpath của Nam thông qua `JpaConfig.java` (đọc biến môi trường, không commit mật khẩu). | **ĐÃ ĐỒNG THUẬN**<br>Sẽ lấy bản của Nam khi merge. |
| **08** | **Thống nhất `web.xml` & `pom.xml`** | Bỏ khai báo `<taglib>` thừa; giữ UTF-8, session HttpOnly, error-page 404/500 của Nam; chốt compiler Java 17; bỏ 3 dependency trùng. | **ĐÃ ĐỒNG THUẬN**<br>Sẽ lấy bản của Nam khi merge. |

---

## 2. Tiến độ thực tế trên nhánh `origin/Nam` (Đã rà soát commit)

Qua kiểm tra lịch sử Git trên nhánh `origin/Nam`, Nam đã hoàn thành các hạng mục lớn sau:
1. **Commit `68eb932`:** Đã kéo toàn bộ Entity của Thành về (`Listing`, `Device`, `ListingProblem`...) và map quan hệ `@ManyToOne Listing listing` trong `OrderItem.java`.
2. **Commit `7f16b28`:** Đã hoàn thiện toàn bộ `CartService` + `OrderService` và 4 Servlet (`CartServlet`, `CheckoutServlet`, `OrderServlet`, `PaymentServlet`), kèm `OrderServiceTest` đạt chuẩn.
3. **Commit `a86e2d2`:** Bổ sung tài liệu phối hợp `PHOI-HOP-TAN.md` chốt session `currentUser`.
4. **Commit `3664f24`:** Tích hợp cổng thanh toán MoMo thật (`MomoGateway`), ký số HMAC-SHA256 cho đơn hàng `NEW_DEVICE`.

---

## 3. Phân chia trách nhiệm & Hợp đồng URL

| Hạng mục | Thành (Chợ Đồ Cũ & Core UI) | Nam (Giỏ Hàng & Đơn Hàng) |
|---|---|---|
| **Servlet Controller** | `/listing` → `ListingServlet`<br>`/home` → `HomeServlet` | `/order` → `OrderServlet`<br>`/cart` → `CartServlet`<br>`/checkout` → `CheckoutServlet`<br>`/payment` → `PaymentServlet` |
| **Service Logic** | `MarketplaceService` | `CartService`, `OrderService`, `MomoGateway` |
| **Entity & Enums** | `Listing`, `ListingImage`, `ListingProblem`, `Device`, `RepairProblem`<br>Enums: `ListingStatus`, `ListingCondition` | `Order`, `OrderItem`, `Cart`, `CartItem`, `Payment`<br>Enums: `OrderStatus`, `OrderType`, `PaymentStatus`, `Role`, `UserStatus` |
| **Giao diện JSP** | `views/home.jsp`, `views/listing/*`<br>`ui_stitch.html` | `views/cart/cart.jsp`, `views/order/*`<br>`css/order.css` |

---

## 4. Giao điểm tạo Order MARKETPLACE ("Chốt mua")

### A. Phía Thành (Client gửi request)
Trên `detail.jsp`, nút bấm gửi request nguyên thủy:
```html
<form method="post" action="${pageContext.request.contextPath}/order?action=create">
    <input type="hidden" name="listingId" value="${listing.id}">
    <button type="submit">CHỐT MUA MÁY (GIỮ CHỖ)</button>
</form>
```

### B. Phía Nam (Server xử lý trong `OrderService`)
1. **Chống tranh mua (BR-20):** Thực hiện atomic conditional UPDATE:
   ```sql
   UPDATE listings 
   SET status = 'RESERVED', reserved_by_order_id = ?, reserved_at = ?
   WHERE id = ? AND status = 'AVAILABLE' AND is_approved = true AND is_removed_by_admin = false
   ```
   Nếu `affected rows == 0` $\to$ Trả HTTP **409 Conflict** (`LISTING_NOT_AVAILABLE`).
2. **Chặn quota Buyer (BR-31/32):** Kiểm tra số đơn hàng active $\ge 3$ $\to$ Trả HTTP **409 Conflict** (`MAX_ACTIVE_LISTING_ORDERS_REACHED`).
3. **Đồng bộ trạng thái `Listing`:**
   - Order hoàn thành (`COMPLETED`) $\to$ Cập nhật `Listing.status = 'SOLD'` (BR-18).
   - Order bị hủy (`CANCELLED`) $\to$ Cập nhật hoàn `Listing.status = 'AVAILABLE'`, set `reserved_by_order_id = null`, `reserved_at = null` (BR-21).

---

## 5. Quy trình 5 bước Merge nhánh không Conflict

```
Bước 1: Thành commit & push các thay đổi:
        - MarketplaceServiceTest (đã fix Long id, test xanh 10/10)
        - index.jsp (đã bỏ scriptlet, dùng c:redirect)
        - Các file PHOI-HOP-*.md

Bước 2: Chuẩn bị Merge origin/Nam vào Thanh:
        - .gitignore                  --> Lấy của Thành (đầy đủ *.log, Thumbs.db)
        - persistence.xml             --> Lấy của Nam (nạp động qua JpaConfig)
        - web.xml                     --> Lấy của Nam (có error-page và HttpOnly)
        - pom.xml                     --> Lấy của Nam (giữ Java 17, bỏ 3 dep trùng)
        - User.java, OrderItem.java   --> Lấy của Nam (đầy đủ Enum Role, UserStatus)
        - Listing.java, Device.java   --> Lấy của Thành
        - Toàn bộ thư mục views/listing và home.jsp --> Lấy của Thành

Bước 3: Chạy 'mvn test' --> Xác nhận toàn bộ test suite của cả Nam và Thành cùng XANH.

Bước 4: Xóa các file jar thủ công trong 'WEB-INF/lib/*.jar' để tránh xung đột classpath Tomcat.

Bước 5: Push nhánh hợp nhất lên remote repository.
```
