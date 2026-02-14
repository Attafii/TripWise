package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DealsController {

    @FXML private Label featuredTitle;
    @FXML private Label featuredDesc;
    @FXML private Label featuredExpiry;
    @FXML private FlowPane dealsContainer;
    @FXML private HBox destinationsContainer;
    @FXML private TextField emailField;
    @FXML private ToggleButton allDealsBtn, flightDealsBtn, hotelDealsBtn, packageDealsBtn;

    private String currentFilter = "ALL";
    private List<Deal> deals = new ArrayList<>();

    @FXML
    private void initialize() {
        createDeals();
        loadDeals();
        loadPopularDestinations();
    }

    @FXML
    private void refreshDeals() {
        loadDeals();
    }

    @FXML private void filterAll() { currentFilter = "ALL"; updateFilterStyles(); loadDeals(); }
    @FXML private void filterFlights() { currentFilter = "FLIGHT"; updateFilterStyles(); loadDeals(); }
    @FXML private void filterHotels() { currentFilter = "HOTEL"; updateFilterStyles(); loadDeals(); }
    @FXML private void filterPackages() { currentFilter = "PACKAGE"; updateFilterStyles(); loadDeals(); }

    private void updateFilterStyles() {
        String active = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 20;";
        String inactive = "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-background-radius: 8; -fx-padding: 8 20;";

        allDealsBtn.setStyle(currentFilter.equals("ALL") ? active : inactive);
        flightDealsBtn.setStyle(currentFilter.equals("FLIGHT") ? active : inactive);
        hotelDealsBtn.setStyle(currentFilter.equals("HOTEL") ? active : inactive);
        packageDealsBtn.setStyle(currentFilter.equals("PACKAGE") ? active : inactive);
    }

    private void createDeals() {
        deals.clear();

        // Flight deals from database
        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT v.numero_vol, ad.ville as depart, aa.ville as arrival, " +
                    "MIN(cv.prix) as min_price, c.nom_compagnie " +
                    "FROM vols v " +
                    "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                    "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                    "JOIN compagnies_aeriennes c ON v.compagnie_id = c.compagnie_id " +
                    "JOIN classes_vol cv ON v.vol_id = cv.vol_id " +
                    "WHERE v.date_depart > NOW() AND v.is_active = 1 " +
                    "GROUP BY v.vol_id " +
                    "ORDER BY min_price ASC LIMIT 6";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                Random rand = new Random();

                while (rs.next()) {
                    double originalPrice = rs.getDouble("min_price");
                    int discount = 10 + rand.nextInt(30); // 10-40% discount
                    double dealPrice = originalPrice * (1 - discount / 100.0);

                    deals.add(new Deal(
                        "FLIGHT",
                        rs.getString("depart") + " to " + rs.getString("arrival"),
                        rs.getString("nom_compagnie") + " • " + rs.getString("numero_vol"),
                        originalPrice,
                        dealPrice,
                        discount,
                        "Limited seats available"
                    ));
                }
            }

            // Hotel deals
            String hotelQuery = "SELECT h.nom_hotel, h.ville, h.etoiles, MIN(c.prix_nuit) as min_price " +
                    "FROM hotels h " +
                    "JOIN chambres c ON h.hotel_id = c.hotel_id " +
                    "WHERE h.is_active = 1 " +
                    "GROUP BY h.hotel_id " +
                    "ORDER BY min_price ASC LIMIT 4";

            try (PreparedStatement stmt = conn.prepareStatement(hotelQuery)) {
                ResultSet rs = stmt.executeQuery();
                Random rand = new Random();

                while (rs.next()) {
                    double originalPrice = rs.getDouble("min_price");
                    int discount = 15 + rand.nextInt(25); // 15-40% discount
                    double dealPrice = originalPrice * (1 - discount / 100.0);
                    int stars = rs.getInt("etoiles");

                    deals.add(new Deal(
                        "HOTEL",
                        rs.getString("nom_hotel"),
                        rs.getString("ville") + " • " + stars + " Stars",
                        originalPrice,
                        dealPrice,
                        discount,
                        "Per night"
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading deals from database: " + e.getMessage());
        }

        // Add some package deals
        deals.add(new Deal("PACKAGE", "Paris Weekend Getaway", "Flight + 3 Nights Hotel", 899, 649, 28, "All inclusive"));
        deals.add(new Deal("PACKAGE", "Dubai Luxury Experience", "Flight + 5 Nights Resort", 1899, 1299, 32, "With breakfast"));
    }

    private void loadDeals() {
        dealsContainer.getChildren().clear();

        for (Deal deal : deals) {
            if (currentFilter.equals("ALL") || deal.type.equals(currentFilter)) {
                VBox card = createDealCard(deal);
                dealsContainer.getChildren().add(card);
            }
        }

        if (dealsContainer.getChildren().isEmpty()) {
            Label noDeals = new Label("No deals available in this category");
            noDeals.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
            dealsContainer.getChildren().add(noDeals);
        }
    }

    private VBox createDealCard(Deal deal) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-cursor: hand;");

        // Type badge
        String badgeColor = deal.type.equals("FLIGHT") ? "#3b82f6" :
                           deal.type.equals("HOTEL") ? "#10b981" : "#8b5cf6";
        Label typeBadge = new Label(deal.type);
        typeBadge.setStyle("-fx-background-color: " + badgeColor + "22; -fx-text-fill: " + badgeColor + "; " +
                          "-fx-padding: 4 10; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Title
        Label titleLabel = new Label(deal.title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        titleLabel.setWrapText(true);

        // Subtitle
        Label subtitleLabel = new Label(deal.subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // Price section
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label newPrice = new Label(String.format("%.0f USD", deal.dealPrice));
        newPrice.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label oldPrice = new Label(String.format("%.0f USD", deal.originalPrice));
        oldPrice.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-strikethrough: true;");

        priceBox.getChildren().addAll(newPrice, oldPrice);

        // Discount badge
        HBox discountBox = new HBox(10);
        discountBox.setAlignment(Pos.CENTER_LEFT);

        Label discountBadge = new Label("-" + deal.discount + "%");
        discountBadge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                              "-fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label noteLabel = new Label(deal.note);
        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        discountBox.getChildren().addAll(discountBadge, noteLabel);

        // Book button
        Button bookBtn = new Button("View Deal");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
        bookBtn.setOnAction(e -> bookDeal(deal));

        card.getChildren().addAll(typeBadge, titleLabel, subtitleLabel, priceBox, discountBox, bookBtn);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-cursor: hand;"));

        return card;
    }

    private void loadPopularDestinations() {
        destinationsContainer.getChildren().clear();

        String[] destinations = {"Paris", "New York", "Dubai", "London", "Tokyo"};
        String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899"};

        for (int i = 0; i < destinations.length; i++) {
            VBox destCard = new VBox(8);
            destCard.setAlignment(Pos.CENTER);
            destCard.setPadding(new Insets(20));
            destCard.setPrefWidth(150);
            destCard.setStyle("-fx-background-color: " + colors[i] + "15; -fx-background-radius: 12; -fx-cursor: hand;");

            Label cityLabel = new Label(destinations[i]);
            cityLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + colors[i] + ";");

            Label dealsLabel = new Label("5+ deals");
            dealsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            destCard.getChildren().addAll(cityLabel, dealsLabel);

            final int idx = i;
            destCard.setOnMouseClicked(e -> searchDealsForCity(destinations[idx]));

            destinationsContainer.getChildren().add(destCard);
        }
    }

    private void bookDeal(Deal deal) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Deal Selected");
        alert.setHeaderText(deal.title);
        alert.setContentText("You selected this deal!\n\n" +
                "Original Price: " + String.format("%.2f USD", deal.originalPrice) + "\n" +
                "Deal Price: " + String.format("%.2f USD", deal.dealPrice) + "\n" +
                "You save: " + deal.discount + "%\n\n" +
                "Navigate to '" + (deal.type.equals("FLIGHT") ? "Book Flight" : "Book Hotel") +
                "' to complete your booking.");
        alert.showAndWait();
    }

    @FXML
    private void bookFeaturedDeal() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Featured Deal");
        alert.setHeaderText("Summer Sale - Up to 40% Off!");
        alert.setContentText("This deal applies automatically when you book any flight or hotel.\n\n" +
                "Use code: SUMMER40 at checkout for maximum savings!");
        alert.showAndWait();
    }

    @FXML
    private void subscribe() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            showAlert("Please enter a valid email address");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Subscribed!");
        alert.setHeaderText("Welcome to TripWise Deals!");
        alert.setContentText("You'll receive exclusive deals at: " + email);
        alert.showAndWait();

        emailField.clear();
    }

    private void searchDealsForCity(String city) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Deals for " + city);
        alert.setHeaderText("Searching deals for " + city);
        alert.setContentText("Check the 'Book Flight' or 'Book Hotel' sections for current deals to " + city);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Notice");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for Deal
    private static class Deal {
        String type;
        String title;
        String subtitle;
        double originalPrice;
        double dealPrice;
        int discount;
        String note;

        Deal(String type, String title, String subtitle, double originalPrice, double dealPrice, int discount, String note) {
            this.type = type;
            this.title = title;
            this.subtitle = subtitle;
            this.originalPrice = originalPrice;
            this.dealPrice = dealPrice;
            this.discount = discount;
            this.note = note;
        }
    }
}
