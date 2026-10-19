# 06 — Đọc code: tầng Entity

> Đây là tầng **bạn đã viết xong thật**. Đọc kỹ file này trước — khi cô mở IDE,
> phần chắc chắn được xem đầu tiên là model.

Sáu entity + năm enum. Mỗi mục: **field nào → vì sao → cô có thể hỏi gì**.

---

## 0. Ba quyết định chung, áp cho cả sáu file

Nắm ba điều này trước, vì cô sẽ hỏi chúng ở *bất kỳ* entity nào.

### 0.1 `implements Serializable` + `serialVersionUID`

Cả sáu entity đều có. **Vì sao:** object đặt vào `HttpSession` phải serialize được — Tomcat có thể ghi session ra đĩa khi restart để khôi phục sau. `serialVersionUID` ghim phiên bản class: nếu bạn đổi cấu trúc entity mà session cũ còn trên đĩa, Tomcat biết là không tương thích và bỏ session đó thay vì đọc ra object hỏng.

> **Nếu cô hỏi "sao không dùng `java.time.LocalDateTime` mà dùng `java.util.Date`?"**
> Trả lời thẳng: `java.util.Date` là kiểu **cũ**, `LocalDateTime` là kiểu hiện đại nên dùng. Em giữ `Date` vì JPA map `Date` mặc định sang `TIMESTAMP` không cần cấu hình gì thêm, còn `LocalDateTime` cần `@Temporal`/converter tuỳ version. **Đây là chỗ em chọn theo sự tiện lợi của JPA, không phải vì `Date` tốt hơn.** Nếu làm lại em sẽ dùng `LocalDateTime`.

### 0.2 Không có annotation JPA — và đây là việc còn lại

Cả sáu file hiện là **POJO thuần**: có field, có getter/setter, `implements Serializable`, nhưng **không có `@Entity`, `@Id`, `@Column`, `@ManyToOne`**. Nghĩa là Hibernate chưa biết chúng là gì.

> **Nói thẳng khi demo:** *"Model em viết xong về mặt field và quan hệ; phần annotation JPA em đang map nốt."* Đừng để cô tự phát hiện.

Chi tiết cần map ở [05](05-checklist-truoc-buoi-cham.md) mục B, dòng 2.

### 0.3 Constructor rỗng `public Order() {}`

**Vì sao bắt buộc:** JPA/Hibernate tạo instance bằng reflection qua constructor không tham số. Thiếu nó thì Hibernate ném lỗi lúc khởi động. Cả sáu file đều có — đây là chi tiết dễ bị coi là "code thừa" nhưng **không** thừa.

---

## 1. `Order.java` — 101 dòng, entity trung tâm

### Field

| Field | Kiểu | Vì sao |
|---|---|---|
| `id` | `Long` | khoá chính |
| `buyer` | `User` | **tên là `buyer`, không phải `customer`** — vì kênh MARKETPLACE người mua cũng có thể là người bán ở đơn khác; `buyer` mô tả đúng vai trò *trong đơn này* |
| `orderType` | `OrderType` | cột phân biệt hai kênh — **điểm thiết kế trung tâm** |
| `status` | `OrderStatus` | `PENDING/CONFIRMED/COMPLETED/CANCELLED` |
| `createdAt` / `updatedAt` | `Date` | vết thời gian; `updatedAt` để biết đơn đứng im bao lâu |
| `items` | `List<OrderItem>` | khởi tạo sẵn `new ArrayList<>()` — **không bao giờ null**, nên vòng lặp trong `getTotalAmount()` không cần kiểm tra null |

**Không có** `totalAmount`. **Không có** `payment`. Cả hai đều là chủ ý — xem mục 1.2 và 1.3.

### 1.1 `getTotalAmount()` — dòng 37–44

```java
public BigDecimal getTotalAmount() {
    BigDecimal total = BigDecimal.ZERO;
    for (OrderItem item : items) {
        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        total = total.add(lineTotal);
    }
    return total;
}
```

Bốn điểm đáng nói:

