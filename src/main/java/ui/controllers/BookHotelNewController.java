package ui.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.Hotel;
import ui.model.HotelBooking;
import ui.service.HotelBookingService;
import ui.service.HotelService;
import ui.util.SessionManager;

import java.time.LocalDate;
import java.util.List;

/**
 * BookHotelNewController - Modern hotel booking interface
 * Matches the design from pasted_image_2
 */
public class BookHotelNewController {

    @FXML
    private TextField destinationField;

    @FXML
    private DatePicker checkInPicker;

    @FXML
    private DatePicker checkOutPicker;

    @FXML
    private Spinner<Integer> guestsSpinner;

    @FXML
    private Spinner<Integer> roomsSpinner;

    @FXML
    private Button searchButton;

    @FXML
    private VBox hotelsContainer;

    @FXML
    private Label resultCountLabel;

    private HotelService hotelService;
    private HotelBookingService bookingService;

    @FXML
    private void initialize() {
        hotelService = new HotelService();
        bookingService = new HotelBookingService();

        // Setup spinners
        guestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
        roomsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));

        // Set default dates
        checkInPicker.setValue(LocalDate.now().plusDays(1));
        checkOutPicker.setValue(LocalDate.now().plusDays(3));

        // Load hotels on startup
        loadHotels();
    }

    @FXML
    private void handleSearch() {
        // ...existing code...
    }

    @FXML
    private void handleBackToBookings() {
        try {
            ui.util.SceneManager.switchScene("/ui/dashboard.fxml");
            // Note: Dashboard will need to auto-navigate to bookings
            System.out.println("📋 Navigating back to Bookings");
        } catch (Exception e) {
            System.err.println("❌ Error navigating to Bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadHotels() {
        System.out.println("📋 Loading available hotels...");
        List<Hotel> hotels = hotelService.getHotelsWithPricing();
        displayHotels(hotels);
    }

    private void displayHotels(List<Hotel> hotels) {
        hotelsContainer.getChildren().clear();
        resultCountLabel.setText("Available Hotels (" + hotels.size() + ")");

        if (hotels.isEmpty()) {
            Label noResults = new Label("No hotels found. Try a different search.");
            noResults.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 40px;");
            hotelsContainer.getChildren().add(noResults);
            return;
        }

        for (Hotel hotel : hotels) {
            HBox hotelCard = createHotelCard(hotel);
            hotelsContainer.getChildren().add(hotelCard);
        }
    }

    private HBox createHotelCard(Hotel hotel) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                     "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefHeight(180);

        // Hotel Image Placeholder
        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setPrefSize(220, 140);
        imagePlaceholder.setStyle("-fx-background-color: linear-gradient(to bottom right, #3b82f6, #8b5cf6); " +
                                 "-fx-background-radius: 8;");

        Label imageIcon = new Label("🏨");
        imageIcon.setStyle("-fx-font-size: 48px;");
        imagePlaceholder.getChildren().add(imageIcon);
        imagePlaceholder.setAlignment(javafx.geometry.Pos.CENTER);

        // Hotel Details
        VBox details = new VBox(8);
        HBox.setHgrow(details, Priority.ALWAYS);

        // Hotel Name (clickable)
        Label nameLabel = new Label(hotel.getNomHotel());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-cursor: hand; -fx-underline: false;");

        // Make hotel name clickable
        nameLabel.setOnMouseClicked(e -> {
            try {
                // Set current hotel for detail page
                HotelDetailController.setCurrentHotel(hotel);
                // Navigate to hotel detail page
                ui.util.SceneManager.switchScene("/ui/hotel-detail.fxml");
            } catch (Exception ex) {
                System.err.println("❌ Error navigating to hotel details: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Hover effect for hotel name
        nameLabel.setOnMouseEntered(e ->
            nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3b82f6; -fx-cursor: hand; -fx-underline: true;")
        );
        nameLabel.setOnMouseExited(e ->
            nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-cursor: hand; -fx-underline: false;")
        );

        // Location
        HBox location = new HBox(5);
        location.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        Label locationText = new Label(hotel.getVille() + ", " + hotel.getPays());
        locationText.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        location.getChildren().addAll(locationIcon, locationText);

        // Rating and Room Type
        HBox meta = new HBox(15);
        meta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Star Rating
        HBox rating = new HBox(3);
        rating.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        double stars = hotel.getEtoiles() != null ? hotel.getEtoiles().doubleValue() : 0;
        Label ratingLabel = new Label(String.format("⭐ %.1f", stars));
        ratingLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        rating.getChildren().add(ratingLabel);

        // Review count (simulated)
        Label reviewCount = new Label("(1250 reviews)");
        reviewCount.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

        meta.getChildren().addAll(rating, reviewCount);

        // Amenities
        HBox amenities = new HBox(15);
        String[] amenityList = hotel.getEquipements() != null ?
            hotel.getEquipements().split(",") : new String[]{"WiFi", "Pool", "Spa", "Restaurant"};

        for (int i = 0; i < Math.min(4, amenityList.length); i++) {
            Label amenity = new Label(getAmenityIcon(amenityList[i].trim()) + " " + amenityList[i].trim());
            amenity.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            amenities.getChildren().add(amenity);
        }

        // Description
        Label description = new Label(hotel.getDescription() != null ?
            hotel.getDescription() : "Luxury hotel with stunning city views");
        description.setWrapText(true);
        description.setMaxWidth(400);
        description.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13px;");

        details.getChildren().addAll(nameLabel, location, meta, amenities, description);

        // Right Side - Price and Booking
        VBox priceSection = new VBox(12);
        priceSection.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        priceSection.setMinWidth(180);

        // Price
        VBox priceBox = new VBox(3);
        priceBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);

        Label priceLabel = new Label("Starting from");
        priceLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        Label price = new Label("$" + String.format("%.0f", hotel.getPricePerNight()));
        price.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        Label perNight = new Label("/ night");
        perNight.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

        priceBox.getChildren().addAll(priceLabel, price, perNight);

        // Book Now Button
        Button bookButton = new Button("Book Now");
        bookButton.setPrefWidth(150);
        bookButton.setPrefHeight(42);
        bookButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                          "-fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; " +
                          "-fx-cursor: hand;");
        bookButton.setOnMouseEntered(e ->
            bookButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                              "-fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;"));
        bookButton.setOnMouseExited(e ->
            bookButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                              "-fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;"));
        bookButton.setOnAction(e -> handleBookNow(hotel));

        // View All Bookings link
        Hyperlink viewBookingsLink = new Hyperlink("View All Bookings");
        viewBookingsLink.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px;");
        viewBookingsLink.setOnAction(e -> showAllBookings());

        priceSection.getChildren().addAll(priceBox, bookButton, viewBookingsLink);

        card.getChildren().addAll(imagePlaceholder, details, priceSection);
        VBox.setMargin(card, new Insets(0, 0, 15, 0));

        return card;
    }

    private void handleBookNow(Hotel hotel) {
        LocalDate checkin = checkInPicker.getValue();
        LocalDate checkout = checkOutPicker.getValue();

        if (checkin == null || checkout == null) {
            showError("Please select check-in and check-out dates.");
            return;
        }

        if (checkin.isAfter(checkout) || checkin.equals(checkout)) {
            showError("Check-out date must be after check-in date.");
            return;
        }

        // Show booking confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("Book " + hotel.getNomHotel());

        int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(checkin, checkout);
        double totalPrice = hotel.getPricePerNight() * nights * roomsSpinner.getValue();

        String details = String.format(
            "Hotel: %s\n" +
            "Location: %s, %s\n" +
            "Check-in: %s\n" +
            "Check-out: %s\n" +
            "Nights: %d\n" +
            "Guests: %d\n" +
            "Rooms: %d\n\n" +
            "Total Price: $%.2f",
            hotel.getNomHotel(),
            hotel.getVille(), hotel.getPays(),
            checkin,
            checkout,
            nights,
            guestsSpinner.getValue(),
            roomsSpinner.getValue(),
            totalPrice
        );

        confirm.setContentText(details);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                createBooking(hotel, checkin, checkout, nights, totalPrice);
            }
        });
    }

    private void createBooking(Hotel hotel, LocalDate checkin, LocalDate checkout, int nights, double totalPrice) {
        // Get current user (need to be logged in as VOYAGEUR)
        var currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Please login to make a booking.");
            return;
        }

        // Create booking (simplified - assuming first room of hotel)
        HotelBooking booking = new HotelBooking();
        booking.setVoyageurId(1); // TODO: Get actual voyageur_id from session
        booking.setHotelId(hotel.getHotelId());
        booking.setChambreId(1); // TODO: Room selection
        booking.setDateCheckin(checkin);
        booking.setDateCheckout(checkout);
        booking.setNombreNuits(nights);
        booking.setNombreAdultes(guestsSpinner.getValue());
        booking.setNombreEnfants(0);
        booking.setPrixTotal(totalPrice);
        booking.setStatutReservation(HotelBooking.StatutReservation.EN_ATTENTE);

        if (bookingService.add(booking)) {
            showSuccess("Booking created successfully!\nConfirmation: " + booking.getNumeroConfirmation());
        } else {
            showError("Failed to create booking. Please try again.");
        }
    }

    private void showAllBookings() {
        try {
            ui.util.SceneManager.switchScene("/ui/all-bookings.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error navigating to bookings: " + e.getMessage());
        }
    }

    private String getAmenityIcon(String amenity) {
        String lower = amenity.toLowerCase();
        if (lower.contains("wifi")) return "📶";
        if (lower.contains("pool")) return "🏊";
        if (lower.contains("spa")) return "💆";
        if (lower.contains("restaurant")) return "🍽️";
        if (lower.contains("gym")) return "💪";
        if (lower.contains("parking")) return "🅿️";
        if (lower.contains("bar")) return "🍸";
        if (lower.contains("beach")) return "🏖️";
        return "✓";
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
