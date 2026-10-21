# HỒ SƠ PHỐI HỢP DỰ ÁN — THÀNH LÊ (CORE UI & CHỢ ĐỒ CŨ)

> **Thành viên:** Lê Tuấn Thành  
> **Phân hệ phụ trách:** Setup Core Dự án, Google Stitch Design System (`home.jsp`, Header/Footer), Sàn Chợ Đồ Cũ C2C (`/listing`, `ListingServlet`, `MarketplaceService`).  
> **Nhánh Git:** `origin/Thanh`  
> **Trạng thái:** Tích hợp hoàn tất, 10/10 Unit Tests `MarketplaceServiceTest` PASS.

---

## 1. CÁC ĐIỀU CHỈNH PHỐI HỢP (ĐÃ HOÀN TẤT & ĐỒNG BỘ)

* **Thiết kế & Layout dùng chung (Google Stitch System):**
  - Hoàn thiện bộ khung chuẩn: `views/home.jsp`, `ui_stitch.html`, `layout/header.jspf`, `layout/footer.jspf`.
  - Tích hợp Dynamic Navbar: Tự động đổi trạng thái hiển thị theo `${sessionScope.currentUser}` (Hiện "Tài khoản" khi chưa login; hiện Tên, Avatar, Badge "Admin" `/admin`, nút "Đăng xuất" khi đã login).
* **Đồng bộ Session Key với Tân:**
  - Chuẩn hóa đọc đối tượng `User` qua session key duy nhất: `"currentUser"`.
  - Form đăng tin (`/listing/form`) tự động gán người bán: `listing.setSeller(currentUser)`.
  - Endpoint AJAX xem số điện thoại người bán (`/listing/contact` — BR-33, BR-34): Bắt buộc kiểm tra đăng nhập, trả HTTP `401 Unauthorized` nếu khách chưa login.
* **Đồng bộ Quy trình Duyệt Tin với Tân (Admin):**
  - Khi người dùng đăng tin mới: Đặt mặc định `status = ListingStatus.PENDING_APPROVAL` (hoặc `isApproved = false`, `isRemovedByAdmin = false`).
  - Trang Chợ Đồ Cũ công khai (`/listing/marketplace`): Chỉ truy vấn các bài đã được Admin duyệt (`isApproved == true` và `isRemovedByAdmin == false`).
* **Liên kết "Chốt mua" C2C với Nam (Order Module):**
  - Trên `views/listing/detail.jsp`: Bọc form POST chuẩn gửi tới `${ctx}/order?action=create` kèm `<input type="hidden" name="listingId" value="${listing.id}">`.
* **Tích hợp Danh mục Lỗi với Quốc (Repair Module):**
  - Form đăng tin lấy danh mục lỗi chuẩn `RepairProblem` của Quốc để người bán tick chọn, lưu vào bảng liên kết `ListingProblem`.
  - `detail.jsp` hiển thị Ma trận lỗi linh kiện (Defect Matrix) minh bạch cho người mua.
* **Chuẩn hóa Mã nguồn & Kiểm thử:**
  - Loại bỏ hoàn toàn scriptlet `<% %>` trong `index.jsp`, thay bằng `<c:redirect url="/home" />`.
  - Sửa toàn bộ literal ID sang kiểu `Long` (`1L`, `999L`) trong `MarketplaceServiceTest`, đưa test suite đạt 10/10 PASS.

---

## 2. CÁC TÌNH TRẠNG CONFLICT CÓ THỂ XẢY RA & GIẢI PHÁP

