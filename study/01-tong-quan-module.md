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
Service  ── OrderService / CartService / MomoGateway
  │        business rule, mở transaction, chặn race-condition
  │        MomoGateway gọi REST v2 của MoMo qua HttpURLConnection
  ▼
Repository ─ OrderRepository / CartRepository / PaymentRepository
  │        EntityManager, JPQL
  ▼
PostgreSQL
```

Ngoài 4 servlet của module, `ListingServlet` / `ProductServlet` / `RepairRequestServlet`
của các bạn khác cũng nằm cùng tầng này; `config/JpaConfig` và `config/MomoConfig`
là hai Holder đọc cấu hình từ biến môi trường, fail fast nếu thiếu.

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
| `GET  /payment?action=create&orderId=` | `PaymentServlet` | tạo giao dịch MoMo → redirect sang `payUrl` |
| `GET  /payment?action=return` | `PaymentServlet` | MoMo redirect người dùng về → đối chiếu bằng `query` API |
| `POST /payment?action=ipn` | `PaymentServlet` | MoMo gọi server-to-server → verify chữ ký → trả JSON |

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
   ├─ tạo Payment(status = PENDING, method = MOMO|null)          [BR-44]
   │    (`null` = COD; chỉ còn 2 lựa chọn, QR và thẻ ngân hàng đã bỏ)
   └─ TẤT CẢ trong CÙNG MỘT transaction                          [BR-43]

3. Rẽ theo phương thức:

   3a. MoMo — Customer bấm "Thanh toán qua MoMo"
       ├─ GET /payment?action=create → MomoGateway.create()
       │    orderId = transactionCode + "-" + 8 hex (MoMo cấm trùng orderId)
       ├─ 302 sang https://test-payment.momo.vn/v2/gateway/pay?…
       ├─ MoMo gọi POST /payment?action=ipn (server-to-server)
       │    → verifyIpn (HMAC-SHA256 trên tham số đã URL-decode)
       │    → OrderService.markPaidByGateway()
       └─ MoMo redirect người dùng về GET /payment?action=return
            → KHÔNG tin tham số URL, gọi query API lấy kết quả thật
            → resultCode == 0 thì markPaidByGateway()

       → Payment PAID + paidAt, Order PENDING → CONFIRMED
         (cùng transaction, BR-46). Không cần Admin làm gì.

   3b. COD — Payment giữ PENDING, hiện "Đã ghi nhận đơn hàng"

4. Admin xác nhận đã nhận tiền (chỉ COD) → Payment PAID
                                          → Order PENDING → CONFIRMED  [BR-46]
   Với MoMo bước này đã xảy ra tự động ở 3a; Admin vẫn xác nhận tay được
   khi cần đối soát (BR-44).

5. Order CONFIRMED + Payment PAID → Customer bấm "Đã nhận hàng"
                                 → Order COMPLETED               [BR-46]
```

## Ma trận chuyển trạng thái (bảng gốc của SDD, phần NEW_DEVICE)

| Từ | Ai | Sang | Điều kiện kèm theo |
|----|----|------|--------------------|
| PENDING | Admin, hoặc MoMo tự động | CONFIRMED | Payment PENDING → PAID |
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

## Ba điểm "ăn điểm" của module

1. **Transaction boundary** — [BR-46](../../reference/_sdd.txt): huỷ đơn là thao tác hai bảng trong cùng một transaction; checkout tạo Order + OrderItem + Payment cũng vậy ([BR-43](../../reference/_sdd.txt), [BR-45](../../reference/_sdd.txt)).
2. **Hai kênh trên một bảng** — `orderType` rẽ nhánh toàn bộ lifecycle, không tạo bảng `Order` thứ hai.
3. **Cổng thanh toán thật** — MoMo REST v2, ký HMAC-SHA256, IPN idempotent. Xem [00 — Bước 10](00-tien-do-cong-viec.md#bước-10--momo-thật-đã-chạy-không-phải-giả-lập).

> ⚠️ **Đừng nhận vơ BR-31/BR-32.** Đó là rule **chống tranh mua của kênh `MARKETPLACE`** — quota N đơn active và lock dòng Buyer, thuộc phần của Thành. Kênh `NEW_DEVICE` của bạn **không** giữ chỗ `Product` ([BR-45](../../reference/_sdd.txt)), nên rủi ro tranh mua thấp hơn hẳn.
>
> Điểm tương đương của bạn chỉ là: **validate `Product.status == AVAILABLE` bên trong transaction tạo đơn**, không validate trước rồi mới tạo. Nói đúng như vậy, đừng nói mình làm lock.
