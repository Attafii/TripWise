package ui.service;

import ui.util.DataSource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * AnalyticsService - Provides booking analytics and statistics
 * Used by Employee Analytics Dashboard
 */
public class AnalyticsService {

    private final Connection connection;

    public AnalyticsService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    /**
     * Get daily statistics for a specific date
     */
    public DailyStats getDailyStats(LocalDate date) {
        DailyStats stats = new DailyStats();
        stats.date = date;
        
        String query = "SELECT " +
                      "COUNT(*) as booking_count, " +
                      "SUM(prix_total) as revenue " +
                      "FROM reservations_hotel " +
                      "WHERE DATE(date_reservation) = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.bookingCount = rs.getInt("booking_count");
                stats.revenue = rs.getDouble("revenue");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting daily stats: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get daily statistics for a date range
     */
    public List<DailyStats> getDailyStatsRange(LocalDate startDate, LocalDate endDate) {
        List<DailyStats> statsList = new ArrayList<>();
        
        String query = "SELECT " +
                      "DATE(date_reservation) as booking_date, " +
                      "COUNT(*) as booking_count, " +
                      "SUM(prix_total) as revenue " +
                      "FROM reservations_hotel " +
                      "WHERE DATE(date_reservation) BETWEEN ? AND ? " +
                      "GROUP BY DATE(date_reservation) " +
                      "ORDER BY booking_date";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DailyStats stats = new DailyStats();
                Date sqlDate = rs.getDate("booking_date");
                stats.date = sqlDate != null ? sqlDate.toLocalDate() : null;
                stats.bookingCount = rs.getInt("booking_count");
                stats.revenue = rs.getDouble("revenue");
                statsList.add(stats);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting daily stats range: " + e.getMessage());
        }
        
        return statsList;
    }

    /**
     * Get period statistics
     */
    public PeriodStats getPeriodStats(LocalDate startDate, LocalDate endDate) {
        PeriodStats stats = new PeriodStats();
        
        String query = "SELECT " +
                      "COUNT(*) as total_bookings, " +
                      "SUM(CASE WHEN statut_reservation = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmed, " +
                      "SUM(CASE WHEN statut_reservation = 'EN_ATTENTE' THEN 1 ELSE 0 END) as pending, " +
                      "SUM(CASE WHEN statut_reservation = 'ANNULEE' THEN 1 ELSE 0 END) as cancelled, " +
                      "SUM(prix_total) as revenue " +
                      "FROM reservations_hotel " +
                      "WHERE DATE(date_reservation) BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.totalBookings = rs.getInt("total_bookings");
                stats.confirmedBookings = rs.getInt("confirmed");
                stats.pendingBookings = rs.getInt("pending");
                stats.cancelledBookings = rs.getInt("cancelled");
                stats.totalRevenue = rs.getDouble("revenue");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting period stats: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get weekly statistics
     */
    public List<WeeklyStats> getWeeklyStats(LocalDate startDate, LocalDate endDate) {
        List<WeeklyStats> weeklyStatsList = new ArrayList<>();
        
        String query = "SELECT " +
                      "WEEK(date_reservation) as week_num, " +
                      "COUNT(*) as total, " +
                      "SUM(CASE WHEN statut_reservation = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmed, " +
                      "SUM(CASE WHEN statut_reservation = 'EN_ATTENTE' THEN 1 ELSE 0 END) as pending, " +
                      "SUM(CASE WHEN statut_reservation = 'ANNULEE' THEN 1 ELSE 0 END) as cancelled, " +
                      "SUM(prix_total) as revenue " +
                      "FROM reservations_hotel " +
                      "WHERE DATE(date_reservation) BETWEEN ? AND ? " +
                      "GROUP BY WEEK(date_reservation) " +
                      "ORDER BY week_num";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WeeklyStats stats = new WeeklyStats();
                stats.weekNumber = rs.getInt("week_num");
                stats.totalBookings = rs.getInt("total");
                stats.confirmedBookings = rs.getInt("confirmed");
                stats.pendingBookings = rs.getInt("pending");
                stats.cancelledBookings = rs.getInt("cancelled");
                stats.revenue = rs.getDouble("revenue");
                weeklyStatsList.add(stats);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting weekly stats: " + e.getMessage());
        }
        
        return weeklyStatsList;
    }

    /**
     * Get booking status distribution
     */
    public Map<String, Integer> getStatusDistribution(LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        
        String query = "SELECT " +
                      "statut_reservation, " +
                      "COUNT(*) as count " +
                      "FROM reservations_hotel " +
                      "WHERE DATE(date_reservation) BETWEEN ? AND ? " +
                      "GROUP BY statut_reservation";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String status = rs.getString("statut_reservation");
                int count = rs.getInt("count");
                
                // Convert status to readable format
                String statusLabel = status;
                switch (status) {
                    case "EN_ATTENTE": statusLabel = "Pending"; break;
                    case "CONFIRMEE": statusLabel = "Confirmed"; break;
                    case "ANNULEE": statusLabel = "Cancelled"; break;
                    case "TERMINEE": statusLabel = "Completed"; break;
                }
                
                distribution.put(statusLabel, count);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting status distribution: " + e.getMessage());
        }
        
        return distribution;
    }

    /**
     * Get monthly revenue comparison
     */
    public List<MonthlyStats> getMonthlyRevenue(int months) {
        List<MonthlyStats> monthlyStatsList = new ArrayList<>();
        
        String query = "SELECT " +
                      "YEAR(date_reservation) as year, " +
                      "MONTH(date_reservation) as month, " +
                      "COUNT(*) as bookings, " +
                      "SUM(prix_total) as revenue " +
                      "FROM reservations_hotel " +
                      "WHERE date_reservation >= DATE_SUB(NOW(), INTERVAL ? MONTH) " +
                      "GROUP BY YEAR(date_reservation), MONTH(date_reservation) " +
                      "ORDER BY year, month";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, months);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MonthlyStats stats = new MonthlyStats();
                stats.year = rs.getInt("year");
                stats.month = rs.getInt("month");
                stats.bookingCount = rs.getInt("bookings");
                stats.revenue = rs.getDouble("revenue");
                monthlyStatsList.add(stats);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting monthly revenue: " + e.getMessage());
        }
        
        return monthlyStatsList;
    }

    /**
     * Inner classes for statistics data
     */
    public static class DailyStats {
        public LocalDate date;
        public int bookingCount;
        public double revenue;
    }

    public static class PeriodStats {
        public int totalBookings;
        public int confirmedBookings;
        public int pendingBookings;
        public int cancelledBookings;
        public double totalRevenue;
    }

    public static class WeeklyStats {
        public int weekNumber;
        public int totalBookings;
        public int confirmedBookings;
        public int pendingBookings;
        public int cancelledBookings;
        public double revenue;
    }

    public static class MonthlyStats {
        public int year;
        public int month;
        public int bookingCount;
        public double revenue;
    }
}
