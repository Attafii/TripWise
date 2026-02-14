-- Sample Deals Data for TripWise
-- Run this script in phpMyAdmin after creating the deals table

-- Clear existing deals (optional)
DELETE FROM deals WHERE deal_id > 0;

-- Daily Deal
INSERT INTO deals (
    deal_type, item_type, title, description, item_id, item_name,
    original_price, discounted_price, discount_percentage,
    start_date, end_date, destination, available_slots, is_active, created_at
) VALUES
('DAILY_DEAL', 'HOTEL', 'Luxury Paris Getaway - Today Only!', 
 'Experience the romance of Paris at Le Royal Monceau. Includes breakfast, spa access, and Eiffel Tower view room.',
 1, 'Le Royal Monceau', 599.00, 399.00, 33,
 NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'Paris, France', 10, 1, NOW());

-- Weekly Deals (Hotels)
INSERT INTO deals (
    deal_type, item_type, title, description, item_id, item_name,
    original_price, discounted_price, discount_percentage,
    start_date, end_date, destination, available_slots, is_active, created_at
) VALUES
('WEEKLY_DEAL', 'HOTEL', 'New York City Adventure', 
 'Stay in the heart of Manhattan at The Plaza Hotel. Includes city tour and Broadway show tickets.',
 2, 'The Plaza Hotel', 899.00, 629.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'New York, USA', 15, 1, NOW()),

