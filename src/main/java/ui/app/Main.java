package ui.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // DEBUG: print the URL we find for the FXML
        URL url = getClass().getResource("/ui/dashboard.fxml");
        System.out.println("Dashboard URL = " + url); // should NOT be null

        if (url == null) {
            throw new IllegalStateException("Cannot find /ui/dashboard.fxml on the classpath");
        }

        FXMLLoader fxml = new FXMLLoader(url);
        Scene scene = new Scene(fxml.load());
        stage.setTitle("TripWise");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}