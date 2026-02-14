package ui.controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import ui.model.User;
import ui.util.DataSource;
import ui.util.NotificationUtil;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Activity Logs and Audit Trail
 * Tracks all system activities for security and compliance
 */
public class AdminActivityLogsController {

    @FXML private StackPane rootPane;
    @FXML private TableView<ActivityLog> logsTable;
    @FXML private TableColumn<ActivityLog, Integer> colId;
    @FXML private TableColumn<ActivityLog, String> colTimestamp;
    @FXML private TableColumn<ActivityLog, String> colUser;
    @FXML private TableColumn<ActivityLog, String> colAction;
    @FXML private TableColumn<ActivityLog, String> colEntity;
    @FXML private TableColumn<ActivityLog, String> colDetails;
    @FXML private TableColumn<ActivityLog, String> colIpAddress;
    @FXML private TableColumn<ActivityLog, String> colSeverity;

    @FXML private Label lblTotalLogs;
    @FXML private Label lblTodayLogs;
    @FXML private Label lblCriticalActions;
    @FXML private Label lblUniqueUsers;

    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cmbAction;
    @FXML private ComboBox<String> cmbSeverity;
    @FXML private TextField txtUserSearch;
    @FXML private TextField txtSearch;

    @FXML private Button btnSearch;
    @FXML private Button btnReset;
    @FXML private Button btnExportCSV;
    @FXML private Button btnRefresh;
    @FXML private Button btnClearOld;

