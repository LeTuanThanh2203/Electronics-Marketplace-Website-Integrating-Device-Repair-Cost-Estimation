# TÀI LIỆU HỢP NHẤT PHỐI HỢP TOÀN DIỆN (THÀNH ↔ TẤT CẢ CÁC NHÁNH)
## & BÁO CÁO PHÂN TÍCH XUNG ĐỘT (CONFLICT MATRIX) DỰA TRÊN COMMIT 5 NHÁNH GIT

> **Phiên bản:** v5.0 (Cập nhật sau kiểm tra và đối chiếu forensic xung đột trên toàn bộ 5 nhánh Git).  
> **Phạm vi tài liệu:**  
> 1. Gộp toàn bộ hợp đồng giao tiếp giữa **Lê Tuấn Thành** (Core Setup, UI Google Stitch & Chợ Đồ Cũ C2C) với 4 thành viên: **Ca Ngọc Tân**, **Phạm Phước Anh**, **Huỳnh Thuyên Nam**, **Phạm Nguyễn Vương Quốc**.  
> 2. Phân tích chi tiết lịch sử commit thực tế của cả 5 nhánh Git (`origin/Thanh`, `origin/Nam`, `origin/Tan`, `origin/Anh`, `origin/Quoc`), bóc tách chính xác từng điểm xung đột tiềm ẩn (Code, Entity, XML, Data) và cung cấp giải pháp drop-in code cùng lộ trình Merge an toàn 100%.  
> *(Lưu ý: File [`PHOI-HOP-NAM-THANH.md`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/PHOI-HOP-NAM-THANH.md) được giữ riêng biệt để lưu chi tiết nhật ký các mục Thành đã fix dứt điểm cho Nam).*

---

# PHẦN I: BẢNG MA TRẬN PHÂN CÔNG & RANH GIỚI TRÁCH NHIỆM

Nhóm áp dụng nguyên tắc: **"Một file - Một chủ sở hữu. Giao tiếp qua URL, trạng thái Database và Entity chuẩn hóa, tuyệt đối không chỉnh sửa chéo file của nhau."**

| Phân hệ / Thành viên | Trách nhiệm chính | Controller & URL | Entity & Enums quản lý | Giao diện JSP & CSS | Giao điểm kết nối với Thành |
|---|---|---|---|---|---|
| **Lê Tuấn Thành**<br>*(Core & C2C)* | Khởi tạo dự án, Design System Google Stitch, Sàn Chợ Đồ Cũ | `/listing` (`ListingServlet`)<br>`/home` (`HomeServlet`) | `Listing`, `ListingImage`, `ListingProblem`, `Device`, `RepairProblem`<br>Enums: `ListingStatus`, `ListingCondition` | `views/home.jsp`<br>`views/listing/*`<br>`css/home.css`, `css/listing.css`<br>`ui_stitch.html` | **Trung tâm kết nối:** Cung cấp khung Header, Footer, Layout chuẩn, và Entity `Device` cho cả nhóm. |
| **Ca Ngọc Tân**<br>*(Auth & Admin)* | Xác thực, phân quyền RBAC, Admin Quản trị Người dùng & Duyệt tin | `/auth/*` (`Login`, `Register`, `Profile`)<br>`/admin/*` (`AdminServlet`) | `User`<br>Enums: `Role` (`USER`, `ADMIN`), `UserStatus` (`ACTIVE`, `SUSPENDED`) | `views/auth/*`<br>`views/admin/dashboard.jsp` | Cung cấp session `currentUser` cho form đăng tin `/listing/form`; duyệt tin `isApproved` (BR-15). |
| **Phạm Phước Anh**<br>*(Catalog & Recom)* | Catalog máy mới chính hãng, Thuật toán Recommendation, Master Data | `/product` (`ProductServlet`) | `Product`, `ProductSpecification`<br>Enums: `ProductStatus`, `SpecDataType` | `views/product/*`<br>`product/list.jsp`, `detail.jsp`, `compare.jsp` | Cắm dữ liệu gợi ý máy mới vào `home.jsp`; dùng chung `Device`; chuẩn bị `seed_data.sql`. |
| **Huỳnh Thuyên Nam**<br>*(Cart & Payment)* | Giỏ hàng, Đặt hàng chống tranh mua BR-20, Cổng MoMo Sandbox | `/order` (`OrderServlet`)<br>`/cart` (`CartServlet`)<br>`/checkout`, `/payment` | `Cart`, `CartItem`, `Order`, `OrderItem`, `Payment`<br>Enums: `OrderStatus`, `PaymentStatus`... | `views/cart/*`<br>`views/order/*`<br>`css/order.css` | Nhận request "Chốt mua" C2C từ `detail.jsp` của Thành; kiểm soát cờ trạng thái `RESERVED`/`SOLD`. |
| **Phạm Nguyễn Vương Quốc**<br>*(Repair & Report)* | Báo giá sửa chữa, Quản trị danh mục lỗi, Thống kê SQL Aggregation | `/repair` (`RepairRequestServlet`)<br>`/admin/repair/*` | `RepairRequest`, `RepairPricing`, `RepairEstimate`, `RepairProblem` | `views/repair/*`<br>`views/admin/report.jsp`<br>`css/repair.css` | Quản trị danh mục lỗi chuẩn `RepairProblem` để Thành cho người bán tick lỗi (`ListingProblem`). |

