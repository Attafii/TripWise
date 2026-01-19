package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import ui.util.SceneManager;

import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label titleLabel;

    @FXML
    public void initialize() {
        titleLabel.setText("Home");
    }

    @FXML
    private void onHome() {
        titleLabel.setText("Home");
        loadCenterView("/ui/home-center.fxml");
    }

    @FXML
    private void onBookHotel() {
        titleLabel.setText("Book Hotel");
        try { HotelController.resetState(); } catch (Throwable ignored) {}
        loadCenterView("/ui/hotel/hotel-search.fxml");
    }

    @FXML
    private void onBookFlight() {
        titleLabel.setText("Book Flight");
        loadCenterView("/ui/book-flight.fxml");
    }

    @FXML
    private void onRentCar() {
        titleLabel.setText("Rent Car");
        try { CarRentalController.resetState(); } catch (Throwable ignored) {}
        loadCenterView("/ui/car/car-search.fxml");
    }

    @FXML
    private void onProfile() {
        titleLabel.setText("Profile");
        loadCenterView("/ui/profile.fxml");
    }

    @FXML
    private void onLogout() {
        SceneManager.switchScene("/ui/login.fxml");
    }

    private void loadCenterView(String resource) {
        try {
            var url = getClass().getResource(resource);
            if (url == null) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Navigation Error");
                a.setHeaderText("View not found");
                a.setContentText("Could not load: " + resource);
                a.showAndWait();
                return;
            }
            Pane view = FXMLLoader.load(url);
            rootPane.setCenter(view);
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Navigation Error");
            a.setHeaderText("Failed to load view");
            a.setContentText("Error loading: " + resource + "\n" + e.getMessage());
            a.showAndWait();
        }
    }
}

