# Tổng Hợp Tích Hợp — Module Repair (Quốc) vs. Các Nhánh Trên GitHub

> **Người thực hiện:** Quốc
> **Ngày rà soát:** 19/10/2026
> **Đã fetch:** `origin/master`, `origin/Quoc`, `origin/Tan`, `origin/Thanh`, `origin/Nam`, `origin/Anh`
> **Mục đích:** Xác định chính xác phần Quốc đã xong chưa, cần sửa gì để khớp các nhánh khác, và còn bị chặn ở đâu.

---

## PHẦN 0 — TRẠNG THÁI CÁC NHÁNH (đã fetch ngày 19/10/2026)

| Nhánh | Commit mới nhất | Nội dung |
|---|---|---|
| `master` | `9072b00 Set up project` | **Chưa ai merge** — vẫn là commit gốc |
| `Quoc` | `9072b00 Set up project` | **Chưa push gì** — vẫn là commit gốc |
| `Tan` | `2b9e8d9 Merge branch 'Nam' into Tan` | Auth/User/Filter + đã merge Nam |
| `Thanh` | `333df71 Fix theo file PHOIHOP...` | Listing/Marketplace + tài liệu phối hợp |
| `Nam` | `3664f24 feat: tich hop cong thanh toan MoMo that...` | Order/Payment |
| `Anh` | `ca687c6 thêm các UI của product` | Product |

**Kết luận quan trọng:** `origin/Quoc` **chưa có code nào**. Toàn bộ module Repair hiện chỉ nằm ở working tree máy Quốc.

---

## PHẦN 1 — PHÁT HIỆN LỚN: TÂN ĐÃ REFACTOR CẤU HÌNH JPA

Đây là thay đổi ảnh hưởng trực tiếp tới code Quốc.

### 1.1. `JpaConfig.java` trên nhánh Tan giờ **không còn rỗng**

Trước đây (bản Quốc từng đọc) `JpaConfig` là class rỗng. Nhánh `Tan` hiện tại đã implement đầy đủ:

```java
public final class JpaConfig {
    public static final String PU_NAME = "Electronics_MarketplacePU";
    private static final String ENV_URL = "DB_URL";
    private static final String ENV_USER = "DB_USER";
    private static final String ENV_PASSWORD = "DB_PASSWORD";

    public static EntityManagerFactory getEntityManagerFactory() { return Holder.EMF; }

    private static EntityManagerFactory build() {
        return Persistence.createEntityManagerFactory(PU_NAME, connectionOverrides());
    }

    private static Map<String, Object> connectionOverrides() {
        // đọc DB_URL / DB_USER / DB_PASSWORD từ biến môi trường
        // ném IllegalStateException nếu thiếu
    }
}
```

### 1.2. `persistence.xml` trên nhánh Tan dùng placeholder `${DB_URL}`

```xml
<persistence-unit name="Electronics_MarketplacePU" transaction-type="RESOURCE_LOCAL">
    <!-- KHÔNG khai báo <class>, KHÔNG bật exclude-unlisted-classes -->
    <property name="jakarta.persistence.jdbc.url" value="${DB_URL}"/>
    <property name="jakarta.persistence.jdbc.user" value="${DB_USER}"/>
    <property name="jakarta.persistence.jdbc.password" value="${DB_PASSWORD}"/>
</persistence-unit>

<persistence-unit name="adminPU" ...>   <!-- vẫn còn -->
    <property name="jakarta.persistence.jdbc.url" value="${DB_URL}"/>
</persistence-unit>
```

### 1.3. `DBConnection.java` **đã bị xóa** trên nhánh Tan

```
origin/Tan/src/main/java/com/electronics/marketplace/DBConnection/
  ├── AuthDB.java     ✅ còn
  ├── DBUtil.java     ✅ còn
  └── UserDB.java     ✅ còn
  (DBConnection.java, DatabaseInitializer.java — ĐÃ XÓA)
```

Thay vào đó là 2 listener mới:
- `listener/AdminSeedListener.java`
- `listener/AppShutdownListener.java`

`DBUtil` trên nhánh Tan gọi `JpaConfig.getEntityManagerFactory()` — **giờ hợp lệ** vì `JpaConfig` đã có method đó.

### 1.4. Hệ quả với code Quốc

| Code Quốc | Trạng thái trên nhánh Tan |
|---|---|
| `util/JpaUtil.java` | ⚠️ **Xung đột** — tạo EMF riêng, không truyền overrides |
| `config/AppLifecycleListener.java` | ⚠️ **Trùng chức năng** với `listener/AppShutdownListener.java` |
| `DBConnection/DBUtil.java` (Quốc sửa) | ❌ Tan đã có bản riêng gọi `JpaConfig` |
| `DBConnection/DBConnection.java` (Quốc sửa) | ❌ File đã bị Tan xóa |
| `DBConnection/DatabaseInitializer.java` | ❌ File đã bị Tan xóa |

