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
    -- name dùng cho module Product/Listing (Anh, Thành); để NULL cho phép
    -- module Repair chỉ nhập brand + series + model.
    name VARCHAR(150),
    brand VARCHAR(100) NOT NULL,
    -- series NOT NULL: PostgreSQL coi mỗi NULL là khác nhau, nên nếu để NULL
    -- thì unique constraint bên dưới không chặn được dòng máy trùng.
    series VARCHAR(100) NOT NULL DEFAULT '',
    model VARCHAR(150) NOT NULL,
    release_year INT,
    cpu VARCHAR(100),
    ram VARCHAR(50),
    storage VARCHAR(50),
    device_type VARCHAR(50) DEFAULT 'LAPTOP',
    CONSTRAINT uq_devices_brand_series_model UNIQUE (brand, series, model),
    CONSTRAINT chk_devices_release_year CHECK (release_year IS NULL OR release_year BETWEEN 1990 AND 2100)
);

-- Bổ sung cột cho database đã tạo trước khi hợp nhất 3 nhánh (idempotent).
ALTER TABLE devices ADD COLUMN IF NOT EXISTS name VARCHAR(150);
ALTER TABLE devices ADD COLUMN IF NOT EXISTS device_type VARCHAR(50) DEFAULT 'LAPTOP';

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
    -- Entity User map khóa chính thành cột "id" (mặc định Hibernate), nên DDL
    -- phải dùng "id" để hbm2ddl.auto=update không sinh thêm cột user_id trùng.
    id BIGSERIAL PRIMARY KEY,
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
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
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

-- Thêm dòng máy mẫu
INSERT INTO devices (brand, series, model, release_year, cpu, ram, storage) VALUES
('Apple', 'iPhone', 'iPhone 16 Pro Max', 2024, 'Apple A18 Pro', '8GB', '256GB'),
('Apple', 'MacBook', 'MacBook Air M2 13-inch', 2022, 'Apple M2', '16GB', '512GB SSD'),
('Dell', 'XPS', 'Dell XPS 15 9530', 2023, 'Intel Core i7-13700H', '32GB', '1TB SSD'),
('Asus', 'ROG', 'ROG Zephyrus G14', 2023, 'AMD Ryzen 9 7940HS', '16GB', '1TB SSD'),
('Samsung', 'Galaxy', 'Galaxy S24 Ultra', 2024, 'Snapdragon 8 Gen 3', '12GB', '512GB')
ON CONFLICT (brand, series, model) DO NOTHING;

-- Thêm các loại lỗi thường gặp
INSERT INTO repair_problems (name, description, is_active, estimated_min_days, estimated_max_days) VALUES
('Vỡ màn hình / Chảy mực', 'Màn hình bị rơi nứt vỡ kính, sọc kẻ hoặc chảy mực', TRUE, 1, 2),
('Chai pin / Phồng pin', 'Thời lượng pin kém, pin bị phồng đội nắp lưng', TRUE, 1, 1),
('Liệt bàn phím / Kẹt phím', 'Một hoặc nhiều phím không phản hồi hoặc bị loạn phím', TRUE, 1, 3),
('Không lên nguồn / Lỗi IC nguồn', 'Cắm sạc không báo tín hiệu, máy không khởi động', TRUE, 2, 5),
('Hỏng camera / Mờ ống kính', 'Camera sau không lấy nét hoặc bị rung, nứt kính cam', TRUE, 1, 2)
ON CONFLICT (name) DO NOTHING;

-- Bảng giá chuẩn: tra ID theo tên thay vì hard-code, để chạy lại script hay
-- xóa/thêm dòng máy trước đó vẫn trỏ đúng bản ghi.
INSERT INTO repair_pricings (device_id, problem_id, parts_cost, labor_cost, estimation_margin, version, is_active)
SELECT d.id, p.problem_id, v.parts_cost, v.labor_cost, v.margin, 1, TRUE
FROM (VALUES
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Vỡ màn hình / Chảy mực', 6500000.00, 350000.00, 0.1000),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Chai pin / Phồng pin',      1800000.00, 200000.00, 0.0500),
    ('Dell',  'XPS',    'Dell XPS 15 9530',    'Vỡ màn hình / Chảy mực', 3800000.00, 400000.00, 0.1000),
    ('Dell',  'XPS',    'Dell XPS 15 9530',    'Liệt bàn phím / Kẹt phím', 1200000.00, 300000.00, 0.1000)
) AS v(brand, series, model, problem_name, parts_cost, labor_cost, margin)
JOIN devices d ON d.brand = v.brand AND d.series = v.series AND d.model = v.model
JOIN repair_problems p ON p.name = v.problem_name
ON CONFLICT (device_id, problem_id, version) DO NOTHING;

-- Người dùng mẫu. Mật khẩu là "123456" (hash bcrypt hợp lệ, 60 ký tự).
-- Đây là tài khoản demo — PHẢI đổi mật khẩu khi triển khai thật.
-- Lưu ý: DatabaseInitializer của module Auth cũng tự seed một tài khoản ADMIN
-- (cntann06@gmail.com) nếu bảng chưa có admin nào. Hai cơ chế không xung đột vì
-- cùng dùng ON CONFLICT theo email.
INSERT INTO users (name, email, phone, password_hash, role, status)
VALUES ('Nguyễn Văn Quốc', 'quoc@example.com', '0987654321',
        '$2a$10$mHo/klVRHYv3lKOZ1AWnhunhq68Tqv4Op7ZF.lTzp3XDGgflG4KU2', 'USER', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- Yêu cầu sửa chữa mẫu để kiểm thử báo cáo thống kê GROUP BY.
-- Chỉ chèn khi bảng còn trống, để chạy lại script không nhân đôi đơn.
INSERT INTO repair_requests (user_id, device_id, problem_id, customer_name, customer_phone, customer_email, description, status)
SELECT u.id, d.id, p.problem_id, v.customer_name, v.phone, v.email, v.description, 'ESTIMATED'
FROM (VALUES
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Vỡ màn hình / Chảy mực', 'Trần Minh Đức', '0901234567', 'duc@example.com', 'Rơi từ bàn làm việc vỡ màn hình'),
    ('Dell',  'XPS',    'Dell XPS 15 9530',    'Vỡ màn hình / Chảy mực', 'Lê Hoàng Nam',  '0902345678', 'nam@example.com', 'Màn hình bị va đập góc cấn sọc ngang'),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Chai pin / Phồng pin',      'Phạm Thu Trang','0903456789', 'trang@example.com', 'Pin chỉ dùng được 2 tiếng là sập'),
    ('Dell',  'XPS',    'Dell XPS 15 9530',    'Liệt bàn phím / Kẹt phím', 'Hoàng Văn Bách','0904567890', 'bach@example.com', 'Phím cách và phím Enter bị liệt hoàn toàn'),
    ('Apple', 'iPhone', 'iPhone 16 Pro Max', 'Vỡ màn hình / Chảy mực',   'Nguyễn Mai Anh','0905678901', 'maianh@example.com', 'Vỡ nát kính trước')
) AS v(dev_brand, dev_series, dev_model, problem_name, customer_name, phone, email, description)
JOIN devices d ON d.brand = v.dev_brand AND d.series = v.dev_series AND d.model = v.dev_model
JOIN repair_problems p ON p.name = v.problem_name
JOIN users u ON u.email = 'quoc@example.com'
WHERE NOT EXISTS (SELECT 1 FROM repair_requests);

-- Sinh snapshot estimate cho các request mẫu dựa trên bảng giá hiện hành.
-- Tính trực tiếp bằng SQL để không phụ thuộc ID cứng.
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
