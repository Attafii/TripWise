package ui.model;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private String name;
    private String city;
    private double pricePerNight; // Base price
    private double rating;
    private String description;
    private List<Room> rooms;

    public Hotel(String name, String city, double pricePerNight, double rating, String description) {
        this.name = name;
        this.city = city;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
        this.description = description;
        this.rooms = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public double getPricePerNight() { return pricePerNight; }
    public double getRating() { return rating; }
    public String getDescription() { return description; }
    public List<Room> getRooms() { return rooms; }

    public void addRoom(Room room) {
        this.rooms.add(room);
    }
}

