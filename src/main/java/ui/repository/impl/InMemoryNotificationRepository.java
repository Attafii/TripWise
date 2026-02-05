package ui.admin.repository.impl;

import ui.admin.model.Notification;
import ui.admin.repository.NotificationRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryNotificationRepository implements NotificationRepository {
    private final List<Notification> history = new CopyOnWriteArrayList<>();
    @Override public List<Notification> history() { return new ArrayList<>(history); }
    @Override public void save(Notification n) { history.add(n); }
}