**Vấn đề:** `JpaUtil` gọi `Persistence.createEntityManagerFactory(PU)` **không truyền
overrides**. Trên nhánh Tan, `persistence.xml` chứa `${DB_URL}` — Hibernate 6.4.4
không resolve được placeholder này. Tôi đã kiểm chứng bằng cách chạy thật:

```
RESULT: FAILED -> ServiceException: Unable to create requested service
[org.hibernate.engine.jdbc.env.spi.JdbcEnvironment] due to:
Unable to determine Dialect without JDBC metadata
(please set 'jakarta.persistence.jdbc.url' ...)
```

→ **Toàn bộ module Repair sẽ không kết nối được DB** nếu giữ `JpaUtil` nguyên bản.

---

## PHẦN 2 — XUNG ĐỘT ENTITY `Device`

Đây là xung đột nghiêm trọng thứ hai.

| Nhánh | `Device` fields | Ghi chú |
|---|---|---|
| **Quốc** | `brand`, **`series`**, `model`, `releaseYear`, `cpu`, `ram`, `storage` | Đúng Class Diagram |
| **Anh** | **`name`**, `brand`, `model`, `releaseYear`, `cpu`, `ram`, `storage`, `deviceType` | Thêm `name`, `deviceType`; **không có `series`** |
| **Thanh** | **`name`**, `brand`, `model`, `releaseYear`, `cpu`, `ram`, `storage` | Thêm `name`; **không có `series`** |
| **Tan** | *(rỗng — stub 5 dòng)* | Không map |

### Hệ quả

- `Device.getDisplayName()` — method Quốc viết dựa trên `brand + series + model` — **không tồn tại** trên bản Anh/Thanh.
- Cột `series` không có trên bản Anh/Thanh.
- Cột `name` và `deviceType` không có trên bản Quốc.

Với `hibernate.hbm2ddl.auto=update`, khi merge sẽ có **cả 4 cột** (`brand`, `series`, `name`, `deviceType`, `model`), trong đó `name` và `series` có thể NULL tùy bản ghi.

### Đề xuất

Chốt một bản `Device` duy nhất, giữ **hợp** các trường cần thiết:
```java
id, brand, series, model, name (tùy chọn), releaseYear, cpu, ram, storage, deviceType
```
Và `getDisplayName()` fallback: nếu có `name` thì dùng, ngược lại ghép `brand + series + model`.

---

## PHẦN 3 — XUNG ĐỘT `RepairProblem`

| Nhánh | Fields | Số dòng |
|---|---|---|
| **Quốc** | `id`, `name`, `description`, `isActive`, `estimatedMinDays`, `estimatedMaxDays` | 116 |
| **Thanh** | `id`, `name`, `isActive`, `estimatedMinDays`, `estimatedMaxDays` — **thiếu `description`** | 80 |

### Điểm quan trọng: `@Column` cho khóa chính

- **Quốc:** `@Id @Column(name = "problem_id") private Long id;`
- **Thanh:** `@Id private Long id;` → Hibernate map cột mặc định là **`id`**, không phải `problem_id`

Nếu Thanh's `RepairProblem` thắng khi merge, cột sẽ là `id` → script SQL của Quốc
(`problem_id BIGSERIAL PRIMARY KEY`) **lệch hoàn toàn**, và mọi khóa ngoại
`problem_id` trong `repair_pricings` / `repair_requests` sẽ gãy.

**Đề xuất:** giữ `@Column(name = "problem_id")` của Quốc, thêm `description` (Thanh không có).

---

## PHẦN 4 — NHỮNG GÌ QUỐC ĐÃ HOÀN THÀNH

### 4.1. Backend

| Thành phần | File | Trạng thái |
|---|---|---|
| Entity | `Device`, `RepairProblem`, `RepairPricing`, `RepairRequest`, `RepairEstimate` | ✅ Khớp Class Diagram |
| Enum | `entity/enums/RepairStatus.java` | ✅ 6 trạng thái |
| Repository | `DeviceRepositoryImpl`, `RepairRepositoryImpl` | ✅ |
| Service | `RepairService` (479 dòng) | ✅ |
| Servlet | `RepairRequestServlet`, `AdminRepairServlet` | ✅ |
| DTO | `EstimateResultDTO`, `ProblemStatisticDTO` | ✅ |
| SQL | `database/schema_and_seed_repair.sql` | ✅ Idempotent |

