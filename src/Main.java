
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.util.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
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