('WEEKLY_DEAL', 'HOTEL', 'Dubai Desert Luxury', 
 'Ultra-luxury experience at Burj Al Arab. Includes airport transfer, dinner, and spa treatment.',
 3, 'Burj Al Arab', 1200.00, 849.00, 29,
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Dubai, UAE', 8, 1, NOW()),

('WEEKLY_DEAL', 'HOTEL', 'London Royal Experience', 
 'Elegant stay at The Ritz London. Includes afternoon tea and royal palace tour.',
 4, 'The Ritz London', 750.00, 525.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'London, UK', 12, 1, NOW()),

('WEEKLY_DEAL', 'HOTEL', 'Tokyo Modern Retreat', 
 'Experience modern Tokyo at Park Hyatt. Includes traditional kaiseki dinner and city view.',
 5, 'Park Hyatt Tokyo', 680.00, 476.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Tokyo, Japan', 20, 1, NOW());

-- Flash Sales
INSERT INTO deals (
    deal_type, item_type, title, description, item_id, item_name,
    original_price, discounted_price, discount_percentage,
    start_date, end_date, destination, available_slots, is_active, created_at
) VALUES
('FLASH_SALE', 'HOTEL', '⚡ Flash: Barcelona Beach Resort',
 'LIMITED TIME! 70% OFF - Mediterranean luxury with beach access, pool, and all meals included.',
 6, 'W Barcelona', 800.00, 240.00, 70,
 NOW(), DATE_ADD(NOW(), INTERVAL 6 HOUR), 'Barcelona, Spain', 5, 1, NOW()),

('FLASH_SALE', 'HOTEL', '⚡ Flash: Rome Historic Hotel',
 'HURRY! 65% OFF - Stay near the Colosseum with guided tours included.',
 7, 'Hotel Raphael', 550.00, 192.50, 65,
 NOW(), DATE_ADD(NOW(), INTERVAL 4 HOUR), 'Rome, Italy', 3, 1, NOW()),

('FLASH_SALE', 'HOTEL', '⚡ Flash: Amsterdam Canal View',
 'LAST CHANCE! 60% OFF - Boutique hotel on famous canal with bike rental.',
 8, 'The Hoxton Amsterdam', 420.00, 168.00, 60,
 NOW(), DATE_ADD(NOW(), INTERVAL 8 HOUR), 'Amsterdam, Netherlands', 7, 1, NOW());

-- Seasonal Offers
INSERT INTO deals (
    deal_type, item_type, title, description, item_id, item_name,
    original_price, discounted_price, discount_percentage,
    start_date, end_date, destination, available_slots, is_active, created_at
) VALUES
('SEASONAL_OFFER', 'HOTEL', 'Spring in Paris Special',
 'Celebrate spring with exclusive rates at premium Parisian hotels. Book 3 nights, get 1 free!',
 1, 'Various Paris Hotels', 1200.00, 900.00, 25,
 NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'Paris, France', 50, 1, NOW()),

('SEASONAL_OFFER', 'HOTEL', 'Summer London Escape',
 'Enjoy summer in London with special rates at top hotels. Includes Thames river cruise.',
 4, 'Various London Hotels', 950.00, 712.50, 25,
 NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 'London, UK', 40, 1, NOW());

-- Package Deals
INSERT INTO deals (
    deal_type, item_type, title, description, item_id, item_name,
    original_price, discounted_price, discount_percentage,
    start_date, end_date, destination, available_slots, is_active, created_at
) VALUES
('PACKAGE_DEAL', 'PACKAGE', '🎁 Dubai Complete Package',
 'ALL-INCLUSIVE: 5 nights luxury hotel + desert safari + city tour + dinner cruise + spa',
 NULL, 'Dubai Experience Package', 2500.00, 1750.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY), 'Dubai, UAE', 25, 1, NOW()),

('PACKAGE_DEAL', 'PACKAGE', '🎁 European Highlights Tour',
 'MEGA DEAL: Paris (3n) + London (3n) + Amsterdam (2n) - Hotels + flights + tours included',
 NULL, 'European Tour Package', 3800.00, 2660.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'Europe', 15, 1, NOW()),

('PACKAGE_DEAL', 'PACKAGE', '🎁 New York Total Experience',
 'COMPLETE NYC: 4 nights hotel + all attractions pass + food tour + shopping vouchers',
 NULL, 'NYC Complete Package', 2200.00, 1540.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'New York, USA', 20, 1, NOW());

-- Additional Hotel Deals
INSERT INTO deals (
    deal_type, item_type, title, description, item_id, item_name,
    original_price, discounted_price, discount_percentage,
    start_date, end_date, destination, available_slots, is_active, created_at
) VALUES
('WEEKLY_DEAL', 'HOTEL', 'Singapore Marina Bay Luxury',
 'Iconic Marina Bay Sands with infinity pool, casino access, and Gardens by the Bay tour.',
 9, 'Marina Bay Sands', 720.00, 504.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Singapore', 18, 1, NOW()),

('WEEKLY_DEAL', 'HOTEL', 'Los Angeles Beach Paradise',
 'Beachfront luxury in Santa Monica. Includes surfing lessons and pier dining voucher.',
 10, 'Shutters on the Beach', 650.00, 455.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Los Angeles, USA', 14, 1, NOW());

-- Flight Deals (if flights are in your database)
INSERT INTO deals (
    deal_type, item_type, title, description, item_id, item_name,
    original_price, discounted_price, discount_percentage,
    start_date, end_date, destination, available_slots, is_active, created_at
) VALUES
('WEEKLY_DEAL', 'FLIGHT', '✈️ Paris Round-Trip Special',
 'Business class tickets to Paris from New York. Limited availability!',
 NULL, 'NYC-Paris Business Class', 3200.00, 2240.00, 30,
 NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 'Paris, France', 30, 1, NOW()),

('FLASH_SALE', 'FLIGHT', '⚡ Flash: Dubai Flight Deal',
 'Economy class round-trip to Dubai from major US cities. Book within 6 hours!',
 NULL, 'USA-Dubai Economy', 1400.00, 560.00, 60,
 NOW(), DATE_ADD(NOW(), INTERVAL 6 HOUR), 'Dubai, UAE', 50, 1, NOW());

-- Display summary
SELECT 'Sample deals inserted successfully!' AS Status;
SELECT deal_type, COUNT(*) AS count, 
       CONCAT(MIN(discount_percentage), '% - ', MAX(discount_percentage), '%') AS discount_range
FROM deals 
WHERE is_active = 1
GROUP BY deal_type;

SELECT 'Total active deals:' AS info, COUNT(*) AS count FROM deals WHERE is_active = 1;
