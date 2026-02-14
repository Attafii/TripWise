package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import ui.model.Hotel;
import ui.model.Room;
import ui.model.HotelBooking;
import ui.model.Payment;
import ui.repo.HotelRepository;
import ui.repo.BookingRepository;
import ui.service.PaymentClient;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class HotelController {

    // --- Search View Fields ---
    @FXML private TextField destinationField;
    @FXML private DatePicker checkInDate;
    @FXML private DatePicker checkOutDate;
    @FXML private Spinner<Integer> guestsSpinner;
    @FXML private TableView<Hotel> hotelTable;
    @FXML private TableColumn<Hotel, String> nameColumn;
    @FXML private TableColumn<Hotel, String> cityColumn;
    @FXML private TableColumn<Hotel, Double> priceColumn;
    @FXML private TableColumn<Hotel, Double> ratingColumn;

    // --- Details View Fields ---
    @FXML private Label hotelNameLabel;
    @FXML private Label hotelLocationLabel;
    @FXML private Label hotelRatingLabel;
    @FXML private Label hotelDescriptionLabel;
    @FXML private ListView<Room> roomListView;

    // --- Booking View Fields ---
    @FXML private Label bookingHotelLabel;
    @FXML private Label bookingRoomLabel;
    @FXML private Label bookingDatesLabel;
    @FXML private Label bookingPriceLabel;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private Label bookingStatusLabel;
    @FXML private Button confirmButton;

    // --- State & Data ---
    private static ObservableList<Hotel> allHotels;
    private static Hotel selectedHotel;
    private static Room selectedRoom;
    private static HotelBooking currentBooking;
    
    // Persist search dates across views
    private static LocalDate lastCheckInDate;
    private static LocalDate lastCheckOutDate;
    
    // We need a way to replace the view. 
    // Since we are inside a BorderPane in Dashboard, we can try to find the root.
    // For simplicity, we will assume we can get the scene's root or pass a reference.
    // But since we can't easily change constructor, we will use a static helper or look up.
    
    @FXML
    private void initialize() {
        if (allHotels == null) {
            initializeSampleData();
        }

        // Initialize Search View if active
        if (hotelTable != null) {
            setupSearchTable();
        }

        // Initialize Details View if active
        if (hotelNameLabel != null && selectedHotel != null) {
            setupDetailsView();
        }

        // Initialize Booking View if active
        if (bookingHotelLabel != null && currentBooking != null) {
            setupBookingView();
        }
    }
    
    public static void resetState() {
        selectedHotel = null;
        selectedRoom = null;
        currentBooking = null;
    }

    private void initializeSampleData() {
        allHotels = FXCollections.observableArrayList(HotelRepository.getAll());
    }

    private void setupSearchTable() {
        guestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        hotelTable.setItems(allHotels);

        // Handle selection
        hotelTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && hotelTable.getSelectionModel().getSelectedItem() != null) {
                selectedHotel = hotelTable.getSelectionModel().getSelectedItem();
                navigateTo("/ui/hotel/hotel-details.fxml");
            }
        });
    }

    private void setupDetailsView() {
        hotelNameLabel.setText(selectedHotel.getName());
        hotelLocationLabel.setText(selectedHotel.getCity());
        hotelRatingLabel.setText("Rating: " + selectedHotel.getRating());
        hotelDescriptionLabel.setText(selectedHotel.getDescription());

        roomListView.setItems(FXCollections.observableArrayList(selectedHotel.getRooms()));
        roomListView.setCellFactory(param -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - $" + item.getPricePerNight() + "/night - Cap: " + item.getCapacity());
                }
            }
        });

        roomListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && roomListView.getSelectionModel().getSelectedItem() != null) {
                selectedRoom = roomListView.getSelectionModel().getSelectedItem();
                
                // Use persisted dates or default
                LocalDate start = lastCheckInDate != null ? lastCheckInDate : LocalDate.now().plusDays(1);
                LocalDate end = lastCheckOutDate != null ? lastCheckOutDate : LocalDate.now().plusDays(3);
                
                long nights = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                if (nights < 1) nights = 1;
                double total = selectedRoom.getPricePerNight() * nights;
                
                currentBooking = new HotelBooking(selectedHotel, selectedRoom, start, end, total);
                navigateTo("/ui/hotel/hotel-booking.fxml");
            }
        });
    }

    private void setupBookingView() {
        bookingHotelLabel.setText("Hotel: " + currentBooking.getHotel().getName());
        bookingRoomLabel.setText("Room: " + currentBooking.getRoom().getName());
        bookingDatesLabel.setText("Dates: " + currentBooking.getCheckIn() + " to " + currentBooking.getCheckOut());
        bookingPriceLabel.setText("Total Price: $" + String.format("%.2f", currentBooking.getTotalPrice()));
        
        bookingStatusLabel.setText("Status: " + currentBooking.getStatus());
    }

    @FXML
    private void handleSearch() {
        String dest = destinationField.getText();
        
        // Persist user-chosen dates
        lastCheckInDate = checkInDate.getValue();
        lastCheckOutDate = checkOutDate.getValue();
        
        if (dest == null || dest.isEmpty()) {
            hotelTable.setItems(allHotels);
            return;
        }

        List<Hotel> filtered = allHotels.stream()
                .filter(h -> h.getCity().toLowerCase().contains(dest.toLowerCase()) || 
                             h.getName().toLowerCase().contains(dest.toLowerCase()))
                .collect(Collectors.toList());
        
        hotelTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleConfirmBooking() {
        PaymentClient.Result result = PaymentClient.processPayment(cardNumberField.getText(), expiryDateField.getText(), cvvField.getText());
        if (result.success) {
            currentBooking.setStatus(HotelBooking.Status.CONFIRMED);
            BookingRepository.save(currentBooking);
            bookingStatusLabel.setText("Status: CONFIRMED! Booking ID: " + currentBooking.getBookingId());
            bookingStatusLabel.setStyle("-fx-text-fill: green;");
            confirmButton.setDisable(true);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Confirmed");
            alert.setHeaderText("Success!");
            alert.setContentText("Saved to database. ID: " + currentBooking.getBookingId() +
                    (result.transactionId != null ? "\nTransaction: " + result.transactionId : ""));
            alert.showAndWait();
            if (hotelTable != null) {
                hotelTable.refresh();
            }
        } else {
            bookingStatusLabel.setText("Payment Failed. " + (result.error != null ? result.error : "Check details."));
            bookingStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleBackToSearch() {
        navigateTo("/ui/hotel/hotel-search.fxml");
    }

    @FXML
    private void handleBackToDetails() {
        navigateTo("/ui/hotel/hotel-details.fxml");
    }

    @FXML
    private void handleViewBookedHotels() {
        navigateTo("/ui/hotel/hotel-booked.fxml");
    }

    // Navigation helper
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            if (hotelTable != null && hotelTable.getScene() != null) {
                 BorderPane outer = (BorderPane) hotelTable.getScene().getRoot();
                 var centerNode = outer.getCenter();
                 if (centerNode instanceof BorderPane) {
                     ((BorderPane) centerNode).setCenter(view);
                 } else {
                     outer.setCenter(view);
                 }
            } else if (hotelNameLabel != null && hotelNameLabel.getScene() != null) {
                BorderPane outer = (BorderPane) hotelNameLabel.getScene().getRoot();
                var centerNode = outer.getCenter();
                if (centerNode instanceof BorderPane) {
                    ((BorderPane) centerNode).setCenter(view);
                } else {
                    outer.setCenter(view);
                }
            } else if (bookingHotelLabel != null && bookingHotelLabel.getScene() != null) {
                 BorderPane outer = (BorderPane) bookingHotelLabel.getScene().getRoot();
                 var centerNode = outer.getCenter();
                 if (centerNode instanceof BorderPane) {
                     ((BorderPane) centerNode).setCenter(view);
                 } else {
                     outer.setCenter(view);
                 }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
