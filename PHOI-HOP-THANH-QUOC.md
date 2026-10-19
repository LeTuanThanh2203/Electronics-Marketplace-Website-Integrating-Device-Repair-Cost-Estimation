# Chốt ranh giới & Hợp đồng phối hợp: Khai Báo Lỗi Chợ Đồ Cũ (Thành) ↔ Dịch Vụ Sửa Chữa & Báo Giá (Quốc)
> **Tài liệu đối chiếu:** Đã rà soát commit trên Git (`origin/Quoc` hiện vẫn ở commit `9072b00 Set up project` — chưa push code mới).  
> **Phiên bản:** v2.0 (Cập nhật sau khi rà soát toàn bộ nhánh Git).  
> **Nguyên tắc:** Quốc quản trị danh mục lỗi chuẩn hóa (`RepairProblem`) và bảng giá sửa chữa; Thành sử dụng danh mục lỗi đó để cho người bán tick chọn tình trạng lỗi khi đăng tin chợ cũ (`ListingProblem`).

---

## 1. HIỆN TRẠNG NHÁNH CỦA QUỐC (ĐÃ RÀ SOÁT)

- Nhánh `origin/Quoc` hiện tại **chưa có commit mới** (vẫn ở commit gốc `9072b00`).
- 👉 **Khuyến nghị quan trọng cho Quốc:** Khi bắt đầu code, Quốc nên `git pull origin/Tan` hoặc `git pull origin/Thanh` để lấy toàn bộ các Entity gốc (`Device`, `RepairProblem`, `User`) và cấu hình JPA `JpaConfig.java` chuẩn đã được cả nhóm hoàn thiện, tránh tự code lại từ đầu gây xung đột.

---

## 2. Phân chia trách nhiệm

| Hạng mục | Thành (Chợ Đồ Cũ & Khai Báo Lỗi) | Quốc (Dịch Vụ Sửa Chữa & Quản Trị Danh Mục) |
|---|---|---|
| **Servlet Controller** | `/listing` → `ListingServlet`, `/home` → `HomeServlet` | `/repair` → `RepairServlet`<br>`/admin/report` → `CategoryAdminServlet` |
| **Service Logic** | `MarketplaceService` | `RepairService` *(tính toán ước lượng chi phí min/max)* |
| **Entity & Enums** | `ListingProblem`, `Listing`<br>Enums: `ListingCondition`, `ListingStatus` | `RepairProblem`, `RepairPricing`, `RepairRequest`, `RepairEstimate` |
| **Giao diện (JSP)** | `views/listing/form.jsp` *(checkbox lỗi)*<br>`views/listing/detail.jsp` *(ma trận lỗi máy cũ)* | `views/repair/request_form.jsp`<br>`views/repair/estimate.jsp`<br>`views/admin/report.jsp` *(thống kê số lượng lỗi)* |

---

## 3. Các điểm giao tiếp cốt lõi (Integration Points)

### Giao điểm 1: Danh mục lỗi chuẩn hóa `RepairProblem` (BR-02, BR-07, BR-09)

