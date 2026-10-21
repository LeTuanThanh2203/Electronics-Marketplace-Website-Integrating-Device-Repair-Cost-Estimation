# HỒ SƠ PHỐI HỢP DỰ ÁN — PHẠM PHƯỚC ANH (CATALOG MÁY MỚI & AI GỢI Ý)

> **Thành viên:** Phạm Phước Anh  
> **Phân hệ phụ trách:** Cửa Hàng Máy Mới Chính Hãng B2C, Ma trận Thông số Kỹ thuật (`ProductSpecification`), Thuật toán Gợi ý Sản phẩm (Matching Score Recommendation Engine), Master Data Sản phẩm.  
> **Nhánh Git:** `origin/Anh`  
> **Trạng thái:** Tích hợp hoàn tất, biên dịch 100% `BUILD SUCCESS`.

---

## 1. CÁC ĐIỀU CHỈNH PHỐI HỢP (ĐÃ HOÀN TẤT & ĐỒNG BỘ)

* **Liên kết Mua hàng sang Giỏ hàng của Nam (`CartServlet`):**
  - Cả 2 nút **"THÊM GIỎ HÀNG"** và **"MUA NGAY"** trên `detail.jsp` và `list.jsp` đã được bọc thẻ `<form action="${ctx}/cart?action=add" method="POST">` mang theo `<input type="hidden" name="productId" value="${product.id}">`.
  - Icon giỏ hàng trên Header chuyển từ `href="#"` thành `href="${ctx}/cart"`.
* **Xóa bỏ Ô chọn số lượng (Khớp quy tắc BR-45):**
  - Quy tắc nghiệp vụ BR-45 quy định sản phẩm thiết bị điện tử khi thêm vào giỏ luôn có `quantity = 1`.
  - Đã xóa bỏ hoàn toàn ô `#qtyInput`, hai nút tăng giảm và hàm `changeQty()` trên `detail.jsp` để đảm bảo giao diện không hứa hẹn sai với backend.
* **Chuẩn hóa Package Controller:**
  - Chuyển `ProductServlet` từ thư mục cấm `controller.public` (từ khóa cấm của Java) sang package chuẩn: `com.electronics.marketplace.controller.storefront`.
  - Hỗ trợ phân tích URL thông minh (`pathInfo`): Tự động nhận diện `/product/list`, `/product/detail`, `/product/compare`, `/product/recommend` mà không bắt buộc query string. Nếu thiếu ID máy, tự động hiển thị sản phẩm mẫu đầu tiên.
* **Tích hợp Thư viện Jackson cho AJAX Recommendation:**
  - Bổ sung `com.fasterxml.jackson.core:jackson-databind:2.17.0` vào `pom.xml`.
  - Thêm annotation `@JsonIgnore` trên `Product.getDevice()` và `ProductSpecification.getProduct()` để triệt tiêu lỗi lặp vô hạn (circular reference) khi trả JSON cho modal "AI Gợi ý máy".
* **Loại bỏ Dữ liệu Giả (Clean Fail-Safe):**
  - Gỡ bỏ `inMemoryStore` và các lệnh `System.err.println` trong `JpaProductRepository`. Khi database có lỗi, cho phép ngoại lệ nổi lên tầng Controller để xử lý tập trung qua `ServiceException`.
  - Sửa các getter phần cứng (`getCpu()`, `getRamGb()`, `getStorageGb()`...): Trả về `null` thay vì số hardcode 16GB/512GB; trên JSP dùng `<c:if test="${not empty ...}">` để ẩn dòng nếu thiếu spec.

---

## 2. CÁC TÌNH TRẠNG CONFLICT CÓ THỂ XẢY RA & GIẢI PHÁP

| Điểm Xung Đột | Nguyên Nhân Phát Sinh | Giải Pháp Đã Thực Thi |
|---|---|---|
| **Lỗi biên dịch package `controller.public`** | Java Language Specification (§3.9) cấm dùng từ khóa `public` để đặt tên package. Lệnh `mvn compile` lập tức báo lỗi `<identifier> expected`. | Xóa sạch thư mục `controller/public/`. Sử dụng duy nhất `com.electronics.marketplace.controller.storefront`. |
| **Xung đột Entity `Device.java` với Thành & Quốc** | Anh cần `deviceType` và quan hệ `@OneToMany List<Product> products`; Thành cần constructor có `id` cho test; Quốc cần `series`. Trùng số lượng tham số 7 chuỗi gây lỗi *Constructor already defined*. | Hợp nhất 3 constructor khác biệt signature: (1) Constructor mặc định, (2) Constructor có ID (Thành), (3) Constructor có `series` (Quốc). Giữ cả quan hệ `products` và `pricings`. |
| **Xung đột Entity `Product.java` với Nam** | Nam từng tạo stub tạm gồm 6 trường; Phước Anh xây dựng entity đầy đủ 20 trường. | **Lấy 100% Entity của Phước Anh:** Bản của Anh là tập mẹ (superset) chứa trọn vẹn các trường của Nam. Phía Nam chỉ cần bổ sung `product.setBrand("TestBrand")` trong `OrderServiceTest`. |
| **Xung đột các file cấu hình dùng chung** | Bản nhánh `Anh` từng hardcode mật khẩu trong `persistence.xml` và nuốt ngoại lệ trong `JpaConfig.java`. | Giữ nguyên các file cấu hình từ HEAD (`JpaConfig`, `persistence.xml`, `web.xml`) để bảo toàn cơ chế đọc biến môi trường và kết nối HikariCP của hệ thống. |
| **Servlet rỗng đè mapping (HTTP 404 / 405)** | Tồn tại file rỗng `controller/pub/ProductServlet.java` tranh chấp mapping URL `/products` với servlet thật của Phước Anh. | Xóa bỏ vĩnh viễn servlet rỗng; chỉ giữ lại servlet hoàn chỉnh trong package `storefront`. |

---

## 3. YÊU CẦU PHỐI HỢP & CHECKLIST HÀNH ĐỘNG

### Đường dẫn kiểm thử module Máy mới:
* Danh mục sản phẩm máy mới: `http://localhost:8080/products`
* Chi tiết sản phẩm: `http://localhost:8080/products?action=detail&id=1`
* So sánh đối đầu 2 máy: `http://localhost:8080/products?action=compare&ids=1,2`
* API AI Gợi ý: `http://localhost:8080/products?action=recommend&budget=30000000&need=GAMING`

### Đối với các thành viên khác:
- [ ] **Thành Lê (Home):** Trong `HomeServlet`, gọi `ProductService.getRecommendedProducts()` và nạp vào `request.setAttribute("recommendedProducts", ...)` để khối máy mới trên `home.jsp` hiển thị dữ liệu thật.
- [ ] **Nam (Cart):** Giữ nguyên mapping `/cart` đón nhận request POST `action=add` kèm `productId` từ các trang xem máy mới của Phước Anh.
- [ ] **Tân (Admin):** Quản lý tài khoản Admin để có quyền thêm, cập nhật trạng thái `ProductStatus` (`AVAILABLE`, `OUT_OF_STOCK`, `DISCONTINUED`).

### Việc Phước Anh cần duy trì:
- [x] Không sửa đổi các file dùng chung (`JpaConfig.java`, `web.xml`, `pom.xml`) trừ khi đã thông báo và thống nhất cả nhóm.
- [x] Đảm bảo file `seed_data.sql` có đủ dữ liệu mẫu cho các bảng `devices`, `products`, và `product_specifications`.
- [x] Đảm bảo các thuộc tính quan hệ hai chiều luôn có `@JsonIgnore` nếu trả ra AJAX để không làm crash Jackson parser.
