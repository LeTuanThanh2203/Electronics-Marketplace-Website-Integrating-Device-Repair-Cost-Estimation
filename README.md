# 🤝 TRUNG TÂM PHỐI HỢP DỰ ÁN & ĐIỀU HỢP XUNG ĐỘT (5 THÀNH VIÊN)
> **Dự án:** Electronics Marketplace & Repair System  
> **Nhánh đích:** `integration` | **Trạng thái:** ✅ **100% BUILD SUCCESS & 31/31 UNIT TESTS PASS**  
> **Nguyên tắc vàng:** *"Mỗi người một module — Giao tiếp qua URL, Entity chuẩn & Session — Không ghi đè file chung."*

---

## 📌 DANH MỤC TÀI LIỆU PHỐI HỢP CỦA 5 THÀNH VIÊN

| Thành viên | Phân hệ phụ trách | Tài liệu hướng dẫn & Xử lý Conflict | Test Suite & Trạng thái |
|---|---|---|:---:|
| **Lê Tuấn Thành** | Chợ Đồ Cũ C2C (`/listing`), Trang Chủ (`/home`) & UI Core Google Stitch | 📖 [PHOI-HOP-THANH.md](phoi-hop/PHOI-HOP-THANH.md) | **10/10 PASS** (`MarketplaceServiceTest`) |
| **Ca Ngọc Tân** | Tài khoản, Bảo mật RBAC (`/auth/*`), Admin Dashboard (`/admin/*`) | 📖 [PHOI-HOP-TAN.md](phoi-hop/PHOI-HOP-TAN.md) | Sạch 100% Session `currentUser` |
| **Phạm Phước Anh** | Cửa Hàng Máy Mới B2C (`/product`), Thuật toán Gợi ý AI & So sánh EAV | 📖 [PHOI-HOP-ANH.md](phoi-hop/PHOI-HOP-ANH.md) | Chuẩn Form POST Giỏ hàng & Fail-safe |
| **Huỳnh Thuyên Nam**| Giỏ Hàng (`/cart`), Đơn Hàng (`/order`), Thanh toán MoMo Sandbox | 📖 [PHOI-HOP-NAM.md](phoi-hop/PHOI-HOP-NAM.md) | **3/3 PASS** (`MomoGatewayTest`) |
| **Phạm Nguyễn Vương Quốc**| Dịch Vụ Sửa Chữa (`/repair`), Báo Giá Dự Toán & Báo Cáo Thống Kê | 📖 [PHOI-HOP-QUOC.md](phoi-hop/PHOI-HOP-QUOC.md) | **18/18 PASS** (`RepairServiceTest`) |

---

## ⚡ 4 QUY TẮC CỐT LÕI BẢO VỆ CODEBASE (TOÀN ĐỘI TUÂN THỦ)

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│ 1. SESSION KEY THỐNG NHẤT:                                                             │
│    • Dùng duy nhất "currentUser" (User), không dùng "user".                            │
│ 2. CHỐNG TRANH MUA MÁY CŨ (BR-20):                                                     │
│    • Chốt mua máy cũ phải qua lệnh Atomic UPDATE trạng thái sang RESERVED.             │
│ 3. KHÔNG SỬA ĐÈ CẤU HÌNH CHUNG:                                                        │
│    • persistence.xml, JpaConfig.java, pom.xml, web.xml do Nam/Tân đồng bộ.             │
│ 4. UNIT TEST TRƯỚC KHI PUSH:                                                           │
│    • Chạy `mvn test` để đảm bảo không ai làm gãy test của người khác.                  │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

# 🚀 HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN ELECTRONICS MARKETPLACE

Dự án Web Java Servlet / Jakarta EE 10 (Tomcat 10.1+) kết hợp Hibernate JPA 6.4.4 và cơ sở dữ liệu PostgreSQL.

---

## 1. Yêu cầu môi trường
* **JDK:** Java 17 trở lên.
* **Database:** PostgreSQL 14+ (chạy cổng mặc định `5432`).
* **Web Server:** Apache Tomcat 10.1+ (hỗ trợ Jakarta EE 10 / Servlet 6.0).
* **Build Tool:** Maven 3.8+.

---

## 2. Các bước thiết lập Database PostgreSQL (Đơn giản nhất)

