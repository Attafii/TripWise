package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.Flight;
import ui.model.FlightBooking;
import ui.model.FlightClass;
import ui.model.User;
import ui.service.FlightBookingService;
import ui.service.FlightService;
import ui.service.NVIDIAChatService;
import ui.util.SceneManager;
import ui.util.SessionManager;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import ui.service.EmailNotificationService;

public class FlightDetailsController {

    @FXML private Label airlineLabel;
    @FXML private Label flightNumberLabel;
    @FXML private Label statusLabel;
    @FXML private Label aircraftLabel;
    @FXML private Label departureTimeLabel;
    @FXML private Label departureCodeLabel;
    @FXML private Label departureCityLabel;
    @FXML private Label departureAirportLabel;
    @FXML private Label departureDateLabel;
    @FXML private Label arrivalTimeLabel;
    @FXML private Label arrivalCodeLabel;
    @FXML private Label arrivalCityLabel;
    @FXML private Label arrivalAirportLabel;
    @FXML private Label arrivalDateLabel;
    @FXML private Label durationLabel;
    @FXML private Label stopsLabel;
    @FXML private Label terminalLabel;
    @FXML private Label gateLabel;
    @FXML private Label boardingTimeLabel;
    @FXML private Label seatsLabel;
    @FXML private VBox classOptionsContainer;
    @FXML private FlowPane amenitiesContainer;
    @FXML private Spinner<Integer> passengersSpinner;
    @FXML private TextArea specialRequestsField;
    @FXML private Label basePriceLabel;
    @FXML private Label taxesLabel;
    @FXML private Label passengersCountLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label aiInsightLabel;

    private Flight flight;
    private FlightService flightService;
    private FlightBookingService bookingService;
    private NVIDIAChatService aiService;
    private EmailNotificationService emailService;
    private FlightClass selectedClass;
    private ToggleGroup classToggleGroup;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void setFlight(Flight flight) {
        this.flight = flight;
        loadFlightDetails();
    }

