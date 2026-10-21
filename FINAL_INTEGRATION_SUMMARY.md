# Tổng Hợp Tích Hợp — Module Repair (Quốc)

> **Người thực hiện:** Quốc
> **Cập nhật:** 19/10/2026 — sau khi rebase lên `origin/Tan` (commit `2b9e8d9`)
> **Nhánh:** `Quoc` (đã rebase, đã sửa P1–P3)
> **Trạng thái:** Compile 0 lỗi (loại `controller/public`), 18/18 test pass, 9 JSP compile 0 lỗi

---

## PHẦN 0 — TRẠNG THÁI NHÁNH

| Nhánh | Commit | Ghi chú |
|---|---|---|
| `master` | `9072b00` | Chưa ai merge |
| `Quoc` | rebase trên `origin/Tan` | **Đã rebase + sửa P1–P3** |
| `Tan` | `2b9e8d9 Merge branch 'Nam'` | Auth/User/Filter + JpaConfig |
| `Thanh` | `333df71` | Listing/Marketplace |
| `Nam` | `3664f24` | Order/Payment |
| `Anh` | `ca687c6` | Product |

**Quan trọng:** nhánh `Quoc` giờ **dựa trên `origin/Tan`**, không còn dựa trên `master`.
Nhờ vậy có `User`, `AuthenticationFilter`, `JpaConfig` thật.

---

## PHẦN 1 — NHỮNG GÌ ĐÃ SỬA TRONG LẦN NÀY

### P1. `JpaUtil` — chuyển sang dùng `JpaConfig` ✅

**Trước:**
```java
// Tự tạo EMF, không truyền cấu hình kết nối
local = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
```

**Sau:**
```java
public static EntityManagerFactory getEntityManagerFactory() {
    return JpaConfig.getEntityManagerFactory();   // dùng chung EMF của toàn app
}
```

**Vì sao bắt buộc:** `persistence.xml` trên nhánh Tan dùng placeholder `${DB_URL}`.
Hibernate 6.4.4 **không** resolve placeholder này khi tạo EMF trực tiếp. Tôi đã kiểm chứng
bằng cách chạy thật:

```
RESULT: FAILED -> ServiceException: Unable to create requested service
[org.hibernate.engine.jdbc.env.spi.JdbcEnvironment] due to:
Unable to determine Dialect without JDBC metadata
(please set 'jakarta.persistence.jdbc.url' ...)
```

`JpaConfig` truyền `DB_URL`/`DB_USER`/`DB_PASSWORD` qua override map → EMF tạo được.

**Lợi ích phụ:** cả hai module dùng **một** EntityManagerFactory và **một** connection pool.

### P2. Bỏ `AppLifecycleListener` — dùng `AppShutdownListener` của Tân ✅

**Đã xóa:** `src/main/java/com/electronics/marketplace/config/AppLifecycleListener.java`

**Vì sao:** Tân đã có `listener/AppShutdownListener.java`. Giữ cả hai → hai listener
cùng đóng EMF → double close.

`JpaUtil.shutdown()` giờ là no-op, có comment giải thích.

### P3. `DBConnection` và `DatabaseInitializer` ✅

Rebase tự động loại bỏ hai file này vì Tân đã xóa chúng. Không cần làm gì thêm.

`DBConnection/` hiện chỉ còn:
```
AuthDB.java    (Tân)
DBUtil.java    (Tân — gọi JpaConfig.getEntityManagerFactory())
UserDB.java    (Tân)
```

### P9 (mới phát hiện). Thiếu Gson trong `pom.xml` ✅

**Vấn đề:** Tân đã bỏ dependency Gson khỏi `pom.xml`, nhưng `RepairRequestServlet`
dùng `Gson` để trả JSON cho `/repair/api/estimate`. Compile fail:

```
error: package com.google.gson does not exist
```

**Đã sửa:** thêm lại vào `pom.xml`:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

**Vì sao chọn thêm Gson thay vì viết JSON thủ công:** `EstimateResultDTO` có 15 field
(6 kiểu `BigDecimal`, 3 `Integer`, 2 `Long`, 2 `String`, 1 `boolean`). Viết tay dễ sai
định dạng số và phải escape chuỗi. Gson chỉ dùng ở **một** file nên rủi ro thấp.

### P6. `persistence.xml` — giữ danh sách `<class>` ✅

**Xung đột khi rebase:** Tân bỏ toàn bộ `<class>` để Hibernate tự quét; Quốc liệt kê tường minh.

**Đã giải quyết:** giữ **cả hai** cấu hình của Tân (`${DB_URL}` + `adminPU`) **và**
danh sách `<class>` của Quốc:

