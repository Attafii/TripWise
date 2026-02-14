package ui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * SeatMap - Represents the seat layout of an airplane
 */
public class SeatMap {

    private int volId;
    private String aircraftType;
    private int totalRows;
    private int seatsPerRow;
    private String[] columns;
    private List<Seat> seats;

    // Class sections
    private int firstClassEndRow;
    private int businessClassEndRow;
    private int premiumClassEndRow;

    // Exit rows
    private List<Integer> exitRows;

    public SeatMap() {
        this.seats = new ArrayList<>();
        this.exitRows = new ArrayList<>();
    }

    /**
     * Generate a standard seat map based on aircraft type
     */
    public static SeatMap generateForAircraft(String aircraftType, int volId) {
        SeatMap seatMap = new SeatMap();
        seatMap.setVolId(volId);
        seatMap.setAircraftType(aircraftType);

        switch (aircraftType != null ? aircraftType.toUpperCase() : "") {
            case "AIRBUS A380":
                seatMap.generateA380Layout();
                break;
            case "BOEING 777":
                seatMap.generateB777Layout();
                break;
            case "AIRBUS A350":
                seatMap.generateA350Layout();
                break;
            case "BOEING 787":
                seatMap.generateB787Layout();
                break;
            case "AIRBUS A320":
            case "AIRBUS A321":
            case "BOEING 737":
            default:
                seatMap.generateNarrowBodyLayout();
                break;
        }

        return seatMap;
    }

    // Generate narrow body layout (A320, 737, etc.)
    private void generateNarrowBodyLayout() {
        this.totalRows = 30;
        this.seatsPerRow = 6;
        this.columns = new String[]{"A", "B", "C", "D", "E", "F"};
        this.firstClassEndRow = 0;
        this.businessClassEndRow = 3;
        this.premiumClassEndRow = 6;
        this.exitRows.add(10);
        this.exitRows.add(20);

        generateSeats();
    }

    // Generate A380 layout
    private void generateA380Layout() {
        this.totalRows = 50;
        this.seatsPerRow = 10;
        this.columns = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "J", "K"};
        this.firstClassEndRow = 5;
        this.businessClassEndRow = 15;
        this.premiumClassEndRow = 25;
        this.exitRows.add(15);
        this.exitRows.add(30);
        this.exitRows.add(45);

        generateSeats();
    }

    // Generate 777 layout
    private void generateB777Layout() {
        this.totalRows = 40;
        this.seatsPerRow = 9;
        this.columns = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "K"};
        this.firstClassEndRow = 4;
        this.businessClassEndRow = 12;
        this.premiumClassEndRow = 20;
        this.exitRows.add(12);
        this.exitRows.add(25);

        generateSeats();
    }

    // Generate A350 layout
    private void generateA350Layout() {
        this.totalRows = 38;
        this.seatsPerRow = 9;
        this.columns = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "K"};
        this.firstClassEndRow = 3;
        this.businessClassEndRow = 10;
        this.premiumClassEndRow = 18;
        this.exitRows.add(10);
        this.exitRows.add(24);

        generateSeats();
    }

    // Generate 787 layout
    private void generateB787Layout() {
        this.totalRows = 36;
        this.seatsPerRow = 9;
        this.columns = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "K"};
        this.firstClassEndRow = 3;
        this.businessClassEndRow = 10;
        this.premiumClassEndRow = 16;
        this.exitRows.add(10);
        this.exitRows.add(22);

        generateSeats();
    }

    private void generateSeats() {
        seats.clear();

        for (int row = 1; row <= totalRows; row++) {
            for (String col : columns) {
                Seat seat = new Seat(String.valueOf(row), col);

                // Determine class
                if (row <= firstClassEndRow) {
                    seat.setFlightClass("PREMIERE");
                    seat.setExtraPrice(50);
                    seat.setHasExtraLegroom(true);
                } else if (row <= businessClassEndRow) {
                    seat.setFlightClass("BUSINESS");
                    seat.setExtraPrice(30);
                    seat.setHasExtraLegroom(true);
                } else if (row <= premiumClassEndRow) {
                    seat.setFlightClass("PREMIUM");
                    seat.setExtraPrice(15);
                } else {
                    seat.setFlightClass("ECONOMIQUE");
                }

                // Exit row seats
                if (exitRows.contains(row)) {
                    seat.setExitRow(true);
                    seat.setHasExtraLegroom(true);
                    seat.setType(Seat.SeatType.EXIT_ROW);
                    if (!"PREMIERE".equals(seat.getFlightClass()) && !"BUSINESS".equals(seat.getFlightClass())) {
                        seat.setExtraPrice(20);
                    }
                }

                // Front row extra legroom
                if (row == 1 || row == firstClassEndRow + 1 || row == businessClassEndRow + 1 || row == premiumClassEndRow + 1) {
                    seat.setType(Seat.SeatType.EXTRA_LEGROOM);
                    seat.setHasExtraLegroom(true);
                }

                seats.add(seat);
            }
        }
    }

    /**
     * Mark seats as occupied based on existing bookings
     */
    public void markOccupiedSeats(List<String> occupiedSeatNumbers) {
        for (String seatNum : occupiedSeatNumbers) {
            for (Seat seat : seats) {
                if (seat.getSeatNumber().equals(seatNum)) {
                    seat.setStatus(Seat.SeatStatus.OCCUPIED);
                    break;
                }
            }
        }
    }

    /**
     * Get seats by row
     */
    public List<Seat> getSeatsByRow(int row) {
        List<Seat> rowSeats = new ArrayList<>();
        for (Seat seat : seats) {
            if (String.valueOf(row).equals(seat.getRow())) {
                rowSeats.add(seat);
            }
        }
        return rowSeats;
    }

    /**
     * Get available seats by class
     */
    public List<Seat> getAvailableSeatsByClass(String flightClass) {
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.isAvailable() && flightClass.equals(seat.getFlightClass())) {
                available.add(seat);
            }
        }
        return available;
    }

    /**
     * Get available window seats
     */
    public List<Seat> getAvailableWindowSeats() {
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.isAvailable() && seat.isWindow()) {
                available.add(seat);
            }
        }
        return available;
    }

    /**
     * Get available aisle seats
     */
    public List<Seat> getAvailableAisleSeats() {
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.isAvailable() && seat.isAisle()) {
                available.add(seat);
            }
        }
        return available;
    }

    /**
     * Find seat by number
     */
    public Seat findSeat(String seatNumber) {
        for (Seat seat : seats) {
            if (seat.getSeatNumber().equals(seatNumber)) {
                return seat;
            }
        }
        return null;
    }

    /**
     * Count available seats
     */
    public int countAvailableSeats() {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.isAvailable()) count++;
        }
        return count;
    }

    /**
     * Count available seats by class
     */
    public int countAvailableSeatsByClass(String flightClass) {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.isAvailable() && flightClass.equals(seat.getFlightClass())) {
                count++;
            }
        }
        return count;
    }

    // Getters and Setters
    public int getVolId() { return volId; }
    public void setVolId(int volId) { this.volId = volId; }

    public String getAircraftType() { return aircraftType; }
    public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }

    public String[] getColumns() { return columns; }
    public void setColumns(String[] columns) { this.columns = columns; }

    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }

    public List<Integer> getExitRows() { return exitRows; }
    public void setExitRows(List<Integer> exitRows) { this.exitRows = exitRows; }
}
