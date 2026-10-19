# Chốt ranh giới & Hợp đồng phối hợp: Chợ Đồ Cũ & UI Core (Thành) ↔ Auth & Admin Dashboard (Tân)
> **Tài liệu đối chiếu:** Đã rà soát các commit thực tế của Tân (`af8b56c`, `3e68d57`, `733691a`, `2b9e8d9` - Tân đã merge `Nam`).  
> **Phiên bản:** v2.0 (Cập nhật sau khi đối soát commit của Tân & Nam).  
> **Nguyên tắc:** Tân quản lý Auth & Admin Dashboard; Thành quản lý Chợ Đồ Cũ & Design System. Giao tiếp qua session `currentUser`, Filter bảo mật và trạng thái duyệt tin trong Database.

---

## 1. TIẾN ĐỘ THỰC TẾ TRÊN NHÁNH `origin/Tan` (ĐÃ RÀ SOÁT)

Tân đã có những bước tiến vượt bậc và hoàn toàn đồng thuận với kiến trúc chung:
1. **Commit `af8b56c`:** Xây dựng đầy đủ phân hệ: `LoginServlet`, `RegisterServlet`, `ProfileServlet`, `AdminServlet`, các Filter (`AuthenticationFilter`, `AuthorizationFilter`, `GzipFilter`), mã hóa `PasswordUtil`, gửi mail `MailUtilGmail`.
2. **Commit `3e68d57`:** Đã đồng bộ session key thành **`"currentUser"`** (thay cho `"user"` cũ) và chuẩn hóa enum **`UserStatus`** (thay cho `Status`).
3. **Commit `733691a`:** Xóa bỏ kết nối JDBC thuần cũ (`DBConnection.java`), đồng bộ hoàn toàn theo `JpaConfig.java` và `persistence.xml` của Nam.
4. **Commit `2b9e8d9`:** **Tân đã merge thành công nhánh `Nam` vào `Tan`!** Điều này đồng nghĩa phân hệ Auth của Tân và phân hệ Order/Cart của Nam đã chạy chung một gốc.

---

## 2. Phân chia trách nhiệm

| Hạng mục | Thành (Chợ Đồ Cũ & UI Core) | Tân (Auth, Phân Quyền & Admin Dashboard) |
|---|---|---|
| **Servlet Controller** | `/listing` → `ListingServlet`, `/home` → `HomeServlet` | `/auth/login` → `LoginServlet`<br>`/auth/register` → `RegisterServlet`<br>`/auth/profile` → `ProfileServlet`<br>`/admin/*` → `AdminServlet` |
| **Service Logic** | `MarketplaceService` | `UserService`, `AuthService` |
| **Filter bảo mật** | Tiếp nhận session và request được filter kiểm tra | `AuthenticationFilter`, `AuthorizationFilter`, `GzipFilter` |
| **Entity & Enums** | `Listing`, `ListingImage`, `ListingProblem`, `Device`, `RepairProblem`<br>Enums: `ListingCondition`, `ListingStatus` | `User`<br>Enums: `Role` (`USER`, `ADMIN`), `UserStatus` (`ACTIVE`, `SUSPENDED`, `DELETED`) |
| **Giao diện (JSP)** | `views/home.jsp`, `views/listing/*`<br>Bản thiết kế Google Stitch (`ui_stitch.html`) | `views/auth/login.jsp`, `views/auth/register.jsp`<br>`views/auth/profile.jsp`, `views/admin/dashboard.jsp` |

---

## 3. Các điểm giao tiếp cốt lõi (Integration Points)

### Giao điểm 1: Quản trị & Duyệt tin chợ đồ cũ trên Admin Dashboard (BR-15, BR-19)

1. **Khế ước trạng thái tin đăng do Thành tạo ra:**
   - Khi Seller đăng bài qua `/listing/form`, `MarketplaceService` khởi tạo:
     - `listing.status = ListingStatus.AVAILABLE`
     - `listing.isApproved = false` *(chờ Admin duyệt theo BR-15)*
     - `listing.isRemovedByAdmin = false`
   - Chỉ tin có `isApproved == true` và `isRemovedByAdmin == false` mới được hiển thị công khai trên `/listing/marketplace`.

