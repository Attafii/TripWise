package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.User;
import ui.service.FlightBookingService;
import ui.service.HotelBookingService;
import ui.util.DataSource;
import ui.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TravelerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private VBox upcomingTripsContainer;
    @FXML private Label noTripsLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label bookingsTrendLabel;
    @FXML private Label upcomingTripsLabel;
    @FXML private Label nextTripLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label spentTrendLabel;
    @FXML private Label loyaltyPointsLabel;
    @FXML private Label loyaltyStatusLabel;
    @FXML private VBox recentActivityContainer;
    @FXML private Label lastUpdatedLabel;
    @FXML private HBox destinationsContainer;

    // Next flight countdown
    @FXML private Label nextFlightLabel;
    @FXML private Label countdownLabel;
    @FXML private Label flightRouteLabel;

    // Travel summary
    @FXML private Label flightCountLabel;
    @FXML private Label hotelCountLabel;
    @FXML private Label carCountLabel;

    private FlightBookingService flightBookingService;
    private HotelBookingService hotelBookingService;
    private int voyageurId = -1;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

    @FXML
    private void initialize() {
        flightBookingService = new FlightBookingService();
        hotelBookingService = new HotelBookingService();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getFirstName() + "! 👋");
            voyageurId = getTravelerIdForUser(currentUser.getUserId());
        }

        // Set current date
        if (dateLabel != null) {
            dateLabel.setText("Today is " + LocalDate.now().format(FULL_DATE_FORMATTER));
        }

        loadDashboardData();
        loadPopularDestinations();

        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
        }
    }

    private void loadDashboardData() {
        if (voyageurId == -1) {
            System.out.println("⚠️ No traveler ID found for current user");
            return;
        }

        loadUpcomingTrips();
        loadStatistics();
        loadRecentActivity();
        loadNextFlightCountdown();
        loadTravelSummary();
    }

    @FXML
    private void onRefresh() {
        loadDashboardData();
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
        }
    }

    private void loadUpcomingTrips() {
        upcomingTripsContainer.getChildren().clear();

        List<TripInfo> upcomingTrips = new ArrayList<>();

        // Load upcoming flight bookings
        try {
            Connection conn = DataSource.getInstance().getConnection();
            String flightQuery = "SELECT rv.*, v.numero_vol, ca.nom_compagnie, " +
                                "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                                "v.date_depart, v.date_arrivee, cv.type_classe " +
                                "FROM reservations_vol rv " +
                                "JOIN vols v ON rv.vol_id = v.vol_id " +
                                "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                                "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                                "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                                "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                                "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                                "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE') " +
                                "ORDER BY v.date_depart ASC LIMIT 5";

            PreparedStatement stmt = conn.prepareStatement(flightQuery);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TripInfo trip = new TripInfo();
                trip.type = "Flight";
                trip.icon = "✈️";
                trip.title = rs.getString("ville_depart") + " → " + rs.getString("ville_arrivee");
                trip.subtitle = rs.getString("nom_compagnie") + " " + rs.getString("numero_vol");
                trip.date = rs.getTimestamp("date_depart").toLocalDateTime();
                trip.status = rs.getString("statut_reservation");
                trip.price = rs.getDouble("prix_total");
                trip.bookingId = rs.getInt("reservation_id");
                upcomingTrips.add(trip);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading flight bookings: " + e.getMessage());
        }

        // Load upcoming hotel bookings
        try {
            Connection conn = DataSource.getInstance().getConnection();
            String hotelQuery = "SELECT rh.*, h.nom_hotel, h.ville, c.type_chambre " +
                               "FROM reservations_hotel rh " +
                               "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                               "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                               "WHERE rh.voyageur_id = ? AND rh.date_checkin > CURDATE() " +
                               "AND rh.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE') " +
                               "ORDER BY rh.date_checkin ASC LIMIT 5";

            PreparedStatement stmt = conn.prepareStatement(hotelQuery);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TripInfo trip = new TripInfo();
                trip.type = "Hotel";
                trip.icon = "🏨";
                trip.title = rs.getString("nom_hotel");
                trip.subtitle = rs.getString("ville") + " • " + rs.getString("type_chambre");
                trip.date = rs.getDate("date_checkin").toLocalDate().atStartOfDay();
                trip.endDate = rs.getDate("date_checkout").toLocalDate();
                trip.status = rs.getString("statut_reservation");
                trip.price = rs.getDouble("prix_total");
                trip.bookingId = rs.getInt("reservation_id");
                upcomingTrips.add(trip);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading hotel bookings: " + e.getMessage());
        }

        if (upcomingTrips.isEmpty()) {
            upcomingTripsContainer.getChildren().add(noTripsLabel);
        } else {
            for (TripInfo trip : upcomingTrips) {
                upcomingTripsContainer.getChildren().add(createTripCard(trip));
            }
        }
    }

    private HBox createTripCard(TripInfo trip) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");

        // Icon
        Label icon = new Label(trip.icon);
        icon.setStyle("-fx-font-size: 28px; -fx-background-color: white; -fx-padding: 12; -fx-background-radius: 10;");

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(trip.title);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitle = new Label(trip.subtitle);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        String dateStr = trip.date.format(DATE_FORMATTER);
        if (trip.endDate != null) {
            dateStr += " - " + trip.endDate.format(DATE_FORMATTER);
        }

        // Calculate days until trip
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), trip.date.toLocalDate());
        String daysText = daysUntil == 0 ? "Today!" : daysUntil == 1 ? "Tomorrow" : "In " + daysUntil + " days";

        Label dateLabel = new Label("📅 " + dateStr + " • " + daysText);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

        info.getChildren().addAll(title, subtitle, dateLabel);

        // Status and price
        VBox rightSection = new VBox(5);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        Label statusLabel = new Label(getStatusDisplay(trip.status));
        String statusColor = trip.status.equals("CONFIRMEE") ? "#10b981" : "#f59e0b";
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 3 8; -fx-background-radius: 10;");

        Label priceLabel = new Label(String.format("$%.0f", trip.price));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        rightSection.getChildren().addAll(statusLabel, priceLabel);

        card.getChildren().addAll(icon, info, rightSection);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;"));
        card.setOnMouseClicked(e -> viewBookingDetails(trip));

        return card;
    }

    private void loadNextFlightCountdown() {
        try {
            Connection conn = DataSource.getInstance().getConnection();
            String query = "SELECT v.numero_vol, v.date_depart, ad.ville as depart, aa.ville as arrival " +
                          "FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                          "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE') " +
                          "ORDER BY v.date_depart ASC LIMIT 1";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String flightNo = rs.getString("numero_vol");
                LocalDateTime departure = rs.getTimestamp("date_depart").toLocalDateTime();
                String route = rs.getString("depart") + " → " + rs.getString("arrival");

                long daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), departure);
                long hoursUntil = ChronoUnit.HOURS.between(LocalDateTime.now(), departure) % 24;

                if (nextFlightLabel != null) nextFlightLabel.setText(flightNo + " • " + departure.format(DATE_FORMATTER));
                if (countdownLabel != null) countdownLabel.setText(daysUntil + "d " + hoursUntil + "h");
                if (flightRouteLabel != null) flightRouteLabel.setText(route);
            } else {
                if (nextFlightLabel != null) nextFlightLabel.setText("No upcoming flights");
                if (countdownLabel != null) countdownLabel.setText("--");
                if (flightRouteLabel != null) flightRouteLabel.setText("Book your next flight!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading next flight: " + e.getMessage());
        }
    }

    private void loadTravelSummary() {
        int flightCount = 0, hotelCount = 0, carCount = 0;

        try {
            Connection conn = DataSource.getInstance().getConnection();

            // Count flights
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM reservations_vol WHERE voyageur_id = ?");
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) flightCount = rs.getInt(1);

            // Count hotels
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM reservations_hotel WHERE voyageur_id = ?");
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();
            if (rs.next()) hotelCount = rs.getInt(1);

            // Count cars (if table exists)
            try {
                stmt = conn.prepareStatement("SELECT COUNT(*) FROM reservations_vehicule WHERE voyageur_id = ?");
                stmt.setInt(1, voyageurId);
                rs = stmt.executeQuery();
                if (rs.next()) carCount = rs.getInt(1);
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.err.println("❌ Error loading travel summary: " + e.getMessage());
        }

        if (flightCountLabel != null) flightCountLabel.setText(String.valueOf(flightCount));
        if (hotelCountLabel != null) hotelCountLabel.setText(String.valueOf(hotelCount));
        if (carCountLabel != null) carCountLabel.setText(String.valueOf(carCount));
    }

    private void loadStatistics() {
        int totalBookings = 0;
        int upcomingCount = 0;
        int thisMonthBookings = 0;
        double totalSpent = 0;
        long daysToNextTrip = -1;

        try {
            Connection conn = DataSource.getInstance().getConnection();

            // Total flight bookings
            String flightCountQuery = "SELECT COUNT(*) as count, COALESCE(SUM(prix_total), 0) as total FROM reservations_vol WHERE voyageur_id = ?";
            PreparedStatement stmt = conn.prepareStatement(flightCountQuery);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalBookings += rs.getInt("count");
                totalSpent += rs.getDouble("total");
            }

            // This month flights
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM reservations_vol WHERE voyageur_id = ? AND MONTH(date_reservation) = MONTH(NOW()) AND YEAR(date_reservation) = YEAR(NOW())");
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();
            if (rs.next()) thisMonthBookings += rs.getInt(1);

            // Total hotel bookings
            String hotelCountQuery = "SELECT COUNT(*) as count, COALESCE(SUM(prix_total), 0) as total FROM reservations_hotel WHERE voyageur_id = ?";
            stmt = conn.prepareStatement(hotelCountQuery);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                totalBookings += rs.getInt("count");
                totalSpent += rs.getDouble("total");
            }

            // This month hotels
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM reservations_hotel WHERE voyageur_id = ? AND MONTH(date_reservation) = MONTH(NOW()) AND YEAR(date_reservation) = YEAR(NOW())");
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();
            if (rs.next()) thisMonthBookings += rs.getInt(1);

            // Upcoming trips count
            String upcomingFlightsQuery = "SELECT COUNT(*) as count FROM reservations_vol rv " +
                                         "JOIN vols v ON rv.vol_id = v.vol_id " +
                                         "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                                         "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";
            stmt = conn.prepareStatement(upcomingFlightsQuery);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();
            if (rs.next()) upcomingCount += rs.getInt("count");

            String upcomingHotelsQuery = "SELECT COUNT(*) as count FROM reservations_hotel " +
                                        "WHERE voyageur_id = ? AND date_checkin > CURDATE() " +
                                        "AND statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";
            stmt = conn.prepareStatement(upcomingHotelsQuery);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();
            if (rs.next()) upcomingCount += rs.getInt("count");

            // Days to next trip
            String nextTripQuery = "SELECT MIN(v.date_depart) as next_date FROM reservations_vol rv " +
                                  "JOIN vols v ON rv.vol_id = v.vol_id " +
                                  "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                                  "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";
            stmt = conn.prepareStatement(nextTripQuery);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getTimestamp("next_date") != null) {
                daysToNextTrip = ChronoUnit.DAYS.between(LocalDate.now(), rs.getTimestamp("next_date").toLocalDateTime().toLocalDate());
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading statistics: " + e.getMessage());
        }

        totalBookingsLabel.setText(String.valueOf(totalBookings));
        upcomingTripsLabel.setText(String.valueOf(upcomingCount));
        totalSpentLabel.setText(String.format("$%.0f", totalSpent));

        int loyaltyPoints = (int)(totalSpent / 10);
        loyaltyPointsLabel.setText(String.valueOf(loyaltyPoints));

        // Set trend labels
        if (bookingsTrendLabel != null) {
            bookingsTrendLabel.setText("↑ " + thisMonthBookings + " this month");
        }

        if (nextTripLabel != null) {
            if (daysToNextTrip >= 0) {
                nextTripLabel.setText("Next trip in " + daysToNextTrip + " days");
            } else {
                nextTripLabel.setText("No upcoming trips");
            }
        }

        if (loyaltyStatusLabel != null) {
            String status = loyaltyPoints >= 1000 ? "Gold Member ⭐" : loyaltyPoints >= 500 ? "Silver Member" : "Bronze Member";
            loyaltyStatusLabel.setText(status);
        }
    }

    private void loadRecentActivity() {
        recentActivityContainer.getChildren().clear();

        try {
            Connection conn = DataSource.getInstance().getConnection();

            String query = "SELECT 'flight' as type, rv.date_reservation as date, v.numero_vol as ref, " +
                          "CONCAT(ad.ville, ' → ', aa.ville) as details, rv.statut_reservation as status " +
                          "FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "WHERE rv.voyageur_id = ? " +
                          "UNION ALL " +
                          "SELECT 'hotel' as type, rh.date_reservation as date, h.nom_hotel as ref, " +
                          "h.ville as details, rh.statut_reservation as status " +
                          "FROM reservations_hotel rh " +
                          "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                          "WHERE rh.voyageur_id = ? " +
                          "ORDER BY date DESC LIMIT 5";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            stmt.setInt(2, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HBox activityItem = createActivityItem(
                    rs.getString("type"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getString("ref"),
                    rs.getString("details"),
                    rs.getString("status")
                );
                recentActivityContainer.getChildren().add(activityItem);
            }

            if (recentActivityContainer.getChildren().isEmpty()) {
                Label noActivity = new Label("No recent activity yet. Start booking!");
                noActivity.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-padding: 20;");
                recentActivityContainer.getChildren().add(noActivity);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading recent activity: " + e.getMessage());
        }
    }

    private HBox createActivityItem(String type, LocalDateTime date, String ref, String details, String status) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-border-width: 0 0 1 0;");

        String icon = type.equals("flight") ? "✈️" : "🏨";
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px; -fx-background-color: #f3f4f6; -fx-padding: 8; -fx-background-radius: 8;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label refLabel = new Label("Booked " + ref);
        refLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        info.getChildren().addAll(refLabel, detailsLabel);

        VBox rightInfo = new VBox(2);
        rightInfo.setAlignment(Pos.CENTER_RIGHT);

        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("MMM dd, h:mm a")));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        Label statusLabel = new Label(getStatusDisplay(status));
        String statusColor = status.equals("CONFIRMEE") ? "#10b981" :
                            status.equals("ANNULEE") ? "#ef4444" : "#f59e0b";
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + statusColor + "; -fx-font-weight: 600;");

        rightInfo.getChildren().addAll(dateLabel, statusLabel);

        item.getChildren().addAll(iconLabel, info, rightInfo);

        // Hover effect
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #f9fafb; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-border-width: 0 0 1 0;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-border-width: 0 0 1 0;"));

        return item;
    }

    private void loadPopularDestinations() {
        if (destinationsContainer == null) return;
        destinationsContainer.getChildren().clear();

        String[][] destinations = {
            {"Paris", "🗼", "#3b82f6"},
            {"New York", "🗽", "#10b981"},
            {"Dubai", "🏙️", "#f59e0b"},
            {"London", "🎡", "#8b5cf6"},
            {"Tokyo", "⛩️", "#ec4899"}
        };

        for (String[] dest : destinations) {
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(20, 25, 20, 25));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
            HBox.setHgrow(card, Priority.ALWAYS);

            Label iconLabel = new Label(dest[1]);
            iconLabel.setStyle("-fx-font-size: 32px;");

            Label nameLabel = new Label(dest[0]);
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

            Label dealsLabel = new Label("View deals →");
            dealsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + dest[2] + ";");

            card.getChildren().addAll(iconLabel, nameLabel, dealsLabel);

            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"));
            card.setOnMouseClicked(e -> searchDestination(dest[0]));

            destinationsContainer.getChildren().add(card);
        }
    }

    private void searchDestination(String city) {
        System.out.println("🔍 Searching for flights to: " + city);
        onBookFlight();
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case "CONFIRMEE": return "✓ Confirmed";
            case "EN_ATTENTE": return "⏳ Pending";
            case "ANNULEE": return "✗ Cancelled";
            case "TERMINEE": return "✓ Completed";
            default: return status;
        }
    }

    private void viewBookingDetails(TripInfo trip) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText(trip.icon + " " + trip.title);

        String content = String.format(
            "Type: %s\n" +
            "Details: %s\n" +
            "Date: %s\n" +
            "Status: %s\n" +
            "Price: $%.2f",
            trip.type, trip.subtitle, trip.date.format(DATE_FORMATTER),
            getStatusDisplay(trip.status), trip.price
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onBookFlight() {
        loadContent("/ui/book-flight-new.fxml");
    }

    @FXML
    private void onBookHotel() {
        loadContent("/ui/book-hotel-new.fxml");
    }

    @FXML
    private void onRentCar() {
        loadContent("/ui/rent-car.fxml");
    }

    @FXML
    private void onViewAllBookings() {
        loadContent("/ui/traveler-bookings.fxml");
    }

    @FXML
    private void onViewFlightAnalytics() {
        loadContent("/ui/flight-analytics.fxml");
    }

    @FXML
    private void onViewSchedule() {
        loadContent("/ui/schedule.fxml");
    }

    @FXML
    private void onViewPayments() {
        loadContent("/ui/payments.fxml");
    }

    @FXML
    private void onTrackFlights() {
        loadContent("/ui/flight-tracking.fxml");
    }

    @FXML
    private void onViewDeals() {
        loadContent("/ui/deals.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            if (welcomeLabel != null && welcomeLabel.getScene() != null) {
                javafx.scene.Parent root = welcomeLabel.getScene().getRoot();
                if (root instanceof javafx.scene.layout.BorderPane) {
                    javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    javafx.scene.Parent content = loader.load();
                    borderPane.setCenter(content);
                    System.out.println("✅ Loaded: " + fxmlPath);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getTravelerIdForUser(int userId) {
        try {
            Connection conn = DataSource.getInstance().getConnection();
            String query = "SELECT voyageur_id FROM voyageurs WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("voyageur_id");
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting traveler ID: " + e.getMessage());
        }
        return -1;
    }

    // Inner class for trip info
    private static class TripInfo {
        String type;
        String icon;
        String title;
        String subtitle;
        LocalDateTime date;
        LocalDate endDate;
        String status;
        double price;
        int bookingId;
    }
}
