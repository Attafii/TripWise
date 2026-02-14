package ui.model;

/**
 * Seat Entity - Represents an airplane seat
 */
public class Seat {

    private String seatNumber;      // e.g., "12A", "15B"
    private String row;             // e.g., "12"
    private String column;          // e.g., "A", "B", "C"
    private SeatType type;
    private SeatStatus status;
    private String flightClass;     // ECONOMIQUE, BUSINESS, etc.
    private double extraPrice;      // Extra cost for premium seats
    private boolean isWindow;
    private boolean isAisle;
    private boolean isMiddle;
    private boolean hasExtraLegroom;
    private boolean isExitRow;

    public enum SeatType {
        STANDARD,
        PREMIUM,
        EXTRA_LEGROOM,
        EXIT_ROW,
        BULKHEAD
    }

    public enum SeatStatus {
        AVAILABLE,
        OCCUPIED,
        SELECTED,
        BLOCKED,
        RESERVED
    }

    public Seat() {
        this.status = SeatStatus.AVAILABLE;
        this.type = SeatType.STANDARD;
        this.extraPrice = 0;
    }

    public Seat(String row, String column) {
        this();
        this.row = row;
        this.column = column;
        this.seatNumber = row + column;
        determinePosition(column);
    }

    public Seat(String seatNumber, SeatStatus status, String flightClass) {
        this.seatNumber = seatNumber;
        this.status = status;
        this.flightClass = flightClass;
        parseSeatNumber(seatNumber);
    }

    private void parseSeatNumber(String seatNumber) {
        if (seatNumber != null && seatNumber.length() >= 2) {
            this.row = seatNumber.substring(0, seatNumber.length() - 1);
            this.column = seatNumber.substring(seatNumber.length() - 1);
            determinePosition(column);
        }
    }

    private void determinePosition(String col) {
        // Standard airplane layout: A-B-C | D-E-F (for 6 seats per row)
        // Window: A, F
        // Aisle: C, D
        // Middle: B, E
        if ("A".equals(col) || "F".equals(col) || "K".equals(col)) {
            this.isWindow = true;
            this.isAisle = false;
            this.isMiddle = false;
        } else if ("C".equals(col) || "D".equals(col) || "G".equals(col) || "H".equals(col)) {
            this.isWindow = false;
            this.isAisle = true;
            this.isMiddle = false;
        } else {
            this.isWindow = false;
            this.isAisle = false;
            this.isMiddle = true;
        }
    }

    // Getters and Setters
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
        parseSeatNumber(seatNumber);
    }

    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }

    public String getColumn() { return column; }
    public void setColumn(String column) {
        this.column = column;
        determinePosition(column);
    }

    public SeatType getType() { return type; }
    public void setType(SeatType type) { this.type = type; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public String getFlightClass() { return flightClass; }
    public void setFlightClass(String flightClass) { this.flightClass = flightClass; }

    public double getExtraPrice() { return extraPrice; }
    public void setExtraPrice(double extraPrice) { this.extraPrice = extraPrice; }

    public boolean isWindow() { return isWindow; }
    public void setWindow(boolean window) { isWindow = window; }

    public boolean isAisle() { return isAisle; }
    public void setAisle(boolean aisle) { isAisle = aisle; }

    public boolean isMiddle() { return isMiddle; }
    public void setMiddle(boolean middle) { isMiddle = middle; }

    public boolean hasExtraLegroom() { return hasExtraLegroom; }
    public void setHasExtraLegroom(boolean hasExtraLegroom) { this.hasExtraLegroom = hasExtraLegroom; }

    public boolean isExitRow() { return isExitRow; }
    public void setExitRow(boolean exitRow) { this.isExitRow = exitRow; }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public String getPositionDescription() {
        if (isWindow) return "Window";
        if (isAisle) return "Aisle";
        return "Middle";
    }

    public String getDisplayInfo() {
        StringBuilder info = new StringBuilder(seatNumber);
        info.append(" (").append(getPositionDescription()).append(")");
        if (hasExtraLegroom) info.append(" +Legroom");
        if (isExitRow) info.append(" Exit Row");
        if (extraPrice > 0) info.append(" +$").append(String.format("%.0f", extraPrice));
        return info.toString();
    }

    @Override
    public String toString() {
        return seatNumber + " [" + status + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Seat seat = (Seat) obj;
        return seatNumber != null && seatNumber.equals(seat.seatNumber);
    }

    @Override
    public int hashCode() {
        return seatNumber != null ? seatNumber.hashCode() : 0;
    }
}
