package ui.model;

import java.util.List;

public class Room {
    private String name;
    private double pricePerNight;
    private int capacity;
    private List<String> amenities;

    public Room(String name, double pricePerNight, int capacity, List<String> amenities) {
        this.name = name;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.amenities = amenities;
    }

    public String getName() { return name; }
    public double getPricePerNight() { return pricePerNight; }
    public int getCapacity() { return capacity; }
    public List<String> getAmenities() { return amenities; }

    @Override
    public String toString() {
        return name + " - $" + pricePerNight + "/night (Capacity: " + capacity + ")";
    }
}
