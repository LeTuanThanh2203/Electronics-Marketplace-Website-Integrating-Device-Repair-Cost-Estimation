# 05 — Checklist trước buổi chấm

> Đọc mục **A** và **B** trước. Hai mục đó quyết định bạn có demo được hay không.

---

## A. Sự thật hiện tại của project (đo ngày 2026-10-19)

Đây là kết quả `wc -l` trên file thật, không phải phỏng đoán.

### Đã xong — module của Nam

| File | Dòng |
|------|------|
| `entity/Cart.java` | 110 |
| `entity/CartItem.java` | 91 |
| `entity/Order.java` | 138 |
| `entity/OrderItem.java` | 106 |
| `entity/Payment.java` | 137 |
| `entity/Product.java` | 106 |
| `config/JpaConfig.java` | 69 |
| `config/MomoConfig.java` | 94 |
| `repository/CartRepository.java` | 40 |
| `repository/OrderRepository.java` | 81 |
| `repository/PaymentRepository.java` | 87 |
| `service/CartService.java` | 129 |
| `service/OrderService.java` | 499 |
| `service/MomoGateway.java` | 307 |
| `service/ServiceException.java` | 28 |
| `controller/user/CartServlet.java` | 137 |
| `controller/user/CheckoutServlet.java` | 89 |
| `controller/user/OrderServlet.java` | 179 |
| `controller/user/PaymentServlet.java` | 316 |
| `webapp/WEB-INF/web.xml` | 56 |
| `webapp/WEB-INF/views/cart/cart.jsp` | 179 |
| `webapp/WEB-INF/views/order/checkout.jsp` | 201 |
| `webapp/WEB-INF/views/order/payment.jsp` | 192 |
| `webapp/WEB-INF/views/order/history.jsp` | 250 |
| `webapp/css/order.css` | 1137 |

Test: **10/10 xanh** — `JpaConfigTest` 2, `EntityMappingTest` 1, `MomoGatewayTest` 3, `OrderServiceTest` 4.