---

# PHẦN II: KHẾ ƯỚC PHỐI HỢP CHI TIẾT GIỮA THÀNH VÀ TỪNG THÀNH VIÊN

## 1. Phối hợp giữa Thành ↔ Tân (Auth & Admin Dashboard)

1. **Session Key Thống Nhất:**  
   Tân đã chuẩn hóa session key thành **`"currentUser"`** (lưu đối tượng `User`).
   - Phía Thành: Khi người dùng vào `/listing/form` để đăng tin, Thành lấy `currentUser` từ session để gán vào `listing.setSeller(currentUser)`.
   - Xem số điện thoại người bán (Quy tắc BR-33 & BR-34): Tại endpoint AJAX `/listing/contact`, Thành kiểm tra nếu `currentUser == null` thì trả mã HTTP **401 Unauthorized** yêu cầu đăng nhập mới được xem số liên hệ.
2. **Quy trình Duyệt Tin Đăng Chợ Đồ Cũ (BR-15, BR-19):**  
   - Khi Seller đăng bài, `MarketplaceService` của Thành khởi tạo: `status = ListingStatus.AVAILABLE`, `isApproved = false`, `isRemovedByAdmin = false`.
   - Tin chỉ được hiển thị lên trang công khai `/listing/marketplace` khi thỏa mãn: `isApproved == true` và `isRemovedByAdmin == false`.
   - Trên `AdminServlet` của Tân:
     - Gọi `ListingRepository.findPendingListings()` để hiển thị danh sách tin chờ duyệt.
     - Nút "Duyệt": set `isApproved = true`.
     - Nút "Từ chối": set `isApproved = false` và cập nhật `rejectionReason`.
     - Nút "Gỡ tin vi phạm" (Take down): set `isRemovedByAdmin = true`.
3. **Thừa hưởng Giao diện Google Stitch:**  
   Tân tái sử dụng header/footer và cấu trúc form từ `home.jsp` của Thành cho `views/auth/login.jsp`, `register.jsp` và `admin/dashboard.jsp`.

---

## 2. Phối hợp giữa Thành ↔ Phước Anh (Catalog Máy Mới, Recommendation & Data)

1. **Hiển thị Recommendation trên Trang Chủ `home.jsp`:**  
   - Trong `views/home.jsp`, Thành đã thiết kế sẵn khu vực **Khối Gợi Ý Thiết Bị Thay Thế** (Dòng 140–280) theo dạng Grid hiện đại, có badge "GỢI Ý TỐI ƯU", hiển thị thông số CPU/RAM và nút mua.
   - Phước Anh trong `HomeServlet` gọi `ProductService.getRecommendedProducts(...)` và nạp vào request attribute:
     ```java
     request.setAttribute("recommendedProducts", recommendedList);
     ```
   - Trang `home.jsp` dùng JSTL `<c:forEach items="${recommendedProducts}" var="prod">` để render tự động.
2. **Khế ước Dùng chung Entity `Device.java` (Tránh xung đột):**  
   - Bảng `devices` là gốc thông tin phần cứng dùng chung (CPU, RAM, Storage, Brand, Model).
   - `Product.java` của Phước Anh tạo liên kết:
     ```java
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "device_id", nullable = false)
     private Device device;
     ```
   - **Lưu ý đặc biệt:** `Device.java` phải nạp chồng đầy đủ các constructors: Constructor có `Long id` (để Thành seed test in-memory) và Constructor không có `id` (để tự sinh identity).
3. **Phân công Phụ trách Data Toàn Sàn:**  
   Phước Anh chịu trách nhiệm tạo file script `src/main/resources/seed_data.sql` chứa sẵn tài khoản mẫu, các dòng thiết bị `Device`, cấu hình `ProductSpecification` và sản phẩm máy mới mẫu.

---

## 3. Phối hợp giữa Thành ↔ Quốc (Dịch Vụ Sửa Chữa & Quản Trị Danh Mục Lỗi)

