package ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * DialogUtil - Modern confirmation and alert dialogs
 * Material Design 3 style dialogs
 */
public class DialogUtil {

    /**
     * Show confirmation dialog
     * @param title Dialog title
     * @param message Dialog message
     * @return true if user confirmed, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        
        // Customize buttons
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        // Apply custom styling
        alert.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
            "-fx-padding: 20;"
        );
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
    
    /**
     * Show delete confirmation dialog
     */
    public static boolean showDeleteConfirmation(String itemName) {
        return showConfirmation(
            "Confirm Deletion",
            "Are you sure you want to delete " + itemName + "?\n\nThis action cannot be undone."
        );
    }
    
    /**
     * Show error dialog
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        
        alert.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
            "-fx-padding: 20;"
        );
        
        alert.showAndWait();
    }
    
    /**
     * Show success dialog
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        
        alert.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
            "-fx-padding: 20;"
        );
        
        alert.showAndWait();
    }
    
    /**
     * Show warning dialog
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        
        alert.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
            "-fx-padding: 20;"
        );
        
        alert.showAndWait();
    }
    
    /**
     * Show info dialog
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        
        alert.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
            "-fx-padding: 20;"
        );
        
        alert.showAndWait();
    }
    
    /**
     * Show custom confirmation with Yes/No/Cancel options
     */
    public static Optional<ButtonType> showConfirmationWithCancel(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        
        alert.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
            "-fx-padding: 20;"
        );
        
        return alert.showAndWait();
    }
}
