# TÀI LIỆU TỔNG HỢP KIẾN THỨC VÀ QUY TẮC DÀNH CHO AGENT
# Framework: Java Servlets, JSP & JPA

## I. MỤC TIÊU CỐT LÕI
Tài liệu này đóng vai trò là bộ quy tắc (Rule set) và Hướng dẫn thực thi (Guidelines) dành riêng cho quá trình xây dựng Project sử dụng công nghệ Web Java.
Các nguyên tắc ở đây được đúc kết nhằm đảm bảo 3 yếu tố:
1. Tính chuẩn xác của kiến trúc (MVC).
2. Hiệu suất hệ thống tối ưu (Performance Tuning).
3. Code sạch, tránh lặp lại hoặc xung đột công nghệ.

---

## II. BỘ QUY TẮC CỐT LÕI CHO AGENT (AGENT RULES)

### Rule 1: Kiến trúc tổng thể (Architecture)
- **BẮT BUỘC** áp dụng mô hình **MVC (Model-View-Controller)**.
- **Controller:** Sử dụng `Servlet` để nhận HTTP Request, xử lý Logic, tương tác với DB (qua Model) và điều hướng (forward/redirect) tới View. Dùng tham số `action` (như `?action=xyz`) để điều hướng chung trong 1 Servlet thay vì tạo quá nhiều Servlet rời rạc.
- **View:** Sử dụng `JSP`. Tuyệt đối **KHÔNG** sử dụng Scriptlet (`<% %>`) trong file JSP. Bắt buộc thay thế toàn bộ logic trên JSP bằng **EL (`${}`)** và **JSTL (`<c:forEach>`, `<c:if>`)**.
- **Model:** Sử dụng các JavaBean tuân thủ chuẩn (có constructor rỗng, getter/setter, `implements Serializable`).

### Rule 2: Truy xuất Dữ liệu (Database Persistence)
- **THAY THẾ JDBC BẰNG JPA:** Để tối ưu code và tận dụng caching, bắt buộc sử dụng **JPA (Java Persistence API)** làm công cụ truy xuất CSDL duy nhất. **KHÔNG** sử dụng hỗn hợp cả JPA và JDBC thuần trong cùng một Project để tránh chồng chéo connection.
- **Quy tắc OR Mapping:** Thống nhất duy nhất 1 kiểu cấu hình Mapping trên Entity: Sử dụng **Field-based mapping** (đặt `@Id`, `@Column` trực tiếp trên các biến instance). TUYỆT ĐỐI KHÔNG dùng Property-based mapping (đặt trên hàm Getter), hoặc trộn lẫn cả hai kiểu trong một hệ thống.
- **Lazy Loading:** Bắt buộc áp dụng `FetchType.LAZY` cho tất cả các quan hệ Collection (`@OneToMany`, `@ManyToMany`). Không sử dụng `FetchType.EAGER` để tránh lỗi N+1 Query và quá tải bộ nhớ (RAM).
- **Batch Processing:** Cấu hình `hibernate.jdbc.batch_size` (vd: 50) trong `persistence.xml` cho các tác vụ insert/update số lượng lớn.

### Rule 3: Quản lý Phiên và Kết nối (State & Connection Management)
- **Connection Pooling:** Bắt buộc sử dụng Connection Pool (như HikariCP, Tomcat JDBC Pool) trong cấu hình của `persistence.xml`. Không được để JPA tự mở từng kết nối TCP riêng lẻ cho mỗi request.
- **Session Memory Optimization:** Chỉ lưu trữ những object thiết yếu trên Session (như `User` hiện tại, `Cart`). Tuyệt đối KHÔNG lưu danh sách lớn (vd: Danh sách toàn bộ Product) vào Session để tránh lỗi `OutOfMemory`. Mọi dữ liệu tạm không bảo mật hãy đẩy xuống lưu trữ ở Cookie hoặc LocalStorage của Client.

### Rule 4: Tối ưu Giao tiếp & Hiệu năng mạng (Network Performance)
- **Sử dụng AJAX/JSON:** Đối với những chức năng chỉ cập nhật một phần nhỏ giao diện (vd: Thêm vào giỏ hàng, Lấy thông tin phụ), bắt buộc sử dụng JavaScript (Fetch API/AJAX) giao tiếp với Server qua chuẩn **JSON**. Không reload lại toàn bộ trang JSP.
- **GZIP Filter:** Áp dụng một `Filter` để nén các phản hồi HTTP dạng text (HTML, CSS, JSON) sang chuẩn GZIP trước khi gửi xuống Client.
- **Asynchronous Tasks:** Với các tác vụ tốn thời gian như Gửi Email, bắt buộc tạo luồng riêng (Thread/Async) để xử lý ngầm, trả ngay response về cho client.

### Rule 5: Bảo mật (Security & Files)
- **Mã hóa Mật khẩu:** KHÔNG lưu plaintext password vào database. Bắt buộc sử dụng thuật toán hash như **BCrypt** hoặc **SHA-256** trước khi insert/update.
- **Xử lý File Upload:** Khi user upload file/hình ảnh, hệ thống chỉ lưu **đường dẫn URL (String)** vào CSDL. File thực tế phải được lưu trên ổ cứng Server hoặc Cloud (vd: AWS S3). Tuyệt đối không lưu file dưới dạng BLOB vào CSDL.
- **Authentication Filter:** Bắt buộc dùng `Filter` để bảo vệ các URI cần đăng nhập. Không kiểm tra bằng code thủ công trong từng Servlet.

---

## III. SO SÁNH: TẠI SAO CHỌN JPA THAY VÌ JDBC?
Để làm rõ việc loại bỏ JDBC và chuẩn hóa JPA cho project:
1. **Lượng Code (Boilerplate):** JPA giúp giảm đến 70% lượng code tương tác DB nhờ tính năng tự tạo câu lệnh SQL từ cấu trúc Entity. Nếu dùng JDBC, lập trình viên phải mở/đóng Connection, Statement và loop ResultSet ở mọi hàm.
2. **First-Level Cache (Đỉnh cao của JPA):** JPA tích hợp sẵn Cache cấp 1 trong `EntityManager`. Khi bạn `.find()` một ID hai lần trong cùng một giao dịch, JPA trả thẳng dữ liệu từ RAM ở lần thứ 2 mà không query xuống CSDL. JDBC không có tính năng này và sẽ query 2 lần.
3. **Lazy Loading:** JPA cho phép load các dữ liệu con chỉ khi nào hàm `getter` tương ứng được gọi, tối ưu hóa RAM rât tốt. Nếu làm bằng JDBC, bạn phải tự viết tay hàng loạt logic rất tốn công.
4. **Kết luận:** Agent **chỉ sử dụng JPA** để xây dựng tầng Data Access (DAO).

---
*(End of Ruleset)*
