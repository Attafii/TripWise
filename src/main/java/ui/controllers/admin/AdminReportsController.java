package ui.controllers.admin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.admin.repository.impl.InMemoryReservationRepository;
import ui.admin.repository.impl.InMemoryUserRepository;
import ui.admin.service.ReportService;
import ui.util.AdminExport;
import ui.util.AdminFX;

import java.io.File;

public class AdminReportsController {

    @FXML private Label lblTotalReservations, lblTotalAmount, lblActiveUsers;
    @FXML private TableView<ReportService.TopUserRow> topUsersTable;
    @FXML private TableColumn<ReportService.TopUserRow, String> colUEmail;
    @FXML private TableColumn<ReportService.TopUserRow, Number> colUCount, colUAmount;

    private final InMemoryUserRepository userRepo = new InMemoryUserRepository();
    private final InMemoryReservationRepository reservationRepo = new InMemoryReservationRepository();
    private final ReportService reportService = new ReportService();
    private final ObservableList<ReportService.TopUserRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Bind with lambdas (no PropertyValueFactory).
        colUEmail.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().email));
        colUCount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().count));
        colUAmount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().amount));

        refresh();
    }

    @FXML
    private void onRefresh() { refresh(); }

    @FXML
    private void onExportCSV() {
        File file = AdminExport.saveCSVDialog("reports.csv");
        if (file == null) return;
        AdminExport.writeTopUsersCSV(file, rows);
        AdminFX.info("Export", "CSV exported to:\n" + file.getAbsolutePath());
    }

    private void refresh() {
        var users = userRepo.findAll();
        var reservations = reservationRepo.findAll();

        lblTotalReservations.setText(String.valueOf(reportService.totalReservations(reservations)));
        lblTotalAmount.setText(String.format("%.2f", reportService.totalAmount(reservations)));
        lblActiveUsers.setText(String.valueOf(reportService.activeUsers(users)));

        rows.setAll(reportService.topUsers(reservations));
        topUsersTable.setItems(rows);
    }
}