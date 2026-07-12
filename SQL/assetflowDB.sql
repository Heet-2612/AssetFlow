DROP DATABASE IF EXISTS assetflow;
CREATE DATABASE assetflow;
USE assetflow;

-- ======================================================
-- DEPARTMENTS
-- ======================================================

CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_department_id INT NULL,
    status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'
);

-- ======================================================
-- USERS
-- ======================================================

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),

    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    phone VARCHAR(20),

    role ENUM(
        'ADMIN',
        'ASSET_MANAGER',
        'DEPARTMENT_HEAD',
        'EMPLOYEE'
    ) DEFAULT 'EMPLOYEE',

    status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',

    department_id INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (department_id)
    REFERENCES departments(department_id)
);

ALTER TABLE departments
ADD COLUMN department_head_id INT NULL,
ADD CONSTRAINT fk_department_head
FOREIGN KEY (department_head_id)
REFERENCES users(user_id);

ALTER TABLE departments
ADD CONSTRAINT fk_parent_department
FOREIGN KEY(parent_department_id)
REFERENCES departments(department_id);

-- ======================================================
-- ASSET CATEGORIES
-- ======================================================

CREATE TABLE asset_categories (

    category_id INT AUTO_INCREMENT PRIMARY KEY,

    category_name VARCHAR(100) UNIQUE NOT NULL,

    description TEXT,

    warranty_months INT,

    status ENUM('ACTIVE','INACTIVE')
    DEFAULT 'ACTIVE'

);

-- ======================================================
-- ASSETS
-- ======================================================

CREATE TABLE assets (

    asset_id INT AUTO_INCREMENT PRIMARY KEY,

    asset_tag VARCHAR(50) UNIQUE NOT NULL,

    asset_name VARCHAR(150) NOT NULL,

    serial_number VARCHAR(100) UNIQUE,

    category_id INT,

    acquisition_date DATE,

    acquisition_cost DECIMAL(12,2),

    asset_condition VARCHAR(50),

    location VARCHAR(150),

    photo_url TEXT,

    is_bookable BOOLEAN DEFAULT FALSE,

    status ENUM(
        'AVAILABLE',
        'ALLOCATED',
        'RESERVED',
        'UNDER_MAINTENANCE',
        'LOST',
        'RETIRED',
        'DISPOSED'
    ) DEFAULT 'AVAILABLE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(category_id)
    REFERENCES asset_categories(category_id)

);

-- ======================================================
-- ASSET ALLOCATIONS
-- ======================================================

CREATE TABLE asset_allocations (

    allocation_id INT AUTO_INCREMENT PRIMARY KEY,

    asset_id INT NOT NULL,

    user_id INT,

    department_id INT,

    allocated_date DATE DEFAULT (CURRENT_DATE),

    expected_return_date DATE,

    actual_return_date DATE,

    allocation_status ENUM(
        'ALLOCATED',
        'RETURNED',
        'TRANSFER_PENDING',
        'OVERDUE'
    ) DEFAULT 'ALLOCATED',

    remarks TEXT,

    FOREIGN KEY(asset_id)
    REFERENCES assets(asset_id),

    FOREIGN KEY(user_id)
    REFERENCES users(user_id),

    FOREIGN KEY(department_id)
    REFERENCES departments(department_id)

);

-- ======================================================
-- TRANSFER REQUESTS
-- ======================================================

CREATE TABLE transfer_requests (

    transfer_id INT AUTO_INCREMENT PRIMARY KEY,

    asset_id INT,

    from_user INT,

    to_user INT,

    request_date DATETIME DEFAULT CURRENT_TIMESTAMP,

    approval_date DATETIME,

    approved_by INT,

    status ENUM(
        'PENDING',
        'APPROVED',
        'REJECTED'
    ) DEFAULT 'PENDING',

    remarks TEXT,

    FOREIGN KEY(asset_id)
    REFERENCES assets(asset_id),

    FOREIGN KEY(from_user)
    REFERENCES users(user_id),

    FOREIGN KEY(to_user)
    REFERENCES users(user_id),

    FOREIGN KEY(approved_by)
    REFERENCES users(user_id)

);

