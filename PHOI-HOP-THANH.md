# Đồng bộ nhánh Nam ↔ Thanh — đọc trước khi sửa file chung

> Viết 2026-10-19, sau khi review `origin/Thanh` @ `2bc27b1`.
> Mục tiêu: hai bên sửa song song mà không conflict, không phá build của nhau.

## TL;DR — 6 nhóm file không được "mỗi người một bản"

| File | Chủ sở hữu | Bên kia làm gì |
|---|---|---|
| `entity/User.java` | **Nam** (đã có `Role`/`UserStatus` enum) | Thêm field mới bằng setter, **không** đổi kiểu `role`/`status` |
| `entity/OrderItem.java` | **Nam** (đã đổi sang `@ManyToOne Listing`) | Chỉ đọc `getListing()`, không đổi field |
| `entity/Listing.java`, `Device`, `ListingImage`, `ListingProblem`, `RepairProblem`, `enums/ListingCondition`, `enums/ListingStatus` | **Thành** | Nam chỉ sửa khi có lý do + báo trước |
| `src/main/resources/META-INF/persistence.xml` | **Nam** (env var + `JpaConfig`) | Thêm entity mới **không cần** sửa file này |
| `src/main/webapp/WEB-INF/web.xml` | **Nam** | Không thêm `<taglib>` — JSTL jar đã tự khai báo |
| `pom.xml` | **Nam** | Không thêm dependency trùng |

## 1. `User` — dùng enum, không dùng String

`origin/Thanh` đang khai `String role` / `String status` + constructor
`User(Long, String name, String email, String phone, String location)`.

Nhánh Nam đã chốt:

```java
@Enumerated(EnumType.STRING) private Role role;
@Enumerated(EnumType.STRING) private UserStatus status = UserStatus.ACTIVE;
```

**Việc bên Thành phải sửa** (3 chỗ trong `ListingRepositoryImpl`, 1 chỗ trong
`MarketplaceService`):

```java
// ListingRepositoryImpl.java:29,32,35 và MarketplaceService.java:104
new User(1L, "Trần Minh Đức", "duc.tran@gmail.com", "0912 888 999", "Quận 1, TP. Hồ Chí Minh");
```

Constructor 5 tham số đó **không còn tồn tại**. Thay bằng:

```java
User seller = new User();
seller.setId(1L);                     // id chỉ có nghĩa khi seed in-memory
seller.setName("Trần Minh Đức");
seller.setEmail("duc.tran@gmail.com");
seller.setPhone("0912 888 999");
seller.setLocation("Quận 1, TP. Hồ Chí Minh");
seller.setRole(Role.USER);
seller.setStatus(UserStatus.ACTIVE);
```

Lý do: `User` đã là `@Entity` map xuống bảng `users` thật, và `EntityMappingTest`
của Nam ghi/đọc `Role.USER` / `UserStatus.ACTIVE`. Giữ String thì test không compile
và cột `role` sẽ lệch kiểu giữa hai nhánh.

## 2. `OrderItem.listing` — Nam đã đổi sang quan hệ

