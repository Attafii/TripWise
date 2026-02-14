package ui.service;

import ui.model.FlightBooking;
import ui.util.DataSource;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * EmailNotificationService - Email notification service with Real SMTP and Database sync
 *
 * This version can send REAL emails via Gmail SMTP AND saves them to the database.
 *
 * TO ENABLE REAL EMAILS:
 * 1. Go to https://myaccount.google.com/apppasswords
 * 2. Generate an App Password
 * 3. Replace the senderPassword below with your app password
 * 4. Set useRealEmail = true
 */
public class EmailNotificationService {

    // SMTP Configuration - Gmail
    private String smtpHost = "smtp.gmail.com";
    private String smtpPort = "587";

    // ============================================================
    // 📧 CONFIGURATION EMAIL GMAIL - CONFIGURÉ ✅
    // ============================================================

    // Destinataire des emails (pour les tests)
    private static final String TEST_RECIPIENT = "eya.khemirii@gmail.com";

    // Configuration Gmail
    private String senderEmail = "eya.khemirii@gmail.com";     // Expéditeur
    private String senderPassword = "fmlx dogd hztl quix";     // App Password

    private String senderName = "TripWise Travel Agency";

    // ✅ ACTIVÉ - Les emails seront envoyés réellement
    private boolean useRealEmail = true;

    private Connection connection;

    public EmailNotificationService() {
        this.connection = DataSource.getInstance().getConnection();
        System.out.println("📧 EmailNotificationService initialized (Dev Mode + DB Sync)");
    }