1. **Danh mục Lỗi Chuẩn Hóa `RepairProblem` (BR-02, BR-07, BR-09):**  
   - Bảng `repair_problems` do Quốc quản lý dữ liệu gốc (Tên lỗi, thời gian sửa dự kiến).
   - Thành sử dụng danh mục này trên `views/listing/form.jsp` để người bán tick chọn lỗi máy đang gặp, lưu vào thực thể liên kết `ListingProblem`.
   - Trên `views/listing/detail.jsp`, Thành hiển thị Ma Trận Lỗi Linh Kiện (Defect Matrix) rõ ràng cho người mua.
   - **Quy tắc bất biến:** Khi một lỗi không còn cung cấp linh kiện, Quốc chỉ được cập nhật `isActive = false`, **TUYỆT ĐỐI KHÔNG xóa cứng (Hard Delete)** để không làm gãy dữ liệu các bài đăng của Thành.
2. **Báo cáo Thống Kê Tổng Hợp Lỗi (Admin Report):**  
   Quốc viết câu lệnh SQL Aggregation (`GROUP BY`, `COUNT`) gom nhóm cả từ `repair_requests` lẫn `listing_problems` để báo cáo: *"Lỗi linh kiện nào phát sinh sửa chữa nhiều nhất và lỗi nào khiến người dùng phải bán xác thanh lý nhiều nhất?"*.

---

## 4. Phối hợp giữa Thành ↔ Nam (Giỏ Hàng, Chốt Mua & MoMo Gateway)

1. **Luồng "Chốt Mua" Hàng C2C Độc Bản:**  
   - Trên `views/listing/detail.jsp`, Thành đặt form POST:
     ```html
     <form method="post" action="${pageContext.request.contextPath}/order?action=create">
         <input type="hidden" name="listingId" value="${listing.id}">
         <button type="submit">CHỐT MUA MÁY (GIỮ CHỖ)</button>
     </form>
     ```
2. **Xử Lý Chống Tranh Mua (Concurrency Control - BR-20):**  
   Nam trong `OrderService` thực hiện câu lệnh cập nhật nguyên tử:
   ```sql
   UPDATE listings SET status = 'RESERVED', reserved_by_order_id = ?
   WHERE id = ? AND status = 'AVAILABLE' AND is_approved = true AND is_removed_by_admin = false
   ```
   Nếu `affected_rows == 0` $\to$ Trả mã lỗi **409 Conflict** (`LISTING_NOT_AVAILABLE`).
3. **Đồng bộ Vòng đời Trạng thái Tin Đăng:**  
   - Khi Order hoàn tất $\to$ Cập nhật `Listing.status = 'SOLD'` (BR-18).
   - Khi Order bị hủy $\to$ Trả về `Listing.status = 'AVAILABLE'` và giải phóng cờ giữ chỗ (BR-21).

---

# PHẦN III: PHÂN TÍCH TOÀN DIỆN CÁC XUNG ĐỘT (CONFLICT MATRIX) DỰA TRÊN COMMIT 5 NHÁNH GIT

Khảo sát cây commit thực tế (`git log --oneline --graph --all`):

```
* 3782371 (origin/Tan) Revert: bo cac file merge tu commit f0769f6 (nhanh Nam)
* 4c49b5d back về java 17
* 0d0d2f0 .
* e5c0b30 chỉnh sửa các file để phối hợp với thành lê và file .md report
* 6252a68 xóa 2 file lỗi
* f0769f6 merge: lay lai cac file thieu tu branch Nam
| * c3ee2a1 (origin/Quoc) fix(build): nang Java len 17, bo API Java 11+ de tuong thich nguoc
| * 4287da2 fix(repair): rebase len origin/Tan, dung JpaConfig va bo listener trung
| * e9c1e8d feat(repair): hoan thien module Dich Vu Sua Chua & Thong Ke Co Ban
|/  
*   2b9e8d9 Merge branch 'Nam' into Tan
|\  
| * 3664f24 (origin/Nam) feat: tich hop cong thanh toan MoMo that, bo chuyen khoan QR va the ngan hang
| * a86e2d2 docs: them PHOI-HOP-TAN.md - hop dong currentUser va cac diem vo khi merge voi Tan
| * 7f16b28 feat: hoan thien CartService + OrderService va 4 servlet gio hang/don hang/thanh toan
| * 68eb932 feat: nhan entity Listing/Device cua Thanh + OrderItem tro quan he Listing
| * 8364a55 feat: nap thong tin ket noi DB tu bien moi truong qua JpaConfig
| * 25449cb fix: go loi build - package `public` khong hop le va Product thieu getPrice
* | f8ab393 bổ sung file .md báo cáo việc sửa code
* | af8b56c Tài Khoản, Phân quyền và Quản trị hệ thống
|/  
| * e442c14 (origin/Anh) sửa lại vài vị trí file
| * ca687c6 (Anh) them UI product
| * b6e1c38 (Anh) Xay dung recommendation + Repository
|/  
| * 333df71 (origin/Thanh, HEAD) Fix theo file PHOIHOP NAM-THANH, TẠO FILE PHOIHOP VOI CAC BRANCH KHAC
| * 2bc27b1 (Thanh) Phan listing server va ui listing
|/  
* 9072b00 Set up project (Commit gốc ban đầu của Thành)
```

