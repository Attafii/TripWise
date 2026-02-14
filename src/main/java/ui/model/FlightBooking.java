package ui.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * FlightBooking - Represents a flight reservation
 * Maps to reservations_vol table with joined data
 */
public class FlightBooking {
    private int reservationId;
    private String bookingId; // e.g., "BK001"
    private String numeroConfirmation;
    
    // Passenger Information
    private String passengerName;
    private int passengerCount;
    private String passengerEmail;
    
    // Flight Information  
    private String flightNumber;  // e.g., "AA 1234"
    private String airlineName;   // e.g., "American Airlines"
    private String flightClass;   // e.g., "Business", "Economy", "First Class"
    private LocalDateTime departureTime;
    
    // Route Information
    private String departureCity;
    private String arrivalCity;
    private LocalDate travelDate;
    
    // Booking Details
    private double totalPrice;
    private StatutReservation status;
    private LocalDateTime bookingDate;
    
    public enum StatutReservation {
        EN_ATTENTE("Pending"),
        CONFIRMEE("Confirmed"),
        ANNULEE("Cancelled"),
        TERMINEE("Completed");
        
        private final String displayName;
        
        StatutReservation(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public FlightBooking() {
        this.status = StatutReservation.EN_ATTENTE;
    }
    
    public FlightBooking(String bookingId, String passengerName, String flightNumber, 
                        String airlineName, String departureCity, String arrivalCity, 
                        double totalPrice, StatutReservation status) {
        this.bookingId = bookingId;
        this.passengerName = passengerName;
        this.flightNumber = flightNumber;
        this.airlineName = airlineName;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    
    // Getters and Setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getNumeroConfirmation() { return numeroConfirmation; }
    public void setNumeroConfirmation(String numeroConfirmation) { this.numeroConfirmation = numeroConfirmation; }
    
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    
    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }
    
    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }
    
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    
    public String getAirlineName() { return airlineName; }
    public void setAirlineName(String airlineName) { this.airlineName = airlineName; }
    
    public String getFlightClass() { return flightClass; }
    public void setFlightClass(String flightClass) { this.flightClass = flightClass; }
    
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    
    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }
    
    public String getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(String arrivalCity) { this.arrivalCity = arrivalCity; }
    
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public StatutReservation getStatus() { return status; }
    public void setStatus(StatutReservation status) { this.status = status; }
    
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    
    // Helper methods for display
    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "Unknown";
    }
    
    public String getRoute() {
        return departureCity + " → " + arrivalCity;
    }
    
    public String getPassengerInfo() {
        return passengerName + "\n" + passengerCount + " passenger" + (passengerCount > 1 ? "s" : "");
    }
    
    public String getFlightDetails() {
        StringBuilder details = new StringBuilder();
        details.append(flightNumber).append("\n");
        details.append(airlineName);
        if (flightClass != null && !flightClass.isEmpty()) {
            details.append("\n").append(flightClass);
        }
        if (departureTime != null) {
            details.append(" • ").append(departureTime.toLocalTime());
        }
        return details.toString();
    }
    
    @Override
    public String toString() {
        return "FlightBooking{" +
                "bookingId='" + bookingId + '\'' +
                ", passenger='" + passengerName + '\'' +
                ", flight='" + flightNumber + '\'' +
                ", route='" + getRoute() + '\'' +
                ", status=" + status +
                ", price=$" + totalPrice +
                '}';
    }
}
