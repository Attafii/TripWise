package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import ui.model.User;
import ui.util.SessionManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ScheduleController - Role-specific calendar and schedule views
 * Admin: System-wide flight schedules
 * Employee: Work schedule and assigned bookings
 * Traveler: Personal trip calendar
 */
public class ScheduleController {

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox upcomingEventsBox;
    @FXML private Label stat1Label;
    @FXML private Label stat1Title;
    @FXML private Label stat2Label;
    @FXML private Label stat2Title;
    @FXML private Label stat3Label;
    @FXML private Label stat3Title;

    private User.UserType userRole;
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ ERROR: No user in session!");
            return;
        }
        
        userRole = currentUser.getUserType();
        System.out.println("✅ Schedule - User Role: " + userRole);
        
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        
        loadRoleSpecificSchedule();
        updateCalendar();
    }

    private void loadRoleSpecificSchedule() {
        if (userRole == null) {
            loadTravelerSchedule();
            return;
        }
        
        switch (userRole) {
            case ADMIN:
            case RESPONSABLE:
                loadAdminSchedule();
                break;
            case EMPLOYE:
                loadEmployeeSchedule();
                break;
            case VOYAGEUR:
            case VISITEUR:
            default:
                loadTravelerSchedule();
                break;
        }
    }

    private void loadAdminSchedule() {
        pageTitle.setText("Flight Schedule Management");
        pageSubtitle.setText("Manage system-wide flight schedules and bookings");
        
        stat1Title.setText("Total Flights Today");
        stat1Label.setText("47");
        stat2Title.setText("Scheduled This Week");
        stat2Label.setText("315");
        stat3Title.setText("Crew Members On Duty");
        stat3Label.setText("128");
        
        loadAdminEvents();
    }

    private void loadEmployeeSchedule() {
        pageTitle.setText("Work Schedule");
        pageSubtitle.setText("Your assigned shifts and booking tasks");
        
        stat1Title.setText("Shifts This Week");
        stat1Label.setText("5");
        stat2Title.setText("Assigned Bookings");
        stat2Label.setText("18");
        stat3Title.setText("Meetings Scheduled");
        stat3Label.setText("3");
        
        loadEmployeeEvents();
    }

    private void loadTravelerSchedule() {
        pageTitle.setText("My Travel Calendar");
        pageSubtitle.setText("View your upcoming trips and bookings");
        
        stat1Title.setText("Upcoming Trips");
        stat1Label.setText("3");
        stat2Title.setText("Days Until Next Trip");
        stat2Label.setText("12");
        stat3Title.setText("Countries Visiting");
        stat3Label.setText("2");
        
        loadTravelerEvents();
    }

    private void loadAdminEvents() {
        upcomingEventsBox.getChildren().clear();
        
        // Sample admin events
        addEventCard("AA 1234 - Departure", "New York → Los Angeles", "Today, 10:30 AM", "#3b82f6");
        addEventCard("DL 5678 - Departure", "Chicago → Miami", "Today, 2:15 PM", "#3b82f6");
        addEventCard("UA 9012 - Arrival", "San Francisco → Seattle", "Tomorrow, 8:00 AM", "#10b981");
        addEventCard("SW 3456 - Maintenance", "Aircraft Inspection", "Dec 22, 9:00 AM", "#f59e0b");
        addEventCard("BA 7890 - Departure", "Boston → London", "Dec 25, 7:45 PM", "#3b82f6");
    }

    private void loadEmployeeEvents() {
        upcomingEventsBox.getChildren().clear();
        
        // Sample employee events
        addEventCard("Morning Shift", "Check-in Counter 3", "Today, 8:00 AM - 4:00 PM", "#8b5cf6");
        addEventCard("Process Booking #BK089", "Hotel Confirmation Required", "Today, 11:00 AM", "#f59e0b");
        addEventCard("Team Meeting", "Weekly Operations Review", "Tomorrow, 2:00 PM", "#3b82f6");
        addEventCard("Customer Support", "Handle Priority Inquiries", "Dec 20, 9:00 AM", "#10b981");
        addEventCard("Evening Shift", "Boarding Gate 7", "Dec 21, 4:00 PM - 12:00 AM", "#8b5cf6");
    }

    private void loadTravelerEvents() {
        upcomingEventsBox.getChildren().clear();
        
        // Sample traveler events
        addEventCard("Flight to New York", "AA 1234 - Business Class", "Dec 15, 10:30 AM", "#3b82f6");
        addEventCard("Hotel Check-in", "Marriott Manhattan - 3 nights", "Dec 15, 3:00 PM", "#10b981");
        addEventCard("Return Flight", "AA 5432 - New York → Home", "Dec 18, 6:45 PM", "#3b82f6");
        addEventCard("Flight to Paris", "AF 007 - Economy", "Dec 25, 8:00 AM", "#3b82f6");
        addEventCard("Paris Hotel", "Hotel Eiffel - 5 nights", "Dec 25, 2:00 PM", "#10b981");
    }

    private void addEventCard(String title, String subtitle, String time, String color) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; " +
                     "-fx-border-color: " + color + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        
        Label timeLabel = new Label("🕐 " + time);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        
        card.getChildren().addAll(titleLabel, subtitleLabel, timeLabel);
        upcomingEventsBox.getChildren().add(card);
    }

    private void updateCalendar() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarGrid.getChildren().clear();
        
        // Add day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #6b7280; -fx-font-size: 12px;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(60);
            dayLabel.setPrefHeight(30);
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // Get first day of month
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        
        // Add calendar days
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 1;
        int col = dayOfWeek;
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setPrefWidth(60);
            dayButton.setPrefHeight(50);
            
            String style = "-fx-background-color: white; -fx-text-fill: #1f2937; " +
                          "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;";
            
            // Highlight today
            if (date.equals(LocalDate.now())) {
                style = "-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                       "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 700; -fx-font-size: 14px;";
            }
            
            // Add event indicators (sample)
            if (day == 15 || day == 18 || day == 22 || day == 25) {
                dayButton.setText(day + "\n•");
                style += " -fx-font-size: 12px;";
            }
            
            dayButton.setStyle(style);
            
            final LocalDate clickedDate = date;
            dayButton.setOnAction(e -> handleDateClick(clickedDate));
            
            calendarGrid.add(dayButton, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private void handleDateClick(LocalDate date) {
        selectedDate = date;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Date Selected");
        alert.setHeaderText(date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        alert.setContentText("Events scheduled for this date will be shown here.");
        alert.showAndWait();
    }

    @FXML
    private void handlePreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleToday() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateCalendar();
    }

    @FXML
    private void handleAddEvent() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Event");
        alert.setHeaderText("Create New Event");
        alert.setContentText("Event creation dialog will be implemented here.");
        alert.showAndWait();
    }
}
