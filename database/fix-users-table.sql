-- ========================================
-- TripWise - Update Users Table
-- Run this script to fix the users table structure
-- ========================================

USE tripwise_db;

-- First, check if users table has the old structure (id instead of user_id)
-- If so, we need to recreate it with the correct structure

-- Drop existing tables that depend on users (if they exist with old structure)
-- Be careful with this in production!

-- Create or update the users table with correct structure
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    user_type ENUM('VISITEUR', 'VOYAGEUR', 'EMPLOYE', 'RESPONSABLE', 'ADMIN') DEFAULT 'VOYAGEUR',
    date_of_birth DATE,
    nationality VARCHAR(50),
    passport_number VARCHAR(30),
    address TEXT,
    profile_image VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample users if table is empty
INSERT IGNORE INTO users (user_id, email, password_hash, first_name, last_name, phone_number, user_type, is_active) VALUES
(1, 'admin@tripwise.com', 'admin123', 'Admin', 'User', '+1234567890', 'ADMIN', TRUE),
(2, 'employee@tripwise.com', 'employee123', 'John', 'Employee', '+1234567891', 'EMPLOYE', TRUE),
(3, 'traveler@tripwise.com', 'traveler123', 'Jane', 'Traveler', '+1234567892', 'VOYAGEUR', TRUE),
(4, 'alice@example.com', 'alice123', 'Alice', 'Martin', '+1234567893', 'VOYAGEUR', TRUE),
(5, 'bob@example.com', 'bob123', 'Bob', 'Wilson', '+1234567894', 'VOYAGEUR', TRUE);

-- Create voyageurs table if not exists
CREATE TABLE IF NOT EXISTS voyageurs (
    voyageur_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    passeport VARCHAR(20),
    nationalite VARCHAR(50),
    date_naissance DATE,
    preferences_voyage TEXT,
    points_fidelite INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create voyageur profiles for existing VOYAGEUR type users
INSERT IGNORE INTO voyageurs (user_id, points_fidelite, created_at)
SELECT user_id, 0, NOW()
FROM users
WHERE user_type = 'VOYAGEUR'
AND user_id NOT IN (SELECT user_id FROM voyageurs);

-- Verify the data
SELECT 'Users' as 'Table', COUNT(*) as 'Count' FROM users
UNION ALL
SELECT 'Voyageurs', COUNT(*) FROM voyageurs
UNION ALL
SELECT 'Flights (vols)', COUNT(*) FROM vols
UNION ALL
SELECT 'Airports', COUNT(*) FROM aeroports;

SELECT '✅ Database updated successfully!' as Message;
