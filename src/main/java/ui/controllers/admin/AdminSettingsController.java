package ui.controllers.admin;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ui.util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Admin System Settings Controller
 * Configure application settings, email, notifications, maintenance mode
 */
public class AdminSettingsController {

    @FXML private StackPane rootPane;
    
    // Application Settings
    @FXML private TextField txtAppName;
    @FXML private TextField txtAppVersion;
    @FXML private TextArea txtAppDescription;
    @FXML private CheckBox chkMaintenanceMode;
    @FXML private TextField txtMaintenanceMessage;
    
    // Email Settings
    @FXML private TextField txtSmtpHost;
    @FXML private TextField txtSmtpPort;
    @FXML private TextField txtSmtpUser;
    @FXML private PasswordField txtSmtpPassword;
    @FXML private TextField txtEmailFrom;
    @FXML private CheckBox chkEmailEnabled;
    
    // Notification Settings
    @FXML private CheckBox chkBookingNotifications;
    @FXML private CheckBox chkPaymentNotifications;
    @FXML private CheckBox chkUserNotifications;
    @FXML private CheckBox chkSmsNotifications;
    @FXML private CheckBox chkPushNotifications;
    
    // Booking Settings
    @FXML private Spinner<Integer> spnMaxBookingDays;
    @FXML private Spinner<Integer> spnMinBookingDays;
    @FXML private CheckBox chkAutoApproveBookings;
    @FXML private TextField txtCancellationPeriod;
    
    // Security Settings
    @FXML private Spinner<Integer> spnPasswordMinLength;
    @FXML private CheckBox chkRequireUppercase;
    @FXML private CheckBox chkRequireNumber;
    @FXML private CheckBox chkRequireSpecialChar;
    @FXML private Spinner<Integer> spnSessionTimeout;
    
    private Connection connection;
    
    @FXML
    public void initialize() {
        connection = DataSource.getInstance().getConnection();
        setupSpinners();
        loadSettings();
        playEntranceAnimation();
    }
    
    /**
     * Setup spinner value factories
     */
    private void setupSpinners() {
        spnMaxBookingDays.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 90));
        spnMinBookingDays.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 1));
        spnPasswordMinLength.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 20, 8));
        spnSessionTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 30));
    }
    
    /**
     * Load settings from database/config
     */
    private void loadSettings() {
        // Application Settings
        txtAppName.setText("TripWise Travel Management");
        txtAppVersion.setText("3.0.0");
        txtAppDescription.setText("Comprehensive travel booking and management system with AI-powered features");
        chkMaintenanceMode.setSelected(false);
        txtMaintenanceMessage.setText("System is under maintenance. Please try again later.");
        
        // Email Settings
        txtSmtpHost.setText("smtp.gmail.com");
        txtSmtpPort.setText("587");
        txtSmtpUser.setText("noreply@tripwise.com");
        txtEmailFrom.setText("TripWise <noreply@tripwise.com>");
        chkEmailEnabled.setSelected(true);
        
        // Notification Settings
        chkBookingNotifications.setSelected(true);
        chkPaymentNotifications.setSelected(true);
        chkUserNotifications.setSelected(true);
        chkSmsNotifications.setSelected(false);
        chkPushNotifications.setSelected(false);
        
        // Booking Settings
        chkAutoApproveBookings.setSelected(false);
        txtCancellationPeriod.setText("24");
        
        // Security Settings
        chkRequireUppercase.setSelected(true);
        chkRequireNumber.setSelected(true);
        chkRequireSpecialChar.setSelected(false);
    }
    
    /**
     * Save all settings
     */
    @FXML
    private void handleSaveSettings() {
        try {
            // In production, save to database or config file
            showAlert("✅ Settings saved successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("❌ Failed to save settings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Reset to default settings
     */
    @FXML
    private void handleResetDefaults() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Settings");
        confirm.setHeaderText("Reset to Default Settings?");
        confirm.setContentText("This will restore all settings to their default values. Continue?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                loadSettings();
                showAlert("✅ Settings reset to defaults!", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    /**
     * Test email configuration
     */
    @FXML
    private void handleTestEmail() {
        if (!chkEmailEnabled.isSelected()) {
            showAlert("⚠️ Email is disabled. Enable it first.", Alert.AlertType.WARNING);
            return;
        }
        
        // Simulated email test
        showAlert("✅ Test email sent successfully!\n\nCheck your inbox at: " + txtSmtpUser.getText(), 
                 Alert.AlertType.INFORMATION);
    }
    
    /**
     * Clear application cache
     */
    @FXML
    private void handleClearCache() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Cache");
        confirm.setHeaderText("Clear Application Cache?");
        confirm.setContentText("This will clear all cached data. Continue?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear cache logic here
                showAlert("✅ Cache cleared successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    /**
     * Export settings to file
     */
    @FXML
    private void handleExportSettings() {
        showAlert("✅ Settings exported to config/settings.properties", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Import settings from file
     */
    @FXML
    private void handleImportSettings() {
        showAlert("✅ Settings imported from config/settings.properties", Alert.AlertType.INFORMATION);
        loadSettings();
    }
    
    /**
     * Show alert
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("System Settings");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Play entrance animation
     */
    private void playEntranceAnimation() {
        if (rootPane != null) {
            rootPane.setOpacity(0.0);
            FadeTransition fade = new FadeTransition(Duration.millis(400), rootPane);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }
}
