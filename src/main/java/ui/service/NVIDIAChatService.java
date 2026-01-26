package ui.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * NVIDIAChatService - Integrates NVIDIA NIM LLM models for AI chatbot
 * Uses database context to help users with bookings, deals, and recommendations
 */
public class NVIDIAChatService {

    private static final String API_KEY = "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";
    private static final String API_ENDPOINT = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String MODEL = "meta/llama-3.1-8b-instruct"; // Using a more reliable model
    private static final boolean USE_FALLBACK = true; // Use fallback responses if API fails

    private final OkHttpClient client;
    private final Gson gson;
    private final List<Message> conversationHistory;
    private final ui.util.DataSource dataSource;

    public static class Message {
        private String role; // "system", "user", or "assistant"
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    public NVIDIAChatService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
        this.dataSource = ui.util.DataSource.getInstance();

        // Initialize system prompt with database context
        initializeSystemPrompt();
    }

    private void initializeSystemPrompt() {
        String systemPrompt = "You are TripWise AI Assistant, a helpful travel booking assistant. " +
            "You help users find the best deals on flights, hotels, and car rentals. " +
            "You have access to real-time database information and can:\n" +
            "1. Search for available flights, hotels, and cars\n" +
            "2. Recommend best deals based on price and ratings\n" +
            "3. Help with booking management\n" +
            "4. Provide travel tips and suggestions\n" +
            "5. Answer questions about bookings, cancellations, and policies\n\n" +
            "Current Database Stats:\n" + getDatabaseStats() + "\n\n" +
            "Always be friendly, helpful, and concise. Provide specific recommendations when possible.";

        conversationHistory.add(new Message("system", systemPrompt));
    }

    /**
     * Send a message to the AI and get response
     */
    public String sendMessage(String userMessage) {
        // Add user message to history
        conversationHistory.add(new Message("user", userMessage));

        try {
            // Enhance message with database context if needed
            String enhancedMessage = enhanceMessageWithContext(userMessage);

            // Build request payload
            JsonObject requestBody = buildRequestPayload(enhancedMessage);

            // Make API call
            Request request = new Request.Builder()
                    .url(API_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                    ))
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                System.err.println("❌ API Error: " + response.code() + " - " + response.message());

                // Use fallback response based on the question
                if (USE_FALLBACK) {
                    String fallbackResponse = generateFallbackResponse(userMessage);
                    conversationHistory.add(new Message("assistant", fallbackResponse));
                    return fallbackResponse;
                }

                return "I'm sorry, I encountered an error. Please try again later.";
            }

            // Parse response
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            String assistantMessage = jsonResponse
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            // Add assistant response to history
            conversationHistory.add(new Message("assistant", assistantMessage));

