package ui.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.admin.model.Reservation;
import ui.admin.repository.impl.InMemoryReservationRepository;
import ui.admin.service.ReservationService;
import ui.util.AdminFX;

public class AdminReservationsController {

    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> colResId, colType, colUser, colStatus, colDate;
    @FXML private TableColumn<Reservation, Number> colAmount;

    private final ReservationService service = new ReservationService(new InMemoryReservationRepository());
    private final ObservableList<Reservation> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colResId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAmount.setCellValueFactory(c -> AdminFX.readOnlyNumber(c.getValue().getAmount()));
        colDate.setCellValueFactory(c -> AdminFX.readOnlyString(String.valueOf(c.getValue().getCreatedAt())));
        refresh();
    }

    @FXML
    private void onVerify() {
        Reservation r = reservationsTable.getSelectionModel().getSelectedItem();
        if (r == null) return;
        service.verify(r);
        refresh();
    }

    @FXML
    private void onCancel() {
        Reservation r = reservationsTable.getSelectionModel().getSelectedItem();
        if (r == null) return;
        service.cancel(r);
        refresh();
    }

    @FXML
    private void onRefresh() { refresh(); }

    private void refresh() {
        data.setAll(service.all());
        reservationsTable.setItems(data);
    }
}
