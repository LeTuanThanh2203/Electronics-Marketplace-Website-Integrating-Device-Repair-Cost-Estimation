# 04 — Câu hỏi phản biện & câu trả lời mẫu

> Cách trả lời ăn điểm: **nêu lý do trước, nêu code sau**. Cô không cần nghe
> bạn đọc lại code, cô cần biết bạn *hiểu vì sao*.

---

## A. Câu hỏi về thiết kế dữ liệu

### A1. "Vì sao hai kênh bán lại dùng chung một bảng `Order`? Sao không tách ra?"

Vì mọi thứ **giống nhau**: cùng người mua, cùng danh sách hàng, cùng vòng đời trạng thái `PENDING → CONFIRMED → COMPLETED/CANCELLED`, cùng cần lịch sử đơn. Chỉ **hai** thứ khác: nguồn hàng (tin đăng máy cũ vs máy mới trong kho) và có thanh toán hay không.

Tách bảng sẽ phải nhân đôi toàn bộ query lịch sử, nhân đôi phân trang, nhân đôi màn hình. Nên em giữ một bảng, thêm cột `orderType`, và mọi quy tắc riêng của kênh marketplace — giữ chỗ tin đăng, giới hạn số đơn — đều **chỉ áp dụng khi `orderType = MARKETPLACE`**.

