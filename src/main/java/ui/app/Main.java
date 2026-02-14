package ui.app;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.util.FlightDataInitializer;
import ui.util.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize flight mock data for testing
        try {
            FlightDataInitializer.initializeFlightData();
        } catch (Exception e) {
            System.err.println("⚠️ Could not initialize flight data: " + e.getMessage());
        }

        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("TripWise - Travel Management");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(720);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // Load the login screen
        SceneManager.switchScene("/ui/login.fxml");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

