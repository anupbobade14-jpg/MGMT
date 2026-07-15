# Society Management System — Java Spring Boot + PostgreSQL

A full-featured Society / Building Maintenance Management System backend built with **Java 17**, **Spring Boot 3.3**, **Spring Security + JWT**, **OAuth2 (Google)**, **PostgreSQL 14+**, **Flyway**, **JavaMail (SMTP)**, and **OpenPDF**.

Implements **all 18 modules** from the specification: dashboard, owners, flats, maintenance billing with approval workflow, PDF receipts, SMTP notifications, automated reminders (7d / 3d / due / weekly overdue), finance (income + expenses), reports (PDF/Excel/CSV), notices, events, documents, complaints, visitors, notifications, admin panel, RBAC, audit log, login history.

---

## Project layout

```
backend-java/
├── pom.xml                      # Maven build
├── Dockerfile                   # Multi-stage build
├── docker-compose.yml           # PostgreSQL + app
├── .env.example                 # Environment template
├── src/main/
│   ├── java/com/society/management/
│   │   ├── SocietyManagementApplication.java   # Main class
│   │   ├── config/              # Beans, JPA auditing, OpenAPI, logging
│   │   ├── security/            # Spring Security, JWT, OAuth2 (Google)
│   │   ├── entity/              # JPA entities (User, Flat, Owner, MaintenanceBill, Payment, …)
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── dto/                 # Request/response DTOs
│   │   ├── service/             # Business logic (Auth, Maintenance, Payment approval,
│   │   │                        #                Email, PDF, Reminders, Dashboard, Finance)
│   │   ├── controller/          # REST controllers for all 18 modules
│   │   ├── scheduler/           # Cron jobs (reminders + monthly bill generation)
│   │   ├── exception/           # Global exception handler
│   │   └── util/                # File storage, security helpers
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__init_schema.sql
│           └── V2__seed_data.sql
└── uploads/                     # Runtime file storage (proofs, receipts, documents)
```

---

## Prerequisites

* Java 17+
* Maven 3.9+
* PostgreSQL 14+ (or use `docker-compose` which starts one for you)
* SmarterMail account (or any SMTP server) for email notifications
* (Optional) Google Cloud project with OAuth2 credentials for Google sign-in

---

## Quick start (with Docker Compose)

```bash
cd backend-java
cp .env.example .env    # edit values (SMTP, JWT secret, Google creds)
docker compose up --build
```

The API starts on **http://localhost:8080** and PostgreSQL on **5432**.

Once the app is up:

* Swagger UI: `http://localhost:8080/api/swagger-ui.html`
* OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`
* Health: `http://localhost:8080/actuator/health`

---

## Quick start (local, no Docker)

```bash
# 1) Start PostgreSQL and create the DB
createdb society_db
createuser society_user --pwprompt        # password: society_pass

# 2) Configure env
cd backend-java
cp .env.example .env         # then edit values

# 3) Build & run
mvn spring-boot:run
# or
mvn -DskipTests package
java -jar target/society-management.jar
```

Flyway will auto-apply `V1__init_schema.sql` and `V2__seed_data.sql` on first run.

---

## Default credentials (seeded)

| Role         | Email                  | Password       |
| ------------ | ---------------------- | -------------- |
| Super Admin  | `admin@society.local`  | `Admin@12345`  |

> The seed inserts a BCrypt hash of `Admin@12345`. Change immediately in production.

---

## Environment variables

| Variable | Description | Default |
| --- | --- | --- |
| `DB_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/society_db` |
| `DB_USERNAME` / `DB_PASSWORD` | DB creds | `society_user` / `society_pass` |
| `SERVER_PORT` | HTTP port | `8080` |
| `JWT_SECRET` | Base64-encoded 512-bit HMAC secret | placeholder — **CHANGE** |
| `SMTP_HOST` / `SMTP_PORT` | SmarterMail SMTP host + port | `mail.example.com` / `587` |
| `SMTP_USERNAME` / `SMTP_PASSWORD` | SMTP creds | — |
| `MAIL_FROM` / `MAIL_FROM_NAME` | From address & display | `noreply@society.local` |
| `MAIL_ENABLED` | Toggle outbound mail | `true` |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth2 (optional) | — |
| `FRONTEND_BASE_URL` | Where to redirect after Google login | `http://localhost:3000` |
| `CORS_ORIGINS` | Comma-separated allowed origins | `http://localhost:3000,http://localhost:5173` |
| `UPLOAD_DIR` | Base directory for uploads | `./uploads` |

