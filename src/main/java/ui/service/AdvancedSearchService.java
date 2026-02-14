package ui.service;

import ui.model.Hotel;
import ui.util.DataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdvancedSearchService - Multi-filter search for hotels, flights, cars
 */
public class AdvancedSearchService {
    
    private final Connection connection;
    
    public AdvancedSearchService() {
        this.connection = DataSource.getInstance().getConnection();
    }
    
    /**
     * Advanced hotel search with multiple filters
     */
    public List<Hotel> searchHotels(SearchFilters filters) {
        List<Hotel> hotels = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT DISTINCT h.* FROM hotels h " +
            "LEFT JOIN chambres c ON h.hotel_id = c.hotel_id " +
            "WHERE h.is_active = 1"
        );
        
        List<Object> params = new ArrayList<>();
        
        // Location filter
        if (filters.getDestination() != null && !filters.getDestination().isEmpty()) {
            query.append(" AND (h.ville LIKE ? OR h.pays LIKE ? OR h.adresse LIKE ?)");
            String dest = "%" + filters.getDestination() + "%";
            params.add(dest);
            params.add(dest);
            params.add(dest);
        }
        
        // Price range filter
        if (filters.getPriceMin() != null && filters.getPriceMax() != null) {
            query.append(" AND c.prix_nuit BETWEEN ? AND ?");
            params.add(filters.getPriceMin());
            params.add(filters.getPriceMax());
        } else if (filters.getPriceMin() != null) {
            query.append(" AND c.prix_nuit >= ?");
            params.add(filters.getPriceMin());
        } else if (filters.getPriceMax() != null) {
            query.append(" AND c.prix_nuit <= ?");
            params.add(filters.getPriceMax());
        }
        
        // Star rating filter
        if (filters.getMinRating() != null) {
            query.append(" AND h.etoiles >= ?");
            params.add(filters.getMinRating());
        }
        
        // Amenities filter
        if (filters.getAmenities() != null && !filters.getAmenities().isEmpty()) {
            for (String amenity : filters.getAmenities()) {
                query.append(" AND h.equipements LIKE ?");
                params.add("%" + amenity + "%");
            }
        }
        
        // Availability filter
        if (filters.getCheckInDate() != null && filters.getCheckOutDate() != null) {
            query.append(" AND c.chambre_id NOT IN (" +
                "SELECT chambre_id FROM reservations_hotel " +
                "WHERE statut_reservation = 'CONFIRMEE' " +
                "AND ((date_checkin BETWEEN ? AND ?) OR (date_checkout BETWEEN ? AND ?))" +
            ")");
            params.add(Date.valueOf(filters.getCheckInDate()));
            params.add(Date.valueOf(filters.getCheckOutDate()));
            params.add(Date.valueOf(filters.getCheckInDate()));
            params.add(Date.valueOf(filters.getCheckOutDate()));
        }
        
        // Sorting
        if (filters.getSortBy() != null) {
            switch (filters.getSortBy()) {
                case "price_low":
                    query.append(" ORDER BY c.prix_nuit ASC");
                    break;
                case "price_high":
                    query.append(" ORDER BY c.prix_nuit DESC");
                    break;
                case "rating":
                    query.append(" ORDER BY h.etoiles DESC");
                    break;
                case "name":
                    query.append(" ORDER BY h.nom_hotel ASC");
                    break;
                default:
                    query.append(" ORDER BY h.created_at DESC");
            }
        } else {
            query.append(" ORDER BY h.created_at DESC");
        }
        
