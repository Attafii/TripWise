package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.User;
import ui.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PaymentsController - Role-specific payment views
 * Admin: System-wide revenue and transaction analytics
 * Employee: Process payments and refunds
 * Traveler: Personal payment methods and transaction history
 */
public class PaymentsController {

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private HBox paymentCardsBox;
    @FXML private TableView<PaymentTransaction> transactionsTable;
    @FXML private TableColumn<PaymentTransaction, String> dateColumn;
    @FXML private TableColumn<PaymentTransaction, String> descriptionColumn;
    @FXML private TableColumn<PaymentTransaction, String> typeColumn;
    @FXML private TableColumn<PaymentTransaction, String> amountColumn;
    @FXML private TableColumn<PaymentTransaction, String> statusColumn;
    @FXML private Label stat1Label;
    @FXML private Label stat1Title;
    @FXML private Label stat2Label;
    @FXML private Label stat2Title;
    @FXML private Label stat3Label;
    @FXML private Label stat3Title;
    @FXML private ComboBox<String> statusFilterCombo;

    private User.UserType userRole;
    private ObservableList<PaymentTransaction> transactions;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ ERROR: No user in session!");
            return;
        }
        
        userRole = currentUser.getUserType();
        System.out.println("✅ Payments - User Role: " + userRole);
        
        // Initialize status filter combo
        if (statusFilterCombo != null) {
            statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All Transactions", "Completed", "Pending", "Failed", "Refunded"
            ));
            statusFilterCombo.setValue("All Transactions");
        }
        
        transactions = FXCollections.observableArrayList();
        setupTable();
        loadRoleSpecificPayments();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDate().format(DATE_FORMATTER)));
        
        descriptionColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        
        typeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        
        amountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedAmount()));
        amountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    PaymentTransaction txn = getTableView().getItems().get(getIndex());
                    String color = txn.getAmount() > 0 ? "#10b981" : "#ef4444";
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: 700; -fx-font-size: 14px;");
                }
            }
        });
        
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(getStatusStyle(status));
                    setGraphic(badge);
                }
            }
        });
    }

    private String getStatusStyle(String status) {
        switch (status.toLowerCase()) {
            case "completed":
                return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 11px;";
            case "pending":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 11px;";
            case "failed":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 11px;";
            case "refunded":
                return "-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 11px;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 11px;";
        }
    }

    private void loadRoleSpecificPayments() {
        if (userRole == null) {
            loadTravelerPayments();
            transactionsTable.setItems(transactions);
            return;
        }
        
        switch (userRole) {
            case ADMIN:
            case RESPONSABLE:
                loadAdminPayments();
                break;
            case EMPLOYE:
                loadEmployeePayments();
                break;
            case VOYAGEUR:
            case VISITEUR:
            default:
                loadTravelerPayments();
                break;
        }
        
        transactionsTable.setItems(transactions);
    }

    private void loadAdminPayments() {
        pageTitle.setText("Revenue & Transactions");
        pageSubtitle.setText("System-wide payment analytics and financial overview");
        
        stat1Title.setText("Total Revenue");
        stat1Label.setText("$284,500");
        stat2Title.setText("Pending Payments");
        stat2Label.setText("$12,340");
        stat3Title.setText("Refunds Processed");
        stat3Label.setText("$8,750");
        
        // Admin sees all system transactions
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusHours(2), "Flight Booking #BK001", "Credit Card", 850.0, "Completed"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusHours(5), "Hotel Booking #HB234", "PayPal", 320.0, "Completed"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusHours(8), "Car Rental #CR456", "Debit Card", 180.0, "Pending"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusDays(1), "Refund - Cancellation", "Refund", -280.0, "Refunded"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusDays(2), "Flight Booking #BK002", "Credit Card", 1200.0, "Completed"));
        
        // No payment cards for admin
        paymentCardsBox.getChildren().clear();
        Label noCardsLabel = new Label("Payment cards not applicable for admin view");
        noCardsLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px; -fx-padding: 40;");
        paymentCardsBox.getChildren().add(noCardsLabel);
    }

    private void loadEmployeePayments() {
        pageTitle.setText("Payment Processing");
        pageSubtitle.setText("Process customer payments and handle refunds");
        
        stat1Title.setText("Processed Today");
        stat1Label.setText("$15,420");
        stat2Title.setText("Pending Approvals");
        stat2Label.setText("$3,200");
        stat3Title.setText("Refunds Issued");
        stat3Label.setText("$890");
        
        // Employee sees assigned transactions
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusMinutes(30), "Process Payment - BK089", "Credit Card", 650.0, "Pending"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusHours(1), "Refund Request - HB123", "Refund", -420.0, "Pending"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusHours(3), "Payment Verification", "Debit Card", 890.0, "Completed"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusHours(5), "Manual Payment Entry", "Cash", 150.0, "Completed"));
        
        // Employee tools instead of payment cards
        paymentCardsBox.getChildren().clear();
        addEmployeeToolCard("Process New Payment", "Handle customer payment transactions");
        addEmployeeToolCard("Issue Refund", "Process cancellation refunds");
        addEmployeeToolCard("Payment Verification", "Verify pending transactions");
    }

    private void loadTravelerPayments() {
        pageTitle.setText("My Payments");
        pageSubtitle.setText("Manage payment methods and view transaction history");
        
        stat1Title.setText("Total Spent");
        stat1Label.setText("$8,450");
        stat2Title.setText("Pending Payments");
        stat2Label.setText("$320");
        stat3Title.setText("Saved Payment Methods");
        stat3Label.setText("3");
        
        // Traveler sees personal transactions
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusDays(5), "Flight to New York - AA 1234", "Visa ****4532", 850.0, "Completed"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusDays(3), "Hotel Booking - Marriott", "Mastercard ****8901", 420.0, "Completed"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusDays(1), "Flight to Paris - AF 007", "Visa ****4532", 1200.0, "Pending"));
        transactions.add(new PaymentTransaction(
            LocalDateTime.now().minusHours(12), "Car Rental - Budget", "PayPal", 180.0, "Completed"));
        
        // Traveler sees saved payment methods
        paymentCardsBox.getChildren().clear();
        addPaymentCard("Visa", "****4532", "Expires 12/26", true);
        addPaymentCard("Mastercard", "****8901", "Expires 08/25", false);
        addPaymentCard("PayPal", "john.doe@email.com", "Linked", false);
    }

    private void addPaymentCard(String type, String number, String expiry, boolean isDefault) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                     "-fx-background-radius: 16; -fx-padding: 24; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); " +
                     "-fx-cursor: hand;");
        card.setPrefHeight(180);
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label(type);
        typeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: white;");
        HBox.setHgrow(typeLabel, javafx.scene.layout.Priority.ALWAYS);
        
        if (isDefault) {
            Label defaultBadge = new Label("Default");
            defaultBadge.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                                 "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 600;");
            header.getChildren().addAll(typeLabel, defaultBadge);
        } else {
            header.getChildren().add(typeLabel);
        }
        
        VBox.setMargin(header, new Insets(0, 0, 20, 0));
        
        Label numberLabel = new Label(number);
        numberLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: white; -fx-letter-spacing: 2;");
        
        Label expiryLabel = new Label(expiry);
        expiryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");
        
        card.getChildren().addAll(header, numberLabel, expiryLabel);
        paymentCardsBox.getChildren().add(card);
    }

    private void addEmployeeToolCard(String title, String description) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; " +
                     "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 12; -fx-cursor: hand;");
        card.setPrefHeight(120);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        descLabel.setWrapText(true);
        
        Button actionButton = new Button("Open");
        actionButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                             "-fx-background-radius: 6; -fx-padding: 6 16; -fx-cursor: hand; -fx-font-size: 12px;");
        
        card.getChildren().addAll(titleLabel, descLabel, actionButton);
        paymentCardsBox.getChildren().add(card);
    }

    @FXML
    private void handleAddPaymentMethod() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Payment Method");
        alert.setHeaderText("Add New Payment Method");
        alert.setContentText("Payment method addition dialog will be implemented here.");
        alert.showAndWait();
    }

    // Payment Transaction Model
    public static class PaymentTransaction {
        private LocalDateTime date;
        private String description;
        private String type;
        private double amount;
        private String status;

        public PaymentTransaction(LocalDateTime date, String description, String type, double amount, String status) {
            this.date = date;
            this.description = description;
            this.type = type;
            this.amount = amount;
            this.status = status;
        }

        public LocalDateTime getDate() { return date; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
        
        public String getFormattedAmount() {
            String sign = amount >= 0 ? "+" : "";
            return sign + "$" + String.format("%,.2f", Math.abs(amount));
        }
    }
}
