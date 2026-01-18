package ui.model;

public class Car {
    private String model;
    private String type;
    private double pricePerDay;

    public Car(String model, String type, double pricePerDay) {
        this.model = model;
        this.type = type;
        this.pricePerDay = pricePerDay;
    }

    public String getModel() { return model; }
    public String getType() { return type; }
    public double getPricePerDay() { return pricePerDay; }
}

