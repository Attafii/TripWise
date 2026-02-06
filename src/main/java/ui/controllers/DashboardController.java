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
        loadCenterView("/ui/book-hotel.fxml");
    }

    @FXML
    private void onBookFlight() {
        titleLabel.setText("Book Flight");
        loadCenterView("/ui/book-flight.fxml");
    }

    @FXML
    private void onRentCar() {
        titleLabel.setText("Rent Car");
        loadCenterView("/ui/rent-car.fxml");
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

    @FXML
    private void openRemboursements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/approvals.fxml"));
            Pane view = loader.load();
            rootPane.setCenter(view);
            titleLabel.setText("Remboursements");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load module: " + e.getMessage()).showAndWait();
        }
    }

    private void loadCenterView(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Pane view = loader.load();
            rootPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}