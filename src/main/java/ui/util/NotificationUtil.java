package ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * NotificationUtil - Toast-style notifications for user feedback
 * Material Design 3 snackbar implementation
 */
public class NotificationUtil {

    public enum NotificationType {
        SUCCESS, ERROR, WARNING, INFO
    }

    /**
     * Show a toast notification
     * @param message The message to display
     * @param type The type of notification (success, error, warning, info)
     * @param parent The parent StackPane to attach the notification to
     */
    public static void showNotification(String message, NotificationType type, StackPane parent) {
        // Create notification container
        VBox notification = new VBox();
        notification.setAlignment(Pos.CENTER);
        notification.setMaxWidth(400);
        notification.setMinHeight(50);
        
        // Style based on type
        String backgroundColor;
        String textColor = "#ffffff";
        String icon;
        
        switch (type) {
            case SUCCESS:
                backgroundColor = "#10b981"; // Green
                icon = "✓ ";
                break;
            case ERROR:
                backgroundColor = "#ef4444"; // Red
                icon = "✕ ";
                break;
            case WARNING:
                backgroundColor = "#f59e0b"; // Amber
                icon = "⚠ ";
                break;
            case INFO:
            default:
                backgroundColor = "#3b82f6"; // Blue
                icon = "ℹ ";
                break;
        }
        
        notification.setStyle(
            "-fx-background-color: " + backgroundColor + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-padding: 15 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        // Create message label
        Label messageLabel = new Label(icon + message);
        messageLabel.setStyle(
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-wrap-text: true;"
        );
        messageLabel.setMaxWidth(380);
        
        notification.getChildren().add(messageLabel);
        
        // Position at top-center
        StackPane.setAlignment(notification, Pos.TOP_CENTER);
        notification.setTranslateY(-100); // Start above the view
        
        parent.getChildren().add(notification);
        
        // Slide down animation
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), notification);
        slideDown.setFromY(-100);
        slideDown.setToY(20);
        
        // Pause for 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        
        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> parent.getChildren().remove(notification));
        
        // Play animations in sequence
        SequentialTransition sequence = new SequentialTransition(slideDown, pause, fadeOut);
        sequence.play();
    }
    
    /**
     * Show success notification
     */
    public static void showSuccess(String message, StackPane parent) {
        showNotification(message, NotificationType.SUCCESS, parent);
    }
    
    /**
     * Show error notification
     */
    public static void showError(String message, StackPane parent) {
        showNotification(message, NotificationType.ERROR, parent);
    }
    
    /**
     * Show warning notification
     */
    public static void showWarning(String message, StackPane parent) {
        showNotification(message, NotificationType.WARNING, parent);
    }
    
    /**
     * Show info notification
     */
    public static void showInfo(String message, StackPane parent) {
        showNotification(message, NotificationType.INFO, parent);
    }
}
