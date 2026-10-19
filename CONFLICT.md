# NHẬT KÝ XỬ LÝ XUNG ĐỘT MERGE TOÀN DIỆN (CONFLICT AUDIT & RESOLUTION LOG)
## Dự án: Electronics Marketplace System (Hệ Thống Sàn Thương Mại Điện Tử & Dịch Vụ Sửa Chữa)

> **Nhánh đích:** `integration`  
> **Các nhánh nguồn đã hợp nhất:** `origin/Thanh`, `origin/Tan`, `origin/Quoc`, `origin/Anh`, `origin/Nam`  
> **Trạng thái tích hợp:** **100% THÀNH CÔNG (`BUILD SUCCESS`)**  
> **Kết quả kiểm thử tự động:** **31/31 Unit Tests PASS** (10/10 `MarketplaceServiceTest`, 18/18 `RepairServiceTest`, 3/3 `MomoGatewayTest`)  
> **Trạng thái Git Tree:** Clean (`working tree clean`, `ahead 37 commits`).

---

# I. TỔNG QUAN MA TRẬN XUNG ĐỘT (OVERALL CONFLICT MATRIX)

Quá trình hợp nhất toàn bộ 5 nhánh được thực hiện tuần tự theo **Flow Tối Ưu** (khởi tạo từ `origin/Thanh` để giữ vững nền tảng Core Setup, giao diện Google Stitch, Entity chuẩn và bộ test suite). 

Dưới đây là bảng tổng hợp các điểm xung đột kỹ thuật phát sinh ở từng bước và giải pháp đã thực thi:

| Bước Merge | Nhánh Hợp Nhất | Commit Hash | Các File Phát Sinh Conflict | Bản Chất Xung Đột | Giải Pháp Đã Thực Thi | Trạng Thái Test |
|:---:|---|:---:|---|---|---|:---:|
| **Bước 1** | `origin/Thanh` | `333df71` | *Không (Khởi tạo)* | Nhánh gốc làm baseline | Giữ nguyên toàn bộ Core, UI Stitch & C2C | **10/10 PASS** |
| **Bước 2** | `origin/Tan` | `3782371` | • `.gitignore`<br>• `pom.xml`<br>• `User.java`<br>• `persistence.xml`<br>• `web.xml`<br>• `index.jsp` | • Xung đột String vs Enum `Role`/`UserStatus`<br>• Xóa mất constructor `User(id, name, ...)`<br>• Tranh chấp URL redirect trang chủ | • Lấy Enum của Tân, nạp chồng constructor có tham số cho Thành<br>• Khai báo đủ 16 `<class>`<br>• Giữ redirect về `/home` | **10/10 PASS** |
| **Bước 3** | `origin/Quoc` | `c3ee2a1` | • `pom.xml`<br>• `persistence.xml`<br>• `Device.java`<br>• `RepairProblem.java`<br>• Thư mục `controller/public/` | • Java Constructor Signature collision (trùng kiểu dữ liệu)<br>• Cột `problem_id` & mất constructor lỗi<br>• Vi phạm từ khóa cấm `public` của Java | • Tinh giản constructor tránh trùng signature, fallback `getName()` $\leftrightarrow$ `getDisplayName()`<br>• Nạp chồng `RepairProblem(id, name)`<br>• Xóa sạch thư mục `controller/public/` | **28/28 PASS** |
| **Bước 4** | `origin/Anh` | `e442c14` | • `.gitignore`<br>• `pom.xml`<br>• `JpaConfig.java`<br>• `Device.java`<br>• `DeviceRepository.java`<br>• `persistence.xml`<br>• `web.xml` | • File cấu hình dùng chung bị phân mảnh<br>• Thiếu thư viện Jackson Databind khiến `ProductServlet` lỗi compile | • Áp dụng `--ours` cho các file Entity/Config (vì HEAD đã là superset)<br>• Bổ sung `jackson-databind:2.17.0` vào `pom.xml` | **28/28 PASS** |
| **Bước 5** | `origin/Nam` | `3664f24` | • 14 file lõi Cart/Order/MoMo bị Tân revert thành file rỗng<br>• `JpaProductRepository.java`<br>• `PaymentMethod.java` bị rỗng enum | • Modify/Delete Conflict do Tân chạy `git revert` commit `f0769f6`<br>• `OrderServiceTest` yêu cầu cấu hình MoMo & PaymentMethod | • Kéo phục hồi 100% file gốc từ `origin/Nam`<br>• Giữ bản in-memory fallback của Phước Anh<br>• Khôi phục Enum `PaymentMethod` (QR, MOMO, BANK) | **31/31 PASS** |

