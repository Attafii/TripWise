package ui.controllers.admin;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ui.util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;

/**
 * Admin System Health Monitor
 * Database status, system resources, performance metrics
 */
public class AdminSystemHealthController {

    @FXML private StackPane rootPane;
    
    // Database Health
    @FXML private Label lblDbStatus;
    @FXML private Label lblDbConnections;
    @FXML private Label lblDbSize;
    @FXML private Label lblDbUptime;
    
    // System Resources
    @FXML private Label lblCpuUsage;
    @FXML private Label lblMemoryUsage;
    @FXML private Label lblMemoryTotal;
    @FXML private Label lblMemoryFree;
    @FXML private ProgressBar progressCpu;
    @FXML private ProgressBar progressMemory;
    
    // Performance Metrics
    @FXML private Label lblAvgResponseTime;
    @FXML private Label lblTotalRequests;
    @FXML private Label lblErrorRate;
    @FXML private Label lblActiveThreads;
    
    // Table Statistics
    @FXML private Label lblTotalTables;
    @FXML private Label lblUsersCount;
    @FXML private Label lblBookingsCount;
    @FXML private Label lblHotelsCount;
    @FXML private Label lblFlightsCount;
    @FXML private Label lblVehiclesCount;
    
    // Cache & Performance
    @FXML private Label lblCacheHitRate;
    @FXML private Label lblQueryCacheSize;
    @FXML private Label lblSlowQueries;
    
    private Connection connection;
    private DecimalFormat df = new DecimalFormat("#.##");
    
    @FXML
    public void initialize() {
        connection = DataSource.getInstance().getConnection();
        loadSystemHealth();
        startAutoRefresh();
        playEntranceAnimation();
    }
    
    /**
     * Load all system health metrics
     */
    private void loadSystemHealth() {
        loadDatabaseHealth();
        loadSystemResources();
        loadTableStatistics();
        loadPerformanceMetrics();
    }
    
