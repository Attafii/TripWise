package ui.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
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
    private Button advancedSearchBtn;

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
    private Button employeeAnalyticsBtn;

    @FXML
    private Button reimbursementsBtn;

    // Admin-specific buttons
    @FXML
    private Separator adminSeparator;

    @FXML
    private Label adminLabel;

    @FXML
    private Button userManagementBtn;

    @FXML
    private Button systemSettingsBtn;

    @FXML
    private Button systemAnalyticsBtn;

    @FXML
    private Button systemHealthBtn;

    @FXML
    private Button backupRestoreBtn;

    @FXML
    private Button activityLogsBtn;

    @FXML
    private VBox sidebar;

    private boolean isTransitioning = false;

    @FXML
    public void initialize() {
        // Display logged-in user's name
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

            // Show/hide employee menu based on user type
            configureEmployeeMenu(currentUser);
            configureAdminMenu(currentUser);
        }

        // Play entrance animations
        playEntranceAnimation();

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

        if (employeeAnalyticsBtn != null) {
            employeeAnalyticsBtn.setVisible(isEmployee);
            employeeAnalyticsBtn.setManaged(isEmployee);
        }

        if (reimbursementsBtn != null) {
            reimbursementsBtn.setVisible(isEmployee);
            reimbursementsBtn.setManaged(isEmployee);
        }

        System.out.println("✅ Employee menu configured - User Type: " + user.getUserType() + " - Visible: " + isEmployee);
    }

    /**
     * Configure admin menu visibility based on user type
     */
    private void configureAdminMenu(User user) {
        boolean isAdmin = user.getUserType() == User.UserType.ADMIN ||
                         user.getUserType() == User.UserType.RESPONSABLE;

        if (adminSeparator != null) {
            adminSeparator.setVisible(isAdmin);
            adminSeparator.setManaged(isAdmin);
        }

        if (adminLabel != null) {
            adminLabel.setVisible(isAdmin);
            adminLabel.setManaged(isAdmin);
        }

        if (userManagementBtn != null) {
            userManagementBtn.setVisible(isAdmin);
            userManagementBtn.setManaged(isAdmin);
        }

        if (systemSettingsBtn != null) {
            systemSettingsBtn.setVisible(isAdmin);
            systemSettingsBtn.setManaged(isAdmin);
        }

        if (systemAnalyticsBtn != null) {
            systemAnalyticsBtn.setVisible(isAdmin);
            systemAnalyticsBtn.setManaged(isAdmin);
        }

        if (systemHealthBtn != null) {
            systemHealthBtn.setVisible(isAdmin);
            systemHealthBtn.setManaged(isAdmin);
        }

        if (backupRestoreBtn != null) {
            backupRestoreBtn.setVisible(isAdmin);
            backupRestoreBtn.setManaged(isAdmin);
        }

        if (activityLogsBtn != null) {
            activityLogsBtn.setVisible(isAdmin);
            activityLogsBtn.setManaged(isAdmin);
        }

        System.out.println("✅ Admin menu configured - User Type: " + user.getUserType() + " - Visible: " + isAdmin);
    }

    // Admin menu handlers
    @FXML
    private void onUserManagement() {
        loadView("User Management", "/ui/admin/admin-user-management.fxml");
        highlightButton(userManagementBtn);
    }

    @FXML
    private void onSystemSettings() {
        loadView("System Settings", "/ui/admin/admin-settings.fxml");
        highlightButton(systemSettingsBtn);
    }

    @FXML
    private void onSystemAnalytics() {
        loadView("System Analytics", "/ui/admin/admin-analytics.fxml");
        highlightButton(systemAnalyticsBtn);
    }

    @FXML
    private void onSystemHealth() {
        loadView("System Health", "/ui/admin/admin-system-health.fxml");
        highlightButton(systemHealthBtn);
    }

    @FXML
    private void onBackupRestore() {
        loadView("Backup & Restore", "/ui/admin/admin-backup.fxml");
        highlightButton(backupRestoreBtn);
    }

    @FXML
    private void onActivityLogs() {
        loadView("Activity Logs", "/ui/admin/admin-activity-logs.fxml");
        highlightButton(activityLogsBtn);
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
    private void onEmployeeAnalytics() {
        loadView("Employee Analytics", "/ui/employee/employee-analytics.fxml");
        highlightButton(employeeAnalyticsBtn);
    }

    /**
     * Open Reimbursement Requests (Islem's module)
     */
    @FXML
    private void onReimbursements() {
        loadView("Reimbursements", "/ui/approvals.fxml");
        highlightButton(reimbursementsBtn);
    }

    @FXML
    private void onDashboard() {
        loadView("Dashboard", "/ui/dashboard-home.fxml");
        highlightButton(dashboardBtn);
    }

    @FXML
    private void onAdvancedSearch() {
        loadView("Advanced Search", "/ui/traveler/traveler-advanced-search.fxml");
        highlightButton(advancedSearchBtn);
    }

    @FXML
    private void onBookings() {
        loadView("My Bookings", "/ui/traveler/traveler-bookings.fxml");
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
        loadView("Deals & Offers", "/ui/traveler/traveler-deals.fxml");
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

        // Add fade out animation before logout
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Navigate back to login after fade completes
            SceneManager.switchScene("/ui/login.fxml");
        });
        fadeOut.play();
    }

    private void loadView(String title, String fxmlPath) {
        // Prevent multiple transitions at once
        if (isTransitioning) {
            return;
        }
        isTransitioning = true;

        try {
            titleLabel.setText(title);
            Object loadedContent = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Handle both Pane and ScrollPane types
            if (loadedContent instanceof javafx.scene.Node) {
                javafx.scene.Node newContent = (javafx.scene.Node) loadedContent;
                
                // Fade out old content
                if (!contentArea.getChildren().isEmpty()) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea.getChildren().get(0));
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    
                    fadeOut.setOnFinished(e -> {
                        // Set new content and fade in
                        contentArea.getChildren().setAll(newContent);
                        
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newContent);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.setOnFinished(ev -> isTransitioning = false);
                        fadeIn.play();
                    });
                    
                    fadeOut.play();
                } else {
                    // First load - just fade in
                    contentArea.getChildren().setAll(newContent);
                    newContent.setOpacity(0.0);
                    
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newContent);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.setOnFinished(e -> isTransitioning = false);
                    fadeIn.play();
                }
            } else {
                System.err.println("❌ Loaded content is not a Node: " + loadedContent.getClass());
                isTransitioning = false;
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading view: " + fxmlPath);
            e.printStackTrace();
            // Try to show a placeholder
            Label errorLabel = new Label("View coming soon: " + title);
            errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #6b7280; -fx-padding: 50;");
            contentArea.getChildren().setAll(errorLabel);
            isTransitioning = false;
        }
    }

    /**
     * Highlight the active menu button with smooth animation
     */
    private void highlightButton(Button activeButton) {
        // Reset all buttons
        Button[] buttons = {dashboardBtn, advancedSearchBtn, bookingsBtn, scheduleBtn, paymentsBtn,
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
                
                // Add subtle scale animation for the active button
                if (btn == activeButton) {
                    ScaleTransition scale = new ScaleTransition(Duration.millis(200), btn);
                    scale.setFromX(0.98);
                    scale.setFromY(0.98);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.play();
                }
            }
        }
    }

    /**
     * Play entrance animations when dashboard loads
     */
    private void playEntranceAnimation() {
        // Animate the content area fade in
        if (contentArea != null) {
            contentArea.setOpacity(0.0);
            FadeTransition contentFade = new FadeTransition(Duration.millis(400), contentArea);
            contentFade.setFromValue(0.0);
            contentFade.setToValue(1.0);
            contentFade.setDelay(Duration.millis(200));
            contentFade.play();
        }

        // Animate sidebar buttons with staggered delay
        Button[] menuButtons = {dashboardBtn, bookingsBtn, scheduleBtn, paymentsBtn,
                               messagesBtn, flightTrackingBtn, aiAgentBtn, dealsBtn};
        
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i] != null) {
                Button btn = menuButtons[i];
                btn.setOpacity(0.0);
                btn.setTranslateX(-20);
                
                // Parallel fade + slide animation
                FadeTransition fade = new FadeTransition(Duration.millis(300), btn);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                
                TranslateTransition slide = new TranslateTransition(Duration.millis(300), btn);
                slide.setFromX(-20);
                slide.setToX(0);
                
                ParallelTransition parallel = new ParallelTransition(fade, slide);
                parallel.setDelay(Duration.millis(100 + (i * 50))); // Stagger each button by 50ms
                parallel.play();
            }
        }

        // Animate employee buttons if visible
        if (employeeProfileBtn != null && employeeProfileBtn.isVisible()) {
            animateEmployeeButtons();
        }
    }

    /**
     * Animate employee menu buttons
     */
    private void animateEmployeeButtons() {
        Button[] employeeButtons = {employeeProfileBtn, manageBookingsBtn, viewCustomersBtn};
        
        for (int i = 0; i < employeeButtons.length; i++) {
            if (employeeButtons[i] != null && employeeButtons[i].isVisible()) {
                Button btn = employeeButtons[i];
                btn.setOpacity(0.0);
                btn.setTranslateX(-20);
                
                FadeTransition fade = new FadeTransition(Duration.millis(300), btn);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                
                TranslateTransition slide = new TranslateTransition(Duration.millis(300), btn);
                slide.setFromX(-20);
                slide.setToX(0);
                
                ParallelTransition parallel = new ParallelTransition(fade, slide);
                parallel.setDelay(Duration.millis(500 + (i * 50))); // Start after main menu
                parallel.play();
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
