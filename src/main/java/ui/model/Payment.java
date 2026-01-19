package ui.model;

public class Payment {
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public Payment(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    // Simulate payment processing
    public boolean processPayment() {
        // Validate basic rules (e.g., non-empty)
        if (cardNumber == null || cardNumber.length() < 13) return false;
        if (expiryDate == null || !expiryDate.contains("/")) return false;
        if (cvv == null || cvv.length() < 3) return false;
        
        return true; // Always succeed for simulation
    }
}
