package ui.model;

import java.time.LocalDate;
import java.util.UUID;

public class HotelBooking {
    public enum Status {
        PENDING_PAYMENT, CONFIRMED, CANCELLED
    }

    private String bookingId;
    private Hotel hotel;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double totalPrice;
    private Status status;

    public HotelBooking(Hotel hotel, Room room, LocalDate checkIn, LocalDate checkOut, double totalPrice) {
        this.bookingId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.hotel = hotel;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.status = Status.PENDING_PAYMENT;
    }

    public String getBookingId() { return bookingId; }
    public Hotel getHotel() { return hotel; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public double getTotalPrice() { return totalPrice; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