### Bước 1: Tạo Database
Mở terminal (PowerShell / Command Prompt) hoặc pgAdmin Query Tool và tạo database tên `electronics_marketplace`:
```sql
CREATE DATABASE electronics_marketplace;
```
*(Hoặc dùng lệnh terminal)*:
```bash
createdb -U postgres electronics_marketplace
```

### Bước 2: Nạp toàn bộ bảng và dữ liệu mẫu (Chỉ 1 file duy nhất)
Toàn bộ schema và dữ liệu mẫu của tất cả các phân hệ (Người dùng, Thiết bị, Sản phẩm mới, Chợ đồ cũ, Sửa chữa, Đơn hàng) đã được gộp trong file:
`src/main/resources/database/init_full_database.sql`

Chạy lệnh nạp script:
```bash
psql -h localhost -p 5432 -U postgres -d electronics_marketplace -f "src/main/resources/database/init_full_database.sql"
```
*(Nếu dùng pgAdmin: Mở pgAdmin -> Chuột phải vào `electronics_marketplace` -> Chọn **Query Tool** -> Mở file `init_full_database.sql` và bấm **F5 (Execute)**).*

> **Lưu ý:** Script này an toàn (`IDEMPOTENT`), bạn có thể chạy lại nhiều lần mà không sợ trùng lặp khóa hay nhân đôi dữ liệu.

---

## 3. Cấu hình biến môi trường kết nối DB

Dự án sử dụng `JpaConfig` tự động nhận diện kết nối theo thứ tự ưu tiên:
1. **Biến môi trường hệ thống (khuyên dùng khi chạy local/production):**
   * `DB_URL`: `jdbc:postgresql://localhost:5432/electronics_marketplace`
   * `DB_USER`: `postgres`
   * `DB_PASSWORD`: `<mật-khẩu-postgresql-của-bạn>`
2. **Fallback mặc định:** Nếu không đặt biến môi trường, hệ thống tự động kết nối tới `jdbc:postgresql://localhost:5432/electronics_marketplace` với user `postgres`.

---

## 4. Tài khoản kiểm thử có sẵn

| Vai trò | Email | Mật khẩu | Mục đích kiểm thử |
|---|---|---|---|
| **Admin** | `admin@marketplace.com` | `admin123` | Quản trị sản phẩm, duyệt tin chợ cũ, xem báo cáo |
| **Seller / User** | `seller@marketplace.com` | `admin123` | Đăng bán máy cũ, xem tin |
| **Buyer / User** | `customer@marketplace.com` | `admin123` | Thêm vào giỏ hàng, mua hàng, chốt giữ chỗ |

---

## 5. Các phân hệ và luồng tính năng chính

1. **Trang chủ (`/home` hoặc `/`):**
   - Tự động hiển thị 4 laptop mới nhất và 4 tin máy cũ đang khả dụng từ database PostgreSQL.
2. **Chợ máy mới chính hãng (`/product` hoặc `/products`):**
   - Danh sách phân loại, tìm kiếm, lọc theo giá/hãng.
   - Chi tiết sản phẩm (`/product/detail?id=...`): Ma trận cấu hình phần cứng xuất xưởng.
   - Nút **"THÊM GIỎ HÀNG" / "MUA NGAY"**: Lưu vào bảng `carts`, `cart_items` trong database.
   - Trang **Giỏ hàng (`/cart`)** & **Thanh toán (`/checkout`)**: Tạo đơn hàng trong bảng `orders`, `order_items` với hình thức COD hoặc cổng MoMo.
3. **Chợ đồ cũ C2C (`/marketplace` hoặc `/listing`):**
   - Danh sách máy cũ kiểm định, lọc tình trạng (Good, Fair, Damaged).
   - Chi tiết tin máy cũ (`/listing/detail?id=...`): Lỗi khai báo, chu kỳ sạc pin, ảnh thật, số điện thoại người bán.
   - Nút **"CHỐT MUA MÁY (GIỮ CHỖ)"**: Khóa giữ chỗ máy và tạo đơn hàng `orders` trạng thái chờ người bán xác nhận.
4. **Phân hệ Sửa chữa (`/repair`):**
   - Dự toán chi phí sửa chữa theo dòng máy và loại lỗi, tra cứu tiến độ sửa chữa (`/repair/track`).