    private User currentUser;
    private ObservableList<ActivityLog> logsList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadAllLogs();
        updateStatistics();
    }

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colEntity.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colIpAddress.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));

        // Style severity column based on severity level
        colSeverity.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(severity);
                    switch (severity) {
                        case "CRITICAL":
                            setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold;");
                            break;
                        case "WARNING":
                            setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-weight: bold;");
                            break;
                        case "INFO":
                            setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        logsTable.setItems(logsList);
    }

    /**
     * Setup filter dropdowns
     */
    private void setupFilters() {
        cmbAction.setItems(FXCollections.observableArrayList(
            "All", "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "APPROVE", "REJECT", "EXPORT"
        ));
        cmbAction.setValue("All");

        cmbSeverity.setItems(FXCollections.observableArrayList(
            "All", "INFO", "WARNING", "CRITICAL"
        ));
        cmbSeverity.setValue("All");

        // Set default date range (last 30 days)
        dpEndDate.setValue(LocalDate.now());
        dpStartDate.setValue(LocalDate.now().minusDays(30));
    }

    /**
     * Load all logs from database
     */
    private void loadAllLogs() {
        logsList.clear();
        
        String query = "SELECT al.*, CONCAT(u.first_name, ' ', u.last_name) as user_name " +
                      "FROM activity_logs al " +
                      "LEFT JOIN users u ON al.user_id = u.user_id " +
                      "WHERE al.created_at >= ? AND al.created_at <= ? ";

        // Add filters
        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(dpStartDate.getValue().atStartOfDay()));
        params.add(Timestamp.valueOf(dpEndDate.getValue().atTime(23, 59, 59)));

        if (!"All".equals(cmbAction.getValue())) {
            query += "AND al.action = ? ";
            params.add(cmbAction.getValue());
        }

        if (!"All".equals(cmbSeverity.getValue())) {
            query += "AND al.severity = ? ";
            params.add(cmbSeverity.getValue());
        }

        if (txtUserSearch.getText() != null && !txtUserSearch.getText().trim().isEmpty()) {
            query += "AND (u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?) ";
            String userSearch = "%" + txtUserSearch.getText().trim() + "%";
            params.add(userSearch);
            params.add(userSearch);
            params.add(userSearch);
        }

        if (txtSearch.getText() != null && !txtSearch.getText().trim().isEmpty()) {
            query += "AND (al.details LIKE ? OR al.entity_type LIKE ?) ";
            String search = "%" + txtSearch.getText().trim() + "%";
            params.add(search);
            params.add(search);
        }

        query += "ORDER BY al.created_at DESC LIMIT 1000";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ActivityLog log = new ActivityLog(
                    rs.getInt("log_id"),
                    rs.getTimestamp("created_at").toLocalDateTime().format(formatter),
                    rs.getString("user_name") != null ? rs.getString("user_name") : "System",
                    rs.getString("action"),
                    rs.getString("entity_type"),
                    rs.getString("details"),
                    rs.getString("ip_address"),
                    rs.getString("severity")
                );
                logsList.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Error loading activity logs: " + e.getMessage());
            NotificationUtil.showError("Failed to load activity logs: " + e.getMessage(), rootPane);
        }
    }

    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            // Total logs
            String totalQuery = "SELECT COUNT(*) as total FROM activity_logs";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalQuery)) {
                if (rs.next()) {
                    lblTotalLogs.setText(String.valueOf(rs.getInt("total")));
                }
            }

            // Today's logs
            String todayQuery = "SELECT COUNT(*) as today FROM activity_logs WHERE DATE(created_at) = CURDATE()";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(todayQuery)) {
                if (rs.next()) {
                    lblTodayLogs.setText(String.valueOf(rs.getInt("today")));
                }
            }

            // Critical actions
            String criticalQuery = "SELECT COUNT(*) as critical FROM activity_logs WHERE severity = 'CRITICAL' AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(criticalQuery)) {
                if (rs.next()) {
                    lblCriticalActions.setText(String.valueOf(rs.getInt("critical")));
                }
            }

            // Unique users today
            String usersQuery = "SELECT COUNT(DISTINCT user_id) as users FROM activity_logs WHERE DATE(created_at) = CURDATE()";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(usersQuery)) {
                if (rs.next()) {
                    lblUniqueUsers.setText(String.valueOf(rs.getInt("users")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    /**
     * Handle search button
     */
    @FXML
    private void handleSearch() {
        loadAllLogs();
        NotificationUtil.showInfo("Search completed - " + logsList.size() + " logs found", rootPane);
    }

    /**
     * Handle reset button
     */
    @FXML
    private void handleReset() {
        dpStartDate.setValue(LocalDate.now().minusDays(30));
        dpEndDate.setValue(LocalDate.now());
        cmbAction.setValue("All");
        cmbSeverity.setValue("All");
        txtUserSearch.clear();
        txtSearch.clear();
        loadAllLogs();
        NotificationUtil.showInfo("Filters reset", rootPane);
    }

    /**
     * Handle export CSV button
     */
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Activity Logs");
        fileChooser.setInitialFileName("activity_logs_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(btnExportCSV.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.write("ID,Timestamp,User,Action,Entity Type,Details,IP Address,Severity\n");

                // Write data
                for (ActivityLog log : logsList) {
                    writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        log.getId(),
                        log.getTimestamp(),
                        log.getUserName(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getDetails().replace("\"", "\"\""),
                        log.getIpAddress(),
                        log.getSeverity()
                    ));
                }

                NotificationUtil.showSuccess("Logs exported successfully to CSV", rootPane);
            } catch (Exception e) {
                NotificationUtil.showError("Failed to export logs: " + e.getMessage(), rootPane);
            }
        }
    }

    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        loadAllLogs();
        updateStatistics();
        NotificationUtil.showInfo("Activity logs refreshed", rootPane);
    }

    /**
     * Handle clear old logs button
     */
    @FXML
    private void handleClearOld() {
        TextInputDialog dialog = new TextInputDialog("90");
        dialog.setTitle("Clear Old Logs");
        dialog.setHeaderText("Delete logs older than specified days");
        dialog.setContentText("Days to keep:");

        dialog.showAndWait().ifPresent(days -> {
            try {
                int daysToKeep = Integer.parseInt(days);
                
                String query = "DELETE FROM activity_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";
                try (Connection conn = DataSource.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, daysToKeep);
                    int deleted = stmt.executeUpdate();
                    
                    NotificationUtil.showSuccess("Deleted " + deleted + " old log entries", rootPane);
                    loadAllLogs();
                    updateStatistics();
                } catch (SQLException e) {
                    NotificationUtil.showError("Failed to clear logs: " + e.getMessage(), rootPane);
                }
            } catch (NumberFormatException e) {
                NotificationUtil.showError("Please enter a valid number of days", rootPane);
            }
        });
    }

    /**
     * Log an activity (static utility method for other controllers to use)
     */
    public static void logActivity(int userId, String action, String entityType, 
                                   String details, String ipAddress, String severity) {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "INSERT INTO activity_logs (user_id, action, entity_type, details, ip_address, severity, created_at) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, action);
                stmt.setString(3, entityType);
                stmt.setString(4, details);
                stmt.setString(5, ipAddress != null ? ipAddress : "127.0.0.1");
                stmt.setString(6, severity);
                stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    /**
     * ActivityLog model class
     */
    public static class ActivityLog {
        private final SimpleStringProperty id;
        private final SimpleStringProperty timestamp;
        private final SimpleStringProperty userName;
        private final SimpleStringProperty action;
        private final SimpleStringProperty entityType;
        private final SimpleStringProperty details;
        private final SimpleStringProperty ipAddress;
        private final SimpleStringProperty severity;

        public ActivityLog(int id, String timestamp, String userName, String action,
                          String entityType, String details, String ipAddress, String severity) {
            this.id = new SimpleStringProperty(String.valueOf(id));
            this.timestamp = new SimpleStringProperty(timestamp);
            this.userName = new SimpleStringProperty(userName);
            this.action = new SimpleStringProperty(action);
            this.entityType = new SimpleStringProperty(entityType);
            this.details = new SimpleStringProperty(details);
            this.ipAddress = new SimpleStringProperty(ipAddress);
            this.severity = new SimpleStringProperty(severity);
        }

        public int getId() { return Integer.parseInt(id.get()); }
        public String getTimestamp() { return timestamp.get(); }
        public String getUserName() { return userName.get(); }
        public String getAction() { return action.get(); }
        public String getEntityType() { return entityType.get(); }
        public String getDetails() { return details.get(); }
        public String getIpAddress() { return ipAddress.get(); }
        public String getSeverity() { return severity.get(); }
    }
}
