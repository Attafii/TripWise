package dao;

import model.Booking;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    // CREATE
    public void addBooking(Booking booking) {
        String sql = "INSERT INTO booking (passenger_name, flight_id, seat_number) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, booking.getPassengerName());
            ps.setInt(2, booking.getFlightId());
            ps.setString(3, booking.getSeatNumber());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM booking";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Booking(
                    rs.getInt("booking_id"),
                    rs.getString("passenger_name"),
                    rs.getInt("flight_id"),
                    rs.getString("seat_number")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int addBookingReturnId(Booking booking) {
        String sql = "INSERT INTO booking (passenger_name, flight_id, seat_number) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, booking.getPassengerName());
            ps.setInt(2, booking.getFlightId());
            ps.setString(3, booking.getSeatNumber());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // booking id
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
