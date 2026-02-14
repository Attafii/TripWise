package ui.admin.repository.impl;

import ui.admin.repository.NotificationRepository;
import ui.model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySqlNotificationRepository implements NotificationRepository {

    @Override
    public List<Notification> history() {
        String sql = """
            SELECT id, title, body, sent_at
            FROM notifications
            ORDER BY sent_at DESC
        """;
        List<Notification> list = new ArrayList<>();
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getString("id"));
                n.setTitle(rs.getString("title"));
                n.setBody(rs.getString("body"));
                Timestamp ts = rs.getTimestamp("sent_at");
                if (ts != null) n.setSentAt(ts.toLocalDateTime());
                list.add(n);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching notifications history", e);
        }
        return list;
    }

    @Override
    public void save(Notification n) {
        String sql = """
            INSERT INTO notifications (id, title, body, sent_at)
            VALUES (UUID(), ?, ?, ?)
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getBody());
            LocalDateTime at = n.getSentAt() == null ? LocalDateTime.now() : n.getSentAt();
            ps.setTimestamp(3, Timestamp.valueOf(at));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving notification", e);
        }
    }

    public void deleteById(String id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting notification by id", e);
        }
    }

    public int countHistory() {
        String sql = "SELECT COUNT(*) FROM notifications";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting notifications", e);
        }
    }

    public List<Notification> historyPage(int limit, int offset) {
        String sql = """
            SELECT id, title, body, sent_at
            FROM notifications
            ORDER BY sent_at DESC
            LIMIT ? OFFSET ?
        """;
        List<Notification> list = new ArrayList<>();
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getString("id"));
                    n.setTitle(rs.getString("title"));
                    n.setBody(rs.getString("body"));
                    Timestamp ts = rs.getTimestamp("sent_at");
                    if (ts != null) n.setSentAt(ts.toLocalDateTime());
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading notifications page", e);
        }
        return list;
    }
}