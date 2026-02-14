TripWise Demo Guide

1. Overview
- TripWise is a JavaFX travel management app with an embedded HTTP API and an H2 file database.
- You can demonstrate booking hotels and renting cars, show persistence in the DB, and exercise API endpoints live.

2. Prerequisites
- Java 17+ and Maven.
- Optional DB viewer (e.g., DBeaver) to browse the H2 database.

3. Run the App
- Open a terminal in the project root:
  - cd /d C:\Users\USER\TripWise
  - mvn clean -DskipTests javafx:run
- On startup, the embedded API prints:
  - Payment API listening on http://localhost:9090/

4. Payment API
- Base URL: http://localhost:9090
- Endpoints:
  - GET / → lists available endpoints.
  - POST /api/payments/process → validates and simulates a card payment, returns a transactionId and records the payment.
  - GET /api/payments → lists recorded payments (id, last4, createdAt).
  - GET /api/bookings/hotels → rollup: hotels with confirmed booking counts.
  - GET /api/rentals/cars → rollup: cars with confirmed rental counts.

4.1 Quick Tests (PowerShell-friendly)
- Form-encoded payment (easiest):
  - curl.exe -H "Content-Type: application/x-www-form-urlencoded" `
    -d "cardNumber=4111111111111111&expiryDate=12/27&cvv=123" `
    http://localhost:9090/api/payments/process
- List payments:
  - curl.exe http://localhost:9090/api/payments
- List booked hotels:
  - curl.exe http://localhost:9090/api/bookings/hotels
- List booked cars:
  - curl.exe http://localhost:9090/api/rentals/cars

4.2 JSON Option (PowerShell-safe)
- Save JSON then POST it:
  - @'
    {"cardNumber":"4111111111111111","expiryDate":"12/27","cvv":"123"}
    '@ | Set-Content body.json -Encoding UTF8
  - curl.exe -H "Content-Type: application/json" --data-binary @body.json http://localhost:9090/api/payments/process

5. Database
- Engine: H2 (file-based), initialized from classpath script at startup.
- Files: tripwise_db.* in the project root.
- JDBC URL: jdbc:h2:file:./tripwise_db;AUTO_SERVER=TRUE
- Credentials: user sa, empty password.
- Schema/tables:
  - HOTELS(name, city, price_per_night, rating, description)
  - ROOMS(hotel_name, hotel_city, name, price_per_night, capacity, amenities)
  - BOOKINGS(id, hotel_name, hotel_city, room_name, check_in, check_out, total_price, status)
  - CAR_RENTALS(id, brand, model, pickup_location, pickup_date, return_date, total_price, status)
  - PAYMENTS(id, last4, created_at)
- Seed data is loaded from src/main/resources/db/mock.sql.

5.1 Example Queries (any H2 client)
- Recent payments:
  - SELECT * FROM PAYMENTS ORDER BY created_at DESC;
- Booked hotel rollup:
  - SELECT h.name, h.city, COUNT(b.id) AS bookings
    FROM BOOKINGS b JOIN HOTELS h ON b.hotel_name=h.name AND b.hotel_city=h.city
    WHERE UPPER(b.status)='CONFIRMED'
    GROUP BY h.name, h.city ORDER BY bookings DESC, h.city, h.name;
- Booked car rollup:
  - SELECT brand, model, COUNT(id) AS bookings
    FROM CAR_RENTALS WHERE UPPER(status)='CONFIRMED'
    GROUP BY brand, model ORDER BY bookings DESC, brand, model;

6. Feature Walkthrough (Teacher Demo)
- 6.1 Hotels
  - Open the app Dashboard → Book Hotel.
  - Search by destination (e.g., “Paris”).
  - Double‑click a hotel → Details → double‑click a room → Booking screen.
  - Enter test card details and Confirm. The app calls the payment API; on success, booking is saved to DB and UI shows CONFIRMED with a transaction ID.
  - Navigate back to “Hotel Search” → click “Booked Hotels” to show the rollup list in the UI.
  - Optional: Show the API rollup in a terminal: curl.exe http://localhost:9090/api/bookings/hotels
  - Optional: Show the saved booking in DB using queries in section 5.1.
- 6.2 Car Rentals
  - Dashboard → Rent Car → choose filters → double‑click a car → Details → Rent Now.
  - Enter card details and Confirm. On success, rental is saved to DB.
  - Back to “Car Search” → “Booked Cars” to show rollup in the UI.
  - Optional: API rollup in terminal: curl.exe http://localhost:9090/api/rentals/cars
- 6.3 Payments list
  - Run another payment (hotel or car) and then show: curl.exe http://localhost:9090/api/payments
  - Explain: items display last four digits and timestamps sourced from PAYMENTS table.

7. Troubleshooting
- API not found/404: verify the app is running and uses http://localhost:9090/ (port printed on startup).
- PowerShell quoting: prefer curl.exe (not curl), or Invoke-RestMethod; avoid ^ line breaks (use ` or single line).
- 400 Invalid payment details: ensure fields are set and encoded properly; form-encoded body is the simplest.
- H2 DB locked: close external viewers before re-running the app.

8. Source References
- App entry: src/main/java/ui/app/Main.java
- API server: src/main/java/ui/api/PaymentApiServer.java
- DB bootstrap: src/main/java/ui/db/Database.java
- Schema/seed: src/main/resources/db/mock.sql
- Repos: src/main/java/ui/repo/*.java
- Controllers: src/main/java/ui/controllers/*.java
- FXML: src/main/resources/ui/**/*

9. Talking Points (during the demo)
- Clean separation between UI actions, API payment handling, and persistence.
- Rollup endpoints and UI screens provide quick visibility of overall activity.
- H2 file DB keeps the demo self‑contained and reproducible without external services.
