CREATE TABLE IF NOT EXISTS HOTELS (
    name VARCHAR NOT NULL,
    city VARCHAR NOT NULL,
    price_per_night DOUBLE,
    rating DOUBLE,
    description VARCHAR,
    PRIMARY KEY (name, city)
);

CREATE TABLE IF NOT EXISTS ROOMS (
    hotel_name VARCHAR NOT NULL,
    hotel_city VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    price_per_night DOUBLE,
    capacity INT,
    amenities VARCHAR,
    PRIMARY KEY (hotel_name, hotel_city, name),
    FOREIGN KEY (hotel_name, hotel_city) REFERENCES HOTELS(name, city)
);

CREATE TABLE IF NOT EXISTS BOOKINGS (
    id VARCHAR PRIMARY KEY,
    hotel_name VARCHAR NOT NULL,
    hotel_city VARCHAR NOT NULL,
    room_name VARCHAR NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_price DOUBLE,
    status VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS CAR_RENTALS (
    id VARCHAR PRIMARY KEY,
    brand VARCHAR NOT NULL,
    model VARCHAR NOT NULL,
    pickup_location VARCHAR NOT NULL,
    pickup_date DATE NOT NULL,
    return_date DATE NOT NULL,
    total_price DOUBLE,
    status VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS PAYMENTS (
    id VARCHAR PRIMARY KEY,
    last4 VARCHAR,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

MERGE INTO HOTELS KEY(name, city) VALUES
('Grand Plaza Hotel','Paris',199.99,4.5,'Luxury hotel near Eiffel Tower.'),
('Ocean View Resort','Miami',299.99,4.8,'Beautiful resort with ocean view.'),
('City Center Inn','New York',159.99,4.2,'Convenient location in Manhattan.'),
('Mountain Lodge','Denver',179.99,4.6,'Cozy lodge in the mountains.'),
('Beach Paradise','Bali',249.99,4.9,'Tropical paradise for relaxation.');

MERGE INTO ROOMS KEY(hotel_name, hotel_city, name) VALUES
('Grand Plaza Hotel','Paris','Standard',199.99,2,'Wifi,TV'),
('Grand Plaza Hotel','Paris','Deluxe',299.99,2,'Wifi,TV,Balcony'),
('Grand Plaza Hotel','Paris','Suite',499.99,4,'Wifi,TV,Balcony,Jacuzzi'),
('Ocean View Resort','Miami','Ocean Front',350.00,2,'Wifi,Ocean View'),
('Ocean View Resort','Miami','Standard',250.00,2,'Wifi'),
('City Center Inn','New York','Single',159.99,1,'Wifi'),
('City Center Inn','New York','Double',189.99,2,'Wifi,TV'),
('Mountain Lodge','Denver','Cabin',179.99,4,'Fireplace,Kitchen'),
('Beach Paradise','Bali','Bungalow',249.99,2,'Private Pool,Breakfast');
