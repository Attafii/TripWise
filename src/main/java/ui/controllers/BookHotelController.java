package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.model.Hotel;
import ui.service.HotelService;

import java.util.List;

public class BookHotelController {

    @FXML
    private TextField destinationField;

    @FXML
    private DatePicker checkInDate;

    @FXML
    private DatePicker checkOutDate;

    @FXML
    private Spinner<Integer> guestsSpinner;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<Hotel> hotelTable;

    @FXML
    private TableColumn<Hotel, String> nameColumn;

    @FXML
    private TableColumn<Hotel, String> cityColumn;

    @FXML
    private TableColumn<Hotel, Double> priceColumn;

    @FXML
    private TableColumn<Hotel, Double> ratingColumn;

    private HotelService hotelService;

    @FXML
    private void initialize() {
        guestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));

        // Initialize hotel service
        hotelService = new HotelService();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Format price column to show currency
        priceColumn.setCellFactory(column -> new TableCell<Hotel, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        // Load all hotels on startup
        loadAllHotels();
    }

    @FXML
    private void handleSearch() {
        String destination = destinationField.getText();

        List<Hotel> hotels;

        // If search criteria provided, search, otherwise show all
        if (destination != null && !destination.isBlank()) {
            System.out.println("🔍 Searching hotels in: " + destination);
            hotels = hotelService.searchByCity(destination);
        } else {
            System.out.println("📋 Loading all hotels with pricing");
            hotels = hotelService.getHotelsWithPricing();
        }

        if (hotels.isEmpty()) {
            showAlert("No Hotels Found", "No hotels match your search criteria. Try a different city.");
        }

        hotelTable.setItems(FXCollections.observableArrayList(hotels));
    }

    /**
     * Load all hotels on initialization
     */
    private void loadAllHotels() {
        System.out.println("📋 Loading all hotels...");
        List<Hotel> hotels = hotelService.getHotelsWithPricing();
        hotelTable.setItems(FXCollections.observableArrayList(hotels));
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

