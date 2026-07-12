# AssetFlow Backend — Person B: Core Services & Business Logic

This covers the full service-layer scope: repositories, services, DTOs, exceptions, and the
asset-tag utility, wired together as a compilable Spring Boot module. `pom.xml`,
`application.properties`, the Flyway migration, and a minimal `AssetFlowApplication.java` are
included so the module builds and runs standalone — hand these off / merge them with whatever
your teammates produce for `config/` (Security, JWT) and any REST controllers.

## What's implemented

**model/** — 8 JPA entities (`User`, `Department`, `AssetCategory`, `Asset`, `Allocation`,
`Booking`, `MaintenanceRequest`, `AuditCycle`, `AuditEntry`) plus one enum per state machine
(`AssetStatus`, `AllocationStatus`, `BookingStatus`, `MaintenanceStatus`, `AuditCycleStatus`,
`AuditResult`, `UserRole`, `AccountStatus`, `MaintenancePriority`).

**repository/** — Spring Data interfaces for all 7 entities named in the spec, plus
`AssetCategoryRepository` and `AuditCycleRepository` (split out from `AuditRepository`, which
per the brief handles `AuditEntry`).

**service/** — all 6 services with the business rules called out in the brief:
- `AssetService` — asset tag auto-generation (`AF-0001`…), serial-number uniqueness, and an
  explicit allowed-transitions map for the asset lifecycle (e.g. `RETIRED` can only go to
  `DISPOSED`; `DISPOSED` is terminal).
- `AllocationService` — blocks double-allocation with `AssetAlreadyAllocatedException`
  (carries the current holder's name, per the Priya/Raj example in the brief), full
  Requested → Approved → Re-allocated transfer flow, return with condition check-in, and
  `getOverdueAllocations()`.
- `BookingService` — half-open interval overlap check (`start < otherEnd AND otherStart < end`)
  so a 10:00 booking right after a 9:00–10:00 one is allowed, matching the Room B2 example.
  Includes reschedule (re-validates against everyone *except* itself) and a
  `refreshBookingStatuses()` sweep for Upcoming → Ongoing → Completed.
- `MaintenanceService` — enforces Pending → Approved/Rejected → Technician Assigned →
  In Progress → Resolved, flips the asset to `UNDER_MAINTENANCE`/`AVAILABLE` at the right
  points, and restricts approve/reject to Asset Manager or Admin.
- `AuditService` — cycle creation with one-or-more auditors, per-asset entries
  (Verified/Missing/Damaged), `generateDiscrepancyReport()`, and `closeAuditCycle()` which
  locks the cycle and flips confirmed-missing assets to `LOST`.
- `DepartmentService` — CRUD, head assignment, parent/child hierarchy, activate/deactivate.

**dto/** — one DTO per service input, plus `ReturnDTO`, `TransferDTO`, `RescheduleDTO`,
`AuditCycleDTO`, `AuditEntryDTO`, `DepartmentDTO`, and `DiscrepancyDTO` (the report row shape).

**exception/** — the 3 named in the spec (`AssetAlreadyAllocatedException`,
`BookingConflictException`, `ResourceNotFoundException`) plus a few more the business rules
needed: `InvalidStatusTransitionException`, `DuplicateResourceException`,
`UnauthorizedActionException`, `InvalidBookingException`. None of these are wired to HTTP
status codes yet — that belongs in a `@ControllerAdvice` on the API side.

**util/AssetTagGenerator.java** — pure formatter (`AF-0001`, `AF-0002`, …); `AssetService`
owns the actual "what's the next number" query against the repository so the generator itself
stays trivially unit-testable.

## Not included (intentionally out of scope for this role)
- `config/SecurityConfig.java`, `config/JwtConfig.java` — stubs are *not* included; the
  `AssetFlowApplication.java` here is a bare-minimum placeholder so the module compiles.
  Without a `SecurityConfig`, Spring Security's auto-config will lock down every endpoint by
  default once someone adds controllers — that's expected until the security teammate fills
  this in.
- REST controllers — services are ready to be called from controllers directly.
- Notification/audit-log dispatch (Screen 10) — services return the changed entities;
  hook notification sends wherever the API layer catches these calls.

## Running it standalone
The main `application.properties` targets Postgres + Flyway. For a quick local check without
either, activate the `test` profile (`--spring.profiles.active=test`), which switches to
in-memory H2 with Hibernate auto-DDL instead of Flyway.
