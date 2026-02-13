package com.airline;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tracking.GPSReceiver;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        GPSReceiver.start();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setTitle("Airline System V2");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
