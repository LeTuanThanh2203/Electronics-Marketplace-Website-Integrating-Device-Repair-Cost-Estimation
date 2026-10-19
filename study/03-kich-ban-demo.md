# 03 — Kịch bản demo (8–10 phút)

> Nguyên tắc: **không đọc code thành tiếng**. Mỗi lần mở file chỉ nói 1–2 câu
> "vì sao viết thế này", rồi chuyển. Cô chấm *kỹ năng trình bày* nên mạch lạc
> quan trọng hơn liệt kê.

---

## Chuẩn bị trước (làm trước 15 phút, không làm trước mặt cô)

- [ ] `mvn clean package` chạy sạch, không warning đỏ
- [ ] Tomcat đã start, DB đã có dữ liệu mẫu: **3 Product `AVAILABLE`** với giá khác nhau
- [ ] Đã đăng nhập sẵn một tài khoản Customer, **giỏ hàng đang có 2 máy**
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
> Module này có hai điểm em muốn cô chú ý: **thứ nhất**, hệ thống có hai kênh bán hàng — khách mua lại máy cũ từ người bán, và khách mua máy mới từ cửa hàng. Hai kênh dùng **chung một bảng `Order`**, phân biệt bằng cột `orderType`. **Thứ hai**, kênh của em có ràng buộc về **chống tranh mua** và **tính toàn vẹn khi huỷ đơn**.
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

1. Cuộn xuống phần **Phương thức thanh toán**, chỉ 4 lựa chọn.
2. Chọn **Chuyển khoản QR**.
3. Bấm **Đặt hàng**.

Lời:
> "Ba phương thức QR, MOMO, BANK map thẳng vào enum `PaymentMethod` đúng như SDD. Còn COD là lựa chọn em lấy thêm từ bản mockup — em **không** thêm nó vào enum mà để `method = null`, vì COD nghĩa là chưa có giao dịch trực tuyến nào cả."

> ⚠️ **Nói câu này trước khi cô hỏi.** SDD chỉ có 3 giá trị mà form có 4 — nếu để cô tự phát hiện, bạn ở thế bị động. Chủ động nói là bạn đang kiểm soát thiết kế.

### 2:30 · Thanh toán — tab `/payment`

1. Chỉ badge `PENDING`.
2. Bấm nút **COPY** ở dòng "Nội dung chuyển khoản".

Lời:
> "Đây là điểm em muốn cô để ý: **hệ thống không tích hợp cổng thanh toán**. Không có QR động, không có callback. Hệ thống tự sinh một `Payment` ở trạng thái `PENDING` kèm mã giao dịch, khách chuyển khoản thủ công, rồi **Admin xác nhận tay**. Đây là quyết định trong SDD chứ không phải em làm thiếu."

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

Mở **`Cart.java:31`** — hàm `getSubtotal()`.

> "Tổng tiền **không lưu trong CSDL**, mà tính từ các dòng chi tiết. Lý do: nếu lưu một cột `totalAmount` rồi ai đó sửa một dòng hàng mà quên cập nhật cột đó, dữ liệu sẽ mâu thuẫn. Tính lại thì không bao giờ lệch.
>
> Và tất cả các phép tính tiền đều dùng `BigDecimal` với `multiply`/`add`, không dùng `double`. Vì `double` là số dấu phẩy động nhị phân, không biểu diễn chính xác số thập phân, cộng nhiều lần sẽ tích luỹ sai số."

### 5:00 · Rẽ nhánh giao diện — `history.jsp`

Mở **`history.jsp`**, cuộn tới khoảng dòng 74–107.

> "Toàn bộ file này **không có một dòng Java nào** — không có scriptlet. Em dùng JSTL và EL. Ví dụ ở đây em đổi enum `OrderStatus` thành nhãn tiếng Việt bằng `c:choose`, và tính luôn class CSS cho badge."

Cuộn xuống **`history.jsp:178`** — điều kiện nút "Đã nhận hàng".

> "Đây là dòng em muốn cô xem nhất:
> ```
> <c:if test="${order.status == 'CONFIRMED' and payment.status == 'PAID'}">
> ```
> Nút 'Đã nhận hàng' chỉ hiện khi **đồng thời** đơn đã xác nhận **và** tiền đã vào. Thiếu một trong hai thì không hiện. Đây là ràng buộc BR-46 trong SDD."

