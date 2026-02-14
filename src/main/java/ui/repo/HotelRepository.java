package ui.repo;

import ui.db.Database;
import ui.model.Hotel;
import ui.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class HotelRepository {
    public static List<Hotel> getAll() {
        Map<String, Hotel> map = new LinkedHashMap<>();
        try {
            Connection c = Database.get();
            String sql = "SELECT h.name, h.city, h.price_per_night, h.rating, h.description, r.name AS room_name, r.price_per_night AS room_price, r.capacity AS room_capacity, r.amenities AS room_amenities " +
                    "FROM HOTELS h LEFT JOIN ROOMS r ON h.name=r.hotel_name AND h.city=r.hotel_city ORDER BY h.city, h.name";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString("name") + "|" + rs.getString("city");
                        Hotel h = map.get(key);
                        if (h == null) {
                            h = new Hotel(rs.getString("name"), rs.getString("city"), rs.getDouble("price_per_night"), rs.getDouble("rating"), rs.getString("description"));
                            map.put(key, h);
                        }
                        String rn = rs.getString("room_name");
                        if (rn != null) {
                            double rp = rs.getDouble("room_price");
                            int cap = rs.getInt("room_capacity");
                            String amenities = rs.getString("room_amenities");
                            List<String> am = amenities != null && !amenities.isEmpty() ? Arrays.asList(amenities.split("\\s*,\\s*")) : Collections.emptyList();
                            h.addRoom(new Room(rn, rp, cap, am));
                        }
                    }
                }
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return new ArrayList<>(map.values());
    }

    public static Set<String> getBookedHotelKeys() {
        Set<String> set = new HashSet<>();
        try {
            Connection c = Database.get();
            String sql = "SELECT DISTINCT hotel_name, hotel_city FROM BOOKINGS WHERE UPPER(status)='CONFIRMED'";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        set.add(rs.getString("hotel_name") + "|" + rs.getString("hotel_city"));
                    }
                }
            }
        } catch (Exception e) {
            return set;
        }
        return set;
    }
}
