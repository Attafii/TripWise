package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.model.HotelBooking;
import ui.service.HotelBookingService;
import ui.util.SceneManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AllBookingsController - Manages the main bookings view
 * Shows all bookings with filters (All, Confirmed, Pending, Cancelled)
 */
public class AllBookingsController {

    @FXML
    private TextField searchField;

    @FXML
    private Button allFilterBtn;

    @FXML
    private Button confirmedFilterBtn;

    @FXML
    private Button pendingFilterBtn;

    @FXML
    private Button cancelledFilterBtn;

    @FXML
    private TableView<HotelBooking> bookingsTable;

    @FXML
    private TableColumn<HotelBooking, String> bookingIdColumn;

    @FXML
    private TableColumn<HotelBooking, String> passengerColumn;

    @FXML
    private TableColumn<HotelBooking, String> hotelDetailsColumn;

    @FXML
    private TableColumn<HotelBooking, String> routeColumn;

    @FXML
    private TableColumn<HotelBooking, String> statusColumn;

    @FXML
    private TableColumn<HotelBooking, Double> priceColumn;

    @FXML
    private TableColumn<HotelBooking, Void> actionsColumn;

    private HotelBookingService bookingService;
    private ObservableList<HotelBooking> allBookings;
    private String currentFilter = "ALL";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    private void initialize() {
        bookingService = new HotelBookingService();
        allBookings = FXCollections.observableArrayList();

        setupTableColumns();
        loadAllBookings();
        setupFilterButtons();
    }

