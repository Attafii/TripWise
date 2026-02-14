-- =========================================
-- TripWise - Seat Selection & Capacity Management Update
-- Run this script to add seat selection support
-- =========================================

USE tripwise_db;

-- =========================================
-- 1. Add seats table for managing individual seats
-- =========================================
CREATE TABLE IF NOT EXISTS seats (
    seat_id INT PRIMARY KEY AUTO_INCREMENT,
    vol_id INT NOT NULL,
    seat_number VARCHAR(5) NOT NULL,
    seat_row INT NOT NULL,
    seat_column CHAR(1) NOT NULL,
    seat_class ENUM('ECONOMIQUE', 'PREMIUM', 'BUSINESS', 'PREMIERE') NOT NULL DEFAULT 'ECONOMIQUE',
    seat_type ENUM('STANDARD', 'WINDOW', 'AISLE', 'MIDDLE', 'EXIT_ROW', 'EXTRA_LEGROOM') NOT NULL DEFAULT 'STANDARD',
    seat_status ENUM('AVAILABLE', 'OCCUPIED', 'BLOCKED', 'RESERVED') NOT NULL DEFAULT 'AVAILABLE',
    extra_price DECIMAL(10, 2) DEFAULT 0.00,
    has_extra_legroom BOOLEAN DEFAULT FALSE,
    is_exit_row BOOLEAN DEFAULT FALSE,
    reservation_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vol_id) REFERENCES vols(vol_id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations_vol(reservation_id) ON DELETE SET NULL,
    UNIQUE KEY unique_seat_per_flight (vol_id, seat_number)
);

-- =========================================
-- 2. Add email notifications log table
-- =========================================
CREATE TABLE IF NOT EXISTS email_notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    reservation_id INT NULL,
    email_type ENUM('BOOKING_CONFIRMATION', 'BOOKING_CANCELLATION', 'SEAT_CONFIRMATION',
                    'FLIGHT_REMINDER', 'BOARDING_PASS', 'PRICE_ALERT', 'GENERAL') NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations_vol(reservation_id) ON DELETE SET NULL
);

-- =========================================
-- 3. Add capacity snapshots table for analytics
-- =========================================
CREATE TABLE IF NOT EXISTS capacity_snapshots (
    snapshot_id INT PRIMARY KEY AUTO_INCREMENT,
    vol_id INT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_capacity INT NOT NULL,
    booked_seats INT NOT NULL,
    available_seats INT NOT NULL,
    occupancy_rate DECIMAL(5, 2) NOT NULL,
    first_class_available INT DEFAULT 0,
    business_available INT DEFAULT 0,
    premium_available INT DEFAULT 0,
    economy_available INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vol_id) REFERENCES vols(vol_id) ON DELETE CASCADE,
    INDEX idx_snapshot_date (snapshot_date),
    INDEX idx_vol_snapshot (vol_id, snapshot_date)
);

