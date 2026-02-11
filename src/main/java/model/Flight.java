
package model;

import java.time.LocalDateTime;

public class Flight {

    private int id;
    private String flightNumber;
    private String origin;        // correspond à la colonne 'departure' en DB
    private String destination;
    private LocalDateTime departureTime; // <-- LocalDateTime au lieu de String
    private int seatsAvailable;

    // Full constructor
    public Flight(int id, String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, int seatsAvailable) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.seatsAvailable = seatsAvailable;
    }

    // Constructor without ID (insert)
    public Flight(String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, int seatsAvailable) {
        this(0, flightNumber, origin, destination, departureTime, seatsAvailable);
    }

    // Getters
    public int getId() { return id; }
    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getSeatsAvailable() { return seatsAvailable; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFlightNumber(String v) { this.flightNumber = v; }
    public void setOrigin(String v) { this.origin = v; }
    public void setDestination(String v) { this.destination = v; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public void setSeatsAvailable(int seatsAvailable) { this.seatsAvailable = seatsAvailable; }
}
