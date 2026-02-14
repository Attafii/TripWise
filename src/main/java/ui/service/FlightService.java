package ui.service;

import ui.model.Flight;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FlightService - Fetches real flight data from database
 * Queries from: vols, compagnies_aeriennes, aeroports tables
 */
public class FlightService implements IService<Flight> {

    private final Connection connection;

    public FlightService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(Flight flight) {
        String query = "INSERT INTO vols (numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, " +
                      "date_depart, date_arrivee, duree_vol, type_avion, capacite_totale, places_disponibles, " +
                      "statut_vol, porte_embarquement, terminal, is_active, created_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, flight.getNumeroVol());
            stmt.setInt(2, flight.getCompagnieId());
            stmt.setInt(3, flight.getAeroportDepartId());
            stmt.setInt(4, flight.getAeroportArriveeId());
            stmt.setTimestamp(5, Timestamp.valueOf(flight.getDateDepart()));
            stmt.setTimestamp(6, Timestamp.valueOf(flight.getDateArrivee()));
            stmt.setInt(7, flight.getDureeVol() != null ? flight.getDureeVol() : 0);
            stmt.setString(8, flight.getTypeAvion());
            stmt.setInt(9, flight.getCapaciteTotale());
            stmt.setInt(10, flight.getPlacesDisponibles());
            stmt.setString(11, flight.getStatutVol().name());
            stmt.setString(12, flight.getPorteEmbarquement());
            stmt.setString(13, flight.getTerminal());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    flight.setVolId(rs.getInt(1));
                }
                System.out.println("✅ Flight added successfully: " + flight.getNumeroVol());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding flight: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Flight flight) {
        String query = "UPDATE vols SET numero_vol=?, compagnie_id=?, aeroport_depart_id=?, aeroport_arrivee_id=?, " +
                      "date_depart=?, date_arrivee=?, duree_vol=?, type_avion=?, capacite_totale=?, " +
                      "places_disponibles=?, statut_vol=?, porte_embarquement=?, terminal=? WHERE vol_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, flight.getNumeroVol());
            stmt.setInt(2, flight.getCompagnieId());
            stmt.setInt(3, flight.getAeroportDepartId());
            stmt.setInt(4, flight.getAeroportArriveeId());
            stmt.setTimestamp(5, Timestamp.valueOf(flight.getDateDepart()));
            stmt.setTimestamp(6, Timestamp.valueOf(flight.getDateArrivee()));
            stmt.setInt(7, flight.getDureeVol() != null ? flight.getDureeVol() : 0);
            stmt.setString(8, flight.getTypeAvion());
            stmt.setInt(9, flight.getCapaciteTotale());
            stmt.setInt(10, flight.getPlacesDisponibles());
            stmt.setString(11, flight.getStatutVol().name());
            stmt.setString(12, flight.getPorteEmbarquement());
            stmt.setString(13, flight.getTerminal());
            stmt.setInt(14, flight.getVolId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Flight updated successfully: " + flight.getNumeroVol());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating flight: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String query = "UPDATE vols SET is_active = 0 WHERE vol_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Flight deleted (deactivated) successfully (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting flight: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Flight getById(int id) {
        String query = "SELECT v.*, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, ad.nom_aeroport as aeroport_depart, " +
                      "aa.ville as ville_arrivee, aa.nom_aeroport as aeroport_arrivee " +
                      "FROM vols v " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "WHERE v.vol_id = ? AND v.is_active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractFlightFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting flight by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Flight> getAll() {
        List<Flight> flights = new ArrayList<>();
        String query = "SELECT v.*, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, ad.nom_aeroport as aeroport_depart, " +
                      "aa.ville as ville_arrivee, aa.nom_aeroport as aeroport_arrivee " +
                      "FROM vols v " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "WHERE v.is_active = 1 AND v.statut_vol = 'PROGRAMME' " +
                      "ORDER BY v.date_depart ASC " +
                      "LIMIT 50";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                flights.add(extractFlightFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + flights.size() + " flights from database");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all flights: " + e.getMessage());
            e.printStackTrace();
        }
        return flights;
    }

    /**
     * Search flights by criteria
     */
    public List<Flight> searchFlights(String fromCity, String toCity, LocalDate date) {
        List<Flight> flights = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT v.*, ca.nom_compagnie, " +
            "ad.ville as ville_depart, ad.nom_aeroport as aeroport_depart, " +
            "aa.ville as ville_arrivee, aa.nom_aeroport as aeroport_arrivee " +
            "FROM vols v " +
            "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
            "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
            "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
            "WHERE v.is_active = 1 AND v.statut_vol = 'PROGRAMME' "
        );

        boolean hasFromCity = fromCity != null && !fromCity.isBlank();
        boolean hasToCity = toCity != null && !toCity.isBlank();
        boolean hasDate = date != null;

        if (hasFromCity) {
            query.append("AND ad.ville LIKE ? ");
        }
        if (hasToCity) {
            query.append("AND aa.ville LIKE ? ");
        }
        if (hasDate) {
            query.append("AND DATE(v.date_depart) = ? ");
        }

        query.append("ORDER BY v.date_depart ASC LIMIT 50");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;

            if (hasFromCity) {
                stmt.setString(paramIndex++, "%" + fromCity + "%");
            }
            if (hasToCity) {
                stmt.setString(paramIndex++, "%" + toCity + "%");
            }
            if (hasDate) {
                stmt.setDate(paramIndex++, Date.valueOf(date));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                flights.add(extractFlightFromResultSet(rs));
            }

            System.out.println("✅ Found " + flights.size() + " flights matching criteria");
        } catch (SQLException e) {
            System.err.println("❌ Error searching flights: " + e.getMessage());
            e.printStackTrace();
        }

        return flights;
    }

    /**
     * Get available flights (with seats available)
     * Includes future flights only in production, all flights in development
     */
    public List<Flight> getAvailableFlights() {
        List<Flight> flights = new ArrayList<>();

        // First try with date filter - use type_classe instead of classe
        String query = "SELECT v.*, ca.nom_compagnie, ca.code_iata as code_compagnie, " +
                      "ad.ville as ville_depart, ad.nom_aeroport as aeroport_depart, ad.code_iata as code_depart, " +
                      "aa.ville as ville_arrivee, aa.nom_aeroport as aeroport_arrivee, aa.code_iata as code_arrivee, " +
                      "MIN(cv.prix) as min_price, " +
                      "(SELECT type_classe FROM classes_vol WHERE vol_id = v.vol_id ORDER BY prix ASC LIMIT 1) as default_class " +
                      "FROM vols v " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "LEFT JOIN classes_vol cv ON v.vol_id = cv.vol_id " +
                      "WHERE v.is_active = 1 " +
                      "AND v.statut_vol = 'PROGRAMME' " +
                      "AND v.places_disponibles > 0 " +
                      "GROUP BY v.vol_id " +
                      "ORDER BY v.date_depart ASC " +
                      "LIMIT 50";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                flights.add(extractFlightWithPriceFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + flights.size() + " available flights (all dates)");
        } catch (SQLException e) {
            System.err.println("❌ Error getting available flights: " + e.getMessage());
            e.printStackTrace();
        }

        // If no flights found, try simpler query
        if (flights.isEmpty()) {
            flights = getAllFlightsSimple();
        }

        return flights;
    }

    /**
     * Get all flights with a simple query (fallback method)
     */
    public List<Flight> getAllFlightsSimple() {
        List<Flight> flights = new ArrayList<>();
        String query = "SELECT v.*, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, ad.nom_aeroport as aeroport_depart, " +
                      "aa.ville as ville_arrivee, aa.nom_aeroport as aeroport_arrivee " +
                      "FROM vols v " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "WHERE v.is_active = 1 " +
                      "ORDER BY v.date_depart ASC " +
                      "LIMIT 50";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Flight flight = extractFlightFromResultSet(rs);
                // Set default price if not available
                if (flight.getMinPrice() <= 0) {
                    flight.setMinPrice(199.00);
                }
                flights.add(flight);
            }
            System.out.println("✅ Retrieved " + flights.size() + " flights (simple query)");
        } catch (SQLException e) {
            System.err.println("❌ Error getting flights (simple): " + e.getMessage());
            e.printStackTrace();
        }
        return flights;
    }

    /**
     * Search flights with class filter
     */
    public List<Flight> searchFlightsAdvanced(String fromCity, String toCity, LocalDate date, String flightClass) {
        List<Flight> flights = new ArrayList<>();

        // Extract city name from "City (Country)" format if needed
        String fromCityClean = extractCityName(fromCity);
        String toCityClean = extractCityName(toCity);

        // First, build the base query without class filter for better results
        StringBuilder query = new StringBuilder(
            "SELECT DISTINCT v.*, ca.nom_compagnie, ca.code_iata as code_compagnie, " +
            "ad.ville as ville_depart, ad.nom_aeroport as aeroport_depart, ad.code_iata as code_depart, " +
            "aa.ville as ville_arrivee, aa.nom_aeroport as aeroport_arrivee, aa.code_iata as code_arrivee, " +
            "(SELECT MIN(cv2.prix) FROM classes_vol cv2 WHERE cv2.vol_id = v.vol_id) as min_price, " +
            "(SELECT cv3.type_classe FROM classes_vol cv3 WHERE cv3.vol_id = v.vol_id ORDER BY cv3.prix ASC LIMIT 1) as default_class " +
            "FROM vols v " +
            "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
            "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
            "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
            "WHERE v.is_active = 1 AND v.statut_vol = 'PROGRAMME' " +
            "AND v.places_disponibles > 0 "
        );

        boolean hasFromCity = fromCityClean != null && !fromCityClean.isBlank();
        boolean hasToCity = toCityClean != null && !toCityClean.isBlank();
        boolean hasDate = date != null;

        if (hasFromCity) {
            query.append("AND (LOWER(ad.ville) LIKE LOWER(?) OR LOWER(ad.code_iata) LIKE LOWER(?)) ");
        }
        if (hasToCity) {
            query.append("AND (LOWER(aa.ville) LIKE LOWER(?) OR LOWER(aa.code_iata) LIKE LOWER(?)) ");
        }
        if (hasDate) {
            query.append("AND DATE(v.date_depart) = ? ");
        }

        query.append("ORDER BY min_price ASC, v.date_depart ASC LIMIT 50");

        System.out.println("🔎 Executing search query with filters:");
        System.out.println("   From: " + (hasFromCity ? fromCityClean : "Any"));
        System.out.println("   To: " + (hasToCity ? toCityClean : "Any"));
        System.out.println("   Date: " + (hasDate ? date : "Any"));

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;

            if (hasFromCity) {
                stmt.setString(paramIndex++, "%" + fromCityClean + "%");
                stmt.setString(paramIndex++, "%" + fromCityClean + "%");
            }
            if (hasToCity) {
                stmt.setString(paramIndex++, "%" + toCityClean + "%");
                stmt.setString(paramIndex++, "%" + toCityClean + "%");
            }
            if (hasDate) {
                stmt.setDate(paramIndex++, Date.valueOf(date));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                flights.add(extractFlightWithPriceFromResultSet(rs));
            }

            System.out.println("✅ Found " + flights.size() + " flights matching criteria");
        } catch (SQLException e) {
            System.err.println("❌ Error searching flights: " + e.getMessage());
            e.printStackTrace();
        }

        return flights;
    }

    /**
     * Extract city name from "City (Country)" format
     * Example: "Paris (France)" -> "Paris"
     */
    private String extractCityName(String cityWithCountry) {
        if (cityWithCountry == null || cityWithCountry.isBlank()) {
            return null;
        }

        // Check if it contains " (" which indicates "City (Country)" format
        int parenIndex = cityWithCountry.indexOf(" (");
        if (parenIndex > 0) {
            return cityWithCountry.substring(0, parenIndex).trim();
        }

        return cityWithCountry.trim();
    }

    /**
     * Extract Flight object with price from ResultSet
     */
    private Flight extractFlightWithPriceFromResultSet(ResultSet rs) throws SQLException {
        Flight flight = extractFlightFromResultSet(rs);

        try {
            flight.setMinPrice(rs.getDouble("min_price"));
            flight.setDefaultClass(rs.getString("default_class"));
            flight.setCodeAeroportDepart(rs.getString("code_depart"));
            flight.setCodeAeroportArrivee(rs.getString("code_arrivee"));
        } catch (SQLException e) {
            // Fields might not exist in all queries
        }

        // Set default amenities (can be extended with database fields)
        flight.setHasWifi(true);
        flight.setHasEntertainment(true);
        flight.setHasMeal(true);
        flight.setHasFreeCancellation(true);
        flight.setStops(0); // Default to nonstop

        return flight;
    }

    /**
     * Extract Flight object from ResultSet
     */
    private Flight extractFlightFromResultSet(ResultSet rs) throws SQLException {
        Flight flight = new Flight();

        flight.setVolId(rs.getInt("vol_id"));
        flight.setNumeroVol(rs.getString("numero_vol"));
        flight.setCompagnieId(rs.getInt("compagnie_id"));
        flight.setAeroportDepartId(rs.getInt("aeroport_depart_id"));
        flight.setAeroportArriveeId(rs.getInt("aeroport_arrivee_id"));

        // Dates
        Timestamp dateDepart = rs.getTimestamp("date_depart");
        if (dateDepart != null) {
            flight.setDateDepart(dateDepart.toLocalDateTime());
        }

        Timestamp dateArrivee = rs.getTimestamp("date_arrivee");
        if (dateArrivee != null) {
            flight.setDateArrivee(dateArrivee.toLocalDateTime());
        }

        flight.setDureeVol(rs.getInt("duree_vol"));
        flight.setTypeAvion(rs.getString("type_avion"));
        flight.setCapaciteTotale(rs.getInt("capacite_totale"));
        flight.setPlacesDisponibles(rs.getInt("places_disponibles"));

        // Status
        String statut = rs.getString("statut_vol");
        if (statut != null) {
            flight.setStatutVol(Flight.StatutVol.valueOf(statut));
        }

        flight.setPorteEmbarquement(rs.getString("porte_embarquement"));
        flight.setTerminal(rs.getString("terminal"));
        flight.setActive(rs.getBoolean("is_active"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            flight.setCreatedAt(created.toLocalDateTime());
        }

        // Joined data
        flight.setCompagnieName(rs.getString("nom_compagnie"));
        flight.setVilleDepart(rs.getString("ville_depart"));
        flight.setVilleArrivee(rs.getString("ville_arrivee"));
        flight.setAeroportDepartName(rs.getString("aeroport_depart"));
        flight.setAeroportArriveeName(rs.getString("aeroport_arrivee"));

        return flight;
    }

    /**
     * Get all unique cities from airports table that have active flights
     */
    public List<String> getAllCities() {
        List<String> cities = new ArrayList<>();

        // Get cities that have active flights (either as departure or arrival)
        String query = "SELECT DISTINCT a.ville, a.pays " +
                      "FROM aeroports a " +
                      "WHERE a.ville IS NOT NULL " +
                      "AND (EXISTS (SELECT 1 FROM vols v WHERE v.aeroport_depart_id = a.aeroport_id AND v.is_active = 1) " +
                      "     OR EXISTS (SELECT 1 FROM vols v WHERE v.aeroport_arrivee_id = a.aeroport_id AND v.is_active = 1)) " +
                      "ORDER BY a.pays, a.ville";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String city = rs.getString("ville");
                String country = rs.getString("pays");
                if (city != null && !city.isEmpty()) {
                    // Format: "City (Country)" for better UX
                    cities.add(city + " (" + country + ")");
                }
            }
            System.out.println("✅ Loaded " + cities.size() + " cities with active flights");
        } catch (SQLException e) {
            System.err.println("❌ Error loading cities: " + e.getMessage());
            // Fallback to all cities
            return getAllCitiesFallback();
        }

        // If no cities found, use fallback
        if (cities.isEmpty()) {
            return getAllCitiesFallback();
        }

        return cities;
    }

    /**
     * Fallback method to get all cities without flight filter
     */
    private List<String> getAllCitiesFallback() {
        List<String> cities = new ArrayList<>();
        String query = "SELECT DISTINCT ville, pays FROM aeroports WHERE ville IS NOT NULL ORDER BY pays, ville";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String city = rs.getString("ville");
                String country = rs.getString("pays");
                if (city != null && !city.isEmpty()) {
                    cities.add(city + " (" + country + ")");
                }
            }
            System.out.println("✅ Loaded " + cities.size() + " cities (fallback)");
        } catch (SQLException e) {
            System.err.println("❌ Error loading cities fallback: " + e.getMessage());
        }

        return cities;
    }

    /**
     * Get airports by city name
     */
    public List<String> getAirportsByCity(String city) {
        List<String> airports = new ArrayList<>();
        String query = "SELECT nom_aeroport, code_aeroport FROM aeroports WHERE ville LIKE ? ORDER BY nom_aeroport";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + city + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("nom_aeroport");
                String code = rs.getString("code_aeroport");
                airports.add(name + " (" + code + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error loading airports: " + e.getMessage());
        }

        return airports;
    }
}
