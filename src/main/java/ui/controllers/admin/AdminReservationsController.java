package ui.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import ui.admin.repository.impl.MySqlReservationRepository;
import ui.model.Reservation;
import ui.util.AdminFX;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminReservationsController {

    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> colResId, colType, colUser, colStatus, colDate;
    @FXML private TableColumn<Reservation, Number> colAmount;

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

    private final ui.service.ReservationService service =
            new ui.service.ReservationService(new MySqlReservationRepository());

    private final ObservableList<Reservation> data = FXCollections.observableArrayList();
    private FilteredList<Reservation> filtered;
    private SortedList<Reservation> sorted;

    @FXML
    private void initialize() {
        colResId.setCellValueFactory(c -> AdminFX.readOnlyString(nullSafe(c.getValue().getId())));
        colType.setCellValueFactory(c -> AdminFX.readOnlyString(nullSafe(c.getValue().getType())));
        colUser.setCellValueFactory(c -> AdminFX.readOnlyString(nullSafe(c.getValue().getUserEmail())));
        colAmount.setCellValueFactory(c ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue().getAmount()));
        colDate.setCellValueFactory(c -> {
            LocalDateTime dt = c.getValue().getCreatedAt();
            return AdminFX.readOnlyString(dt == null ? "" : dt.toString());
        });

        // Status chip renderer
        colStatus.setCellValueFactory(c -> AdminFX.readOnlyString(nullSafe(c.getValue().getStatus())));
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label chip = new Label();
            private final StackPane wrapper = new StackPane(chip);
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                String s = status.toUpperCase();
                chip.setText(s);
                chip.getStyleClass().setAll("chip");
                switch (s) {
                    case "PAID"      -> chip.getStyleClass().add("chip-paid");
                    case "PENDING"   -> chip.getStyleClass().add("chip-pending");
                    case "CANCELLED" -> chip.getStyleClass().add("chip-cancelled");
                    default          -> chip.getStyleClass().add("chip-pending");
                }
                setGraphic(wrapper);
                setText(null);
            }
        });

        // Filter/sort
        filtered = new FilteredList<>(data, r -> true);
        sorted   = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(reservationsTable.comparatorProperty());
        reservationsTable.setItems(sorted);

        if (fromDate != null) fromDate.valueProperty().addListener((o, old, v) -> applyFilters());
        if (toDate   != null) toDate.valueProperty().addListener((o, old, v) -> applyFilters());

        refresh();
    }

    @FXML private void onVerify() {
        var r = reservationsTable.getSelectionModel().getSelectedItem();
        if (r == null) return; service.verify(r); refresh();
    }
    @FXML private void onCancel() {
        var r = reservationsTable.getSelectionModel().getSelectedItem();
        if (r == null) return; service.cancel(r); refresh();
    }
    @FXML private void onRefresh() { refresh(); }

    private void refresh() {
        data.setAll(service.all());
        applyFilters();
    }
    private void applyFilters() {
        LocalDate from = fromDate == null ? null : fromDate.getValue();
        LocalDate to   = toDate   == null ? null : toDate.getValue();
        filtered.setPredicate(r -> {
            LocalDate d = r.getCreatedAt() == null ? null : r.getCreatedAt().toLocalDate();
            if (from != null && (d == null || d.isBefore(from))) return false;
            if (to   != null && (d == null || d.isAfter(to)))   return false;
            return true;
        });
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}