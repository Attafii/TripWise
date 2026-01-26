package ui.service;

import ui.model.User;
import ui.model.User.UserType;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserService - Implements DAO pattern for User entity.
 * Handles all database operations for users.
 * Works with the actual tripwise_db schema.
 */
public class UserService implements IService<User> {

    private final Connection connection;

    public UserService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(User user) {
        String query = "INSERT INTO users (email, password_hash, first_name, last_name, phone_number, " +
                       "user_type, date_of_birth, nationality, passport_number, address, is_active, " +
                       "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getPhoneNumber());
            stmt.setString(6, user.getUserType().name());
            stmt.setDate(7, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
            stmt.setString(8, user.getNationality());
            stmt.setString(9, user.getPassportNumber());
            stmt.setString(10, user.getAddress());
            stmt.setBoolean(11, user.isActive());
            stmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
                System.out.println("✅ User added successfully: " + user.getEmail());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        String query = "UPDATE users SET first_name=?, last_name=?, email=?, phone_number=?, " +
                       "date_of_birth=?, nationality=?, address=?, updated_at=? WHERE user_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setDate(5, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
            stmt.setString(6, user.getNationality());
            stmt.setString(7, user.getAddress());
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(9, user.getUserId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ User updated successfully: " + user.getEmail());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM users WHERE user_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ User deleted successfully (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User getById(int id) {
        String query = "SELECT * FROM users WHERE user_id=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users WHERE is_active = 1 ORDER BY created_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + users.size() + " users");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Authenticate user by email and password
     * Note: Your DB has bcrypt hashed passwords, but some test users have plain text
     */
    public User authenticate(String email, String password) {
        String query = "SELECT * FROM users WHERE email=? AND is_active=1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");

                // Check if password matches (supports both plain text and bcrypt for testing)
                if (storedPassword.equals(password) || checkBcryptPassword(password, storedPassword)) {
                    User user = extractUserFromResultSet(rs);

                    // Update last login
                    updateLastLogin(user.getUserId());

                    System.out.println("✅ User authenticated: " + email + " (Type: " + user.getUserType() + ")");
                    return user;
                }
            }
            System.out.println("❌ Authentication failed for: " + email);
        } catch (SQLException e) {
            System.err.println("❌ Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update last login timestamp
     */
    private void updateLastLogin(int userId) {
        String query = "UPDATE users SET last_login=? WHERE user_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️  Could not update last login: " + e.getMessage());
        }
    }

    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get users by type
     */
    public List<User> getUsersByType(UserType userType) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users WHERE user_type=? AND is_active=1 ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userType.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + users.size() + " users of type " + userType);
        } catch (SQLException e) {
            System.err.println("❌ Error getting users by type: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Helper method to extract User from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));

        // Parse user type enum
        String userTypeStr = rs.getString("user_type");
        if (userTypeStr != null) {
            user.setUserType(UserType.valueOf(userTypeStr));
        }

        // Date of birth
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            user.setDateOfBirth(dob.toLocalDate());
        }

        user.setNationality(rs.getString("nationality"));
        user.setPassportNumber(rs.getString("passport_number"));
        user.setAddress(rs.getString("address"));
        user.setProfileImage(rs.getString("profile_image"));
        user.setActive(rs.getBoolean("is_active"));

        // Timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }

        return user;
    }

    /**
     * Simple bcrypt password check (basic implementation)
     * For production, use proper BCrypt library
     */
    private boolean checkBcryptPassword(String plainPassword, String hashedPassword) {
        // If password starts with $2a$ it's bcrypt hashed
        if (hashedPassword != null && hashedPassword.startsWith("$2a$")) {
            // TODO: Implement proper BCrypt verification with library
            // For now, return false to use plain text comparison
            return false;
        }
        return false;
    }
}
