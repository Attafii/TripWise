package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import ui.model.Car;
import ui.model.CarRental;
import ui.model.Payment;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CarRentalController {

    // --- Search View Fields ---
    @FXML private ComboBox<String> locationCombo;
    @FXML private DatePicker pickupDate;
    @FXML private DatePicker returnDatePicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TableView<Car> carTable;
    @FXML private TableColumn<Car, String> brandColumn;
    @FXML private TableColumn<Car, String> modelColumn;
    @FXML private TableColumn<Car, String> typeColumn;
    @FXML private TableColumn<Car, Double> priceColumn;
    @FXML private TableColumn<Car, String> transmissionColumn;

    // --- Details View Fields ---
    @FXML private Label carNameLabel;
    @FXML private Label carTypeLabel;
    @FXML private Label carSpecsLabel;
    @FXML private Label carPriceLabel;

    // --- Booking View Fields ---
    @FXML private Label rentalCarLabel;
    @FXML private Label rentalLocationLabel;
    @FXML private Label rentalDatesLabel;
    @FXML private Label rentalPriceLabel;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private Label bookingStatusLabel;
    @FXML private Button confirmButton;

    // --- State & Data ---
    private static ObservableList<Car> allCars;
    private static Car selectedCar;
    private static CarRental currentRental;
    
    // Persist search selections across views
    private static String lastLocation;
    private static LocalDate lastPickupDate;
    private static LocalDate lastReturnDate;

    @FXML
    private void initialize() {
        if (allCars == null) {
            initializeSampleData();
        }

        if (carTable != null) {
            setupSearchTable();
        }

        if (carNameLabel != null && selectedCar != null) {
            setupDetailsView();
        }

        if (rentalCarLabel != null && currentRental != null) {
            setupBookingView();
        }
    }
    
    public static void resetState() {
        selectedCar = null;
        currentRental = null;
    }

    private void initializeSampleData() {
        allCars = FXCollections.observableArrayList();
        allCars.add(new Car("Toyota", "Camry", "Sedan", 5, "Automatic", "Petrol", 50.0));
        allCars.add(new Car("Honda", "CR-V", "SUV", 5, "Automatic", "Hybrid", 75.0));
        allCars.add(new Car("Ford", "Mustang", "Convertible", 4, "Manual", "Petrol", 120.0));
        allCars.add(new Car("Tesla", "Model 3", "Sedan", 5, "Automatic", "Electric", 90.0));
        allCars.add(new Car("Chevrolet", "Suburban", "SUV", 7, "Automatic", "Diesel", 150.0));
    }

    private void setupSearchTable() {
        locationCombo.getItems().addAll("New York Airport", "Los Angeles Downtown", "Miami Beach", "Chicago O'Hare", "San Francisco Bay");
        typeCombo.getItems().addAll("All", "Sedan", "SUV", "Convertible");
        typeCombo.setValue("All");

        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        transmissionColumn.setCellValueFactory(new PropertyValueFactory<>("transmission"));

        carTable.setItems(allCars);

        carTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && carTable.getSelectionModel().getSelectedItem() != null) {
                selectedCar = carTable.getSelectionModel().getSelectedItem();
                navigateTo("/ui/car/car-details.fxml");
            }
        });
    }

    private void setupDetailsView() {
        carNameLabel.setText(selectedCar.getFullName());
        carTypeLabel.setText(selectedCar.getType());
        carSpecsLabel.setText("Seats: " + selectedCar.getSeats() + " | Transmission: " + selectedCar.getTransmission() + " | Fuel: " + selectedCar.getFuelType());
        carPriceLabel.setText("$" + selectedCar.getPricePerDay() + "/day");
    }

    private void setupBookingView() {
        rentalCarLabel.setText("Vehicle: " + currentRental.getCar().getFullName());
        rentalLocationLabel.setText("Pick-up: " + currentRental.getPickUpLocation());
        rentalDatesLabel.setText("Dates: " + currentRental.getPickUpDate() + " to " + currentRental.getReturnDate());
        rentalPriceLabel.setText("Total Price: $" + String.format("%.2f", currentRental.getTotalPrice()));
        bookingStatusLabel.setText("Status: " + currentRental.getStatus());
    }

    @FXML
    private void handleSearch() {
        String type = typeCombo.getValue();
        
        // Persist criteria
        lastLocation = locationCombo.getValue();
        lastPickupDate = pickupDate.getValue();
        lastReturnDate = returnDatePicker.getValue();
        
        List<Car> filtered = allCars.stream()
                .filter(c -> type == null || type.equals("All") || c.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        carTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleRentNow() {
        LocalDate start = lastPickupDate != null ? lastPickupDate : LocalDate.now().plusDays(1);
        LocalDate end = lastReturnDate != null ? lastReturnDate : LocalDate.now().plusDays(3);
        String loc = lastLocation != null ? lastLocation : "Default Location";
        
        currentRental = new CarRental(selectedCar, loc, start, end);
        navigateTo("/ui/car/car-booking.fxml");
    }

    @FXML
    private void handleConfirmRental() {
        Payment payment = new Payment(cardNumberField.getText(), expiryDateField.getText(), cvvField.getText());
        if (payment.processPayment()) {
            currentRental.setStatus(CarRental.Status.CONFIRMED);
            bookingStatusLabel.setText("Status: CONFIRMED! Rental ID: " + currentRental.getRentalId());
            bookingStatusLabel.setStyle("-fx-text-fill: green;");
            confirmButton.setDisable(true);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rental Confirmed");
            alert.setHeaderText("Success!");
            alert.setContentText("Your rental has been confirmed. ID: " + currentRental.getRentalId());
            alert.showAndWait();
        } else {
            bookingStatusLabel.setText("Payment Failed.");
            bookingStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleBackToSearch() {
        navigateTo("/ui/car/car-search.fxml");
    }

    @FXML
    private void handleBackToDetails() {
        navigateTo("/ui/car/car-details.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            if (carTable != null && carTable.getScene() != null) {
                 Parent root = carTable.getScene().getRoot();
                 if (root instanceof BorderPane) {
                     BorderPane outer = (BorderPane) root;
                     var centerNode = outer.getCenter();
                     if (centerNode instanceof BorderPane) {
                         ((BorderPane) centerNode).setCenter(view);
                     } else {
                         outer.setCenter(view);
                     }
                 }
            } else if (carNameLabel != null && carNameLabel.getScene() != null) {
                Parent root = carNameLabel.getScene().getRoot();
                if (root instanceof BorderPane) {
                    BorderPane outer = (BorderPane) root;
                    var centerNode = outer.getCenter();
                    if (centerNode instanceof BorderPane) {
                        ((BorderPane) centerNode).setCenter(view);
                    } else {
                        outer.setCenter(view);
                    }
                }
            } else if (rentalCarLabel != null && rentalCarLabel.getScene() != null) {
                 Parent root = rentalCarLabel.getScene().getRoot();
                 if (root instanceof BorderPane) {
                     BorderPane outer = (BorderPane) root;
                     var centerNode = outer.getCenter();
                     if (centerNode instanceof BorderPane) {
                         ((BorderPane) centerNode).setCenter(view);
                     } else {
                         outer.setCenter(view);
                     }
                 }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
