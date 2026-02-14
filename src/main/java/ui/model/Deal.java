package ui.model;

import java.time.LocalDateTime;

/**
 * Deal model - Represents special offers and discounts
 */
public class Deal {
    private int dealId;
    private String dealType; // HOTEL, FLIGHT, CAR, PACKAGE
    private String title;
    private String description;
    private int itemId; // Reference to hotel_id, vol_id, or vehicule_id
    private String itemName;
    private double originalPrice;
    private double discountedPrice;
    private double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;
    private String destination;
    private int availableSlots;
    private boolean isActive;
    private String[] highlights; // Special features
    private LocalDateTime createdAt;
    
    // Deal Types
    public enum DealType {
        DAILY_DEAL,
        WEEKLY_DEAL,
        FLASH_SALE,
        SEASONAL_OFFER,
        PACKAGE_DEAL
    }
    
    // Constructors
    public Deal() {}
    
    public Deal(int dealId, String dealType, String title, String description) {
        this.dealId = dealId;
        this.dealType = dealType;
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public int getDealId() {
        return dealId;
    }
    
    public void setDealId(int dealId) {
        this.dealId = dealId;
    }
    
    public String getDealType() {
        return dealType;
    }
    
    public void setDealType(String dealType) {
        this.dealType = dealType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public double getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public double getDiscountedPrice() {
        return discountedPrice;
    }
    
    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }
    
    public double getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public int getAvailableSlots() {
        return availableSlots;
    }
    
    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String[] getHighlights() {
        return highlights;
    }
    
    public void setHighlights(String[] highlights) {
        this.highlights = highlights;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Calculate savings amount
     */
    public double getSavings() {
        return originalPrice - discountedPrice;
    }
    
    /**
     * Check if deal is currently active
     */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(startDate) && 
               now.isBefore(endDate) &&
               availableSlots > 0;
    }
    
    /**
     * Get formatted discount text
     */
    public String getDiscountText() {
        return String.format("%.0f%% OFF", discountPercentage);
    }
}