```xml
<persistence-unit name="Electronics_MarketplacePU" ...>
    <class>com.electronics.marketplace.entity.User</class>
    <class>com.electronics.marketplace.entity.Device</class>
    <class>com.electronics.marketplace.entity.RepairProblem</class>
    <class>com.electronics.marketplace.entity.RepairPricing</class>
    <class>com.electronics.marketplace.entity.RepairRequest</class>
    <class>com.electronics.marketplace.entity.RepairEstimate</class>
    <exclude-unlisted-classes>true</exclude-unlisted-classes>
    <property name="jakarta.persistence.jdbc.url" value="${DB_URL}"/>
    ...
</persistence-unit>

<persistence-unit name="adminPU" ...>   <!-- giữ nguyên của Tân -->
```

**Vì sao giữ `<class>`:** khi Thanh/Anh implement `Listing`, `Product`, `Order`... và thêm
vào classpath, Hibernate tự quét sẽ map **tất cả** — kể cả entity chưa hoàn chỉnh.
Liệt kê tường minh giữ schema ổn định.

**Việc cần làm khi thêm entity:** thêm một dòng `<class>` tương ứng.

---

## PHẦN 2 — CÒN LẠI GÌ

### 🟡 P4. Xung đột entity `Device` (chưa giải quyết — cần họp nhóm)

| Nhánh | Trường |
|---|---|
| **Quốc** | `brand`, **`series`**, `model`, `releaseYear`, `cpu`, `ram`, `storage` |
| **Anh** | **`name`**, `brand`, `model`, `releaseYear`, `cpu`, `ram`, `storage`, **`deviceType`** |
| **Thanh** | **`name`**, `brand`, `model`, `releaseYear`, `cpu`, `ram`, `storage` |

**Xung đột cụ thể:**
- `series` chỉ có ở Quốc
- `name` và `deviceType` chỉ có ở Anh/Thanh
- `getDisplayName()` của Quốc dựa trên `brand + series + model` → không chạy trên bản Anh/Thanh

**Đề xuất:** giữ **hợp** các trường:
```java
id, brand, series, model, name, releaseYear, cpu, ram, storage, deviceType
```
`getDisplayName()` fallback: dùng `name` nếu có, ngược lại ghép `brand + series + model`.

**Chưa sửa** vì đây là quyết định của cả nhóm, không phải của riêng Quốc.

### 🟡 P5. `RepairProblem` — Quốc đúng, Thanh thiếu `@Column` ✅ (không cần sửa)

| Nhánh | Khai báo khóa chính |
|---|---|
| **Quốc** | `@Id @Column(name = "problem_id") private Long id;` ✅ |
| **Thanh** | `@Id private Long id;` ❌ → Hibernate map cột `id` |

Bản Quốc khớp script SQL (`problem_id BIGSERIAL`). Khi merge cần giữ bản Quốc.

### 🟡 P7. `index.jsp` — vẫn là redirect ✅ (chưa sửa)

Nhánh Tan vẫn `<c:redirect url="/login"/>`. Bản Quốc (trang chủ thật) **đã bị mất khi rebase**
vì file thuộc Tân và Tân chưa có thay đổi đó.

**Đề xuất:** nếu muốn khách vào được `/repair` từ trang chủ, cần Tân đồng ý sửa `index.jsp`.

### 🟡 P8. `web.xml` — error-page trỏ file không tồn tại (thuộc Tân)

```xml
<error-page><error-code>404</error-code>
    <location>/WEB-INF/views/error/404.jsp</location></error-page>
<error-page><error-code>500</error-code>
    <location>/WEB-INF/views/error/500.jsp</location></error-page>
```

Cả 2 file **không tồn tại** trên nhánh Tan. Khi có lỗi 404/500 sẽ lỗi lồng nhau.
Cần Tân tạo 2 file hoặc bỏ khai báo.

### 🟡 P10. `db.properties` — file mồ côi

File vẫn tồn tại nhưng **không ai đọc** (Tân chuyển sang `JpaConfig` + biến môi trường).
Không gây lỗi, chỉ gây nhầm lẫn. Đề xuất xóa hoặc ghi chú rõ trong file.

---

## PHẦN 3 — BLOCKER VẪN CÒN

### 🔴 `controller.public` — thuộc Phước Anh + Thành

```
src/main/java/com/electronics/marketplace/controller/public/ProductServlet.java
src/main/java/com/electronics/marketplace/controller/public/ListingServlet.java
```

`public` là từ khóa Java → `mvn clean compile` fail:
```
[ERROR] ProductServlet.java:[1,48] <identifier> expected
```

