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

public class FlightTrackingController {

    @FXML private TextField flightNumberField;
    @FXML private VBox trackedFlightContainer;
    @FXML private VBox myFlightsContainer;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML
    private void initialize() {
        loadMyFlights();
    }

    @FXML
    private void refreshTracking() {
        loadMyFlights();
    }

    @FXML
    private void trackFlight() {
        String flightNumber = flightNumberField.getText().trim().toUpperCase();
        if (flightNumber.isEmpty()) {
            showAlert("Please enter a flight number");
            return;
        }

        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT v.vol_id, v.numero_vol, v.date_depart, v.date_arrivee, " +
                    "v.statut_vol, v.porte_embarquement, v.terminal, v.type_avion, " +
                    "ad.ville as depart_city, ad.code_iata as depart_code, " +
                    "aa.ville as arrival_city, aa.code_iata as arrival_code, " +
                    "c.nom_compagnie " +
                    "FROM vols v " +
                    "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                    "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                    "JOIN compagnies_aeriennes c ON v.compagnie_id = c.compagnie_id " +
                    "WHERE v.numero_vol = ? " +
                    "ORDER BY v.date_depart DESC LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, flightNumber);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    displayTrackedFlight(rs);
                } else {
                    showAlert("Flight " + flightNumber + " not found");
                    trackedFlightContainer.setVisible(false);
                    trackedFlightContainer.setManaged(false);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error tracking flight: " + e.getMessage());
            showAlert("Error tracking flight: " + e.getMessage());
        }
    }

    private void displayTrackedFlight(ResultSet rs) throws SQLException {
        trackedFlightContainer.getChildren().clear();
        trackedFlightContainer.setVisible(true);
        trackedFlightContainer.setManaged(true);

        String flightNumber = rs.getString("numero_vol");
        String airline = rs.getString("nom_compagnie");
        String departCity = rs.getString("depart_city");
        String departCode = rs.getString("depart_code");
        String arrivalCity = rs.getString("arrival_city");
        String arrivalCode = rs.getString("arrival_code");
        LocalDateTime departure = rs.getTimestamp("date_depart").toLocalDateTime();
        LocalDateTime arrival = rs.getTimestamp("date_arrivee").toLocalDateTime();
        String status = rs.getString("statut_vol");
        String gate = rs.getString("porte_embarquement");
        String terminal = rs.getString("terminal");
        String aircraft = rs.getString("type_avion");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label flightLabel = new Label(flightNumber);
        flightLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        Label airlineLabel = new Label(airline);
        airlineLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = createStatusBadge(status, departure);

        header.getChildren().addAll(flightLabel, airlineLabel, spacer, statusBadge);

        // Route visualization
        HBox routeBox = new HBox(20);
        routeBox.setAlignment(Pos.CENTER);
        routeBox.setPadding(new Insets(20, 0, 20, 0));

        // Departure
        VBox departBox = new VBox(5);
        departBox.setAlignment(Pos.CENTER);
        Label departCodeLabel = new Label(departCode);
        departCodeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label departCityLabel = new Label(departCity);
        departCityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        Label departTimeLabel = new Label(departure.format(TIME_FORMATTER));
        departTimeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label departDateLabel = new Label(departure.format(DATE_FORMATTER));
        departDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        departBox.getChildren().addAll(departCodeLabel, departCityLabel, departTimeLabel, departDateLabel);

        // Flight path
        VBox pathBox = new VBox(5);
        pathBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(pathBox, Priority.ALWAYS);

        long durationMinutes = ChronoUnit.MINUTES.between(departure, arrival);
        String durationStr = String.format("%dh %dm", durationMinutes / 60, durationMinutes % 60);

        Label durationLabel = new Label(durationStr);
        durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        HBox lineBox = new HBox();
        lineBox.setAlignment(Pos.CENTER);
        Label line = new Label("━━━━━━━━━━✈━━━━━━━━━━");
        line.setStyle("-fx-font-size: 16px; -fx-text-fill: #3b82f6;");
        lineBox.getChildren().add(line);

        Label aircraftLabel = new Label(aircraft != null ? aircraft : "Aircraft TBD");
        aircraftLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        pathBox.getChildren().addAll(durationLabel, lineBox, aircraftLabel);

        // Arrival
        VBox arrivalBox = new VBox(5);
        arrivalBox.setAlignment(Pos.CENTER);
        Label arrivalCodeLabel = new Label(arrivalCode);
        arrivalCodeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label arrivalCityLabel = new Label(arrivalCity);
        arrivalCityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        Label arrivalTimeLabel = new Label(arrival.format(TIME_FORMATTER));
        arrivalTimeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label arrivalDateLabel = new Label(arrival.format(DATE_FORMATTER));
        arrivalDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        arrivalBox.getChildren().addAll(arrivalCodeLabel, arrivalCityLabel, arrivalTimeLabel, arrivalDateLabel);

        routeBox.getChildren().addAll(departBox, pathBox, arrivalBox);

        // Additional info
        HBox infoBox = new HBox(40);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12;");

        VBox gateBox = new VBox(3);
        gateBox.setAlignment(Pos.CENTER);
        Label gateTitle = new Label("Gate");
        gateTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label gateValue = new Label(gate != null ? gate : "TBD");
        gateValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        gateBox.getChildren().addAll(gateTitle, gateValue);

        VBox terminalBox = new VBox(3);
        terminalBox.setAlignment(Pos.CENTER);
        Label terminalTitle = new Label("Terminal");
        terminalTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label terminalValue = new Label(terminal != null ? terminal : "TBD");
        terminalValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        terminalBox.getChildren().addAll(terminalTitle, terminalValue);

        VBox timeToBox = new VBox(3);
        timeToBox.setAlignment(Pos.CENTER);
        Label timeToTitle = new Label("Time to Departure");
        timeToTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        String timeTo = calculateTimeTo(departure);
        Label timeToValue = new Label(timeTo);
        timeToValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " +
            (departure.isAfter(LocalDateTime.now()) ? "#10b981" : "#6b7280") + ";");
        timeToBox.getChildren().addAll(timeToTitle, timeToValue);

        infoBox.getChildren().addAll(gateBox, terminalBox, timeToBox);

        trackedFlightContainer.getChildren().addAll(header, new Separator(), routeBox, infoBox);
    }

    private void loadMyFlights() {
        myFlightsContainer.getChildren().clear();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        int voyageurId = getVoyageurId(currentUser.getUserId());
        if (voyageurId == -1) {
            Label noUser = new Label("Please log in to see your flights");
            noUser.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
            myFlightsContainer.getChildren().add(noUser);
            return;
        }

        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT v.numero_vol, v.date_depart, v.date_arrivee, v.statut_vol, " +
                    "v.porte_embarquement, v.terminal, " +
                    "ad.ville as depart_city, ad.code_iata as depart_code, " +
                    "aa.ville as arrival_city, aa.code_iata as arrival_code, " +
                    "c.nom_compagnie, rv.numero_confirmation " +
                    "FROM reservations_vol rv " +
                    "JOIN vols v ON rv.vol_id = v.vol_id " +
                    "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                    "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                    "JOIN compagnies_aeriennes c ON v.compagnie_id = c.compagnie_id " +
                    "WHERE rv.voyageur_id = ? AND v.date_depart > NOW() " +
                    "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE') " +
                    "ORDER BY v.date_depart ASC LIMIT 5";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();

                boolean hasFlights = false;
                while (rs.next()) {
                    hasFlights = true;
                    VBox card = createFlightCard(rs);
                    myFlightsContainer.getChildren().add(card);
                }

                if (!hasFlights) {
                    VBox emptyBox = new VBox(10);
                    emptyBox.setAlignment(Pos.CENTER);
                    emptyBox.setPadding(new Insets(30));
                    emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

                    Label emptyLabel = new Label("No upcoming flights");
                    emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");

                    Button bookBtn = new Button("Book a Flight");
                    bookBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 25;");

                    emptyBox.getChildren().addAll(emptyLabel, bookBtn);
                    myFlightsContainer.getChildren().add(emptyBox);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading my flights: " + e.getMessage());
        }
    }

    private VBox createFlightCard(ResultSet rs) throws SQLException {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        String flightNumber = rs.getString("numero_vol");
        String airline = rs.getString("nom_compagnie");
        LocalDateTime departure = rs.getTimestamp("date_depart").toLocalDateTime();
        LocalDateTime arrival = rs.getTimestamp("date_arrivee").toLocalDateTime();
        String status = rs.getString("statut_vol");
        String confirmation = rs.getString("numero_confirmation");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label flightLabel = new Label(flightNumber + " • " + airline);
        flightLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = createStatusBadge(status, departure);

        header.getChildren().addAll(flightLabel, spacer, statusBadge);

        // Route
        HBox routeBox = new HBox(15);
        routeBox.setAlignment(Pos.CENTER_LEFT);

        Label routeLabel = new Label(rs.getString("depart_code") + " → " + rs.getString("arrival_code"));
        routeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        Label citiesLabel = new Label(rs.getString("depart_city") + " to " + rs.getString("arrival_city"));
        citiesLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        routeBox.getChildren().addAll(routeLabel, citiesLabel);

        // Time info
        HBox timeBox = new HBox(30);

        VBox departTime = new VBox(2);
        Label depLabel = new Label("Departure");
        depLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        Label depValue = new Label(departure.format(DATETIME_FORMATTER));
        depValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        departTime.getChildren().addAll(depLabel, depValue);

        VBox arrivalTime = new VBox(2);
        Label arrLabel = new Label("Arrival");
        arrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        Label arrValue = new Label(arrival.format(DATETIME_FORMATTER));
        arrValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        arrivalTime.getChildren().addAll(arrLabel, arrValue);

        VBox confBox = new VBox(2);
        Label confLabel = new Label("Confirmation");
        confLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        Label confValue = new Label(confirmation != null ? confirmation : "N/A");
        confValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        confBox.getChildren().addAll(confLabel, confValue);

        timeBox.getChildren().addAll(departTime, arrivalTime, confBox);

        card.getChildren().addAll(header, routeBox, timeBox);
        return card;
    }

    private Label createStatusBadge(String status, LocalDateTime departure) {
        String displayStatus;
        String bgColor;
        String textColor;

        if (status == null) status = "PROGRAMME";

        LocalDateTime now = LocalDateTime.now();

        if (status.equals("ANNULE")) {
            displayStatus = "Cancelled";
            bgColor = "#fee2e2";
            textColor = "#dc2626";
        } else if (status.equals("ATTERRI")) {
            displayStatus = "Landed";
            bgColor = "#ede9fe";
            textColor = "#7c3aed";
        } else if (status.equals("EN_COURS")) {
            displayStatus = "In Flight";
            bgColor = "#dbeafe";
            textColor = "#2563eb";
        } else if (status.equals("RETARDE")) {
            displayStatus = "Delayed";
            bgColor = "#fef3c7";
            textColor = "#d97706";
        } else if (departure.isBefore(now)) {
            displayStatus = "Departed";
            bgColor = "#e5e7eb";
            textColor = "#6b7280";
        } else {
            displayStatus = "On Time";
            bgColor = "#d1fae5";
            textColor = "#059669";
        }

        Label badge = new Label(displayStatus);
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                      "-fx-padding: 6 14; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");
        return badge;
    }

    private String calculateTimeTo(LocalDateTime departure) {
        LocalDateTime now = LocalDateTime.now();

        if (departure.isBefore(now)) {
            return "Departed";
        }

        long days = ChronoUnit.DAYS.between(now, departure);
        long hours = ChronoUnit.HOURS.between(now, departure) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, departure) % 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + " min";
        }
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Flight Tracking");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