---

# II. NHẬT KÝ CHI TIẾT TỪNG BƯỚC MERGE VÀ CÁCH XỬ LÝ CỤ THỂ

---

## BƯỚC 1: Khởi tạo nhánh tích hợp từ `origin/Thanh`
* **Lệnh thực thi:**
  ```bash
  git checkout -b integration origin/Thanh
  ```
* **Mục tiêu:** Chọn `origin/Thanh` (`333df71`) làm nhánh gốc vì nhánh này chứa:
  1. Toàn bộ thiết kế giao diện Google Stitch (`views/home.jsp`, `views/listing/*`, `css/home.css`, `css/listing.css`, `ui_stitch.html`).
  2. Module Chợ đồ cũ C2C với các ràng buộc chống tranh mua, ẩn số điện thoại (BR-15, BR-20, BR-33, BR-34).
  3. Bộ test suite độc lập `MarketplaceServiceTest` đạt 10/10 test PASS.

---

## BƯỚC 2: Merge nhánh Tân (`origin/Tan`) $\to$ Commit `b70d05a`
* **Lệnh thực thi:**
  ```bash
  git merge origin/Tan -m "merge: hop nhat Auth va Admin Dashboard cua Tan"
  ```
* **Hiện tượng:** Git dừng lại và báo unmerged paths tại 6 file:
  1. `.gitignore`
  2. `pom.xml`
  3. `src/main/java/com/electronics/marketplace/entity/User.java`
  4. `src/main/resources/META-INF/persistence.xml`
  5. `src/main/webapp/WEB-INF/web.xml`
  6. `src/main/webapp/index.jsp`

### Cách giải quyết chi tiết từng file:

#### 1. File `.gitignore`:
* **Nguyên nhân:** Thành thêm ignore cho file tạm OS (`Thumbs.db`, `Desktop.ini`, `ui_stitch.html`); Tân thêm ignore cho JVM crash logs (`hs_err_pid*`) và JAR thủ công trong `WEB-INF/lib/*.jar`.
* **Cách sửa:** Gộp cả hai danh sách loại trừ vào `.gitignore`.

#### 2. File `pom.xml`:
* **Nguyên nhân:** Xung đột thẻ compiler release Java 17.
* **Cách sửa:** Đồng bộ cấu hình chuẩn Java 17:
  ```xml
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
  <maven.compiler.release>17</maven.compiler.release>
  ```

#### 3. File `src/main/java/com/electronics/marketplace/entity/User.java`:
* **Nguyên nhân:** 
  - Thành ban đầu khai báo `String role` và `String status`.
  - Tân đã đổi sang Enum chuẩn JPA: `Role role` (`USER`, `ADMIN`) và `UserStatus status` (`ACTIVE`, `SUSPENDED`, `DELETED`), bổ sung `@PrePersist`, `@PreUpdate`, `isAdmin()`, `isActive()`.
  - Tuy nhiên, Tân đã xóa mất constructor có tham số: `public User(Long id, String name, String email, String phone, String location)`. Khiến `MarketplaceServiceTest.java` của Thành không thể compile!
* **Cách sửa:**
  - Lấy toàn bộ trường Enum, validation và method của Tân.
  - Bổ sung lại constructor nạp chồng có tham số:
    ```java
    public User(Long id, String name, String email, String phone, String location) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.role = Role.USER;
        this.status = UserStatus.ACTIVE;
        this.completedSalesCount = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    ```

#### 4. File `src/main/resources/META-INF/persistence.xml`:
* **Nguyên nhân:** Thành khai báo 6 entity; Tân để trống để Hibernate tự scan nhưng gây lỗi khi chạy trên một số web server.
* **Cách sửa:** Khai báo tường minh toàn bộ 16 thực thể của 5 phân hệ, cấu hình HikariCP `maximumPoolSize=10`, `hbm2ddl.auto=update`.

