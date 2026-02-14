package ui.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
<<<<<<< HEAD
import ui.util.SceneManager;
import ui.api.PaymentApiServer;
=======
>>>>>>> origin/main

public class Main extends Application {

    @Override
<<<<<<< HEAD
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        // Load the login screen
            server.start();
            System.out.println("Payment API listening on http://localhost:9090/");
        } catch (Exception e) {
            System.out.println("Payment API failed to start: " + e.getMessage());
        }

        SceneManager.switchScene("/ui/login.fxml");
        primaryStage.show();
=======
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("TripWise");
        stage.show();
>>>>>>> origin/main
    }

    public static void main(String[] args) {
        launch(args);
    }
}