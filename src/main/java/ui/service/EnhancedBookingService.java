package ui.service;

import ui.controllers.admin.AdminActivityLogsController;
import ui.model.HotelBooking;
import ui.model.Room;
import ui.model.Payment;
import ui.model.User;
import ui.util.DataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EnhancedBookingService - Integrates booking with email and PDF generation
 * Provides complete booking workflow with notifications
 */
public class EnhancedBookingService {

    private final HotelBookingService hotelBookingService;
    private final EmailService emailService;
    private final RoomService roomService;
    private final PaymentService paymentService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EnhancedBookingService() {
        this.hotelBookingService = new HotelBookingService();
        this.emailService = new EmailService();
        this.roomService = new RoomService();
        this.paymentService = new PaymentService();
    }

    /**
     * Create hotel booking with email and PDF receipt
     */
    public BookingResult createHotelBooking(HotelBooking booking, User user) {
        BookingResult result = new BookingResult();

        try {
            // 1. Create the booking
            boolean bookingCreated = hotelBookingService.add(booking);
            
            if (!bookingCreated) {
                result.setSuccess(false);
                result.setMessage("Failed to create booking");
                return result;
            }

            result.setSuccess(true);
            result.setBookingId(String.valueOf(booking.getReservationId()));

            // 2. Get hotel details
            HotelDetails hotelDetails = getHotelDetails(booking.getHotelId());
            if (hotelDetails == null) {
                result.setMessage("Booking created but hotel details not found");
                return result;
            }

            // 3. Generate PDF receipt
            String receiptsDir = System.getProperty("user.home") + File.separator + "TripWise_Receipts";
            String pdfPath = receiptsDir + File.separator + "booking_" + booking.getReservationId() + ".pdf";
            
            String roomType = "Standard Room";
            if (booking.getChambreId() > 0) {
                Room room = roomService.getById(booking.getChambreId());
                if (room != null && room.getTypeChambre() != null) {
                    roomType = room.getTypeChambre().name();
                }
            }
            
            File pdfFile = PDFReceiptService.generateBookingReceipt(
                pdfPath,
                String.valueOf(booking.getReservationId()),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                hotelDetails.name,
                hotelDetails.address,
                booking.getDateCheckin().format(formatter),
                booking.getDateCheckout().format(formatter),
                roomType,
                booking.getNombreNuits(),
                booking.getPrixTotal() / booking.getNombreNuits(),
                booking.getPrixTotal(),
                "Credit Card",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            if (pdfFile != null) {
                result.setPdfPath(pdfFile.getAbsolutePath());
            }

            // 4. Send confirmation email
            boolean emailSent = emailService.sendBookingConfirmation(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                String.valueOf(booking.getReservationId()),
                hotelDetails.name,
                booking.getDateCheckin().format(formatter),
                booking.getDateCheckout().format(formatter),
                booking.getPrixTotal()
            );

            result.setEmailSent(emailSent);

            // 5. Log activity
            AdminActivityLogsController.logActivity(
                user.getUserId(),
                "CREATE",
                "Booking",
                "Created hotel booking #" + booking.getReservationId() + " at " + hotelDetails.name,
                "127.0.0.1",
                "INFO"
            );

            result.setMessage("Booking created successfully! " + 
                            (emailSent ? "Confirmation email sent." : "") +
                            (pdfFile != null ? " Receipt saved." : ""));

            return result;

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error creating booking: " + e.getMessage());
            System.err.println("❌ Error in enhanced booking: " + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    /**
     * Create flight booking with email and PDF receipt
     */
    public BookingResult createFlightBooking(int userId, int flightId, User user,
                                            int passengers, double totalPrice) {
        BookingResult result = new BookingResult();

        try {
            // 1. Get flight details
            FlightDetails flightDetails = getFlightDetails(flightId);
            if (flightDetails == null) {
                result.setSuccess(false);
                result.setMessage("Flight not found");
                return result;
            }

            // 2. Create booking ID
            String bookingId = "FL" + System.currentTimeMillis();
            result.setBookingId(bookingId);
            result.setSuccess(true);

            // 3. Generate PDF receipt
            String receiptsDir = System.getProperty("user.home") + File.separator + "TripWise_Receipts";
            String pdfPath = receiptsDir + File.separator + "flight_" + bookingId + ".pdf";

            File pdfFile = PDFReceiptService.generateFlightReceipt(
                pdfPath,
                bookingId,
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                flightDetails.flightNumber,
                flightDetails.airline,
                flightDetails.departure,
                flightDetails.arrival,
                flightDetails.departureTime,
                flightDetails.arrivalTime,
                "Economy",
                passengers,
                totalPrice / passengers,
                totalPrice,
                "Credit Card",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            if (pdfFile != null) {
                result.setPdfPath(pdfFile.getAbsolutePath());
            }

            // 4. Send confirmation email
            boolean emailSent = emailService.sendFlightBookingConfirmation(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                bookingId,
                flightDetails.flightNumber,
                flightDetails.departure,
                flightDetails.arrival,
                flightDetails.departureTime,
                flightDetails.arrivalTime,
                totalPrice
            );

            result.setEmailSent(emailSent);

            // 5. Log activity
            AdminActivityLogsController.logActivity(
                user.getUserId(),
                "CREATE",
                "Flight Booking",
                "Booked flight " + flightDetails.flightNumber + " from " + flightDetails.departure + " to " + flightDetails.arrival,
                "127.0.0.1",
                "INFO"
            );

            result.setMessage("Flight booked successfully! " +
                            (emailSent ? "Confirmation email sent." : "") +
                            (pdfFile != null ? " Receipt saved." : ""));

            return result;

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error booking flight: " + e.getMessage());
            System.err.println("❌ Error in flight booking: " + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    /**
     * Send booking status update
     */
    public boolean sendBookingStatusUpdate(int bookingId, String status, String reason, User user) {
        try {
            boolean emailSent = emailService.sendBookingStatusUpdate(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                String.valueOf(bookingId),
                status,
                reason
            );

            // Log activity
            AdminActivityLogsController.logActivity(
                user.getUserId(),
                status.equalsIgnoreCase("APPROVED") ? "APPROVE" : "REJECT",
                "Booking",
                "Booking #" + bookingId + " " + status.toLowerCase() + (reason != null ? ": " + reason : ""),
                "127.0.0.1",
                status.equalsIgnoreCase("REJECTED") ? "WARNING" : "INFO"
            );

            return emailSent;

        } catch (Exception e) {
            System.err.println("❌ Error sending status update: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send welcome email to new user
     */
    public boolean sendWelcomeEmail(User user) {
        try {
            boolean emailSent = emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName()
            );

            // Log activity
            if (emailSent) {
                AdminActivityLogsController.logActivity(
                    user.getUserId(),
                    "CREATE",
                    "User",
                    "Welcome email sent to " + user.getEmail(),
                    "127.0.0.1",
                    "INFO"
                );
            }

            return emailSent;

        } catch (Exception e) {
            System.err.println("❌ Error sending welcome email: " + e.getMessage());
            return false;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Get hotel details from database
     */
    private HotelDetails getHotelDetails(int hotelId) {
        String query = "SELECT nom_hotel, adresse, ville, pays FROM hotels WHERE hotel_id = ?";
        
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                HotelDetails details = new HotelDetails();
                details.name = rs.getString("nom_hotel");
                details.address = rs.getString("adresse") + ", " + 
                                rs.getString("ville") + ", " + 
                                rs.getString("pays");
                return details;
            }
        } catch (SQLException e) {
            System.err.println("Error getting hotel details: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get flight details from database
     */
    private FlightDetails getFlightDetails(int flightId) {
        String query = "SELECT numero_vol, compagnie_aerienne, ville_depart, ville_arrivee, " +
                      "heure_depart, heure_arrivee FROM vols WHERE vol_id = ?";
        
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                FlightDetails details = new FlightDetails();
                details.flightNumber = rs.getString("numero_vol");
                details.airline = rs.getString("compagnie_aerienne");
                details.departure = rs.getString("ville_depart");
                details.arrival = rs.getString("ville_arrivee");
                details.departureTime = rs.getString("heure_depart");
                details.arrivalTime = rs.getString("heure_arrivee");
                return details;
            }
        } catch (SQLException e) {
            System.err.println("Error getting flight details: " + e.getMessage());
        }
        
        return null;
    }

    // ==================== Inner Classes ====================

    /**
     * Hotel details holder
     */
    private static class HotelDetails {
        String name;
        String address;
    }

    /**
     * Flight details holder
     */
    private static class FlightDetails {
        String flightNumber;
        String airline;
        String departure;
        String arrival;
        String departureTime;
        String arrivalTime;
    }

    /**
     * Booking result class
     */
    public static class BookingResult {
        private boolean success;
        private String message;
        private String bookingId;
        private String pdfPath;
        private boolean emailSent;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }
        
        public String getPdfPath() { return pdfPath; }
        public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
        
        public boolean isEmailSent() { return emailSent; }
        public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }
    }
}
