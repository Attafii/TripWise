package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

/**
 * DashboardHomeController - Modern dashboard with stats and charts
 */
public class DashboardHomeController {

    @FXML
    private Label completedTripsLabel;

    @FXML
    private Label activeTripsLabel;

    @FXML
    private Label cancelledTripsLabel;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private BarChart<String, Number> salesChart;

    @FXML
    private LineChart<String, Number> flightScheduleChart;

    @FXML
    private void initialize() {
        loadStatistics();
        loadSalesChart();
        loadFlightScheduleChart();
    }

    private void loadStatistics() {
        // Load stats from database (simplified for now)
        completedTripsLabel.setText("225");
        activeTripsLabel.setText("80");
        cancelledTripsLabel.setText("25");
        totalRevenueLabel.setText("$15,000");
    }

    private void loadSalesChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");

        series.getData().add(new XYChart.Data<>("Mon", 8));
        series.getData().add(new XYChart.Data<>("Tue", 12));
        series.getData().add(new XYChart.Data<>("Wed", 15));
        series.getData().add(new XYChart.Data<>("Thu", 20));
        series.getData().add(new XYChart.Data<>("Fri", 18));
        series.getData().add(new XYChart.Data<>("Sat", 22));
        series.getData().add(new XYChart.Data<>("Sun", 25));

        salesChart.getData().add(series);
        salesChart.setLegendVisible(false);
    }

    private void loadFlightScheduleChart() {
        XYChart.Series<String, Number> domesticSeries = new XYChart.Series<>();
        domesticSeries.setName("Domestic");

        XYChart.Series<String, Number> internationalSeries = new XYChart.Series<>();
        internationalSeries.setName("International");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] domesticData = {30, 35, 32, 38, 42, 45};
        int[] internationalData = {25, 28, 30, 32, 35, 38};

        for (int i = 0; i < months.length; i++) {
            domesticSeries.getData().add(new XYChart.Data<>(months[i], domesticData[i]));
            internationalSeries.getData().add(new XYChart.Data<>(months[i], internationalData[i]));
        }

        flightScheduleChart.getData().addAll(domesticSeries, internationalSeries);
    }
}
