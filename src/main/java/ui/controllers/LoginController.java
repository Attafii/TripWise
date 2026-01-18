package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ui.util.SceneManager;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

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
    private void handleLogin() {
        String email = emailField.getText();
        String pwd = passwordField.getText();

        if (email == null || email.isBlank() || pwd == null || pwd.isBlank()) {
            messageLabel.setText("Please enter email and password");
            messageLabel.setStyle("-fx-text-fill: #f56565;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return;
        }

        // Mock successful login
        SceneManager.switchScene("/ui/dashboard.fxml");
    }

    @FXML
    private void handleSignUp() {
        // Navigate to signup page
        SceneManager.switchScene("/ui/signup.fxml");
    }
}