-- ======================================================
-- RESOURCE BOOKINGS
-- ======================================================

CREATE TABLE resource_bookings (

    booking_id INT AUTO_INCREMENT PRIMARY KEY,

    asset_id INT,

    booked_by INT,

    start_time DATETIME,

    end_time DATETIME,

    booking_status ENUM(
        'UPCOMING',
        'ONGOING',
        'COMPLETED',
        'CANCELLED'
    ) DEFAULT 'UPCOMING',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(asset_id)
    REFERENCES assets(asset_id),

    FOREIGN KEY(booked_by)
    REFERENCES users(user_id)

);

-- ======================================================
-- MAINTENANCE REQUESTS
-- ======================================================

CREATE TABLE maintenance_requests (

    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,

    asset_id INT,

    requested_by INT,

    approved_by INT,

    issue_description TEXT,

    priority ENUM(
        'LOW',
        'MEDIUM',
        'HIGH',
        'CRITICAL'
    ),

    status ENUM(
        'PENDING',
        'APPROVED',
        'REJECTED',
        'TECHNICIAN_ASSIGNED',
        'IN_PROGRESS',
        'RESOLVED'
    ) DEFAULT 'PENDING',

    technician VARCHAR(100),

    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    resolved_date TIMESTAMP NULL,

    FOREIGN KEY(asset_id)
    REFERENCES assets(asset_id),

    FOREIGN KEY(requested_by)
    REFERENCES users(user_id),

    FOREIGN KEY(approved_by)
    REFERENCES users(user_id)

);

-- ======================================================
-- AUDIT CYCLES
-- ======================================================

CREATE TABLE audit_cycles (

    audit_cycle_id INT AUTO_INCREMENT PRIMARY KEY,

    audit_name VARCHAR(150),

    department_id INT,

    location VARCHAR(150),

    start_date DATE,

    end_date DATE,

    status ENUM(
        'OPEN',
        'CLOSED'
    ) DEFAULT 'OPEN',

    FOREIGN KEY(department_id)
    REFERENCES departments(department_id)

);

-- ======================================================
-- AUDIT RECORDS
-- ======================================================

CREATE TABLE audit_records (

    audit_record_id INT AUTO_INCREMENT PRIMARY KEY,

    audit_cycle_id INT,

    asset_id INT,

    auditor_id INT,

    verification_status ENUM(
        'VERIFIED',
        'MISSING',
        'DAMAGED'
    ),

    remarks TEXT,

    verified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(audit_cycle_id)
    REFERENCES audit_cycles(audit_cycle_id),

    FOREIGN KEY(asset_id)
    REFERENCES assets(asset_id),

    FOREIGN KEY(auditor_id)
    REFERENCES users(user_id)

);

-- ======================================================
-- NOTIFICATIONS
-- ======================================================

CREATE TABLE notifications (

    notification_id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT,

    title VARCHAR(200),

    message TEXT,

    is_read BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id)
    REFERENCES users(user_id)

);

-- ======================================================
-- ACTIVITY LOGS
-- ======================================================

CREATE TABLE activity_logs (

    log_id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT,

    action VARCHAR(200),

    entity_name VARCHAR(100),

    entity_id INT,

    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    description TEXT,

    FOREIGN KEY(user_id)
    REFERENCES users(user_id)

);

-- ======================================================
-- ASSET HISTORY
-- ======================================================

CREATE TABLE asset_history (

    history_id INT AUTO_INCREMENT PRIMARY KEY,

    asset_id INT,

    action VARCHAR(100),

    performed_by INT,

    remarks TEXT,

    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(asset_id)
    REFERENCES assets(asset_id),

    FOREIGN KEY(performed_by)
    REFERENCES users(user_id)

);

-- ======================================================
-- DEFAULT ADMIN USER
-- Password should be replaced with a BCrypt hash
-- before using Spring Security authentication.
-- ======================================================

INSERT INTO users
(first_name,last_name,email,password,role,status)
VALUES
(
'Admin',
'User',
'admin@assetflow.com',
'admin123',
'ADMIN',
'ACTIVE'
);