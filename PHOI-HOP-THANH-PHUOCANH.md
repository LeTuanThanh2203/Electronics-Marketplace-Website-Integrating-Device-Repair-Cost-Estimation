# Chốt ranh giới & Hợp đồng phối hợp: Core Layout & Chợ Đồ Cũ (Thành) ↔ Catalog Máy Mới & Gợi Ý Cấu Hình (Phước Anh)
> **Tài liệu đối chiếu:** Đã rà soát commit thực tế của Phước Anh (`b6e1c38` trên nhánh `origin/Anh`).  
> **Phiên bản:** v2.0 (Cập nhật sau khi rà soát commit `b6e1c38`).  
> **Nguyên tắc:** Thành cung cấp khung Layout trang chủ, hệ thống Design System từ Google Stitch và Entity `Device` dùng chung; Phước Anh hoàn thiện Catalog máy mới (`Product`) và cắm thuật toán Recommendation Engine vào trang chủ.

---

## 1. TIẾN ĐỘ THỰC TẾ TRÊN NHÁNH `origin/Anh` (ĐÃ RÀ SOÁT)

Phước Anh đã hoàn tất khối lượng công việc rất lớn trong commit `b6e1c38`:
1. **Recommendation Engine:** Đã xây dựng `RecommendationRequest`, `RecommendedProduct`, và `ProductService` tính toán Matching Score gợi ý thiết bị theo ngân sách và nhu cầu.
2. **Tầng JPA Repository:** Đã tạo `JpaProductRepository`, `MockProductData` và `DeviceRepository`.
3. **Entity Model:** Đã ánh xạ JPA hoàn chỉnh cho `Product.java` và `ProductSpecification.java`.
4. **Chỉnh sửa `Device.java`:** Đã thêm quan hệ `@OneToMany(mappedBy = "device") List<Product> products` và trường `deviceType = "LAPTOP"`.

---

## 2. ⚠️ CẢNH BÁO XUNG ĐỘT QUAN TRỌNG TRONG `Device.java` & CÁCH FIX

Khi đối chiếu diff giữa nhánh `Thanh` và commit `b6e1c38` của Phước Anh:
*   **Vấn đề:** Phước Anh đã xóa constructor có ID của Thành:
    ```java
    // Constructor của Thành đang dùng trong MarketplaceService.java:109:
    public Device(Long id, String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage)
    ```
    và thay bằng constructor không có ID:
    ```java
    public Device(String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage)
    ```
*   **Hậu quả:** Nếu merge đè file của Phước Anh, class `MarketplaceService` của Thành sẽ bị lỗi biên dịch ngay lập tức.
*   👉 **Giải pháp hợp nhất (Bắt buộc):**
    File `Device.java` khi merge phải **giữ cả 2 constructors** (overloading):
    1. Constructor đầy đủ tham số kèm `Long id` (để Thành seed dữ liệu in-memory test).
    2. Constructor không có `id` (để Phước Anh lưu mới tự sinh IDENTITY ID).
    3. Giữ các bổ sung mới của Phước Anh: `deviceType` và `List<Product> products`.

---

## 3. Phân chia trách nhiệm

| Hạng mục | Thành (Core Layout & Chợ Đồ Cũ) | Phước Anh (Catalog Máy Mới & Gợi Ý) |
|---|---|---|
| **Servlet Controller** | `/home` → `HomeServlet`<br>`/listing` → `ListingServlet` | `/product` → `ProductServlet` (thuộc package `controller.storefront`) |
| **Service Logic** | `MarketplaceService` | `ProductService` *(thuật toán Matching Score recommendation)* |
| **Entity & Enums** | `Device`, `Listing`, `ListingImage`<br>Enums: `ListingStatus`, `ListingCondition` | `Product`, `ProductSpecification`<br>Enums: `ProductStatus`, `SpecDataType` |
| **Giao diện (JSP)** | `views/home.jsp`, `views/listing/*`<br>Bản thiết kế Google Stitch (`ui_stitch.html`) | `views/product/list.jsp`, `views/product/detail.jsp`<br>`views/product/compare.jsp` |