            return assistantMessage;

        } catch (IOException e) {
            System.err.println("❌ Network error: " + e.getMessage());
            e.printStackTrace();

            // Use fallback response
            if (USE_FALLBACK) {
                String fallbackResponse = generateFallbackResponse(userMessage);
                conversationHistory.add(new Message("assistant", fallbackResponse));
                return fallbackResponse;
            }

            return "I'm having trouble connecting right now. Please try again.";
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();

            // Use fallback response
            if (USE_FALLBACK) {
                String fallbackResponse = generateFallbackResponse(userMessage);
                conversationHistory.add(new Message("assistant", fallbackResponse));
                return fallbackResponse;
            }

            return "Something went wrong. Please rephrase your question.";
        }
    }

    /**
     * Enhance user message with relevant database context
     */
    private String enhanceMessageWithContext(String message) {
        String lower = message.toLowerCase();
        StringBuilder context = new StringBuilder(message);

        try {
            Connection conn = dataSource.getConnection();

            // If asking about hotels
            if (lower.contains("hotel") || lower.contains("stay") || lower.contains("accommodation")) {
                String hotelInfo = getHotelDeals(conn);
                if (!hotelInfo.isEmpty()) {
                    context.append("\n\n[Database Context - Available Hotels:]\n").append(hotelInfo);
                }
            }

            // If asking about flights
            if (lower.contains("flight") || lower.contains("fly") || lower.contains("airline")) {
                String flightInfo = getFlightDeals(conn);
                if (!flightInfo.isEmpty()) {
                    context.append("\n\n[Database Context - Available Flights:]\n").append(flightInfo);
                }
            }

            // If asking about cars
            if (lower.contains("car") || lower.contains("rent") || lower.contains("vehicle")) {
                String carInfo = getCarDeals(conn);
                if (!carInfo.isEmpty()) {
                    context.append("\n\n[Database Context - Available Cars:]\n").append(carInfo);
                }
            }

            // If asking about bookings or reservations
            if (lower.contains("booking") || lower.contains("reservation") || lower.contains("my trips")) {
                String bookingInfo = getRecentBookings(conn);
                if (!bookingInfo.isEmpty()) {
                    context.append("\n\n[Database Context - Recent Bookings:]\n").append(bookingInfo);
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️  Could not fetch database context: " + e.getMessage());
        }

        return context.toString();
    }

    /**
     * Get database statistics for system prompt
     */
    private String getDatabaseStats() {
        StringBuilder stats = new StringBuilder();

        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();

            // Count hotels
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM hotels WHERE is_active=1");
            if (rs.next()) {
                stats.append("- ").append(rs.getInt("count")).append(" active hotels\n");
            }

            // Count flights
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM vols WHERE is_active=1");
            if (rs.next()) {
                stats.append("- ").append(rs.getInt("count")).append(" active flights\n");
            }

            // Count vehicles
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM vehicules WHERE is_available=1");
            if (rs.next()) {
                stats.append("- ").append(rs.getInt("count")).append(" available vehicles");
            }

        } catch (Exception e) {
            System.err.println("⚠️  Could not fetch database stats: " + e.getMessage());
        }

        return stats.toString();
    }

    private String getHotelDeals(Connection conn) {
        StringBuilder info = new StringBuilder();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT h.nom_hotel, h.ville, h.etoiles, MIN(c.prix_nuit) as min_price " +
                "FROM hotels h " +
                "LEFT JOIN chambres c ON h.hotel_id = c.hotel_id " +
                "WHERE h.is_active = 1 " +
                "GROUP BY h.hotel_id " +
                "ORDER BY min_price ASC " +
                "LIMIT 5"
            );

            while (rs.next()) {
                info.append(String.format("- %s in %s (%.1f stars) - from $%.0f/night\n",
                    rs.getString("nom_hotel"),
                    rs.getString("ville"),
                    rs.getDouble("etoiles"),
                    rs.getDouble("min_price")));
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error fetching hotel deals: " + e.getMessage());
        }
        return info.toString();
    }

    private String getFlightDeals(Connection conn) {
        StringBuilder info = new StringBuilder();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT v.numero_vol, ca.nom_compagnie, ad.ville as depart, aa.ville as arrivee, " +
                "v.date_depart, MIN(cv.prix) as min_price " +
                "FROM vols v " +
                "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                "JOIN classes_vol cv ON v.vol_id = cv.vol_id " +
                "WHERE v.is_active = 1 AND v.places_disponibles > 0 " +
                "GROUP BY v.vol_id " +
                "ORDER BY min_price ASC " +
                "LIMIT 5"
            );

            while (rs.next()) {
                info.append(String.format("- %s %s: %s → %s on %s - from $%.0f\n",
                    rs.getString("nom_compagnie"),
                    rs.getString("numero_vol"),
                    rs.getString("depart"),
                    rs.getString("arrivee"),
                    rs.getDate("date_depart"),
                    rs.getDouble("min_price")));
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error fetching flight deals: " + e.getMessage());
        }
        return info.toString();
    }

    private String getCarDeals(Connection conn) {
        StringBuilder info = new StringBuilder();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT marque, modele, categorie, prix_jour " +
                "FROM vehicules " +
                "WHERE is_available = 1 " +
                "ORDER BY prix_jour ASC " +
                "LIMIT 5"
            );

            while (rs.next()) {
                info.append(String.format("- %s %s (%s) - $%.0f/day\n",
                    rs.getString("marque"),
                    rs.getString("modele"),
                    rs.getString("categorie"),
                    rs.getDouble("prix_jour")));
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error fetching car deals: " + e.getMessage());
        }
        return info.toString();
    }

    private String getRecentBookings(Connection conn) {
        StringBuilder info = new StringBuilder();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT numero_confirmation, nom_hotel, date_checkin, statut_reservation " +
                "FROM reservations_hotel rh " +
                "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                "ORDER BY date_reservation DESC " +
                "LIMIT 3"
            );

            while (rs.next()) {
                info.append(String.format("- Booking %s: %s on %s (Status: %s)\n",
                    rs.getString("numero_confirmation"),
                    rs.getString("nom_hotel"),
                    rs.getDate("date_checkin"),
                    rs.getString("statut_reservation")));
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error fetching bookings: " + e.getMessage());
        }
        return info.toString();
    }

    /**
     * Build request payload for NVIDIA API
     */
    private JsonObject buildRequestPayload(String message) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", MODEL);
        payload.addProperty("temperature", 0.7);
        payload.addProperty("max_tokens", 1024);

        JsonArray messages = new JsonArray();

        // Add conversation history (last 10 messages for context)
        int startIndex = Math.max(0, conversationHistory.size() - 10);
        for (int i = startIndex; i < conversationHistory.size(); i++) {
            Message msg = conversationHistory.get(i);
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole());
            msgObj.addProperty("content", msg.getContent());
            messages.add(msgObj);
        }

        // Add current message if not already in history
        if (!conversationHistory.isEmpty() &&
            !conversationHistory.get(conversationHistory.size() - 1).getContent().equals(message)) {
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", message);
            messages.add(userMsg);
        }

        payload.add("messages", messages);

        return payload;
    }

    /**
     * Clear conversation history
     */
    public void clearHistory() {
        conversationHistory.clear();
        initializeSystemPrompt();
    }

    /**
     * Get conversation history size
     */
    public int getHistorySize() {
        return conversationHistory.size();
    }

    /**
     * Generate intelligent fallback responses when API is unavailable
     */
    private String generateFallbackResponse(String userMessage) {
        String lower = userMessage.toLowerCase();

        try {
            Connection conn = dataSource.getConnection();

            // Hotel queries
            if (lower.contains("hotel") || lower.contains("stay") || lower.contains("accommodation")) {
                String hotels = getHotelDeals(conn);
                if (!hotels.isEmpty()) {
                    return "Here are our top hotel recommendations:\n\n" + hotels +
                           "\n\nWould you like more details about any of these hotels?";
                }
                return "I can help you find hotels! We have properties in Paris, New York, Dubai, London, and Los Angeles. " +
                       "What destination are you interested in?";
            }

            // Booking queries
            if (lower.contains("booking") || lower.contains("reservation")) {
                if (lower.contains("pending") || lower.contains("waiting")) {
                    return "I can check pending bookings for you. Click on 'Manage Bookings' in the employee menu " +
                           "to see all pending reservations that need approval.";
                }
                return "I can help you with bookings! You can:\n" +
                       "• View all bookings\n" +
                       "• Check booking status\n" +
                       "• Approve or cancel bookings\n" +
                       "• Create new reservations\n\n" +
                       "What would you like to do?";
            }

            // Deal/price queries
            if (lower.contains("deal") || lower.contains("cheap") || lower.contains("price") || lower.contains("best")) {
                return "Great question! Our best deals include:\n\n" +
                       "🏨 Hotels from $160/night\n" +
                       "✈️ Flights starting at $120\n" +
                       "🚗 Car rentals from $35/day\n\n" +
                       "Would you like to see specific deals for hotels, flights, or cars?";
            }

            // Help/greeting
            if (lower.contains("hello") || lower.contains("hi") || lower.contains("help") ||
                lower.contains("what can you") || lower.contains("how can")) {
                return "Hello! I'm your TripWise AI Assistant. I can help you with:\n\n" +
                       "✅ Finding and booking hotels\n" +
                       "✅ Searching for flights\n" +
                       "✅ Renting cars\n" +
                       "✅ Managing your bookings\n" +
                       "✅ Finding the best travel deals\n\n" +
                       "What would you like to do today?";
            }

            // Cancel queries
            if (lower.contains("cancel")) {
                return "I can help you cancel a booking. Please provide the booking ID number, " +
                       "and I'll process the cancellation for you.\n\n" +
                       "Example: 'Cancel booking #123'";
            }

            // Default intelligent response
            return "I understand you're asking about: \"" + userMessage + "\"\n\n" +
                   "I can help you with:\n" +
                   "• Hotel bookings and recommendations\n" +
                   "• Flight searches\n" +
                   "• Car rentals\n" +
                   "• Managing existing bookings\n" +
                   "• Finding the best deals\n\n" +
                   "Could you please rephrase your question or tell me which service you need?";

        } catch (Exception e) {
            return "I'm here to help! I can assist you with hotels, flights, car rentals, " +
                   "and managing your bookings. What would you like to know?";
        }
    }
}
