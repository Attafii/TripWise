package ui.admin.repository;

import ui.admin.repository.impl.InMemoryNotificationRepository;
import ui.admin.repository.impl.InMemoryReservationRepository;
import ui.admin.repository.impl.InMemoryUserRepository;

public class RepositoryProvider {
    private static final InMemoryUserRepository USER_REPO = new InMemoryUserRepository();
    private static final InMemoryReservationRepository RES_REPO = new InMemoryReservationRepository();
    private static final InMemoryNotificationRepository NOTIF_REPO = new InMemoryNotificationRepository();

    public static InMemoryUserRepository users() { return USER_REPO; }
    public static InMemoryReservationRepository reservations() { return RES_REPO; }
    public static InMemoryNotificationRepository notifications() { return NOTIF_REPO; }
}