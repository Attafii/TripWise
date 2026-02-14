-- ========================================
-- Fix Booking Issues - For Current Database Schema
-- Run this script to fix booking issues
-- ========================================

USE tripwise_db;

-- ========================================
-- 1. Check current voyageurs (travelers)
-- ========================================
SELECT 'Current voyageurs:' as Info;
SELECT v.voyageur_id, v.user_id, u.email, u.first_name, u.last_name
FROM voyageurs v
JOIN users u ON v.user_id = u.user_id;

-- ========================================
-- 2. Create voyageur profiles for users who don't have one
-- (Users with VOYAGEUR type who aren't in voyageurs table)
-- ========================================
INSERT INTO voyageurs (user_id, seating_preference, meal_preference, loyalty_points, loyalty_status)
SELECT u.user_id, 'PAS_DE_PREFERENCE', 'STANDARD', 0, 'BRONZE'
FROM users u
WHERE u.user_type = 'VOYAGEUR'
AND u.user_id NOT IN (SELECT user_id FROM voyageurs);

-- ========================================
-- 3. Verify classes_vol has data for flights
-- ========================================
SELECT 'Current flight classes:' as Info;
SELECT cv.classe_id, cv.vol_id, cv.type_classe, cv.prix, cv.places_disponibles
FROM classes_vol cv
JOIN vols v ON cv.vol_id = v.vol_id
WHERE v.is_active = 1
LIMIT 20;

-- ========================================
-- 4. Check if all active flights have classes
-- ========================================
SELECT 'Flights without classes:' as Info;
SELECT v.vol_id, v.numero_vol
FROM vols v
WHERE v.is_active = 1
AND v.vol_id NOT IN (SELECT DISTINCT vol_id FROM classes_vol);

-- ========================================
-- 5. Add classes for flights that don't have any
-- ========================================
INSERT INTO classes_vol (vol_id, type_classe, prix, places_disponibles, bagages_inclus, poids_bagage_max)
SELECT v.vol_id, 'ECONOMIQUE', 199.00, 50, 1, 23
FROM vols v
WHERE v.is_active = 1
AND v.vol_id NOT IN (SELECT DISTINCT vol_id FROM classes_vol);

-- ========================================
-- 6. Verify data after fixes
-- ========================================
SELECT 'Summary after fixes:' as Info;
SELECT
    (SELECT COUNT(*) FROM users WHERE user_type = 'VOYAGEUR') as total_voyageur_users,
    (SELECT COUNT(*) FROM voyageurs) as total_voyageur_profiles,
    (SELECT COUNT(*) FROM vols WHERE is_active = 1) as total_active_flights,
    (SELECT COUNT(DISTINCT vol_id) FROM classes_vol) as flights_with_classes,
    (SELECT COUNT(*) FROM reservations_vol) as total_bookings;

-- ========================================
-- 7. Show sample flight data ready for booking
-- ========================================
SELECT 'Flights ready for booking:' as Info;
SELECT v.vol_id, v.numero_vol, v.places_disponibles,
       ad.ville as 'From', aa.ville as 'To',
       cv.classe_id, cv.type_classe, cv.prix
FROM vols v
JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
JOIN classes_vol cv ON v.vol_id = cv.vol_id
WHERE v.is_active = 1 AND cv.places_disponibles > 0
ORDER BY v.vol_id
LIMIT 20;

SELECT '✅ Database fix complete!' as Status;
