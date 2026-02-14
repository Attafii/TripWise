package ui.model;

import java.time.LocalDateTime;

/**
 * LuggageTracking model - Track checked baggage
 */
public class LuggageTracking {
    private int luggageId;
    private int reservationId;
    private String baggageTag;
    private int voyageurId;
    private String passengerName;
    private String flightNumber;
    private LuggageStatus status;
    private String currentLocation;
    private String destination;
    private double weight;
    private String description;
    private LocalDateTime checkedInAt;
    private LocalDateTime lastScanned;
    private String[] scanHistory;
    private boolean isDelayed;
    private boolean isLost;
    private String contactPhone;
    private String remarks;
    
    // Luggage Status enum
    public enum LuggageStatus {
        CHECKED_IN("Checked In", "#3b82f6"),
        IN_TRANSIT("In Transit", "#06b6d4"),
        AT_DESTINATION("At Destination", "#10b981"),
        READY_FOR_PICKUP("Ready for Pickup", "#10b981"),
        PICKED_UP("Picked Up", "#6b7280"),
        DELAYED("Delayed", "#f59e0b"),
        LOST("Lost", "#ef4444"),
        DAMAGED("Damaged", "#ef4444");
        
        private final String display;
        private final String color;
        
        LuggageStatus(String display, String color) {
            this.display = display;
            this.color = color;
        }
        
        public String getDisplay() {
            return display;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    // Constructors
    public LuggageTracking() {
        this.checkedInAt = LocalDateTime.now();
        this.lastScanned = LocalDateTime.now();
    }
    
    public LuggageTracking(String baggageTag, int voyageurId, String flightNumber) {
        this.baggageTag = baggageTag;
        this.voyageurId = voyageurId;
        this.flightNumber = flightNumber;
        this.status = LuggageStatus.CHECKED_IN;
        this.checkedInAt = LocalDateTime.now();
        this.lastScanned = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getLuggageId() {
        return luggageId;
    }
    
    public void setLuggageId(int luggageId) {
        this.luggageId = luggageId;
    }
    
    public int getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
    
    public String getBaggageTag() {
        return baggageTag;
    }
    
    public void setBaggageTag(String baggageTag) {
        this.baggageTag = baggageTag;
    }
    
    public int getVoyageurId() {
        return voyageurId;
    }
    
    public void setVoyageurId(int voyageurId) {
        this.voyageurId = voyageurId;
    }
    
    public String getPassengerName() {
        return passengerName;
    }
    
    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }
    
    public String getFlightNumber() {
        return flightNumber;
    }
    
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }
    
    public LuggageStatus getStatus() {
        return status;
    }
    
    public void setStatus(LuggageStatus status) {
        this.status = status;
    }
    
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }
    
    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }
    
    public LocalDateTime getLastScanned() {
        return lastScanned;
    }
    
    public void setLastScanned(LocalDateTime lastScanned) {
        this.lastScanned = lastScanned;
    }
    
    public String[] getScanHistory() {
        return scanHistory;
    }
    
    public void setScanHistory(String[] scanHistory) {
        this.scanHistory = scanHistory;
    }
    
    public boolean isDelayed() {
        return isDelayed;
    }
    
    public void setDelayed(boolean delayed) {
        isDelayed = delayed;
    }
    
    public boolean isLost() {
        return isLost;
    }
    
    public void setLost(boolean lost) {
        isLost = lost;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    /**
     * Add scan to history
     */
    public void addScan(String location) {
        this.currentLocation = location;
        this.lastScanned = LocalDateTime.now();
    }
    
    /**
     * Get status icon
     */
    public String getStatusIcon() {
        switch (status) {
            case CHECKED_IN: return "✓";
            case IN_TRANSIT: return "✈";
            case AT_DESTINATION: return "📍";
            case READY_FOR_PICKUP: return "📦";
            case PICKED_UP: return "✓";
            case DELAYED: return "⏱";
            case LOST: return "❌";
            case DAMAGED: return "⚠";
            default: return "•";
        }
    }
}
