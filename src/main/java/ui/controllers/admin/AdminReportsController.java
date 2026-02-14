package ui.controllers.admin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import ui.admin.repository.impl.MySqlReservationRepository;
import ui.admin.repository.impl.MySqlUserRepository;
import ui.model.Reservation;
import ui.model.User;
import ui.service.ReportService;
import ui.util.AdminExport;
import ui.util.AdminFX;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AdminReportsController {

    @FXML private Label lblTotalReservations, lblTotalAmount, lblActiveUsers;
    @FXML private TableView<ReportService.TopUserRow> topUsersTable;
    @FXML private TableColumn<ReportService.TopUserRow, String>  colUEmail;
    @FXML private TableColumn<ReportService.TopUserRow, Number>  colUCount, colUAmount;

    // Date range pickers
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

    // Charts
    @FXML private BarChart<String, Number> barTopUsersAmount;
    @FXML private LineChart<String, Number> lineResPerDay;

    // Repos (DB)
    private final MySqlUserRepository userRepo = new MySqlUserRepository();
    private final MySqlReservationRepository reservationRepo = new MySqlReservationRepository();

    private final ReportService reportService = new ReportService();
    private final ObservableList<ReportService.TopUserRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Table columns
        colUEmail.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().email));
        colUCount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().count));
        colUAmount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().amount));

        // Date listeners
        if (fromDate != null) fromDate.valueProperty().addListener((o, old, v) -> refresh());
        if (toDate   != null) toDate.valueProperty().addListener((o, old, v) -> refresh());

        // Initial load
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
        LocalDate from = fromDate == null ? null : fromDate.getValue();
        LocalDate to   = toDate   == null ? null : toDate.getValue();

        Task<ReportData> task = new Task<>() {
            @Override protected ReportData call() {
                // 1) Fetch all
                List<User> users = userRepo.findAll();
                List<Reservation> reservations = reservationRepo.findAll();

                // 2) Filter by date range (inclusive)
                if (from != null || to != null) {
                    reservations = reservations.stream()
                            .filter(r -> {
                                LocalDateTime cAt = r.getCreatedAt();
                                if (cAt == null) return false;
                                LocalDate d = cAt.toLocalDate();
                                if (from != null && d.isBefore(from)) return false;
                                if (to   != null && d.isAfter(to))     return false;
                                return true;
                            })
                            .toList();
                }

                // 3) KPIs
                long   totalRes    = reportService.totalReservations(reservations);
                double totalAmount = reportService.totalAmount(reservations);
                long   activeUsers = reportService.activeUsers(users);

                // 4) Top users
                List<ReportService.TopUserRow> topUsers = reportService.topUsers(reservations);

                // 5) Data for charts
                XYChart.Series<String, Number> barSeries = buildTopUsersAmountSeries(topUsers);
                XYChart.Series<String, Number> lineSeries = buildResPerDaySeries(reservations);

                return new ReportData(totalRes, totalAmount, activeUsers, topUsers, barSeries, lineSeries);
            }
        };

        task.setOnSucceeded(e -> {
            ReportData r = task.getValue();

            // KPIs
            lblTotalReservations.setText(String.valueOf(r.totalRes));
            lblTotalAmount.setText(String.format("%.2f", r.totalAmount));
            lblActiveUsers.setText(String.valueOf(r.activeUsers));

            // Table
            rows.setAll(r.topUsers);
            topUsersTable.setItems(rows);

            // Charts
            barTopUsersAmount.getData().setAll(r.barSeries);
            barTopUsersAmount.setLegendVisible(false);

            lineResPerDay.getData().setAll(r.lineSeries);
            lineResPerDay.setLegendVisible(false);
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            AdminFX.warn("Reports", "Could not compute reports.\n\n"
                    + ui.util.AdminFX.fullCauseMessage(task.getException()));
            rows.clear();
            topUsersTable.setItems(rows);

            barTopUsersAmount.getData().clear();
            lineResPerDay.getData().clear();

            lblTotalReservations.setText("0");
            lblTotalAmount.setText("0.00");
            lblActiveUsers.setText("0");
        });

        new Thread(task, "compute-reports").start();
    }

    // ---- Helpers to build chart series ----

    private XYChart.Series<String, Number> buildTopUsersAmountSeries(List<ReportService.TopUserRow> top) {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        // Limit to top 10 for readability
        top.stream()
                .sorted(Comparator.comparingDouble((ReportService.TopUserRow r) -> r.amount).reversed())
                .limit(10)
                .forEach(r -> s.getData().add(new XYChart.Data<>(r.email, r.amount)));
        return s;
    }

    private XYChart.Series<String, Number> buildResPerDaySeries(List<Reservation> reservations) {
        Map<String, Long> perDay = reservations.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().toLocalDate().toString(),
                        TreeMap::new, // keep days sorted
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        perDay.forEach((day, cnt) -> s.getData().add(new XYChart.Data<>(day, cnt)));
        return s;
    }

    // Data holder
    private record ReportData(long totalRes, double totalAmount, long activeUsers,
                              List<ReportService.TopUserRow> topUsers,
                              XYChart.Series<String, Number> barSeries,
                              XYChart.Series<String, Number> lineSeries) {}
}