# Society Management — Bootstrap 5 Frontend

Static HTML/CSS/JavaScript frontend for the Java Spring Boot backend. Zero build step — just copy files and open in a browser.

## Contents (12 files)

```
frontend/
├── index.html              redirects to login or dashboard
├── login.html              Sign-in page
├── register.html           New user registration
├── dashboard.html          Owner dashboard (cards + charts + notices/events)
├── payments.html           My maintenance bills + payment upload with proof
├── notices.html            Notice board + events
├── complaints.html         Complaint tracker
├── admin/
│   ├── index.html          Admin dashboard (finance charts + stats)
│   ├── owners.html         Owners/Flats/Buildings CRUD
│   ├── payments.html       Payment approval workflow (view proof, approve/reject, download receipt)
│   ├── finance.html        Expenses + Other income
│   └── reports.html        Owner ledger, pending report, collection report + CSV
└── assets/
    ├── css/app.css
    └── js/
        ├── api.js          API client + JWT auth
        └── app.js          Layout / toasts / formatters
```

## Quick start — Windows Server 2022

### 1. Copy folder to server
Copy `/frontend/*` to `C:\society\frontend\` on your Windows Server.

### 2. Point IIS to serve it
Admin PowerShell:
```powershell
Import-Module WebAdministration
Set-ItemProperty "IIS:\Sites\society" -Name PhysicalPath -Value "C:\society\frontend"
icacls "C:\society\frontend" /grant "IIS_IUSRS:(OI)(CI)(RX)"
iisreset
```

### 3. Update CORS in the Java service
Open `C:\society\service\society-management.xml` in Notepad, find and update:
```xml
<env name="CORS_ORIGINS" value="http://localhost,http://your-server-ip,https://society.yourdomain.com"/>
```
Restart:
```powershell
cd C:\society\service
.\society-management.exe stop; Start-Sleep 3; .\society-management.exe start
```

### 4. Open in browser
- Local: `http://localhost/login.html`
- LAN:   `http://<server-ip>/login.html`
- Web:   `https://society.yourdomain.com/login.html`

Login with `admin@society.local` / `Admin@12345`.

## Customization

- **Theme color** — edit `--sm-primary` in `assets/css/app.css`
- **Brand name** — edit "Society Management" in login/register/app.js
- **API URL** — edit `API_BASE` in `assets/js/api.js` (default `http://localhost:8080`)
