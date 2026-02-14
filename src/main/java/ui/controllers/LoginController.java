package ui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import ui.model.User;
import ui.service.UserService;
import ui.util.SceneManager;
import ui.util.SessionManager;

public class LoginController {

    @FXML
    private StackPane rootPane;

    @FXML
    private VBox loginCard;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private StackPane loadingOverlay;

    private UserService userService;

    @FXML
    private void initialize() {
        // Initialize user service
        userService = new UserService();

        // Hide message label initially
        if (messageLabel != null) {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        }

        // Play entrance animation
        playEntranceAnimation();

        // Add input validation listeners
        setupValidation();

        // Focus on email field
        Platform.runLater(() -> emailField.requestFocus());
    }

    /**
     * Play smooth entrance animation for login card
     */
    private void playEntranceAnimation() {
        // Initial state
        loginCard.setOpacity(0);
        loginCard.setTranslateY(20);

        // Fade in and slide up animation
        FadeTransition fade = new FadeTransition(Duration.millis(500), loginCard);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(500), loginCard);
        slide.setFromY(20);
        slide.setToY(0);

        // Play animations together
        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.setInterpolator(Interpolator.EASE_OUT);
        parallel.play();
    }

    /**
     * Setup real-time input validation
     */
    private void setupValidation() {
        // Email validation
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (messageLabel.isVisible() && messageLabel.getStyleClass().contains("error-label")) {
                hideMessage();
            }
        });

        // Password validation
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (messageLabel.isVisible() && messageLabel.getStyleClass().contains("error-label")) {
                hideMessage();
            }
        });
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Input validation
        if (!validateInputs(email, password)) {
            return;
        }

        // Show loading state
        showLoading(true);
        loginButton.setDisable(true);

        // Perform authentication in background thread
        new Thread(() -> {
            try {
                // Simulate minimum loading time for smooth UX
                Thread.sleep(500);

                // Authenticate user with database
                User user = userService.authenticate(email, password);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    showLoading(false);
                    loginButton.setDisable(false);

                    if (user != null) {
                        handleSuccessfulLogin(user);
                    } else {
                        handleFailedLogin();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    loginButton.setDisable(false);
                    showError("An error occurred. Please try again.");
                    System.err.println("❌ Login error: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /**
     * Validate user inputs
     */
    private boolean validateInputs(String email, String password) {
        // Check if fields are empty
        if (email == null || email.isBlank()) {
            showError("Please enter your email address");
            shakeAnimation(emailField);
            emailField.requestFocus();
            return false;
        }

        if (password == null || password.isBlank()) {
            showError("Please enter your password");
            shakeAnimation(passwordField);
            passwordField.requestFocus();
            return false;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address");
            shakeAnimation(emailField);
            emailField.requestFocus();
            return false;
        }

        // Validate password length
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            shakeAnimation(passwordField);
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Handle successful login
     */
    private void handleSuccessfulLogin(User user) {
        // Save user session
        SessionManager.getInstance().setCurrentUser(user);
        
        // Show success message
        showSuccess("Welcome back, " + user.getFirstName() + "!");
        System.out.println("✅ Login successful: " + user.getFullName() + " (" + user.getUserType() + ")");

        // Fade out and navigate to dashboard
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loginCard);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            SceneManager.switchScene("/ui/dashboard.fxml");
        });
        fadeOut.play();
    }

    /**
     * Handle failed login
     */
    private void handleFailedLogin() {
        showError("Invalid email or password. Please try again.");
        shakeAnimation(loginCard);
        passwordField.clear();
        passwordField.requestFocus();
    }

    /**
     * Show loading overlay
     */
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
            loadingOverlay.setManaged(show);

            if (show) {
                // Fade in loading overlay
                FadeTransition fade = new FadeTransition(Duration.millis(200), loadingOverlay);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        }
    }

    /**
     * Show error message with animation
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().clear();
        messageLabel.getStyleClass().add("error-label");
        showMessage();
    }

    /**
     * Show success message with animation
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().clear();
        messageLabel.getStyleClass().add("success-label");
        showMessage();
    }

    /**
     * Show message with fade animation
     */
    private void showMessage() {
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        messageLabel.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(300), messageLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Hide message with fade animation
     */
    private void hideMessage() {
        FadeTransition fade = new FadeTransition(Duration.millis(200), messageLabel);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(event -> {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        });
        fade.play();
    }

    /**
     * Shake animation for error feedback
     */
    private void shakeAnimation(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setInterpolator(Interpolator.LINEAR);
        shake.setOnFinished(event -> node.setTranslateX(0));
        shake.play();
    }

    @FXML
    private void handleSignUp() {
        // Fade out animation
        FadeTransition fade = new FadeTransition(Duration.millis(300), loginCard);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(event -> {
            SceneManager.switchScene("/ui/signup.fxml");
        });
        fade.play();
    }
}
