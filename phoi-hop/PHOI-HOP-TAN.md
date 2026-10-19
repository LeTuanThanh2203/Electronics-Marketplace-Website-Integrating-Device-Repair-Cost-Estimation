# Đồng bộ nhánh Nam ↔ Tân — hợp đồng `currentUser` và các điểm vỡ khi merge

> Viết 2026-10-19, sau khi review `origin/Tan` @ `af8b56c` đối chiếu `Nam` @ `68eb932`.
> Mục tiêu: Tân sửa xong phần auth thì merge vào là chạy, không phải sửa lại lần hai.

## TL;DR — một attribute, một tên

| Hạng mục | Chốt | Ai sửa |
|---|---|---|
| Tên attribute session chứa user | **`currentUser`** | **Tân** — 7 chỗ đang dùng `"user"` |
| `AuthenticationFilter.urlPatterns` | thêm `/checkout`, `/order`, `/payment`, và `/cart` khi có `action` | **Tân** |
| Filter trả gì khi chưa login | AJAX (`Accept: application/json`) → **401 + JSON**; còn lại → redirect `/login` | **Tân** |
| `entity/User.java` | lấy bản **Nam** (`Long id`), **giữ** getter dẫn xuất của Tân | **Tân** port sang |
| `persistence.xml` | lấy bản **Nam** (auto-scan, env var) | **Tân** bỏ `<class>murach.business.User</class>` |
| `DBConnection` / `DatabaseInitializer` | **xoá**, dùng `config/JpaConfig` | **Tân** |
| `web.xml`, `pom.xml` | lấy bản **Nam** | — |

---

## 1. Session key: `"currentUser"`, không phải `"user"`

Đây là điểm vỡ lớn nhất. Hai bên đang đọc hai key khác nhau nên **đăng nhập xong vẫn
bị coi là chưa đăng nhập** — không có exception nào, chỉ là `null` im lặng.

**Bên Nam (không đổi):**

```java
// CartServlet:124, CheckoutServlet:68, OrderServlet:146, PaymentServlet:67
private User currentUser(HttpServletRequest req) {
    return (User) req.getSession().getAttribute("currentUser");
}
```

```jsp
<%-- WEB-INF/views/order/_header.jspf:12 --%>
<c:set var="currentUser" value="${sessionScope.currentUser}" />
```

**Bên Tân phải đổi — 7 chỗ:**

| File | Dòng | Hiện tại | Đổi thành |
|---|---|---|---|
| `controller/auth/LoginServlet.java` | 17 | `s.getAttribute("user")` | `"currentUser"` |
| `controller/auth/LoginServlet.java` | 38 | `setAttribute("user", u)` | `"currentUser"` |
| `controller/auth/ProfileServlet.java` | 24 | `session.getAttribute("user")` | `"currentUser"` |
| `controller/auth/ProfileServlet.java` | 54 | `session.setAttribute("user", …)` | `"currentUser"` |
| `controller/admin/AdminServlet.java` | 34 | `getAttribute("user")` | `"currentUser"` |
| `filter/AuthenticationFilter.java` | 17 | `getAttribute("user")` | `"currentUser"` |
| `filter/AuthorizationFilter.java` | 18 | `getAttribute("user")` | `"currentUser"` |
| `WEB-INF/layout/header.jspf` | 16, 17, 20 | `${sessionScope.user…}` | `${sessionScope.currentUser…}` |
| `WEB-INF/auth/profile.jsp` | 6 | `${sessionScope.user}` | `${sessionScope.currentUser}` |

Lý do chọn `currentUser`: Rule 3 của dự án ghi rõ session chỉ giữ **User + Cart**;
`cart` đã là tên attribute rồi, nên `user` một chữ dễ lẫn với biến cục bộ trong JSP
(`<c:set var="u">`) và với `pageContext.request.userPrincipal`. Đổi ở Tân rẻ hơn:
9 dòng, so với 4 servlet + 2 view + fragment của Nam.

> Sau khi đổi, **xoá luôn** nhánh `getAttribute("user")` cũ, đừng giữ fallback
> `getAttribute("currentUser") != null ? … : getAttribute("user")`. Hai nguồn sự thật
> cho một attribute là cách chắc chắn nhất để lần sau lại lệch.

