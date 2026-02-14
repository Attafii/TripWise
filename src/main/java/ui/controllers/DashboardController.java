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

            // Configure menu based on user type
            configureMenuForUserType(currentUser);
        }

        // Load appropriate dashboard based on user type
        loadDefaultDashboard();
        highlightButton(dashboardBtn);

        // Add global AI Chatbot floating button
        addGlobalChatbot();
    }

    /**
     * Load the appropriate dashboard based on user type
     */
    private void loadDefaultDashboard() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadView("Dashboard", "/ui/dashboard-home.fxml");
            return;
        }

        switch (currentUser.getUserType()) {
            case VOYAGEUR:
                loadView("My Dashboard", "/ui/traveler-dashboard.fxml");
                break;
            case EMPLOYE:
            case ADMIN:
            case RESPONSABLE:
                loadView("Dashboard", "/ui/dashboard-home.fxml");
                break;
            default:
                loadView("Dashboard", "/ui/dashboard-home.fxml");
        }
    }

    /**
     * Configure menu visibility based on user type
     */
    private void configureMenuForUserType(User user) {
        boolean isTraveler = user.getUserType() == User.UserType.VOYAGEUR;
        boolean isVisitor = user.getUserType() == User.UserType.VISITEUR;

        // For travelers, hide admin-only features
        if (isTraveler || isVisitor) {
            // Hide AI Agent for regular travelers (optional - can enable if desired)
            // aiAgentBtn.setVisible(false);
            // aiAgentBtn.setManaged(false);
        }

        System.out.println("✅ Menu configured for user type: " + user.getUserType());
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
        loadView("Manage Hotel Bookings", "/ui/employee-booking-management.fxml");
        highlightButton(manageBookingsBtn);
    }

    @FXML
    private void onManageFlights() {
        loadView("Manage Flight Bookings", "/ui/employee-flight-management.fxml");
    }

    @FXML
    private void onViewCustomers() {
        loadView("View Customers", "/ui/employee-customer-management.fxml");
        highlightButton(viewCustomersBtn);
    }

    @FXML
    private void onDashboard() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUserType() == User.UserType.VOYAGEUR) {
            loadView("My Dashboard", "/ui/traveler-dashboard.fxml");
        } else {
            loadView("Dashboard", "/ui/dashboard-home.fxml");
        }
        highlightButton(dashboardBtn);
    }

    @FXML
    private void onBookings() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUserType() == User.UserType.VOYAGEUR) {
            loadView("My Bookings", "/ui/traveler-bookings.fxml");
        } else {
            loadView("Bookings", "/ui/all-bookings.fxml");
        }
        highlightButton(bookingsBtn);
    }

    @FXML
    private void onBookFlight() {
        loadView("Book Flight", "/ui/book-flight-new.fxml");
    }

    @FXML
    private void onBookHotel() {
        loadView("Book Hotel", "/ui/book-hotel-new.fxml");
    }

    @FXML
    private void onRentCar() {
        loadView("Rent Car", "/ui/rent-car.fxml");
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
    private void onFlightAnalytics() {
        loadView("Flight Analytics", "/ui/flight-analytics.fxml");
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
        // Reset all buttons with their unique colors
        resetMenuButtonStyles();

        // Apply active style to selected button
        if (activeButton != null) {
            String activeColor = getButtonActiveColor(activeButton);
            String activeStyle = "-fx-background-color: " + activeColor + "; -fx-text-fill: white; " +
                               "-fx-font-size: 13px; -fx-font-weight: 600; -fx-alignment: center-left; " +
                               "-fx-padding: 12 15; -fx-background-radius: 10; -fx-cursor: hand; -fx-min-width: 180;";
            activeButton.setStyle(activeStyle);
        }
    }

    /**
     * Reset all menu buttons to their default colored styles
     */
    private void resetMenuButtonStyles() {
        String baseStyle = "-fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 12 15; " +
                          "-fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: 500; -fx-min-width: 180;";

        // Dashboard - Blue
        if (dashboardBtn != null) {
            dashboardBtn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; " + baseStyle);
        }

        // Bookings - Purple
        if (bookingsBtn != null) {
            bookingsBtn.setStyle("-fx-background-color: #f5f3ff; -fx-text-fill: #8b5cf6; " + baseStyle);
        }

        // Schedule - Cyan
        if (scheduleBtn != null) {
            scheduleBtn.setStyle("-fx-background-color: #ecfeff; -fx-text-fill: #06b6d4; " + baseStyle);
        }

        // Payments - Green
        if (paymentsBtn != null) {
            paymentsBtn.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; " + baseStyle);
        }

        // Messages - Pink
        if (messagesBtn != null) {
            messagesBtn.setStyle("-fx-background-color: #fdf2f8; -fx-text-fill: #ec4899; " + baseStyle);
        }

        // Flight Tracking - Orange
        if (flightTrackingBtn != null) {
            flightTrackingBtn.setStyle("-fx-background-color: #fff7ed; -fx-text-fill: #f97316; " + baseStyle);
        }

        // AI Agent - Purple
        if (aiAgentBtn != null) {
            aiAgentBtn.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #9333ea; " + baseStyle);
        }

        // Deals - Yellow/Gold
        if (dealsBtn != null) {
            dealsBtn.setStyle("-fx-background-color: #fefce8; -fx-text-fill: #ca8a04; " + baseStyle);
        }
    }

    /**
     * Get active color for each button
     */
    private String getButtonActiveColor(Button button) {
        if (button == dashboardBtn) return "#2563eb";      // Blue
        if (button == bookingsBtn) return "#7c3aed";       // Purple
        if (button == scheduleBtn) return "#0891b2";       // Cyan
        if (button == paymentsBtn) return "#059669";       // Green
        if (button == messagesBtn) return "#db2777";       // Pink
        if (button == flightTrackingBtn) return "#ea580c"; // Orange
        if (button == aiAgentBtn) return "#7c3aed";        // Purple
        if (button == dealsBtn) return "#d97706";          // Gold
        return "#3b82f6"; // default blue
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
