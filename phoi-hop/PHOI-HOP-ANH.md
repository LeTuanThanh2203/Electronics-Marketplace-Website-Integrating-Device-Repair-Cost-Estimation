# Đồng bộ nhánh Nam ↔ Phước Anh — `product/*` và các file chung

> Viết 2026-10-19, sau khi review `origin/Anh` @ `e442c14` (merge-base `9072b00`).
> Mục tiêu: Anh sửa xong `detail.jsp` thì merge vào `Nam` là chạy, không phải sửa lần hai.
> Xem thêm [PHOI-HOP-THANH.md](PHOI-HOP-THANH.md) — mục 8 có 2 lỗi trùng với file này.

## TL;DR — 5 điểm vỡ

| Hạng mục | Chốt | Ai sửa |
|---|---|---|
| Nút "THÊM GIỎ HÀNG" ở `detail.jsp:203` | `POST ${ctx}/cart?action=add` + hidden `productId` | **Anh** |
| Ô chọn số lượng `#qtyInput` ở `detail.jsp:185-194` | **xoá** — BR-45 quy định quantity luôn = 1 | **Anh** |
| `entity/Device.java` | **Thành** sở hữu (xem PHOI-HOP-THANH mục TL;DR); Anh **không** đổi constructor | **Anh** trả lại |
| `JpaConfig`, `persistence.xml`, `web.xml`, `pom.xml` | lấy bản **Nam** | **Anh** không merge bản của mình |
| `MockProductData` + fallback in-memory trong `JpaProductRepository` | **xoá** trước khi merge | **Anh** |

---

## 1. Nút "THÊM GIỎ HÀNG" — hợp đồng với `CartServlet`

Đây là lý do `CartServlet` hiện chưa có đường vào từ giao diện: cả 3 nút mua trên
`origin/Anh` đều là `<button type="button">` **không có handler**, và ô số lượng
không được đọc ở đâu cả.

**Bản hiện tại** — `detail.jsp:202-206`:

```html
<!-- Add to Cart CTA -->
<button type="button" class="h-11 px-5 border border-brand-black bg-white ...">
    <span class="material-symbols-outlined text-base">shopping_cart</span>
    <span>THÊM GIỎ HÀNG</span>
</button>
```

**Sửa thành** (giữ nguyên class, chỉ bọc `<form>`):

```html
<form method="post" action="${pageContext.request.contextPath}/cart?action=add" class="shrink-0">
    <input type="hidden" name="productId" value="${product.id}">
    <button type="submit" class="h-11 px-5 border border-brand-black bg-white hover:bg-brand-card text-brand-black font-mono font-bold text-xs uppercase tracking-wider transition-colors flex items-center justify-center gap-1.5">
        <span class="material-symbols-outlined text-base">shopping_cart</span>
        <span>THÊM GIỎ HÀNG</span>
    </button>
</form>
```

Hợp đồng phía server (`controller/user/CartServlet.java:47-78`, **không đổi**):

| Điều kiện | Kết quả |
|---|---|
| method + `action` | chỉ nhận **POST** `?action=add`; GET `?action=add` không có nhánh riêng |
| tham số | `productId` (Long) — thiếu/sai → **400** |
| chưa login | **401** (`sessionScope.currentUser == null`) |
| sản phẩm không tồn tại / `status != AVAILABLE` | **404** / **409** |
| thành công | **302 → `${ctx}/cart`** |

Hai hệ quả cần biết:

1. **Chưa login bấm nút → trang 401 của Tomcat.** Chưa có gì chặn trước; khi
   `AuthenticationFilter` (Tân) merge thì nó redirect `/login` — xem
   [PHOI-HOP-TAN.md](PHOI-HOP-TAN.md) mục 1-2. Không cần Anh tự xử lý.
2. **Không gửi `quantity`.** `CartService.addItem` luôn ghi `quantity = 1` và bỏ qua
   nếu `(cart_id, product_id)` đã có (BR-45). Vì vậy:

   **Xoá ô số lượng** `detail.jsp:185-194` (`#qtyInput`, 2 nút `changeQty`) **và**
   hàm `changeQty()` ở `detail.jsp:235-241`. Để lại là UI hứa một thứ backend
   không làm — người dùng bấm `+` lên 3 rồi thêm vào giỏ vẫn chỉ được 1.

