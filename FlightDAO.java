
package dao;

import model.Flight;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {

    // CREATE
    public void addFlight(Flight flight) {
        String sql = "INSERT INTO flight (flight_number, departure, destination) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flight.getFlightNumber());
            ps.setString(2, flight.getOrigin());
            ps.setString(3, flight.getDestination());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<Flight> getAllFlights() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT flight_id, flight_number, departure, destination FROM flight";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                flights.add(new Flight(
                        rs.getInt("flight_id"),
                        rs.getString("flight_number"),
                        rs.getString("departure"),
                        rs.getString("destination")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flights;
    }

    // UPDATE
    public void updateFlight(Flight flight) {
        String sql = "UPDATE flight SET flight_number=?, departure=?, destination=? WHERE flight_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flight.getFlightNumber());
            ps.setString(2, flight.getOrigin());
            ps.setString(3, flight.getDestination());
            ps.setInt(4, flight.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
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

    // SEARCH (by keyword in flight_number, departure, destination)
    public List<Flight> searchFlights(String keyword) {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT flight_id, flight_number, departure, destination FROM flight " +
                "WHERE flight_number LIKE ? OR departure LIKE ? OR destination LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                flights.add(new Flight(
                        rs.getInt("flight_id"),
                        rs.getString("flight_number"),
                        rs.getString("departure"),
                        rs.getString("destination")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flights;
    }

    // CREATE and return generated ID
    public int addFlightReturnId(Flight flight) {
        String sql = "INSERT INTO flight (flight_number, departure, destination) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, flight.getFlightNumber());
            ps.setString(2, flight.getOrigin());
            ps.setString(3, flight.getDestination());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
