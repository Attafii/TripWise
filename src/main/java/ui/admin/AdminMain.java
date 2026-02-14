package ui.admin;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.util.SceneManager;

public class AdminMain extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("TripWise – Admin");
        SceneManager.switchScene("/ui/admin/admin-dashboard.fxml");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}