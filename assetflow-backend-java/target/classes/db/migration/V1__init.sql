-- AssetFlow initial schema
-- Matches the JPA entities in com.assetflow.model

CREATE TABLE departments (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL UNIQUE,
    head_user_id          BIGINT,
    parent_department_id  BIGINT REFERENCES departments(id),
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL DEFAULT 'EMPLOYEE',
    department_id BIGINT REFERENCES departments(id),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP
);

ALTER TABLE departments
    ADD CONSTRAINT fk_department_head FOREIGN KEY (head_user_id) REFERENCES users(id);

CREATE TABLE asset_categories (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL UNIQUE,
    custom_fields VARCHAR(1000)
);

CREATE TABLE assets (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    category_id       BIGINT NOT NULL REFERENCES asset_categories(id),
    asset_tag         VARCHAR(50) NOT NULL UNIQUE,
    serial_number     VARCHAR(255) UNIQUE,
    acquisition_date  DATE,
    acquisition_cost  NUMERIC(14,2),
    condition         VARCHAR(255),
    location          VARCHAR(255),
    department_id     BIGINT REFERENCES departments(id),
    status            VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    shared_bookable   BOOLEAN NOT NULL DEFAULT FALSE,
    photo_url         VARCHAR(500),
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP
);

CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_assets_department ON assets(department_id);
CREATE INDEX idx_assets_category ON assets(category_id);

CREATE TABLE allocations (
    id                        BIGSERIAL PRIMARY KEY,
    asset_id                  BIGINT NOT NULL REFERENCES assets(id),
    employee_id               BIGINT REFERENCES users(id),
    department_id             BIGINT REFERENCES departments(id),
    allocated_date            DATE NOT NULL,
    expected_return_date      DATE,
    actual_return_date        DATE,
    status                    VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    return_condition_notes    VARCHAR(1000),
    transfer_requested_to_id  BIGINT REFERENCES users(id),
    transfer_requested_at     TIMESTAMP,
    transfer_approved_by_id   BIGINT REFERENCES users(id),
    created_at                TIMESTAMP NOT NULL,
    updated_at                TIMESTAMP
);

CREATE INDEX idx_allocations_asset ON allocations(asset_id);
CREATE INDEX idx_allocations_employee ON allocations(employee_id);
CREATE INDEX idx_allocations_status ON allocations(status);

CREATE TABLE bookings (
    id             BIGSERIAL PRIMARY KEY,
    asset_id       BIGINT NOT NULL REFERENCES assets(id),
    booked_by_id   BIGINT NOT NULL REFERENCES users(id),
    department_id  BIGINT REFERENCES departments(id),
    start_time     TIMESTAMP NOT NULL,
    end_time       TIMESTAMP NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    purpose        VARCHAR(500),
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP
);

CREATE INDEX idx_bookings_asset_time ON bookings(asset_id, start_time, end_time);
CREATE INDEX idx_bookings_booked_by ON bookings(booked_by_id);

CREATE TABLE maintenance_requests (
    id                  BIGSERIAL PRIMARY KEY,
    asset_id            BIGINT NOT NULL REFERENCES assets(id),
    raised_by_id        BIGINT NOT NULL REFERENCES users(id),
    issue_description   VARCHAR(2000) NOT NULL,
    priority            VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    photo_url           VARCHAR(500),
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_by_id      BIGINT REFERENCES users(id),
    rejection_reason    VARCHAR(1000),
    technician_id       BIGINT REFERENCES users(id),
    resolution_notes    VARCHAR(2000),
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP,
    resolved_at         TIMESTAMP
);

CREATE INDEX idx_maintenance_asset ON maintenance_requests(asset_id);
CREATE INDEX idx_maintenance_status ON maintenance_requests(status);

CREATE TABLE audit_cycles (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(255),
    scope_department_id   BIGINT REFERENCES departments(id),
    scope_location        VARCHAR(255),
    start_date            DATE NOT NULL,
    end_date              DATE NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at            TIMESTAMP NOT NULL,
    closed_at             TIMESTAMP
);

CREATE TABLE audit_cycle_auditors (
    audit_cycle_id  BIGINT NOT NULL REFERENCES audit_cycles(id),
    auditor_id      BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (audit_cycle_id, auditor_id)
);

CREATE TABLE audit_entries (
    id              BIGSERIAL PRIMARY KEY,
    audit_cycle_id  BIGINT NOT NULL REFERENCES audit_cycles(id),
    asset_id        BIGINT NOT NULL REFERENCES assets(id),
    auditor_id      BIGINT NOT NULL REFERENCES users(id),
    result          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes           VARCHAR(1000),
    verified_at     TIMESTAMP
);

CREATE INDEX idx_audit_entries_cycle ON audit_entries(audit_cycle_id);
CREATE INDEX idx_audit_entries_asset ON audit_entries(asset_id);
