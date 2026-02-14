package ui.service;

import ui.model.FlightBooking;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FlightBookingService - Handles flight booking operations
 */
public class FlightBookingService {
    
    private final DataSource dataSource;
    
    public FlightBookingService() {
        this.dataSource = DataSource.getInstance();
    }
    
    /**
     * Get all flight bookings with joined data
     */
    public List<FlightBooking> getAllFlightBookings() {
        List<FlightBooking> bookings = new ArrayList<>();
        
        // For now, return sample data matching the screenshot
        // TODO: Replace with actual database queries when reservations_vol table is populated
        bookings.addAll(getSampleBookings());
        
        return bookings;
    }
    
    /**
     * Get flight bookings by status
     */
    public List<FlightBooking> getBookingsByStatus(FlightBooking.StatutReservation status) {
        List<FlightBooking> all = getAllFlightBookings();
        List<FlightBooking> filtered = new ArrayList<>();
        
        for (FlightBooking booking : all) {
            if (booking.getStatus() == status) {
                filtered.add(booking);
            }
        }
        
        return filtered;
    }
    
    /**
     * Update booking status
     */
    public boolean updateBookingStatus(int reservationId, FlightBooking.StatutReservation newStatus) {
        String sql = "UPDATE reservations_vol SET statut_reservation = ? WHERE reservation_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, reservationId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating booking status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a flight booking
     */
    public boolean deleteBooking(int reservationId) {
        String sql = "DELETE FROM reservations_vol WHERE reservation_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, reservationId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sample data matching the screenshot
     * This will be replaced with real database queries
     */
    private List<FlightBooking> getSampleBookings() {
        List<FlightBooking> bookings = new ArrayList<>();
        
        // Booking 1: John Anderson - AA 1234 (Confirmed)
        FlightBooking booking1 = new FlightBooking();
        booking1.setReservationId(1);
        booking1.setBookingId("BK001");
        booking1.setNumeroConfirmation("CNF-AA1234-001");
        booking1.setPassengerName("John Anderson");
        booking1.setPassengerCount(2);
        booking1.setFlightNumber("AA 1234");
        booking1.setAirlineName("American Airlines");
        booking1.setFlightClass("Business");
        booking1.setDepartureTime(LocalDateTime.of(2024, 12, 15, 10, 30));
        booking1.setDepartureCity("New York");
        booking1.setArrivalCity("Los Angeles");
        booking1.setTravelDate(LocalDate.of(2024, 12, 15));
        booking1.setTotalPrice(850.0);
        booking1.setStatus(FlightBooking.StatutReservation.CONFIRMEE);
        booking1.setBookingDate(LocalDateTime.of(2024, 12, 1, 14, 30));
        bookings.add(booking1);
        
        // Booking 2: Sarah Williams - DL 5678 (Pending)
        FlightBooking booking2 = new FlightBooking();
        booking2.setReservationId(2);
        booking2.setBookingId("BK002");
        booking2.setNumeroConfirmation("CNF-DL5678-002");
        booking2.setPassengerName("Sarah Williams");
        booking2.setPassengerCount(1);
        booking2.setFlightNumber("DL 5678");
        booking2.setAirlineName("Delta Airlines");
        booking2.setFlightClass("Economy");
        booking2.setDepartureTime(LocalDateTime.of(2024, 12, 18, 14, 15));
        booking2.setDepartureCity("Chicago");
        booking2.setArrivalCity("Miami");
        booking2.setTravelDate(LocalDate.of(2024, 12, 18));
        booking2.setTotalPrice(320.0);
        booking2.setStatus(FlightBooking.StatutReservation.EN_ATTENTE);
        booking2.setBookingDate(LocalDateTime.of(2024, 12, 3, 9, 15));
        bookings.add(booking2);
        
        // Booking 3: Michael Chen - UA 9012 (Confirmed)
        FlightBooking booking3 = new FlightBooking();
        booking3.setReservationId(3);
        booking3.setBookingId("BK003");
        booking3.setNumeroConfirmation("CNF-UA9012-003");
        booking3.setPassengerName("Michael Chen");
        booking3.setPassengerCount(1);
        booking3.setFlightNumber("UA 9012");
        booking3.setAirlineName("United Airlines");
        booking3.setFlightClass("First Class");
        booking3.setDepartureTime(LocalDateTime.of(2024, 12, 20, 8, 0));
        booking3.setDepartureCity("San Francisco");
        booking3.setArrivalCity("Seattle");
        booking3.setTravelDate(LocalDate.of(2024, 12, 20));
        booking3.setTotalPrice(1200.0);
        booking3.setStatus(FlightBooking.StatutReservation.CONFIRMEE);
        booking3.setBookingDate(LocalDateTime.of(2024, 12, 5, 11, 45));
        bookings.add(booking3);
        
        // Booking 4: Emily Davis - SW 3456 (Cancelled)
        FlightBooking booking4 = new FlightBooking();
        booking4.setReservationId(4);
        booking4.setBookingId("BK004");
        booking4.setNumeroConfirmation("CNF-SW3456-004");
        booking4.setPassengerName("Emily Davis");
        booking4.setPassengerCount(3);
        booking4.setFlightNumber("SW 3456");
        booking4.setAirlineName("Southwest Airlines");
        booking4.setFlightClass("Economy");
        booking4.setDepartureTime(LocalDateTime.of(2024, 12, 22, 16, 20));
        booking4.setDepartureCity("Dallas");
        booking4.setArrivalCity("Denver");
        booking4.setTravelDate(LocalDate.of(2024, 12, 22));
        booking4.setTotalPrice(280.0);
        booking4.setStatus(FlightBooking.StatutReservation.ANNULEE);
        booking4.setBookingDate(LocalDateTime.of(2024, 11, 28, 16, 0));
        bookings.add(booking4);
        
        // Booking 5: Robert Taylor - BA 7890 (Confirmed)
        FlightBooking booking5 = new FlightBooking();
        booking5.setReservationId(5);
        booking5.setBookingId("BK005");
        booking5.setNumeroConfirmation("CNF-BA7890-005");
        booking5.setPassengerName("Robert Taylor");
        booking5.setPassengerCount(2);
        booking5.setFlightNumber("BA 7890");
        booking5.setAirlineName("British Airways");
        booking5.setFlightClass("Business");
        booking5.setDepartureTime(LocalDateTime.of(2024, 12, 25, 19, 45));
        booking5.setDepartureCity("Boston");
        booking5.setArrivalCity("London");
        booking5.setTravelDate(LocalDate.of(2024, 12, 25));
        booking5.setTotalPrice(2450.0);
        booking5.setStatus(FlightBooking.StatutReservation.CONFIRMEE);
        booking5.setBookingDate(LocalDateTime.of(2024, 12, 2, 13, 20));
        bookings.add(booking5);
        
        return bookings;
    }
}
