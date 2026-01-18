package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.model.Hotel;

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

    @FXML
    private void initialize() {
        guestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
    }

    @FXML
    private void handleSearch() {
        ObservableList<Hotel> hotels = FXCollections.observableArrayList(
                new Hotel("Grand Plaza Hotel", "Paris", 199.99, 4.5),
                new Hotel("Ocean View Resort", "Miami", 299.99, 4.8),
                new Hotel("City Center Inn", "New York", 159.99, 4.2),
                new Hotel("Mountain Lodge", "Denver", 179.99, 4.6),
                new Hotel("Beach Paradise", "Bali", 249.99, 4.9)
        );

        hotelTable.setItems(hotels);
    }
}

