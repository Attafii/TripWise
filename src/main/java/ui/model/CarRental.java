package ui.model;

import java.time.LocalDate;
import java.util.UUID;

public class CarRental {
    public enum Status {
        PENDING_PAYMENT,
        CONFIRMED,
        CANCELLED
    }

    private String rentalId;
    private Car car;
    private String pickUpLocation;
    private LocalDate pickUpDate;
    private LocalDate returnDate;
    private double totalPrice;
    private Status status;

    public CarRental(Car car, String pickUpLocation, LocalDate pickUpDate, LocalDate returnDate) {
        this.rentalId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.car = car;
        this.pickUpLocation = pickUpLocation;
        this.pickUpDate = pickUpDate;
        this.returnDate = returnDate;
        this.status = Status.PENDING_PAYMENT;
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        long days = returnDate.toEpochDay() - pickUpDate.toEpochDay();
        if (days <= 0) days = 1;
        this.totalPrice = car.getPricePerDay() * days;
    }

    public String getRentalId() { return rentalId; }
    public Car getCar() { return car; }
    public String getPickUpLocation() { return pickUpLocation; }
    public LocalDate getPickUpDate() { return pickUpDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getTotalPrice() { return totalPrice; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
