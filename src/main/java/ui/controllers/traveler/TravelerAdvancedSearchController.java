package ui.controllers.traveler;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
// TODO: Uncomment when JavaFX Web module is loaded (after mvn clean install)
// import javafx.scene.web.WebView;
// import javafx.scene.web.WebEngine;
import ui.model.Hotel;
import ui.service.AdvancedSearchService;
import ui.service.AdvancedSearchService.SearchFilters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * TravelerAdvancedSearchController - Advanced hotel search with filters
 */
public class TravelerAdvancedSearchController {

    // Search Filter Controls
    @FXML private ComboBox<String> destinationComboBox;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private Spinner<Integer> guestsSpinner;
    @FXML private Spinner<Integer> roomsSpinner;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> starRatingComboBox;
    @FXML private ComboBox<String> sortByComboBox;
    
    // Amenities Checkboxes
    @FXML private CheckBox wifiCheckBox;
    @FXML private CheckBox poolCheckBox;
    @FXML private CheckBox spaCheckBox;
    @FXML private CheckBox gymCheckBox;
    @FXML private CheckBox parkingCheckBox;
    @FXML private CheckBox restaurantCheckBox;
    @FXML private CheckBox barCheckBox;
    @FXML private CheckBox airportShuttleCheckBox;
    @FXML private CheckBox petFriendlyCheckBox;
    @FXML private CheckBox businessCenterCheckBox;
    
    // Price Calculator Labels
    @FXML private Label nightsLabel;
    @FXML private Label roomsLabel;
    @FXML private Label avgPriceLabel;
    @FXML private Label totalCostLabel;
    @FXML private Label priceRangeLabel;
    @FXML private Label resultsCountLabel;
    
    // Map View
    @FXML private CheckBox showMapCheckBox;
    // TODO: Uncomment when JavaFX Web module is loaded
    // @FXML private WebView mapWebView;
    
    // Results
    @FXML private VBox resultsContainer;
    @FXML private FlowPane resultsFlowPane;
    @FXML private Button gridViewBtn;
    @FXML private Button listViewBtn;
    @FXML private Label pageLabel;
    
    // Services
    private AdvancedSearchService searchService;
    
    // State
    private List<Hotel> currentResults;
    private int currentPage = 1;
    private int resultsPerPage = 12;
    private boolean isGridView = true;

    @FXML
    private void initialize() {
        searchService = new AdvancedSearchService();
        currentResults = new ArrayList<>();
        
        initializeFilters();
        setupEventHandlers();
        loadInitialData();
        
        System.out.println("✅ Advanced Search Controller initialized");
    }

    private void initializeFilters() {
        // Populate destination dropdown
        destinationComboBox.getItems().addAll(
            "Paris, France",
            "New York, USA",
            "Dubai, UAE",
            "London, UK",
            "Los Angeles, USA",
            "Tokyo, Japan",
            "Barcelona, Spain",
            "Rome, Italy",
            "Amsterdam, Netherlands",
            "Singapore"
        );
        
        // Populate star rating
        starRatingComboBox.getItems().addAll(
            "Any Rating",
            "5 Stars",
            "4+ Stars",
            "3+ Stars",
            "2+ Stars"
        );
        starRatingComboBox.setValue("Any Rating");
        
        // Populate sort options
        sortByComboBox.getItems().addAll(
            "Best Match",
            "Price: Low to High",
            "Price: High to Low",
            "Star Rating: High to Low",
            "Name: A to Z"
        );
        sortByComboBox.setValue("Best Match");
        
        // Set default price range
        minPriceField.setText("50");
        maxPriceField.setText("500");
        
        // Set date constraints
        checkInDatePicker.setValue(LocalDate.now().plusDays(1));
        checkOutDatePicker.setValue(LocalDate.now().plusDays(3));
        
        // Initialize spinners
        SpinnerValueFactory<Integer> guestsFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2);
        guestsSpinner.setValueFactory(guestsFactory);
        
