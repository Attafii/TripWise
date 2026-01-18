package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private void initialize() {
        nameField.setText("John Doe");
        emailField.setText("john.doe@tripwise.com");
        phoneField.setText("+1 (555) 123-4567");
    }

    @FXML
    private void handleSaveChanges() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Profile");
        a.setHeaderText(null);
        a.setContentText("Profile updated successfully (mock)!");
        a.showAndWait();
    }
}