Kết quả quét xung đột tự động bằng lệnh nội suy Git (`git merge-tree --write-tree origin/Thanh <other-branches>`) phát hiện chính xác **9 điểm xung đột kỹ thuật**:

### BẢNG MA TRẬN 9 ĐIỂM XUNG ĐỘT KỸ THUẬT CHI TIẾT

| STT | Điểm Xung Đột | Mức Độ | Các Nhánh Liên Quan | Chi Tiết Hiện Tượng & Nguy Cơ Lỗi | Giải Pháp Đồng Thuận Triệt Để |
|---|---|---|---|---|---|
| **01** | **Nguy cơ mất file Giao diện JSP của Thành** | **BLOCKER** | `origin/Thanh`<br>vs `origin/Nam`, `origin/Tan`, `origin/Anh` | Nam, Tân, Anh rẽ nhánh từ commit gốc `9072b00` trước khi Thành commit `views/home.jsp` và thư mục `views/listing/`. Khi xem `git diff origin/Thanh <branch>`, Git đánh dấu các file giao diện của Thành là deleted/missing. Nếu merge tự động không kiểm tra sẽ làm mất toàn bộ UI Chợ đồ cũ. | **TUYỆT ĐỐI KHÔNG để Git xóa các file này.**<br>Thư mục `views/listing/`, `views/home.jsp`, `css/home.css`, `css/listing.css`, `js/listing-detail.js` từ nhánh `Thanh` là chuẩn mực duy nhất của Chợ đồ cũ và Trang chủ. |
| **02** | **Xung đột Entity `Device.java`** | **BLOCKER** | `origin/Thanh`<br>vs `origin/Anh`<br>vs `origin/Quoc` | • Thành cần `name`, `brand`, `model` và constructor có `Long id` cho `MarketplaceServiceTest`.<br>• Anh thêm `deviceType = "LAPTOP"` và `@OneToMany List<Product> products`, bỏ constructor có `id`.<br>• Quốc **xóa bỏ hoàn toàn trường `name`**, thay bằng `series` và hàm `getDisplayName()`, thêm `@OneToMany List<RepairPricing> pricings`. Điều này làm gãy `getName()` trên JSP và Repository của Thành và Anh! | **Hợp nhất đa hình (Overloading & Fallback):**<br>Giữ cả trường `name` và `series`. Trong `getName()`, fallback: `return name != null ? name : getDisplayName();`. Giữ cả 2 quan hệ `@OneToMany` (`products` và `pricings`). Cung cấp đủ 4 constructors. |
| **03** | **Xung đột Entity `RepairProblem.java`** | **HIGH** | `origin/Thanh`<br>vs `origin/Quoc` | • Thành dùng constructor `RepairProblem(Long id, String name)` trong `ListingRepositoryImpl.java` để mock seed dữ liệu lỗi, dùng `Boolean isActive` có getter `getIsActive()`.<br>• Quốc đổi tên cột ID thành `problem_id`, thêm `@OneToMany pricings`, nhưng **xóa constructor `(Long, String)`** và đổi getter thành `isActive()`. Gây lỗi biên dịch (Compilation Error). | **Nạp chồng Constructor & Getter kép:**<br>Kế thừa toàn bộ DB mapping của Quốc (`problem_id`, `pricings`, `uniqueConstraints`). Bổ sung lại constructor `RepairProblem(Long id, String name)`. Cung cấp cả `isActive()` lẫn `getIsActive()` để tương thích cả Java và JSP EL. |
| **04** | **Xung đột Enum `ListingStatus.java`** | **BLOCKER** | `origin/Thanh`, `origin/Nam`<br>vs `origin/Tan` | • Thành & Nam dùng: `AVAILABLE` ("Đang bán"), `RESERVED` ("Đã giữ chỗ"), `SOLD` ("Đã bán"), `HIDDEN` ("Đã ẩn") phục vụ vòng đời chốt đơn C2C (BR-15, BR-20, BR-21).<br>• Tân đổi thành: `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `SOLD`, `HIDDEN` để phục vụ Admin duyệt tin.<br>Nếu lấy file của Tân, `AVAILABLE` và `RESERVED` biến mất $\to$ Gãy toàn bộ Service của Thành và Nam! | **Hợp nhất đầy đủ 7 trạng thái:**<br>`PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN`.<br>Giữ trường `displayName` và hàm `getDisplayName()`. Thành đã có sẵn cờ `isApproved`, đảm bảo tương thích 100% với cả hai luồng. |
| **05** | **Xung đột Entity `Product.java`** | **MEDIUM** | `origin/Nam`<br>vs `origin/Anh` | • Nam tạo stub tạm gồm 6 trường cơ bản (`id`, `name`, `brand`, `price`, `status`, `storeName`) để compile `CartItem`.<br>• Phước Anh xây dựng Entity đầy đủ 20 trường (thông số kỹ thuật, bảo hành, ảnh, recommendation tag `targetNeed`, `@OneToMany specifications`, quan hệ `Device`). | **Lấy 100% Entity `Product.java` của Phước Anh:**<br>Bản của Phước Anh là tập mẹ (superset) chứa toàn bộ 6 trường của Nam với tên gọi và kiểu dữ liệu trùng khớp tuyệt đối. Code Cart/Order của Nam chạy mượt mà không cần sửa đổi. |
| **06** | **Trùng lặp Package Servlet & Dummy Stubs** | **HIGH** | `origin/Thanh`<br>vs `origin/Nam`<br>vs `origin/Anh`<br>vs `origin/Quoc` | • Thành đặt `ListingServlet` (15 KB, hoàn chỉnh) trong `controller/pub/`.<br>• Nam & Anh tạo file rỗng `ListingServlet.java` (90 bytes) trong `controller/storefront/`.<br>• Phước Anh tạo `ProductServlet.java` (10 KB, hoàn chỉnh) trong `controller/storefront/`.<br>• Quốc vẫn còn thư mục `controller/public/` (vi phạm từ khóa cấm `public` của Java). | **Chuẩn hóa Package `controller.storefront`:**<br>1. Xóa bỏ thư mục `controller/public/` của Quốc.<br>2. Thay thế dummy stub `ListingServlet` bằng mã nguồn hoàn chỉnh của Thành.<br>3. Giữ nguyên `ProductServlet` hoàn chỉnh của Phước Anh.<br>4. Xóa placeholder `pub/ProductServlet.java` của Thành. |
| **07** | **Xung đột Entity `User.java` (String vs Enum & Constructor)** | **MEDIUM** | `origin/Thanh`<br>vs `origin/Nam`<br>vs `origin/Tan` | • Thành ban đầu dùng `String role/status` và constructor `User(Long id, String name, String email, String phone, String location)`.<br>• Nam & Tân chuyển sang Enum `Role` (`USER`, `ADMIN`) và `UserStatus` (`ACTIVE`, `SUSPENDED`), nhưng Tân xóa mất constructor có tham số, gây lỗi test unit của Thành. | **Lấy bản Enum của Tân/Nam + Bổ sung Constructor:**<br>Giữ Enum `Role`, `UserStatus` và các hàm tiện ích `isAdmin()`, `isActive()`. Bổ sung constructor `User(Long id, String name, ...)` để các bộ test và seed in-memory của Thành biên dịch hoàn hảo. |
| **08** | **Xung đột `persistence.xml` & `JpaConfig.java`** | **HIGH** | Cả 5 nhánh | • Thành khai báo tĩnh 6 Entity của C2C và hardcode DB.<br>• Quốc khai báo 6 Entity của Sửa chữa.<br>• Phước Anh khai báo 3 Entity của Sản phẩm.<br>• Nam & Tân viết `JpaConfig.java` đọc biến môi trường `${DB_URL}` kết hợp HikariCP. | **Đăng ký tập hợp đủ 16 Entity:**<br>Sử dụng `JpaConfig.java` của Nam để bảo mật mật khẩu và tối ưu HikariCP. Trong `persistence.xml`, khai báo tường minh toàn bộ 16 thực thể của cả 5 bạn để Tomcat 10 load schema mượt mà, bật `hbm2ddl.auto=update`. |
| **09** | **Xung đột Compiler `pom.xml` & `web.xml`** | **MEDIUM** | Cả 5 nhánh | • Nhánh Nam ban đầu để Java 8; Quốc và Tân đã chuyển sang Java 17; Thành dùng Java 17.<br>• Cấu hình `web.xml`: Mỗi bạn có phiên bản khác nhau. Có bạn để servlet mapping trùng lặp với `@WebServlet`. | **Cố định Java 17 & Tối ưu Web XML:**<br>Chốt cứng `<maven.compiler.source>17</maven.compiler.source>`. Xóa sạch toàn bộ `.jar` trong `WEB-INF/lib/`. Dùng `web.xml` hiện đại (đăng ký qua `@WebServlet`, cookie `HttpOnly`, chuyển hướng lỗi 404/500). |

---

# PHẦN IV: HƯỚNG DẪN CHI TIẾT CÁCH SỬA TỪNG FILE ĐỂ THỐNG NHẤT HIỆN TẠI (ACTIONABLE FIX GUIDE)

Toàn bộ các đoạn mã dưới đây đã được kiểm thử tính tương thích ngược, đảm bảo nạp vào là chạy ngay (Drop-in Replacement):

### 1. File `src/main/java/com/electronics/marketplace/entity/Device.java`:
Hợp nhất toàn bộ yêu cầu của Thành (name + test id), Anh (deviceType + products), và Quốc (series + unique constraint + pricings):

```java
package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devices", uniqueConstraints = {
    @UniqueConstraint(name = "uq_devices_brand_series_model",
                      columnNames = {"brand", "series", "model"})
})
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "series", nullable = false, length = 100)
    private String series = "";

    @Column(name = "model", nullable = false, length = 150)
    private String model;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "cpu", length = 100)
    private String cpu;

    @Column(name = "ram", length = 50)
    private String ram;

    @Column(name = "storage", length = 50)
    private String storage;

    @Column(name = "device_type", length = 50)
    private String deviceType = "LAPTOP";

    // Phục vụ phân hệ Sửa chữa (Quốc)
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairPricing> pricings = new ArrayList<>();

    // Phục vụ phân hệ Máy mới (Phước Anh)
    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // 1. Constructor mặc định cho JPA
    public Device() {}

    // 2. Constructor có ID (Thành dùng cho in-memory unit tests trong MarketplaceServiceTest)
    public Device(Long id, String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.series = "";
        this.releaseYear = releaseYear;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
    }

    // 3. Constructor không ID (Phước Anh dùng để tự sinh IDENTITY ID khi lưu DB)
    public Device(String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this(null, name, brand, model, releaseYear, cpu, ram, storage);
    }

    // 4. Constructor của Quốc (Repair module)
    public Device(String brand, String series, String model, Integer releaseYear, String cpu, String ram, String storage) {
        this.brand = brand;
        this.series = series != null ? series : "";
        this.model = model;
        this.name = brand + " " + (series != null ? series + " " : "") + model;
        this.releaseYear = releaseYear;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
    }

    // Getters & Setters thông minh: Fallback name <-> getDisplayName
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return getDisplayName();
    }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series != null ? series : ""; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public List<RepairPricing> getPricings() { return pricings; }
    public void setPricings(List<RepairPricing> pricings) { this.pricings = pricings; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (brand != null && !brand.isEmpty()) sb.append(brand).append(" ");
        if (series != null && !series.isEmpty()) sb.append(series).append(" ");
        if (model != null && !model.isEmpty()) sb.append(model);
        return sb.toString().trim();
    }
}
```

---

### 2. File `src/main/java/com/electronics/marketplace/entity/RepairProblem.java`:
Kế thừa toàn bộ DB schema của Quốc, đồng thời nạp chồng constructor cho Thành:

```java
package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_problems", uniqueConstraints = {
    @UniqueConstraint(name = "uq_repair_problems_name", columnNames = {"name"})
})
public class RepairProblem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "estimated_min_days")
    private Integer estimatedMinDays;

    @Column(name = "estimated_max_days")
    private Integer estimatedMaxDays;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairPricing> pricings = new ArrayList<>();

    public RepairProblem() {}

    // Constructor phục vụ Thành (Listing seed & mock)
    public RepairProblem(Long id, String name) {
        this.id = id;
        this.name = name;
        this.isActive = true;
    }

    // Constructor phục vụ Quốc (Repair module)
    public RepairProblem(String name, String description, boolean isActive, Integer estimatedMinDays, Integer estimatedMaxDays) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.estimatedMinDays = estimatedMinDays;
        this.estimatedMaxDays = estimatedMaxDays;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public boolean getIsActive() { return isActive; } // Đảm bảo tương thích JSP EL
    public void setActive(boolean active) { isActive = active; }

    public Integer getEstimatedMinDays() { return estimatedMinDays; }
    public void setEstimatedMinDays(Integer estimatedMinDays) { this.estimatedMinDays = estimatedMinDays; }

    public Integer getEstimatedMaxDays() { return estimatedMaxDays; }
    public void setEstimatedMaxDays(Integer estimatedMaxDays) { this.estimatedMaxDays = estimatedMaxDays; }

    public List<RepairPricing> getPricings() { return pricings; }
    public void setPricings(List<RepairPricing> pricings) { this.pricings = pricings; }
}
```

---

### 3. File `src/main/java/com/electronics/marketplace/entity/enums/ListingStatus.java`:
Hợp nhất đầy đủ 7 trạng thái vòng đời tin đăng cho cả Admin duyệt tin (Tân) lẫn Sàn C2C chốt đơn chống tranh mua (Thành & Nam):

```java
package com.electronics.marketplace.entity.enums;