**Chưa sửa trên bất kỳ nhánh nào** — kể cả `origin/Thanh` và `origin/Anh`.

---

## PHẦN 4 — KIỂM CHỨNG SAU REBASE

| Hạng mục | Kết quả |
|---|---|
| Java compile (loại `controller/public`) | ✅ **0 lỗi** |
| `RepairServiceTest` | ✅ **18/18 pass** |
| Jasper compile 9 JSP | ✅ **0 lỗi** |
| Gson resolve | ✅ Có trong classpath |
| `JpaUtil` → `JpaConfig` | ✅ Đã chuyển |
| `AppLifecycleListener` | ✅ Đã xóa |
| `DBConnection.java` / `DatabaseInitializer.java` | ✅ Không còn |
| Conflict `persistence.xml` | ✅ Đã giải quyết |

### Chưa kiểm chứng

**Chưa chạy Tomcat thật.** PostgreSQL trên máy chỉ cài `pgAgent`, không có
`pg_ctl.exe` / `postgres.exe`. Không có DB để kết nối.

Để test thật:
1. Cài PostgreSQL server
2. Tạo database (hoặc để `AdminSeedListener` tự tạo)
3. Đặt biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD`
4. Chạy `schema_and_seed_repair.sql`
5. Deploy WAR lên Tomcat 11

---

## PHẦN 5 — NHỮNG GÌ QUỐC ĐÃ HOÀN THÀNH

### Backend

| Thành phần | File |
|---|---|
| Entity | `Device`, `RepairProblem`, `RepairPricing`, `RepairRequest`, `RepairEstimate` |
| Enum | `entity/enums/RepairStatus.java` (6 trạng thái) |
| Repository | `DeviceRepositoryImpl`, `RepairRepositoryImpl` |
| Service | `RepairService` (479 dòng) |
| Servlet | `RepairRequestServlet`, `AdminRepairServlet` |
| DTO | `EstimateResultDTO`, `ProblemStatisticDTO` |
| SQL | `database/schema_and_seed_repair.sql` (idempotent) |

### UI — 9 màn hình

| Màn hình | File |
|---|---|
| Repair Request | `views/repair/request_form.jsp` |
| Repair Estimate | `views/repair/estimate.jsp` |
| Tra cứu tiến độ | `views/repair/track.jsp` |
| Quản lý đơn | `views/admin/repair_requests.jsp` |
| Dòng máy | `views/admin/device_list.jsp` |
| Loại lỗi | `views/admin/problem_list.jsp` |
| Bảng giá | `views/admin/pricing_list.jsp` |
| Thống kê | `views/admin/report.jsp` |
| Fragment | `_header.jspf` ×2, `_footer.jspf` |

Design system: `css/repair.css` (633 dòng).

---

## PHẦN 6 — VIỆC CẦN LÀM TIẾP

| # | Việc | Người | Mức độ |
|---|---|---|---|
| 1 | Đổi package `controller.public` | Phước Anh + Thành | 🔴 Chặn build |
| 2 | Chốt trường `Device` (`series` vs `name`) | Quốc + Anh + Thanh | 🟡 Xung đột entity |
| 3 | Tạo `error/404.jsp`, `error/500.jsp` | Tân | 🟡 web.xml trỏ file không tồn tại |
| 4 | Quyết định `index.jsp` có làm trang chủ không | Tân | 🟡 Khách khó vào `/repair` |
| 5 | Xóa hoặc ghi chú `db.properties` | Tân | 🟢 File mồ côi |
| 6 | Thêm `<class>` khi implement entity mới | Tất cả | 🟡 Tránh schema trôi |

---

## PHẦN 7 — LƯU Ý KỸ THUẬT

### Cách chạy local

```bash
export DB_URL="jdbc:postgresql://localhost:5432/electronics_marketplace"
export DB_USER="postgres"
export DB_PASSWORD="<mat-khau-cua-ban>"
mvn clean package
# deploy target/*.war lên Tomcat 11
```

`JpaConfig` **fail fast**: thiếu biến môi trường sẽ báo ngay
`IllegalStateException: Thieu bien moi truong DB_URL...` thay vì lỗi Hibernate khó hiểu.

### Tài liệu phối hợp

- `PHOI-HOP-THANH-QUOC.md` (nhánh `origin/Thanh`):
  - Quốc quản `RepairProblem`, Thành dùng `@ManyToOne` trỏ tới
  - Quốc **không được xóa cứng** `RepairProblem`, chỉ `isActive = false`
  - Quốc không tạo `Device` riêng, dùng chung với Thành
- `Dieu_chinh_19_10_2026.md` (nhánh `origin/Tan`): điều chỉnh ngày 19/10 của Tân