    private void setupTableColumns() {
        // Booking ID column
        bookingIdColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBookingId()));

        // Passenger column with passenger count
        passengerColumn.setCellValueFactory(cellData -> {
            HotelBooking booking = cellData.getValue();
            String text = booking.getGuestName() + "\n" +
                         "👤 " + (booking.getNombreAdultes() + booking.getNombreEnfants()) + " guests";
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        // Hotel Details column
        hotelDetailsColumn.setCellValueFactory(cellData -> {
            HotelBooking booking = cellData.getValue();
            String text = booking.getNumeroConfirmation() + "\n" +
                         booking.getChambreType() + " • " +
                         booking.getDateCheckin().format(DATE_FORMATTER);
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        // Route column (Check-in -> Check-out with dates)
        routeColumn.setCellValueFactory(cellData -> {
            HotelBooking booking = cellData.getValue();
            String text = "📅 " + booking.getHotelName() + "\n" +
                         booking.getDateCheckin().format(DATE_FORMATTER) + " → " +
                         booking.getDateCheckout().format(DATE_FORMATTER);
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        // Status column with styled labels
        statusColumn.setCellFactory(column -> new TableCell<HotelBooking, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HotelBooking booking = getTableRow().getItem();
                    Label statusLabel = new Label(booking.getStatusDisplay());
                    statusLabel.setPadding(new Insets(5, 15, 5, 15));
                    statusLabel.setStyle(getStatusStyle(booking.getStatutReservation()));
                    setGraphic(statusLabel);
                }
            }
        });

        // Price column
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        priceColumn.setCellFactory(column -> new TableCell<HotelBooking, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.0f", price));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1e40af;");
                }
            }
        });

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button editBtn = new Button("✏");
            private final Button deleteBtn = new Button("🗑");

            {
                viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");

                viewBtn.setOnAction(event -> {
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    viewBookingDetails(booking);
                });

                editBtn.setOnAction(event -> {
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    editBooking(booking);
                });

                deleteBtn.setOnAction(event -> {
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    deleteBooking(booking);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actions = new HBox(5, viewBtn, editBtn, deleteBtn);
                    setGraphic(actions);
                }
            }
        });
    }

    private void setupFilterButtons() {
        allFilterBtn.setOnAction(e -> filterBookings("ALL"));
        confirmedFilterBtn.setOnAction(e -> filterBookings("CONFIRMEE"));
        pendingFilterBtn.setOnAction(e -> filterBookings("EN_ATTENTE"));
        cancelledFilterBtn.setOnAction(e -> filterBookings("ANNULEE"));
    }

    private void loadAllBookings() {
        System.out.println("📋 Loading all bookings...");
        List<HotelBooking> bookings = bookingService.getAll();
        allBookings.setAll(bookings);
        bookingsTable.setItems(allBookings);
    }

    private void filterBookings(String status) {
        currentFilter = status;
        updateFilterButtonStyles();

        if ("ALL".equals(status)) {
            bookingsTable.setItems(allBookings);
        } else {
            HotelBooking.StatutReservation statutFilter = HotelBooking.StatutReservation.valueOf(status);
            List<HotelBooking> filtered = bookingService.getBookingsByStatus(statutFilter);
            bookingsTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void updateFilterButtonStyles() {
        String activeStyle = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;";
        String inactiveStyle = "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";

        allFilterBtn.setStyle("ALL".equals(currentFilter) ? activeStyle : inactiveStyle);
        confirmedFilterBtn.setStyle("CONFIRMEE".equals(currentFilter) ? activeStyle : inactiveStyle);
        pendingFilterBtn.setStyle("EN_ATTENTE".equals(currentFilter) ? activeStyle : inactiveStyle);
        cancelledFilterBtn.setStyle("ANNULEE".equals(currentFilter) ? activeStyle : inactiveStyle);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isBlank()) {
            bookingsTable.setItems(allBookings);
            return;
        }

        ObservableList<HotelBooking> filtered = allBookings.filtered(booking ->
            booking.getBookingId().toLowerCase().contains(searchText) ||
            booking.getGuestName().toLowerCase().contains(searchText) ||
            booking.getNumeroConfirmation().toLowerCase().contains(searchText) ||
            booking.getHotelName().toLowerCase().contains(searchText)
        );

        bookingsTable.setItems(filtered);
    }

    @FXML
    private void handleBookHotel() {
        try {
            SceneManager.switchScene("/ui/book-hotel-new.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error opening hotel booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBookFlight() {
        try {
            SceneManager.switchScene("/ui/book-flight.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error opening flight booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRentCar() {
        try {
            SceneManager.switchScene("/ui/rent-car.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error opening car rental: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewBookingDetails(HotelBooking booking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText(booking.getBookingId() + " - " + booking.getHotelName());

        String details = String.format(
            "Guest: %s\n" +
            "Email: %s\n" +
            "Confirmation: %s\n\n" +
            "Hotel: %s\n" +
            "Room Type: %s\n" +
            "Check-in: %s\n" +
            "Check-out: %s\n" +
            "Nights: %d\n\n" +
            "Guests: %d adults, %d children\n" +
            "Total Price: $%.2f\n" +
            "Status: %s\n\n" +
            "Special Requests: %s",
            booking.getGuestName(),
            booking.getGuestEmail(),
            booking.getNumeroConfirmation(),
            booking.getHotelName(),
            booking.getChambreType(),
            booking.getDateCheckin().format(DATE_FORMATTER),
            booking.getDateCheckout().format(DATE_FORMATTER),
            booking.getNombreNuits(),
            booking.getNombreAdultes(),
            booking.getNombreEnfants(),
            booking.getPrixTotal(),
            booking.getStatusDisplay(),
            booking.getDemandesSpeciales() != null ? booking.getDemandesSpeciales() : "None"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void editBooking(HotelBooking booking) {
        // Show dialog to change status
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Confirmed",
            "Confirmed", "Pending", "Cancelled", "Completed");
        dialog.setTitle("Update Booking Status");
        dialog.setHeaderText("Booking: " + booking.getBookingId());
        dialog.setContentText("Change status to:");

        dialog.showAndWait().ifPresent(status -> {
            HotelBooking.StatutReservation newStatus;
            switch (status) {
                case "Confirmed": newStatus = HotelBooking.StatutReservation.CONFIRMEE; break;
                case "Pending": newStatus = HotelBooking.StatutReservation.EN_ATTENTE; break;
                case "Cancelled": newStatus = HotelBooking.StatutReservation.ANNULEE; break;
                case "Completed": newStatus = HotelBooking.StatutReservation.TERMINEE; break;
                default: return;
            }

            booking.setStatutReservation(newStatus);
            if (bookingService.update(booking)) {
                showSuccess("Booking status updated successfully!");
                loadAllBookings();
            } else {
                showError("Failed to update booking status.");
            }
        });
    }

    private void deleteBooking(HotelBooking booking) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Booking");
        confirm.setHeaderText("Delete " + booking.getBookingId() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (bookingService.delete(booking.getReservationId())) {
                    showSuccess("Booking deleted successfully!");
                    loadAllBookings();
                } else {
                    showError("Failed to delete booking.");
                }
            }
        });
    }

    private String getStatusStyle(HotelBooking.StatutReservation status) {
        switch (status) {
            case CONFIRMEE:
                return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; " +
                       "-fx-background-radius: 12; -fx-font-weight: bold;";
            case EN_ATTENTE:
                return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                       "-fx-background-radius: 12; -fx-font-weight: bold;";
            case ANNULEE:
                return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; " +
                       "-fx-background-radius: 12; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; " +
                       "-fx-background-radius: 12; -fx-font-weight: bold;";
        }
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
