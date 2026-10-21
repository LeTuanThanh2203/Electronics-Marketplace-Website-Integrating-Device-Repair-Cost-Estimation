# 📋 Báo cáo Review Trước Merge — Electronics Marketplace

> **Ngày kiểm tra:** 2026-10-19  
> **Branch:** Tân (origin/Tan)  
> **Mục đích:** Xác nhận trạng thái codebase trước khi push để merge với branch Nam

---

## 1. Tổng quan kết quả

```
 Đạt:        11 / 14 mục
 Cần sửa:     1 mục   (Tân — xoá 2 file scaffolding)
 Chưa chạy:   1 mục   (mvn -o test)
 Chờ merge:   2 mục   (việc của Nam sau merge)
```

---

## 2. File đã xoá thành công

| File | Lý do xoá | Trạng thái |
|------|-----------|-----------|
| `entity/enums/Status.java` | Trùng với `UserStatus.java` | ✅ Đã xoá |
| `DBConnection/DBConnection.java` | JDBC cũ, thay bằng JPA | ✅ Đã xoá |
| `DBConnection/DatabaseInitializer.java` | Không cần, Hibernate tự tạo bảng | ✅ Đã xoá |
| `hs_err_pid*.log` | JVM crash dump | ✅ Không có |

---

## 3. File CẦN XOÁ — chưa xoá

> [!CAUTION]
> 2 file scaffolding của JAX-RS vẫn còn trong source:

| File | Nội dung | Hành động |
|------|----------|----------|
| `com/example/electronics_marketplace_project/HelloApplication.java` | `@ApplicationPath("/api")` — scaffolding JAX-RS, không ai dùng | **XOÁ** |
| `com/example/electronics_marketplace_project/HelloResource.java` | `@Path("/hello-world")` — endpoint test, không ai dùng | **XOÁ** |

Đường dẫn đầy đủ:
```
src/main/java/com/example/electronics_marketplace_project/HelloApplication.java
src/main/java/com/example/electronics_marketplace_project/HelloResource.java
```

Sau khi xoá 2 file, thư mục `com/example/` sẽ rỗng — xoá luôn cả thư mục.

---

## 4. Checklist mục 9 (Tân) — Chi tiết

### 9.1 ✅ `"user"` → `"currentUser"`

Không còn session attribute `"user"` trong toàn bộ source. Đã thống nhất dùng `"currentUser"`:

| File | Dòng | Vai trò |
|------|------|---------|
| `AuthenticationFilter.java` | 14 | `SESSION_KEY = "currentUser"` (hằng số dùng chung) |
| `header.jspf` | 12 | `${sessionScope.currentUser}` |

### 9.2 ✅ AuthenticationFilter phủ đủ route + AJAX 401 JSON

```java
@WebFilter(urlPatterns = {"/profile", "/admin", "/admin/*",
                          "/checkout", "/order", "/payment", "/cart"})
```

- AJAX: `HttpUtil.wantsJson(req)` → trả `401 JSON`, không redirect ✅
- Ngoại lệ hợp lý: `GET /cart` không có `action` → cho phép khách chưa đăng nhập ✅

### 9.3 ✅ User.java — helper methods + lifecycle callbacks

```java
// L185-188: Convenience getters cho JSP EL
public boolean isAdmin()     { return role == Role.ADMIN; }
public boolean isActive()    { return status == UserStatus.ACTIVE; }
public boolean isSuspended() { return status == UserStatus.SUSPENDED; }
public boolean isDeleted()   { return status == UserStatus.DELETED; }

// L190-192: getInitial — KHÔNG dùng isBlank()
public String getInitial() {
    return (name == null || name.trim().isEmpty()) ? "?" : name.trim().substring(0, 1).toUpperCase();
}

// L194-204: JPA lifecycle
@PrePersist void onCreate() { ... }
@PreUpdate  void onUpdate() { ... }
```

### 9.4 ✅ `getUserID()` → `getId()`, `int` → `Long`

- `User.java`: field `private Long id` + `getId()` / `setId(Long)` ✅
- `UserDB.java`: `insert()` trả `Long`, `selectById(Long)`, `updatePassword(Long, ...)`, `updateStatus(Long, ...)` ✅
- Không còn `getUserID()` trong toàn bộ source ✅

