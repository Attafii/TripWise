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
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, flight.getNumeroVol());
            stmt.setInt(2, flight.getCompagnieId());
            stmt.setInt(3, flight.getAeroportDepartId());
            stmt.setInt(4, flight.getAeroportArriveeId());
            stmt.setTimestamp(5, flight.getDateDepart() != null ? Timestamp.valueOf(flight.getDateDepart()) : null);
            stmt.setTimestamp(6, flight.getDateArrivee() != null ? Timestamp.valueOf(flight.getDateArrivee()) : null);
            stmt.setInt(7, flight.getDureeVol() != null ? flight.getDureeVol() : 0);
            stmt.setString(8, flight.getTypeAvion());
            stmt.setInt(9, flight.getCapaciteTotale());
            stmt.setInt(10, flight.getPlacesDisponibles());
            stmt.setString(11, flight.getStatutVol() != null ? flight.getStatutVol().name() : "PROGRAMME");
            stmt.setString(12, flight.getPorteEmbarquement());
            stmt.setString(13, flight.getTerminal());
            stmt.setBoolean(14, flight.isActive());
            stmt.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            
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
                      "date_depart=?, date_arrivee=?, duree_vol=?, type_avion=?, capacite_totale=?, places_disponibles=?, " +
                      "statut_vol=?, porte_embarquement=?, terminal=?, is_active=? " +
                      "WHERE vol_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, flight.getNumeroVol());
            stmt.setInt(2, flight.getCompagnieId());
            stmt.setInt(3, flight.getAeroportDepartId());
            stmt.setInt(4, flight.getAeroportArriveeId());
            stmt.setTimestamp(5, flight.getDateDepart() != null ? Timestamp.valueOf(flight.getDateDepart()) : null);
            stmt.setTimestamp(6, flight.getDateArrivee() != null ? Timestamp.valueOf(flight.getDateArrivee()) : null);
            stmt.setInt(7, flight.getDureeVol() != null ? flight.getDureeVol() : 0);
            stmt.setString(8, flight.getTypeAvion());
            stmt.setInt(9, flight.getCapaciteTotale());
            stmt.setInt(10, flight.getPlacesDisponibles());
            stmt.setString(11, flight.getStatutVol() != null ? flight.getStatutVol().name() : "PROGRAMME");
            stmt.setString(12, flight.getPorteEmbarquement());
            stmt.setString(13, flight.getTerminal());
            stmt.setBoolean(14, flight.isActive());
            stmt.setInt(15, flight.getVolId());
            
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
        // Soft delete - set is_active to false
        String query = "UPDATE vols SET is_active = 0 WHERE vol_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Flight deleted successfully (ID: " + id + ")");
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
     */
    public List<Flight> getAvailableFlights() {
        List<Flight> flights = new ArrayList<>();
        String query = "SELECT v.*, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, ad.nom_aeroport as aeroport_depart, " +
                      "aa.ville as ville_arrivee, aa.nom_aeroport as aeroport_arrivee " +
                      "FROM vols v " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "WHERE v.is_active = 1 " +
                      "AND v.statut_vol = 'PROGRAMME' " +
                      "AND v.places_disponibles > 0 " +
                      "AND v.date_depart > NOW() " +
                      "ORDER BY v.date_depart ASC " +
                      "LIMIT 50";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                flights.add(extractFlightFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + flights.size() + " available flights");
        } catch (SQLException e) {
            System.err.println("❌ Error getting available flights: " + e.getMessage());
            e.printStackTrace();
        }
        return flights;
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
}
