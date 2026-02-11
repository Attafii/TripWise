package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController {
    @FXML
    private void openFlights() throws Exception {
        openWindow("/fxml/Flights.fxml", "Flights", 700, 450);
    }

    @FXML
    private void openBookings() throws Exception {
        openWindow("/fxml/Bookings.fxml", "Bookings", 800, 500);
    }

    @FXML
    private void openLuggage() throws Exception {
        openWindow("/fxml/Luggage.fxml", "Luggage", 700, 450);
    }

    private void openWindow(String fxml, String title, int w, int h) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage s = new Stage();
        s.setTitle(title);
        s.setScene(new Scene(root, w, h));
        s.show();
    }
}
