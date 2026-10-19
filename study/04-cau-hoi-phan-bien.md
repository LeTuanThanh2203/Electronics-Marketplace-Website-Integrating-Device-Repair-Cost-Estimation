# 04 — Câu hỏi phản biện & câu trả lời mẫu

> Cách trả lời ăn điểm: **nêu lý do trước, nêu code sau**. Cô không cần nghe
> bạn đọc lại code, cô cần biết bạn *hiểu vì sao*.

---

## A. Câu hỏi về thiết kế dữ liệu

### A1. "Vì sao hai kênh bán lại dùng chung một bảng `Order`? Sao không tách ra?"

Vì mọi thứ **giống nhau**: cùng người mua, cùng danh sách hàng, cùng vòng đời trạng thái `PENDING → CONFIRMED → COMPLETED/CANCELLED`, cùng cần lịch sử đơn. Chỉ **hai** thứ khác: nguồn hàng (tin đăng máy cũ vs máy mới trong kho) và có thanh toán hay không.

Tách bảng sẽ phải nhân đôi toàn bộ query lịch sử, nhân đôi phân trang, nhân đôi màn hình. Nên em giữ một bảng, thêm cột `orderType`, và mọi quy tắc riêng của kênh marketplace — giữ chỗ tin đăng, giới hạn số đơn — đều **chỉ áp dụng khi `orderType = MARKETPLACE`**.

