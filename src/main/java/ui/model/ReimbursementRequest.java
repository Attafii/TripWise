package ui.model;

import java.time.LocalDate;

public class ReimbursementRequest {

    private int id;
    private String employeeName;
    private LocalDate date;
    private double amount;
    private String status;
    private String reference;

    public ReimbursementRequest(int id, String employeeName, LocalDate date,
                                double amount, String status, String reference) {
        this.id = id;
        this.employeeName = employeeName;
        this.date = date;
        this.amount = amount;
        this.status = status;
        this.reference = reference;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getReference() { return reference; }
}