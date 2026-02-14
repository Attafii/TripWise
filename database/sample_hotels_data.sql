-- ===================================================================
-- TripWise - Sample Data for Testing Advanced Search
-- ===================================================================

USE tripwise_db;

-- Insert sample hotels for testing
INSERT INTO hotels (nom_hotel, adresse, ville, pays, code_postal, etoiles, phone_number, email, site_web, description, equipements, politique_annulation, heure_checkin, heure_checkout, image_url, is_active) VALUES
('Hotel Ritz Paris', '15 Place Vendôme', 'Paris', 'France', '75001', 5, '+33 1 43 16 30 30', 'reservations@ritzparis.com', 'www.ritzparis.com', 'Legendary 5-star hotel in the heart of Paris', 'WiFi,Pool,Spa,Gym,Restaurant,Bar,Business Center', 'Free cancellation up to 24h before check-in', '14:00:00', '12:00:00', NULL, 1),
('Le Meurice', '228 Rue de Rivoli', 'Paris', 'France', '75001', 5, '+33 1 44 58 10 10', 'reservations@lemeurice.com', 'www.lemeurice.com', 'Palace hotel overlooking the Tuileries Garden', 'WiFi,Spa,Gym,Restaurant,Bar,Airport Shuttle,Business Center', 'Free cancellation up to 48h before check-in', '15:00:00', '12:00:00', NULL, 1),
('Hotel Plaza Athénée', '25 Avenue Montaigne', 'Paris', 'France', '75008', 5, '+33 1 53 67 66 65', 'reservations@plaza-athenee-paris.com', 'www.plaza-athenee-paris.com', 'Luxury hotel on Avenue Montaigne', 'WiFi,Pool,Spa,Gym,Restaurant,Bar,Pet Friendly,Business Center', 'Free cancellation up to 24h before check-in', '15:00:00', '11:00:00', NULL, 1),
('Hotel du Louvre', 'Place André Malraux', 'Paris', 'France', '75001', 4, '+33 1 44 58 38 38', 'reservations@hoteldulouvre.com', 'www.hoteldulouvre.com', '4-star hotel with views of the Louvre', 'WiFi,Gym,Restaurant,Bar,Parking,Business Center', 'Free cancellation up to 24h before check-in', '14:00:00', '12:00:00', NULL, 1),
('Hotel des Arts Montmartre', '5 Rue Tholozé', 'Paris', 'France', '75018', 3, '+33 1 46 06 30 52', 'contact@arts-hotel-paris.com', 'www.arts-hotel-paris.com', 'Charming 3-star hotel in Montmartre', 'WiFi,Bar', 'Cancellation fee applies after 48h', '14:00:00', '11:00:00', NULL, 1),

('The Plaza Hotel', 'Fifth Avenue at Central Park South', 'New York', 'USA', '10019', 5, '+1 212-759-3000', 'reservations@theplazany.com', 'www.theplazany.com', 'Iconic luxury hotel in Manhattan', 'WiFi,Pool,Spa,Gym,Restaurant,Bar,Airport Shuttle,Business Center,Pet Friendly', 'Free cancellation up to 24h before check-in', '15:00:00', '12:00:00', NULL, 1),
('The St. Regis New York', '2 East 55th Street', 'New York', 'USA', '10022', 5, '+1 212-753-4500', 'reservations@stregisnewyork.com', 'www.stregisnewyork.com', 'Luxury Beaux-Arts landmark hotel', 'WiFi,Spa,Gym,Restaurant,Bar,Business Center', 'Free cancellation up to 48h before check-in', '15:00:00', '12:00:00', NULL, 1),
('The Time Hotel', '224 West 49th Street', 'New York', 'USA', '10019', 4, '+1 212-246-5252', 'reservations@thetimeny.com', 'www.thetimeny.com', 'Boutique hotel in Times Square', 'WiFi,Gym,Restaurant,Bar', 'Cancellation fee applies after 72h', '15:00:00', '11:00:00', NULL, 1),

('Burj Al Arab Jumeirah', 'Jumeirah Beach Road', 'Dubai', 'UAE', '00000', 5, '+971 4 301 7777', 'reservations@burjalarab.com', 'www.burjalarab.com', 'Iconic 7-star luxury hotel', 'WiFi,Pool,Spa,Gym,Restaurant,Bar,Airport Shuttle,Business Center', 'Free cancellation up to 24h before check-in', '15:00:00', '12:00:00', NULL, 1),
('Atlantis The Palm', 'Crescent Road', 'Dubai', 'UAE', '00000', 5, '+971 4 426 2000', 'reservations@atlantisthepalm.com', 'www.atlantis.com', 'Resort on the Palm Jumeirah', 'WiFi,Pool,Spa,Gym,Restaurant,Bar,Parking,Pet Friendly', 'Free cancellation up to 48h before check-in', '15:00:00', '12:00:00', NULL, 1),

('The Ritz London', '150 Piccadilly', 'London', 'UK', 'W1J 9BR', 5, '+44 20 7493 8181', 'reservations@theritzlondon.com', 'www.theritzlondon.com', 'Historic 5-star hotel in Mayfair', 'WiFi,Spa,Gym,Restaurant,Bar,Business Center', 'Free cancellation up to 24h before check-in', '14:00:00', '12:00:00', NULL, 1),
('The Savoy', 'Strand', 'London', 'UK', 'WC2R 0EZ', 5, '+44 20 7836 4343', 'reservations@the-savoy.com', 'www.fairmont.com/savoy', 'Iconic luxury hotel on the Thames', 'WiFi,Pool,Spa,Gym,Restaurant,Bar,Business Center', 'Free cancellation up to 48h before check-in', '15:00:00', '12:00:00', NULL, 1);

