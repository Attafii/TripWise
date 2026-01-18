package ui.model;

public class Hotel {
    private String name;
    private String city;
    private double pricePerNight;
    private double rating;

    public Hotel(String name, String city, double pricePerNight, double rating) {
        this.name = name;
        this.city = city;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public double getPricePerNight() { return pricePerNight; }
    public double getRating() { return rating; }
}

