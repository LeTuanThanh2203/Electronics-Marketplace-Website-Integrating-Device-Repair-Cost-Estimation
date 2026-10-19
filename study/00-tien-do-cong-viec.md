# 00 — Bảng tiến độ công việc (module Nam)

> Cập nhật: **2026-10-19**. Số liệu lấy từ `wc -c` trên file thật, không phải ước lượng.

## Bảng task

| # | Hạng mục | File | Trạng thái |
|---|----------|------|-----------|
| **0** | Chốt thông tin với team | — | ✅ đóng (tự xử) |
| **1** | `User.java` + `Role` + `UserStatus` | `entity/User.java`, `entity/enums/` | ✅ xong |
| **1** | Cấu hình JPA + credentials qua env | `config/JpaConfig.java`, `persistence.xml` | ✅ xong |
| **2** | Map 5 entity JPA | `Cart`, `CartItem`, `Order`, `OrderItem`, `Payment` | ✅ xong, **đã verify** |
| **3** | 3 repository | `CartRepository`, `OrderRepository`, `PaymentRepository` | ✅ xong |
| **4** | 2 service | `OrderService`, `CartService` + `ServiceException` | ✅ xong, **có test** |
| **5** | `CartServlet` — `add` + `remove` JSON | `controller/user/CartServlet.java` | ✅ xong |
| **6** | `CheckoutServlet` POST | `controller/user/CheckoutServlet.java` | ✅ xong |
| **7** | `OrderServlet` — history / cancel / complete | `controller/user/OrderServlet.java` | ✅ xong |
| **8** | `PaymentServlet` — view + chặn IDOR | `controller/user/PaymentServlet.java` | ✅ xong |
| **9** | Chạy thử end-to-end trên Tomcat | — | ✅ xong (xem dưới) |

**Xong cả 9 bước.** `mvn -o test` → **7/7 pass** (3 test cũ + 4 test `OrderServiceTest`).

## Bước 9 đã chạy được gì (Tomcat 10.1.57, context `/shb`)

| Đường | Kết quả |
|---|---|
| deploy WAR | `Deployment ... has finished in [4,023] ms`, không lỗi |
| boot `JpaConfig` + Hikari | `select current_database()` → `sua_hay_ban`, PostgreSQL 18.6 |
| `GET /cart` (chưa login) | 200, render giỏ rỗng |
| `GET /order?action=history`, `/payment?orderId=…` (chưa login) | 401 |
| `POST /cart?action=add`, `POST /checkout` (chưa login) | 401 |
| `GET /cart?action=remove` | 405 |
| add → checkout → history (có session) | 302 → history 200, đúng `e2e-product`, 5.000.000, `PENDING`, flash OK |
| `GET /payment?orderId=…` (Buyer) | 200, có `TXN-…`, trạng thái `PENDING` |
| `complete` khi chưa PAID | 302 + flash lỗi, đơn vẫn `PENDING` |
| user khác xem payment / cancel đơn | **403** cả hai (IDOR chặn đúng) |
| cancel → history + payment | `CANCELLED`, Payment chuyển `FAILED` trong cùng transaction (BR-46) |

Session giả lập bằng một JSP tạm đặt **ngoài** `src/main` (đã xoá sau khi chạy);
dữ liệu test đã `cleanup` khỏi DB. Luồng thật vẫn cần `AuthenticationFilter` +
`LoginServlet` để có `currentUser` thật.

**Bug tìm được ở bước 9:** `payment.jsp` có 3 comment kết thúc bằng `%>` thay vì
`--%>` (dòng 105/137/147) → Jasper `Unterminated [<%--] tag`, `GET /payment` trả
500. Đã sửa. JSP chỉ lỗi lúc compile, `mvn compile` không bắt được — lần sau đổi
JSP thì phải gọi thử route, không chỉ build.

## Bước 2 đã verify bằng gì

