package ui.service;

import ui.model.Flight;
import ui.model.FlightClass;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CapacityManagementService - Track and manage flight capacity
 */
public class CapacityManagementService {

    private final Connection connection;

    public CapacityManagementService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    // ==================== CAPACITY TRACKING ====================

    /**
     * Get real-time capacity for a specific flight
     */
    public FlightCapacityInfo getFlightCapacity(int volId) {
        FlightCapacityInfo info = new FlightCapacityInfo();

        String query = """
            SELECT v.vol_id, v.numero_vol, v.capacite_totale, v.places_disponibles, v.type_avion,
                   v.date_depart, ad.ville as depart, aa.ville as arrivee,
                   ca.nom_compagnie
            FROM vols v
            JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
            JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
            JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id
            WHERE v.vol_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, volId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                info.volId = rs.getInt("vol_id");
                info.flightNumber = rs.getString("numero_vol");
                info.totalCapacity = rs.getInt("capacite_totale");
                info.availableSeats = rs.getInt("places_disponibles");
                info.bookedSeats = info.totalCapacity - info.availableSeats;
                info.occupancyRate = (double) info.bookedSeats / info.totalCapacity * 100;
                info.aircraftType = rs.getString("type_avion");
                info.route = rs.getString("depart") + " → " + rs.getString("arrivee");
                info.airline = rs.getString("nom_compagnie");

                Timestamp ts = rs.getTimestamp("date_depart");
                if (ts != null) {
                    info.departureTime = ts.toLocalDateTime();
                }

                // Get class breakdown
                info.classCapacity = getClassCapacity(volId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting flight capacity: " + e.getMessage());
        }

        return info;
    }

    /**
     * Get capacity breakdown by class for a flight
     */
    public Map<String, ClassCapacity> getClassCapacity(int volId) {
        Map<String, ClassCapacity> capacityMap = new HashMap<>();

        String query = """
            SELECT cv.type_classe as classe, cv.places_disponibles,
                   (SELECT COUNT(*) FROM reservations_vol rv 
                    WHERE rv.classe_id = cv.classe_id 
                    AND rv.statut_reservation NOT IN ('ANNULEE')) as booked
            FROM classes_vol cv
            WHERE cv.vol_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, volId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ClassCapacity cc = new ClassCapacity();
                cc.className = rs.getString("classe");
                cc.available = rs.getInt("places_disponibles");
                cc.booked = rs.getInt("booked");
                cc.total = cc.available + cc.booked;
                cc.occupancyRate = cc.total > 0 ? (double) cc.booked / cc.total * 100 : 0;

                capacityMap.put(cc.className, cc);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting class capacity: " + e.getMessage());
        }

