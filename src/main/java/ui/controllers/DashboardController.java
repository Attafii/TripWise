package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import ui.model.User;
import ui.util.SceneManager;
import ui.util.SessionManager;
import ui.components.AIChatbotButton;

import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label titleLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private StackPane contentArea;

    // Menu buttons for highlighting
    @FXML
    private Button dashboardBtn;

    @FXML
    private Button bookingsBtn;

    @FXML
    private Button scheduleBtn;

    @FXML
    private Button paymentsBtn;

    @FXML
    private Button messagesBtn;

    @FXML
    private Button flightTrackingBtn;

    @FXML
    private Button aiAgentBtn;

    @FXML
    private Button dealsBtn;

    // Employee-specific buttons
    @FXML
    private Separator employeeSeparator;

    @FXML
    private Label employeeLabel;

    @FXML
    private Button employeeProfileBtn;

    @FXML
    private Button manageBookingsBtn;

    @FXML
    private Button viewCustomersBtn;

    @FXML
    public void initialize() {
        // Display logged-in user's name
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

            // Show/hide employee menu based on user type
            configureEmployeeMenu(currentUser);
        }

        // Load dashboard home view by default
        loadView("Dashboard", "/ui/dashboard-home.fxml");
        highlightButton(dashboardBtn);

        // Add global AI Chatbot floating button
        addGlobalChatbot();
    }

    /**
     * Configure employee menu visibility based on user type
     */
    private void configureEmployeeMenu(User user) {
        boolean isEmployee = user.getUserType() == User.UserType.EMPLOYE ||
                            user.getUserType() == User.UserType.ADMIN;

        if (employeeSeparator != null) {
            employeeSeparator.setVisible(isEmployee);
            employeeSeparator.setManaged(isEmployee);
        }

        if (employeeLabel != null) {
            employeeLabel.setVisible(isEmployee);
            employeeLabel.setManaged(isEmployee);
        }

        if (employeeProfileBtn != null) {
            employeeProfileBtn.setVisible(isEmployee);
            employeeProfileBtn.setManaged(isEmployee);
        }

        if (manageBookingsBtn != null) {
            manageBookingsBtn.setVisible(isEmployee);
            manageBookingsBtn.setManaged(isEmployee);
        }

        if (viewCustomersBtn != null) {
            viewCustomersBtn.setVisible(isEmployee);
            viewCustomersBtn.setManaged(isEmployee);
        }

        System.out.println("✅ Employee menu configured - User Type: " + user.getUserType() + " - Visible: " + isEmployee);
    }

    // Employee menu handlers
    @FXML
    private void onEmployeeProfile() {
        loadView("Employee Profile", "/ui/employee-profile.fxml");
        highlightButton(employeeProfileBtn);
    }

    @FXML
    private void onManageBookings() {
        loadView("Manage Bookings", "/ui/employee-booking-management.fxml");
        highlightButton(manageBookingsBtn);
    }

    @FXML
    private void onViewCustomers() {
        loadView("View Customers", "/ui/employee-customer-management.fxml");
        highlightButton(viewCustomersBtn);
    }

    @FXML
    private void onDashboard() {
        loadView("Dashboard", "/ui/dashboard-home.fxml");
        highlightButton(dashboardBtn);
    }

    @FXML
    private void onBookings() {
        loadView("Bookings", "/ui/all-bookings.fxml");
        highlightButton(bookingsBtn);
    }

    @FXML
    private void onSchedule() {
        loadView("Schedule", "/ui/schedule.fxml");
        highlightButton(scheduleBtn);
    }

    @FXML
    private void onPayments() {
        loadView("Payments", "/ui/payments.fxml");
        highlightButton(paymentsBtn);
    }

    @FXML
    private void onMessages() {
        loadView("Messages", "/ui/messages.fxml");
        highlightButton(messagesBtn);
    }

    @FXML
    private void onFlightTracking() {
        loadView("Flight Tracking", "/ui/flight-tracking.fxml");
        highlightButton(flightTrackingBtn);
    }

    @FXML
    private void onAIAgent() {
        loadView("AI Agent", "/ui/ai-agent.fxml");
        highlightButton(aiAgentBtn);
    }

    @FXML
    private void onDeals() {
        loadView("Deals", "/ui/deals.fxml");
        highlightButton(dealsBtn);
    }

    @FXML
    private void onLogout() {
        // Clear user session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        SessionManager.getInstance().clearSession();

        if (currentUser != null) {
            System.out.println("✅ User session cleared: " + currentUser.getFullName());
        }

        // Navigate back to login
        SceneManager.switchScene("/ui/login.fxml");
    }

    private void loadView(String title, String fxmlPath) {
        try {
            titleLabel.setText(title);
            Object loadedContent = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Handle both Pane and ScrollPane types
            if (loadedContent instanceof javafx.scene.Node) {
                contentArea.getChildren().setAll((javafx.scene.Node) loadedContent);
            } else {
                System.err.println("❌ Loaded content is not a Node: " + loadedContent.getClass());
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading view: " + fxmlPath);
            e.printStackTrace();
            // Try to show a placeholder
            Label errorLabel = new Label("View coming soon: " + title);
            errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #6b7280; -fx-padding: 50;");
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    /**
     * Highlight the active menu button
     */
    private void highlightButton(Button activeButton) {
        // Reset all buttons
        Button[] buttons = {dashboardBtn, bookingsBtn, scheduleBtn, paymentsBtn,
                           messagesBtn, flightTrackingBtn, aiAgentBtn, dealsBtn};

        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #6b7280; " +
                              "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 12 15; " +
                              "-fx-background-radius: 10; -fx-cursor: hand;";

        String activeStyle = "-fx-background-color: #eff6ff; -fx-text-fill: #1e40af; " +
                           "-fx-font-size: 13px; -fx-font-weight: 600; -fx-alignment: center-left; " +
                           "-fx-padding: 12 15; -fx-background-radius: 10; -fx-cursor: hand;";

        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle(btn == activeButton ? activeStyle : inactiveStyle);
            }
        }
    }

    /**
     * Add global AI Chatbot floating button (available on all pages)
     */
    private void addGlobalChatbot() {
        try {
            AIChatbotButton chatbot = new AIChatbotButton();
            rootPane.getChildren().add(chatbot);
            System.out.println("✅ Global AI Chatbot initialized - Available on all pages");
        } catch (Exception e) {
            System.err.println("❌ Error initializing Global AI Chatbot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