Set them in `.env` (loaded by `docker compose`) or export before `mvn spring-boot:run`.

---

## Google OAuth setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/), create a project.
2. `APIs & Services` → `Credentials` → *Create Credentials* → **OAuth Client ID** → **Web application**.
3. Add **Authorized redirect URI**: `http://localhost:8080/api/auth/oauth2/callback/google`  (production: your https domain).
4. Copy Client ID + Secret into `.env`.
5. Start the app. Users can now:  `GET /oauth2/authorize/google` → Google login → app receives token → redirects to `${FRONTEND_BASE_URL}/oauth2/callback?token=<jwt>`.

If Google creds are blank the OAuth login endpoint is simply unavailable — JWT login/registration keeps working.

---

## Modules & Endpoints (summary)

| Module | Base URL | Notes |
| --- | --- | --- |
| Auth (JWT) | `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/refresh` | Roles: `SUPER_ADMIN`, `SOCIETY_ADMIN`, `ACCOUNTANT`, `COMMITTEE`, `OWNER`, `TENANT`, `SECURITY` |
| Auth (Google) | `GET /oauth2/authorize/google` | Redirects to Google → back with JWT |
| Dashboard | `GET /api/dashboard` | Cards + charts + notices + events |
| Buildings & Flats | `/api/buildings`, `/api/buildings/{id}/flats`, `/api/buildings/flats` |  |
| Owners | `/api/owners`, `/api/owners/{id}/family`, `/api/owners/{id}/vehicles` |  |
| Maintenance | `POST /api/maintenance/generate`, `GET /api/maintenance?status=PENDING` | Auto-generation cron: 1st of month 01:00 |
| Payments | `POST /api/payments` (multipart: `data`+`proof`), `POST /api/payments/{id}/review`, `GET /api/payments/{id}/receipt` | Approval workflow, PDF receipt |
| Finance | `/api/finance/summary`, `/api/finance/expenses`, `/api/finance/incomes`, `/api/finance/category-expense-report` | Debit/Credit summary |
| Reports | `/api/reports/owner-ledger/{id}`, `/api/reports/pending`, `/api/reports/collection`, `/api/reports/collection.csv` | CSV export |
| Notices & Events | `/api/notices`, `/api/notices/events` |  |
| Documents | `POST /api/documents` (multipart), `GET /api/documents`, `GET /api/documents/{id}/download` | AGM/Audit/Rules/Vendor/Receipt |
| Complaints | `/api/complaints`, `/api/complaints/mine`, `PATCH /api/complaints/{id}` | Ticket, priority, status |
| Visitors | `/api/visitors`, `PATCH /api/visitors/{id}/approve?approve=true`, `PATCH /api/visitors/{id}/exit` |  |
| Notifications | `/api/notifications`, `/api/notifications/unread-count`, `PATCH /api/notifications/{id}/read` | In-app |
| Admin panel | `/api/admin/users`, `/api/admin/settings`, `/api/admin/email-templates`, `/api/admin/audit-logs`, `/api/admin/login-history` | RBAC-locked |

Full auto-generated docs: **Swagger UI at `/api/swagger-ui.html`**.

---

## Approval workflow (payment)

