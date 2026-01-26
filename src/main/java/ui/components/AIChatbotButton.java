package ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Window;
import ui.service.NVIDIAChatService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AIChatbotButton - Floating AI chatbot button (bottom-right corner)
 * Opens a chat window with NVIDIA AI assistant
 */
public class AIChatbotButton extends StackPane {

    private final Button chatButton;
    private Popup chatPopup;
    private VBox messagesContainer;
    private TextField messageInput;
    private ScrollPane scrollPane;
    private NVIDIAChatService chatService;
    private boolean isTyping = false;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AIChatbotButton() {
        chatService = new NVIDIAChatService();

        // Create floating button - Blue circular with sparkle icon
        chatButton = new Button("✨");
        chatButton.setPrefSize(56, 56);
        chatButton.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-background-radius: 28;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 26px;" +
            "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.4), 12, 0, 0, 4);" +
            "-fx-cursor: hand;"
        );

        // Green online indicator
        Circle onlineIndicator = new Circle(8);
        onlineIndicator.setStyle("-fx-fill: #10b981;");
        StackPane.setAlignment(onlineIndicator, Pos.TOP_RIGHT);
        StackPane.setMargin(onlineIndicator, new Insets(2, 2, 0, 0));

        // Hover effect
        chatButton.setOnMouseEntered(e -> {
            chatButton.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-background-radius: 28;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 26px;" +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.6), 16, 0, 0, 6);" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.05;" +
                "-fx-scale-y: 1.05;"
            );
        });

        chatButton.setOnMouseExited(e -> {
            chatButton.setStyle(
                "-fx-background-color: #3b82f6;" +
                "-fx-background-radius: 28;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 26px;" +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.4), 12, 0, 0, 4);" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.0;" +
                "-fx-scale-y: 1.0;"
            );
        });

        chatButton.setOnAction(e -> toggleChatWindow());

        // Position in bottom-right corner
        StackPane.setAlignment(chatButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatButton, new Insets(0, 30, 30, 0));
        StackPane.setAlignment(onlineIndicator, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(onlineIndicator, new Insets(0, 30, 70, 0));

        getChildren().addAll(chatButton, onlineIndicator);
        setPickOnBounds(false); // Only the button should be clickable
    }

    private void toggleChatWindow() {
        if (chatPopup == null) {
            createChatWindow();
        }

        if (chatPopup.isShowing()) {
            chatPopup.hide();
            chatButton.setText("💬");
        } else {
            Window owner = chatButton.getScene().getWindow();
            chatPopup.show(owner);

            // Position bottom-right above the button
            double x = owner.getX() + owner.getWidth() - 400 - 40;
            double y = owner.getY() + owner.getHeight() - 550 - 110;
            chatPopup.setX(x);
            chatPopup.setY(y);

            chatButton.setText("✕");
            messageInput.requestFocus();
        }
    }

    private void createChatWindow() {
        chatPopup = new Popup();

        VBox chatBox = new VBox(0);
        chatBox.setPrefSize(480, 600);
        chatBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 8);"
        );

        // Header - Blue gradient matching the button
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 20, 24, 20));
        header.setStyle("-fx-background-color: #3b82f6;" +
                       "-fx-background-radius: 20 20 0 0;");

        // AI Icon
        Label iconLabel = new Label("✨");
        iconLabel.setStyle("-fx-font-size: 28px; -fx-background-color: white; -fx-background-radius: 20; -fx-padding: 8 12;");

        VBox headerText = new VBox(3);
        Label titleLabel = new Label("AI Travel Assistant");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label statusLabel = new Label("Powered by TripWise AI");
        statusLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12px;");
        headerText.getChildren().addAll(titleLabel, statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 30; -fx-min-height: 30;");
        closeBtn.setOnAction(e -> toggleChatWindow());

        header.getChildren().addAll(iconLabel, headerText, spacer, closeBtn);

        // Messages Container
        messagesContainer = new VBox(15);
        messagesContainer.setPadding(new Insets(20));
        messagesContainer.setStyle("-fx-background-color: white;");

        scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-background: white;");
        scrollPane.setPrefHeight(420);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Welcome message
        addAssistantMessage("Hi! I'm your AI travel assistant. I can help you plan your perfect trip, find the best destinations, compare flight options, and provide personalized recommendations. How can I help you today?");

        // Quick Suggestions
        VBox suggestionsBox = new VBox(12);
        suggestionsBox.setPadding(new Insets(0, 20, 15, 20));
        Label suggestionsLabel = new Label("Quick suggestions:");
        suggestionsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");

        HBox row1 = new HBox(10);
        Button suggestBtn1 = createSuggestionButton("📍 Find best destinations for summer");
        Button suggestBtn2 = createSuggestionButton("📅 Plan a weekend getaway");
        row1.getChildren().addAll(suggestBtn1, suggestBtn2);

        HBox row2 = new HBox(10);
        Button suggestBtn3 = createSuggestionButton("💵 Budget-friendly travel options");
        Button suggestBtn4 = createSuggestionButton("👨‍👩‍👧 Family vacation recommendations");
        row2.getChildren().addAll(suggestBtn3, suggestBtn4);

        suggestionsBox.getChildren().addAll(suggestionsLabel, row1, row2);

        // Input Area
        HBox inputArea = new HBox(12);
        inputArea.setPadding(new Insets(15, 20, 20, 20));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        messageInput = new TextField();
        messageInput.setPromptText("Ask me anything about your travel plans...");
        messageInput.setPrefHeight(44);
        messageInput.setStyle(
            "-fx-background-color: #f3f4f6;" +
            "-fx-background-radius: 22;" +
            "-fx-padding: 12 18;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 14px;"
        );
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendBtn = new Button("➤");
        sendBtn.setPrefSize(44, 44);
        sendBtn.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-background-radius: 22;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 8, 0, 0, 2);"
        );

        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle(
            "-fx-background-color: #2563eb;" +
            "-fx-background-radius: 22;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.5), 10, 0, 0, 3);"
        ));

        sendBtn.setOnMouseExited(e -> sendBtn.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-background-radius: 22;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 8, 0, 0, 2);"
        ));

        sendBtn.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());

        inputArea.getChildren().addAll(messageInput, sendBtn);

        chatBox.getChildren().addAll(header, scrollPane, suggestionsBox, inputArea);
        chatPopup.getContent().add(chatBox);
    }

    private Button createSuggestionButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: #eff6ff;" +
            "-fx-text-fill: #3b82f6;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 14;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #dbeafe;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #dbeafe;" +
            "-fx-text-fill: #2563eb;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 14;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #3b82f6;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #eff6ff;" +
            "-fx-text-fill: #3b82f6;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 14;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #dbeafe;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;"
        ));

        btn.setOnAction(e -> {
            messageInput.setText(text.replaceAll("^[📍📅💵👨‍👩‍👧]\\s*", ""));
            sendMessage();
        });

        btn.setWrapText(true);
        btn.setMaxWidth(220);
        return btn;
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty() || isTyping) return;

        // Add user message
        addUserMessage(message);
        messageInput.clear();

        // Show typing indicator
        isTyping = true;
        Label typingLabel = new Label("AI is typing...");
        typingLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px; -fx-font-style: italic;");
        messagesContainer.getChildren().add(typingLabel);
        scrollToBottom();

        // Get AI response in background thread
        new Thread(() -> {
            String response = chatService.sendMessage(message);

            Platform.runLater(() -> {
                messagesContainer.getChildren().remove(typingLabel);
                addAssistantMessage(response);
                isTyping = false;
            });
        }).start();
    }

    private void addUserMessage(String text) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(320);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-background-radius: 18 18 4 18;"
        );

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-line-spacing: 3px;");

        Label timeLabel = new Label(LocalDateTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");

        bubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(bubble);

        messagesContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addAssistantMessage(String text) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(320);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setStyle(
            "-fx-background-color: #f3f4f6;" +
            "-fx-background-radius: 18 18 18 4;"
        );

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px; -fx-line-spacing: 3px;");

        Label timeLabel = new Label(LocalDateTime.now().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");

        bubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(bubble);

        messagesContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}