---

## 2. `AuthenticationFilter` — phải phủ 4 URI đang hở

Bản `origin/Tan`:

```java
@WebFilter(urlPatterns = {"/profile", "/admin", "/admin/*"})
```

Nhưng module Cart/Order/Payment có 4 URI cần đăng nhập và hiện **không filter nào che**:

| URI | Hành vi hiện tại | Ghi chú |
|---|---|---|
| `GET /cart` | cho xem giỏ rỗng khi chưa login | **cố ý** — đừng chặn |
| `POST /cart?action=add` | 401 trong servlet | cần filter |
| `POST /cart?action=remove` | 401 JSON | cần filter, xem mục 2.1 |
| `POST /checkout` | 401 trong servlet | cần filter |
| `GET /order?action=history` | 401 trong servlet | cần filter |
| `GET /payment?orderId=…` | 401 trong servlet | cần filter |

Đề xuất:

```java
@WebFilter(urlPatterns = {"/profile", "/admin", "/admin/*", "/checkout", "/order", "/payment", "/cart"})
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession s = req.getSession(false);

        if (s == null || s.getAttribute("currentUser") == null) {
            // GET /cart khong co action = xem gio (cho phep anonymous).
            if ("/cart".equals(req.getServletPath()) && req.getParameter("action") == null) {
                chain.doFilter(request, response);
                return;
            }
            // AJAX phai nhan JSON, khong duoc nhan 302 (xem 2.1).
            if (wantsJson(req)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write("{\"ok\":false,\"code\":\"UNAUTHENTICATED\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }
}
```

### 2.1 Vì sao AJAX không được nhận `sendRedirect`

`cart.jsp:158-161` gọi:

```js
fetch(btn.dataset.url, { method: 'POST', headers: { 'Accept': 'application/json' } })
  .then(function (res) { if (!res.ok) throw new Error(res.status); return res.json(); })
```

Nếu filter `sendRedirect` → `fetch` tự đi theo 302 → nhận về **HTML của trang login**
→ `res.json()` ném `SyntaxError` → rơi vào `.catch` → reload trang. Nhìn thì "vẫn
chạy", nhưng hợp đồng JSON đã vỡ và người dùng bị đá về một trang trắng không lý do.
SDD §14 quy định 401 cho thiếu auth — trả đúng mã đó.

`Accept` là header chính xác để phân biệt, không phải `X-Requested-With`: chỗ gọi
trong `cart.jsp` có set `Accept`, không set header kia.

### 2.2 `AuthorizationFilter`

Giữ nguyên logic, chỉ đổi key ở dòng 18. Nó chặn `/admin/*` bằng `u.isAdmin()` —
`isAdmin()` phải còn sau khi merge (mục 3).

---

## 3. `User` — lấy entity của Nam, port getter của Tân

Hai bản lệch nhau ở kiểu khoá chính và tên enum, nên **không thể** để "ai merge sau
thì thắng": bản nào thắng cũng làm đỏ test hoặc đỏ view của bên kia.

### 3.1 Bắt buộc lấy từ Nam

| | Nam | Tân | Chốt |
|---|---|---|---|
| Khoá chính | `Long id` / `getId()` | `int userID` / `getUserID()` | **`Long id`** |
| Enum trạng thái | `UserStatus` | `Status` | **`UserStatus`** |
| `createdAt` | `java.util.Date` + `@Temporal` | `java.sql.Timestamp` | **`Date`** |
| `phone` | nullable | `nullable = false` | **nullable** |

Vì sao `Long id`: `Cart.customer`, `Order.buyer`, `Listing.seller` đều là
`@ManyToOne User` với khoá `Long`; `EntityMappingTest` và `OrderServiceTest` đều gọi
`user.getId()`. Đổi sang `int` là phải sửa toàn bộ khoá ngoại.

Vì sao `phone` nullable: `EntityMappingTest:45-51` và `OrderServiceTest:169-175` tạo
`new User()` mà **không** `setPhone`. Để `nullable=false` là hai test này chết. Ngoài
ra `hbm2ddl.auto=update` **không** thêm `NOT NULL` cho cột đã tồn tại, nên khai
`nullable=false` cũng chẳng có tác dụng gì trên DB `sua_hay_ban` hiện tại — chỉ gây
lệch metadata.

