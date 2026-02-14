package ui.model;

public class BookedCarSummary {
    private final String brand;
    private final String model;
    private final int bookings;

    public BookedCarSummary(String brand, String model, int bookings) {
        this.brand = brand;
        this.model = model;
        this.bookings = bookings;
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getBookings() { return bookings; }
}
