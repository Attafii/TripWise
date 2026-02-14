package ui.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * ValidationUtil - Real-time form validation utilities
 * Provides validation for email, phone, passwords, dates, etc.
 */
public class ValidationUtil {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,9}$"
    );
    
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$"
    );

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && !phone.isBlank() && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validate URL
     */
    public static boolean isValidUrl(String url) {
        return url != null && !url.isBlank() && URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * Validate password strength
     * Minimum 8 characters, at least one letter and one number
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }
    
    /**
     * Validate required field (not empty)
     */
    public static boolean isRequired(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate minimum length
     */
    public static boolean minLength(String value, int min) {
        return value != null && value.length() >= min;
    }
    
    /**
     * Validate maximum length
     */
    public static boolean maxLength(String value, int max) {
        return value != null && value.length() <= max;
    }
    
    /**
     * Validate number range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate date is in the future
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }
    
    /**
     * Validate date is in the past
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }
    
    /**
     * Validate date is not more than X years ago
     */
    public static boolean isRecentDate(LocalDate date, int years) {
        return date != null && date.isAfter(LocalDate.now().minusYears(years));
    }
    
    /**
     * Get email validation error message
     */
    public static String getEmailError(String email) {
        if (email == null || email.isBlank()) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Invalid email format";
        }
        return null;
    }
    
    /**
     * Get phone validation error message
     */
    public static String getPhoneError(String phone) {
        if (phone == null || phone.isBlank()) {
            return "Phone number is required";
        }
        if (!isValidPhone(phone)) {
            return "Invalid phone number format";
        }
        return null;
    }
    
    /**
     * Get password validation error message
     */
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            return "Password must contain at least one letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number";
        }
        return null;
    }
    
    /**
     * Get required field error message
     */
    public static String getRequiredError(String fieldName) {
        return fieldName + " is required";
    }
    
    /**
     * Validate postal code (flexible format)
     */
    public static boolean isValidPostalCode(String postalCode) {
        return postalCode != null && postalCode.matches("^[A-Za-z0-9\\s-]{3,10}$");
    }
    
    /**
     * Validate positive number
     */
    public static boolean isPositiveNumber(String value) {
        try {
            double num = Double.parseDouble(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate integer
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
