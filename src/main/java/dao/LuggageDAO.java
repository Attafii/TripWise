package dao;

import model.Luggage;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LuggageDAO {
    // CREATE
    public void addLuggage(Luggage luggage) {
        String sql = "INSERT INTO luggage (booking_id, weight, status) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, luggage.getBookingId());
            ps.setDouble(2, luggage.getWeight());
            ps.setString(3, luggage.getStatus());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<Luggage> getLuggageByBooking(int bookingId) {
        List<Luggage> list = new ArrayList<>();
        String sql = "SELECT * FROM luggage WHERE booking_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Luggage(
                    rs.getInt("luggage_id"),
                    rs.getInt("booking_id"),
                    rs.getDouble("weight"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
