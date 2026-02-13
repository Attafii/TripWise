package controller;

import dao.BookingDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.NumberStringConverter;
import model.Booking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookingController {
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> colId;
    @FXML private TableColumn<Booking, String> colPassenger;
    @FXML private TableColumn<Booking, Integer> colFlightId;
    @FXML private TableColumn<Booking, String> colSeat;
    @FXML private TableColumn<Booking, LocalDateTime> colBookingDate; // NEW

    @FXML private TextField tfPassenger;
    @FXML private TextField tfFlightId;
    @FXML private TextField tfSeat;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ObservableList<Booking> data = FXCollections.observableArrayList();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // Bind columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        colFlightId.setCellValueFactory(new PropertyValueFactory<>("flightId"));
        colSeat.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));

        // Booking date column + formatting
        if (colBookingDate != null) {
            colBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
            colBookingDate.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.format(dtf));
                }
            });
        }

        // When a row is selected, put values in text fields
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                tfPassenger.setText(sel.getPassengerName());
                tfFlightId.setText(String.valueOf(sel.getFlightId()));
                tfSeat.setText(sel.getSeatNumber());
            }
        });

        refresh();
    }

    @FXML
    private void addBooking() {
        // Basic validation
        if (tfPassenger.getText().isBlank() || tfFlightId.getText().isBlank() || tfSeat.getText().isBlank()) {
            showWarn("Please fill Passenger, Flight ID, and Seat.");
            return;
        }

        int flightId;
        try {
            flightId = Integer.parseInt(tfFlightId.getText().trim());
        } catch (NumberFormatException e) {
            showWarn("Flight ID must be a number.");
            return;
        }

        Booking b = new Booking(tfPassenger.getText().trim(), flightId, tfSeat.getText().trim());
        bookingDAO.addBooking(b);
        clearFields();
        refresh();
    }

    @FXML
    private void delBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a booking row to delete.");
            return;
        }
        // Confirm dialog (optional)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete booking #" + selected.getId() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;

        bookingDAO.deleteBooking(selected.getId());
        clearFields();
        refresh();
    }

    @FXML
    private void updateBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a booking row to update.");
            return;
        }

        if (tfPassenger.getText().isBlank() || tfFlightId.getText().isBlank() || tfSeat.getText().isBlank()) {
            showWarn("Please fill Passenger, Flight ID, and Seat.");
            return;
        }

        int flightId;
        try {
            flightId = Integer.parseInt(tfFlightId.getText().trim());
        } catch (NumberFormatException e) {
            showWarn("Flight ID must be a number.");
            return;
        }

        selected.setPassengerName(tfPassenger.getText().trim());
        selected.setFlightId(flightId);
        selected.setSeatNumber(tfSeat.getText().trim());

        bookingDAO.updateBooking(selected);
        clearFields();
        refresh();
    }

    private void refresh() {
        data.setAll(bookingDAO.getAllBookings());
        bookingsTable.setItems(data);
        bookingsTable.refresh();
    }

    private void clearFields() {
        tfPassenger.clear();
        tfFlightId.clear();
        tfSeat.clear();
        bookingsTable.getSelectionModel().clearSelection();
    }

    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}