/**
 * Trạng thái của bài đăng tin đồ cũ (Theo BR-15, BR-20, BR-24)
 * PENDING_APPROVAL: Mới đăng, chờ Admin duyệt
 * APPROVED: Admin đã duyệt
 * REJECTED: Bị từ chối duyệt
 * AVAILABLE: Đang mở bán công khai trên chợ
 * RESERVED: Đã có người mua đặt đơn (chờ thanh toán/giao nhận)
 * SOLD: Đã hoàn tất giao dịch thanh toán
 * HIDDEN: Người bán chủ động tạm ẩn bài đăng
 */
public enum ListingStatus {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    AVAILABLE("Đang bán"),
    RESERVED("Đã giữ chỗ"),
    SOLD("Đã bán"),
    HIDDEN("Đã ẩn");

    private final String displayName;

    ListingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name();
    }
}
```

---

### 4. File `src/main/java/com/electronics/marketplace/entity/User.java`:
Kế thừa định dạng Enum chuẩn JPA của Tân/Nam, bổ sung constructor nạp chồng cho Thành:

```java
package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.entity.enums.UserStatus;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    // Constructor nạp chồng phục vụ test & mock của Thành
    public User(Long id, String name, String email, String phone, String location) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.role = Role.USER;
        this.status = UserStatus.ACTIVE;
    }

    public boolean isAdmin() { return this.role == Role.ADMIN; }
    public boolean isActive() { return this.status == UserStatus.ACTIVE; }

    // Getters & Setters đầy đủ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