→ Mở [history.jsp:100-103](../../src/main/webapp/WEB-INF/views/order/history.jsp#L100-L103) chỉ chỗ rẽ nhánh.

---

### A2. "`OrderItem` có cả `listing` và `product` — sao không dùng một trường chung?"

Vì hai thứ đó là hai entity khác nhau, không có lớp cha chung. Một dòng hàng **hoặc** trỏ tới tin đăng máy cũ **hoặc** trỏ tới máy mới trong kho, không bao giờ cả hai — ràng buộc này ghi rõ trong SDD.

Nếu gộp thành một trường `itemId` chung thì mất khoá ngoại, mất tính toàn vẹn tham chiếu, và phải tự kiểm tra thủ công. Đây là đánh đổi có ý thức: chấp nhận hai cột nullable để giữ được FK thật.

---

### A3. "Vì sao `OrderItem.price` lưu lại giá? Sao không đọc từ `Product`?"

Vì **giá phải là ảnh chụp tại thời điểm đặt**. Nếu mai Admin đổi giá niêm yết, đơn cũ phải giữ nguyên giá cũ — đó là điều khách đã đồng ý trả.

Nếu đọc động từ `Product`, mọi đơn cũ sẽ tự đổi giá theo, và tổng tiền lịch sử sẽ sai. Đây là nguyên tắc chuẩn của mọi hệ thống thương mại điện tử; SDD phát biểu nó thành BR-41 (cho phía `Listing`), và em áp dụng cùng nguyên tắc cho phía `Product`.

---

### A4. "Vì sao `Order` không có trường `payment`?"

Vì quan hệ là **một-nhiều-nhất-một**: một đơn có tối đa một thanh toán, và ràng buộc "tối đa một" được đảm bảo bằng unique index trên `Payment.orderId` — ở phía `Payment`.

Nếu để `Order.paymentId` thì không có gì chặn được hai Payment cùng trỏ vào một Order. Đặt khoá ngoại ở phía "nhiều" là cách chuẩn để biểu diễn quan hệ một-một.

**Hệ quả ở tầng view:** JSP không viết được `${order.payment}`, nên servlet phải tra một `Map<orderId, Payment>` rồi truyền vào — [history.jsp:156](../../src/main/webapp/WEB-INF/views/order/history.jsp#L156).

---

### A5. "Vì sao tổng tiền không lưu thành cột?"

Vì đó là **dữ liệu dẫn xuất**. Lưu nó ra là tạo cơ hội cho dữ liệu mâu thuẫn: sửa một dòng hàng mà quên cập nhật tổng, hoặc ngược lại.

Tính lại mỗi lần đọc thì không bao giờ lệch, và với quy mô một giỏ hàng vài món thì chi phí không đáng kể. Đây là đánh đổi có ý thức: đổi chút CPU lấy tính đúng đắn.

SDD ghi rõ điều này ở phần đặc tả `Order`: `getTotalAmount()` = `SUM(OrderItem.price × quantity)`, "computed instead of storing a denormalized totalAmount field".

→ [Cart.java:62](../../src/main/java/com/electronics/marketplace/entity/Cart.java#L62)

---

### A6. "Sao không dùng `double` cho giá tiền?"

Vì `double` là số dấu phẩy động **nhị phân** — nó không biểu diễn chính xác `0.1`. Cộng nhiều lần sẽ tích luỹ sai số, và với tiền thì sai một đồng cũng là lỗi.

`BigDecimal` biểu diễn thập phân chính xác, có `multiply`/`add`/`setScale` để kiểm soát làm tròn. Mọi trường tiền trong module em đều là `BigDecimal`.

---

## B. Câu hỏi về nghiệp vụ

### B1. "Nếu hai khách cùng mua một máy thì sao?"

Kênh máy mới của em: giỏ hàng **không giữ chỗ** sản phẩm — bỏ vào giỏ không làm máy đó thành "đã bán". Đây là BR-45, và cũng là lý do kênh của em **không cần lock**.

Nên chỗ duy nhất có thể tranh mua là lúc checkout. Cách chặn: kiểm tra `Product.status == AVAILABLE` **bên trong** transaction tạo đơn, không kiểm tra trước rồi mới tạo. Nếu một máy đã bán, em ném exception để rollback toàn bộ — không tạo đơn dở dang.

> **Nếu cô hỏi về `SELECT ... FOR UPDATE`:** đó là [BR-31/BR-32](../../reference/_sdd.txt) — rule của kênh **MARKETPLACE**, nơi phải giữ chỗ tin đăng và giới hạn quota N đơn. Kênh của em không giữ chỗ nên không cần. Nói rõ vậy, **đừng nhận là mình làm lock**.

---

### B2. "Huỷ đơn thì thanh toán xử lý sao?"

Ba trường hợp:

| Payment lúc huỷ | Sau khi huỷ |
|---|---|
| `PENDING` | → `FAILED`, **trong cùng transaction** với việc huỷ đơn |
| `PAID` | **giữ nguyên** `PAID` |
| không có (đơn marketplace) | không đụng tới |

Giữ nguyên khi đã `PAID` vì bản MVP **không có hoàn tiền** — hoàn tiền là quy trình kế toán ngoài hệ thống. Và đó cũng là lý do `PaymentStatus` cố tình **không có** giá trị `REFUNDED`.

Điểm quan trọng: hai bảng phải nằm trong **một transaction**. Nếu tách ra, hệ thống có thể crash giữa hai bước và để lại đơn đã huỷ nhưng vẫn báo chờ thanh toán.

---

### B3. "Vì sao số lượng trong giỏ luôn bằng 1?"

Vì mỗi máy mới là **một cá thể duy nhất** — có số serial, có tình trạng riêng. Không có chuyện "mua 3 cái iPhone 15" từ một dòng sản phẩm; muốn 3 máy thì kho phải có 3 cá thể, và mỗi cá thể là một dòng riêng.

Đây là BR-45. Nếu quantity > 1 thì phải có cột tồn kho, phải trừ kho, phải xử lý hoàn kho — nằm ngoài phạm vi MVP.

**Hệ quả ở giao diện:** bản mockup có stepper chọn số lượng 1..5, em **bỏ**, và ghi lý do ngay trong comment đầu file để người đọc sau không tưởng là em quên.

---

### B4. "Vì sao chọn COD lại để `method = null`?"

⚠️ **Câu này cô rất dễ hỏi** — vì SDD §12.4 ghi `method ∈ {QR, MOMO, BANK}`, tức **3** giá trị, mà form của em chỉ có **2** lựa chọn (COD + MoMo), và COD lại không nằm trong enum. Trả lời thẳng:

> "Enum `PaymentMethod` vẫn giữ đúng 3 giá trị như SDD: `QR`, `MOMO`, `BANK`. Em **không** xoá `QR` và `BANK` dù giao diện không còn dùng, vì enum mô tả **dữ liệu có thể có trong CSDL** — xoá đi thì Hibernate sẽ nổ khi đọc một dòng Payment cũ mang giá trị đó. Cái em đóng lại là **giao diện**: form chỉ còn COD và MoMo, nên không có đường nào sinh ra Payment `QR`/`BANK` mới nữa.
>
> Còn COD em **không** thêm vào enum, để `method = null`: COD nghĩa là **không có giao dịch trực tuyến nào cả**, nên `method` không có gì để mô tả. Thêm `COD` vào enum sẽ buộc mọi chỗ xử lý thanh toán phải rẽ nhánh "nếu COD thì bỏ qua", làm hỏng ý nghĩa của enum."

→ [checkout.jsp:115](../../src/main/webapp/WEB-INF/views/order/checkout.jsp#L115) (radio COD, `value=""`) và [:125](../../src/main/webapp/WEB-INF/views/order/checkout.jsp#L125) (radio MoMo) và [payment.jsp:67](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L67) (hiển thị COD).

> **Nếu cô hỏi "vậy đơn COD có Payment không?"** — Trả lời: theo SDD thì mọi đơn `NEW_DEVICE` đều sinh Payment ([BR-44](../../reference/_sdd.txt)), nhưng đơn COD không có giao dịch trực tuyến nào để xác nhận. Đây là **khoảng trống trong SDD**, không phải lỗi code. Em chọn hướng: vẫn tạo `Payment` ở `PENDING` với `method = null`, và Admin tự chuyển thẳng `PAID` khi giao hàng. **Nói rõ đây là chỗ em phải tự quyết vì SDD chưa chốt.**

---

### B5. "Sao không tích hợp cổng thanh toán thật?"

**Câu này giờ phải trả lời ngược lại** — em **đã** tích hợp rồi.

> "Em có tích hợp, và là tích hợp thật chứ không giả lập: gọi REST v2 của MoMo sandbox. Ba việc phải làm cho đúng:
>
> **Một — ký request.** MoMo yêu cầu chuỗi ký là các field xếp **theo alphabet**, nối bằng `&`, rồi HMAC-SHA256 bằng `secretKey`. Sai thứ tự field là sai chữ ký, sai một ký tự là sai luôn.
>
> **Hai — không tin callback.** Có hai đường về: IPN (MoMo gọi server-to-server) và URL redirect người dùng. Với IPN em verify chữ ký trên tham số đã URL-decode; với URL redirect em **bỏ qua** tham số trên URL và gọi lại `query` API lấy kết quả thật — vì người dùng sửa được query string.
>
> **Ba — idempotent.** MoMo gửi IPN lặp tới khi nhận HTTP 200, nên 'đã PAID' là trạng thái bình thường, không phải lỗi. Em trả 200 và không đổi gì thêm.
>
> Không dùng SDK vì repo chính thức `momo-wallet/payment` không có bản Java, còn SDK Java cộng đồng kéo `log4j 1.2.17` đã EOL. Project target Java 17 nên `java.net.http.HttpClient` có sẵn — em vẫn dùng `HttpURLConnection` + `javax.crypto` vì đủ dùng, không phải viết thêm gì và không thêm dependency nào."

→ [MomoGateway.java](../../src/main/java/com/electronics/marketplace/service/MomoGateway.java) · [PaymentServlet.java:113](../../src/main/java/com/electronics/marketplace/controller/user/PaymentServlet.java#L113) · chi tiết ở [02 — mục 21](02-kien-thuc-da-ap-dung.md#21-tích-hợp-cổng-thanh-toán--hmac-sha256--callback).

> **Nếu cô hỏi "sao không chạy được giao dịch thật trên máy cô?"** — sandbox cần app **"ứng dụng MoMo Test"** riêng, phải gỡ app MoMo thật trước khi cài, và `payUrl` có TTL ngắn. Đó là giới hạn của phía MoMo, không phải của code. Đường IPN cũng cần URL công khai nên localhost không nhận được — em đã chuẩn bị biến `MOMO_PUBLIC_BASE_URL` cho lúc tunnel/deploy.

---

### B6. "Đơn khi nào được coi là hoàn thành?"

Chỉ khi **đồng thời** đơn ở `CONFIRMED` **và** thanh toán ở `PAID`. Đây là BR-46, và em cài đặt nó ở hai tầng: tầng view ẩn nút nếu chưa đủ điều kiện, tầng service từ chối nếu client cố gửi request.

→ [history.jsp:179](../../src/main/webapp/WEB-INF/views/order/history.jsp#L179)

---

## C. Câu hỏi về kỹ thuật Java Web

### C1. "Vì sao không viết Java trong JSP?"

Vì JSP là **view**, không phải controller. Viết `<% %>` khiến logic nằm rải rác trong file HTML, không test được, không tái sử dụng được, và khi có lỗi thì phải sửa ở nhiều màn hình.

Em dùng EL và JSTL: `<c:choose>` để rẽ nhánh, `<c:forEach>` để duyệt, `<fmt:formatNumber>` để định dạng tiền. Cả 5 màn hình của em **không có một scriptlet nào**.

Đây cũng là Rule 1 trong yêu cầu môn học.

---

### C2. "Vì sao dùng một servlet với tham số `action` thay vì mỗi thao tác một servlet?"

Vì các thao tác trên cùng một tài nguyên nên nằm cùng một controller. `CartServlet` xử lý mọi thứ liên quan tới giỏ, `OrderServlet` mọi thứ liên quan tới đơn. Một servlet một thao tác sẽ đẻ ra hàng chục class gần giống nhau.

Đánh đổi: bên trong phải có `switch` theo `action`. Nhưng đó là một chỗ rẽ nhánh duy nhất, dễ đọc hơn nhiều so với việc rải rác.

Đây cũng là Rule 1 của môn.

---

### C3. "Vì sao dùng `FetchType.LAZY`?"

Vì `EAGER` khiến mỗi lần load một `Order` là load luôn toàn bộ `OrderItem`, và nếu `OrderItem` lại trỏ tới `Product` thì thành chuỗi load lan. Load 20 đơn hàng có thể thành hàng trăm câu query — đây là lỗi N+1 kinh điển.

`LAZY` chỉ load khi thực sự gọi getter. Cái giá là phải dùng `JOIN FETCH` khi biết trước sẽ cần, và phải cẩn thận `LazyInitializationException` nếu truy cập ngoài transaction.

Đây là Rule 2 của môn, bắt buộc với mọi quan hệ collection.

---

### C4. "Vì sao để mật khẩu trong biến môi trường mà không để trong file config?"

Vì file config nằm trong git, và git thì có lịch sử — commit một lần là mật khẩu nằm vĩnh viễn trong repo dù sau đó có xoá.

Em đọc từ `System.getenv()` trong [JpaConfig](../../src/main/java/com/electronics/marketplace/config/JpaConfig.java) rồi truyền vào override map. Có một chi tiết em từng vấp: Hibernate 6.4.4 **không** hỗ trợ cú pháp `${env.DB_URL}` trong `persistence.xml` — nó chỉ thay `${tên_system_property}`. Thử cách đó thì app chết lúc khởi động với lỗi `HHH10001004: No JDBC URL specified`. Nên việc đọc biến môi trường **buộc** phải làm ở tầng Java.

Em còn viết test ghim lại hai hành vi này để sau này ai "đơn giản hoá" bằng cách xoá `JpaConfig` thì test đỏ ngay — [JpaConfigTest.java](../../src/test/java/com/electronics/marketplace/config/JpaConfigTest.java).

---

### C5. "AJAX khác gì so với load lại trang?"

Load lại trang: trình duyệt gửi request, server trả về **cả trang HTML**, trình duyệt render lại từ đầu — mất vị trí cuộn, nháy màn hình.

AJAX: JavaScript gửi request, server trả về **chỉ dữ liệu JSON**, JavaScript tự cập nhật đúng phần tử cần đổi.

Trong giỏ hàng, xoá một dòng chỉ cần đổi con số tổng tiền và số món trên header — không cần dựng lại cả trang. Nên server trả `{ok, subtotal, count}`.

→ [cart.jsp:149-177](../../src/main/webapp/WEB-INF/views/cart/cart.jsp#L149-L177)

---

### C6. "Vì sao `web.xml` rỗng mà app vẫn chạy được?"

Vì từ Servlet 3.0 trở đi có thể khai báo servlet và filter bằng **annotation** ngay trên class (`@WebServlet`, `@WebFilter`) thay vì trong `web.xml`.

Trong module em, cả 4 servlet đều dùng `@WebServlet`: `CartServlet`, `CheckoutServlet`, `OrderServlet`, `PaymentServlet`. `web.xml` chỉ còn giữ 4 việc: `jsp-config` khai encoding UTF-8 cho `*.jsp`/`*.jspf` (fragment không có page directive nên thiếu dòng này là mojibake), `welcome-file`, `session-config` (`HttpOnly`, `tracking-mode COOKIE`, timeout 30), và `error-page` 404/500 để không bao giờ đổ stacktrace ra cho người dùng.

Ví dụ [PaymentServlet.java:38](../../src/main/java/com/electronics/marketplace/controller/user/PaymentServlet.java#L38) — `@WebServlet("/payment")`, không có dòng nào trong `web.xml` nhắc tới nó.

> **Nếu cô hỏi "vậy filter thì sao?"** — `AuthenticationFilter` hiện **rỗng**, đây là điểm hở duy nhất còn lại và thuộc phần bạn khác trong nhóm. Hiện các servlet tự kiểm tra `currentUser` và trả 401; khi filter xong thì bỏ phần kiểm tra trùng đó. Nói thẳng, đừng nhận là xong.

---

## D. Câu hỏi "hỏi xoáy" — chuẩn bị tinh thần

### D1. "Nếu cô bấm F5 ở trang thanh toán thì sao?"

Trang thanh toán là `GET`, chỉ đọc dữ liệu, nên F5 vô hại.

Nhưng **form đặt hàng** là `POST`. Nếu khách F5 ở trang kết quả thì trình duyệt hỏi "gửi lại form?". Cách xử lý chuẩn là **POST-redirect-GET**: sau khi tạo đơn xong, servlet không forward sang JSP mà `sendRedirect` sang `/payment?action=view&orderId=...`. Như vậy F5 chỉ load lại trang GET, không tạo đơn thứ hai.

> Riêng nút "Thanh toán qua MoMo" cũng là `GET` và **cố tình** dùng được nhiều lần: mỗi lần bấm sinh một `orderId` mới cho MoMo (MoMo trả `resultCode 41` nếu trùng). Bấm F5 chỉ tạo thêm một phiên thanh toán mới, không sinh đơn mới, và phiên cũ tự hết hạn.

### D2. "Nếu khách sửa `orderId` trong form huỷ đơn thành đơn của người khác?"

Đây là lỗ hổng **IDOR** kinh điển. Phòng thủ **bắt buộc** ở tầng service: load đơn theo id, rồi kiểm tra `order.buyer.id == currentUser.id`, không khớp thì trả 403. Không bao giờ tin `orderId` từ client.

Cùng cách áp dụng cho `/payment?action=view&orderId=`. Đây là code thật, không phải thiết kế:

```java
// PaymentServlet.java:96
if (!canView(user, payment)) {
    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

Đã verify bằng request thật: user khác mở payment của người khác → **403**, và huỷ đơn người khác → **403**. Riêng `action=create` chặt hơn — chỉ Buyer, **Admin cũng không** tạo giao dịch hộ được ([PaymentServlet.java:131](../../src/main/java/com/electronics/marketplace/controller/user/PaymentServlet.java#L131)), vì tiền phải do chính người mua trả.

### D3. "Session của em chứa gì?"

Chỉ hai thứ: `currentUser` và `cart`. Không chứa danh sách đơn hàng.

Lý do: session nằm trong RAM server. Nhồi danh sách lớn vào session thì vài trăm người dùng đồng thời là hết RAM. Danh sách đơn hàng nạp theo từng request và có phân trang.

Đây là Rule 3 của môn.

### D4. "Vì sao giá hiển thị thẳng cột được?"

Vì em dùng `font-variant-numeric: tabular-nums`. Mặc định font dùng chữ số tỉ lệ — chữ `1` hẹp hơn chữ `8` — nên khi số thay đổi, cột giá bị nhảy. `tabular-nums` cho mọi chữ số cùng bề rộng.

→ [order.css:57](../../src/main/webapp/css/order.css#L57)

### D5. "Sao em chọn CSS thuần thay vì Bootstrap/Tailwind?"

Bản mockup dùng Tailwind qua CDN. Em chuyển sang CSS thuần vì ba lý do: không cần build step, không phụ thuộc script ngoài, và toàn bộ design token nằm một chỗ trong `:root` nên đổi màu chủ đạo chỉ sửa một dòng.

Với quy mô 5 màn hình thì CSS thuần khoảng 1100 dòng là hợp lý; nếu là 50 màn hình thì em sẽ cân nhắc lại.

→ [order.css:8-33](../../src/main/webapp/css/order.css#L8-L33)

### D6. "Em có viết test không?"

Có — **10 test, `mvn -o test` xanh 10/10**, chia bốn file:

| File | Số test | Ghim điều gì |
|---|---|---|
| `JpaConfigTest` | 2 | Placeholder `${env.X}` không được Hibernate thay; override map mới là đường đúng |
| `EntityMappingTest` | 1 | Boot EMF thật, ghi/đọc lại 5 entity, assert trên **metamodel** chứ không `information_schema` |
| `MomoGatewayTest` | 3 | Vector HMAC-SHA256 đối chiếu độc lập bằng `openssl dgst -sha256 -hmac`; parse response; verify IPN |
| `OrderServiceTest` | 4 | Checkout chốt giá từ DB chứ không từ form; huỷ đơn trả `Listing` về `AVAILABLE`; hai người không mua được cùng một `Listing`; `complete` bị chặn khi chưa `PAID` |

`OrderServiceTest` chạy trên **PostgreSQL thật** (không mock), mỗi test tự dọn dữ liệu trong `@AfterEach` — nên nó bắt được cả lỗi transaction và ràng buộc unique, thứ mà mock không bắt được.

> Phần **chưa** có test là tầng servlet (HTTP status, forward/redirect). Lý do trung thực: `AuthenticationFilter` còn rỗng nên chưa dựng được session thật trong test; hiện em kiểm bằng request tay (bảng kết quả ở [00 — Bước 9](00-tien-do-cong-viec.md#bước-9-đã-chạy-được-gì-tomcat-10157-context-shb)). Khi filter xong thì viết test servlet bằng `MockHttpServletRequest`.

---

## E. Bốn câu tuyệt đối không được nói

| ❌ Đừng nói | ✅ Nói thay |
|---|---|
| "Cái này em copy trên mạng" | "Em tham khảo cách làm chuẩn rồi áp vào bài toán của mình" |
| "Em không biết" (rồi im) | "Em chưa xử lý trường hợp đó. Em nghĩ hướng đúng sẽ là..." |
| "Cái đó của bạn khác trong nhóm" | "Phần đó ngoài phạm vi em phụ trách, nhưng theo em hiểu thì..." |
| "Cái này em làm rồi" (khi chưa làm) | "Em chưa xong phần đó." → rồi nêu thiết kế dự định |

> Câu cuối là câu nguy hiểm nhất. Cô mở code ra là biết ngay — mất điểm **hai lần**: một lần vì chưa làm, một lần vì nói dối. Nói trước phần chưa xong luôn được tính là trung thực.
