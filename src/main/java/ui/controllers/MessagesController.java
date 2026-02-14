package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.User;
import ui.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessagesController {

    @FXML private TextField searchField;
    @FXML private VBox conversationsList;
    @FXML private Label chatTitleLabel;
    @FXML private Label chatStatusLabel;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private ScrollPane messagesScroll;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private List<Conversation> conversations = new ArrayList<>();
    private Conversation selectedConversation;

    @FXML
    private void initialize() {
        // Create sample conversations
        createSampleConversations();
        loadConversations();

        // Enter key to send message
        messageInput.setOnAction(e -> sendMessage());
    }

    private void createSampleConversations() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String userName = currentUser != null ? currentUser.getFirstName() : "Traveler";

        conversations.add(new Conversation(
            1, "TripWise Support", "Online",
            "Hello " + userName + "! How can we help you today?",
            LocalDateTime.now().minusHours(1)
        ));

        conversations.add(new Conversation(
            2, "Booking Assistance", "Online",
            "Your recent booking has been confirmed!",
            LocalDateTime.now().minusDays(1)
        ));

        conversations.add(new Conversation(
            3, "Flight Updates", "System",
            "Your flight AF001 is on schedule.",
            LocalDateTime.now().minusDays(2)
        ));
    }

    private void loadConversations() {
        conversationsList.getChildren().clear();

        for (Conversation conv : conversations) {
            HBox convCard = createConversationCard(conv);
            conversationsList.getChildren().add(convCard);
        }
    }

    private HBox createConversationCard(Conversation conv) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle("-fx-background-color: white; -fx-cursor: hand;");

        // Avatar
        Label avatar = new Label(conv.name.substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                       "-fx-font-size: 14px; -fx-font-weight: bold; " +
                       "-fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40; " +
                       "-fx-background-radius: 20; -fx-alignment: center;");
        avatar.setAlignment(Pos.CENTER);

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(conv.name);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lastMsg = new Label(conv.lastMessage);
        lastMsg.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lastMsg.setMaxWidth(180);

        info.getChildren().addAll(nameLabel, lastMsg);

        // Time
        Label timeLabel = new Label(formatTime(conv.lastMessageTime));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        card.getChildren().addAll(avatar, info, timeLabel);

        // Click handler
        card.setOnMouseClicked(e -> selectConversation(conv));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f3f4f6; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " +
            (selectedConversation == conv ? "#eff6ff" : "white") + "; -fx-cursor: hand;"));

        return card;
    }

    private void selectConversation(Conversation conv) {
        selectedConversation = conv;
        chatTitleLabel.setText(conv.name);
        chatStatusLabel.setText(conv.status);

        loadMessages(conv);
        loadConversations(); // Refresh to update selection
    }

    private void loadMessages(Conversation conv) {
        messagesContainer.getChildren().clear();

        // Add welcome/system message
        addSystemMessage("This is the beginning of your conversation with " + conv.name);

        // Add sample messages based on conversation
        if (conv.id == 1) {
            addReceivedMessage("Hello! Welcome to TripWise Support. How can I assist you today?",
                LocalDateTime.now().minusHours(1));
        } else if (conv.id == 2) {
            addReceivedMessage("Great news! Your booking has been confirmed.",
                LocalDateTime.now().minusDays(1));
            addReceivedMessage("You can view your booking details in the 'My Bookings' section.",
                LocalDateTime.now().minusDays(1).plusMinutes(2));
        } else if (conv.id == 3) {
            addReceivedMessage("Flight Status Update: Your flight AF001 Paris to New York is on schedule.",
                LocalDateTime.now().minusDays(2));
            addReceivedMessage("Departure: Feb 18, 2026 at 14:20",
                LocalDateTime.now().minusDays(2).plusMinutes(1));
        }

        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-background-color: #f3f4f6; " +
                      "-fx-padding: 8 15; -fx-background-radius: 15;");

        box.getChildren().add(label);
        messagesContainer.getChildren().add(box);
    }

    private void addReceivedMessage(String text, LocalDateTime time) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 50, 5, 10));

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(350);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setStyle("-fx-background-color: white; -fx-background-radius: 18 18 18 4; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");

        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #111827;");

        Label timeLabel = new Label(time.format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        bubble.getChildren().addAll(msgLabel, timeLabel);
        box.getChildren().add(bubble);
        messagesContainer.getChildren().add(box);
    }

    private void addSentMessage(String text, LocalDateTime time) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(5, 10, 5, 50));

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(350);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 18 18 4 18;");

        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        Label timeLabel = new Label(time.format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.7);");

        bubble.getChildren().addAll(msgLabel, timeLabel);
        box.getChildren().add(bubble);
        messagesContainer.getChildren().add(box);
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || selectedConversation == null) return;

        // Add sent message
        addSentMessage(text, LocalDateTime.now());
        messageInput.clear();

        // Simulate response
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}

            String response = generateAutoResponse(text);
            addReceivedMessage(response, LocalDateTime.now());
            scrollToBottom();
        });

        scrollToBottom();
    }

    private String generateAutoResponse(String userMessage) {
        String lower = userMessage.toLowerCase();

        if (lower.contains("booking") || lower.contains("reservation")) {
            return "I can help you with your booking! Please go to 'My Bookings' to view all your reservations, or tell me your confirmation number.";
        } else if (lower.contains("flight") || lower.contains("vol")) {
            return "For flight information, please check the 'Flight Tracking' section or provide your flight number.";
        } else if (lower.contains("cancel")) {
            return "To cancel a booking, please go to 'My Bookings', select the booking you want to cancel, and click the 'Cancel' button.";
        } else if (lower.contains("help")) {
            return "I'm here to help! You can ask me about:\n- Bookings and reservations\n- Flight status\n- Payment questions\n- Account settings";
        } else if (lower.contains("thank")) {
            return "You're welcome! Is there anything else I can help you with?";
        } else {
            return "Thank you for your message. A support agent will review your inquiry shortly. In the meantime, you can check our FAQ section or browse 'My Bookings' for your travel details.";
        }
    }

    @FXML
    private void startNewConversation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("Start a new support conversation");
        dialog.setContentText("What can we help you with?");

        dialog.showAndWait().ifPresent(topic -> {
            if (!topic.trim().isEmpty()) {
                Conversation newConv = new Conversation(
                    conversations.size() + 1,
                    "Support: " + topic.substring(0, Math.min(topic.length(), 20)),
                    "Online",
                    topic,
                    LocalDateTime.now()
                );
                conversations.add(0, newConv);
                loadConversations();
                selectConversation(newConv);

                // Send the initial message
                addSentMessage(topic, LocalDateTime.now());

                // Auto response
                javafx.application.Platform.runLater(() -> {
                    addReceivedMessage("Thank you for contacting TripWise Support! A team member will assist you shortly.",
                        LocalDateTime.now());
                    scrollToBottom();
                });
            }
        });
    }

    private void scrollToBottom() {
        javafx.application.Platform.runLater(() -> messagesScroll.setVvalue(1.0));
    }

    private String formatTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        if (time.toLocalDate().equals(now.toLocalDate())) {
            return time.format(TIME_FORMATTER);
        } else if (time.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "Yesterday";
        } else {
            return time.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }

    // Inner class for conversation
    private static class Conversation {
        int id;
        String name;
        String status;
        String lastMessage;
        LocalDateTime lastMessageTime;

        Conversation(int id, String name, String status, String lastMessage, LocalDateTime lastMessageTime) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
        }
    }
}