1. **Không lưu thành cột.** SDD ghi rõ *"computed instead of storing a denormalized totalAmount field"*. Lý do: nếu lưu, sửa một dòng hàng mà quên cập nhật tổng là dữ liệu tự mâu thuẫn. Tính lại thì không bao giờ lệch.
2. **`item.getPrice()` chứ không phải `item.getProduct().getPrice()`.** Đây là **giá snapshot** — giá lúc đặt, không phải giá hiện tại. Đổi sang `getProduct()` là mọi đơn cũ tự đổi giá theo. **Đây là câu cô rất dễ hỏi — xem [04](04-cau-hoi-phan-bien.md) mục A3.**
3. **`BigDecimal.valueOf(quantity)`** — `quantity` là `int`, phải chuyển sang `BigDecimal` mới `multiply` được. Dùng `valueOf` (nhận `long`) chứ không `new BigDecimal(int)`.
4. **`total = total.add(...)`** — `BigDecimal` **bất biến**, `add()` trả về object mới chứ không sửa `total`. Viết `total.add(x);` mà không gán lại là bug im lặng. Đây là câu hỏi kinh điển về immutability.

> **Cô có thể hỏi: "hàm này gọi ở đâu?"** → `history.jsp:151` — `${order.totalAmount}`. EL gọi getter này tự động, không cần viết gì thêm trong JSP.

> ⚠️ **Điểm yếu thật của hàm này:** nó duyệt `items`. Nếu `items` là `LAZY` và hàm được gọi **ngoài transaction** (ví dụ servlet đã đóng `EntityManager` rồi mới forward sang JSP), Hibernate ném `LazyInitializationException`. Cách chặn: `JOIN FETCH items` trong query lịch sử đơn. Xem [04](04-cau-hoi-phan-bien.md) mục C3. **Chủ động nói điểm này trước — nó cho thấy bạn hiểu tầng, không chỉ viết được getter.**

### 1.2 Không có `payment` — chủ ý

Quan hệ một-đơn-tối-đa-một-thanh-toán được biểu diễn bằng **unique index trên `Payment.orderId`**, tức khoá ngoại nằm ở phía `Payment`. Nếu để `Order.paymentId` thì không có gì chặn hai `Payment` cùng trỏ vào một `Order`.

**Hệ quả ở tầng view:** JSP không viết được `${order.payment}` → servlet phải tra `Map<orderId, Payment>` rồi truyền vào. Đó là lý do có dòng `history.jsp:155`:

```jsp
<c:set var="payment" value="${payments[order.id]}" />
```

### 1.3 Không có `shippingAddress`

`checkout.jsp` có textarea địa chỉ giao hàng, nhưng giá trị đó là **form-only**, không phải field của `Order` — SDD 13.7 không có trường này, và `User` chỉ có một trường `location` dạng chuỗi tự do. Comment đầu `checkout.jsp` ghi rõ điều này (dòng 9–10).

> **Nếu cô hỏi "địa chỉ giao hàng lưu ở đâu?"** → *"Em không lưu. SDD 13.7 không có trường địa chỉ trên `Order`, và `User` chỉ có `location` dạng chuỗi. Em giữ ô nhập để đúng luồng giao diện nhưng giá trị không vào CSDL. Nếu cần lưu thật thì phải thêm trường vào `Order` — đó là thay đổi schema, em không tự làm ngoài SDD."*

---

## 2. `OrderItem.java` — 73 dòng

### Field

| Field | Vì sao |
|---|---|
| `order` | cha |
| `listing` | nguồn hàng kênh MARKETPLACE |
| `product` | nguồn hàng kênh NEW_DEVICE |
| `price` | **snapshot** — bất biến sau khi tạo |
| `quantity` | `int`, khởi tạo `= 1` |

### 2.1 Hai cột nullable, loại trừ nhau

Một dòng hàng trỏ **hoặc** `listing` **hoặc** `product`, không bao giờ cả hai, không bao giờ không có cái nào.

**Vì sao không gộp thành một trường `itemId` chung:** gộp thì mất khoá ngoại thật, mất tính toàn vẹn tham chiếu, và phải tự kiểm tra thủ công ở tầng service. Đây là đánh đổi có ý thức: chấp nhận hai cột nullable để giữ FK.

> ⚠️ **Điểm yếu thật:** ràng buộc "loại trừ nhau" **chưa được cưỡng chế ở đâu cả**. Không có CHECK constraint trong DB, không có validation ở constructor. Hiện tại nó chỉ là quy ước. Cách làm đúng: `CHECK ((listing_id IS NULL) <> (product_id IS NULL))` ở tầng DB.
>
> **Nếu cô hỏi:** *"Ràng buộc đó em chưa cưỡng chế ở DB, hiện là quy ước ở tầng service. Cách đúng là thêm CHECK constraint — em chưa làm."* Đừng nhận là đã chặn.

