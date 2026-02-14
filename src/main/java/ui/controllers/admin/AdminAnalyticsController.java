package ui.controllers.admin;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ui.util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Admin System Analytics Dashboard
 * System-wide metrics, revenue analytics, user growth, booking trends
 */
public class AdminAnalyticsController {

    @FXML private StackPane rootPane;
    
    // Statistics Cards
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalBookings;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblPendingBookings;
    @FXML private Label lblMonthlyGrowth;
    @FXML private Label lblAvgBookingValue;
    @FXML private Label lblTopDestination;
    
    // Charts
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> bookingsChart;
    @FXML private PieChart userTypePieChart;
    @FXML private AreaChart<String, Number> userGrowthChart;
    
    private Connection connection;
    
    @FXML
    public void initialize() {
        connection = DataSource.getInstance().getConnection();
        loadStatistics();
        loadCharts();
        playEntranceAnimation();
    }
    
    /**
     * Load summary statistics
     */
    private void loadStatistics() {
        try {
            // Total Users
            String userQuery = "SELECT COUNT(*) as total FROM users WHERE is_active = 1";
            PreparedStatement stmt = connection.prepareStatement(userQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lblTotalUsers.setText(String.format("%,d", rs.getInt("total")));
            }
            
            // Total Bookings
            String bookingQuery = "SELECT COUNT(*) as total FROM reservations_hotel";
            stmt = connection.prepareStatement(bookingQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblTotalBookings.setText(String.format("%,d", rs.getInt("total")));
            }
            
            // Total Revenue
            String revenueQuery = "SELECT SUM(prix_total) as total FROM reservations_hotel WHERE statut_reservation = 'CONFIRMEE'";
            stmt = connection.prepareStatement(revenueQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblTotalRevenue.setText("$" + String.format("%,.2f", rs.getDouble("total")));
            }
            
            // Active Users (logged in last 30 days)
            String activeQuery = "SELECT COUNT(DISTINCT user_id) as active FROM users WHERE is_active = 1";
            stmt = connection.prepareStatement(activeQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblActiveUsers.setText(String.format("%,d", rs.getInt("active")));
            }
            
            // Pending Bookings
            String pendingQuery = "SELECT COUNT(*) as pending FROM reservations_hotel WHERE statut_reservation = 'EN_ATTENTE'";
            stmt = connection.prepareStatement(pendingQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblPendingBookings.setText(String.format("%,d", rs.getInt("pending")));
            }
            
            // Monthly Growth
            String growthQuery = "SELECT " +
                                "COUNT(CASE WHEN DATE(date_reservation) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) as current_month, " +
                                "COUNT(CASE WHEN DATE(date_reservation) BETWEEN DATE_SUB(CURDATE(), INTERVAL 60 DAY) AND DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) as previous_month " +
                                "FROM reservations_hotel";
            stmt = connection.prepareStatement(growthQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int current = rs.getInt("current_month");
                int previous = rs.getInt("previous_month");
                double growth = previous > 0 ? ((current - previous) / (double) previous) * 100 : 0;
                lblMonthlyGrowth.setText(String.format("%+.1f%%", growth));
            }
            
            // Average Booking Value
            String avgQuery = "SELECT AVG(prix_total) as avg_value FROM reservations_hotel WHERE statut_reservation = 'CONFIRMEE'";
            stmt = connection.prepareStatement(avgQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblAvgBookingValue.setText("$" + String.format("%.2f", rs.getDouble("avg_value")));
            }
            
            // Top Destination
            String destQuery = "SELECT h.ville, COUNT(*) as count FROM reservations_hotel r " +
                              "JOIN hotels h ON r.hotel_id = h.hotel_id " +
                              "GROUP BY h.ville ORDER BY count DESC LIMIT 1";
            stmt = connection.prepareStatement(destQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblTopDestination.setText(rs.getString("ville"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load all charts
     */
    private void loadCharts() {
        loadRevenueChart();
        loadBookingsChart();
        loadUserTypeChart();
        loadUserGrowthChart();
    }
    
    /**
     * Load revenue trend line chart (last 30 days)
     */
    private void loadRevenueChart() {
        revenueChart.getData().clear();
        revenueChart.setTitle("Revenue Trend (Last 30 Days)");
        
        try {
            String query = "SELECT DATE(date_reservation) as booking_date, SUM(prix_total) as daily_revenue " +
                          "FROM reservations_hotel " +
                          "WHERE DATE(date_reservation) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                          "AND statut_reservation = 'CONFIRMEE' " +
                          "GROUP BY DATE(date_reservation) " +
                          "ORDER BY booking_date";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");
            
            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("booking_date");
                if (sqlDate != null) {
                    LocalDate date = sqlDate.toLocalDate();
                    String dateLabel = date.format(DateTimeFormatter.ofPattern("MMM dd"));
                    series.getData().add(new XYChart.Data<>(dateLabel, rs.getDouble("daily_revenue")));
                }
            }
            
            revenueChart.getData().add(series);
            revenueChart.setLegendVisible(false);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading revenue chart: " + e.getMessage());
        }
    }
    
    /**
     * Load bookings by status bar chart
     */
    private void loadBookingsChart() {
        bookingsChart.getData().clear();
        bookingsChart.setTitle("Bookings by Status");
        
        try {
            String query = "SELECT statut_reservation, COUNT(*) as count FROM reservations_hotel GROUP BY statut_reservation";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Bookings");
            
            while (rs.next()) {
                String status = rs.getString("statut_reservation");
                String displayStatus = status.replace("_", " ");
                series.getData().add(new XYChart.Data<>(displayStatus, rs.getInt("count")));
            }
            
            bookingsChart.getData().add(series);
            bookingsChart.setLegendVisible(false);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading bookings chart: " + e.getMessage());
        }
    }
    
    /**
     * Load user type distribution pie chart
     */
    private void loadUserTypeChart() {
        userTypePieChart.getData().clear();
        userTypePieChart.setTitle("User Distribution by Type");
        
        try {
            String query = "SELECT user_type, COUNT(*) as count FROM users WHERE is_active = 1 GROUP BY user_type";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String type = rs.getString("user_type");
                int count = rs.getInt("count");
                PieChart.Data slice = new PieChart.Data(type + " (" + count + ")", count);
                userTypePieChart.getData().add(slice);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error loading user type chart: " + e.getMessage());
        }
    }
    
    /**
     * Load user growth area chart (last 6 months)
     */
    private void loadUserGrowthChart() {
        userGrowthChart.getData().clear();
        userGrowthChart.setTitle("User Growth (Last 6 Months)");
        
        try {
            String query = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as new_users " +
                          "FROM users " +
                          "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                          "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
                          "ORDER BY month";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("New Users");
            
            while (rs.next()) {
                String month = rs.getString("month");
                series.getData().add(new XYChart.Data<>(month, rs.getInt("new_users")));
            }
            
            userGrowthChart.getData().add(series);
            userGrowthChart.setLegendVisible(false);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading user growth chart: " + e.getMessage());
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