        return capacityMap;
    }

    /**
     * Get all flights with low capacity (below threshold)
     */
    public List<FlightCapacityInfo> getLowCapacityFlights(double thresholdPercent) {
        List<FlightCapacityInfo> lowCapacity = new ArrayList<>();

        String query = """
            SELECT v.vol_id, v.numero_vol, v.capacite_totale, v.places_disponibles,
                   ad.ville as depart, aa.ville as arrivee, v.date_depart,
                   ca.nom_compagnie
            FROM vols v
            JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
            JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
            JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id
            WHERE v.is_active = 1 
            AND v.statut_vol = 'PROGRAMME'
            AND v.date_depart > NOW()
            AND ((v.capacite_totale - v.places_disponibles) / v.capacite_totale * 100) < ?
            ORDER BY v.date_depart
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, thresholdPercent);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FlightCapacityInfo info = new FlightCapacityInfo();
                info.volId = rs.getInt("vol_id");
                info.flightNumber = rs.getString("numero_vol");
                info.totalCapacity = rs.getInt("capacite_totale");
                info.availableSeats = rs.getInt("places_disponibles");
                info.bookedSeats = info.totalCapacity - info.availableSeats;
                info.occupancyRate = (double) info.bookedSeats / info.totalCapacity * 100;
                info.route = rs.getString("depart") + " → " + rs.getString("arrivee");
                info.airline = rs.getString("nom_compagnie");

                Timestamp ts = rs.getTimestamp("date_depart");
                if (ts != null) {
                    info.departureTime = ts.toLocalDateTime();
                }

                lowCapacity.add(info);
            }

            System.out.println("⚠️ Found " + lowCapacity.size() + " flights below " + thresholdPercent + "% capacity");
        } catch (SQLException e) {
            System.err.println("❌ Error getting low capacity flights: " + e.getMessage());
        }

        return lowCapacity;
    }

    /**
     * Get flights nearing full capacity
     */
    public List<FlightCapacityInfo> getHighCapacityFlights(double thresholdPercent) {
        List<FlightCapacityInfo> highCapacity = new ArrayList<>();

        String query = """
            SELECT v.vol_id, v.numero_vol, v.capacite_totale, v.places_disponibles,
                   ad.ville as depart, aa.ville as arrivee, v.date_depart,
                   ca.nom_compagnie
            FROM vols v
            JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
            JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
            JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id
            WHERE v.is_active = 1 
            AND v.statut_vol = 'PROGRAMME'
            AND v.date_depart > NOW()
            AND ((v.capacite_totale - v.places_disponibles) / v.capacite_totale * 100) >= ?
            ORDER BY v.date_depart
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, thresholdPercent);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FlightCapacityInfo info = new FlightCapacityInfo();
                info.volId = rs.getInt("vol_id");
                info.flightNumber = rs.getString("numero_vol");
                info.totalCapacity = rs.getInt("capacite_totale");
                info.availableSeats = rs.getInt("places_disponibles");
                info.bookedSeats = info.totalCapacity - info.availableSeats;
                info.occupancyRate = (double) info.bookedSeats / info.totalCapacity * 100;
                info.route = rs.getString("depart") + " → " + rs.getString("arrivee");
                info.airline = rs.getString("nom_compagnie");

                Timestamp ts = rs.getTimestamp("date_depart");
                if (ts != null) {
                    info.departureTime = ts.toLocalDateTime();
                }

                highCapacity.add(info);
            }

            System.out.println("✅ Found " + highCapacity.size() + " flights above " + thresholdPercent + "% capacity");
        } catch (SQLException e) {
            System.err.println("❌ Error getting high capacity flights: " + e.getMessage());
        }

        return highCapacity;
    }

    // ==================== CAPACITY ANALYTICS ====================

    /**
     * Get overall capacity statistics
     */
    public CapacityStats getOverallCapacityStats() {
        CapacityStats stats = new CapacityStats();

        String query = """
            SELECT 
                COUNT(*) as total_flights,
                SUM(capacite_totale) as total_capacity,
                SUM(places_disponibles) as total_available,
                AVG((capacite_totale - places_disponibles) / capacite_totale * 100) as avg_occupancy
            FROM vols
            WHERE is_active = 1 AND statut_vol = 'PROGRAMME' AND date_depart > NOW()
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                stats.totalFlights = rs.getInt("total_flights");
                stats.totalCapacity = rs.getInt("total_capacity");
                stats.totalAvailable = rs.getInt("total_available");
                stats.totalBooked = stats.totalCapacity - stats.totalAvailable;
                stats.averageOccupancy = rs.getDouble("avg_occupancy");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting capacity stats: " + e.getMessage());
        }

        // Get class distribution
        stats.classDistribution = getClassDistribution();

        return stats;
    }

    /**
     * Get capacity by class distribution
    /**
     * Get class distribution across all flights
     */
    public Map<String, Integer> getClassDistribution() {
        Map<String, Integer> distribution = new HashMap<>();

        String query = """
            SELECT cv.type_classe as classe, SUM(cv.places_disponibles) as available
            FROM classes_vol cv
            JOIN vols v ON cv.vol_id = v.vol_id
            WHERE v.is_active = 1 AND v.statut_vol = 'PROGRAMME'
            GROUP BY cv.type_classe
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                distribution.put(rs.getString("classe"), rs.getInt("available"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting class distribution: " + e.getMessage());
        }

        return distribution;
    }

    /**
     * Get capacity trend by date
     */
    public List<DailyCapacity> getCapacityTrend(LocalDate startDate, LocalDate endDate) {
        List<DailyCapacity> trend = new ArrayList<>();

        String query = """
            SELECT DATE(date_depart) as flight_date,
                   SUM(capacite_totale) as total_capacity,
                   SUM(places_disponibles) as available,
                   COUNT(*) as flight_count
            FROM vols
            WHERE is_active = 1 
            AND DATE(date_depart) BETWEEN ? AND ?
            GROUP BY DATE(date_depart)
            ORDER BY flight_date
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DailyCapacity dc = new DailyCapacity();
                dc.date = rs.getDate("flight_date").toLocalDate();
                dc.totalCapacity = rs.getInt("total_capacity");
                dc.available = rs.getInt("available");
                dc.booked = dc.totalCapacity - dc.available;
                dc.flightCount = rs.getInt("flight_count");
                dc.occupancyRate = dc.totalCapacity > 0 ? (double) dc.booked / dc.totalCapacity * 100 : 0;

                trend.add(dc);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting capacity trend: " + e.getMessage());
        }

        return trend;
    }

    /**
     * Get route capacity analysis
     */
    public List<RouteCapacity> getRouteCapacityAnalysis() {
        List<RouteCapacity> routes = new ArrayList<>();

        String query = """
            SELECT ad.ville as depart, aa.ville as arrivee,
                   COUNT(*) as flight_count,
                   SUM(v.capacite_totale) as total_capacity,
                   SUM(v.places_disponibles) as available,
                   AVG((v.capacite_totale - v.places_disponibles) / v.capacite_totale * 100) as avg_occupancy
            FROM vols v
            JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
            JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
            WHERE v.is_active = 1 AND v.statut_vol = 'PROGRAMME'
            GROUP BY ad.ville, aa.ville
            ORDER BY avg_occupancy DESC
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                RouteCapacity rc = new RouteCapacity();
                rc.departure = rs.getString("depart");
                rc.arrival = rs.getString("arrivee");
                rc.route = rc.departure + " → " + rc.arrival;
                rc.flightCount = rs.getInt("flight_count");
                rc.totalCapacity = rs.getInt("total_capacity");
                rc.available = rs.getInt("available");
                rc.booked = rc.totalCapacity - rc.available;
                rc.averageOccupancy = rs.getDouble("avg_occupancy");

                routes.add(rc);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting route capacity: " + e.getMessage());
        }

        return routes;
    }

    // ==================== CAPACITY MANAGEMENT ACTIONS ====================

    /**
     * Adjust class capacity for a flight
     */
    public boolean adjustClassCapacity(int volId, String className, int newCapacity) {
        String query = "UPDATE classes_vol SET places_disponibles = ? WHERE vol_id = ? AND classe = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newCapacity);
            stmt.setInt(2, volId);
            stmt.setString(3, className);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Capacity adjusted for " + className + " on flight " + volId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adjusting capacity: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if upgrade is available from one class to another
     */
    public boolean isUpgradeAvailable(int volId, String fromClass, String toClass) {
        Map<String, ClassCapacity> capacity = getClassCapacity(volId);
        ClassCapacity target = capacity.get(toClass);
        return target != null && target.available > 0;
    }

    /**
     * Get upgrade recommendations for a flight
     */
    public List<UpgradeRecommendation> getUpgradeRecommendations(int volId) {
        List<UpgradeRecommendation> recommendations = new ArrayList<>();
        Map<String, ClassCapacity> capacity = getClassCapacity(volId);

        // Check if economy is full but higher classes available
        ClassCapacity economy = capacity.get("ECONOMIQUE");
        ClassCapacity premium = capacity.get("PREMIUM");
        ClassCapacity business = capacity.get("BUSINESS");
        ClassCapacity first = capacity.get("PREMIERE");

        if (economy != null && economy.occupancyRate > 90) {
            if (premium != null && premium.available > 0) {
                UpgradeRecommendation rec = new UpgradeRecommendation();
                rec.fromClass = "ECONOMIQUE";
                rec.toClass = "PREMIUM";
                rec.availableSeats = premium.available;
                rec.reason = "Economy is nearly full (" + String.format("%.0f%%", economy.occupancyRate) + ")";
                recommendations.add(rec);
            }
            if (business != null && business.available > 0) {
                UpgradeRecommendation rec = new UpgradeRecommendation();
                rec.fromClass = "ECONOMIQUE";
                rec.toClass = "BUSINESS";
                rec.availableSeats = business.available;
                rec.reason = "Economy is nearly full, consider business upgrade offers";
                recommendations.add(rec);
            }
        }

        if (premium != null && premium.occupancyRate > 90 && business != null && business.available > 0) {
            UpgradeRecommendation rec = new UpgradeRecommendation();
            rec.fromClass = "PREMIUM";
            rec.toClass = "BUSINESS";
            rec.availableSeats = business.available;
            rec.reason = "Premium is nearly full";
            recommendations.add(rec);
        }

        return recommendations;
    }

    // ==================== INNER CLASSES ====================

    public static class FlightCapacityInfo {
        public int volId;
        public String flightNumber;
        public String route;
        public String airline;
        public String aircraftType;
        public LocalDateTime departureTime;
        public int totalCapacity;
        public int bookedSeats;
        public int availableSeats;
        public double occupancyRate;
        public Map<String, ClassCapacity> classCapacity;

        public String getOccupancyDisplay() {
            return String.format("%.1f%% (%d/%d)", occupancyRate, bookedSeats, totalCapacity);
        }

        public String getCapacityStatus() {
            if (occupancyRate >= 95) return "FULL";
            if (occupancyRate >= 80) return "HIGH";
            if (occupancyRate >= 50) return "MODERATE";
            if (occupancyRate >= 20) return "LOW";
            return "VERY_LOW";
        }
    }

    public static class ClassCapacity {
        public String className;
        public int total;
        public int booked;
        public int available;
        public double occupancyRate;
    }

    public static class CapacityStats {
        public int totalFlights;
        public int totalCapacity;
        public int totalBooked;
        public int totalAvailable;
        public double averageOccupancy;
        public Map<String, Integer> classDistribution;
    }

    public static class DailyCapacity {
        public LocalDate date;
        public int totalCapacity;
        public int booked;
        public int available;
        public int flightCount;
        public double occupancyRate;
    }

    public static class RouteCapacity {
        public String departure;
        public String arrival;
        public String route;
        public int flightCount;
        public int totalCapacity;
        public int booked;
        public int available;
        public double averageOccupancy;
    }

    public static class UpgradeRecommendation {
        public String fromClass;
        public String toClass;
        public int availableSeats;
        public String reason;
    }
}
