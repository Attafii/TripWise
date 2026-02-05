package ui.admin.service;

import ui.admin.model.Reservation;
import ui.admin.model.User;

import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    public int totalReservations(List<Reservation> rs) { return rs.size(); }

    public double totalAmount(List<Reservation> rs) {
        return rs.stream().mapToDouble(Reservation::getAmount).sum();
    }

    public long activeUsers(List<User> users) {
        return users.stream().filter(User::isActive).count();
    }

    public static class TopUserRow {
        public final String email;
        public final long count;
        public final double amount;
        public TopUserRow(String email, long count, double amount) {
            this.email = email; this.count = count; this.amount = amount;
        }
    }

    public List<TopUserRow> topUsers(List<Reservation> rs) {
        Map<String, List<Reservation>> byUser =
                rs.stream().collect(Collectors.groupingBy(Reservation::getUserEmail));

        return byUser.entrySet().stream()
                .map(e -> new TopUserRow(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().mapToDouble(Reservation::getAmount).sum()))
                .sorted(Comparator.comparingDouble((TopUserRow r) -> r.amount).reversed())
                .limit(20)
                .toList();
    }
}