### 9.5 ✅ Xoá `enums/Status.java`, import `UserStatus`

- File `Status.java` không tồn tại ✅
- Không file nào import `enums.Status` ✅
- Tất cả dùng `enums.UserStatus` ✅

### 9.6 ✅ DBUtil lấy EMF từ JpaConfig — Không còn Hibernate Session / JDBC

```java
// DBUtil.java:12-13
private static EntityManager em() {
    return JpaConfig.getEntityManagerFactory().createEntityManager();
}
```

| Kiểm tra | Kết quả |
|----------|---------|
| `import org.hibernate.Session` | ✅ Không có |
| `import java.sql.*` | ✅ Không có |
| `DBConnection.java` (class JDBC cũ) | ✅ Đã xoá |
| `DatabaseInitializer.java` | ✅ Đã xoá |

> [!NOTE]
> Package tên `DBConnection/` (viết hoa) vẫn còn chứa `DBUtil.java`, `UserDB.java`, `AuthDB.java`. Đây là vấn đề naming convention, không ảnh hưởng chức năng — cân nhắc đổi tên package về `dao` hoặc `repository` trong sprint sau.

### 9.7 ✅ persistence.xml sạch

- Không có `<class>murach.business.User</class>` ✅
- Không có `exclude-unlisted-classes=true` ✅
- Comment giải thích: *"Khong khai bao \<class\> va khong bat exclude-unlisted-classes: de Hibernate tu quet @Entity trong classpath"*

### 9.8 ✅ Bỏ ADMIN_EMAIL / ADMIN_PASSWORD hardcode

`AdminSeedListener.java` đọc từ biến môi trường:
```java
String email = System.getenv("ADMIN_EMAIL");
String password = System.getenv("ADMIN_PASSWORD");
```
Không có giá trị hardcode trong source ✅

### 9.9 ✅ Thay `isBlank()` 

Tất cả 4 chỗ đã được thay bằng `trim().isEmpty()`:

| File | Dòng | Đã sửa |
|------|------|--------|
| `User.java` → `getInitial()` | 191 | `name.trim().isEmpty()` ✅ |
| `AdminSeedListener.java` | 19 | `email.trim().isEmpty()` ✅ |
| `MailUtilGmail.java` | 35 | `v.trim().isEmpty()` ✅ |
| `SQLUtil.java` | 10 | `s.trim().isEmpty()` ✅ |

### 9.10 ⏳ `mvn -o test` — Chưa chạy

> [!IMPORTANT]
> Cần chạy `mvn -o test` và xác nhận **7/7 test xanh** trước khi push.

### 9.11 ✅ `.gitignore` đã bổ sung

Đã thêm 3 rule vào `.gitignore`:

```gitignore
### Project ###
*.log
hs_err_pid*
**/WEB-INF/lib/*.jar
```

| Item | Trạng thái |
|------|-----------|
| `target/` | ✅ Đã có từ trước (dòng 1) |
| `*.log` | ✅ Đã bổ sung |
| `hs_err_pid*` | ✅ Đã bổ sung |
| `WEB-INF/lib/*.jar` | ✅ Đã bổ sung |
| Trên disk có file vi phạm? | ✅ Không có |

---

## 5. Checklist mục 10 (Nam — sau merge)

> [!NOTE]
> Các mục dưới đây là việc của **Nam sau khi merge**, liệt kê ở đây để reviewer biết trước.

### 10.1 ⏳ `header.jspf:39-40` — Link đăng nhập/đăng ký

Hiện tại:
```html
<a href="${ctx}/auth?action=login" ...>Đăng nhập</a>
<a href="${ctx}/auth?action=register" ...>Đăng ký</a>
```

Cần đổi thành:
```html
<a href="${ctx}/login" ...>Đăng nhập</a>
<a href="${ctx}/register" ...>Đăng ký</a>
```

### 10.2 ⏳ Xoá 4 JSP rỗng (0 byte)

| File | Kích thước |
|------|-----------|
| `WEB-INF/views/auth/login.jsp` | 0 B — xoá |
| `WEB-INF/views/auth/register.jsp` | 0 B — xoá |
| `WEB-INF/views/admin/dashboard.jsp` | 0 B — xoá |
| `WEB-INF/views/home.jsp` | 0 B — xoá |

