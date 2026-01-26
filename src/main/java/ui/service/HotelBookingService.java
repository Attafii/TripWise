package ui.service;

import ui.model.HotelBooking;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * HotelBookingService - Manages hotel reservations
 */
public class HotelBookingService implements IService<HotelBooking> {

    private final Connection connection;

    public HotelBookingService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(HotelBooking booking) {
        String query = "INSERT INTO reservations_hotel (voyageur_id, hotel_id, chambre_id, " +
                      "date_checkin, date_checkout, nombre_nuits, nombre_adultes, nombre_enfants, " +
                      "prix_total, statut_reservation, demandes_speciales, numero_confirmation, date_reservation) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getVoyageurId());
            stmt.setInt(2, booking.getHotelId());
            stmt.setInt(3, booking.getChambreId());
            stmt.setDate(4, Date.valueOf(booking.getDateCheckin()));
            stmt.setDate(5, Date.valueOf(booking.getDateCheckout()));
            stmt.setInt(6, booking.getNombreNuits());
            stmt.setInt(7, booking.getNombreAdultes());
            stmt.setInt(8, booking.getNombreEnfants());
            stmt.setDouble(9, booking.getPrixTotal());
            stmt.setString(10, booking.getStatutReservation().name());
            stmt.setString(11, booking.getDemandesSpeciales());
            stmt.setString(12, generateConfirmationNumber());
            stmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    booking.setReservationId(rs.getInt(1));
                }
                System.out.println("✅ Hotel booking created: " + booking.getBookingId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating hotel booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(HotelBooking booking) {
        String query = "UPDATE reservations_hotel SET statut_reservation=?, demandes_speciales=? WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, booking.getStatutReservation().name());
            stmt.setString(2, booking.getDemandesSpeciales());
            stmt.setInt(3, booking.getReservationId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Hotel booking updated: " + booking.getBookingId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating hotel booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM reservations_hotel WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Hotel booking deleted (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting hotel booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public HotelBooking getById(int id) {
        String query = "SELECT rh.*, h.nom_hotel, c.type_chambre, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_hotel rh " +
                      "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                      "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                      "JOIN voyageurs v ON rh.voyageur_id = v.voyageur_id " +
                      "JOIN users u ON v.user_id = u.user_id " +
                      "WHERE rh.reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractBookingFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting hotel booking: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<HotelBooking> getAll() {
        List<HotelBooking> bookings = new ArrayList<>();
        String query = "SELECT rh.*, h.nom_hotel, c.type_chambre, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_hotel rh " +
                      "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                      "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                      "JOIN voyageurs v ON rh.voyageur_id = v.voyageur_id " +
                      "JOIN users u ON v.user_id = u.user_id " +
                      "ORDER BY rh.date_reservation DESC " +
                      "LIMIT 100";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + bookings.size() + " hotel bookings");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Cancel a booking
     */
    public boolean cancelBooking(int reservationId) {
        String query = "UPDATE reservations_hotel SET statut_reservation='ANNULEE' WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Booking cancelled: BK" + String.format("%04d", reservationId));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Confirm a booking
     */
    public boolean confirmBooking(int reservationId) {
        String query = "UPDATE reservations_hotel SET statut_reservation='CONFIRMEE' WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Booking confirmed: BK" + String.format("%04d", reservationId));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error confirming booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Search bookings by status
     */
    public List<HotelBooking> getBookingsByStatus(HotelBooking.StatutReservation status) {
        List<HotelBooking> bookings = new ArrayList<>();
        String query = "SELECT rh.*, h.nom_hotel, c.type_chambre, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_hotel rh " +
                      "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                      "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                      "JOIN voyageurs v ON rh.voyageur_id = v.voyageur_id " +
                      "JOIN users u ON v.user_id = u.user_id " +
                      "WHERE rh.statut_reservation = ? " +
                      "ORDER BY rh.date_reservation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Found " + bookings.size() + " " + status + " bookings");
        } catch (SQLException e) {
            System.err.println("❌ Error searching bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Calculate total nights between check-in and check-out
     */
    public static int calculateNights(LocalDate checkin, LocalDate checkout) {
        return (int) ChronoUnit.DAYS.between(checkin, checkout);
    }

    /**
     * Generate unique confirmation number
     */
    private String generateConfirmationNumber() {
        Random random = new Random();
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();

        // 2 letters
        for (int i = 0; i < 2; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }

        // 4 digits
        sb.append(String.format("%04d", random.nextInt(10000)));

        return sb.toString();
    }

    /**
     * Extract booking from ResultSet
     */
    private HotelBooking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        HotelBooking booking = new HotelBooking();

        booking.setReservationId(rs.getInt("reservation_id"));
        booking.setVoyageurId(rs.getInt("voyageur_id"));
        booking.setHotelId(rs.getInt("hotel_id"));
        booking.setChambreId(rs.getInt("chambre_id"));

        Date checkin = rs.getDate("date_checkin");
        if (checkin != null) {
            booking.setDateCheckin(checkin.toLocalDate());
        }

        Date checkout = rs.getDate("date_checkout");
        if (checkout != null) {
            booking.setDateCheckout(checkout.toLocalDate());
        }

        booking.setNombreNuits(rs.getInt("nombre_nuits"));
        booking.setNombreAdultes(rs.getInt("nombre_adultes"));
        booking.setNombreEnfants(rs.getInt("nombre_enfants"));
        booking.setPrixTotal(rs.getDouble("prix_total"));

        String statut = rs.getString("statut_reservation");
        if (statut != null) {
            booking.setStatutReservation(HotelBooking.StatutReservation.valueOf(statut));
        }

        booking.setDemandesSpeciales(rs.getString("demandes_speciales"));
        booking.setNumeroConfirmation(rs.getString("numero_confirmation"));

        Timestamp dateRes = rs.getTimestamp("date_reservation");
        if (dateRes != null) {
            booking.setDateReservation(dateRes.toLocalDateTime());
        }

        // Joined data
        booking.setHotelName(rs.getString("nom_hotel"));
        booking.setChambreType(rs.getString("type_chambre"));
        booking.setGuestName(rs.getString("first_name") + " " + rs.getString("last_name"));
        booking.setGuestEmail(rs.getString("email"));

        return booking;
    }
}
