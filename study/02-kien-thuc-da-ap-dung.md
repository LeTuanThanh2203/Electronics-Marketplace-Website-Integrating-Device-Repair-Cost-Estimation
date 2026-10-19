# 02 — Kiến thức đã học & đã thực hành được

Mỗi mục: **kiến thức → đã dùng ở đâu trong code → nói gì khi demo**.
Đường dẫn tính từ gốc project.

---

## 1. Mô hình MVC trong Java Web

**Kiến thức:** `Servlet` = Controller (nhận request, điều hướng), `JSP` = View (chỉ hiển thị), `JavaBean`/`Entity` = Model. Không viết logic trong JSP.

**Đã thực hành:** Mỗi màn hình là một cặp servlet + JSP. Servlet nhận `action`, chuẩn bị model, `forward` sang JSP trong `WEB-INF/views/`.

**Nói khi demo:**
> "Em không viết một dòng Java nào trong JSP. Toàn bộ logic đẩy sang Servlet và Service; JSP chỉ đọc model bằng EL/JSTL. Ví dụ dòng 111 của `history.jsp` chỉ là `<c:forEach var="item" items="${order.items}">`."

---

## 2. JSP + JSTL + EL — tuyệt đối không Scriptlet

**Kiến thức:** `<% %>` (Scriptlet) trộn Java vào HTML, khó test, dễ lỗi. Thay bằng EL `${}` và JSTL `<c:forEach>`, `<c:if>`, `<c:choose>`, `<fmt:formatNumber>`.

**Đã thực hành** — 5 màn hình JSP, không một Scriptlet nào:

