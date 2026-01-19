package controller;

import dao.BookingDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Booking;

public class BookingController {
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> colId;
    @FXML private TableColumn<Booking, String> colPassenger;
    @FXML private TableColumn<Booking, Integer> colFlightId;
    @FXML private TableColumn<Booking, String> colSeat;

    @FXML private TextField tfPassenger;
    @FXML private TextField tfFlightId;
    @FXML private TextField tfSeat;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ObservableList<Booking> data = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        colFlightId.setCellValueFactory(new PropertyValueFactory<>("flightId"));
        colSeat.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        refresh();
    }

    @FXML
    private void addBooking(){
        if(tfPassenger.getText().isBlank() || tfFlightId.getText().isBlank() || tfSeat.getText().isBlank()) return;
        int flightId = Integer.parseInt(tfFlightId.getText());
        Booking b = new Booking(tfPassenger.getText(), flightId, tfSeat.getText());
        bookingDAO.addBooking(b);
        tfPassenger.clear(); tfFlightId.clear(); tfSeat.clear();
        refresh();
    }
    @FXML
    private void delBooking(){
        if(tfPassenger.getText().isBlank() || tfFlightId.getText().isBlank() || tfSeat.getText().isBlank()) return;
        int flightId = Integer.parseInt(tfFlightId.getText());
        Booking b = new Booking(tfPassenger.getText(), flightId, tfSeat.getText());
        bookingDAO.addBooking(b);
        tfPassenger.clear(); tfFlightId.clear(); tfSeat.clear();
        refresh();
    }
    @FXML
    private void updateBooking(){
        if(tfPassenger.getText().isBlank() || tfFlightId.getText().isBlank() || tfSeat.getText().isBlank()) return;
        int flightId = Integer.parseInt(tfFlightId.getText());
        Booking b = new Booking(tfPassenger.getText(), flightId, tfSeat.getText());
        bookingDAO.addBooking(b);
        tfPassenger.clear(); tfFlightId.clear(); tfSeat.clear();
        refresh();
    }

    private void refresh(){
        data.setAll(bookingDAO.getAllBookings());
        bookingsTable.setItems(data);
    }
}
