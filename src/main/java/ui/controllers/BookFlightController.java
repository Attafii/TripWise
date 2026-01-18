package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.model.Flight;

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
    private TableColumn<Flight, Double> priceColumn;

    @FXML
    private void initialize() {
        passengersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1));

        airlineColumn.setCellValueFactory(new PropertyValueFactory<>("airline"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    @FXML
    private void handleSearch() {
        ObservableList<Flight> flights = FXCollections.observableArrayList(
                new Flight("Delta Airlines", "NYC → LAX", "08:00 AM", 349.99),
                new Flight("United Airlines", "NYC → LAX", "11:30 AM", 299.99),
                new Flight("American Airlines", "NYC → LAX", "02:45 PM", 389.99),
                new Flight("Southwest", "NYC → LAX", "06:15 PM", 279.99)
        );

        flightTable.setItems(flights);
    }
}