> Ngoài ra còn 11 JSP rỗng khác là placeholder chờ implement (cart, listing, order, product, repair) — **không xoá**, cần điền nội dung.

### 10.3 ✅ `web.xml` đã có `jsp-config` UTF-8

```xml
<jsp-config>
    <jsp-property-group>
        <url-pattern>*.jsp</url-pattern>
        <url-pattern>*.jspf</url-pattern>
        <page-encoding>UTF-8</page-encoding>
    </jsp-property-group>
</jsp-config>
```

---

## 6. Cấu trúc source hiện tại

```
src/main/java/com/electronics/marketplace/
├── config/
│   └── JpaConfig.java
├── controller/
│   ├── admin/    AdminServlet, AdminProductServlet, AdminRepairServlet
│   ├── auth/     LoginServlet, LogoutServlet, ProfileServlet, RegisterServlet
│   ├── public/   ListingServlet, ProductServlet
│   └── user/     CartServlet, CheckoutServlet, OrderServlet, PaymentServlet, RepairRequestServlet
├── DBConnection/
│   ├── AuthDB.java
│   ├── DBUtil.java          ← lấy EMF từ JpaConfig ✅
│   └── UserDB.java
├── entity/
│   ├── enums/   ListingStatus, OrderStatus, OrderType, PaymentMethod,
│   │            ProductStatus, Role, SpecDataType, UserStatus
│   ├── Listing, ListingImage, ListingProblem, Order, OrderItem,
│   │   Payment, Product, ProductSpecification,
│   │   RepairEstimate, RepairPricing, RepairProblem, RepairRequest, User
├── filter/      AuthenticationFilter, AuthorizationFilter, EncodingFilter, GzipFilter
├── listener/    AdminSeedListener, AppShutdownListener
├── repository/  Cart, Device, Listing, Order, Payment, Product, Repair, User
├── service/     Marketplace, Order, Repair, User
└── util/        HttpUtil, MailUtilGmail, PasswordUtil, SQLUtil, ValidationUtil

⚠️ CẦN XOÁ:
src/main/java/com/example/electronics_marketplace_project/
├── HelloApplication.java
└── HelloResource.java
```

---

## 7. Hành động còn lại

### Tân — trước khi push

| # | Việc | Ước tính |
|---|------|---------|
| 1 | Xoá `com/example/electronics_marketplace_project/HelloApplication.java` | 10s |
| 2 | Xoá `com/example/electronics_marketplace_project/HelloResource.java` | 10s |
| 3 | Xoá thư mục rỗng `com/example/` | 10s |
| 4 | Chạy `mvn -o test` — xác nhận 7/7 xanh | 1-2 phút |

### Nam — sau khi merge

| # | Việc |
|---|------|
| 1 | Đổi 2 link ở `header.jspf:39-40` |
| 2 | Xoá 4 JSP 0 B (views/auth/login, register, admin/dashboard, home) |
| 3 | Giữ `web.xml` + `pom.xml` của Nam |
| 4 | `mvn -o clean test` → 7/7 xanh |
| 5 | Deploy Tomcat, gọi thử route: login → cart → checkout → history → payment |
| 6 | Kiểm tra `${currentUser.name}` hiện đúng |

---

## 8. Thứ tự merge đề xuất

```
1. Tân xoá 2 file scaffolding, chạy mvn test, push origin/Tan
2. Nam commit nốt phần đang dở (CartService, ServiceException, test)
3. Merge origin/Tan vào Nam, giải quyết conflict:
     User.java                      → bản Nam + getter của Tân
     persistence.xml                → bản Nam
     web.xml                        → bản Nam
     pom.xml                        → bản Nam
     index.jsp                      → bản Nam
     controller/user/*.java         → bản Nam
     repository/UserRepository.java → bản Nam (rỗng)
     entity/enums/Status.java       → xoá
     DBConnection/DBConnection.java → xoá
     DBConnection/DatabaseInitializer.java → xoá
     filter/*.java                  → bản Tân
     controller/auth/*, admin/*     → bản Tân
4. Nam sửa 2 link ở header.jspf, xoá 4 JSP 0 B
5. mvn -o clean test → phải 7/7 xanh
6. Deploy Tomcat, gọi thử route (đừng chỉ tin build)
```
