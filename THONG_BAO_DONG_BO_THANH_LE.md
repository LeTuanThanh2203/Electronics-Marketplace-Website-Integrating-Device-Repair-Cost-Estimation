# THÔNG BÁO ĐỒNG BỘ HỆ THỐNG & YÊU CẦU HOÀN THIỆN PHÂN HỆ
> **Người gửi:** Tân (Phụ trách Auth, Security & Admin Dashboard)  
> **Người nhận:** Thành Lê (Phụ trách Chợ Đồ Cũ & UI Core)  
> **Trạng thái:** Nhánh `Tan` đã đồng bộ hoàn chỉnh kiến trúc JPA của Nam và sẵn sàng tích hợp với phân hệ Chợ Đồ Cũ.

---

## 1. CÁC THAY ĐỔI TÂN ĐÃ HOÀN TẤT & ĐỒNG BỘ TRÊN NHÁNH `Tan`

1. **Chuẩn hóa Session Key & User Entity:**
   - Session key chính thức toàn hệ thống: `session.getAttribute("currentUser")` (hoặc hằng số `AuthenticationFilter.SESSION_KEY`).
   - Đối tượng trả về: `com.electronics.marketplace.entity.User`.
   - Các tiện ích có sẵn để Thành dùng trong JSP/JSTL:
     - `${sessionScope.currentUser}`: Kiểm tra đăng nhập.
     - `${currentUser.name}`, `${currentUser.email}`, `${currentUser.phone}`: Thông tin người bán.
     - `${currentUser.admin}`: Boolean kiểm tra quyền quản trị viên.
     - `${currentUser.initial}`: Ký tự viết tắt để làm avatar mặc định.
     - `${currentUser.status}`: Trạng thái tài khoản (`ACTIVE`, `SUSPENDED`, `DELETED`).

2. **Cập nhật Bộ lọc Bảo mật (`AuthenticationFilter`):**
   - Đã cấu hình mở quyền truy cập công khai cho khách xem chợ đồ cũ (`GET /listing`, `GET /listing?action=detail`).
   - Tự động bắt đăng nhập (chuyển hướng về `/login`) khi người dùng thực hiện: Đăng tin mới (`action=create`, `action=save`) hoặc xem tin cá nhân (`action=my-listings`).
   - Thành không cần phải viết lại logic kiểm tra session thủ công trong servlet của mình.

3. **Chốt danh sách Enum Hợp đồng:**
   - `ListingStatus`: `AVAILABLE` (Đang bán), `RESERVED` (Đã giữ chỗ), `SOLD` (Đã bán), `HIDDEN` (Đã ẩn).
   - `ListingCondition`: `DAMAGED` (Hư hỏng), `USED_FAIR` (Tạm ổn 95%), `USED_GOOD` (Như mới 99%).

4. **Sẵn sàng API & Giao diện Admin Duyệt Tin:**
   - Cơ chế duyệt tin theo BR-15 tách biệt với trạng thái giao dịch: dùng cờ `isApproved` và `isRemovedByAdmin`.
   - Phân hệ Admin (`AdminServlet` + `dashboard.jsp`) đã hoàn thiện bảng danh sách tin chờ duyệt và endpoint xử lý duyệt/từ chối:
     - `action=approveListing&listingId=...` -> Đặt `isApproved = true`, `isRemovedByAdmin = false`.
     - `action=rejectListing&listingId=...&reason=...` -> Đặt `isApproved = false`, `isRemovedByAdmin = true`, kèm `rejectionReason`.

---

## 2. NHỮNG PHẦN YÊU CẦU THÀNH LÊ HOÀN THIỆN

Để hệ thống ghép nối mượt mà và không phát sinh lỗi 404/NullPointerException, Thành cần hoàn thiện các mục sau:

### Mục 1: Giao diện Header (`src/main/webapp/WEB-INF/layout/header.jspf`)
* **Đã cập nhật chuẩn:**
  - Link đăng nhập trỏ về: `${ctx}/login`
  - Link đăng ký trỏ về: `${ctx}/register`
  - Link hồ sơ cá nhân: `${ctx}/profile`
  - Link đăng xuất: `${ctx}/logout`
  - Link đăng tin chợ cũ: `${ctx}/listing?action=create`
  - Link quản trị (nếu là admin): `<c:if test="${currentUser.admin}"><a href="${ctx}/admin">Quản trị</a></c:if>`

