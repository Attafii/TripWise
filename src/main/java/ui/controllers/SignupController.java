package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ui.util.SceneManager;

public class SignupController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        }
    }

    @FXML
    private void handleSignup() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (name == null || name.isBlank()) {
            showError("Please enter your full name");
            return;
        }

        if (email == null || email.isBlank()) {
            showError("Please enter your email address");
            return;
        }

        if (password == null || password.isBlank()) {
            showError("Please enter a password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        // Mock successful signup - go to dashboard
        SceneManager.switchScene("/ui/dashboard.fxml");
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.switchScene("/ui/login.fxml");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
