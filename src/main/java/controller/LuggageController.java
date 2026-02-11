package controller;

import dao.LuggageDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Luggage;

public class LuggageController {
    @FXML private TableView<Luggage> luggageTable;
    @FXML private TableColumn<Luggage, Integer> colId;
    @FXML private TableColumn<Luggage, Integer> colBooking;
    @FXML private TableColumn<Luggage, Double> colWeight;
    @FXML private TableColumn<Luggage, String> colStatus;

    @FXML private TextField tfBookingId;
    @FXML private TextField tfWeight;
    @FXML private TextField tfStatus;

    private final LuggageDAO luggageDAO = new LuggageDAO();
    private final ObservableList<Luggage> data = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBooking.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    private void loadByBooking(){
        if(tfBookingId.getText().isBlank()) return;
        int bookingId = Integer.parseInt(tfBookingId.getText());
        data.setAll(luggageDAO.getLuggageByBooking(bookingId));
        luggageTable.setItems(data);
    }

    @FXML
    private void addLuggage(){
        if(tfBookingId.getText().isBlank() || tfWeight.getText().isBlank() || tfStatus.getText().isBlank()) return;
        int bookingId = Integer.parseInt(tfBookingId.getText());
        double weight = Double.parseDouble(tfWeight.getText());
        String status = tfStatus.getText();
        luggageDAO.addLuggage(new Luggage(bookingId, weight, status));
        loadByBooking();
        tfWeight.clear(); tfStatus.clear();
    }
}
