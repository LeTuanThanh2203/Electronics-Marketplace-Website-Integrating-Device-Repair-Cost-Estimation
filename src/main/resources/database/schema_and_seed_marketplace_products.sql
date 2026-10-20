-- =============================================================================
-- DATABASE SCHEMA & SEED DATA CHO MODULE CHỢ ĐỒ CŨ (LISTINGS), MÁY MỚI (PRODUCTS),
-- GIỎ HÀNG (CARTS) VÀ ĐƠN HÀNG (ORDERS)
-- Cơ sở dữ liệu: PostgreSQL
--
-- Script này IDEMPOTENT: chạy nhiều lần không gây lỗi hoặc nhân đôi dữ liệu.
-- Khớp 100% với các Entity JPA trong dự án.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. BẢNG SẢN PHẨM MÁY MỚI (PRODUCTS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT,
    name VARCHAR(150) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    price DECIMAL(14, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    sku VARCHAR(50) UNIQUE,
    series VARCHAR(100),
    original_price DECIMAL(14, 2),
    discount_tag VARCHAR(100),
    badge VARCHAR(50) DEFAULT 'SEAL 100%',
    image_url VARCHAR(500),
    warranty VARCHAR(150) DEFAULT 'Bảo hành 24 tháng chính hãng',
    description TEXT,
    target_need VARCHAR(50),
    stock_quantity INT NOT NULL DEFAULT 10,
    store_name VARCHAR(150),
    device_id BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_products_sku ON products(sku);
ALTER TABLE products ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE products ALTER COLUMN created_at DROP NOT NULL;
ALTER TABLE products ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE products ALTER COLUMN updated_at DROP NOT NULL;

-- -----------------------------------------------------------------------------
-- 2. BẢNG THÔNG SỐ KỸ THUẬT MÁY MỚI (PRODUCT_SPECIFICATIONS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_specifications (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spec_key VARCHAR(100) NOT NULL,
    data_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    numeric_value INT,
    text_value VARCHAR(500),
    unit VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_spec_product_id ON product_specifications(product_id);

-- -----------------------------------------------------------------------------
-- 3. BẢNG BÀI ĐĂNG CHỢ ĐỒ CŨ (LISTINGS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS listings (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE RESTRICT,
    condition_status VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    reserved_by_order_id BIGINT,
    reserved_at TIMESTAMP,
    is_approved BOOLEAN DEFAULT TRUE,
    is_removed_by_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_listings_seller_id ON listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status);
CREATE INDEX IF NOT EXISTS idx_listings_device_id ON listings(device_id);

-- -----------------------------------------------------------------------------
-- 4. BẢNG ẢNH BÀI ĐĂNG MÁY CŨ (LISTING_IMAGES)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS listing_images (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    sort_order INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_listing_images_listing_id ON listing_images(listing_id);

-- -----------------------------------------------------------------------------
-- 5. BẢNG LỖI ĐÃ KHAI BÁO CỦA MÁY CŨ (LISTING_PROBLEMS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS listing_problems (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    problem_id BIGINT REFERENCES repair_problems(problem_id) ON DELETE SET NULL,
    note TEXT
);

CREATE INDEX IF NOT EXISTS idx_listing_problems_listing_id ON listing_problems(listing_id);

-- -----------------------------------------------------------------------------
-- 6. BẢNG GIỎ HÀNG (CARTS) & CHI TIẾT GIỎ (CART_ITEMS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id)
);

-- -----------------------------------------------------------------------------
-- 7. BẢNG ĐƠN HÀNG (ORDERS) & CHI TIẾT ĐƠN HÀNG (ORDER_ITEMS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    order_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    listing_id BIGINT REFERENCES listings(id) ON DELETE SET NULL,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    price DECIMAL(14, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- -----------------------------------------------------------------------------
-- 8. BẢNG THANH TOÁN (PAYMENTS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    method VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    transaction_code VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP
);

-- =============================================================================
-- SEED DATA MÁY MỚI (PRODUCTS) & THÔNG SỐ (SPECIFICATIONS)
-- =============================================================================

INSERT INTO products (sku, name, brand, price, status, series, original_price, discount_tag, badge, image_url, warranty, description, target_need, stock_quantity, created_at, updated_at)
SELECT v.sku, v.name, v.brand, v.price, v.status, v.series, v.original_price, v.discount_tag, v.badge, v.image_url, v.warranty, v.description, v.target_need, v.stock_quantity, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('XPS-9320-OLED', 'Dell XPS 13 Plus 9320', 'Dell', 35900000.00, 'AVAILABLE', 'XPS SERIES', 38900000.00, 'ƯU ĐÃI -3.000.000₫', 'SEAL 100%', 'https://lh3.googleusercontent.com/aida-public/AB6AXuBgd9ksdOyJ2lnuUjd006sDwXcEnfIRfDJZTxHKikqqcuocKnck8V_k-2ZGgDHYD38OenXZfcj6CBM8LGeFjtqrZunmXgIu2syI1cXzGRi_HsoELH2imeygnlcU8b-ZYY2lNjU9r2hMhHccJ4MvD5cMpo9sO09pm491bfNCpOpGQ3rNQkKnUURFMklwZUgzblRRcwZWbpwJZw6n4Ni7De0dAuKI7ESIQIWX-rvdqUdXFpmxe3ZWP3Nd', 'Bảo hành 24 tháng chính hãng tại nhà', 'Mỏng nhẹ đỉnh cao, phím cảm ứng zero-lattice, màn hình OLED 3.5K sắc nét.', 'OFFICE', 8),
    ('MBA15-M3-16G', 'MacBook Air 15 M3 2024', 'Apple', 37490000.00, 'AVAILABLE', 'SILICON M3', 39990000.00, 'TRỢ GIÁ SINH VIÊN', 'NEW SEAL 100%', 'https://lh3.googleusercontent.com/aida-public/AB6AXuBH9khgwGVRWUr7pveEsWsrcrRTf1hc0XxMk3WqBU-iNkQ9vLl30BXCO8V3_eIq9V84sBWEa3GMJ-xWKkOjRgw2QsmmZY_4EgL3lwZ9vmdiM4v--QRzytRbb7wbkmOPA1ReAhPrYvATcEMIVJsCGWhLzMOMMItNI1l3ylHgHoaDY9SgTGoQNVrgcQUlq7PtuBWIPMJociJyfylwJ_6R_fvDDJWh33Yv3Y2t91jx9b0', 'Bảo hành 12 tháng Apple Care toàn cầu', 'Chip M3 tân tiến với GPU dò tia Ray Tracing, pin 18 giờ liên tục.', 'OFFICE', 15),
    ('X1C-GEN11-I7', 'ThinkPad X1 Carbon Gen 11', 'Lenovo', 42900000.00, 'AVAILABLE', 'X1 FLAGSHIP', 46500000.00, 'TẶNG BALO THINKPAD', 'CHÍNH HÃNG VN', 'https://lh3.googleusercontent.com/aida-public/AB6AXuBhsfIBalXmMjhm1NLSQt6Qkz_OmWr2qcpKxJyrVsvza8nGYMeKenQaqdW_amrvTYf5pRjv8UBUSw4A0wDt_dUw7SWUxo34ZS_r0k0CZi43jMo1RTEUvkVGpIc1bVGAIMEgdhXDI33Vm-9hUhDYA6j_1l4U54e0k94ozBGr9ID-NWC-wAa72xeVo2ukKbWRZA0oPGhsaat6O_hva43CM99e-XhMkJ24anxVCuySREIaIgoX6VxWOSUx', 'Bảo hành 36 tháng Premier Support', 'Khung sợi carbon chuẩn quân đội MIL-STD-810H siêu bền, bàn phím gõ êm ái bậc nhất thế giới.', 'OFFICE', 5),
    ('ROG-G16-GU605', 'ASUS ROG Zephyrus G16 (2024)', 'Asus', 52990000.00, 'AVAILABLE', 'ROG ZEPHYRUS', 56990000.00, 'ƯU ĐÃI TẶNG CHUỘT ROG', 'OLED 240HZ', 'https://lh3.googleusercontent.com/aida-public/AB6AXuDfW4DtavcquK1SUWDVWq4B-j-lm9TuqguAQN4Xa6MQg4U4aiGZnrk7v3ldHxLGKSeCe8M5zgisMD067mW34tC5KAdqFcN_v98c15IBo7SF0WlHF0EA-qXE2BsIcR-_VzT0LHni0Dr-Tlk-bNzyvpNTtHWVtuTOIxw8cQTE2qfcWCPYRDdie9vqz-Is7Wax6QMcB-0Odv_pp0ewcZPHPH8VU788RLkdImm_NIV1pXw', 'Bảo hành 24 tháng ASUS VIP', 'Màn hình ROG Nebula OLED 2.5K 240Hz sắc lạnh, vỏ nhôm CNC cao cấp siêu mỏng chỉ 1.49cm.', 'GAMING', 6),
    ('HP-SPEC-14-2024', 'HP Spectre x360 14 (2024)', 'HP', 39990000.00, 'AVAILABLE', 'SPECTRE X360', 42990000.00, 'KÈM BÚT CẢM ỨNG', 'XOAY GẬP 360°', 'https://lh3.googleusercontent.com/aida-public/AB6AXuDSDK_Y1Tn41n44-PHzjN4HBUkeJo0v_OSMIby1f3oGoKS1XIgWMT0OQ7x-70QP5_J2MFN6k8tKhSjLkWka7UUtU190qsoRx8njivi8sfEnxOxj0eqC61J_i-nvl-El0uUx2un5gtrIJ0X82JdlkRINC2sMFwV9_sGOrsFS2Xrq_UeQ-yYBeTdWKy0wfJSICusiILvie4qLItiyxiLfxq_uz5a50m5ER5NzpuL4wsU', 'Bảo hành 24 tháng chính hãng HP', 'Thiết kế vát cạnh kim cương tinh xảo, camera 9MP khử nhiễu AI, xoay gập linh hoạt làm tablet vẽ.', 'GRAPHICS', 10),
    ('PRED-HELIOS-16', 'Acer Predator Helios 16', 'Acer', 44990000.00, 'AVAILABLE', 'PREDATOR HELIOS', 48990000.00, 'TẶNG BALO PREDATOR', 'RTX 4070 140W', 'https://lh3.googleusercontent.com/aida-public/AB6AXuDiUuFj9nirVJgi2UB2s0CpHLB99-isqHObkrQVP4tE1GH5-Dxrwyv-Ni21oPfk9Y-00bjxVqJTbDdpplGnae49oFLSUi207gyj8DbIRrMataGx5YbzEl_wKQkcBE9TRH42EVYW1fPTpyRY7MepF4YMpVt6u7yy8XOjVm6R1mlEhDFN-_Fk8evmVFBMcosnqg8_3ICZaqkfcLfJtXaXUrglKwf1EzJrPutJ_dvH58E', 'Bảo hành 3S1 VIP (quá 3 ngày đổi mới)', 'Quái vật hiệu năng gaming với tản nhiệt quạt kim loại AeroBlade 3D thế hệ 5 và keo tản nhiệt kim loại lỏng.', 'GAMING', 7)
) AS v(sku, name, brand, price, status, series, original_price, discount_tag, badge, image_url, warranty, description, target_need, stock_quantity)
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.sku = v.sku);

-- Nạp specifications cho products nếu bảng specifications rỗng
INSERT INTO product_specifications (product_id, spec_key, data_type, numeric_value, text_value, unit)
SELECT p.id, s.spec_key, s.data_type, s.numeric_val, s.text_val, s.unit
FROM products p
CROSS JOIN LATERAL (
    VALUES
        ('CPU', 'TEXT', NULL, 'Intel Core i7-1360P (12 Nhân, 16 Luồng)', NULL),
        ('RAM', 'NUMBER', 16, '16', 'GB'),
        ('STORAGE', 'NUMBER', 512, '512', 'GB'),
        ('SCREEN_SIZE', 'NUMBER', 13, '13.4', 'inch')
) AS s(spec_key, data_type, numeric_val, text_val, unit)
WHERE p.sku = 'XPS-9320-OLED' AND NOT EXISTS (SELECT 1 FROM product_specifications WHERE product_id = p.id);

INSERT INTO product_specifications (product_id, spec_key, data_type, numeric_value, text_value, unit)
SELECT p.id, s.spec_key, s.data_type, s.numeric_val, s.text_val, s.unit
FROM products p
CROSS JOIN LATERAL (
    VALUES
        ('CPU', 'TEXT', NULL, 'Apple M3 8-Core CPU', NULL),
        ('RAM', 'NUMBER', 16, '16', 'GB'),
        ('STORAGE', 'NUMBER', 512, '512', 'GB'),
        ('SCREEN_SIZE', 'NUMBER', 15, '15.3', 'inch')
) AS s(spec_key, data_type, numeric_val, text_val, unit)
WHERE p.sku = 'MBA15-M3-16G' AND NOT EXISTS (SELECT 1 FROM product_specifications WHERE product_id = p.id);

INSERT INTO product_specifications (product_id, spec_key, data_type, numeric_value, text_value, unit)
SELECT p.id, s.spec_key, s.data_type, s.numeric_val, s.text_val, s.unit
FROM products p
CROSS JOIN LATERAL (
    VALUES
        ('CPU', 'TEXT', NULL, 'Intel Core i7-1365U vPro', NULL),
        ('RAM', 'NUMBER', 32, '32', 'GB'),
        ('STORAGE', 'NUMBER', 1024, '1024', 'GB'),
        ('SCREEN_SIZE', 'NUMBER', 14, '14.0', 'inch')
) AS s(spec_key, data_type, numeric_val, text_val, unit)
WHERE p.sku = 'X1C-GEN11-I7' AND NOT EXISTS (SELECT 1 FROM product_specifications WHERE product_id = p.id);

INSERT INTO product_specifications (product_id, spec_key, data_type, numeric_value, text_value, unit)
SELECT p.id, s.spec_key, s.data_type, s.numeric_val, s.text_val, s.unit
FROM products p
CROSS JOIN LATERAL (
    VALUES
        ('CPU', 'TEXT', NULL, 'Intel Core Ultra 9 185H', NULL),
        ('RAM', 'NUMBER', 32, '32', 'GB'),
        ('STORAGE', 'NUMBER', 1024, '1024', 'GB'),
        ('SCREEN_SIZE', 'NUMBER', 16, '16.0', 'inch')
) AS s(spec_key, data_type, numeric_val, text_val, unit)
WHERE p.sku = 'ROG-G16-GU605' AND NOT EXISTS (SELECT 1 FROM product_specifications WHERE product_id = p.id);

-- =============================================================================
-- SEED DATA BÀI ĐĂNG CHỢ ĐỒ CŨ (LISTINGS & IMAGES)
-- =============================================================================

-- Nạp 3 bài đăng mẫu vào bảng listings
INSERT INTO listings (seller_id, device_id, condition_status, description, price, status, is_approved, is_removed_by_admin)
SELECT
    u.user_id,
    d.id,
    v.condition,
    v.description,
    v.price,
    'AVAILABLE',
    TRUE,
    FALSE
FROM (VALUES
    ('Apple', 'MacBook', 'MacBook Air M2 13-inch', 'USED_GOOD', 'MacBook Air M2 bản Midnight cực đẹp, pin sạc 45 lần còn 99%. Mọi chức năng hoàn hảo kèm sạc cáp zin MagSafe 3.', 18500000.00),
    ('Dell', 'XPS', 'Dell XPS 15 9530', 'USED_GOOD', 'Máy cá nhân dùng làm đồ họa tại nhà giữ gìn cẩn thận. Màn OLED sắc nét không tì vết, đã dán skin carbon mặt A.', 26800000.00),
    ('Asus', 'ROG', 'ROG Zephyrus G14', 'DAMAGED', 'Máy gaming cày đồ họa nhiều. Loa bên phải bị rè nhẹ khi bật trên 80%. Màn hình và bàn phím còn rất mới, tặng chuột ROG.', 14500000.00)
) AS v(brand, series, model, condition, description, price)
JOIN devices d ON d.brand = v.brand AND d.series = v.series AND d.model = v.model
JOIN users u ON u.email = 'quoc@example.com'
WHERE NOT EXISTS (SELECT 1 FROM listings);

-- Nạp ảnh đính kèm cho listings
INSERT INTO listing_images (listing_id, image_url, sort_order)
SELECT l.id,
       'https://lh3.googleusercontent.com/aida-public/AB6AXuBH9khgwGVRWUr7pveEsWsrcrRTf1hc0XxMk3WqBU-iNkQ9vLl30BXCO8V3_eIq9V84sBWEa3GMJ-xWKkOjRgw2QsmmZY_4EgL3lwZ9vmdiM4v--QRzytRbb7wbkmOPA1ReAhPrYvATcEMIVJsCGWhLzMOMMItNI1l3ylHgHoaDY9SgTGoQNVrgcQUlq7PtuBWIPMJociJyfylwJ_6R_fvDDJWh33Yv3Y2t91jx9b0',
       0
FROM listings l
WHERE NOT EXISTS (SELECT 1 FROM listing_images WHERE listing_id = l.id);