-- =========================================
-- 4. Add flight reports log table
-- =========================================
CREATE TABLE IF NOT EXISTS flight_reports (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    report_type ENUM('FLIGHTS', 'BOOKINGS', 'CAPACITY', 'REVENUE', 'ANALYTICS') NOT NULL,
    report_format ENUM('PDF', 'EXCEL', 'CSV') NOT NULL,
    generated_by INT NOT NULL,
    file_path VARCHAR(500),
    date_range_start DATE NULL,
    date_range_end DATE NULL,
    parameters JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (generated_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =========================================
-- 5. Update reservations_vol to ensure seat column exists
-- =========================================
-- Check if sieges_attribues column exists, if not add it
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns
                   WHERE table_schema = 'tripwise_db'
                   AND table_name = 'reservations_vol'
                   AND column_name = 'sieges_attribues');

SET @query = IF(@col_exists = 0,
    'ALTER TABLE reservations_vol ADD COLUMN sieges_attribues VARCHAR(100) NULL AFTER numero_confirmation',
    'SELECT "Column already exists"');

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================
-- 6. Create stored procedure to update seat availability
-- =========================================
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS sp_update_seat_availability(IN p_vol_id INT)
BEGIN
    DECLARE v_total INT;
    DECLARE v_available INT;

    -- Count available seats for the flight
    SELECT
        SUM(cv.places_disponibles) INTO v_available
    FROM classes_vol cv
    WHERE cv.vol_id = p_vol_id;

    -- Update flight's available seats
    UPDATE vols
    SET places_disponibles = COALESCE(v_available, 0)
    WHERE vol_id = p_vol_id;

    SELECT CONCAT('Updated flight ', p_vol_id, ' with ', v_available, ' available seats') AS result;
END //

DELIMITER ;

-- =========================================
-- 7. Create stored procedure to record capacity snapshot
-- =========================================
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS sp_record_capacity_snapshot()
BEGIN
    INSERT INTO capacity_snapshots (vol_id, snapshot_date, total_capacity, booked_seats,
                                    available_seats, occupancy_rate,
                                    first_class_available, business_available,
                                    premium_available, economy_available)
    SELECT
        v.vol_id,
        CURDATE(),
        v.capacite_totale,
        v.capacite_totale - v.places_disponibles,
        v.places_disponibles,
        ROUND((v.capacite_totale - v.places_disponibles) / v.capacite_totale * 100, 2),
        COALESCE((SELECT SUM(places_disponibles) FROM classes_vol WHERE vol_id = v.vol_id AND classe = 'PREMIERE'), 0),
        COALESCE((SELECT SUM(places_disponibles) FROM classes_vol WHERE vol_id = v.vol_id AND classe = 'BUSINESS'), 0),
        COALESCE((SELECT SUM(places_disponibles) FROM classes_vol WHERE vol_id = v.vol_id AND classe = 'PREMIUM'), 0),
        COALESCE((SELECT SUM(places_disponibles) FROM classes_vol WHERE vol_id = v.vol_id AND classe = 'ECONOMIQUE'), 0)
    FROM vols v
    WHERE v.is_active = 1
    AND v.statut_vol = 'PROGRAMME'
    AND v.date_depart > NOW()
    ON DUPLICATE KEY UPDATE
        booked_seats = VALUES(booked_seats),
        available_seats = VALUES(available_seats),
        occupancy_rate = VALUES(occupancy_rate);

    SELECT CONCAT('Recorded capacity snapshot for ', ROW_COUNT(), ' flights') AS result;
END //

DELIMITER ;

-- =========================================
-- 8. Create view for capacity dashboard
-- =========================================
CREATE OR REPLACE VIEW vw_flight_capacity AS
SELECT
    v.vol_id,
    v.numero_vol,
    ca.nom_compagnie,
    ad.ville AS ville_depart,
    aa.ville AS ville_arrivee,
    CONCAT(ad.ville, ' → ', aa.ville) AS route,
    v.date_depart,
    v.type_avion,
    v.capacite_totale,
    v.places_disponibles,
    v.capacite_totale - v.places_disponibles AS booked_seats,
    ROUND((v.capacite_totale - v.places_disponibles) / v.capacite_totale * 100, 1) AS occupancy_rate,
    CASE
        WHEN (v.capacite_totale - v.places_disponibles) / v.capacite_totale >= 0.95 THEN 'FULL'
        WHEN (v.capacite_totale - v.places_disponibles) / v.capacite_totale >= 0.80 THEN 'HIGH'
        WHEN (v.capacite_totale - v.places_disponibles) / v.capacite_totale >= 0.50 THEN 'MODERATE'
        WHEN (v.capacite_totale - v.places_disponibles) / v.capacite_totale >= 0.20 THEN 'LOW'
        ELSE 'VERY_LOW'
    END AS capacity_status,
    v.statut_vol
FROM vols v
JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id
JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
WHERE v.is_active = 1
ORDER BY v.date_depart;

-- =========================================
-- 9. Sample seat data for testing
-- =========================================
-- Generate seats for first 5 flights (simplified)
INSERT IGNORE INTO seats (vol_id, seat_number, seat_row, seat_column, seat_class, seat_type, extra_price, has_extra_legroom, is_exit_row)
SELECT
    v.vol_id,
    CONCAT(r.row_num, c.col),
    r.row_num,
    c.col,
    CASE
        WHEN r.row_num <= 3 THEN 'BUSINESS'
        WHEN r.row_num <= 6 THEN 'PREMIUM'
        ELSE 'ECONOMIQUE'
    END,
    CASE
        WHEN c.col IN ('A', 'F') THEN 'WINDOW'
        WHEN c.col IN ('C', 'D') THEN 'AISLE'
        ELSE 'MIDDLE'
    END,
    CASE
        WHEN r.row_num <= 3 THEN 30
        WHEN r.row_num <= 6 THEN 15
        WHEN r.row_num = 10 OR r.row_num = 20 THEN 20
        ELSE 0
    END,
    r.row_num <= 6 OR r.row_num IN (10, 20),
    r.row_num IN (10, 20)
FROM vols v
CROSS JOIN (
    SELECT 1 AS row_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) r
CROSS JOIN (
    SELECT 'A' AS col UNION SELECT 'B' UNION SELECT 'C'
    UNION SELECT 'D' UNION SELECT 'E' UNION SELECT 'F'
) c
WHERE v.vol_id <= 5
LIMIT 900;

-- =========================================
-- 10. Update some seats as occupied (for demo)
-- =========================================
UPDATE seats
SET seat_status = 'OCCUPIED'
WHERE vol_id <= 5
AND seat_number IN ('1A', '1B', '2A', '3C', '5D', '10A', '10B', '15E', '20F', '25A', '25B');

-- =========================================
-- Summary
-- =========================================
SELECT 'Database update complete!' AS status;
SELECT
    (SELECT COUNT(*) FROM seats) AS total_seats_created,
    (SELECT COUNT(*) FROM seats WHERE seat_status = 'OCCUPIED') AS occupied_seats,
    (SELECT COUNT(*) FROM email_notifications) AS email_logs,
    (SELECT COUNT(*) FROM capacity_snapshots) AS capacity_snapshots;
