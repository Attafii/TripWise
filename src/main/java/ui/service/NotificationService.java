package ui.admin.service;

import ui.admin.model.Notification;
import ui.admin.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {
    private final NotificationRepository repo;
    public NotificationService(NotificationRepository repo) { this.repo = repo; }

    public void broadcast(String title, String body) {
        Notification n = new Notification(title, body, LocalDateTime.now());
        repo.save(n);
    }

    public List<Notification> history() { return repo.history(); }
}