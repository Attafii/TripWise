package ui.repo;

import ui.db.Database;
import ui.model.HotelBooking;
import ui.model.BookedHotelSummary;
import ui.model.CarRental;
import ui.model.BookedCarSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {
    public static void save(HotelBooking booking) {
        try {
            Connection c = Database.get();
            String sql = "INSERT INTO BOOKINGS(id, hotel_name, hotel_city, room_name, check_in, check_out, total_price, status) VALUES(?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, booking.getBookingId());
                ps.setString(2, booking.getHotel().getName());
                ps.setString(3, booking.getHotel().getCity());
                ps.setString(4, booking.getRoom().getName());
                ps.setObject(5, booking.getCheckIn());
                ps.setObject(6, booking.getCheckOut());
                ps.setDouble(7, booking.getTotalPrice());
                ps.setString(8, booking.getStatus().name());
                ps.executeUpdate();
            }
        } catch (Exception e) {
        }
    }

    public static List<BookedHotelSummary> listBookedHotels() {
        List<BookedHotelSummary> out = new ArrayList<>();
        try {
            Connection c = Database.get();
            String sql = "SELECT h.name, h.city, COUNT(b.id) AS bookings " +
                    "FROM BOOKINGS b JOIN HOTELS h ON b.hotel_name=h.name AND b.hotel_city=h.city " +
                    "WHERE UPPER(b.status)='CONFIRMED' " +
                    "GROUP BY h.name, h.city ORDER BY bookings DESC, h.city, h.name";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new BookedHotelSummary(
                                rs.getString("name"),
                                rs.getString("city"),
                                rs.getInt("bookings")));
                    }
                }
            }
        } catch (Exception e) {
            return out;
        }
        return out;
    }

    public static void save(CarRental rental) {
        try {
            Connection c = Database.get();
            String sql = "INSERT INTO CAR_RENTALS(id, brand, model, pickup_location, pickup_date, return_date, total_price, status) VALUES(?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, rental.getRentalId());
                ps.setString(2, rental.getCar().getBrand());
                ps.setString(3, rental.getCar().getModel());
                ps.setString(4, rental.getPickUpLocation());
                ps.setObject(5, rental.getPickUpDate());
                ps.setObject(6, rental.getReturnDate());
                ps.setDouble(7, rental.getTotalPrice());
                ps.setString(8, rental.getStatus().name());
                ps.executeUpdate();
            }
        } catch (Exception e) {
        }
    }

    public static List<BookedCarSummary> listBookedCars() {
        List<BookedCarSummary> out = new ArrayList<>();
        try {
            Connection c = Database.get();
            String sql = "SELECT brand, model, COUNT(id) AS bookings " +
                    "FROM CAR_RENTALS WHERE UPPER(status)='CONFIRMED' " +
                    "GROUP BY brand, model ORDER BY bookings DESC, brand, model";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new BookedCarSummary(
                                rs.getString("brand"),
                                rs.getString("model"),
                                rs.getInt("bookings")));
                    }
                }
            }
        } catch (Exception e) {
            return out;
        }
        return out;
    }
}
