-- =====================================================================
-- Society Management System — Initial Schema (V1)
-- PostgreSQL 14+
-- =====================================================================

-- ---------- USERS & AUTH -------------------------------------------------
CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    email             VARCHAR(150) NOT NULL UNIQUE,
    password_hash     VARCHAR(255),
    full_name         VARCHAR(150) NOT NULL,
    phone             VARCHAR(30),
    role              VARCHAR(30)  NOT NULL,   -- SUPER_ADMIN, SOCIETY_ADMIN, ACCOUNTANT, COMMITTEE, OWNER, TENANT, SECURITY
    provider          VARCHAR(20)  NOT NULL DEFAULT 'LOCAL', -- LOCAL, GOOGLE
    provider_id       VARCHAR(150),
    profile_picture   VARCHAR(500),
    mfa_enabled       BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret        VARCHAR(255),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at     TIMESTAMPTZ,
    last_login_ip     VARCHAR(45),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_provider ON users(provider, provider_id);

CREATE TABLE login_history (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users(id) ON DELETE SET NULL,
    email         VARCHAR(150),
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(500),
    success       BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_login_history_user ON login_history(user_id);
CREATE INDEX idx_login_history_created ON login_history(created_at);

CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(80),
    entity_id    VARCHAR(80),
    details      TEXT,
    ip_address   VARCHAR(45),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- ---------- BUILDINGS / WINGS / FLATS -----------------------------------
CREATE TABLE buildings (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    wing         VARCHAR(50),
    address      VARCHAR(500),
    total_floors INT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE flats (
    id             BIGSERIAL PRIMARY KEY,
    building_id    BIGINT NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    flat_number    VARCHAR(30) NOT NULL,
    floor          INT,
    area_sqft      NUMERIC(10,2),
    bhk            VARCHAR(10),
    occupancy      VARCHAR(20) NOT NULL DEFAULT 'VACANT', -- OCCUPIED, VACANT, RENTED
    monthly_maintenance NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (building_id, flat_number)
);
CREATE INDEX idx_flats_occupancy ON flats(occupancy);

-- ---------- OWNERS / TENANTS --------------------------------------------
CREATE TABLE owners (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT REFERENCES users(id) ON DELETE SET NULL,
    flat_id            BIGINT NOT NULL REFERENCES flats(id) ON DELETE CASCADE,
    full_name          VARCHAR(150) NOT NULL,
    email              VARCHAR(150),
    phone              VARCHAR(30),
    alternate_phone    VARCHAR(30),
    emergency_contact  VARCHAR(150),
    move_in_date       DATE,
    is_primary         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_owners_flat ON owners(flat_id);
CREATE INDEX idx_owners_user ON owners(user_id);

CREATE TABLE tenants (
    id            BIGSERIAL PRIMARY KEY,
    flat_id       BIGINT NOT NULL REFERENCES flats(id) ON DELETE CASCADE,
    user_id       BIGINT REFERENCES users(id) ON DELETE SET NULL,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150),
    phone         VARCHAR(30),
    move_in_date  DATE,
    move_out_date DATE,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE family_members (
    id           BIGSERIAL PRIMARY KEY,
    owner_id     BIGINT NOT NULL REFERENCES owners(id) ON DELETE CASCADE,
    full_name    VARCHAR(150) NOT NULL,
    relation     VARCHAR(50),
    age          INT,
    phone        VARCHAR(30),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE vehicles (
    id             BIGSERIAL PRIMARY KEY,
    owner_id       BIGINT NOT NULL REFERENCES owners(id) ON DELETE CASCADE,
    vehicle_number VARCHAR(30) NOT NULL,
    vehicle_type   VARCHAR(30),  -- CAR, BIKE, SUV, EV
    make_model     VARCHAR(120),
    parking_slot   VARCHAR(30),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------- MAINTENANCE / PAYMENTS --------------------------------------
CREATE TABLE maintenance_bills (
    id              BIGSERIAL PRIMARY KEY,
    flat_id         BIGINT NOT NULL REFERENCES flats(id) ON DELETE CASCADE,
    owner_id        BIGINT REFERENCES owners(id) ON DELETE SET NULL,
    bill_month      INT NOT NULL,           -- 1-12
    bill_year       INT NOT NULL,           -- 2026
    amount          NUMERIC(12,2) NOT NULL,
    late_fee        NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(12,2) NOT NULL,
    due_date        DATE NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
       -- PENDING, UNDER_VERIFICATION, APPROVED, REJECTED, OVERDUE
    remarks         VARCHAR(500),
    generated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (flat_id, bill_month, bill_year)
);
CREATE INDEX idx_maint_status ON maintenance_bills(status);
CREATE INDEX idx_maint_due ON maintenance_bills(due_date);

CREATE TABLE payments (
    id                BIGSERIAL PRIMARY KEY,
    maintenance_id    BIGINT NOT NULL REFERENCES maintenance_bills(id) ON DELETE CASCADE,
    owner_id          BIGINT REFERENCES owners(id) ON DELETE SET NULL,
    amount            NUMERIC(12,2) NOT NULL,
    payment_mode      VARCHAR(30) NOT NULL, -- CASH, UPI, BANK_TRANSFER, CHEQUE, ONLINE
    transaction_ref   VARCHAR(120),
    payment_date      DATE NOT NULL,
    proof_file_path   VARCHAR(500),
    proof_content_type VARCHAR(80),
    status            VARCHAR(30) NOT NULL DEFAULT 'UNDER_VERIFICATION',
                                            -- UNDER_VERIFICATION, APPROVED, REJECTED
    reviewed_by       BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at       TIMESTAMPTZ,
    review_notes      VARCHAR(500),
    receipt_number    VARCHAR(50) UNIQUE,
    receipt_file_path VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_maint ON payments(maintenance_id);

-- ---------- FINANCE: INCOME (non-maintenance) & EXPENSES ----------------
CREATE TABLE income_categories (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE other_incomes (
    id            BIGSERIAL PRIMARY KEY,
    category_id   BIGINT REFERENCES income_categories(id) ON DELETE SET NULL,
    amount        NUMERIC(12,2) NOT NULL,
    income_date   DATE NOT NULL,
    description   VARCHAR(500),
    reference_no  VARCHAR(120),
    created_by    BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE expense_categories (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE expenses (
    id            BIGSERIAL PRIMARY KEY,
    category_id   BIGINT REFERENCES expense_categories(id) ON DELETE SET NULL,
    amount        NUMERIC(12,2) NOT NULL,
    expense_date  DATE NOT NULL,
    vendor        VARCHAR(150),
    description   VARCHAR(500),
    invoice_no    VARCHAR(120),
    invoice_file_path VARCHAR(500),
    payment_mode  VARCHAR(30),
    created_by    BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_expenses_cat ON expenses(category_id);

-- ---------- NOTICES / EVENTS --------------------------------------------
CREATE TABLE notices (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    body         TEXT NOT NULL,
    category     VARCHAR(40) NOT NULL DEFAULT 'GENERAL', -- GENERAL, MEETING, EMERGENCY, EVENT
    is_pinned    BOOLEAN NOT NULL DEFAULT FALSE,
    published_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    published_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMPTZ
);

CREATE TABLE events (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    event_date    TIMESTAMPTZ NOT NULL,
    location      VARCHAR(200),
    created_by    BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------- DOCUMENTS ----------------------------------------------------
CREATE TABLE documents (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    category     VARCHAR(50) NOT NULL, -- AGM, AUDIT, RULES, VENDOR, RECEIPT, OTHER
    file_path    VARCHAR(500) NOT NULL,
    content_type VARCHAR(80),
    file_size    BIGINT,
    uploaded_by  BIGINT REFERENCES users(id) ON DELETE SET NULL,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------- COMPLAINTS --------------------------------------------------
CREATE TABLE complaints (
    id                BIGSERIAL PRIMARY KEY,
    ticket_number     VARCHAR(30) UNIQUE NOT NULL,
    raised_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    flat_id           BIGINT REFERENCES flats(id) ON DELETE SET NULL,
    category          VARCHAR(60),  -- PLUMBING, ELECTRICAL, SECURITY, HOUSEKEEPING, LIFT, OTHER
    priority          VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    subject           VARCHAR(200) NOT NULL,
    description       TEXT,
    status            VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    assigned_to       BIGINT REFERENCES users(id) ON DELETE SET NULL,
    rating            INT,           -- 1..5 after close
    feedback          VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at       TIMESTAMPTZ
);
CREATE INDEX idx_complaints_status ON complaints(status);

-- ---------- VISITORS -----------------------------------------------------
CREATE TABLE visitors (
    id             BIGSERIAL PRIMARY KEY,
    visitor_name   VARCHAR(150) NOT NULL,
    phone          VARCHAR(30),
    purpose        VARCHAR(200),
    visit_type     VARCHAR(30), -- GUEST, DELIVERY, VENDOR, MAID
    vehicle_number VARCHAR(30),
    flat_id        BIGINT REFERENCES flats(id) ON DELETE SET NULL,
    entry_time     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    exit_time      TIMESTAMPTZ,
    approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    approved_by    BIGINT REFERENCES users(id) ON DELETE SET NULL,
    added_by       BIGINT REFERENCES users(id) ON DELETE SET NULL,
    photo_path     VARCHAR(500)
);
CREATE INDEX idx_visitors_flat ON visitors(flat_id);
CREATE INDEX idx_visitors_entry ON visitors(entry_time);

-- ---------- NOTIFICATIONS -----------------------------------------------
CREATE TABLE notifications (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    message      TEXT NOT NULL,
    category     VARCHAR(50),
    link_url     VARCHAR(500),
    is_read      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notif_user ON notifications(user_id, is_read);

-- ---------- EMAIL TEMPLATES / REMINDER SETTINGS --------------------------
CREATE TABLE email_templates (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(80) NOT NULL UNIQUE,   -- e.g. MAINT_GENERATED, PAYMENT_APPROVED
    subject      VARCHAR(200) NOT NULL,
    body_html    TEXT NOT NULL,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE app_settings (
    id           BIGSERIAL PRIMARY KEY,
    setting_key   VARCHAR(80) NOT NULL UNIQUE,
    setting_value TEXT,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
