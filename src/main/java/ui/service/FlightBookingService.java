package ui.service;

import ui.model.FlightBooking;
import ui.model.FlightClass;
import ui.util.DataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * FlightBookingService - Manages flight reservations
 */
public class FlightBookingService implements IService<FlightBooking> {

    private final Connection connection;

    public FlightBookingService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(FlightBooking booking) {
        // Generate confirmation number first
        String confirmationNumber = generateConfirmationNumber();

        // Use classe_id to match database schema
        String query = "INSERT INTO reservations_vol (voyageur_id, vol_id, classe_id, " +
                      "date_reservation, nombre_passagers, prix_total, statut_reservation, " +
                      "numero_confirmation, sieges_attribues) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getVoyageurId());
            stmt.setInt(2, booking.getVolId());
            stmt.setInt(3, booking.getClasseVolId());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, booking.getNombrePassagers());
            stmt.setBigDecimal(6, booking.getPrixTotal());
            stmt.setString(7, booking.getStatutReservation().name());
            stmt.setString(8, confirmationNumber);
            stmt.setString(9, booking.getSiegesAttribues());

            System.out.println("📋 Creating booking with:");
            System.out.println("   voyageur_id: " + booking.getVoyageurId());
            System.out.println("   vol_id: " + booking.getVolId());
            System.out.println("   classe_id: " + booking.getClasseVolId());
            System.out.println("   passengers: " + booking.getNombrePassagers());
            System.out.println("   price: " + booking.getPrixTotal());
            System.out.println("   confirmation: " + confirmationNumber);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    booking.setReservationId(rs.getInt(1));
                }

                // Set the confirmation number on the booking object
                booking.setNumeroConfirmation(confirmationNumber);

                // Update available seats
                updateFlightSeats(booking.getVolId(), booking.getClasseVolId(), -booking.getNombrePassagers());

                System.out.println("✅ Flight booking created: " + booking.getBookingId() + " (Confirmation: " + confirmationNumber + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating flight booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(FlightBooking booking) {
        String query = "UPDATE reservations_vol SET statut_reservation=?, demandes_speciales=?, " +
                      "sieges_attribues=?, bagage=?, repas=? WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, booking.getStatutReservation().name());
            stmt.setString(2, booking.getDemandesSpeciales());
            stmt.setString(3, booking.getSiegesAttribues());
            stmt.setString(4, booking.getBagage());
            stmt.setString(5, booking.getRepas());
            stmt.setInt(6, booking.getReservationId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Flight booking updated: " + booking.getBookingId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating flight booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        // Get booking details first to restore seats
        FlightBooking booking = getById(id);
        if (booking == null) return false;

        String query = "DELETE FROM reservations_vol WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Restore seats
                updateFlightSeats(booking.getVolId(), booking.getClasseVolId(), booking.getNombrePassagers());
                System.out.println("✅ Flight booking deleted (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting flight booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public FlightBooking getById(int id) {
        String query = "SELECT rv.*, v.numero_vol, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                      "v.date_depart, v.date_arrivee, cv.type_classe as classe, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_vol rv " +
                      "JOIN vols v ON rv.vol_id = v.vol_id " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                      "JOIN voyageurs voy ON rv.voyageur_id = voy.voyageur_id " +
                      "JOIN users u ON voy.user_id = u.user_id " +
                      "WHERE rv.reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractBookingFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting flight booking: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<FlightBooking> getAll() {
        List<FlightBooking> bookings = new ArrayList<>();
        String query = "SELECT rv.*, v.numero_vol, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                      "v.date_depart, v.date_arrivee, cv.type_classe as classe, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_vol rv " +
                      "JOIN vols v ON rv.vol_id = v.vol_id " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                      "JOIN voyageurs voy ON rv.voyageur_id = voy.voyageur_id " +
                      "JOIN users u ON voy.user_id = u.user_id " +
                      "ORDER BY rv.date_reservation DESC " +
                      "LIMIT 100";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + bookings.size() + " flight bookings");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all flight bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Get bookings for a specific traveler
     */
    public List<FlightBooking> getBookingsByTraveler(int voyageurId) {
        List<FlightBooking> bookings = new ArrayList<>();
        String query = "SELECT rv.*, v.numero_vol, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                      "v.date_depart, v.date_arrivee, cv.type_classe as classe, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_vol rv " +
                      "JOIN vols v ON rv.vol_id = v.vol_id " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                      "JOIN voyageurs voy ON rv.voyageur_id = voy.voyageur_id " +
                      "JOIN users u ON voy.user_id = u.user_id " +
                      "WHERE rv.voyageur_id = ? " +
                      "ORDER BY rv.date_reservation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Found " + bookings.size() + " bookings for traveler " + voyageurId);
        } catch (SQLException e) {
            System.err.println("❌ Error getting traveler bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Cancel a booking
     */
    public boolean cancelBooking(int reservationId) {
        String query = "UPDATE reservations_vol SET statut_reservation='ANNULEE' WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Flight booking cancelled: FL" + String.format("%04d", reservationId));

                // Try to restore seats (optional, don't fail if it doesn't work)
                try {
                    String getQuery = "SELECT vol_id, classe_id, nombre_passagers FROM reservations_vol WHERE reservation_id = ?";
                    PreparedStatement getStmt = connection.prepareStatement(getQuery);
                    getStmt.setInt(1, reservationId);
                    ResultSet rs = getStmt.executeQuery();
                    if (rs.next()) {
                        int volId = rs.getInt("vol_id");
                        int classeId = rs.getInt("classe_id");
                        int passengers = rs.getInt("nombre_passagers");
                        updateFlightSeats(volId, classeId, passengers);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Could not restore seats: " + e.getMessage());
                }

                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cancelling flight booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Confirm a booking - sends confirmation email to traveler
     */
    public boolean confirmBooking(int reservationId) {
        String query = "UPDATE reservations_vol SET statut_reservation='CONFIRMEE' WHERE reservation_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Flight booking confirmed: FL" + String.format("%04d", reservationId));

                // Send confirmation email to traveler
                sendConfirmationEmailToTraveler(reservationId);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error confirming flight booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Send confirmation email when employee confirms the booking
     */
    private void sendConfirmationEmailToTraveler(int reservationId) {
        try {
            // Get booking details
            FlightBooking booking = getById(reservationId);
            if (booking != null && booking.getPassengerEmail() != null) {
                EmailNotificationService emailService = new EmailNotificationService();

                String recipientEmail = "eya.khemirii@gmail.com"; // Fixed for testing
                String subject = "✅ TripWise - Booking CONFIRMED! " + booking.getNumeroConfirmation();

                StringBuilder content = new StringBuilder();
                content.append("========================================\n");
                content.append("   🎉 YOUR BOOKING IS NOW CONFIRMED!\n");
                content.append("========================================\n\n");
                content.append("Dear ").append(booking.getPassengerName()).append(",\n\n");
                content.append("Great news! Your flight booking has been CONFIRMED by our team.\n\n");
                content.append("🎫 Confirmation: ").append(booking.getNumeroConfirmation()).append("\n\n");
                content.append("FLIGHT DETAILS:\n");
                content.append("───────────────────────────────────────\n");
                content.append("✈️ Flight: ").append(booking.getNumeroVol()).append("\n");
                content.append("📍 Route: ").append(booking.getVilleDepart()).append(" → ").append(booking.getVilleArrivee()).append("\n");
                content.append("📅 Departure: ").append(booking.getDateDepart() != null ?
                    booking.getDateDepart().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "N/A").append("\n");
                content.append("💺 Class: ").append(booking.getClasseNom()).append("\n");
                content.append("🪑 Seats: ").append(booking.getSiegesAttribues() != null ? booking.getSiegesAttribues() : "Not assigned").append("\n");
                content.append("💰 Total: $").append(String.format("%.2f", booking.getPriceAsDouble())).append("\n");
                content.append("───────────────────────────────────────\n\n");
                content.append("✅ STATUS: CONFIRMED\n\n");
                content.append("You're all set! Have a great flight!\n\n");
                content.append("Best regards,\n");
                content.append("TripWise Travel Agency ✨\n");
                content.append("========================================\n");

                emailService.sendEmailDirect(recipientEmail, subject, content.toString());
                System.out.println("📧 Confirmation email sent to traveler: " + recipientEmail);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not send confirmation email: " + e.getMessage());
            // Don't fail the confirmation if email fails
        }
    }

    /**
     * Get bookings by status
     */
    public List<FlightBooking> getBookingsByStatus(FlightBooking.StatutReservation status) {
        List<FlightBooking> bookings = new ArrayList<>();
        String query = "SELECT rv.*, v.numero_vol, ca.nom_compagnie, " +
                      "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                      "v.date_depart, v.date_arrivee, cv.type_classe as classe, " +
                      "u.first_name, u.last_name, u.email " +
                      "FROM reservations_vol rv " +
                      "JOIN vols v ON rv.vol_id = v.vol_id " +
                      "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                      "JOIN voyageurs voy ON rv.voyageur_id = voy.voyageur_id " +
                      "JOIN users u ON voy.user_id = u.user_id " +
                      "WHERE rv.statut_reservation = ? " +
                      "ORDER BY rv.date_reservation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
            System.out.println("✅ Found " + bookings.size() + " " + status + " flight bookings");
        } catch (SQLException e) {
            System.err.println("❌ Error searching flight bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * Get flight classes for a specific flight
     */
    public List<FlightClass> getFlightClasses(int volId) {
        List<FlightClass> classes = new ArrayList<>();
        String query = "SELECT * FROM classes_vol WHERE vol_id = ? AND places_disponibles > 0";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, volId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FlightClass fc = new FlightClass();
                // Support both column names (classe_id or classe_vol_id)
                try {
                    fc.setClasseVolId(rs.getInt("classe_id"));
                } catch (SQLException e) {
                    fc.setClasseVolId(rs.getInt("classe_vol_id"));
                }
                fc.setVolId(rs.getInt("vol_id"));
                // Support both column names (type_classe or classe)
                String classeStr;
                try {
                    classeStr = rs.getString("type_classe");
                } catch (SQLException e) {
                    classeStr = rs.getString("classe");
                }
                // Map type_classe values to ClasseType enum
                fc.setClasse(mapToClasseType(classeStr));
                fc.setPrix(rs.getBigDecimal("prix"));
                fc.setPlacesDisponibles(rs.getInt("places_disponibles"));
                try {
                    fc.setAvantages(rs.getString("avantages"));
                } catch (SQLException e) {
                    fc.setAvantages(rs.getString("conditions_annulation"));
                }
                classes.add(fc);
            }
            System.out.println("✅ Found " + classes.size() + " classes for vol_id " + volId);
        } catch (SQLException e) {
            System.err.println("❌ Error getting flight classes: " + e.getMessage());
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * Map database class type to enum
     */
    private FlightClass.ClasseType mapToClasseType(String classeStr) {
        if (classeStr == null) return FlightClass.ClasseType.ECONOMIQUE;
        switch (classeStr.toUpperCase()) {
            case "ECONOMIQUE": return FlightClass.ClasseType.ECONOMIQUE;
            case "AFFAIRES":
            case "BUSINESS": return FlightClass.ClasseType.BUSINESS;
            case "PREMIERE":
            case "FIRST": return FlightClass.ClasseType.PREMIERE;
            case "PREMIUM": return FlightClass.ClasseType.PREMIUM;
            default: return FlightClass.ClasseType.ECONOMIQUE;
        }
    }

    /**
     * Update flight seats after booking
     */
    private void updateFlightSeats(int volId, int classeVolId, int change) {
        try {
            // Update classes_vol - support both column names
            String classQuery = "UPDATE classes_vol SET places_disponibles = places_disponibles + ? WHERE classe_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(classQuery)) {
                stmt.setInt(1, change);
                stmt.setInt(2, classeVolId);
                stmt.executeUpdate();
            }

            // Update vols total
            String volQuery = "UPDATE vols SET places_disponibles = places_disponibles + ? WHERE vol_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(volQuery)) {
                stmt.setInt(1, change);
                stmt.setInt(2, volId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error updating flight seats: " + e.getMessage());
        }
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
    private FlightBooking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        FlightBooking booking = new FlightBooking();

        booking.setReservationId(rs.getInt("reservation_id"));
        booking.setVoyageurId(rs.getInt("voyageur_id"));
        booking.setVolId(rs.getInt("vol_id"));

        // Try classe_id first, fallback to classe_vol_id for compatibility
        try {
            booking.setClasseVolId(rs.getInt("classe_id"));
        } catch (SQLException e) {
            try {
                booking.setClasseVolId(rs.getInt("classe_vol_id"));
            } catch (SQLException e2) {
                booking.setClasseVolId(1); // Default
            }
        }

        Timestamp dateRes = rs.getTimestamp("date_reservation");
        if (dateRes != null) {
            booking.setDateReservation(dateRes.toLocalDateTime());
        }

        booking.setNombrePassagers(rs.getInt("nombre_passagers"));
        booking.setPrixTotal(rs.getBigDecimal("prix_total"));

        String statut = rs.getString("statut_reservation");
        if (statut != null) {
            booking.setStatutReservation(FlightBooking.StatutReservation.valueOf(statut));
        }

        booking.setNumeroConfirmation(rs.getString("numero_confirmation"));
        booking.setSiegesAttribues(rs.getString("sieges_attribues"));
        booking.setBagage(rs.getString("bagage"));
        booking.setRepas(rs.getString("repas"));
        booking.setDemandesSpeciales(rs.getString("demandes_speciales"));

        // Joined data
        booking.setNumeroVol(rs.getString("numero_vol"));
        booking.setCompagnieName(rs.getString("nom_compagnie"));
        booking.setVilleDepart(rs.getString("ville_depart"));
        booking.setVilleArrivee(rs.getString("ville_arrivee"));

        Timestamp dateDepart = rs.getTimestamp("date_depart");
        if (dateDepart != null) {
            booking.setDateDepart(dateDepart.toLocalDateTime());
        }

        Timestamp dateArrivee = rs.getTimestamp("date_arrivee");
        if (dateArrivee != null) {
            booking.setDateArrivee(dateArrivee.toLocalDateTime());
        }

        booking.setClasseNom(rs.getString("classe"));
        booking.setPassengerName(rs.getString("first_name") + " " + rs.getString("last_name"));
        booking.setPassengerEmail(rs.getString("email"));

        return booking;
    }

    /**
     * Get booking statistics for a traveler
     */
    public TravelerFlightStats getTravelerStats(int voyageurId) {
        TravelerFlightStats stats = new TravelerFlightStats();

        try {
            // Total bookings and spending
            String query = "SELECT COUNT(*) as total_bookings, " +
                          "COALESCE(SUM(prix_total), 0) as total_spent, " +
                          "COALESCE(AVG(prix_total), 0) as avg_price " +
                          "FROM reservations_vol WHERE voyageur_id = ? AND statut_reservation != 'ANNULEE'";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.totalBookings = rs.getInt("total_bookings");
                stats.totalSpent = rs.getDouble("total_spent");
                stats.averagePrice = rs.getDouble("avg_price");
            }

            // Upcoming flights
            query = "SELECT COUNT(*) as upcoming FROM reservations_vol rv " +
                   "JOIN vols v ON rv.vol_id = v.vol_id " +
                   "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                   "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                stats.upcomingFlights = rs.getInt("upcoming");
            }

            // Most visited destination
            query = "SELECT aa.ville, COUNT(*) as count FROM reservations_vol rv " +
                   "JOIN vols v ON rv.vol_id = v.vol_id " +
                   "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                   "WHERE rv.voyageur_id = ? " +
                   "GROUP BY aa.ville ORDER BY count DESC LIMIT 1";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                stats.favoriteDestination = rs.getString("ville");
            }

            // Favorite airline
            query = "SELECT ca.nom_compagnie, COUNT(*) as count FROM reservations_vol rv " +
                   "JOIN vols v ON rv.vol_id = v.vol_id " +
                   "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                   "WHERE rv.voyageur_id = ? " +
                   "GROUP BY ca.nom_compagnie ORDER BY count DESC LIMIT 1";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                stats.favoriteAirline = rs.getString("nom_compagnie");
            }

            // Preferred class
            query = "SELECT cv.type_classe as classe, COUNT(*) as count FROM reservations_vol rv " +
                   "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                   "WHERE rv.voyageur_id = ? " +
                   "GROUP BY cv.type_classe ORDER BY count DESC LIMIT 1";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                stats.preferredClass = rs.getString("classe");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting traveler stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Get AI-powered flight recommendations based on user history
     */
    public List<String> getAIRecommendations(int voyageurId) {
        List<String> recommendations = new ArrayList<>();
        TravelerFlightStats stats = getTravelerStats(voyageurId);

        // Generate recommendations based on stats
        if (stats.totalBookings == 0) {
            recommendations.add("🆕 Welcome! Start your journey by booking your first flight.");
            recommendations.add("💡 Tip: Book early for better prices and seat selection.");
            recommendations.add("✈️ Popular destinations: Paris, New York, Dubai, London.");
        } else {
            if (stats.favoriteDestination != null) {
                recommendations.add("🎯 Based on your history, you love traveling to " + stats.favoriteDestination + "!");
            }

            if (stats.averagePrice > 500) {
                recommendations.add("💰 Consider booking 6-8 weeks ahead to save up to 20%.");
            }

            if (stats.favoriteAirline != null) {
                recommendations.add("✈️ You prefer " + stats.favoriteAirline + ". Check their loyalty program!");
            }

            if ("ECONOMIQUE".equals(stats.preferredClass)) {
                recommendations.add("⬆️ Try Premium Economy for extra legroom on long flights.");
            }

            if (stats.upcomingFlights > 0) {
                recommendations.add("📅 You have " + stats.upcomingFlights + " upcoming flight(s). Don't forget to check-in!");
            }

            // General tips
            recommendations.add("💡 Tuesday and Wednesday flights are often cheaper.");
        }

        return recommendations;
    }

    /**
     * Inner class for traveler flight statistics
     */
    public static class TravelerFlightStats {
        public int totalBookings;
        public double totalSpent;
        public double averagePrice;
        public int upcomingFlights;
        public String favoriteDestination;
        public String favoriteAirline;
        public String preferredClass;
    }
}
