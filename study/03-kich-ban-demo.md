# 03 — Kịch bản demo (8–10 phút)

> Nguyên tắc: **không đọc code thành tiếng**. Mỗi lần mở file chỉ nói 1–2 câu
> "vì sao viết thế này", rồi chuyển. Cô chấm *kỹ năng trình bày* nên mạch lạc
> quan trọng hơn liệt kê.

---

## Chuẩn bị trước (làm trước 15 phút, không làm trước mặt cô)

- [ ] `mvn clean package` chạy sạch, không warning đỏ
- [ ] Tomcat đã start, DB đã có dữ liệu mẫu: **3 Product `AVAILABLE`** với giá khác nhau
- [ ] **Session đã dựng sẵn** — `LoginServlet` còn rỗng nên không đăng nhập bằng
      form được; phải nạp `currentUser` + `cart` vào session từ nhà (JSP tạm hoặc
      Tomcat manager), và **giỏ hàng đang có 2 máy**
- [ ] **Biến môi trường MoMo đã set** (`MOMO_PARTNER_CODE`, `MOMO_ACCESS_KEY`,
      `MOMO_SECRET_KEY`) và **đã restart Tomcat** — thiếu thì nút MoMo chỉ ra
      flash "Cổng thanh toán chưa được cấu hình", demo sẽ mất mạch
- [ ] Đã bấm thử MoMo một lần ở nhà, thấy trang `test-payment.momo.vn` hiện ra
- [ ] Mở sẵn 6 tab theo đúng thứ tự sẽ dùng:
  1. `/cart`
  2. `/checkout`
  3. `/order?action=history`
  4. `/payment?action=view&orderId=1`
  5. IDE: `Cart.java` + `Order.java` + `OrderItem.java`
  6. IDE: `history.jsp` + `cart.jsp`
- [ ] Tắt thông báo Messenger/Zalo. Thu nhỏ font IDE xuống để thấy được nhiều dòng.

---

## 0:00 – 0:40 · Mở đầu

Nói **đúng 4 câu**, không dài hơn:

> "Em phụ trách module **Giỏ hàng – Đặt hàng – Thanh toán**, tức là kênh bán **máy mới**: khách chọn máy, bỏ vào giỏ, đặt hàng, thanh toán, rồi theo dõi đơn.
>
> Module này có hai điểm em muốn cô chú ý: **thứ nhất**, hệ thống có hai kênh bán hàng — khách mua lại máy cũ từ người bán, và khách mua máy mới từ cửa hàng. Hai kênh dùng **chung một bảng `Order`**, phân biệt bằng cột `orderType`. **Thứ hai**, kênh của em có tích hợp **cổng thanh toán MoMo thật** và ràng buộc **tính toàn vẹn khi huỷ đơn**.
>
> Em xin demo giao diện trước, rồi mở source code giải thích."

→ Chuyển tab `/cart`.

---

## 0:40 – 3:30 · Demo giao diện (2 phút 50)

Đi theo **đúng một vòng đời đơn hàng**, không nhảy tab lung tung.

### 0:40 · Giỏ hàng — tab `/cart`

Thao tác:
1. Chỉ vào cột giá, nói: *"Cột giá dùng font mono, chữ số thẳng hàng dọc."*
2. Bấm **Xoá** ở dòng thứ hai.

Lời:
> "Xoá một dòng ở đây **không load lại trang**. Trình duyệt gọi server bằng Fetch, server trả về JSON gồm tổng tiền mới và số món còn lại, JavaScript tự cập nhật con số. Đây là yêu cầu AJAX trong đề bài."

3. Nói thêm một câu: *"Mỗi máy trong giỏ luôn có số lượng bằng 1 — mỗi máy mới là một cá thể duy nhất, không có chuyện mua 2 cái cùng lúc. Em sẽ giải thích ở phần code."*

### 1:40 · Đặt hàng — tab `/checkout`

