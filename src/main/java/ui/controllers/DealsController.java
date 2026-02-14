package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import ui.model.User;
import ui.util.SessionManager;

/**
 * DealsController - Role-specific travel deals and promotions
 */
public class DealsController {

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private GridPane dealsGrid;
    @FXML private Label stat1Label;
    @FXML private Label stat1Title;
    @FXML private Label stat2Label;
    @FXML private Label stat2Title;
    @FXML private Label stat3Label;
    @FXML private Label stat3Title;

    private User.UserType userRole;
    private ObservableList<Deal> deals;

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ ERROR: No user in session!");
            return;
        }
        
        userRole = currentUser.getUserType();
        System.out.println("✅ Deals - User Role: " + userRole);
        
        deals = FXCollections.observableArrayList();
        setupFilters();
        loadRoleSpecificDeals();
    }

    private void setupFilters() {
        categoryFilter.getItems().addAll("All Deals", "Flights", "Hotels", "Packages", "Car Rentals");
        categoryFilter.setValue("All Deals");
        
        sortFilter.getItems().addAll("Best Value", "Price: Low to High", "Price: High to Low", "Ending Soon");
        sortFilter.setValue("Best Value");
    }

    private void loadRoleSpecificDeals() {
        if (userRole == null) {
            loadTravelerDeals();
            return;
        }
        
        switch (userRole) {
            case ADMIN:
            case RESPONSABLE:
                loadAdminDeals();
                break;
            case EMPLOYE:
                loadEmployeeDeals();
                break;
            case VOYAGEUR:
            case VISITEUR:
            default:
                loadTravelerDeals();
                break;
        }
        
        displayDeals();
    }

    private void loadAdminDeals() {
        pageTitle.setText("Manage Deals & Promotions");
        pageSubtitle.setText("Create and manage travel deals for customers");
        
        stat1Title.setText("Active Deals");
        stat1Label.setText("24");
        stat2Title.setText("Total Revenue from Deals");
        stat2Label.setText("$125,400");
        stat3Title.setText("Conversion Rate");
        stat3Label.setText("28%");
        
        deals.add(new Deal("Winter Escape to Paris", "5-day trip with hotel and flights", 
            1299.0, 1899.0, "31%", "Hotel + Flight", "⭐ Featured"));
        deals.add(new Deal("Caribbean Beach Resort", "All-inclusive 7 nights", 
            899.0, 1599.0, "44%", "Hotel", "🔥 Hot Deal"));
        deals.add(new Deal("Business Class Upgrade", "Upgrade to business on all routes", 
            299.0, 599.0, "50%", "Flight", "✈️ Premium"));
        deals.add(new Deal("European Rail Pass", "15-day unlimited train travel", 
            449.0, 799.0, "44%", "Transportation", "🚄 New"));
    }

    private void loadEmployeeDeals() {
        pageTitle.setText("Promote Deals to Customers");
        pageSubtitle.setText("Recommend and apply deals to bookings");
        
        stat1Title.setText("Deals Applied Today");
        stat1Label.setText("12");
        stat2Title.setText("Commission Earned");
        stat2Label.setText("$245");
        stat3Title.setText("Customer Satisfaction");
        stat3Label.setText("4.8/5");
        
        deals.add(new Deal("Flash Sale: NYC Hotel", "3 nights in Manhattan", 
            359.0, 699.0, "49%", "Hotel", "⚡ Flash"));
        deals.add(new Deal("Last Minute: Florida", "Round-trip flights + car rental", 
            449.0, 799.0, "44%", "Package", "⏰ Last Minute"));
        deals.add(new Deal("Group Discount: Europe", "10+ travelers special rate", 
            1199.0, 1999.0, "40%", "Flight", "👥 Group"));
    }

    private void loadTravelerDeals() {
        pageTitle.setText("Exclusive Travel Deals");
        pageSubtitle.setText("Discover amazing offers on flights, hotels, and packages");
        
        stat1Title.setText("Deals Available");
        stat1Label.setText("24");
        stat2Title.setText("Average Savings");
        stat2Label.setText("38%");
        stat3Title.setText("Deals Expiring Soon");
        stat3Label.setText("6");
        
        deals.add(new Deal("Paris Getaway Special", "Round-trip flight + 4 nights hotel", 
            899.0, 1599.0, "44%", "Package", "⭐ Featured"));
        deals.add(new Deal("Bali Beach Paradise", "7 nights all-inclusive resort", 
            1299.0, 2199.0, "41%", "Hotel", "🌴 Tropical"));
        deals.add(new Deal("Tokyo Adventure", "Flight + 5 nights + tours", 
            1599.0, 2899.0, "45%", "Package", "🗾 Cultural"));
        deals.add(new Deal("Dubai Luxury Stay", "5-star hotel, 6 nights", 
            799.0, 1499.0, "47%", "Hotel", "💎 Luxury"));
        deals.add(new Deal("London City Break", "3 nights + attractions pass", 
            549.0, 899.0, "39%", "Package", "🏰 City"));
        deals.add(new Deal("NYC Hotel Sale", "Times Square hotel, 4 nights", 
            399.0, 799.0, "50%", "Hotel", "🔥 Hot"));
    }

    private void displayDeals() {
        dealsGrid.getChildren().clear();
        int row = 0;
        int col = 0;
        
        for (Deal deal : deals) {
            VBox dealCard = createDealCard(deal);
            dealsGrid.add(dealCard, col, row);
            
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDealCard(Deal deal) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4); -fx-cursor: hand;");
        card.setPrefWidth(320);
        card.setPrefHeight(340);
        
        // Badge
        Label badge = new Label(deal.getBadge());
        badge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 6 12; " +
                      "-fx-background-radius: 16; -fx-font-size: 11px; -fx-font-weight: 600;");
        badge.setMaxWidth(Region.USE_PREF_SIZE);
        
        // Title
        Label title = new Label(deal.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1f2937;");
        title.setWrapText(true);
        title.setMaxHeight(50);
        
        // Description
        Label description = new Label(deal.getDescription());
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        description.setWrapText(true);
        description.setMaxHeight(40);
        
        // Category
        Label category = new Label("📦 " + deal.getCategory());
        category.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        
        // Price section
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox prices = new VBox(2);
        Label originalPrice = new Label("$" + String.format("%.0f", deal.getOriginalPrice()));
        originalPrice.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-strikethrough: true;");
        
        Label salePrice = new Label("$" + String.format("%.0f", deal.getSalePrice()));
        salePrice.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        
        prices.getChildren().addAll(originalPrice, salePrice);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label discount = new Label(deal.getDiscountPercent() + " OFF");
        discount.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 6 12; " +
                         "-fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700;");
        
        priceBox.getChildren().addAll(prices, spacer, discount);
        
        // Button
        Button bookButton = new Button("Book Now");
        bookButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 10; " +
                           "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand; -fx-pref-width: 100%;");
        bookButton.setOnAction(e -> handleBookDeal(deal));
        
        card.getChildren().addAll(badge, title, description, category, priceBox, bookButton);
        return card;
    }

    @FXML
    private void handleFilterChange() {
        // Filter logic would go here
        System.out.println("Filter changed - Category: " + categoryFilter.getValue() + ", Sort: " + sortFilter.getValue());
    }

    private void handleBookDeal(Deal deal) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Deal");
        alert.setHeaderText(deal.getTitle());
        alert.setContentText(String.format("You're booking this deal for $%.0f\n\nOriginal Price: $%.0f\nYou Save: $%.0f (%s)",
            deal.getSalePrice(), deal.getOriginalPrice(), 
            deal.getOriginalPrice() - deal.getSalePrice(), deal.getDiscountPercent()));
        alert.showAndWait();
    }

    // Deal Model
    public static class Deal {
        private String title;
        private String description;
        private double salePrice;
        private double originalPrice;
        private String discountPercent;
        private String category;
        private String badge;

        public Deal(String title, String description, double salePrice, double originalPrice, 
                   String discountPercent, String category, String badge) {
            this.title = title;
            this.description = description;
            this.salePrice = salePrice;
            this.originalPrice = originalPrice;
            this.discountPercent = discountPercent;
            this.category = category;
            this.badge = badge;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public double getSalePrice() { return salePrice; }
        public double getOriginalPrice() { return originalPrice; }
        public String getDiscountPercent() { return discountPercent; }
        public String getCategory() { return category; }
        public String getBadge() { return badge; }
    }
}