### 3.2 Phải giữ từ Tân

Bốn getter dẫn xuất và hai callback này đang được view dùng, xoá là trắng trang:

```java
// ${sessionScope.currentUser.admin}   -> layout/header.jspf:17
// ${u.active}                          -> auth/profile.jsp
public boolean isAdmin()     { return role == Role.ADMIN; }
public boolean isActive()    { return status == UserStatus.ACTIVE; }
public boolean isSuspended() { return status == UserStatus.SUSPENDED; }
public boolean isDeleted()   { return status == UserStatus.DELETED; }
public String  getInitial()  { … }        // ${u.initial}

@PrePersist void onCreate() { createdAt = new Date(); updatedAt = new Date(); }
@PreUpdate  void onUpdate() { updatedAt = new Date(); }
```

`@PrePersist` không phải "cho đẹp": **không một dòng production nào** gọi
`setCreatedAt` cho `User` (chỉ test gọi). Không có callback thì `INSERT` vi phạm
`created_at NOT NULL` và `RegisterServlet` nổ.

`getInitial()` phải đổi `name.isBlank()` → `name.trim().isEmpty()` — xem mục 6.

### 3.3 Việc Tân phải sửa sau khi lấy entity của Nam

`getUserID()` xuất hiện ở 3 file + 1 view. Đổi hết thành `getId()` và `int` → `Long`:

- `DBConnection/UserDB.java`: `insert`, `selectById(int)`, `updateProfile`,
  `updatePassword(int, …)`, `updateStatus(int, …)`
- `controller/auth/ProfileServlet.java:24,54`
- `controller/admin/AdminServlet.java:34` và biến `int id`
- `WEB-INF/admin/dashboard.jsp:28,33,36,40,43,46`: `${x.userID}` → `${x.id}`

Enum: `entity/enums/Status.java` (Tân) trùng giá trị với `UserStatus` (Nam) →
**xoá file của Tân**, sửa import ở `AuthDB`, `UserDB`, `LoginServlet`,
`AdminServlet` sang `entity.enums.UserStatus`.

### 3.4 Độ dài cột — để nguyên bản Nam

| Cột | Nam | Tân |
|---|---|---|
| `avatar` | 255 | 500 |
| `location` | 150 | 200 |
| `role` / `status` | 20 | 10 |
| `updated_at` | nullable | `NOT NULL` |

`hbm2ddl.auto=update` không bao giờ `ALTER` cột đã có, nên đổi khai báo **không** đổi
DB thật. Lấy bản Nam cho khớp cột đang tồn tại; muốn nới `avatar` lên 500 thì làm
một migration riêng, đừng trộn vào merge.

---

## 4. URL và đường dẫn view đang trỏ vào hư không

### 4.1 `_header.jspf` gọi `/auth?action=login`

```jsp
<%-- Nam, _header.jspf:39-40 --%>
<a href="${ctx}/auth?action=login">Đăng nhập</a>
<a href="${ctx}/auth?action=register">Đăng ký</a>
```

Không tồn tại `AuthServlet` ở **cả hai nhánh**. Tân đăng ký `/login` và `/register`
thành hai servlet riêng (`LoginServlet:12`, `RegisterServlet:13`).

→ **Nam sửa 2 dòng** thành `${ctx}/login` / `${ctx}/register`. Không đổi mapping của
Tân: `/profile`, `/logout`, `/admin` đã có sẵn và form trong `login.jsp:8` post thẳng
vào `${contextPath}/login`.

### 4.2 Hai bộ header/footer

| | Nam | Tân |
|---|---|---|
| Header | `WEB-INF/views/order/_header.jspf` | `WEB-INF/layout/header.jspf` |
| Footer | `WEB-INF/views/order/_footer.jsp` | `WEB-INF/layout/footer.jspf` |
| CSS | `css/order.css` (20.8 KB) | `css/style.css` (**0 B**) |
| JSP | `WEB-INF/views/{auth,admin}/*.jsp` (**0 B**) | `WEB-INF/auth/*.jsp`, `WEB-INF/admin/*.jsp` |

