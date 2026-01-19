package ui.model;

import java.time.LocalDate;
import java.util.UUID;

public class CarRental {
    public enum Status {
        PENDING_PAYMENT, CONFIRMED, CANCELLED
    }

    private String rentalId;
    private Car car;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private double totalPrice;
    private Status status;

    public CarRental(Car car, LocalDate pickupDate, LocalDate returnDate, double totalPrice) {
        this.rentalId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.car = car;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.totalPrice = totalPrice;
        this.status = Status.PENDING_PAYMENT;
    }

    public String getRentalId() { return rentalId; }
    public Car getCar() { return car; }
    public LocalDate getPickupDate() { return pickupDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getTotalPrice() { return totalPrice; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
