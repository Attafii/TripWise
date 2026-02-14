-- ===================================================================
-- Create Admin Account for TripWise
-- Run this in phpMyAdmin or MySQL Workbench
-- ===================================================================

USE tripwise_db;

-- Delete existing admin if exists (to avoid duplicates)
DELETE FROM users WHERE email = 'admin@tripwise.com';

-- Insert admin account with BCrypt hashed password
-- Password: Admin123!
-- BCrypt hash generated with rounds=10
INSERT INTO users (
    email, 
    password_hash, 
    first_name, 
    last_name, 
    phone_number, 
    user_type, 
    date_of_birth,
    nationality,
    passport_number,
    address,
    is_active, 
    created_at, 
    updated_at
) VALUES (
    'admin@tripwise.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkO', -- Admin123!
    'System',
    'Administrator',
    '+1 (555) 000-0000',
    'ADMIN',
    '1990-01-01',
    'United States',
    'ADMIN001',
    '123 TripWise HQ, San Francisco, CA 94102',
    TRUE,
    NOW(),
    NOW()
);

-- Verify admin was created
SELECT 
    user_id, 
    email, 
    CONCAT(first_name, ' ', last_name) AS full_name,
    user_type,
    is_active,
    created_at
FROM users 
WHERE email = 'admin@tripwise.com';

-- Also create a test employee account
DELETE FROM users WHERE email = 'employee@tripwise.com';

INSERT INTO users (
    email, 
    password_hash, 
    first_name, 
    last_name, 
    phone_number, 
    user_type, 
    is_active, 
    created_at, 
    updated_at
) VALUES (
    'employee@tripwise.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkO', -- Admin123!
    'John',
    'Employee',
    '+1 (555) 111-1111',
    'EMPLOYE',
    TRUE,
    NOW(),
    NOW()
);

-- Create a test traveler account
DELETE FROM users WHERE email = 'traveler@tripwise.com';

INSERT INTO users (
    email, 
    password_hash, 
    first_name, 
    last_name, 
    phone_number, 
    user_type, 
    is_active, 
    created_at, 
    updated_at
) VALUES (
    'traveler@tripwise.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkO', -- Admin123!
    'Jane',
    'Traveler',
    '+1 (555) 222-2222',
    'VOYAGEUR',
    TRUE,
    NOW(),
    NOW()
);

-- Show all test accounts
SELECT 
    user_id,
    email,
    CONCAT(first_name, ' ', last_name) AS name,
    user_type,
    'Admin123!' AS password
FROM users 
WHERE email IN ('admin@tripwise.com', 'employee@tripwise.com', 'traveler@tripwise.com')
ORDER BY user_type;

COMMIT;

-- ===================================================================
-- CREDENTIALS FOR TESTING:
-- ===================================================================
-- ADMIN:     admin@tripwise.com     / Admin123!
-- EMPLOYEE:  employee@tripwise.com  / Admin123!
-- TRAVELER:  traveler@tripwise.com  / Admin123!
-- ===================================================================