Sau merge mà để nguyên thì: `login.jsp` include `/WEB-INF/layout/header.jspf` →
**tồn tại** (Tân mang theo), nên không 500. Nhưng trang login sẽ dùng
`style.css` rỗng ⇒ **trắng trơn, không style**, trong khi giỏ hàng dùng `order.css`
đầy đủ. Đây không phải lỗi merge, chỉ là hai giao diện khác nhau trong cùng app.

Xử lý tối thiểu, không chặn merge:
1. Merge giữ **cả hai** thư mục — Tân giữ `/WEB-INF/auth|admin|layout`, Nam giữ
   `/WEB-INF/views/...`. Không file nào đụng nhau.
2. 4 file 0 B bên Nam (`views/auth/login.jsp`, `views/auth/register.jsp`,
   `views/admin/dashboard.jsp`, `views/home.jsp`) → **xoá**, chúng chỉ là placeholder.
3. Hợp nhất CSS để sau: trỏ `layout/header.jspf` sang `${ctx}/css/order.css` và đổi
   class `.navbar/.card/.auth-card/.alert` → class của `order.css`. Việc riêng, không
   làm trong commit merge.

Kiểm tra nhanh sau merge: `grep -c 'auth-card\|navbar\|alert' css/order.css` → hiện
`0`; nếu vẫn 0 thì trang login vẫn chưa có style.

### 4.3 Link đang chết trên cả hai nhánh

`_header.jspf:30` trỏ `${ctx}/home`, `:33` `/product?action=list`, `:34`
`/listing?action=marketplace`, `:35` `/repair?action=form`. Bốn đường này **chưa có
servlet nào** (`@WebServlet` hiện chỉ có `/cart`, `/checkout`, `/order`, `/payment`
bên Nam; `/login`, `/register`, `/logout`, `/profile`, `/admin` bên Tân). Không phải
việc của Tân — ghi lại để lúc demo không tưởng là merge hỏng.

---

## 5. `DBConnection` / `DatabaseInitializer` — xoá, dùng `JpaConfig`

Bản Tân có nguyên một tầng kết nối song song với `JpaConfig` của Nam, và nó **không
chạy được** trên nhánh này:

| Vấn đề | Chi tiết |
|---|---|
| PU không tồn tại | `DBConnection.java:49` gọi `Persistence.createEntityManagerFactory("chodocuPU", …)` — `persistence.xml` chỉ có `Electronics_MarketplacePU` và `adminPU` |
| DB mặc định sai | `dbName()` fallback `"chodocu"`; DB thật là `sua_hay_ban` |
| Trùng nguồn cấu hình | `JpaConfig` đọc `DB_URL/DB_USER/DB_PASSWORD`; `DBConnection` đọc `DB_HOST/DB_PORT/DB_NAME/…` |
| `DatabaseInitializer.createDatabaseIfMissing()` | dùng `em.unwrap(org.hibernate.Session.class).doWork(conn -> …)` + `PreparedStatement` thô — vi phạm quy ước "chỉ Servlet + JSP/JSTL + JPA chuẩn", và `JpaConfig` đã fail-fast khi thiếu env nên không cần tự tạo DB |

**Cách sửa rẻ nhất (5 dòng), giữ nguyên `DBUtil` / `UserDB` / `AuthDB`:**

```java
// DBConnection/DBUtil.java — thay phần lấy EMF
private static EntityManager em() {
    return com.electronics.marketplace.config.JpaConfig
            .getEntityManagerFactory().createEntityManager();
}
```

Rồi **xoá** `DBConnection/DBConnection.java` và `DBConnection/DatabaseInitializer.java`.
`DBUtil` chỉ cần `query/inTransaction/execute` như hiện tại.

`DatabaseInitializer` còn một việc là `seedAdmin()` — nếu vẫn cần tài khoản admin đầu
tiên thì viết lại bằng JPA thuần (`em.createQuery("SELECT COUNT(u) FROM User u WHERE
u.role = :r")` rồi `em.persist`) trong một `@WebListener` riêng, **không** dùng
`Session.doWork`.

`contextDestroyed` của nó còn gọi `MailUtilGmail.shutdown()`. Xoá listener là mất chỗ
tắt `ExecutorService` 2 thread của Tân ⇒ leak thread mỗi lần redeploy. Giữ một
`@WebListener` chỉ để shutdown mail, hoặc đăng ký `Runtime.addShutdownHook`.