-- Insert sample rooms (chambres) for each hotel
INSERT INTO chambres (hotel_id, numero_chambre, type_chambre, capacite, prix_nuit, description, equipements_chambre, est_disponible) VALUES
-- Hotel Ritz Paris (hotel_id=1)
(1, '101', 'Suite Deluxe', 2, 850.00, 'Luxurious suite with city views', 'King bed, Minibar, Safe, TV', 1),
(1, '102', 'Suite Deluxe', 2, 850.00, 'Luxurious suite with city views', 'King bed, Minibar, Safe, TV', 1),
(1, '201', 'Suite Prestige', 2, 1200.00, 'Premium suite with balcony', 'King bed, Sitting area, Balcony', 1),

-- Le Meurice (hotel_id=2)
(2, '101', 'Chambre Classique', 2, 650.00, 'Classic room with garden views', 'Queen bed, Minibar, TV', 1),
(2, '102', 'Chambre Classique', 2, 650.00, 'Classic room with garden views', 'Queen bed, Minibar, TV', 1),
(2, '201', 'Suite Junior', 2, 900.00, 'Junior suite with seating area', 'King bed, Sofa, Minibar', 1),

-- Plaza Athénée (hotel_id=3)
(3, '101', 'Chambre Supérieure', 2, 750.00, 'Superior room with modern amenities', 'King bed, Minibar, TV', 1),
(3, '201', 'Suite Eiffel', 2, 1500.00, 'Suite with Eiffel Tower views', 'King bed, Living room, Balcony', 1),

-- Hotel du Louvre (hotel_id=4)
(4, '101', 'Chambre Standard', 2, 350.00, 'Comfortable standard room', 'Double bed, TV', 1),
(4, '102', 'Chambre Standard', 2, 350.00, 'Comfortable standard room', 'Double bed, TV', 1),
(4, '103', 'Chambre Standard', 2, 350.00, 'Comfortable standard room', 'Double bed, TV', 1),

-- Hotel des Arts Montmartre (hotel_id=5)
(5, '101', 'Chambre Double', 2, 180.00, 'Cozy double room', 'Double bed', 1),
(5, '102', 'Chambre Double', 2, 180.00, 'Cozy double room', 'Double bed', 1),

-- The Plaza Hotel NYC (hotel_id=6)
(6, '101', 'Grand Deluxe Room', 2, 795.00, 'Luxurious room with park views', 'King bed, Minibar, TV', 1),
(6, '102', 'Grand Deluxe Room', 2, 795.00, 'Luxurious room with park views', 'King bed, Minibar, TV', 1),
(6, '501', 'Plaza Suite', 2, 1450.00, 'Elegant suite overlooking Central Park', 'King bed, Living room', 1),

-- St. Regis NYC (hotel_id=7)
(7, '101', 'Deluxe Room', 2, 725.00, 'Sophisticated room with modern amenities', 'King bed, Minibar', 1),
(7, '201', 'St. Regis Suite', 2, 1200.00, 'Luxury suite with butler service', 'King bed, Dining area', 1),

-- The Time Hotel NYC (hotel_id=8)
(8, '101', 'Standard Room', 2, 320.00, 'Modern boutique room', 'Queen bed, TV', 1),
(8, '102', 'Standard Room', 2, 320.00, 'Modern boutique room', 'Queen bed, TV', 1),

-- Burj Al Arab (hotel_id=9)
(9, '2501', 'One Bedroom Suite', 2, 2500.00, 'Opulent suite with Arabian Gulf views', 'King bed, Living room, Butler', 1),

-- Atlantis The Palm (hotel_id=10)
(10, '101', 'Deluxe Room', 2, 550.00, 'Resort room with partial ocean views', 'King bed, Balcony', 1),
(10, '102', 'Deluxe Room', 2, 550.00, 'Resort room with partial ocean views', 'King bed, Balcony', 1),

-- The Ritz London (hotel_id=11)
(11, '101', 'Deluxe Room', 2, 695.00, 'Classic room with period features', 'King bed, Minibar, TV', 1),
(11, '201', 'Royal Suite', 2, 1800.00, 'Majestic suite with sitting room', 'King bed, Living room', 1),

-- The Savoy London (hotel_id=12)
(12, '101', 'Superior Room', 2, 625.00, 'Elegant room with Thames views', 'King bed, TV', 1),
(12, '201', 'River View Suite', 2, 1100.00, 'Suite overlooking the Thames', 'King bed, Sofa, River view', 1);

COMMIT;

-- Verify data
SELECT 
    h.hotel_id,
    h.nom_hotel,
    h.ville,
    h.etoiles,
    COUNT(c.chambre_id) as room_count,
    MIN(c.prix_nuit) as min_price,
    MAX(c.prix_nuit) as max_price
FROM hotels h
LEFT JOIN chambres c ON h.hotel_id = c.hotel_id
WHERE h.is_active = 1
GROUP BY h.hotel_id
ORDER BY h.ville, h.nom_hotel;

SELECT 'Sample hotels and rooms inserted successfully!' as status;