### Hai nút còn lại

| Chỗ | Hiện tại | Xử lý |
|---|---|---|
| `detail.jsp:196-200` "MUA NGAY" | nút rỗng | **để sau** — cần add-to-cart rồi mới redirect `/checkout`, làm khi luồng chính chạy được |
| `list.jsp:425-427` "MUA NGAY" | nút rỗng | cùng cách, hoặc đổi thành link `?action=detail&id=${p.id}` cho tới lúc đó |
| `detail.jsp:66` icon giỏ ở header | `href="#"` | đổi `href="${pageContext.request.contextPath}/cart"` — một dòng, cho giỏ có lối vào |
| `list.jsp:77` icon giỏ ở header | `href="#"` | y hệt |

---

## 2. `Device.java` — không phải file của Anh

`PHOI-HOP-THANH.md` đã chốt `Device` thuộc **Thành**. `origin/Anh` đổi:

```java
// origin/Thanh và bản cũ: có id
public Device(Long id, String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage)

// origin/Anh: bỏ id, brand/model thành NOT NULL, thêm deviceType
public Device(String name, String brand, String model, Integer releaseYear, String cpu, String ram, String storage)
```

Đổi này **làm đỏ nhánh Thành** ở 2 file, 7 chỗ:

- `ListingRepositoryImpl.java:39-44` — `new Device(1L, "ThinkPad T14 Gen 2", …)` ×6
- `MarketplaceService.java:109` — `new Device(deviceId != null ? deviceId : 1L, …)`

**Cách rẻ nhất:** Anh giữ constructor 8 tham số cũ (id) và bỏ `NOT NULL` ở
`brand`/`model`, vì `MockProductData` chỉ cần `setId()` sau khi tạo. Nếu vẫn muốn
bỏ `id` khỏi constructor thì phải báo Thành trước và Thành sửa 7 chỗ đó.

Thêm `deviceType = "LAPTOP"` là field mới, không xung đột — giữ được, nhưng
`@OneToMany List<Product> products` nên để Thành thêm nếu cần.

---

## 3. `Product` — lấy bản Anh, nhưng có 2 hệ quả

Bản Nam hiện tại chỉ có `id, name, brand, price, status, storeName` (đủ cho
Cart/Order). Bản Anh là entity đầy đủ và **nên lấy** — nhưng:

| Thay đổi | Hệ quả |
|---|---|
| `brand` → `NOT NULL` | `OrderServiceTest.persistProduct` chỉ set `name/price/status` → **test đỏ**. Thêm `setBrand("TestBrand")`. |
| `createdAt` (`NOT NULL`, `updatable=false`), `stockQuantity` (`NOT NULL`) | `hbm2ddl.auto=update` sinh `ALTER TABLE products ADD COLUMN created_at ... NOT NULL`. **Nếu bảng `products` còn dòng, PostgreSQL từ chối** → deploy fail. Xoá dữ liệu test trong `products` trước, hoặc thêm `columnDefinition = "timestamp default now()"`. |
| `adminId` | đang là `Long` thường, không có FK tới `users`. Spec ghi `adminId` nên tạm ổn; nếu sau này cần FK thì đó là việc của module Admin. |
| `getCpu/getRamGb/...` hard-code fallback (16GB/512GB/14.0) | `list.jsp` đọc các getter này. Không sai về build, nhưng máy thiếu spec sẽ hiện số bịa — nên sửa thành `null` và để JSP `<c:if>` ẩn dòng. |

`ProductSpecification` + `SpecDataType` là entity/enum **mới** — không đụng ai,
giữ nguyên. `persistence.xml` của Nam tự quét classpath nên không cần đăng ký.

---

## 4. Bốn file chung: **không** merge bản của Anh