| Điểm Xung Đột | Nguyên Nhân Phát Sinh | Giải Pháp Đã Thực Thi |
|---|---|---|
| **Nguy cơ mất giao diện JSP của Thành** | Các nhánh khác rẽ từ commit ban đầu `9072b00` trước khi Thành commit UI Stitch. Git tự động coi các file JSP/CSS của Thành là đã bị xóa. | **Bảo toàn tuyệt đối:** Giữ 100% thư mục `views/listing/`, `views/home.jsp`, `css/home.css`, `css/listing.css` từ nhánh `Thanh` làm chuẩn mực sàn giao dịch. |
| **Xung đột `User.java` (String vs Enum & Constructor)** | Thành ban đầu khai báo `String role`, `String status` và constructor 5 tham số. Tân/Nam đổi sang Enum và xóa constructor khiến test của Thành bị lỗi compile. | **Hợp nhất đa hình:** Lấy Enum `Role` và `UserStatus` của Tân/Nam; đồng thời nạp chồng lại constructor `User(Long id, String name, String email, String phone, String location)`. |
| **Xung đột `Device.java`** | Thành cần `name`, `brand`, `model` và constructor có ID; Quốc xóa `name` thay bằng `series`; Anh thêm `deviceType`. Trùng kiểu tham số 7 chuỗi gây lỗi compiler. | **Hợp nhất Constructor & Getter Fallback:** Giữ cả `name` lẫn `series`. Hàm `getName()` tự fallback: trả `name` nếu có, ngược lại gọi `getDisplayName()`. Giữ constructor có `id` cho Thành. |
| **Xung đột Enum `ListingStatus.java`** | Thành & Nam cần `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN` cho luồng đơn hàng; Tân đổi thành `PENDING_APPROVAL`, `APPROVED`, `REJECTED` cho Admin. | **Hợp nhất đầy đủ 7 giá trị:** `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN`. |
| **Xung đột `RepairProblem.java`** | Quốc đổi tên cột thành `problem_id` và xóa constructor `(Long, String)` khiến `ListingRepositoryImpl` không compile được. | Giữ DB mapping của Quốc nhưng nạp chồng lại constructor `RepairProblem(Long id, String name)`; hỗ trợ cả `isActive()` lẫn `getIsActive()`. |
| **Lỗi Jasper thiếu Taglib `fn` (HTTP 500)** | Header dùng `${fn:length(...)}` nhưng thiếu thư viện `jakarta.tags.functions`. | Trích xuất `fn.tld` vào `WEB-INF/tld/` và khai báo `<taglib>` trong `web.xml`. |
| **Lỗi xung đột `contentType` (HTTP 500)** | `header.jspf` và `cart.jsp` lệch khoảng trắng trong directive `contentType`. | Xóa bỏ directive `contentType` khỏi `header.jspf` (vì file nhúng tĩnh `.jspf` không được khai báo lại). |

---

## 3. YÊU CẦU PHỐI HỢP & CHECKLIST HÀNH ĐỘNG

### Đối với các thành viên khác:
- [ ] **Tân (Auth & Admin):** Duy trì session key `"currentUser"`. Khi Admin duyệt/từ chối bài trên `/admin`, cập nhật đúng `isApproved` để tin xuất hiện trên Chợ Đồ Cũ.
- [ ] **Nam (Order):** Khi xử lý "Chốt mua" (`/order?action=create`), áp dụng conditional update chống tranh mua (BR-20). Khi đơn hoàn tất cập nhật `Listing.status = SOLD`; khi đơn hủy hoàn lại `AVAILABLE`.
- [ ] **Phước Anh (Catalog):** Nạp danh sách máy mới gợi ý vào request attribute `recommendedProducts` để khối "Gợi Ý Tối Ưu" trên `home.jsp` hiển thị động.
- [ ] **Quốc (Repair):** Tuân thủ quy tắc BR-02: Khi ngưng hỗ trợ một loại linh kiện lỗi, chỉ cập nhật `isActive = false`, **tuyệt đối không xóa cứng** trong bảng `repair_problems`.

### Việc Thành cần duy trì:
- [x] Không tự ý xóa bỏ các thuộc tính của `Device` và `RepairProblem` mà Quốc và Anh đang phụ thuộc.
- [x] Đảm bảo mọi URL trên Header/Footer dùng link chuẩn: `/home`, `/listing`, `/products`, `/repair/request`, `/cart`, `/order`, `/login`, `/profile`, `/admin`.
- [x] Giữ nguyên bộ test `MarketplaceServiceTest` 10/10 PASS trước mọi lần push code.