        SpinnerValueFactory<Integer> roomsFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1);
        roomsSpinner.setValueFactory(roomsFactory);
    }

    private void setupEventHandlers() {
        // Update price calculator when dates change
        checkInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePriceCalculator();
            validateDates();
        });
        
        checkOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePriceCalculator();
            validateDates();
        });
        
        // Update rooms label when spinner changes
        roomsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            roomsLabel.setText(String.valueOf(newVal));
            updatePriceCalculator();
        });
        
        // Update price range label
        minPriceField.textProperty().addListener((obs, oldVal, newVal) -> updatePriceRangeLabel());
        maxPriceField.textProperty().addListener((obs, oldVal, newVal) -> updatePriceRangeLabel());
    }

    private void loadInitialData() {
        updatePriceCalculator();
        updatePriceRangeLabel();
    }

    @FXML
    private void handleSearch() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Build search filters
            SearchFilters filters = buildSearchFilters();
            
            // Show loading indicator
            resultsCountLabel.setText("Searching...");
            
            // Perform search
            currentResults = searchService.searchHotels(filters);
            
            // Update UI
            currentPage = 1;
            displayResults();
            updateResultsCount();
            
            // Update map if enabled
            if (showMapCheckBox.isSelected()) {
                loadMapWithHotels(currentResults);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Search Complete", 
                currentResults.size() + " hotels found");
            
        } catch (Exception e) {
            System.err.println("❌ Search error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Search Failed", 
                "An error occurred while searching. Please try again.");
        }
    }

    private boolean validateInputs() {
        // Validate destination
        if (destinationComboBox.getValue() == null || destinationComboBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select a destination");
            destinationComboBox.requestFocus();
            return false;
        }
        
        // Validate dates
        if (checkInDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select check-in date");
            checkInDatePicker.requestFocus();
            return false;
        }
        
        if (checkOutDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select check-out date");
            checkOutDatePicker.requestFocus();
            return false;
        }
        
        if (checkInDatePicker.getValue().isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "Check-in date cannot be in the past");
            checkInDatePicker.requestFocus();
            return false;
        }
        
        if (checkOutDatePicker.getValue().isBefore(checkInDatePicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "Check-out date must be after check-in date");
            checkOutDatePicker.requestFocus();
            return false;
        }
        
        // Validate price range
        try {
            double minPrice = Double.parseDouble(minPriceField.getText());
            double maxPrice = Double.parseDouble(maxPriceField.getText());
            
            if (minPrice < 0 || maxPrice < 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Price", "Price cannot be negative");
                return false;
            }
            
            if (minPrice > maxPrice) {
                showAlert(Alert.AlertType.WARNING, "Invalid Price Range", 
                    "Minimum price cannot be greater than maximum price");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Price", 
                "Please enter valid numeric values for price range");
            return false;
        }
        
        return true;
    }

    private void validateDates() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        
        if (checkIn != null && checkOut != null && checkOut.isBefore(checkIn)) {
            checkOutDatePicker.setValue(checkIn.plusDays(1));
        }
    }

    private SearchFilters buildSearchFilters() {
        SearchFilters filters = new SearchFilters();
        
        // Extract city from destination (e.g., "Paris, France" -> "Paris")
        String destination = destinationComboBox.getValue();
        if (destination != null && destination.contains(",")) {
            filters.setDestination(destination.split(",")[0].trim());
        } else {
            filters.setDestination(destination);
        }
        
        filters.setCheckInDate(checkInDatePicker.getValue());
        filters.setCheckOutDate(checkOutDatePicker.getValue());
        filters.setGuests(guestsSpinner.getValue());
        
        // Price range
        filters.setPriceMin(Double.parseDouble(minPriceField.getText()));
        filters.setPriceMax(Double.parseDouble(maxPriceField.getText()));
        
        // Star rating
        String starRating = starRatingComboBox.getValue();
        if (starRating != null && !starRating.equals("Any Rating")) {
            int minStars = Integer.parseInt(starRating.replaceAll("[^0-9]", ""));
            filters.setMinRating(minStars);
        }
        
        // Amenities
        List<String> amenities = new ArrayList<>();
        if (wifiCheckBox.isSelected()) amenities.add("WiFi");
        if (poolCheckBox.isSelected()) amenities.add("Pool");
        if (spaCheckBox.isSelected()) amenities.add("Spa");
        if (gymCheckBox.isSelected()) amenities.add("Gym");
        if (parkingCheckBox.isSelected()) amenities.add("Parking");
        if (restaurantCheckBox.isSelected()) amenities.add("Restaurant");
        if (barCheckBox.isSelected()) amenities.add("Bar");
        if (airportShuttleCheckBox.isSelected()) amenities.add("Airport Shuttle");
        if (petFriendlyCheckBox.isSelected()) amenities.add("Pet Friendly");
        if (businessCenterCheckBox.isSelected()) amenities.add("Business Center");
        filters.setAmenities(amenities);
        
        // Sort criteria
        String sortBy = sortByComboBox.getValue();
        if (sortBy != null) {
            filters.setSortBy(convertSortCriteria(sortBy));
        }
        
        return filters;
    }

    private String convertSortCriteria(String displayText) {
        switch (displayText) {
            case "Price: Low to High": return "price_low";
            case "Price: High to Low": return "price_high";
            case "Star Rating: High to Low": return "rating";
            case "Name: A to Z": return "name";
            default: return "best_match";
        }
    }

    private void displayResults() {
        resultsFlowPane.getChildren().clear();
        
        if (currentResults.isEmpty()) {
            Label noResults = new Label("No hotels found matching your criteria.\nTry adjusting your filters.");
            noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280; -fx-padding: 40;");
            resultsFlowPane.getChildren().add(noResults);
            return;
        }
        
        // Calculate pagination
        int startIndex = (currentPage - 1) * resultsPerPage;
        int endIndex = Math.min(startIndex + resultsPerPage, currentResults.size());
        
        // Display results for current page
        for (int i = startIndex; i < endIndex; i++) {
            Hotel hotel = currentResults.get(i);
            resultsFlowPane.getChildren().add(createHotelCard(hotel));
        }
        
        // Update pagination label
        int totalPages = (int) Math.ceil((double) currentResults.size() / resultsPerPage);
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
    }

    private VBox createHotelCard(Hotel hotel) {
        VBox card = new VBox(12);
        card.setPrefWidth(isGridView ? 350 : 800);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 12; " +
                     "-fx-padding: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4); " +
                     "-fx-cursor: hand;");
        
        // Hotel image placeholder
        Label imageLabel = new Label("🏨");
        imageLabel.setStyle("-fx-font-size: 48px; -fx-alignment: center;");
        imageLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Hotel name
        Label nameLabel = new Label(hotel.getNomHotel() != null ? hotel.getNomHotel() : "Unknown Hotel");
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        nameLabel.setWrapText(true);
        
        // Star rating
        HBox starsBox = new HBox(3);
        int stars = hotel.getEtoiles() != null ? hotel.getEtoiles().intValue() : 0;
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < stars ? "★" : "☆");
            star.setStyle("-fx-text-fill: " + (i < stars ? "#f59e0b" : "#d1d5db") + "; -fx-font-size: 14px;");
            starsBox.getChildren().add(star);
        }
        
        // Location
        Label locationLabel = new Label("📍 " + (hotel.getVille() != null ? hotel.getVille() : "Unknown"));
        locationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        
        // Price
        Label priceLabel = new Label("$" + hotel.getPricePerNight() + " / night");
        priceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");
        
        // Available rooms
        Label roomsAvailableLabel = new Label("Available");
        roomsAvailableLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;");
        
        // Book button
        Button bookButton = new Button("View Details & Book");
        bookButton.setStyle("-fx-background-color: #3b82f6; " +
                           "-fx-text-fill: white; " +
                           "-fx-background-radius: 8; " +
                           "-fx-padding: 10 20; " +
                           "-fx-font-weight: bold; " +
                           "-fx-cursor: hand;");
        bookButton.setMaxWidth(Double.MAX_VALUE);
        bookButton.setOnAction(e -> handleBookHotel(hotel));
        
        // Hover effect
        card.setOnMouseEntered(e -> 
            card.setStyle(card.getStyle() + "-fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> 
            card.setStyle(card.getStyle() + "-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        
        card.getChildren().addAll(
            imageLabel, nameLabel, starsBox, locationLabel, 
            priceLabel, roomsAvailableLabel, bookButton
        );
        
        return card;
    }

    @FXML
    private void handleClearFilters() {
        destinationComboBox.setValue(null);
        checkInDatePicker.setValue(LocalDate.now().plusDays(1));
        checkOutDatePicker.setValue(LocalDate.now().plusDays(3));
        guestsSpinner.getValueFactory().setValue(2);
        roomsSpinner.getValueFactory().setValue(1);
        minPriceField.setText("50");
        maxPriceField.setText("500");
        starRatingComboBox.setValue("Any Rating");
        sortByComboBox.setValue("Best Match");
        
        // Clear amenities
        wifiCheckBox.setSelected(false);
        poolCheckBox.setSelected(false);
        spaCheckBox.setSelected(false);
        gymCheckBox.setSelected(false);
        parkingCheckBox.setSelected(false);
        restaurantCheckBox.setSelected(false);
        barCheckBox.setSelected(false);
        airportShuttleCheckBox.setSelected(false);
        petFriendlyCheckBox.setSelected(false);
        businessCenterCheckBox.setSelected(false);
        
        // Clear results
        currentResults.clear();
        resultsFlowPane.getChildren().clear();
        resultsCountLabel.setText("");
        
        showAlert(Alert.AlertType.INFORMATION, "Filters Cleared", 
            "All search filters have been reset");
    }

    @FXML
    private void handleToggleMap() {
        boolean showMap = showMapCheckBox.isSelected();
        // TODO: Uncomment when JavaFX Web module is loaded
        // mapWebView.setVisible(showMap);
        // mapWebView.setManaged(showMap);
        
        if (showMap && !currentResults.isEmpty()) {
            loadMapWithHotels(currentResults);
        }
    }

    private void loadMapWithHotels(List<Hotel> hotels) {
        if (hotels.isEmpty()) return;
        
        // TODO: Uncomment when JavaFX Web module is loaded
        /*
        WebEngine engine = mapWebView.getEngine();
        
        // Get center coordinates (use first hotel's city)
        String city = hotels.get(0).getVille();
        if (city == null) city = "Paris";
        
        // Simple Google Maps embed
        String mapHTML = String.format(
            "<html><body style='margin:0;padding:0;'>" +
            "<iframe width='100%%' height='400' frameborder='0' style='border:0' " +
            "src='https://maps.google.com/maps?q=%s&output=embed' " +
            "allowfullscreen></iframe>" +
            "</body></html>",
            city.replace(" ", "+")
        );
        
        engine.loadContent(mapHTML);
        */
        
        System.out.println("Map feature disabled - requires JavaFX Web module");
    }

    @FXML
    private void handleGridView() {
        isGridView = true;
        resultsFlowPane.setHgap(20);
        resultsFlowPane.setVgap(20);
        displayResults();
    }

    @FXML
    private void handleListView() {
        isGridView = false;
        resultsFlowPane.setHgap(0);
        resultsFlowPane.setVgap(15);
        displayResults();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            displayResults();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) currentResults.size() / resultsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            displayResults();
        }
    }

    private void handleBookHotel(Hotel hotel) {
        SearchFilters filters = buildSearchFilters();
        
        // Show booking confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Booking");
        alert.setHeaderText("Book " + hotel.getNomHotel());
        alert.setContentText(String.format(
            "Check-in: %s\n" +
            "Check-out: %s\n" +
            "Guests: %d\n\n" +
            "Do you want to proceed with this booking?",
            filters.getCheckInDate(),
            filters.getCheckOutDate(),
            filters.getGuests()
        ));
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                proceedWithBooking(hotel, filters);
            }
        });
    }

    private void proceedWithBooking(Hotel hotel, SearchFilters filters) {
        // TODO: Navigate to booking confirmation page
        showAlert(Alert.AlertType.INFORMATION, "Booking Initiated", 
            "Redirecting to booking confirmation page...");
        
        System.out.println("📋 Booking Details:");
        System.out.println("   Hotel: " + hotel.getNomHotel());
        System.out.println("   Check-in: " + filters.getCheckInDate());
        System.out.println("   Check-out: " + filters.getCheckOutDate());
    }

    private void updatePriceCalculator() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        
        if (checkIn != null && checkOut != null && !checkOut.isBefore(checkIn)) {
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            nightsLabel.setText(String.valueOf(nights));
            
            int rooms = roomsSpinner.getValue();
            roomsLabel.setText(String.valueOf(rooms));
            
            // Calculate average price (use mid-range of price filter)
            try {
                double minPrice = Double.parseDouble(minPriceField.getText());
                double maxPrice = Double.parseDouble(maxPriceField.getText());
                double avgPrice = (minPrice + maxPrice) / 2;
                avgPriceLabel.setText(String.format("$%.0f", avgPrice));
                
                // Calculate total
                double total = nights * rooms * avgPrice;
                totalCostLabel.setText(String.format("$%.0f", total));
            } catch (NumberFormatException e) {
                avgPriceLabel.setText("$0");
                totalCostLabel.setText("$0");
            }
        } else {
            nightsLabel.setText("0");
            totalCostLabel.setText("$0");
        }
    }

    private void updatePriceRangeLabel() {
        try {
            String min = minPriceField.getText();
            String max = maxPriceField.getText();
            priceRangeLabel.setText("$" + min + " - $" + max);
        } catch (Exception e) {
            priceRangeLabel.setText("Invalid range");
        }
    }

    private void updateResultsCount() {
        if (currentResults.isEmpty()) {
            resultsCountLabel.setText("No results found");
        } else {
            resultsCountLabel.setText(currentResults.size() + " hotels found");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
