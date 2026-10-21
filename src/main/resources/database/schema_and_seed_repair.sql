-- =============================================================================
-- DATABASE SCHEMA & SEED DATA CHO MODULE SỬA CHỮA (REPAIR SERVICE & BASIC ADMIN)
-- Phụ trách: Quốc
-- Cơ sở dữ liệu: PostgreSQL
--
-- Script này IDEMPOTENT: chạy lại nhiều lần không nhân đôi dữ liệu.
-- Kiểu khóa dùng BIGSERIAL/BIGINT để khớp với entity JPA (Long + IDENTITY),
-- đúng quy chuẩn "khóa chính & khóa ngoại kiểu Long" đã thống nhất trong nhóm.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. BẢNG DÒNG MÁY / THIẾT BỊ (DEVICES)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    -- series NOT NULL: PostgreSQL coi mỗi NULL là khác nhau, nên nếu để NULL
    -- thì unique constraint bên dưới không chặn được dòng máy trùng.
    series VARCHAR(100) NOT NULL DEFAULT '',
    model VARCHAR(150) NOT NULL,
    release_year INT,
    cpu VARCHAR(100),
    ram VARCHAR(50),
    storage VARCHAR(50),
    CONSTRAINT uq_devices_brand_series_model UNIQUE (brand, series, model),
    CONSTRAINT chk_devices_release_year CHECK (release_year IS NULL OR release_year BETWEEN 1990 AND 2100)
);