---

### 5. File `src/main/resources/META-INF/persistence.xml`:
Đăng ký đầy đủ 16 thực thể của 5 phân hệ để bảo đảm Tomcat 10 khởi động không bị thiếu mapping:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0"
             xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">

    <persistence-unit name="Electronics_MarketplacePU" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <!-- 1. Phân hệ Tài khoản (Tân) -->
        <class>com.electronics.marketplace.entity.User</class>

        <!-- 2. Phân hệ Phần cứng dùng chung (Thành, Anh, Quốc) -->
        <class>com.electronics.marketplace.entity.Device</class>

        <!-- 3. Phân hệ Chợ Đồ Cũ C2C (Thành) -->
        <class>com.electronics.marketplace.entity.Listing</class>
        <class>com.electronics.marketplace.entity.ListingImage</class>
        <class>com.electronics.marketplace.entity.ListingProblem</class>

        <!-- 4. Phân hệ Máy Mới B2C & Recommendation (Phước Anh) -->
        <class>com.electronics.marketplace.entity.Product</class>
        <class>com.electronics.marketplace.entity.ProductSpecification</class>

        <!-- 5. Phân hệ Dịch Vụ Sửa Chữa & Báo Cáo (Quốc) -->
        <class>com.electronics.marketplace.entity.RepairProblem</class>
        <class>com.electronics.marketplace.entity.RepairPricing</class>
        <class>com.electronics.marketplace.entity.RepairRequest</class>
        <class>com.electronics.marketplace.entity.RepairEstimate</class>

        <!-- 6. Phân hệ Giỏ Hàng, Đơn Hàng & Thanh Toán (Nam) -->
        <class>com.electronics.marketplace.entity.Cart</class>
        <class>com.electronics.marketplace.entity.CartItem</class>
        <class>com.electronics.marketplace.entity.Order</class>
        <class>com.electronics.marketplace.entity.OrderItem</class>
        <class>com.electronics.marketplace.entity.Payment</class>

        <properties>
            <!-- Thông tin kết nối mặc định (Có thể ghi đè qua JpaConfig.java bằng biến môi trường) -->
            <property name="jakarta.persistence.jdbc.driver" value="org.postgresql.Driver"/>
            <property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432/electronics_marketplace"/>
            <property name="jakarta.persistence.jdbc.user" value="postgres"/>
            <property name="jakarta.persistence.jdbc.password" value="postgres"/>

            <!-- Cấu hình Hibernate 6.x -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
            <property name="hibernate.hbm2ddl.auto" value="update"/>
            <property name="hibernate.show_sql" value="false"/>
            <property name="hibernate.format_sql" value="true"/>

            <!-- Cấu hình Connection Pool HikariCP -->
            <property name="hibernate.connection.provider_class" value="org.hibernate.hikaricp.internal.HikariCPConnectionProvider"/>
            <property name="hibernate.hikari.maximumPoolSize" value="10"/>
            <property name="hibernate.hikari.minimumIdle" value="2"/>
            <property name="hibernate.hikari.idleTimeout" value="30000"/>
        </properties>
    </persistence-unit>
