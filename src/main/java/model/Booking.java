package model;

import java.time.LocalDateTime;

public class Booking {
    private int id;
    private String passengerName;
    private int flightId;
    private String seatNumber;
    private LocalDateTime bookingDate; // NEW

    // Full constructor
    public Booking(int id, String passengerName, int flightId, String seatNumber, LocalDateTime bookingDate){
        this.id = id;
        this.passengerName = passengerName;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
    }

    // Old constructor kept for convenience when adding (date comes from DB)
    public Booking(int id, String passengerName, int flightId, String seatNumber){
        this(id, passengerName, flightId, seatNumber, null);
    }

    // Constructor for adding a new booking
    public Booking(String passengerName, int flightId, String seatNumber){
        this(0, passengerName, flightId, seatNumber, null);
    }

    public int getId(){ return id; }
    public String getPassengerName(){ return passengerName; }
    public int getFlightId(){ return flightId; }
    public String getSeatNumber(){ return seatNumber; }
    public LocalDateTime getBookingDate(){ return bookingDate; } // NEW

    public void setId(int id){ this.id = id; }
    public void setPassengerName(String v){ this.passengerName = v; }
    public void setFlightId(int v){ this.flightId = v; }
    public void setSeatNumber(String v){ this.seatNumber = v; }
    public void setBookingDate(LocalDateTime dt){ this.bookingDate = dt; } // NEW
}