package ui.service;

import ui.model.Notification;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements IService<Notification> {

    private final Connection connection;

    public NotificationService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(Notification notification) {
        String query = "INSERT INTO notifications (user_id, type_notification, titre, message, is_read, date_envoi, created_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getTypeNotification() != null ? notification.getTypeNotification().name() : "SYSTEM_ALERT");
            stmt.setString(3, notification.getTitre());
            stmt.setString(4, notification.getMessage());
            stmt.setBoolean(5, false);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    notification.setNotificationId(rs.getInt(1));
                }
                System.out.println("✅ Notification added successfully: " + notification.getTitre());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding notification: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Notification notification) {
        String query = "UPDATE notifications SET type_notification=?, titre=?, message=?, is_read=? " +
                      "WHERE notification_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, notification.getTypeNotification() != null ? notification.getTypeNotification().name() : "SYSTEM_ALERT");
            stmt.setString(2, notification.getTitre());
            stmt.setString(3, notification.getMessage());
            stmt.setBoolean(4, notification.isRead());
            stmt.setInt(5, notification.getNotificationId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Notification updated successfully (ID: " + notification.getNotificationId() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating notification: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM notifications WHERE notification_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Notification deleted successfully (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting notification: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Notification getById(int id) {
        String query = "SELECT * FROM notifications WHERE notification_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractNotificationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting notification by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Notification> getAll() {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications ORDER BY date_envoi DESC LIMIT 100";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + notifications.size() + " notifications from database");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all notifications: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    public List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY date_envoi DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting unread notifications: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    public boolean markAllAsRead(int userId) {
        String query = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Marked " + rowsAffected + " notifications as read for user " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error marking notifications as read: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY date_envoi DESC LIMIT 50";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(extractNotificationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting notifications by user: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    private Notification extractNotificationFromResultSet(ResultSet rs) throws SQLException {
        Notification notification = new Notification();

        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        
        String type = rs.getString("type_notification");
        if (type != null) {
            notification.setTypeNotification(Notification.TypeNotification.valueOf(type));
        }
        
        notification.setTitre(rs.getString("titre"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        
        Timestamp dateEnvoi = rs.getTimestamp("date_envoi");
        if (dateEnvoi != null) {
            notification.setDateEnvoi(dateEnvoi.toLocalDateTime());
        }
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            notification.setCreatedAt(created.toLocalDateTime());
        }

        return notification;
    }
}