### 2.2 `quantity = 1` — hằng số chứ không phải ràng buộc

```java
private int quantity = 1;
```

Có **setter** `setQuantity(int)`. Nghĩa là giá trị 1 chỉ là **mặc định**, không phải ràng buộc — ai gọi setter vẫn đổi được.

**Vì sao vẫn để setter:** JPA cần nó để gán giá trị đọc từ DB. Bỏ setter thì Hibernate không map được.

> **Cô rất dễ hỏi: "vậy sao gọi là luôn bằng 1?"** → Trả lời đúng: *"BR-45 nói `quantity` luôn bằng 1. Ở tầng Java em để mặc định 1, nhưng ràng buộc thật phải nằm ở DB bằng `CHECK (quantity = 1)`. Hiện em chưa thêm. Về nghiệp vụ thì không có đường nào trong giao diện đặt được số khác 1 — form không có ô số lượng."*

**Chi tiết đáng nói:** bản mockup **có** stepper chọn số lượng 1..5, và bạn đã **bỏ nó**. Comment đầu `checkout.jsp` ghi lý do. Đây là ví dụ tốt về "bám SDD chứ không bám mockup".

---

## 3. `Payment.java` — 95 dòng

| Field | Ghi chú |
|---|---|
| `order` | quan hệ ngược về `Order` |
| `method` | `PaymentMethod` — **nullable** (COD → `null`) |
| `status` | `PaymentStatus` = `PENDING/PAID/FAILED` |
| `amount` | `BigDecimal` — **sao chép từ `Order.getTotalAmount()` lúc tạo** |
| `transactionCode` | mã để khách ghi vào nội dung chuyển khoản |
| `createdAt` / `paidAt` | `paidAt` null cho tới khi Admin xác nhận |

### 3.1 `method` nullable — chỗ bạn phải tự quyết

SDD §12.4 ghi `method ∈ {QR, MOMO, BANK}` — **ba** giá trị. Form của bạn có **bốn** lựa chọn vì có COD. Cách xử lý: **không** thêm `COD` vào enum, mà để `method = null`.

**Vì sao không thêm `COD` vào enum:** nếu thêm thì mọi chỗ xử lý thanh toán đều phải rẽ nhánh "nếu là COD thì bỏ qua" — làm hỏng ý nghĩa của enum. Để `null` rồi rẽ nhánh **một lần** ở tầng view thì sạch hơn.

