# 🔐 SỔ TAY PHỐI HỢP & XỬ LÝ CONFLICT — CA NGỌC TÂN
> **Phân hệ phụ trách:** Tài khoản, Bảo mật RBAC (`/auth/*`), Quản trị Admin Dashboard (`/admin/*`)  
> **Thành viên:** Ca Ngọc Tân | **Nhánh Git:** `Tan` / `integration`  
> **Trọng tâm:** Thống nhất Session `currentUser`, Bảo mật Filter & Duyệt tin C2C

---

## 1. PHẠM VI SỞ HỮU FILE (RANH GIỚI TRÁCH NHIỆM)

| Tầng kiến trúc | Tân sở hữu độc quyền (Chỉ Tân sửa) | Phối hợp với thành viên khác |
|---|---|---|
| **Entities & Enums** | `User.java`, Enums: `Role.java` (`USER`, `ADMIN`), `UserStatus.java` (`ACTIVE`, `SUSPENDED`, `DELETED`) | Cung cấp đối tượng `User` cho toàn nhóm qua Session |
| **Bảo mật & Filter**| `AuthenticationFilter.java`, `AuthorizationFilter.java` | Bảo vệ các route `/cart`, `/checkout`, `/order`, `/admin` |
| **Controller** | `LoginServlet.java`, `RegisterServlet.java`, `ProfileServlet.java`, `LogoutServlet.java`, `AdminServlet.java` | Đón nhận URL chuẩn: `/login`, `/register`, `/logout`, `/profile`, `/admin` |
| **Giao diện JSP** | `views/auth/login.jsp`, `register.jsp`, `profile.jsp`, `views/admin/dashboard.jsp` | Kế thừa Layout Google Stitch từ Thành |

---

## 2. LỊCH SỬ THAY ĐỔI (CHANGELOG)

* **2026-10-19:** Đồng bộ toàn bộ 7 vị trí từ `"user"` $\to$ **`"currentUser"`** (Khắc phục lỗi đăng nhập thành công nhưng vẫn bị coi là khách).
* **2026-10-19:** Xóa dứt điểm các file rác/scaffolding: `HelloApplication.java`, `HelloResource.java`, `DBConnection.java`, `DatabaseInitializer.java`, `Status.java`.
* **2026-10-20:** Nâng cấp `AuthenticationFilter`: Hỗ trợ trả mã `401 JSON` cho AJAX request (cho Thành & Nam) và redirect `/login` cho web form thông thường.
* **2026-10-20:** Trang bị endpoint Admin duyệt tin C2C: `action=approveListing` & `action=rejectListing` (hỗ trợ quy tắc BR-15, BR-19 của Thành).
* **2026-10-21:** Merge vào nhánh `integration` (Commit `3782371`), giải quyết xung đột `User.java` và `.gitignore`.

---

## 3. MA TRẬN XUNG ĐỘT (CONFLICTS & GIẢI PHÁP)

> [!WARNING]
> Những điểm conflict nghiêm trọng từng phát sinh và quy tắc bắt buộc không tái phạm:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   BẢN ĐỒ XUNG ĐỘT CỦA TÂN                              │
├──────────────┬───────────────────────────────┬─────────────────────────────────────────┤
│ Đối tác      │ Điểm xung đột kỹ thuật        │ Giải pháp chốt & File cần sửa           │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Lê Thành  │ • Tân xóa mất constructor có  │ • Giữ Enum Role/Status nhưng BẮT BUỘC   │
│ (C2C/Core)   │   tham số trong `User.java`   │   nạp chồng `User(id, name, email...)`  │
│              │ • Header link sai (/auth?...) │ • Link Header trỏ thẳng `/login`.       │
│              │ • Admin duyệt tin chợ cũ      │ • AdminServlet gọi ListingRepository.   │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Huỳnh Nam │ • Revert nhầm commit của Nam  │ • Tuyệt đối không revert commit Cart.   │
│ (Cart/Order) │ • Lệch key Session ("user")   │ • Chốt 100% key `"currentUser"`.        │
│              │ • Filter chặn nhầm trang Cart │ • GET /cart không action cho phép xem.  │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Vương Quốc│ • JpaUtil lệch config DB      │ • Tân cung cấp `AppShutdownListener`,   │
│ (Repair)     │ • Trùng Lifecycle Listener   │   Quốc dùng chung `JpaConfig` của Tân.  │
└──────────────┴───────────────────────────────┴─────────────────────────────────────────┘
```

---

## 4. HƯỚNG DẪN THỰC THI — NÊN SỬA & TRÁNH SỬA FILE NÀO

### 🟢 File Tân NÊN SỬA khi có yêu cầu Auth/Admin:
* `src/main/java/com/electronics/marketplace/filter/AuthenticationFilter.java`: Bổ sung route cần bảo vệ (như URL thanh toán mới).
* `src/main/java/com/electronics/marketplace/controller/admin/AdminServlet.java`: Logic khóa user, mở user, duyệt tin đăng chợ cũ.
* `src/main/webapp/WEB-INF/layout/header.jspf`: Khớp link `/login`, `/register`, `/profile`, `/logout`.

### 🔴 File Tân TUYỆT ĐỐI KHÔNG TỰ Ý SỬA:
* `src/main/java/com/electronics/marketplace/entity/User.java`: **KHÔNG ĐƯỢC XÓA** constructor 5 tham số `User(Long id, String name, String email, String phone, String location)`.
* Các file trong gói `com.electronics.marketplace.service.*`: Không tự ý `git revert` làm mất các class Cart, Order, Payment.
* `src/main/resources/database/*`: Không tự viết script đè cấu trúc bảng của các phân hệ khác.

---

## 5. CHECKLIST KIỂM SOÁT NHANH (QUICK CHECKLIST)
- [x] Session key lưu thông tin đăng nhập: `req.getSession().setAttribute("currentUser", user)`.
- [x] Đã xóa toàn bộ thư mục scaffolding `com/example/`.
- [x] Chạy `mvn compile` và `mvn test` đạt `BUILD SUCCESS` trước khi push.