### 4.2. UI — 9 màn hình

| # | Màn hình | File |
|---|---|---|
| 0 | Trang chủ | `index.jsp` |
| 13 | Repair Request | `views/repair/request_form.jsp` |
| 14 | Repair Estimate | `views/repair/estimate.jsp` |
| — | Tra cứu tiến độ | `views/repair/track.jsp` |
| — | Quản lý đơn | `views/admin/repair_requests.jsp` |
| — | Dòng máy | `views/admin/device_list.jsp` |
| — | Loại lỗi | `views/admin/problem_list.jsp` |
| — | Bảng giá | `views/admin/pricing_list.jsp` |
| — | Thống kê | `views/admin/report.jsp` |

Dùng chung: `css/repair.css` (633 dòng), `views/repair/_header.jspf`, `_footer.jspf`, `views/admin/_header.jspf`.

### 4.3. Kiểm chứng

| Hạng mục | Kết quả |
|---|---|
| Java compile (58 file, loại `controller/public`) | ✅ 0 lỗi |
| `RepairServiceTest` | ✅ 18/18 pass |
| Jasper compile 13 JSP | ✅ 0 lỗi |
| JPQL (GROUP BY, JOIN FETCH, COUNT) | ✅ 11/11 parse OK |
| EMF + map 6 entity | ✅ OK |

---

## PHẦN 5 — QUỐC CẦN SỬA GÌ (theo nhánh Tan mới)

### 🔴 P1. Bỏ `JpaUtil`, dùng `JpaConfig` của Tân

**Vấn đề:** `JpaUtil` gọi `Persistence.createEntityManagerFactory(PU)` không truyền
overrides → `${DB_URL}` không resolve → **EMF tạo thất bại** (đã kiểm chứng bằng lệnh chạy thật).

**Sửa:** `JpaUtil.getEntityManagerFactory()` chuyển sang gọi
`JpaConfig.getEntityManagerFactory()`. Giữ class `JpaUtil` làm vỏ mỏng để
`RepairRepositoryImpl` / `DeviceRepositoryImpl` không phải đổi import.

```java
public static EntityManagerFactory getEntityManagerFactory() {
    return JpaConfig.getEntityManagerFactory();
}
```

### 🔴 P2. Bỏ `AppLifecycleListener`, dùng `AppShutdownListener` của Tân

**Vấn đề:** hai listener cùng đóng EMF → double close.

**Sửa:** xóa `config/AppLifecycleListener.java`; `JpaUtil.shutdown()` chuyển sang
gọi `JpaConfig` (nếu Tân có `close()`), hoặc để `AppShutdownListener` xử lý.

### 🔴 P3. Xóa `DBConnection/*` và `DatabaseInitializer`

**Vấn đề:** các file này Tân đã xóa trên nhánh của họ. Giữ lại sẽ tạo 2 EMF song song
(`DBConnection` tự tạo EMF riêng, `JpaUtil` tạo EMF riêng), và `DatabaseInitializer`
gọi `createAdminEmf()` trỏ vào `adminPU` — unit này còn khai báo nhưng cấu hình
khác `Electronics_MarketplacePU`.

**Sửa:** xóa `DBConnection.java`, `DatabaseInitializer.java`. `DBUtil.java` giữ nguyên
bản của Tân (đã gọi `JpaConfig`).

### 🟡 P4. `Device` — chốt lại với Anh/Thanh

Xem Phần 2. Cần thống nhất trường `series` / `name` / `deviceType`.

### 🟡 P5. `RepairProblem` — giữ `@Column(name="problem_id")`

Xem Phần 3. Bản Quốc đúng, bản Thanh thiếu.

### 🟡 P6. `persistence.xml` — khôi phục khai báo `<class>`

Bản Tan bỏ `<class>` và bật quét tự động. Nếu có entity trùng tên ở package khác
sẽ map nhầm. Đề xuất: bật lại `exclude-unlisted-classes=true` + liệt kê đủ 6 entity.

### 🟡 P7. `index.jsp` — Tân vẫn để redirect

Nhánh Tan vẫn là `<c:redirect url="/login"/>`. Bản Quốc đã làm trang chủ thật.
Khi merge cần giữ bản Quốc.

### 🟡 P8. `web.xml` — Tân trỏ error-page tới file không tồn tại

```xml
<error-page><error-code>404</error-code>
    <location>/WEB-INF/views/error/404.jsp</location></error-page>
<error-page><error-code>500</error-code>
    <location>/WEB-INF/views/error/500.jsp</location></error-page>
```