→ [checkout.jsp:116](../../src/main/webapp/WEB-INF/views/order/checkout.jsp#L116) (radio `value=""`) và [payment.jsp:76](../../src/main/webapp/WEB-INF/views/order/payment.jsp#L76) (`empty payment.method` → "Nhận hàng trả tiền (COD)").

> ⚠️ **Nói câu này TRƯỚC khi cô hỏi.** SDD 3 giá trị mà form 4 lựa chọn — để cô tự phát hiện là bạn ở thế bị động. Chi tiết ở [04](04-cau-hoi-phan-bien.md) mục B4.

### 3.2 `amount` là bản sao, không phải nguồn sự thật

`Order.getTotalAmount()` mới là nguồn sự thật. `Payment.amount` là **bản chụp** tại thời điểm tạo, để đối soát với sao kê ngân hàng.

> **Nếu cô hỏi "vậy có mâu thuẫn không, hai chỗ cùng lưu tiền?"** → Trả lời: *"Về nguyên tắc thì `amount` là dữ liệu dẫn xuất nên lẽ ra không nên lưu. Nhưng giao dịch ngân hàng là bằng chứng độc lập — số tiền khách thực chuyển phải được ghi lại tại thời điểm đó, không tính lại từ đơn. Nếu sau này đơn bị sửa, `Payment.amount` vẫn phải giữ số cũ để đối soát."* Đây là lý do chính đáng để phá nguyên tắc "không lưu dữ liệu dẫn xuất" — biết nói ra là ăn điểm.

### 3.3 `PaymentStatus` chỉ có ba giá trị — **cố ý không có `REFUNDED`**

Vì MVP **không có hoàn tiền**. Hoàn tiền là quy trình kế toán ngoài hệ thống. Khi huỷ đơn mà `Payment` đã `PAID`, hệ thống **giữ nguyên** `PAID` — không có trạng thái nào để chuyển sang.

→ [BR-46](../../reference/_sdd.txt). Chi tiết ở [04](04-cau-hoi-phan-bien.md) mục B2.

---

## 4. `Cart.java` — 80 dòng

| Field | Ghi chú |
|---|---|
| `customer` | **unique** — mỗi Customer đúng một Cart |
| `createdAt` / `updatedAt` | |
| `items` | `List<CartItem>`, khởi tạo sẵn |

### 4.1 `getSubtotal()` — dòng 31–39

Cùng khuôn với `Order.getTotalAmount()`, **nhưng khác một chỗ quan trọng**:

```java
BigDecimal lineTotal = item.getProduct().getPrice()   // <-- Product, không phải snapshot
        .multiply(BigDecimal.valueOf(item.getQuantity()));
```

Giỏ hàng đọc **giá hiện tại** từ `Product` — đúng, vì giỏ chưa phải cam kết mua. Giá chỉ được "chốt" (snapshot) vào lúc checkout, khi tạo `OrderItem`.

> **Đây là câu hỏi hay và cô rất có thể hỏi:** *"Sao giỏ hàng đọc giá động mà đơn hàng lại lưu giá?"*
>
> Trả lời: *"Vì hai thời điểm khác nhau về nghiệp vụ. Trong giỏ, khách chưa cam kết gì — Admin đổi giá thì khách phải thấy giá mới. Lúc bấm đặt hàng mới là lúc chốt: `OrderItem.price` sao chép giá tại đúng thời điểm đó và bất biến từ đó về sau. Nếu giỏ cũng snapshot thì khách sẽ thấy giá cũ và bị tính giá sai."*

**Comment ở dòng 29** giải thích vì sao cần hàm này thay vì tính trong JSP: *"EL không duyệt tính được tổng nên JSP cần hàm này."* Chính xác — EL không có vòng lặp kiểu `sum`, nên tổng phải là một getter.

### 4.2 Cart **không** giữ chỗ Product — BR-45

Bỏ máy vào giỏ **không** làm `Product.status` đổi. Không có cột `reserved` nào. Đây là lý do kênh của bạn không cần lock — xem [01](01-tong-quan-module.md) mục "Hai điểm ăn điểm".

---

## 5. `CartItem.java` — 63 dòng

| Field | Ghi chú |
|---|---|
| `cart` | cha |
| `product` | **không nullable** — giỏ chỉ chứa máy mới |
| `quantity` | `int = 1` — cùng vấn đề như `OrderItem` |
| `addedAt` | để sắp xếp giỏ theo thứ tự thêm |

**Ràng buộc `(cartId, productId)` unique:** chặn thêm cùng một máy hai lần vào giỏ. Đây là ràng buộc ở tầng DB, chưa map annotation.

> **Nếu cô hỏi "thêm trùng thì sao?"** → *"Ở DB có unique `(cartId, productId)`. Ở tầng service, khi thêm một Product đã có trong giỏ thì em không tạo dòng mới — vì `quantity` luôn bằng 1 nên tăng số lượng là vô nghĩa, em chỉ báo 'đã có trong giỏ'."*

**Vì sao `CartItem` không có `listing`:** giỏ hàng là tính năng của kênh NEW_DEVICE. Kênh MARKETPLACE mua trực tiếp từ tin đăng, không qua giỏ.

---

## 6. `Product.java` — 81 dòng

Đây là entity **không thuộc phạm vi bạn** — thuộc module Product của Phước Anh. Bạn chỉ khai báo **tối thiểu** để `Cart`/`CartItem` và view chạy được.

| Field | Ghi chú |
|---|---|
| `id`, `name`, `brand`, `price` | |
| `status` | `ProductStatus` — `AVAILABLE/OUT_OF_STOCK/DISCONTINUED` |
| `storeName` | **display-only**, FR-BUY-03; MVP không có entity `Store` |

**Comment `ponytail:` ở dòng 15–17** ghi rõ: chỉ khai báo field mà `Cart`/`CartItem` và view đang dùng; các FK (`device`, `deviceModel`, `admin`) và `avgRating` thuộc phạm vi Phước Anh, thêm annotation khi bạn đó map entity.

> **Nếu cô hỏi "sao `Product` sơ sài vậy?"** → *"Vì `Product` thuộc module của bạn khác trong nhóm. Em chỉ khai báo phần em cần dùng — `price` và `status` cho giỏ hàng và checkout. Em cố tình không viết thêm để tránh hai người sửa cùng một file rồi xung đột."*
>
> **Nói câu này được điểm cộng về làm việc nhóm** — biết ranh giới, không giẫm chân nhau.

---

## 7. Năm enum — và hai enum rỗng

### Enum của bạn (đã xong)

| Enum | Giá trị | Ràng buộc |
|---|---|---|
| `OrderType` | `MARKETPLACE`, `NEW_DEVICE` | [BR-43](../../reference/_sdd.txt) |
| `OrderStatus` | `PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED` | [BR-46](../../reference/_sdd.txt) |
| `PaymentMethod` | `QR`, `MOMO`, `BANK` | [BR-44](../../reference/_sdd.txt) — **chỉ 3**, COD để `null` |
| `PaymentStatus` | `PENDING`, `PAID`, `FAILED` | [BR-44](../../reference/_sdd.txt) — **không có `REFUNDED`** |
| `ProductStatus` | `AVAILABLE`, `OUT_OF_STOCK`, `DISCONTINUED` | SDD 13.5, dùng ở [BR-45](../../reference/_sdd.txt) |

**Vì sao enum chứ không phải `String`:** nếu `status` là `String` thì `"PENDING"`, `"pending"`, `"Pendng"` đều lọt vào DB và không ai chặn. Enum chặn ngay lúc compile. Ở tầng DB, `@Enumerated(EnumType.STRING)` map thành `VARCHAR` + có thể thêm CHECK constraint.

> ⚠️ **Chỗ dễ bị hỏi:** *"Sao không dùng `EnumType.ORDINAL`?"* → Vì `ORDINAL` lưu **số thứ tự**. Thêm một giá trị vào giữa enum là mọi dòng cũ trong DB đọc sai nghĩa — `PENDING` thành `CONFIRMED` mà không ai biết. `STRING` lưu tên, an toàn khi enum tiến hoá. **Đây là câu trả lời ăn điểm, nên nhớ.**

### Enum rỗng — không phải của bạn

`ListingCondition`, `ListingStatus`, `SpecDataType` — **rỗng**, thuộc module marketplace/product. Đừng nhận là của mình, đừng sửa.

---

## 8. Ba câu cô chắc chắn hỏi về tầng entity

| Câu hỏi | Trả lời ngắn |
|---|---|
| "Sao `Order` không có `totalAmount`?" | Dữ liệu dẫn xuất — lưu ra là tạo cơ hội mâu thuẫn. SDD ghi rõ *computed instead of storing a denormalized field*. → [04](04-cau-hoi-phan-bien.md) A5 |
| "Sao `OrderItem` lưu `price` mà `CartItem` không?" | Hai thời điểm nghiệp vụ khác nhau: giỏ chưa cam kết (giá động), đơn đã cam kết (snapshot bất biến). → mục 4.1 ở trên |
| "Ràng buộc `quantity = 1` chặn ở đâu?" | Mặc định ở Java, **chưa** có CHECK constraint ở DB. Nói thẳng là chưa làm. → mục 2.2 |

---

## 9. Tự kiểm tra — trả lời được hết là đủ tự tin

1. `getTotalAmount()` gọi `item.getPrice()` chứ không `item.getProduct().getPrice()` — vì sao?
2. Vì sao `getSubtotal()` của `Cart` thì **ngược lại**, gọi `item.getProduct().getPrice()`?
3. `total = total.add(lineTotal)` — bỏ phép gán `total =` thì chuyện gì xảy ra?
4. Vì sao `Payment` không có `REFUNDED`?
5. Vì sao `Order` không có field `payment`? Hệ quả ở JSP là gì?
6. `serialVersionUID` để làm gì?
7. Vì sao `EnumType.STRING` chứ không `ORDINAL`?
8. Ràng buộc "`OrderItem` trỏ hoặc `listing` hoặc `product`" hiện được cưỡng chế ở đâu?

> Câu 3 và câu 8 là hai câu **bạn phải trả lời được mà không cần mở code**. Câu 8 đặc biệt — nó là chỗ bạn nói thẳng "em chưa cưỡng chế ở DB", và nói thẳng như vậy được tính là hiểu bài.