### 5.1 `persistence.xml`

Bản Tân đang đăng ký **0 entity**:

```xml
<class>murach.business.User</class>
<exclude-unlisted-classes>true</exclude-unlisted-classes>
```

`murach.business.User` không tồn tại trong project. Với `exclude-unlisted-classes=true`
thì Hibernate chỉ nạp đúng class đó ⇒ không có `User`, `Cart`, `Order`, `Payment`,
`Product`, `Listing` nào được map. App boot lên, `JpaConfig` tạo EMF thành công, rồi
mọi query nổ `Unknown entity`.

→ Lấy nguyên `src/main/resources/META-INF/persistence.xml` của Nam: **không** khai
`<class>`, để auto-scan. Tân thêm entity mới cũng không phải sửa file.

---

## 6. ~~Bẫy Java 8: `String.isBlank()`~~ — đã hết, cả nhóm lên 17

> **Cập nhật 2026-10-19:** nhánh Nam đã nâng `maven.compiler.source/target` lên
> **17** (theo yêu cầu của Thành), nên `isBlank()` không còn là bẫy. Mục này giữ
> lại làm lịch sử. Khi merge, nhớ pom của nhánh gộp cũng phải để 17 — đừng để
> conflict resolution kéo về 8.

`isBlank()` là API Java 11. Nếu có nhánh nào còn để `source/target = 8` thì nó
biên dịch được (JDK đang chạy là 17, `-source 8` chỉ cảnh báo) nhưng sẽ nổ trên
JVM 8 thật.

4 chỗ từng phải sửa (nay không cần, vì đã lên 17):

| File | Dòng | Sửa nếu phải quay lại 8 |
|---|---|---|
| `DBConnection/DBConnection.java` | 28 | `!v.trim().isEmpty()` |
| `entity/User.java` | 97 | `name.trim().isEmpty()` |
| `util/MailUtilGmail.java` | 35 | `!v.trim().isEmpty()` |
| `util/SQLUtil.java` | 10 | `s.trim().isEmpty()` |

Hoặc nâng cả hai pom lên 17 ở **một commit riêng** — nhưng đừng trộn vào commit merge,
vì nó đổi cấu hình build của cả nhóm.

---

## 7. Bảo mật: `DatabaseInitializer` hardcode tài khoản admin

```java
// DatabaseInitializer.java:22-23
public static final String ADMIN_EMAIL = "cntann06@gmail.com";
public static final String ADMIN_PASSWORD = "Admin@123";
```

File này đang ở nhánh `origin/Tan` và nếu merge nguyên thì **mật khẩu admin mặc định
đi vào git history**, ai đọc repo cũng đăng nhập được `/admin`. Trước khi merge:

- [ ] Xoá hai hằng số khỏi source; nếu cần seed thì đọc từ env
      (`ADMIN_EMAIL` / `ADMIN_PASSWORD`) và chỉ chạy khi cả hai có giá trị.
- [ ] Không log mật khẩu ở bất kỳ nhánh nào.
- [ ] Nếu repo đã từng push hai giá trị này, đổi mật khẩu tài khoản thật sau khi seed.

---

## 8. File chết / file ghi đè được — không cần thương lượng

Hai bên có cùng đường dẫn nhưng nội dung khác hẳn. Các file dưới đây **bên Tân là
stub rỗng**, nên khi merge lấy bản Nam, không mất gì:

| File | Tân | Nam |
|---|---|---|
| `controller/user/CartServlet.java` | 84 B | 137 dòng, có `@WebServlet("/cart")` |
| `controller/user/CheckoutServlet.java` | 88 B | thật |
| `controller/user/OrderServlet.java` | 85 B | thật |
| `controller/user/PaymentServlet.java` | 87 B | thật |
| `controller/user/RepairRequestServlet.java` | 93 B | stub cả hai bên |
| `controller/admin/AdminProductServlet.java` | 93 B | 98 B |
| `controller/admin/AdminRepairServlet.java` | 92 B | 97 B |
| `repository/UserRepository.java` | 86 B | 91 B (**interface rỗng**, không ai dùng) |
| `service/UserService.java` | 76 B | 81 B (**rỗng**, không ai dùng) |
| `entity/Admin.java` | 69 B — `public class Admin {}`, không phải entity | — |
| `util/ValidationUtil.java` | 76 B | 81 B |

