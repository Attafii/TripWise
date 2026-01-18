package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ui.model.Car;

import java.util.Arrays;
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

    @FXML
    private void initialize() {
        locationCombo.getItems().addAll(
                "New York Airport", "Los Angeles Downtown", "Miami Beach",
                "Chicago O'Hare", "San Francisco Bay"
        );
    }

    @FXML
    private void handleSearch() {
        carContainer.getChildren().clear();

        List<Car> cars = Arrays.asList(
                new Car("Toyota Camry", "Sedan", 45.99),
                new Car("Honda CR-V", "SUV", 65.99),
                new Car("Ford Mustang", "Sports", 89.99),
                new Car("Tesla Model 3", "Electric", 99.99),
                new Car("Jeep Wrangler", "Off-Road", 79.99)
        );

        for (Car car : cars) {
            VBox card = createCarCard(car);
            carContainer.getChildren().add(card);
        }
    }

    private VBox createCarCard(Car car) {
        VBox card = new VBox(8);
        card.getStyleClass().add("car-card");
        card.setPadding(new Insets(12));

        Label name = new Label(car.getModel());
        name.getStyleClass().add("car-name");

        Label type = new Label(car.getType());
        type.getStyleClass().add("car-type");

        Label price = new Label(String.format("$%.2f / day", car.getPricePerDay()));
        price.getStyleClass().add("car-price");

        Button rent = new Button("Rent Now");
        rent.getStyleClass().add("primary-button");

        card.getChildren().addAll(name, type, price, rent);
        return card;
    }
}

