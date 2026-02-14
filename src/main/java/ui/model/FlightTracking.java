package ui.model;

import java.time.LocalDateTime;

/**
 * FlightTracking model - Real-time flight status tracking
 */
public class FlightTracking {
    private int trackingId;
    private int volId;
    private String flightNumber;
    private String airline;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime actualDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualArrival;
    private FlightStatus status;
    private int delayMinutes;
    private String gate;
    private String terminal;
    private String baggage;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private String remarks;
    private LocalDateTime lastUpdated;
    
    // Flight Status enum
    public enum FlightStatus {
        SCHEDULED("Scheduled", "#3b82f6"),
        BOARDING("Boarding", "#10b981"),
        DEPARTED("Departed", "#06b6d4"),
        IN_FLIGHT("In Flight", "#8b5cf6"),
        LANDED("Landed", "#10b981"),
        DELAYED("Delayed", "#f59e0b"),
        CANCELLED("Cancelled", "#ef4444"),
        DIVERTED("Diverted", "#f59e0b");
        
        private final String display;
        private final String color;
        
        FlightStatus(String display, String color) {
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
    public FlightTracking() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    public FlightTracking(int volId, String flightNumber) {
        this.volId = volId;
        this.flightNumber = flightNumber;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getTrackingId() {
        return trackingId;
    }
    
    public void setTrackingId(int trackingId) {
        this.trackingId = trackingId;
    }
    
    public int getVolId() {
        return volId;
    }
    
    public void setVolId(int volId) {
        this.volId = volId;
    }
    
    public String getFlightNumber() {
        return flightNumber;
    }
    
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }
    
    public String getAirline() {
        return airline;
    }
    
    public void setAirline(String airline) {
        this.airline = airline;
    }
    
    public String getDepartureAirport() {
        return departureAirport;
    }
    
    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }
    
    public String getArrivalAirport() {
        return arrivalAirport;
    }
    
    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }
    
    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }
    
    public void setScheduledDeparture(LocalDateTime scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }
    
    public LocalDateTime getActualDeparture() {
        return actualDeparture;
    }
    
    public void setActualDeparture(LocalDateTime actualDeparture) {
        this.actualDeparture = actualDeparture;
    }
    
    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }
    
    public void setScheduledArrival(LocalDateTime scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }
    
    public LocalDateTime getActualArrival() {
        return actualArrival;
    }
    
    public void setActualArrival(LocalDateTime actualArrival) {
        this.actualArrival = actualArrival;
    }
    
    public FlightStatus getStatus() {
        return status;
    }
    
    public void setStatus(FlightStatus status) {
        this.status = status;
    }
    
    public int getDelayMinutes() {
        return delayMinutes;
    }
    
    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = delayMinutes;
    }
    
    public String getGate() {
        return gate;
    }
    
    public void setGate(String gate) {
        this.gate = gate;
    }
    
    public String getTerminal() {
        return terminal;
    }
    
    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }
    
    public String getBaggage() {
        return baggage;
    }
    
    public void setBaggage(String baggage) {
        this.baggage = baggage;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getAltitude() {
        return altitude;
    }
    
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Get formatted delay message
     */
    public String getDelayMessage() {
        if (delayMinutes <= 0) {
            return "On Time";
        } else if (delayMinutes < 60) {
            return delayMinutes + " min delay";
        } else {
            int hours = delayMinutes / 60;
            int mins = delayMinutes % 60;
            return hours + "h " + mins + "m delay";
        }
    }
    
    /**
     * Check if flight is significantly delayed
     */
    public boolean isDelayed() {
        return delayMinutes > 15;
    }
    
    /**
     * Get route description
     */
    public String getRoute() {
        return departureAirport + " → " + arrivalAirport;
    }
}
