package ui.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing database connections.
 * Handles connection to MySQL database (XAMPP).
 */
public class DataSource {

    // Singleton instance
    private static DataSource instance;

    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/tripwise_db?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Default XAMPP password is empty
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Connection object
    private Connection connection;

    /**
     * Private constructor for Singleton pattern
     */
    private DataSource() {
        try {
            // Load MySQL JDBC Driver
            Class.forName(DRIVER);
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    /**
     * Get singleton instance
     */
    public static DataSource getInstance() {
        if (instance == null) {
            synchronized (DataSource.class) {
                if (instance == null) {
                    instance = new DataSource();
                }
            }
        }
        return instance;
    }

    /**
     * Establish and return database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is closed or null
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("✅ Database connection established");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database!");
            System.err.println("Make sure XAMPP MySQL is running on localhost:3306");
            System.err.println("Database 'tripwise_db' should exist");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection");
            e.printStackTrace();
        }
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection test SUCCESSFUL");
                System.out.println("📊 Database: " + conn.getCatalog());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test FAILED");
            e.printStackTrace();
        }
        return false;
    }
}
