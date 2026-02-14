package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import ui.model.FlightBooking;
import ui.model.Seat;
import ui.model.SeatMap;
import ui.service.EmailNotificationService;
import ui.service.SeatSelectionService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for seat selection interface
 */
public class SeatSelectionController implements Initializable {

    @FXML private Button btnBack;
    @FXML private Label lblFlightNumber;
    @FXML private Label lblRoute;
    @FXML private Label lblPassengerCount;
    @FXML private Label lblClassType;
    @FXML private VBox seatMapContainer;
    @FXML private VBox selectedSeatsContainer;
    @FXML private ComboBox<String> cmbPreference;
    @FXML private Label lblBasePrice;
    @FXML private Label lblSeatExtra;
    @FXML private Label lblTotalPrice;
    @FXML private Label lblStatus;
    @FXML private Label lblAvailableCount;
    @FXML private Button btnConfirmSeats;
    @FXML private Button btnAutoAssign;
    @FXML private Button btnClearSelection;

    private SeatSelectionService seatService;
    private EmailNotificationService emailService;

    private int volId;
    private int reservationId;
    private int passengerCount = 1;
    private String flightClass = "ECONOMIQUE";
    private double basePrice = 0;

    private SeatMap seatMap;
    private List<Seat> selectedSeats = new ArrayList<>();
    private List<Button> seatButtons = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        seatService = new SeatSelectionService();
        emailService = new EmailNotificationService();

        cmbPreference.setValue("Any");