---

### Mục 2: Entity Bài Đăng (`com.electronics.marketplace.entity.Listing`)
Entity `Listing.java` đã hoàn thiện và đồng bộ đầy đủ các trường:
* `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seller_id", nullable = false) private User seller;`
* `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "device_id", nullable = false) private Device device;`
* `price` (BigDecimal, nullable = false)
* `condition` (Enum `ListingCondition`)
* `status` (Enum `ListingStatus`, mặc định là `AVAILABLE`)
* `isApproved` (Boolean, mặc định là `false` khi người bán đăng tin - chờ Admin duyệt)
* `isRemovedByAdmin` (Boolean, mặc định là `false`)
* `rejectionReason` (String)
* `createdAt`, `updatedAt` (Date)
* `images` (List<ListingImage>), `problems` (List<ListingProblem>)

---

### Mục 3: Triển khai Service & DAO Chợ Đồ Cũ (`MarketplaceService.java`)
Tuân thủ chuẩn kết nối JPA thông qua `DBUtil` (đã cấu hình sẵn trong `com.electronics.marketplace.DBConnection.DBUtil`):
* **Hàm lấy danh sách tin công khai cho Storefront (Theo BR-15):**
  - Điều kiện lọc: Bài đăng phải mở bán công khai `status = ListingStatus.AVAILABLE`, Admin đã duyệt `isApproved = true`, không bị Admin gỡ `isRemovedByAdmin = false` VÀ người bán còn hoạt động `seller.status = UserStatus.ACTIVE`.
  - Không hiển thị bài của user đang bị Admin khóa (`SUSPENDED`).
* **Hàm tạo bài đăng mới:**
  - Nhận `User seller` lấy từ `session.getAttribute("currentUser")`.
  - Đặt trạng thái ban đầu: `status = ListingStatus.AVAILABLE`, `isApproved = false` (chờ Admin duyệt).

---

### Mục 4: Triển khai Servlet Điều Khiển (`ListingServlet.java`)
* Khai báo `@WebServlet("/listing")`.
* Phân phối hành động theo tham số `action`:
  - `action=marketplace` (hoặc rỗng): Lấy danh sách tin duyệt hiển thị ra `marketplace.jsp`.
  - `action=detail&id=...`: Hiển thị chi tiết bài đăng ra `detail.jsp`.
  - `action=create`: Hiển thị form đăng tin `form.jsp` (yêu cầu điền tiêu đề, giá, mô tả, tình trạng máy).
  - `doPost`: Nhận dữ liệu submit, trích xuất `currentUser` từ session gán vào người bán và gọi `MarketplaceService` lưu vào DB.

---

## 3. BẢNG TỔNG KẾT KHỚP NỐI

| Chức năng | URL Endpoint | Trách nhiệm | Ghi chú tích hợp |
|---|---|---|---|
| Đăng nhập / Đăng ký | `/login`, `/register` | **Tân** | Đã xong, dùng BCrypt + Session |
| Hồ sơ cá nhân | `/profile` | **Tân** | Cập nhật tên, SĐT, địa chỉ, đổi pass |
| Xem Chợ Đồ Cũ | `/listing?action=marketplace` | **Thành** | Chỉ hiện tin `APPROVED` |
| Đăng tin Bán Đồ Cũ | `/listing?action=create` | **Thành** | Filter của Tân sẽ tự chặn nếu chưa login |
| Duyệt bài đăng | `/admin` | **Tân** | Tân duyệt tin chờ của Thành |
| Header/Footer chung | `WEB-INF/layout/` | **Thành** | Thành đổi lại URL `/login`, `/register` |

*Mọi thắc mắc về cách lấy Session hoặc cấu hình DBUtil/JPA, Thành có thể tham khảo trực tiếp cách viết mẫu trong `LoginServlet.java` và `UserDB.java` trên nhánh `Tan`.*
