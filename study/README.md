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
| `07-doc-code-repository.md` | Repository | ⏳ chờ bạn viết code |
| `08-doc-code-service.md` | Service (checkout / cancel / complete) | ⏳ chờ bạn viết code |
| `09-doc-code-servlet.md` | Servlet + filter | ⏳ chờ bạn viết code |
| `10-doc-code-view.md` | JSP + CSS + AJAX | ⏳ chờ bạn viết code |


## Tình trạng module (cập nhật 2026-10-16)

| Tầng | Trạng thái |
|------|-----------|
| Entity + Enum | ✅ Xong (`Cart`, `CartItem`, `Order`, `OrderItem`, `Payment`, `Product`, 5 enum) — **đã map annotation JPA và đã verify bằng test** |
| Config + Test | ✅ Xong (`JpaConfig` + `JpaConfigTest` + `EntityMappingTest`, 3/3 xanh) |
| View JSP + CSS | ✅ Xong (5 màn: cart, checkout, payment, history + 2 fragment) |
| Repository | ❌ Còn rỗng (3 file; `ProductRepository` đã xong) |
| Service | ❌ Còn rỗng (và **chưa có `CartService`**) |
| Servlet | ❌ Chưa có logic — `@WebServlet` đã đăng ký nhưng mọi action trả `NOT_IMPLEMENTED` |
| `web.xml` | ✅ Xong — nhưng **chưa có `AuthenticationFilter`** nên URI bảo vệ vẫn hở |

> Chi tiết đầy đủ + hợp đồng JSP: xem [00-tien-do-cong-viec.md](00-tien-do-cong-viec.md).
