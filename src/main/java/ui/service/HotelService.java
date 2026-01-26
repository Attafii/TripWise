package ui.service;

import ui.model.Hotel;
import ui.util.DataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HotelService - Fetches real hotel data from database
 * Queries from: hotels table
 */
public class HotelService implements IService<Hotel> {

    private final Connection connection;

    public HotelService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(Hotel hotel) {
        // TODO: Implement when needed
        return false;
    }

    @Override
    public boolean update(Hotel hotel) {
        // TODO: Implement when needed
        return false;
    }

    @Override
    public boolean delete(int id) {
        // TODO: Implement when needed
        return false;
    }

    @Override
    public Hotel getById(int id) {
        String query = "SELECT * FROM hotels WHERE hotel_id = ? AND is_active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractHotelFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting hotel by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Hotel> getAll() {
        List<Hotel> hotels = new ArrayList<>();
        String query = "SELECT * FROM hotels WHERE is_active = 1 ORDER BY etoiles DESC, nom_hotel ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                hotels.add(extractHotelFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + hotels.size() + " hotels from database");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all hotels: " + e.getMessage());
            e.printStackTrace();
        }
        return hotels;
    }

    /**
     * Search hotels by city
     */
    public List<Hotel> searchByCity(String city) {
        List<Hotel> hotels = new ArrayList<>();
        String query = "SELECT * FROM hotels WHERE ville LIKE ? AND is_active = 1 ORDER BY etoiles DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + city + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hotels.add(extractHotelFromResultSet(rs));
            }
            System.out.println("✅ Found " + hotels.size() + " hotels in " + city);
        } catch (SQLException e) {
            System.err.println("❌ Error searching hotels: " + e.getMessage());
            e.printStackTrace();
        }
        return hotels;
    }

    /**
     * Search hotels by city and rating
     */
    public List<Hotel> searchHotels(String city, Double minRating) {
        List<Hotel> hotels = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT * FROM hotels WHERE is_active = 1 "
        );

        boolean hasCity = city != null && !city.isBlank();
        boolean hasRating = minRating != null && minRating > 0;

        if (hasCity) {
            query.append("AND ville LIKE ? ");
        }
        if (hasRating) {
            query.append("AND etoiles >= ? ");
        }

        query.append("ORDER BY etoiles DESC, nom_hotel ASC");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;

            if (hasCity) {
                stmt.setString(paramIndex++, "%" + city + "%");
            }
            if (hasRating) {
                stmt.setDouble(paramIndex++, minRating);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hotels.add(extractHotelFromResultSet(rs));
            }

            System.out.println("✅ Found " + hotels.size() + " hotels matching criteria");
        } catch (SQLException e) {
            System.err.println("❌ Error searching hotels: " + e.getMessage());
            e.printStackTrace();
        }

        return hotels;
    }

    /**
     * Get hotels with minimum price per night from chambres table
     */
    public List<Hotel> getHotelsWithPricing() {
        List<Hotel> hotels = new ArrayList<>();
        String query = "SELECT h.*, MIN(c.prix_nuit) as min_price " +
                      "FROM hotels h " +
                      "LEFT JOIN chambres c ON h.hotel_id = c.hotel_id " +
                      "WHERE h.is_active = 1 " +
                      "GROUP BY h.hotel_id " +
                      "ORDER BY h.etoiles DESC, h.nom_hotel ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Hotel hotel = extractHotelFromResultSet(rs);
                // Set minimum price from rooms
                double minPrice = rs.getDouble("min_price");
                hotel.setPricePerNight(minPrice);
                hotels.add(hotel);
            }
            System.out.println("✅ Retrieved " + hotels.size() + " hotels with pricing");
        } catch (SQLException e) {
            System.err.println("❌ Error getting hotels with pricing: " + e.getMessage());
            e.printStackTrace();
        }
        return hotels;
    }

    /**
     * Extract Hotel object from ResultSet
     */
    private Hotel extractHotelFromResultSet(ResultSet rs) throws SQLException {
        Hotel hotel = new Hotel();

        hotel.setHotelId(rs.getInt("hotel_id"));
        hotel.setNomHotel(rs.getString("nom_hotel"));
        hotel.setAdresse(rs.getString("adresse"));
        hotel.setVille(rs.getString("ville"));
        hotel.setPays(rs.getString("pays"));
        hotel.setCodePostal(rs.getString("code_postal"));

        BigDecimal etoiles = rs.getBigDecimal("etoiles");
        if (etoiles != null) {
            hotel.setEtoiles(etoiles);
            hotel.setRating(etoiles.doubleValue());
        }

        hotel.setPhoneNumber(rs.getString("phone_number"));
        hotel.setEmail(rs.getString("email"));
        hotel.setSiteWeb(rs.getString("site_web"));
        hotel.setDescription(rs.getString("description"));
        hotel.setEquipements(rs.getString("equipements"));
        hotel.setPolitiqueAnnulation(rs.getString("politique_annulation"));

        Time checkin = rs.getTime("heure_checkin");
        if (checkin != null) {
            hotel.setHeureCheckin(checkin.toLocalTime());
        }

        Time checkout = rs.getTime("heure_checkout");
        if (checkout != null) {
            hotel.setHeureCheckout(checkout.toLocalTime());
        }

        hotel.setImageUrl(rs.getString("image_url"));
        hotel.setActive(rs.getBoolean("is_active"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            hotel.setCreatedAt(created.toLocalDateTime());
        }

        return hotel;
    }
}
