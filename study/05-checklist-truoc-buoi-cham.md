# 05 — Checklist trước buổi chấm

> Đọc mục **A** và **B** trước. Hai mục đó quyết định bạn có demo được hay không.

---

## A. Sự thật hiện tại của project (đo ngày 2026-10-16)

Đây là kết quả đếm file thật, không phải phỏng đoán.

### Đã xong — module của Nam

| File | Dòng |
|------|------|
| `entity/Cart.java` | 80 |
| `entity/CartItem.java` | 63 |
| `entity/Order.java` | 101 |
| `entity/OrderItem.java` | 73 |
| `entity/Payment.java` | 95 |
| `entity/Product.java` | 81 |
| `entity/enums/OrderStatus, OrderType, PaymentMethod, PaymentStatus, ProductStatus` | 6–12 |
| `config/JpaConfig.java` | 69 |
| `test/.../JpaConfigTest.java` | 71 |
| `webapp/WEB-INF/views/cart/cart.jsp` | 179 |
| `webapp/WEB-INF/views/order/checkout.jsp` | 222 |
| `webapp/WEB-INF/views/order/payment.jsp` | 207 |
| `webapp/WEB-INF/views/order/history.jsp` | 249 |
| `webapp/WEB-INF/views/order/_header.jspf` | 56 |
| `webapp/WEB-INF/views/order/_footer.jsp` | 12 |
| `webapp/css/order.css` | 1137 |

### Còn rỗng — 5 dòng/class, chỉ có khai báo

- **11 servlet**: `CartServlet`, `CheckoutServlet`, `OrderServlet`, `PaymentServlet`, `RepairRequestServlet`, `LoginServlet`, `RegisterServlet`, `ListingServlet`, `ProductServlet`, `AdminProductServlet`, `AdminRepairServlet`
- **8 repository**: `Cart`, `Order`, `Payment`, `Product`, `Listing`, `User`, `Device`, `Repair`
- **4 service**: `OrderService`, `MarketplaceService`, `RepairService`, `UserService` — **không có `CartService`**
- **3 filter**: `AuthenticationFilter`, `EncodingFilter`, `GzipFilter`
- **2 util**: `PasswordUtil`, `ValidationUtil`
- **Entity của bạn khác**: `User`, `Listing`, `Device`, `ListingImage`, `ListingProblem`, `ProductSpecification`, `RepairRequest`, `RepairEstimate`, `RepairPricing`, `RepairProblem`, `Admin`

### File 0 byte

`WEB-INF/web.xml` · `index.jsp` · `js/main.js` · `css/style.css` · và **11 JSP** khác (home, login, register, product, listing, repair, admin)

### Hệ quả trực tiếp

1. **`web.xml` rỗng + servlet chưa có annotation ⇒ không có URL nào tồn tại.** Gõ `/cart` bây giờ là 404.
2. **`User.java` rỗng ⇒ `sessionScope.currentUser` không có gì sinh ra nó.** `_header.jspf` sẽ hiện trạng thái chưa đăng nhập mãi mãi.
3. **`index.jsp` rỗng ⇒ không có cửa vào.** Phải gõ URL trực tiếp.

→ **Kết luận: hiện tại KHÔNG demo được giao diện.** Kịch bản ở [03](03-kich-ban-demo.md) chỉ chạy được sau khi xong mục B.

### Rác cần xoá

`src/main/java/com/example/electronics_marketplace_project/` — `HelloApplication.java` + `HelloResource.java`, di sản của project khởi tạo, dùng JAX-RS không thuộc stack của bài. Xoá cả thư mục.

---

## B. Tối thiểu phải xong

Sắp theo **thứ tự phụ thuộc** — làm từ trên xuống, dừng ở đâu cũng còn demo được phần trên.

### Mức 0 — Đủ để giao diện chạy (ưu tiên tuyệt đối)

