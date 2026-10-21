# 📦 SỔ TAY PHỐI HỢP & XỬ LÝ CONFLICT — LÊ TUẤN THÀNH
> **Phân hệ phụ trách:** Chợ Đồ Cũ C2C (`/listing`), Trang Chủ (`/home`) & UI Core Google Stitch  
> **Thành viên:** Lê Tuấn Thành | **Nhánh Git:** `Thanh` / `integration`  
> **Trạng thái kiểm thử:** ✅ 10/10 Unit Tests PASS (`MarketplaceServiceTest`)

---

## 1. PHẠM VI SỞ HỮU FILE (RANH GIỚI TRÁCH NHIỆM)

| Tầng kiến trúc | Thành sở hữu độc quyền (Chỉ Thành sửa) | Thành viên khác giao tiếp qua |
|---|---|---|
| **Entities & Enums** | `Listing.java`, `ListingImage.java`, `ListingProblem.java`<br>Enums: `ListingStatus.java`, `ListingCondition.java` | Khóa ngoại `@ManyToOne Listing` trong `OrderItem.java` (Nam) |
| **Dùng chung** | `Device.java`, `RepairProblem.java` (Phối hợp với Quốc, Anh) | Gọi hàm getter, không tự ý xóa constructor |
| **Controller** | `ListingServlet.java`, `HomeServlet.java` | URL `/listing`, `/marketplace`, `/home` |
| **Service & Repo** | `MarketplaceService.java`, `ListingRepository.java`, `JpaListingRepository.java` | Giao tiếp qua Interface Service |
| **Giao diện & Style** | `views/home.jsp`, `views/listing/*`, `css/home.css`, `css/listing.css`, `ui_stitch.html` | Layout dùng chung, CSS Design System Google Stitch |

---

## 2. LỊCH SỬ THAY ĐỔI (CHANGELOG)

* **2026-10-19:** Sửa 8 lỗi literal int trong `MarketplaceServiceTest.java` (`1` $\to$ `1L`, `999` $\to$ `999L`) $\to$ Đạt **10/10 Test PASS**.
* **2026-10-19:** Gỡ toàn bộ scriptlet Java `<% %>` trong `index.jsp`, thay bằng `<c:redirect url="/home" />` chuẩn JSTL.
* **2026-10-20:** Thêm nút "Chốt mua" form POST sang `/order?action=create` kèm `listingId` tại `listing/detail.jsp` cho Nam.
* **2026-10-20:** Bổ sung route `/marketplace` và `action=marketplace` trong `ListingServlet.java` khớp với Header menu.
* **2026-10-21:** Hợp nhất thành công vào nhánh `integration` (Commit `333df71`), giữ vững khung UI Stitch và test suite.

---

## 3. MA TRẬN XUNG ĐỘT (CONFLICTS & GIẢI PHÁP)

<<<<<<< Updated upstream:PHOI-HOP-THANH.md
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
=======
> [!IMPORTANT]
> Dưới đây là các điểm xung đột kỹ thuật cốt lõi giữa Thành và 4 thành viên cùng cách giải quyết chuẩn:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   BẢN ĐỒ XUNG ĐỘT CỦA THÀNH                            │
├──────────────┬───────────────────────────────┬─────────────────────────────────────────┤
│ Đối tác      │ Điểm xung đột kỹ thuật        │ Giải pháp chốt & File cần sửa           │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Ca Ngọc Tân│ • Xung đột String vs Enum Role │ • Lấy Enum Role/UserStatus từ Tân.      │
│ (Auth/Admin) │ • Mất constructor User(...)    │ • BẮT BUỘC giữ constructor nạp chồng    │
│              │ • Sai link login ở Header     │   trong `User.java` để không gãy Test.  │
│              │ • Khóa duyệt tin (BR-15/19)   │ • Sửa link `header.jspf` về `/login`.   │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Huỳnh Nam │ • Chốt mua C2C (BR-20)        │ • Thành gửi POST `listingId` từ detail. │
│ (Cart/Order) │ • Tranh mua / Khóa chỗ máy    │ • Nam atomic UPDATE status sang RESERVED│
│              │ • File config chung           │ • Nam sở hữu config; Thành không ghi đè │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Phước Anh │ • Cắm AI Recom vào home.jsp   │ • Anh đẩy List sang request attribute.  │
│ (Product)    │ • Tranh chấp `Device.java`    │ • Thành duyệt render <c:forEach> ở Home │
├──────────────┼───────────────────────────────┼─────────────────────────────────────────┤
│ 👤 Vương Quốc│ • Signature constructor Device│ • Loại bỏ constructor trùng kiểu dữ liệu│
│ (Repair)     │ • Danh mục lỗi `RepairProblem`│ • Thống nhất method getName() fallback  │
└──────────────┴───────────────────────────────┴─────────────────────────────────────────┘
```

---

## 4. HƯỚNG DẪN THỰC THI — NÊN SỬA & TRÁNH SỬA FILE NÀO

### 🟢 File Thành NÊN SỬA khi cần điều chỉnh:
* `src/main/webapp/WEB-INF/views/home.jsp`: Khu vực hiển thị đề xuất máy mới và danh sách đồ cũ.
* `src/main/webapp/WEB-INF/views/listing/detail.jsp`: Nút xem số điện thoại (AJAX `/listing/contact` cần bắt đăng nhập BR-33, BR-34) và form gửi mua sang Nam.
* `src/main/java/com/electronics/marketplace/controller/marketplace/ListingServlet.java`: Logic lọc, đăng tin, xem chi tiết tin đăng.

### 🔴 File Thành TUYỆT ĐỐI KHÔNG TỰ Ý SỬA:
* `pom.xml`, `persistence.xml`, `JpaConfig.java`: Đã được tích hợp chuẩn từ Nam & Tân.
* `src/main/java/com/electronics/marketplace/entity/User.java`: Chỉ đọc, không đổi kiểu Enum thành String.
* `src/main/java/com/electronics/marketplace/entity/OrderItem.java`: Do Nam quản lý logic đơn hàng.

---

## 5. CHECKLIST KIỂM SOÁT NHANH (QUICK CHECKLIST)
- [x] Session đọc người dùng chuẩn: `(User) session.getAttribute("currentUser")`.
- [x] Chỉ hiển thị tin đồ cũ khi `isApproved == true` và `isRemovedByAdmin == false`.
- [x] Chạy `mvn test` xác nhận `MarketplaceServiceTest` đạt 10/10 PASS trước khi push.
>>>>>>> Stashed changes:phoi-hop/PHOI-HOP-THANH.md