Đã lấy entity của Thành, nên `OrderItem` giờ là:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "listing_id")
private Listing listing;      // thay cho Long listingId
```

Cột DB vẫn tên `listing_id` — **không cần migrate**. Ai đang gọi `getListingId()`
thì đổi thành `getListing().getId()`.

## 3. `persistence.xml` — đừng commit bản hardcode credentials

Bản của Thành hardcode `postgres/postgres`, db `electronics_marketplace`, và có
`<class>` list + `<exclude-unlisted-classes>false</exclude-unlisted-classes>`.

Bản của Nam:
- đọc `${DB_URL}` / `${DB_USER}` / `${DB_PASSWORD}` (nạp qua `JpaConfig`)
- **không** khai `<class>` → Hibernate tự quét classpath

Hệ quả: Thành thêm entity mới **không phải sửa** `persistence.xml`. Nếu lấy bản
của Thành, danh sách `<class>` thiếu `Cart`, `Order`, `OrderItem`, `Payment`,
`Product` → Hibernate bỏ qua và module của Nam chết.

## 4. `web.xml` — không khai lại `<taglib>`

Bản của Thành trỏ `/WEB-INF/tld/c.tld` + `fmt.tld`. Không cần:

- JSTL 3.0.1 (đã có trong `pom.xml`) tự khai báo TLD với URI
  `jakarta.tags.core` / `jakarta.tags.fmt` / `jakarta.tags.functions`
- Thành đang thiếu `fn.tld` nhưng JSP của Nam dùng `jakarta.tags.functions`

Bản của Nam có thêm: `page-encoding UTF-8` cho `.jsp/.jspf`, session cookie
`HttpOnly`, error-page 404/500. Merge mà lấy bản của Thành là **mất hết**.

## 5. `pom.xml` — 3 dependency không được thêm

| Dependency | Lý do |
|---|---|
| `jakarta.persistence-api:3.1.0` | `hibernate-core:6.4.4` đã kéo vào; thêm nữa là trùng |
| `jakarta.servlet.jsp.jstl-api:3.0.2` | Nam đang dùng 3.0.0; nâng 3.0.2 không cần thiết |
| `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1` | Nam **đã có** y hệt |

Ngoài ra `maven.compiler.source/target`: nhánh Nam đang để **8**, Thành để **17**.
Code Thành chỉ dùng lambda + diamond (Java 8 là đủ) nhưng cả nhóm nên chốt một
mức. Đề xuất: **17** (JDK đang chạy là 17.0.20) — nhưng phải sửa ở một commit
riêng để không lẫn vào merge.

> **Đã chốt 2026-10-19:** nhánh Nam nâng lên **17** (`source`/`target` = 17, cùng
> kiểu với Thành) trong commit riêng, đúng như đề xuất trên. Mục này giữ lại làm
> lịch sử; không cần làm gì thêm.

## 6. `WEB-INF/lib/*.jar` — không commit

Thành commit 4 jar (`jakarta.el-api`, `jakarta.persistence-api`, `jstl`,
`jstl-api`) vào `WEB-INF/lib`. Maven đã đóng gói các jar này vào WAR. Hai nguồn
cùng tên class ⇒ dễ nạp nhầm version khi Tomcat deploy. Xoá, giữ `pom.xml` làm
nguồn duy nhất.

## 7. `index.jsp` — không dùng scriptlet

Bản của Thành:

```jsp
<% response.sendRedirect(request.getContextPath() + "/home"); %>
```

Vi phạm rule "no scriptlet". Bản của Nam dùng `<c:redirect url="/cart" />`.
Khi merge: giữ `<c:redirect>`, chỉ đổi `url` thành `/home` nếu `HomeServlet` đã
được merge (nó nằm trong nhánh Thành nên sau merge sẽ có).

## 8. Những chỗ Thành tự sửa trước khi merge

- [ ] `MarketplaceServiceTest`: 8 chỗ truyền `int` vào tham số `Long` (dòng 58, 60,
      68, 70, 87, 95, 98, 99) → thêm hậu tố `L`, và `new User(...)` đổi theo mục 1.
- [ ] `ProductServlet` đang rỗng nhưng JSP link `/product/list` → 404.
- [ ] `?demo=true` bỏ qua BR-33 ở `ListingServlet:123` — phải xoá trước khi demo.
- [ ] `currentUserId = ... : 1L` ở `ListingServlet:213` — anonymous sửa được tin
      của seller 1 (vi phạm BR-11).
- [ ] JSON ở `ListingServlet` ghép bằng `String.format` không escape → tên có `"`
      là vỡ response.
- [ ] EL raw trong `listing/detail.jsp` (tên seller, note lỗi, `data-phone`) và
      `listing/form.jsp` (brand/model/cpu/ram/storage trong `value=""`) → dùng
      `<c:out>`.
- [ ] Không commit `hs_err_pid*.log`.

## 9. Quy ước tránh conflict về sau

1. **Một file, một chủ.** File chung (`pom.xml`, `web.xml`, `persistence.xml`,
   `User.java`, `OrderItem.java`) chỉ một người sửa; người kia gửi yêu cầu.
2. **Entity mới thì tự thêm file**, không sửa file của người khác.
   `persistence.xml` tự quét nên không cần đăng ký.
3. **Không format lại cả file** khi chỉ sửa vài dòng — khuếch đại conflict.
4. **Chạy `mvn -o test` trước khi push.** Nhánh nào làm đỏ test là nhánh đó sửa.
5. **Không commit file sinh ra**: `target/`, `*.log`, `hs_err_pid*`, `WEB-INF/lib/*.jar`.

## 10. Thứ tự merge đề xuất

```
1. Thành sửa 8 lỗi test + User constructor        → push
2. Nam commit phần đang làm dở (entity JPA, web.xml, persistence.xml)
3. Merge origin/Thanh vào Nam, giải quyết:
     .gitignore                  → lấy của Thành (có *.log, Thumbs.db)
     persistence.xml             → lấy của Nam
     pom.xml                     → lấy của Nam (bỏ 3 dep trùng)
     web.xml                     → lấy của Nam
     index.jsp                   → lấy của Nam, đổi url="/home"
     User.java, OrderItem.java   → lấy của Nam
4. mvn -o test → phải 3/3 xanh
5. Xoá WEB-INF/lib/*.jar, chạy lại build
```