Nhưng **cả 2 file đều không tồn tại** trên nhánh Tan (`fatal: path ... does not exist`).
Khi có lỗi 404/500, Tomcat sẽ báo lỗi lồng nhau. Cần tạo 2 file này hoặc bỏ khai báo.

---

## PHẦN 6 — CÒN BỊ CHẶN Ở ĐÂU

### 🔴 BLOCKER duy nhất chặn build

**Package `controller.public`** — thuộc **Phước Anh + Thành**

```
src/main/java/com/electronics/marketplace/controller/public/ProductServlet.java
src/main/java/com/electronics/marketplace/controller/public/ListingServlet.java
```

`public` là từ khóa Java → `mvn clean compile` fail:
```
[ERROR] ProductServlet.java:[1,48] <identifier> expected
```

**Vẫn chưa sửa trên cả 5 nhánh** (kể cả `origin/Thanh` và `origin/Anh`).

### 🟡 Không chặn build nhưng ảnh hưởng

| # | Vấn đề | Nhánh | Ảnh hưởng |
|---|---|---|---|
| B2 | `css/style.css` rỗng **0 byte** | Tan | Dashboard/login/register/profile không có style |
| B3 | 38 file stub rỗng | Tất cả | Chức năng chưa tồn tại |
| B4 | `error/404.jsp`, `error/500.jsp` không tồn tại | Tan | 404/500 sẽ lỗi lồng nhau |
| B5 | `origin/master` chưa merge gì | — | Cả nhóm chưa có bản tích hợp chung |
| B6 | `origin/Quoc` chưa có commit | — | Module Repair chưa lên GitHub |

### 🟢 Đã giải quyết (không còn là lỗi)

Các lỗi L1–L4 tôi từng báo cáo **không còn đúng** với nhánh Tan mới:

| Lỗi cũ | Thực tế trên nhánh Tan hiện tại |
|---|---|
| `DBConnection` gọi `"chodocuPU"` / `"adminPU"` | **File `DBConnection.java` đã bị xóa hẳn.** Không còn chỗ nào gọi 2 PU này. `adminPU` vẫn khai báo trong `persistence.xml` nhưng không ai dùng. |
| `DBUtil` gọi `JpaConfig.getEntityManagerFactory()` (class rỗng) | `JpaConfig` **đã được implement đầy đủ** — method tồn tại, lỗi compile không còn |
| `db.properties` ghi `electronics_market`, mật khẩu `____` | File **vẫn còn nguyên** nhưng **không còn được đọc** — Tân chuyển sang biến môi trường `DB_URL` / `DB_USER` / `DB_PASSWORD` qua `JpaConfig` |
| `AdminServlet` dùng `int` + `getUserID()` | Tân đã đổi sang `Long` + `getId()` |
| `ProfileServlet` gọi `getUserID()` | Tân đã đổi sang `getId()` |
| `persistence.xml` khai báo `murach.business.User` | Tân đã bỏ toàn bộ khối `<class>`, chuyển sang Hibernate tự quét |

**Bài học:** các lỗi tôi sửa trên bản cũ Tân đã sửa theo cách khác. Giữ bản cũ sẽ
xung đột — cần rebase lên nhánh Tan thay vì giữ nguyên.

---

## PHẦN 7 — KẾ HOẠCH HÀNH ĐỘNG

### Bước 1 — Chờ nhóm sửa blocker

- [ ] Phước Anh + Thành: đổi package `controller.public`

### Bước 2 — Quốc rebase lên nhánh Tan

```bash
git checkout -b Quoc-rebased origin/Tan
# copy code Quốc vào, bỏ JpaUtil/AppLifecycleListener/DBConnection cũ
# sửa JpaUtil gọi JpaConfig
# chạy mvn clean test
```

### Bước 3 — Thống nhất entity với Anh/Thanh

- [ ] `Device`: chốt danh sách trường
- [ ] `RepairProblem`: giữ `@Column(name="problem_id")`
- [ ] `persistence.xml`: khôi phục danh sách `<class>`

### Bước 4 — Push

- [ ] Push `origin/Quoc` sau khi rebase và test xanh

---

## PHẦN 8 — TÀI LIỆU THAM KHẢO

- `PHOI-HOP-THANH-QUOC.md` (nhánh `origin/Thanh`) — Thành đã ghi rõ hợp đồng:
  - Quốc quản `RepairProblem`, Thành dùng `@ManyToOne` trỏ tới
  - Quốc **không được xóa cứng** `RepairProblem`, chỉ `isActive = false`
  - Quốc không tạo `Device` riêng, dùng chung với Thành
- `Dieu_chinh_19_10_2026.md` (nhánh `origin/Tan`) — Tân ghi các điều chỉnh ngày 19/10
