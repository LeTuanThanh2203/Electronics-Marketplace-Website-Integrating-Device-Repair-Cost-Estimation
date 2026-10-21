# 📦 SỔ TAY PHỐI HỢP & XỬ LÝ CONFLICT — LÊ TUẤN THÀNH
> **Phân hệ phụ trách:** Chợ Đồ Cũ C2C (`/listing`), Trang Chủ (`/home`) & UI Core Google Stitch  
> **Thành viên:** Lê Tuấn Thành | **Nhánh Git:** `Thanh` / `integration`  
> **Trạng thái kiểm thử:** ✅ 10/10 Unit Tests PASS (`MarketplaceServiceTest`)

---

## 1. PHẠM VI SỞ HỮU FILE (RANH GIỚI TRÁCH NHIỆM)

| Tầng kiến trúc | Thành sở hữu độc quyền (Chỉ Thành sửa) | Thành viên khác giao tiếp qua |
|---|---|---|
| **Entities & Enums** | `Listing.java`, `ListingImage.java`, `ListingProblem.java`<br>Enums: `ListingStatus.java`, `ListingCondition.java` | Khóa ngoại `@ManyToOne Listing` trong `OrderItem.java` (Nam) |
| **Dùng chung** | `Device.java`, `RepairProblem.java` (Phối hợp với Quốc, Anh) | Gọi hàm getter, không tự ý xóa constructor |
| **Controller** | `ListingServlet.java`, `HomeServlet.java` | URL `/listing`, `/marketplace`, `/home` |
| **Service & Repo** | `MarketplaceService.java`, `ListingRepository.java`, `ListingRepositoryImpl.java` | Giao tiếp qua Interface Service |
| **Giao diện & Style** | `views/home.jsp`, `views/listing/*`, `css/home.css`, `css/listing.css`, `ui_stitch.html` | Layout dùng chung, CSS Design System Google Stitch |

---

## 2. LỊCH SỬ THAY ĐỔI (CHANGELOG)

* **2026-10-19:** Sửa 8 lỗi literal int trong `MarketplaceServiceTest.java` (`1` $\to$ `1L`, `999` $\to$ `999L`) $\to$ Đạt **10/10 Test PASS**.
* **2026-10-19:** Gỡ toàn bộ scriptlet Java `<% %>` trong `index.jsp`, thay bằng `<c:redirect url="/home" />` chuẩn JSTL.
* **2026-10-20:** Thêm nút "Chốt mua" form POST sang `/order?action=create` kèm `listingId` tại `listing/detail.jsp` cho Nam.
* **2026-10-20:** Bổ sung route `/marketplace` và `action=marketplace` trong `ListingServlet.java` khớp với Header menu.
* **2026-10-21:** Hợp nhất thành công vào nhánh `integration` (Commit `333df71`), giữ vững khung UI Stitch và test suite.

---

## 3. MA TRẬN XUNG ĐỘT (CONFLICTS & GIẢI PHÁP)

