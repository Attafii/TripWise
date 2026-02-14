package ui.admin.repository.impl;

import ui.admin.repository.ReservationRepository;
import ui.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlReservationRepository implements ReservationRepository {

    @Override
    public List<Reservation> findAll() {
        String sql = """
            SELECT id, user_email, type, status, amount, created_at
            FROM reservations
            ORDER BY created_at DESC
        """;
        List<Reservation> list = new ArrayList<>();
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setUserEmail(rs.getString("user_email"));
                r.setType(rs.getString("type"));
                r.setStatus(rs.getString("status"));
                r.setAmount(rs.getDouble("amount"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
                list.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching reservations", e);
        }
        return list;
    }

    @Override
    public void save(Reservation r) {
        String sql = """
            INSERT INTO reservations (id, user_email, type, status, amount, created_at)
            VALUES (UUID(), ?, ?, ?, ?, NOW())
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, r.getUserEmail());
            ps.setString(2, r.getType());
            ps.setString(3, r.getStatus());
            ps.setDouble(4, r.getAmount());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting reservation", e);
        }
    }

    @Override
    public void update(Reservation r) {
        // Demo strategy: update the latest reservation row for that user/type
        String sql = """
            UPDATE reservations
            SET status = ?
            WHERE user_email = ?
              AND type = ?
            ORDER BY created_at DESC
            LIMIT 1
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, r.getStatus());
            ps.setString(2, r.getUserEmail());
            ps.setString(3, r.getType());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating reservation", e);
        }
    }
}