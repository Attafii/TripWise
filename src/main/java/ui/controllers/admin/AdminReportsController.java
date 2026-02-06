package ui.controllers.admin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.admin.repository.impl.MySqlReservationRepository ;
import ui.admin.repository.impl.MySqlUserRepository ;
import ui.admin.service.ReportService;
import ui.util.AdminExport;
import ui.util.AdminFX;

import java.io.File;

public class AdminReportsController {

    @FXML private Label lblTotalReservations, lblTotalAmount, lblActiveUsers;
    @FXML private TableView<ReportService.TopUserRow> topUsersTable;
    @FXML private TableColumn<ReportService.TopUserRow, String> colUEmail;
    @FXML private TableColumn<ReportService.TopUserRow, Number> colUCount, colUAmount;

    // Use MySQL repositories so the view doesn't fail due to missing in-memory classes
    private final MySqlUserRepository userRepo = new MySqlUserRepository();
    private final MySqlReservationRepository reservationRepo = new MySqlReservationRepository();

    private final ReportService reportService = new ReportService();
    private final ObservableList<ReportService.TopUserRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colUEmail.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().email));
        colUCount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().count));
        colUAmount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().amount));

        // Protect refresh so the view renders even if DB has issues
        try {
            refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            AdminFX.warn("Reports", "Could not load reports.\nCause: " + ex.getMessage());
            topUsersTable.setItems(FXCollections.observableArrayList());
        }
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
        var users = userRepo.findAll();               // hits DB
        var reservations = reservationRepo.findAll(); // hits DB

        lblTotalReservations.setText(String.valueOf(reportService.totalReservations(reservations)));
        lblTotalAmount.setText(String.format("%.2f", reportService.totalAmount(reservations)));
        lblActiveUsers.setText(String.valueOf(reportService.activeUsers(users)));

        rows.setAll(reportService.topUsers(reservations));
        topUsersTable.setItems(rows);
    }
}