package dao;

import model.Booking;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
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

    // CREATE returning ID
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
                return rs.getInt(1); // booking_id
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // READ
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT booking_id, passenger_name, flight_id, seat_number, booking_date FROM booking ORDER BY booking_id DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("booking_date");
                LocalDateTime bookingDate = ts != null ? ts.toLocalDateTime() : null;

                list.add(new Booking(
                        rs.getInt("booking_id"),
                        rs.getString("passenger_name"),
                        rs.getInt("flight_id"),
                        rs.getString("seat_number"),
                        bookingDate
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // UPDATE
    public void updateBooking(Booking booking) {
        String sql = "UPDATE booking SET passenger_name = ?, flight_id = ?, seat_number = ? WHERE booking_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, booking.getPassengerName());
            ps.setInt(2, booking.getFlightId());
            ps.setString(3, booking.getSeatNumber());
            ps.setInt(4, booking.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteBooking(int bookingId) {
        String sql = "DELETE FROM booking WHERE booking_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}