```
Owner uploads jpg/png proof → POST /api/payments (multipart)
   ↓
Payment saved with status UNDER_VERIFICATION
Maintenance bill moved to UNDER_VERIFICATION
Owner emailed: "Payment received - under verification"
   ↓
Admin reviews: POST /api/payments/{id}/review  { "approve": true/false, "notes": "..." }
   ↓ approved                                  ↓ rejected
- Payment.status = APPROVED                    - Payment.status = REJECTED
- Bill.status    = APPROVED                    - Bill.status    = PENDING (retryable)
- Receipt No generated (RCPT-YYYYMM-NNNNNN)    - Owner emailed rejection
- PDF Receipt generated (OpenPDF)
- Owner emailed approval + receipt link
```

Reject reasons flow back to the owner via email; the maintenance can be re-submitted.

---

## Automated jobs

Configured in `application.yml`:

* `app.maintenance.reminder-cron` (default `0 0 9 * * *` — 09:00 daily)
  Sends **DUE_REMINDER** to bills due in 7 / 3 / 0 days.
  Every Monday also sends **LATE_FEE_REMINDER** for overdue bills.
* `app.maintenance.generation-cron` (default `0 0 1 1 * *` — 01:00 on 1st of every month)
  Generates maintenance bills for all occupied flats (using flat's monthly amount).

Manual generation:
```
POST /api/maintenance/generate
{
  "month": 3,
  "year": 2026,
  "overrideAmount": 2500,
  "dueDate": "2026-03-10"
}
```

---

## Security features

* Role-Based Access Control via `@PreAuthorize`
* JWT access (24h) + refresh (7d) tokens (`HS512`)
* BCrypt password hashing
* Login history + audit log tables (`login_history`, `audit_logs`)
* CORS whitelist
* Stateless session (no cookies)
* HTTPS ready: terminate TLS at reverse proxy (Apache/Nginx) — see below
* Multi-Factor Authentication: fields `mfa_enabled`, `mfa_secret` in `users` table — hook for TOTP (extension point)
* IP restriction: extend `JwtAuthFilter` with an allow-list

### Recommended reverse-proxy (Apache)

```apache
<VirtualHost *:443>
  ServerName society.example.com
  SSLEngine on
  SSLCertificateFile      /etc/letsencrypt/live/society.example.com/fullchain.pem
  SSLCertificateKeyFile   /etc/letsencrypt/live/society.example.com/privkey.pem

  ProxyPreserveHost On
  ProxyPass        /  http://127.0.0.1:8080/
  ProxyPassReverse /  http://127.0.0.1:8080/
</VirtualHost>
```

---

## Database backup

Daily backup via `cron`:
```bash
0 2 * * * docker exec society_postgres pg_dump -U society_user society_db | gzip > /backups/society_$(date +\%Y\%m\%d).sql.gz
```
Restore:
```bash
gunzip < backup.sql.gz | docker exec -i society_postgres psql -U society_user -d society_db
```

---

## Testing

```bash
mvn test
```

`AuthController` integration test uses H2 in-memory DB (auto-configured via `spring-boot-starter-test`).

---

## Extending

* **WhatsApp / SMS:** Implement Twilio/WhatsApp adapters in `service/`; the reminder cron already calls `EmailService.sendTemplate`, add a `SmsService` next to it.
* **Payment Gateway (UPI / Razorpay / Stripe):** Add a `GatewayService` and a `/api/payments/gateway/webhook` endpoint that auto-approves upon successful webhook.
* **QR-code payment:** Generate a `data:image/png;base64,…` QR containing UPI intent per bill.
* **Mobile app:** All endpoints are REST + JWT — reuse for Android/iOS.

---

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| `FlywayValidateException` | Delete DB volume (`docker compose down -v`) or bump migration version |
| `Unable to obtain JDBC Connection` | Verify `DB_URL`, DB running, credentials |
| Login always 401 | Check `JWT_SECRET` matches between restarts; check `is_active=true` |
| SmarterMail rejects | Test with `openssl s_client -starttls smtp -connect $SMTP_HOST:587` — verify DKIM/DMARC on your domain |
| Google OAuth 401 | Redirect URI in Google Console must exactly match `http(s)://<host>:<port>/api/auth/oauth2/callback/google` |

---

## License

Proprietary — © 2026 Society Management System. All rights reserved.