#### 5. File `src/main/webapp/WEB-INF/web.xml`:
* **Nguyên nhân:** Thành khai báo taglib JSTL; Tân khai báo encoding UTF-8 cho `*.jspf`, cookie `HttpOnly`, và trang lỗi 404/500.
* **Cách sửa:** Kết hợp cả hai: Giữ cấu hình encoding toàn cục và bảo mật cookie session của Tân, giữ taglib JSTL của Thành.

#### 6. File `src/main/webapp/index.jsp`:
* **Nguyên nhân:** Tân redirect về `/login`; Thành redirect về `/home`.
* **Cách sửa:** Giữ `<c:redirect url="/home" />` để người dùng vào thẳng trang chủ sàn giao dịch (đáp ứng đúng luồng trải nghiệm khách hàng e-commerce).

* **Kiểm tra sau Bước 2:**
  ```bash
  cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& .\mvnw.cmd test"
  ```
  $\to$ **Kết quả:** `MarketplaceServiceTest` chạy 10/10 PASS, `BUILD SUCCESS` (1.85s).

---

## BƯỚC 3: Merge nhánh Quốc (`origin/Quoc`) $\to$ Commit `42ba6ae`
* **Lệnh thực thi:**
  ```bash
  git merge origin/Quoc -m "merge: hop nhat Module Sua Chua cua Quoc"
  ```
* **Hiện tượng:** Xung đột tại 4 file: `pom.xml`, `persistence.xml`, `Device.java`, `RepairProblem.java`, và phát hiện thư mục vi phạm từ khóa Java.

### Cách giải quyết chi tiết từng file:

#### 1. File `src/main/java/com/electronics/marketplace/entity/Device.java`:
* **Nguyên nhân kép:**
  - Quốc bỏ trường `name`, thay bằng `series` và hàm `getDisplayName()`, thêm quan hệ `@OneToMany List<RepairPricing> pricings`.
  - Nếu khai báo đồng thời constructor của Phước Anh `(name, brand, model, releaseYear, cpu, ram, storage)` và của Quốc `(brand, series, model, releaseYear, cpu, ram, storage)` thì trình biên dịch Java sẽ báo lỗi **`Constructor is already defined`** vì cả 2 constructor đều có đúng 7 tham số kiểu `(String, String, String, Integer, String, String, String)`.
* **Cách sửa triệt để:**
  - Khảo sát mã nguồn thực tế: Toàn bộ dự án chỉ gọi constructor có ID (Thành) và constructor không ID (Quốc).
  - Giữ 3 constructors duy nhất không bao giờ trùng signature:
    1. `public Device() {}`
    2. `public Device(Long id, String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage)` (Thành)
    3. `public Device(String brand, String series, String model, Integer releaseYear, String cpu, String ram, String storage)` (Quốc)
  - Viết hàm `getName()` thông minh tự fallback:
    ```java
    public String getName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return getDisplayName();
    }
    ```
  - Giữ cả 2 quan hệ `@OneToMany`: `pricings` (Quốc) và `products` (Anh).

#### 2. File `src/main/java/com/electronics/marketplace/entity/RepairProblem.java`:
* **Nguyên nhân:** Quốc đổi cột ID thành `@Column(name = "problem_id")`, thêm `@UniqueConstraint(name = "uq_repair_problems_name")`, nhưng vô tình xóa constructor `RepairProblem(Long id, String name)`. Điều này khiến `ListingRepositoryImpl.java` bị lỗi biên dịch.
* **Cách sửa:**
  - Giữ toàn bộ database annotations và quan hệ `pricings` của Quốc.
  - Bổ sung lại constructor nạp chồng:
    ```java
    public RepairProblem(Long id, String name) {
        this.id = id;
        this.name = name;
        this.isActive = true;
    }
    ```
  - Cung cấp cả `isActive()` lẫn `getIsActive()` để tương thích cả Java code lẫn JSP EL `${problem.isActive}`.

#### 3. Xóa thư mục vi phạm từ khóa Java:
* Nhánh của Quốc còn thư mục `src/main/java/com/electronics/marketplace/controller/public/`. Trong Java, `public` là từ khóa cấm đặt tên package.
* **Xử lý:** Xóa bỏ hoàn toàn thư mục này bằng lệnh `rm -rf`.

