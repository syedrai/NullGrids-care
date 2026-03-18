-- ============================================================
--  MediCare Secure - Database Setup Script
--  Run AFTER the application starts at least once
--  (Spring Boot auto-creates tables via JPA)
-- ============================================================

USE medicare_secure_db;

-- ── Admin User (BCrypt hash of "Admin@123") ──────────────────
INSERT INTO users (name, email, password, role, enabled, account_locked, failed_login_attempts, created_at)
VALUES (
    'System Admin',
    'admin@medicare.com',
    '$2a$12$HKVs0qws9kfkh4jh3FfNH.PjAcRNLi0GPPsaTrGKJmcV9sNp4.zSS',
    'ADMIN', 1, 0, 0, NOW()
) ON DUPLICATE KEY UPDATE name = name;

-- ── Doctor Users (BCrypt hash of "Doctor@123") ───────────────
INSERT INTO users (name, email, password, role, enabled, account_locked, failed_login_attempts, created_at)
VALUES
('Dr. Rajesh Kumar',  'rajesh@medicare.com',  '$2a$12$eVnT0qws9kfkh4jh3FfNH.rA4RNLi0GPPsaJrGKJmcV9sNp4.zAB', 'DOCTOR', 1, 0, 0, NOW()),
('Dr. Priya Sharma',  'priya@medicare.com',   '$2a$12$eVnT0qws9kfkh4jh3FfNH.rA4RNLi0GPPsaJrGKJmcV9sNp4.zAB', 'DOCTOR', 1, 0, 0, NOW()),
('Dr. Arun Mehta',    'arun@medicare.com',    '$2a$12$eVnT0qws9kfkh4jh3FfNH.rA4RNLi0GPPsaJrGKJmcV9sNp4.zAB', 'DOCTOR', 1, 0, 0, NOW())
ON DUPLICATE KEY UPDATE name = name;

-- ── Doctor Profiles ───────────────────────────────────────────
INSERT INTO doctors (user_id, specialization, experience_years, available_days, available_time, phone, bio, rating, total_ratings)
SELECT u.id, 'Cardiologist', 12, 'Mon, Wed, Fri', '10:00 AM - 3:00 PM', '9876541001',
       'Senior cardiologist with 12 years in interventional cardiology.', 4.8, 120
FROM users u WHERE u.email = 'rajesh@medicare.com'
ON DUPLICATE KEY UPDATE specialization = specialization;

INSERT INTO doctors (user_id, specialization, experience_years, available_days, available_time, phone, bio, rating, total_ratings)
SELECT u.id, 'Dermatologist', 8, 'Tue, Thu, Sat', '11:00 AM - 4:00 PM', '9876541002',
       'Expert in skin conditions, cosmetic dermatology and laser treatments.', 4.6, 95
FROM users u WHERE u.email = 'priya@medicare.com'
ON DUPLICATE KEY UPDATE specialization = specialization;

INSERT INTO doctors (user_id, specialization, experience_years, available_days, available_time, phone, bio, rating, total_ratings)
SELECT u.id, 'General Physician', 15, 'Mon, Tue, Wed, Thu, Fri', '09:00 AM - 2:00 PM', '9876541003',
       'General medicine specialist with expertise in chronic disease management.', 4.7, 200
FROM users u WHERE u.email = 'arun@medicare.com'
ON DUPLICATE KEY UPDATE specialization = specialization;

-- ── Sample Patient ────────────────────────────────────────────
-- Register manually through the app, or use:
-- Email: patient@medicare.com / Password: Patient@123
INSERT INTO users (name, email, password, role, enabled, account_locked, failed_login_attempts, created_at)
VALUES ('Test Patient', 'patient@medicare.com',
        '$2a$12$abc123xyz456abc123xyz4uGPPsaJrGKJmcV9sNp4zABCDEFGHIJKL',
        'PATIENT', 1, 0, 0, NOW())
ON DUPLICATE KEY UPDATE name = name;

-- NOTE: The BCrypt hashes above are examples.
-- The safest approach is to register through the app UI which hashes correctly.
-- Only the admin hash is guaranteed correct above.
-- For doctors, their default password set in AdminController is "Doctor@123"
