package ui.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static SceneManager instance;
    private static Stage primaryStage;
    private static final String STYLE_PATH = "/ui/style.css";

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Switch scene by FXML resource path (e.g. "/ui/login.fxml").
     */
    public static void switchScene(String fxmlResource) {
        try {
            System.out.println("Switching to scene: " + fxmlResource);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlResource));
            if (SceneManager.class.getResource(fxmlResource) == null) {
                System.err.println("Error: FXML resource not found: " + fxmlResource);
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);

            try {
                if (SceneManager.class.getResource(STYLE_PATH) != null) {
                    String css = SceneManager.class.getResource(STYLE_PATH).toExternalForm();
                    scene.getStylesheets().add(css);
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not load stylesheet: " + e.getMessage());
            }

            primaryStage.setScene(scene);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlResource);
            e.printStackTrace();
        }
    }

    // Navigation methods
    public void switchToBookFlightNew() {
        switchScene("/ui/book-flight-new.fxml");
    }

    public void switchToFlightAnalytics() {
        switchScene("/ui/flight-analytics.fxml");
    }

    public void switchToTravelerBookings() {
        switchScene("/ui/traveler-bookings.fxml");
    }

    public void switchToDashboard() {
        switchScene("/ui/dashboard.fxml");
    }

    public void switchToTravelerDashboard() {
        switchScene("/ui/traveler-dashboard.fxml");
    }
}