        // Limit results
        if (filters.getLimit() > 0) {
            query.append(" LIMIT ?");
            params.add(filters.getLimit());
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Double) {
                    stmt.setDouble(i + 1, (Double) param);
                } else if (param instanceof Date) {
                    stmt.setDate(i + 1, (Date) param);
                }
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                hotels.add(extractHotelFromResultSet(rs));
            }
            
            System.out.println("✅ Found " + hotels.size() + " hotels matching filters");
        } catch (SQLException e) {
            System.err.println("❌ Error searching hotels: " + e.getMessage());
            e.printStackTrace();
        }
        
        return hotels;
    }
    
    /**
     * Calculate total price for stay
     */
    public double calculateTotalPrice(int hotelId, int roomId, LocalDate checkIn, LocalDate checkOut, int guests) {
        double totalPrice = 0.0;
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        
        String query = "SELECT prix_nuit FROM chambres WHERE chambre_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double pricePerNight = rs.getDouble("prix_nuit");
                totalPrice = pricePerNight * nights;
                
                // Add guest surcharge if more than 2 guests
                if (guests > 2) {
                    totalPrice += (guests - 2) * 25.0 * nights; // $25 per extra guest per night
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error calculating price: " + e.getMessage());
            e.printStackTrace();
        }
        
        return totalPrice;
    }
    
    /**
     * Get available rooms for hotel and dates
     */
    public Map<String, Integer> getAvailableRooms(int hotelId, LocalDate checkIn, LocalDate checkOut) {
        Map<String, Integer> availableRooms = new HashMap<>();
        
        String query = "SELECT c.type_chambre, COUNT(*) as available " +
                      "FROM chambres c " +
                      "WHERE c.hotel_id = ? AND c.est_disponible = 1 " +
                      "AND c.chambre_id NOT IN (" +
                      "  SELECT chambre_id FROM reservations_hotel " +
                      "  WHERE statut_reservation = 'CONFIRMEE' " +
                      "  AND ((date_checkin BETWEEN ? AND ?) OR (date_checkout BETWEEN ? AND ?))" +
                      ") " +
                      "GROUP BY c.type_chambre";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            stmt.setDate(2, Date.valueOf(checkIn));
            stmt.setDate(3, Date.valueOf(checkOut));
            stmt.setDate(4, Date.valueOf(checkIn));
            stmt.setDate(5, Date.valueOf(checkOut));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                availableRooms.put(rs.getString("type_chambre"), rs.getInt("available"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting available rooms: " + e.getMessage());
            e.printStackTrace();
        }
        
        return availableRooms;
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
        hotel.setEtoiles(BigDecimal.valueOf(rs.getInt("etoiles")));
        hotel.setPhoneNumber(rs.getString("phone_number"));
        hotel.setEmail(rs.getString("email"));
        hotel.setSiteWeb(rs.getString("site_web"));
        hotel.setDescription(rs.getString("description"));
        hotel.setEquipements(rs.getString("equipements"));
        hotel.setPolitiqueAnnulation(rs.getString("politique_annulation"));
        
        // Parse time strings
        String checkinTime = rs.getString("heure_checkin");
        if (checkinTime != null) {
            hotel.setHeureCheckin(LocalTime.parse(checkinTime));
        }
        String checkoutTime = rs.getString("heure_checkout");
        if (checkoutTime != null) {
            hotel.setHeureCheckout(LocalTime.parse(checkoutTime));
        }
        hotel.setImageUrl(rs.getString("image_url"));
        hotel.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            hotel.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return hotel;
    }
    
    /**
     * Search Filters Inner Class
     */
    public static class SearchFilters {
        private String destination;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer guests;
        private Double priceMin;
        private Double priceMax;
        private Integer minRating;
        private List<String> amenities;
        private String sortBy;
        private int limit = 50;
        
        // Getters and Setters
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        
        public LocalDate getCheckInDate() { return checkInDate; }
        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
        
        public LocalDate getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
        
        public Integer getGuests() { return guests; }
        public void setGuests(Integer guests) { this.guests = guests; }
        
        public Double getPriceMin() { return priceMin; }
        public void setPriceMin(Double priceMin) { this.priceMin = priceMin; }
        
        public Double getPriceMax() { return priceMax; }
        public void setPriceMax(Double priceMax) { this.priceMax = priceMax; }
        
        public Integer getMinRating() { return minRating; }
        public void setMinRating(Integer minRating) { this.minRating = minRating; }
        
        public List<String> getAmenities() { return amenities; }
        public void setAmenities(List<String> amenities) { this.amenities = amenities; }
        
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
    }
}
