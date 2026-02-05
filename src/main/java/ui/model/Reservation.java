package ui.admin.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Reservation {
    private final String id = UUID.randomUUID().toString();
    private String userEmail;
    private String type;   // FLIGHT / HOTEL / CAR
    private String status; // PENDING / CONFIRMED / CANCELED
    private double amount;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Reservation() {}

    public Reservation(String userEmail, String type, String status, double amount) {
        this.userEmail = userEmail;
        this.type = type;
        this.status = status;
        this.amount = amount;
    }

    public String getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}