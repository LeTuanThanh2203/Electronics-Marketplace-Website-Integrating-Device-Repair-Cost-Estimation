# 🔧 SỔ TAY PHỐI HỢP & XỬ LÝ CONFLICT — PHẠM NGUYỄN VƯƠNG QUỐC
> **Phân hệ phụ trách:** Dịch Vụ Sửa Chữa (`/repair`), Báo Giá Dự Toán (`RepairEstimate`) & Báo Cáo Thống Kê Admin  
> **Thành viên:** Phạm Nguyễn Vương Quốc | **Nhánh Git:** `Quoc` / `integration`  
> **Trạng thái kiểm thử:** ✅ 18/18 Unit Tests PASS (`RepairServiceTest`) | 9 trang JSP compile 0 lỗi

---

## 1. PHẠM VI SỞ HỮU FILE (RANH GIỚI TRÁCH NHIỆM)

| Tầng kiến trúc | Quốc sở hữu độc quyền (Chỉ Quốc sửa) | Phối hợp với thành viên khác |
|---|---|---|
| **Entities & Enums** | `RepairRequest.java`, `RepairPricing.java`, `RepairEstimate.java`<br>`RepairProblem.java` (Quản trị danh mục lỗi) | Cung cấp danh mục lỗi `RepairProblem` để Thành tick chọn `ListingProblem` |
| **Dùng chung** | `Device.java` (Phối hợp với Thành và Anh) | Dùng chung thông tin dòng máy, không đổi constructor |
| **Service & Repo** | `RepairService.java`, `RepairRepository.java`, `JpaRepairRepository.java` | 18/18 Unit Test PASS |
| **Controller** | `RepairRequestServlet.java`, `AdminRepairServlet.java`, `ReportServlet.java` | Tiếp nhận yêu cầu báo giá sửa chữa và thống kê doanh thu |
| **Giao diện & Style** | `views/repair/*`, `views/admin/report.jsp`, `css/repair.css` | Tái sử dụng Header, Footer và CSS layout chung |

---

## 2. LỊCH SỬ THAY ĐỔI (CHANGELOG)

* **2026-10-19:** Rebase nhánh thành công lên `origin/Tan` (commit `2b9e8d9`), tiếp nhận cấu hình JPA và `User` mới nhất.
* **2026-10-19:** **Xóa package cấm:** Xóa sạch thư mục `controller/public/` (do từ khóa `public` là keyword bảo lưu của Java gây lỗi compile nghiêm trọng).
* **2026-10-20:** Sửa `JpaUtil` chuyển sang dùng chung `JpaConfig.getEntityManagerFactory()` — giải quyết triệt để lỗi không resolve được biến môi trường `${DB_URL}` trong `persistence.xml`.
* **2026-10-20:** Xóa file listener thừa `AppLifecycleListener.java`, đồng bộ dùng `AppShutdownListener` của Tân.
* **2026-10-21:** Hợp nhất vào nhánh `integration` (Commit `c3ee2a1`), nạp chồng constructor `RepairProblem(id, name)` và fallback `getName()` $\leftrightarrow$ `getDisplayName()` cho Thành.

---

## 3. MA TRẬN XUNG ĐỘT (CONFLICTS & GIẢI PHÁP)

> [!NOTE]
> Các xung đột kỹ thuật từng gặp và cách xử lý dứt điểm:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   BẢN ĐỒ XUNG ĐỘT CỦA QUỐC                             │
├──────────────┬───────────────────────────────┬─────────────────────────────────────────┤
│ Đối tác      │ Điểm xung đột kỹ thuật        │ Giải pháp chốt & File cần sửa           │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Lê Thành  │ • Signature constructor trùng │ • Tinh giản constructor Device.java,    │
│ (C2C/Core)   │   lặp trong `Device.java`     │   dùng getter/setter cho các trường phụ.│
│              │ • Cột problem_id & tên lỗi    │ • Giữ constructor RepairProblem(id,name)│
│              │   trong `RepairProblem.java`  │   và hỗ trợ cả 2 hàm getName / getTitle.│
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Ca Ngọc Tân│ • Trùng lặp App Listener      │ • Đã xóa AppLifecycleListener, dùng     │
│ (Auth/Config)│ • Lỗi Dialect khi tạo EMF riêng│   AppShutdownListener của Tân.          │
│              │                               │ • Lấy EMF từ JpaConfig, không tạo mới.  │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ ☕ Java Core │ • Thư mục controller/public/  │ • ĐÃ XÓA VĨNH VIỄN thư mục này vì vi    │
│              │   vi phạm từ khóa Java        │   phạm keyword của Java compiler.       │
└──────────────┴───────────────────────────────┴─────────────────────────────────────────┘
```

---

## 4. HƯỚNG DẪN THỰC THI — NÊN SỬA & TRÁNH SỬA FILE NÀO

### 🟢 File Quốc NÊN SỬA khi phát triển tiếp:
* `src/main/java/com/electronics/marketplace/service/RepairService.java`: Công thức tính biên độ giá dự toán sửa chữa, thời gian sửa chữa ước tính.
* `src/main/java/com/electronics/marketplace/controller/admin/ReportServlet.java`: Các câu truy vấn báo cáo tổng hợp SQL/JPQL cho dashboard.
* `src/main/webapp/WEB-INF/views/repair/*`: Form tiếp nhận máy hỏng, tra cứu tiến độ sửa chữa.

### 🔴 File Quốc TUYỆT ĐỐI KHÔNG TỰ Ý SỬA:
* **TUYỆT ĐỐI KHÔNG** tạo lại bất kỳ package nào có tên `controller/public/` hay `util/public/`.
* `src/main/java/com/electronics/marketplace/config/JpaConfig.java`: Dùng chung, không sửa đè.
* `src/main/java/com/electronics/marketplace/entity/Device.java`: Không thêm constructor có kiểu dữ liệu trùng lặp (`String, String, String...`).

---

## 5. CHECKLIST KIỂM SOÁT NHANH (QUICK CHECKLIST)
- [x] Luôn lấy `EntityManagerFactory` thông qua `JpaConfig.getEntityManagerFactory()`.
- [x] Không còn file thừa `AppLifecycleListener.java`.
- [x] Chạy `mvn test` xác nhận `RepairServiceTest` đạt 18/18 PASS trước khi push.
