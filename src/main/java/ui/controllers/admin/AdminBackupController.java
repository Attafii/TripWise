package ui.controllers.admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ui.model.User;
import ui.util.DataSource;
import ui.util.NotificationUtil;
import ui.util.DialogUtil;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Controller for Database Backup and Restore
 * Handles database backup creation, scheduling, and restoration
 */
public class AdminBackupController {

    @FXML private StackPane rootPane;
    @FXML private TableView<BackupEntry> backupTable;
    @FXML private TableColumn<BackupEntry, String> colFileName;
    @FXML private TableColumn<BackupEntry, String> colDate;
    @FXML private TableColumn<BackupEntry, String> colSize;
    @FXML private TableColumn<BackupEntry, String> colType;
    @FXML private TableColumn<BackupEntry, Void> colActions;

    @FXML private Label lblTotalBackups;
    @FXML private Label lblTotalSize;
    @FXML private Label lblLastBackup;
    @FXML private Label lblBackupLocation;

    @FXML private TextField txtBackupLocation;
    @FXML private CheckBox chkAutoBackup;
    @FXML private ComboBox<String> cmbBackupFrequency;
    @FXML private Spinner<Integer> spnRetentionDays;
    @FXML private CheckBox chkCompressBackups;

    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;

    @FXML private Button btnCreateBackup;
    @FXML private Button btnRestoreBackup;
    @FXML private Button btnBrowse;
    @FXML private Button btnRefresh;
    @FXML private Button btnSaveSettings;

    private User currentUser;
    private String defaultBackupPath;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFrequencyComboBox();
        setupRetentionSpinner();
        loadDefaultBackupPath();
        loadBackupSettings();
        loadBackupHistory();
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
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        colType.setCellValueFactory(new PropertyValueFactory<>("backupType"));

        // Add action buttons column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnRestore = new Button("🔄 Restore");
            private final Button btnDelete = new Button("🗑️ Delete");

