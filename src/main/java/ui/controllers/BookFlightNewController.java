package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Popup;
import ui.model.Flight;
import ui.model.FlightBooking;
import ui.model.FlightClass;
import ui.model.User;
import ui.service.FlightBookingService;
import ui.service.FlightService;
import ui.util.SceneManager;
import ui.util.SessionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BookFlightNewController {

    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private DatePicker departureDate;
    @FXML private DatePicker returnDate;
    @FXML private VBox returnDateBox;
    @FXML private Spinner<Integer> passengersSpinner;
    @FXML private VBox flightResultsContainer;
    @FXML private Label noResultsLabel;

    // Trip type buttons
    @FXML private ToggleButton roundTripBtn;
    @FXML private ToggleButton oneWayBtn;
    @FXML private ToggleButton multiCityBtn;

    // Class buttons
    @FXML private ToggleButton economyBtn;
    @FXML private ToggleButton premiumBtn;
    @FXML private ToggleButton businessBtn;
    @FXML private ToggleButton firstBtn;

    private FlightService flightService;
    private FlightBookingService bookingService;
    private ToggleGroup tripTypeGroup;
    private ToggleGroup classGroup;
    private String selectedClass = "ECONOMIQUE";

    // City autocomplete
    private List<String> availableCities;
    private Popup fromPopup;
    private Popup toPopup;
    private ListView<String> fromListView;
    private ListView<String> toListView;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private static final String ACTIVE_BTN_STYLE = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-size: 13px; -fx-cursor: hand;";
    private static final String INACTIVE_BTN_STYLE = "-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 20; -fx-padding: 8 20; -fx-border-color: #e5e7eb; -fx-border-radius: 20; -fx-font-size: 13px; -fx-cursor: hand;";

    @FXML
    private void initialize() {
        flightService = new FlightService();
        bookingService = new FlightBookingService();

        // Setup passengers spinner
        passengersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1));

        // Setup toggle groups
        setupTripTypeToggle();
        setupClassToggle();

        // Set default date
        departureDate.setValue(LocalDate.now().plusDays(7));
        returnDate.setValue(LocalDate.now().plusDays(14));

        // Load available cities and setup autocomplete
        loadAvailableCities();
        setupCityAutocomplete();

        // Load initial flights
        loadAvailableFlights();

        // Add listener to close popups when scene changes
        setupSceneChangeListener();
    }

    /**
     * Setup listener to close popups when navigating away from this page
     */
    private void setupSceneChangeListener() {
        // Listen for when this component is removed from scene
        flightResultsContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            // Close all popups when scene changes
            closeAllPopups();
        });

        // Also close popups when parent changes
        flightResultsContainer.parentProperty().addListener((obs, oldParent, newParent) -> {
            closeAllPopups();
        });
    }

    /**
     * Close all open popups
     */
    public void closeAllPopups() {
        if (fromPopup != null && fromPopup.isShowing()) {
            fromPopup.hide();
        }
        if (toPopup != null && toPopup.isShowing()) {
            toPopup.hide();
        }
    }

    private void loadAvailableCities() {
        // Get all unique cities from flights
        availableCities = flightService.getAllCities();

        // If no cities from database, use default list
        if (availableCities == null || availableCities.isEmpty()) {
            availableCities = List.of(
                "Paris", "New York", "London", "Tokyo", "Dubai",
                "Los Angeles", "Chicago", "Miami", "San Francisco", "Boston",
                "Toronto", "Montreal", "Vancouver", "Mexico City",
                "Madrid", "Barcelona", "Rome", "Milan", "Berlin", "Munich",
                "Amsterdam", "Brussels", "Zurich", "Vienna", "Prague",
                "Singapore", "Hong Kong", "Seoul", "Bangkok", "Sydney",
                "Melbourne", "Auckland", "Cairo", "Johannesburg", "Cape Town",
                "Casablanca", "Marrakech", "Tunis", "Algiers",
                "Sao Paulo", "Rio de Janeiro", "Buenos Aires", "Lima", "Bogota"
            );
        }
        System.out.println("Loaded " + availableCities.size() + " cities for autocomplete");
    }

    private void setupCityAutocomplete() {
        // Setup FROM field autocomplete
        fromPopup = new Popup();
        fromPopup.setAutoHide(true); // Auto hide when clicking outside
        fromListView = new ListView<>();
        fromListView.setPrefHeight(200);
        fromListView.setPrefWidth(250);
        fromListView.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");
        fromPopup.getContent().add(fromListView);

        fromField.setOnMouseClicked(e -> showCityPopup(fromField, fromPopup, fromListView, ""));
        fromField.textProperty().addListener((obs, oldVal, newVal) -> {
            showCityPopup(fromField, fromPopup, fromListView, newVal);
        });
        fromField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                // Small delay to allow selection
                javafx.application.Platform.runLater(() -> {
                    if (!fromListView.isFocused()) {
                        fromPopup.hide();
                    }
                });
            }
        });
        fromListView.setOnMouseClicked(e -> {
            String selected = fromListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                fromField.setText(selected);
                fromPopup.hide();
            }
        });

        // Setup TO field autocomplete
        toPopup = new Popup();
        toPopup.setAutoHide(true); // Auto hide when clicking outside
        toListView = new ListView<>();
        toListView.setPrefHeight(200);
        toListView.setPrefWidth(250);
        toListView.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");
        toPopup.getContent().add(toListView);

        toField.setOnMouseClicked(e -> showCityPopup(toField, toPopup, toListView, ""));
        toField.textProperty().addListener((obs, oldVal, newVal) -> {
            showCityPopup(toField, toPopup, toListView, newVal);
        });
        toField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                javafx.application.Platform.runLater(() -> {
                    if (!toListView.isFocused()) {
                        toPopup.hide();
                    }
                });
            }
        });
        toListView.setOnMouseClicked(e -> {
            String selected = toListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toField.setText(selected);
                toPopup.hide();
            }
        });
    }

    private void showCityPopup(TextField field, Popup popup, ListView<String> listView, String filter) {
        if (field.getScene() == null || field.getScene().getWindow() == null) return;

        List<String> filteredCities;
        if (filter == null || filter.isEmpty()) {
            filteredCities = availableCities;
        } else {
            String lowerFilter = filter.toLowerCase();
            filteredCities = availableCities.stream()
                .filter(city -> city.toLowerCase().contains(lowerFilter))
                .collect(Collectors.toList());
        }

        if (filteredCities.isEmpty()) {
            popup.hide();
            return;
        }

        listView.setItems(FXCollections.observableArrayList(filteredCities));

        if (!popup.isShowing()) {
            // Position the popup below the text field
            var bounds = field.localToScreen(field.getBoundsInLocal());
            if (bounds != null) {
                popup.show(field.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY() + 5);
            }
        }
    }

    private void setupTripTypeToggle() {
        tripTypeGroup = new ToggleGroup();
        roundTripBtn.setToggleGroup(tripTypeGroup);
        oneWayBtn.setToggleGroup(tripTypeGroup);
        multiCityBtn.setToggleGroup(tripTypeGroup);
        roundTripBtn.setSelected(true);

        tripTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateTripTypeStyles();
            // Show/hide return date based on selection
            boolean isRoundTrip = roundTripBtn.isSelected();
            returnDateBox.setVisible(isRoundTrip);
            returnDateBox.setManaged(isRoundTrip);
        });
    }

    private void setupClassToggle() {
        classGroup = new ToggleGroup();
        economyBtn.setToggleGroup(classGroup);
        premiumBtn.setToggleGroup(classGroup);
        businessBtn.setToggleGroup(classGroup);
        firstBtn.setToggleGroup(classGroup);
        economyBtn.setSelected(true);

        classGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateClassStyles();
            if (economyBtn.isSelected()) selectedClass = "ECONOMIQUE";
            else if (premiumBtn.isSelected()) selectedClass = "PREMIUM";
            else if (businessBtn.isSelected()) selectedClass = "BUSINESS";
            else if (firstBtn.isSelected()) selectedClass = "PREMIERE";
        });
    }

    private void updateTripTypeStyles() {
        roundTripBtn.setStyle(roundTripBtn.isSelected() ? ACTIVE_BTN_STYLE : INACTIVE_BTN_STYLE);
        oneWayBtn.setStyle(oneWayBtn.isSelected() ? ACTIVE_BTN_STYLE : INACTIVE_BTN_STYLE);
        multiCityBtn.setStyle(multiCityBtn.isSelected() ? ACTIVE_BTN_STYLE : INACTIVE_BTN_STYLE);
    }

    private void updateClassStyles() {
        economyBtn.setStyle(economyBtn.isSelected() ? ACTIVE_BTN_STYLE : INACTIVE_BTN_STYLE);
        premiumBtn.setStyle(premiumBtn.isSelected() ? ACTIVE_BTN_STYLE : INACTIVE_BTN_STYLE);
        businessBtn.setStyle(businessBtn.isSelected() ? ACTIVE_BTN_STYLE : INACTIVE_BTN_STYLE);
        firstBtn.setStyle(firstBtn.isSelected() ? ACTIVE_BTN_STYLE : INACTIVE_BTN_STYLE);
    }

    @FXML
    private void handleSwapCities() {
        String from = fromField.getText();
        String to = toField.getText();
        fromField.setText(to);
        toField.setText(from);
    }

    @FXML
    private void handleSearch() {
        // Close any open popups first
        closeAllPopups();

        String from = fromField.getText() != null ? fromField.getText().trim() : "";
        String to = toField.getText() != null ? toField.getText().trim() : "";
        LocalDate date = departureDate.getValue();

        System.out.println("🔍 Searching flights...");
        System.out.println("   From: " + (from.isEmpty() ? "Any" : from));
        System.out.println("   To: " + (to.isEmpty() ? "Any" : to));
        System.out.println("   Date: " + (date != null ? date : "Any"));
        System.out.println("   Class: " + selectedClass);

        try {
            List<Flight> flights;

            // If both fields are empty, load all available flights
            if (from.isEmpty() && to.isEmpty() && date == null) {
                System.out.println("📋 Loading all available flights (no filters)...");
                flights = flightService.getAvailableFlights();
            } else {
                // Search with specific criteria
                System.out.println("🔎 Searching with filters...");
                flights = flightService.searchFlightsAdvanced(from, to, date, selectedClass);

                // If still no results, try without date filter
                if (flights.isEmpty() && date != null && (!from.isEmpty() || !to.isEmpty())) {
                    System.out.println("⚠️ No flights on this date, trying without date filter...");
                    flights = flightService.searchFlightsAdvanced(from, to, null, selectedClass);
                }
            }

            System.out.println("✅ Found " + flights.size() + " flights matching criteria");

            // Display results (don't fallback to all flights - show "no results" instead)
            displayFlightResults(flights, from, to);

        } catch (Exception e) {
            System.err.println("❌ Error searching flights: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Search Error", "Failed to search flights: " + e.getMessage());
        }
    }

    private void loadAvailableFlights() {
        List<Flight> flights = flightService.getAvailableFlights();
        displayFlightResults(flights, "", "");
    }

    private void displayFlightResults(List<Flight> flights, String searchFrom, String searchTo) {
        flightResultsContainer.getChildren().clear();

        System.out.println("📊 Displaying " + flights.size() + " flights");

        if (flights.isEmpty()) {
            String message;
            if (!searchFrom.isEmpty() || !searchTo.isEmpty()) {
                message = String.format("No flights found from '%s' to '%s'.\nTry different cities or dates.",
                    searchFrom.isEmpty() ? "Any" : searchFrom,
                    searchTo.isEmpty() ? "Any" : searchTo);
            } else {
                message = "No flights found. Try different search criteria.";
            }
            noResultsLabel.setText(message);
            noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-padding: 40;");
            flightResultsContainer.getChildren().add(noResultsLabel);

            // Add a helpful message
            Label helpLabel = new Label("💡 Available cities: Paris, New York, Dubai, London, Los Angeles, Tokyo, Frankfurt, Singapore");
            helpLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-padding: 10;");
            helpLabel.setWrapText(true);
            flightResultsContainer.getChildren().add(helpLabel);
            return;
        }

        // Show results header
        String resultsHeader = String.format("Found %d flight%s", flights.size(), flights.size() > 1 ? "s" : "");
        if (!searchFrom.isEmpty() || !searchTo.isEmpty()) {
            resultsHeader += String.format(" from %s to %s",
                searchFrom.isEmpty() ? "Any" : searchFrom,
                searchTo.isEmpty() ? "Any" : searchTo);
        }
        Label headerLabel = new Label(resultsHeader);
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #374151; -fx-padding: 0 0 10 0;");
        flightResultsContainer.getChildren().add(headerLabel);

        for (Flight flight : flights) {
            try {
                VBox flightCard = createFlightCard(flight);
                flightResultsContainer.getChildren().add(flightCard);
            } catch (Exception e) {
                System.err.println("❌ Error creating flight card: " + e.getMessage());
            }
        }

        System.out.println("✅ Flight results displayed successfully");
    }

    private void displayFlightResults(List<Flight> flights) {
        displayFlightResults(flights, "", "");
    }

    private VBox createFlightCard(Flight flight) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Main content row
        HBox mainRow = new HBox(20);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // Left: Airline info and departure time
        VBox leftSection = new VBox(3);
        leftSection.setPrefWidth(120);

        // Departure time
        Label departureTime = new Label(flight.getDateDepart() != null ?
            flight.getDateDepart().format(TIME_FORMATTER) : "N/A");
        departureTime.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // Departure city code
        Label departureCity = new Label(flight.getCodeAeroportDepart() != null ?
            flight.getCodeAeroportDepart() : flight.getVilleDepart());
        departureCity.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        // Airline and flight number
        Label airlineLabel = new Label(flight.getCompagnieName() != null ?
            flight.getCompagnieName() : "Airline");
        airlineLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        Label flightNumber = new Label(flight.getNumeroVol());
        flightNumber.setStyle("-fx-font-size: 11px; -fx-text-fill: #3b82f6;");

        leftSection.getChildren().addAll(departureTime, departureCity, airlineLabel, flightNumber);

        // Center: Flight route visualization
        VBox centerSection = new VBox(5);
        centerSection.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerSection, Priority.ALWAYS);

        // Duration
        Label durationLabel = new Label(flight.getDurationDisplay());
        durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // Flight path line with plane icon
        HBox flightPath = new HBox();
        flightPath.setAlignment(Pos.CENTER);

        Line leftLine = new Line(0, 0, 150, 0);
        leftLine.setStroke(Color.web("#e5e7eb"));
        leftLine.setStrokeWidth(2);

        Label planeIcon = new Label("✈");
        planeIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #3b82f6;");

        Line rightLine = new Line(0, 0, 150, 0);
        rightLine.setStroke(Color.web("#e5e7eb"));
        rightLine.setStrokeWidth(2);

        flightPath.getChildren().addAll(leftLine, planeIcon, rightLine);

        // Stops info
        Label stopsLabel = new Label(flight.getStopsDisplay());
        stopsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        centerSection.getChildren().addAll(durationLabel, flightPath, stopsLabel);

        // Right arrival: Arrival time
        VBox arrivalSection = new VBox(3);
        arrivalSection.setPrefWidth(100);
        arrivalSection.setAlignment(Pos.CENTER_LEFT);

        Label arrivalTime = new Label(flight.getDateArrivee() != null ?
            flight.getDateArrivee().format(TIME_FORMATTER) : "N/A");
        arrivalTime.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label arrivalCity = new Label(flight.getCodeAeroportArrivee() != null ?
            flight.getCodeAeroportArrivee() : flight.getVilleArrivee());
        arrivalCity.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        arrivalSection.getChildren().addAll(arrivalTime, arrivalCity);

        // Price and book section
        VBox priceSection = new VBox(5);
        priceSection.setAlignment(Pos.CENTER_RIGHT);
        priceSection.setPrefWidth(150);

        Label priceLabel = new Label(String.format("$%.0f", flight.getMinPrice()));
        priceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        Label classLabel = new Label(getClassDisplayName(flight.getDefaultClass()));
        classLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Button selectBtn = new Button("Select Flight");
        selectBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 13px; " +
                          "-fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        selectBtn.setOnAction(e -> handleSelectFlight(flight));

        Label seatsLabel = new Label(flight.getPlacesDisponibles() + " seats available");
        seatsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        priceSection.getChildren().addAll(priceLabel, classLabel, selectBtn, seatsLabel);

        mainRow.getChildren().addAll(leftSection, centerSection, arrivalSection, priceSection);

        // Amenities row
        HBox amenitiesRow = new HBox(20);
        amenitiesRow.setAlignment(Pos.CENTER_LEFT);
        amenitiesRow.setPadding(new Insets(10, 0, 0, 0));

        if (flight.isHasWifi()) {
            amenitiesRow.getChildren().add(createAmenityLabel("✓ Free WiFi"));
        }
        if (flight.isHasEntertainment()) {
            amenitiesRow.getChildren().add(createAmenityLabel("✓ In-flight Entertainment"));
        }
        if (flight.isHasMeal()) {
            amenitiesRow.getChildren().add(createAmenityLabel("✓ Meal Included"));
        }
        if (flight.isHasFreeCancellation()) {
            amenitiesRow.getChildren().add(createAmenityLabel("✓ Free Cancellation"));
        }

        card.getChildren().addAll(mainRow, amenitiesRow);

        return card;
    }

    private Label createAmenityLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;");
        return label;
    }

    private String getClassDisplayName(String classType) {
        if (classType == null) return "Economy";
        switch (classType) {
            case "ECONOMIQUE": return "Economy";
            case "PREMIUM": return "Premium";
            case "BUSINESS": return "Business";
            case "PREMIERE": return "First Class";
            default: return classType;
        }
    }

    private void handleSelectFlight(Flight flight) {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Login Required",
                     "Please log in to book a flight.");
            return;
        }

        // Check if user is a traveler (VOYAGEUR) or has booking privileges
        if (currentUser.getUserType() == User.UserType.VISITEUR) {
            showAlert(Alert.AlertType.WARNING, "Account Required",
                     "Please create a traveler account to book flights.");
            return;
        }

        // Navigate to flight details page
        navigateToFlightDetails(flight);
    }

    private void navigateToFlightDetails(Flight flight) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/ui/flight-details.fxml")
            );
            javafx.scene.layout.Pane detailsPane = loader.load();

            FlightDetailsController controller = loader.getController();
            controller.setFlight(flight);

            // Replace current content with flight details
            javafx.scene.Scene scene = flightResultsContainer.getScene();
            if (scene != null) {
                javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) scene.getRoot();
                root.setCenter(detailsPane);
            }
        } catch (Exception e) {
            System.err.println("❌ Error navigating to flight details: " + e.getMessage());
            e.printStackTrace();
            // Fallback to old booking dialog
            showBookingDialog(flight);
        }
    }

    private void showBookingDialog(Flight flight) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Flight - Step 1: Select Class");
        dialog.setHeaderText("Step 1: Choose Your Class");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // Flight details
        Label flightInfo = new Label(String.format("✈️ Flight: %s\n📍 Route: %s → %s\n📅 Date: %s\n⏰ Departure: %s",
            flight.getNumeroVol(),
            flight.getVilleDepart(),
            flight.getVilleArrivee(),
            flight.getDateDepart() != null ? flight.getDateDepart().format(DATE_FORMATTER) : "N/A",
            flight.getDateDepart() != null ? flight.getDateDepart().format(TIME_FORMATTER) : "N/A"));
        flightInfo.setStyle("-fx-font-size: 14px; -fx-background-color: #f0f9ff; -fx-padding: 15; -fx-background-radius: 8;");

        // Class selection
        Label classLabel = new Label("Select Your Class:");
        classLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.getItems().addAll("Economy", "Premium", "Business", "First Class");
        classCombo.setValue(getClassDisplayName(selectedClass));
        classCombo.setMaxWidth(Double.MAX_VALUE);
        classCombo.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        // Passengers
        Label passengersLabel = new Label("👥 Passengers: " + passengersSpinner.getValue());
        passengersLabel.setStyle("-fx-font-size: 14px;");

        // Price calculation
        double basePrice = flight.getMinPrice();
        int passengers = passengersSpinner.getValue();
        double totalPrice = basePrice * passengers;

        Label priceLabel = new Label(String.format("💰 Total Price: $%.2f", totalPrice));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        // Step indicator
        Label stepIndicator = new Label("Step 1 of 3: Class Selection → Seat Selection → Confirmation");
        stepIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        content.getChildren().addAll(stepIndicator, new Separator(), flightInfo, new Separator(),
                                     classLabel, classCombo, passengersLabel, new Separator(), priceLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.CANCEL);

        Button nextButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.NEXT);
        nextButton.setText("Next: Select Seats →");
        nextButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.NEXT) {
                // Proceed to seat selection
                showSeatSelectionDialog(flight, passengers, totalPrice, classCombo.getValue());
            }
        });
    }

    /**
     * Step 2: Seat Selection Dialog
     */
    private void showSeatSelectionDialog(Flight flight, int passengers, double totalPrice, String className) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Flight - Step 2: Select Seats");
        dialog.setHeaderText("Step 2: Choose Your Seats");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        content.setPrefHeight(450);

        // Step indicator
        Label stepIndicator = new Label("Step 2 of 3: Class Selection ✓ → Seat Selection → Confirmation");
        stepIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        // Flight summary
        Label summaryLabel = new Label(String.format("✈️ %s | %s → %s | %s | %d passenger(s)",
            flight.getNumeroVol(), flight.getVilleDepart(), flight.getVilleArrivee(), className, passengers));
        summaryLabel.setStyle("-fx-font-size: 13px; -fx-background-color: #f0f9ff; -fx-padding: 10; -fx-background-radius: 8;");

        // Seat map header
        Label seatHeader = new Label("🪑 Select Your Seat(s):");
        seatHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Seat legend
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        Label availableLegend = new Label("🟢 Available");
        Label occupiedLegend = new Label("🔴 Occupied");
        Label selectedLegend = new Label("🔵 Selected");
        legend.getChildren().addAll(availableLegend, occupiedLegend, selectedLegend);

        // Seat selection grid
        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(8);
        seatGrid.setVgap(8);
        seatGrid.setAlignment(Pos.CENTER);
        seatGrid.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Column headers (A B C - aisle - D E F)
        String[] columns = {"A", "B", "C", "", "D", "E", "F"};
        for (int col = 0; col < columns.length; col++) {
            Label colLabel = new Label(columns[col]);
            colLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-min-width: 35; -fx-alignment: center;");
            colLabel.setAlignment(Pos.CENTER);
            seatGrid.add(colLabel, col, 0);
        }

        // Track selected seats
        java.util.List<String> selectedSeats = new java.util.ArrayList<>();
        java.util.List<Button> seatButtons = new java.util.ArrayList<>();

        // Generate seat rows (rows 1-10 for demo)
        int maxRows = Math.min(10, (int) Math.ceil(flight.getCapaciteTotale() / 6.0));
        java.util.Random random = new java.util.Random(flight.getVolId()); // Consistent random for same flight

        for (int row = 1; row <= maxRows; row++) {
            // Row number
            Label rowLabel = new Label(String.valueOf(row));
            rowLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            seatGrid.add(rowLabel, 7, row);

            for (int col = 0; col < columns.length; col++) {
                if (columns[col].isEmpty()) {
                    // Aisle
                    Label aisle = new Label("  ");
                    seatGrid.add(aisle, col, row);
                    continue;
                }

                String seatNumber = row + columns[col];
                boolean isOccupied = random.nextDouble() < 0.3; // 30% occupied

                Button seatBtn = new Button(seatNumber);
                seatBtn.setPrefSize(35, 30);
                seatBtn.setStyle(isOccupied ?
                    "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 10px; -fx-background-radius: 5;" :
                    "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 10px; -fx-background-radius: 5; -fx-cursor: hand;");

                if (!isOccupied) {
                    seatButtons.add(seatBtn);
                    seatBtn.setOnAction(e -> {
                        if (selectedSeats.contains(seatNumber)) {
                            // Deselect
                            selectedSeats.remove(seatNumber);
                            seatBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 10px; -fx-background-radius: 5; -fx-cursor: hand;");
                        } else if (selectedSeats.size() < passengers) {
                            // Select
                            selectedSeats.add(seatNumber);
                            seatBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 5; -fx-cursor: hand;");
                        } else {
                            // Max seats reached
                            showAlert(Alert.AlertType.WARNING, "Seat Limit",
                                "You can only select " + passengers + " seat(s). Deselect a seat first.");
                        }
                    });
                } else {
                    seatBtn.setDisable(true);
                }

                seatGrid.add(seatBtn, col, row);
            }
        }

        // Scroll pane for seat grid
        ScrollPane scrollPane = new ScrollPane(seatGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Selected seats display
        Label selectedLabel = new Label("Selected seats: None");
        selectedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        // Update selected label when seats change
        for (Button btn : seatButtons) {
            btn.setOnAction(e -> {
                String seatNumber = btn.getText();
                if (selectedSeats.contains(seatNumber)) {
                    selectedSeats.remove(seatNumber);
                    btn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 10px; -fx-background-radius: 5; -fx-cursor: hand;");
                } else if (selectedSeats.size() < passengers) {
                    selectedSeats.add(seatNumber);
                    btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 5; -fx-cursor: hand;");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Seat Limit",
                        "You can only select " + passengers + " seat(s). Deselect a seat first.");
                    return;
                }
                selectedLabel.setText("Selected seats: " + (selectedSeats.isEmpty() ? "None" : String.join(", ", selectedSeats)));
            });
        }

        content.getChildren().addAll(stepIndicator, summaryLabel, new Separator(),
                                     seatHeader, legend, scrollPane, selectedLabel);

        dialog.getDialogPane().setContent(content);

        ButtonType confirmType = new ButtonType("Confirm Booking", ButtonBar.ButtonData.OK_DONE);
        ButtonType backType = new ButtonType("← Back", ButtonBar.ButtonData.BACK_PREVIOUS);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, backType, ButtonType.CANCEL);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmType);
        confirmButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == confirmType) {
                // Check if seats selected
                if (selectedSeats.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "No Seats Selected",
                        "Please select at least one seat before confirming.");
                    showSeatSelectionDialog(flight, passengers, totalPrice, className);
                    return;
                }
                // Proceed to confirmation
                String seatsStr = String.join(", ", selectedSeats);
                createBookingWithSeats(flight, passengers, totalPrice, className, seatsStr);
            } else if (response == backType) {
                // Go back to class selection
                showBookingDialog(flight);
            }
        });
    }

    /**
     * Step 3: Create booking with selected seats
     */
    private void createBookingWithSeats(Flight flight, int passengers, double totalPrice, String className, String selectedSeats) {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        // Get voyageur_id from user
        int voyageurId = getTravelerIdForUser(currentUser.getUserId());
        if (voyageurId == -1) {
            showAlert(Alert.AlertType.ERROR, "Booking Error",
                     "Could not find traveler profile. Please contact support.");
            return;
        }

        // Get class ID
        List<FlightClass> classes = bookingService.getFlightClasses(flight.getVolId());
        int classeVolId = classes.isEmpty() ? 1 : classes.get(0).getClasseVolId();

        // Find matching class
        for (FlightClass fc : classes) {
            if (fc.getDisplayName().equals(className)) {
                classeVolId = fc.getClasseVolId();
                break;
            }
        }

        // Create booking with seats
        FlightBooking booking = new FlightBooking();
        booking.setVoyageurId(voyageurId);
        booking.setVolId(flight.getVolId());
        booking.setClasseVolId(classeVolId);
        booking.setNombrePassagers(passengers);
        booking.setPrixTotal(BigDecimal.valueOf(totalPrice));
        booking.setStatutReservation(FlightBooking.StatutReservation.EN_ATTENTE);
        booking.setSiegesAttribues(selectedSeats); // Set selected seats

        System.out.println("📋 Creating booking with seats: " + selectedSeats);

        boolean success = bookingService.add(booking);

        if (success) {
            // Set additional info for email (using existing setters)
            booking.setPassengerName(currentUser.getFirstName() + " " + currentUser.getLastName());
            booking.setPassengerEmail(currentUser.getEmail());
            booking.setNumeroVol(flight.getNumeroVol());
            booking.setVilleDepart(flight.getVilleDepart());
            booking.setVilleArrivee(flight.getVilleArrivee());
            booking.setDateDepart(flight.getDateDepart());
            booking.setDateArrivee(flight.getDateArrivee());
            booking.setClasseNom(className);

            // Send confirmation email
            sendBookingConfirmationEmail(booking, flight, className, selectedSeats, totalPrice);

            // Show final confirmation dialog
            showFinalConfirmationDialog(booking, flight, className, selectedSeats, totalPrice);
            // Refresh flight list
            loadAvailableFlights();
        } else {
            showAlert(Alert.AlertType.ERROR, "Booking Failed",
                     "Could not create booking. Please try again.");
        }
    }

    /**
     * Send booking confirmation email to eya.khemirii@gmail.com
     */
    private void sendBookingConfirmationEmail(FlightBooking booking, Flight flight, String className, String seats, double totalPrice) {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();

            // Send to this fixed email address for testing
            String recipientEmail = "eya.khemirii@gmail.com";

            // Create email service
            ui.service.EmailNotificationService emailService = new ui.service.EmailNotificationService();

            // Build email subject
            String subject = "✈️ TripWise - Booking Confirmation " + booking.getNumeroConfirmation();

            // Build email content
            StringBuilder content = new StringBuilder();
            content.append("========================================\n");
            content.append("       ✈️ BOOKING CONFIRMATION\n");
            content.append("========================================\n\n");
            content.append("Dear ").append(currentUser.getFirstName()).append(" ").append(currentUser.getLastName()).append(",\n\n");
            content.append("Your flight has been successfully booked!\n\n");
            content.append("🎫 CONFIRMATION NUMBER: ").append(booking.getNumeroConfirmation()).append("\n\n");
            content.append("FLIGHT DETAILS:\n");
            content.append("───────────────────────────────────────\n");
            content.append("✈️ Flight: ").append(flight.getNumeroVol()).append("\n");
            content.append("📍 Route: ").append(flight.getVilleDepart()).append(" → ").append(flight.getVilleArrivee()).append("\n");
            content.append("📅 Date: ").append(flight.getDateDepart() != null ? flight.getDateDepart().format(DATE_FORMATTER) : "N/A").append("\n");
            content.append("🕐 Departure: ").append(flight.getDateDepart() != null ? flight.getDateDepart().format(TIME_FORMATTER) : "N/A").append("\n");
            content.append("🕐 Arrival: ").append(flight.getDateArrivee() != null ? flight.getDateArrivee().format(TIME_FORMATTER) : "N/A").append("\n");
            content.append("💺 Class: ").append(className).append("\n");
            content.append("🪑 Seats: ").append(seats).append("\n");
            content.append("👥 Passengers: ").append(booking.getNombrePassagers()).append("\n");
            content.append("───────────────────────────────────────\n");
            content.append("💰 TOTAL PRICE: $").append(String.format("%.2f", totalPrice)).append("\n\n");
            content.append("⏳ STATUS: Pending Confirmation\n");
            content.append("An employee will confirm your booking shortly.\n\n");
            content.append("Thank you for choosing TripWise! ✨\n\n");
            content.append("Best regards,\n");
            content.append("TripWise Travel Agency\n");
            content.append("========================================\n");

            // Send email using the service
            boolean sent = emailService.sendEmailDirect(recipientEmail, subject, content.toString());

            if (sent) {
                System.out.println("✅ Confirmation email sent to: " + recipientEmail);
            } else {
                System.out.println("📧 Email logged (check console above)");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Could not send confirmation email: " + e.getMessage());
            // Don't fail the booking if email fails
        }
    }

    /**
     * Show final booking confirmation with all details
     */
    private void showFinalConfirmationDialog(FlightBooking booking, Flight flight, String className, String seats, double totalPrice) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("✅ Booking Confirmed!");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setPrefWidth(450);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white;");

        // Success icon
        Label successIcon = new Label("✅");
        successIcon.setStyle("-fx-font-size: 60px;");

        // Title
        Label titleLabel = new Label("Booking Confirmed!");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

        // Confirmation number - handle null case
        String confirmNumber = booking.getNumeroConfirmation();
        if (confirmNumber == null || confirmNumber.isEmpty()) {
            confirmNumber = "FL" + String.format("%04d", booking.getReservationId() > 0 ? booking.getReservationId() : System.currentTimeMillis() % 10000);
        }
        Label confirmLabel = new Label("Confirmation: " + confirmNumber);
        confirmLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #f0fdf4; -fx-padding: 10 20; -fx-background-radius: 8;");

        // Details box
        VBox detailsBox = new VBox(8);
        detailsBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Label flightDetail = new Label(String.format("✈️ Flight: %s", flight.getNumeroVol()));
        Label routeDetail = new Label(String.format("📍 Route: %s → %s", flight.getVilleDepart(), flight.getVilleArrivee()));
        Label dateDetail = new Label(String.format("📅 Date: %s",
            flight.getDateDepart() != null ? flight.getDateDepart().format(DATE_FORMATTER) : "N/A"));
        Label classDetail = new Label(String.format("💺 Class: %s", className));
        Label seatsDetail = new Label(String.format("🪑 Seats: %s", seats));
        Label passengersDetail = new Label(String.format("👥 Passengers: %d", booking.getNombrePassagers()));
        Label priceDetail = new Label(String.format("💰 Total: $%.2f", totalPrice));
        priceDetail.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3b82f6;");

        detailsBox.getChildren().addAll(flightDetail, routeDetail, dateDetail, classDetail, seatsDetail, passengersDetail, priceDetail);

        // Status
        Label statusLabel = new Label("⏳ Status: Pending Confirmation");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f59e0b;");

        // Info
        Label infoLabel = new Label("An employee will confirm your booking shortly.\nYou will receive a notification once confirmed.");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-text-alignment: center;");
        infoLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        content.getChildren().addAll(successIcon, titleLabel, confirmLabel, new Separator(),
                                     detailsBox, statusLabel, infoLabel);

        dialog.getDialogPane().setContent(content);

        ButtonType viewBookingsType = new ButtonType("View My Bookings", ButtonBar.ButtonData.LEFT);
        ButtonType doneType = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(viewBookingsType, doneType);

        Button viewButton = (Button) dialog.getDialogPane().lookupButton(viewBookingsType);
        viewButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white;");

        Button doneButton = (Button) dialog.getDialogPane().lookupButton(doneType);
        doneButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == viewBookingsType) {
                // Navigate to My Bookings
                try {
                    SceneManager.switchScene("/ui/traveler-bookings.fxml");
                } catch (Exception e) {
                    System.err.println("Could not navigate to bookings: " + e.getMessage());
                }
            }
        });
    }

    private int getTravelerIdForUser(int userId) {
        try {
            java.sql.Connection conn = ui.util.DataSource.getInstance().getConnection();

            // First, try to get existing voyageur
            String query = "SELECT voyageur_id FROM voyageurs WHERE user_id = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("voyageur_id");
            }

            // If not found, create a new voyageur profile
            System.out.println("Creating new traveler profile for user: " + userId);
            String insertQuery = "INSERT INTO voyageurs (user_id, points_fidelite, created_at) VALUES (?, 0, NOW())";
            java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertQuery, java.sql.Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, userId);
            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                java.sql.ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newVoyageurId = generatedKeys.getInt(1);
                    System.out.println("✅ Created new traveler profile with ID: " + newVoyageurId);
                    return newVoyageurId;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting/creating traveler ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
