# HỒ SƠ PHỐI HỢP DỰ ÁN — TÂN (AUTH, SECURITY & ADMIN DASHBOARD)

> **Thành viên:** Nguyễn Cảnh Tân  
> **Phân hệ phụ trách:** Xác thực (Login/Register), Phân quyền RBAC, Hồ sơ cá nhân (Profile), Bộ lọc bảo mật (`AuthenticationFilter`), Admin Quản trị Người dùng & Duyệt bài sàn C2C.  
> **Nhánh Git:** `origin/Tan`  
> **Trạng thái:** Tích hợp hoàn tất, đồng bộ kiến trúc JPA chung của toàn đội.

---

## 1. CÁC ĐIỀU CHỈNH PHỐI HỢP (ĐÃ HOÀN TẤT & ĐỒNG BỘ)

* **Chuẩn hóa Session Key toàn hệ thống:**
  - Thống nhất dùng duy nhất session attribute **`"currentUser"`** (lưu đối tượng `com.electronics.marketplace.entity.User`).
  - Xóa bỏ hoàn toàn key cũ `"user"` ở 7 vị trí (trong Servlets, JSP và Filters).
  - Cung cấp các tiện ích EL sẵn có cho JSP: `${currentUser.name}`, `${currentUser.email}`, `${currentUser.admin}`, `${currentUser.initial}`, `${currentUser.status}`.
* **Hỗ trợ Dual URL Pattern (Chống lỗi 404):**
  - Đồng bộ Servlets để tiếp nhận cả 2 kiểu đường dẫn (từ navbar cũ lẫn mới):
    - Đăng nhập: `/login` và `/auth/login` (`LoginServlet`)
    - Đăng ký: `/register` và `/auth/register` (`RegisterServlet`)
    - Hồ sơ: `/profile` và `/auth/profile` (`ProfileServlet`)
    - Đăng xuất: `/logout` và `/auth/logout` (`LogoutServlet`)
    - Quản trị: `/admin`, `/admin/dashboard`, `/admin/users` (`AdminServlet`)
* **Nâng cấp Bộ lọc Bảo mật (`AuthenticationFilter`):**
  - Mở quyền truy cập công khai: Khách vãng lai xem trang chủ (`/home`), xem chợ cũ (`GET /listing`), xem máy mới (`GET /products`), và xem giỏ hàng trống (`GET /cart` không action).
  - Bắt buộc đăng nhập với: `/profile`, `/admin`, `/admin/*`, `/checkout`, `/order`, `/payment`, `/listing?action=create`, và `/cart` khi có thao tác POST.
  - **Xử lý thông minh cho AJAX:** Nếu request có Header `Accept: application/json` (như nút xóa món trong `cart.jsp`), Filter trả về **HTTP 401 JSON** `{"ok":false,"code":"UNAUTHENTICATED"}` thay vì `sendRedirect` (tránh làm sập Fetch API).
* **API Admin Duyệt Tin cho Thành Lê:**
  - `AdminServlet` cung cấp 2 endpoint điều khiển:
    - `action=approveListing&listingId=...` $\to$ Cập nhật `isApproved = true`, đưa tin lên sàn.
    - `action=rejectListing&listingId=...` $\to$ Cập nhật từ chối và lý do `rejectionReason`.
* **Kiến trúc CSS Phân tầng & Tinh giản Form:**
  - Tách bạch rõ ràng: `style.css` (tokens chung dự án), `auth.css` (riêng form Login/Register/Profile có namespacing `.auth-*`, `.profile-*`), và `admin.css` (bảng quản trị `.admin-*`).
  - Gỡ bỏ các tem nhãn bảo mật rườm rà và icon đè chữ placeholder; thêm nút con mắt ẩn/hiện mật khẩu và nút nạp nhanh tài khoản mẫu.
* **Nâng cấp Chọn Ảnh & Xem Trước (Live Preview) trong Hồ Sơ:**
  - Bỏ trường nhập URL ảnh đại diện truyền thống.
  - Cho phép người dùng chọn tệp ảnh từ máy tính (`avatarFile`), hiển thị xem trước tức thì (Live Preview) cả ở form cập nhật và avatar lớn trên thanh sidebar trước khi bấm Lưu.
  - Hỗ trợ xóa ảnh đại diện quay về ký tự viết tắt mặc định.
