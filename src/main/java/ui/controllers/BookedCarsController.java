package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import ui.model.BookedCarSummary;
import ui.repo.BookingRepository;

import java.io.IOException;
import java.util.List;

public class BookedCarsController {
    @FXML private TableView<BookedCarSummary> bookedTable;
    @FXML private TableColumn<BookedCarSummary, String> brandColumn;
    @FXML private TableColumn<BookedCarSummary, String> modelColumn;
    @FXML private TableColumn<BookedCarSummary, Integer> bookingsColumn;

    @FXML
    private void initialize() {
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        bookingsColumn.setCellValueFactory(new PropertyValueFactory<>("bookings"));

        List<BookedCarSummary> data = BookingRepository.listBookedCars();
        ObservableList<BookedCarSummary> items = FXCollections.observableArrayList(data);
        bookedTable.setItems(items);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/car/car-search.fxml"));
            Parent view = loader.load();
            BorderPane outer = (BorderPane) bookedTable.getScene().getRoot();
            var centerNode = outer.getCenter();
            if (centerNode instanceof BorderPane) {
                ((BorderPane) centerNode).setCenter(view);
            } else {
                outer.setCenter(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