Chỉ vào **`history.jsp:155`**.

> "Cô có thể thắc mắc vì sao em viết `${payments[order.id]}` mà không phải `${order.payment}`. Vì `Order` **không có** trường `payment` — quan hệ một-một nằm ở phía `Payment`, và `Payment.orderId` là duy nhất. Nên servlet phải tra một map rồi truyền vào. Đây là chủ ý thiết kế để tránh quan hệ vòng."

### 6:20 · AJAX — `cart.jsp`

Mở **`cart.jsp`**, cuộn tới khoảng dòng 145–177.

> "Đây là phần AJAX. Chú ý comment ngay trên: em ghi rõ **hợp đồng JSON** giữa client và server — server trả về `ok`, `subtotal`, `count`. Ghi ngay trong file để người viết servlet sau này biết phải trả gì."

Chỉ vào dòng `var vnd = new Intl.NumberFormat(...)`.

> "Số tiền do JavaScript render ra phải giống hệt số tiền do JSTL render ra ở lần load đầu, nên em dùng cùng locale `vi-VN`."

### 7:00 · Điểm nhấn: race-condition & transaction

> "Hai thứ khó nhất trong module này em chưa demo được vì tầng service đang làm dở, nhưng em xin nói thiết kế.
>
> **Thứ nhất — chống bán trùng.** Kênh của em, giỏ hàng **không giữ chỗ** sản phẩm — bỏ vào giỏ không làm máy đó thành đã bán. Nên chỗ duy nhất có thể xảy ra tranh mua là lúc checkout. Cách chặn là kiểm tra `Product.status == AVAILABLE` **bên trong** transaction tạo đơn, chứ không kiểm tra trước rồi mới tạo. Nếu kiểm tra trước, giữa hai bước đó người khác mua mất. Không `AVAILABLE` thì em ném exception để rollback — không tạo Order dở dang.
>
> **Thứ hai — huỷ đơn là thao tác hai bảng.** Khi huỷ một đơn máy mới, trong **cùng một transaction** em phải chuyển `Payment` từ `PENDING` sang `FAILED`. Nếu không gói chung, hệ thống có thể crash giữa hai bước và để lại đơn đã huỷ nhưng vẫn báo chờ thanh toán. Còn nếu khách đã trả tiền rồi thì `Payment` **giữ nguyên** — bản MVP không có hoàn tiền, nên `PaymentStatus` cố tình chỉ có ba giá trị và không có `REFUNDED`."

---

## 8:00 – 9:00 · Kết

> "Tóm lại module của em có ba điểm:
> 1. **Một bảng `Order` cho hai kênh bán**, phân biệt bằng `orderType` — không nhân đôi schema.
> 2. **Tiền dùng `BigDecimal` và tổng được tính chứ không lưu** — tránh sai số và tránh dữ liệu mâu thuẫn.
> 3. **Ràng buộc nghiệp vụ đặt ở tầng view và tầng service**, ví dụ nút 'Đã nhận hàng' chỉ hiện khi đủ hai điều kiện, và huỷ đơn phải là một transaction hai bảng.
>
> Phần giao diện và model em đã xong. Tầng repository và service em đang hoàn thiện."

**Nói thẳng phần chưa xong** — đừng để cô tự phát hiện. Nói trước sẽ được tính là trung thực, bị phát hiện sẽ bị tính là giấu.

---

## Nếu bị hỏi câu khó ngay giữa lúc demo

Câu ngắn: *"Câu này hay, em xin trả lời sau phần demo để không cắt mạch — em có chuẩn bị sẵn ở phần cuối."* Rồi tiếp tục. Chỉ làm vậy **một lần**; lần thứ hai thì trả lời luôn.

## Nếu demo giao diện không chạy được

Chuyển ngay sang **ảnh chụp màn hình** đã chuẩn bị sẵn (chụp 5 màn trước ở nhà). Nói: *"Phần chạy thật em demo sau, giờ em xin mở source code trước."* Không ngồi sửa lỗi trước mặt cô.
