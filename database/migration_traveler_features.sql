-- ===================================================================
-- TripWise Database - Traveler Experience Enhancement Migration
-- This script creates all tables needed for the traveler features
-- ===================================================================

USE tripwise_db;

-- ===================================================================
-- 1. DEALS & OFFERS TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS deals (
    deal_id INT PRIMARY KEY AUTO_INCREMENT,
    deal_type ENUM('DAILY_DEAL', 'WEEKLY_DEAL', 'FLASH_SALE', 'SEASONAL_OFFER', 'PACKAGE_DEAL') NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    item_type ENUM('HOTEL', 'FLIGHT', 'CAR', 'PACKAGE') NOT NULL,
    item_id INT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    original_price DECIMAL(10,2) NOT NULL,
    discounted_price DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    image_url VARCHAR(500),
    destination VARCHAR(200),
    available_slots INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    highlights TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_deal_type (deal_type),
    INDEX idx_item_type (item_type),
    INDEX idx_destination (destination),
    INDEX idx_active_dates (is_active, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 2. FLIGHT TRACKING TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS flight_tracking (
    tracking_id INT PRIMARY KEY AUTO_INCREMENT,
    vol_id INT NOT NULL,
    flight_number VARCHAR(20) NOT NULL,
    airline VARCHAR(100),
    departure_airport VARCHAR(100) NOT NULL,
    arrival_airport VARCHAR(100) NOT NULL,
    scheduled_departure DATETIME NOT NULL,
    actual_departure DATETIME,
    scheduled_arrival DATETIME NOT NULL,
    actual_arrival DATETIME,
    status ENUM('SCHEDULED', 'BOARDING', 'DEPARTED', 'IN_FLIGHT', 'LANDED', 'DELAYED', 'CANCELLED', 'DIVERTED') DEFAULT 'SCHEDULED',
    delay_minutes INT DEFAULT 0,
    gate VARCHAR(10),
    terminal VARCHAR(10),
    baggage VARCHAR(10),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    altitude DECIMAL(10,2),
    speed DECIMAL(10,2),
    remarks TEXT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vol_id) REFERENCES vols(vol_id) ON DELETE CASCADE,
    INDEX idx_flight_number (flight_number),
    INDEX idx_status (status),
    INDEX idx_departure_date (scheduled_departure)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 3. LUGGAGE TRACKING TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS luggage_tracking (
    luggage_id INT PRIMARY KEY AUTO_INCREMENT,
    reservation_id INT,
    baggage_tag VARCHAR(50) UNIQUE NOT NULL,
    voyageur_id INT NOT NULL,
    passenger_name VARCHAR(200) NOT NULL,
    flight_number VARCHAR(20) NOT NULL,
    status ENUM('CHECKED_IN', 'IN_TRANSIT', 'AT_DESTINATION', 'READY_FOR_PICKUP', 'PICKED_UP', 'DELAYED', 'LOST', 'DAMAGED') DEFAULT 'CHECKED_IN',
    current_location VARCHAR(200),
    destination VARCHAR(200) NOT NULL,
    weight DECIMAL(5,2),
    description TEXT,
    checked_in_at DATETIME NOT NULL,
    last_scanned DATETIME,
    scan_history TEXT,
    is_delayed BOOLEAN DEFAULT FALSE,
    is_lost BOOLEAN DEFAULT FALSE,
    contact_phone VARCHAR(20),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (voyageur_id) REFERENCES voyageurs(voyageur_id) ON DELETE CASCADE,
    INDEX idx_baggage_tag (baggage_tag),
    INDEX idx_flight_number (flight_number),
    INDEX idx_status (status),
    INDEX idx_voyageur (voyageur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 4. SEARCH HISTORY TABLE (for personalized recommendations)
-- ===================================================================
CREATE TABLE IF NOT EXISTS search_history (
    search_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    search_type ENUM('HOTEL', 'FLIGHT', 'CAR', 'PACKAGE') NOT NULL,
    destination VARCHAR(200),
    check_in_date DATE,
    check_out_date DATE,
    guests INT,
    price_range_min DECIMAL(10,2),
    price_range_max DECIMAL(10,2),
    filters TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_destination (user_id, destination),
    INDEX idx_search_type (search_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- 5. USER PREFERENCES TABLE
-- ===================================================================
CREATE TABLE IF NOT EXISTS user_preferences (
    preference_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    preferred_destinations TEXT,
    preferred_airlines TEXT,
    preferred_hotel_chains TEXT,
    budget_range_min DECIMAL(10,2),
    budget_range_max DECIMAL(10,2),
    preferred_amenities TEXT,
    room_preferences TEXT,
    meal_preferences TEXT,
    notification_email BOOLEAN DEFAULT TRUE,
    notification_sms BOOLEAN DEFAULT FALSE,
    notification_push BOOLEAN DEFAULT TRUE,
    language VARCHAR(10) DEFAULT 'en',
    currency VARCHAR(10) DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- SAMPLE DATA FOR TESTING
-- ===================================================================

-- Insert sample deals
INSERT INTO deals (deal_type, title, description, item_type, item_id, item_name, 
    original_price, discounted_price, discount_percentage, start_date, end_date, 
    destination, available_slots, highlights) VALUES
('DAILY_DEAL', 'Luxury Beachfront Resort - 50% OFF', 
    'Experience paradise at half the price! Includes breakfast and spa access.', 
    'HOTEL', 1, 'Paradise Beach Resort', 400.00, 200.00, 50.00, 
    NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'Maldives', 10, 
    'Free breakfast,Spa access,Beach view,Pool'),
('WEEKLY_DEAL', 'Paris Getaway - 3 Nights', 
    'Romantic escape to the City of Lights. Eiffel Tower view included!', 
    'HOTEL', 2, 'Hotel de Paris', 600.00, 420.00, 30.00, 
    NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Paris, France', 20, 
    'Eiffel view,Free WiFi,Breakfast included,City center'),
('FLASH_SALE', 'Last Minute Flight to Dubai', 
    'Limited seats! Fly to Dubai tomorrow at unbeatable prices.', 
    'FLIGHT', 1, 'Emirates EK203', 800.00, 550.00, 31.25, 
    NOW(), DATE_ADD(NOW(), INTERVAL 12 HOUR), 'Dubai, UAE', 5, 
    'Business class,Free meal,Extra legroom'),
('WEEKLY_DEAL', 'SUV Rental - Week Special', 
    'Rent a premium SUV for a week at special rates. Perfect for family trips!', 
    'CAR', 1, 'Toyota RAV4', 420.00, 315.00, 25.00, 
    NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Los Angeles, USA', 15, 
    'GPS included,Unlimited miles,Insurance,Child seats available');

-- ===================================================================
-- INDEXES FOR PERFORMANCE
-- ===================================================================
ALTER TABLE reservations_hotel ADD INDEX idx_voyageur_status (voyageur_id, statut_reservation);
ALTER TABLE reservations_hotel ADD INDEX idx_dates (date_checkin, date_checkout);

-- ===================================================================
-- VIEWS FOR COMMON QUERIES
-- ===================================================================

-- View: Active Bookings with User Info
CREATE OR REPLACE VIEW v_active_bookings AS
SELECT 
    r.reservation_id,
    r.numero_confirmation,
    u.user_id,
    u.email,
    CONCAT(u.first_name, ' ', u.last_name) AS guest_name,
    h.nom_hotel,
    h.ville AS hotel_city,
    r.date_checkin,
    r.date_checkout,
    r.nombre_nuits,
    r.prix_total,
    r.statut_reservation,
    r.date_reservation
FROM reservations_hotel r
JOIN voyageurs v ON r.voyageur_id = v.voyageur_id
JOIN users u ON v.user_id = u.user_id
JOIN hotels h ON r.hotel_id = h.hotel_id
WHERE r.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')
ORDER BY r.date_reservation DESC;

-- View: User Booking History
CREATE OR REPLACE VIEW v_booking_history AS
SELECT 
    u.user_id,
    CONCAT(u.first_name, ' ', u.last_name) AS guest_name,
    'HOTEL' AS booking_type,
    h.nom_hotel AS item_name,
    r.date_checkin AS start_date,
    r.date_checkout AS end_date,
    r.prix_total AS total_price,
    r.statut_reservation AS status,
    r.date_reservation AS booked_at
FROM reservations_hotel r
JOIN voyageurs v ON r.voyageur_id = v.voyageur_id
JOIN users u ON v.user_id = u.user_id
JOIN hotels h ON r.hotel_id = h.hotel_id
ORDER BY r.date_reservation DESC;

COMMIT;

-- ===================================================================
-- END OF MIGRATION SCRIPT
-- ===================================================================
SELECT 'Migration completed successfully! All traveler experience tables created.' AS Status;
