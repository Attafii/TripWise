package ui.service;

import ui.model.Room;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomService implements IService<Room> {

    private final Connection connection;

    public RoomService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(Room room) {
        String query = "INSERT INTO chambres (hotel_id, numero_chambre, type_chambre, prix_nuit, " +
                      "capacite_adultes, capacite_enfants, superficie, equipements, is_available, created_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, room.getHotelId());
            stmt.setString(2, room.getNumeroChambre());
            stmt.setString(3, room.getTypeChambre() != null ? room.getTypeChambre().name() : "STANDARD");
            stmt.setBigDecimal(4, room.getPrixNuit());
            stmt.setInt(5, room.getCapaciteAdultes());
            stmt.setInt(6, room.getCapaciteEnfants());
            stmt.setDouble(7, room.getSuperficie() != null ? room.getSuperficie() : 0.0);
            stmt.setString(8, room.getEquipements());
            stmt.setBoolean(9, room.isAvailable());
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    room.setChambreId(rs.getInt(1));
                }
                System.out.println("✅ Room added successfully: " + room.getNumeroChambre());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding room: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Room room) {
        String query = "UPDATE chambres SET hotel_id=?, numero_chambre=?, type_chambre=?, prix_nuit=?, " +
                      "capacite_adultes=?, capacite_enfants=?, superficie=?, equipements=?, is_available=? " +
                      "WHERE chambre_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, room.getHotelId());
            stmt.setString(2, room.getNumeroChambre());
            stmt.setString(3, room.getTypeChambre() != null ? room.getTypeChambre().name() : "STANDARD");
            stmt.setBigDecimal(4, room.getPrixNuit());
            stmt.setInt(5, room.getCapaciteAdultes());
            stmt.setInt(6, room.getCapaciteEnfants());
            stmt.setDouble(7, room.getSuperficie() != null ? room.getSuperficie() : 0.0);
            stmt.setString(8, room.getEquipements());
            stmt.setBoolean(9, room.isAvailable());
            stmt.setInt(10, room.getChambreId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Room updated successfully: " + room.getNumeroChambre());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating room: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String query = "UPDATE chambres SET is_available = 0 WHERE chambre_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Room deleted successfully (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting room: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Room getById(int id) {
        String query = "SELECT c.*, h.nom_hotel FROM chambres c " +
                      "JOIN hotels h ON c.hotel_id = h.hotel_id " +
                      "WHERE c.chambre_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractRoomFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting room by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT c.*, h.nom_hotel FROM chambres c " +
                      "JOIN hotels h ON c.hotel_id = h.hotel_id " +
                      "ORDER BY h.nom_hotel, c.numero_chambre";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + rooms.size() + " rooms from database");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all rooms: " + e.getMessage());
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Room> getRoomsByHotel(int hotelId) {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT c.*, h.nom_hotel FROM chambres c " +
                      "JOIN hotels h ON c.hotel_id = h.hotel_id " +
                      "WHERE c.hotel_id = ? " +
                      "ORDER BY c.numero_chambre";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting rooms by hotel: " + e.getMessage());
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Room> getAvailableRoomsByHotel(int hotelId, LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT c.*, h.nom_hotel FROM chambres c " +
                      "JOIN hotels h ON c.hotel_id = h.hotel_id " +
                      "WHERE c.hotel_id = ? AND c.is_available = 1 " +
                      "AND c.chambre_id NOT IN (" +
                      "  SELECT chambre_id FROM reservations_hotel " +
                      "  WHERE (date_checkin <= ? AND date_checkout >= ?) " +
                      "  AND statut_reservation != 'ANNULEE'" +
                      ") " +
                      "ORDER BY c.numero_chambre";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            stmt.setDate(2, Date.valueOf(checkOut));
            stmt.setDate(3, Date.valueOf(checkIn));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting available rooms: " + e.getMessage());
            e.printStackTrace();
        }
        return rooms;
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        Room room = new Room();

        room.setChambreId(rs.getInt("chambre_id"));
        room.setHotelId(rs.getInt("hotel_id"));
        room.setNumeroChambre(rs.getString("numero_chambre"));
        
        String typeStr = rs.getString("type_chambre");
        if (typeStr != null) {
            room.setTypeChambre(Room.TypeChambre.valueOf(typeStr));
        }
        
        room.setPrixNuit(rs.getBigDecimal("prix_nuit"));
        room.setCapaciteAdultes(rs.getInt("capacite_adultes"));
        room.setCapaciteEnfants(rs.getInt("capacite_enfants"));
        room.setSuperficie(rs.getDouble("superficie"));
        room.setEquipements(rs.getString("equipements"));
        room.setAvailable(rs.getBoolean("is_available"));
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            room.setCreatedAt(created.toLocalDateTime());
        }
        
        room.setHotelName(rs.getString("nom_hotel"));

        return room;
    }
}
