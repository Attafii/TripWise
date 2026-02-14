package ui.service;

import ui.model.Seat;
import ui.model.SeatMap;
import ui.util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SeatSelectionService - Manages seat selection for flights
 */
public class SeatSelectionService {

    private final Connection connection;

    public SeatSelectionService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    /**
     * Get the seat map for a specific flight
     */
    public SeatMap getSeatMapForFlight(int volId) {
        String aircraftType = getAircraftType(volId);
        SeatMap seatMap = SeatMap.generateForAircraft(aircraftType, volId);

        // Mark occupied seats
        List<String> occupiedSeats = getOccupiedSeats(volId);
        seatMap.markOccupiedSeats(occupiedSeats);

        return seatMap;
    }

    /**
     * Get aircraft type for a flight
     */
    private String getAircraftType(int volId) {
        String query = "SELECT type_avion FROM vols WHERE vol_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, volId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("type_avion");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting aircraft type: " + e.getMessage());
        }
        return "Airbus A320"; // Default
    }

    /**
     * Get all occupied seats for a flight
     */
    public List<String> getOccupiedSeats(int volId) {
        List<String> occupied = new ArrayList<>();
        String query = "SELECT sieges_attribues FROM reservations_vol " +
                      "WHERE vol_id = ? AND statut_reservation IN ('CONFIRMEE', 'EN_ATTENTE', 'EMBARQUEE') " +
                      "AND sieges_attribues IS NOT NULL";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, volId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String seats = rs.getString("sieges_attribues");
                if (seats != null && !seats.isEmpty()) {
                    // Seats are stored as "12A, 12B" format
                    String[] seatArray = seats.split(",");
                    for (String seat : seatArray) {
                        occupied.add(seat.trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting occupied seats: " + e.getMessage());
        }
        return occupied;
    }

    /**
     * Reserve seats for a booking
     */
    public boolean reserveSeats(int reservationId, List<String> seatNumbers) {
        String seatsString = String.join(", ", seatNumbers);
        String query = "UPDATE reservations_vol SET sieges_attribues = ? WHERE reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, seatsString);
            stmt.setInt(2, reservationId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Seats reserved: " + seatsString);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error reserving seats: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if specific seats are available
     */
    public boolean areSeatsAvailable(int volId, List<String> seatNumbers) {
        List<String> occupied = getOccupiedSeats(volId);

        for (String seat : seatNumbers) {
            if (occupied.contains(seat)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Auto-assign seats for passengers
     */
    public List<String> autoAssignSeats(int volId, int numPassengers, String flightClass, String preference) {
        List<String> assigned = new ArrayList<>();
        SeatMap seatMap = getSeatMapForFlight(volId);

        List<Seat> availableSeats;

        // Get seats based on preference
        switch (preference != null ? preference.toUpperCase() : "") {
            case "WINDOW":
                availableSeats = seatMap.getAvailableWindowSeats();
                break;
            case "AISLE":
                availableSeats = seatMap.getAvailableAisleSeats();
                break;
            default:
                availableSeats = seatMap.getAvailableSeatsByClass(flightClass);
        }

        // Filter by class if not enough seats from preference
        if (availableSeats.size() < numPassengers) {
            availableSeats = seatMap.getAvailableSeatsByClass(flightClass);
        }

        // Try to find adjacent seats
        List<String> adjacentSeats = findAdjacentSeats(seatMap, numPassengers, flightClass);
        if (adjacentSeats.size() == numPassengers) {
            return adjacentSeats;
        }

        // Otherwise, take first available
        int count = 0;
        for (Seat seat : availableSeats) {
            if (count >= numPassengers) break;
            if (flightClass == null || flightClass.equals(seat.getFlightClass())) {
                assigned.add(seat.getSeatNumber());
                count++;
            }
        }

        return assigned;
    }

    /**
     * Find adjacent seats (for families/groups)
     */
    private List<String> findAdjacentSeats(SeatMap seatMap, int numPassengers, String flightClass) {
        List<String> result = new ArrayList<>();

        for (int row = 1; row <= seatMap.getTotalRows(); row++) {
            List<Seat> rowSeats = seatMap.getSeatsByRow(row);
            List<String> consecutiveAvailable = new ArrayList<>();

            for (Seat seat : rowSeats) {
                if (seat.isAvailable() &&
                    (flightClass == null || flightClass.equals(seat.getFlightClass()))) {
                    consecutiveAvailable.add(seat.getSeatNumber());

                    if (consecutiveAvailable.size() == numPassengers) {
                        return consecutiveAvailable;
                    }
                } else {
                    consecutiveAvailable.clear();
                }
            }
        }

        return result;
    }

    /**
     * Get seat price breakdown
     */
    public double calculateSeatExtraCost(int volId, List<String> seatNumbers) {
        double total = 0;
        SeatMap seatMap = getSeatMapForFlight(volId);

        for (String seatNum : seatNumbers) {
            Seat seat = seatMap.findSeat(seatNum);
            if (seat != null) {
                total += seat.getExtraPrice();
            }
        }

        return total;
    }

    /**
     * Change seats for an existing booking
     */
    public boolean changeSeats(int reservationId, int volId, List<String> newSeatNumbers) {
        // First, check availability
        if (!areSeatsAvailable(volId, newSeatNumbers)) {
            System.err.println("❌ Requested seats are not available");
            return false;
        }

        // Get current seats to release them
        String currentSeatsQuery = "SELECT sieges_attribues FROM reservations_vol WHERE reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(currentSeatsQuery)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Update with new seats
                return reserveSeats(reservationId, newSeatNumbers);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error changing seats: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get seat statistics for a flight
     */
    public SeatStatistics getSeatStatistics(int volId) {
        SeatStatistics stats = new SeatStatistics();
        SeatMap seatMap = getSeatMapForFlight(volId);

        stats.totalSeats = seatMap.getSeats().size();
        stats.availableSeats = seatMap.countAvailableSeats();
        stats.occupiedSeats = stats.totalSeats - stats.availableSeats;
        stats.occupancyRate = (double) stats.occupiedSeats / stats.totalSeats * 100;

        stats.availableFirstClass = seatMap.countAvailableSeatsByClass("PREMIERE");
        stats.availableBusiness = seatMap.countAvailableSeatsByClass("BUSINESS");
        stats.availablePremium = seatMap.countAvailableSeatsByClass("PREMIUM");
        stats.availableEconomy = seatMap.countAvailableSeatsByClass("ECONOMIQUE");

        stats.availableWindow = seatMap.getAvailableWindowSeats().size();
        stats.availableAisle = seatMap.getAvailableAisleSeats().size();

        return stats;
    }

    /**
     * Inner class for seat statistics
     */
    public static class SeatStatistics {
        public int totalSeats;
        public int availableSeats;
        public int occupiedSeats;
        public double occupancyRate;
        public int availableFirstClass;
        public int availableBusiness;
        public int availablePremium;
        public int availableEconomy;
        public int availableWindow;
        public int availableAisle;

        @Override
        public String toString() {
            return String.format("Seats: %d/%d (%.1f%% full)", occupiedSeats, totalSeats, occupancyRate);
        }
    }
}
