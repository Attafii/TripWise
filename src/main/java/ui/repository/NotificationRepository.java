package ui.admin.repository;

import ui.admin.model.Notification;

import java.util.List;

public interface NotificationRepository {
    List<Notification> history();
    void save(Notification n);
}