        btnBack.setOnAction(e -> goBack());
    }

    /**
     * Initialize with flight and booking data
     */
    public void initData(int volId, int reservationId, int passengers, String flightClass, double basePrice,
                         String flightNumber, String route) {
        this.volId = volId;
        this.reservationId = reservationId;
        this.passengerCount = passengers;
        this.flightClass = flightClass;
        this.basePrice = basePrice;

        lblFlightNumber.setText(flightNumber);
        lblRoute.setText(route);
        lblPassengerCount.setText(String.valueOf(passengers));
        lblClassType.setText(getClassDisplayName(flightClass));
        lblBasePrice.setText(String.format("$%.2f", basePrice));

        loadSeatMap();
        updatePriceSummary();
    }

    /**
     * Load and display the seat map
     */
    private void loadSeatMap() {
        seatMap = seatService.getSeatMapForFlight(volId);
        seatMapContainer.getChildren().clear();
        seatButtons.clear();

        // Column headers
        HBox headerRow = new HBox(5);
        headerRow.setAlignment(Pos.CENTER);
        headerRow.getChildren().add(createRowLabel("")); // Empty for row numbers

        for (String col : seatMap.getColumns()) {
            Label colLabel = new Label(col);
            colLabel.setMinWidth(35);
            colLabel.setAlignment(Pos.CENTER);
            colLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
            headerRow.getChildren().add(colLabel);

            // Add aisle spacing
            if ("C".equals(col) || "F".equals(col) || "G".equals(col)) {
                Region spacer = new Region();
                spacer.setMinWidth(20);
                headerRow.getChildren().add(spacer);
            }
        }
        seatMapContainer.getChildren().add(headerRow);

        // Generate seat rows
        for (int row = 1; row <= seatMap.getTotalRows(); row++) {
            HBox rowBox = createSeatRow(row);
            seatMapContainer.getChildren().add(rowBox);

            // Add class separator
            if (row == 3 || row == 6 || row == 10) {
                Separator sep = new Separator();
                sep.setStyle("-fx-padding: 5 0;");
                seatMapContainer.getChildren().add(sep);
            }
        }

        updateAvailableCount();
    }

    /**
     * Create a row of seats
     */
    private HBox createSeatRow(int rowNum) {
        HBox rowBox = new HBox(5);
        rowBox.setAlignment(Pos.CENTER);

        // Row number label
        rowBox.getChildren().add(createRowLabel(String.valueOf(rowNum)));

        List<Seat> rowSeats = seatMap.getSeatsByRow(rowNum);

        for (Seat seat : rowSeats) {
            Button seatBtn = createSeatButton(seat);
            rowBox.getChildren().add(seatBtn);
            seatButtons.add(seatBtn);

            // Add aisle spacing
            String col = seat.getColumn();
            if ("C".equals(col) || "F".equals(col) || "G".equals(col)) {
                Region spacer = new Region();
                spacer.setMinWidth(20);
                rowBox.getChildren().add(spacer);
            }
        }

        return rowBox;
    }

    /**
     * Create a seat button
     */
    private Button createSeatButton(Seat seat) {
        Button btn = new Button(seat.getSeatNumber());
        btn.setMinSize(35, 35);
        btn.setMaxSize(35, 35);
        btn.setUserData(seat);

        updateSeatButtonStyle(btn, seat);

        if (seat.isAvailable()) {
            btn.setOnAction(e -> toggleSeatSelection(seat, btn));
        }

        // Tooltip
        String tooltip = seat.getSeatNumber() + " - " + seat.getPositionDescription();
        if (seat.hasExtraLegroom()) tooltip += "\n+Extra Legroom";
        if (seat.isExitRow()) tooltip += "\n+Exit Row";
        if (seat.getExtraPrice() > 0) tooltip += "\n+$" + String.format("%.0f", seat.getExtraPrice());
        btn.setTooltip(new Tooltip(tooltip));

        return btn;
    }

    /**
     * Update seat button appearance based on status
     */
    private void updateSeatButtonStyle(Button btn, Seat seat) {
        String baseStyle = "-fx-min-width: 35; -fx-min-height: 35; -fx-font-size: 10px; -fx-background-radius: 5; -fx-cursor: hand; ";

        if (seat.getStatus() == Seat.SeatStatus.OCCUPIED) {
            btn.setStyle(baseStyle + "-fx-background-color: #ccc; -fx-text-fill: #666;");
            btn.setDisable(true);
        } else if (seat.getStatus() == Seat.SeatStatus.SELECTED) {
            btn.setStyle(baseStyle + "-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (seat.isExitRow()) {
            btn.setStyle(baseStyle + "-fx-background-color: #FF6B6B; -fx-text-fill: white;");
        } else if (seat.hasExtraLegroom()) {
            btn.setStyle(baseStyle + "-fx-background-color: #FFD700; -fx-text-fill: #333;");
        } else {
            btn.setStyle(baseStyle + "-fx-background-color: #4CAF50; -fx-text-fill: white;");
        }
    }

    /**
     * Toggle seat selection
     */
    private void toggleSeatSelection(Seat seat, Button btn) {
        if (selectedSeats.contains(seat)) {
            // Deselect
            selectedSeats.remove(seat);
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            updateSeatButtonStyle(btn, seat);
        } else {
            // Check if max passengers reached
            if (selectedSeats.size() >= passengerCount) {
                showAlert("Maximum Reached", "You can only select " + passengerCount + " seat(s).");
                return;
            }

            // Check class restriction
            if (!isClassCompatible(seat.getFlightClass())) {
                showAlert("Class Restriction", "This seat is in " + seat.getFlightClass() + " class. You booked " + flightClass + ".");
                return;
            }

            // Select
            selectedSeats.add(seat);
            seat.setStatus(Seat.SeatStatus.SELECTED);
            updateSeatButtonStyle(btn, seat);
        }

        updateSelectedSeatsDisplay();
        updatePriceSummary();
        updateStatus();
    }

    /**
     * Check if seat class is compatible with booking
     */
    private boolean isClassCompatible(String seatClass) {
        // Allow selecting seats from same or higher class
        int bookingLevel = getClassLevel(flightClass);
        int seatLevel = getClassLevel(seatClass);
        return seatLevel >= bookingLevel;
    }

    private int getClassLevel(String className) {
        switch (className) {
            case "ECONOMIQUE": return 1;
            case "PREMIUM": return 2;
            case "BUSINESS": return 3;
            case "PREMIERE": return 4;
            default: return 1;
        }
    }

    /**
     * Update the selected seats display panel
     */
    private void updateSelectedSeatsDisplay() {
        selectedSeatsContainer.getChildren().clear();

        if (selectedSeats.isEmpty()) {
            Label noSeats = new Label("No seats selected");
            noSeats.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            selectedSeatsContainer.getChildren().add(noSeats);
        } else {
            for (Seat seat : selectedSeats) {
                HBox seatItem = new HBox(10);
                seatItem.setAlignment(Pos.CENTER_LEFT);
                seatItem.setStyle("-fx-background-color: #f0f4ff; -fx-padding: 8; -fx-background-radius: 5;");

                Label seatLabel = new Label("💺 " + seat.getSeatNumber());
                seatLabel.setStyle("-fx-font-weight: bold;");

                Label posLabel = new Label(seat.getPositionDescription());
                posLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label priceLabel = new Label("");
                if (seat.getExtraPrice() > 0) {
                    priceLabel.setText("+$" + String.format("%.0f", seat.getExtraPrice()));
                    priceLabel.setStyle("-fx-text-fill: #667eea; -fx-font-size: 11px;");
                }

                seatItem.getChildren().addAll(seatLabel, posLabel, spacer, priceLabel);
                selectedSeatsContainer.getChildren().add(seatItem);
            }
        }
    }

    /**
     * Update price summary
     */
    private void updatePriceSummary() {
        double seatExtra = 0;
        for (Seat seat : selectedSeats) {
            seatExtra += seat.getExtraPrice();
        }

        lblSeatExtra.setText(String.format("$%.2f", seatExtra));
        lblTotalPrice.setText(String.format("$%.2f", basePrice + seatExtra));
    }

    /**
     * Update status message
     */
    private void updateStatus() {
        int remaining = passengerCount - selectedSeats.size();
        if (remaining > 0) {
            lblStatus.setText("Select " + remaining + " more seat(s)");
            lblStatus.setStyle("-fx-text-fill: #f39c12;");
        } else {
            lblStatus.setText("✓ All seats selected");
            lblStatus.setStyle("-fx-text-fill: #4CAF50;");
        }
    }

    /**
     * Update available seats count
     */
    private void updateAvailableCount() {
        int available = seatMap.countAvailableSeats();
        lblAvailableCount.setText("Available: " + available + " seats");
    }

    /**
     * Handle auto-assign seats
     */
    @FXML
    private void handleAutoAssign() {
        String preference = cmbPreference.getValue();
        List<String> assigned = seatService.autoAssignSeats(volId, passengerCount, flightClass, preference);

        if (assigned.size() < passengerCount) {
            showAlert("Limited Availability", "Could only find " + assigned.size() + " available seats.");
        }

        // Clear current selection
        handleClearSelection();

        // Select assigned seats
        for (String seatNum : assigned) {
            for (Button btn : seatButtons) {
                Seat seat = (Seat) btn.getUserData();
                if (seat.getSeatNumber().equals(seatNum)) {
                    toggleSeatSelection(seat, btn);
                    break;
                }
            }
        }
    }

    /**
     * Handle clear selection
     */
    @FXML
    private void handleClearSelection() {
        for (Seat seat : new ArrayList<>(selectedSeats)) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
        }
        selectedSeats.clear();

        // Refresh button styles
        for (Button btn : seatButtons) {
            Seat seat = (Seat) btn.getUserData();
            updateSeatButtonStyle(btn, seat);
        }

        updateSelectedSeatsDisplay();
        updatePriceSummary();
        updateStatus();
    }

    /**
     * Handle confirm seats
     */
    @FXML
    private void handleConfirmSeats() {
        if (selectedSeats.size() != passengerCount) {
            showAlert("Incomplete Selection", "Please select " + passengerCount + " seat(s).");
            return;
        }

        // Get seat numbers
        List<String> seatNumbers = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            seatNumbers.add(seat.getSeatNumber());
        }

        // Reserve seats in database
        boolean success = seatService.reserveSeats(reservationId, seatNumbers);

        if (success) {
            showSuccess("Seats Confirmed", "Your seats have been reserved: " + String.join(", ", seatNumbers));

            // Send confirmation email (optional)
            // emailService.sendSeatConfirmation(booking, String.join(", ", seatNumbers));

            goBack();
        } else {
            showAlert("Error", "Failed to reserve seats. Please try again.");
        }
    }

    /**
     * Navigate back
     */
    private void goBack() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/ui/traveler-bookings.fxml"));
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Label createRowLabel(String text) {
        Label label = new Label(text);
        label.setMinWidth(30);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
        return label;
    }

    private String getClassDisplayName(String className) {
        switch (className) {
            case "ECONOMIQUE": return "Economy";
            case "PREMIUM": return "Premium Economy";
            case "BUSINESS": return "Business";
            case "PREMIERE": return "First Class";
            default: return className;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