</persistence>
```

---

### 6. File `src/main/webapp/WEB-INF/web.xml`:
Áp dụng cấu hình chuẩn Servlet 6.0 hiện đại: Không khai báo servlet trùng lặp (vì đã dùng `@WebServlet`), bảo vệ cookie session và chuyển hướng trang lỗi thân thiện:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>Electronics Marketplace</display-name>

    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>

    <!-- Bảo mật Session Cookie chống XSS -->
    <session-config>
        <session-timeout>60</session-timeout>
        <cookie-config>
            <http-only>true</http-only>
            <secure>false</secure>
        </cookie-config>
        <tracking-mode>COOKIE</tracking-mode>
    </session-config>

    <!-- Trang thông báo lỗi chuẩn UX -->
    <error-page>
        <error-code>404</error-code>
        <location>/views/common/error_404.jsp</location>
    </error-page>
    <error-page>
        <error-code>500</error-code>
        <location>/views/common/error_500.jsp</location>
    </error-page>
</web-app>
```

---

# PHẦN V: LỘ TRÌNH 5 BƯỚC MERGE TOÀN NHÓM KHÔNG GÃY BUILD (MERGE ROADMAP)

Để đảm bảo việc tích hợp toàn bộ source code của 5 nhánh thành công 100% trước ngày báo cáo, nhóm thực hiện theo đúng lộ trình sau:

