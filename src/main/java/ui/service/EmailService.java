package ui.service;

import ui.util.DataSource;

import javax.mail.*;
import javax.mail.internet.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * EmailService - Handles sending emails for various notifications
 * Supports booking confirmations, password resets, and general notifications
 */
public class EmailService {

    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String fromEmail;
    private boolean emailEnabled;

    /**
     * Constructor - loads email settings from database
     */
    public EmailService() {
        loadEmailSettings();
    }

    /**
     * Load email settings from system_settings table
     */
    private void loadEmailSettings() {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT setting_key, setting_value FROM system_settings WHERE setting_key LIKE 'smtp_%' OR setting_key LIKE 'email_%'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Default values
            smtpHost = "smtp.gmail.com";
            smtpPort = 587;
            smtpUsername = "";
            smtpPassword = "";
            fromEmail = "noreply@tripwise.com";
            emailEnabled = false;

            while (rs.next()) {
                String key = rs.getString("setting_key");
                String value = rs.getString("setting_value");

                switch (key) {
                    case "smtp_host":
                        smtpHost = value;
                        break;
                    case "smtp_port":
                        smtpPort = Integer.parseInt(value);
                        break;
                    case "smtp_username":
                        smtpUsername = value;
                        break;
                    case "smtp_password":
                        smtpPassword = value;
                        break;
                    case "email_from":
                        fromEmail = value;
                        break;
                    case "email_enabled":
                        emailEnabled = "true".equalsIgnoreCase(value);
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading email settings: " + e.getMessage());
        }
    }

    /**
     * Send email using configured SMTP settings
     */
    public boolean sendEmail(String to, String subject, String htmlBody) {
        if (!emailEnabled) {
            System.out.println("📧 Email disabled - Would have sent to: " + to);
            System.out.println("Subject: " + subject);
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "TripWise Travel"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send booking confirmation email
     */
    public boolean sendBookingConfirmation(String userEmail, String userName, 
                                          String bookingId, String hotelName, 
                                          String checkIn, String checkOut, 
                                          double totalPrice) {
        String subject = "Booking Confirmation - " + bookingId;
        String htmlBody = generateBookingConfirmationEmail(userName, bookingId, hotelName, 
                                                          checkIn, checkOut, totalPrice);
        return sendEmail(userEmail, subject, htmlBody);
    }

    /**
     * Send flight booking confirmation email
     */
    public boolean sendFlightBookingConfirmation(String userEmail, String userName,
                                                String bookingId, String flightNumber,
                                                String departure, String arrival,
                                                String departureTime, String arrivalTime,
                                                double totalPrice) {
        String subject = "Flight Booking Confirmation - " + bookingId;
        String htmlBody = generateFlightConfirmationEmail(userName, bookingId, flightNumber,
                                                         departure, arrival, departureTime, 
                                                         arrivalTime, totalPrice);
        return sendEmail(userEmail, subject, htmlBody);
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(String userEmail, String userName, String resetToken) {
        String subject = "Password Reset Request - TripWise";
        String resetLink = "http://tripwise.com/reset-password?token=" + resetToken;
        String htmlBody = generatePasswordResetEmail(userName, resetLink);
        return sendEmail(userEmail, subject, htmlBody);
    }

    /**
     * Send welcome email to new users
     */
    public boolean sendWelcomeEmail(String userEmail, String userName) {
        String subject = "Welcome to TripWise! 🎉";
        String htmlBody = generateWelcomeEmail(userName);
        return sendEmail(userEmail, subject, htmlBody);
    }

    /**
     * Send booking status update email
     */
    public boolean sendBookingStatusUpdate(String userEmail, String userName, 
                                          String bookingId, String status, String reason) {
        String subject = "Booking " + status + " - " + bookingId;
        String htmlBody = generateBookingStatusEmail(userName, bookingId, status, reason);
        return sendEmail(userEmail, subject, htmlBody);
    }

    /**
     * Test email configuration
     */
    public boolean sendTestEmail(String toEmail) {
        String subject = "TripWise Email Configuration Test";
        String htmlBody = generateTestEmail();
        return sendEmail(toEmail, subject, htmlBody);
    }

    // ==================== EMAIL TEMPLATES ====================

    /**
     * Generate booking confirmation email HTML
     */
    private String generateBookingConfirmationEmail(String userName, String bookingId, 
                                                   String hotelName, String checkIn, 
                                                   String checkOut, double totalPrice) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                "<div style='background-color: #3498db; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0;'>✅ Booking Confirmed!</h1>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<p>Dear <strong>" + userName + "</strong>,</p>" +
                "<p>Your booking has been confirmed! We're excited to host you.</p>" +
                "<div style='background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "<h2 style='color: #3498db; margin-top: 0;'>Booking Details</h2>" +
                "<table style='width: 100%; border-collapse: collapse;'>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>Booking ID:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + bookingId + "</td></tr>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>Hotel:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + hotelName + "</td></tr>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>Check-in:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + checkIn + "</td></tr>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>Check-out:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + checkOut + "</td></tr>" +
                "<tr><td style='padding: 8px;'><strong>Total Price:</strong></td><td style='padding: 8px; color: #27ae60; font-size: 18px; font-weight: bold;'>$" + String.format("%.2f", totalPrice) + "</td></tr>" +
                "</table>" +
                "</div>" +
                "<p>A confirmation has been saved to your account. You can view it anytime in your bookings section.</p>" +
                "<div style='text-align: center; margin-top: 30px;'>" +
                "<a href='http://tripwise.com/bookings' style='display: inline-block; padding: 12px 30px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;'>View My Bookings</a>" +
                "</div>" +
                "<p style='margin-top: 30px; color: #7f8c8d; font-size: 14px;'>If you have any questions, please contact our support team.</p>" +
                "<p style='color: #7f8c8d; font-size: 14px;'>Best regards,<br><strong>The TripWise Team</strong></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;'>" +
                "<p>© 2026 TripWise. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Generate flight confirmation email HTML
     */
    private String generateFlightConfirmationEmail(String userName, String bookingId,
                                                  String flightNumber, String departure,
                                                  String arrival, String departureTime,
                                                  String arrivalTime, double totalPrice) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                "<div style='background-color: #e67e22; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0;'>✈️ Flight Booked!</h1>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<p>Dear <strong>" + userName + "</strong>,</p>" +
                "<p>Your flight has been successfully booked!</p>" +
                "<div style='background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "<h2 style='color: #e67e22; margin-top: 0;'>Flight Details</h2>" +
                "<table style='width: 100%; border-collapse: collapse;'>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>Booking ID:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + bookingId + "</td></tr>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>Flight:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + flightNumber + "</td></tr>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>From:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + departure + " (" + departureTime + ")</td></tr>" +
                "<tr><td style='padding: 8px; border-bottom: 1px solid #ddd;'><strong>To:</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + arrival + " (" + arrivalTime + ")</td></tr>" +
                "<tr><td style='padding: 8px;'><strong>Total Price:</strong></td><td style='padding: 8px; color: #27ae60; font-size: 18px; font-weight: bold;'>$" + String.format("%.2f", totalPrice) + "</td></tr>" +
                "</table>" +
                "</div>" +
                "<p style='background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; border-radius: 5px;'>" +
                "<strong>⚠️ Important:</strong> Please arrive at the airport at least 2 hours before departure." +
                "</p>" +
                "<div style='text-align: center; margin-top: 30px;'>" +
                "<a href='http://tripwise.com/bookings' style='display: inline-block; padding: 12px 30px; background-color: #e67e22; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;'>View My Bookings</a>" +
                "</div>" +
                "<p style='margin-top: 30px; color: #7f8c8d; font-size: 14px;'>Have a safe flight!</p>" +
                "<p style='color: #7f8c8d; font-size: 14px;'>Best regards,<br><strong>The TripWise Team</strong></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;'>" +
                "<p>© 2026 TripWise. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Generate password reset email HTML
     */
    private String generatePasswordResetEmail(String userName, String resetLink) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                "<div style='background-color: #e74c3c; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0;'>🔐 Password Reset Request</h1>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<p>Dear <strong>" + userName + "</strong>,</p>" +
                "<p>We received a request to reset your password. Click the button below to create a new password:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetLink + "' style='display: inline-block; padding: 15px 40px; background-color: #e74c3c; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px;'>Reset Password</a>" +
                "</div>" +
                "<p style='background-color: #fee; padding: 15px; border-left: 4px solid #e74c3c; border-radius: 5px;'>" +
                "<strong>⚠️ Security Note:</strong> This link will expire in 24 hours. If you didn't request this reset, please ignore this email." +
                "</p>" +
                "<p style='color: #7f8c8d; font-size: 12px; margin-top: 30px;'>If the button doesn't work, copy and paste this link: <br>" + resetLink + "</p>" +
                "<p style='color: #7f8c8d; font-size: 14px; margin-top: 30px;'>Best regards,<br><strong>The TripWise Team</strong></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;'>" +
                "<p>© 2026 TripWise. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Generate welcome email HTML
     */
    private String generateWelcomeEmail(String userName) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                "<div style='background-color: #27ae60; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0;'>🎉 Welcome to TripWise!</h1>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<p>Dear <strong>" + userName + "</strong>,</p>" +
                "<p>Welcome to TripWise! We're thrilled to have you join our travel community.</p>" +
                "<h2 style='color: #27ae60;'>What can you do with TripWise?</h2>" +
                "<ul style='line-height: 2;'>" +
                "<li>✈️ <strong>Book Flights</strong> - Find the best deals on flights worldwide</li>" +
                "<li>🏨 <strong>Reserve Hotels</strong> - Discover amazing accommodations</li>" +
                "<li>🚗 <strong>Rent Vehicles</strong> - Get around with ease</li>" +
                "<li>🤖 <strong>AI Travel Assistant</strong> - Get personalized recommendations</li>" +
                "<li>📊 <strong>Track Your Bookings</strong> - Manage all your trips in one place</li>" +
                "</ul>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='http://tripwise.com/dashboard' style='display: inline-block; padding: 15px 40px; background-color: #27ae60; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px;'>Explore TripWise</a>" +
                "</div>" +
                "<p>If you have any questions, our support team is always here to help!</p>" +
                "<p style='color: #7f8c8d; font-size: 14px; margin-top: 30px;'>Happy travels!<br><strong>The TripWise Team</strong></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;'>" +
                "<p>© 2026 TripWise. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Generate booking status update email HTML
     */
    private String generateBookingStatusEmail(String userName, String bookingId, 
                                             String status, String reason) {
        String color = status.equalsIgnoreCase("APPROVED") ? "#27ae60" : "#e74c3c";
        String emoji = status.equalsIgnoreCase("APPROVED") ? "✅" : "❌";
        
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                "<div style='background-color: " + color + "; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0;'>" + emoji + " Booking " + status + "</h1>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<p>Dear <strong>" + userName + "</strong>,</p>" +
                "<p>Your booking <strong>#" + bookingId + "</strong> has been <strong>" + status.toLowerCase() + "</strong>.</p>" +
                (reason != null && !reason.isEmpty() ? 
                "<div style='background-color: #f8f9fa; padding: 15px; border-left: 4px solid " + color + "; border-radius: 5px; margin: 20px 0;'>" +
                "<p style='margin: 0;'><strong>Reason:</strong> " + reason + "</p>" +
                "</div>" : "") +
                "<div style='text-align: center; margin-top: 30px;'>" +
                "<a href='http://tripwise.com/bookings' style='display: inline-block; padding: 12px 30px; background-color: " + color + "; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;'>View Booking Details</a>" +
                "</div>" +
                "<p style='color: #7f8c8d; font-size: 14px; margin-top: 30px;'>Thank you for choosing TripWise!</p>" +
                "<p style='color: #7f8c8d; font-size: 14px;'>Best regards,<br><strong>The TripWise Team</strong></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;'>" +
                "<p>© 2026 TripWise. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Generate test email HTML
     */
    private String generateTestEmail() {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                "<div style='background-color: #3498db; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0;'>✅ Email Configuration Test</h1>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<p>Congratulations! Your email configuration is working correctly.</p>" +
                "<p>This is a test email from TripWise to verify that your SMTP settings are configured properly.</p>" +
                "<div style='background-color: #d4edda; padding: 15px; border-left: 4px solid #28a745; border-radius: 5px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #155724;'><strong>✓ SMTP Connection Successful</strong></p>" +
                "</div>" +
                "<p>You can now send emails for:</p>" +
                "<ul>" +
                "<li>Booking confirmations</li>" +
                "<li>Password reset requests</li>" +
                "<li>Welcome messages</li>" +
                "<li>Status updates</li>" +
                "</ul>" +
                "<p style='color: #7f8c8d; font-size: 14px; margin-top: 30px;'>Best regards,<br><strong>The TripWise Team</strong></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px;'>" +
                "<p>© 2026 TripWise. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Reload settings from database
     */
    public void reloadSettings() {
        loadEmailSettings();
    }

    // Getters
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }
}
