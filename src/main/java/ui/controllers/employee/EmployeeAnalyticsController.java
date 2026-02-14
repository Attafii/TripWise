package ui.controllers.employee;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ui.service.AnalyticsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Employee Analytics Dashboard
 * Revenue charts, booking stats, performance metrics
 */
public class EmployeeAnalyticsController {

    @FXML private StackPane rootPane;
    
    // Statistics cards
    @FXML private Label lblTodayBookings;
    @FXML private Label lblWeekBookings;
    @FXML private Label lblMonthBookings;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblWeekRevenue;
    @FXML private Label lblMonthRevenue;
    @FXML private Label lblAvgBookingValue;
    @FXML private Label lblConversionRate;
    
    // Charts
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> bookingsChart;
    @FXML private PieChart statusPieChart;
    @FXML private AreaChart<String, Number> trendChart;
    
    // Filters
    @FXML private ComboBox<String> cbPeriod;
    
    private final AnalyticsService analyticsService;
    
    public EmployeeAnalyticsController() {
        this.analyticsService = new AnalyticsService();
    }
    
    @FXML
    public void initialize() {
        setupFilters();
        loadStatistics();
        loadCharts();
        playEntranceAnimation();
    }
    
    /**
     * Setup period filter
     */
    private void setupFilters() {
        cbPeriod.setItems(FXCollections.observableArrayList(
            "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last Year"
        ));
        cbPeriod.setValue("Last 30 Days");
        
        cbPeriod.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadStatistics();
            loadCharts();
        });
    }
    
    /**
     * Load summary statistics
     */
    private void loadStatistics() {
        // Today's stats
        AnalyticsService.DailyStats todayStats = analyticsService.getDailyStats(LocalDate.now());
        lblTodayBookings.setText(String.valueOf(todayStats.bookingCount));
        lblTodayRevenue.setText("$" + String.format("%,.2f", todayStats.revenue));
        
        // Week stats
        LocalDate weekStart = LocalDate.now().minusDays(7);
        AnalyticsService.PeriodStats weekStats = analyticsService.getPeriodStats(weekStart, LocalDate.now());
        lblWeekBookings.setText(String.valueOf(weekStats.totalBookings));
        lblWeekRevenue.setText("$" + String.format("%,.2f", weekStats.totalRevenue));
        
        // Month stats
        LocalDate monthStart = LocalDate.now().minusDays(30);
        AnalyticsService.PeriodStats monthStats = analyticsService.getPeriodStats(monthStart, LocalDate.now());
        lblMonthBookings.setText(String.valueOf(monthStats.totalBookings));
        lblMonthRevenue.setText("$" + String.format("%,.2f", monthStats.totalRevenue));
        
        // Calculated metrics
        double avgBookingValue = monthStats.totalBookings > 0 ? 
            monthStats.totalRevenue / monthStats.totalBookings : 0;
        lblAvgBookingValue.setText("$" + String.format("%.2f", avgBookingValue));
        
        double conversionRate = monthStats.totalBookings > 0 ?
            (monthStats.confirmedBookings / (double) monthStats.totalBookings) * 100 : 0;
        lblConversionRate.setText(String.format("%.1f%%", conversionRate));
    }
    
    /**
     * Load all charts
     */
    private void loadCharts() {
        loadRevenueChart();
        loadBookingsChart();
        loadStatusPieChart();
        loadTrendChart();
    }
    
    /**
     * Load revenue line chart
     */
    private void loadRevenueChart() {
        revenueChart.getData().clear();
        revenueChart.setTitle("Daily Revenue Trend");
        
        int days = getPeriodDays();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        List<AnalyticsService.DailyStats> dailyStats = 
            analyticsService.getDailyStatsRange(startDate, LocalDate.now());
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        
        for (AnalyticsService.DailyStats stats : dailyStats) {
            String dateLabel = stats.date.format(DateTimeFormatter.ofPattern("MMM dd"));
            series.getData().add(new XYChart.Data<>(dateLabel, stats.revenue));
        }
        
        revenueChart.getData().add(series);
        
        // Style
        revenueChart.setLegendVisible(false);
        revenueChart.setCreateSymbols(true);
    }
    
    /**
     * Load bookings bar chart
     */
    private void loadBookingsChart() {
        bookingsChart.getData().clear();
        bookingsChart.setTitle("Bookings by Week");
        
        LocalDate startDate = LocalDate.now().minusDays(getPeriodDays());
        List<AnalyticsService.WeeklyStats> weeklyStats = 
            analyticsService.getWeeklyStats(startDate, LocalDate.now());
        
        XYChart.Series<String, Number> confirmedSeries = new XYChart.Series<>();
        confirmedSeries.setName("Confirmed");
        
        XYChart.Series<String, Number> pendingSeries = new XYChart.Series<>();
        pendingSeries.setName("Pending");
        
        XYChart.Series<String, Number> cancelledSeries = new XYChart.Series<>();
        cancelledSeries.setName("Cancelled");
        
        for (AnalyticsService.WeeklyStats stats : weeklyStats) {
            String weekLabel = "Week " + stats.weekNumber;
            confirmedSeries.getData().add(new XYChart.Data<>(weekLabel, stats.confirmedBookings));
            pendingSeries.getData().add(new XYChart.Data<>(weekLabel, stats.pendingBookings));
            cancelledSeries.getData().add(new XYChart.Data<>(weekLabel, stats.cancelledBookings));
        }
        
        bookingsChart.getData().addAll(confirmedSeries, pendingSeries, cancelledSeries);
    }
    
    /**
     * Load status distribution pie chart
     */
    private void loadStatusPieChart() {
        statusPieChart.getData().clear();
        statusPieChart.setTitle("Booking Status Distribution");
        
        LocalDate startDate = LocalDate.now().minusDays(getPeriodDays());
        Map<String, Integer> statusDistribution = 
            analyticsService.getStatusDistribution(startDate, LocalDate.now());
        
        for (Map.Entry<String, Integer> entry : statusDistribution.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            statusPieChart.getData().add(slice);
        }
        
        // Show percentages on labels
        statusPieChart.getData().forEach(data -> {
            int total = statusPieChart.getData().stream()
                .mapToInt(d -> (int) d.getPieValue())
                .sum();
            double percentage = (data.getPieValue() / total) * 100;
            data.setName(data.getName() + " (" + String.format("%.1f%%", percentage) + ")");
        });
    }
    
    /**
     * Load trend area chart
     */
    private void loadTrendChart() {
        trendChart.getData().clear();
        trendChart.setTitle("Booking Trend");
        
        int days = getPeriodDays();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        List<AnalyticsService.DailyStats> dailyStats = 
            analyticsService.getDailyStatsRange(startDate, LocalDate.now());
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Bookings");
        
        for (AnalyticsService.DailyStats stats : dailyStats) {
            String dateLabel = stats.date.format(DateTimeFormatter.ofPattern("MMM dd"));
            series.getData().add(new XYChart.Data<>(dateLabel, stats.bookingCount));
        }
        
        trendChart.getData().add(series);
        trendChart.setLegendVisible(false);
    }
    
    /**
     * Get number of days based on selected period
     */
    private int getPeriodDays() {
        String period = cbPeriod.getValue();
        switch (period) {
            case "Last 7 Days": return 7;
            case "Last 30 Days": return 30;
            case "Last 3 Months": return 90;
            case "Last Year": return 365;
            default: return 30;
        }
    }
    
    /**
     * Refresh all data
     */
    @FXML
    private void handleRefresh() {
        loadStatistics();
        loadCharts();
    }
    
    /**
     * Play entrance animation
     */
    private void playEntranceAnimation() {
        if (rootPane != null) {
            rootPane.setOpacity(0.0);
            FadeTransition fade = new FadeTransition(Duration.millis(400), rootPane);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }
}