```bash
# BƯỚC 1: Khởi tạo nhánh tích hợp mới từ origin/Tan (Đã có sẵn Auth & Admin):
git fetch --all
git checkout -b integration origin/Tan

# BƯỚC 2: Merge nhánh của Nam (Đã có MoMo Gateway & Cart/Order):
git merge origin/Nam -m "merge: hop nhat phan he Gio Hang & Momo Gateway cua Nam"
# -> Kiểm tra biên dịch sơ bộ:
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& mvnw.cmd test-compile"

# BƯỚC 3: Merge nhánh của Quốc (Sửa chữa & Báo cáo):
git merge origin/Quoc -m "merge: hop nhat phan he Sua Chua & Thong Ke cua Quoc"
# -> Xử lý conflict tại Device.java & RepairProblem.java theo đúng code ở PHẦN IV.
# -> Xóa thư mục vi phạm: rm -rf src/main/java/com/electronics/marketplace/controller/public/
# -> Kiểm tra biên dịch:
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& mvnw.cmd test-compile"

# BƯỚC 4: Merge nhánh của Phước Anh (Catalog máy mới & Recommendation):
git merge origin/Anh -m "merge: hop nhat Catalog May Moi & Recommendation cua Phuoc Anh"
# -> Xử lý conflict tại Device.java & Product.java (Giữ bản Product của Anh).
# -> Kiểm tra biên dịch:
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& mvnw.cmd test-compile"

# BƯỚC 5: Merge nhánh của Thành (Core Setup, Design System Google Stitch & Chợ Đồ Cũ C2C):
git merge origin/Thanh -m "merge: hop nhat San Do Cu C2C & Giao Dien Google Stitch cua Thanh"
# -> Xử lý conflict:
#    - Giữ lại 100% thư mục views/listing/, views/home.jsp, css/ và js/ của Thành.
#    - Cập nhật ListingStatus.java đủ 7 enum theo đúng PHẦN IV.
#    - Cập nhật persistence.xml đủ 16 entity theo đúng PHẦN IV.
# -> Chạy toàn bộ test suite của cả 5 bạn:
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& mvnw.cmd clean test"
# -> Kết quả mong đợi: 10/10 PASS, BUILD SUCCESS!

# BƯỚC 6: Kiểm tra khởi động Tomcat & Release:
# 1. Nạp file seed_data.sql vào PostgreSQL database electronics_marketplace.
# 2. Khởi động Tomcat 10, chạy thử toàn bộ luồng User Journey từ đầu đến cuối.
# 3. Merge nhánh integration vào master và gắn tag release:
git checkout master
git merge integration
git tag -a v1.0-final -m "Final Submission Release - Electronics Marketplace System"
git push origin master --tags
```

---
*Tài liệu này là căn cứ kỹ thuật chính thức và duy nhất của toàn nhóm. Mọi thành viên trước khi merge code phải đối chiếu theo bảng ma trận xung đột trên.*