2. **Tân hoàn thiện trên `AdminServlet` (`/admin/listings`):**
   - **Xem tin chờ duyệt:** Gọi `ListingRepository.findPendingListings()`.
   - **Duyệt tin (Approve):** Set `isApproved = true`.
   - **Từ chối tin (Reject):** Set `isApproved = false` kèm lý do vào `rejectionReason`.
   - **Gỡ tin vi phạm (Take down - BR-19):** Set `isRemovedByAdmin = true`. **Chỉ Admin mới có quyền gỡ hoặc phục hồi cờ này**; Seller không tự sửa được cờ vi phạm.

---

### Giao điểm 2: Session `currentUser` & Filter bảo mật (BR-33, Rule 5)

1. **Đồng thuận Session Key:**
   Tân đã đổi thành công sang key **`"currentUser"`**. Sau khi đăng nhập:
   ```java
   req.getSession().setAttribute("currentUser", authenticatedUser);
   ```
2. **Các trang của Thành gắn kết với `currentUser`:**
   - **Đăng tin bán máy (`/listing/form`):** Lấy `currentUser` từ session để gán `seller`.
   - **Xem số điện thoại người bán (`/listing/contact` - BR-33, BR-34):**
     - Khách vãng lai bấm xem $\to$ Báo lỗi 401 yêu cầu đăng nhập.
     - User đã đăng nhập $\to$ Endpoint AJAX trả về số điện thoại an toàn.
   - **Sửa tin bán máy (BR-11, BR-40):** Kiểm tra `currentUser.getId().equals(listing.getSeller().getId())`.
3. **Phạm vi bảo vệ của `AuthenticationFilter` của Tân:**
   - Bắt buộc bảo vệ: `/listing/form`, `/order/*`, `/cart/*`, `/checkout/*`, `/payment/*`.
   - Chặn truy cập trái phép, lưu target URL và redirect về `/auth/login`. Đối với request AJAX (`Accept: application/json`), trả mã HTTP **401 Unauthorized** kèm thông báo JSON thay vì redirect HTML.

---

### Giao điểm 3: Chuẩn hóa Design System & Kế thừa Google Stitch

1. Thành đã thiết lập trọn vẹn Design System từ Google Stitch ([`ui_stitch.html`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/ui_stitch.html)) và áp dụng trên [`home.jsp`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/src/main/webapp/WEB-INF/views/home.jsp):
   - Bảng màu: Nền tối/trắng tương phản, điểm nhấn sắc cam công nghệ `#FF4A1C`.
   - Typography: Font Google Inter / Roboto.
   - Header điều hướng thống nhất, Footer chuẩn thương mại điện tử.
2. **Khuyến nghị cho Tân:**
   - Khi hoàn thiện giao diện [`views/auth/login.jsp`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/src/main/webapp/WEB-INF/views/auth/login.jsp), `register.jsp`, `profile.jsp` và `admin/dashboard.jsp`, Tân hãy áp dụng CSS và cấu trúc thẻ từ `home.jsp` của Thành để toàn bộ sàn giao dịch có cùng một phong cách thiết kế cao cấp, đồng bộ 100%.

---

## 4. Checklist công việc phối hợp cho Tân

- [x] Đã đổi session key thành `"currentUser"` (Commit `3e68d57`).
- [x] Đã dùng enum `Role` và `UserStatus` (Commit `3e68d57`).
- [x] Đã xóa kết nối JDBC thuần, dùng `JpaConfig` (Commit `733691a`).
- [x] Đã merge nhánh `Nam` thành công (Commit `2b9e8d9`).
- [ ] Bổ sung URL `/listing/form` vào danh sách URL bảo vệ của `AuthenticationFilter`.
- [ ] Thêm chức năng Quản lý duyệt tin đăng chợ cũ (`approve` / `reject` / `remove`) vào `AdminServlet`.
- [ ] Kế thừa giao diện CSS/Header từ `home.jsp` của Thành cho các trang Auth & Admin.