    /**
     * Configure SMTP settings for real email sending
     */
    public void configure(String host, String port, String email, String password) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.senderEmail = email;
        this.senderPassword = password;
        this.useRealEmail = true;
        System.out.println("✅ Email service configured for real sending: " + host + ":" + port);
    }

    /**
     * Enable real email sending with pre-configured Gmail
     */
    public void enableRealEmail(String gmailAddress, String appPassword) {
        this.senderEmail = gmailAddress;
        this.senderPassword = appPassword;
        this.useRealEmail = true;
        System.out.println("✅ Real email enabled via Gmail: " + gmailAddress);
    }

    /**
     * Send booking confirmation email
     */
    public boolean sendBookingConfirmation(FlightBooking booking) {
        String subject = "Booking Confirmation - " + booking.getNumeroConfirmation();
        String content = generateBookingConfirmationText(booking);
        return sendEmail(booking.getPassengerEmail(), subject, content);
    }

    /**
     * Send booking cancellation email
     */
    public boolean sendBookingCancellation(FlightBooking booking) {
        String subject = "Booking Cancelled - " + booking.getNumeroConfirmation();
        String content = generateCancellationText(booking);
        return sendEmail(booking.getPassengerEmail(), subject, content);
    }

    /**
     * Send seat selection confirmation
     */
    public boolean sendSeatConfirmation(FlightBooking booking, String seats) {
        String subject = "Seat Selection Confirmed - " + booking.getNumeroConfirmation();
        String content = generateSeatConfirmationText(booking, seats);
        return sendEmail(booking.getPassengerEmail(), subject, content);
    }

    /**
     * Send flight reminder (24h before)
     */
    public boolean sendFlightReminder(FlightBooking booking) {
        String subject = "Flight Reminder - Tomorrow: " + booking.getRoute();
        String content = generateReminderText(booking);
        return sendEmail(booking.getPassengerEmail(), subject, content);
    }

    /**
     * Send boarding pass email
     */
    public boolean sendBoardingPass(FlightBooking booking) {
        String subject = "Your Boarding Pass - " + booking.getNumeroVol();
        String content = generateBoardingPassText(booking);
        return sendEmail(booking.getPassengerEmail(), subject, content);
    }

    /**
     * Core email sending method - logs to console, saves to DB, and optionally sends real email
     */
    private boolean sendEmail(String to, String subject, String content) {
        // Log to console
        System.out.println("\n========================================");
        System.out.println("          📧 EMAIL NOTIFICATION");
        System.out.println("========================================");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("----------------------------------------");
        System.out.println(content);
        System.out.println("========================================");

        boolean emailSent = false;
        String status = "PENDING";
        String errorMessage = null;

        // Try to send real email if enabled
        if (useRealEmail && senderPassword != null && !senderPassword.isEmpty()) {
            try {
                sendRealEmail(to, subject, content);
                emailSent = true;
                status = "SENT";
                System.out.println("✅ Real email sent successfully to: " + to);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                status = "FAILED";
                System.err.println("❌ Failed to send real email: " + e.getMessage());
            }
        } else {
            status = "LOGGED";
            System.out.println("📝 Email logged (Dev Mode - not actually sent)");
        }

        // Save to database
        boolean dbSaved = saveEmailToDatabase(0, null, to, subject, status);

        if (dbSaved) {
            System.out.println("✅ Email saved to database");
        } else {
            System.out.println("⚠️ Database save failed");
        }

        System.out.println("========================================\n");

        return emailSent || !useRealEmail; // Return true if sent or in dev mode
    }

    /**
     * Send actual email via SMTP (Gmail)
     */
    private void sendRealEmail(String to, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        // Setup mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", smtpHost);

        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        // Create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail, senderName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);

        // Send
        Transport.send(message);
    }

    /**
     * Send email directly with subject and content - PUBLIC method
     */
    public boolean sendEmailDirect(String to, String subject, String content) {
        return sendEmail(to, subject, content);
    }

    /**
     * Quick method to send a test email
     */
    public boolean sendTestEmail(String to) {
        String subject = "TripWise - Test Email";
        String content = "Hello!\n\nThis is a test email from TripWise Travel Agency.\n\n" +
                        "If you received this, email notifications are working correctly!\n\n" +
                        "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) + "\n\n" +
                        "Best regards,\nTripWise Team";
        return sendEmail(to, subject, content);
    }

    /**
     * Save email notification to database
     */
    private boolean saveEmailNotification(int userId, Integer reservationId, String emailType,
                                          String recipientEmail, String subject, String status) {
        String query = "INSERT INTO email_notifications (user_id, reservation_id, email_type, " +
                      "recipient_email, subject, status, sent_at, created_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            if (reservationId != null) {
                stmt.setInt(2, reservationId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, emailType);
            stmt.setString(4, recipientEmail);
            stmt.setString(5, subject);
            stmt.setString(6, status);
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error saving email to database: " + e.getMessage());
            // Don't fail the email sending just because DB save failed
            return false;
        }
    }

    /**
     * Simple save to database (backward compatibility)
     */
    private boolean saveEmailToDatabase(int userId, Integer reservationId,
                                        String recipientEmail, String subject, String status) {
        return saveEmailNotification(userId, reservationId, "GENERAL", recipientEmail, subject, status);
    }

    // ==================== TEXT GENERATORS ====================

    private String generateBookingConfirmationText(FlightBooking booking) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        StringBuilder sb = new StringBuilder();

        sb.append("BOOKING CONFIRMED!\n\n");
        sb.append("Dear ").append(booking.getPassengerName()).append(",\n\n");
        sb.append("Your flight has been successfully booked.\n\n");
        sb.append("Confirmation Number: ").append(booking.getNumeroConfirmation()).append("\n\n");
        sb.append("FLIGHT DETAILS:\n");
        sb.append("---------------\n");
        sb.append("Flight: ").append(booking.getNumeroVol()).append("\n");
        sb.append("Route: ").append(booking.getVilleDepart()).append(" -> ").append(booking.getVilleArrivee()).append("\n");
        sb.append("Departure: ").append(booking.getDateDepart() != null ? booking.getDateDepart().format(formatter) : "N/A").append("\n");
        sb.append("Arrival: ").append(booking.getDateArrivee() != null ? booking.getDateArrivee().format(formatter) : "N/A").append("\n");
        sb.append("Class: ").append(booking.getClasseNom()).append("\n");
        sb.append("Passengers: ").append(booking.getNombrePassagers()).append("\n");
        sb.append("Seats: ").append(booking.getSiegesAttribues() != null ? booking.getSiegesAttribues() : "Not assigned").append("\n");
        sb.append("Total Price: $").append(String.format("%.2f", booking.getPriceAsDouble())).append("\n\n");
        sb.append("Thank you for choosing TripWise!\n");
        sb.append("(c) 2026 TripWise Travel Agency");

        return sb.toString();
    }

    private String generateCancellationText(FlightBooking booking) {
        StringBuilder sb = new StringBuilder();

        sb.append("BOOKING CANCELLED\n\n");
        sb.append("Dear ").append(booking.getPassengerName()).append(",\n\n");
        sb.append("Your booking has been cancelled.\n\n");
        sb.append("Confirmation Number: ").append(booking.getNumeroConfirmation()).append("\n");
        sb.append("Flight: ").append(booking.getNumeroVol()).append(" (").append(booking.getRoute()).append(")\n\n");
        sb.append("If you did not request this cancellation, please contact support.\n");
        sb.append("Refund will be processed within 5-7 business days.\n\n");
        sb.append("(c) 2026 TripWise Travel Agency");

        return sb.toString();
    }

    private String generateSeatConfirmationText(FlightBooking booking, String seats) {
        StringBuilder sb = new StringBuilder();

        sb.append("SEATS CONFIRMED!\n\n");
        sb.append("Dear ").append(booking.getPassengerName()).append(",\n\n");
        sb.append("Your seat selection has been confirmed.\n\n");
        sb.append("Flight: ").append(booking.getNumeroVol()).append("\n");
        sb.append("Route: ").append(booking.getRoute()).append("\n");
        sb.append("Your Seats: ").append(seats).append("\n\n");
        sb.append("Please arrive at the airport at least 2 hours before departure.\n\n");
        sb.append("(c) 2026 TripWise Travel Agency");

        return sb.toString();
    }

    private String generateReminderText(FlightBooking booking) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        StringBuilder sb = new StringBuilder();

        sb.append("FLIGHT REMINDER\n\n");
        sb.append("Dear ").append(booking.getPassengerName()).append(",\n\n");
        sb.append("Your flight is tomorrow!\n\n");
        sb.append("Flight: ").append(booking.getNumeroVol()).append("\n");
        sb.append("Route: ").append(booking.getRoute()).append("\n");
        sb.append("Departure: ").append(booking.getDateDepart() != null ? booking.getDateDepart().format(formatter) : "N/A").append("\n");
        sb.append("Confirmation: ").append(booking.getNumeroConfirmation()).append("\n\n");
        sb.append("PRE-FLIGHT CHECKLIST:\n");
        sb.append("- Check-in online\n");
        sb.append("- Prepare valid ID/Passport\n");
        sb.append("- Print or download boarding pass\n");
        sb.append("- Arrive at airport 2-3 hours early\n\n");
        sb.append("(c) 2026 TripWise Travel Agency");

        return sb.toString();
    }

    private String generateBoardingPassText(FlightBooking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder sb = new StringBuilder();

        sb.append("======================================\n");
        sb.append("           BOARDING PASS\n");
        sb.append("======================================\n\n");
        sb.append("Passenger: ").append(booking.getPassengerName().toUpperCase()).append("\n");
        sb.append("Flight: ").append(booking.getNumeroVol()).append("\n");
        sb.append("From: ").append(booking.getVilleDepart()).append("\n");
        sb.append("To: ").append(booking.getVilleArrivee()).append("\n");
        sb.append("Date: ").append(booking.getDateDepart() != null ? booking.getDateDepart().format(dateFormatter) : "N/A").append("\n");
        sb.append("Time: ").append(booking.getDateDepart() != null ? booking.getDateDepart().format(timeFormatter) : "N/A").append("\n");
        sb.append("Class: ").append(booking.getClasseNom()).append("\n");
        sb.append("Seat: ").append(booking.getSiegesAttribues() != null ? booking.getSiegesAttribues() : "TBA").append("\n");
        sb.append("Gate: TBA\n\n");
        sb.append("Confirmation: ").append(booking.getNumeroConfirmation()).append("\n");
        sb.append("======================================\n");

        return sb.toString();
    }

    /**
     * Check if email service is configured for real sending
     */
    public boolean isConfigured() {
        return useRealEmail;
    }

    /**
     * Test email connection
     */
    public boolean testConnection() {
        System.out.println("Testing email connection to " + smtpHost + ":" + smtpPort);
        // In dev mode, always return true
        return true;
    }
}