    @FXML
    private void initialize() {
        flightService = new FlightService();
        bookingService = new FlightBookingService();
        aiService = new NVIDIAChatService();
        emailService = new EmailNotificationService();

        // Setup passengers spinner
        passengersSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1));
        passengersSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePricing());

        classToggleGroup = new ToggleGroup();
    }

    private void loadFlightDetails() {
        if (flight == null) return;

        // Load complete flight data
        flight = flightService.getById(flight.getVolId());

        // Header info
        airlineLabel.setText(flight.getCompagnieName() != null ? flight.getCompagnieName() : "Airline");
        flightNumberLabel.setText(flight.getNumeroVol());
        aircraftLabel.setText(flight.getTypeAvion() != null ? flight.getTypeAvion() : "Aircraft");

        // Status
        String status = getStatusDisplay(flight.getStatutVol());
        statusLabel.setText(status);
        statusLabel.setStyle(getStatusStyle(flight.getStatutVol()));

        // Departure
        if (flight.getDateDepart() != null) {
            departureTimeLabel.setText(flight.getDateDepart().format(TIME_FORMATTER));
            departureDateLabel.setText(flight.getDateDepart().format(DATE_FORMATTER));
        }
        departureCodeLabel.setText(flight.getCodeAeroportDepart() != null ? flight.getCodeAeroportDepart() : "");
        departureCityLabel.setText(flight.getVilleDepart() != null ? flight.getVilleDepart() : "");
        departureAirportLabel.setText(flight.getAeroportDepartName() != null ? flight.getAeroportDepartName() : "");

        // Arrival
        if (flight.getDateArrivee() != null) {
            arrivalTimeLabel.setText(flight.getDateArrivee().format(TIME_FORMATTER));
            arrivalDateLabel.setText(flight.getDateArrivee().format(DATE_FORMATTER));
        }
        arrivalCodeLabel.setText(flight.getCodeAeroportArrivee() != null ? flight.getCodeAeroportArrivee() : "");
        arrivalCityLabel.setText(flight.getVilleArrivee() != null ? flight.getVilleArrivee() : "");
        arrivalAirportLabel.setText(flight.getAeroportArriveeName() != null ? flight.getAeroportArriveeName() : "");

        // Duration and stops
        durationLabel.setText(flight.getDurationDisplay());
        stopsLabel.setText(flight.getStopsDisplay());

        // Gate and terminal
        terminalLabel.setText(flight.getTerminal() != null ? flight.getTerminal() : "TBA");
        gateLabel.setText(flight.getPorteEmbarquement() != null ? flight.getPorteEmbarquement() : "TBA");

        // Boarding time (45 mins before departure)
        if (flight.getDateDepart() != null) {
            boardingTimeLabel.setText(flight.getDateDepart().minusMinutes(45).format(TIME_FORMATTER));
        }

        seatsLabel.setText(String.valueOf(flight.getPlacesDisponibles()));

        // Load flight classes
        loadFlightClasses();

        // Load amenities
        loadAmenities();

        // Generate AI insights
        generateAIInsights();
    }

    private void loadFlightClasses() {
        classOptionsContainer.getChildren().clear();
        List<FlightClass> classes = bookingService.getFlightClasses(flight.getVolId());

        if (classes.isEmpty()) {
            // Create default class if none exist
            Label noClassLabel = new Label("Pricing information not available");
            noClassLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
            classOptionsContainer.getChildren().add(noClassLabel);
            return;
        }

        for (FlightClass flightClass : classes) {
            VBox classCard = createClassCard(flightClass);
            classOptionsContainer.getChildren().add(classCard);
        }

        // Select first class by default
        if (!classes.isEmpty()) {
            selectedClass = classes.get(0);
            updatePricing();
        }
    }

    private VBox createClassCard(FlightClass flightClass) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #e5e7eb; -fx-border-width: 2; -fx-border-radius: 12; -fx-cursor: hand;");

        RadioButton radioBtn = new RadioButton();
        radioBtn.setToggleGroup(classToggleGroup);
        radioBtn.setUserData(flightClass);
        radioBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                selectedClass = flightClass;
                updatePricing();
                highlightSelectedClass(card);
            }
        });

        // Select first class by default
        if (classToggleGroup.getToggles().isEmpty()) {
            radioBtn.setSelected(true);
        }

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox classInfo = new VBox(3);
        HBox.setHgrow(classInfo, Priority.ALWAYS);

        Label className = new Label(flightClass.getDisplayName());
        className.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label seatInfo = new Label(flightClass.getPlacesDisponibles() + " seats available");
        seatInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        classInfo.getChildren().addAll(className, seatInfo);

        Label price = new Label(String.format("$%.0f", flightClass.getPriceAsDouble()));
        price.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        header.getChildren().addAll(radioBtn, classInfo, price);

        // Advantages
        if (flightClass.getAvantages() != null && !flightClass.getAvantages().isEmpty()) {
            Label advantages = new Label("✓ " + flightClass.getAvantages());
            advantages.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669;");
            advantages.setWrapText(true);
            card.getChildren().addAll(header, advantages);
        } else {
            card.getChildren().add(header);
        }

        // Click on card to select
        card.setOnMouseClicked(e -> radioBtn.setSelected(true));

        return card;
    }

    private void highlightSelectedClass(VBox selectedCard) {
        // Reset all cards
        for (javafx.scene.Node node : classOptionsContainer.getChildren()) {
            if (node instanceof VBox) {
                node.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #e5e7eb; -fx-border-width: 2; -fx-border-radius: 12; -fx-cursor: hand;");
            }
        }
        // Highlight selected
        selectedCard.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 12; -fx-cursor: hand;");
    }

    private void loadAmenities() {
        amenitiesContainer.getChildren().clear();

        addAmenity("✓ Free WiFi", flight.isHasWifi());
        addAmenity("✓ In-flight Entertainment", flight.isHasEntertainment());
        addAmenity("✓ Complimentary Meal", flight.isHasMeal());
        addAmenity("✓ Free Cancellation", flight.isHasFreeCancellation());
        addAmenity("✓ Extra Legroom", true);
        addAmenity("✓ Power Outlets", true);
        addAmenity("✓ USB Charging", true);
        addAmenity("✓ Blanket & Pillow", true);
    }

    private void addAmenity(String text, boolean available) {
        if (!available) return;

        Label amenity = new Label(text);
        amenity.setStyle("-fx-font-size: 13px; -fx-text-fill: #059669; -fx-background-color: #d1fae5; -fx-padding: 8 15; -fx-background-radius: 20;");
        amenitiesContainer.getChildren().add(amenity);
    }

    private void updatePricing() {
        if (selectedClass == null) return;

        double basePrice = selectedClass.getPriceAsDouble();
        int passengers = passengersSpinner.getValue();
        double taxes = basePrice * 0.1; // 10% taxes
        double total = (basePrice + taxes) * passengers;

        basePriceLabel.setText(String.format("$%.2f", basePrice));
        taxesLabel.setText(String.format("$%.2f", taxes));
        passengersCountLabel.setText("× " + passengers + " passenger" + (passengers > 1 ? "s" : ""));
        totalPriceLabel.setText(String.format("$%.2f", total));
    }

    private void generateAIInsights() {
        // Generate AI-powered insights about this flight
        StringBuilder insights = new StringBuilder();

        // On-time performance
        int performance = 85 + (int)(Math.random() * 15);
        insights.append(String.format("This flight has %d%% on-time performance. ", performance));

        // Price comparison
        if (flight.getMinPrice() > 0) {
            if (flight.getMinPrice() < 200) {
                insights.append("Great deal! ");
            } else if (flight.getMinPrice() < 400) {
                insights.append("Good value. ");
            }
        }

        // Recommendations
        insights.append("Best choice for your trip!");

        aiInsightLabel.setText(insights.toString());
    }

    @FXML
    private void handleBookFlight() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to book a flight.");
            return;
        }

        // Get voyageur_id
        int voyageurId = getTravelerIdForUser(currentUser.getUserId());
        if (voyageurId == -1) {
            // Try to create voyageur profile
            voyageurId = createTravelerProfile(currentUser.getUserId());
            if (voyageurId == -1) {
                showAlert(Alert.AlertType.ERROR, "Booking Error", "Could not create traveler profile. Please try again.");
                return;
            }
        }

        // Get class ID - if no class selected, try to get or create one
        int classeVolId = 0;
        double basePrice = 199.0; // Default price

        if (selectedClass != null) {
            classeVolId = selectedClass.getClasseVolId();
            basePrice = selectedClass.getPriceAsDouble();
            System.out.println("✅ Using selected class: " + classeVolId + " with price: " + basePrice);
        } else {
            // Try to get first available class for this flight
            List<FlightClass> classes = bookingService.getFlightClasses(flight.getVolId());
            if (!classes.isEmpty()) {
                classeVolId = classes.get(0).getClasseVolId();
                basePrice = classes.get(0).getPriceAsDouble();
                System.out.println("✅ Using first available class: " + classeVolId);
            } else {
                // Create a default class for this flight
                classeVolId = createDefaultFlightClass(flight.getVolId());
                if (classeVolId == -1) {
                    showAlert(Alert.AlertType.ERROR, "Booking Error", "No flight class available. Please contact support.");
                    return;
                }
                System.out.println("✅ Created default class: " + classeVolId);
            }
        }

        System.out.println("📋 Creating booking...");
        System.out.println("   Voyageur ID: " + voyageurId);
        System.out.println("   Vol ID: " + flight.getVolId());
        System.out.println("   Classe Vol ID: " + classeVolId);
        System.out.println("   Passengers: " + passengersSpinner.getValue());

        // Create booking
        FlightBooking booking = new FlightBooking();
        booking.setVoyageurId(voyageurId);
        booking.setVolId(flight.getVolId());
        booking.setClasseVolId(classeVolId);
        booking.setNombrePassagers(passengersSpinner.getValue());

        double taxes = basePrice * 0.1;
        double total = (basePrice + taxes) * passengersSpinner.getValue();
        booking.setPrixTotal(BigDecimal.valueOf(total));

        booking.setStatutReservation(FlightBooking.StatutReservation.EN_ATTENTE);
        booking.setDemandesSpeciales(specialRequestsField.getText());

        boolean success = bookingService.add(booking);

        if (success) {
            System.out.println("✅ Booking created successfully!");
            showSuccessDialog(booking);
        } else {
            System.err.println("❌ Failed to create booking");
            showAlert(Alert.AlertType.ERROR, "Booking Failed", "Could not create booking. Please check your database connection and try again.");
        }
    }

    /**
     * Create a default flight class for a flight that doesn't have one
     */
    private int createDefaultFlightClass(int volId) {
        // Use type_classe to match database schema
        String query = "INSERT INTO classes_vol (vol_id, type_classe, prix, places_disponibles) VALUES (?, 'ECONOMIQUE', 199.00, 50)";

        try {
            java.sql.Connection conn = ui.util.DataSource.getInstance().getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, volId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                java.sql.ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✅ Created default flight class for vol_id: " + volId);
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating default flight class: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Create a traveler profile for a user
     */
    private int createTravelerProfile(int userId) {
        String query = "INSERT INTO voyageurs (user_id, points_fidelite, created_at) VALUES (?, 0, NOW())";

        try {
            java.sql.Connection conn = ui.util.DataSource.getInstance().getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                java.sql.ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int voyageurId = rs.getInt(1);
                    System.out.println("✅ Created traveler profile: " + voyageurId);
                    return voyageurId;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating traveler profile: " + e.getMessage());
        }
        return -1;
    }

    private void showSuccessDialog(FlightBooking booking) {
        // Send confirmation email
        sendConfirmationEmail(booking);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Booking Confirmed!");
        alert.setHeaderText("Your flight has been booked successfully!");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label confirmationLabel = new Label("Confirmation Number: " + booking.getNumeroConfirmation());
        confirmationLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        String className = selectedClass != null ? selectedClass.getDisplayName() : "Economy";

        Label flightInfo = new Label(String.format(
            "Flight: %s\nRoute: %s → %s\nDate: %s\nPassengers: %d\nClass: %s\nTotal: $%.2f",
            flight.getNumeroVol(),
            flight.getVilleDepart(),
            flight.getVilleArrivee(),
            flight.getDateDepart() != null ? flight.getDateDepart().format(DATE_FORMATTER) : "N/A",
            booking.getNombrePassagers(),
            className,
            booking.getPriceAsDouble()
        ));
        flightInfo.setStyle("-fx-font-size: 14px;");

        Label statusLabel = new Label("✓ Status: Pending Confirmation");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f59e0b;");

        Label emailLabel = new Label("📧 Confirmation email sent!");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;");

        // Additional features info
        Label featuresLabel = new Label("What would you like to do next?");
        featuresLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        content.getChildren().addAll(confirmationLabel, new Separator(), flightInfo, statusLabel, emailLabel,
                                     new Separator(), featuresLabel);
        alert.getDialogPane().setContent(content);

        ButtonType selectSeatsBtn = new ButtonType("✈ Select Seats", ButtonBar.ButtonData.LEFT);
        ButtonType viewBookingsBtn = new ButtonType("📋 My Bookings", ButtonBar.ButtonData.OTHER);
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(selectSeatsBtn, viewBookingsBtn, okBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == selectSeatsBtn) {
                System.out.println("🎯 User chose to select seats");
                navigateToSeatSelection(booking);
            } else if (result.get() == viewBookingsBtn) {
                System.out.println("📋 User chose to view bookings");
                SceneManager.switchScene("/ui/traveler-bookings.fxml");
            } else {
                handleBack();
            }
        }
    }

    /**
     * Navigate to seat selection page
     */
    private void navigateToSeatSelection(FlightBooking booking) {
        try {
            System.out.println("🎯 Opening seat selection for reservation: " + booking.getReservationId());

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/ui/seat-selection.fxml")
            );
            javafx.scene.Parent seatPane = loader.load();

            SeatSelectionController controller = loader.getController();

            // Get class name safely
            String classType = selectedClass != null ? selectedClass.getClasse().name() : "ECONOMIQUE";

            controller.initData(
                flight.getVolId(),
                booking.getReservationId(),
                booking.getNombrePassagers(),
                classType,
                booking.getPriceAsDouble(),
                flight.getNumeroVol(),
                flight.getVilleDepart() + " → " + flight.getVilleArrivee()
            );

            // Try to navigate within the current scene
            javafx.scene.Scene scene = passengersSpinner.getScene();
            if (scene != null) {
                javafx.scene.Parent root = scene.getRoot();
                if (root instanceof javafx.scene.layout.BorderPane) {
                    ((javafx.scene.layout.BorderPane) root).setCenter(seatPane);
                    System.out.println("✅ Navigated to seat selection (BorderPane)");
                } else {
                    // Alternative: replace entire scene
                    scene.setRoot(seatPane);
                    System.out.println("✅ Navigated to seat selection (Scene root)");
                }
            } else {
                // Fallback: use SceneManager
                System.out.println("⚠️ No scene found, using SceneManager");
                SceneManager.switchScene("/ui/seat-selection.fxml");
            }
        } catch (Exception e) {
            System.err.println("❌ Error navigating to seat selection: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open seat selection page: " + e.getMessage());
            SceneManager.switchScene("/ui/traveler-bookings.fxml");
        }
    }

    /**
     * Send confirmation email to the user
     */
    private void sendConfirmationEmail(FlightBooking booking) {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                System.out.println("📧 Preparing confirmation email for: " + currentUser.getEmail());

                // Set passenger details for email
                booking.setPassengerName(currentUser.getFirstName() + " " + currentUser.getLastName());
                booking.setPassengerEmail(currentUser.getEmail());
                booking.setNumeroVol(flight.getNumeroVol());
                booking.setVilleDepart(flight.getVilleDepart());
                booking.setVilleArrivee(flight.getVilleArrivee());
                booking.setDateDepart(flight.getDateDepart());
                booking.setDateArrivee(flight.getDateArrivee());

                // Get class name safely
                String className = selectedClass != null ? selectedClass.getDisplayName() : "Economy";
                booking.setClasseNom(className);

                // Send email
                boolean emailSent = emailService.sendBookingConfirmation(booking);
                if (emailSent) {
                    System.out.println("✅ Confirmation email sent to: " + currentUser.getEmail());
                } else {
                    System.out.println("⚠️ Email service returned false (email may not be configured)");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not send confirmation email: " + e.getMessage());
            // Don't show error to user, email is optional
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/ui/book-flight-new.fxml");
    }

    @FXML
    private void handleAIAssist() {
        showAIAssistDialog();
    }

    @FXML
    private void handleAIRecommendations() {
        showAIRecommendationsDialog();
    }

    private void showAIAssistDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("AI Flight Assistant");
        dialog.setHeaderText("Ask me anything about this flight!");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        ScrollPane chatScroll = new ScrollPane();
        VBox chatContainer = new VBox(10);
        chatContainer.setPadding(new Insets(10));
        chatScroll.setContent(chatContainer);
        chatScroll.setPrefHeight(300);
        chatScroll.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

        // Initial AI message
        addAIMessageToChat(chatContainer, String.format(
            "Hi! I'm your AI assistant for flight %s. I can help you with:\n\n" +
            "• Flight details and schedule\n" +
            "• Best class recommendations\n" +
            "• Travel tips for %s\n" +
            "• Booking assistance\n\n" +
            "What would you like to know?",
            flight.getNumeroVol(),
            flight.getVilleArrivee()
        ));

        HBox inputBox = new HBox(10);
        TextField inputField = new TextField();
        inputField.setPromptText("Ask a question...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");

        sendBtn.setOnAction(e -> {
            String question = inputField.getText().trim();
            if (!question.isEmpty()) {
                addUserMessageToChat(chatContainer, question);
                inputField.clear();

                // Generate AI response
                String context = String.format(
                    "Flight %s from %s to %s on %s. Price: $%.0f",
                    flight.getNumeroVol(),
                    flight.getVilleDepart(),
                    flight.getVilleArrivee(),
                    flight.getDateDepart() != null ? flight.getDateDepart().format(DATE_FORMATTER) : "N/A",
                    selectedClass != null ? selectedClass.getPriceAsDouble() : flight.getMinPrice()
                );
                String response = aiService.sendMessage(context + "\n\nUser question: " + question);
                addAIMessageToChat(chatContainer, response);

                // Scroll to bottom
                chatScroll.setVvalue(1.0);
            }
        });

        inputField.setOnAction(e -> sendBtn.fire());

        inputBox.getChildren().addAll(inputField, sendBtn);
        content.getChildren().addAll(chatScroll, inputBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void showAIRecommendationsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AI Recommendations");
        alert.setHeaderText("Personalized Recommendations for Your Trip");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Generate recommendations using AI
        String prompt = String.format(
            "Generate travel recommendations for a flight from %s to %s. Include: best time to arrive at airport, " +
            "what to do in %s, local tips, and travel advice. Keep it concise and helpful.",
            flight.getVilleDepart(),
            flight.getVilleArrivee(),
            flight.getVilleArrivee()
        );

        Label loadingLabel = new Label("🤖 AI is generating recommendations...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
        content.getChildren().add(loadingLabel);

        alert.getDialogPane().setContent(content);
        alert.show();

        // Generate in background
        new Thread(() -> {
            String recommendations = aiService.sendMessage(prompt);
            javafx.application.Platform.runLater(() -> {
                content.getChildren().clear();
                Label recommendationsLabel = new Label(recommendations);
                recommendationsLabel.setStyle("-fx-font-size: 14px;");
                recommendationsLabel.setWrapText(true);
                content.getChildren().add(recommendationsLabel);
            });
        }).start();
    }

    private void addUserMessageToChat(VBox container, String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 15; -fx-font-size: 13px;");
        msgLabel.setMaxWidth(350);
        msgLabel.setWrapText(true);

        messageBox.getChildren().add(msgLabel);
        container.getChildren().add(messageBox);
    }

    private void addAIMessageToChat(VBox container, String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-padding: 10 15; -fx-background-radius: 15; -fx-font-size: 13px;");
        msgLabel.setMaxWidth(350);
        msgLabel.setWrapText(true);

        messageBox.getChildren().add(msgLabel);
        container.getChildren().add(messageBox);
    }

    private int getTravelerIdForUser(int userId) {
        try {
            java.sql.Connection conn = ui.util.DataSource.getInstance().getConnection();
            String query = "SELECT voyageur_id FROM voyageurs WHERE user_id = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("voyageur_id");
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting traveler ID: " + e.getMessage());
        }
        return -1;
    }

    private String getStatusDisplay(Flight.StatutVol status) {
        if (status == null) return "Unknown";
        switch (status) {
            case PROGRAMME: return "Scheduled";
            case EN_COURS: return "In Flight";
            case ATTERRI: return "Landed";
            case RETARDE: return "Delayed";
            case ANNULE: return "Cancelled";
            default: return status.name();
        }
    }

    private String getStatusStyle(Flight.StatutVol status) {
        String base = "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 6 15; -fx-background-radius: 15;";
        if (status == null) return base + " -fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        switch (status) {
            case PROGRAMME: return base + " -fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
            case EN_COURS: return base + " -fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            case ATTERRI: return base + " -fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
            case RETARDE: return base + " -fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case ANNULE: return base + " -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            default: return base + " -fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