> [!IMPORTANT]
> Dưới đây là các điểm xung đột kỹ thuật cốt lõi giữa Thành và 4 thành viên cùng cách giải quyết chuẩn:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   BẢN ĐỒ XUNG ĐỘT CỦA THÀNH                            │
├──────────────┬───────────────────────────────┬─────────────────────────────────────────┤
│ Đối tác      │ Điểm xung đột kỹ thuật        │ Giải pháp chốt & File cần sửa           │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Ca Ngọc Tân│ • Xung đột String vs Enum Role │ • Lấy Enum Role/UserStatus từ Tân.      │
│ (Auth/Admin) │ • Mất constructor User(...)    │ • BẮT BUỘC giữ constructor nạp chồng    │
│              │ • Sai link login ở Header     │   trong `User.java` để không gãy Test.  │
│              │ • Khóa duyệt tin (BR-15/19)   │ • Sửa link `header.jspf` về `/login`.   │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Huỳnh Nam │ • Chốt mua C2C (BR-20)        │ • Thành gửi POST `listingId` từ detail. │
│ (Cart/Order) │ • Tranh mua / Khóa chỗ máy    │ • Nam atomic UPDATE status sang RESERVED│
│              │ • File config chung           │ • Nam sở hữu config; Thành không ghi đè │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Phước Anh │ • Cắm AI Recom vào home.jsp   │ • Anh đẩy List sang request attribute.  │
│ (Product)    │ • Tranh chấp `Device.java`    │ • Thành duyệt render <c:forEach> ở Home │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Vương Quốc│ • Signature constructor Device│ • Loại bỏ constructor trùng kiểu dữ liệu│
│ (Repair)     │ • Danh mục lỗi `RepairProblem`│ • Thống nhất method getName() fallback  │
└──────────────┴───────────────────────────────┴─────────────────────────────────────────┘
```

### Chi tiết các điểm xung đột kỹ thuật đã giải quyết:

#### 1. Constructor `User` & Enum Role / Status (Tân & Nam)
- **Vấn đề:** Ban đầu Thành dùng constructor 5 tham số `new User(1L, "Trần Minh Đức", "duc.tran@gmail.com", "0912 888 999", "Quận 1, TP. Hồ Chí Minh")` cho seed in-memory. Khi Nam & Tân map `User` thành `@Entity` với `Role` và `UserStatus`, các bài test và mapper bị xung đột kiểu.
- **Giải pháp:** Trong `User.java`, duy trì constructor linh hoạt đồng thời gán `Role.USER` và `UserStatus.ACTIVE` mặc định. Các hàm tạo mới của Thành cũng tự động set đầy đủ thuộc tính setter tương thích với cả CSDL thật lẫn kiểm thử tự động.

#### 2. Quan hệ `OrderItem.listing` (Nam)
- **Vấn đề:** Nam từng lưu trường `Long listingId` dạng cột thô.
- **Giải pháp:** Đổi sang quan hệ JPA chuẩn:
  ```java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id")
  private Listing listing;
  ```
  Cột trong DB vẫn là `listing_id`, các hàm đọc gọi `orderItem.getListing().getId()`.

#### 3. Quản lý cấu hình chung (`persistence.xml`, `web.xml`, `pom.xml`)
- **`persistence.xml`:** Nam quản lý cấu hình nạp biến môi trường và Hibernate tự quét entity. Thành không ghi đè thông tin kết nối hardcode.
- **`web.xml`:** Giữ nguyên encoding UTF-8, session cookie HttpOnly và error-pages chuẩn của Nam. Không khai báo lại các thẻ `<taglib>` thừa vì JSTL 3.0.1 đã tự nhận diện.
- **`pom.xml`:** Giữ phiên bản Java 17 và loại bỏ các dependencies trùng lặp.

---

## 4. HƯỚNG DẪN THỰC THI — NÊN SỬA & TRÁNH SỬA FILE NÀO

### 🟢 File Thành NÊN SỬA khi cần điều chỉnh:
* `src/main/webapp/WEB-INF/views/home.jsp`: Khu vực hiển thị đề xuất máy mới và danh sách đồ cũ.
* `src/main/webapp/WEB-INF/views/listing/detail.jsp`: Nút xem số điện thoại (AJAX `/listing/contact` cần bắt đăng nhập BR-33, BR-34) và form gửi mua sang Nam.
* `src/main/webapp/WEB-INF/views/listing/marketplace.jsp`: Bộ lọc, thanh tìm kiếm live search, grid card sản phẩm.
* `src/main/java/com/electronics/marketplace/controller/pub/ListingServlet.java`: Logic lọc, đăng tin, xem chi tiết tin đăng.

### 🔴 File Thành TUYỆT ĐỐI KHÔNG TỰ Ý SỬA:
* `pom.xml`, `persistence.xml`, `JpaConfig.java`: Đã được tích hợp chuẩn từ Nam & Tân.
* `src/main/java/com/electronics/marketplace/entity/User.java`: Chỉ đọc, không đổi kiểu Enum thành String.
* `src/main/java/com/electronics/marketplace/entity/OrderItem.java`: Do Nam quản lý logic đơn hàng.

---

## 5. CHECKLIST KIỂM SOÁT NHANH (QUICK CHECKLIST)
- [x] Session đọc người dùng chuẩn: `(User) session.getAttribute("currentUser")`.
- [x] Chỉ hiển thị tin đồ cũ khi `isApproved == true` và `isRemovedByAdmin == false`.
- [x] Chạy `mvn test` xác nhận `MarketplaceServiceTest` đạt 10/10 PASS trước khi push.