| Kỹ thuật | Vị trí |
|----------|--------|
| `<c:choose>/<c:when>/<c:otherwise>` rẽ 3 nhánh trạng thái | [payment.jsp:104-156](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L104-L156) |
| `<c:forEach>` duyệt danh sách | [history.jsp:111](../../src/main/webapp/WEB-INF/views/order/history.jsp#L111) |
| `<fmt:formatNumber>` + `currencySymbol="₫"` | [cart.jsp:83](../../src/main/webapp/WEB-INF/views/cart/cart.jsp#L83) |
| `<fmt:formatDate>` định dạng ngày VN | [history.jsp:105](../../src/main/webapp/WEB-INF/views/order/history.jsp#L105) |
| `<fmt:setLocale value="vi_VN">` | [cart.jsp:13](../../src/main/webapp/WEB-INF/views/cart/cart.jsp#L13) |
| `<c:set>` tính nhãn/badge từ enum | [history.jsp:76-92](../../src/main/webapp/WEB-INF/views/order/history.jsp#L76-L92) |
| `<c:if test="${empty cart.items}">` trạng thái rỗng | [cart.jsp:32](../../src/main/webapp/WEB-INF/views/cart/cart.jsp#L32) |

**Điểm hay để khoe** — `history.jsp:76-92`, đổi enum thành nhãn tiếng Việt mà không cần `if/else` Java:

```jsp
<c:set var="statusLabel">
    <c:choose>
        <c:when test="${order.status == 'PENDING'}">Chờ xử lý</c:when>
        <c:when test="${order.status == 'CONFIRMED'}">Đã xác nhận</c:when>
        ...
```

---

## 3. Static include — tái sử dụng layout (DRY)

**Kiến thức:** `<%@ include file="..." %>` chèn nội dung file **lúc dịch**, dùng chung được biến. Khác `jsp:include` (chèn lúc chạy).

**Đã thực hành:** [_header.jspf](../../src/main/webapp/WEB-INF/views/order/_header.jspf) + [_footer.jsp](../../src/main/webapp/WEB-INF/views/order/_footer.jsp) cho cả 5 màn. Header tự tính `ctx` (context path) và `cartCount`.

```jsp
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="cartCount" value="${fn:length(sessionScope.cart.items)}" />
```

**Nói khi demo:**
> "Header/footer dùng chung nên đổi logo hay menu chỉ sửa một file. `ctx` lấy từ `pageContext` nên deploy tên project là gì link vẫn đúng, không hardcode đường dẫn."

---

## 4. JPA / Hibernate — ORM

**Kiến thức:** Ánh xạ class Java ↔ bảng CSDL, không viết SQL thủ công. Field-based mapping (annotation trên field, không trên getter).

**Đã thực hành:**
- Cấu hình `persistence.xml` với HikariCP pool, batch size 50 — [persistence.xml](../../src/main/resources/META-INF/persistence.xml)
- **Nạp mật khẩu từ biến môi trường** thay vì hardcode: [JpaConfig.java](../../src/main/java/com/electronics/marketplace/config/JpaConfig.java)

**Câu chuyện đáng kể nhất** (bạn tự làm, có commit riêng `8364a55`):
> "Em không commit mật khẩu DB vào git. Ban đầu thử `${env.DB_URL}` trong `persistence.xml` thì Hibernate 6.4.4 báo lỗi `HHH10001004: No JDBC URL specified` — hoá ra nó chỉ thay thế `${system.property}`, không hỗ trợ `${env.}`. Nên em viết `JpaConfig` đọc `System.getenv()` rồi truyền vào qua override map của `Persistence.createEntityManagerFactory`. Override map luôn thắng giá trị trong file."

Kèm kỹ thuật **Singleton + holder class** — JVM đảm bảo chỉ khởi tạo một lần, không cần `synchronized`:
```java
private static class Holder {
    private static final EntityManagerFactory EMF = build();
}
```

Và **fail fast** — thiếu biến môi trường thì báo lỗi rõ ràng ngay, không để Hibernate báo lỗi khó hiểu.

---

## 5. Lazy Loading & chống N+1 Query

**Kiến thức:** `FetchType.LAZY` cho quan hệ collection — chỉ load khi gọi getter. `EAGER` gây N+1 query và tốn RAM. Bắt buộc theo Rule 2 của project.

**Đã thực hành:** quan hệ `Cart → CartItem`, `Order → OrderItem` đều là collection sẽ khai báo LAZY. Khi màn lịch sử cần `OrderItem`, phải dùng `JOIN FETCH` trong JPQL — đây là chỗ dễ mắc lỗi `LazyInitializationException` nhất.

> ⚠️ Chưa map annotation vì `Order`/`Cart` đang là POJO thuần. Xem [05-checklist](05-checklist-truoc-buoi-cham.md).

---

## 6. JavaBean chuẩn & Serializable

**Kiến thức:** Model phải có constructor rỗng, getter/setter đúng chuẩn, `implements Serializable` — để JSP/EL đọc được và để lưu vào Session.

**Đã thực hành:** cả 5 entity đều đúng chuẩn — ví dụ [Cart.java:14](../../src/main/java/com/electronics/marketplace/entity/Cart.java#L14) `public class Cart implements Serializable` với `serialVersionUID`.

---

## 7. Enum — thay cho "magic string"

**Kiến thức:** Trạng thái nên là enum, không phải chuỗi tự do. Sai chính tả bị compiler chặn ngay.

**Đã thực hành:** 4 enum tách riêng file — [OrderStatus](../../src/main/java/com/electronics/marketplace/entity/enums/OrderStatus.java), [OrderType](../../src/main/java/com/electronics/marketplace/entity/enums/OrderType.java), [PaymentMethod](../../src/main/java/com/electronics/marketplace/entity/enums/PaymentMethod.java), [PaymentStatus](../../src/main/java/com/electronics/marketplace/entity/enums/PaymentStatus.java).

**Nói khi demo:**
> "PaymentStatus chỉ có 3 giá trị `PENDING, PAID, FAILED` — cố tình **không có** `REFUNDED`, vì MVP không hoàn tiền. Đây là quyết định trong buổi review SDD v1.17."

---

## 8. BigDecimal cho tiền tệ — KHÔNG dùng double

**Kiến thức:** `double` là số dấu phẩy động nhị phân, không biểu diễn chính xác 0.1 → sai số tích luỹ. Tiền **bắt buộc** dùng `BigDecimal`.

**Đã thực hành:** mọi trường tiền là `BigDecimal` — `Product.price`, `CartItem` → `Cart.getSubtotal()`, `OrderItem.price`, `Order.getTotalAmount()`, `Payment.amount`.

```java
// Cart.java:31
public BigDecimal getSubtotal() {
    BigDecimal total = BigDecimal.ZERO;
    for (CartItem item : items) {
        BigDecimal lineTotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        total = total.add(lineTotal);
    }
    return total;
}
```

**Nói khi demo:**
> "Nhân rồi cộng bằng `multiply`/`add`, không dùng `*` và `+`. Và tổng tiền **tính toán chứ không lưu cột `totalAmount`** — tránh dữ liệu denormalized bị lệch với các dòng chi tiết. Đây là chủ ý thiết kế trong SDD."

---

## 9. AJAX / Fetch API + JSON (Rule 4)

**Kiến thức:** Thao tác chỉ đổi một phần giao diện thì gọi server bằng Fetch và cập nhật DOM, **không reload cả trang**.

**Đã thực hành:** nút "Xoá" trong giỏ — [cart.jsp:149-177](../../src/main/webapp/WEB-INF/views/cart/cart.jsp#L149-L177)

```js
fetch(btn.dataset.url, {method: 'POST', headers: {'Accept': 'application/json'}})
    .then(res => res.json())
    .then(function (data) {
        var row = document.querySelector('.cart-item[data-item-id="' + btn.dataset.itemId + '"]');
        if (row) row.remove();
        subtotalEl.textContent = vnd.format(data.subtotal);
        if (data.count === 0) window.location.reload();
    })
```

Hợp đồng JSON của `POST /cart?action=remove`:
```json
{ "ok": true, "subtotal": 26390000, "count": 2 }
```

**Nói khi demo:**
> "Xoá một dòng chỉ trả về JSON gồm `subtotal` mới và `count`, JS tự cập nhật lại con số tổng — không load lại trang. `count === 0` thì mới reload để hiện màn hình giỏ trống."

**Quyết định thiết kế đáng nói:** nút xoá là `<button>` chứ không phải `<a>`. Cố ý — không có JS thì không xoá được, vì đây là hành động phá huỷ, không nên âm thầm fallback.

---

## 10. `Intl.NumberFormat` — định dạng tiền phía client

**Kiến thức:** API có sẵn của trình duyệt, không cần thư viện.

```js
var vnd = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
```

Dùng ở [cart.jsp:151](../../src/main/webapp/WEB-INF/views/cart/cart.jsp#L151). Đảm bảo số tiền JS render ra **giống hệt** số tiền JSTL render ra — cùng locale `vi_VN`.

---

## 11. HTML5 form validation & input type

**Kiến thức:** Dùng thuộc tính HTML thay vì viết JS kiểm tra.

**Đã thực hành:** [checkout.jsp](../../src/main/webapp/WEB-INF/views/order/checkout.jsp)
- `type="tel"` cho số điện thoại → bàn phím số trên mobile
- `type="email"` → bàn phím có sẵn ký tự `@`
- `required` → trình duyệt tự chặn submit rỗng
- `maxlength="300"` cho ghi chú
- `novalidate` trên `<form>` — **cố ý tắt** để server tự kiểm tra (server-side validation vẫn là chốt chặn thật)

---

## 12. Xác nhận trước hành động phá huỷ (UX an toàn)

**Kiến thức:** Hành động không hoàn tác được phải hỏi lại.

**Đã thực hành:** huỷ đơn — [history.jsp:242-246](../../src/main/webapp/WEB-INF/views/order/history.jsp#L242-L246)

```js
document.querySelectorAll('form.js-confirm').forEach(function (form) {
    form.addEventListener('submit', function (e) {
        if (!window.confirm(form.dataset.confirm)) e.preventDefault();
    });
});
```

Dùng `data-confirm` để mỗi form mang thông điệp riêng, không hardcode trong JS.

---

## 13. Bảo mật — không tin dữ liệu client

**Kiến thức:** Mọi thứ từ client đều có thể bị sửa. `orderId` gửi lên từ form huỷ đơn phải được server kiểm tra **có thuộc về user đang đăng nhập không**.

**Đã thực hành (thiết kế):** form huỷ đơn gửi `orderId` qua hidden input; servlet **phải** xác minh `order.buyer.id == currentUser.id` trước khi huỷ. Tương tự `payment?action=view&orderId=`.

> ⚠️ Đây là lỗ hổng IDOR kinh điển nếu quên. Phải có trong `OrderService`/`PaymentService` trước khi chấm.

---

## 14. Session — chỉ chứa thứ thiết yếu (Rule 3)

**Kiến thức:** Session nằm trong RAM server; nhồi danh sách lớn → `OutOfMemory`. Chỉ lưu `currentUser` và `cart`.

**Đã thực hành:** [_header.jspf:11-12](../../src/main/webapp/WEB-INF/views/order/_header.jspf#L11-L12) đọc đúng hai thứ đó:
```jsp
<c:set var="cartCount" value="${fn:length(sessionScope.cart.items)}" />
<c:set var="currentUser" value="${sessionScope.currentUser}" />
```

Danh sách đơn hàng thì **không** để ở session — nạp theo từng request, có phân trang.

---

## 15. Phân trang

**Kiến thức:** Không bao giờ load toàn bộ bảng. `LIMIT`/`OFFSET` + đếm tổng.

**Đã thực hành (view):** [history.jsp:191-200](../../src/main/webapp/WEB-INF/views/order/history.jsp#L191-L200) render dãy số trang từ `${page}` / `${totalPages}` do servlet truyền vào, và giữ nguyên bộ lọc `status` khi đổi trang.

---

## 16. Race condition & transaction (điểm nhấn của module)

**Kiến thức:** Hai request đồng thời cùng mua một máy → cả hai đều đọc thấy "còn hàng" → bán trùng.

**Quy định:** [BR-43](../../reference/_sdd.txt) + [BR-45](../../reference/_sdd.txt) — `Cart` **không** giữ chỗ `Product`; checkout một Cart hợp lệ tạo Order `NEW_DEVICE` với một OrderItem cho mỗi CartItem, tất cả trong **cùng một transaction**.

**Đã thực hành (thiết kế):** `OrderService.checkout()` mở `EntityManager` transaction, validate `Product.status == AVAILABLE` **bên trong** transaction, tạo Order + OrderItem + Payment, rollback toàn bộ nếu bất kỳ bước nào fail.

**Nói khi demo:**
> "Kênh của em không giữ chỗ sản phẩm nên rủi ro tranh mua thấp — nhưng em vẫn validate lại `Product.status` **bên trong** transaction, chứ không kiểm tra trước rồi mới tạo đơn. Kiểm tra trước là khe hở: giữa hai bước đó người khác mua mất."

> ⚠️ Cơ chế **lock dòng Buyer bằng `SELECT ... FOR UPDATE`** là của [BR-31/BR-32](../../reference/_sdd.txt) — rule **kênh MARKETPLACE**, thuộc phần Thành. Đừng nhận là mình làm.

---

## 17. Transaction boundary — huỷ đơn là thao tác 2 bảng

**Kiến thức:** Một hành động nghiệp vụ chạm nhiều bảng phải là một transaction.

**Đã thực hành (thiết kế):** [BR-46](../../reference/_sdd.txt) — khi Order NEW_DEVICE chuyển `CANCELLED`, **trong cùng transaction** phải chuyển `Payment` từ `PENDING → FAILED`. Nhưng nếu Payment đã `PAID` thì **giữ nguyên** (không hoàn tiền trong MVP).

**Nói khi demo:**
> "Đây là ví dụ rõ nhất cho việc vì sao cần transaction: nếu em update Order thành CANCELLED mà crash trước khi update Payment, dữ liệu sẽ mâu thuẫn — đơn đã huỷ nhưng vẫn báo chờ thanh toán."

---

## 18. `c:choose` rẽ nhánh theo enum trong view

**Đã thực hành:** [payment.jsp:36-41](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L36-L41) — tiêu đề trang đổi theo `PaymentStatus`, và [payment.jsp:104-156](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L104-L156) — khối nội dung đổi theo trạng thái (PENDING hiện thông tin chuyển khoản, PAID hiện xác nhận, còn lại hiện thất bại).

---

## 19. CSS: design token & `tabular-nums`

**Kiến thức:** Không hardcode màu/khoảng cách rải rác — khai báo biến CSS một chỗ.

**Đã thực hành:** [order.css:8-33](../../src/main/webapp/css/order.css#L8-L33) khai báo toàn bộ token trong `:root` (màu, bán kính, khoảng cách, font).

```css
.money, .mono {
    font-family: var(--font-mono);
    font-variant-numeric: tabular-nums;
}
```

**Nói khi demo:**
> "`tabular-nums` làm mọi chữ số rộng bằng nhau, nên cột giá thẳng hàng dọc, không bị nhảy khi số thay đổi. Bộ màu lấy từ file DESIGN.md của bản mockup, em chuyển từ Tailwind CDN sang CSS thuần để không cần build step và không phụ thuộc script ngoài."

---

## 20. Cắt bỏ phạm vi có lý do (scope discipline)

**Kiến thức:** Không implement thứ không có trong data model.

**Đã thực hành:** ghi rõ trong comment đầu file [checkout.jsp:4-10](../../src/main/webapp/WEB-INF/views/order/checkout.jsp#L4-L10) những gì **cố ý không làm**, kèm lý do:
- voucher/mã giảm giá → SDD không có entity `Voucher`
- phí vận chuyển → ngoài MVP
- select tỉnh/quận/phường → `User` chỉ có một trường `location` dạng chuỗi

**Nói khi demo:**
> "Bản mockup có stepper số lượng 1..5, nhưng em bỏ vì `CartItem.quantity` luôn bằng 1 — mỗi máy mới là một đơn vị duy nhất ([BR-45](../../reference/_sdd.txt)). Em ghi lại lý do ngay trong comment để người đọc sau không tưởng là em quên."
