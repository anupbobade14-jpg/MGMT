/* ==========================================================================
   Society Management - UI helpers (layout, toast, formatters)
   ========================================================================== */

// ---- Number & date formatters ----
const fmt = {
  money: (n) => 'Rs. ' + Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
  int:   (n) => Number(n || 0).toLocaleString('en-IN'),
  date:  (d) => d ? new Date(d).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' }) : '',
  dateTime: (d) => d ? new Date(d).toLocaleString('en-GB', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) : '',
  monthYear: (m, y) => new Date(y, m - 1, 1).toLocaleDateString('en-GB', { month:'long', year:'numeric' }),
};

// ---- Toast (uses Bootstrap 5 toasts) ----
function showToast(message, variant = 'primary') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    document.body.appendChild(container);
  }
  const bg = { success:'success', danger:'danger', warning:'warning', primary:'primary', info:'info' }[variant] || 'primary';
  const el = document.createElement('div');
  el.className = `toast align-items-center text-bg-${bg} border-0 show`;
  el.role = 'alert';
  el.innerHTML = `
    <div class="d-flex">
      <div class="toast-body">${message}</div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>`;
  container.appendChild(el);
  setTimeout(() => el.remove(), 5000);
}

function showSpinner(on = true) {
  let el = document.querySelector('.spinner-overlay');
  if (!el) {
    el = document.createElement('div');
    el.className = 'spinner-overlay';
    el.innerHTML = '<div class="spinner-border text-primary" style="width:3rem; height:3rem;" role="status"></div>';
    document.body.appendChild(el);
  }
  el.classList.toggle('active', on);
}

// ---- Sidebar builder (shared across all pages) ----
function renderLayout(activeNav, pageTitle) {
  const u = Auth.user();
  if (!u) return;
  const initials = (u.fullName || u.email || 'U').split(' ').map(s => s[0]).slice(0,2).join('').toUpperCase();
  const isAdmin = ['SUPER_ADMIN','SOCIETY_ADMIN','ACCOUNTANT','COMMITTEE'].includes(u.role);

  const links = [
    { key: 'dashboard', href: 'dashboard.html', icon: 'grid-1x2-fill', label: 'Dashboard' },
    { key: 'payments',  href: 'payments.html',  icon: 'wallet2',       label: 'My Maintenance' },
    { key: 'notices',   href: 'notices.html',   icon: 'megaphone-fill',label: 'Notice Board' },
    { key: 'complaints',href: 'complaints.html',icon: 'chat-square-dots-fill', label: 'Complaints' },
  ];
  const adminLinks = [
    { key: 'admin',            href: 'admin/index.html',    icon: 'speedometer2',       label: 'Admin Home' },
    { key: 'admin-owners',     href: 'admin/owners.html',   icon: 'people-fill',        label: 'Owners & Flats' },
    { key: 'admin-payments',   href: 'admin/payments.html', icon: 'check2-circle',      label: 'Payment Approvals' },
    { key: 'admin-finance',    href: 'admin/finance.html',  icon: 'cash-coin',          label: 'Finance' },
    { key: 'admin-reports',    href: 'admin/reports.html',  icon: 'file-earmark-bar-graph', label: 'Reports' },
  ];

  const isInAdmin = window.location.pathname.includes('/admin/');
  const prefix = isInAdmin ? '../' : '';

  const nav = links.map(l => `
    <a href="${prefix}${l.href}" class="nav-link ${activeNav === l.key ? 'active' : ''}">
      <i class="bi bi-${l.icon}"></i>${l.label}
    </a>
  `).join('');

  const adminNav = !isAdmin ? '' : `
    <div class="nav-section">Administration</div>
    ${adminLinks.map(l => `
      <a href="${isInAdmin ? '' : ''}${l.href.replace(isInAdmin ? 'admin/' : '', isInAdmin ? '' : 'admin/')}"
         class="nav-link ${activeNav === l.key ? 'active' : ''}">
        <i class="bi bi-${l.icon}"></i>${l.label}
      </a>
    `).join('')}
  `;

  document.getElementById('sm-sidebar').innerHTML = `
    <a href="${prefix}dashboard.html" class="brand">
      <i class="bi bi-building-fill-check"></i>
      <span>Society MGMT</span>
    </a>
    <div class="nav-section">Resident</div>
    ${nav}
    ${adminNav}
    <div class="mt-4 pt-3" style="border-top:1px solid rgba(255,255,255,.15)">
      <a href="#" onclick="Auth.logout(); return false;" class="nav-link">
        <i class="bi bi-box-arrow-right"></i>Sign out
      </a>
    </div>
  `;

  document.getElementById('sm-topbar').innerHTML = `
    <div class="d-flex align-items-center gap-2">
      <button class="btn btn-light sm-menu-toggle" onclick="document.querySelector('.sm-sidebar').classList.toggle('open')">
        <i class="bi bi-list"></i>
      </button>
      <h1>${pageTitle}</h1>
    </div>
    <div class="user-chip">
      <div class="avatar">${initials}</div>
      <div class="d-none d-md-block">
        <div style="font-weight:600; font-size:.9rem; line-height:1;">${u.fullName || u.email}</div>
        <div style="font-size:.75rem; color:var(--sm-muted);">${u.role.replace('_',' ')}</div>
      </div>
    </div>
  `;
}

window.fmt = fmt;
window.showToast = showToast;
window.showSpinner = showSpinner;
window.renderLayout = renderLayout;
