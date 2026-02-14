-- ========================================
-- Update Flight Dates to Future Dates
-- Run this script if flights are not showing
-- ========================================

USE tripwise_db;

-- Update all flight dates to be in the future (starting from today + 3 days)
-- This ensures flights will appear in search results

-- First, let's see current flights
SELECT vol_id, numero_vol, date_depart, date_arrivee, places_disponibles, statut_vol
FROM vols
ORDER BY date_depart;

-- Update flights to future dates (adds days to make them future)
UPDATE vols SET
    date_depart = DATE_ADD(NOW(), INTERVAL (vol_id * 2 + 3) DAY),
    date_arrivee = DATE_ADD(NOW(), INTERVAL (vol_id * 2 + 3) DAY) + INTERVAL duree_vol MINUTE
WHERE is_active = 1;

-- Verify the update
SELECT vol_id, numero_vol, date_depart, date_arrivee, places_disponibles, statut_vol
FROM vols
WHERE is_active = 1
ORDER BY date_depart;

-- Make sure all flights have 'PROGRAMME' status and available seats
UPDATE vols SET
    statut_vol = 'PROGRAMME',
    places_disponibles = GREATEST(places_disponibles, 50)
WHERE is_active = 1 AND places_disponibles < 10;

-- Verify classes_vol has data
SELECT cv.*, v.numero_vol
FROM classes_vol cv
JOIN vols v ON cv.vol_id = v.vol_id
LIMIT 20;

-- Summary
SELECT
    COUNT(*) as total_flights,
    SUM(places_disponibles) as total_available_seats,
    MIN(date_depart) as earliest_flight,
    MAX(date_depart) as latest_flight
FROM vols
WHERE is_active = 1 AND statut_vol = 'PROGRAMME' AND date_depart > NOW();

