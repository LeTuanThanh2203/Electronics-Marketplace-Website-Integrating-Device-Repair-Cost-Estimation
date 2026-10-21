# 🛒 SỔ TAY PHỐI HỢP & XỬ LÝ CONFLICT — HUỲNH THUYÊN NAM
> **Phân hệ phụ trách:** Giỏ Hàng (`/cart`), Đơn Hàng (`/order`, `/checkout`) & Cổng Thanh Toán MoMo (`/payment`)  
> **Thành viên:** Huỳnh Thuyên Nam | **Nhánh Git:** `Nam` / `integration`  
> **Trọng tâm:** Chống tranh mua BR-20, Tích hợp MoMo Sandbox HMAC-SHA256 & Nền tảng Config

---

## 1. PHẠM VI SỞ HỮU FILE (RANH GIỚI TRÁCH NHIỆM)

| Tầng kiến trúc | Nam sở hữu độc quyền (Chỉ Nam sửa) | Phối hợp với thành viên khác |
|---|---|---|
| **Entities & Enums** | `Cart.java`, `CartItem.java`, `Order.java`, `OrderItem.java`, `Payment.java`<br>Enums: `OrderStatus`, `OrderType`, `PaymentStatus`, `PaymentMethod` | `OrderItem` liên kết `@ManyToOne` với cả `Product` (Anh) và `Listing` (Thành) |
| **Cấu hình Core** | `JpaConfig.java`, `persistence.xml`, `web.xml`, `pom.xml` | Cung cấp cấu hình HikariCP và tự động quét Entity cho toàn đội |
| **Service & Gateway**| `CartService.java`, `OrderService.java`, `MomoGateway.java` | Ký HMAC-SHA256, 3/3 Unit Test PASS |
| **Controller & View**| `CartServlet.java`, `CheckoutServlet.java`, `OrderServlet.java`, `PaymentServlet.java`<br>`views/cart/*`, `views/order/*`, `css/order.css` | Đón nhận Form POST từ trang chi tiết sản phẩm của Thành và Anh |

---

## 2. LỊCH SỬ THAY ĐỔI (CHANGELOG)

* **2026-10-19:** Kéo toàn bộ Entity C2C của Thành về, map trường `OrderItem.listing` để đặt hàng máy cũ.
* **2026-10-19:** Hoàn thiện 4 Servlet lõi (`Cart`, `Checkout`, `Order`, `Payment`), đạt chuẩn kiến trúc MVC.
* **2026-10-20:** Tích hợp cổng thanh toán MoMo Sandbox ký HMAC-SHA256 cho đơn hàng `NEW_DEVICE` (3/3 test pass).
* **2026-10-20:** Triển khai cơ chế chống tranh mua (BR-20) cho chợ C2C bằng câu lệnh Atomic Conditional UPDATE.
* **2026-10-21:** Khôi phục 100% 14 file lõi bị revert nhầm khi merge vào nhánh `integration` (Commit `3664f24`).

---

## 3. MA TRẬN XUNG ĐỘT (CONFLICTS & GIẢI PHÁP)

> [!CAUTION]
> Các điểm nóng xung đột luồng nghiệp vụ giữa Nam và các phân hệ:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   BẢN ĐỒ XUNG ĐỘT CỦA NAM                              │
├──────────────┬───────────────────────────────┬─────────────────────────────────────────┤
│ Đối tác      │ Điểm xung đột kỹ thuật        │ Giải pháp chốt & File cần sửa           │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Lê Thành  │ • Chốt mua C2C (BR-20)        │ • Thành gửi form POST /order?action=    │
│ (C2C/Core)   │ • Tranh mua máy cũ            │   create kèm listingId.                 │
│              │ • Trạng thái giữ chỗ          │ • Nam cập nhật: AVAILABLE -> RESERVED;  │
│              │                               │   Thành công -> SOLD; Hủy -> AVAILABLE. │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Ca Ngọc Tân│ • Revert mất 14 file Cart     │ • Đã khôi phục toàn bộ mã nguồn.        │
│ (Auth/Admin) │ • Lệch key Session User       │ • Nam đọc chuẩn key "currentUser".      │
│              │ • Filter chặn nhầm trang Cart │ • Filter cho phép khách vãng lai GET    │
│              │                               │   /cart (chỉ bắt login khi action=add). │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Phước Anh │ • Nút "Thêm giỏ hàng" bị liệt │ • Anh bọc form POST /cart?action=add,   │
│ (Product)    │ • Ô nhập số lượng (qty)       │   truyền productId; Nam bắt trong       │
│              │                               │   CartServlet và tăng số lượng giỏ.     │
└──────────────┴───────────────────────────────┴─────────────────────────────────────────┘
```

---

## 4. HƯỚNG DẪN THỰC THI — NÊN SỬA & TRÁNH SỬA FILE NÀO

### 🟢 File Nam NÊN SỬA khi tinh chỉnh giỏ hàng / đơn hàng:
* `src/main/java/com/electronics/marketplace/service/OrderService.java`: Logic chống tranh mua, timeout hoàn tiền, giải phóng hàng giữ chỗ:
  ```sql
  UPDATE listings SET status = 'RESERVED' WHERE id = ? AND status = 'AVAILABLE';
  ```
* `src/main/java/com/electronics/marketplace/service/payment/MomoGateway.java`: Cấu hình endpoint sandbox, API key MoMo.
* `src/main/java/com/electronics/marketplace/controller/user/CartServlet.java`: Xử lý thêm sản phẩm mới vào session cart.

### 🔴 File Nam TUYỆT ĐỐI KHÔNG TỰ Ý SỬA:
* `src/main/java/com/electronics/marketplace/entity/Listing.java`: Do Thành làm chủ.
* `src/main/java/com/electronics/marketplace/entity/Product.java`: Do Anh làm chủ.
* `src/main/java/com/electronics/marketplace/entity/User.java`: Do Tân làm chủ (không tự sửa xóa trường enum).

---

## 5. CHECKLIST KIỂM SOÁT NHANH (QUICK CHECKLIST)
- [x] Lấy user từ session qua helper: `req.getSession().getAttribute("currentUser")`.
- [x] Đã map quan hệ nullable `@ManyToOne Listing listing` và `@ManyToOne Product product` trong `OrderItem`.
- [x] Chạy `mvn test` xác nhận `MomoGatewayTest` đạt 3/3 PASS trước khi push.
