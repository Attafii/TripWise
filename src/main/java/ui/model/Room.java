package ui.model;

import java.util.List;

public class Room {
    private String type; // e.g., "Standard", "Deluxe", "Suite"
    private double pricePerNight;
    private int capacity;
    private List<String> amenities;

    public Room(String type, double pricePerNight, int capacity, List<String> amenities) {
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.amenities = amenities;
    }

    public String getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public int getCapacity() { return capacity; }
    public List<String> getAmenities() { return amenities; }

    @Override
    public String toString() {
        return type + " - $" + pricePerNight + "/night";
    }
}
