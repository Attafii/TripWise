package ui.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CarRental {
    public enum Status {
        PENDING_PAYMENT, CONFIRMED, CANCELLED
    }

    private String rentalId;
    private Car car;
    private String pickupLocation;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private double totalPrice;
    private Status status;

    public CarRental(Car car, String pickupLocation, LocalDate pickupDate, LocalDate returnDate) {
        this.rentalId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.car = car;
        this.pickupLocation = pickupLocation;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        
        long days = ChronoUnit.DAYS.between(pickupDate, returnDate);
        if (days < 1) days = 1; // Minimum 1 day
        this.totalPrice = car.getPricePerDay() * days;
        
        this.status = Status.PENDING_PAYMENT;
    }

    public String getRentalId() { return rentalId; }
    public Car getCar() { return car; }
    public String getPickUpLocation() { return pickupLocation; }
    public LocalDate getPickUpDate() { return pickupDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getTotalPrice() { return totalPrice; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