Ngược lại, các file **Tân viết thật, Nam đang là stub** — merge lấy bản Tân:

| File | Ghi chú |
|---|---|
| `util/PasswordUtil.java` | BCrypt, dùng `jbcrypt 0.4` (đã có trong pom) |
| `util/SQLUtil.java` | `blankToNull` |
| `util/MailUtilGmail.java` | `jakarta.mail 2.0.3` (đã có trong pom) — **không thêm dependency** |
| `filter/GzipFilter.java` | `@WebFilter("/*")`, chỉ nén css/js/JSON, đúng Rule 4 |
| `filter/AuthenticationFilter.java`, `AuthorizationFilter.java` | sau khi sửa mục 1 + 2 |
| `controller/auth/LoginServlet.java`, `RegisterServlet.java`, `LogoutServlet.java`, `ProfileServlet.java` | sau khi sửa mục 1 + 3 |
| `controller/admin/AdminServlet.java` | sau khi sửa mục 1 + 3 |

`filter/EncodingFilter.java` là stub ở **cả hai** bên. Hiện `LoginServlet:25` và
`ProfileServlet:23` tự gọi `req.setCharacterEncoding("UTF-8")` nên chưa vỡ; nhưng
`CheckoutServlet` không gọi, và một filter UTF-8 cho `/*` là chỗ đúng để làm việc đó
(Rule 5). Không chặn merge.

Xoá luôn khi merge: `com/example/electronics_marketplace_project/HelloApplication.java`
+ `HelloResource.java` (scaffolding `@ApplicationPath("/api")`, không dùng),
`entity/enums/Status.java` (mục 3.3), và `hs_err_pid*.log` nếu có.

---

## 9. Checklist của Tân trước khi push để merge

