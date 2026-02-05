package ui.admin.service;

import ui.admin.model.Reservation;
import ui.admin.repository.ReservationRepository;

import java.util.List;

public class ReservationService {

    private final ReservationRepository repo;

    public ReservationService(ReservationRepository repo) {
        this.repo = repo;
    }

    public List<Reservation> all() {
        return repo.findAll();
    }

    public void verify(Reservation r) {
        r.setStatus("CONFIRMED");
        repo.update(r);
    }

    public void cancel(Reservation r) {
        r.setStatus("CANCELED");
        repo.update(r);
    }
}