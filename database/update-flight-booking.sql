-- ========================================
-- TripWise Database Update Script
-- Flight Booking Tables and Sample Data
-- Run this after initial setup.sql
-- ========================================

USE tripwise_db;

-- ========================================
-- Table: reservations_vol (Flight Bookings)
-- ========================================
CREATE TABLE IF NOT EXISTS reservations_vol (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    voyageur_id INT NOT NULL,
    vol_id INT NOT NULL,
    classe_vol_id INT NOT NULL,
    date_reservation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_passagers INT NOT NULL DEFAULT 1,
    prix_total DECIMAL(10, 2) NOT NULL,
    statut_reservation ENUM('EN_ATTENTE', 'CONFIRMEE', 'ANNULEE', 'EMBARQUEE', 'TERMINEE') DEFAULT 'EN_ATTENTE',
    numero_confirmation VARCHAR(10),
    sieges_attribues VARCHAR(100),
    bagage VARCHAR(50),
    repas VARCHAR(50),
    demandes_speciales TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (voyageur_id) REFERENCES voyageurs(voyageur_id) ON DELETE CASCADE,
    FOREIGN KEY (vol_id) REFERENCES vols(vol_id) ON DELETE CASCADE,
    FOREIGN KEY (classe_vol_id) REFERENCES classes_vol(classe_vol_id) ON DELETE CASCADE,
    INDEX idx_voyageur_id (voyageur_id),
    INDEX idx_vol_id (vol_id),
    INDEX idx_statut (statut_reservation),
    INDEX idx_date_reservation (date_reservation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: reservations_vehicule (Car Rentals)
-- ========================================
CREATE TABLE IF NOT EXISTS reservations_vehicule (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    voyageur_id INT NOT NULL,
    vehicule_id INT NOT NULL,
    date_reservation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    lieu_prise VARCHAR(100),
    lieu_retour VARCHAR(100),
    prix_total DECIMAL(10, 2) NOT NULL,
    statut_location ENUM('EN_ATTENTE', 'CONFIRMEE', 'ANNULEE', 'EN_COURS', 'TERMINEE') DEFAULT 'EN_ATTENTE',
    options_supplementaires TEXT,
    demandes_speciales TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (voyageur_id) REFERENCES voyageurs(voyageur_id) ON DELETE CASCADE,
    FOREIGN KEY (vehicule_id) REFERENCES vehicules(vehicule_id) ON DELETE CASCADE,
    INDEX idx_voyageur_id (voyageur_id),
    INDEX idx_vehicule_id (vehicule_id),
    INDEX idx_statut (statut_location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Sample Flight Bookings
-- ========================================
-- First, ensure we have voyageurs for the test users
INSERT IGNORE INTO voyageurs (user_id, preferences_voyage, points_fidelite)
SELECT user_id, 'Window seat preferred', 0
FROM users
WHERE user_type = 'VOYAGEUR'
ON DUPLICATE KEY UPDATE points_fidelite = points_fidelite;

-- Add sample flight bookings
INSERT INTO reservations_vol (voyageur_id, vol_id, classe_vol_id, nombre_passagers, prix_total, statut_reservation, numero_confirmation)
SELECT
    v.voyageur_id,
    vl.vol_id,
    cv.classe_vol_id,
    2,
    cv.prix * 2,
    'EN_ATTENTE',
    CONCAT(CHAR(65 + FLOOR(RAND() * 26)), CHAR(65 + FLOOR(RAND() * 26)), LPAD(FLOOR(RAND() * 10000), 4, '0'))
FROM voyageurs v
CROSS JOIN vols vl
JOIN classes_vol cv ON vl.vol_id = cv.vol_id
WHERE vl.is_active = 1 AND vl.date_depart > NOW()
LIMIT 5;

-- Add some confirmed bookings
INSERT INTO reservations_vol (voyageur_id, vol_id, classe_vol_id, nombre_passagers, prix_total, statut_reservation, numero_confirmation)
SELECT
    v.voyageur_id,
    vl.vol_id,
    cv.classe_vol_id,
    1,
    cv.prix,
    'CONFIRMEE',
    CONCAT(CHAR(65 + FLOOR(RAND() * 26)), CHAR(65 + FLOOR(RAND() * 26)), LPAD(FLOOR(RAND() * 10000), 4, '0'))
FROM voyageurs v
CROSS JOIN vols vl
JOIN classes_vol cv ON vl.vol_id = cv.vol_id
WHERE vl.is_active = 1 AND vl.date_depart > NOW()
LIMIT 3;

-- ========================================
-- Verify Tables Created
-- ========================================
SELECT 'reservations_vol table created' AS Status, COUNT(*) AS Rows FROM reservations_vol
UNION ALL
SELECT 'reservations_vehicule table created' AS Status, COUNT(*) AS Rows FROM reservations_vehicule;

SELECT '✅ Database update completed successfully!' AS Message;
