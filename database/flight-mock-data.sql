-- ========================================
-- TripWise Flight Mock Data
-- Complete flight data for testing
-- Run after setup.sql
-- ========================================

USE tripwise_db;

-- ========================================
-- Table: compagnies_aeriennes (Airlines)
-- ========================================
CREATE TABLE IF NOT EXISTS compagnies_aeriennes (
    compagnie_id INT AUTO_INCREMENT PRIMARY KEY,
    nom_compagnie VARCHAR(100) NOT NULL,
    code_iata VARCHAR(3),
    pays VARCHAR(50),
    logo_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: aeroports (Airports)
-- ========================================
CREATE TABLE IF NOT EXISTS aeroports (
    aeroport_id INT AUTO_INCREMENT PRIMARY KEY,
    code_iata VARCHAR(3) NOT NULL UNIQUE,
    nom_aeroport VARCHAR(150) NOT NULL,
    ville VARCHAR(100) NOT NULL,
    pays VARCHAR(50) NOT NULL,
    timezone VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: vols (Flights)
-- ========================================
CREATE TABLE IF NOT EXISTS vols (
    vol_id INT AUTO_INCREMENT PRIMARY KEY,
    numero_vol VARCHAR(10) NOT NULL,
    compagnie_id INT NOT NULL,
    aeroport_depart_id INT NOT NULL,
    aeroport_arrivee_id INT NOT NULL,
    date_depart DATETIME NOT NULL,
    date_arrivee DATETIME NOT NULL,
    duree_vol INT, -- in minutes
    type_avion VARCHAR(50),
    capacite_totale INT NOT NULL DEFAULT 180,
    places_disponibles INT NOT NULL DEFAULT 180,
    statut_vol ENUM('PROGRAMME', 'EN_COURS', 'ATTERRI', 'ANNULE', 'RETARDE') DEFAULT 'PROGRAMME',
    porte_embarquement VARCHAR(10),
    terminal VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (compagnie_id) REFERENCES compagnies_aeriennes(compagnie_id),
    FOREIGN KEY (aeroport_depart_id) REFERENCES aeroports(aeroport_id),
    FOREIGN KEY (aeroport_arrivee_id) REFERENCES aeroports(aeroport_id),
    INDEX idx_numero_vol (numero_vol),
    INDEX idx_date_depart (date_depart),
    INDEX idx_statut (statut_vol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: classes_vol (Flight Classes)
-- ========================================
CREATE TABLE IF NOT EXISTS classes_vol (
    classe_vol_id INT AUTO_INCREMENT PRIMARY KEY,
    vol_id INT NOT NULL,
    classe ENUM('ECONOMIQUE', 'PREMIUM', 'BUSINESS', 'PREMIERE') NOT NULL,
    prix DECIMAL(10, 2) NOT NULL,
    places_disponibles INT NOT NULL DEFAULT 50,
    avantages TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vol_id) REFERENCES vols(vol_id) ON DELETE CASCADE,
    INDEX idx_vol_classe (vol_id, classe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- Table: voyageurs (Travelers)
-- ========================================
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
    INDEX idx_statut (statut_reservation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- INSERT MOCK DATA
-- ========================================

-- Clear existing data (optional - comment out if you want to keep existing data)
-- DELETE FROM reservations_vol;
-- DELETE FROM classes_vol;
-- DELETE FROM vols;
-- DELETE FROM aeroports;
-- DELETE FROM compagnies_aeriennes;

-- Insert Airlines
INSERT IGNORE INTO compagnies_aeriennes (compagnie_id, nom_compagnie, code_iata, pays, is_active) VALUES
(1, 'Air France', 'AF', 'France', TRUE),
(2, 'Emirates', 'EK', 'United Arab Emirates', TRUE),
(3, 'Lufthansa', 'LH', 'Germany', TRUE),
(4, 'British Airways', 'BA', 'United Kingdom', TRUE),
(5, 'Qatar Airways', 'QR', 'Qatar', TRUE),
(6, 'Turkish Airlines', 'TK', 'Turkey', TRUE),
(7, 'Tunisair', 'TU', 'Tunisia', TRUE),
(8, 'Delta Airlines', 'DL', 'United States', TRUE),
(9, 'Singapore Airlines', 'SQ', 'Singapore', TRUE),
(10, 'KLM Royal Dutch', 'KL', 'Netherlands', TRUE);

-- Insert Airports
INSERT IGNORE INTO aeroports (aeroport_id, code_iata, nom_aeroport, ville, pays, is_active) VALUES
(1, 'CDG', 'Charles de Gaulle Airport', 'Paris', 'France', TRUE),
(2, 'JFK', 'John F. Kennedy International', 'New York', 'United States', TRUE),
(3, 'DXB', 'Dubai International Airport', 'Dubai', 'United Arab Emirates', TRUE),
(4, 'LHR', 'London Heathrow Airport', 'London', 'United Kingdom', TRUE),
(5, 'TUN', 'Tunis-Carthage Airport', 'Tunis', 'Tunisia', TRUE),
(6, 'FRA', 'Frankfurt Airport', 'Frankfurt', 'Germany', TRUE),
(7, 'IST', 'Istanbul Airport', 'Istanbul', 'Turkey', TRUE),
(8, 'DOH', 'Hamad International Airport', 'Doha', 'Qatar', TRUE),
(9, 'SIN', 'Singapore Changi Airport', 'Singapore', 'Singapore', TRUE),
(10, 'AMS', 'Amsterdam Schiphol Airport', 'Amsterdam', 'Netherlands', TRUE),
(11, 'LAX', 'Los Angeles International', 'Los Angeles', 'United States', TRUE),
(12, 'FCO', 'Leonardo da Vinci Airport', 'Rome', 'Italy', TRUE),
(13, 'BCN', 'Barcelona–El Prat Airport', 'Barcelona', 'Spain', TRUE),
(14, 'MUC', 'Munich Airport', 'Munich', 'Germany', TRUE),
(15, 'ORY', 'Paris Orly Airport', 'Paris', 'France', TRUE);

-- Insert Flights (Future dates for testing - February to April 2026)
INSERT IGNORE INTO vols (vol_id, numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, porte_embarquement, terminal, is_active) VALUES
-- Paris to New York
(1, 'AF001', 1, 1, 2, '2026-02-10 08:30:00', '2026-02-10 11:45:00', 495, 'Airbus A380', 450, 120, 'PROGRAMME', 'A12', '2E', TRUE),
(2, 'AF003', 1, 1, 2, '2026-02-15 14:00:00', '2026-02-15 17:15:00', 495, 'Boeing 777', 350, 85, 'PROGRAMME', 'B8', '2E', TRUE),
(3, 'DL100', 8, 2, 1, '2026-02-12 09:00:00', '2026-02-12 21:30:00', 510, 'Airbus A350', 300, 65, 'PROGRAMME', 'C5', '1', TRUE),

-- Dubai Routes
(4, 'EK201', 2, 3, 4, '2026-02-08 02:00:00', '2026-02-08 07:30:00', 450, 'Airbus A380', 500, 180, 'PROGRAMME', 'A1', '3', TRUE),
(5, 'EK205', 2, 3, 1, '2026-02-11 10:00:00', '2026-02-11 15:30:00', 420, 'Boeing 777', 380, 150, 'PROGRAMME', 'B3', '3', TRUE),
(6, 'EK301', 2, 4, 3, '2026-02-20 20:00:00', '2026-02-21 05:30:00', 450, 'Airbus A380', 500, 200, 'PROGRAMME', 'A5', '3', TRUE),

-- European Routes
(7, 'LH440', 3, 6, 2, '2026-02-14 11:00:00', '2026-02-14 14:30:00', 540, 'Airbus A340', 280, 90, 'PROGRAMME', 'D12', '1', TRUE),
(8, 'BA116', 4, 4, 2, '2026-02-16 09:30:00', '2026-02-16 12:45:00', 495, 'Boeing 787', 250, 75, 'PROGRAMME', 'C8', '5', TRUE),
(9, 'AF1024', 1, 1, 12, '2026-02-18 07:00:00', '2026-02-18 09:15:00', 135, 'Airbus A320', 180, 45, 'PROGRAMME', 'B2', '2F', TRUE),
(10, 'LH1810', 3, 14, 13, '2026-02-19 12:30:00', '2026-02-19 14:45:00', 135, 'Airbus A321', 200, 60, 'PROGRAMME', 'A8', '2', TRUE),

-- Tunisia Routes
(11, 'TU720', 7, 5, 1, '2026-02-10 06:00:00', '2026-02-10 09:30:00', 150, 'Airbus A320', 150, 40, 'PROGRAMME', 'A1', '1', TRUE),
(12, 'TU721', 7, 1, 5, '2026-02-10 18:00:00', '2026-02-10 20:30:00', 150, 'Airbus A320', 150, 55, 'PROGRAMME', 'B5', '2G', TRUE),
(13, 'TU730', 7, 5, 7, '2026-02-22 08:00:00', '2026-02-22 11:00:00', 180, 'Boeing 737', 160, 70, 'PROGRAMME', 'A3', '1', TRUE),

-- Middle East Routes
(14, 'QR001', 5, 8, 4, '2026-02-13 01:00:00', '2026-02-13 07:30:00', 420, 'Airbus A350', 350, 100, 'PROGRAMME', 'C1', 'A', TRUE),
(15, 'TK1800', 6, 7, 1, '2026-02-17 10:00:00', '2026-02-17 13:30:00', 210, 'Boeing 787', 280, 85, 'PROGRAMME', 'B10', 'I', TRUE),

-- Asia Routes
(16, 'SQ322', 9, 9, 4, '2026-02-21 08:30:00', '2026-02-21 15:00:00', 810, 'Airbus A380', 400, 130, 'PROGRAMME', 'A20', '3', TRUE),
(17, 'EK354', 2, 3, 9, '2026-02-25 03:00:00', '2026-02-25 14:30:00', 450, 'Boeing 777', 350, 95, 'PROGRAMME', 'B8', '3', TRUE),

-- Americas Routes
(18, 'DL401', 8, 2, 11, '2026-02-12 08:00:00', '2026-02-12 11:30:00', 330, 'Boeing 767', 220, 80, 'PROGRAMME', 'D5', '4', TRUE),
(19, 'AF066', 1, 1, 11, '2026-02-28 11:00:00', '2026-02-28 14:30:00', 660, 'Airbus A350', 300, 110, 'PROGRAMME', 'A15', '2E', TRUE),

-- Netherlands Routes
(20, 'KL1230', 10, 10, 1, '2026-02-09 07:00:00', '2026-02-09 08:20:00', 80, 'Embraer 190', 100, 35, 'PROGRAMME', 'C2', 'D', TRUE),
(21, 'KL1231', 10, 1, 10, '2026-02-09 19:00:00', '2026-02-09 20:20:00', 80, 'Embraer 190', 100, 42, 'PROGRAMME', 'B3', '2F', TRUE),

-- March Flights
(22, 'AF007', 1, 1, 2, '2026-03-01 09:00:00', '2026-03-01 12:15:00', 495, 'Boeing 777', 350, 120, 'PROGRAMME', 'A10', '2E', TRUE),
(23, 'EK215', 2, 3, 2, '2026-03-05 08:00:00', '2026-03-05 15:30:00', 840, 'Airbus A380', 500, 200, 'PROGRAMME', 'A2', '3', TRUE),
(24, 'BA118', 4, 4, 2, '2026-03-10 10:00:00', '2026-03-10 13:15:00', 495, 'Airbus A350', 280, 95, 'PROGRAMME', 'C10', '5', TRUE),
(25, 'LH450', 3, 6, 11, '2026-03-15 14:00:00', '2026-03-15 18:00:00', 720, 'Airbus A340', 290, 110, 'PROGRAMME', 'D8', '1', TRUE);

-- Insert Flight Classes for each flight
INSERT IGNORE INTO classes_vol (vol_id, classe, prix, places_disponibles, avantages) VALUES
-- Flight 1 (AF001 Paris-NYC)
(1, 'ECONOMIQUE', 450.00, 50, 'Repas inclus, Bagage 23kg'),
(1, 'PREMIUM', 850.00, 30, 'Repas premium, Bagage 32kg, Priority boarding'),
(1, 'BUSINESS', 2200.00, 20, 'Lit plat, Lounge access, 2 bagages'),
(1, 'PREMIERE', 5500.00, 8, 'Suite privée, Chef à bord, Limousine'),

-- Flight 2 (AF003 Paris-NYC)
(2, 'ECONOMIQUE', 420.00, 45, 'Repas inclus, Bagage 23kg'),
(2, 'PREMIUM', 780.00, 25, 'Repas premium, Bagage 32kg, Priority boarding'),
(2, 'BUSINESS', 2000.00, 15, 'Lit plat, Lounge access, 2 bagages'),

-- Flight 3 (DL100 NYC-Paris)
(3, 'ECONOMIQUE', 380.00, 40, 'Snacks, Bagage 23kg'),
(3, 'BUSINESS', 1800.00, 20, 'Full meal, Lounge, Priority'),

-- Flight 4 (EK201 Dubai-London)
(4, 'ECONOMIQUE', 550.00, 100, 'Repas, Entertainment, 30kg bagage'),
(4, 'BUSINESS', 2800.00, 40, 'Onboard bar, Lit plat, Lounge'),
(4, 'PREMIERE', 7000.00, 14, 'Private suite, Shower spa, Chauffeur'),

-- Flight 5 (EK205 Dubai-Paris)
(5, 'ECONOMIQUE', 480.00, 80, 'Repas, Entertainment, 30kg bagage'),
(5, 'BUSINESS', 2400.00, 35, 'Onboard bar, Lit plat, Lounge'),
(5, 'PREMIERE', 6000.00, 12, 'Private suite, Shower spa, Chauffeur'),

-- Flight 6 (EK301 London-Dubai)
(6, 'ECONOMIQUE', 520.00, 110, 'Repas, Entertainment, 30kg bagage'),
(6, 'BUSINESS', 2600.00, 45, 'Onboard bar, Lit plat, Lounge'),

-- Flight 7 (LH440 Frankfurt-NYC)
(7, 'ECONOMIQUE', 490.00, 50, 'Repas, 23kg bagage'),
(7, 'BUSINESS', 2100.00, 25, 'Full service, Lounge, Priority'),

-- Flight 8 (BA116 London-NYC)
(8, 'ECONOMIQUE', 520.00, 40, 'Repas inclus, 23kg'),
(8, 'PREMIUM', 920.00, 20, 'Extra legroom, 32kg'),
(8, 'BUSINESS', 2400.00, 15, 'Club World, Lounge'),

-- Flight 9 (AF1024 Paris-Rome)
(9, 'ECONOMIQUE', 120.00, 30, 'Snack, 23kg'),
(9, 'BUSINESS', 350.00, 15, 'Repas, Priority, Lounge'),

-- Flight 10 (LH1810 Munich-Barcelona)
(10, 'ECONOMIQUE', 95.00, 40, 'Snack, 23kg'),
(10, 'BUSINESS', 280.00, 20, 'Repas, Priority'),

-- Flight 11 (TU720 Tunis-Paris)
(11, 'ECONOMIQUE', 180.00, 30, 'Repas tunisien, 23kg'),
(11, 'BUSINESS', 450.00, 10, 'Repas premium, Lounge, 32kg'),

-- Flight 12 (TU721 Paris-Tunis)
(12, 'ECONOMIQUE', 175.00, 40, 'Repas tunisien, 23kg'),
(12, 'BUSINESS', 430.00, 15, 'Repas premium, Lounge, 32kg'),

-- Flight 13 (TU730 Tunis-Istanbul)
(13, 'ECONOMIQUE', 220.00, 50, 'Repas, 23kg'),
(13, 'BUSINESS', 520.00, 20, 'Full service, Priority'),

-- Flight 14 (QR001 Doha-London)
(14, 'ECONOMIQUE', 580.00, 60, 'Award-winning meals, 30kg'),
(14, 'BUSINESS', 3200.00, 25, 'Qsuite, Al Mourjan Lounge'),
(14, 'PREMIERE', 8000.00, 8, 'First Class suite, Exclusive'),

-- Flight 15 (TK1800 Istanbul-Paris)
(15, 'ECONOMIQUE', 250.00, 55, 'Turkish cuisine, 30kg'),
(15, 'BUSINESS', 750.00, 30, 'Full service, Lounge, Priority'),

-- Flight 16 (SQ322 Singapore-London)
(16, 'ECONOMIQUE', 680.00, 80, 'Award meals, 30kg'),
(16, 'PREMIUM', 1200.00, 30, 'Extra space, 35kg'),
(16, 'BUSINESS', 4500.00, 15, 'Full flat bed, Premium service'),
(16, 'PREMIERE', 9500.00, 5, 'Private suite, Exceptional'),

-- Flight 17 (EK354 Dubai-Singapore)
(17, 'ECONOMIQUE', 520.00, 60, 'Entertainment, 30kg'),
(17, 'BUSINESS', 2800.00, 25, 'Onboard bar, Full flat'),

-- Flight 18 (DL401 NYC-LA)
(18, 'ECONOMIQUE', 180.00, 50, 'Snacks, WiFi'),
(18, 'BUSINESS', 550.00, 20, 'Full service, Priority'),

-- Flight 19 (AF066 Paris-LA)
(19, 'ECONOMIQUE', 620.00, 70, 'Repas, Entertainment'),
(19, 'PREMIUM', 1100.00, 25, 'Extra legroom, 32kg'),
(19, 'BUSINESS', 3200.00, 15, 'Full flat, Lounge'),

-- Flight 20 (KL1230 Amsterdam-Paris)
(20, 'ECONOMIQUE', 85.00, 25, 'Snack, 23kg'),
(20, 'BUSINESS', 220.00, 10, 'Meal, Priority'),

-- Flight 21 (KL1231 Paris-Amsterdam)
(21, 'ECONOMIQUE', 80.00, 30, 'Snack, 23kg'),
(21, 'BUSINESS', 210.00, 12, 'Meal, Priority'),

-- Flight 22 (AF007 Paris-NYC March)
(22, 'ECONOMIQUE', 440.00, 70, 'Repas, Bagage 23kg'),
(22, 'BUSINESS', 2100.00, 30, 'Lit plat, Lounge'),
(22, 'PREMIERE', 5200.00, 10, 'Suite privée, Chef'),

-- Flight 23 (EK215 Dubai-NYC)
(23, 'ECONOMIQUE', 750.00, 120, 'Entertainment, 30kg'),
(23, 'BUSINESS', 4000.00, 50, 'Onboard shower, Lounge'),
(23, 'PREMIERE', 12000.00, 10, 'Private suite, Chauffeur'),

-- Flight 24 (BA118 London-NYC March)
(24, 'ECONOMIQUE', 495.00, 55, 'Repas, 23kg'),
(24, 'BUSINESS', 2300.00, 25, 'Club World, Lounge'),

-- Flight 25 (LH450 Frankfurt-LA)
(25, 'ECONOMIQUE', 580.00, 70, 'Repas, Entertainment'),
(25, 'BUSINESS', 2800.00, 30, 'Full service, Lounge');

-- ========================================
-- Create test travelers if not exist
-- ========================================
INSERT IGNORE INTO voyageurs (user_id, preferences_voyage, points_fidelite)
SELECT user_id, 'Window seat preferred, Vegetarian meals', 500
FROM users
WHERE user_type = 'VOYAGEUR'
LIMIT 5;

-- For users without user_type column, try with existing users
INSERT IGNORE INTO voyageurs (voyageur_id, user_id, preferences_voyage, points_fidelite)
VALUES
(1, 1, 'Window seat, Extra legroom', 1250),
(2, 2, 'Aisle seat, Kosher meals', 850),
(3, 3, 'Any seat, Halal meals', 2100);

-- ========================================
-- Sample Flight Bookings
-- ========================================
INSERT IGNORE INTO reservations_vol (voyageur_id, vol_id, classe_vol_id, nombre_passagers, prix_total, statut_reservation, numero_confirmation, sieges_attribues, bagage, repas) VALUES
-- Confirmed bookings
(1, 1, 1, 2, 900.00, 'CONFIRMEE', 'AF2345', '12A, 12B', '2x23kg', 'Standard'),
(1, 4, 11, 1, 2800.00, 'CONFIRMEE', 'EK7823', '2A', '32kg + carry-on', 'Gourmet'),
(2, 11, 29, 1, 180.00, 'CONFIRMEE', 'TU1122', '8C', '23kg', 'Tunisian'),
(3, 5, 13, 2, 960.00, 'CONFIRMEE', 'EK4455', '15A, 15B', '2x30kg', 'Standard'),

-- Pending bookings
(1, 22, 57, 1, 440.00, 'EN_ATTENTE', 'AF9012', NULL, '23kg', 'Standard'),
(2, 16, 41, 2, 1360.00, 'EN_ATTENTE', 'SQ3344', NULL, '2x30kg', 'Asian vegetarian'),
(3, 14, 36, 1, 580.00, 'EN_ATTENTE', 'QR5566', NULL, '30kg', 'Halal'),

-- Completed bookings (past)
(1, 9, 23, 1, 120.00, 'TERMINEE', 'AF8877', '22D', '23kg', 'Snack'),
(2, 20, 51, 1, 85.00, 'TERMINEE', 'KL6677', '5A', '23kg', 'Snack'),

-- Cancelled booking
(3, 7, 17, 1, 490.00, 'ANNULEE', 'LH9988', NULL, NULL, NULL);

-- Display success message
SELECT 'Flight mock data inserted successfully!' AS Status;
SELECT COUNT(*) AS 'Total Flights' FROM vols;
SELECT COUNT(*) AS 'Total Flight Classes' FROM classes_vol;
SELECT COUNT(*) AS 'Total Bookings' FROM reservations_vol;
