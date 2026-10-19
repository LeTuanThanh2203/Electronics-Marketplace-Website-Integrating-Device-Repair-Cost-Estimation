# study/ — Ôn tập & chuẩn bị demo (Nam)

Folder này là tài liệu tự học, không phải source code của app.
Mục đích: hiểu tường tận module **Giỏ hàng – Đặt hàng – Thanh toán** để tự tin trình bày và trả lời câu hỏi.

## Đọc theo thứ tự

| # | File | Dùng để |
|---|------|---------|
| 0 | [00-tien-do-cong-viec.md](00-tien-do-cong-viec.md) | **Bảng tiến độ + còn mấy bước** — đọc trước tiên |
| 1 | [01-tong-quan-module.md](01-tong-quan-module.md) | Nắm bản đồ module: ai gọi ai, luồng chạy, đang xong tới đâu |
| 2 | [02-kien-thuc-da-ap-dung.md](02-kien-thuc-da-ap-dung.md) | Danh sách kiến thức đã học + thực hành được, kèm mảnh code thật |
| 3 | [03-kich-ban-demo.md](03-kich-ban-demo.md) | Kịch bản demo 8–10 phút, chia theo mốc thời gian |
| 4 | [04-cau-hoi-phan-bien.md](04-cau-hoi-phan-bien.md) | Câu hỏi cô có thể hỏi + câu trả lời mẫu |
| 5 | [05-checklist-truoc-buoi-cham.md](05-checklist-truoc-buoi-cham.md) | Việc phải làm trước ngày chấm |

## Đọc code — bộ file riêng

`tạo dần` — mỗi file đọc sâu một tầng, viết dần theo tiến độ làm bài.

| File | Tầng | Trạng thái tài liệu |
|------|------|---------------------|
| [06-doc-code-entity.md](06-doc-code-entity.md) | Entity + Enum (6 file, 5 enum) | ✅ đã viết — tầng này đã xong thật |
| `07-doc-code-repository.md` | Repository | ⏳ **code đã xong**, tài liệu chưa viết |
| `08-doc-code-service.md` | Service (checkout / cancel / complete / MoMo) | ⏳ **code đã xong**, tài liệu chưa viết |
| `09-doc-code-servlet.md` | Servlet + filter | ⏳ **code đã xong** (trừ `AuthenticationFilter`), tài liệu chưa viết |
| `10-doc-code-view.md` | JSP + CSS + AJAX | ⏳ **code đã xong**, tài liệu chưa viết |

> Ba tầng repository / service / servlet giờ đã có code thật. Chưa viết doc riêng cho
> từng tầng, nhưng [02](02-kien-thuc-da-ap-dung.md) đã gắn link tới code thật của cả ba.

## Tình trạng module (cập nhật 2026-10-19)

| Tầng | Trạng thái |
|------|-----------|
| Entity + Enum | ✅ Xong (`Cart`, `CartItem`, `Order`, `OrderItem`, `Payment`, `Product`, 5 enum) — **đã map annotation JPA và đã verify bằng test** |
| Config + Test | ✅ Xong (`JpaConfig` + `MomoConfig`; **10/10 test xanh**) |
| View JSP + CSS | ✅ Xong (4 màn của module: cart, checkout, payment, history + 2 fragment) |
| Repository | ✅ Xong (`CartRepository`, `OrderRepository`, `PaymentRepository`, `ProductRepository`) |
| Service | ✅ Xong (`CartService`, `OrderService`, `MomoGateway`, `ServiceException`) |
| Servlet | ✅ Xong (`CartServlet`, `CheckoutServlet`, `OrderServlet`, `PaymentServlet`) |
| Thanh toán | ✅ COD + **MoMo thật** (REST v2, sandbox) — đã chạy end-to-end |
| `web.xml` | ✅ Xong (encoding, HttpOnly cookie, `tracking-mode COOKIE`, error-page) |
| `AuthenticationFilter` | ❌ Vẫn rỗng — **là điểm hở duy nhất còn lại**, thuộc phần Tân |

> Chi tiết đầy đủ + hợp đồng JSP: xem [00-tien-do-cong-viec.md](00-tien-do-cong-viec.md).
