package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.User;
import ui.util.SessionManager;

/**
 * DashboardHomeController - Role-specific dashboard views
 * Different stats and charts for Admin, Employee, and Traveler
 */
public class DashboardHomeController {

    @FXML private Label stat1Label;
    @FXML private Label stat1Title;
    @FXML private Label stat2Label;
    @FXML private Label stat2Title;
    @FXML private Label stat3Label;
    @FXML private Label stat3Title;
    @FXML private Label stat4Label;
    @FXML private Label stat4Title;
    
    @FXML private BarChart<String, Number> salesChart;
    @FXML private LineChart<String, Number> flightScheduleChart;
    @FXML private Label chart1Title;
    @FXML private Label chart2Title;
    
    @FXML private VBox stat1Card;
    @FXML private VBox stat2Card;
    @FXML private VBox stat3Card;
    @FXML private VBox stat4Card;

    private User.UserType userRole;

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ ERROR: No user in session!");
            return;
        }
        
        userRole = currentUser.getUserType();
        System.out.println("✅ Dashboard Home - User Role: " + userRole);
        
        loadRoleSpecificData();
    }

    private void loadRoleSpecificData() {
        if (userRole == null) {
            System.err.println("❌ ERROR: User role is null!");
            loadTravelerDashboard(); // fallback
            return;
        }
        
        System.out.println("🔄 Loading dashboard for role: " + userRole);
        
        switch (userRole) {
            case ADMIN:
            case RESPONSABLE:
                loadAdminDashboard();
                break;
            case EMPLOYE:
                loadEmployeeDashboard();
                break;
            case VOYAGEUR:
            case VISITEUR:
            default:
                loadTravelerDashboard();
                break;
        }
    }

    /**
     * ADMIN Dashboard - System-wide analytics and management
     */
    private void loadAdminDashboard() {
        // Admin-specific statistics
        stat1Title.setText("Total Users");
        stat1Label.setText("1,247");
        
        stat2Title.setText("Total Bookings");
        stat2Label.setText("3,856");
        
        stat3Title.setText("Active Employees");
        stat3Label.setText("42");
        
        stat4Title.setText("Total Revenue");
        stat4Label.setText("$284,500");
        
        // Admin chart titles
        chart1Title.setText("Monthly Revenue");
        chart2Title.setText("Booking Trends");
        
        // Load admin charts
        loadAdminRevenueChart();
        loadAdminBookingTrendsChart();
    }

    /**
     * EMPLOYEE Dashboard - Work-related metrics and tasks
     */
    private void loadEmployeeDashboard() {
        // Employee-specific statistics
        stat1Title.setText("Assigned Bookings");
        stat1Label.setText("28");
        
        stat2Title.setText("Pending Approvals");
        stat2Label.setText("12");
        
        stat3Title.setText("Resolved Today");
        stat3Label.setText("15");
        
        stat4Title.setText("Customer Inquiries");
        stat4Label.setText("7");
        
        // Employee chart titles
        chart1Title.setText("Weekly Performance");
        chart2Title.setText("Booking Status Distribution");
        
        // Load employee charts
        loadEmployeePerformanceChart();
        loadEmployeeBookingStatusChart();
    }

    /**
     * TRAVELER Dashboard - Personal travel insights
     */
    private void loadTravelerDashboard() {
        // Traveler-specific statistics
        stat1Title.setText("Upcoming Trips");
        stat1Label.setText("3");
        
        stat2Title.setText("Completed Trips");
        stat2Label.setText("12");
        
        stat3Title.setText("Pending Bookings");
        stat3Label.setText("2");
        
        stat4Title.setText("Total Spent");
        stat4Label.setText("$8,450");
        
        // Traveler chart titles
        chart1Title.setText("Travel History");
        chart2Title.setText("Favorite Destinations");
        
        // Load traveler charts
        loadTravelerHistoryChart();
        loadTravelerDestinationsChart();
    }

    // ADMIN CHARTS
    private void loadAdminRevenueChart() {
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        
        series.getData().add(new XYChart.Data<>("Jan", 45000));
        series.getData().add(new XYChart.Data<>("Feb", 52000));
        series.getData().add(new XYChart.Data<>("Mar", 48000));
        series.getData().add(new XYChart.Data<>("Apr", 61000));
        series.getData().add(new XYChart.Data<>("May", 58000));
        series.getData().add(new XYChart.Data<>("Jun", 67000));
        
        salesChart.getData().add(series);
        salesChart.setLegendVisible(false);
    }

    private void loadAdminBookingTrendsChart() {
        flightScheduleChart.getData().clear();
        
        XYChart.Series<String, Number> flights = new XYChart.Series<>();
        flights.setName("Flights");
        XYChart.Series<String, Number> hotels = new XYChart.Series<>();
        hotels.setName("Hotels");
        
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] flightData = {150, 180, 165, 220, 195, 240};
        int[] hotelData = {85, 92, 88, 110, 105, 125};
        
        for (int i = 0; i < months.length; i++) {
            flights.getData().add(new XYChart.Data<>(months[i], flightData[i]));
            hotels.getData().add(new XYChart.Data<>(months[i], hotelData[i]));
        }
        
        flightScheduleChart.getData().addAll(flights, hotels);
    }

    // EMPLOYEE CHARTS
    private void loadEmployeePerformanceChart() {
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings Processed");
        
        series.getData().add(new XYChart.Data<>("Mon", 12));
        series.getData().add(new XYChart.Data<>("Tue", 18));
        series.getData().add(new XYChart.Data<>("Wed", 15));
        series.getData().add(new XYChart.Data<>("Thu", 22));
        series.getData().add(new XYChart.Data<>("Fri", 19));
        series.getData().add(new XYChart.Data<>("Sat", 8));
        series.getData().add(new XYChart.Data<>("Sun", 5));
        
        salesChart.getData().add(series);
        salesChart.setLegendVisible(false);
    }

    private void loadEmployeeBookingStatusChart() {
        flightScheduleChart.getData().clear();
        
        XYChart.Series<String, Number> confirmed = new XYChart.Series<>();
        confirmed.setName("Confirmed");
        XYChart.Series<String, Number> pending = new XYChart.Series<>();
        pending.setName("Pending");
        
        String[] weeks = {"Week 1", "Week 2", "Week 3", "Week 4"};
        int[] confirmedData = {25, 32, 28, 35};
        int[] pendingData = {8, 6, 10, 7};
        
        for (int i = 0; i < weeks.length; i++) {
            confirmed.getData().add(new XYChart.Data<>(weeks[i], confirmedData[i]));
            pending.getData().add(new XYChart.Data<>(weeks[i], pendingData[i]));
        }
        
        flightScheduleChart.getData().addAll(confirmed, pending);
    }

    // TRAVELER CHARTS
    private void loadTravelerHistoryChart() {
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Trips");
        
        series.getData().add(new XYChart.Data<>("Jan", 2));
        series.getData().add(new XYChart.Data<>("Feb", 1));
        series.getData().add(new XYChart.Data<>("Mar", 3));
        series.getData().add(new XYChart.Data<>("Apr", 2));
        series.getData().add(new XYChart.Data<>("May", 1));
        series.getData().add(new XYChart.Data<>("Jun", 3));
        
        salesChart.getData().add(series);
        salesChart.setLegendVisible(false);
    }

    private void loadTravelerDestinationsChart() {
        flightScheduleChart.getData().clear();
        
        XYChart.Series<String, Number> domestic = new XYChart.Series<>();
        domestic.setName("Domestic");
        XYChart.Series<String, Number> international = new XYChart.Series<>();
        international.setName("International");
        
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] domesticData = {1, 0, 2, 1, 0, 2};
        int[] internationalData = {1, 1, 1, 1, 1, 1};
        
        for (int i = 0; i < months.length; i++) {
            domestic.getData().add(new XYChart.Data<>(months[i], domesticData[i]));
            international.getData().add(new XYChart.Data<>(months[i], internationalData[i]));
        }
        
        flightScheduleChart.getData().addAll(domestic, international);
    }
}
