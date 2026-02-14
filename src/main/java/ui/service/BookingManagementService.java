package ui.service;

import ui.model.HotelBooking;
import ui.model.HotelBooking.StatutReservation;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BookingManagementService - Employee booking operations
 * Handles hotel bookings with approval/rejection workflow
 */
public class BookingManagementService {

    private final Connection connection;

    public BookingManagementService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    /**
     * Get all pending bookings (for employee approval)
     */
    public List<HotelBooking> getPendingBookings() {
        List<HotelBooking> bookings = new ArrayList<>();
        String query = "SELECT rh.*, h.nom_hotel, c.type_chambre, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_hotel rh " +
                      "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                      "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                      "JOIN users u ON rh.voyageur_id = u.user_id " +
                      "WHERE rh.statut_reservation = 'EN_ATTENTE' " +
                      "ORDER BY rh.date_reservation DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + bookings.size() + " pending bookings");
        } catch (SQLException e) {
            System.err.println("❌ Error getting pending bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Get bookings for a specific traveler
     */
    public List<HotelBooking> getBookingsByTraveler(int travelerId) {
        List<HotelBooking> bookings = new ArrayList<>();
        String query = "SELECT rh.*, h.nom_hotel, c.type_chambre, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_hotel rh " +
                      "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                      "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                      "JOIN users u ON rh.voyageur_id = u.user_id " +
                      "WHERE rh.voyageur_id = ? " +
                      "ORDER BY rh.date_reservation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, travelerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + bookings.size() + " bookings for traveler: " + travelerId);
        } catch (SQLException e) {
            System.err.println("❌ Error getting bookings for traveler: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Get all bookings with filters
     */
    public List<HotelBooking> getAllBookings(StatutReservation status, LocalDate fromDate, LocalDate toDate) {
        List<HotelBooking> bookings = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT rh.*, h.nom_hotel, c.type_chambre, " +
            "u.first_name, u.last_name, u.email " +
            "FROM reservations_hotel rh " +
            "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
            "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
            "JOIN users u ON rh.voyageur_id = u.user_id " +
            "WHERE 1=1 "
        );

        if (status != null) {
            query.append("AND rh.statut_reservation = ? ");
        }
        if (fromDate != null) {
            query.append("AND rh.date_checkin >= ? ");
        }
        if (toDate != null) {
            query.append("AND rh.date_checkout <= ? ");
        }
        
        query.append("ORDER BY rh.date_reservation DESC LIMIT 100");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;
            
            if (status != null) {
                stmt.setString(paramIndex++, status.name());
            }
            if (fromDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(fromDate));
            }
            if (toDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(toDate));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + bookings.size() + " bookings");
        } catch (SQLException e) {
            System.err.println("❌ Error getting bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Approve booking
     */
    public boolean approveBooking(int bookingId, int employeeId) {
        String query = "UPDATE reservations_hotel SET statut_reservation = 'CONFIRMEE', " +
                      "numero_confirmation = ?, updated_at = NOW() " +
                      "WHERE reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String confirmationNumber = generateConfirmationNumber();
            stmt.setString(1, confirmationNumber);
            stmt.setInt(2, bookingId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Booking approved: " + bookingId + " by employee: " + employeeId);
                // TODO: Log employee action
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error approving booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reject booking
     */
    public boolean rejectBooking(int bookingId, int employeeId, String reason) {
        String query = "UPDATE reservations_hotel SET statut_reservation = 'ANNULEE', " +
                      "demandes_speciales = CONCAT(IFNULL(demandes_speciales, ''), '\\nRejection reason: ', ?), " +
                      "updated_at = NOW() " +
                      "WHERE reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, reason);
            stmt.setInt(2, bookingId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Booking rejected: " + bookingId + " by employee: " + employeeId);
                // TODO: Log employee action
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error rejecting booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Modify booking dates
     */
    public boolean modifyBooking(int bookingId, LocalDate newCheckin, LocalDate newCheckout) {
        String query = "UPDATE reservations_hotel SET date_checkin = ?, date_checkout = ?, " +
                      "nombre_nuits = DATEDIFF(?, ?), updated_at = NOW() " +
                      "WHERE reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(newCheckin));
            stmt.setDate(2, Date.valueOf(newCheckout));
            stmt.setDate(3, Date.valueOf(newCheckout));
            stmt.setDate(4, Date.valueOf(newCheckin));
            stmt.setInt(5, bookingId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Booking modified: " + bookingId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error modifying booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Bulk approve bookings
     */
    public int bulkApproveBookings(List<Integer> bookingIds, int employeeId) {
        int successCount = 0;
        for (Integer bookingId : bookingIds) {
            if (approveBooking(bookingId, employeeId)) {
                successCount++;
            }
        }
        System.out.println("✅ Bulk approved " + successCount + " out of " + bookingIds.size() + " bookings");
        return successCount;
    }

    /**
     * Bulk reject bookings
     */
    public int bulkRejectBookings(List<Integer> bookingIds, int employeeId, String reason) {
        int successCount = 0;
        for (Integer bookingId : bookingIds) {
            if (rejectBooking(bookingId, employeeId, reason)) {
                successCount++;
            }
        }
        System.out.println("✅ Bulk rejected " + successCount + " out of " + bookingIds.size() + " bookings");
        return successCount;
    }

    /**
     * Search bookings by guest name or email
     */
    public List<HotelBooking> searchBookings(String searchTerm) {
        List<HotelBooking> bookings = new ArrayList<>();
        String query = "SELECT rh.*, h.nom_hotel, c.type_chambre, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_hotel rh " +
                      "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                      "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                      "JOIN users u ON rh.voyageur_id = u.user_id " +
                      "WHERE u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ? " +
                      "OR rh.numero_confirmation LIKE ? " +
                      "ORDER BY rh.date_reservation DESC LIMIT 50";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Found " + bookings.size() + " bookings matching: " + searchTerm);
        } catch (SQLException e) {
            System.err.println("❌ Error searching bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Get booking statistics for dashboard
     */
    public BookingStats getBookingStats(LocalDate fromDate, LocalDate toDate) {
        BookingStats stats = new BookingStats();
        
        String query = "SELECT " +
                      "COUNT(*) as total, " +
                      "SUM(CASE WHEN statut_reservation = 'EN_ATTENTE' THEN 1 ELSE 0 END) as pending, " +
                      "SUM(CASE WHEN statut_reservation = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmed, " +
                      "SUM(CASE WHEN statut_reservation = 'ANNULEE' THEN 1 ELSE 0 END) as cancelled, " +
                      "SUM(prix_total) as total_revenue " +
                      "FROM reservations_hotel " +
                      "WHERE date_reservation BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.totalBookings = rs.getInt("total");
                stats.pendingBookings = rs.getInt("pending");
                stats.confirmedBookings = rs.getInt("confirmed");
                stats.cancelledBookings = rs.getInt("cancelled");
                stats.totalRevenue = rs.getDouble("total_revenue");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting booking stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Extract HotelBooking from ResultSet
     */
    private HotelBooking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        HotelBooking booking = new HotelBooking();
        
        booking.setReservationId(rs.getInt("reservation_id"));
        booking.setVoyageurId(rs.getInt("voyageur_id"));
        booking.setHotelId(rs.getInt("hotel_id"));
        booking.setChambreId(rs.getInt("chambre_id"));
        
        Date checkin = rs.getDate("date_checkin");
        if (checkin != null) booking.setDateCheckin(checkin.toLocalDate());
        
        Date checkout = rs.getDate("date_checkout");
        if (checkout != null) booking.setDateCheckout(checkout.toLocalDate());
        
        booking.setNombreNuits(rs.getInt("nombre_nuits"));
        booking.setNombreAdultes(rs.getInt("nombre_adultes"));
        booking.setNombreEnfants(rs.getInt("nombre_enfants"));
        booking.setPrixTotal(rs.getDouble("prix_total"));
        
        String statut = rs.getString("statut_reservation");
        if (statut != null) {
            booking.setStatutReservation(StatutReservation.valueOf(statut));
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

    /**
     * Generate unique confirmation number
     */
    private String generateConfirmationNumber() {
        return "CNF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Inner class for booking statistics
     */
    public static class BookingStats {
        public int totalBookings;
        public int pendingBookings;
        public int confirmedBookings;
        public int cancelledBookings;
        public double totalRevenue;
    }
}
