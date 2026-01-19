
package controller;

import dao.FlightDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Flight;

public class FlightController {

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, Integer> colId;
    @FXML private TableColumn<Flight, String> colNumber;
    @FXML private TableColumn<Flight, String> colOrigin;
    @FXML private TableColumn<Flight, String> colDestination;

    @FXML private TextField tfNumber;
    @FXML private TextField tfOrigin;
    @FXML private TextField tfDestination;
    @FXML private TextField tfSearch;

    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;


    private final FlightDAO flightDAO = new FlightDAO();
    private final ObservableList<Flight> data = FXCollections.observableArrayList();
    private FilteredList<Flight> filteredData;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));

        refresh();

        filteredData = new FilteredList<>(data, p -> true);
        flightsTable.setItems(filteredData);

        // live filter by search text
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = (newVal == null) ? "" : newVal.trim().toLowerCase();
            filteredData.setPredicate(flight -> {
                if (keyword.isEmpty()) return true;
                return (flight.getFlightNumber() != null && flight.getFlightNumber().toLowerCase().contains(keyword))
                        || (flight.getOrigin() != null && flight.getOrigin().toLowerCase().contains(keyword))
                        || (flight.getDestination() != null && flight.getDestination().toLowerCase().contains(keyword));
            });
        });

        // When a row is selected, fill fields to update
        flightsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfNumber.setText(newSel.getFlightNumber());
                tfOrigin.setText(newSel.getOrigin());
                tfDestination.setText(newSel.getDestination());
            }
        });
    }

    @FXML
    private void addFlight() {
        if (isBlank(tfNumber) || isBlank(tfOrigin) || isBlank(tfDestination)) {
            showWarn("Please fill Flight Number, Origin and Destination.");
            return;
        }
        Flight f = new Flight(tfNumber.getText().trim(), tfOrigin.getText().trim(), tfDestination.getText().trim());
        flightDAO.addFlight(f);
        clearFields();
        refresh();
    }

    @FXML
    private void updateFlight() {
        Flight selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a flight to update.");
            return;
        }
        if (isBlank(tfNumber) || isBlank(tfOrigin) || isBlank(tfDestination)) {
            showWarn("Please fill Flight Number, Origin and Destination.");
            return;
        }
        selected.setFlightNumber(tfNumber.getText().trim());
        selected.setOrigin(tfOrigin.getText().trim());
        selected.setDestination(tfDestination.getText().trim());
        flightDAO.updateFlight(selected);
        clearFields();
        refresh();
    }

    @FXML
    private void deleteFlight() {
        Flight selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a flight to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete flight #" + selected.getId() + " ?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                flightDAO.deleteFlight(selected.getId());
                clearFields();
                refresh();
            }
        });
    }

    private void refresh() {
        data.setAll(flightDAO.getAllFlights());
        if (filteredData != null) {
            filteredData = new FilteredList<>(data, p -> true);
            flightsTable.setItems(filteredData);
        }
    }

    private void clearFields() {
        tfNumber.clear();
        tfOrigin.clear();
        tfDestination.clear();
    }

    private boolean isBlank(TextField tf) {
        return tf.getText() == null || tf.getText().trim().isEmpty();
    }

    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
