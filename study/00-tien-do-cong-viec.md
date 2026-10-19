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
| **10** | **Tích hợp MoMo thật** (bỏ QR + thẻ ngân hàng) | `MomoConfig`, `MomoGateway`, `PaymentServlet` create/return/ipn, `OrderService.markPaidByGateway` | ✅ xong, **chạy thật với sandbox** |

**Xong cả 10 bước.** `mvn -o test` → **10/10 pass**
(`JpaConfigTest` 2 + `EntityMappingTest` 1 + `MomoGatewayTest` 3 + `OrderServiceTest` 4).

Commit tích hợp MoMo: **`3664f24`** trên nhánh `Nam`
(*"feat: tich hop cong thanh toan MoMo that, bo chuyen khoan QR va the ngan hang"*).

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

**Bug tìm được ở bước 9:** `payment.jsp` có 3 comment trong khối `<c:choose>` kết
thúc bằng `%>` thay vì `--%>` → Jasper `Unterminated [<%--] tag`, `GET /payment` trả
500. Đã sửa. JSP chỉ lỗi lúc compile, `mvn compile` không bắt được — lần sau đổi
JSP thì phải gọi thử route, không chỉ build.

## Bước 10 — MoMo thật (đã chạy, không phải giả lập)

Trước bước này form có **4 lựa chọn** (COD, QR, MoMo, BANK) và trang thanh toán
chỉ hiện thông tin chuyển khoản — Admin xác nhận tay. Giờ **chỉ còn COD + MoMo**,
và MoMo gọi **REST v2 của MoMo** thật (sandbox).

| Thành phần | Vai trò |
|---|---|
| `config/MomoConfig.java` | Holder đọc env `MOMO_PARTNER_CODE`, `MOMO_ACCESS_KEY`, `MOMO_SECRET_KEY`, `MOMO_ENDPOINT`, `MOMO_PUBLIC_BASE_URL`; **fail fast** nếu thiếu |
| `service/MomoGateway.java` | `create` (POST `/v2/gateway/api/create`), `query` (POST `/v2/gateway/api/query`), `verifyIpn`, `hmacSha256` |
| `PaymentServlet` `action=create` | Sinh `orderId` mới → gọi `create` → `sendRedirect(result.payUrl)` |
| `PaymentServlet` `action=return` | MoMo redirect người dùng về → **không tin URL**, gọi `query` đối chiếu |
| `PaymentServlet` `action=ipn` | MoMo gọi server-to-server → `verifyIpn` → trả JSON 200 |
| `OrderService.markPaidByGateway` | Đổi `Payment` → `PAID` + `paidAt`, và `Order` `PENDING → CONFIRMED` trong **cùng transaction** (BR-46) |

### Đã chạy thật những gì

| Đường | Kết quả |
|---|---|
| checkout chọn MoMo | `302 → /payment?action=create&orderId=77` → `302 → https://test-payment.momo.vn/v2/gateway/pay?t=…` (URL thật, có chữ ký) |
| trang `/payment` của đơn MoMo | 200, `PENDING`, phương thức "Ví MoMo", nút "Thanh toán qua MoMo" |
| checkout chọn COD (`method=""`) | `302 → /order?action=history`; DB: `method = null`, `PENDING` |
| đối chiếu DB | payment 56 / order 77 / `MOMO` / `PENDING` / 15.990.000 · payment 57 / order 78 / `null` / `PENDING` / 31.990.000 |
| `mvn -o test -Dtest=MomoGatewayTest` | 3/3 xanh — vector HMAC đối chiếu độc lập bằng `openssl dgst -sha256 -hmac` |

> **Vì sao không có dòng "đã PAID" trong bảng trên:** chưa ai bấm trả tiền thật
> trong phiên đó. Đường `PAID` được kiểm bằng IPN/query, mô tả ở mục tiếp theo.

### Ba điều học được khi làm MoMo

1. **`orderId` không được dùng lại.** MoMo trả `resultCode 41` (duplicate orderId)
   kể cả khi `requestId` khác. Nên mỗi lần bấm "Thanh toán" phải sinh
   `transactionCode + "-" + 8 hex` (`PaymentServlet#momoOrderId`), và khi nhận
   callback thì cắt hậu tố đó để tra lại `Payment`.
