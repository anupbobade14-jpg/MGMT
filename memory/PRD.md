# Society Management System — PRD

## Original Problem Statement
Build a Society / Building Maintenance Management Website with 18 modules including
dashboard, owner management, maintenance module with approval workflow, finance,
reports, notice board, documents, complaints, visitor management, RBAC security
features, and admin panel. Tech stack: **Java + Spring Boot + PostgreSQL** (mandated).

## User Choices Locked
- **Scope**: All 18 modules from the specification
- **Auth**: Both — JWT (email/password) + Google Social Login
- **Email**: SmarterMail SMTP (custom SMTP)
- **Payment proof**: JPEG/PNG upload + admin approval + PDF receipt generation
- **Stack**: Java Spring Boot + PostgreSQL (**source code only** — cannot run in Emergent preview)

## Deliverable location
`/app/backend-java/` — 111 Java files, 14 SQL/YAML/config files, 1 Dockerfile, 1 compose, README.

## Architecture
- **Runtime**: Java 17 + Spring Boot 3.3.4 + Maven
- **Persistence**: PostgreSQL 14+ (Flyway migrations V1 schema + V2 seed)
- **Security**: Spring Security + JWT (HS512, 24h access + 7d refresh) + OAuth2 Google (profile-activated)
- **Email**: JavaMail (SMTP) with DB-backed HTML templates
- **PDF**: OpenPDF (LibrePDF) for receipts
- **CSV/Excel**: OpenCSV + Apache POI
- **API docs**: SpringDoc OpenAPI 2.6 (Swagger UI at `/api/swagger-ui.html`)
- **Scheduler**: Spring @Scheduled cron jobs (reminders + monthly bill generation)
- **Deployment**: Dockerfile + docker-compose.yml (bundled Postgres)

## User Personas
1. **Super Admin** — full system control
2. **Society Admin/Manager** — society-level administration
3. **Accountant** — finance / expense / collection
4. **Committee Member** — notices, events, complaint routing
5. **Flat Owner** — maintenance payment, complaints, visitors
6. **Tenant** — read-only (optional)
7. **Security Guard** — visitor entry/exit (optional)

## What's Implemented (Jan-05-2026)

### Modules (all 18 delivered)
1. Dashboard with cards & charts (`GET /api/dashboard`)
2. Owner management + family + vehicles (`/api/owners/**`)
3. Maintenance module w/ auto generation + late fee (`/api/maintenance/**`)
4. Approval workflow (submit → verify → approve/reject → PDF receipt + email)
5. Payment proof upload (JPEG/PNG) — `POST /api/payments` multipart
6. PDF receipts (OpenPDF) — `GET /api/payments/{id}/receipt`
7. SMTP email notifications with DB templates (7 seeded templates)
8. Reminder scheduler (7d/3d/due/weekly overdue) — cron `0 0 9 * * *`
9. Auto monthly bill generation — cron `0 0 1 1 * *`
10. Finance module: income, expense, categories (`/api/finance/**`)
11. Debit/Credit reports + CSV export (`/api/reports/**`)
12. Notice board + events (`/api/notices/**`)
13. Document management (upload / download / categorize)
14. Complaint management with ticket + status + assignment + rating
15. Visitor management with approval flow
16. RBAC (7 roles, method-level `@PreAuthorize`)
17. Audit log + login history tables + endpoints
18. Admin panel (users, settings, email templates, logs)

### Extras
- Global exception handler with structured error responses
- BCrypt password hashing (seeded admin: `admin@society.local` / `Admin@12345`)
- Flyway migrations with idempotent seed data
- H2 in-memory test profile (Spring context load test passes)
- Google OAuth via optional Spring profile `google`
- Docker Compose for one-command startup with bundled Postgres

## Build & test status
- `mvn compile` → PASS
- `mvn test` → PASS (1 test — Spring context boots + all 18 controllers wired correctly)
- `mvn package` → PASS (produces `society-management.jar`, 88 MB fat jar)

## Backlog / Future
- **P0**: React (or Bootstrap) frontend
- **P1**: WhatsApp/SMS integration (Twilio) — email adapter already abstracted
- **P1**: Payment gateway integration (UPI/Razorpay/Stripe) — endpoint hooks ready
- **P2**: MFA (TOTP) — user table has `mfa_secret` column ready
- **P2**: Parking / Club house / Event booking (module #18 future items)
- **P2**: Water & electricity meter reading
- **P2**: Mobile app (Android/iOS) — REST APIs already consumable

## Next Action Items
- Frontend build (Bootstrap 5 + jQuery or React) to consume these APIs
- Configure real SmarterMail SMTP credentials
- Optional: Google OAuth credentials + activate `google` profile
