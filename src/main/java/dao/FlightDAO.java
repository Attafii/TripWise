
package dao;

import model.Flight;
import util.DBConnection; // adapte le package si besoin

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {

    public List<Flight> getAllFlights() {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT flight_id, flight_number, departure, destination, departure_time, seats_available FROM flight";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("flight_id");
                String number = rs.getString("flight_number");
                String origin = rs.getString("departure");
                String destination = rs.getString("destination");
                Timestamp ts = rs.getTimestamp("departure_time"); // DATETIME en DB
                LocalDateTime departureTime = (ts == null) ? null : ts.toLocalDateTime();
                int seats = rs.getInt("seats_available");

                list.add(new Flight(id, number, origin, destination, departureTime, seats));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addFlight(Flight flight) {
        String sql = "INSERT INTO flight (flight_number, departure, destination, departure_time, seats_available) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flight.getFlightNumber());
            ps.setString(2, flight.getOrigin());
            ps.setString(3, flight.getDestination());

            LocalDateTime dt = flight.getDepartureTime();
            if (dt == null) {
                ps.setNull(4, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(dt));
            }

            ps.setInt(5, flight.getSeatsAvailable());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFlight(Flight flight) {
        String sql = "UPDATE flight SET flight_number=?, departure=?, destination=?, departure_time=?, seats_available=? WHERE flight_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flight.getFlightNumber());
            ps.setString(2, flight.getOrigin());
            ps.setString(3, flight.getDestination());

            LocalDateTime dt = flight.getDepartureTime();
            if (dt == null) {
                ps.setNull(4, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(dt));
            }

            ps.setInt(5, flight.getSeatsAvailable());
            ps.setInt(6, flight.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFlight(int id) {
        String sql = "DELETE FROM flight WHERE flight_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