* **Vệ sinh Codebase:**
  - Xóa bỏ 2 file scaffolding JAX-RS không dùng: `HelloApplication.java` và `HelloResource.java`.
  - Thay thế hàm `isBlank()` bằng `trim().isEmpty()` ở 4 vị trí để tương thích bytecode.
  - Bỏ hardcode email/mật khẩu Admin, chuyển sang nạp từ biến môi trường qua `AdminSeedListener`.

---

## 2. CÁC TÌNH TRẠNG CONFLICT CÓ THỂ XẢY RA & GIẢI PHÁP

| Điểm Xung Đột | Nguyên Nhân Phát Sinh | Giải Pháp Đã Thực Thi |
|---|---|---|
| **Conflict Modify/Delete làm rỗng 14 file của Nam** | Commit `3782371` của Tân từng chạy lệnh revert `f0769f6`, vô tình xóa mất toàn bộ mã nguồn Giỏ hàng, Đơn hàng, MoMo của Nam. | **Khôi phục 100%:** Kéo lại toàn bộ 14 file lõi từ `origin/Nam` (`CartService`, `OrderService`, `PaymentMethod`, `cart.jsp`, `MomoGateway`...). |
| **Xóa mất constructor của `User.java`** | Tân chuyển sang dùng Enum `Role`/`UserStatus` nhưng xóa mất constructor có tham số, khiến `MarketplaceServiceTest` của Thành không compile được. | Giữ toàn bộ Enum và lifecycle annotations của Tân, nạp chồng lại constructor `User(id, name, email, phone, location)`. |
| **Lỗi compile `MailUtilGmail.java`** | Tomcat 11 là Servlet Container thuần, thiếu thư viện Mail Jakarta EE dẫn đến 19 lỗi biên dịch `package jakarta.mail does not exist`. | Bổ sung 2 dependency chuẩn vào `pom.xml`: `jakarta.mail-api:2.1.3` và `angus-mail:2.0.3` (runtime). |
| **Mâu thuẫn cấu hình `persistence.xml`** | Tân bỏ danh sách `<class>` để Hibernate tự quét, nhưng gây thiếu Entity trên một số web server. | Khai báo tường minh đủ 16 thực thể của 5 phân hệ, kết hợp nạp biến môi trường DB qua `JpaConfig`. |
| **Lỗi trang 404/500 điều hướng vòng lặp** | `web.xml` khai báo trỏ tới file `/views/common/error_404.jsp` không có thật trên ổ đĩa. | Tạo mới và cập nhật đường dẫn chính xác: `/WEB-INF/views/error/404.jsp` và `/WEB-INF/views/error/500.jsp`. |

---

## 3. YÊU CẦU PHỐI HỢP & CHECKLIST HÀNH ĐỘNG

### Tài khoản mẫu Tân đã nạp sẵn phục vụ kiểm thử:
* **Tài khoản Quản trị:** Email `admin@marketplace.com` / Mật khẩu `admin123` (Role `ADMIN`)
* **Tài khoản Người dùng:** Email `user@marketplace.com` / Mật khẩu `user123` (Role `USER`)

### Đối với các thành viên khác:
- [ ] **Thành Lê (C2C):** Mọi thao tác cần xác định người đăng hoặc kiểm tra quyền xem SĐT bắt buộc đọc từ `session.getAttribute("currentUser")`.
- [ ] **Nam (Order):** Đọc `currentUser` để gán người mua cho `Cart` và `Order`. Giữ nguyên logic trả lỗi 401 khi chưa đăng nhập.
- [ ] **Cả nhóm:** Luôn kiểm tra quyền Admin qua hàm tiện ích `currentUser.isAdmin()`. Không tự ý tạo lại session attribute tên `"user"`.

### Việc Tân cần duy trì:
- [x] Đảm bảo `AuthenticationFilter` luôn bảo vệ đầy đủ các URL: `/checkout`, `/order`, `/payment`, `/profile`, `/admin`.
- [x] Không can thiệp hoặc ghi đè vào các Entity nghiệp vụ của bạn khác (`Product`, `Listing`, `Order`, `Repair*`).
- [x] Luôn chạy `mvn test` và xác nhận toàn bộ unit test cùng xanh trước khi commit.
