# BÁO CÁO CHI TIẾT CÔNG VIỆC THỰC HIỆN - PHẠM PHƯỚC ANH

- **Thành viên thực hiện:** Phạm Phước Anh
- **Phân hệ phụ trách:** **Cửa Hàng Máy Mới & Thuật toán Gợi ý Sản phẩm (B2C Catalog & Recommendation Engine)**
- **Nhánh Git (Branch):** `Anh`
- **Mục đích tài liệu:** Lưu trữ chi tiết toàn bộ kiến trúc, mã nguồn, logic nghiệp vụ và hướng dẫn mở rộng để các thành viên trong nhóm hoặc các trợ lý AI khác có thể dễ dàng đọc hiểu, bảo trì và phát triển tiếp.

---

## 1. PHẠM VI TRÁCH NHIỆM & NGUYÊN TẮC BẢO TOÀN DỰ ÁN

1. **Phạm vi sở hữu mã nguồn:**
   - **Entities:** `Product`, `Device`, `ProductSpecification`, `ProductStatus`, `SpecDataType`.
   - **Repositories:** `ProductRepository`, `DeviceRepository`, `JpaProductRepository`, `MockProductData`.
   - **Services:** `ProductService`, DTOs (`RecommendationRequest`, `RecommendedProduct`).
   - **Controllers:** `ProductServlet` (mapped: `/products`, `/product`).
   - **Views (JSP):** `WEB-INF/views/product/list.jsp`, `detail.jsp`, `compare.jsp`, `views/home.jsp`, `index.jsp`.
2. **Nguyên tắc độc lập tuyệt đối (Strict Isolation):**
   - Không can thiệp, không ghi đè vào các phân hệ của các bạn khác (`User`, `Admin`, `Cart`, `Listing`, `Order`, `Repair`, `PasswordUtil`).
   - Mọi chỉnh sửa đều đảm bảo dự án luôn biên dịch thành công (`mvn compile` đạt `BUILD SUCCESS`).

---

## 2. KIẾN TRÚC & CÔNG NGHỆ ÁP DỤNG

Dự án tuân thủ nghiêm ngặt mô hình chuẩn theo giáo trình Murach's Java Servlets and JSP và JPA:
- **Kiến trúc phân lớp:** `View (JSP)` $\leftrightarrow$ `Controller (Servlet)` $\leftrightarrow$ `Service (Business & AI)` $\leftrightarrow$ `Repository (JPA / DAO)` $\leftrightarrow$ `Database (PostgreSQL / HikariCP)`.
- **Java:** Phiên bản Java 17+, Jakarta EE 10 (Servlet 6.0, JSP 3.0, JSTL 3.0).
- **ORM & Persistence:** Hibernate 6.4.4.Final kết hợp Connection Pooling qua HikariCP 5.1.0. Quản lý EntityManagerFactory tập trung tại `JpaConfig.java`.
- **Giao diện (Frontend):** Thiết kế theo phong cách công nghiệp tối giản **Swiss Industrial / High-Contrast** (Tailwind CSS, Font Space Grotesk & Space Mono, tông màu cam kỹ thuật `#F05A28`, đen `#111315`, xám thẻ `#F6F6F4`). Hoàn toàn không dùng scriptlet `<% %>`, 100% sử dụng thẻ JSTL `<c:*>`, `<fmt:*>` và EL (`${...}`).

---

## 3. CHI TIẾT CÁC THÀNH PHẦN ĐÃ XÂY DỰNG

### 3.1. Tầng Thực thể Dữ liệu (Domain Entities) - Khớp 100% Lucidchart UML

| File | Package | Mô tả chi tiết |
| :--- | :--- | :--- |
| **`Product.java`** | `...entity` | Đại diện sản phẩm máy mới. Khớp bảng UML: `id`, `adminId`, `name`, `brand`, `price`, `status`, liên kết `@ManyToOne` với `Device` và `@OneToMany` với `ProductSpecification`. Bổ sung các trường thương mại: `sku`, `series`, `originalPrice`, `discountTag`, `badge`, `warranty`, `description`, `targetNeed`, `stockQuantity`. Bổ sung các getter phần cứng tiện ích cho EL: `getCpu()`, `getRamGb()`, `getStorageGb()`, `getScreenSize()`, `getScreenResolution()`, `getGpu()`. |
| **`Device.java`** | `...entity` | Thiết bị phần cứng quy chuẩn: `id`, `name`, `brand`, `model`, `releaseYear`, `cpu`, `ram`, `storage`, quan kết `1..N` với `Product`. |
| **`ProductSpecification.java`** | `...entity` | Bảng ma trận thông số kỹ thuật EAV: `id`, `product`, `specKey`, `dataType`, `numericValue`, `textValue`, `unit`. Cung cấp phương thức static tạo nhanh `numberSpec()` và `textSpec()`. |
| **`ProductStatus.java`** | `...entity.enums` | Enum trạng thái sản phẩm: `AVAILABLE`, `OUT_OF_STOCK`, `DISCONTINUED`. |
| **`SpecDataType.java`** | `...entity.enums` | Enum kiểu dữ liệu thông số: `NUMBER`, `TEXT`, `BOOLEAN`. |

