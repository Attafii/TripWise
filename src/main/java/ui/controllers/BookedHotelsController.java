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
import ui.model.BookedHotelSummary;
import ui.repo.BookingRepository;

import java.io.IOException;
import java.util.List;

public class BookedHotelsController {
    @FXML private TableView<BookedHotelSummary> bookedTable;
    @FXML private TableColumn<BookedHotelSummary, String> nameColumn;
    @FXML private TableColumn<BookedHotelSummary, String> cityColumn;
    @FXML private TableColumn<BookedHotelSummary, Integer> bookingsColumn;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        bookingsColumn.setCellValueFactory(new PropertyValueFactory<>("bookings"));

        List<BookedHotelSummary> data = BookingRepository.listBookedHotels();
        ObservableList<BookedHotelSummary> items = FXCollections.observableArrayList(data);
        bookedTable.setItems(items);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/hotel/hotel-search.fxml"));
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
