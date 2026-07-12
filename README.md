# AssetFlow - Enterprise Asset & Resource Management System

## Overview

AssetFlow is a full-stack Enterprise Asset & Resource Management System developed to simplify and digitize asset management within an organization. The application enables organizations to efficiently register, allocate, maintain, audit, and track assets through their complete lifecycle while providing secure role-based access and real-time operational insights.

This project was developed as part of a hackathon to demonstrate ERP architecture, scalable module design, and modern full-stack development using Java, Spring Boot, PostgreSQL, and React.

---

# Problem Statement

Many organizations still manage physical assets using spreadsheets and manual processes, leading to:

* Asset duplication
* Lost equipment
* Poor maintenance tracking
* Resource booking conflicts
* Lack of accountability
* Difficult auditing

AssetFlow addresses these challenges by providing a centralized ERP platform for managing organizational assets and shared resources.

---

# Features

## Authentication & Authorization

* User Registration
* Secure Login
* JWT Authentication
* Role-Based Access Control
* Password Encryption
* Session Management

### Roles

* Admin
* Asset Manager
* Department Head
* Employee

---

## Dashboard

* Assets Available
* Assets Allocated
* Active Bookings
* Maintenance Requests
* Pending Transfers
* Upcoming Returns
* KPI Cards
* Quick Actions

---

## Organization Management

### Department Management

* Create Department
* Update Department
* Delete Department
* Department Hierarchy
* Assign Department Head

### Employee Management

* Employee Directory
* Role Assignment
* Department Mapping
* Account Status Management

### Asset Category Management

* Electronics
* Furniture
* Vehicles
* Office Equipment
* Custom Categories

---

## Asset Management

* Register Assets
* Auto-generated Asset Tags
* Asset Categories
* Serial Number Tracking
* Acquisition Details
* Asset Condition
* Asset Location
* Upload Documents
* Asset Search & Filters
* Asset Lifecycle Tracking

### Asset Status

* Available
* Allocated
* Reserved
* Under Maintenance
* Lost
* Retired
* Disposed

---

## Asset Allocation

* Allocate Assets
* Transfer Assets
* Return Assets
* Allocation History
* Expected Return Date
* Conflict Detection
* Overdue Tracking

---

## Resource Booking

* Book Shared Resources
* Time Slot Booking
* Calendar View
* Booking Validation
* Overlap Prevention
* Booking Cancellation
* Booking Reminders

---

## Maintenance Management

* Raise Maintenance Request
* Approval Workflow
* Technician Assignment
* Progress Tracking
* Maintenance History
* Asset Status Updates

Workflow

Pending → Approved → Technician Assigned → In Progress → Resolved

---

## Asset Audit

* Create Audit Cycle
* Assign Auditors
* Verify Assets
* Missing Asset Detection
* Damaged Asset Reporting
* Discrepancy Reports
* Audit History

---

## Reports & Analytics

* Asset Utilization
* Department-wise Assets
* Maintenance Reports
* Booking Reports
* Asset Status Reports
* Export Reports

---

## Notifications

* Asset Allocation
* Booking Confirmation
* Booking Reminder
* Maintenance Updates
* Transfer Approval
* Audit Alerts
* Overdue Return Alerts

---

## Activity Logs

Tracks every important action performed by users including:

* Login
* Asset Registration
* Asset Allocation
* Booking
* Maintenance
* Audit
* Department Updates

---

# Technology Stack

## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* Maven
* JWT Authentication

## Frontend

* React
* Vite
* Material UI
* Axios
* React Router

## Database

* PostgreSQL

---

# Project Structure

```
AssetFlow

├── backend
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   ├── config
│   ├── security
│   └── resources
│
├── frontend
│   ├── components
│   ├── pages
│   ├── services
│   ├── context
│   ├── assets
│   └── App.jsx
│
└── database
    └── assetflow.sql
```

---

# Database

The system uses PostgreSQL with normalized relational tables.

Main tables include:

* users
* departments
* asset_categories
* assets
* asset_allocations
* transfer_requests
* resource_bookings
* maintenance_requests
* audit_cycles
* audit_records
* notifications
* activity_logs
* asset_history

---

# Installation

## Clone Repository

```bash
git clone https://github.com/Heet-2612/AssetFlow.git
```

---

## Backend

```bash
cd backend
```

Configure PostgreSQL in `application.properties`.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/assetflow
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
```

Run the application

```bash
mvn spring-boot:run
```

---

## Frontend

```bash
cd frontend

npm install

npm run dev
```

---

# API Modules

* Authentication API
* Department API
* Employee API
* Category API
* Asset API
* Allocation API
* Booking API
* Maintenance API
* Audit API
* Notification API
* Dashboard API
* Reports API

---

# Future Enhancements

* QR Code Asset Tracking
* Barcode Scanner
* Email Notifications
* AI-based Maintenance Prediction
* Mobile Application
* RFID Integration
* Cloud Deployment
* Advanced Analytics Dashboard

---

# Contributors

* Heet Modi
* Bhavya Mehta
* Aditya Rajput

---

# License

This project was developed for educational and hackathon purposes.

---

# Screens

* Login
* Dashboard
* Organization Setup
* Asset Registration
* Asset Allocation
* Resource Booking
* Maintenance
* Asset Audit
* Reports
* Notifications

---

# Acknowledgements

Developed as part of a hackathon to demonstrate Enterprise Resource Planning (ERP) concepts, secure role-based workflows, asset lifecycle management, and scalable software architecture using modern Java technologies.