---

### 3.2. Tầng Truy xuất Dữ liệu (Repository & Persistence)

1. **`JpaConfig.java` & `persistence.xml`:**
   - Cấu hình Persistence Unit: `Electronics_MarketplacePU`.
   - Sử dụng cơ chế Lazy-initialization cho `EntityManagerFactory` giúp khởi động ứng dụng tức thì.
2. **`JpaProductRepository.java`:**
   - Viết chuẩn theo phong cách Murach: `EntityTransaction trans = em.getTransaction()`, các câu truy vấn JPQL tham số hóa (`:brand`, `:minPrice`, `:maxPrice`, `:keyword`) để chống triệt để SQL Injection.
   - Triển khai đầy đủ: `findAll()`, `findById()`, `findByBrand()`, `findByDevice()`, `filter()`, `search()`, `save()`, `update()`, `delete()`.
   - **Cơ chế Fallback thông minh (Fail-safe):** Khi chưa bật cơ sở dữ liệu PostgreSQL (hoặc sai mật khẩu), Repository sẽ tự động bắt ngoại lệ và nạp danh sách 6 sản phẩm mẫu từ `MockProductData.java`, giúp ứng dụng luôn chạy mượt mà để kiểm thử giao diện mà không bị crash.
3. **`MockProductData.java`:**
   - Chứa 6 sản phẩm chuẩn nguyên seal xuất xưởng:
     1. *Dell XPS 13 Plus 9320* (OLED 3.5K, Core i7-1360P, 16GB, 512GB).
     2. *MacBook Air 15 M3 2024* (Apple M3, 16GB, 512GB).
     3. *ASUS ROG Zephyrus G16 OLED 2024* (Core Ultra 9, RTX 4080, 32GB, 1TB).
     4. *Lenovo ThinkPad X1 Carbon Gen 12* (Core Ultra 7, 32GB, 1TB).
     5. *HP Pavilion 14 Plus OLED* (Core i5-1340P, 16GB, 512GB).
     6. *Acer Predator Helios Neo 16* (Core i7-14700HX, RTX 4060, 16GB, 512GB).

---

### 3.3. Tầng Nghiệp vụ (Service & Thuật toán Gợi ý AI)

- **`ProductService.java`:**
  - Cung cấp các hàm nghiệp vụ: lọc sản phẩm nâng cao, lấy chi tiết, và tính năng so sánh đa sản phẩm `compareProducts(List<Long> ids)`.
- **Thuật toán Gợi ý (Matching Score Recommendation Engine):**
  - **Mục tiêu:** Nhận vào nhu cầu của khách hàng qua DTO `RecommendationRequest` (Ngân sách `budget`, Nhu cầu `need`: OFFICE / GAMING / GRAPHICS, Yêu cầu `minRam`, Thương hiệu ưu tiên `brand`).
  - **Công thức tính điểm (Thang 100 điểm):**
    $$\text{Tổng điểm} = S_{\text{budget}} (40đ) + S_{\text{need}} (30đ) + S_{\text{ram}} (20đ) + S_{\text{brand}} (10đ)$$
    - *Ngân sách (40đ):* Trong tầm giá hoặc chênh lệch $\le 10\%$ được điểm tối đa.
    - *Nhu cầu sử dụng (30đ):* So khớp chính xác phân khúc máy (Gaming / Văn phòng / Đồ họa).
    - *Dung lượng RAM (20đ):* Đạt hoặc vượt dung lượng RAM mong muốn.
    - *Thương hiệu ưu tiên (10đ):* Khớp hãng yêu thích.
  - Kết quả trả về danh sách `RecommendedProduct` sắp xếp giảm dần theo điểm số kèm lý do gợi ý chi tiết (ví dụ: *"Hoàn hảo trong tầm giá", "Khớp phân khúc GAMING"*).

---

### 3.4. Tầng Điều khiển (Controller & Routing)

- **`ProductServlet.java`:**
  - Vị trí: `com.electronics.marketplace.controller.storefront` (URL pattern: `/products`, `/product`).
  - **Quy tắc điều hướng (Action Routing):**
    - `action=list` (mặc định): Trả về trang danh mục sản phẩm, hỗ trợ lọc theo hãng, khoảng giá, nhu cầu, phân trang và sắp xếp.
    - `action=detail&id=X`: Trả về trang chi tiết sản phẩm, tải toàn bộ ma trận thông số kỹ thuật `specifications`.
    - `action=compare&ids=1,2`: Trả về trang so sánh đối đầu thông số kỹ thuật.
    - `action=recommend`: Tiếp nhận thông số tư vấn, gọi `ProductService` và trả về kết quả gợi ý.
    - `action=search&q=...`: Tìm kiếm nhanh sản phẩm.
  - **Hỗ trợ RESTful / AJAX:** Tự động phát hiện yêu cầu AJAX (`format=json` hoặc Header `Accept: application/json`) và dùng Jackson `ObjectMapper` để trả về JSON cho các ứng dụng frontend gọi ngầm.