1. Cuộn xuống phần **Phương thức thanh toán**, chỉ **2 lựa chọn**: COD và Ví MoMo
   ([checkout.jsp:115](../../src/main/webapp/WEB-INF/views/order/checkout.jsp#L115) và
   [:125](../../src/main/webapp/WEB-INF/views/order/checkout.jsp#L125)).
2. Chọn **Ví MoMo**.
3. Bấm **Đặt hàng** → tự chuyển sang trang `/payment`.

Lời:
> "Form chỉ còn hai lựa chọn: COD và Ví MoMo. Enum `PaymentMethod` vẫn giữ giá trị `QR` và `BANK` vì đó là dữ liệu có thể có trong CSDL — xoá đi thì Hibernate sẽ nổ khi đọc dòng cũ. Nhưng giao diện đã đóng, chỉ còn hai đường đi. Còn COD em để `method = null` chứ không thêm vào enum, vì COD nghĩa là chưa có giao dịch trực tuyến nào cả."

> ⚠️ **Chủ động nói câu này.** Enum có 3 giá trị mà form có 2 — nếu để cô tự phát hiện, bạn ở thế bị động.

### 2:10 · Thanh toán — tab `/payment`

1. Chỉ badge `PENDING`, dòng **Mã giao dịch** có nút **COPY**
   ([payment.jsp:76](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L76)).
2. Bấm **Thanh toán qua MoMo** ([payment.jsp:106](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L106)).
3. Trình duyệt nhảy sang `https://test-payment.momo.vn/v2/gateway/pay?…` — **dừng ở đây**, không nhập gì.

Lời:
> "Đây là điểm em muốn cô để ý nhất: **cổng thanh toán là thật**, không phải giả lập. Em gọi REST v2 của MoMo sandbox bằng `HttpURLConnection` của JDK, ký HMAC-SHA256 bằng `javax.crypto`. URL cô đang thấy có chữ ký thật trong query string.
>
> Khi MoMo xác nhận, nó gọi ngược về server em qua IPN; em verify chữ ký rồi đổi `Payment` sang `PAID` **và** `Order` sang `CONFIRMED` trong cùng một transaction. Đường dự phòng khi IPN không tới được máy local là URL `return` — nhưng ở đó em **không tin tham số trên URL**, em gọi lại API `query` lấy kết quả thật."

> Nếu cô hỏi "sao không bấm trả tiền luôn": sandbox cần app **"ứng dụng MoMo Test"** riêng, và phải gỡ app MoMo thật trước. Nói thẳng điều đó — nó chứng tỏ bạn đã đọc tài liệu, không phải làm hỏng.

### 3:00 · Lịch sử đơn — tab `/order?action=history`

1. Chỉ badge trạng thái, chỉ tab lọc.
2. Chỉ nút **Huỷ đơn**.
3. Bấm **Huỷ đơn** → hộp thoại `confirm` hiện ra → bấm **Cancel** (đừng huỷ thật).

Lời:
> "Đơn đang ở trạng thái chờ xử lý nên có nút Huỷ. Thao tác này không hoàn tác được nên em bắt buộc hỏi lại. Còn nút **Đã nhận hàng** chưa hiện — vì nó chỉ hiện khi đơn đã `CONFIRMED` **và** thanh toán đã `PAID`. Em sẽ chỉ đúng dòng code đó."

→ Chuyển sang IDE.

---

## 3:30 – 8:00 · Demo source code (4 phút 30)

### 3:30 · Mô hình dữ liệu — `Cart.java`, `Order.java`, `OrderItem.java`

Mở **`Order.java`**, chỉ vào danh sách field.

> "`Order` có `orderType` — đây là cột phân biệt hai kênh. `MARKETPLACE` là khách mua lại máy cũ, tiền mặt, không có thanh toán. `NEW_DEVICE` là kênh của em, có thanh toán. Một bảng duy nhất cho cả hai, không tách bảng, vì mọi thứ khác — người mua, danh sách hàng, trạng thái — đều giống nhau."

Mở **`OrderItem.java`**, chỉ vào chỗ `listing` **hoặc** `product`.

> "Một dòng hàng chỉ có thể trỏ tới **hoặc** một tin đăng máy cũ **hoặc** một máy mới, không bao giờ cả hai. Và trường `price` ở đây là **ảnh chụp giá tại thời điểm đặt**. Nếu mai Admin đổi giá niêm yết, đơn cũ vẫn giữ giá cũ — đây là yêu cầu nghiệp vụ, không phải em quên cập nhật."

Mở **`Cart.java:62`** — hàm `getSubtotal()`.

> "Tổng tiền **không lưu trong CSDL**, mà tính từ các dòng chi tiết. Lý do: nếu lưu một cột `totalAmount` rồi ai đó sửa một dòng hàng mà quên cập nhật cột đó, dữ liệu sẽ mâu thuẫn. Tính lại thì không bao giờ lệch.
>
> Và tất cả các phép tính tiền đều dùng `BigDecimal` với `multiply`/`add`, không dùng `double`. Vì `double` là số dấu phẩy động nhị phân, không biểu diễn chính xác số thập phân, cộng nhiều lần sẽ tích luỹ sai số."

### 5:00 · Rẽ nhánh giao diện — `history.jsp`

Mở **`history.jsp`**, cuộn tới khoảng dòng 76–96.

> "Toàn bộ file này **không có một dòng Java nào** — không có scriptlet. Em dùng JSTL và EL. Ví dụ ở đây em đổi enum `OrderStatus` thành nhãn tiếng Việt bằng `c:choose`, và tính luôn class CSS cho badge."

Cuộn xuống **`history.jsp:179`** — điều kiện nút "Đã nhận hàng".

> "Đây là dòng em muốn cô xem nhất:
> ```
> <c:if test="${order.status == 'CONFIRMED' and payment.status == 'PAID'}">
> ```
> Nút 'Đã nhận hàng' chỉ hiện khi **đồng thời** đơn đã xác nhận **và** tiền đã vào. Thiếu một trong hai thì không hiện. Đây là ràng buộc BR-46 trong SDD."

Chỉ vào **`history.jsp:156`**.

> "Cô có thể thắc mắc vì sao em viết `${payments[order.id]}` mà không phải `${order.payment}`. Vì `Order` **không có** trường `payment` — quan hệ một-một nằm ở phía `Payment`, và `Payment.orderId` là duy nhất. Nên servlet phải tra một map rồi truyền vào. Đây là chủ ý thiết kế để tránh quan hệ vòng."

### 6:20 · AJAX — `cart.jsp`

Mở **`cart.jsp`**, cuộn tới khoảng dòng 140–177.

> "Đây là phần AJAX. Chú ý comment ngay trên: em ghi rõ **hợp đồng JSON** giữa client và server — server trả về `ok`, `subtotal`, `count`. Ghi ngay trong file để người viết servlet sau này biết phải trả gì."

Chỉ vào dòng `var vnd = new Intl.NumberFormat(...)`.

> "Số tiền do JavaScript render ra phải giống hệt số tiền do JSTL render ra ở lần load đầu, nên em dùng cùng locale `vi-VN`."

### 7:00 · Điểm nhấn: transaction & cổng thanh toán

> "Hai thứ khó nhất trong module này em xin nói ở tầng code.
>
> **Thứ nhất — huỷ đơn là thao tác hai bảng.** Khi huỷ một đơn máy mới, trong **cùng một transaction** em phải chuyển `Payment` từ `PENDING` sang `FAILED`. Nếu không gói chung, hệ thống có thể crash giữa hai bước và để lại đơn đã huỷ nhưng vẫn báo chờ thanh toán. Còn nếu khách đã trả tiền rồi thì `Payment` **giữ nguyên** — bản MVP không có hoàn tiền, nên `PaymentStatus` cố tình chỉ có ba giá trị và không có `REFUNDED`.
>
> **Thứ hai — thanh toán MoMo cũng là transaction hai bảng, chiều ngược lại.** Khi MoMo báo đã trả tiền, `Payment` sang `PAID` kèm `paidAt` **và** `Order` từ `PENDING` sang `CONFIRMED` trong cùng một transaction. Cả IPN lẫn URL `return` đều gọi chung một hàm `markPaidByGateway`, nên hai đường về không bao giờ cho ra hai trạng thái khác nhau.
>
> **Chống bán trùng** thì kênh của em nhẹ hơn kênh marketplace: giỏ hàng **không giữ chỗ** sản phẩm, nên chỗ duy nhất có thể tranh mua là lúc checkout. Em kiểm tra `Product.status == AVAILABLE` **bên trong** transaction tạo đơn, không kiểm tra trước rồi mới tạo — nếu kiểm tra trước, giữa hai bước đó người khác mua mất. Không `AVAILABLE` thì ném exception để rollback, không tạo Order dở dang."

> Nói thêm một câu nếu còn giờ: *"Em cố tình không nói mình làm lock dòng Buyer — đó là rule chống tranh mua của kênh marketplace, thuộc phần bạn khác."*

---

## 8:00 – 9:00 · Kết

> "Tóm lại module của em có ba điểm:
> 1. **Một bảng `Order` cho hai kênh bán**, phân biệt bằng `orderType` — không nhân đôi schema.
> 2. **Tiền dùng `BigDecimal` và tổng được tính chứ không lưu** — tránh sai số và tránh dữ liệu mâu thuẫn.
> 3. **Cổng thanh toán thật** — MoMo REST v2, ký và verify HMAC-SHA256, IPN idempotent; cộng với ràng buộc nghiệp vụ ở tầng service: huỷ đơn và xác nhận thanh toán đều là transaction hai bảng.
>
> Bốn tầng servlet, service, repository, view của module em đã xong và đã chạy end-to-end trên Tomcat; **10/10 test xanh**."

**Nói thẳng phần chưa xong** — đừng để cô tự phát hiện. Nói trước sẽ được tính là trung thực, bị phát hiện sẽ bị tính là giấu.

Phần còn hở, nói đúng một câu:
> "Còn một điểm chưa xong: `AuthenticationFilter` đang rỗng — hiện các servlet tự kiểm tra `currentUser` và trả 401. Filter thuộc phần bạn em phụ trách session, em đã viết sẵn hợp đồng bàn giao."

Đừng nói "em đang hoàn thiện tầng repository/service" — không còn đúng nữa, và nói sai về chính bài của mình còn tệ hơn nói thiếu.

---

## Nếu bị hỏi câu khó ngay giữa lúc demo

Câu ngắn: *"Câu này hay, em xin trả lời sau phần demo để không cắt mạch — em có chuẩn bị sẵn ở phần cuối."* Rồi tiếp tục. Chỉ làm vậy **một lần**; lần thứ hai thì trả lời luôn.

## Nếu demo giao diện không chạy được

Chuyển ngay sang **ảnh chụp màn hình** đã chuẩn bị sẵn (chụp 5 màn trước ở nhà). Nói: *"Phần chạy thật em demo sau, giờ em xin mở source code trước."* Không ngồi sửa lỗi trước mặt cô.
