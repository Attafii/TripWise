package ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import ui.model.User;
import ui.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class EmployeeBookingManagementController {

    @FXML
    private Label employeeNameLabel;

    @FXML
    private Button allBtn, pendingBtn, approvedBtn, rejectedBtn;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<HotelBooking> bookingsTable;

    @FXML
    private TableColumn<HotelBooking, String> bookingIdCol;

    @FXML
    private TableColumn<HotelBooking, String> customerCol;

    @FXML
    private TableColumn<HotelBooking, String> hotelCol;

    @FXML
    private TableColumn<HotelBooking, String> roomCol;

    @FXML
    private TableColumn<HotelBooking, String> checkinCol;

    @FXML
    private TableColumn<HotelBooking, String> checkoutCol;

    @FXML
    private TableColumn<HotelBooking, String> guestsCol;

    @FXML
    private TableColumn<HotelBooking, String> priceCol;

    @FXML
    private TableColumn<HotelBooking, String> statusCol;

    @FXML
    private TableColumn<HotelBooking, Void> actionsCol;

    private ObservableList<HotelBooking> allBookings = FXCollections.observableArrayList();
    private String currentFilter = "ALL";

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            employeeNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }

        setupTableColumns();
        loadAllBookings();
        setupSearchFilter();
    }

    private void setupTableColumns() {
        bookingIdCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().reservationId)));
        customerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().customerName));
        hotelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().hotelName));
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().roomType));
        checkinCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().checkIn.toString()));
        checkoutCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().checkOut.toString()));
        guestsCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().guests)));
        priceCol.setCellValueFactory(data -> new SimpleStringProperty("$" + String.format("%.2f", data.getValue().totalPrice)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        // Actions column with buttons
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("✓ Approve");
            private final Button rejectBtn = new Button("✕ Reject");
            private final Button viewBtn = new Button("👁 View");
            private final HBox pane = new HBox(8);

            {
                approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-font-size: 11px; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-font-size: 11px; -fx-cursor: hand;");

                approveBtn.setOnAction(event -> {
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    handleApproveBooking(booking);
                });

                rejectBtn.setOnAction(event -> {
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    handleRejectBooking(booking);
                });

                viewBtn.setOnAction(event -> {
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    handleViewBookingDetails(booking);
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
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    if ("CONFIRMEE".equals(booking.status)) {
                        approveBtn.setDisable(true);
                        rejectBtn.setDisable(true);
                    } else if ("ANNULEE".equals(booking.status)) {
                        approveBtn.setDisable(true);
                        rejectBtn.setDisable(true);
                    } else {
                        approveBtn.setDisable(false);
                        rejectBtn.setDisable(false);
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadAllBookings() {
        allBookings.clear();
        try {
            Connection conn = ui.util.DataSource.getInstance().getConnection();
            String query = "SELECT r.reservation_id, r.date_checkin, r.date_checkout, r.nombre_adultes, " +
                          "r.nombre_enfants, r.prix_total, r.statut_reservation, " +
                          "u.first_name, u.last_name, h.nom_hotel, c.type_chambre " +
                          "FROM reservations_hotel r " +
                          "JOIN voyageurs v ON r.voyageur_id = v.voyageur_id " +
                          "JOIN users u ON v.user_id = u.user_id " +
                          "JOIN hotels h ON r.hotel_id = h.hotel_id " +
                          "JOIN chambres c ON r.chambre_id = c.chambre_id " +
                          "ORDER BY r.date_reservation DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HotelBooking booking = new HotelBooking();
                booking.reservationId = rs.getInt("reservation_id");
                booking.customerName = rs.getString("first_name") + " " + rs.getString("last_name");
                booking.hotelName = rs.getString("nom_hotel");
                booking.roomType = rs.getString("type_chambre");
                booking.checkIn = rs.getDate("date_checkin").toLocalDate();
                booking.checkOut = rs.getDate("date_checkout").toLocalDate();
                booking.guests = rs.getInt("nombre_adultes") + rs.getInt("nombre_enfants");
                booking.totalPrice = rs.getDouble("prix_total");
                booking.status = rs.getString("statut_reservation");

                allBookings.add(booking);
            }

            bookingsTable.setItems(allBookings);
            updateFilterCounts();

        } catch (Exception e) {
            System.err.println("❌ Error loading bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFilterCounts() {
        long pending = allBookings.stream().filter(b -> "EN_ATTENTE".equals(b.status)).count();
        long approved = allBookings.stream().filter(b -> "CONFIRMEE".equals(b.status)).count();
        long rejected = allBookings.stream().filter(b -> "ANNULEE".equals(b.status)).count();

        pendingBtn.setText("Pending (" + pending + ")");
        approvedBtn.setText("Approved (" + approved + ")");
        rejectedBtn.setText("Rejected (" + rejected + ")");
    }

    @FXML
    private void filterAll() {
        currentFilter = "ALL";
        bookingsTable.setItems(allBookings);
    }

    @FXML
    private void filterPending() {
        currentFilter = "PENDING";
        ObservableList<HotelBooking> filtered = allBookings.filtered(b -> "EN_ATTENTE".equals(b.status));
        bookingsTable.setItems(filtered);
    }

    @FXML
    private void filterApproved() {
        currentFilter = "APPROVED";
        ObservableList<HotelBooking> filtered = allBookings.filtered(b -> "CONFIRMEE".equals(b.status));
        bookingsTable.setItems(filtered);
    }

    @FXML
    private void filterRejected() {
        currentFilter = "REJECTED";
        ObservableList<HotelBooking> filtered = allBookings.filtered(b -> "ANNULEE".equals(b.status));
        bookingsTable.setItems(filtered);
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                applyCurrentFilter();
                return;
            }

            String search = newValue.toLowerCase();
            ObservableList<HotelBooking> filtered = allBookings.filtered(b ->
                b.customerName.toLowerCase().contains(search) ||
                String.valueOf(b.reservationId).contains(search) ||
                b.hotelName.toLowerCase().contains(search)
            );
            bookingsTable.setItems(filtered);
        });
    }

    private void applyCurrentFilter() {
        switch (currentFilter) {
            case "PENDING": filterPending(); break;
            case "APPROVED": filterApproved(); break;
            case "REJECTED": filterRejected(); break;
            default: filterAll(); break;
        }
    }

    private void handleApproveBooking(HotelBooking booking) {
        try {
            Connection conn = ui.util.DataSource.getInstance().getConnection();
            String query = "UPDATE reservations_hotel SET statut_reservation = 'CONFIRMEE' WHERE reservation_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, booking.reservationId);
            stmt.executeUpdate();

            showAlert("Success", "Booking #" + booking.reservationId + " has been approved!", Alert.AlertType.INFORMATION);
            loadAllBookings();

        } catch (Exception e) {
            showAlert("Error", "Failed to approve booking: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleRejectBooking(HotelBooking booking) {
        try {
            Connection conn = ui.util.DataSource.getInstance().getConnection();
            String query = "UPDATE reservations_hotel SET statut_reservation = 'ANNULEE' WHERE reservation_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, booking.reservationId);
            stmt.executeUpdate();

            showAlert("Success", "Booking #" + booking.reservationId + " has been rejected!", Alert.AlertType.INFORMATION);
            loadAllBookings();

        } catch (Exception e) {
            showAlert("Error", "Failed to reject booking: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleViewBookingDetails(HotelBooking booking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText("Booking #" + booking.reservationId);
        alert.setContentText(
            "Customer: " + booking.customerName + "\n" +
            "Hotel: " + booking.hotelName + "\n" +
            "Room: " + booking.roomType + "\n" +
            "Check-in: " + booking.checkIn + "\n" +
            "Check-out: " + booking.checkOut + "\n" +
            "Guests: " + booking.guests + "\n" +
            "Total Price: $" + String.format("%.2f", booking.totalPrice) + "\n" +
            "Status: " + booking.status
        );
        alert.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class for booking data
    public static class HotelBooking {
        int reservationId;
        String customerName;
        String hotelName;
        String roomType;
        LocalDate checkIn;
        LocalDate checkOut;
        int guests;
        double totalPrice;
        String status;
    }
}