---

### 3.5. Tầng Giao diện Người dùng (JSP Views & Design System)

1. **`src/main/webapp/WEB-INF/views/product/list.jsp`:**
   - Thanh công bố trên cùng (Ticker bar) chạy chữ chuẩn công nghiệp.
   - Header điều hướng, ô tìm kiếm nhanh, nút mở modal "AI Trợ lý Gợi ý Máy".
   - 3 khối Bento thể loại chính (FLAGSHIP LAPTOPS, ULTRA WORKSTATION, ESSENTIAL PHONES).
   - Sidebar bộ lọc nhiều tiêu chí: Thương hiệu (Apple, Dell, ASUS, Lenovo, HP, Acer), Khoảng giá, Nhu cầu (Văn phòng, Gaming, Đồ họa).
   - Lưới sản phẩm (Product Grid) hiển thị ảnh nguyên bản, nhãn SEAL 100%, mã SKU, cấu hình tóm tắt, giá niêm yết và nút CTA.
   - Modal gợi ý máy thông minh tích hợp tính toán điểm trực tiếp bằng JavaScript gọi Servlet API.
2. **`src/main/webapp/WEB-INF/views/product/detail.jsp`:**
   - Breadcrumb điều hướng phân cấp.
   - Khung trình diễn hình ảnh sản phẩm với nhãn bảo chứng chính hãng.
   - Khối chọn số lượng, nút "MUA NGAY (GIAO TẬN NƠI)" và "THÊM GIỎ HÀNG".
   - **Bảng ma trận thông số phần cứng tiêu chuẩn xuất xưởng:** Liệt kê chi tiết từng dòng CPU, RAM, Ổ cứng, GPU, Màn hình, Tần số quét, Trọng lượng.
3. **`src/main/webapp/WEB-INF/views/product/compare.jsp`:**
   - Ma trận so sánh đối đầu giữa 2 hoặc nhiều sản phẩm theo từng tiêu chí kỹ thuật: CPU, RAM, SSD, GPU, Màn hình, Nhu cầu, Giá bán và Bảo hành.
4. **`src/main/webapp/WEB-INF/views/home.jsp` & `index.jsp`:**
   - Trang chủ tích hợp điều hướng sang Cửa hàng máy mới và `index.jsp` tự động chuyển tiếp về `/products`.

---

## 4. HƯỚNG DẪN DÀNH CHO THÀNH VIÊN KHÁC / AI TIẾP QUẢN

### 4.1. Cách Khởi chạy và Kiểm thử Web
1. Mở dự án trong **IntelliJ IDEA**.
2. Cấu hình **Run/Debug Configurations**:
   - Thêm cấu hình mới: **Tomcat Server** $\rightarrow$ **Local**.
   - Tab **Deployment**: Nhấn dấu `+` $\rightarrow$ chọn **Artifact...** $\rightarrow$ chọn `Electronics_Marketplace_Project:war exploded`.
   - Đặt **Application context**: `/`.
3. Bấm **Run** hoặc **Debug**.
4. Truy cập các đường dẫn sau trên trình duyệt:
   - Danh sách máy mới: `http://localhost:8080/products`
   - Chi tiết sản phẩm: `http://localhost:8080/products?action=detail&id=1`
   - So sánh máy: `http://localhost:8080/products?action=compare&ids=1,2`
   - Gọi API gợi ý: `http://localhost:8080/products?action=recommend&budget=35000000&need=GAMING`

### 4.2. Lưu ý về Cấu trúc Package
- **Đồng bộ với nhánh Nam:** Toàn bộ Servlet storefront công khai phục vụ khách hàng được gom vào package chuẩn:
  - `com.electronics.marketplace.controller.storefront` (gồm `ProductServlet.java` và `ListingServlet.java`).
- Vui lòng **không sử dụng** thư mục package có tên là `public` (vì `public` là từ khóa cấm trong Java JLS §3.9) hoặc `catalog`.

### 4.3. Kết nối Cơ sở Dữ liệu Thật (PostgreSQL)
- Cấu hình thông tin kết nối tại: `src/main/resources/META-INF/persistence.xml`.
- Các bảng liên quan đến module máy mới gồm: `devices`, `products`, `product_specifications`.
- Khi PostgreSQL hoạt động, `JpaProductRepository` sẽ tự động truy vấn trực tiếp vào database; khi DB tắt, hệ thống tự động chạy qua Mock Data mà không làm gián đoạn việc phát triển giao diện.
