package ui.controllers.traveler;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import ui.model.Deal;
import ui.service.DealService;
import ui.util.SessionManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TravelerDealsController - Comprehensive deals and offers management
 * Features: Daily deals, flash sales, filters, sorting, pagination
 */
public class TravelerDealsController {

    // Filter Controls
    @FXML private ComboBox<String> dealTypeComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> destinationComboBox;
    @FXML private ComboBox<String> sortByComboBox;

    // Daily Deal Container
    @FXML private VBox dailyDealContainer;

    // Flash Sale Banner
    @FXML private HBox flashSaleBanner;
    @FXML private Text flashSaleText;
    @FXML private Text flashSaleTimer;

    // Deals Display
    @FXML private Label dealCountLabel;
    @FXML private GridPane dealsGrid;

    // Empty/Loading States
    @FXML private VBox emptyStateContainer;
    @FXML private VBox loadingContainer;

    // Pagination
    @FXML private HBox paginationContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    // Services
    private DealService dealService;

    // Data
    private List<Deal> allDeals;
    private List<Deal> filteredDeals;
    private int currentPage = 1;
    private int dealsPerPage = 9; // 3x3 grid

    // Date formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        dealService = new DealService();
        allDeals = new ArrayList<>();
        filteredDeals = new ArrayList<>();

        // Populate ComboBoxes
        dealTypeComboBox.getItems().addAll("All Deals", "Hotels", "Flights", "Cars", "Packages");
        categoryComboBox.getItems().addAll("All Categories", "Daily Deals", "Weekly Deals", "Flash Sales", "Seasonal Offers");
        destinationComboBox.getItems().addAll("Any Destination", "Paris", "New York", "Dubai", "London", "Tokyo");
        sortByComboBox.getItems().addAll("Best Deals", "Highest Discount", "Lowest Price", "Ending Soon", "Newest");

        // Set default filter values
        dealTypeComboBox.setValue("All Deals");
        categoryComboBox.setValue("All Categories");
        destinationComboBox.setValue("Any Destination");
        sortByComboBox.setValue("Best Deals");

