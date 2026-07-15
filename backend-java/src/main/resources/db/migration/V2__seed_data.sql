-- =====================================================================
-- Seed data: default categories, admin user, email templates
-- Password for super admin below is BCrypt hash of "Admin@12345"
-- =====================================================================

INSERT INTO users (email, password_hash, full_name, phone, role, provider, is_active) VALUES
    ('admin@society.local',
     '$2b$10$8YmannhO37cEcPu.k4d1VeQgbKQWwv3Dk3ocC/63oqzAPye94N6Qq',
     'Super Admin', '9999999999', 'SUPER_ADMIN', 'LOCAL', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO expense_categories (name) VALUES
    ('Electricity'), ('Water'), ('Security Salary'), ('Housekeeping'),
    ('Lift Maintenance'), ('Gardening'), ('Repairs'), ('Generator'),
    ('Office Expenses'), ('Other')
ON CONFLICT (name) DO NOTHING;

INSERT INTO income_categories (name) VALUES
    ('Parking Charges'), ('Penalty'), ('Interest'), ('Rental'), ('Donation'), ('Other')
ON CONFLICT (name) DO NOTHING;

INSERT INTO email_templates (code, subject, body_html) VALUES
 ('MAINT_GENERATED',
  'Monthly Maintenance Bill Generated - {{month}} {{year}}',
  '<p>Dear {{ownerName}},</p><p>Your maintenance bill for <b>{{month}} {{year}}</b> of <b>₹ {{amount}}</b> has been generated.</p><p>Due date: <b>{{dueDate}}</b>.</p><p>Please login to the portal to pay.</p><p>Regards,<br/>Society Management</p>'),
 ('PAYMENT_RECEIVED',
  'Payment received - under verification',
  '<p>Dear {{ownerName}},</p><p>We have received your payment of <b>₹ {{amount}}</b> for {{month}} {{year}}. It is currently under verification.</p>'),
 ('PAYMENT_APPROVED',
  'Payment Approved - Receipt #{{receiptNo}}',
  '<p>Dear {{ownerName}},</p><p>Your maintenance payment for <b>{{month}} {{year}}</b> has been verified and approved successfully. Receipt No: <b>{{receiptNo}}</b>.</p><p>Thank you for your payment.</p>'),
 ('PAYMENT_REJECTED',
  'Payment Rejected',
  '<p>Dear {{ownerName}},</p><p>Your maintenance payment for <b>{{month}} {{year}}</b> has been <b>rejected</b>. Reason: {{reason}}.</p><p>Please re-submit valid proof.</p>'),
 ('DUE_REMINDER',
  'Reminder: Maintenance due on {{dueDate}}',
  '<p>Dear {{ownerName}},</p><p>This is a friendly reminder that your maintenance of <b>₹ {{amount}}</b> for {{month}} {{year}} is due on <b>{{dueDate}}</b>.</p>'),
 ('LATE_FEE_REMINDER',
  'Overdue: Late fee applied for {{month}} {{year}}',
  '<p>Dear {{ownerName}},</p><p>Your maintenance for {{month}} {{year}} is overdue. A late fee of <b>₹ {{lateFee}}</b> has been applied.</p>'),
 ('NOTICE_PUBLISHED',
  'New Notice: {{title}}',
  '<p>{{body}}</p>')
ON CONFLICT (code) DO NOTHING;

INSERT INTO app_settings (setting_key, setting_value) VALUES
    ('society.name', 'Green Valley Residency'),
    ('society.address', '123 MG Road, City'),
    ('society.contact_email', 'admin@society.local'),
    ('society.contact_phone', '9999999999'),
    ('maintenance.default_amount', '2500'),
    ('maintenance.late_fee', '100'),
    ('maintenance.due_day', '10')
ON CONFLICT (setting_key) DO NOTHING;

-- One sample building/flat so the app works out-of-the-box
INSERT INTO buildings (name, wing, address, total_floors) VALUES
    ('Tower A', 'A', 'Green Valley Residency, 123 MG Road', 10)
ON CONFLICT DO NOTHING;
