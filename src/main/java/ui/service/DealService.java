package ui.service;

import ui.model.Deal;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DealService - Manages deals, offers, and promotions
 */
public class DealService {
    
    private final Connection connection;
    
    public DealService() {
        this.connection = DataSource.getInstance().getConnection();
    }
    
    /**
     * Get today's featured deal
     */
    public Deal getDailyDeal() {
        String query = "SELECT * FROM deals WHERE deal_type = 'DAILY_DEAL' AND is_active = 1 " +
                      "AND start_date <= NOW() AND end_date >= NOW() " +
                      "ORDER BY created_at DESC LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractDealFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting daily deal: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all active weekly deals
     */
    public List<Deal> getWeeklyDeals() {
        List<Deal> deals = new ArrayList<>();
        String query = "SELECT * FROM deals WHERE deal_type = 'WEEKLY_DEAL' AND is_active = 1 " +
                      "AND start_date <= NOW() AND end_date >= NOW() " +
                      "ORDER BY discount_percentage DESC LIMIT 10";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deals.add(extractDealFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + deals.size() + " weekly deals");
        } catch (SQLException e) {
            System.err.println("❌ Error getting weekly deals: " + e.getMessage());
            e.printStackTrace();
        }
        return deals;
    }
    
    /**
     * Get personalized deals based on user preferences
     */
    public List<Deal> getPersonalizedDeals(int userId, String destination) {
        List<Deal> deals = new ArrayList<>();
        String query = "SELECT * FROM deals WHERE is_active = 1 " +
                      "AND start_date <= NOW() AND end_date >= NOW() " +
                      "AND (destination LIKE ? OR destination IS NULL) " +
                      "ORDER BY discount_percentage DESC LIMIT 5";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + destination + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deals.add(extractDealFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting personalized deals: " + e.getMessage());
            e.printStackTrace();
        }
        return deals;
    }
    
    /**
     * Get all active deals by type
     */
    public List<Deal> getDealsByType(String dealType) {
        List<Deal> deals = new ArrayList<>();
        String query = "SELECT * FROM deals WHERE item_type = ? AND is_active = 1 " +
                      "AND start_date <= NOW() AND end_date >= NOW() " +
                      "ORDER BY discount_percentage DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, dealType);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deals.add(extractDealFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting deals by type: " + e.getMessage());
            e.printStackTrace();
        }
        return deals;
    }
    
    /**
     * Get all active deals
     */
    public List<Deal> getAllActiveDeals() {
        List<Deal> deals = new ArrayList<>();
        String query = "SELECT * FROM deals WHERE is_active = 1 " +
                      "AND start_date <= NOW() AND end_date >= NOW() " +
                      "ORDER BY discount_percentage DESC LIMIT 50";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deals.add(extractDealFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + deals.size() + " active deals");
        } catch (SQLException e) {
            System.err.println("❌ Error getting active deals: " + e.getMessage());
            e.printStackTrace();
        }
        return deals;
    }

    /**
     * Get flash sales (limited time offers)
     */
    public List<Deal> getFlashSales() {
        List<Deal> deals = new ArrayList<>();
        String query = "SELECT * FROM deals WHERE deal_type = 'FLASH_SALE' AND is_active = 1 " +
                      "AND start_date <= NOW() AND end_date >= NOW() " +
                      "AND available_slots > 0 " +
                      "ORDER BY end_date ASC LIMIT 6";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deals.add(extractDealFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting flash sales: " + e.getMessage());
            e.printStackTrace();
        }
        return deals;
    }
    
    /**
     * Search deals by destination or keywords
     */
    public List<Deal> searchDeals(String searchTerm) {
        List<Deal> deals = new ArrayList<>();
        String query = "SELECT * FROM deals WHERE is_active = 1 " +
                      "AND (title LIKE ? OR description LIKE ? OR destination LIKE ?) " +
                      "AND start_date <= NOW() AND end_date >= NOW() " +
                      "ORDER BY discount_percentage DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deals.add(extractDealFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching deals: " + e.getMessage());
            e.printStackTrace();
        }
        return deals;
    }
    
    /**
     * Book a deal (reduce available slots)
     */
    public boolean bookDeal(int dealId) {
        String query = "UPDATE deals SET available_slots = available_slots - 1 " +
                      "WHERE deal_id = ? AND available_slots > 0";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dealId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Deal booked successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error booking deal: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get deal by ID
     */
    public Deal getDealById(int dealId) {
        String query = "SELECT * FROM deals WHERE deal_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dealId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractDealFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting deal: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Extract Deal object from ResultSet
     */
    private Deal extractDealFromResultSet(ResultSet rs) throws SQLException {
        Deal deal = new Deal();
        deal.setDealId(rs.getInt("deal_id"));
        deal.setDealType(rs.getString("deal_type"));
        deal.setTitle(rs.getString("title"));
        deal.setDescription(rs.getString("description"));
        deal.setItemId(rs.getInt("item_id"));
        deal.setItemName(rs.getString("item_name"));
        deal.setOriginalPrice(rs.getDouble("original_price"));
        deal.setDiscountedPrice(rs.getDouble("discounted_price"));
        deal.setDiscountPercentage(rs.getDouble("discount_percentage"));
        
        Timestamp startDate = rs.getTimestamp("start_date");
        if (startDate != null) {
            deal.setStartDate(startDate.toLocalDateTime());
        }
        
        Timestamp endDate = rs.getTimestamp("end_date");
        if (endDate != null) {
            deal.setEndDate(endDate.toLocalDateTime());
        }
        
        deal.setImageUrl(rs.getString("image_url"));
        deal.setDestination(rs.getString("destination"));
        deal.setAvailableSlots(rs.getInt("available_slots"));
        deal.setActive(rs.getBoolean("is_active"));
        
        // Parse highlights JSON array if stored as string
        String highlightsStr = rs.getString("highlights");
        if (highlightsStr != null && !highlightsStr.isEmpty()) {
            deal.setHighlights(highlightsStr.split(","));
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            deal.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return deal;
    }
}
