package ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import ui.service.CapacityManagementService;
import ui.service.CapacityManagementService.*;
import ui.service.FlightReportService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for capacity management dashboard
 */
public class CapacityManagementController implements Initializable {

    @FXML private Label lblTotalFlights;
    @FXML private Label lblTotalCapacity;
    @FXML private Label lblBookedSeats;
    @FXML private Label lblBookedPercent;
    @FXML private Label lblAvailableSeats;
    @FXML private Label lblLastUpdated;

    @FXML private PieChart classDistributionChart;
    @FXML private BarChart<String, Number> routeOccupancyChart;

    @FXML private TableView<FlightCapacityRow> tblCapacity;
    @FXML private TableColumn<FlightCapacityRow, String> colFlight;
    @FXML private TableColumn<FlightCapacityRow, String> colRoute;
    @FXML private TableColumn<FlightCapacityRow, String> colDeparture;
    @FXML private TableColumn<FlightCapacityRow, String> colAirline;
    @FXML private TableColumn<FlightCapacityRow, Integer> colTotal;
    @FXML private TableColumn<FlightCapacityRow, Integer> colBooked;
    @FXML private TableColumn<FlightCapacityRow, Integer> colAvailable;
    @FXML private TableColumn<FlightCapacityRow, String> colOccupancy;
    @FXML private TableColumn<FlightCapacityRow, String> colStatus;
    @FXML private TableColumn<FlightCapacityRow, Void> colActions;

    @FXML private ComboBox<String> cmbFilter;
    @FXML private TextField txtSearch;

    private CapacityManagementService capacityService;
    private FlightReportService reportService;
    private ObservableList<FlightCapacityRow> capacityData;
    private FilteredList<FlightCapacityRow> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        capacityService = new CapacityManagementService();
        reportService = new FlightReportService();
        capacityData = FXCollections.observableArrayList();

        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        colFlight.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departure"));
        colAirline.setCellValueFactory(new PropertyValueFactory<>("airline"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));
        colBooked.setCellValueFactory(new PropertyValueFactory<>("bookedSeats"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        colOccupancy.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("%.1f%%", data.getValue().getOccupancyRate())));