| File | Bản Anh | Vì sao không lấy |
|---|---|---|
| `config/JpaConfig.java` | nuốt exception, trả `emf = null` | Mọi service của Nam gọi `getEntityManagerFactory().createEntityManager()` → **NPE** thay vì lỗi rõ ràng. Bản Nam fail-fast. |
| `META-INF/persistence.xml` | hardcode `postgres/postgres`, db `electronics_marketplace`, `hbm2ddl.auto=none`, có `<class>` list | **Hardcode credential** (vi phạm rule bảo mật). `hbm2ddl=none` ⇒ bảng `carts/orders/payments` không tồn tại ⇒ cả module Nam chết. `<class>` list thiếu entity của Nam. |
| `web.xml` | chỉ có `display-name` | Mất `page-encoding UTF-8`, session cookie `HttpOnly`, error-page 404/500. |
| `pom.xml` | thêm `jackson-databind:2.17.0`; hạ `junit` 5.13.2 → 5.10.2 | Chỉ **lấy đúng 1 dependency** `jackson-databind`. Giữ junit 5.13.2 và block `classpathDependencyExcludes` cho `jakarta.json.bind-api` (xem comment trong `pom.xml`) — bỏ nó là `mvn test` chết. |

`ProductServlet` của Anh dùng Jackson cho `isJsonRequest`/`sendJsonResponse` nên
dependency này là bắt buộc. Không dùng cho chỗ nào khác.

---

## 5. `ProductRepository`: class → interface là **an toàn**

Bản Nam là class có `findById/findAvailable/save`. Grep toàn repo: **không có
caller nào** ngoài chính nó. Thay bằng interface + `JpaProductRepository` của Anh
không phá gì.

Nhưng `JpaProductRepository` có 2 thứ phải xoá trước khi merge:

```java
private static final Map<Long, Product> inMemoryStore = new ConcurrentHashMap<>();
// ... và trong mọi catch:
System.err.println("JPA findById warning: " + e.getMessage());
return Optional.ofNullable(inMemoryStore.get(id));
```

- **Fallback in-memory trả sản phẩm giả khi DB lỗi.** Trên máy dev sẽ trông như
  chạy được, tới lúc demo mới lộ. Nguy hiểm hơn: `save/delete` cũng ghi vào map
  nên dữ liệu hai nguồn lệch nhau.
- `System.err.println` là debug output — rule dự án cấm. Bỏ hẳn, để exception nổi lên.

`MockProductData` chỉ nên còn dùng trong **test**, không nằm trên đường chạy thật.

---

## 6. Những chỗ Anh tự sửa trước khi merge

- [ ] `detail.jsp:203` → form POST `/cart?action=add` + hidden `productId` (mục 1)
- [ ] `detail.jsp:185-194` + `changeQty()` → xoá (BR-45)
- [ ] `detail.jsp:66`, `list.jsp:77` → icon giỏ trỏ `${ctx}/cart`
- [ ] `Device` → trả constructor 8 tham số, bỏ `NOT NULL` ở `brand`/`model` (mục 2)
- [ ] `JpaConfig`, `persistence.xml`, `web.xml`, `pom.xml` → không mang bản của mình; chỉ xin thêm `jackson-databind`
- [ ] `JpaProductRepository` → xoá `inMemoryStore` + `System.err.println`
- [ ] `index.jsp`, `home.jsp` (0 B trên `origin/Anh`) → **không ghi đè** bản của Nam
- [ ] `getCpu/getRamGb/...` fallback số bịa → trả `null` nếu spec không có
- [ ] EL raw trong `detail.jsp`/`list.jsp`/`compare.jsp` (`${product.name}`, `${p.brand}`, `${spec.textValue}`) → `<c:out>`. Cùng lỗi đã ghi ở [PHOI-HOP-THANH.md](PHOI-HOP-THANH.md) mục 8.

---

## 7. Việc phía Nam phải làm khi merge (để Anh không phải sửa)

1. Lấy entity + repository + service + servlet + 3 JSP của Anh.
2. Giữ `JpaConfig`, `persistence.xml`, `web.xml`, `index.jsp`, junit/surefire của Nam.
3. Thêm `jackson-databind` vào `pom.xml`.
4. `OrderServiceTest.persistProduct` → thêm `setBrand(...)`; `EntityMappingTest:138`
   và `OrderServiceTest.persistListing` → thêm `setModel(...)` cho `Device`.
5. `mvn -o test` → phải xanh hết trước khi push.
6. Deploy Tomcat: xoá `work/Catalina/localhost/shb` rồi mới copy WAR, nếu không
   JSP cũ còn cache và `detail.jsp` mới không hiện.
