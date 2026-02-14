package ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.FlightBooking;
import ui.model.User;
import ui.service.FlightBookingService;
import ui.util.DataSource;
import ui.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class EmployeeFlightManagementController {

    @FXML private Label employeeNameLabel;
    @FXML private Label totalFlightsLabel;
    @FXML private Label pendingBookingsLabel;
    @FXML private Label confirmedBookingsLabel;
    @FXML private Label todayRevenueLabel;

    @FXML private ToggleButton allBtn;
    @FXML private ToggleButton pendingBtn;
    @FXML private ToggleButton confirmedBtn;
    @FXML private ToggleButton cancelledBtn;
    @FXML private TextField searchField;

    @FXML private TableView<FlightBookingRow> bookingsTable;
    @FXML private TableColumn<FlightBookingRow, String> bookingIdCol;
    @FXML private TableColumn<FlightBookingRow, String> passengerCol;
    @FXML private TableColumn<FlightBookingRow, String> flightCol;
    @FXML private TableColumn<FlightBookingRow, String> routeCol;
    @FXML private TableColumn<FlightBookingRow, String> dateCol;
    @FXML private TableColumn<FlightBookingRow, String> classCol;
    @FXML private TableColumn<FlightBookingRow, String> passengersCol;
    @FXML private TableColumn<FlightBookingRow, String> priceCol;
    @FXML private TableColumn<FlightBookingRow, String> statusCol;
    @FXML private TableColumn<FlightBookingRow, Void> actionsCol;

    private FlightBookingService bookingService;
    private ObservableList<FlightBookingRow> allBookings = FXCollections.observableArrayList();
    private String currentFilter = "ALL";
    private ToggleGroup filterGroup;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final String ACTIVE_BTN = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;";
    private static final String INACTIVE_BTN = "-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 20; -fx-padding: 8 20; -fx-border-color: #e5e7eb; -fx-border-radius: 20;";

    @FXML
    private void initialize() {
        bookingService = new FlightBookingService();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            employeeNameLabel.setText("Logged in as: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        }

        setupToggleGroup();
        setupTableColumns();
        loadAllBookings();
        loadStatistics();
        setupSearchFilter();
    }

    private void setupToggleGroup() {
        filterGroup = new ToggleGroup();
        allBtn.setToggleGroup(filterGroup);
        pendingBtn.setToggleGroup(filterGroup);
        confirmedBtn.setToggleGroup(filterGroup);
        cancelledBtn.setToggleGroup(filterGroup);
        allBtn.setSelected(true);
        allBtn.setStyle(ACTIVE_BTN);
        pendingBtn.setStyle(INACTIVE_BTN);
        confirmedBtn.setStyle(INACTIVE_BTN);
        cancelledBtn.setStyle(INACTIVE_BTN);
    }

    private void updateButtonStyles() {
        allBtn.setStyle(allBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
        pendingBtn.setStyle(pendingBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
        confirmedBtn.setStyle(confirmedBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
        cancelledBtn.setStyle(cancelledBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
    }

    private void setupTableColumns() {
        bookingIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().bookingId));
        passengerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().passengerName));
        flightCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().flightNumber));
        routeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().route));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().departureDate));
        classCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().flightClass));
        passengersCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().passengers)));
        priceCol.setCellValueFactory(data -> new SimpleStringProperty("$" + String.format("%.0f", data.getValue().price)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        // Status column with colored badges
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(getStatusDisplay(status));
                    label.setStyle(getStatusStyle(status));
                    setGraphic(label);
                }
            }
        });

        // Actions column
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("✓");
            private final Button rejectBtn = new Button("✕");
            private final Button viewBtn = new Button("👁");
            private final HBox pane = new HBox(5);

            {
                approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");

                approveBtn.setTooltip(new Tooltip("Approve Booking"));
                rejectBtn.setTooltip(new Tooltip("Cancel Booking"));
                viewBtn.setTooltip(new Tooltip("View Details"));

                approveBtn.setOnAction(e -> {
                    FlightBookingRow booking = getTableView().getItems().get(getIndex());
                    handleApprove(booking);
                });

                rejectBtn.setOnAction(e -> {
                    FlightBookingRow booking = getTableView().getItems().get(getIndex());
                    handleReject(booking);
                });

                viewBtn.setOnAction(e -> {
                    FlightBookingRow booking = getTableView().getItems().get(getIndex());
                    handleViewDetails(booking);
                });

                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(approveBtn, rejectBtn, viewBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FlightBookingRow booking = getTableView().getItems().get(getIndex());
                    boolean canModify = booking.status.equals("EN_ATTENTE");
                    approveBtn.setDisable(!canModify);
                    rejectBtn.setDisable(!canModify && !booking.status.equals("CONFIRMEE"));
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadAllBookings() {
        allBookings.clear();
        try {
            Connection conn = DataSource.getInstance().getConnection();
            String query = "SELECT rv.reservation_id, rv.nombre_passagers, rv.prix_total, rv.statut_reservation, " +
                          "rv.numero_confirmation, v.numero_vol, v.date_depart, cv.type_classe as classe, " +
                          "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                          "u.first_name, u.last_name " +
                          "FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                          "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "JOIN voyageurs voy ON rv.voyageur_id = voy.voyageur_id " +
                          "JOIN users u ON voy.user_id = u.user_id " +
                          "ORDER BY rv.date_reservation DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FlightBookingRow row = new FlightBookingRow();
                row.reservationId = rs.getInt("reservation_id");
                row.bookingId = "FL" + String.format("%04d", row.reservationId);
                row.passengerName = rs.getString("first_name") + " " + rs.getString("last_name");
                row.flightNumber = rs.getString("numero_vol");
                row.route = rs.getString("ville_depart") + " → " + rs.getString("ville_arrivee");
                row.departureDate = rs.getTimestamp("date_depart").toLocalDateTime().format(DATE_FORMATTER);
                row.flightClass = getClassDisplay(rs.getString("classe"));
                row.passengers = rs.getInt("nombre_passagers");
                row.price = rs.getDouble("prix_total");
                row.status = rs.getString("statut_reservation");
                row.confirmation = rs.getString("numero_confirmation");
                allBookings.add(row);
            }

            bookingsTable.setItems(allBookings);
            updateFilterCounts();

        } catch (Exception e) {
            System.err.println("❌ Error loading flight bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try {
            Connection conn = DataSource.getInstance().getConnection();

            // Total flights
            String totalQuery = "SELECT COUNT(*) as count FROM vols WHERE is_active = 1";
            PreparedStatement stmt = conn.prepareStatement(totalQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalFlightsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Pending bookings
            String pendingQuery = "SELECT COUNT(*) as count FROM reservations_vol WHERE statut_reservation = 'EN_ATTENTE'";
            stmt = conn.prepareStatement(pendingQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                pendingBookingsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Confirmed bookings
            String confirmedQuery = "SELECT COUNT(*) as count FROM reservations_vol WHERE statut_reservation = 'CONFIRMEE'";
            stmt = conn.prepareStatement(confirmedQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                confirmedBookingsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Today's revenue
            String revenueQuery = "SELECT COALESCE(SUM(prix_total), 0) as total FROM reservations_vol " +
                                 "WHERE DATE(date_reservation) = CURDATE() AND statut_reservation = 'CONFIRMEE'";
            stmt = conn.prepareStatement(revenueQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                todayRevenueLabel.setText("$" + String.format("%.0f", rs.getDouble("total")));
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading statistics: " + e.getMessage());
        }
    }

    private void updateFilterCounts() {
        long pending = allBookings.stream().filter(b -> b.status.equals("EN_ATTENTE")).count();
        long confirmed = allBookings.stream().filter(b -> b.status.equals("CONFIRMEE")).count();
        long cancelled = allBookings.stream().filter(b -> b.status.equals("ANNULEE")).count();

        pendingBtn.setText("Pending (" + pending + ")");
        confirmedBtn.setText("Confirmed (" + confirmed + ")");
        cancelledBtn.setText("Cancelled (" + cancelled + ")");
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                applyFilter();
            } else {
                String search = newVal.toLowerCase();
                ObservableList<FlightBookingRow> filtered = allBookings.filtered(b ->
                    b.bookingId.toLowerCase().contains(search) ||
                    b.passengerName.toLowerCase().contains(search) ||
                    b.flightNumber.toLowerCase().contains(search) ||
                    b.route.toLowerCase().contains(search)
                );
                bookingsTable.setItems(filtered);
            }
        });
    }

    @FXML
    private void filterAll() {
        currentFilter = "ALL";
        updateButtonStyles();
        applyFilter();
    }

    @FXML
    private void filterPending() {
        currentFilter = "EN_ATTENTE";
        updateButtonStyles();
        applyFilter();
    }

    @FXML
    private void filterConfirmed() {
        currentFilter = "CONFIRMEE";
        updateButtonStyles();
        applyFilter();
    }

    @FXML
    private void filterCancelled() {
        currentFilter = "ANNULEE";
        updateButtonStyles();
        applyFilter();
    }

    private void applyFilter() {
        if (currentFilter.equals("ALL")) {
            bookingsTable.setItems(allBookings);
        } else {
            ObservableList<FlightBookingRow> filtered = allBookings.filtered(b -> b.status.equals(currentFilter));
            bookingsTable.setItems(filtered);
        }
    }

    private void handleApprove(FlightBookingRow booking) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("Approve Flight Booking?");
        confirm.setContentText("Booking: " + booking.bookingId + "\nPassenger: " + booking.passengerName);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = bookingService.confirmBooking(booking.reservationId);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Booking confirmed successfully!");
                    loadAllBookings();
                    loadStatistics();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to confirm booking.");
                }
            }
        });
    }

    private void handleReject(FlightBookingRow booking) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Cancel Flight Booking?");
        confirm.setContentText("Booking: " + booking.bookingId + "\nPassenger: " + booking.passengerName);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = bookingService.cancelBooking(booking.reservationId);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Booking cancelled successfully!");
                    loadAllBookings();
                    loadStatistics();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel booking.");
                }
            }
        });
    }

    private void handleViewDetails(FlightBookingRow booking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText("Flight Booking: " + booking.bookingId);

        String content = String.format(
            "Confirmation: %s\n\n" +
            "Passenger: %s\n" +
            "Flight: %s\n" +
            "Route: %s\n" +
            "Departure: %s\n" +
            "Class: %s\n" +
            "Passengers: %d\n" +
            "Total Price: $%.2f\n" +
            "Status: %s",
            booking.confirmation,
            booking.passengerName,
            booking.flightNumber,
            booking.route,
            booking.departureDate,
            booking.flightClass,
            booking.passengers,
            booking.price,
            getStatusDisplay(booking.status)
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onAddFlight() {
        // Show dialog to add new flight
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Flight");
        dialog.setHeaderText("Create a new flight");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField flightNumber = new TextField();
        flightNumber.setPromptText("Flight Number (e.g., AA1234)");

        ComboBox<String> airline = new ComboBox<>();
        airline.getItems().addAll("Air France", "Emirates", "Tunisair", "Lufthansa", "Delta", "United");
        airline.setPromptText("Select Airline");

        TextField fromCity = new TextField();
        fromCity.setPromptText("Departure City");

        TextField toCity = new TextField();
        toCity.setPromptText("Arrival City");

        DatePicker departureDate = new DatePicker();
        departureDate.setPromptText("Departure Date");

        TextField capacity = new TextField();
        capacity.setPromptText("Total Capacity");

        grid.add(new Label("Flight Number:"), 0, 0);
        grid.add(flightNumber, 1, 0);
        grid.add(new Label("Airline:"), 0, 1);
        grid.add(airline, 1, 1);
        grid.add(new Label("From:"), 0, 2);
        grid.add(fromCity, 1, 2);
        grid.add(new Label("To:"), 0, 3);
        grid.add(toCity, 1, 3);
        grid.add(new Label("Departure:"), 0, 4);
        grid.add(departureDate, 1, 4);
        grid.add(new Label("Capacity:"), 0, 5);
        grid.add(capacity, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert(Alert.AlertType.INFORMATION, "Flight Added",
                         "Flight " + flightNumber.getText() + " has been added.\n(Feature implementation pending)");
            }
        });
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case "CONFIRMEE": return "Confirmed";
            case "EN_ATTENTE": return "Pending";
            case "ANNULEE": return "Cancelled";
            case "EMBARQUEE": return "Boarded";
            case "TERMINEE": return "Completed";
            default: return status;
        }
    }

    private String getStatusStyle(String status) {
        String baseStyle = "-fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 4 10; -fx-background-radius: 12;";
        switch (status) {
            case "CONFIRMEE": return baseStyle + " -fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
            case "EN_ATTENTE": return baseStyle + " -fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case "ANNULEE": return baseStyle + " -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            default: return baseStyle + " -fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
        }
    }

    private String getClassDisplay(String classType) {
        if (classType == null) return "Economy";
        switch (classType) {
            case "ECONOMIQUE": return "Economy";
            case "PREMIUM": return "Premium";
            case "BUSINESS": return "Business";
            case "PREMIERE": return "First Class";
            default: return classType;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for table rows
    public static class FlightBookingRow {
        int reservationId;
        String bookingId;
        String passengerName;
        String flightNumber;
        String route;
        String departureDate;
        String flightClass;
        int passengers;
        double price;
        String status;
        String confirmation;
    }
}
