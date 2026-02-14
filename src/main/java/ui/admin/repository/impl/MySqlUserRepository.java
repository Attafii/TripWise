package ui.admin.repository.impl;

import ui.admin.repository.UserRepository;
import ui.model.Role;
import ui.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlUserRepository implements UserRepository {

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, email, full_name, role, active FROM users ORDER BY email";
        List<User> list = new ArrayList<>();
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setEmail(rs.getString("email"));
                u.setFullName(rs.getString("full_name"));
                String role = rs.getString("role");
                u.setRole(role == null ? Role.USER : Role.valueOf(role));
                u.setActive(rs.getBoolean("active"));
                list.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users", e);
        }
        return list;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, full_name, role, active FROM users WHERE email = ?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setEmail(rs.getString("email"));
                    u.setFullName(rs.getString("full_name"));
                    String role = rs.getString("role");
                    u.setRole(role == null ? Role.USER : Role.valueOf(role));
                    u.setActive(rs.getBoolean("active"));
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) {
        String update =
                "UPDATE users SET full_name=?, role=?, active=? WHERE email=?";
        String insert =
                "INSERT INTO users (id, email, full_name, role, active) VALUES (UUID(), ?, ?, ?, ?)";

        try (Connection cn = DB.get()) {
            try (PreparedStatement ps = cn.prepareStatement(update)) {
                ps.setString(1, user.getFullName());
                ps.setString(2, user.getRole() == null ? Role.USER.name() : user.getRole().name());
                ps.setBoolean(3, user.isActive());
                ps.setString(4, user.getEmail());
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement ins = cn.prepareStatement(insert)) {
                        ins.setString(1, user.getEmail());
                        ins.setString(2, user.getFullName());
                        ins.setString(3, user.getRole() == null ? Role.USER.name() : user.getRole().name());
                        ins.setBoolean(4, user.isActive());
                        ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException("deleteById is not used; prefer deleteByEmail.");
    }

    // Helper for UI
    public void deleteByEmail(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user by email", e);
        }
    }
}