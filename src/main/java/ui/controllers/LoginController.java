package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ui.model.User;
import ui.service.UserService;
import ui.util.SceneManager;
import ui.util.SessionManager;

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

    private UserService userService;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        }
        // Initialize user service
        userService = new UserService();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String pwd = passwordField.getText();

        if (email == null || email.isBlank() || pwd == null || pwd.isBlank()) {
            showError("Please enter email and password");
            return;
        }

        // Authenticate user with database
        User user = userService.authenticate(email, pwd);

        if (user != null) {
            // Save user session
            SessionManager.getInstance().setCurrentUser(user);
            System.out.println("✅ Login successful: " + user.getFullName());

            // Navigate to dashboard
            SceneManager.switchScene("/ui/dashboard.fxml");
        } else {
            showError("Invalid email or password");
        }
    }

    @FXML
    private void handleSignUp() {
        // Navigate to signup page
        SceneManager.switchScene("/ui/signup.fxml");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #f56565;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
