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

> **Trạng thái 2026-10-19 @ `333df71`: xong 1/7 — xem [mục 11.2](#112-8--7-việc-thành-tự-sửa-xong-17).**

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
1. Thành xoá 7 chỗ `new User(...)` 5 tham số (mục 11.3) → push
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

## 11. Kết quả kiểm tra nhánh Thành — 2026-10-19 @ `origin/Thanh` `333df71`

### 11.0 Đọc diff cho đúng trước đã

`origin/Thanh` tách từ `9072b00` — **trước** toàn bộ module của Nam. Nên
`git diff origin/Nam origin/Thanh` báo *100 file, −12.025 dòng* thực chất là
"file Nam có mà Thành chưa từng có", **không phải** Thành xoá. Diff thật của
Thành là `9072b00..333df71`: **41 file, +5.867 dòng**, 3 commit
(`9072b00` → `2bc27b1` → `333df71`).

### 11.1 §1–§7 — file dùng chung

| Mục | Kết quả | Bằng chứng |
|---|---|---|
| §1 `User` dùng enum | ❌ | vẫn `private String role` / `private String status`; ctor 5 tham số còn ở `User.java:53` |
| §2 `OrderItem` quan hệ | 🟡 | `OrderItem.java` bên Thành là class **rỗng** (73 byte) → merge lấy bản Nam, không conflict |
| §3 `persistence.xml` | ❌ | vẫn `postgres`/`postgres`, db `electronics_marketplace`, 6 `<class>` + `exclude-unlisted-classes=false` |
| §4 `web.xml` bỏ `<taglib>` | ❌ | vẫn trỏ `/WEB-INF/tld/c.tld` + `fmt.tld`; thiếu `page-encoding`, `session-config`, `error-page` |
| §5 `pom.xml` | 🟡 | Java 17 ✅ nhưng không có `<release>`; còn `jakarta.persistence-api:3.1.0` + `jstl-api:3.0.2`; **0 dependency Hibernate** |
| §6 `WEB-INF/lib/*.jar` | ❌ | vẫn commit 4 jar (`el-api`, `persistence-api`, `jstl`, `jstl-api`) |
| §7 `index.jsp` bỏ scriptlet | ✅ | `<c:redirect url="/home" />` |

### 11.2 §8 — 7 việc Thành tự sửa: xong 1/7

| # | Việc | Kết quả |
|---|---|---|
| 1 | Test `int` → `Long` | 🟡 literal đã sửa (`1L`, `999L`, `10L`) nhưng `new User(...)` vẫn 5 tham số ở `MarketplaceServiceTest:58,68,98` |
| 2 | `ProductServlet` rỗng | ❌ vẫn 367 byte, thân class rỗng |
| 3 | `?demo=true` bỏ BR-33 | ❌ còn ở `ListingServlet:122` |
| 4 | `currentUserId … : 1L` | ❌ còn ở `ListingServlet:213` (`// Fallback demo seller id`) |
| 5 | JSON ghép `String.format` | ❌ còn ở `ListingServlet:142` |
| 6 | EL raw trong JSP | ❌ `listing/detail.jsp:282,302,306,309,321`; `listing/form.jsp:161,167,184,188,192,296` |
| 7 | Không commit `hs_err_pid*.log` | ✅ sạch |

### 11.3 Hai phát hiện ngoài danh sách trên

1. **Nhánh Thành không có tầng persistence nào chạy được.** `JpaConfig.java`
   là class **rỗng** (73 byte), `ListingRepositoryImpl` là `ConcurrentHashMap`
   in-memory, **0** tham chiếu `EntityManager`/`createQuery`; `pom.xml` không có
   Hibernate. "10/10 test xanh" chỉ chứng minh đường in-memory —
   `persistence.xml` ở đó không được dùng tới.
2. **Merge xong sẽ đỏ ở compile, không phải ở assert.** 7 chỗ gọi
   `new User(Long, String, String, String, String)` còn lại:
   `ListingRepositoryImpl:29,32,35`, `MarketplaceService:104`,
   `MarketplaceServiceTest:58,68,98`. `User.java` của Nam **chỉ có** ctor
   không tham số (`User.java:83`).

Phụ: `AuthenticationFilter` rỗng (84 byte), `EncodingFilter` rỗng, còn sót
`com/example/electronics_marketplace_project/{HelloApplication,HelloResource}`.
`Cart`/`Order`/`Payment` bên Thành cũng là stub rỗng (68–71 byte) — merge lấy
bản Nam nên không ảnh hưởng.

### 11.4 Kết luận

Thành mới xong **1/7** việc trong §8 và **0/4** mục file dùng chung (§3–§6);
chỉ `index.jsp` đã sửa đúng. Chính `PHOI-HOP-NAM-THANH.md` v3.0 của Thành ghi
mục 06–08 là *"ĐÃ ĐỒNG THUẬN — sẽ lấy bản của Nam khi merge"* → Thành chủ động
hoãn, không phải bỏ sót.

Thứ tự merge ở §10 vẫn giữ nguyên, nhưng **thêm một bước trước bước 1**:
xoá 7 dòng `new User(...)` 5 tham số ở trên (đổi sang `new User()` + setter
như §1), nếu không merge xong là đỏ compile.