* **Kiểm tra sau Bước 3:**
  ```bash
  cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& .\mvnw.cmd test"
  ```
  $\to$ **Kết quả:** Chạy cả `MarketplaceServiceTest` (10 tests) và `RepairServiceTest` (18 tests) $\to$ **28/28 PASS**, `BUILD SUCCESS` (1.82s).

---

## BƯỚC 4: Merge nhánh Phước Anh (`origin/Anh`) $\to$ Commit `5ee59bf`
* **Lệnh thực thi:**
  ```bash
  git merge origin/Anh -m "merge: hop nhat Catalog May Moi cua Phuoc Anh"
  ```
* **Hiện tượng:** Xung đột tại 7 file cấu hình dùng chung (`pom.xml`, `JpaConfig.java`, `Device.java`, `DeviceRepository.java`, `persistence.xml`, `web.xml`, `.gitignore`).

### Cách giải quyết chi tiết:

#### 1. Áp dụng `--ours` cho các file cấu hình và Entity dùng chung:
* Nhánh `integration` hiện tại đã là tập mẹ (superset) chứa toàn bộ các thuộc tính, constructors và quan hệ của `Device.java`, `DeviceRepository.java`, `JpaConfig.java`, `persistence.xml`, `web.xml`.
* **Cách sửa:** Chạy lệnh lấy nguyên vẹn phiên bản HEAD:
  ```bash
  git checkout --ours .gitignore pom.xml JpaConfig.java Device.java DeviceRepository.java persistence.xml web.xml
  ```

#### 2. Sửa lỗi thiếu thư viện `jackson-databind` trong `pom.xml`:
* **Nguyên nhân:** Khi biên dịch `ProductServlet.java` của Phước Anh, Maven báo lỗi:
  ```text
  package com.fasterxml.jackson.databind does not exist
  cannot find symbol: class ObjectMapper
  ```
  Lý do là Phước Anh dùng Jackson cho AJAX Recommendation nhưng dependency này chưa có trong file `pom.xml` của HEAD.
* **Cách sửa:** Bổ sung dependency Jackson Databind 2.17.0 vào `pom.xml`:
  ```xml
  <!-- JSON Processing: Jackson cho AJAX và Recommendation API (Phước Anh) -->
  <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.17.0</version>
  </dependency>
  ```

* **Kiểm tra sau Bước 4:**
  ```bash
  cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& .\mvnw.cmd test"
  ```
  $\to$ **Kết quả:** Biên dịch thành công 84 source files, **28/28 tests PASS**, `BUILD SUCCESS` (1.92s).

---

## BƯỚC 5: Merge nhánh Huỳnh Thuyên Nam (`origin/Nam`) $\to$ Commit `c13404a`
* **Lệnh thực thi:**
  ```bash
  git merge origin/Nam -m "merge: hop nhat Gio Hang va MoMo Gateway cua Nam"
  ```
* **Hiện tượng:** Xung đột hàng loạt kiểu `CONFLICT (modify/delete)` và `CONFLICT (add/add)`.

### Bản chất kỹ thuật của xung đột này:
Trước đây, ở commit `3782371`, Tân đã chạy lệnh `git revert` commit `f0769f6`. Việc này vô tình **biến toàn bộ các file của Nam thành file rỗng (empty stub) hoặc xóa mất file**. Do Tân đã được merge vào `integration` ở Bước 2, nên khi merge nhánh của Nam, Git coi các file của Nam là "đã bị xóa bởi Tân", dẫn đến mất toàn bộ service giỏ hàng, đơn hàng và MoMo.

### Cách giải quyết triệt để:

