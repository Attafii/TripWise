package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.User;
import ui.util.DataSource;
import ui.util.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentsController {

    @FXML private Label totalSpentLabel;
    @FXML private Label pendingPaymentsLabel;
    @FXML private Label completedPaymentsLabel;
    @FXML private Label thisMonthLabel;
    @FXML private VBox transactionsContainer;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ToggleButton allPaymentsBtn, flightsBtn, hotelsBtn, carsBtn;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private String currentFilter = "ALL";

    @FXML
    private void initialize() {
        sortCombo.getItems().addAll("Date (Newest)", "Date (Oldest)", "Amount (High)", "Amount (Low)");
        sortCombo.setValue("Date (Newest)");
        sortCombo.setOnAction(e -> loadTransactions());

        loadPaymentStats();
        loadTransactions();
    }

    @FXML
    private void refreshPayments() {
        loadPaymentStats();
        loadTransactions();
    }

    @FXML private void filterAll() { currentFilter = "ALL"; updateFilterStyles(); loadTransactions(); }
    @FXML private void filterFlights() { currentFilter = "VOL"; updateFilterStyles(); loadTransactions(); }
    @FXML private void filterHotels() { currentFilter = "HOTEL"; updateFilterStyles(); loadTransactions(); }
    @FXML private void filterCars() { currentFilter = "VEHICULE"; updateFilterStyles(); loadTransactions(); }

    private void updateFilterStyles() {
        String active = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 20;";
        String inactive = "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-background-radius: 8; -fx-padding: 8 20;";

        allPaymentsBtn.setStyle(currentFilter.equals("ALL") ? active : inactive);
        flightsBtn.setStyle(currentFilter.equals("VOL") ? active : inactive);
        hotelsBtn.setStyle(currentFilter.equals("HOTEL") ? active : inactive);
        carsBtn.setStyle(currentFilter.equals("VEHICULE") ? active : inactive);
    }

    private void loadPaymentStats() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        int voyageurId = getVoyageurId(currentUser.getUserId());
        if (voyageurId == -1) return;

        try (Connection conn = DataSource.getInstance().getConnection()) {
            double totalSpent = 0;
            int pending = 0;
            int completed = 0;
            double thisMonth = 0;

            // Flight bookings
            String flightQuery = "SELECT prix_total, statut_reservation, date_reservation FROM reservations_vol WHERE voyageur_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(flightQuery)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    double amount = rs.getDouble("prix_total");
                    String status = rs.getString("statut_reservation");
                    LocalDateTime date = rs.getTimestamp("date_reservation").toLocalDateTime();

                    if (status.equals("CONFIRMEE") || status.equals("TERMINEE")) {
                        totalSpent += amount;
                        completed++;
                    } else if (status.equals("EN_ATTENTE")) {
                        pending++;
                    }

                    if (date.getMonth() == LocalDateTime.now().getMonth() &&
                        date.getYear() == LocalDateTime.now().getYear()) {
                        thisMonth += amount;
                    }
                }
            }

            // Hotel bookings
            String hotelQuery = "SELECT prix_total, statut_reservation, date_reservation FROM reservations_hotel WHERE voyageur_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(hotelQuery)) {
                stmt.setInt(1, voyageurId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    double amount = rs.getDouble("prix_total");
                    String status = rs.getString("statut_reservation");
                    LocalDateTime date = rs.getTimestamp("date_reservation").toLocalDateTime();

                    if (status.equals("CONFIRMEE") || status.equals("TERMINEE")) {
                        totalSpent += amount;
                        completed++;
                    } else if (status.equals("EN_ATTENTE")) {
                        pending++;
                    }

                    if (date.getMonth() == LocalDateTime.now().getMonth() &&
                        date.getYear() == LocalDateTime.now().getYear()) {
                        thisMonth += amount;
                    }
                }
            }

            totalSpentLabel.setText(String.format("%.2f USD", totalSpent));
            pendingPaymentsLabel.setText(String.valueOf(pending));
            completedPaymentsLabel.setText(String.valueOf(completed));
            thisMonthLabel.setText(String.format("%.2f USD", thisMonth));

        } catch (SQLException e) {
            System.err.println("Error loading payment stats: " + e.getMessage());
        }
    }

    private void loadTransactions() {
        // Clear existing content except header
        while (transactionsContainer.getChildren().size() > 2) {
            transactionsContainer.getChildren().remove(2);
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        int voyageurId = getVoyageurId(currentUser.getUserId());
        if (voyageurId == -1) return;

        try (Connection conn = DataSource.getInstance().getConnection()) {
            // Load flight transactions
            if (currentFilter.equals("ALL") || currentFilter.equals("VOL")) {
                loadFlightTransactions(conn, voyageurId);
            }

            // Load hotel transactions
            if (currentFilter.equals("ALL") || currentFilter.equals("HOTEL")) {
                loadHotelTransactions(conn, voyageurId);
            }

            if (transactionsContainer.getChildren().size() <= 2) {
                Label noTrans = new Label("No transactions found.");
                noTrans.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 20;");
                transactionsContainer.getChildren().add(noTrans);
            }

        } catch (SQLException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
    }

    private void loadFlightTransactions(Connection conn, int voyageurId) throws SQLException {
        String query = "SELECT rv.reservation_id, rv.numero_confirmation, rv.prix_total, " +
                "rv.statut_reservation, rv.date_reservation, " +
                "ad.ville as depart, aa.ville as arrival, v.numero_vol " +
                "FROM reservations_vol rv " +
                "JOIN vols v ON rv.vol_id = v.vol_id " +
                "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                "WHERE rv.voyageur_id = ? ORDER BY rv.date_reservation DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HBox card = createTransactionCard(
                    "Flight",
                    rs.getString("depart") + " → " + rs.getString("arrival"),
                    rs.getString("numero_vol"),
                    rs.getDouble("prix_total"),
                    rs.getString("statut_reservation"),
                    rs.getTimestamp("date_reservation").toLocalDateTime(),
                    rs.getString("numero_confirmation")
                );
                transactionsContainer.getChildren().add(card);
            }
        }
    }

    private void loadHotelTransactions(Connection conn, int voyageurId) throws SQLException {
        String query = "SELECT rh.reservation_id, rh.numero_confirmation, rh.prix_total, " +
                "rh.statut_reservation, rh.date_reservation, " +
                "h.nom_hotel, h.ville " +
                "FROM reservations_hotel rh " +
                "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                "WHERE rh.voyageur_id = ? ORDER BY rh.date_reservation DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, voyageurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HBox card = createTransactionCard(
                    "Hotel",
                    rs.getString("nom_hotel"),
                    rs.getString("ville"),
                    rs.getDouble("prix_total"),
                    rs.getString("statut_reservation"),
                    rs.getTimestamp("date_reservation").toLocalDateTime(),
                    rs.getString("numero_confirmation")
                );
                transactionsContainer.getChildren().add(card);
            }
        }
    }

    private HBox createTransactionCard(String type, String title, String subtitle,
                                        double amount, String status, LocalDateTime date, String confirmation) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        // Icon
        Label icon = new Label(type.equals("Flight") ? "✈️" : type.equals("Hotel") ? "🏨" : "🚗");
        icon.setStyle("-fx-font-size: 28px; -fx-background-color: " +
                     (type.equals("Flight") ? "#eff6ff" : type.equals("Hotel") ? "#f0fdf4" : "#fef3c7") +
                     "; -fx-padding: 12; -fx-background-radius: 10;");

        // Info
        VBox infoBox = new VBox(3);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitleLabel = new Label(subtitle + " • " + date.format(DATE_FORMATTER));
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Label confLabel = new Label("Ref: " + (confirmation != null ? confirmation : "N/A"));
        confLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        infoBox.getChildren().addAll(titleLabel, subtitleLabel, confLabel);

        // Amount and Status
        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        Label amountLabel = new Label(String.format("%.2f USD", amount));
        amountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        String statusText = status.equals("CONFIRMEE") ? "Paid" :
                           status.equals("EN_ATTENTE") ? "Pending" :
                           status.equals("ANNULEE") ? "Cancelled" : "Completed";
        String statusColor = status.equals("CONFIRMEE") || status.equals("TERMINEE") ? "#10b981" :
                            status.equals("EN_ATTENTE") ? "#f59e0b" : "#ef4444";

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + statusColor + ";");

        rightBox.getChildren().addAll(amountLabel, statusLabel);

        card.getChildren().addAll(icon, infoBox, rightBox);
        return card;
    }

    private int getVoyageurId(int userId) {
        try (Connection conn = DataSource.getInstance().getConnection()) {
            String query = "SELECT voyageur_id FROM voyageurs WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("voyageur_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting voyageur_id: " + e.getMessage());
        }
        return -1;
    }
}
