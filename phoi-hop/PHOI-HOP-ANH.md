# 💻 SỔ TAY PHỐI HỢP & XỬ LÝ CONFLICT — PHẠM PHƯỚC ANH
> **Phân hệ phụ trách:** Cửa Hàng Máy Mới B2C (`/product`), Thuật toán Gợi ý AI (Matching Score) & So sánh EAV  
> **Thành viên:** Phạm Phước Anh | **Nhánh Git:** `Anh` / `integration`  
> **Trọng tâm:** Kết nối Giỏ hàng Nam, Chia sẻ Device với Thành & Master Seed Data

---

## 1. PHẠM VI SỞ HỮU FILE (RANH GIỚI TRÁCH NHIỆM)

| Tầng kiến trúc | Phước Anh sở hữu độc quyền (Chỉ Anh sửa) | Phối hợp với thành viên khác |
|---|---|---|
| **Entities & Enums** | `Product.java`, `ProductSpecification.java`<br>Enums: `ProductStatus.java`, `SpecDataType.java` | Liên kết `@ManyToOne` tới `Device.java` (Thành sở hữu) |
| **Service & Engine** | `ProductService.java`, DTOs (`RecommendationRequest`, `RecommendedProduct`), `MockProductData.java` | Đẩy dữ liệu sang `HomeServlet` (Thành) hiển thị |
| **Controller** | `ProductServlet.java` (đón nhận `/product`, `/products`, `/compare`) | Nhận tham số lọc, so sánh và gọi Service |
| **Giao diện JSP** | `views/product/list.jsp`, `detail.jsp`, `compare.jsp` | Bọc Form gửi dữ liệu sang CartServlet (Nam) |

---

## 2. LỊCH SỬ THAY ĐỔI (CHANGELOG)

* **2026-10-19:** Xây dựng Engine gợi ý 100đ: $S_{\text{budget}}(40đ) + S_{\text{need}}(30đ) + S_{\text{ram}}(20đ) + S_{\text{brand}}(10đ)$.
* **2026-10-19:** Tích hợp `MockProductData.java` (cơ chế Fail-safe tự kích hoạt khi PostgreSQL chưa chạy để test UI mượt mà).
* **2026-10-20:** **Xử lý điểm vỡ nút Mua với Nam:** Chuyển đổi `<button type="button">` tĩnh trên `detail.jsp` thành form POST chuẩn gửi tới `${ctx}/cart?action=add`.
* **2026-10-20:** Xóa ô nhập số lượng `#qtyInput` trên giao diện theo đúng quy tắc nghiệp vụ BR-45 (mỗi máy bán lẻ số lượng = 1).
* **2026-10-21:** Hợp nhất vào nhánh `integration` (Commit `e442c14`), bổ sung `jackson-databind:2.17.0` vào `pom.xml`.

---

## 3. MA TRẬN XUNG ĐỘT (CONFLICTS & GIẢI PHÁP)

> [!TIP]
> Tóm tắt xung đột và hợp đồng giao tiếp giữa Phước Anh với các thành viên:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 BẢN ĐỒ XUNG ĐỘT CỦA PHƯỚC ANH                          │
├──────────────┬───────────────────────────────┬─────────────────────────────────────────┤
│ Đối tác      │ Điểm xung đột kỹ thuật        │ Giải pháp chốt & File cần sửa           │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Huỳnh Nam │ • Nút "Thêm giỏ hàng" bị liệt │ • Sửa `detail.jsp`: bọc thẻ <form> POST │
│ (Cart/Order) │ • Dư thừa ô số lượng (qty)    │   vào `${ctx}/cart?action=add`.         │
│              │ • Thiếu productId khi submit  │ • Xóa ô `#qtyInput` (BR-45 quantity=1). │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Lê Thành  │ • Tranh chấp `Device.java`    │ • Thành giữ quyền sở hữu `Device.java`. │
│ (C2C/Core)   │ • Cắm AI Recom vào Trang chủ  │ • Anh chỉ `@ManyToOne`, không đổi cột.  │
│              │                               │ • Gửi `recommendedProducts` sang Home.  │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Ca Ngọc Tân│ • Phân mảnh file cấu hình    │ • Lấy `JpaConfig.java` và               │
│ (Auth/Config)│ • Lỗi thư viện JSON           │   `persistence.xml` chuẩn của Nam/Tân.  │
│              │                               │ • Pom.xml thêm `jackson-databind`.      │
└──────────────┴───────────────────────────────┴─────────────────────────────────────────┘
```

---

## 4. HƯỚNG DẪN THỰC THI — NÊN SỬA & TRÁNH SỬA FILE NÀO

### 🟢 File Phước Anh NÊN SỬA khi tinh chỉnh nghiệp vụ:
* `src/main/webapp/WEB-INF/views/product/detail.jsp`: Form "Thêm giỏ hàng" gửi sang Nam (phải giữ nguyên `name="productId"`):
  ```html
  <form method="post" action="${pageContext.request.contextPath}/cart?action=add">
      <input type="hidden" name="productId" value="${product.id}">
      <button type="submit">THÊM GIỎ HÀNG</button>
  </form>
  ```
* `src/main/java/com/electronics/marketplace/service/ProductService.java`: Công thức điểm gợi ý hoặc so sánh sản phẩm.
* `src/main/resources/database/schema_and_seed_marketplace_products.sql`: Cập nhật thêm sản phẩm mẫu.

### 🔴 File Phước Anh TUYỆT ĐỐI KHÔNG TỰ Ý SỬA:
* `src/main/java/com/electronics/marketplace/entity/Device.java`: Không thêm constructor làm vỡ mapping của Thành & Quốc.
* `src/main/resources/META-INF/persistence.xml`: Dùng cấu hình chung tự quét entity.
* `src/main/java/com/electronics/marketplace/controller/user/CartServlet.java`: Do Nam toàn quyền xử lý.

---

## 5. CHECKLIST KIỂM SOÁT NHANH (QUICK CHECKLIST)
- [x] Nút "THÊM GIỎ HÀNG" trên `detail.jsp` gửi đúng `POST ${ctx}/cart?action=add` kèm hidden `productId`.
- [x] Không còn ô input chọn số lượng trên chi tiết sản phẩm máy mới.
- [x] Đã kiểm tra `mvn compile` đạt `BUILD SUCCESS` không bị thiếu dependency Jackson.
