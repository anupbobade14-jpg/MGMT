/* ==========================================================================
   Society Management - API client (JWT + fetch wrapper)
   ========================================================================== */

// >>> EDIT THIS if your Java backend runs on a different host/port
const API_BASE = window.SM_API_BASE || 'http://localhost:8080';

const TOKEN_KEY = 'sm_access_token';
const USER_KEY  = 'sm_user';

const Auth = {
  save(authResp) {
    localStorage.setItem(TOKEN_KEY, authResp.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(authResp.user));
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
  token()  { return localStorage.getItem(TOKEN_KEY); },
  user()   { try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch(_) { return null; } },
  isLoggedIn() { return !!this.token(); },
  hasRole(...roles) {
    const u = this.user(); if (!u) return false;
    return roles.includes(u.role);
  },
  requireLogin(redirectTo = 'login.html') {
    if (!this.isLoggedIn()) { window.location.href = redirectTo; }
  },
  requireRole(roles, redirectTo = 'dashboard.html') {
    if (!this.isLoggedIn()) { window.location.href = 'login.html'; return; }
    if (!roles.includes(this.user()?.role)) { window.location.href = redirectTo; }
  },
  logout() {
    this.clear();
    window.location.href = 'login.html';
  }
};

async function apiFetch(path, opts = {}) {
  const url = path.startsWith('http') ? path : (API_BASE + path);
  const headers = Object.assign(
    { 'Accept': 'application/json' },
    opts.headers || {}
  );
  if (opts.body && !(opts.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  const tok = Auth.token();
  if (tok) headers['Authorization'] = 'Bearer ' + tok;

  const body = (opts.body && !(opts.body instanceof FormData) && typeof opts.body !== 'string')
              ? JSON.stringify(opts.body) : opts.body;

  const res = await fetch(url, {
    method: opts.method || 'GET',
    headers,
    body,
    mode: 'cors'
  });

  if (res.status === 401) {
    Auth.clear();
    if (!window.location.pathname.includes('login')) window.location.href = 'login.html';
    throw new Error('Session expired');
  }

  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch(_) { data = text; }

  if (!res.ok) {
    const msg = (data && data.message) || res.statusText || 'Request failed';
    throw new Error(msg);
  }
  return data;
}

const API = {
  // ---------- Auth ----------
  login:    (email, password) => apiFetch('/api/auth/login',    { method:'POST', body:{ email, password }}),
  register: (payload)         => apiFetch('/api/auth/register', { method:'POST', body: payload }),

  // ---------- Dashboard ----------
  dashboard: () => apiFetch('/api/dashboard'),

  // ---------- Buildings & Flats ----------
  buildings:     () => apiFetch('/api/buildings'),
  createBuilding:(b) => apiFetch('/api/buildings', { method:'POST', body:b }),
  flatsOf:       (id) => apiFetch(`/api/buildings/${id}/flats`),
  createFlat:    (f) => apiFetch('/api/buildings/flats', { method:'POST', body:f }),

  // ---------- Owners ----------
  owners:      () => apiFetch('/api/owners'),
  owner:       (id) => apiFetch(`/api/owners/${id}`),
  createOwner: (o) => apiFetch('/api/owners', { method:'POST', body:o }),
  updateOwner: (id, o) => apiFetch(`/api/owners/${id}`, { method:'PUT', body:o }),
  deleteOwner: (id) => apiFetch(`/api/owners/${id}`, { method:'DELETE' }),

  // ---------- Maintenance ----------
  maintenanceList: (status) => apiFetch(`/api/maintenance${status ? `?status=${status}` : ''}`),
  maintenanceOfFlat: (flatId) => apiFetch(`/api/maintenance/flat/${flatId}`),
  generateMaintenance: (payload) => apiFetch('/api/maintenance/generate', { method:'POST', body:payload }),

  // ---------- Payments ----------
  submitPayment: (data, proofFile) => {
    const fd = new FormData();
    fd.append('data', new Blob([JSON.stringify(data)], { type:'application/json' }));
    if (proofFile) fd.append('proof', proofFile);
    return apiFetch('/api/payments', { method:'POST', body:fd });
  },
  paymentsList:  (status) => apiFetch(`/api/payments${status ? `?status=${status}` : ''}`),
  reviewPayment: (id, approve, notes) => apiFetch(`/api/payments/${id}/review`, { method:'POST', body:{ approve, notes }}),
  receiptUrl:    (id) => `${API_BASE}/api/payments/${id}/receipt`,
  proofUrl:      (id) => `${API_BASE}/api/payments/${id}/proof`,

  // ---------- Finance ----------
  financeSummary: (from, to) => apiFetch(`/api/finance/summary?from=${from}&to=${to}`),
  expenses:       (from, to) => apiFetch(`/api/finance/expenses?from=${from}&to=${to}`),
  addExpense:     (e) => apiFetch('/api/finance/expenses', { method:'POST', body:e }),
  expenseCats:    () => apiFetch('/api/finance/expense-categories'),
  incomes:        (from, to) => apiFetch(`/api/finance/incomes?from=${from}&to=${to}`),
  addIncome:      (i) => apiFetch('/api/finance/incomes', { method:'POST', body:i }),
  incomeCats:     () => apiFetch('/api/finance/income-categories'),
  categoryReport: (from, to) => apiFetch(`/api/finance/category-expense-report?from=${from}&to=${to}`),

  // ---------- Notices & Events ----------
  notices:      () => apiFetch('/api/notices'),
  createNotice: (n) => apiFetch('/api/notices', { method:'POST', body:n }),
  deleteNotice: (id) => apiFetch(`/api/notices/${id}`, { method:'DELETE' }),
  events:       () => apiFetch('/api/notices/events'),
  createEvent:  (e) => apiFetch('/api/notices/events', { method:'POST', body:e }),

  // ---------- Complaints ----------
  complaints: (status) => apiFetch(`/api/complaints${status ? `?status=${status}` : ''}`),
  myComplaints: () => apiFetch('/api/complaints/mine'),
  createComplaint: (c) => apiFetch('/api/complaints', { method:'POST', body:c }),
  updateComplaint: (id, patch) => apiFetch(`/api/complaints/${id}`, { method:'PATCH', body:patch }),

  // ---------- Reports ----------
  ownerLedger: (id) => apiFetch(`/api/reports/owner-ledger/${id}`),
  pendingReport: () => apiFetch('/api/reports/pending'),
  collectionReport: () => apiFetch('/api/reports/collection'),
  collectionCsvUrl: () => `${API_BASE}/api/reports/collection.csv`,

  // ---------- Notifications ----------
  notifications: () => apiFetch('/api/notifications'),
  unread: () => apiFetch('/api/notifications/unread-count'),
  markRead: (id) => apiFetch(`/api/notifications/${id}/read`, { method:'PATCH' }),

  // ---------- Admin ----------
  users: () => apiFetch('/api/admin/users'),
  updateUserRole: (id, role) => apiFetch(`/api/admin/users/${id}/role`, { method:'PATCH', body:{ role }}),
  setUserActive:  (id, active) => apiFetch(`/api/admin/users/${id}/active?active=${active}`, { method:'PATCH' }),
  emailTemplates: () => apiFetch('/api/admin/email-templates'),
  updateEmailTemplate: (id, t) => apiFetch(`/api/admin/email-templates/${id}`, { method:'PUT', body:t }),
  settings: () => apiFetch('/api/admin/settings'),
  updateSetting: (key, value) => apiFetch(`/api/admin/settings/${key}`, { method:'PUT', body:{ value }}),
  auditLogs: () => apiFetch('/api/admin/audit-logs'),
  loginHistory: () => apiFetch('/api/admin/login-history'),
};

window.API = API;
window.Auth = Auth;
