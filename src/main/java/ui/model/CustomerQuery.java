package ui.model;

import java.time.LocalDateTime;

/**
 * CustomerQuery - Support ticket/message entity
 * Maps to 'customer_queries' table
 */
public class CustomerQuery {

    private int queryId;
    private int userId;
    private int assignedEmployeeId;
    private String subject;
    private String message;
    private String response;
    private QueryStatus status;
    private QueryCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    
    // Display fields
    private String customerName;
    private String customerEmail;
    private String employeeName;

    public enum QueryStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }

    public enum QueryCategory {
        BOOKING_ISSUE, PAYMENT_ISSUE, CANCELLATION, MODIFICATION, COMPLAINT, GENERAL
    }

    public CustomerQuery() {
        this.status = QueryStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getQueryId() { return queryId; }
    public void setQueryId(int queryId) { this.queryId = queryId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAssignedEmployeeId() { return assignedEmployeeId; }
    public void setAssignedEmployeeId(int assignedEmployeeId) { this.assignedEmployeeId = assignedEmployeeId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public QueryStatus getStatus() { return status; }
    public void setStatus(QueryStatus status) { this.status = status; }

    public QueryCategory getCategory() { return category; }
    public void setCategory(QueryCategory category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getTicketId() {
        return "TKT" + String.format("%05d", queryId);
    }

    public String getStatusDisplay() {
        switch (status) {
            case OPEN: return "Open";
            case IN_PROGRESS: return "In Progress";
            case RESOLVED: return "Resolved";
            case CLOSED: return "Closed";
            default: return status.name();
        }
    }

    @Override
    public String toString() {
        return "CustomerQuery{" +
                "queryId=" + queryId +
                ", subject='" + subject + '\'' +
                ", status=" + status +
                ", category=" + category +
                '}';
    }
}
