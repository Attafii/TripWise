-- ========================================
-- TripWise Database Schema
-- MySQL 8.0.33
-- ========================================

-- Create Database
CREATE DATABASE IF NOT EXISTS tripwise_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE tripwise_db;

-- ========================================
-- Table: users
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: flights
-- ========================================
CREATE TABLE IF NOT EXISTS flights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    airline VARCHAR(100) NOT NULL,
    flight_number VARCHAR(20) NOT NULL,
    departure_city VARCHAR(100) NOT NULL,
    arrival_city VARCHAR(100) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    available_seats INT NOT NULL DEFAULT 0,
    status ENUM('scheduled', 'delayed', 'cancelled', 'completed') DEFAULT 'scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_departure_city (departure_city),
    INDEX idx_arrival_city (arrival_city),
    INDEX idx_departure_time (departure_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: hotels
-- ========================================
CREATE TABLE IF NOT EXISTS hotels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    rating DECIMAL(2, 1) DEFAULT 0.0,
    available_rooms INT NOT NULL DEFAULT 0,
    amenities TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city (city),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: cars
-- ========================================
CREATE TABLE IF NOT EXISTS cars (
    id INT AUTO_INCREMENT PRIMARY KEY,
    model VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    year INT,
    price_per_day DECIMAL(10, 2) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    location VARCHAR(100),
    features TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: flight_bookings
-- ========================================
CREATE TABLE IF NOT EXISTS flight_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    flight_id INT NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    number_of_passengers INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('confirmed', 'pending', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_booking_date (booking_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: hotel_bookings
-- ========================================
CREATE TABLE IF NOT EXISTS hotel_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    hotel_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_rooms INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('confirmed', 'pending', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_check_in_date (check_in_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: car_rentals
-- ========================================
CREATE TABLE IF NOT EXISTS car_rentals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    car_id INT NOT NULL,
    rental_start_date DATE NOT NULL,
    rental_end_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('confirmed', 'pending', 'cancelled', 'completed') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_rental_start_date (rental_start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Sample Data for Testing
-- ========================================

-- Insert sample users (password: "password123" - CHANGE THIS IN PRODUCTION!)
INSERT INTO users (first_name, last_name, email, password, phone, address) VALUES
('John', 'Doe', 'john.doe@example.com', 'password123', '+1234567890', '123 Main St, New York, NY'),
('Jane', 'Smith', 'jane.smith@example.com', 'password123', '+1987654321', '456 Oak Ave, Los Angeles, CA'),
('Ahmed', 'Attafi', 'ahmed.attafi@example.com', 'password123', '+216123456789', 'Tunis, Tunisia');

-- Insert sample flights
INSERT INTO flights (airline, flight_number, departure_city, arrival_city, departure_time, arrival_time, price, available_seats) VALUES
('Air France', 'AF1234', 'Paris', 'New York', '2026-02-01 08:00:00', '2026-02-01 11:00:00', 450.00, 150),
('Emirates', 'EK5678', 'Dubai', 'London', '2026-02-05 14:00:00', '2026-02-05 18:30:00', 650.00, 200),
('Tunisair', 'TU9012', 'Tunis', 'Paris', '2026-02-10 06:00:00', '2026-02-10 08:30:00', 180.00, 100),
('Lufthansa', 'LH3456', 'Berlin', 'Rome', '2026-02-15 10:00:00', '2026-02-15 12:00:00', 220.00, 120);

-- Insert sample hotels
INSERT INTO hotels (name, city, address, price_per_night, rating, available_rooms, amenities, description) VALUES
('Grand Hotel Paris', 'Paris', '10 Rue de la Paix, Paris', 250.00, 4.5, 50, 'WiFi, Pool, Spa, Restaurant', 'Luxury hotel in the heart of Paris'),
('Dubai Palace', 'Dubai', 'Sheikh Zayed Road, Dubai', 400.00, 4.8, 80, 'WiFi, Pool, Gym, Beach Access', 'Premium hotel with stunning views'),
('Hotel Carthage', 'Tunis', 'Avenue Habib Bourguiba, Tunis', 120.00, 4.2, 30, 'WiFi, Restaurant, City View', 'Modern hotel in downtown Tunis'),
('Rome Imperial', 'Rome', 'Via Veneto, Rome', 180.00, 4.3, 40, 'WiFi, Restaurant, Rooftop Bar', 'Historic hotel near the Colosseum');

-- Insert sample cars
INSERT INTO cars (model, brand, type, year, price_per_day, available, location, features) VALUES
('Corolla', 'Toyota', 'Sedan', 2024, 35.00, TRUE, 'Paris', 'Automatic, AC, GPS'),
('X5', 'BMW', 'SUV', 2024, 90.00, TRUE, 'Dubai', 'Automatic, Leather, Premium Sound'),
('Clio', 'Renault', 'Compact', 2023, 25.00, TRUE, 'Tunis', 'Manual, AC'),
('Model 3', 'Tesla', 'Electric', 2024, 85.00, TRUE, 'Rome', 'Autopilot, Premium Interior');

-- Display success message
SELECT 'Database setup completed successfully!' AS Status;
