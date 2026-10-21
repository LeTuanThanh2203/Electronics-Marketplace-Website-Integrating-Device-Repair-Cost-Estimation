# HỒ SƠ PHỐI HỢP DỰ ÁN — QUỐC (DỊCH VỤ SỬA CHỮA & BÁO CÁO LAB)

> **Thành viên:** Phạm Nguyễn Vương Quốc (Võ Minh Quốc)  
> **Phân hệ phụ trách:** Dịch Vụ Sửa Chữa & Dự Toán Chi Phí Linh Kiện (Repair Module), Quản trị Danh mục Lỗi, Thống kê Báo cáo SQL Aggregation (Admin Repair Lab).  
> **Nhánh Git:** `origin/Quoc`  
> **Trạng thái:** Tích hợp hoàn tất, 18/18 Unit Tests `RepairServiceTest` PASS, 9 JSP màn hình compile 0 lỗi.

---

## 1. CÁC ĐIỀU CHỈNH PHỐI HỢP (ĐÃ HOÀN TẤT & ĐỒNG BỘ)

* **Hợp nhất Tầng Persistence sang `JpaConfig`:**
  - Chuyển `JpaUtil.getEntityManagerFactory()` trỏ trực tiếp sang `JpaConfig.getEntityManagerFactory()`.
  - Toàn bộ module Sửa chữa sử dụng chung **một EntityManagerFactory** và **một HikariCP Connection Pool** của toàn sàn, giải quyết dứt điểm lỗi Hibernate không phân giải được biến môi trường `${DB_URL}`.
  - Bỏ `AppLifecycleListener`, đồng bộ dùng chung `AppShutdownListener` của Tân để chống lỗi đóng kết nối 2 lần (double close) khi tắt Tomcat.
* **Bổ sung Gson vào `pom.xml` phục vụ API Báo giá:**
  - Thêm `com.google.code.gson:gson:2.10.1` vào `pom.xml`.
  - Phục vụ chuyển đổi đối tượng `EstimateResultDTO` (chứa 15 trường dữ liệu số phức tạp và giá tiền min/max) thành JSON chuẩn trả về cho endpoint `/repair/api/estimate`.
* **Kế thừa Master Layout & Giữ bản sắc Module:**
  - Toàn bộ 9 trang JSP Sửa chữa (`views/repair/*` và `views/admin/repair*`) đã chuyển sang nhúng `header.jspf` và `footer.jspf` chuẩn hệ thiết kế Stitch của Thành.
  - Vẫn giữ nguyên thanh điều hướng nhanh đặc thù `rp-breadcrumb` và 2 nút tiện ích: *"Tra cứu tiến độ sửa"* (`/repair/track`) và *"Dự toán chi phí"* (`/repair/estimate`).
* **Hoàn thiện Bộ CSDL Sửa chữa Mẫu:**
  - Chuẩn bị file script SQL idempotent `src/main/resources/database/schema_and_seed_repair.sql` tạo bảng và nạp sẵn 7 loại lỗi chuẩn hóa kèm bảng giá linh kiện theo từng dòng máy `Device`.

---

## 2. CÁC TÌNH TRẠNG CONFLICT CÓ THỂ XẢY RA & GIẢI PHÁP

| Điểm Xung Đột | Nguyên Nhân Phát Sinh | Giải Pháp Đã Thực Thi |
|---|---|---|
| **Xung đột Entity `Device.java` với Thành & Anh** | Quốc bỏ trường `name`, thay bằng `series` và hàm `getDisplayName()`, thêm `@OneToMany pricings`. Nếu cùng khai báo 7 tham số chuỗi sẽ trùng signature constructor trong Java. | **Hợp nhất đa hình & Fallback:** Giữ cả `name` lẫn `series`. Trong `getName()`, fallback: `return (name != null && !name.trim().isEmpty()) ? name : getDisplayName();`. Giữ cả quan hệ `pricings` (Quốc) và `products` (Anh). |
| **Xung đột Entity `RepairProblem.java` với Thành** | Quốc đổi cột khóa chính thành `@Column(name = "problem_id")`, thêm `@UniqueConstraint`, nhưng xóa constructor `(Long id, String name)` làm `ListingRepositoryImpl` bị lỗi biên dịch. | Giữ toàn bộ database mapping và quan hệ `pricings` của Quốc; nạp chồng lại constructor `RepairProblem(Long id, String name)`; cung cấp cả `isActive()` lẫn `getIsActive()`. |
| **Lỗi biên dịch package `controller.public`** | Trong nhánh ban đầu của Quốc còn sót thư mục `controller/public/` (vi phạm từ khóa Java). | Xóa bỏ hoàn toàn thư mục này khỏi dự án; chuyển servlet sang cấu trúc package hợp lệ. |
| **Xung đột danh sách `<class>` trong `persistence.xml`** | Tân muốn bỏ `<class>` để Hibernate tự quét; Quốc cần khai báo tường minh để schema không bị trôi khi thêm Entity mới. | Khai báo tường minh toàn bộ danh sách 16 entity (`User`, `Device`, `RepairProblem`, `RepairPricing`, `RepairRequest`, `RepairEstimate`...) kết hợp nạp biến môi trường qua `JpaConfig`. |
| **Trang báo lỗi 404/500 trỏ file không tồn tại** | Nhánh gốc cấu hình error-page sai vị trí file gây lỗi lồng lỗi trên Tomcat. | Đã phối hợp Tân sửa cấu hình `web.xml` trỏ về đúng `/WEB-INF/views/error/404.jsp` và `/WEB-INF/views/error/500.jsp`. |

---

## 3. YÊU CẦU PHỐI HỢP & CHECKLIST HÀNH ĐỘNG

### Đường dẫn kiểm thử module Sửa chữa:
* Đặt lịch sửa chữa: `http://localhost:8080/repair/request`
* Dự toán chi phí tức thì (AJAX): `http://localhost:8080/repair/estimate`
* Tra cứu tiến độ đơn sửa: `http://localhost:8080/repair/track`
* Admin quản lý đơn sửa: `http://localhost:8080/admin/repairs`
* Admin báo cáo thống kê lỗi: `http://localhost:8080/admin/repair/reports`

### Đối với các thành viên khác:
- [ ] **Thành Lê (C2C):** Khi cho người bán chọn lỗi máy cũ trên `form.jsp`, lấy danh mục từ `RepairProblem` của Quốc để lưu vào `ListingProblem`.
- [ ] **Tân (Admin):** Thêm các tab quản trị Sửa chữa (`/admin/repairs`, `/admin/repair/devices`, `/admin/repair/problems`, `/admin/repair/pricing`, `/admin/repair/reports`) vào thanh Tab Admin.
- [ ] **Phước Anh (Catalog):** Dùng chung thực thể `Device.java` chuẩn với Quốc, không tạo thực thể thiết bị phần cứng riêng.

### Việc Quốc cần duy trì:
- [x] **QUY TẮC BẤT BIẾN BR-02:** Khi một loại lỗi không còn cung cấp linh kiện, chỉ cập nhật `isActive = false`, **TUYỆT ĐỐI KHÔNG XÓA CỨNG (Hard Delete)** trong bảng `repair_problems` để không làm gãy tin đăng của Thành.
- [x] Tiền tệ trong `RepairPricing` và `RepairEstimate` bắt buộc dùng `BigDecimal` để đảm bảo độ chính xác tuyệt đối.
- [x] Duy trì 18/18 Unit Tests `RepairServiceTest` PASS trước mọi lần hợp nhất code.
