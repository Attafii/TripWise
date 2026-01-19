package ui.model;

import java.time.LocalDate;
import java.util.UUID;

public class HotelBooking {
    public enum Status {
        PENDING_PAYMENT,
        CONFIRMED,
        CANCELLED
    }

    private String bookingId;
    private Hotel hotel;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice;
    private Status status;

    public HotelBooking(Hotel hotel, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.bookingId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.hotel = hotel;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = Status.PENDING_PAYMENT;
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        long days = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        if (days <= 0) days = 1;
        this.totalPrice = room.getPricePerNight() * days;
    }

    public String getBookingId() { return bookingId; }
    public Hotel getHotel() { return hotel; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public double getTotalPrice() { return totalPrice; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