-- -----------------------------------------------------------------------------
-- 2. BẢNG LOẠI LỖI (REPAIR_PROBLEMS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS repair_problems (
    problem_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    estimated_min_days INT DEFAULT 1,
    estimated_max_days INT DEFAULT 3,
    CONSTRAINT uq_repair_problems_name UNIQUE (name),
    CONSTRAINT chk_repair_problems_days CHECK (
        (estimated_min_days IS NULL OR estimated_min_days >= 0)
        AND (estimated_max_days IS NULL OR estimated_max_days >= 0)
        AND (estimated_min_days IS NULL OR estimated_max_days IS NULL OR estimated_min_days <= estimated_max_days)
    )
);

-- -----------------------------------------------------------------------------
-- 3. BẢNG GIÁ DỰ TOÁN SỬA CHỮA (REPAIR_PRICINGS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS repair_pricings (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    problem_id BIGINT NOT NULL REFERENCES repair_problems(problem_id) ON DELETE CASCADE,
    parts_cost DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    labor_cost DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    -- Biên độ LUÔN là tỷ lệ trong [0, 1): 0.1000 = 10%.
    estimation_margin DECIMAL(8, 4) NOT NULL DEFAULT 0.1000,
    version INT NOT NULL DEFAULT 1,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_device_problem_version UNIQUE (device_id, problem_id, version),
    CONSTRAINT chk_repair_pricings_cost CHECK (parts_cost >= 0 AND labor_cost >= 0),
    CONSTRAINT chk_repair_pricings_margin CHECK (estimation_margin >= 0 AND estimation_margin < 1),
    CONSTRAINT chk_repair_pricings_version CHECK (version >= 1)
);

-- -----------------------------------------------------------------------------
-- 4. BẢNG NGƯỜI DÙNG (USERS)
--    Bảng này do module Auth (Tân) sở hữu. Tên cột dưới đây đã đồng bộ với
--    @Column trong com.electronics.marketplace.entity.User:
--      avatar (không phải avatar_url), location, completed_sales_count.
--    Nếu entity đổi tên cột, sửa cả hai nơi để hbm2ddl không tạo cột trùng.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    avatar VARCHAR(255),
    location VARCHAR(150),
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    completed_sales_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

-- -----------------------------------------------------------------------------
-- 5. BẢNG YÊU CẦU SỬA CHỮA (REPAIR_REQUESTS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS repair_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE RESTRICT,
    problem_id BIGINT NOT NULL REFERENCES repair_problems(problem_id) ON DELETE RESTRICT,
    customer_name VARCHAR(100),
    customer_phone VARCHAR(20),
    customer_email VARCHAR(150),
    description TEXT,
    image_url VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Danh sách này phải khớp enum RepairStatus.
    CONSTRAINT chk_repair_requests_status CHECK (
        status IN ('PENDING', 'WAITING_PRICING', 'ESTIMATED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')
    )
);

-- Index phục vụ báo cáo GROUP BY problem_id và tra cứu theo người dùng.
CREATE INDEX IF NOT EXISTS idx_repair_requests_problem_id ON repair_requests(problem_id);
CREATE INDEX IF NOT EXISTS idx_repair_requests_user_id ON repair_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_repair_requests_created_at ON repair_requests(created_at DESC);

-- -----------------------------------------------------------------------------
-- 6. BẢNG SNAPSHOT BÁO GIÁ DỰ KIẾN (REPAIR_ESTIMATES)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS repair_estimates (
    id BIGSERIAL PRIMARY KEY,
    repair_request_id BIGINT UNIQUE NOT NULL REFERENCES repair_requests(id) ON DELETE CASCADE,
    min_cost DECIMAL(12, 2) NOT NULL,
    max_cost DECIMAL(12, 2) NOT NULL,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pricing_version INT DEFAULT 1,
    parts_cost_snapshot DECIMAL(12, 2) DEFAULT 0.00,
    labor_cost_snapshot DECIMAL(12, 2) DEFAULT 0.00,
    estimation_margin_snapshot DECIMAL(8, 4) DEFAULT 0.1000,
    CONSTRAINT chk_repair_estimates_range CHECK (min_cost >= 0 AND max_cost >= min_cost)
);

-- =============================================================================
-- DỮ LIỆU MẪU (SEED DATA) — IDEMPOTENT
-- Dùng ON CONFLICT kèm conflict target, vì "ON CONFLICT DO NOTHING" không có
-- target sẽ không bao giờ conflict trên bảng chỉ có PK tự sinh → chạy 2 lần
-- là nhân đôi dữ liệu.
-- =============================================================================

-- Thêm dòng máy mẫu phong phú (48 dòng thiết bị thực tế)
INSERT INTO devices (brand, series, model, release_year, cpu, ram, storage) VALUES
-- Apple iPhone
('Apple', 'iPhone', 'iPhone 16 Pro Max', 2024, 'Apple A18 Pro', '8GB', '256GB'),
('Apple', 'iPhone', 'iPhone 16', 2024, 'Apple A18', '8GB', '128GB'),
('Apple', 'iPhone', 'iPhone 15 Pro Max', 2023, 'Apple A17 Pro', '8GB', '256GB'),
('Apple', 'iPhone', 'iPhone 15', 2023, 'Apple A16 Bionic', '6GB', '128GB'),
('Apple', 'iPhone', 'iPhone 14 Pro Max', 2022, 'Apple A16 Bionic', '6GB', '128GB'),
('Apple', 'iPhone', 'iPhone 14', 2022, 'Apple A15 Bionic', '6GB', '128GB'),
('Apple', 'iPhone', 'iPhone 13 Pro Max', 2021, 'Apple A15 Bionic', '6GB', '128GB'),
('Apple', 'iPhone', 'iPhone 13', 2021, 'Apple A15 Bionic', '4GB', '128GB'),
('Apple', 'iPhone', 'iPhone 12', 2020, 'Apple A14 Bionic', '4GB', '64GB'),
-- Apple Mac & iPad
('Apple', 'MacBook', 'MacBook Air M2 13-inch', 2022, 'Apple M2', '16GB', '512GB SSD'),
('Apple', 'MacBook', 'MacBook Air M1 13-inch', 2020, 'Apple M1', '8GB', '256GB SSD'),
('Apple', 'MacBook', 'MacBook Air M3 15-inch', 2024, 'Apple M3', '16GB', '512GB SSD'),
('Apple', 'MacBook', 'MacBook Pro 14 M3 Pro', 2023, 'Apple M3 Pro', '18GB', '512GB SSD'),
('Apple', 'MacBook', 'MacBook Pro 16 M2 Max', 2023, 'Apple M2 Max', '32GB', '1TB SSD'),
('Apple', 'iPad', 'iPad Pro 11 M4', 2024, 'Apple M4', '8GB', '256GB'),
('Apple', 'iPad', 'iPad Air 5 M1', 2022, 'Apple M1', '8GB', '64GB'),
-- Samsung
('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 2024, 'Snapdragon 8 Gen 3', '12GB', '512GB'),
('Samsung', 'Galaxy', 'Galaxy S24 Plus', 2024, 'Exynos 2400', '12GB', '256GB'),
('Samsung', 'Galaxy', 'Galaxy S23 Ultra', 2023, 'Snapdragon 8 Gen 2', '12GB', '256GB'),
('Samsung', 'Galaxy', 'Galaxy S22 Ultra', 2022, 'Snapdragon 8 Gen 1', '8GB', '128GB'),
('Samsung', 'Galaxy', 'Galaxy Z Fold5', 2023, 'Snapdragon 8 Gen 2', '12GB', '512GB'),
('Samsung', 'Galaxy', 'Galaxy Z Flip5', 2023, 'Snapdragon 8 Gen 2', '8GB', '256GB'),
('Samsung', 'Galaxy', 'Galaxy A54 5G', 2023, 'Exynos 1380', '8GB', '128GB'),
-- Dell
('Dell', 'XPS', 'Dell XPS 15 9530', 2023, 'Intel Core i7-13700H', '32GB', '1TB SSD'),
('Dell', 'XPS', 'Dell XPS 13 Plus 9320', 2023, 'Intel Core i7-1360P', '16GB', '512GB SSD'),
('Dell', 'Inspiron', 'Dell Inspiron 15 3520', 2023, 'Intel Core i5-1235U', '16GB', '512GB SSD'),
('Dell', 'Inspiron', 'Dell Inspiron 14 5430', 2023, 'Intel Core i7-1360P', '16GB', '512GB SSD'),
('Dell', 'Latitude', 'Dell Latitude 5440', 2023, 'Intel Core i5-1335U', '16GB', '256GB SSD'),
('Dell', 'Alienware', 'Dell Alienware m16 R1', 2023, 'Intel Core i9-13900HX', '32GB', '1TB SSD'),
-- Asus
('Asus', 'ROG', 'ROG Zephyrus G14', 2023, 'AMD Ryzen 9 7940HS', '16GB', '1TB SSD'),
('Asus', 'ROG', 'ROG Zephyrus G16 GU605', 2024, 'Intel Core Ultra 9 185H', '32GB', '1TB SSD'),
('Asus', 'TUF Gaming', 'TUF Gaming F15 FX507', 2023, 'Intel Core i7-12700H', '16GB', '512GB SSD'),
('Asus', 'ZenBook', 'ZenBook 14 OLED UX3405', 2024, 'Intel Core Ultra 7 155H', '16GB', '1TB SSD'),
('Asus', 'VivoBook', 'VivoBook 15 OLED A1505', 2023, 'AMD Ryzen 7 7730U', '16GB', '512GB SSD'),
-- Lenovo
('Lenovo', 'ThinkPad', 'ThinkPad X1 Carbon Gen 11', 2023, 'Intel Core i7-1365U', '16GB', '512GB SSD'),
('Lenovo', 'ThinkPad', 'ThinkPad T14 Gen 4', 2023, 'AMD Ryzen 7 PRO 7840U', '16GB', '512GB SSD'),
('Lenovo', 'Legion', 'Legion 5 Pro 16IRX8', 2023, 'Intel Core i7-13700HX', '16GB', '1TB SSD'),
('Lenovo', 'IdeaPad', 'IdeaPad Slim 5 14IAH8', 2023, 'Intel Core i5-12450H', '16GB', '512GB SSD'),
-- HP
('HP', 'Spectre', 'HP Spectre x360 14', 2024, 'Intel Core Ultra 7 155H', '16GB', '1TB SSD'),
('HP', 'Envy', 'HP Envy x360 14', 2023, 'Intel Core i7-1355U', '16GB', '512GB SSD'),
('HP', 'Pavilion', 'HP Pavilion 15 eg3095TU', 2023, 'Intel Core i5-1335U', '16GB', '512GB SSD'),
('HP', 'Victus', 'HP Victus 16 r0127TX', 2023, 'Intel Core i5-13500H', '16GB', '512GB SSD'),
('HP', 'EliteBook', 'HP EliteBook 840 G10', 2023, 'Intel Core i7-1360P', '16GB', '512GB SSD'),
-- Acer
('Acer', 'Predator', 'Predator Helios 16 PH16', 2023, 'Intel Core i7-13700HX', '16GB', '1TB SSD'),
('Acer', 'Nitro', 'Nitro 5 Tiger AN515', 2023, 'Intel Core i5-12500H', '16GB', '512GB SSD'),
('Acer', 'Swift', 'Swift Go 14 SFG14', 2023, 'Intel Core i5-13500H', '16GB', '512GB SSD'),
('Acer', 'Aspire', 'Aspire 7 Gaming A715', 2023, 'AMD Ryzen 5 5625U', '16GB', '512GB SSD'),
-- Xiaomi
('Xiaomi', '14 Series', 'Xiaomi 14 Ultra', 2024, 'Snapdragon 8 Gen 3', '16GB', '512GB'),
('Xiaomi', '13 Series', 'Xiaomi 13 Pro', 2023, 'Snapdragon 8 Gen 2', '12GB', '256GB'),
('Xiaomi', 'Redmi Note', 'Redmi Note 13 Pro Plus', 2024, 'Dimensity 7200 Ultra', '12GB', '256GB')
ON CONFLICT (brand, series, model) DO NOTHING;

-- Thêm các loại lỗi thường gặp (14 danh mục lỗi phần cứng thực tế)
INSERT INTO repair_problems (name, description, is_active, estimated_min_days, estimated_max_days) VALUES
('Vỡ màn hình / Chảy mực', 'Màn hình bị rơi nứt vỡ kính, sọc kẻ hoặc chảy mực', TRUE, 1, 2),
('Chai pin / Phồng pin', 'Thời lượng pin kém, pin bị phồng đội nắp lưng', TRUE, 1, 1),
('Liệt bàn phím / Kẹt phím', 'Một hoặc nhiều phím không phản hồi hoặc bị loạn phím', TRUE, 1, 3),
('Không lên nguồn / Lỗi IC nguồn', 'Cắm sạc không báo tín hiệu, máy không khởi động, chập nguồn bo mạch', TRUE, 2, 5),
('Hỏng camera / Mờ ống kính', 'Camera sau không lấy nét hoặc bị rung, nứt kính cam', TRUE, 1, 2),
('Hỏng cổng sạc / Chân sạc lỏng', 'Cắm sạc không nhận điện, chân sạc Type-C/Lightning bị chập chờn, cháy chân tiếp xúc', TRUE, 1, 2),
('Hỏng loa / Rè loa / Mất âm thanh', 'Loa trong hoặc loa ngoài bị rè, méo tiếng hoặc mất âm thanh hoàn toàn', TRUE, 1, 2),
('Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 'Quạt kêu to, kẹt cánh quạt, máy nóng ran tự động ngắt nguồn bảo vệ', TRUE, 1, 2),
('Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 'Bản lề bị cứng, gãy chân ốc, bung viền màn hình khi gập mở', TRUE, 1, 3),
('Vỡ mặt kính lưng / Nứt nắp lưng', 'Mặt kính lưng bị nứt vỡ, trầy xước nặng hoặc móp méo vỏ kim loại', TRUE, 1, 2),
('Hỏng FaceID / Mất cảm biến vân tay', 'Không nhận diện khuôn mặt FaceID hoặc cảm biến vân tay không hoạt động', TRUE, 1, 3),
('Máy bị vào nước / Oxy hóa bo mạch', 'Thiết bị rơi nước, cần sấy khô công nghiệp, tẩy rỉ sét siêu âm và phục hồi mạch', TRUE, 2, 4),
('Hỏng trackpad / Liệt chuột cảm ứng', 'Chuột cảm ứng laptop bị đơ, chập nhảy con trỏ hoặc không bấm click được', TRUE, 1, 2),
('Lỗi card Wifi / Mất kết nối mạng', 'Không bắt được Wifi, Bluetooth chập chờn hoặc chip mạng chập cháy', TRUE, 1, 2)
ON CONFLICT (name) DO NOTHING;

-- Bảng giá chuẩn: tra cứu theo brand, series, model và tên lỗi thị trường
INSERT INTO repair_pricings (device_id, problem_id, parts_cost, labor_cost, estimation_margin, version, is_active)
SELECT d.id, p.problem_id, v.parts_cost, v.labor_cost, v.margin, 1, TRUE
FROM (VALUES
    -- ================= APPLE IPHONE =================
    -- iPhone 16 Pro Max
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Vỡ màn hình / Chảy mực',       6500000.00, 350000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Chai pin / Phồng pin',           1800000.00, 200000.00, 0.0500),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Hỏng camera / Mờ ống kính',      2200000.00, 250000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Vỡ mặt kính lưng / Nứt nắp lưng', 850000.00, 200000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Hỏng cổng sạc / Chân sạc lỏng',   650000.00, 200000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Hỏng FaceID / Mất cảm biến vân tay', 900000.00, 300000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Không lên nguồn / Lỗi IC nguồn', 2200000.00, 400000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Máy bị vào nước / Oxy hóa bo mạch', 900000.00, 350000.00, 0.1200),
    -- iPhone 16
    ('Apple', 'iPhone', 'iPhone 16',         'Vỡ màn hình / Chảy mực',       3500000.00, 300000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 16',         'Chai pin / Phồng pin',           1450000.00, 180000.00, 0.0500),
    ('Apple', 'iPhone', 'iPhone 16',         'Hỏng camera / Mờ ống kính',      1600000.00, 200000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 16',         'Vỡ mặt kính lưng / Nứt nắp lưng', 650000.00, 180000.00, 0.0800),
    -- iPhone 15 Pro Max
    ('Apple', 'iPhone', 'iPhone 15 Pro Max', 'Vỡ màn hình / Chảy mực',       5800000.00, 300000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 15 Pro Max', 'Chai pin / Phồng pin',           1500000.00, 200000.00, 0.0500),
    ('Apple', 'iPhone', 'iPhone 15 Pro Max', 'Hỏng camera / Mờ ống kính',      1900000.00, 250000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 15 Pro Max', 'Vỡ mặt kính lưng / Nứt nắp lưng', 750000.00, 200000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 15 Pro Max', 'Hỏng cổng sạc / Chân sạc lỏng',   600000.00, 200000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 15 Pro Max', 'Hỏng FaceID / Mất cảm biến vân tay', 850000.00, 250000.00, 0.1000),
    -- iPhone 14 Pro Max
    ('Apple', 'iPhone', 'iPhone 14 Pro Max', 'Vỡ màn hình / Chảy mực',       4800000.00, 300000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 14 Pro Max', 'Chai pin / Phồng pin',           1300000.00, 180000.00, 0.0500),
    ('Apple', 'iPhone', 'iPhone 14 Pro Max', 'Hỏng camera / Mờ ống kính',      1500000.00, 200000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 14 Pro Max', 'Vỡ mặt kính lưng / Nứt nắp lưng', 650000.00, 180000.00, 0.1000),
    -- iPhone 13 Pro Max & iPhone 13
    ('Apple', 'iPhone', 'iPhone 13 Pro Max', 'Vỡ màn hình / Chảy mực',       3850000.00, 250000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 13 Pro Max', 'Chai pin / Phồng pin',           1150000.00, 150000.00, 0.0500),
    ('Apple', 'iPhone', 'iPhone 13 Pro Max', 'Hỏng camera / Mờ ống kính',      1300000.00, 200000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 13',         'Vỡ màn hình / Chảy mực',       1950000.00, 250000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 13',         'Chai pin / Phồng pin',           1050000.00, 150000.00, 0.0500),
    ('Apple', 'iPhone', 'iPhone 13',         'Hỏng camera / Mờ ống kính',      1100000.00, 200000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 13',         'Vỡ mặt kính lưng / Nứt nắp lưng', 500000.00, 150000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 12',         'Vỡ màn hình / Chảy mực',       1650000.00, 200000.00, 0.0800),
    ('Apple', 'iPhone', 'iPhone 12',         'Chai pin / Phồng pin',            850000.00, 150000.00, 0.0500),

    -- ================= APPLE MACBOOK & IPAD =================
    -- MacBook Air M2 13-inch
    ('Apple', 'MacBook', 'MacBook Air M2 13-inch', 'Vỡ màn hình / Chảy mực',       6800000.00, 500000.00, 0.0800),
    ('Apple', 'MacBook', 'MacBook Air M2 13-inch', 'Chai pin / Phồng pin',           2100000.00, 300000.00, 0.0500),
    ('Apple', 'MacBook', 'MacBook Air M2 13-inch', 'Liệt bàn phím / Kẹt phím',       1800000.00, 350000.00, 0.0800),
    ('Apple', 'MacBook', 'MacBook Air M2 13-inch', 'Không lên nguồn / Lỗi IC nguồn', 1600000.00, 500000.00, 0.1200),
    ('Apple', 'MacBook', 'MacBook Air M2 13-inch', 'Hỏng trackpad / Liệt chuột cảm ứng', 1200000.00, 250000.00, 0.0800),
    ('Apple', 'MacBook', 'MacBook Air M2 13-inch', 'Máy bị vào nước / Oxy hóa bo mạch', 1500000.00, 600000.00, 0.1500),
    -- MacBook Air M1 13-inch
    ('Apple', 'MacBook', 'MacBook Air M1 13-inch', 'Vỡ màn hình / Chảy mực',       5200000.00, 450000.00, 0.0800),
    ('Apple', 'MacBook', 'MacBook Air M1 13-inch', 'Chai pin / Phồng pin',           1650000.00, 250000.00, 0.0500),
    ('Apple', 'MacBook', 'MacBook Air M1 13-inch', 'Liệt bàn phím / Kẹt phím',       1400000.00, 300000.00, 0.0800),
    ('Apple', 'MacBook', 'MacBook Air M1 13-inch', 'Hỏng loa / Rè loa / Mất âm thanh', 850000.00, 200000.00, 0.0800),
    -- MacBook Pro 14 M3 Pro
    ('Apple', 'MacBook', 'MacBook Pro 14 M3 Pro',  'Vỡ màn hình / Chảy mực',      10500000.00, 600000.00, 0.0800),
    ('Apple', 'MacBook', 'MacBook Pro 14 M3 Pro',  'Chai pin / Phồng pin',          2500000.00, 350000.00, 0.0500),
    ('Apple', 'MacBook', 'MacBook Pro 14 M3 Pro',  'Liệt bàn phím / Kẹt phím',      2200000.00, 400000.00, 0.0800),
    ('Apple', 'MacBook', 'MacBook Pro 14 M3 Pro',  'Không lên nguồn / Lỗi IC nguồn',2400000.00, 600000.00, 0.1200),
    ('Apple', 'MacBook', 'MacBook Pro 14 M3 Pro',  'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 900000.00, 250000.00, 0.0800),
    -- iPad Pro 11 M4
    ('Apple', 'iPad',    'iPad Pro 11 M4',         'Vỡ màn hình / Chảy mực',       5800000.00, 400000.00, 0.0800),
    ('Apple', 'iPad',    'iPad Pro 11 M4',         'Chai pin / Phồng pin',          1750000.00, 250000.00, 0.0500),

    -- ================= SAMSUNG GALAXY =================
    -- Galaxy S24 Ultra
    ('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 'Vỡ màn hình / Chảy mực',       6800000.00, 400000.00, 0.0800),
    ('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 'Chai pin / Phồng pin',           750000.00, 180000.00, 0.0500),
    ('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 'Hỏng camera / Mờ ống kính',      2400000.00, 250000.00, 0.0800),
    ('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 'Vỡ mặt kính lưng / Nứt nắp lưng', 800000.00, 150000.00, 0.0800),
    ('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 'Hỏng cổng sạc / Chân sạc lỏng',   650000.00, 180000.00, 0.0800),
    ('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 'Không lên nguồn / Lỗi IC nguồn', 1900000.00, 400000.00, 0.1000),
    -- Galaxy S23 Ultra
    ('Samsung', 'Galaxy', 'Galaxy S23 Ultra', 'Vỡ màn hình / Chảy mực',       5900000.00, 350000.00, 0.0800),
    ('Samsung', 'Galaxy', 'Galaxy S23 Ultra', 'Chai pin / Phồng pin',           650000.00, 150000.00, 0.0500),
    ('Samsung', 'Galaxy', 'Galaxy S23 Ultra', 'Hỏng camera / Mờ ống kính',      2100000.00, 250000.00, 0.0800),
    ('Samsung', 'Galaxy', 'Galaxy S23 Ultra', 'Vỡ mặt kính lưng / Nứt nắp lưng', 650000.00, 150000.00, 0.0800),
    -- Galaxy Z Fold5 & Flip5
    ('Samsung', 'Galaxy', 'Galaxy Z Fold5',   'Vỡ màn hình / Chảy mực',       9500000.00, 600000.00, 0.1000),
    ('Samsung', 'Galaxy', 'Galaxy Z Fold5',   'Chai pin / Phồng pin',          1200000.00, 250000.00, 0.0800),
    ('Samsung', 'Galaxy', 'Galaxy Z Fold5',   'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 2800000.00, 500000.00, 0.1200),
    ('Samsung', 'Galaxy', 'Galaxy Z Flip5',   'Vỡ màn hình / Chảy mực',       6200000.00, 450000.00, 0.1000),
    ('Samsung', 'Galaxy', 'Galaxy Z Flip5',   'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 2200000.00, 400000.00, 0.1200),

    -- ================= DELL LAPTOPS =================
    -- Dell XPS 15 9530
    ('Dell', 'XPS', 'Dell XPS 15 9530', 'Vỡ màn hình / Chảy mực',       3800000.00, 400000.00, 0.1000),
    ('Dell', 'XPS', 'Dell XPS 15 9530', 'Chai pin / Phồng pin',           1600000.00, 250000.00, 0.0500),
    ('Dell', 'XPS', 'Dell XPS 15 9530', 'Liệt bàn phím / Kẹt phím',       1200000.00, 300000.00, 0.1000),
    ('Dell', 'XPS', 'Dell XPS 15 9530', 'Không lên nguồn / Lỗi IC nguồn', 1800000.00, 500000.00, 0.1200),
    ('Dell', 'XPS', 'Dell XPS 15 9530', 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 750000.00, 200000.00, 0.0800),
    ('Dell', 'XPS', 'Dell XPS 15 9530', 'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 600000.00, 200000.00, 0.1000),
    -- Dell XPS 13 Plus 9320
    ('Dell', 'XPS', 'Dell XPS 13 Plus 9320', 'Vỡ màn hình / Chảy mực',   4500000.00, 450000.00, 0.1000),
    ('Dell', 'XPS', 'Dell XPS 13 Plus 9320', 'Chai pin / Phồng pin',       1500000.00, 250000.00, 0.0500),
    ('Dell', 'XPS', 'Dell XPS 13 Plus 9320', 'Liệt bàn phím / Kẹt phím',   1600000.00, 350000.00, 0.1000),
    -- Dell Inspiron 15 3520
    ('Dell', 'Inspiron', 'Dell Inspiron 15 3520', 'Vỡ màn hình / Chảy mực', 1650000.00, 250000.00, 0.0800),
    ('Dell', 'Inspiron', 'Dell Inspiron 15 3520', 'Chai pin / Phồng pin',     850000.00, 150000.00, 0.0500),
    ('Dell', 'Inspiron', 'Dell Inspiron 15 3520', 'Liệt bàn phím / Kẹt phím', 550000.00, 150000.00, 0.0800),
    ('Dell', 'Inspiron', 'Dell Inspiron 15 3520', 'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 550000.00, 150000.00, 0.0800),
    ('Dell', 'Inspiron', 'Dell Inspiron 15 3520', 'Không lên nguồn / Lỗi IC nguồn', 1100000.00, 350000.00, 0.1000),
    -- Dell Alienware m16 R1
    ('Dell', 'Alienware', 'Dell Alienware m16 R1', 'Vỡ màn hình / Chảy mực', 4200000.00, 450000.00, 0.1000),
    ('Dell', 'Alienware', 'Dell Alienware m16 R1', 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 950000.00, 250000.00, 0.0800),

    -- ================= ASUS LAPTOPS =================
    -- Asus ROG Zephyrus G14 & G16
    ('Asus', 'ROG', 'ROG Zephyrus G14', 'Vỡ màn hình / Chảy mực',       3600000.00, 400000.00, 0.1000),
    ('Asus', 'ROG', 'ROG Zephyrus G14', 'Chai pin / Phồng pin',           1450000.00, 250000.00, 0.0500),
    ('Asus', 'ROG', 'ROG Zephyrus G14', 'Liệt bàn phím / Kẹt phím',       1100000.00, 300000.00, 0.0800),
    ('Asus', 'ROG', 'ROG Zephyrus G14', 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 850000.00, 250000.00, 0.0800),
    ('Asus', 'ROG', 'ROG Zephyrus G14', 'Không lên nguồn / Lỗi IC nguồn', 2100000.00, 500000.00, 0.1200),
    ('Asus', 'ROG', 'ROG Zephyrus G16 GU605', 'Vỡ màn hình / Chảy mực',   4800000.00, 450000.00, 0.1000),
    -- Asus TUF Gaming F15 FX507
    ('Asus', 'TUF Gaming', 'TUF Gaming F15 FX507', 'Vỡ màn hình / Chảy mực', 2200000.00, 300000.00, 0.0800),
    ('Asus', 'TUF Gaming', 'TUF Gaming F15 FX507', 'Chai pin / Phồng pin',     1100000.00, 200000.00, 0.0500),
    ('Asus', 'TUF Gaming', 'TUF Gaming F15 FX507', 'Liệt bàn phím / Kẹt phím',  750000.00, 200000.00, 0.0800),
    ('Asus', 'TUF Gaming', 'TUF Gaming F15 FX507', 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 700000.00, 200000.00, 0.0800),
    ('Asus', 'TUF Gaming', 'TUF Gaming F15 FX507', 'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 550000.00, 150000.00, 0.0800),
    -- Asus ZenBook 14 OLED UX3405
    ('Asus', 'ZenBook', 'ZenBook 14 OLED UX3405', 'Vỡ màn hình / Chảy mực', 4200000.00, 400000.00, 0.1000),
    ('Asus', 'ZenBook', 'ZenBook 14 OLED UX3405', 'Chai pin / Phồng pin',     1350000.00, 250000.00, 0.0500),
    ('Asus', 'ZenBook', 'ZenBook 14 OLED UX3405', 'Liệt bàn phím / Kẹt phím', 1250000.00, 300000.00, 0.0800),

    -- ================= LENOVO =================
    -- ThinkPad X1 Carbon Gen 11
    ('Lenovo', 'ThinkPad', 'ThinkPad X1 Carbon Gen 11', 'Vỡ màn hình / Chảy mực', 3900000.00, 400000.00, 0.0800),
    ('Lenovo', 'ThinkPad', 'ThinkPad X1 Carbon Gen 11', 'Chai pin / Phồng pin',     1600000.00, 250000.00, 0.0500),
    ('Lenovo', 'ThinkPad', 'ThinkPad X1 Carbon Gen 11', 'Liệt bàn phím / Kẹt phím', 1450000.00, 300000.00, 0.0800),
    ('Lenovo', 'ThinkPad', 'ThinkPad X1 Carbon Gen 11', 'Không lên nguồn / Lỗi IC nguồn', 1800000.00, 500000.00, 0.1200),
    ('Lenovo', 'ThinkPad', 'ThinkPad X1 Carbon Gen 11', 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 800000.00, 200000.00, 0.0800),
    -- ThinkPad T14 Gen 4
    ('Lenovo', 'ThinkPad', 'ThinkPad T14 Gen 4', 'Vỡ màn hình / Chảy mực', 2500000.00, 300000.00, 0.0800),
    ('Lenovo', 'ThinkPad', 'ThinkPad T14 Gen 4', 'Liệt bàn phím / Kẹt phím',  950000.00, 200000.00, 0.0800),
    ('Lenovo', 'ThinkPad', 'ThinkPad T14 Gen 4', 'Hỏng cổng sạc / Chân sạc lỏng', 550000.00, 180000.00, 0.0800),
    -- Legion 5 Pro 16IRX8
    ('Lenovo', 'Legion', 'Legion 5 Pro 16IRX8', 'Vỡ màn hình / Chảy mực', 2900000.00, 350000.00, 0.0800),
    ('Lenovo', 'Legion', 'Legion 5 Pro 16IRX8', 'Chai pin / Phồng pin',     1300000.00, 200000.00, 0.0500),
    ('Lenovo', 'Legion', 'Legion 5 Pro 16IRX8', 'Liệt bàn phím / Kẹt phím',  950000.00, 250000.00, 0.0800),
    ('Lenovo', 'Legion', 'Legion 5 Pro 16IRX8', 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 850000.00, 250000.00, 0.0800),

    -- ================= HP =================
    -- HP Spectre x360 14
    ('HP', 'Spectre', 'HP Spectre x360 14', 'Vỡ màn hình / Chảy mực',       4800000.00, 450000.00, 0.1000),
    ('HP', 'Spectre', 'HP Spectre x360 14', 'Chai pin / Phồng pin',           1500000.00, 250000.00, 0.0500),
    ('HP', 'Spectre', 'HP Spectre x360 14', 'Liệt bàn phím / Kẹt phím',       1300000.00, 300000.00, 0.0800),
    ('HP', 'Spectre', 'HP Spectre x360 14', 'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 750000.00, 250000.00, 0.1000),
    -- HP Pavilion 15
    ('HP', 'Pavilion', 'HP Pavilion 15 eg3095TU', 'Vỡ màn hình / Chảy mực', 1700000.00, 250000.00, 0.0800),
    ('HP', 'Pavilion', 'HP Pavilion 15 eg3095TU', 'Chai pin / Phồng pin',     850000.00, 150000.00, 0.0500),
    ('HP', 'Pavilion', 'HP Pavilion 15 eg3095TU', 'Liệt bàn phím / Kẹt phím', 600000.00, 150000.00, 0.0800),
    ('HP', 'Pavilion', 'HP Pavilion 15 eg3095TU', 'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 550000.00, 150000.00, 0.0800),
    -- HP Victus 16
    ('HP', 'Victus',   'HP Victus 16 r0127TX',   'Vỡ màn hình / Chảy mực', 2300000.00, 300000.00, 0.0800),
    ('HP', 'Victus',   'HP Victus 16 r0127TX',   'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 750000.00, 200000.00, 0.0800),

    -- ================= ACER =================
    -- Predator Helios 16 PH16
    ('Acer', 'Predator', 'Predator Helios 16 PH16', 'Vỡ màn hình / Chảy mực', 3400000.00, 400000.00, 0.0800),
    ('Acer', 'Predator', 'Predator Helios 16 PH16', 'Chai pin / Phồng pin',     1400000.00, 250000.00, 0.0500),
    ('Acer', 'Predator', 'Predator Helios 16 PH16', 'Liệt bàn phím / Kẹt phím', 1050000.00, 250000.00, 0.0800),
    ('Acer', 'Predator', 'Predator Helios 16 PH16', 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 900000.00, 250000.00, 0.0800),
    -- Nitro 5 Tiger AN515
    ('Acer', 'Nitro',    'Nitro 5 Tiger AN515',    'Vỡ màn hình / Chảy mực', 2100000.00, 300000.00, 0.0800),
    ('Acer', 'Nitro',    'Nitro 5 Tiger AN515',    'Chai pin / Phồng pin',     1050000.00, 200000.00, 0.0500),
    ('Acer', 'Nitro',    'Nitro 5 Tiger AN515',    'Liệt bàn phím / Kẹt phím',  750000.00, 200000.00, 0.0800),
    ('Acer', 'Nitro',    'Nitro 5 Tiger AN515',    'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 550000.00, 150000.00, 0.0800),

    -- ================= XIAOMI =================
    -- Xiaomi 14 Ultra
    ('Xiaomi', '14 Series', 'Xiaomi 14 Ultra', 'Vỡ màn hình / Chảy mực',       5200000.00, 350000.00, 0.0800),
    ('Xiaomi', '14 Series', 'Xiaomi 14 Ultra', 'Chai pin / Phồng pin',           650000.00, 150000.00, 0.0500),
    ('Xiaomi', '14 Series', 'Xiaomi 14 Ultra', 'Hỏng camera / Mờ ống kính',      2500000.00, 250000.00, 0.0800),
    ('Xiaomi', '14 Series', 'Xiaomi 14 Ultra', 'Vỡ mặt kính lưng / Nứt nắp lưng', 700000.00, 150000.00, 0.0800),
    ('Xiaomi', '14 Series', 'Xiaomi 14 Ultra', 'Hỏng cổng sạc / Chân sạc lỏng',   500000.00, 150000.00, 0.0800),
    -- Xiaomi 13 Pro
    ('Xiaomi', '13 Series', 'Xiaomi 13 Pro',   'Vỡ màn hình / Chảy mực',       3900000.00, 300000.00, 0.0800),
    ('Xiaomi', '13 Series', 'Xiaomi 13 Pro',   'Chai pin / Phồng pin',           600000.00, 150000.00, 0.0500),
    -- Redmi Note 13 Pro Plus
    ('Xiaomi', 'Redmi Note', 'Redmi Note 13 Pro Plus', 'Vỡ màn hình / Chảy mực', 1950000.00, 200000.00, 0.0800),
    ('Xiaomi', 'Redmi Note', 'Redmi Note 13 Pro Plus', 'Chai pin / Phồng pin',     500000.00, 120000.00, 0.0500)
) AS v(brand, series, model, problem_name, parts_cost, labor_cost, margin)
JOIN devices d ON d.brand = v.brand AND d.series = v.series AND d.model = v.model
JOIN repair_problems p ON p.name = v.problem_name
ON CONFLICT (device_id, problem_id, version) DO NOTHING;

-- Đảm bảo 100% toàn bộ 48 dòng thiết bị đều có đầy đủ bảng giá cho toàn bộ 14 danh mục lỗi
-- Tự động tính toán định mức chuẩn xác theo phân loại thương hiệu / laptop / điện thoại
INSERT INTO repair_pricings (device_id, problem_id, parts_cost, labor_cost, estimation_margin, version, is_active)
SELECT 
    d.id, 
    p.problem_id,
    -- Ước tính linh kiện (parts_cost) theo phân khúc máy và loại lỗi
    CASE 
        WHEN p.name = 'Vỡ màn hình / Chảy mực' THEN 
            CASE 
                WHEN d.brand = 'Apple' AND d.series = 'MacBook' THEN 6500000.00
                WHEN d.brand = 'Apple' AND d.series = 'iPhone' THEN 3200000.00
                WHEN d.brand = 'Apple' AND d.series = 'iPad' THEN 3800000.00
                WHEN d.brand = 'Samsung' AND d.model LIKE '%Ultra%' THEN 6200000.00
                WHEN d.model LIKE '%Fold%' THEN 9500000.00
                WHEN d.model LIKE '%Flip%' THEN 6200000.00
                WHEN d.series IN ('XPS', 'ROG', 'Spectre', 'Predator', 'Legion', 'ZenBook', 'Alienware') THEN 4200000.00
                ELSE 2100000.00 
            END
        WHEN p.name = 'Chai pin / Phồng pin' THEN 
            CASE 
                WHEN d.series = 'MacBook' THEN 1800000.00
                WHEN d.brand = 'Apple' THEN 1200000.00
                WHEN d.brand = 'Samsung' OR d.brand = 'Xiaomi' THEN 650000.00
                WHEN d.series IN ('ROG', 'Predator', 'Legion', 'Alienware') THEN 1350000.00
                ELSE 950000.00 
            END
        WHEN p.name = 'Liệt bàn phím / Kẹt phím' THEN 
            CASE 
                WHEN d.series = 'MacBook' THEN 1600000.00
                WHEN d.series IN ('ThinkPad', 'XPS', 'Spectre', 'ROG', 'Alienware') THEN 1200000.00
                WHEN d.series IN ('iPhone', 'Galaxy', '14 Series', '13 Series', 'Redmi Note', 'iPad') THEN 0.00
                ELSE 650000.00 
            END
        WHEN p.name = 'Không lên nguồn / Lỗi IC nguồn' THEN 
            CASE 
                WHEN d.series = 'MacBook' THEN 1800000.00
                WHEN d.brand IN ('Apple', 'Samsung') THEN 1600000.00
                WHEN d.series IN ('ROG', 'Alienware', 'Predator') THEN 1900000.00
                ELSE 1300000.00 
            END
        WHEN p.name = 'Hỏng camera / Mờ ống kính' THEN 
            CASE 
                WHEN d.brand IN ('Apple', 'Samsung', 'Xiaomi') THEN 1600000.00
                ELSE 650000.00 
            END
        WHEN p.name = 'Hỏng cổng sạc / Chân sạc lỏng' THEN 550000.00
        WHEN p.name = 'Hỏng loa / Rè loa / Mất âm thanh' THEN 
            CASE WHEN d.series = 'MacBook' THEN 850000.00 ELSE 450000.00 END
        WHEN p.name = 'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy' THEN 
            CASE 
                WHEN d.series IN ('ROG', 'Predator', 'Legion', 'Alienware', 'Victus', 'TUF Gaming') THEN 850000.00 
                WHEN d.series IN ('iPhone', 'Galaxy', '14 Series', '13 Series', 'Redmi Note', 'iPad') THEN 0.00
                ELSE 600000.00 
            END
        WHEN p.name = 'Gãy bản lề / Hỏng cơ cấu gập vỏ máy' THEN 
            CASE 
                WHEN d.model LIKE '%Fold%' OR d.model LIKE '%Flip%' THEN 2500000.00 
                WHEN d.series IN ('iPhone', 'Galaxy', '14 Series', '13 Series', 'Redmi Note', 'iPad') THEN 0.00
                ELSE 550000.00 
            END
        WHEN p.name = 'Vỡ mặt kính lưng / Nứt nắp lưng' THEN 
            CASE 
                WHEN d.brand = 'Apple' AND d.series = 'iPhone' THEN 750000.00
                WHEN d.brand IN ('Samsung', 'Xiaomi') THEN 650000.00
                ELSE 800000.00
            END
        WHEN p.name = 'Hỏng FaceID / Mất cảm biến vân tay' THEN 750000.00
        WHEN p.name = 'Máy bị vào nước / Oxy hóa bo mạch' THEN 
            CASE WHEN d.series = 'MacBook' THEN 1500000.00 ELSE 950000.00 END
        WHEN p.name = 'Hỏng trackpad / Liệt chuột cảm ứng' THEN 
            CASE 
                WHEN d.series = 'MacBook' THEN 1300000.00 
                WHEN d.series IN ('iPhone', 'Galaxy', '14 Series', '13 Series', 'Redmi Note', 'iPad') THEN 0.00
                ELSE 650000.00 
            END
        WHEN p.name = 'Lỗi card Wifi / Mất kết nối mạng' THEN 500000.00
        ELSE 500000.00
    END AS parts_cost,
    -- Tiền công kỹ thuật (labor_cost)
    CASE 
        WHEN p.name IN ('Không lên nguồn / Lỗi IC nguồn', 'Máy bị vào nước / Oxy hóa bo mạch') THEN 400000.00
        WHEN p.name = 'Vỡ màn hình / Chảy mực' THEN 300000.00
        WHEN p.name = 'Liệt bàn phím / Kẹt phím' THEN 250000.00
        WHEN p.name = 'Gãy bản lề / Hỏng cơ cấu gập vỏ máy' AND (d.model LIKE '%Fold%' OR d.model LIKE '%Flip%') THEN 450000.00
        ELSE 200000.00
    END AS labor_cost,
    -- Biên độ ước lượng (estimation_margin)
    CASE 
        WHEN p.name IN ('Không lên nguồn / Lỗi IC nguồn', 'Máy bị vào nước / Oxy hóa bo mạch') THEN 0.1200
        WHEN p.name = 'Vỡ màn hình / Chảy mực' THEN 0.1000
        ELSE 0.0800
    END AS margin,
    1,
    TRUE
FROM devices d
CROSS JOIN repair_problems p
WHERE NOT EXISTS (
    SELECT 1 FROM repair_pricings rp 
    WHERE rp.device_id = d.id AND rp.problem_id = p.problem_id AND rp.version = 1
)
ON CONFLICT (device_id, problem_id, version) DO NOTHING;


-- Người dùng mẫu. Mật khẩu là "123456" (hash bcrypt hợp lệ, 60 ký tự).
INSERT INTO users (name, email, phone, password_hash, role, status, completed_sales_count, created_at)
VALUES ('Nguyễn Văn Quốc', 'quoc@example.com', '0987654321',
        '$2a$10$mHo/klVRHYv3lKOZ1AWnhunhq68Tqv4Op7ZF.lTzp3XDGgflG4KU2', 'USER', 'ACTIVE', 0, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Yêu cầu sửa chữa mẫu phong phú (12 đơn mẫu để kiểm thử báo cáo thống kê GROUP BY và phân loại)
INSERT INTO repair_requests (user_id, device_id, problem_id, customer_name, customer_phone, customer_email, description, status, created_at)
SELECT u.id, d.id, p.problem_id, v.customer_name, v.phone, v.email, v.description, v.status, CURRENT_TIMESTAMP
FROM (VALUES
    ('Apple',   'iPhone',     'iPhone 16 Pro Max',         'Vỡ màn hình / Chảy mực',       'Trần Minh Đức',   '0901234567', 'duc@example.com',    'Rơi từ bàn làm việc vỡ màn hình nứt góc',       'ESTIMATED'),
    ('Dell',    'XPS',        'Dell XPS 15 9530',          'Vỡ màn hình / Chảy mực',       'Lê Hoàng Nam',    '0902345678', 'nam@example.com',    'Màn hình bị va đập góc cấn sọc ngang',          'IN_PROGRESS'),
    ('Apple',   'iPhone',     'iPhone 16 Pro Max',         'Chai pin / Phồng pin',         'Phạm Thu Trang',  '0903456789', 'trang@example.com',  'Pin chỉ dùng được 2 tiếng là sập nguồn',        'COMPLETED'),
    ('Dell',    'XPS',        'Dell XPS 15 9530',          'Liệt bàn phím / Kẹt phím',     'Hoàng Văn Bách',  '0904567890', 'bach@example.com',   'Phím cách và phím Enter bị liệt hoàn toàn',     'ESTIMATED'),
    ('Apple',   'iPhone',     'iPhone 16 Pro Max',         'Vỡ màn hình / Chảy mực',       'Nguyễn Mai Anh',  '0905678901', 'maianh@example.com', 'Vỡ nát kính trước cần thay cụm màn',           'ESTIMATED'),
    ('Apple',   'MacBook',    'MacBook Air M2 13-inch',    'Chai pin / Phồng pin',         'Vũ Quốc Thái',    '0906789012', 'thai@example.com',   'Pin phồng đội nắp đáy cần thay pin gấp',        'COMPLETED'),
    ('Samsung', 'Galaxy',     'Galaxy S24 Ultra',          'Vỡ màn hình / Chảy mực',       'Đỗ Bích Ngọc',    '0907890123', 'ngoc@example.com',   'Rơi đường nứt kính sọc màn Dynamic AMOLED',     'IN_PROGRESS'),
    ('Samsung', 'Galaxy',     'Galaxy S24 Ultra',          'Hỏng camera / Mờ ống kính',    'Bùi Tiến Đạt',    '0908901234', 'dat@example.com',    'Camera 200MP rung lắc không lấy nét được',      'ESTIMATED'),
    ('Asus',    'ROG',        'ROG Zephyrus G14',          'Hỏng quạt tản nhiệt / Quá nhiệt tắt máy', 'Phan Gia Huy', '0909012345', 'huy@example.com', 'Quạt GPU kêu rít to, nhiệt độ 95 độ tắt máy', 'IN_PROGRESS'),
    ('Lenovo',  'ThinkPad',   'ThinkPad X1 Carbon Gen 11', 'Liệt bàn phím / Kẹt phím',     'Lương Thảo My',   '0910123456', 'my@example.com',     'Đổ nước ngọt vào kẹt phím TrackPoint',          'WAITING_PRICING'),
    ('HP',      'Spectre',    'HP Spectre x360 14',        'Gãy bản lề / Hỏng cơ cấu gập vỏ máy', 'Trịnh Đình Quang', '0911234567', 'quang@example.com', 'Bản lề xoay 360 bị kẹt cứng bung vỏ C',      'ESTIMATED'),
    ('Acer',    'Nitro',      'Nitro 5 Tiger AN515',       'Không lên nguồn / Lỗi IC nguồn','Ngô Hải Đăng',   '0912345678', 'dang@example.com',   'Cắm sạc qua đêm sáng ra không lên đèn nguồn',   'WAITING_PRICING')
) AS v(dev_brand, dev_series, dev_model, problem_name, customer_name, phone, email, description, status)
JOIN devices d ON d.brand = v.dev_brand AND d.series = v.dev_series AND d.model = v.dev_model
JOIN repair_problems p ON p.name = v.problem_name
JOIN users u ON u.email = 'quoc@example.com'
WHERE NOT EXISTS (SELECT 1 FROM repair_requests);

-- Sinh snapshot estimate cho các request mẫu dựa trên bảng giá hiện hành
INSERT INTO repair_estimates (repair_request_id, min_cost, max_cost, pricing_version,
                              parts_cost_snapshot, labor_cost_snapshot, estimation_margin_snapshot)
SELECT r.id,
       ROUND((rp.parts_cost + rp.labor_cost) * (1 - rp.estimation_margin), 2),
       ROUND((rp.parts_cost + rp.labor_cost) * (1 + rp.estimation_margin), 2),
       rp.version,
       rp.parts_cost,
       rp.labor_cost,
       rp.estimation_margin
FROM repair_requests r
JOIN repair_pricings rp
  ON rp.device_id = r.device_id AND rp.problem_id = r.problem_id AND rp.is_active = TRUE
WHERE NOT EXISTS (SELECT 1 FROM repair_estimates);

