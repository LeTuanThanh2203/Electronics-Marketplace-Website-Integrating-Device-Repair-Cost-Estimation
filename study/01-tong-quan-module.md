# 01 — Tổng quan module Nam

## Phạm vi trách nhiệm

**Giỏ hàng, Đặt hàng & Thanh toán** — kênh `NEW_DEVICE` (Customer mua máy mới từ Admin).

> Phân biệt quan trọng: hệ thống có **2 kênh bán** ([BR-43](../../reference/_sdd.txt))
> - `MARKETPLACE` — Customer ↔ Seller qua `Listing`, tiền mặt offline, **không có Payment**. Của Thành.
> - `NEW_DEVICE` — Customer ↔ Admin qua `Product`, **có Payment**. Của Nam.
>
> Hai kênh dùng **chung một bảng `Order`**, phân biệt bằng cột `orderType`. Đây là điểm thiết kế trung tâm của module.

## Sơ đồ tầng (3-Tier)

```
Browser
  │  HTTP
  ▼
Servlet  ── CartServlet / CheckoutServlet / OrderServlet / PaymentServlet
  │        nhận request, đọc tham số, gọi Service, forward JSP
  ▼
Service  ── OrderService / CartService
  │        business rule, mở transaction, chặn race-condition
  ▼
Repository ─ OrderRepository / CartRepository / PaymentRepository
  │        EntityManager, JPQL
  ▼
PostgreSQL
```

Quy ước URL của module (đọc từ JSP để biết servlet phải nhận gì):

| URL | Servlet | Việc |
|-----|---------|------|
| `GET  /cart` | `CartServlet` | hiện giỏ |
| `POST /cart?action=remove&itemId=` | `CartServlet` | xoá 1 dòng, **trả JSON** |
| `GET  /checkout` | `CheckoutServlet` | hiện form xác nhận |
| `POST /checkout` | `CheckoutServlet` | tạo Order + Payment |
| `GET  /order?action=history&status=&page=` | `OrderServlet` | lịch sử đơn |
| `POST /order?action=cancel` | `OrderServlet` | huỷ đơn |
| `POST /order?action=complete` | `OrderServlet` | xác nhận đã nhận hàng |
| `GET  /payment?action=view&orderId=` | `PaymentServlet` | màn thanh toán |

Dùng **một servlet + tham số `action`** thay vì nhiều servlet rời rạc — đúng Rule 1 trong [.agents/rules/web_programming_requires.md](../../.agents/rules/web_programming_requires.md).

## Luồng nghiệp vụ chính (12.4 trong SDD)

```
1. Customer xem Product  →  thêm vào Cart
                            CartItem.quantity LUÔN = 1
                            Cart KHÔNG giữ chỗ Product        [BR-45]

2. Customer checkout
   ├─ validate từng CartItem: Product.status == AVAILABLE       [BR-45]
   │    └─ sai → 409 PRODUCT_NOT_AVAILABLE
   ├─ tạo Order(orderType = NEW_DEVICE, status = PENDING)
   ├─ tạo OrderItem cho mỗi CartItem, price = SNAPSHOT
   │    (BR-41 chỉ nói về phía Listing; em áp cùng nguyên tắc cho Product)
   ├─ tạo Payment(status = PENDING, method = QR|MOMO|BANK|null) [BR-44]
   └─ TẤT CẢ trong CÙNG MỘT transaction                          [BR-43]

3. Customer xem /payment → thấy PENDING + thông tin chuyển khoản
   (MVP không có cổng thanh toán, không có QR động)              [BR-44]

4. Admin xác nhận đã nhận tiền → Payment PAID
                              → Order PENDING → CONFIRMED        [BR-46]

5. Order CONFIRMED + Payment PAID → Customer bấm "Đã nhận hàng"
                                 → Order COMPLETED               [BR-46]
```

## Ma trận chuyển trạng thái (bảng gốc của SDD, phần NEW_DEVICE)

| Từ | Ai | Sang | Điều kiện kèm theo |
|----|----|------|--------------------|
| PENDING | Admin | CONFIRMED | Payment PENDING → PAID |
| CONFIRMED | Admin hoặc Customer | COMPLETED | **chỉ khi** Payment.status = PAID |
| PENDING / CONFIRMED | Customer hoặc Admin | CANCELLED | trước COMPLETED |
| — | hệ thống | CANCELLED | khi huỷ: Payment PENDING → FAILED **cùng transaction**; Payment đã PAID **giữ nguyên** |

Bất kỳ chuyển trạng thái nào **không có trong bảng này đều bị Business Tier từ chối**.

## Ràng buộc dữ liệu cần nhớ

| Ràng buộc | Ở đâu |
|-----------|-------|
| Mỗi Customer có **đúng một** Cart (`customerId` unique) | `Cart` |
| `(cartId, productId)` unique — không thêm trùng 1 máy 2 lần | `CartItem` |
| Mỗi Order có **tối đa một** Payment (`orderId` unique) | `Payment` |
| `OrderItem` tham chiếu **hoặc** `listing` **hoặc** `product`, loại trừ nhau | `OrderItem` |
| `OrderItem.price` là snapshot, **bất biến** sau khi tạo | [BR-41](../../reference/_sdd.txt) |
| `Order` **không có** trường `payment` — quan hệ 1-0..1 nằm ở phía `Payment` | thiết kế |

Ràng buộc cuối là lý do `history.jsp` cần một `Map<orderId, Payment>` riêng do servlet truyền vào, chứ không viết được `${order.payment}`.

## Hai điểm "ăn điểm" của module

1. **Transaction boundary** — [BR-46](../../reference/_sdd.txt): huỷ đơn là thao tác hai bảng trong cùng một transaction; checkout tạo Order + OrderItem + Payment cũng vậy ([BR-43](../../reference/_sdd.txt), [BR-45](../../reference/_sdd.txt)).
2. **Hai kênh trên một bảng** — `orderType` rẽ nhánh toàn bộ lifecycle, không tạo bảng `Order` thứ hai.

> ⚠️ **Đừng nhận vơ BR-31/BR-32.** Đó là rule **chống tranh mua của kênh `MARKETPLACE`** — quota N đơn active và lock dòng Buyer, thuộc phần của Thành. Kênh `NEW_DEVICE` của bạn **không** giữ chỗ `Product` ([BR-45](../../reference/_sdd.txt)), nên rủi ro tranh mua thấp hơn hẳn.
>
> Điểm tương đương của bạn chỉ là: **validate `Product.status == AVAILABLE` bên trong transaction tạo đơn**, không validate trước rồi mới tạo. Nói đúng như vậy, đừng nói mình làm lock.
