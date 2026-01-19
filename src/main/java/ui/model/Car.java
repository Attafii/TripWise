package ui.model;

public class Car {
    private String brand;
    private String model;
    private String type; // SUV, Sedan, etc.
    private int seats;
    private String transmission; // Manual, Automatic
    private String fuelType; // Petrol, Diesel, Electric
    private double pricePerDay;

    public Car(String brand, String model, String type, int seats, String transmission, String fuelType, double pricePerDay) {
        this.brand = brand;
        this.model = model;
        this.type = type;
        this.seats = seats;
        this.transmission = transmission;
        this.fuelType = fuelType;
        this.pricePerDay = pricePerDay;
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getType() { return type; }
    public int getSeats() { return seats; }
    public String getTransmission() { return transmission; }
    public String getFuelType() { return fuelType; }
    public double getPricePerDay() { return pricePerDay; }

    public String getFullName() {
        return brand + " " + model;
    }
}