            {
                btnRestore.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                btnRestore.setOnAction(event -> {
                    BackupEntry backup = getTableView().getItems().get(getIndex());
                    handleRestoreBackup(backup);
                });

                btnDelete.setOnAction(event -> {
                    BackupEntry backup = getTableView().getItems().get(getIndex());
                    handleDeleteBackup(backup);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, btnRestore, btnDelete);
                    setGraphic(buttons);
                }
            }
        });
    }

    /**
     * Setup frequency combo box
     */
    private void setupFrequencyComboBox() {
        cmbBackupFrequency.getItems().addAll("Daily", "Weekly", "Monthly");
        cmbBackupFrequency.setValue("Daily");
    }

    /**
     * Setup retention days spinner
     */
    private void setupRetentionSpinner() {
        spnRetentionDays.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 365, 30));
    }

    /**
     * Load default backup path
     */
    private void loadDefaultBackupPath() {
        defaultBackupPath = System.getProperty("user.home") + File.separator + "TripWise_Backups";
        File backupDir = new File(defaultBackupPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        txtBackupLocation.setText(defaultBackupPath);
        lblBackupLocation.setText("Location: " + defaultBackupPath);
    }

    /**
     * Load backup settings from database
     */
    private void loadBackupSettings() {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT * FROM system_settings WHERE setting_key LIKE 'backup_%'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String key = rs.getString("setting_key");
                String value = rs.getString("setting_value");

                switch (key) {
                    case "backup_auto_enabled":
                        chkAutoBackup.setSelected("true".equals(value));
                        break;
                    case "backup_frequency":
                        cmbBackupFrequency.setValue(value);
                        break;
                    case "backup_retention_days":
                        spnRetentionDays.getValueFactory().setValue(Integer.parseInt(value));
                        break;
                    case "backup_compress":
                        chkCompressBackups.setSelected("true".equals(value));
                        break;
                    case "backup_location":
                        if (value != null && !value.isEmpty()) {
                            txtBackupLocation.setText(value);
                        }
                        break;
                }
            }
        } catch (SQLException e) {
            // Settings table might not exist yet, use defaults
            System.out.println("Using default backup settings");
        }
    }

    /**
     * Load backup history from directory
     */
    private void loadBackupHistory() {
        backupTable.getItems().clear();
        
        File backupDir = new File(txtBackupLocation.getText());
        if (!backupDir.exists()) {
            return;
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> 
            name.startsWith("tripwise_backup_") && (name.endsWith(".sql") || name.endsWith(".sql.gz"))
        );

        if (backupFiles != null) {
            for (File file : backupFiles) {
                BackupEntry entry = new BackupEntry(
                    file.getName(),
                    formatFileDate(file.lastModified()),
                    formatFileSize(file.length()),
                    file.getName().endsWith(".gz") ? "Compressed" : "Standard",
                    file.getAbsolutePath()
                );
                backupTable.getItems().add(entry);
            }
        }
    }

    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        int totalBackups = backupTable.getItems().size();
        long totalSize = 0;
        String lastBackupDate = "Never";

        File backupDir = new File(txtBackupLocation.getText());
        if (backupDir.exists()) {
            File[] files = backupDir.listFiles((dir, name) -> 
                name.startsWith("tripwise_backup_")
            );
            
            if (files != null && files.length > 0) {
                for (File file : files) {
                    totalSize += file.length();
                }
                
                // Find most recent backup
                long mostRecent = 0;
                for (File file : files) {
                    if (file.lastModified() > mostRecent) {
                        mostRecent = file.lastModified();
                    }
                }
                if (mostRecent > 0) {
                    lastBackupDate = formatFileDate(mostRecent);
                }
            }
        }

        lblTotalBackups.setText(String.valueOf(totalBackups));
        lblTotalSize.setText(formatFileSize(totalSize));
        lblLastBackup.setText(lastBackupDate);
    }

    /**
     * Handle create backup button
     */
    @FXML
    private void handleCreateBackup() {
        if (!DialogUtil.showConfirmation("Create Backup", 
            "This will create a full database backup. Continue?")) {
            return;
        }

        btnCreateBackup.setDisable(true);
        progressBar.setProgress(0);
        lblProgress.setText("Creating backup...");

        new Thread(() -> {
            try {
                String backupPath = createDatabaseBackup();
                
                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    lblProgress.setText("Backup completed successfully!");
                    NotificationUtil.showSuccess("Backup created: " + new File(backupPath).getName(), rootPane);
                    loadBackupHistory();
                    updateStatistics();
                    btnCreateBackup.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    lblProgress.setText("Backup failed!");
                    NotificationUtil.showError("Backup failed: " + e.getMessage(), rootPane);
                    btnCreateBackup.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Create database backup
     */
    private String createDatabaseBackup() throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "tripwise_backup_" + timestamp + ".sql";
        String backupPath = txtBackupLocation.getText() + File.separator + fileName;

        try (Connection conn = DataSource.getInstance().getConnection();
             FileWriter writer = new FileWriter(backupPath)) {

            // Get all tables
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables("tripwise_db", null, "%", new String[]{"TABLE"});

            writer.write("-- TripWise Database Backup\n");
            writer.write("-- Created: " + LocalDateTime.now().format(formatter) + "\n\n");
            writer.write("SET FOREIGN_KEY_CHECKS=0;\n\n");

            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }

            // Backup each table
            for (String tableName : tableNames) {
                Platform.runLater(() -> lblProgress.setText("Backing up table: " + tableName));
                backupTable(conn, writer, tableName);
            }

            writer.write("SET FOREIGN_KEY_CHECKS=1;\n");

            // Compress if enabled
            if (chkCompressBackups.isSelected()) {
                String compressedPath = backupPath + ".gz";
                compressFile(backupPath, compressedPath);
                new File(backupPath).delete();
                return compressedPath;
            }

            return backupPath;
        }
    }

    /**
     * Backup individual table
     */
    private void backupTable(Connection conn, FileWriter writer, String tableName) throws Exception {
        writer.write("-- Table: " + tableName + "\n");
        writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");

        // Get CREATE TABLE statement
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
            if (rs.next()) {
                writer.write(rs.getString(2) + ";\n\n");
            }
        }

        // Get table data
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                StringBuilder insert = new StringBuilder("INSERT INTO `" + tableName + "` VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    if (value == null) {
                        insert.append("NULL");
                    } else if (value instanceof String || value instanceof Date || value instanceof Timestamp) {
                        insert.append("'").append(value.toString().replace("'", "\\'")).append("'");
                    } else {
                        insert.append(value);
                    }
                    if (i < columnCount) insert.append(", ");
                }
                insert.append(");\n");
                writer.write(insert.toString());
            }
        }
        writer.write("\n");
    }

    /**
     * Compress file using GZIP
     */
    private void compressFile(String sourceFile, String targetFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, len);
            }
        }
    }

    /**
     * Handle restore backup
     */
    private void handleRestoreBackup(BackupEntry backup) {
        if (!DialogUtil.showConfirmation("Restore Backup", 
            "WARNING: This will overwrite the current database with the backup from:\n" + 
            backup.getCreatedDate() + "\n\nThis action cannot be undone. Continue?")) {
            return;
        }

        btnRestoreBackup.setDisable(true);
        progressBar.setProgress(0);
        lblProgress.setText("Restoring backup...");

        new Thread(() -> {
            try {
                restoreDatabaseBackup(backup.getFilePath());
                
                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    lblProgress.setText("Restore completed successfully!");
                    NotificationUtil.showSuccess("Database restored from backup", rootPane);
                    btnRestoreBackup.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    lblProgress.setText("Restore failed!");
                    NotificationUtil.showError("Restore failed: " + e.getMessage(), rootPane);
                    btnRestoreBackup.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Restore database from backup file
     */
    private void restoreDatabaseBackup(String backupPath) throws Exception {
        File backupFile = new File(backupPath);
        
        // Decompress if needed
        if (backupPath.endsWith(".gz")) {
            String decompressedPath = backupPath.replace(".gz", "");
            decompressFile(backupPath, decompressedPath);
            backupFile = new File(decompressedPath);
        }

        try (Connection conn = DataSource.getInstance().getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
            
            Statement stmt = conn.createStatement();
            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                sql.append(line);
                if (line.trim().endsWith(";")) {
                    stmt.execute(sql.toString());
                    sql.setLength(0);
                }
            }
        }
    }

    /**
     * Decompress GZIP file
     */
    private void decompressFile(String sourceFile, String targetFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             GZIPInputStream gzipIS = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }
    }

    /**
     * Handle delete backup
     */
    private void handleDeleteBackup(BackupEntry backup) {
        if (!DialogUtil.showConfirmation("Delete Backup", 
            "Are you sure you want to delete this backup?\n" + backup.getFileName())) {
            return;
        }

        File file = new File(backup.getFilePath());
        if (file.delete()) {
            NotificationUtil.showSuccess("Backup deleted", rootPane);
            loadBackupHistory();
            updateStatistics();
        } else {
            NotificationUtil.showError("Failed to delete backup", rootPane);
        }
    }

    /**
     * Handle browse button
     */
    @FXML
    private void handleBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Backup Location");
        File selectedDir = chooser.showDialog(btnBrowse.getScene().getWindow());
        
        if (selectedDir != null) {
            txtBackupLocation.setText(selectedDir.getAbsolutePath());
            lblBackupLocation.setText("Location: " + selectedDir.getAbsolutePath());
        }
    }

    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        loadBackupHistory();
        updateStatistics();
        NotificationUtil.showInfo("Backup list refreshed", rootPane);
    }

    /**
     * Handle save settings button
     */
    @FXML
    private void handleSaveSettings() {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            saveBackupSetting(conn, "backup_auto_enabled", String.valueOf(chkAutoBackup.isSelected()));
            saveBackupSetting(conn, "backup_frequency", cmbBackupFrequency.getValue());
            saveBackupSetting(conn, "backup_retention_days", String.valueOf(spnRetentionDays.getValue()));
            saveBackupSetting(conn, "backup_compress", String.valueOf(chkCompressBackups.isSelected()));
            saveBackupSetting(conn, "backup_location", txtBackupLocation.getText());

            NotificationUtil.showSuccess("Backup settings saved", rootPane);
        } catch (SQLException e) {
            NotificationUtil.showError("Failed to save settings: " + e.getMessage(), rootPane);
        }
    }

    /**
     * Save individual backup setting
     */
    private void saveBackupSetting(Connection conn, String key, String value) throws SQLException {
        String query = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                      "ON DUPLICATE KEY UPDATE setting_value = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.executeUpdate();
        }
    }

    /**
     * Format file date
     */
    private String formatFileDate(long timestamp) {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp), 
            java.time.ZoneId.systemDefault()
        ).format(formatter);
    }

    /**
     * Format file size
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * BackupEntry model class
     */
    public static class BackupEntry {
        private final String fileName;
        private final String createdDate;
        private final String fileSize;
        private final String backupType;
        private final String filePath;

        public BackupEntry(String fileName, String createdDate, String fileSize, 
                          String backupType, String filePath) {
            this.fileName = fileName;
            this.createdDate = createdDate;
            this.fileSize = fileSize;
            this.backupType = backupType;
            this.filePath = filePath;
        }

        public String getFileName() { return fileName; }
        public String getCreatedDate() { return createdDate; }
        public String getFileSize() { return fileSize; }
        public String getBackupType() { return backupType; }
        public String getFilePath() { return filePath; }
    }
}