> **Kết quả kiểm tra 2026-10-19 @ `origin/Tan` `03c057f`: xem [mục 12](#12-kết-quả-kiểm-tra-nhánh-tân--2026-10-19-origintan-03c057f).**
> 9/11 ô đã đạt; 2 ô còn lại là `mvn test` và `WEB-INF/lib`.

- [ ] Đổi `"user"` → `"currentUser"` đủ 9 chỗ (bảng mục 1).
- [ ] `AuthenticationFilter` phủ thêm `/checkout`, `/order`, `/payment`, `/cart`;
      AJAX trả 401 JSON, không redirect.
- [ ] Lấy `entity/User.java` của Nam; port `isAdmin/isActive/isSuspended/isDeleted/
      getInitial` + `@PrePersist/@PreUpdate`; `getInitial` bỏ `isBlank()`.
- [ ] `getUserID()` → `getId()`, `int` → `Long` ở `UserDB`, `ProfileServlet`,
      `AdminServlet`, `dashboard.jsp`.
- [ ] Xoá `enums/Status.java`, đổi import sang `UserStatus`.
- [ ] Xoá `DBConnection.java` + `DatabaseInitializer.java`; `DBUtil` lấy EMF từ
      `JpaConfig`. Không còn `org.hibernate.Session` hay `java.sql.*` trong source.
- [ ] `persistence.xml`: không còn `<class>murach.business.User</class>`, không còn
      `exclude-unlisted-classes=true`.
- [ ] Bỏ `ADMIN_EMAIL` / `ADMIN_PASSWORD` hardcode.
- [ ] Thay 4 chỗ `isBlank()`.
- [ ] `mvn -o test` xanh (7/7) **trước khi** push.
- [ ] Không commit `WEB-INF/lib/*.jar`, `target/`, `*.log`.

## 12. Kết quả kiểm tra nhánh Tân — 2026-10-19 @ `origin/Tan` `03c057f`

### 12.1 Đối chiếu checklist mục 9

| # | Hạng mục | Kết quả | Bằng chứng |
|---|---|---|---|
| 1 | `"user"` → `"currentUser"` đủ 9 chỗ | ✅ | `AuthenticationFilter:14 SESSION_KEY = "currentUser"`; `git grep 'getAttribute("user")\|sessionScope\.user\b'` trên `origin/Tan` = **0** |
| 2 | Filter phủ `/checkout /order /payment /cart`, AJAX 401 JSON | ✅ | `AuthenticationFilter:10` `{"/profile","/admin","/admin/*","/checkout","/order","/payment","/cart","/listing"}`; `:52 HttpUtil.writeJson(401)`; `:58 sendRedirect("/login")` |
| 3 | `User` của Nam + getter Tân + `@PrePersist` | ✅ | `Long id`, `Role role`, `UserStatus status`, `Date` + `@Temporal`; 5 getter dẫn xuất; `@PrePersist/@PreUpdate` |
| 4 | `getUserID()` → `getId()`, `int` → `Long` | ✅ | `git grep getUserID` = 0; `DBConnection/UserDB` + `ProfileServlet` + `AdminServlet` + `dashboard.jsp` đã đổi |
| 5 | Xoá `enums/Status.java` | ✅ | Không còn file; `git grep 'enums.Status'` = 0 |
| 6 | Xoá `DBConnection` + `DatabaseInitializer`, dùng `JpaConfig` | ✅ | Chỉ còn `DBUtil/AuthDB/UserDB`; `DBUtil:13 JpaConfig.getEntityManagerFactory()`; `org.hibernate.*` + `java.sql.*` trong `src/main/java` = **0** |
| 7 | `persistence.xml` auto-scan | ✅ | Không `<class>`, không `exclude-unlisted-classes`, không URL/user/password |
| 8 | Bỏ hardcode admin | ✅ | `AdminSeedListener:17-18` đọc `System.getenv("ADMIN_EMAIL"/"ADMIN_PASSWORD")`, thiếu thì log rồi bỏ qua |
| 9 | `isBlank()` → `trim().isEmpty()` | ✅ (không cần) | Đã lên Java 17; `git grep 'isBlank('` = 0 |
| 10 | `mvn -o test` xanh | ❌ | `src/test` **rỗng** — `3782371` xoá cả 4 file test của Nam |
| 11 | Không commit `WEB-INF/lib/*.jar`, `target/`, `*.log` | ✅ | `git ls-tree origin/Tan -- src/main/webapp/WEB-INF/lib` = rỗng |

### 12.2 Nguyên nhân gốc: commit revert `3782371`

Lịch sử trên `origin/Tan`:

```
2b9e8d9 Merge branch 'Nam' into Tan        <- merge đầu tiên
f0769f6 merge: lay lai cac file thieu tu branch Nam
...
3782371 Revert: bo cac file merge tu commit f0769f6   <- XOÁ LẠI toàn bộ
4c49b5d back về java 17
03c057f chỉnh sửa AdminServlet, dashboard theo ListingStatus
```

`3782371` xoá **45 file / 9.329 dòng**: toàn bộ `service/CartService`, `service/MomoGateway`,
`ServiceException`, 5 JSP giỏ/đơn/thanh toán, `order.css`, 4 file test, và cả `study/`.
Vì vậy nhánh Tân hiện **không có module Cart/Order/Payment** — merge vào Nam bây giờ
sẽ mất module đó nếu không giải quyết conflict đúng (mục 11).

### 12.3 Lỗi ngoài checklist vẫn còn

| Vấn đề | Vị trí | Mức |
|---|---|---|
| `pom.xml` mất block surefire loại `jakarta.json.bind-api` | `pom.xml` — không còn `maven-surefire-plugin` | **CAO** — thêm lại test là `mvn test` chết `JsonBindingProvider` |
| `css/order.css` không tồn tại nhưng `header.jspf:22` trỏ vào | `origin/Tan` không có `src/main/webapp/css/order.css` | **CAO** — mọi trang login/register/admin trắng style |
| `css/style.css` = **0 byte** | `src/main/webapp/css/style.css` | Trung bình |
| `header.jspf:39-40` vẫn `/auth?action=login\|register` | không có `AuthServlet` ở cả hai nhánh | **CAO** — link đăng nhập 404 |
| `HelloApplication` / `HelloResource` còn nguyên | `com/example/electronics_marketplace_project/` | Thấp — scaffolding `@ApplicationPath("/api")` |
| `EncodingFilter` rỗng | `public class EncodingFilter { }` | Thấp — chưa filter nào set UTF-8 |
| `db.properties` (`db.password=____`) + `mail.properties` | `src/main/resources/` | Trung bình — file mẫu, vô hại nhưng gây nhiễu |

### 12.4 Xung đột mới: `ListingStatus` (`03c057f`)

`03c057f` đổi enum trên nhánh Tân:

```java
// trước: PENDING_APPROVAL, APPROVED, REJECTED, SOLD, HIDDEN
// sau  : AVAILABLE("Đang bán"), RESERVED("Đã giữ chỗ"), SOLD("Đã bán"), HIDDEN("Đã ẩn")
```

Nhánh **Nam và Thành đều đã dùng** `AVAILABLE/RESERVED/SOLD/HIDDEN` (commit `68eb932`,
`2bc27b1`), nên đây là **Tân đuổi kịp hai nhánh kia**, không phải xung đột. Chỉ cần
lưu ý: file `THONG_BAO_DONG_BO_THANH_LE.md` trên nhánh Tân vẫn mô tả enum cũ
(`PENDING_APPROVAL`…) — tài liệu lệch code, sửa khi tiện.

### 12.5 Kết luận

Tân **đã làm đúng hợp đồng** ở mục 1–8 (10/11 checklist). Nhánh chưa merge được vì:

1. Module Cart/Order/Payment đã bị revert (`3782371`) — merge sẽ mất.
2. Không còn test nào → không có lưới an toàn.
3. `order.css` thiếu nhưng header trỏ vào → UI hỏng.
4. `header.jspf:39-40` link chết.

Cách rẻ nhất: merge `origin/Tan` vào Nam **theo đúng thứ tự mục 11**, lấy bản Nam cho
`controller/user/*`, `service/*`, `css/order.css`, `src/test/*`; giữ bản Tân cho
`filter/*`, `controller/auth/*`, `controller/admin/*`, `listener/*`. Sau merge sửa
2 dòng `_header.jspf` + thêm lại block surefire (mục 12.3).

---

## 10. Checklist của Nam sau khi merge

- [ ] `_header.jspf:39-40`: `/auth?action=login|register` → `/login|/register`.
- [ ] Xoá 4 JSP 0 B: `views/auth/login.jsp`, `views/auth/register.jsp`,
      `views/admin/dashboard.jsp`, `views/home.jsp`.
- [ ] Giữ `web.xml` của Nam (có `jsp-config` UTF-8 — cần cho `.jspf` của cả hai bên).
- [ ] Giữ `pom.xml` của Nam; xác nhận không có dependency trùng.
- [ ] Deploy lại, **gọi thử từng route** — JSP chỉ compile lúc request, `mvn package`
      không bắt được lỗi JSP (bài học `payment.jsp`).
- [ ] Đăng nhập → `GET /cart` → thêm hàng → checkout → history → payment, kiểm tra
      `${currentUser.name}` hiện đúng.

## 11. Thứ tự merge đề xuất

```
1. Tân sửa xong checklist mục 9, push origin/Tan
2. Nam commit nốt phần đang dở (CartService, ServiceException, test)
3. Merge origin/Tan vào Nam, giải quyết:
     User.java                      -> bản Nam + getter của Tân
     persistence.xml                -> bản Nam
     web.xml                        -> bản Nam
     pom.xml                        -> bản Nam
     index.jsp                      -> bản Nam (giữ <c:redirect>, đổi /cart -> /home khi có HomeServlet)
     controller/user/*.java         -> bản Nam
     repository/UserRepository.java -> bản Nam (rỗng)
     entity/enums/Status.java       -> xoá
     DBConnection/DBConnection.java -> xoá
     DBConnection/DatabaseInitializer.java -> xoá
     filter/*.java                  -> bản Tân (sau khi sửa mục 1+2)
     controller/auth/*, admin/*     -> bản Tân (sau khi sửa mục 1+3)
4. Nam sửa 2 link ở _header.jspf, xoá 4 JSP 0 B
5. mvn -o clean test -> phải 7/7 xanh
6. Deploy Tomcat, gọi thử route (đừng chỉ tin build)
```
