package ui.util;

import org.mindrot.jbcrypt.BCrypt;
import ui.model.User;
import ui.model.User.UserType;
import ui.service.UserService;

import java.time.LocalDate;

/**
 * Utility to create initial admin account for testing
 */
public class CreateAdminAccount {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  TripWise - Admin Account Creator");
        System.out.println("===========================================\n");
        
        // Test database connection
        DataSource dataSource = DataSource.getInstance();
        if (!dataSource.testConnection()) {
            System.err.println("❌ Cannot connect to database. Please ensure:");
            System.err.println("   1. XAMPP MySQL is running");
            System.err.println("   2. Database 'tripwise_db' exists");
            System.err.println("   3. Port 3306 is available");
            return;
        }
        
        // Create UserService
        UserService userService = new UserService();
        
        // Check if admin already exists
        if (userService.emailExists("admin@tripwise.com")) {
            System.out.println("⚠️  Admin account already exists!");
            System.out.println("Email: admin@tripwise.com");
            System.out.println("\nTo reset password, delete the user and run this again.");
            return;
        }
        
        // Create admin user
        User admin = new User();
        admin.setEmail("admin@tripwise.com");
        admin.setPasswordHash("Admin123!"); // Will be hashed by UserService
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setPhoneNumber("+1 (555) 000-0000");
        admin.setUserType(UserType.ADMIN);
        admin.setDateOfBirth(LocalDate.of(1990, 1, 1));
        admin.setNationality("United States");
        admin.setPassportNumber("ADMIN001");
        admin.setAddress("123 TripWise HQ, San Francisco, CA 94102");
        admin.setActive(true);
        
        // Add to database
        if (userService.add(admin)) {
            System.out.println("\n✅ Admin account created successfully!");
            System.out.println("\n===========================================");
            System.out.println("  LOGIN CREDENTIALS");
            System.out.println("===========================================");
            System.out.println("Email:    admin@tripwise.com");
            System.out.println("Password: Admin123!");
            System.out.println("===========================================");
            System.out.println("\n⚠️  IMPORTANT: Change this password after first login!");
        } else {
            System.err.println("\n❌ Failed to create admin account");
        }
    }
}
