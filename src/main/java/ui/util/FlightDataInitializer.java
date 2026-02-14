package ui.util;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * FlightDataInitializer - Initializes mock flight data for testing
 * Call FlightDataInitializer.initializeFlightData() at app startup
 */
public class FlightDataInitializer {

    private static boolean dataInitialized = false;

    /**
     * Initialize flight data if not already present
     */
    public static void initializeFlightData() {
        if (dataInitialized) {
            System.out.println("✅ Flight data already initialized");
            return;
        }

        Connection conn = DataSource.getInstance().getConnection();

        try {
            // Check if data already exists
            if (hasExistingData(conn)) {
                System.out.println("✅ Flight data already exists in database");
                dataInitialized = true;
                return;
            }

            System.out.println("📦 Initializing flight mock data...");

            // Create tables if not exist
            createTablesIfNotExist(conn);

            // Insert airlines
            insertAirlines(conn);

            // Insert airports
            insertAirports(conn);

            // Insert flights
            insertFlights(conn);

            // Insert flight classes
            insertFlightClasses(conn);

            // Insert test travelers
            insertTestTravelers(conn);

            // Insert sample bookings
            insertSampleBookings(conn);

            dataInitialized = true;
            System.out.println("✅ Flight mock data initialized successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error initializing flight data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean hasExistingData(Connection conn) throws SQLException {
        try {
            String query = "SELECT COUNT(*) FROM vols";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // Table might not exist
            return false;
        }
        return false;
    }

    private static void createTablesIfNotExist(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        // Airlines table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS compagnies_aeriennes (
                compagnie_id INT AUTO_INCREMENT PRIMARY KEY,
                nom_compagnie VARCHAR(100) NOT NULL,
                code_iata VARCHAR(3),
                pays VARCHAR(50),
                logo_url VARCHAR(255),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Airports table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS aeroports (
                aeroport_id INT AUTO_INCREMENT PRIMARY KEY,
                code_iata VARCHAR(3) NOT NULL UNIQUE,
                nom_aeroport VARCHAR(150) NOT NULL,
                ville VARCHAR(100) NOT NULL,
                pays VARCHAR(50) NOT NULL,
                timezone VARCHAR(50),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Flights table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS vols (
                vol_id INT AUTO_INCREMENT PRIMARY KEY,
                numero_vol VARCHAR(10) NOT NULL,
                compagnie_id INT NOT NULL,
                aeroport_depart_id INT NOT NULL,
                aeroport_arrivee_id INT NOT NULL,
                date_depart DATETIME NOT NULL,
                date_arrivee DATETIME NOT NULL,
                duree_vol INT,
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
                FOREIGN KEY (aeroport_arrivee_id) REFERENCES aeroports(aeroport_id)
            )
        """);

        // Flight classes table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS classes_vol (
                classe_vol_id INT AUTO_INCREMENT PRIMARY KEY,
                vol_id INT NOT NULL,
                classe ENUM('ECONOMIQUE', 'PREMIUM', 'BUSINESS', 'PREMIERE') NOT NULL,
                prix DECIMAL(10, 2) NOT NULL,
                places_disponibles INT NOT NULL DEFAULT 50,
                avantages TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (vol_id) REFERENCES vols(vol_id) ON DELETE CASCADE
            )
        """);

        // Travelers table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS voyageurs (
                voyageur_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL UNIQUE,
                passeport VARCHAR(20),
                nationalite VARCHAR(50),
                date_naissance DATE,
                preferences_voyage TEXT,
                points_fidelite INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Flight bookings table
        stmt.execute("""
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
                FOREIGN KEY (classe_vol_id) REFERENCES classes_vol(classe_vol_id) ON DELETE CASCADE
            )
        """);

        System.out.println("✅ Tables created successfully");
    }

    private static void insertAirlines(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO compagnies_aeriennes (compagnie_id, nom_compagnie, code_iata, pays, is_active) VALUES (?, ?, ?, ?, TRUE)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        Object[][] airlines = {
            {1, "Air France", "AF", "France"},
            {2, "Emirates", "EK", "United Arab Emirates"},
            {3, "Lufthansa", "LH", "Germany"},
            {4, "British Airways", "BA", "United Kingdom"},
            {5, "Qatar Airways", "QR", "Qatar"},
            {6, "Turkish Airlines", "TK", "Turkey"},
            {7, "Tunisair", "TU", "Tunisia"},
            {8, "Delta Airlines", "DL", "United States"},
            {9, "Singapore Airlines", "SQ", "Singapore"},
            {10, "KLM Royal Dutch", "KL", "Netherlands"}
        };

        for (Object[] airline : airlines) {
            stmt.setInt(1, (int) airline[0]);
            stmt.setString(2, (String) airline[1]);
            stmt.setString(3, (String) airline[2]);
            stmt.setString(4, (String) airline[3]);
            stmt.addBatch();
        }
        stmt.executeBatch();
        System.out.println("✅ Airlines inserted: " + airlines.length);
    }

    private static void insertAirports(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO aeroports (aeroport_id, code_iata, nom_aeroport, ville, pays, is_active) VALUES (?, ?, ?, ?, ?, TRUE)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        Object[][] airports = {
            {1, "CDG", "Charles de Gaulle Airport", "Paris", "France"},
            {2, "JFK", "John F. Kennedy International", "New York", "United States"},
            {3, "DXB", "Dubai International Airport", "Dubai", "United Arab Emirates"},
            {4, "LHR", "London Heathrow Airport", "London", "United Kingdom"},
            {5, "TUN", "Tunis-Carthage Airport", "Tunis", "Tunisia"},
            {6, "FRA", "Frankfurt Airport", "Frankfurt", "Germany"},
            {7, "IST", "Istanbul Airport", "Istanbul", "Turkey"},
            {8, "DOH", "Hamad International Airport", "Doha", "Qatar"},
            {9, "SIN", "Singapore Changi Airport", "Singapore", "Singapore"},
            {10, "AMS", "Amsterdam Schiphol Airport", "Amsterdam", "Netherlands"},
            {11, "LAX", "Los Angeles International", "Los Angeles", "United States"},
            {12, "FCO", "Leonardo da Vinci Airport", "Rome", "Italy"},
            {13, "BCN", "Barcelona–El Prat Airport", "Barcelona", "Spain"},
            {14, "MUC", "Munich Airport", "Munich", "Germany"},
            {15, "ORY", "Paris Orly Airport", "Paris", "France"}
        };

        for (Object[] airport : airports) {
            stmt.setInt(1, (int) airport[0]);
            stmt.setString(2, (String) airport[1]);
            stmt.setString(3, (String) airport[2]);
            stmt.setString(4, (String) airport[3]);
            stmt.setString(5, (String) airport[4]);
            stmt.addBatch();
        }
        stmt.executeBatch();
        System.out.println("✅ Airports inserted: " + airports.length);
    }

    private static void insertFlights(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO vols (vol_id, numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, " +
                    "date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, " +
                    "statut_vol, porte_embarquement, terminal, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PROGRAMME', ?, ?, TRUE)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        // Generate flights for the next 60 days
        LocalDateTime baseDate = LocalDateTime.now().plusDays(5);
        Random rand = new Random();

        Object[][] flightTemplates = {
            // {id, flightNum, airlineId, depAirport, arrAirport, durationMins, aircraft, capacity, gate, terminal}
            {1, "AF001", 1, 1, 2, 495, "Airbus A380", 450, "A12", "2E"},
            {2, "AF003", 1, 1, 2, 495, "Boeing 777", 350, "B8", "2E"},
            {3, "DL100", 8, 2, 1, 510, "Airbus A350", 300, "C5", "1"},
            {4, "EK201", 2, 3, 4, 450, "Airbus A380", 500, "A1", "3"},
            {5, "EK205", 2, 3, 1, 420, "Boeing 777", 380, "B3", "3"},
            {6, "LH440", 3, 6, 2, 540, "Airbus A340", 280, "D12", "1"},
            {7, "BA116", 4, 4, 2, 495, "Boeing 787", 250, "C8", "5"},
            {8, "AF1024", 1, 1, 12, 135, "Airbus A320", 180, "B2", "2F"},
            {9, "TU720", 7, 5, 1, 150, "Airbus A320", 150, "A1", "1"},
            {10, "TU721", 7, 1, 5, 150, "Airbus A320", 150, "B5", "2G"},
            {11, "QR001", 5, 8, 4, 420, "Airbus A350", 350, "C1", "A"},
            {12, "TK1800", 6, 7, 1, 210, "Boeing 787", 280, "B10", "I"},
            {13, "SQ322", 9, 9, 4, 810, "Airbus A380", 400, "A20", "3"},
            {14, "KL1230", 10, 10, 1, 80, "Embraer 190", 100, "C2", "D"},
            {15, "DL401", 8, 2, 11, 330, "Boeing 767", 220, "D5", "4"}
        };

        int id = 1;
        for (int dayOffset = 0; dayOffset < 30; dayOffset += 2) {
            for (Object[] template : flightTemplates) {
                LocalDateTime departure = baseDate.plusDays(dayOffset).plusHours(rand.nextInt(12) + 6);
                LocalDateTime arrival = departure.plusMinutes((int) template[5]);

                stmt.setInt(1, id++);
                stmt.setString(2, (String) template[1]);
                stmt.setInt(3, (int) template[2]);
                stmt.setInt(4, (int) template[3]);
                stmt.setInt(5, (int) template[4]);
                stmt.setTimestamp(6, Timestamp.valueOf(departure));
                stmt.setTimestamp(7, Timestamp.valueOf(arrival));
                stmt.setInt(8, (int) template[5]);
                stmt.setString(9, (String) template[6]);
                stmt.setInt(10, (int) template[7]);
                stmt.setInt(11, (int) template[7] - rand.nextInt(100));
                stmt.setString(12, (String) template[8]);
                stmt.setString(13, (String) template[9]);
                stmt.addBatch();
            }
        }
        stmt.executeBatch();
        System.out.println("✅ Flights inserted: " + (id - 1));
    }

    private static void insertFlightClasses(Connection conn) throws SQLException {
        // Get all flight IDs
        String selectSql = "SELECT vol_id FROM vols";
        Statement selectStmt = conn.createStatement();
        ResultSet rs = selectStmt.executeQuery(selectSql);

        String insertSql = "INSERT IGNORE INTO classes_vol (vol_id, classe, prix, places_disponibles, avantages) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);

        Random rand = new Random();
        int count = 0;

        while (rs.next()) {
            int volId = rs.getInt("vol_id");

            // Economy class for all flights
            double basePrice = 150 + rand.nextInt(500);
            insertStmt.setInt(1, volId);
            insertStmt.setString(2, "ECONOMIQUE");
            insertStmt.setDouble(3, basePrice);
            insertStmt.setInt(4, 50 + rand.nextInt(100));
            insertStmt.setString(5, "Repas inclus, Bagage 23kg, Entertainment");
            insertStmt.addBatch();
            count++;

            // Premium class (80% of flights)
            if (rand.nextDouble() < 0.8) {
                insertStmt.setInt(1, volId);
                insertStmt.setString(2, "PREMIUM");
                insertStmt.setDouble(3, basePrice * 1.8);
                insertStmt.setInt(4, 20 + rand.nextInt(40));
                insertStmt.setString(5, "Extra legroom, Priority boarding, 32kg bagage");
                insertStmt.addBatch();
                count++;
            }

            // Business class (60% of flights)
            if (rand.nextDouble() < 0.6) {
                insertStmt.setInt(1, volId);
                insertStmt.setString(2, "BUSINESS");
                insertStmt.setDouble(3, basePrice * 4.0);
                insertStmt.setInt(4, 10 + rand.nextInt(30));
                insertStmt.setString(5, "Lit plat, Lounge access, 2 bagages, Repas gourmet");
                insertStmt.addBatch();
                count++;
            }

            // First class (20% of flights)
            if (rand.nextDouble() < 0.2) {
                insertStmt.setInt(1, volId);
                insertStmt.setString(2, "PREMIERE");
                insertStmt.setDouble(3, basePrice * 10.0);
                insertStmt.setInt(4, 5 + rand.nextInt(10));
                insertStmt.setString(5, "Suite privée, Chef à bord, Limousine, Spa access");
                insertStmt.addBatch();
                count++;
            }
        }
        insertStmt.executeBatch();
        System.out.println("✅ Flight classes inserted: " + count);
    }

    private static void insertTestTravelers(Connection conn) throws SQLException {
        // First check if users exist
        String checkSql = "SELECT user_id FROM users LIMIT 5";
        Statement checkStmt = conn.createStatement();
        ResultSet rs = checkStmt.executeQuery(checkSql);

        String insertSql = "INSERT IGNORE INTO voyageurs (user_id, preferences_voyage, points_fidelite, nationalite) VALUES (?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);

        String[] prefs = {"Window seat, Vegetarian", "Aisle seat, Halal", "Any seat, Kosher", "Window seat, Vegan", "Exit row preferred"};
        String[] nationalities = {"French", "Tunisian", "American", "British", "German"};
        Random rand = new Random();
        int count = 0;

        while (rs.next()) {
            int userId = rs.getInt("user_id");
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, prefs[rand.nextInt(prefs.length)]);
            insertStmt.setInt(3, rand.nextInt(5000));
            insertStmt.setString(4, nationalities[rand.nextInt(nationalities.length)]);
            insertStmt.addBatch();
            count++;
        }
        insertStmt.executeBatch();
        System.out.println("✅ Test travelers inserted: " + count);
    }

    private static void insertSampleBookings(Connection conn) throws SQLException {
        // Get traveler and flight info
        String travelerSql = "SELECT voyageur_id FROM voyageurs LIMIT 3";
        String flightSql = "SELECT cv.classe_vol_id, cv.vol_id, cv.prix FROM classes_vol cv JOIN vols v ON cv.vol_id = v.vol_id WHERE v.date_depart > NOW() LIMIT 20";

        Statement stmt = conn.createStatement();
        ResultSet travelers = stmt.executeQuery(travelerSql);
        int[] travelerIds = new int[3];
        int idx = 0;
        while (travelers.next() && idx < 3) {
            travelerIds[idx++] = travelers.getInt("voyageur_id");
        }

        if (idx == 0) {
            System.out.println("⚠️ No travelers found, skipping sample bookings");
            return;
        }

        ResultSet flights = stmt.executeQuery(flightSql);

        String insertSql = "INSERT IGNORE INTO reservations_vol (voyageur_id, vol_id, classe_vol_id, nombre_passagers, " +
                          "prix_total, statut_reservation, numero_confirmation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);

        Random rand = new Random();
        String[] statuses = {"EN_ATTENTE", "CONFIRMEE", "CONFIRMEE", "CONFIRMEE"};
        int count = 0;

        while (flights.next() && count < 15) {
            int passengers = 1 + rand.nextInt(3);
            double price = flights.getDouble("prix") * passengers;

            insertStmt.setInt(1, travelerIds[rand.nextInt(idx)]);
            insertStmt.setInt(2, flights.getInt("vol_id"));
            insertStmt.setInt(3, flights.getInt("classe_vol_id"));
            insertStmt.setInt(4, passengers);
            insertStmt.setDouble(5, price);
            insertStmt.setString(6, statuses[rand.nextInt(statuses.length)]);
            insertStmt.setString(7, generateConfirmationCode());
            insertStmt.addBatch();
            count++;
        }
        insertStmt.executeBatch();
        System.out.println("✅ Sample bookings inserted: " + count);
    }

    private static String generateConfirmationCode() {
        Random rand = new Random();
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return "" + letters.charAt(rand.nextInt(26)) + letters.charAt(rand.nextInt(26)) +
               String.format("%04d", rand.nextInt(10000));
    }
}