1. **Khế ước thực thể `RepairProblem.java` Thành đã tạo sẵn:**
   - Bảng: `repair_problems`
   - Thuộc tính:
     - `id` (kiểu `Long`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`)
     - `name`: Tên lỗi chuẩn hóa (ví dụ: *Màn hình có sọc / điểm chết, Pin chai, Bàn phím liệt...*)
     - `isActive`: Trạng thái còn áp dụng hay không (mặc định `true`)

2. **Cách thức Thành sử dụng:**
   - Trong `form.jsp` (Đăng tin bán máy cũ), Thành cho phép người bán tick chọn các lỗi mà máy đang gặp.
   - Khi lưu vào Database, Thành tạo các bản ghi trong thực thể liên kết [`ListingProblem.java`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/src/main/java/com/electronics/marketplace/entity/ListingProblem.java):
     ```java
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "problem_id", nullable = false)
     private RepairProblem problem;
     ```
   - Trên trang `detail.jsp`, Thành hiển thị "Ma Trận Lỗi Linh Kiện" (Defect Matrix) để người mua thấy rõ máy bị hỏng phần nào trước khi bấm "Chốt mua".

3. **Việc Quốc cần quản trị:**
   - **Nhập dữ liệu gốc (Seed Data):** Quốc chuẩn bị script SQL thêm sẵn 5–7 loại lỗi phổ biến vào bảng `repair_problems` để khi web chạy lên là có sẵn danh mục lỗi cho cả form Đăng tin chợ lẫn form Yêu cầu sửa chữa.
   - **Quy tắc nghỉ hưu lỗi (BR-02):** Khi một loại lỗi không còn cung cấp linh kiện sửa chữa, Admin Quốc chỉ được cập nhật `isActive = false`, **TUYỆT ĐỐI KHÔNG xóa cứng (Hard Delete)** bản ghi trong DB để tránh làm gãy các tin đăng cũ của Thành.

---

### Giao điểm 2: Dùng chung thực thể phần cứng `Device.java`

1. **Thành đã hoàn tất mapping JPA cho `Device.java`:**
   - Bảng `devices` chứa thông tin các dòng thiết bị (Laptop, Smartphone...).
2. **Khế ước đối với Quốc:**
   - Khi tạo Entity bảng giá sửa chữa `RepairPricing.java` và yêu cầu sửa chữa `RepairRequest.java`, Quốc trỏ trực tiếp khóa ngoại tới `Device`:
     ```java
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "device_id", nullable = false)
     private Device device;
     ```
   - Quốc không tạo bảng hay class `Device` riêng.

---

### Giao điểm 3: Báo cáo thống kê tần suất lỗi hỏng (Admin Report)

1. **Chức năng của Quốc:**
   - Viết 1 câu truy vấn SQL gom nhóm (`GROUP BY`, `COUNT`) để hiển thị biểu đồ hoặc bảng thống kê trên `views/admin/report.jsp`.
2. **Điểm mở rộng giá trị cao:**
   - Quốc có thể truy vấn số lượng lỗi từ bảng `repair_requests` và hoàn toàn có thể `UNION` hoặc `JOIN` thêm từ bảng `listing_problems` của Thành để trả lời câu hỏi: *"Loại linh kiện nào người dùng mang đi sửa nhiều nhất, và loại linh kiện nào khiến người dùng phải bán thanh lý máy cũ nhiều nhất?"*. Đây là điểm cộng lớn cho bài báo cáo đồ án.

---

### Giao điểm 4: Kế thừa Google Stitch Design System cho trang Sửa Chữa

1. Quốc sử dụng Header, Footer và tông màu giao diện từ [`home.jsp`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/src/main/webapp/WEB-INF/views/home.jsp) của Thành cho các trang:
   - `views/repair/request_form.jsp` (Form khách chọn máy và tick chọn lỗi cần sửa).
   - `views/repair/estimate.jsp` (Trang hiển thị khoảng giá dự kiến min - max).
   - `views/admin/report.jsp` (Bảng thống kê lỗi).

---

## 4. Checklist công việc phối hợp cho Quốc

- [ ] Kéo code từ `origin/Thanh` để lấy `RepairProblem.java` và `Device.java` làm gốc.
- [ ] Bổ sung các trường `estimatedMinDays`, `estimatedMaxDays` vào `RepairProblem.java` (nếu cần).
- [ ] Tạo các Entity JPA: `RepairPricing`, `RepairRequest`, `RepairEstimate`.
- [ ] Chuẩn bị script INSERT dữ liệu mẫu cho bảng `repair_problems` và `repair_pricings`.
- [ ] Xây dựng thuật toán tính chi phí min/max trong `RepairService` (BR-04, BR-30).
- [ ] Viết câu query SQL `GROUP BY` đếm lỗi hiển thị lên `admin/report.jsp`.
- [ ] Dựng giao diện `request_form.jsp` và `estimate.jsp` theo đúng chuẩn CSS của `home.jsp`.
