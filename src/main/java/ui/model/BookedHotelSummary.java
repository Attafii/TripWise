package ui.model;

public class BookedHotelSummary {
    private final String name;
    private final String city;
    private final int bookings;

    public BookedHotelSummary(String name, String city, int bookings) {
        this.name = name;
        this.city = city;
        this.bookings = bookings;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public int getBookings() { return bookings; }
}