| # | Việc | Ghi chú |
|---|------|---------|
| 1 | `User.java` — entity tối thiểu: `id, name, email, passwordHash, phone, role, status` | Không có nó thì session rỗng, header vô nghĩa. `role` là **`ENUM(USER, ADMIN)`** — KHÔNG có `CUSTOMER`/`SELLER` (BR-06 + SDD 13.2: Buyer/Seller là vai trò nghiệp vụ suy ra từ ngữ cảnh, không phải cột riêng) |
| 2 | Map JPA annotation cho `Cart`, `CartItem`, `Order`, `OrderItem`, `Payment` | Hiện là POJO thuần, chưa có `@Entity` |
| 3 | `CartRepository`, `OrderRepository`, `PaymentRepository` — chỉ 3–4 method mỗi cái | `findByCustomerId`, `save`, `findById`. **`ProductRepository` KHÔNG thuộc module này** — nó thuộc module Product của Phước Anh (xem `06-doc-code-entity.md` mục 6). Nam chỉ viết bản tối thiểu để `Cart` chạy được; PA map entity xong thì sửa đè. |
| 4 | `@WebServlet` trên 4 servlet: Cart, Checkout, Order, Payment | Không cần sửa `web.xml` |
| 5 | `index.jsp` — redirect thẳng sang `/cart` hoặc `/product` | 3 dòng |
| 6 | Cách có user đăng nhập: **seed sẵn 1 Customer trong DB**, và một filter tạm gán `session.currentUser` nếu chưa có | Xem cảnh báo bên dưới |

> ⚠️ **Về việc tạm gán `currentUser`:** đây là lối tắt **chỉ để demo**, phải ghi rõ `ponytail:` trong code và **nói với cô là đang tạm**. Tuyệt đối không để nó tồn tại như thể là code thật — đó là lỗ hổng xác thực.

### Mức 1 — Đủ để ăn điểm kỹ thuật (nên cố)

| # | Việc | Điểm ăn được |
|---|------|--------------|
| 7 | `OrderService.checkout()` — 1 transaction: validate `Product.status == AVAILABLE` → tạo Order + OrderItem (snapshot giá) + Payment | BR-43, BR-44, BR-45 |
| 8 | `OrderService.cancel()` — 1 transaction 2 bảng: Order → CANCELLED, Payment PENDING → FAILED | BR-46, transaction boundary |
| 9 | `OrderService.complete()` — chặn nếu không đồng thời `CONFIRMED` + `PAID` | BR-46 |
| 10 | `OrderService.findHistory()` — phân trang + `JOIN FETCH items` | Rule 2, N+1 |
| 11 | **Kiểm tra quyền sở hữu** trong `cancel()`, `complete()`, `payment?action=view` | IDOR — xem mục D |
| 12 | `POST /cart?action=remove` trả JSON `{ok, subtotal, count}` | Rule 4, khớp hợp đồng đã ghi trong `cart.jsp` |
| 13 | `sendRedirect` sau POST checkout (POST-redirect-GET) | Chống F5 tạo đơn trùng |

### Mức 2 — Nếu còn thời gian

- `AuthenticationFilter` + `EncodingFilter` + `GzipFilter` (Rule 5) — nêu tên trong demo là được, chưa cần chạy
- `PasswordUtil` (BCrypt — jbcrypt đã có trong `pom.xml`)
- Unit test cho `OrderService.checkout()` và `cancel()`

### Nếu KHÔNG kịp Mức 0

Đường tắt: viết **một `DemoServlet`** nạp model giả vào request rồi forward sang JSP. Khoảng 30 dòng cho mỗi màn, không cần DB, không cần repository.

Nói với cô **đúng câu này**:
> "Tầng repository và service em chưa xong nên phần demo giao diện em nạp dữ liệu mẫu để cô xem tầng view. Phần model và view là thật, em xin demo source code trước."

Trung thực + vẫn cho cô thấy được sản phẩm. Tệ hơn nhiều so với việc ngồi sửa lỗi trước mặt cô.

---

## C. Checklist kỹ thuật — chạy trước khi nộp

- [ ] `mvn clean package` → `BUILD SUCCESS`, không warning đỏ
- [ ] Deploy WAR lên Tomcat 10.1, không có stacktrace lúc khởi động
- [ ] DB có dữ liệu mẫu: **3 `Product` `AVAILABLE`** giá khác nhau, **1 `User` role `USER`**, **1 `User` role `ADMIN`**
- [ ] `git status` sạch, không có file `.env` hay mật khẩu nào bị add
- [ ] Không còn `System.out.println` / `console.log` sót lại
- [ ] Không hardcode mật khẩu DB — vẫn đọc `DB_URL` / `DB_USER` / `DB_PASSWORD`
- [ ] Xoá package rác `com/example/electronics_marketplace_project/`

