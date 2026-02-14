package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import ui.model.User;
import ui.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MessagesController - Role-specific messaging interface
 * Admin: System announcements and broadcast messages
 * Employee: Customer support and internal communication
 * Traveler: Support tickets and booking inquiries
 */
public class MessagesController {

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private ListView<ChatConversation> conversationsList;
    @FXML private VBox chatMessagesBox;
    @FXML private TextField messageInput;
    @FXML private Label chatHeaderName;
    @FXML private Label chatHeaderStatus;
    @FXML private Label stat1Label;
    @FXML private Label stat1Title;
    @FXML private Label stat2Label;
    @FXML private Label stat2Title;
    @FXML private Label stat3Label;
    @FXML private Label stat3Title;

    private User.UserType userRole;
    private ObservableList<ChatConversation> conversations;
    private ChatConversation selectedConversation;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ ERROR: No user in session!");
            return;
        }
        
        userRole = currentUser.getUserType();
        System.out.println("✅ Messages - User Role: " + userRole);
        
        conversations = FXCollections.observableArrayList();
        setupConversationsList();
        loadRoleSpecificMessages();
    }

    private void setupConversationsList() {
        conversationsList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ChatConversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setGraphic(null);
                } else {
                    VBox cell = new VBox(6);
                    cell.setPadding(new Insets(12));
                    cell.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-cursor: hand;");
                    
                    HBox header = new HBox(8);
                    header.setAlignment(Pos.CENTER_LEFT);
                    
                    Label nameLabel = new Label(conversation.getName());
                    nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1f2937;");
                    HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
                    
                    Label timeLabel = new Label(conversation.getLastMessageTime());
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
                    
                    header.getChildren().addAll(nameLabel, timeLabel);
                    
                    Label messageLabel = new Label(conversation.getLastMessage());
                    messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
                    messageLabel.setWrapText(true);
                    messageLabel.setMaxWidth(250);
                    
                    HBox footer = new HBox(8);
                    footer.setAlignment(Pos.CENTER_LEFT);
                    
                    if (conversation.getUnreadCount() > 0) {
                        Label unreadBadge = new Label(String.valueOf(conversation.getUnreadCount()));
                        unreadBadge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                                           "-fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 11px; -fx-font-weight: 600;");
                        footer.getChildren().add(unreadBadge);
                    }
                    
                    cell.getChildren().addAll(header, messageLabel, footer);
                    setGraphic(cell);
                }
            }
        });
        
        conversationsList.setItems(conversations);
        conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadConversation(newVal);
            }
        });
    }

    private void loadRoleSpecificMessages() {
        if (userRole == null) {
            loadTravelerMessages();
            return;
        }
        
        switch (userRole) {
            case ADMIN:
            case RESPONSABLE:
                loadAdminMessages();
                break;
            case EMPLOYE:
                loadEmployeeMessages();
                break;
            case VOYAGEUR:
            case VISITEUR:
            default:
                loadTravelerMessages();
                break;
        }
    }

    private void loadAdminMessages() {
        pageTitle.setText("System Messages");
        pageSubtitle.setText("Broadcast announcements and system notifications");
        
        stat1Title.setText("Active Users");
        stat1Label.setText("1,247");
        stat2Title.setText("Unread Messages");
        stat2Label.setText("23");
        stat3Title.setText("Announcements Sent");
        stat3Label.setText("156");
        
        conversations.add(new ChatConversation("All Employees", "System maintenance scheduled for...", "10:30", 0, true));
        conversations.add(new ChatConversation("All Travelers", "Holiday season travel deals available!", "Yesterday", 0, true));
        conversations.add(new ChatConversation("Employee John Smith", "Request for booking override approval", "2h ago", 2, false));
        conversations.add(new ChatConversation("System Alerts", "Payment gateway upgrade completed", "5h ago", 0, true));
    }

    private void loadEmployeeMessages() {
        pageTitle.setText("Customer Support");
        pageSubtitle.setText("Handle customer inquiries and support tickets");
        
        stat1Title.setText("Open Tickets");
        stat1Label.setText("12");
        stat2Title.setText("Resolved Today");
        stat2Label.setText("8");
        stat3Title.setText("Avg Response Time");
        stat3Label.setText("15min");
        
        conversations.add(new ChatConversation("Sarah Williams", "Question about flight change policy", "10:45", 3, false));
        conversations.add(new ChatConversation("Michael Chen", "Need help with hotel booking", "11:20", 1, false));
        conversations.add(new ChatConversation("Emily Davis", "Refund request for cancelled flight", "Yesterday", 0, false));
        conversations.add(new ChatConversation("Robert Taylor", "Upgrade to business class inquiry", "2 days ago", 0, false));
    }

    private void loadTravelerMessages() {
        pageTitle.setText("My Messages");
        pageSubtitle.setText("Chat with customer support and view notifications");
        
        stat1Title.setText("Active Conversations");
        stat1Label.setText("2");
        stat2Title.setText("Unread Messages");
        stat2Label.setText("3");
        stat3Title.setText("Support Tickets");
        stat3Label.setText("1");
        
        conversations.add(new ChatConversation("Customer Support", "Your refund has been processed", "10:30", 2, false));
        conversations.add(new ChatConversation("Flight Updates", "Flight AA 1234 - Gate change to B12", "1h ago", 1, true));
        conversations.add(new ChatConversation("TripWise Notifications", "New travel deals to your favorite destinations", "Yesterday", 0, true));
        conversations.add(new ChatConversation("Booking Confirmations", "Hotel booking confirmed - Marriott NYC", "2 days ago", 0, true));
    }

    private void loadConversation(ChatConversation conversation) {
        selectedConversation = conversation;
        chatHeaderName.setText(conversation.getName());
        chatHeaderStatus.setText(conversation.isSystemMessage() ? "Automated" : "Online");
        
        chatMessagesBox.getChildren().clear();
        
        // Load sample messages based on role and conversation
        if (userRole == User.UserType.VOYAGEUR) {
            addReceivedMessage("Hello! How can I help you today?", "09:30");
            addSentMessage("I need help changing my flight date", "09:32");
            addReceivedMessage("I'd be happy to help with that. Can you provide your booking reference?", "09:33");
            addSentMessage("Sure, it's BK001", "09:34");
            addReceivedMessage("Thank you! I can see your booking. What date would you like to change to?", "09:35");
        } else if (userRole == User.UserType.EMPLOYE) {
            addReceivedMessage("Hi, I have a question about my hotel booking", "11:20");
            addSentMessage("Hello! I'm here to help. What's your question?", "11:21");
            addReceivedMessage("Can I upgrade to a suite?", "11:22");
        } else {
            // Admin broadcast preview
            addSystemMessage("ANNOUNCEMENT: System maintenance scheduled for tonight at 11 PM EST. Expected downtime: 2 hours.", "10:30");
        }
        
        // Mark as read
        conversation.setUnreadCount(0);
        conversationsList.refresh();
    }

    private void addSentMessage(String text, String time) {
        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER_RIGHT);
        messageRow.setPadding(new Insets(4, 0, 4, 60));
        
        VBox messageBubble = new VBox(4);
        messageBubble.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 16 16 4 16; -fx-padding: 12 16;");
        messageBubble.setMaxWidth(400);
        
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        textLabel.setWrapText(true);
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
        
        messageBubble.getChildren().addAll(textLabel, timeLabel);
        messageRow.getChildren().add(messageBubble);
        
        chatMessagesBox.getChildren().add(messageRow);
    }

    private void addReceivedMessage(String text, String time) {
        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER_LEFT);
        messageRow.setPadding(new Insets(4, 60, 4, 0));
        
        VBox messageBubble = new VBox(4);
        messageBubble.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 16 16 16 4; -fx-padding: 12 16;");
        messageBubble.setMaxWidth(400);
        
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px;");
        textLabel.setWrapText(true);
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        
        messageBubble.getChildren().addAll(textLabel, timeLabel);
        messageRow.getChildren().add(messageBubble);
        
        chatMessagesBox.getChildren().add(messageRow);
    }

    private void addSystemMessage(String text, String time) {
        VBox systemMsg = new VBox(6);
        systemMsg.setAlignment(Pos.CENTER);
        systemMsg.setPadding(new Insets(12, 0, 12, 0));
        
        Label msgLabel = new Label(text);
        msgLabel.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 12 20; " +
                         "-fx-background-radius: 12; -fx-font-size: 13px; -fx-font-weight: 500;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(500);
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        
        systemMsg.getChildren().addAll(msgLabel, timeLabel);
        chatMessagesBox.getChildren().add(systemMsg);
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && selectedConversation != null) {
            addSentMessage(message, LocalDateTime.now().format(TIME_FORMATTER));
            messageInput.clear();
            
            // Simulate response after delay (in real app, this would be async)
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        addReceivedMessage("Thank you for your message. Let me look into that for you.", 
                                         LocalDateTime.now().format(TIME_FORMATTER));
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void handleNewConversation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Conversation");
        alert.setHeaderText("Start New Chat");
        alert.setContentText("New conversation dialog will be implemented here.");
        alert.showAndWait();
    }

    // Chat Conversation Model
    public static class ChatConversation {
        private String name;
        private String lastMessage;
        private String lastMessageTime;
        private int unreadCount;
        private boolean isSystemMessage;

        public ChatConversation(String name, String lastMessage, String lastMessageTime, int unreadCount, boolean isSystemMessage) {
            this.name = name;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
            this.unreadCount = unreadCount;
            this.isSystemMessage = isSystemMessage;
        }

        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public String getLastMessageTime() { return lastMessageTime; }
        public int getUnreadCount() { return unreadCount; }
        public void setUnreadCount(int count) { this.unreadCount = count; }
        public boolean isSystemMessage() { return isSystemMessage; }
    }
}