`mvn -o clean test` → **3/3 pass**. `EntityMappingTest` boot EMF thật, ghi/đọc lại
cả 5 entity rồi assert trên **metamodel của Hibernate** + round-trip giá trị.

DB `sua_hay_ban` đã sinh 7 bảng: `users`, `products`, `carts`, `cart_items`,
`orders`, `order_items`, `payments` — kèm đúng các ràng buộc mang business rule:

| Ràng buộc | Rule |
|---|---|
| `cart_items UNIQUE(cart_id, product_id)` | BR-45 |
| `payments UNIQUE(order_id)` | BR-44 |
| `carts UNIQUE(customer_id)` | 1-1 User ↔ Cart |

Không dùng `information_schema` để assert: `hbm2ddl.auto=update` **không bao giờ
drop cột**, nên check kiểu đó pass giả khi mapping hỏng.

## Hợp đồng đã bị JSP khoá cứng

Đọc trực tiếp từ view — servlet viết ra **phải khớp đúng**, không phải suy đoán.

| Nơi gọi | Yêu cầu |
|---|---|
| `cart.jsp:159` | `POST ${ctx}/cart?action=remove` → JSON `{ok, subtotal, count}`; JS tự reload khi `count === 0` |
| `cart.jsp:16` | đọc `${cart}` **hoặc** `${sessionScope.cart}` → phải `request.setAttribute("cart", …)` |
| `checkout.jsp:25` | `${cart}`; form gửi `recipientName/Phone/Email/shippingAddress/paymentMethod/note` |
| `history.jsp:9-14` | `${orders}`, `${currentStatus}`, `${payments}` = **`Map<Long orderId, Payment>`**, `${page}`, `${totalPages}` |
| `history.jsp:147` | `POST ${ctx}/order?action=cancel` + hidden `orderId` |
| `history.jsp:161` | `POST ${ctx}/order?action=complete` |
| `payment.jsp:27` | `${payment}` và `${payment.order}` |
| `checkout.jsp:70-100` | `${currentUser.name/phone/email/location}` |

## 3 chỗ tắc — không phải việc của Nam

1. ~~**`Listing.java` + `Device.java` + `ListingStatus` + `ListingCondition`** (Thành)~~ —
   ✅ **đã gỡ 2026-10-19**: entity lấy từ `origin/Thanh`; `OrderItem.listingId` đã đổi
   thành `@ManyToOne Listing` nên `history.jsp:134` có dữ liệu. Nhánh MARKETPLACE
   của `OrderService` giờ làm được.
2. **`AuthenticationFilter`** (89 B rỗng) — chặn `${currentUser}` ở checkout/payment.
   Rule dự án bắt buộc filter cho URI bảo vệ, không check tay trong servlet.
   → Hợp đồng đã viết sẵn cho Tân ở [../PHOI-HOP-TAN.md](../PHOI-HOP-TAN.md) mục 1-2.
3. **`product/detail.jsp`** (Phước Anh, 0 B) — nút "Thêm vào giỏ" của kênh NEW_DEVICE
   phải nằm ở đây. `grep action=add` toàn webapp: **không có chỗ nào gọi**, nên
   `CartServlet` hiện chưa có đường vào.

> Điểm cần phối hợp với Thành khi merge: xem [../PHOI-HOP-THANH.md](../PHOI-HOP-THANH.md).
> Điểm cần phối hợp với Tân (session key `currentUser`, filter, `User`, `persistence.xml`):
> xem [../PHOI-HOP-TAN.md](../PHOI-HOP-TAN.md).

## Thứ tự làm đề xuất

Bước **3 → 4 → 5 → 7 → 8** đều chỉ dùng `User`/`Product`/`Cart`/`Order`/`Payment`,
tất cả đã map xong ⇒ **làm được ngay, không phụ thuộc ai**.

Nhánh MARKETPLACE trong `OrderService` (BR-20 conditional update, BR-31/32 đếm đơn
active) **đã hết chờ** — `Listing` đã có trên nhánh này.
