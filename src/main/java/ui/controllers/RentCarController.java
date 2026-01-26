package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ui.model.Car;
import ui.service.VehiculeService;

import java.util.List;

public class RentCarController {

    @FXML
    private ComboBox<String> locationCombo;

    @FXML
    private DatePicker pickupDate;

    @FXML
    private DatePicker returnDatePicker;

    @FXML
    private FlowPane carContainer;

    private VehiculeService vehiculeService;

    @FXML
    private void initialize() {
        // Initialize vehicle service
        vehiculeService = new VehiculeService();

        locationCombo.getItems().addAll(
                "Paris", "New York", "Dubai", "London", "Los Angeles",
                "Rome", "Tokyo", "Singapore"
        );

        // Load all available cars on startup
        loadAllCars();
    }

    @FXML
    private void handleSearch() {
        carContainer.getChildren().clear();

        System.out.println("📋 Loading available vehicles...");
        List<Car> cars = vehiculeService.getAvailableCars();

        if (cars.isEmpty()) {
            showNoResultsMessage();
            return;
        }

        for (Car car : cars) {
            VBox card = createCarCard(car);
            carContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handleBackToBookings() {
        try {
            ui.util.SceneManager.switchScene("/ui/dashboard.fxml");
            System.out.println("📋 Navigating back to Bookings");
        } catch (Exception e) {
            System.err.println("❌ Error navigating to Bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all cars on initialization
     */
    private void loadAllCars() {
        System.out.println("📋 Loading all available vehicles...");
        List<Car> cars = vehiculeService.getAvailableCars();

        for (Car car : cars) {
            VBox card = createCarCard(car);
            carContainer.getChildren().add(card);
        }
    }

    private VBox createCarCard(Car car) {
        VBox card = new VBox(8);
        card.getStyleClass().add("car-card");
        card.setPadding(new Insets(12));
        card.setPrefWidth(200);

        // Car name (Brand + Model)
        Label name = new Label(car.getModel());
        name.getStyleClass().add("car-name");
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Category
        Label type = new Label(car.getType());
        type.getStyleClass().add("car-type");
        type.setStyle("-fx-text-fill: #666;");

        // Features
        StringBuilder features = new StringBuilder();
        if (car.isClimatisation()) features.append("🌡️ AC  ");
        if (car.isGps()) features.append("🗺️ GPS  ");
        if (car.isKilometrageIllimite()) features.append("∞ Unlimited KM");

        Label featuresLabel = new Label(features.toString());
        featuresLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        // Transmission & Fuel
        String specs = car.getTransmission() + " • " + car.getCarburant();
        Label specsLabel = new Label(specs);
        specsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        // Price
        Label price = new Label(String.format("$%.2f / day", car.getPricePerDay()));
        price.getStyleClass().add("car-price");
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2563eb;");

        // Company name
        if (car.getCompagnieName() != null) {
            Label company = new Label("via " + car.getCompagnieName());
            company.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
            card.getChildren().add(company);
        }

        Button rent = new Button("Rent Now");
        rent.getStyleClass().add("primary-button");
        rent.setMaxWidth(Double.MAX_VALUE);
        rent.setOnAction(e -> handleRentCar(car));

        card.getChildren().addAll(name, type, specsLabel, featuresLabel, price, rent);
        return card;
    }

    private void handleRentCar(Car car) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Car Rental");
        alert.setHeaderText("Rent " + car.getModel());
        alert.setContentText(String.format(
            "You selected:\n%s %s\nCategory: %s\nPrice: $%.2f/day\n\nBooking feature coming soon!",
            car.getMarque(), car.getModele(), car.getCategorie(), car.getPricePerDay()
        ));
        alert.showAndWait();
    }

    private void showNoResultsMessage() {
        Label noResults = new Label("No vehicles available at the moment.");
        noResults.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20px;");
        carContainer.getChildren().add(noResults);
    }
}
