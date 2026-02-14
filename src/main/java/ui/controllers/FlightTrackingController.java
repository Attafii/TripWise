package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import ui.model.User;
import ui.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FlightTrackingController - Role-specific flight tracking
 * Admin: Monitor all flights system-wide
 * Employee: Track assigned flights and gate management
 * Traveler: Track personal booked flights
 */
public class FlightTrackingController {

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private TextField searchField;
    @FXML private VBox flightsListBox;
    @FXML private Label stat1Label;
    @FXML private Label stat1Title;
    @FXML private Label stat2Label;
    @FXML private Label stat2Title;
    @FXML private Label stat3Label;
    @FXML private Label stat3Title;
    @FXML private Label mapLabel;

    private User.UserType userRole;
    private ObservableList<FlightInfo> flights;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ ERROR: No user in session!");
            return;
        }
        
        userRole = currentUser.getUserType();
        System.out.println("✅ Flight Tracking - User Role: " + userRole);
        
        flights = FXCollections.observableArrayList();
        loadRoleSpecificFlights();
    }

    private void loadRoleSpecificFlights() {
        if (userRole == null) {
            loadTravelerFlights();
            displayFlights();
            return;
        }
        
        switch (userRole) {
            case ADMIN:
            case RESPONSABLE:
                loadAdminFlights();
                break;
            case EMPLOYE:
                loadEmployeeFlights();
                break;
            case VOYAGEUR:
            case VISITEUR:
            default:
                loadTravelerFlights();
                break;
        }
        
        displayFlights();
    }

    private void loadAdminFlights() {
        pageTitle.setText("Flight Operations Monitor");
        pageSubtitle.setText("Real-time tracking of all flights in the system");
        
        stat1Title.setText("Active Flights");
        stat1Label.setText("47");
        stat2Title.setText("On-Time Performance");
        stat2Label.setText("92%");
        stat3Title.setText("Delayed Flights");
        stat3Label.setText("4");
        
        mapLabel.setText("🌍 Live Flight Map\n\n47 flights currently in operation\nTracking across 23 countries");
        
        flights.add(new FlightInfo("AA 1234", "American Airlines", "JFK", "LAX", 
            LocalDateTime.now().plusHours(2), "On Time", "B12", 85));
        flights.add(new FlightInfo("DL 5678", "Delta Airlines", "ORD", "MIA", 
            LocalDateTime.now().plusHours(1), "Boarding", "A7", 92));
        flights.add(new FlightInfo("UA 9012", "United Airlines", "SFO", "SEA", 
            LocalDateTime.now().minusMinutes(30), "In Air", "C4", 100));
        flights.add(new FlightInfo("SW 3456", "Southwest", "DFW", "DEN", 
            LocalDateTime.now().plusMinutes(45), "Delayed", "D8", 78));
        flights.add(new FlightInfo("BA 7890", "British Airways", "BOS", "LHR", 
            LocalDateTime.now().plusHours(3), "On Time", "E15", 95));
    }

    private void loadEmployeeFlights() {
        pageTitle.setText("Flight Management Dashboard");
        pageSubtitle.setText("Track and manage your assigned flights");
        
        stat1Title.setText("Assigned Flights");
        stat1Label.setText("8");
        stat2Title.setText("Boarding Now");
        stat2Label.setText("2");
        stat3Title.setText("Next Departure");
        stat3Label.setText("45min");
        
        mapLabel.setText("📋 Gate Management\n\nGate A7 - Boarding\nGate B12 - Preparing\nGate C4 - On Schedule");
        
        flights.add(new FlightInfo("DL 5678", "Delta Airlines", "ORD", "MIA", 
            LocalDateTime.now().plusMinutes(45), "Boarding", "A7", 92));
        flights.add(new FlightInfo("AA 1234", "American Airlines", "JFK", "LAX", 
            LocalDateTime.now().plusHours(2), "Preparing", "B12", 85));
        flights.add(new FlightInfo("UA 9012", "United Airlines", "SFO", "SEA", 
            LocalDateTime.now().plusHours(3), "On Time", "C4", 78));
    }

    private void loadTravelerFlights() {
        pageTitle.setText("My Flight Tracker");
        pageSubtitle.setText("Track your booked flights in real-time");
        
        stat1Title.setText("Upcoming Flights");
        stat1Label.setText("2");
        stat2Title.setText("Next Flight");
        stat2Label.setText("12 days");
        stat3Title.setText("Miles This Year");
        stat3Label.setText("15,420");
        
        mapLabel.setText("✈️ Your Next Journey\n\nNew York → Los Angeles\nDec 15, 10:30 AM\nGate B12");
        
        flights.add(new FlightInfo("AA 1234", "American Airlines", "JFK", "LAX", 
            LocalDateTime.of(2024, 12, 15, 10, 30), "Scheduled", "B12", 85));
        flights.add(new FlightInfo("AF 007", "Air France", "JFK", "CDG", 
            LocalDateTime.of(2024, 12, 25, 8, 0), "Scheduled", "E8", 90));
    }

    private void displayFlights() {
        flightsListBox.getChildren().clear();
        
        for (FlightInfo flight : flights) {
            flightsListBox.getChildren().add(createFlightCard(flight));
        }
    }

    private VBox createFlightCard(FlightInfo flight) {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 24; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2); " +
                     "-fx-border-color: " + getStatusColor(flight.getStatus()) + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12;");
        
        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox flightInfo = new VBox(4);
        Label flightNumber = new Label(flight.getFlightNumber());
        flightNumber.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        Label airline = new Label(flight.getAirline());
        airline.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        flightInfo.getChildren().addAll(flightNumber, airline);
        HBox.setHgrow(flightInfo, javafx.scene.layout.Priority.ALWAYS);
        
        Label statusBadge = new Label(flight.getStatus());
        statusBadge.setStyle("-fx-background-color: " + getStatusBgColor(flight.getStatus()) + "; " +
                           "-fx-text-fill: " + getStatusColor(flight.getStatus()) + "; " +
                           "-fx-padding: 6 16; -fx-background-radius: 16; -fx-font-weight: 600; -fx-font-size: 12px;");
        
        header.getChildren().addAll(flightInfo, statusBadge);
        
        // Route
        HBox route = new HBox(20);
        route.setAlignment(Pos.CENTER);
        
        VBox departure = new VBox(4);
        departure.setAlignment(Pos.CENTER);
        Label depCode = new Label(flight.getDeparture());
        depCode.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        Label depLabel = new Label("Departure");
        depLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        departure.getChildren().addAll(depCode, depLabel);
        
        Region arrow = new Region();
        arrow.setStyle("-fx-background-color: #3b82f6; -fx-pref-width: 80; -fx-pref-height: 2;");
        Label plane = new Label("✈");
        plane.setStyle("-fx-font-size: 20px;");
        VBox arrowBox = new VBox(4, arrow, plane);
        arrowBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(arrowBox, javafx.scene.layout.Priority.ALWAYS);
        
        VBox arrival = new VBox(4);
        arrival.setAlignment(Pos.CENTER);
        Label arrCode = new Label(flight.getArrival());
        arrCode.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        Label arrLabel = new Label("Arrival");
        arrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        arrival.getChildren().addAll(arrCode, arrLabel);
        
        route.getChildren().addAll(departure, arrowBox, arrival);
        
        // Details
        HBox details = new HBox(30);
        details.setAlignment(Pos.CENTER_LEFT);
        
        addDetail(details, "🕐 Time", flight.getDepartureTime().format(TIME_FORMATTER));
        addDetail(details, "📅 Date", flight.getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd")));
        if (flight.getGate() != null && !flight.getGate().isEmpty()) {
            addDetail(details, "🚪 Gate", flight.getGate());
        }
        addDetail(details, "👥 Capacity", flight.getCapacity() + "%");
        
        // Actions
        HBox actions = new HBox(10);
        Button detailsBtn = new Button("View Details");
        detailsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; " +
                           "-fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: 600;");
        detailsBtn.setOnAction(e -> showFlightDetails(flight));
        
        Button trackBtn = new Button("Track Live");
        trackBtn.setStyle("-fx-background-color: white; -fx-text-fill: #3b82f6; -fx-border-color: #3b82f6; " +
                         "-fx-border-width: 1.5; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand; " +
                         "-fx-font-size: 13px; -fx-font-weight: 600;");
        
        actions.getChildren().addAll(detailsBtn, trackBtn);
        
        card.getChildren().addAll(header, route, details, actions);
        return card;
    }

    private void addDetail(HBox container, String label, String value) {
        VBox detail = new VBox(2);
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
        detail.getChildren().addAll(labelText, valueText);
        container.getChildren().add(detail);
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "on time":
            case "scheduled":
                return "#10b981";
            case "boarding":
                return "#3b82f6";
            case "in air":
                return "#8b5cf6";
            case "delayed":
                return "#f59e0b";
            case "cancelled":
                return "#ef4444";
            default:
                return "#6b7280";
        }
    }

    private String getStatusBgColor(String status) {
        switch (status.toLowerCase()) {
            case "on time":
            case "scheduled":
                return "#d1fae5";
            case "boarding":
                return "#dbeafe";
            case "in air":
                return "#ede9fe";
            case "delayed":
                return "#fef3c7";
            case "cancelled":
                return "#fee2e2";
            default:
                return "#f3f4f6";
        }
    }

    private void showFlightDetails(FlightInfo flight) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Flight Details");
        alert.setHeaderText(flight.getFlightNumber() + " - " + flight.getAirline());
        
        String content = String.format(
            "Route: %s → %s\n" +
            "Departure: %s\n" +
            "Status: %s\n" +
            "Gate: %s\n" +
            "Capacity: %d%%",
            flight.getDeparture(), flight.getArrival(),
            flight.getDepartureTime().format(DATE_TIME_FORMATTER),
            flight.getStatus(),
            flight.getGate(),
            flight.getCapacity()
        );
        
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            displayFlights();
        } else {
            flightsListBox.getChildren().clear();
            for (FlightInfo flight : flights) {
                if (flight.getFlightNumber().toLowerCase().contains(searchText) ||
                    flight.getAirline().toLowerCase().contains(searchText) ||
                    flight.getDeparture().toLowerCase().contains(searchText) ||
                    flight.getArrival().toLowerCase().contains(searchText)) {
                    flightsListBox.getChildren().add(createFlightCard(flight));
                }
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadRoleSpecificFlights();
    }

    // Flight Info Model
    public static class FlightInfo {
        private String flightNumber;
        private String airline;
        private String departure;
        private String arrival;
        private LocalDateTime departureTime;
        private String status;
        private String gate;
        private int capacity;

        public FlightInfo(String flightNumber, String airline, String departure, String arrival,
                         LocalDateTime departureTime, String status, String gate, int capacity) {
            this.flightNumber = flightNumber;
            this.airline = airline;
            this.departure = departure;
            this.arrival = arrival;
            this.departureTime = departureTime;
            this.status = status;
            this.gate = gate;
            this.capacity = capacity;
        }

        public String getFlightNumber() { return flightNumber; }
        public String getAirline() { return airline; }
        public String getDeparture() { return departure; }
        public String getArrival() { return arrival; }
        public LocalDateTime getDepartureTime() { return departureTime; }
        public String getStatus() { return status; }
        public String getGate() { return gate; }
        public int getCapacity() { return capacity; }
    }
}
