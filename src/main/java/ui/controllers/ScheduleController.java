package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.User;
import ui.util.DataSource;
import ui.util.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ScheduleController {

    @FXML private Label upcomingFlightsLabel;
    @FXML private Label thisWeekLabel;
    @FXML private Label thisMonthLabel;
    @FXML private Label nextFlightLabel;
    @FXML private VBox scheduleContainer;
    @FXML private VBox pastFlightsContainer;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        loadScheduleData();
    }

    @FXML
    private void refreshSchedule() {
        loadScheduleData();
    }

    private void loadScheduleData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        int voyageurId = getVoyageurId(currentUser.getUserId());
        if (voyageurId == -1) return;

        loadStats(voyageurId);
        loadUpcomingFlights(voyageurId);
        loadPastFlights(voyageurId);
    }

    private void loadStats(int voyageurId) {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            // Upcoming flights count
            String upcomingQuery = "SELECT COUNT(*) FROM reservations_vol rv " +
                    "JOIN vols v ON rv.vol_id = v.vol_id " +
                    "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                    "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";

            try (PreparedStatement stmt = conn.prepareStatement(upcomingQuery)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    upcomingFlightsLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // This week
            String weekQuery = "SELECT COUNT(*) FROM reservations_vol rv " +
                    "JOIN vols v ON rv.vol_id = v.vol_id " +
                    "WHERE rv.voyageur_id = ? AND v.date_depart BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY) " +
                    "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";

            try (PreparedStatement stmt = conn.prepareStatement(weekQuery)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    thisWeekLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // This month
            String monthQuery = "SELECT COUNT(*) FROM reservations_vol rv " +
                    "JOIN vols v ON rv.vol_id = v.vol_id " +
                    "WHERE rv.voyageur_id = ? AND MONTH(v.date_depart) = MONTH(NOW()) AND YEAR(v.date_depart) = YEAR(NOW()) " +
                    "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";

            try (PreparedStatement stmt = conn.prepareStatement(monthQuery)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    thisMonthLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // Next flight countdown
            String nextQuery = "SELECT v.date_depart FROM reservations_vol rv " +
                    "JOIN vols v ON rv.vol_id = v.vol_id " +
                    "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                    "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE') " +
                    "ORDER BY v.date_depart ASC LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(nextQuery)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    LocalDateTime nextFlight = rs.getTimestamp("date_depart").toLocalDateTime();
                    long days = ChronoUnit.DAYS.between(LocalDateTime.now(), nextFlight);
                    if (days == 0) {
                        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), nextFlight);
                        nextFlightLabel.setText(hours + " hrs");
                    } else if (days == 1) {
                        nextFlightLabel.setText("Tomorrow");
                    } else {
                        nextFlightLabel.setText(days + " days");
                    }
                } else {
                    nextFlightLabel.setText("No flights");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading schedule stats: " + e.getMessage());
        }
    }

    private void loadUpcomingFlights(int voyageurId) {
        // Clear existing content except header
        while (scheduleContainer.getChildren().size() > 2) {
            scheduleContainer.getChildren().remove(2);
        }

        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT rv.reservation_id, rv.numero_confirmation, rv.statut_reservation, " +
                    "rv.prix_total, rv.nombre_passagers, rv.sieges_attribues, " +
                    "v.numero_vol, v.date_depart, v.date_arrivee, " +
                    "ad.ville as depart_city, aa.ville as arrival_city, " +
                    "c.nom_compagnie " +
                    "FROM reservations_vol rv " +
                    "JOIN vols v ON rv.vol_id = v.vol_id " +
                    "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                    "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                    "JOIN compagnies_aeriennes c ON v.compagnie_id = c.compagnie_id " +
                    "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                    "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE') " +
                    "ORDER BY v.date_depart ASC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();

                boolean hasFlights = false;
                while (rs.next()) {
                    hasFlights = true;
                    HBox flightCard = createFlightCard(rs, true);
                    scheduleContainer.getChildren().add(flightCard);
                }

                if (!hasFlights) {
                    Label noFlights = new Label("No upcoming flights. Book your next adventure!");
                    noFlights.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 20;");
                    scheduleContainer.getChildren().add(noFlights);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading upcoming flights: " + e.getMessage());
        }
    }

    private void loadPastFlights(int voyageurId) {
        // Clear existing content except header
        while (pastFlightsContainer.getChildren().size() > 2) {
            pastFlightsContainer.getChildren().remove(2);
        }

        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT rv.reservation_id, rv.numero_confirmation, rv.statut_reservation, " +
                    "rv.prix_total, rv.nombre_passagers, " +
                    "v.numero_vol, v.date_depart, v.date_arrivee, " +
                    "ad.ville as depart_city, aa.ville as arrival_city, " +
                    "c.nom_compagnie " +
                    "FROM reservations_vol rv " +
                    "JOIN vols v ON rv.vol_id = v.vol_id " +
                    "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                    "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                    "JOIN compagnies_aeriennes c ON v.compagnie_id = c.compagnie_id " +
                    "WHERE rv.voyageur_id = ? AND v.date_depart <= NOW() " +
                    "ORDER BY v.date_depart DESC LIMIT 5";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();

                boolean hasFlights = false;
                while (rs.next()) {
                    hasFlights = true;
                    HBox flightCard = createFlightCard(rs, false);
                    scheduleContainer.getChildren().add(flightCard);
                }

                if (!hasFlights) {
                    Label noFlights = new Label("No past flights yet.");
                    noFlights.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 20;");
                    pastFlightsContainer.getChildren().add(noFlights);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading past flights: " + e.getMessage());
        }
    }

    private HBox createFlightCard(ResultSet rs, boolean isUpcoming) throws SQLException {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + (isUpcoming ? "#f0f9ff" : "#f9fafb") + "; " +
                     "-fx-background-radius: 12; -fx-border-color: " + (isUpcoming ? "#bfdbfe" : "#e5e7eb") + "; " +
                     "-fx-border-radius: 12;");

        // Date section
        LocalDateTime departure = rs.getTimestamp("date_depart").toLocalDateTime();
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setMinWidth(80);

        Label dayLabel = new Label(String.valueOf(departure.getDayOfMonth()));
        dayLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        Label monthLabel = new Label(departure.getMonth().toString().substring(0, 3));
        monthLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        dateBox.getChildren().addAll(dayLabel, monthLabel);

        // Flight info
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label routeLabel = new Label(rs.getString("depart_city") + " → " + rs.getString("arrival_city"));
        routeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label flightLabel = new Label(rs.getString("numero_vol") + " • " + rs.getString("nom_compagnie"));
        flightLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        Label timeLabel = new Label("Departure: " + departure.format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        infoBox.getChildren().addAll(routeLabel, flightLabel, timeLabel);

        // Status and price
        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        String status = rs.getString("statut_reservation");
        Label statusLabel = new Label(status.equals("CONFIRMEE") ? "Confirmed" : "Pending");
        statusLabel.setStyle("-fx-background-color: " + (status.equals("CONFIRMEE") ? "#d1fae5" : "#fef3c7") + "; " +
                            "-fx-text-fill: " + (status.equals("CONFIRMEE") ? "#065f46" : "#92400e") + "; " +
                            "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 12px;");

        Label priceLabel = new Label(String.format("%.2f USD", rs.getDouble("prix_total")));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        rightBox.getChildren().addAll(statusLabel, priceLabel);

        card.getChildren().addAll(dateBox, infoBox, rightBox);
        return card;
    }

    private int getVoyageurId(int userId) {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT voyageur_id FROM voyageurs WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("voyageur_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting voyageur_id: " + e.getMessage());
        }
        return -1;
    }
}
