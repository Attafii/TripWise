package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.User;
import ui.service.NVIDIAChatService;
import ui.service.AIQueryGeneratorService;
import ui.util.SceneManager;
import ui.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AIAgentController {

    @FXML
    private VBox chatMessagesContainer;

    @FXML
    private TextField aiInputField;

    @FXML
    private VBox employeeActionsSection;
    
    @FXML
    private HBox quickActionsBox;

    private NVIDIAChatService chatService;
    private AIQueryGeneratorService aiQueryService;
    private Connection dbConnection;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private List<ChatMessage> conversationHistory;
    private static final int MAX_HISTORY = 10;

    @FXML
    private void initialize() {
        conversationHistory = new ArrayList<>();
        
        try {
            chatService = new NVIDIAChatService();
            System.out.println("✅ NVIDIA Mistral AI chatbot initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize NVIDIA chatbot: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            aiQueryService = new AIQueryGeneratorService();
            System.out.println("✅ AI Query Generator initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize AI Query Generator: " + e.getMessage());
        }

        try {
            dbConnection = ui.util.DataSource.getInstance().getConnection();
            System.out.println("✅ Database connected successfully");
        } catch (Exception e) {
            System.err.println("❌ Error connecting to database: " + e.getMessage());
        }

        // Check if user is employee
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            boolean isEmployee = currentUser.getUserType() == User.UserType.EMPLOYE ||
                                currentUser.getUserType() == User.UserType.ADMIN ||
                                currentUser.getUserType() == User.UserType.RESPONSABLE;
            employeeActionsSection.setVisible(isEmployee);
            employeeActionsSection.setManaged(isEmployee);
        }
        
        // Add quick action buttons
        addQuickActionButtons();

        // Add welcome message
        addAIMessage("🤖 Hello! I'm your AI Travel Assistant powered by **NVIDIA Mistral AI** with **Natural Language Database Access**.\n\n" +
                    "💡 **NEW FEATURES:**\n" +
                    "   • Ask questions in plain English!\n" +
                    "   • I'll automatically query the database\n" +
                    "   • Context-aware conversations\n\n" +
                    "📊 **EXAMPLES:**\n" +
                    "   • \"Show me all confirmed bookings\"\n" +
                    "   • \"Find flights to New York\"\n" +
                    "   • \"Which hotels are in Paris?\"\n" +
                    "   • \"How many users are registered?\"\n" +
                    "   • \"Show pending reservations\"\n\n" +
                    "🚀 **TRY IT:** Use the quick action buttons below or type your question!");
    }
    
    private void addQuickActionButtons() {
        if (quickActionsBox == null) return;
        
        quickActionsBox.getChildren().clear();
        
        String[] actions = {
            "📋 All Bookings",
            "✈️ Find Flights", 
            "🏨 Browse Hotels",
            "👥 User Stats",
            "📊 Analytics"
        };
        
        for (String action : actions) {
            Button btn = new Button(action);
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #3b82f6; -fx-border-color: #3b82f6; " +
                        "-fx-border-width: 1.5; -fx-background-radius: 20; -fx-padding: 8 16; -fx-cursor: hand; " +
                        "-fx-font-size: 12px; -fx-font-weight: 600;");
            btn.setOnAction(e -> handleQuickAction(action));
            quickActionsBox.getChildren().add(btn);
        }
    }
    
    private void handleQuickAction(String action) {
        switch (action) {
            case "📋 All Bookings":
                aiInputField.setText("Show me all bookings");
                break;
            case "✈️ Find Flights":
                aiInputField.setText("Show available flights");
                break;
            case "🏨 Browse Hotels":
                aiInputField.setText("List all hotels");
                break;
            case "👥 User Stats":
                aiInputField.setText("How many users are registered?");
                break;
            case "📊 Analytics":
                aiInputField.setText("Generate analytics report");
                break;
        }
        handleSendMessage();
    }

    @FXML
    private void handleSendMessage() {
        String message = aiInputField.getText().trim();
        if (message.isEmpty()) return;

        addUserMessage(message);
        conversationHistory.add(new ChatMessage("user", message));
        if (conversationHistory.size() > MAX_HISTORY) {
            conversationHistory.remove(0);
        }
        
        aiInputField.clear();

        // Show typing indicator
        Label typingLabel = new Label("AI is thinking...");
        typingLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px; -fx-font-style: italic; -fx-padding: 10;");
        chatMessagesContainer.getChildren().add(typingLabel);

        // Process AI command in background
        new Thread(() -> {
            String response = processAICommand(message);
            Platform.runLater(() -> {
                chatMessagesContainer.getChildren().remove(typingLabel);
                addAIMessage(response);
            });
        }).start();
    }

    private String processAICommand(String command) {
        String lowerCommand = command.toLowerCase();

        try {
            // ========== STEP 1: TRY AI QUERY GENERATION FIRST ==========
            if (aiQueryService != null) {
                System.out.println("🤖 Attempting AI query generation for: " + command);
                
                // Use the processNaturalLanguageQuery method which handles everything
                String aiResult = aiQueryService.processNaturalLanguageQuery(command);
                
                // If AI query succeeded (didn't return error), use it
                if (aiResult != null && !aiResult.startsWith("❌")) {
                    System.out.println("✅ AI query succeeded!");
                    conversationHistory.add(new ChatMessage("assistant", aiResult));
                    return aiResult;
                } else {
                    System.out.println("⚠️ AI query failed, falling back to pattern matching");
                }
            }
            
            // ========== STEP 2: FALLBACK TO PATTERN MATCHING ==========
            // ========== USER MANAGEMENT ==========
            if (lowerCommand.contains("user") || lowerCommand.contains("customer") || lowerCommand.contains("client")) {
                if (lowerCommand.contains("show") || lowerCommand.contains("view") || lowerCommand.contains("list") || lowerCommand.contains("all")) {
                    return showAllUsers();
                }
                if (lowerCommand.contains("search") || lowerCommand.contains("find")) {
                    return searchUsers(command);
                }
                if (lowerCommand.contains("count") || lowerCommand.contains("total") || lowerCommand.contains("how many")) {
                    return countUsers();
                }
            }

            // ========== HOTEL OPERATIONS ==========
            if (lowerCommand.contains("hotel")) {
                if (lowerCommand.contains("booking")) {
                    if (lowerCommand.contains("show") || lowerCommand.contains("view") || lowerCommand.contains("list")) {
                        return showHotelBookings();
                    }
                    if (lowerCommand.contains("pending")) {
                        return showPendingBookings();
                    }
                    if (lowerCommand.contains("cancel")) {
                        return executeCancelBooking(command);
                    }
                } else {
                    if (lowerCommand.contains("show") || lowerCommand.contains("view") || lowerCommand.contains("list") || lowerCommand.contains("all")) {
                        return showAllHotels();
                    }
                    if (lowerCommand.contains("search") || lowerCommand.contains("in ")) {
                        return searchHotels(command);
                    }
                }
            }

            // ========== FLIGHT OPERATIONS ==========
            if (lowerCommand.contains("flight")) {
                if (lowerCommand.contains("booking")) {
                    return showFlightBookings();
                } else {
                    if (lowerCommand.contains("show") || lowerCommand.contains("view") || lowerCommand.contains("list") || lowerCommand.contains("all")) {
                        return showAllFlights();
                    }
                    if (lowerCommand.contains("search") || lowerCommand.contains("from") || lowerCommand.contains("to")) {
                        return searchFlights(command);
                    }
                }
            }

            // ========== CAR RENTAL OPERATIONS ==========
            if (lowerCommand.contains("car") || lowerCommand.contains("vehicle") || lowerCommand.contains("rental")) {
                if (lowerCommand.contains("booking") || lowerCommand.contains("rental")) {
                    return showCarRentals();
                } else {
                    if (lowerCommand.contains("show") || lowerCommand.contains("view") || lowerCommand.contains("list") || lowerCommand.contains("all")) {
                        return showAllCars();
                    }
                    if (lowerCommand.contains("search") || lowerCommand.contains("in ") || lowerCommand.contains("available")) {
                        return searchCars(command);
                    }
                }
            }

            // ========== GENERAL BOOKING OPERATIONS ==========
            if (lowerCommand.contains("booking") && !lowerCommand.contains("hotel") &&
                !lowerCommand.contains("flight") && !lowerCommand.contains("car")) {
                if (lowerCommand.contains("create") || lowerCommand.contains("new") || lowerCommand.contains("make")) {
                    return "To create a booking, I need:\n" +
                           "1. Type (hotel/flight/car)\n" +
                           "2. Customer name or ID\n" +
                           "3. Specific details (dates, destination, etc.)\n\n" +
                           "Example: 'Create hotel booking for John Doe at Grand Hotel Paris from 2026-02-15 to 2026-02-18'";
                }
                if (lowerCommand.contains("cancel")) {
                    return executeCancelBooking(command);
                }
                if (lowerCommand.contains("pending") || lowerCommand.contains("waiting")) {
                    return showAllPendingBookings();
                }
                if (lowerCommand.contains("show") || lowerCommand.contains("view") || lowerCommand.contains("list") || lowerCommand.contains("all")) {
                    return showAllBookingsSummary();
                }
            }

            // ========== ANALYTICS & REPORTS ==========
            if (lowerCommand.contains("analytics") || lowerCommand.contains("report") ||
                lowerCommand.contains("statistics") || lowerCommand.contains("stats") ||
                lowerCommand.contains("summary")) {
                return generateComprehensiveAnalytics();
            }

            // ========== REVENUE & FINANCIAL ==========
            if (lowerCommand.contains("revenue") || lowerCommand.contains("income") ||
                lowerCommand.contains("earnings") || lowerCommand.contains("money")) {
                return calculateRevenue();
            }

            // Default: Use built-in AI responses
            return generateIntelligentResponse(command);

        } catch (Exception e) {
            return "❌ Error processing command: " + e.getMessage();
        }
    }

    private String generateIntelligentResponse(String command) {
        String lower = command.toLowerCase();

        // Greeting
        if (lower.contains("hello") || lower.contains("hi") || lower.matches("^(hey|sup|yo)\\b.*")) {
            return "Hello! I'm your AI Travel Assistant powered by Mistral AI. I can help you with hotel bookings, " +
                   "flights, car rentals, analytics, and managing reservations. What would you like to do?";
        }

        // Help
        if (lower.contains("help") || lower.contains("what can you")) {
            return "I can help you with:\n\n" +
                   "👥 USERS: 'Show all users', 'Search user John'\n" +
                   "🏨 HOTELS: 'Show all hotels', 'Search hotels in Paris'\n" +
                   "✈️ FLIGHTS: 'Show all flights', 'Search flights from Paris to New York'\n" +
                   "🚗 CARS: 'Show all cars', 'Search cars in Dubai'\n" +
                   "📋 BOOKINGS: 'Show all bookings', 'Show pending bookings', 'Cancel booking #123'\n" +
                   "📊 ANALYTICS: 'Generate analytics', 'Show revenue', 'Generate report'\n\n" +
                   "Try any of these commands, or ask me anything about travel!";
        }

        // Use NVIDIA Mistral chatbot for general questions
        try {
            if (chatService != null) {
                System.out.println("🤖 Using NVIDIA Mistral AI for: " + command);
                String aiResponse = chatService.sendMessage(command);
                return "🤖 AI Assistant (Powered by Mistral):\n\n" + aiResponse;
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling NVIDIA API: " + e.getMessage());
            e.printStackTrace();
        }

        // Default fallback
        return "I understand you're asking: \"" + command + "\"\n\n" +
               "I can help with:\n" +
               "👥 User Management: 'Show all users'\n" +
               "🏨 Hotels: 'Show all hotels'\n" +
               "✈️ Flights: 'Show all flights'\n" +
               "🚗 Cars: 'Show all cars'\n" +
               "📋 Bookings: 'Show all bookings'\n" +
               "📊 Analytics: 'Generate analytics'\n\n" +
               "Type 'help' for more examples!";
    }

    private String executeCancelBooking(String command) {
        try {
            // Extract booking ID from command
            String[] parts = command.split("#");
            if (parts.length > 1) {
                int bookingId = Integer.parseInt(parts[1].trim().split(" ")[0]);

                String query = "UPDATE reservations_hotel SET statut_reservation = 'ANNULEE' WHERE reservation_id = ?";
                PreparedStatement stmt = dbConnection.prepareStatement(query);
                stmt.setInt(1, bookingId);
                int updated = stmt.executeUpdate();

                if (updated > 0) {
                    return "✅ Successfully cancelled booking #" + bookingId;
                } else {
                    return "❌ Booking #" + bookingId + " not found";
                }
            }
            return "Please specify a booking ID. Example: 'Cancel booking #123'";
        } catch (Exception e) {
            return "❌ Error cancelling booking: " + e.getMessage();
        }
    }

    private String showPendingBookings() {
        try {
            String query = "SELECT r.reservation_id, CONCAT(u.first_name, ' ', u.last_name) as customer, " +
                          "h.nom_hotel, r.date_checkin, r.prix_total " +
                          "FROM reservations_hotel r " +
                          "JOIN voyageurs v ON r.voyageur_id = v.voyageur_id " +
                          "JOIN users u ON v.user_id = u.user_id " +
                          "JOIN hotels h ON r.hotel_id = h.hotel_id " +
                          "WHERE r.statut_reservation = 'EN_ATTENTE' " +
                          "LIMIT 10";

            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("📋 Pending Bookings:\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("🔸 Booking #%d\n", rs.getInt("reservation_id")));
                result.append(String.format("   Customer: %s\n", rs.getString("customer")));
                result.append(String.format("   Hotel: %s\n", rs.getString("nom_hotel")));
                result.append(String.format("   Check-in: %s\n", rs.getDate("date_checkin")));
                result.append(String.format("   Price: $%.2f\n\n", rs.getDouble("prix_total")));
            }

            if (count == 0) {
                return "✅ No pending bookings found!";
            }

            return result.toString() + "Total: " + count + " pending booking(s)";
        } catch (Exception e) {
            return "❌ Error fetching pending bookings: " + e.getMessage();
        }
    }

    private String showAllBookings() {
        try {
            String query = "SELECT COUNT(*) as total, " +
                          "SUM(CASE WHEN statut_reservation = 'EN_ATTENTE' THEN 1 ELSE 0 END) as pending, " +
                          "SUM(CASE WHEN statut_reservation = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmed, " +
                          "SUM(CASE WHEN statut_reservation = 'ANNULEE' THEN 1 ELSE 0 END) as cancelled " +
                          "FROM reservations_hotel";

            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return String.format("📊 Booking Summary:\n\n" +
                                   "📋 Total Bookings: %d\n" +
                                   "⏳ Pending: %d\n" +
                                   "✅ Confirmed: %d\n" +
                                   "❌ Cancelled: %d",
                                   rs.getInt("total"),
                                   rs.getInt("pending"),
                                   rs.getInt("confirmed"),
                                   rs.getInt("cancelled"));
            }

            return "No bookings found";
        } catch (Exception e) {
            return "❌ Error fetching bookings: " + e.getMessage();
        }
    }

    // ========== USER MANAGEMENT METHODS ==========

    private String showAllUsers() {
        try {
            String query = "SELECT user_id, first_name, last_name, email, phone_number FROM users LIMIT 20";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("👥 USER LIST:\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("👤 ID: %d\n", rs.getInt("user_id")));
                result.append(String.format("   Name: %s %s\n", rs.getString("first_name"), rs.getString("last_name")));
                result.append(String.format("   Email: %s\n", rs.getString("email")));
                String phone = rs.getString("phone_number");
                if (phone != null && !phone.isEmpty()) {
                    result.append(String.format("   Phone: %s\n\n", phone));
                } else {
                    result.append("   Phone: N/A\n\n");
                }
            }

            if (count == 0) {
                return "No users found in the database.";
            }

            return result.toString() + String.format("Total: %d user(s) shown", count);
        } catch (Exception e) {
            return "❌ Error fetching users: " + e.getMessage();
        }
    }

    private String searchUsers(String command) {
        try {
            // Extract search term (simple extraction)
            String searchTerm = command.toLowerCase()
                .replace("search", "")
                .replace("user", "")
                .replace("find", "")
                .replace("for", "")
                .trim();

            String query = "SELECT user_id, first_name, last_name, email, phone_number FROM users " +
                          "WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? LIMIT 10";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("🔍 SEARCH RESULTS for '" + searchTerm + "':\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("👤 ID: %d - %s %s\n",
                    rs.getInt("user_id"), rs.getString("first_name"), rs.getString("last_name")));
                result.append(String.format("   Email: %s | Phone: %s\n\n",
                    rs.getString("email"), rs.getString("phone_number")));
            }

            if (count == 0) {
                return "No users found matching '" + searchTerm + "'";
            }

            return result.toString() + String.format("Found %d user(s)", count);
        } catch (Exception e) {
            return "❌ Error searching users: " + e.getMessage();
        }
    }

    private String countUsers() {
        try {
            String query = "SELECT COUNT(*) as total FROM users";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return String.format("👥 Total Users in Database: %d", rs.getInt("total"));
            }
            return "No users found";
        } catch (Exception e) {
            return "❌ Error counting users: " + e.getMessage();
        }
    }

    // ========== HOTEL METHODS ==========

    private String showAllHotels() {
        try {
            String query = "SELECT hotel_id, nom_hotel, ville, etoiles FROM hotels LIMIT 15";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("🏨 HOTEL LIST:\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("🏨 %s\n", rs.getString("nom_hotel")));
                result.append(String.format("   📍 %s | ⭐ %.1f stars\n\n",
                    rs.getString("ville"), rs.getDouble("etoiles")));
            }

            if (count == 0) {
                return "No hotels found in the database.";
            }

            return result.toString() + String.format("Total: %d hotel(s)", count);
        } catch (Exception e) {
            return "��� Error fetching hotels: " + e.getMessage();
        }
    }

    private String searchHotels(String command) {
        try {
            // Extract city name
            String searchTerm = command.toLowerCase()
                .replace("search", "")
                .replace("hotel", "")
                .replace("in", "")
                .replace("find", "")
                .trim();

            String query = "SELECT hotel_id, nom_hotel, ville, etoiles FROM hotels " +
                          "WHERE LOWER(ville) LIKE ? OR LOWER(nom_hotel) LIKE ? LIMIT 10";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("🔍 HOTELS matching '" + searchTerm + "':\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("🏨 %s\n", rs.getString("nom_hotel")));
                result.append(String.format("   📍 %s | ⭐ %.1f stars\n\n",
                    rs.getString("ville"), rs.getDouble("etoiles")));
            }

            if (count == 0) {
                return "No hotels found matching '" + searchTerm + "'";
            }

            return result.toString() + String.format("Found %d hotel(s)", count);
        } catch (Exception e) {
            return "❌ Error searching hotels: " + e.getMessage();
        }
    }

    private String showHotelBookings() {
        try {
            String query = "SELECT COUNT(*) as total, " +
                          "SUM(CASE WHEN statut_reservation = 'EN_ATTENTE' THEN 1 ELSE 0 END) as pending, " +
                          "SUM(CASE WHEN statut_reservation = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmed, " +
                          "SUM(prix_total) as revenue FROM reservations_hotel";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return String.format("🏨 HOTEL BOOKINGS:\n\n" +
                                   "📋 Total: %d bookings\n" +
                                   "⏳ Pending: %d\n" +
                                   "✅ Confirmed: %d\n" +
                                   "💰 Total Revenue: $%.2f",
                                   rs.getInt("total"),
                                   rs.getInt("pending"),
                                   rs.getInt("confirmed"),
                                   rs.getDouble("revenue"));
            }
            return "No hotel bookings found";
        } catch (Exception e) {
            return "❌ Error fetching hotel bookings: " + e.getMessage();
        }
    }

    // ========== FLIGHT METHODS ==========

    private String showAllFlights() {
        try {
            String query = "SELECT v.vol_id, v.numero_vol, c.nom_compagnie, " +
                          "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                          "v.date_depart, v.places_disponibles, v.statut_vol " +
                          "FROM vols v " +
                          "JOIN compagnies_aeriennes c ON v.compagnie_id = c.compagnie_id " +
                          "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "LIMIT 15";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("✈️ FLIGHT LIST:\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("✈️ %s %s\n",
                    rs.getString("nom_compagnie"), rs.getString("numero_vol")));
                result.append(String.format("   %s → %s\n",
                    rs.getString("ville_depart"), rs.getString("ville_arrivee")));
                result.append(String.format("   🕐 %s | 💺 %d seats\n",
                    rs.getTimestamp("date_depart"), rs.getInt("places_disponibles")));
                result.append(String.format("   Status: %s\n\n", rs.getString("statut_vol")));
            }

            if (count == 0) {
                return "No flights found in the database.";
            }

            return result.toString() + String.format("Total: %d flight(s)", count);
        } catch (Exception e) {
            return "❌ Error fetching flights: " + e.getMessage();
        }
    }

    private String searchFlights(String command) {
        try {
            String query = "SELECT v.vol_id, v.numero_vol, c.nom_compagnie, " +
                          "ad.ville as ville_depart, aa.ville as ville_arrivee, " +
                          "v.date_depart, v.places_disponibles " +
                          "FROM vols v " +
                          "JOIN compagnies_aeriennes c ON v.compagnie_id = c.compagnie_id " +
                          "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "LIMIT 10";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("🔍 AVAILABLE FLIGHTS:\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("✈️ %s %s: %s → %s\n",
                    rs.getString("nom_compagnie"), rs.getString("numero_vol"),
                    rs.getString("ville_depart"), rs.getString("ville_arrivee")));
                result.append(String.format("   💺 %d seats available\n\n",
                    rs.getInt("places_disponibles")));
            }

            if (count == 0) {
                return "No flights found";
            }

            return result.toString() + String.format("Found %d flight(s)", count);
        } catch (Exception e) {
            return "❌ Error searching flights: " + e.getMessage();
        }
    }

    private String showFlightBookings() {
        try {
            String query = "SELECT COUNT(*) as total, " +
                          "SUM(CASE WHEN statut_reservation = 'EN_ATTENTE' THEN 1 ELSE 0 END) as pending, " +
                          "SUM(CASE WHEN statut_reservation = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmed, " +
                          "SUM(prix_total) as revenue FROM reservations_vol";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return String.format("✈️ FLIGHT BOOKINGS:\n\n" +
                                   "📋 Total: %d bookings\n" +
                                   "⏳ Pending: %d\n" +
                                   "✅ Confirmed: %d\n" +
                                   "💰 Total Revenue: $%.2f",
                                   rs.getInt("total"),
                                   rs.getInt("pending"),
                                   rs.getInt("confirmed"),
                                   rs.getDouble("revenue"));
            }
            return "No flight bookings found";
        } catch (Exception e) {
            return "❌ Error fetching flight bookings: " + e.getMessage();
        }
    }

    // ========== CAR RENTAL METHODS ==========

    private String showAllCars() {
        try {
            String query = "SELECT vehicule_id, marque, modele, categorie, prix_jour, is_available FROM vehicules LIMIT 15";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("🚗 CAR RENTAL LIST:\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                String availability = rs.getBoolean("is_available") ? "✅ Available" : "❌ Rented";
                result.append(String.format("🚗 %s %s (%s)\n",
                    rs.getString("marque"), rs.getString("modele"), rs.getString("categorie")));
                result.append(String.format("   💰 $%.2f/day | %s\n\n",
                    rs.getDouble("prix_jour"), availability));
            }

            if (count == 0) {
                return "No cars found in the database.";
            }

            return result.toString() + String.format("Total: %d car(s)", count);
        } catch (Exception e) {
            return "❌ Error fetching cars: " + e.getMessage();
        }
    }

    private String searchCars(String command) {
        try {
            String searchTerm = command.toLowerCase()
                .replace("search", "")
                .replace("car", "")
                .replace("in", "")
                .replace("find", "")
                .replace("available", "")
                .trim();

            String query = "SELECT vehicule_id, marque, modele, categorie, prix_jour, is_available FROM vehicules " +
                          "WHERE (LOWER(marque) LIKE ? OR LOWER(categorie) LIKE ?) AND is_available = TRUE LIMIT 10";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("🔍 AVAILABLE CARS matching '" + searchTerm + "':\n\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append(String.format("🚗 %s %s\n",
                    rs.getString("marque"), rs.getString("modele")));
                result.append(String.format("   💰 $%.2f/day\n\n",
                    rs.getDouble("prix_jour")));
            }

            if (count == 0) {
                return "No available cars found matching '" + searchTerm + "'";
            }

            return result.toString() + String.format("Found %d available car(s)", count);
        } catch (Exception e) {
            return "❌ Error searching cars: " + e.getMessage();
        }
    }

    private String showCarRentals() {
        try {
            String query = "SELECT COUNT(*) as total, " +
                          "SUM(CASE WHEN statut_reservation = 'EN_ATTENTE' THEN 1 ELSE 0 END) as pending, " +
                          "SUM(CASE WHEN statut_reservation = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmed, " +
                          "SUM(prix_total) as revenue FROM reservations_vehicule";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return String.format("🚗 CAR RENTALS:\n\n" +
                                   "📋 Total: %d rentals\n" +
                                   "⏳ Pending: %d\n" +
                                   "✅ Confirmed: %d\n" +
                                   "💰 Total Revenue: $%.2f",
                                   rs.getInt("total"),
                                   rs.getInt("pending"),
                                   rs.getInt("confirmed"),
                                   rs.getDouble("revenue"));
            }
            return "No car rentals found";
        } catch (Exception e) {
            return "❌ Error fetching car rentals: " + e.getMessage();
        }
    }

    // ========== COMPREHENSIVE BOOKING METHODS ==========

    private String showAllPendingBookings() {
        try {
            StringBuilder result = new StringBuilder("⏳ ALL PENDING BOOKINGS:\n\n");

            // Hotel bookings
            String hotelQuery = "SELECT COUNT(*) as count FROM reservations_hotel WHERE statut_reservation = 'EN_ATTENTE'";
            PreparedStatement stmt = dbConnection.prepareStatement(hotelQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result.append(String.format("🏨 Hotel Bookings: %d pending\n", rs.getInt("count")));
            }

            // Flight bookings
            String flightQuery = "SELECT COUNT(*) as count FROM reservations_vol WHERE statut_reservation = 'EN_ATTENTE'";
            stmt = dbConnection.prepareStatement(flightQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                result.append(String.format("✈️ Flight Bookings: %d pending\n", rs.getInt("count")));
            }

            // Car rentals
            String carQuery = "SELECT COUNT(*) as count FROM reservations_vehicule WHERE statut_reservation = 'EN_ATTENTE'";
            stmt = dbConnection.prepareStatement(carQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                result.append(String.format("🚗 Car Rentals: %d pending\n", rs.getInt("count")));
            }

            return result.toString();
        } catch (Exception e) {
            return "❌ Error fetching pending bookings: " + e.getMessage();
        }
    }

    private String showAllBookingsSummary() {
        try {
            StringBuilder result = new StringBuilder("📊 COMPLETE BOOKING SUMMARY:\n\n");

            // Hotels
            String hotelQuery = "SELECT COUNT(*) as total, SUM(prix_total) as revenue FROM reservations_hotel";
            PreparedStatement stmt = dbConnection.prepareStatement(hotelQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result.append(String.format("🏨 Hotels: %d bookings | $%.2f\n",
                    rs.getInt("total"), rs.getDouble("revenue")));
            }

            // Flights
            String flightQuery = "SELECT COUNT(*) as total, SUM(prix_total) as revenue FROM reservations_vol";
            stmt = dbConnection.prepareStatement(flightQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                result.append(String.format("✈️ Flights: %d bookings | $%.2f\n",
                    rs.getInt("total"), rs.getDouble("revenue")));
            }

            // Cars
            String carQuery = "SELECT COUNT(*) as total, SUM(prix_total) as revenue FROM reservations_vehicule";
            stmt = dbConnection.prepareStatement(carQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                result.append(String.format("🚗 Cars: %d rentals | $%.2f\n",
                    rs.getInt("total"), rs.getDouble("revenue")));
            }

            return result.toString();
        } catch (Exception e) {
            return "❌ Error generating booking summary: " + e.getMessage();
        }
    }

    private String calculateRevenue() {
        try {
            StringBuilder result = new StringBuilder("💰 REVENUE REPORT:\n\n");

            double totalRevenue = 0;

            // Hotel revenue
            String hotelQuery = "SELECT COALESCE(SUM(prix_total), 0) as revenue FROM reservations_hotel WHERE statut_reservation = 'CONFIRMEE'";
            PreparedStatement stmt = dbConnection.prepareStatement(hotelQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double hotelRev = rs.getDouble("revenue");
                totalRevenue += hotelRev;
                result.append(String.format("🏨 Hotels: $%.2f\n", hotelRev));
            }

            // Flight revenue
            String flightQuery = "SELECT COALESCE(SUM(prix_total), 0) as revenue FROM reservations_vol WHERE statut_reservation = 'CONFIRMEE'";
            stmt = dbConnection.prepareStatement(flightQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                double flightRev = rs.getDouble("revenue");
                totalRevenue += flightRev;
                result.append(String.format("✈️ Flights: $%.2f\n", flightRev));
            }

            // Car revenue
            String carQuery = "SELECT COALESCE(SUM(prix_total), 0) as revenue FROM reservations_vehicule WHERE statut_reservation = 'CONFIRMEE'";
            stmt = dbConnection.prepareStatement(carQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                double carRev = rs.getDouble("revenue");
                totalRevenue += carRev;
                result.append(String.format("🚗 Cars: $%.2f\n", carRev));
            }

            result.append(String.format("\n💵 TOTAL REVENUE: $%.2f", totalRevenue));

            return result.toString();
        } catch (Exception e) {
            return "❌ Error calculating revenue: " + e.getMessage();
        }
    }

    private String generateComprehensiveAnalytics() {
        try {
            StringBuilder analytics = new StringBuilder("📊 COMPREHENSIVE ANALYTICS REPORT\n");
            analytics.append("=" .repeat(40) + "\n\n");

            // Total users
            String userQuery = "SELECT COUNT(*) as total FROM users";
            PreparedStatement stmt = dbConnection.prepareStatement(userQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("👥 Total Users: %d\n\n", rs.getInt("total")));
            }

            // Inventory
            analytics.append("📦 INVENTORY:\n");

            String hotelCountQuery = "SELECT COUNT(*) as total FROM hotels";
            stmt = dbConnection.prepareStatement(hotelCountQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("  🏨 Hotels: %d\n", rs.getInt("total")));
            }

            String flightCountQuery = "SELECT COUNT(*) as total FROM vols";
            stmt = dbConnection.prepareStatement(flightCountQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("  ✈️ Flights: %d\n", rs.getInt("total")));
            }

            String carCountQuery = "SELECT COUNT(*) as total FROM vehicules";
            stmt = dbConnection.prepareStatement(carCountQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("  🚗 Cars: %d\n\n", rs.getInt("total")));
            }

            // Bookings summary
            analytics.append("📋 BOOKINGS:\n");

            String hotelBookingQuery = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN statut_reservation='CONFIRMEE' THEN prix_total ELSE 0 END) as revenue FROM reservations_hotel";
            stmt = dbConnection.prepareStatement(hotelBookingQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("  🏨 Hotel: %d ($%.2f)\n",
                    rs.getInt("total"), rs.getDouble("revenue")));
            }

            String flightBookingQuery = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN statut_reservation='CONFIRMEE' THEN prix_total ELSE 0 END) as revenue FROM reservations_vol";
            stmt = dbConnection.prepareStatement(flightBookingQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("  ✈️ Flight: %d ($%.2f)\n",
                    rs.getInt("total"), rs.getDouble("revenue")));
            }

            String carRentalQuery = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN statut_reservation='CONFIRMEE' THEN prix_total ELSE 0 END) as revenue FROM reservations_vehicule";
            stmt = dbConnection.prepareStatement(carRentalQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("  🚗 Car: %d ($%.2f)\n\n",
                    rs.getInt("total"), rs.getDouble("revenue")));
            }

            // Top performing hotels
            analytics.append("🏆 TOP HOTELS:\n");
            String topHotelsQuery = "SELECT h.nom_hotel, COUNT(*) as bookings FROM reservations_hotel rh " +
                "JOIN hotels h ON rh.hotel_id = h.hotel_id GROUP BY h.nom_hotel ORDER BY bookings DESC LIMIT 3";
            stmt = dbConnection.prepareStatement(topHotelsQuery);
            rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                analytics.append(String.format("  %d. %s (%d bookings)\n",
                    rank++, rs.getString("nom_hotel"), rs.getInt("bookings")));
            }

            return analytics.toString();
        } catch (Exception e) {
            return "❌ Error generating analytics: " + e.getMessage();
        }
    }

    private String generateAnalytics() {
        try {
            StringBuilder analytics = new StringBuilder("📊 TripWise Analytics Report\n\n");

            // Total revenue
            String revenueQuery = "SELECT SUM(prix_total) as total_revenue FROM reservations_hotel WHERE statut_reservation = 'CONFIRMEE'";
            PreparedStatement stmt = dbConnection.prepareStatement(revenueQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                analytics.append(String.format("💰 Total Revenue: $%.2f\n\n", rs.getDouble("total_revenue")));
            }

            // Top hotels
            String hotelsQuery = "SELECT h.nom_hotel, COUNT(*) as bookings " +
                                "FROM reservations_hotel r " +
                                "JOIN hotels h ON r.hotel_id = h.hotel_id " +
                                "GROUP BY h.nom_hotel " +
                                "ORDER BY bookings DESC " +
                                "LIMIT 3";

            stmt = dbConnection.prepareStatement(hotelsQuery);
            rs = stmt.executeQuery();

            analytics.append("🏨 Top Hotels:\n");
            int rank = 1;
            while (rs.next()) {
                analytics.append(String.format("%d. %s (%d bookings)\n",
                                              rank++, rs.getString("nom_hotel"), rs.getInt("bookings")));
            }

            return analytics.toString();
        } catch (Exception e) {
            return "❌ Error generating analytics: " + e.getMessage();
        }
    }

    // Employee quick actions
    @FXML
    private void handleManageBookings() {
        try {
            SceneManager.switchScene("/ui/employee-booking-management.fxml");
        } catch (Exception e) {
            showAlert("Error", "Could not navigate to booking management", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSmartSearch() {
        addAIMessage("🔍 Smart Search activated! What are you looking for?\n\n" +
                    "Examples:\n" +
                    "- 'Show me hotels in Paris under $200/night'\n" +
                    "- 'Find luxury hotels with 5 stars'\n" +
                    "- 'Search for available rooms this weekend'");
    }

    @FXML
    private void handleAnalytics() {
        String analytics = generateAnalytics();
        addAIMessage(analytics);
    }

    @FXML
    private void handleCreateBooking() {
        addAIMessage("📝 Creating a new booking...\n\n" +
                    "Please provide:\n" +
                    "1. Customer name or ID\n" +
                    "2. Hotel name\n" +
                    "3. Check-in date (YYYY-MM-DD)\n" +
                    "4. Check-out date (YYYY-MM-DD)\n" +
                    "5. Number of guests");
    }

    @FXML
    private void handleApproveAll() {
        try {
            String query = "UPDATE reservations_hotel SET statut_reservation = 'CONFIRMEE' WHERE statut_reservation = 'EN_ATTENTE'";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            int updated = stmt.executeUpdate();

            addAIMessage("✅ Successfully approved " + updated + " pending booking(s)!");
        } catch (Exception e) {
            addAIMessage("❌ Error approving bookings: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateBooking() {
        addAIMessage("🔄 Update Booking\n\n" +
                    "Provide booking ID and what you want to update.\n" +
                    "Example: 'Update booking #123 check-out date to 2026-02-20'");
    }

    @FXML
    private void handleCancelBooking() {
        addAIMessage("❌ Cancel Booking\n\n" +
                    "Provide the booking ID you want to cancel.\n" +
                    "Example: 'Cancel booking #123'");
    }

    private void addUserMessage(String text) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5));

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 18 18 4 18;");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label timeLabel = new Label(LocalDateTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");

        bubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(bubble);

        chatMessagesContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addAIMessage(String text) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5));

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 18 18 18 4;");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px;");

        Label timeLabel = new Label(LocalDateTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");

        bubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(bubble);

        chatMessagesContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (chatMessagesContainer.getParent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) chatMessagesContainer.getParent();
                scrollPane.setVvalue(1.0);
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ========== INNER CLASSES ==========
    
    /**
     * Chat Message Model for conversation history tracking
     */
    private static class ChatMessage {
        private String role; // "user" or "assistant"
        private String content;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() { 
            return role; 
        }
        
        public String getContent() { 
            return content; 
        }
    }
}