2. **Không tin tham số trên URL redirect.** Người dùng sửa được. `action=return`
   phải gọi `query` API lấy kết quả thật rồi mới đổi trạng thái.
3. **IPN phải trả HTTP 200 JSON**, kể cả khi đã `PAID` (IPN trùng lặp là bình
   thường) — trả lỗi thì MoMo sẽ retry liên tục.

> **Giới hạn môi trường local:** IPN là server-to-server, `localhost` không nhận
> được. Đường đối soát ở local là `return` + `query` API. Muốn test IPN thật thì
> phải mở tunnel (`MOMO_PUBLIC_BASE_URL`) hoặc deploy.

> **Về mã QR sandbox:** quét bằng **app MoMo thật trên điện thoại sẽ báo "không
> tồn tại / hết hạn"** — đó là bình thường. Sandbox cần app riêng
> **"ứng dụng MoMo Test"**, và theo `developers.momo.vn` phải **gỡ app MoMo thật**
> trước khi cài. Ví test: SĐT 10 số bất kỳ, OTP `0000`, mật khẩu `000000`.
> `payUrl` cũng có TTL ngắn và chỉ dùng một lần.

## Bước 2 đã verify bằng gì

Lúc làm bước 2: `mvn -o clean test` → **3/3 pass**. Giờ cả bộ là **10/10** —
`EntityMappingTest` vẫn là test ghim tầng mapping: boot EMF thật, ghi/đọc lại
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
| `cart.jsp:158` | `POST ${ctx}/cart?action=remove` → JSON `{ok, subtotal, count}`; JS tự reload khi `count === 0` |
| `cart.jsp:16` | đọc `${cart}` **hoặc** `${sessionScope.cart}` → phải `request.setAttribute("cart", …)` |
| `checkout.jsp:24` | `${cart}`; form gửi `recipientName/Phone/Email/shippingAddress/paymentMethod/note` |
| `history.jsp:9-14` | `${orders}`, `${currentStatus}`, `${payments}` = **`Map<Long orderId, Payment>`**, `${page}`, `${totalPages}` |
| `history.jsp:171` | `POST ${ctx}/order?action=cancel` + hidden `orderId` |
| `history.jsp:180` | `POST ${ctx}/order?action=complete` |
| `payment.jsp:18-20` | `${payment}` và `${payment.order}` |
| `checkout.jsp:69-99` | `${currentUser.name/phone/email/location}` |

## 2 chỗ tắc còn lại — không phải việc của Nam

1. **`AuthenticationFilter`** (89 B rỗng) — chặn `${currentUser}` ở checkout/payment.
   Rule dự án bắt buộc filter cho URI bảo vệ, không check tay trong servlet.
   → Hợp đồng đã viết sẵn cho Tân ở [../PHOI-HOP-TAN.md](../PHOI-HOP-TAN.md) mục 1-2.
2. **`product/detail.jsp`** (Phước Anh, 0 B) — nút "Thêm vào giỏ" của kênh NEW_DEVICE
   phải nằm ở đây. `grep action=add` toàn webapp: **không có chỗ nào gọi**, nên
   `CartServlet` hiện chưa có đường vào từ giao diện.

> Đã gỡ 2026-10-19: `Listing.java` + `Device.java` + `ListingStatus` + `ListingCondition`
> (Thành) — entity lấy từ `origin/Thanh`; `OrderItem.listingId` đã đổi thành
> `@ManyToOne Listing` nên `history.jsp:133` có dữ liệu. Nhánh MARKETPLACE của
> `OrderService` giờ làm được.

> Điểm cần phối hợp với Thành khi merge: xem [../PHOI-HOP-THANH.md](../PHOI-HOP-THANH.md).
> Điểm cần phối hợp với Tân (session key `currentUser`, filter, `User`, `persistence.xml`):
> xem [../PHOI-HOP-TAN.md](../PHOI-HOP-TAN.md).

## Thứ tự làm đề xuất

**Không còn bước nào phải làm.** Bước 3 → 10 đã xong hết, đã chạy end-to-end và có
test. Hai chỗ tắc ở trên đều thuộc module khác (Tân, Phước Anh) — chỉ chờ merge.

