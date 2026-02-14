package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.model.FlightBooking;
import ui.service.FlightBookingService;
import ui.util.SceneManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AllBookingsController - Modern flight bookings management interface
 * Matches the blue/white design from the screenshot
 */
public class AllBookingsController {

    @FXML private TextField searchField;
    @FXML private Button allFilterBtn, confirmedFilterBtn, pendingFilterBtn, cancelledFilterBtn;
    @FXML private TableView<FlightBooking> bookingsTable;
    @FXML private TableColumn<FlightBooking, String> bookingIdColumn;
    @FXML private TableColumn<FlightBooking, String> passengerColumn;
    @FXML private TableColumn<FlightBooking, String> flightDetailsColumn;
    @FXML private TableColumn<FlightBooking, String> routeColumn;
    @FXML private TableColumn<FlightBooking, String> statusColumn;
    @FXML private TableColumn<FlightBooking, Double> priceColumn;
    @FXML private TableColumn<FlightBooking, Void> actionsColumn;
    @FXML private Label paginationLabel;

    private FlightBookingService bookingService;
    private ObservableList<FlightBooking> allBookings;
    private String currentFilter = "ALL";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    @FXML
    private void initialize() {
        bookingService = new FlightBookingService();
        allBookings = FXCollections.observableArrayList();

        setupTableColumns();
        loadAllBookings();
        updatePaginationLabel();
    }

