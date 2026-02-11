
package controller;

import dao.FlightDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Flight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FlightController {

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, Integer> colId;
    @FXML private TableColumn<Flight, String> colNumber;
    @FXML private TableColumn<Flight, String> colOrigin;
    @FXML private TableColumn<Flight, String> colDestination;
    @FXML private TableColumn<Flight, String> colDepartureTime; // affichage formaté
    @FXML private TableColumn<Flight, Integer> colSeatsAvailable;

    @FXML private TextField tfNumber;
    @FXML private TextField tfOrigin;
    @FXML private TextField tfDestination;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilter;

    // Sélection date + heure
    @FXML private DatePicker dpDepartureDate;
    @FXML private Spinner<Integer> spHour;
    @FXML private Spinner<Integer> spMinute;

    @FXML private TextField tfSeatsAvailable;

    private final FlightDAO flightDAO = new FlightDAO();
    private final ObservableList<Flight> data = FXCollections.observableArrayList();
    private FilteredList<Flight> filteredData;

    private static final DateTimeFormatter VIEW_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // Colonnes simples
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colSeatsAvailable.setCellValueFactory(new PropertyValueFactory<>("seatsAvailable"));

        // Colonne date/heure : formatage depuis LocalDateTime vers String
        colDepartureTime.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getDepartureTime();
            String s = (dt == null) ? "" : dt.format(VIEW_FMT);
            return new SimpleStringProperty(s);
        });

        // Init spinners heure/minute
        spHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        spMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        refresh();

        filteredData = new FilteredList<>(data, p -> true);

        cbFilter.getSelectionModel().select("All"); // valeur par défaut

        flightsTable.setItems(filteredData);

        // Filtre live

        cbFilter.setItems(FXCollections.observableArrayList(
                "All",
                "Flight Number",
                "Origin",
                "Destination",
                "Departure Date",
                "Seats Available"
        ));

        tfSearch.textProperty().addListener((obs, ov, nv) -> applyFilters());
        cbFilter.valueProperty().addListener((obs, ov, nv) -> applyFilters());


        // Sélection d'une ligne -> remplir les champs
        flightsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, n) -> {
            if (n != null) {
                tfNumber.setText(n.getFlightNumber());
                tfOrigin.setText(n.getOrigin());
                tfDestination.setText(n.getDestination());
                tfSeatsAvailable.setText(String.valueOf(n.getSeatsAvailable()));

                LocalDateTime dt = n.getDepartureTime();
                if (dt != null) {
                    dpDepartureDate.setValue(dt.toLocalDate());
                    spHour.getValueFactory().setValue(dt.getHour());
                    spMinute.getValueFactory().setValue(dt.getMinute());
                } else {
                    dpDepartureDate.setValue(null);
                    spHour.getValueFactory().setValue(0);
                    spMinute.getValueFactory().setValue(0);
                }
            }
        });
    }

    @FXML
    private void addFlight() {
        if (isBlank(tfNumber) || isBlank(tfOrigin) || isBlank(tfDestination)) {
            showWarn("Please fill Flight Number, Origin and Destination.");
            return;
        }

        int seats = 0;
        String seatsText = tfSeatsAvailable.getText();
        if (seatsText != null && !seatsText.trim().isEmpty()) {
            try {
                seats = Integer.parseInt(seatsText.trim());
            } catch (NumberFormatException e) {
                showWarn("Seats Available must be an integer.");
                return;
            }
        }

        LocalDateTime departure = buildDateTimeFromInputs();

        Flight flight = new Flight(
                tfNumber.getText().trim(),
                tfOrigin.getText().trim(),
                tfDestination.getText().trim(),
                departure,
                seats
        );

        flightDAO.addFlight(flight);
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

        int seats = selected.getSeatsAvailable();
        String seatsText = tfSeatsAvailable.getText();
        if (seatsText != null && !seatsText.trim().isEmpty()) {
            try {
                seats = Integer.parseInt(seatsText.trim());
            } catch (NumberFormatException e) {
                showWarn("Seats Available must be an integer.");
                return;
            }
        }

        LocalDateTime departure = buildDateTimeFromInputs();

        selected.setFlightNumber(tfNumber.getText().trim());
        selected.setOrigin(tfOrigin.getText().trim());
        selected.setDestination(tfDestination.getText().trim());
        selected.setDepartureTime(departure);
        selected.setSeatsAvailable(seats);

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

    private LocalDateTime buildDateTimeFromInputs() {
        LocalDate d = dpDepartureDate == null ? null : dpDepartureDate.getValue();
        Integer h = spHour.getValue();
        Integer m = spMinute.getValue();
        if (d == null) return null; // accepte null si l'utilisateur ne choisit pas de date
        if (h == null) h = 0;
        if (m == null) m = 0;
        return LocalDateTime.of(d, LocalTime.of(h, m));
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
        tfSeatsAvailable.clear();
        if (dpDepartureDate != null) dpDepartureDate.setValue(null);
        if (spHour != null) spHour.getValueFactory().setValue(0);
        if (spMinute != null) spMinute.getValueFactory().setValue(0);
    }

    private boolean isBlank(TextField tf) {
        return tf.getText() == null || tf.getText().trim().isEmpty();
    }

    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void applyFilters() {

        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String filter = cbFilter.getValue();

        filteredData.setPredicate(flight -> {

            if (keyword.isEmpty() || filter.equals("All"))
                return true;

            switch (filter) {
                case "Flight Number":
                    return flight.getFlightNumber() != null &&
                            flight.getFlightNumber().toLowerCase().contains(keyword);

                case "Origin":
                    return flight.getOrigin() != null &&
                            flight.getOrigin().toLowerCase().contains(keyword);

                case "Destination":
                    return flight.getDestination() != null &&
                            flight.getDestination().toLowerCase().contains(keyword);

                case "Seats Available":
                    return String.valueOf(flight.getSeatsAvailable()).contains(keyword);

                case "Departure Date":
                    if (flight.getDepartureTime() != null) {
                        String dateText = flight.getDepartureTime().toLocalDate().toString().toLowerCase();
                        return dateText.contains(keyword);
                    }
                    return false;

                default:
                    return true;
            }
        });
    }

}
