-- ========================================
-- Add More Flight Routes for Testing
-- Run this in phpMyAdmin (tripwise_db)
-- ========================================

USE tripwise_db;

-- ========================================
-- 1. First, check existing airports
-- ========================================
SELECT 'Current airports:' as Info;
SELECT aeroport_id, nom_aeroport, code_iata, ville, pays FROM aeroports;

-- ========================================
-- 2. Add more flights between different countries
-- ========================================

-- Paris (France) to Dubai (UAE)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'AF801', 1, 1, 3, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 7 HOUR), 420, 'Airbus A380', 450, 420, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'AF801');

-- Dubai (UAE) to Paris (France)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'EK075', 2, 3, 1, DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 7 HOUR), 420, 'Boeing 777', 350, 320, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'EK075');

-- London (UK) to New York (USA)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'BA115', 4, 4, 2, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 8 HOUR), 480, 'Boeing 777', 300, 280, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'BA115');

-- New York (USA) to London (UK)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'BA178', 4, 2, 4, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 7 HOUR), 420, 'Airbus A350', 280, 250, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'BA178');

-- Tokyo (Japan) to Los Angeles (USA)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'AA154', 3, 6, 5, DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 11 HOUR), 660, 'Boeing 787', 240, 220, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'AA154');

-- Los Angeles (USA) to Tokyo (Japan)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'AA155', 3, 5, 6, DATE_ADD(NOW(), INTERVAL 8 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 DAY), INTERVAL 12 HOUR), 720, 'Boeing 787', 240, 210, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'AA155');

-- Singapore to Dubai (UAE)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'EK355', 2, 8, 3, DATE_ADD(NOW(), INTERVAL 9 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 9 DAY), INTERVAL 7 HOUR), 420, 'Airbus A380', 500, 480, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'EK355');

-- Dubai (UAE) to Singapore
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'EK356', 2, 3, 8, DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 10 DAY), INTERVAL 7 HOUR), 420, 'Airbus A380', 500, 470, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'EK356');

-- Frankfurt (Germany) to New York (USA)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'LH400', 5, 7, 2, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 9 HOUR), 540, 'Airbus A340', 280, 260, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'LH400');

-- New York (USA) to Frankfurt (Germany)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'LH401', 5, 2, 7, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 8 HOUR), 480, 'Airbus A340', 280, 250, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'LH401');

-- Paris (France) to Tokyo (Japan)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'AF276', 1, 1, 6, DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 12 HOUR), 720, 'Boeing 777', 300, 280, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'AF276');

-- Tokyo (Japan) to Paris (France)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'AF277', 1, 6, 1, DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 13 HOUR), 780, 'Boeing 777', 300, 275, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'AF277');

-- London (UK) to Dubai (UAE)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'EK002', 2, 4, 3, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 7 HOUR), 420, 'Airbus A380', 500, 480, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'EK002');

-- Dubai (UAE) to London (UK)
INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, statut_vol, is_active)
SELECT 'EK003', 2, 3, 4, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 8 HOUR), 480, 'Airbus A380', 500, 470, 'PROGRAMME', 1
WHERE NOT EXISTS (SELECT 1 FROM vols WHERE numero_vol = 'EK003');

-- ========================================
-- 3. Add classes for new flights
-- ========================================

-- Add classes for all new flights that don't have classes yet
INSERT INTO classes_vol (vol_id, type_classe, prix, places_disponibles, bagages_inclus, poids_bagage_max, modifiable, remboursable)
SELECT v.vol_id, 'ECONOMIQUE',
    CASE
        WHEN v.duree_vol > 600 THEN 850.00
        WHEN v.duree_vol > 400 THEN 550.00
        ELSE 350.00
    END,
    FLOOR(v.capacite_totale * 0.7), 1, 23, 1, 0
FROM vols v
WHERE v.is_active = 1
AND NOT EXISTS (SELECT 1 FROM classes_vol cv WHERE cv.vol_id = v.vol_id AND cv.type_classe = 'ECONOMIQUE');

INSERT INTO classes_vol (vol_id, type_classe, prix, places_disponibles, bagages_inclus, poids_bagage_max, modifiable, remboursable)
SELECT v.vol_id, 'AFFAIRES',
    CASE
        WHEN v.duree_vol > 600 THEN 3500.00
        WHEN v.duree_vol > 400 THEN 2200.00
        ELSE 1200.00
    END,
    FLOOR(v.capacite_totale * 0.2), 2, 32, 1, 1
FROM vols v
WHERE v.is_active = 1
AND NOT EXISTS (SELECT 1 FROM classes_vol cv WHERE cv.vol_id = v.vol_id AND cv.type_classe = 'AFFAIRES');

INSERT INTO classes_vol (vol_id, type_classe, prix, places_disponibles, bagages_inclus, poids_bagage_max, modifiable, remboursable)
SELECT v.vol_id, 'PREMIERE',
    CASE
        WHEN v.duree_vol > 600 THEN 8000.00
        WHEN v.duree_vol > 400 THEN 5500.00
        ELSE 3000.00
    END,
    FLOOR(v.capacite_totale * 0.05), 3, 40, 1, 1
FROM vols v
WHERE v.is_active = 1
AND NOT EXISTS (SELECT 1 FROM classes_vol cv WHERE cv.vol_id = v.vol_id AND cv.type_classe = 'PREMIERE');

-- ========================================
-- 4. Verify the results
-- ========================================
SELECT 'All available routes:' as Info;
SELECT
    v.numero_vol,
    ca.nom_compagnie as airline,
    ad.ville as 'From City',
    ad.pays as 'From Country',
    aa.ville as 'To City',
    aa.pays as 'To Country',
    DATE(v.date_depart) as departure_date,
    (SELECT MIN(cv.prix) FROM classes_vol cv WHERE cv.vol_id = v.vol_id) as min_price
FROM vols v
JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id
JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
WHERE v.is_active = 1 AND v.statut_vol = 'PROGRAMME'
ORDER BY ad.pays, ad.ville, aa.ville;

SELECT '✅ Flight routes added successfully!' as Status;