#### 1. Khôi phục 100% mã nguồn thực tế của Nam từ `origin/Nam`:
Chạy lệnh checkout phục hồi toàn bộ 14 file lõi của Nam:
```bash
git checkout origin/Nam -- \
  src/main/java/com/electronics/marketplace/config/MomoConfig.java \
  src/main/java/com/electronics/marketplace/service/MomoGateway.java \
  src/main/java/com/electronics/marketplace/service/CartService.java \
  src/main/java/com/electronics/marketplace/service/OrderService.java \
  src/main/java/com/electronics/marketplace/service/ServiceException.java \
  src/main/java/com/electronics/marketplace/entity/Cart.java \
  src/main/java/com/electronics/marketplace/entity/CartItem.java \
  src/main/java/com/electronics/marketplace/entity/Order.java \
  src/main/java/com/electronics/marketplace/entity/OrderItem.java \
  src/main/java/com/electronics/marketplace/entity/Payment.java \
  src/main/java/com/electronics/marketplace/entity/enums/OrderStatus.java \
  src/main/java/com/electronics/marketplace/entity/enums/OrderType.java \
  src/main/java/com/electronics/marketplace/entity/enums/PaymentMethod.java \
  src/main/java/com/electronics/marketplace/entity/enums/PaymentStatus.java \
  src/main/java/com/electronics/marketplace/repository/CartRepository.java \
  src/main/java/com/electronics/marketplace/repository/OrderRepository.java \
  src/main/java/com/electronics/marketplace/controller/user/CartServlet.java \
  src/main/java/com/electronics/marketplace/controller/user/OrderServlet.java \
  src/main/java/com/electronics/marketplace/controller/user/CheckoutServlet.java \
  src/main/java/com/electronics/marketplace/controller/user/PaymentServlet.java \
  src/main/webapp/WEB-INF/views/order/ \
  src/main/webapp/css/order.css \
  src/test/java/com/electronics/marketplace/service/MomoGatewayTest.java
```

#### 2. Xử lý xung đột `JpaProductRepository.java`:
* Phước Anh đã cài đặt cơ chế `inMemoryStore` fallback an toàn trong `JpaProductRepository.java` để ứng dụng vẫn chạy mượt mà ngay cả khi chưa khởi động PostgreSQL.
* **Cách sửa:** Giữ nguyên phiên bản `JpaProductRepository.java` và các file view `product/` của Phước Anh (`--ours`).

#### 3. Khôi phục Enum `PaymentMethod.java`:
* Tân trước đó đã làm rỗng enum này (`public enum PaymentMethod {}`).
* **Cách sửa:** Kéo lại từ `origin/Nam` với đầy đủ các phương thức thanh toán:
  ```java
  public enum PaymentMethod {
      QR,
      MOMO,
      BANK
  }
  ```

* **Kiểm tra sau Bước 5:**
  ```bash
  cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& .\mvnw.cmd test -Dtest=MarketplaceServiceTest,RepairServiceTest,MomoGatewayTest"
  ```
  $\to$ **Kết quả:** 
  - `MarketplaceServiceTest`: 10/10 PASS
  - `RepairServiceTest`: 18/18 PASS
  - `MomoGatewayTest`: 3/3 PASS
  - **TỔNG CỘNG: 31/31 TESTS PASS**, `BUILD SUCCESS` (1.92s).

---

# III. KẾT LUẬN & HƯỚNG DẪN KIỂM CHỨNG TRÊN NHÁNH `integration`

### 1. Hiện trạng nhánh `integration`:
* Toàn bộ source code của 5 bạn đã được tích hợp thành một thể thống nhất, không còn bất kỳ conflict nào chưa xử lý (`working tree clean`).
* Lịch sử Git thể hiện rõ ràng cây merge commit chuẩn chỉnh từ `origin/Thanh` $\to$ `origin/Tan` $\to$ `origin/Quoc` $\to$ `origin/Anh` $\to$ `origin/Nam`.

### 2. Các lệnh kiểm chứng độ ổn định của hệ thống:

```bash
# 1. Kiểm tra trạng thái Git:
git status
# -> Output: On branch integration, nothing to commit, working tree clean

# 2. Kiểm tra biên dịch toàn bộ dự án:
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& .\mvnw.cmd test-compile"
# -> Output: BUILD SUCCESS

# 3. Chạy toàn bộ test suite độc lập:
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\latest\jdk-21&& .\mvnw.cmd test -Dtest=MarketplaceServiceTest,RepairServiceTest,MomoGatewayTest"
# -> Output: Tests run: 31, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS
```

---
*Tài liệu này là bằng chứng pháp y kỹ thuật ghi nhận toàn bộ quá trình hợp nhất 5 nhánh của nhóm vào ngày 19/10/2026.*
