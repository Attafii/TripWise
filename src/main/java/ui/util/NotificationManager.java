package ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NotificationManager - Advanced notification system
 * Supports queued notifications, different types, and customizable duration
 */
public class NotificationManager {

    private static final Queue<NotificationData> notificationQueue = new ConcurrentLinkedQueue<>();
    private static boolean isShowing = false;

    /**
     * Notification types
     */
    public enum NotificationType {
        SUCCESS("✅", "#27ae60", "#d4edda", "#155724"),
        ERROR("❌", "#e74c3c", "#f8d7da", "#721c24"),
        WARNING("⚠️", "#f39c12", "#fff3cd", "#856404"),
        INFO("ℹ️", "#3498db", "#d1ecf1", "#0c5460");

        private final String icon;
        private final String borderColor;
        private final String backgroundColor;
        private final String textColor;

        NotificationType(String icon, String borderColor, String backgroundColor, String textColor) {
            this.icon = icon;
            this.borderColor = borderColor;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
        }

        public String getIcon() { return icon; }
        public String getBorderColor() { return borderColor; }
        public String getBackgroundColor() { return backgroundColor; }
        public String getTextColor() { return textColor; }
    }

    /**
     * Notification data holder
     */
    private static class NotificationData {
        String message;
        NotificationType type;
        int durationSeconds;

        NotificationData(String message, NotificationType type, int durationSeconds) {
            this.message = message;
            this.type = type;
            this.durationSeconds = durationSeconds;
        }
    }

    /**
     * Show success notification (3 seconds)
     */
    public static void showSuccess(String message, StackPane parentPane) {
        show(message, NotificationType.SUCCESS, 3, parentPane);
    }

    /**
     * Show error notification (5 seconds)
     */
    public static void showError(String message, StackPane parentPane) {
        show(message, NotificationType.ERROR, 5, parentPane);
    }

    /**
     * Show warning notification (4 seconds)
     */
    public static void showWarning(String message, StackPane parentPane) {
        show(message, NotificationType.WARNING, 4, parentPane);
    }

    /**
     * Show info notification (3 seconds)
     */
    public static void showInfo(String message, StackPane parentPane) {
        show(message, NotificationType.INFO, 3, parentPane);
    }

    /**
     * Show notification with custom duration
     */
    public static void show(String message, NotificationType type, int durationSeconds, StackPane parentPane) {
        if (parentPane == null) {
            System.out.println("[" + type.name() + "] " + message);
            return;
        }

        NotificationData notification = new NotificationData(message, type, durationSeconds);
        notificationQueue.offer(notification);

        if (!isShowing) {
            processNextNotification(parentPane);
        }
    }

    /**
     * Process next notification in queue
     */
    private static void processNextNotification(StackPane parentPane) {
        NotificationData notification = notificationQueue.poll();
        if (notification == null) {
            isShowing = false;
            return;
        }

        isShowing = true;
        Platform.runLater(() -> displayNotification(notification, parentPane));
    }

    /**
     * Display notification with animation
     */
    private static void displayNotification(NotificationData notification, StackPane parentPane) {
        // Create notification box
        HBox notificationBox = new HBox(15);
        notificationBox.setAlignment(Pos.CENTER);
        notificationBox.setStyle(
                "-fx-background-color: " + notification.type.getBackgroundColor() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 15 25;" +
                "-fx-border-color: " + notification.type.getBorderColor() + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );

        // Icon
        Label iconLabel = new Label(notification.type.getIcon());
        iconLabel.setStyle("-fx-font-size: 24px;");

        // Message
        Label messageLabel = new Label(notification.message);
        messageLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + notification.type.getTextColor() + ";"
        );
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        notificationBox.getChildren().addAll(iconLabel, messageLabel);

        // Position at top center
        StackPane.setAlignment(notificationBox, Pos.TOP_CENTER);
        notificationBox.setTranslateY(20);

        // Add to parent pane
        parentPane.getChildren().add(notificationBox);

        // Animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition pause = new PauseTransition(Duration.seconds(notification.durationSeconds));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.setOnFinished(e -> {
            parentPane.getChildren().remove(notificationBox);
            processNextNotification(parentPane); // Process next in queue
        });

        sequence.play();
    }

    /**
     * Show confirmation dialog with callback
     */
    public static void showConfirmation(String title, String message, 
                                       StackPane parentPane,
                                       Runnable onConfirm, Runnable onCancel) {
        if (parentPane == null) {
            System.out.println("[CONFIRMATION] " + title + ": " + message);
            return;
        }

        Platform.runLater(() -> {
            // Create overlay
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

            // Create dialog box
            VBox dialogBox = new VBox(20);
            dialogBox.setAlignment(Pos.CENTER);
            dialogBox.setMaxWidth(400);
            dialogBox.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 30;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);"
            );

            // Title
            Label titleLabel = new Label(title);
            titleLabel.setStyle(
                    "-fx-font-size: 20px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #2c3e50;"
            );

            // Message
            Label messageLabel = new Label(message);
            messageLabel.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #7f8c8d;" +
                    "-fx-wrap-text: true;"
            );
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(350);

            // Buttons
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            javafx.scene.control.Button confirmBtn = new javafx.scene.control.Button("Confirm");
            confirmBtn.setStyle(
                    "-fx-background-color: #27ae60;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 10 30;" +
                    "-fx-background-radius: 5;" +
                    "-fx-cursor: hand;"
            );
            confirmBtn.setOnAction(e -> {
                parentPane.getChildren().remove(overlay);
                if (onConfirm != null) onConfirm.run();
            });

            javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Cancel");
            cancelBtn.setStyle(
                    "-fx-background-color: #95a5a6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 10 30;" +
                    "-fx-background-radius: 5;" +
                    "-fx-cursor: hand;"
            );
            cancelBtn.setOnAction(e -> {
                parentPane.getChildren().remove(overlay);
                if (onCancel != null) onCancel.run();
            });

            buttonBox.getChildren().addAll(confirmBtn, cancelBtn);
            dialogBox.getChildren().addAll(titleLabel, messageLabel, buttonBox);

            overlay.getChildren().add(dialogBox);
            parentPane.getChildren().add(overlay);

            // Fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
    }

    /**
     * Show loading indicator
     */
    public static StackPane showLoading(String message, StackPane parentPane) {
        if (parentPane == null) {
            System.out.println("[LOADING] " + message);
            return null;
        }

        // Create overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // Create loading box
        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setMaxWidth(300);
        loadingBox.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 30;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);"
        );

        // Spinner (using simple animation)
        Label spinner = new Label("⏳");
        spinner.setStyle("-fx-font-size: 48px;");

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #7f8c8d;" +
                "-fx-font-weight: bold;"
        );

        loadingBox.getChildren().addAll(spinner, messageLabel);
        overlay.getChildren().add(loadingBox);

        Platform.runLater(() -> {
            parentPane.getChildren().add(overlay);

            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        return overlay;
    }

    /**
     * Hide loading indicator
     */
    public static void hideLoading(StackPane loadingOverlay, StackPane parentPane) {
        if (loadingOverlay == null || parentPane == null) {
            return;
        }

        Platform.runLater(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> parentPane.getChildren().remove(loadingOverlay));
            fadeOut.play();
        });
    }

    /**
     * Clear notification queue
     */
    public static void clearQueue() {
        notificationQueue.clear();
        isShowing = false;
    }
}