        // Load data
        loadDeals();
    }

    /**
     * Load all deals from database
     */
    private void loadDeals() {
        showLoading(true);

        new Thread(() -> {
            try {
                // Load different types of deals
                List<Deal> allDealsList = new ArrayList<>();
                
                // Get daily deal
                Deal dailyDeal = dealService.getDailyDeal();
                if (dailyDeal != null) {
                    Platform.runLater(() -> displayDailyDeal(dailyDeal));
                }

                // Get weekly deals
                allDealsList.addAll(dealService.getWeeklyDeals());

                // Get flash sales
                List<Deal> flashSales = dealService.getFlashSales();
                if (!flashSales.isEmpty()) {
                    Platform.runLater(() -> displayFlashSaleBanner(flashSales));
                }
                allDealsList.addAll(flashSales);

                // Get all active deals
                allDealsList.addAll(dealService.getAllActiveDeals());

                // Remove duplicates (by deal_id)
                allDealsList = allDealsList.stream()
                    .distinct()
                    .collect(Collectors.toList());

                final List<Deal> finalList = allDealsList;
                Platform.runLater(() -> {
                    allDeals.clear();
                    allDeals.addAll(finalList);
                    applyFilters();
                    showLoading(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Failed to load deals: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Display featured daily deal
     */
    private void displayDailyDeal(Deal deal) {
        dailyDealContainer.getChildren().clear();

        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); " +
                     "-fx-background-radius: 16; -fx-padding: 32; " +
                     "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 20, 0, 0, 8);");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text icon = new Text("⭐");
        icon.setStyle("-fx-font-size: 32px;");
        
        Text title = new Text("Deal of the Day");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: white;");
        
        header.getChildren().addAll(icon, title);

        // Deal info
        Text dealTitle = new Text(deal.getTitle());
        dealTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; -fx-fill: white;");
        dealTitle.setWrappingWidth(600);

        Text description = new Text(deal.getDescription());
        description.setStyle("-fx-font-size: 14px; -fx-fill: rgba(255,255,255,0.9);");
        description.setWrappingWidth(600);

        // Price section
        HBox priceSection = new HBox(24);
        priceSection.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(4);
        Text originalPrice = new Text("$" + String.format("%.2f", deal.getOriginalPrice()));
        originalPrice.setStyle("-fx-font-size: 18px; -fx-fill: rgba(255,255,255,0.7); -fx-strikethrough: true;");
        
        Text discountedPrice = new Text("$" + String.format("%.2f", deal.getDiscountedPrice()));
        discountedPrice.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: white;");
        
        priceBox.getChildren().addAll(originalPrice, discountedPrice);

        Label discountBadge = new Label(deal.getDiscountText());
        discountBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                              "-fx-background-radius: 8; -fx-padding: 8 16; " +
                              "-fx-font-size: 18px; -fx-font-weight: bold;");

        priceSection.getChildren().addAll(priceBox, discountBadge);

        // Action buttons
        HBox actions = new HBox(12);
        
        Button bookButton = new Button("Book This Deal");
        bookButton.setStyle("-fx-background-color: white; -fx-text-fill: #1e40af; " +
                           "-fx-background-radius: 8; -fx-padding: 12 24; " +
                           "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        bookButton.setOnAction(e -> handleBookDeal(deal));

        Button detailsButton = new Button("View Details");
        detailsButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                              "-fx-background-radius: 8; -fx-padding: 12 24; " +
                              "-fx-font-size: 16px; -fx-font-weight: 600; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> handleViewDetails(deal));

        actions.getChildren().addAll(bookButton, detailsButton);

        // Expiry info
        if (deal.getEndDate() != null) {
            Text expiryText = new Text("Expires: " + deal.getEndDate().format(DATE_FORMATTER));
            expiryText.setStyle("-fx-font-size: 12px; -fx-fill: rgba(255,255,255,0.8);");
            card.getChildren().addAll(header, dealTitle, description, priceSection, actions, expiryText);
        } else {
            card.getChildren().addAll(header, dealTitle, description, priceSection, actions);
        }

        dailyDealContainer.getChildren().add(card);
        dailyDealContainer.setVisible(true);
        dailyDealContainer.setManaged(true);
    }

    /**
     * Display flash sale banner
     */
    private void displayFlashSaleBanner(List<Deal> flashSales) {
        if (flashSales.isEmpty()) return;

        Deal firstFlash = flashSales.get(0);
        flashSaleText.setText("Limited time offers - Up to " + 
            (int)flashSales.stream().mapToDouble(Deal::getDiscountPercentage).max().orElse(0) + 
            "% off!");

        // TODO: Implement countdown timer
        flashSaleTimer.setText("Limited Time");

        flashSaleBanner.setVisible(true);
        flashSaleBanner.setManaged(true);
    }

    /**
     * Apply current filters
     */
    @FXML
    private void handleApplyFilters() {
        applyFilters();
    }

    /**
     * Apply filters and refresh display
     */
    private void applyFilters() {
        String typeFilter = dealTypeComboBox.getValue();
        String categoryFilter = categoryComboBox.getValue();
        String destinationFilter = destinationComboBox.getValue();
        String sortBy = sortByComboBox.getValue();

        filteredDeals = allDeals.stream()
            .filter(deal -> {
                // Type filter
                if (!"All Deals".equals(typeFilter)) {
                    String dealType = typeFilter.replace("s", "").toUpperCase();
                    if (!dealType.equals(deal.getDealType())) {
                        return false;
                    }
                }

                // Category filter
                if (!"All Categories".equals(categoryFilter)) {
                    // Map category to deal_type in database
                    String category = categoryFilter.replace(" ", "_").toUpperCase();
                    if (deal.getDealType() != null && !deal.getDealType().contains(category)) {
                        return false;
                    }
                }

                // Destination filter
                if (!"Any Destination".equals(destinationFilter)) {
                    if (deal.getDestination() == null || 
                        !deal.getDestination().contains(destinationFilter)) {
                        return false;
                    }
                }

                return true;
            })
            .collect(Collectors.toList());

        // Apply sorting
        applySorting(sortBy);

        // Update count
        dealCountLabel.setText("(" + filteredDeals.size() + " deals)");

        // Reset to first page
        currentPage = 1;

        // Display results
        displayDeals();
    }

    /**
     * Apply sorting to filtered deals
     */
    private void applySorting(String sortBy) {
        switch (sortBy) {
            case "Highest Discount":
                filteredDeals.sort(Comparator.comparingDouble(Deal::getDiscountPercentage).reversed());
                break;
            case "Lowest Price":
                filteredDeals.sort(Comparator.comparingDouble(Deal::getDiscountedPrice));
                break;
            case "Ending Soon":
                filteredDeals.sort(Comparator.comparing(Deal::getEndDate));
                break;
            case "Newest":
                filteredDeals.sort(Comparator.comparing(Deal::getCreatedAt).reversed());
                break;
            default: // "Best Deals"
                filteredDeals.sort(Comparator.comparingDouble(Deal::getDiscountPercentage).reversed());
                break;
        }
    }

    /**
     * Display deals in grid
     */
    private void displayDeals() {
        dealsGrid.getChildren().clear();

        if (filteredDeals.isEmpty()) {
            showEmptyState();
            return;
        }

        hideEmptyState();

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) filteredDeals.size() / dealsPerPage);
        int startIndex = (currentPage - 1) * dealsPerPage;
        int endIndex = Math.min(startIndex + dealsPerPage, filteredDeals.size());

        // Display deals for current page
        int row = 0;
        int col = 0;
        for (int i = startIndex; i < endIndex; i++) {
            VBox dealCard = createDealCard(filteredDeals.get(i));
            dealsGrid.add(dealCard, col, row);
            
            col++;
            if (col >= 3) { // 3 columns
                col = 0;
                row++;
            }
        }

        // Update pagination
        updatePagination(totalPages);
    }

    /**
     * Create a deal card
     */
    private VBox createDealCard(Deal deal) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);");
        card.setPrefWidth(300);
        card.setMinHeight(350);

        // Icon and discount badge
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Text icon = new Text(getIconForDealType(deal.getDealType()));
        icon.setStyle("-fx-font-size: 32px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label discountBadge = new Label(deal.getDiscountText());
        discountBadge.getStyleClass().add("badge-warning");
        
        headerRow.getChildren().addAll(icon, spacer, discountBadge);

        // Title
        Text title = new Text(deal.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-fill: #111827;");
        title.setWrappingWidth(260);

        // Description
        Text description = new Text(deal.getDescription() != null ? deal.getDescription() : "");
        description.setStyle("-fx-font-size: 13px; -fx-fill: #6b7280;");
        description.setWrappingWidth(260);

        // Destination (if available)
        HBox destinationRow = new HBox(6);
        destinationRow.setAlignment(Pos.CENTER_LEFT);
        if (deal.getDestination() != null) {
            Text destIcon = new Text("📍");
            Text destText = new Text(deal.getDestination());
            destText.setStyle("-fx-font-size: 12px; -fx-fill: #9ca3af;");
            destinationRow.getChildren().addAll(destIcon, destText);
        }

        // Prices
        HBox priceRow = new HBox(12);
        priceRow.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(2);
        Text originalPrice = new Text("$" + String.format("%.2f", deal.getOriginalPrice()));
        originalPrice.setStyle("-fx-font-size: 14px; -fx-fill: #9ca3af; -fx-strikethrough: true;");
        
        Text discountedPrice = new Text("$" + String.format("%.2f", deal.getDiscountedPrice()));
        discountedPrice.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: #1f2937;");
        
        priceBox.getChildren().addAll(originalPrice, discountedPrice);
        priceRow.getChildren().add(priceBox);

        // Available slots (if limited)
        if (deal.getAvailableSlots() > 0 && deal.getAvailableSlots() <= 10) {
            Text slotsText = new Text("Only " + deal.getAvailableSlots() + " left!");
            slotsText.setStyle("-fx-font-size: 11px; -fx-fill: #ef4444; -fx-font-weight: bold;");
            card.getChildren().add(slotsText);
        }

        // Expiry date
        if (deal.getEndDate() != null) {
            Text expiryText = new Text("Expires: " + deal.getEndDate().format(DATE_FORMATTER));
            expiryText.setStyle("-fx-font-size: 11px; -fx-fill: #9ca3af;");
            card.getChildren().add(expiryText);
        }

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        Button viewButton = new Button("View Details");
        viewButton.getStyleClass().add("btn-secondary");
        viewButton.setPrefWidth(125);
        viewButton.setOnAction(e -> handleViewDetails(deal));

        Button bookButton = new Button("Book Now");
        bookButton.getStyleClass().add("btn-primary");
        bookButton.setPrefWidth(125);
        bookButton.setOnAction(e -> handleBookDeal(deal));

        actions.getChildren().addAll(viewButton, bookButton);

        card.getChildren().addAll(headerRow, title, description, destinationRow, priceRow, actions);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 6); -fx-cursor: hand;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);");
        });

        return card;
    }

    /**
     * Get icon for deal type
     */
    private String getIconForDealType(String type) {
        if (type == null) return "🎯";
        switch (type.toUpperCase()) {
            case "HOTEL": return "🏨";
            case "FLIGHT": return "✈️";
            case "CAR": return "🚗";
            case "PACKAGE": return "🎁";
            default: return "🎯";
        }
    }

    /**
     * Handle book deal
     */
    private void handleBookDeal(Deal deal) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Book Deal");
        confirmAlert.setHeaderText("Book this amazing deal?");
        confirmAlert.setContentText(deal.getTitle() + "\n\nPrice: $" + 
            String.format("%.2f", deal.getDiscountedPrice()) + " (Save $" + 
            String.format("%.2f", deal.getSavings()) + ")");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (dealService.bookDeal(deal.getDealId())) {
                    showSuccess("Deal booked successfully! Check your bookings to view details.");
                    loadDeals(); // Refresh to update available slots
                } else {
                    showError("Failed to book deal. Please try again or contact support.");
                }
            }
        });
    }

    /**
     * Handle view deal details
     */
    private void handleViewDetails(Deal deal) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Deal Details");
        alert.setHeaderText(deal.getTitle());
        
        StringBuilder content = new StringBuilder();
        content.append("Description: ").append(deal.getDescription()).append("\n\n");
        content.append("Type: ").append(deal.getDealType()).append("\n");
        if (deal.getDestination() != null) {
            content.append("Destination: ").append(deal.getDestination()).append("\n");
        }
        content.append("\nOriginal Price: $").append(String.format("%.2f", deal.getOriginalPrice())).append("\n");
        content.append("Discounted Price: $").append(String.format("%.2f", deal.getDiscountedPrice())).append("\n");
        content.append("You Save: $").append(String.format("%.2f", deal.getSavings())).append(" (")
               .append(deal.getDiscountText()).append(")\n\n");
        
        if (deal.getStartDate() != null) {
            content.append("Valid From: ").append(deal.getStartDate().format(DATE_FORMATTER)).append("\n");
        }
        if (deal.getEndDate() != null) {
            content.append("Valid Until: ").append(deal.getEndDate().format(DATE_FORMATTER)).append("\n");
        }
        
        if (deal.getAvailableSlots() > 0) {
            content.append("\nAvailable Slots: ").append(deal.getAvailableSlots()).append(" remaining");
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Update pagination controls
     */
    private void updatePagination(int totalPages) {
        if (totalPages <= 1) {
            paginationContainer.setVisible(false);
            paginationContainer.setManaged(false);
        } else {
            paginationContainer.setVisible(true);
            paginationContainer.setManaged(true);
            pageLabel.setText("Page " + currentPage + " of " + totalPages);
            prevButton.setDisable(currentPage == 1);
            nextButton.setDisable(currentPage == totalPages);
        }
    }

    /**
     * Handle previous page
     */
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            displayDeals();
        }
    }

    /**
     * Handle next page
     */
    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) filteredDeals.size() / dealsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            displayDeals();
        }
    }

    /**
     * Show loading state
     */
    private void showLoading(boolean show) {
        loadingContainer.setVisible(show);
        loadingContainer.setManaged(show);
        dealsGrid.setVisible(!show);
        dealsGrid.setManaged(!show);
    }

    /**
     * Show empty state
     */
    private void showEmptyState() {
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        dealsGrid.setVisible(false);
        dealsGrid.setManaged(false);
        paginationContainer.setVisible(false);
        paginationContainer.setManaged(false);
    }

    /**
     * Hide empty state
     */
    private void hideEmptyState() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        dealsGrid.setVisible(true);
        dealsGrid.setManaged(true);
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