        colOccupancy.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    double rate = Double.parseDouble(item.replace("%", ""));
                    if (rate >= 90) {
                        setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    } else if (rate >= 70) {
                        setStyle("-fx-background-color: #dcedc8; -fx-text-fill: #558b2f;");
                    } else if (rate >= 30) {
                        setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;");
                    } else {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                    }
                }
            }
        });

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(getStatusStyle(item));
                    setGraphic(badge);
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnDetails = new Button("📊");
            private final Button btnAlerts = new Button("⚠");

            {
                btnDetails.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 8;");
                btnAlerts.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 8;");

                btnDetails.setOnAction(e -> {
                    FlightCapacityRow row = getTableView().getItems().get(getIndex());
                    showCapacityDetails(row);
                });

                btnAlerts.setOnAction(e -> {
                    FlightCapacityRow row = getTableView().getItems().get(getIndex());
                    showCapacityAlerts(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, btnDetails, btnAlerts);
                    setGraphic(buttons);
                }
            }
        });

        filteredData = new FilteredList<>(capacityData, p -> true);
        tblCapacity.setItems(filteredData);
    }

    private void setupFilters() {
        cmbFilter.setValue("All Flights");
        cmbFilter.setOnAction(e -> applyFilters());

        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String filter = cmbFilter.getValue();
        String search = txtSearch.getText().toLowerCase();

        filteredData.setPredicate(row -> {
            // Search filter
            boolean matchesSearch = search.isEmpty() ||
                row.getFlightNumber().toLowerCase().contains(search) ||
                row.getRoute().toLowerCase().contains(search) ||
                row.getAirline().toLowerCase().contains(search);

            if (!matchesSearch) return false;

            // Occupancy filter
            double rate = row.getOccupancyRate();
            switch (filter) {
                case "Low Occupancy (<30%)":
                    return rate < 30;
                case "Medium (30-70%)":
                    return rate >= 30 && rate <= 70;
                case "High Occupancy (>70%)":
                    return rate > 70;
                case "Nearly Full (>90%)":
                    return rate > 90;
                default:
                    return true;
            }
        });
    }

    private void loadData() {
        // Load statistics
        CapacityStats stats = capacityService.getOverallCapacityStats();

        lblTotalFlights.setText(String.valueOf(stats.totalFlights));
        lblTotalCapacity.setText(String.valueOf(stats.totalCapacity));
        lblBookedSeats.setText(String.valueOf(stats.totalBooked));
        lblAvailableSeats.setText(String.valueOf(stats.totalAvailable));
        lblBookedPercent.setText(String.format("%.1f%% occupancy", stats.averageOccupancy));

        lblLastUpdated.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Load class distribution chart
        loadClassDistributionChart(stats.classDistribution);

        // Load route occupancy chart
        loadRouteOccupancyChart();

        // Load table data
        loadTableData();
    }

    private void loadClassDistributionChart(Map<String, Integer> distribution) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        distribution.forEach((className, count) -> {
            String displayName = getClassDisplayName(className);
            pieData.add(new PieChart.Data(displayName + " (" + count + ")", count));
        });

        classDistributionChart.setData(pieData);
        classDistributionChart.setTitle("");
    }

    private void loadRouteOccupancyChart() {
        List<RouteCapacity> routes = capacityService.getRouteCapacityAnalysis();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Occupancy %");

        // Take top 8 routes
        routes.stream().limit(8).forEach(route -> {
            String shortRoute = route.departure.substring(0, Math.min(3, route.departure.length())).toUpperCase()
                + "-" + route.arrival.substring(0, Math.min(3, route.arrival.length())).toUpperCase();
            series.getData().add(new XYChart.Data<>(shortRoute, route.averageOccupancy));
        });

        routeOccupancyChart.getData().clear();
        routeOccupancyChart.getData().add(series);
        routeOccupancyChart.setLegendVisible(false);
    }

    private void loadTableData() {
        capacityData.clear();

        // Get all flights with capacity info
        List<FlightCapacityInfo> flights = capacityService.getHighCapacityFlights(0); // Get all

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm");

        for (FlightCapacityInfo info : flights) {
            FlightCapacityRow row = new FlightCapacityRow();
            row.setFlightNumber(info.flightNumber);
            row.setRoute(info.route);
            row.setDeparture(info.departureTime != null ? info.departureTime.format(formatter) : "-");
            row.setAirline(info.airline);
            row.setTotalSeats(info.totalCapacity);
            row.setBookedSeats(info.bookedSeats);
            row.setAvailableSeats(info.availableSeats);
            row.setOccupancyRate(info.occupancyRate);
            row.setStatus(info.getCapacityStatus());
            row.setVolId(info.volId);

            capacityData.add(row);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleExportExcel() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Export Location");
        File dir = chooser.showDialog(getStage());

        if (dir != null) {
            try {
                String path;
                // Try to use POI for real Excel export, fallback to CSV
                if (reportService.isExcelExportAvailable()) {
                    path = reportService.generateFlightsExcelReportPOI(dir.getAbsolutePath());
                    showSuccess("Excel Export Complete", "Excel report (.xlsx) saved to:\n" + path);
                } else {
                    path = reportService.generateCapacityReportExcel(dir.getAbsolutePath());
                    showSuccess("Export Complete", "CSV report saved to:\n" + path + "\n\n(Install Apache POI for .xlsx format)");
                }
            } catch (Exception e) {
                showError("Export Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPDF() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Export Location");
        File dir = chooser.showDialog(getStage());

        if (dir != null) {
            try {
                String path = reportService.generateFlightsPdfReport(dir.getAbsolutePath());
                showSuccess("Export Complete", "PDF report saved to:\n" + path);
            } catch (Exception e) {
                showError("Export Failed", e.getMessage());
            }
        }
    }

    private void showCapacityDetails(FlightCapacityRow row) {
        FlightCapacityInfo info = capacityService.getFlightCapacity(row.getVolId());

        StringBuilder details = new StringBuilder();
        details.append("Flight: ").append(info.flightNumber).append("\n");
        details.append("Route: ").append(info.route).append("\n");
        details.append("Aircraft: ").append(info.aircraftType).append("\n\n");
        details.append("CAPACITY BREAKDOWN:\n");
        details.append("━━━━━━━━━━━━━━━━━━\n");
        details.append(String.format("Total Seats: %d\n", info.totalCapacity));
        details.append(String.format("Booked: %d (%.1f%%)\n", info.bookedSeats, info.occupancyRate));
        details.append(String.format("Available: %d\n\n", info.availableSeats));

        if (info.classCapacity != null) {
            details.append("BY CLASS:\n");
            info.classCapacity.forEach((className, capacity) -> {
                details.append(String.format("  %s: %d/%d (%.0f%%)\n",
                    getClassDisplayName(className), capacity.booked, capacity.total, capacity.occupancyRate));
            });
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Capacity Details");
        alert.setHeaderText(info.flightNumber + " - " + info.route);
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void showCapacityAlerts(FlightCapacityRow row) {
        List<UpgradeRecommendation> recommendations = capacityService.getUpgradeRecommendations(row.getVolId());

        StringBuilder alerts = new StringBuilder();

        if (row.getOccupancyRate() < 30) {
            alerts.append("⚠️ LOW OCCUPANCY ALERT\n");
            alerts.append("Consider promotional pricing or consolidation.\n\n");
        } else if (row.getOccupancyRate() > 90) {
            alerts.append("✅ HIGH DEMAND\n");
            alerts.append("Flight is nearly full. Consider dynamic pricing.\n\n");
        }

        if (!recommendations.isEmpty()) {
            alerts.append("UPGRADE OPPORTUNITIES:\n");
            for (UpgradeRecommendation rec : recommendations) {
                alerts.append(String.format("• %s → %s (%d seats available)\n  Reason: %s\n",
                    rec.fromClass, rec.toClass, rec.availableSeats, rec.reason));
            }
        } else {
            alerts.append("No upgrade recommendations at this time.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Capacity Alerts");
        alert.setHeaderText(row.getFlightNumber());
        alert.setContentText(alerts.toString());
        alert.showAndWait();
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "FULL":
                return "-fx-background-color: #c62828; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;";
            case "HIGH":
                return "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;";
            case "MODERATE":
                return "-fx-background-color: #f57c00; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;";
            case "LOW":
                return "-fx-background-color: #fbc02d; -fx-text-fill: #333; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;";
            default:
                return "-fx-background-color: #e57373; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;";
        }
    }

    private String getClassDisplayName(String className) {
        switch (className) {
            case "ECONOMIQUE": return "Economy";
            case "PREMIUM": return "Premium";
            case "BUSINESS": return "Business";
            case "PREMIERE": return "First";
            default: return className;
        }
    }

    private Stage getStage() {
        return (Stage) tblCapacity.getScene().getWindow();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for table data
    public static class FlightCapacityRow {
        private int volId;
        private String flightNumber;
        private String route;
        private String departure;
        private String airline;
        private int totalSeats;
        private int bookedSeats;
        private int availableSeats;
        private double occupancyRate;
        private String status;

        // Getters and Setters
        public int getVolId() { return volId; }
        public void setVolId(int volId) { this.volId = volId; }

        public String getFlightNumber() { return flightNumber; }
        public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }

        public String getDeparture() { return departure; }
        public void setDeparture(String departure) { this.departure = departure; }

        public String getAirline() { return airline; }
        public void setAirline(String airline) { this.airline = airline; }

        public int getTotalSeats() { return totalSeats; }
        public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

        public int getBookedSeats() { return bookedSeats; }
        public void setBookedSeats(int bookedSeats) { this.bookedSeats = bookedSeats; }

        public int getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

        public double getOccupancyRate() { return occupancyRate; }
        public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