    private void setupTableColumns() {
        // Booking ID column with blue styling
        bookingIdColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBookingId()));
        bookingIdColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: 700; -fx-font-size: 13px;");
                    
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    VBox vbox = new VBox(2);
                    vbox.getChildren().add(label);
                    
                    if (booking.getBookingDate() != null) {
                        Label dateLabel = new Label("📅 " + booking.getBookingDate().format(DATE_FORMATTER));
                        dateLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
                        vbox.getChildren().add(dateLabel);
                    }
                    
                    setGraphic(vbox);
                }
            }
        });

        // Passenger column
        passengerColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPassengerName()));
        passengerColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    Label nameLabel = new Label(booking.getPassengerName());
                    nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1a202c;");
                    
                    Label passengersLabel = new Label("👤 " + booking.getPassengerCount() + " passenger" + 
                                                     (booking.getPassengerCount() > 1 ? "s" : ""));
                    passengersLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
                    
                    VBox vbox = new VBox(3, nameLabel, passengersLabel);
                    setGraphic(vbox);
                }
            }
        });

        // Flight Details column
        flightDetailsColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFlightNumber()));
        flightDetailsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    
                    Label flightLabel = new Label(booking.getFlightNumber());
                    flightLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #1a202c;");
                    
                    Label airlineLabel = new Label(booking.getAirlineName());
                    airlineLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 12px;");
                    
                    HBox classTimeBox = new HBox(5);
                    if (booking.getFlightClass() != null) {
                        Label classLabel = new Label(booking.getFlightClass());
                        classLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px; -fx-font-weight: 600;");
                        classTimeBox.getChildren().add(classLabel);
                    }
                    
                    if (booking.getDepartureTime() != null) {
                        Label timeLabel = new Label("🕐 " + booking.getDepartureTime().format(TIME_FORMATTER));
                        timeLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 11px;");
                        classTimeBox.getChildren().add(timeLabel);
                    }
                    
                    VBox vbox = new VBox(3, flightLabel, airlineLabel, classTimeBox);
                    setGraphic(vbox);
                }
            }
        });

        // Route column
        routeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoute()));
        routeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    
                    Label routeLabel = new Label("📍 " + booking.getDepartureCity() + " → " + booking.getArrivalCity());
                    routeLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; -fx-text-fill: #2d3748;");
                    
                    if (booking.getTravelDate() != null) {
                        Label dateLabel = new Label(booking.getTravelDate().format(DATE_FORMATTER));
                        dateLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
                        
                        VBox vbox = new VBox(3, routeLabel, dateLabel);
                        setGraphic(vbox);
                    } else {
                        setGraphic(routeLabel);
                    }
                }
            }
        });

        // Status column with colored badges
        statusColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplay()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    Label statusLabel = new Label(booking.getStatusDisplay());
                    statusLabel.setPadding(new Insets(6, 14, 6, 14));
                    statusLabel.setStyle(getStatusStyle(booking.getStatus()));
                    statusLabel.setAlignment(Pos.CENTER);
                    
                    HBox hbox = new HBox(statusLabel);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);
                }
            }
        });

        // Price column
        priceColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("$" + String.format("%,.0f", price));
                    setStyle("-fx-font-weight: 700; -fx-text-fill: #1a202c; -fx-font-size: 14px;");
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Actions column with icon buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = createIconButton("👁");
            private final Button editBtn = createIconButton("✏");
            private final Button deleteBtn = createIconButton("🗑");

            {
                viewBtn.setOnAction(event -> {
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    viewBookingDetails(booking);
                });

                editBtn.setOnAction(event -> {
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    editBooking(booking);
                });

                deleteBtn.setOnAction(event -> {
                    FlightBooking booking = getTableView().getItems().get(getIndex());
                    deleteBooking(booking);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actions = new HBox(4, viewBtn, editBtn, deleteBtn);
                    actions.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(actions);
                }
            }
        });
    }

    private Button createIconButton(String icon) {
        Button btn = new Button(icon);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #edf2f7; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4; -fx-background-radius: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4;"));
        return btn;
    }

    private String getStatusStyle(FlightBooking.StatutReservation status) {
        switch (status) {
            case CONFIRMEE:
                return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-background-radius: 16; -fx-font-weight: 600; -fx-font-size: 12px;";
            case EN_ATTENTE:
                return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 16; -fx-font-weight: 600; -fx-font-size: 12px;";
            case ANNULEE:
                return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-background-radius: 16; -fx-font-weight: 600; -fx-font-size: 12px;";
            default:
                return "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-background-radius: 16; -fx-font-weight: 600; -fx-font-size: 12px;";
        }
    }

    private void loadAllBookings() {
        System.out.println("📋 Loading all flight bookings...");
        List<FlightBooking> bookings = bookingService.getAllFlightBookings();
        allBookings.setAll(bookings);
        bookingsTable.setItems(allBookings);
        updatePaginationLabel();
    }

    @FXML
    private void handleFilterAll() {
        filterBookings("ALL");
    }

    @FXML
    private void handleFilterConfirmed() {
        filterBookings("CONFIRMEE");
    }

    @FXML
    private void handleFilterPending() {
        filterBookings("EN_ATTENTE");
    }

    @FXML
    private void handleFilterCancelled() {
        filterBookings("ANNULEE");
    }

    private void filterBookings(String status) {
        currentFilter = status;
        updateFilterButtonStyles();

        if ("ALL".equals(status)) {
            bookingsTable.setItems(allBookings);
        } else {
            FlightBooking.StatutReservation statutFilter = FlightBooking.StatutReservation.valueOf(status);
            List<FlightBooking> filtered = bookingService.getBookingsByStatus(statutFilter);
            bookingsTable.setItems(FXCollections.observableArrayList(filtered));
        }
        updatePaginationLabel();
    }

    private void updateFilterButtonStyles() {
        String activeStyle = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: 600;";
        String inactiveStyle = "-fx-background-color: #edf2f7; -fx-text-fill: #4a5568;";

        allFilterBtn.setStyle(("ALL".equals(currentFilter) ? activeStyle : inactiveStyle) + " -fx-background-radius: 6; -fx-padding: 9 18; -fx-cursor: hand; -fx-font-size: 13px;");
        confirmedFilterBtn.setStyle(("CONFIRMEE".equals(currentFilter) ? activeStyle : inactiveStyle) + " -fx-background-radius: 6; -fx-padding: 9 18; -fx-cursor: hand; -fx-font-size: 13px;");
        pendingFilterBtn.setStyle(("EN_ATTENTE".equals(currentFilter) ? activeStyle : inactiveStyle) + " -fx-background-radius: 6; -fx-padding: 9 18; -fx-cursor: hand; -fx-font-size: 13px;");
        cancelledFilterBtn.setStyle(("ANNULEE".equals(currentFilter) ? activeStyle : inactiveStyle) + " -fx-background-radius: 6; -fx-padding: 9 18; -fx-cursor: hand; -fx-font-size: 13px;");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isBlank()) {
            bookingsTable.setItems(allBookings);
        } else {
            ObservableList<FlightBooking> filtered = allBookings.filtered(booking ->
                booking.getBookingId().toLowerCase().contains(searchText) ||
                booking.getPassengerName().toLowerCase().contains(searchText) ||
                booking.getFlightNumber().toLowerCase().contains(searchText) ||
                booking.getAirlineName().toLowerCase().contains(searchText)
            );
            bookingsTable.setItems(filtered);
        }
        updatePaginationLabel();
    }

    @FXML
    private void handleBookFlight() {
        try {
            SceneManager.switchScene("/ui/book-flight.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error opening flight booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleBookHotel() {
        try {
            SceneManager.switchScene("/ui/book-hotel-new.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error opening hotel booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleRentCar() {
        try {
            SceneManager.switchScene("/ui/rent-car.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error opening car rental: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportData() {
        showInfo("Export feature coming soon!");
    }

    @FXML
    private void handlePrevPage() {
        showInfo("Previous page");
    }

    @FXML
    private void handleNextPage() {
        showInfo("Next page");
    }

    @FXML
    private void handlePageClick() {
        showInfo("Page clicked");
    }

    private void updatePaginationLabel() {
        int total = bookingsTable.getItems().size();
        paginationLabel.setText("Showing 1-" + Math.min(5, total) + " of " + total + " bookings");
    }

    private void viewBookingDetails(FlightBooking booking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText(booking.getBookingId() + " - " + booking.getFlightNumber());

        String details = String.format(
            "Passenger: %s\n" +
            "Passengers: %d\n\n" +
            "Flight: %s\n" +
            "Airline: %s\n" +
            "Class: %s\n" +
            "Departure: %s\n\n" +
            "Route: %s → %s\n" +
            "Travel Date: %s\n\n" +
            "Total Price: $%.0f\n" +
            "Status: %s\n" +
            "Confirmation: %s",
            booking.getPassengerName(),
            booking.getPassengerCount(),
            booking.getFlightNumber(),
            booking.getAirlineName(),
            booking.getFlightClass(),
            booking.getDepartureTime() != null ? booking.getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A",
            booking.getDepartureCity(),
            booking.getArrivalCity(),
            booking.getTravelDate() != null ? booking.getTravelDate().format(DATE_FORMATTER) : "N/A",
            booking.getTotalPrice(),
            booking.getStatusDisplay(),
            booking.getNumeroConfirmation()
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void editBooking(FlightBooking booking) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Confirmed",
            "Confirmed", "Pending", "Cancelled", "Completed");
        dialog.setTitle("Update Booking Status");
        dialog.setHeaderText("Booking: " + booking.getBookingId());
        dialog.setContentText("Change status to:");

        dialog.showAndWait().ifPresent(status -> {
            FlightBooking.StatutReservation newStatus;
            switch (status) {
                case "Confirmed": newStatus = FlightBooking.StatutReservation.CONFIRMEE; break;
                case "Pending": newStatus = FlightBooking.StatutReservation.EN_ATTENTE; break;
                case "Cancelled": newStatus = FlightBooking.StatutReservation.ANNULEE; break;
                case "Completed": newStatus = FlightBooking.StatutReservation.TERMINEE; break;
                default: return;
            }

            booking.setStatus(newStatus);
            if (bookingService.updateBookingStatus(booking.getReservationId(), newStatus)) {
                showSuccess("Booking status updated successfully!");
                bookingsTable.refresh();
            } else {
                showError("Failed to update booking status.");
            }
        });
    }

    private void deleteBooking(FlightBooking booking) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Booking");
        confirm.setHeaderText("Delete " + booking.getBookingId() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (bookingService.deleteBooking(booking.getReservationId())) {
                    showSuccess("Booking deleted successfully!");
                    loadAllBookings();
                } else {
                    showError("Failed to delete booking.");
                }
            }
        });
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