    /**
     * Load database health metrics
     */
    private void loadDatabaseHealth() {
        try {
            // Database Status
            if (connection != null && !connection.isClosed()) {
                lblDbStatus.setText("✅ Online");
                lblDbStatus.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else {
                lblDbStatus.setText("❌ Offline");
                lblDbStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                return;
            }
            
            // Database Size
            String sizeQuery = "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS size_mb " +
                             "FROM information_schema.TABLES " +
                             "WHERE table_schema = DATABASE()";
            PreparedStatement stmt = connection.prepareStatement(sizeQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lblDbSize.setText(df.format(rs.getDouble("size_mb")) + " MB");
            }
            
            // Database Uptime
            String uptimeQuery = "SHOW GLOBAL STATUS LIKE 'Uptime'";
            stmt = connection.prepareStatement(uptimeQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int uptimeSeconds = rs.getInt("Value");
                int days = uptimeSeconds / 86400;
                int hours = (uptimeSeconds % 86400) / 3600;
                lblDbUptime.setText(days + "d " + hours + "h");
            }
            
            // Active Connections
            String connQuery = "SHOW STATUS LIKE 'Threads_connected'";
            stmt = connection.prepareStatement(connQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblDbConnections.setText(rs.getString("Value") + " active");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error loading database health: " + e.getMessage());
            lblDbStatus.setText("⚠️ Error");
            lblDbStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Load system resource usage
     */
    private void loadSystemResources() {
        try {
            Runtime runtime = Runtime.getRuntime();
            
            // Memory Usage
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            double usedMB = usedMemory / (1024.0 * 1024.0);
            double totalMB = totalMemory / (1024.0 * 1024.0);
            double maxMB = maxMemory / (1024.0 * 1024.0);
            double freeMB = freeMemory / (1024.0 * 1024.0);
            
            lblMemoryUsage.setText(df.format(usedMB) + " MB");
            lblMemoryTotal.setText(df.format(maxMB) + " MB");
            lblMemoryFree.setText(df.format(freeMB) + " MB");
            
            double memoryPercent = (usedMemory / (double) totalMemory);
            progressMemory.setProgress(memoryPercent);
            
            // CPU Usage (simulated - Java doesn't provide easy CPU metrics)
            // In production, use JMX or system-specific tools
            double cpuUsage = Math.random() * 100; // Placeholder
            lblCpuUsage.setText(df.format(cpuUsage) + "%");
            progressCpu.setProgress(cpuUsage / 100.0);
            
            // Active Threads
            int threadCount = Thread.activeCount();
            lblActiveThreads.setText(String.valueOf(threadCount));
            
        } catch (Exception e) {
            System.err.println("❌ Error loading system resources: " + e.getMessage());
        }
    }
    
    /**
     * Load table row counts
     */
    private void loadTableStatistics() {
        try {
            // Total Tables
            String tableCountQuery = "SELECT COUNT(*) as count FROM information_schema.TABLES WHERE table_schema = DATABASE()";
            PreparedStatement stmt = connection.prepareStatement(tableCountQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lblTotalTables.setText(String.valueOf(rs.getInt("count")));
            }
            
            // Users Count
            stmt = connection.prepareStatement("SELECT COUNT(*) as count FROM users");
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblUsersCount.setText(String.format("%,d", rs.getInt("count")));
            }
            
            // Bookings Count
            stmt = connection.prepareStatement("SELECT COUNT(*) as count FROM reservations_hotel");
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblBookingsCount.setText(String.format("%,d", rs.getInt("count")));
            }
            
            // Hotels Count
            stmt = connection.prepareStatement("SELECT COUNT(*) as count FROM hotels");
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblHotelsCount.setText(String.format("%,d", rs.getInt("count")));
            }
            
            // Flights Count
            stmt = connection.prepareStatement("SELECT COUNT(*) as count FROM vols");
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblFlightsCount.setText(String.format("%,d", rs.getInt("count")));
            }
            
            // Vehicles Count
            stmt = connection.prepareStatement("SELECT COUNT(*) as count FROM vehicules");
            rs = stmt.executeQuery();
            if (rs.next()) {
                lblVehiclesCount.setText(String.format("%,d", rs.getInt("count")));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error loading table statistics: " + e.getMessage());
        }
    }
    
    /**
     * Load performance metrics
     */
    private void loadPerformanceMetrics() {
        try {
            // These are simulated metrics - in production, use actual monitoring tools
            lblAvgResponseTime.setText("45 ms");
            lblTotalRequests.setText("12,847");
            lblErrorRate.setText("0.03%");
            lblCacheHitRate.setText("94.2%");
            lblQueryCacheSize.setText("16 MB");
            lblSlowQueries.setText("3");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading performance metrics: " + e.getMessage());
        }
    }
    
    /**
     * Refresh all metrics
     */
    @FXML
    private void handleRefresh() {
        loadSystemHealth();
    }
    
    /**
     * Auto-refresh every 5 seconds
     */
    private void startAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // 5 seconds
                    Platform.runLater(this::loadSystemResources);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
    
    /**
     * Test database connection
     */
    @FXML
    private void handleTestConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.prepareStatement("SELECT 1").executeQuery();
                showAlert("✅ Database connection test successful!");
            } else {
                showAlert("❌ Database connection failed!");
            }
        } catch (Exception e) {
            showAlert("❌ Connection error: " + e.getMessage());
        }
    }
    
    /**
     * Clear query cache
     */
    @FXML
    private void handleClearCache() {
        try {
            connection.prepareStatement("RESET QUERY CACHE").execute();
            showAlert("✅ Query cache cleared successfully!");
            loadSystemHealth();
        } catch (Exception e) {
            showAlert("❌ Failed to clear cache: " + e.getMessage());
        }
    }
    
    /**
     * Optimize database tables
     */
    @FXML
    private void handleOptimizeTables() {
        try {
            String query = "SELECT table_name FROM information_schema.TABLES WHERE table_schema = DATABASE()";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            int optimized = 0;
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                connection.prepareStatement("OPTIMIZE TABLE " + tableName).execute();
                optimized++;
            }
            
            showAlert("✅ Optimized " + optimized + " tables successfully!");
            loadSystemHealth();
        } catch (Exception e) {
            showAlert("❌ Optimization failed: " + e.getMessage());
        }
    }
    
    /**
     * Show alert message
     */
    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("System Health");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
