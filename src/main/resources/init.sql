-- ============================================================
-- Restaurant Table Reservation System - Database Init Script
-- ============================================================

USE restaurant_db;

-- ============================================================
-- MENU MANAGEMENT SUBDOMAIN
-- ============================================================

CREATE TABLE IF NOT EXISTS menus (
    menu_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    effective_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (
    menu_item_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_id       BIGINT,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(500),
    amount        DECIMAL(10,2) NOT NULL,
    currency      VARCHAR(3)  NOT NULL DEFAULT 'CAD',
    category      VARCHAR(30) NOT NULL,
    is_available  BOOLEAN     NOT NULL DEFAULT TRUE,
    dietary_tags  VARCHAR(255),
    CONSTRAINT chk_price_positive CHECK (amount > 0),
    FOREIGN KEY (menu_id) REFERENCES menus(menu_id)
);

-- ============================================================
-- FLOOR LAYOUT SUBDOMAIN
-- ============================================================

CREATE TABLE IF NOT EXISTS floor_sections (
    section_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS dining_tables (
    table_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number     VARCHAR(20)  NOT NULL UNIQUE,
    seating_capacity INT          NOT NULL,
    table_type       VARCHAR(30)  NOT NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE',
    section_id       BIGINT,
    position_x       INT          NOT NULL DEFAULT 0,
    position_y       INT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_capacity_positive CHECK (seating_capacity >= 1),
    FOREIGN KEY (section_id) REFERENCES floor_sections(section_id)
);

-- ============================================================
-- CUSTOMER LOYALTY SUBDOMAIN
-- ============================================================

CREATE TABLE IF NOT EXISTS loyalty_accounts (
    account_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT NOT NULL UNIQUE,
    points_balance  INT    NOT NULL DEFAULT 0,
    tier            VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    enrollment_date DATE NOT NULL,
    CONSTRAINT chk_points_non_negative CHECK (points_balance >= 0)
);

CREATE TABLE IF NOT EXISTS points_transactions (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id       BIGINT NOT NULL,
    points           INT    NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    reference_id     BIGINT,
    transaction_date DATETIME NOT NULL,
    FOREIGN KEY (account_id) REFERENCES loyalty_accounts(account_id)
);

-- ============================================================
-- TABLE RESERVATION MANAGEMENT (CORE) SUBDOMAIN
-- ============================================================

CREATE TABLE IF NOT EXISTS table_bookings (
    booking_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id           BIGINT       NOT NULL,
    table_id              BIGINT       NOT NULL,
    pre_order_id          BIGINT,
    reservation_date      DATE         NOT NULL,
    time_slot_start       TIME         NOT NULL,
    time_slot_end         TIME         NOT NULL,
    party_size            INT          NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    loyalty_points_earned INT          NOT NULL DEFAULT 0,
    created_at            DATETIME     NOT NULL,
    CONSTRAINT chk_timeslot CHECK (time_slot_end > time_slot_start)
);

CREATE TABLE IF NOT EXISTS pre_orders (
    pre_order_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id    BIGINT         NOT NULL,
    total_amount  DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    currency      VARCHAR(3)     NOT NULL DEFAULT 'CAD',
    status        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    submitted_at  DATETIME
);

CREATE TABLE IF NOT EXISTS order_line_items (
    line_item_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    pre_order_id  BIGINT         NOT NULL,
    menu_item_id  BIGINT         NOT NULL,
    quantity      INT            NOT NULL,
    unit_amount   DECIMAL(10,2)  NOT NULL,
    currency      VARCHAR(3)     NOT NULL DEFAULT 'CAD',
    FOREIGN KEY (pre_order_id) REFERENCES pre_orders(pre_order_id)
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Menus
INSERT INTO menus (name, effective_date) VALUES
    ('Spring Menu 2026', '2026-03-01'),
    ('Brunch Menu', '2026-03-01');

-- Menu Items (12 items)
INSERT INTO menu_items (menu_id, name, description, amount, currency, category, is_available, dietary_tags) VALUES
    (1, 'Caesar Salad',        'Romaine, parmesan, croutons',          14.50, 'CAD', 'APPETIZER',  TRUE,  'VEGETARIAN'),
    (1, 'French Onion Soup',   'Classic with gruyere crust',           12.00, 'CAD', 'APPETIZER',  TRUE,  'VEGETARIAN'),
    (1, 'Spring Rolls',        'Crispy vegetable rolls with dip',      11.00, 'CAD', 'APPETIZER',  TRUE,  'VEGAN'),
    (1, 'Grilled Salmon',      'With lemon butter and seasonal veg',   34.00, 'CAD', 'MAIN',       TRUE,  'GLUTEN_FREE'),
    (1, 'Beef Tenderloin',     '8oz with truffle mash and red wine jus',48.00, 'CAD', 'MAIN',      TRUE,  ''),
    (1, 'Mushroom Risotto',    'Wild mushroom, arborio, parmesan',     26.00, 'CAD', 'MAIN',       TRUE,  'VEGETARIAN,GLUTEN_FREE'),
    (1, 'Chicken Piccata',     'Pan-seared with capers and lemon',     28.00, 'CAD', 'MAIN',       TRUE,  ''),
    (1, 'Vegan Buddha Bowl',   'Quinoa, roasted veg, tahini',          22.00, 'CAD', 'MAIN',       TRUE,  'VEGAN,GLUTEN_FREE'),
    (1, 'Chocolate Lava Cake', 'Warm with vanilla ice cream',          11.00, 'CAD', 'DESSERT',    TRUE,  'VEGETARIAN'),
    (1, 'Crème Brûlée',        'Classic French vanilla custard',        9.50, 'CAD', 'DESSERT',    TRUE,  'VEGETARIAN,GLUTEN_FREE'),
    (1, 'Sparkling Water',     '750ml bottle',                          5.00, 'CAD', 'BEVERAGE',   TRUE,  'VEGAN,GLUTEN_FREE'),
    (1, 'House Red Wine',      'Glass of the week',                    12.00, 'CAD', 'BEVERAGE',   TRUE,  'VEGAN,GLUTEN_FREE');

-- Floor Sections
INSERT INTO floor_sections (section_name) VALUES
    ('Main Dining Room'),
    ('Patio'),
    ('Private Dining');

-- Dining Tables (12 tables)
INSERT INTO dining_tables (table_number, seating_capacity, table_type, status, section_id, position_x, position_y) VALUES
    ('T01', 2,  'INDOOR',  'AVAILABLE', 1, 1, 1),
    ('T02', 4,  'INDOOR',  'AVAILABLE', 1, 2, 1),
    ('T03', 4,  'INDOOR',  'AVAILABLE', 1, 3, 1),
    ('T04', 6,  'INDOOR',  'AVAILABLE', 1, 4, 1),
    ('T05', 8,  'INDOOR',  'AVAILABLE', 1, 5, 1),
    ('T06', 2,  'OUTDOOR', 'AVAILABLE', 2, 1, 1),
    ('T07', 4,  'OUTDOOR', 'AVAILABLE', 2, 2, 1),
    ('T08', 4,  'OUTDOOR', 'AVAILABLE', 2, 3, 1),
    ('T09', 6,  'OUTDOOR', 'MAINTENANCE', 2, 4, 1),
    ('T10', 10, 'BOOTH',   'AVAILABLE', 3, 1, 1),
    ('T11', 12, 'BOOTH',   'AVAILABLE', 3, 2, 1),
    ('T12', 4,  'BAR',     'AVAILABLE', 1, 6, 1);

-- Loyalty Accounts (10 accounts, customer IDs 1–10)
INSERT INTO loyalty_accounts (customer_id, points_balance, tier, enrollment_date) VALUES
    (1,  1500, 'SILVER',   '2024-01-15'),
    (2,  250,  'BRONZE',   '2024-03-20'),
    (3,  8200, 'PLATINUM', '2023-06-01'),
    (4,  3100, 'GOLD',     '2023-11-10'),
    (5,  480,  'BRONZE',   '2025-01-05'),
    (6,  2200, 'GOLD',     '2024-05-22'),
    (7,  900,  'SILVER',   '2024-07-14'),
    (8,  50,   'BRONZE',   '2025-08-30'),
    (9,  1100, 'SILVER',   '2024-09-19'),
    (10, 4500, 'GOLD',     '2023-12-25');

-- Points Transactions (12 sample records)
INSERT INTO points_transactions (account_id, points, transaction_type, reference_id, transaction_date) VALUES
    (1, 200,  'EARNED',   1,  '2026-01-10 19:00:00'),
    (1, 300,  'EARNED',   2,  '2026-01-25 20:30:00'),
    (1, -100, 'REDEEMED', 3,  '2026-02-01 18:00:00'),
    (2, 250,  'EARNED',   4,  '2026-02-14 21:00:00'),
    (3, 500,  'EARNED',   5,  '2026-01-05 12:30:00'),
    (3, -200, 'REDEEMED', 6,  '2026-01-20 19:45:00'),
    (4, 400,  'EARNED',   7,  '2026-02-20 20:00:00'),
    (5, 180,  'EARNED',   8,  '2026-02-28 19:00:00'),
    (6, 300,  'EARNED',   9,  '2026-01-15 18:30:00'),
    (7, 150,  'EARNED',   10, '2026-02-10 20:15:00'),
    (8, 50,   'EARNED',   11, '2026-03-01 13:00:00'),
    (9, 220,  'EARNED',   12, '2026-02-22 19:30:00');

-- Table Bookings (10 bookings)
INSERT INTO table_bookings (customer_id, table_id, reservation_date, time_slot_start, time_slot_end, party_size, status, loyalty_points_earned, created_at) VALUES
    (1,  2, '2026-03-10', '18:00:00', '20:00:00', 3, 'CONFIRMED',  200, '2026-03-01 10:00:00'),
    (2,  3, '2026-03-11', '19:00:00', '21:00:00', 4, 'PENDING',    0,   '2026-03-02 11:00:00'),
    (3,  4, '2026-03-12', '12:00:00', '14:00:00', 5, 'CONFIRMED',  300, '2026-03-02 09:00:00'),
    (4,  5, '2026-03-13', '19:30:00', '21:30:00', 6, 'CONFIRMED',  0,   '2026-03-03 14:00:00'),
    (5,  1, '2026-03-14', '20:00:00', '22:00:00', 2, 'PENDING',    0,   '2026-03-04 08:00:00'),
    (6,  7, '2026-03-15', '18:30:00', '20:30:00', 3, 'CONFIRMED',  0,   '2026-03-05 15:00:00'),
    (7, 10, '2026-03-16', '19:00:00', '21:00:00', 8, 'CONFIRMED',  0,   '2026-03-06 10:00:00'),
    (8, 12, '2026-03-17', '12:30:00', '14:30:00', 2, 'CANCELLED',  0,   '2026-03-06 12:00:00'),
    (9,  2, '2026-03-18', '20:00:00', '22:00:00', 4, 'PENDING',    0,   '2026-03-07 09:00:00'),
    (10, 4, '2026-03-20', '18:00:00', '20:00:00', 4, 'CONFIRMED',  0,   '2026-03-07 10:00:00');

-- Pre-orders (5 sample pre-orders)
INSERT INTO pre_orders (booking_id, total_amount, currency, status, submitted_at) VALUES
    (1, 86.00,  'CAD', 'CONFIRMED', '2026-03-01 10:30:00'),
    (3, 130.00, 'CAD', 'CONFIRMED', '2026-03-02 09:30:00'),
    (4, 96.00,  'CAD', 'SUBMITTED', '2026-03-03 14:30:00'),
    (6, 74.00,  'CAD', 'CONFIRMED', '2026-03-05 15:30:00'),
    (7, 192.00, 'CAD', 'DRAFT',     NULL);

-- Order Line Items
INSERT INTO order_line_items (pre_order_id, menu_item_id, quantity, unit_amount, currency) VALUES
    -- Pre-order 1 (booking 1): Caesar Salad x2 + Grilled Salmon x1 + Chocolate Lava Cake x1
    (1, 1, 2, 14.50, 'CAD'),
    (1, 4, 1, 34.00, 'CAD'),
    (1, 9, 1, 11.00, 'CAD'),
    -- Pre-order 2 (booking 3): Spring Rolls x2 + Beef Tenderloin x2
    (2, 3, 2, 11.00, 'CAD'),
    (2, 5, 2, 48.00, 'CAD'),
    -- Pre-order 3 (booking 4): Mushroom Risotto x2 + Chicken Piccata x2
    (3, 6, 2, 26.00, 'CAD'),
    (3, 7, 2, 28.00, 'CAD'),
    -- Pre-order 4 (booking 6): French Onion Soup x2 + Grilled Salmon x2
    (4, 2, 2, 12.00, 'CAD'),
    (4, 4, 2, 34.00, 'CAD'),
    -- Pre-order 5 (booking 7): Beef Tenderloin x4
    (5, 5, 4, 48.00, 'CAD');

-- Update table_bookings to link pre_order_id
UPDATE table_bookings SET pre_order_id = 1 WHERE booking_id = 1;
UPDATE table_bookings SET pre_order_id = 2 WHERE booking_id = 3;
UPDATE table_bookings SET pre_order_id = 3 WHERE booking_id = 4;
UPDATE table_bookings SET pre_order_id = 4 WHERE booking_id = 6;
UPDATE table_bookings SET pre_order_id = 5 WHERE booking_id = 7;
