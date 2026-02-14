package ui.app;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.util.SceneManager;
import ui.api.PaymentApiServer;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("TripWise - Travel Management");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(720);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        try {
            PaymentApiServer server = new PaymentApiServer(9090);
            server.start();
            System.out.println("Payment API listening on http://localhost:9090/");
        } catch (Exception e) {
            System.out.println("Payment API failed to start: " + e.getMessage());
        }

        SceneManager.switchScene("/ui/login.fxml");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

