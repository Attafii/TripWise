-- ========================================
-- Setup Seats and Complete Booking Flow
-- Run this after fix-booking-data.sql
-- ========================================

USE tripwise_db;

-- ========================================
-- 1. Verify seats table exists
-- ========================================
SELECT 'Checking seats table...' as Info;

-- Count existing seats
SELECT COUNT(*) as existing_seats FROM seats;

-- ========================================
-- 2. Generate seats for all active flights (if empty)
-- This creates a standard seat layout for each flight
-- ========================================

-- Only insert if no seats exist for a flight
INSERT INTO seats (vol_id, seat_number, seat_row, seat_column, seat_class, seat_type, seat_status, extra_price, has_extra_legroom, is_exit_row)
SELECT
    v.vol_id,
    CONCAT(row_num.n, col.letter) as seat_number,
    row_num.n as seat_row,
    col.letter as seat_column,
    CASE
        WHEN row_num.n <= 3 THEN 'PREMIERE'
        WHEN row_num.n <= 6 THEN 'BUSINESS'
        WHEN row_num.n <= 10 THEN 'PREMIUM'
        ELSE 'ECONOMIQUE'
    END as seat_class,
    CASE
        WHEN col.letter IN ('A', 'F') THEN 'WINDOW'
        WHEN col.letter IN ('C', 'D') THEN 'AISLE'
        ELSE 'MIDDLE'
    END as seat_type,
    'AVAILABLE' as seat_status,
    CASE
        WHEN row_num.n <= 3 THEN 150.00
        WHEN row_num.n <= 6 THEN 75.00
        WHEN row_num.n = 12 OR row_num.n = 13 THEN 50.00  -- Exit rows
        WHEN col.letter IN ('A', 'F') THEN 25.00  -- Window seats
        ELSE 0.00
    END as extra_price,
    CASE WHEN row_num.n IN (1, 2, 3) THEN 1 ELSE 0 END as has_extra_legroom,
    CASE WHEN row_num.n IN (12, 13) THEN 1 ELSE 0 END as is_exit_row
FROM vols v
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
) as row_num
CROSS JOIN (
    SELECT 'A' as letter UNION SELECT 'B' UNION SELECT 'C'
    UNION SELECT 'D' UNION SELECT 'E' UNION SELECT 'F'
) as col
WHERE v.is_active = 1
AND NOT EXISTS (SELECT 1 FROM seats s WHERE s.vol_id = v.vol_id)
ORDER BY v.vol_id, row_num.n, col.letter;

-- ========================================
-- 3. Verify seats were created
-- ========================================
SELECT 'Seats created per flight:' as Info;
SELECT vol_id, COUNT(*) as seat_count
FROM seats
GROUP BY vol_id
ORDER BY vol_id;

-- ========================================
-- 4. Show sample seat data
-- ========================================
SELECT 'Sample seat data:' as Info;
SELECT seat_id, vol_id, seat_number, seat_row, seat_column, seat_class, seat_type, seat_status, extra_price
FROM seats
WHERE vol_id = 1
ORDER BY seat_row, seat_column
LIMIT 20;

-- ========================================
-- 5. Summary
-- ========================================
SELECT 'Final Summary:' as Info;
SELECT
    (SELECT COUNT(*) FROM vols WHERE is_active = 1) as active_flights,
    (SELECT COUNT(DISTINCT vol_id) FROM seats) as flights_with_seats,
    (SELECT COUNT(*) FROM seats) as total_seats,
    (SELECT COUNT(*) FROM seats WHERE seat_status = 'AVAILABLE') as available_seats,
    (SELECT COUNT(*) FROM reservations_vol WHERE statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')) as active_bookings;

SELECT '✅ Seat setup complete! You can now select seats for your bookings.' as Status;
