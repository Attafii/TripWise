package ui.model;

public class Flight {
    private String airline;
    private String route;
    private String departureTime;
    private double price;

    public Flight(String airline, String route, String departureTime, double price) {
        this.airline = airline;
        this.route = route;
        this.departureTime = departureTime;
        this.price = price;
    }

    public String getAirline() { return airline; }
    public String getRoute() { return route; }
    public String getDepartureTime() { return departureTime; }
    public double getPrice() { return price; }
}

