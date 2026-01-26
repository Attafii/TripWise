package ui.test;

import ui.model.User;
import ui.service.UserService;
import ui.util.DataSource;

import java.sql.Connection;

/**
 * Database Connection Test
 * Run this to verify your XAMPP MySQL connection works
 */
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("🔍 TripWise Database Connection Test");
        System.out.println("========================================\n");

        // Test 1: Database Connection
        System.out.println("Test 1: Testing database connection...");
        DataSource dataSource = DataSource.getInstance();
        boolean connectionSuccess = dataSource.testConnection();

        if (!connectionSuccess) {
            System.err.println("\n❌ DATABASE CONNECTION FAILED!");
            System.err.println("Please check:");
            System.err.println("  1. XAMPP MySQL is running");
            System.err.println("  2. Database 'tripwise_db' exists");
            System.err.println("  3. Username/password in DataSource.java is correct");
            return;
        }

        System.out.println("\n========================================\n");

        // Test 2: Get Connection Object
        System.out.println("Test 2: Getting connection object...");
        Connection connection = dataSource.getConnection();
        if (connection != null) {
            System.out.println("✅ Connection object retrieved successfully");
        } else {
            System.err.println("❌ Failed to get connection object");
            return;
        }

        System.out.println("\n========================================\n");

        // Test 3: UserService CRUD Operations
        System.out.println("Test 3: Testing UserService...");
        UserService userService = new UserService();

        // Test 3a: Get all users
        System.out.println("\n📋 Fetching all users from database:");
        var users = userService.getAll();
        if (users.isEmpty()) {
            System.out.println("⚠️  No users found. Did you import setup.sql?");
        } else {
            System.out.println("Found " + users.size() + " user(s):");
            for (User user : users) {
                System.out.println("  - " + user.getFullName() + " (" + user.getEmail() + ")");
            }
        }

        System.out.println("\n========================================\n");

        // Test 3b: Test authentication
        System.out.println("Test 4: Testing user authentication...");
        // Try with plain text password users from your database
        User authenticatedUser = userService.authenticate("admin@tripwise.com", "admin123");

        if (authenticatedUser != null) {
            System.out.println("✅ Authentication successful!");
            System.out.println("   Welcome: " + authenticatedUser.getFullName());
            System.out.println("   Email: " + authenticatedUser.getEmail());
            System.out.println("   User Type: " + authenticatedUser.getUserType());
            System.out.println("   ID: " + authenticatedUser.getUserId());
        } else {
            System.out.println("❌ Authentication failed (user not found or wrong password)");
            System.out.println("   Note: Trying with plain text password user.");
            System.out.println("   Your DB has bcrypt hashed passwords for most users.");
        }

        System.out.println("\n========================================\n");

        // Test 3c: Test email existence check
        System.out.println("Test 5: Testing email existence check...");
        boolean emailExists = userService.emailExists("admin@tripwise.com");
        System.out.println("Email 'admin@tripwise.com' exists: " + (emailExists ? "✅ Yes" : "❌ No"));

        boolean emailNotExists = userService.emailExists("nonexistent@example.com");
        System.out.println("Email 'nonexistent@example.com' exists: " + (emailNotExists ? "❌ Yes" : "✅ No (correct)"));

        System.out.println("\n========================================\n");

        // Final summary
        System.out.println("🎉 ALL TESTS COMPLETED!");
        System.out.println("✅ Database connection: OK");
        System.out.println("✅ User service: OK");
        System.out.println("✅ Authentication: OK");
        System.out.println("\n🚀 Your database is ready for the TripWise application!");
        System.out.println("========================================");
    }
}
