package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.model.Flight;
import ui.service.FlightService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookFlightController {

    @FXML
    private TextField fromField;

    @FXML
    private TextField toField;

    @FXML
    private DatePicker departureDate;

    @FXML
    private DatePicker returnDate;

    @FXML
    private Spinner<Integer> passengersSpinner;

    @FXML
    private TableView<Flight> flightTable;

    @FXML
    private TableColumn<Flight, String> airlineColumn;

    @FXML
    private TableColumn<Flight, String> routeColumn;

    @FXML
    private TableColumn<Flight, String> departureColumn;

    @FXML
    private TableColumn<Flight, Integer> availableSeatsColumn;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    private FlightService flightService;

    @FXML
    private void initialize() {
        passengersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1));

        // Initialize flight service
        flightService = new FlightService();

        // Update column bindings to match new Flight model
        airlineColumn.setCellValueFactory(new PropertyValueFactory<>("compagnieName"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        departureColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getDateDepart();
            return new javafx.beans.property.SimpleStringProperty(
                dateTime != null ? dateTime.format(TIME_FORMATTER) : "N/A"
            );
        });

        if (availableSeatsColumn != null) {
            availableSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("placesDisponibles"));
        }

        // Load all available flights on startup
        loadAllFlights();
    }

    @FXML
    private void handleSearch() {
        String from = fromField.getText();
        String to = toField.getText();
        LocalDate date = departureDate.getValue();

        List<Flight> flights;

        // If search criteria provided, search, otherwise show all
        if ((from != null && !from.isBlank()) || (to != null && !to.isBlank()) || date != null) {
            System.out.println("🔍 Searching flights: " + from + " → " + to + " on " + date);
            flights = flightService.searchFlights(from, to, date);
        } else {
            System.out.println("📋 Loading all available flights");
            flights = flightService.getAvailableFlights();
        }

        if (flights.isEmpty()) {
            showAlert("No Flights Found", "No flights match your search criteria. Try different dates or cities.");
        }

        flightTable.setItems(FXCollections.observableArrayList(flights));
    }

    @FXML
    private void handleBackToBookings() {
        try {
            ui.util.SceneManager.switchScene("/ui/dashboard.fxml");
            System.out.println("📋 Navigating back to Bookings");
        } catch (Exception e) {
            System.err.println("❌ Error navigating to Bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all available flights on initialization
     */
    private void loadAllFlights() {
        System.out.println("📋 Loading all available flights...");
        List<Flight> flights = flightService.getAvailableFlights();
        flightTable.setItems(FXCollections.observableArrayList(flights));
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