→ Mở [history.jsp:99-102](../../src/main/webapp/WEB-INF/views/order/history.jsp#L99-L102) chỉ chỗ rẽ nhánh.

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

**Hệ quả ở tầng view:** JSP không viết được `${order.payment}`, nên servlet phải tra một `Map<orderId, Payment>` rồi truyền vào — [history.jsp:155](../../src/main/webapp/WEB-INF/views/order/history.jsp#L155).

---

### A5. "Vì sao tổng tiền không lưu thành cột?"

Vì đó là **dữ liệu dẫn xuất**. Lưu nó ra là tạo cơ hội cho dữ liệu mâu thuẫn: sửa một dòng hàng mà quên cập nhật tổng, hoặc ngược lại.

Tính lại mỗi lần đọc thì không bao giờ lệch, và với quy mô một giỏ hàng vài món thì chi phí không đáng kể. Đây là đánh đổi có ý thức: đổi chút CPU lấy tính đúng đắn.

SDD ghi rõ điều này ở phần đặc tả `Order`: `getTotalAmount()` = `SUM(OrderItem.price × quantity)`, "computed instead of storing a denormalized totalAmount field".

→ [Cart.java:31](../../src/main/java/com/electronics/marketplace/entity/Cart.java#L31)

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

⚠️ **Câu này cô rất dễ hỏi** — vì SDD §12.4 ghi `method ∈ {QR, MOMO, BANK}`, tức chỉ **3** giá trị, mà form của em có **4** lựa chọn. Trả lời thẳng:

> "COD là lựa chọn em lấy từ bản mockup, **không có trong SDD**. Em giữ lại vì đó là cách mua phổ biến nhất ở Việt Nam, nhưng em **không** thêm `COD` vào enum `PaymentMethod` — enum giữ đúng 3 giá trị như SDD quy định. Chọn COD thì form gửi `value=""`, servlet hiểu là `null`, nghĩa là **không tạo giao dịch trực tuyến nào**."

Vì sao **không** thêm `COD` vào enum: nếu thêm thì mọi chỗ xử lý thanh toán đều phải rẽ nhánh "nếu là COD thì bỏ qua", làm hỏng ý nghĩa của enum. Để `null` rồi rẽ nhánh một lần ở tầng view thì sạch hơn.

→ [checkout.jsp:116](../../src/main/webapp/WEB-INF/views/order/checkout.jsp#L116) (value rỗng) và [payment.jsp:76](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L76) (hiển thị COD).

> **Nếu cô hỏi "vậy đơn COD có Payment không?"** — Trả lời: theo SDD thì mọi đơn `NEW_DEVICE` đều sinh Payment ([BR-44](../../reference/_sdd.txt)), nhưng đơn COD không có giao dịch nào để xác nhận. Đây là **khoảng trống trong SDD**, không phải lỗi code. Em chọn hướng: vẫn tạo `Payment` ở `PENDING` nhưng không hiện thông tin chuyển khoản — Admin tự chuyển thẳng `PAID` khi giao hàng. **Nói rõ đây là chỗ em phải tự quyết vì SDD chưa chốt.**

---

### B5. "Sao không tích hợp cổng thanh toán thật?"

Vì SDD chốt là **không** tích hợp trong MVP. Hệ thống tự sinh `Payment` ở `PENDING` kèm mã giao dịch, khách chuyển khoản thủ công, Admin đối soát và xác nhận tay.

Lý do: tích hợp cổng thật cần tài khoản doanh nghiệp, webhook công khai, xử lý chữ ký — vượt phạm vi môn học. Nhưng mô hình dữ liệu em làm **đã sẵn sàng** cho việc đó: chỉ cần thêm một endpoint nhận callback và đổi `Payment.status`, không phải sửa schema.

→ [BR-44](../../reference/_sdd.txt)

---

### B6. "Đơn khi nào được coi là hoàn thành?"

Chỉ khi **đồng thời** đơn ở `CONFIRMED` **và** thanh toán ở `PAID`. Đây là BR-46, và em cài đặt nó ở hai tầng: tầng view ẩn nút nếu chưa đủ điều kiện, tầng service từ chối nếu client cố gửi request.

→ [history.jsp:178](../../src/main/webapp/WEB-INF/views/order/history.jsp#L178)

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

→ [cart.jsp:145-177](../../src/main/webapp/WEB-INF/views/cart/cart.jsp#L145-L177)

---

### C6. "Vì sao `web.xml` rỗng mà app vẫn chạy được?"

Vì từ Servlet 3.0 trở đi có thể khai báo servlet và filter bằng **annotation** ngay trên class (`@WebServlet`, `@WebFilter`) thay vì trong `web.xml`.

> ⚠️ **Đây là chỗ em phải trả lời cẩn thận** — xem [05-checklist](05-checklist-truoc-buoi-cham.md). Hiện tại các servlet còn là class rỗng **chưa có annotation**, nên câu trả lời đúng lúc này là: *"Em dùng annotation, phần đó em đang hoàn thiện."* **Đừng nói là đã xong.**

---

## D. Câu hỏi "hỏi xoáy" — chuẩn bị tinh thần

### D1. "Nếu cô bấm F5 ở trang thanh toán thì sao?"

Trang thanh toán là `GET`, chỉ đọc dữ liệu, nên F5 vô hại.

Nhưng **form đặt hàng** là `POST`. Nếu khách F5 ở trang kết quả thì trình duyệt hỏi "gửi lại form?". Cách xử lý chuẩn là **POST-redirect-GET**: sau khi tạo đơn xong, servlet không forward sang JSP mà `sendRedirect` sang `/payment?action=view&orderId=...`. Như vậy F5 chỉ load lại trang GET, không tạo đơn thứ hai.

### D2. "Nếu khách sửa `orderId` trong form huỷ đơn thành đơn của người khác?"

Đây là lỗ hổng **IDOR** kinh điển. Phòng thủ **bắt buộc** ở tầng service: load đơn theo id, rồi kiểm tra `order.buyer.id == currentUser.id`, không khớp thì trả 403. Không bao giờ tin `orderId` từ client.

Cùng cách áp dụng cho `/payment?action=view&orderId=` — nếu không kiểm tra thì bất kỳ ai cũng xem được thông tin thanh toán của người khác.

> Đây là thứ em phải nhớ **viết** khi làm tầng service, không chỉ nhớ để trả lời.

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

Có một test cho `JpaConfig` — ghim hai hành vi về placeholder và override map. Em viết vì đó là chỗ em từng hiểu sai và sửa mất thời gian.

> ⚠️ **Trả lời trung thực:** phần Cart/Order/Payment chưa có unit test vì tầng service chưa viết xong. Nếu cô hỏi tiếp, nói thẳng: *"Em dự định viết test cho `OrderService.checkout()` và luồng huỷ đơn — đó là hai chỗ logic phức tạp nhất."*

---

## E. Bốn câu tuyệt đối không được nói

| ❌ Đừng nói | ✅ Nói thay |
|---|---|
| "Cái này em copy trên mạng" | "Em tham khảo cách làm chuẩn rồi áp vào bài toán của mình" |
| "Em không biết" (rồi im) | "Em chưa xử lý trường hợp đó. Em nghĩ hướng đúng sẽ là..." |
| "Cái đó của bạn khác trong nhóm" | "Phần đó ngoài phạm vi em phụ trách, nhưng theo em hiểu thì..." |
| "Cái này em làm rồi" (khi chưa làm) | "Em chưa xong phần đó." → rồi nêu thiết kế dự định |

> Câu cuối là câu nguy hiểm nhất. Cô mở code ra là biết ngay — mất điểm **hai lần**: một lần vì chưa làm, một lần vì nói dối. Nói trước phần chưa xong luôn được tính là trung thực.