---

## 4. Các điểm giao tiếp cốt lõi (Integration Points)

### Giao điểm 1: Trang chủ `home.jsp` — Nơi hiển thị kết quả Recommendation

1. **Khung giao diện Thành đã chuẩn bị sẵn:**
   - Trong file [`views/home.jsp`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/src/main/webapp/WEB-INF/views/home.jsp), Thành đã dựng sẵn 3 khu vực lớn:
     - **Hero Section:** Banner và thanh tìm kiếm toàn sàn.
     - **Khối Recommendation & Máy Mới (Dòng 140–280):** Thiết kế sẵn dạng Grid thẻ máy tính hiện đại, có badge "GỢI Ý TỐI ƯU", thông số CPU/RAM/Ổ cứng, giá bán và nút "Thêm vào giỏ".
     - **Khối Chợ Đồ Cũ (C2C):** Danh sách máy cũ của Thành.
2. **Việc Phước Anh cần phối hợp cắm dữ liệu:**
   - Trong `HomeServlet`, Phước Anh gọi `ProductService.getRecommendedProducts(...)` để lấy danh sách máy mới phù hợp nhất.
   - Truyền dữ liệu vào Request Attribute:
     ```java
     request.setAttribute("newProducts", newProductsList);
     request.setAttribute("recommendedProducts", recommendedList);
     ```
   - Trong `home.jsp`, Thành đã viết sẵn vòng lặp JSTL:
     ```html
     <c:forEach items="${recommendedProducts}" var="prod">
         <!-- Card hiển thị sản phẩm máy mới -->
     </c:forEach>
     ```
     Phước Anh chỉ cần đảm bảo đối tượng `Product` có các getter: `prod.name`, `prod.price`, `prod.device.cpu`, `prod.device.ram`.

---

### Giao điểm 2: Dùng chung thực thể phần cứng `Device.java`

1. **Thành đã hoàn tất mapping JPA cho `Device.java`:**
   - Bảng: `devices` (Khóa chính `Long id`).
   - Các trường thuộc tính: `name`, `brand`, `model`, `releaseYear`, `cpu`, `ram`, `storage`.
2. **Khế ước bắt buộc đối với Phước Anh:**
   - Trong `Product.java`, Phước Anh liên kết khóa ngoại:
     ```java
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "device_id", nullable = false)
     private Device device;
     ```

---

### Giao điểm 3: Kế thừa Google Stitch Design System cho Catalog Máy Mới

1. Thành đã định hình toàn bộ phong cách thẩm mỹ thương mại điện tử chuyên nghiệp trong [`ui_stitch.html`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/ui_stitch.html) và [`css/home.css`](file:///c:/Users/LENOVO/Desktop/Study/WEB%20PROGRAMING/Electronics_Marketplace_Project/src/main/webapp/css/home.css):
   - Bảng màu: Nền trắng tối giản, điểm nhấn cam `#FF4A1C`.
   - Cấu trúc: Thẻ hiển thị sản phẩm viền sắc nét, lưới thông số kỹ thuật (Spec Grid), nút bấm chuyển trạng thái.
2. **Khuyến nghị cho Phước Anh:**
   - Khi viết `views/product/list.jsp` và `views/product/detail.jsp`, Phước Anh hãy copy cấu trúc CSS class và Header/Footer từ `home.jsp` của Thành để đảm bảo trải nghiệm người dùng liền mạch giữa kênh Máy Mới (B2C) và Chợ Đồ Cũ (C2C).

---

## 5. Checklist công việc phối hợp cho Phước Anh

- [x] Đã hoàn thành `ProductService` và thuật toán Recommendation (Commit `b6e1c38`).
- [x] Đã map JPA cho `Product` và `ProductSpecification` (Commit `b6e1c38`).
- [ ] Bổ sung lại constructor `Device(Long id, ...)` trong `Device.java` để không làm gãy code của Thành.
- [ ] Đổ dữ liệu từ `ProductService` vào `views/home.jsp` qua `HomeServlet`.
- [ ] Đổi package `ProductServlet` sang `com.electronics.marketplace.controller.storefront`.
