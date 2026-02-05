package ui.admin.repository.impl;

import ui.admin.model.Reservation;
import ui.admin.repository.ReservationRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryReservationRepository implements ReservationRepository {
    private final List<Reservation> list = new CopyOnWriteArrayList<>();

    public InMemoryReservationRepository() {
        list.add(new Reservation("user@tripwise.com", "FLIGHT", "CONFIRMED", 320.50));
        list.add(new Reservation("user@tripwise.com", "HOTEL", "PENDING", 540.00));
        list.add(new Reservation("user@tripwise.com", "CAR", "CANCELED", 78.90));
    }

    @Override public List<Reservation> findAll() { return new ArrayList<>(list); }

    @Override public void save(Reservation r) { list.add(r); }

    @Override public void update(Reservation r) {
        // In-memory no-op for demo; could replace by id if needed
    }
}