package ui.admin.repository;

import ui.model.Reservation;
import java.util.List;

public interface ReservationRepository {
    List<Reservation> findAll();
    void save(Reservation r);
    void update(Reservation r);
}