Đã chạy end-to-end trên Tomcat 10.1.57, context `/shb` — bảng kết quả từng route ở
[00 — Bước 9](00-tien-do-cong-viec.md#bước-9-đã-chạy-được-gì-tomcat-10157-context-shb).
MoMo đã chạy thật với sandbox, xem [00 — Bước 10](00-tien-do-cong-viec.md#bước-10--momo-thật-đã-chạy-không-phải-giả-lập).

### Còn rỗng — thuộc module khác, **không phải việc của Nam**

- **7 servlet**: `RepairRequestServlet`, `LoginServlet`, `RegisterServlet`, `ListingServlet`, `ProductServlet`, `AdminProductServlet`, `AdminRepairServlet`
- **5 repository**: `Product`, `Listing`, `User`, `Device`, `Repair`
- **3 service**: `MarketplaceService`, `RepairService`, `UserService`
- **3 filter**: `AuthenticationFilter`, `EncodingFilter`, `GzipFilter` — đều 5 dòng
- **2 util**: `PasswordUtil`, `ValidationUtil`
- **Entity của bạn khác**: `User`, `Listing`, `Device`, `ListingImage`, `ListingProblem`, `ProductSpecification`, `RepairRequest`, `RepairEstimate`, `RepairPricing`, `RepairProblem`, `Admin`

> `Listing.java` + `Device.java` **không** còn rỗng — đã lấy từ `origin/Thanh`, và
> `OrderItem.listing` đã trỏ `@ManyToOne Listing`.

### Hệ quả trực tiếp

1. **Module của em demo được đầy đủ**, kể cả cổng thanh toán thật. Không còn route nào 404 trong phạm vi 4 servlet của em.
2. **Chưa đăng nhập thật được** — `LoginServlet` rỗng. Hiện các servlet tự kiểm tra `currentUser` và trả **401** khi vắng mặt (đã verify). Muốn có session thật để demo thì phải tự dựng session bằng JSP tạm ở nhà, hoặc chờ `LoginServlet` của Tân.
3. **Không có cửa vào từ trang chủ** — `index.jsp` rỗng, `product/detail.jsp` (nút "Thêm vào giỏ") cũng rỗng. Demo bằng cách gõ URL trực tiếp `/cart`.

→ **Kết luận: demo được**, nhưng phải mở thẳng `/cart` và phải chuẩn bị session trước. Kịch bản đầy đủ ở [03](03-kich-ban-demo.md).

### Rác

Package `src/main/java/com/example/electronics_marketplace_project/` — di sản của
project khởi tạo, dùng JAX-RS không thuộc stack của bài. Nên xoá cả thư mục.

---

## B. Mức tối thiểu — đã xong hết

Bảng cũ liệt kê 13 việc phải làm. Tình trạng thật bây giờ:

| Mức | Việc | Trạng thái |
|---|---|---|
| 0 | `User` + map JPA 5 entity | ✅ xong, verify bằng `EntityMappingTest` |
| 0 | 3 repository (Cart/Order/Payment) | ✅ xong |
| 0 | `@WebServlet` trên 4 servlet | ✅ xong |
| 0 | Có đường đăng nhập | ❌ **còn hở** — `LoginServlet` + `AuthenticationFilter` của Tân |
| 1 | `checkout()` 1 transaction: validate `AVAILABLE` → Order + OrderItem snapshot + Payment | ✅ xong |
| 1 | `cancel()` — 2 bảng cùng transaction | ✅ xong |
| 1 | `complete()` chặn khi chưa `CONFIRMED` + `PAID` | ✅ xong |
| 1 | `findHistory()` phân trang + `JOIN FETCH` | ✅ xong (two-query pattern để tránh `HHH000104`) |
| 1 | Kiểm tra quyền sở hữu (IDOR) | ✅ xong, đã verify **403** bằng request thật |
| 1 | `POST /cart?action=remove` trả JSON `{ok, subtotal, count}` | ✅ xong |
| 1 | POST-redirect-GET sau checkout | ✅ xong |
| 2 | `OrderService` unit test | ✅ 4 test |
| 2 | **MoMo thật** (không có trong bảng cũ) | ✅ xong, chạy với sandbox |

**Không còn việc nào trong Mức 0/1/2 của module em.** Thứ duy nhất còn chặn demo
trọn vẹn là đường đăng nhập — và đó là phần phối hợp, không phải phần em viết.

> ⚠️ **Đừng tái sử dụng "đường tắt DemoServlet"** mà bảng cũ đề xuất. Không cần nữa —
> tầng repository/service thật đã chạy. Nạp model giả bây giờ là **làm yếu** bài.

---

## C. Checklist kỹ thuật — chạy trước khi nộp

- [ ] `mvn clean package` → `BUILD SUCCESS`, không warning đỏ
- [ ] `mvn -o test` → **10/10 xanh**
- [ ] Deploy WAR lên Tomcat 10.1, không có stacktrace lúc khởi động
- [ ] DB có dữ liệu mẫu: **3 `Product` `AVAILABLE`** giá khác nhau, **1 `User` role `USER`**, **1 `User` role `ADMIN`**
- [ ] **Biến môi trường MoMo đã set** — `MOMO_PARTNER_CODE`, `MOMO_ACCESS_KEY`, `MOMO_SECRET_KEY`, tuỳ chọn `MOMO_ENDPOINT` / `MOMO_PUBLIC_BASE_URL`. **Restart Tomcat sau khi set** (VS Code Tomcat extension đọc env lúc spawn `java.exe`; sửa env mà không restart là không có tác dụng)
- [ ] Bấm thử MoMo một lần, xác nhận ra trang `test-payment.momo.vn` chứ không phải flash "Cổng thanh toán chưa được cấu hình"
- [ ] `git status` sạch, không có file `.env` hay mật khẩu nào bị add — **đặc biệt `MOMO_*` không được nằm trong bất kỳ file nào của repo**
- [ ] Không còn `System.out.println` / `console.log` sót lại
- [ ] Không hardcode mật khẩu DB — vẫn đọc `DB_URL` / `DB_USER` / `DB_PASSWORD`
- [ ] Xoá package rác `com/example/electronics_marketplace_project/`


---

## D. Bảo mật — không được bỏ qua

Đây là những thứ **cô rất dễ hỏi**. Cột cuối là tình trạng thật, đã grep kiểm tra.

| Rủi ro | Yêu cầu | Tình trạng |
|--------|---------|-----------|
| **IDOR** — khách sửa `orderId` thành đơn người khác | Mọi thao tác trên đơn (`cancel`, `complete`, `payment?action=view`) kiểm tra `order.buyer.id == currentUser.id`, sai → **403** | ✅ đã làm, verify bằng request thật |
| **CSRF** — form huỷ đơn bị lợi dụng từ site khác | Token CSRF trong hidden input, hoặc tối thiểu `SameSite=Lax` trên cookie session | ❌ **chưa có** — cookie chỉ có `HttpOnly`. Đây là lỗ hổng thật, nói thẳng nếu bị hỏi |
| **XSS** | Không dùng `<c:out escapeXml="false"/>` với dữ liệu người dùng nhập | ✅ không có chỗ nào dùng; EL mặc định escape |
| **SQL injection** | Không nối chuỗi vào JPQL — luôn dùng tham số `:name` | ✅ chỉ nối `StringBuilder` cho **mệnh đề** (có/không `AND o.status = :status`), giá trị luôn qua `setParameter` |
| **Mật khẩu** | BCrypt, không lưu plaintext, không log | ⏳ `PasswordUtil` (Tân) còn rỗng — nhưng module em **không** xử lý mật khẩu |
| **Rò rỉ lỗi** | Trang lỗi không hiện stacktrace | ✅ `web.xml` map 404/500 sang JSP riêng |
| **Tin client** | Giá tiền **không** nhận từ form — đọc lại `Product.price` trong DB | ✅ xong, có test ghim (`OrderServiceTest.checkoutChotGiaTuDbVaTaoPaymentPending`) |
| **Bí mật** | Không hardcode credentials | ✅ `JpaConfig` + `MomoConfig` đều đọc env, fail fast |

> **Nếu bị hỏi về CSRF, trả lời đúng thế này:** *"Em chưa làm token CSRF. Hiện em mới có `HttpOnly` chống JS đọc cookie. Cách đúng là thêm hidden input token sinh theo session và `SameSite=Lax` — em biết chỗ phải sửa, đó là `_header.jspf` và `OrderServlet`."* Đừng nói đã có.


---

## E. Checklist 15 phút trước khi vào phòng chấm

- [ ] `mvn clean package` chạy lại một lần cuối, sạch
- [ ] Tomcat đã start, mở thử `/cart` xác nhận không 404
- [ ] **Có session đăng nhập sẵn** — nếu `LoginServlet` vẫn rỗng thì phải dựng session bằng JSP tạm ở nhà, **đừng** làm trước mặt cô
- [ ] Giỏ hàng đang có **2 máy**
- [ ] Mở sẵn 6 tab theo đúng thứ tự ở [03](03-kich-ban-demo.md#chuẩn-bị-trước-làm-trước-15-phút-không-làm-trước-mặt-cô)
- [ ] **Chụp sẵn 5 ảnh màn hình** để dành làm phương án dự phòng
- [ ] IDE mở sẵn: `Order.java`, `OrderItem.java`, `Cart.java`, `history.jsp`, `cart.jsp`, `MomoGateway.java`
- [ ] Tắt thông báo Messenger/Zalo, thu nhỏ font IDE
- [ ] **Đọc lại mục E của [04](04-cau-hoi-phan-bien.md)** — bốn câu tuyệt đối không nói

---

## F. Đừng làm lúc này

- ❌ Thêm tính năng mới ngoài SDD (voucher, phí ship, tồn kho, hoàn tiền)
- ❌ Đổi schema hay đổi tên cột — mọi thứ đã nhất quán với SDD rồi
- ❌ Refactor CSS — 1137 dòng đang chạy tốt
- ❌ Cài thêm thư viện — `pom.xml` đã đủ
- ❌ **Viết thêm test** — 10/10 đã xanh, thêm test lúc này chỉ rước rủi ro
- ❌ Đụng vào `MomoGateway` — đã chạy thật với sandbox, sửa lúc này là tự bắn chân

---

## G. Kịch bản xấu & cách nói

| Tình huống | Nói gì |
|-----------|--------|
| Tomcat không start | "Em xin demo bằng ảnh chụp và đi thẳng vào source code." → mở IDE, **không** ngồi sửa |
| MoMo không ra trang thanh toán | "Cổng MoMo cần biến môi trường trên máy này chưa set. Em xin demo bằng ảnh chụp lúc chạy thật ở nhà." → **không** sửa env trước mặt cô |
| Cô hỏi phần chưa làm | "Phần đó em chưa xong. Em dự định làm theo hướng..." → nêu thiết kế, đừng im |
| Cô hỏi về module bạn khác | "Phần đó ngoài phạm vi em phụ trách, nhưng theo em hiểu thì..." |
| Cô hỏi câu không biết | "Em chưa xử lý trường hợp đó." → **rồi nêu hướng suy nghĩ**. Im lặng mới mất điểm |
| Hết giờ giữa lúc demo code | Nhảy ngay tới `history.jsp:179` — dòng BR-46 là điểm nhấn, phải nói được |

---

## H. Nếu chỉ còn một buổi

Bảng cũ liệt kê 3 việc ưu tiên. **Cả 3 đã xong.** Giờ chỉ còn đúng một việc nằm
ngoài tầm với của em:

1. **`LoginServlet` + `AuthenticationFilter`** (Tân) — không có nó thì không có
   `currentUser` thật, phải dựng session tay để demo. Hợp đồng bàn giao đã ghi ở
   [../PHOI-HOP-TAN.md](../PHOI-HOP-TAN.md).

Nếu buộc phải tự làm trong một buổi: viết `AuthenticationFilter` (chặn URI bảo vệ,
401 khi vắng `currentUser`) là 30 dòng, và nó là Rule 5 của môn — đáng làm hơn mọi
thứ khác. `LoginServlet` thì có thể bỏ qua nếu vẫn dựng được session.

Còn lại: đọc lại [03](03-kich-ban-demo.md) và [04](04-cau-hoi-phan-bien.md) cho trôi.