---

## D. Bảo mật — không được bỏ qua

Đây là những thứ **cô rất dễ hỏi** và cũng là lỗi thật nếu thiếu.

| Rủi ro | Bắt buộc làm |
|--------|--------------|
| **IDOR** — khách sửa `orderId` thành đơn người khác | Mọi thao tác trên đơn (`cancel`, `complete`, `payment?action=view`) phải kiểm tra `order.buyer.id == currentUser.id`, sai → 403 |
| **CSRF** — form huỷ đơn bị lợi dụng từ site khác | Token CSRF trong hidden input, hoặc tối thiểu `SameSite=Lax` trên cookie session |
| **XSS** | Không bao giờ dùng `<c:out value="..." escapeXml="false"/>` với dữ liệu người dùng nhập (ghi chú đơn, tên) |
| **SQL injection** | Không nối chuỗi vào JPQL — luôn dùng tham số `:name` |
| **Mật khẩu** | BCrypt, không bao giờ lưu plaintext; không log mật khẩu |
| **Rò rỉ lỗi** | Trang lỗi không hiện stacktrace ra cho người dùng |
| **Tin client** | Giá tiền **không** nhận từ form — luôn đọc lại từ `Product.price` trong DB |

---

## E. Checklist 15 phút trước khi vào phòng chấm

- [ ] `mvn clean package` chạy lại một lần cuối, sạch
- [ ] Tomcat đã start, mở thử `/cart` xác nhận không 404
- [ ] Đăng nhập sẵn, **giỏ hàng đang có 2 máy**
- [ ] Mở sẵn 6 tab theo đúng thứ tự ở [03](03-kich-ban-demo.md#chuẩn-bị-trước-làm-trước-15-phút-không-làm-trước-mặt-cô)
- [ ] **Chụp sẵn 5 ảnh màn hình** để dành làm phương án dự phòng
- [ ] IDE mở sẵn: `Order.java`, `OrderItem.java`, `Cart.java`, `history.jsp`, `cart.jsp`
- [ ] Tắt thông báo Messenger/Zalo, thu nhỏ font IDE
- [ ] **Đọc lại mục E của [04](04-cau-hoi-phan-bien.md)** — bốn câu tuyệt đối không nói

---

## F. Đừng làm lúc này

- ❌ Thêm tính năng mới ngoài SDD (voucher, phí ship, tồn kho, hoàn tiền)
- ❌ Đổi schema hay đổi tên cột — mọi thứ đã nhất quán với SDD rồi
- ❌ Refactor CSS — 1137 dòng đang chạy tốt
- ❌ Cài thêm thư viện — `pom.xml` đã đủ
- ❌ Viết test cho phần chưa có logic

---

## G. Kịch bản xấu & cách nói

| Tình huống | Nói gì |
|-----------|--------|
| Tomcat không start | "Em xin demo bằng ảnh chụp và đi thẳng vào source code." → mở IDE, **không** ngồi sửa |
| Cô hỏi phần chưa làm | "Phần đó em chưa xong. Em dự định làm theo hướng..." → nêu thiết kế, đừng im |
| Cô hỏi về module bạn khác | "Phần đó ngoài phạm vi em phụ trách, nhưng theo em hiểu thì..." |
| Cô hỏi câu không biết | "Em chưa xử lý trường hợp đó." → **rồi nêu hướng suy nghĩ**. Im lặng mới mất điểm |
| Hết giờ giữa lúc demo code | Nhảy ngay tới `history.jsp:178` — dòng BR-46 là điểm nhấn, phải nói được |

---

## H. Ưu tiên nếu chỉ còn một buổi

Làm **đúng 3 việc** này, theo thứ tự:

1. `User.java` + map JPA annotation cho 5 entity → mở khoá mọi thứ khác
2. `@WebServlet` + repository tối thiểu cho 4 servlet của module → giao diện chạy được
3. `OrderService.checkout()` và `cancel()` trong một transaction → đây là **điểm nhấn kỹ thuật** của module, mất nó là mất phần đáng giá nhất

Ba việc này đủ để demo trọn vòng đời đơn hàng và trả lời được mọi câu ở [04](04-cau-hoi-phan-bien.md).
