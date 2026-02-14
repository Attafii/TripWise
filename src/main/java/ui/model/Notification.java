package ui.model;

import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private int userId;
    private TypeNotification typeNotification;
    private String titre;
    private String message;
    private boolean isRead;
    private LocalDateTime dateEnvoi;
    private LocalDateTime createdAt;

    public enum TypeNotification {
        BOOKING_CONFIRMATION, PAYMENT_SUCCESS, BOOKING_CANCELLED, SYSTEM_ALERT, PROMOTION
    }

    public Notification() {
        this.isRead = false;
        this.dateEnvoi = LocalDateTime.now();
    }

    public Notification(int userId, TypeNotification typeNotification, String titre, String message) {
        this();
        this.userId = userId;
        this.typeNotification = typeNotification;
        this.titre = titre;
        this.message = message;
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public TypeNotification getTypeNotification() { return typeNotification; }
    public void setTypeNotification(TypeNotification typeNotification) { this.typeNotification = typeNotification; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", typeNotification=" + typeNotification +
                ", titre='" + titre + '\'' +
                ", isRead=" + isRead +
                ", dateEnvoi=" + dateEnvoi +
                '}';
    }
}
