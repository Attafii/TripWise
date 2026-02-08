package ui.service;

import ui.admin.repository.NotificationRepository;
import ui.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {
    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void broadcast(String title, String body) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setBody(body);
        n.setSentAt(LocalDateTime.now());
        repo.save(n);
    }

    public List<Notification> history() {
        return repo.history();
    }
}