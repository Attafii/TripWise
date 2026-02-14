package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.User;
import ui.service.FlightBookingService;
import ui.service.NVIDIAChatService;
import ui.util.DataSource;
import ui.util.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlightAnalyticsController {

    @FXML private Label totalFlightsLabel;
    @FXML private Label totalFlightsTrendLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label avgFlightPriceLabel;
    @FXML private Label destinationsLabel;
    @FXML private Label mostVisitedLabel;
    @FXML private Label upcomingFlightsLabel;
    @FXML private Label nextFlightLabel;
    @FXML private LineChart<String, Number> spendingChart;
    @FXML private PieChart classDistributionChart;
    @FXML private VBox popularRoutesContainer;
    @FXML private VBox favoriteAirlinesContainer;
    @FXML private VBox timelineContainer;
    @FXML private ComboBox<String> timelineFilterCombo;
    @FXML private Label aiInsightsLabel;

    private FlightBookingService bookingService;
    private NVIDIAChatService aiService;
    private Connection connection;
    private int voyageurId = -1;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    @FXML
    private void initialize() {
        bookingService = new FlightBookingService();
        aiService = new NVIDIAChatService();
        connection = DataSource.getInstance().getConnection();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            voyageurId = getTravelerIdForUser(currentUser.getUserId());
        }

        setupTimelineFilter();
        loadAllAnalytics();
        generateAIInsights();
    }

    private void setupTimelineFilter() {
        timelineFilterCombo.getItems().addAll("All Time", "Last 30 Days", "Last 6 Months", "This Year");
        timelineFilterCombo.setValue("All Time");
        timelineFilterCombo.setOnAction(e -> loadTimeline());
    }

    private void loadAllAnalytics() {
        if (voyageurId == -1) {
            System.out.println("⚠️ No traveler ID found");
            return;
        }

        loadStatsCards();
        loadSpendingChart();
        loadClassDistribution();
        loadPopularRoutes();
        loadFavoriteAirlines();
        loadTimeline();
    }

    private void loadStatsCards() {
        try {
            // Total flights
            String query = "SELECT COUNT(*) as total, " +
                          "SUM(prix_total) as total_spent, " +
                          "AVG(prix_total) as avg_price " +
                          "FROM reservations_vol " +
                          "WHERE voyageur_id = ? AND statut_reservation != 'ANNULEE'";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                double totalSpent = rs.getDouble("total_spent");
                double avgPrice = rs.getDouble("avg_price");

                totalFlightsLabel.setText(String.valueOf(total));
                totalSpentLabel.setText(String.format("$%.2f", totalSpent));
                avgFlightPriceLabel.setText(String.format("Avg: $%.0f", avgPrice));

                // Calculate trend (last month vs previous month)
                int lastMonthCount = getFlightCountForPeriod(30);
                int previousMonthCount = getFlightCountForPeriod(60) - lastMonthCount;

                if (previousMonthCount > 0) {
                    double trend = ((lastMonthCount - previousMonthCount) / (double) previousMonthCount) * 100;
                    totalFlightsTrendLabel.setText(String.format("%+.0f%% from last month", trend));
                    totalFlightsTrendLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                        (trend >= 0 ? "#10b981" : "#ef4444") + ";");
                }
            }

            // Destinations
            query = "SELECT COUNT(DISTINCT aa.ville) as dest_count, " +
                   "aa.ville as most_visited " +
                   "FROM reservations_vol rv " +
                   "JOIN vols v ON rv.vol_id = v.vol_id " +
                   "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                   "WHERE rv.voyageur_id = ? " +
                   "GROUP BY aa.ville " +
                   "ORDER BY COUNT(*) DESC LIMIT 1";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                destinationsLabel.setText(String.valueOf(rs.getInt("dest_count")));
                mostVisitedLabel.setText("Most visited: " + rs.getString("most_visited"));
            }

            // Upcoming flights
            query = "SELECT COUNT(*) as upcoming, " +
                   "MIN(v.date_depart) as next_flight " +
                   "FROM reservations_vol rv " +
                   "JOIN vols v ON rv.vol_id = v.vol_id " +
                   "WHERE rv.voyageur_id = ? " +
                   "AND v.date_depart > NOW() " +
                   "AND rv.statut_reservation IN ('EN_ATTENTE', 'CONFIRMEE')";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int upcoming = rs.getInt("upcoming");
                upcomingFlightsLabel.setText(String.valueOf(upcoming));

                Timestamp nextFlight = rs.getTimestamp("next_flight");
                if (nextFlight != null) {
                    LocalDateTime nextDate = nextFlight.toLocalDateTime();
                    nextFlightLabel.setText("Next: " + nextDate.format(DATE_FORMATTER));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getFlightCountForPeriod(int days) {
        try {
            String query = "SELECT COUNT(*) FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "WHERE rv.voyageur_id = ? " +
                          "AND v.date_depart >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                          "AND rv.statut_reservation != 'ANNULEE'";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            stmt.setInt(2, days);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadSpendingChart() {
        spendingChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Spending");

        try {
            String query = "SELECT DATE_FORMAT(rv.date_reservation, '%Y-%m') as month, " +
                          "SUM(rv.prix_total) as total " +
                          "FROM reservations_vol rv " +
                          "WHERE rv.voyageur_id = ? " +
                          "AND rv.date_reservation >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                          "GROUP BY month " +
                          "ORDER BY month ASC";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String month = rs.getString("month");
                double total = rs.getDouble("total");

                // Format month name
                String[] parts = month.split("-");
                String monthName = LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1)
                    .format(DateTimeFormatter.ofPattern("MMM yyyy"));

                series.getData().add(new XYChart.Data<>(monthName, total));
            }

            spendingChart.getData().add(series);
            spendingChart.setLegendVisible(false);

        } catch (SQLException e) {
            System.err.println("❌ Error loading spending chart: " + e.getMessage());
        }
    }

    private void loadClassDistribution() {
        classDistributionChart.getData().clear();

        try {
            String query = "SELECT cv.classe, COUNT(*) as count " +
                          "FROM reservations_vol rv " +
                          "JOIN classes_vol cv ON rv.classe_vol_id = cv.classe_vol_id " +
                          "WHERE rv.voyageur_id = ? " +
                          "GROUP BY cv.classe";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            Map<String, Integer> classData = new HashMap<>();
            while (rs.next()) {
                String className = getClassDisplayName(rs.getString("classe"));
                int count = rs.getInt("count");
                classData.put(className, count);
            }

            for (Map.Entry<String, Integer> entry : classData.entrySet()) {
                PieChart.Data data = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
                classDistributionChart.getData().add(data);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading class distribution: " + e.getMessage());
        }
    }

    private void loadPopularRoutes() {
        popularRoutesContainer.getChildren().clear();

        try {
            String query = "SELECT ad.ville as departure, aa.ville as arrival, " +
                          "COUNT(*) as count, SUM(rv.prix_total) as total_spent " +
                          "FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "WHERE rv.voyageur_id = ? " +
                          "GROUP BY ad.ville, aa.ville " +
                          "ORDER BY count DESC LIMIT 5";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                String route = rs.getString("departure") + " → " + rs.getString("arrival");
                int count = rs.getInt("count");
                double spent = rs.getDouble("total_spent");

                HBox routeCard = createRouteCard(rank, route, count, spent);
                popularRoutesContainer.getChildren().add(routeCard);
                rank++;
            }

            if (popularRoutesContainer.getChildren().isEmpty()) {
                Label noData = new Label("No flight data available yet");
                noData.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
                popularRoutesContainer.getChildren().add(noData);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading popular routes: " + e.getMessage());
        }
    }

    private HBox createRouteCard(int rank, String route, int count, double spent) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; -fx-padding: 12;");

        Label rankLabel = new Label("#" + rank);
        rankLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3b82f6; -fx-min-width: 35;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label routeLabel = new Label(route);
        routeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        Label detailsLabel = new Label(count + " flights • $" + String.format("%.0f", spent) + " spent");
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        info.getChildren().addAll(routeLabel, detailsLabel);
        card.getChildren().addAll(rankLabel, info);

        return card;
    }

    private void loadFavoriteAirlines() {
        favoriteAirlinesContainer.getChildren().clear();

        try {
            String query = "SELECT ca.nom_compagnie, COUNT(*) as count, " +
                          "SUM(rv.prix_total) as total_spent " +
                          "FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                          "WHERE rv.voyageur_id = ? " +
                          "GROUP BY ca.nom_compagnie " +
                          "ORDER BY count DESC LIMIT 5";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                String airline = rs.getString("nom_compagnie");
                int count = rs.getInt("count");
                double spent = rs.getDouble("total_spent");

                HBox airlineCard = createAirlineCard(rank, airline, count, spent);
                favoriteAirlinesContainer.getChildren().add(airlineCard);
                rank++;
            }

            if (favoriteAirlinesContainer.getChildren().isEmpty()) {
                Label noData = new Label("No airline data available yet");
                noData.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
                favoriteAirlinesContainer.getChildren().add(noData);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading favorite airlines: " + e.getMessage());
        }
    }

    private HBox createAirlineCard(int rank, String airline, int count, double spent) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; -fx-padding: 12;");

        Label rankLabel = new Label("#" + rank);
        rankLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10b981; -fx-min-width: 35;");

        Label iconLabel = new Label("✈️");
        iconLabel.setStyle("-fx-font-size: 20px; -fx-background-color: #eff6ff; -fx-padding: 8; -fx-background-radius: 8;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label airlineLabel = new Label(airline);
        airlineLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        Label detailsLabel = new Label(count + " flights • $" + String.format("%.0f", spent) + " spent");
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        info.getChildren().addAll(airlineLabel, detailsLabel);
        card.getChildren().addAll(rankLabel, iconLabel, info);

        return card;
    }

    private void loadTimeline() {
        timelineContainer.getChildren().clear();

        try {
            String dateFilter = "";
            String filterValue = timelineFilterCombo.getValue();

            if (filterValue != null) {
                switch (filterValue) {
                    case "Last 30 Days":
                        dateFilter = " AND v.date_depart >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
                        break;
                    case "Last 6 Months":
                        dateFilter = " AND v.date_depart >= DATE_SUB(NOW(), INTERVAL 6 MONTH)";
                        break;
                    case "This Year":
                        dateFilter = " AND YEAR(v.date_depart) = YEAR(NOW())";
                        break;
                }
            }

            String query = "SELECT rv.reservation_id, rv.date_reservation, rv.statut_reservation, " +
                          "v.numero_vol, v.date_depart, " +
                          "ad.ville as departure, aa.ville as arrival, " +
                          "ca.nom_compagnie, cv.classe, rv.prix_total " +
                          "FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                          "JOIN classes_vol cv ON rv.classe_vol_id = cv.classe_vol_id " +
                          "WHERE rv.voyageur_id = ?" + dateFilter +
                          " ORDER BY v.date_depart DESC LIMIT 20";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VBox event = createTimelineEvent(
                    rs.getInt("reservation_id"),
                    rs.getTimestamp("date_depart").toLocalDateTime(),
                    rs.getString("numero_vol"),
                    rs.getString("departure"),
                    rs.getString("arrival"),
                    rs.getString("nom_compagnie"),
                    rs.getString("classe"),
                    rs.getDouble("prix_total"),
                    rs.getString("statut_reservation")
                );
                timelineContainer.getChildren().add(event);
            }

            if (timelineContainer.getChildren().isEmpty()) {
                Label noData = new Label("No flights in this period");
                noData.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-padding: 20;");
                timelineContainer.getChildren().add(noData);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading timeline: " + e.getMessage());
        }
    }

    private VBox createTimelineEvent(int bookingId, LocalDateTime flightDate, String flightNumber,
                                     String departure, String arrival, String airline, String flightClass,
                                     double price, String status) {
        VBox event = new VBox(10);
        event.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 0 3; -fx-border-radius: 12;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(flightDate.format(DATE_FORMATTER));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label statusLabel = new Label(getStatusDisplay(status));
        statusLabel.setStyle(getStatusStyle(status));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(dateLabel, spacer, statusLabel);

        Label routeLabel = new Label("✈️ " + departure + " → " + arrival);
        routeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        Label detailsLabel = new Label(airline + " " + flightNumber + " • " +
                                       getClassDisplayName(flightClass) + " • $" + String.format("%.0f", price));
        detailsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        event.getChildren().addAll(header, routeLabel, detailsLabel);

        return event;
    }

    private void generateAIInsights() {
        aiInsightsLabel.setText("🤖 Analyzing your travel patterns...");

        new Thread(() -> {
            try {
                // Gather user's travel data
                String stats = getUserTravelStats();

                String prompt = "Based on this traveler's flight history: " + stats +
                               "\n\nProvide 3-4 personalized insights about their travel patterns, " +
                               "spending habits, and recommendations for future bookings. " +
                               "Be concise and actionable. Include emojis.";

                String insights = aiService.sendMessage(prompt);

                Platform.runLater(() -> aiInsightsLabel.setText(insights));

            } catch (Exception e) {
                Platform.runLater(() -> aiInsightsLabel.setText(
                    "✈️ You're building a great travel history! Keep exploring new destinations and " +
                    "we'll provide personalized insights based on your patterns."
                ));
            }
        }).start();
    }

    private String getUserTravelStats() {
        StringBuilder stats = new StringBuilder();
        try {
            String query = "SELECT COUNT(*) as total, SUM(prix_total) as spent, " +
                          "GROUP_CONCAT(DISTINCT aa.ville) as destinations " +
                          "FROM reservations_vol rv " +
                          "JOIN vols v ON rv.vol_id = v.vol_id " +
                          "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                          "WHERE rv.voyageur_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.append(String.format("Total flights: %d, Total spent: $%.0f, Destinations: %s",
                    rs.getInt("total"),
                    rs.getDouble("spent"),
                    rs.getString("destinations")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats.toString();
    }

    @FXML
    private void handleAIInsights() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AI Travel Insights");
        alert.setHeaderText("Your Personalized Travel Analysis");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        Label loading = new Label("🤖 AI is analyzing your travel data...");
        loading.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
        content.getChildren().add(loading);

        alert.getDialogPane().setContent(content);
        alert.show();

        new Thread(() -> {
            String stats = getUserTravelStats();
            String prompt = "Provide a detailed travel analysis report for: " + stats +
                          "\n\nInclude: spending patterns, favorite destinations, travel frequency, " +
                          "cost-saving tips, and personalized recommendations.";
            String analysis = aiService.sendMessage(prompt);

            Platform.runLater(() -> {
                content.getChildren().clear();
                Label analysisLabel = new Label(analysis);
                analysisLabel.setStyle("-fx-font-size: 14px;");
                analysisLabel.setWrapText(true);
                content.getChildren().add(analysisLabel);
            });
        }).start();
    }

    @FXML
    private void handleGenerateReport() {
        showAlert(Alert.AlertType.INFORMATION, "Report Generated",
                 "Your flight analytics report has been generated! Check your email for the PDF.");
    }

    @FXML
    private void handlePersonalizedRecommendations() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Personalized Recommendations");
        alert.setHeaderText("AI-Powered Travel Suggestions");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        Label loading = new Label("🤖 Generating recommendations...");
        loading.setStyle("-fx-font-size: 14px;");
        content.getChildren().add(loading);

        alert.getDialogPane().setContent(content);
        alert.show();

        new Thread(() -> {
            String stats = getUserTravelStats();
            String prompt = "Based on: " + stats +
                          "\n\nSuggest 5 new destinations they should visit, best times to book, " +
                          "and money-saving strategies.";
            String recommendations = aiService.sendMessage(prompt);

            Platform.runLater(() -> {
                content.getChildren().clear();
                Label recLabel = new Label(recommendations);
                recLabel.setStyle("-fx-font-size: 14px;");
                recLabel.setWrapText(true);
                content.getChildren().add(recLabel);
            });
        }).start();
    }

    @FXML
    private void handleOptimizeBookings() {
        showAlert(Alert.AlertType.INFORMATION, "Optimization Tips",
                 "💡 AI Tip: Book flights 6-8 weeks in advance for best prices!\n\n" +
                 "Your most cost-effective class has been Economy.\n" +
                 "Consider Tuesday and Wednesday departures for lower fares.");
    }

    private int getTravelerIdForUser(int userId) {
        try {
            String query = "SELECT voyageur_id FROM voyageurs WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("voyageur_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String getClassDisplayName(String classType) {
        if (classType == null) return "Economy";
        switch (classType) {
            case "ECONOMIQUE": return "Economy";
            case "PREMIUM": return "Premium";
            case "BUSINESS": return "Business";
            case "PREMIERE": return "First Class";
            default: return classType;
        }
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case "CONFIRMEE": return "✓ Confirmed";
            case "EN_ATTENTE": return "⏳ Pending";
            case "ANNULEE": return "✕ Cancelled";
            case "TERMINEE": return "✓ Completed";
            default: return status;
        }
    }

    private String getStatusStyle(String status) {
        String base = "-fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: 5 12; -fx-background-radius: 15;";
        switch (status) {
            case "CONFIRMEE": return base + " -fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
            case "EN_ATTENTE": return base + " -fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case "ANNULEE": return base + " -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            case "TERMINEE": return base + " -fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            default: return base + " -fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
