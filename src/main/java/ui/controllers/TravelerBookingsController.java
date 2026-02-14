package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.model.User;
import ui.service.EmailNotificationService;
import ui.service.FlightBookingService;
import ui.service.HotelBookingService;
import ui.service.PDFExportService;
import ui.util.DataSource;
import ui.util.SceneManager;
import ui.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TravelerBookingsController {

    @FXML private Button backBtn;
    @FXML private VBox bookingsContainer;
    @FXML private Label noBookingsLabel;
    @FXML private ToggleButton allBtn;
    @FXML private ToggleButton flightsBtn;
    @FXML private ToggleButton hotelsBtn;
    @FXML private ToggleButton carsBtn;
    @FXML private ComboBox<String> statusFilter;

    private FlightBookingService flightBookingService;
    private HotelBookingService hotelBookingService;
    private int voyageurId = -1;
    private ToggleGroup filterGroup;
    private String currentFilter = "all";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private static final String ACTIVE_BTN = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;";
    private static final String INACTIVE_BTN = "-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 20; -fx-padding: 8 20; -fx-border-color: #e5e7eb; -fx-border-radius: 20; -fx-cursor: hand;";

    @FXML
    private void initialize() {
        flightBookingService = new FlightBookingService();
        hotelBookingService = new HotelBookingService();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            voyageurId = getTravelerIdForUser(currentUser.getUserId());
        }

        setupFilters();
        loadAllBookings();
    }

    private void setupFilters() {
        filterGroup = new ToggleGroup();
        allBtn.setToggleGroup(filterGroup);
        flightsBtn.setToggleGroup(filterGroup);
        hotelsBtn.setToggleGroup(filterGroup);
        carsBtn.setToggleGroup(filterGroup);
        allBtn.setSelected(true);

        filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonStyles();
            if (allBtn.isSelected()) currentFilter = "all";
            else if (flightsBtn.isSelected()) currentFilter = "flights";
            else if (hotelsBtn.isSelected()) currentFilter = "hotels";
            else if (carsBtn.isSelected()) currentFilter = "cars";
            loadAllBookings();
        });

        statusFilter.getItems().addAll("All Statuses", "Confirmed", "Pending", "Cancelled", "Completed");
        statusFilter.setValue("All Statuses");
        statusFilter.setOnAction(e -> loadAllBookings());
    }

    /**
     * Handle back button - navigate back to book flight page with sidebar
     */
    @FXML
    private void handleBack() {
        try {
            // Try to navigate within the dashboard BorderPane (preserves sidebar)
            if (bookingsContainer != null && bookingsContainer.getScene() != null) {
                javafx.scene.Parent root = bookingsContainer.getScene().getRoot();

                // Check if we're inside a BorderPane (dashboard layout)
                if (root instanceof javafx.scene.layout.BorderPane) {
                    javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;

                    // Load the book-flight page into the center
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/ui/book-flight-new.fxml")
                    );
                    javafx.scene.Parent bookFlightPane = loader.load();
                    borderPane.setCenter(bookFlightPane);

                    System.out.println("✅ Navigated back to Book Flight page");
                    return;
                }
            }

            // Fallback: switch entire scene to dashboard
            SceneManager.switchScene("/ui/dashboard.fxml");

        } catch (Exception e) {
            System.err.println("❌ Error navigating back: " + e.getMessage());
            e.printStackTrace();
            // Last resort fallback
            try {
                SceneManager.switchScene("/ui/dashboard.fxml");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Handle refresh button - reload all bookings
     */
    @FXML
    private void handleRefresh() {
        loadAllBookings();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Bookings have been refreshed.");
    }

    private void updateButtonStyles() {
        allBtn.setStyle(allBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
        flightsBtn.setStyle(flightsBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
        hotelsBtn.setStyle(hotelsBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
        carsBtn.setStyle(carsBtn.isSelected() ? ACTIVE_BTN : INACTIVE_BTN);
    }

    private void loadAllBookings() {
        bookingsContainer.getChildren().clear();
        List<BookingInfo> bookings = new ArrayList<>();

        String statusFilterValue = getStatusFilterValue();

        if (currentFilter.equals("all") || currentFilter.equals("flights")) {
            bookings.addAll(loadFlightBookings(statusFilterValue));
        }
        if (currentFilter.equals("all") || currentFilter.equals("hotels")) {
            bookings.addAll(loadHotelBookings(statusFilterValue));
        }
        if (currentFilter.equals("all") || currentFilter.equals("cars")) {
            bookings.addAll(loadCarRentals(statusFilterValue));
        }

        // Sort by date descending
        bookings.sort((a, b) -> b.bookingDate.compareTo(a.bookingDate));

        if (bookings.isEmpty()) {
            bookingsContainer.getChildren().add(noBookingsLabel);
        } else {
            for (BookingInfo booking : bookings) {
                bookingsContainer.getChildren().add(createBookingCard(booking));
            }
        }
    }

    private String getStatusFilterValue() {
        String selected = statusFilter.getValue();
        if (selected == null || selected.equals("All Statuses")) return null;
        switch (selected) {
            case "Confirmed": return "CONFIRMEE";
            case "Pending": return "EN_ATTENTE";
            case "Cancelled": return "ANNULEE";
            case "Completed": return "TERMINEE";
            default: return null;
        }
    }

    private List<BookingInfo> loadFlightBookings(String statusFilter) {
        List<BookingInfo> bookings = new ArrayList<>();
        try {
            Connection conn = DataSource.getInstance().getConnection();
            StringBuilder query = new StringBuilder(
                "SELECT rv.reservation_id, rv.date_reservation, rv.prix_total, rv.statut_reservation, " +
                "rv.numero_confirmation, rv.nombre_passagers, rv.sieges_attribues, " +
                "v.numero_vol, v.date_depart, v.date_arrivee, " +
                "ca.nom_compagnie, cv.type_classe as classe, " +
                "ad.ville as ville_depart, aa.ville as ville_arrivee " +
                "FROM reservations_vol rv " +
                "JOIN vols v ON rv.vol_id = v.vol_id " +
                "JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id " +
                "JOIN classes_vol cv ON rv.classe_id = cv.classe_id " +
                "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                "WHERE rv.voyageur_id = ? "
            );

            if (statusFilter != null) {
                query.append("AND rv.statut_reservation = ? ");
            }
            query.append("ORDER BY rv.date_reservation DESC");

            PreparedStatement stmt = conn.prepareStatement(query.toString());
            stmt.setInt(1, voyageurId);
            if (statusFilter != null) {
                stmt.setString(2, statusFilter);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BookingInfo booking = new BookingInfo();
                booking.id = rs.getInt("reservation_id");
                booking.type = "Flight";
                booking.icon = "✈️";
                booking.title = rs.getString("ville_depart") + " → " + rs.getString("ville_arrivee");
                booking.subtitle = rs.getString("nom_compagnie") + " " + rs.getString("numero_vol");
                booking.confirmationNumber = rs.getString("numero_confirmation");
                booking.status = rs.getString("statut_reservation");
                booking.price = rs.getDouble("prix_total");
                booking.bookingDate = rs.getTimestamp("date_reservation").toLocalDateTime();
                booking.tripDate = rs.getTimestamp("date_depart").toLocalDateTime();

                // Include seats if available
                String seats = rs.getString("sieges_attribues");
                String seatsInfo = (seats != null && !seats.isEmpty()) ? " • Seats: " + seats : "";
                booking.details = rs.getString("classe") + " • " + rs.getInt("nombre_passagers") + " passenger(s)" + seatsInfo;
                bookings.add(booking);
            }
            System.out.println("✅ Loaded " + bookings.size() + " flight bookings for voyageur_id: " + voyageurId);
        } catch (Exception e) {
            System.err.println("❌ Error loading flight bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    private List<BookingInfo> loadHotelBookings(String statusFilter) {
        List<BookingInfo> bookings = new ArrayList<>();
        try {
            Connection conn = DataSource.getInstance().getConnection();
            StringBuilder query = new StringBuilder(
                "SELECT rh.reservation_id, rh.date_reservation, rh.prix_total, rh.statut_reservation, " +
                "rh.numero_confirmation, rh.date_checkin, rh.date_checkout, rh.nombre_nuits, " +
                "rh.nombre_adultes, rh.nombre_enfants, " +
                "h.nom_hotel, h.ville, c.type_chambre " +
                "FROM reservations_hotel rh " +
                "JOIN hotels h ON rh.hotel_id = h.hotel_id " +
                "JOIN chambres c ON rh.chambre_id = c.chambre_id " +
                "WHERE rh.voyageur_id = ? "
            );

            if (statusFilter != null) {
                query.append("AND rh.statut_reservation = ? ");
            }
            query.append("ORDER BY rh.date_reservation DESC");

            PreparedStatement stmt = conn.prepareStatement(query.toString());
            stmt.setInt(1, voyageurId);
            if (statusFilter != null) {
                stmt.setString(2, statusFilter);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BookingInfo booking = new BookingInfo();
                booking.id = rs.getInt("reservation_id");
                booking.type = "Hotel";
                booking.icon = "🏨";
                booking.title = rs.getString("nom_hotel");
                booking.subtitle = rs.getString("ville");
                booking.confirmationNumber = rs.getString("numero_confirmation");
                booking.status = rs.getString("statut_reservation");
                booking.price = rs.getDouble("prix_total");
                booking.bookingDate = rs.getTimestamp("date_reservation").toLocalDateTime();
                booking.tripDate = rs.getDate("date_checkin").toLocalDate().atStartOfDay();
                booking.endDate = rs.getDate("date_checkout").toLocalDate();
                int guests = rs.getInt("nombre_adultes") + rs.getInt("nombre_enfants");
                booking.details = rs.getString("type_chambre") + " • " + rs.getInt("nombre_nuits") +
                                 " nights • " + guests + " guest(s)";
                bookings.add(booking);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading hotel bookings: " + e.getMessage());
        }
        return bookings;
    }

    private List<BookingInfo> loadCarRentals(String statusFilter) {
        List<BookingInfo> bookings = new ArrayList<>();
        try {
            Connection conn = DataSource.getInstance().getConnection();
            StringBuilder query = new StringBuilder(
                "SELECT rl.location_id, rl.date_reservation, rl.prix_total, rl.statut_location, " +
                "rl.date_debut, rl.date_fin, " +
                "v.marque, v.modele, v.categorie, " +
                "cl.nom_compagnie " +
                "FROM reservations_vehicule rl " +
                "JOIN vehicules v ON rl.vehicule_id = v.vehicule_id " +
                "LEFT JOIN compagnies_location cl ON v.compagnie_id = cl.compagnie_id " +
                "WHERE rl.voyageur_id = ? "
            );

            if (statusFilter != null) {
                query.append("AND rl.statut_location = ? ");
            }
            query.append("ORDER BY rl.date_reservation DESC");

            PreparedStatement stmt = conn.prepareStatement(query.toString());
            stmt.setInt(1, voyageurId);
            if (statusFilter != null) {
                stmt.setString(2, statusFilter);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BookingInfo booking = new BookingInfo();
                booking.id = rs.getInt("location_id");
                booking.type = "Car";
                booking.icon = "🚗";
                booking.title = rs.getString("marque") + " " + rs.getString("modele");
                booking.subtitle = rs.getString("nom_compagnie");
                booking.status = rs.getString("statut_location");
                booking.price = rs.getDouble("prix_total");
                booking.bookingDate = rs.getTimestamp("date_reservation").toLocalDateTime();
                booking.tripDate = rs.getDate("date_debut").toLocalDate().atStartOfDay();
                booking.endDate = rs.getDate("date_fin").toLocalDate();
                booking.details = rs.getString("categorie");
                bookings.add(booking);
            }
        } catch (Exception e) {
            // Table might not exist yet
            System.out.println("⚠️ Car rentals table not available or empty");
        }
        return bookings;
    }

    private VBox createBookingCard(BookingInfo booking) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Header row
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon and type
        Label icon = new Label(booking.icon);
        icon.setStyle("-fx-font-size: 32px; -fx-background-color: #f3f4f6; -fx-padding: 15; -fx-background-radius: 12;");

        VBox titleBox = new VBox(3);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label title = new Label(booking.title);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitle = new Label(booking.subtitle);
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");

        if (booking.confirmationNumber != null) {
            Label confirmation = new Label("Confirmation: " + booking.confirmationNumber);
            confirmation.setStyle("-fx-font-size: 12px; -fx-text-fill: #3b82f6;");
            titleBox.getChildren().addAll(title, subtitle, confirmation);
        } else {
            titleBox.getChildren().addAll(title, subtitle);
        }

        // Status and price
        VBox rightSection = new VBox(5);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        Label statusLabel = new Label(getStatusDisplay(booking.status));
        String statusStyle = getStatusStyle(booking.status);
        statusLabel.setStyle(statusStyle);

        Label priceLabel = new Label(String.format("$%.0f", booking.price));
        priceLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        rightSection.getChildren().addAll(statusLabel, priceLabel);

        header.getChildren().addAll(icon, titleBox, rightSection);

        // Details row
        HBox details = new HBox(25);
        details.setStyle("-fx-padding: 15 0 0 0; -fx-border-color: #f3f4f6 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        // Trip date
        VBox dateBox = new VBox(2);
        Label dateLabel = new Label("📅 " + booking.tripDate.format(DATE_FORMATTER));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        if (booking.endDate != null) {
            Label endDateLabel = new Label("to " + booking.endDate.format(DATE_FORMATTER));
            endDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
            dateBox.getChildren().addAll(dateLabel, endDateLabel);
        } else {
            dateBox.getChildren().add(dateLabel);
        }

        // Details
        Label detailsLabel = new Label("📋 " + booking.details);
        detailsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        // Booking date
        Label bookingDateLabel = new Label("Booked: " + booking.bookingDate.format(DATE_FORMATTER));
        bookingDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

        details.getChildren().addAll(dateBox, detailsLabel, new Region(), bookingDateLabel);
        HBox.setHgrow(details.getChildren().get(2), Priority.ALWAYS);

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("View Details");
        viewBtn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> viewBookingDetails(booking));

        // Add Edit button for pending/confirmed bookings
        if (booking.type.equals("Flight") && (booking.status.equals("EN_ATTENTE") || booking.status.equals("CONFIRMEE"))) {
            // Add Select Seats button
            Button selectSeatsBtn = new Button("✈ Select Seats");
            selectSeatsBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            selectSeatsBtn.setOnAction(e -> openSeatSelection(booking));
            actions.getChildren().add(selectSeatsBtn);

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            editBtn.setOnAction(e -> editFlightBooking(booking));
            actions.getChildren().add(editBtn);
        }

        if (booking.status.equals("EN_ATTENTE") || booking.status.equals("CONFIRMEE")) {
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            cancelBtn.setOnAction(e -> cancelBooking(booking));
            actions.getChildren().add(cancelBtn);
        }

        actions.getChildren().add(viewBtn);

        card.getChildren().addAll(header, details, actions);
        return card;
    }

    private void editFlightBooking(BookingInfo booking) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Flight Booking");
        dialog.setHeaderText("Modify Your Booking Details");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // Flight info (read-only)
        Label flightInfo = new Label(String.format("Flight: %s\n%s", booking.subtitle, booking.title));
        flightInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        // Passengers (editable)
        Label passengersLabel = new Label("Number of Passengers:");
        passengersLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        // Extract current passengers from details
        int currentPassengers = 1;
        try {
            String[] parts = booking.details.split("passenger");
            if (parts.length > 0) {
                String num = parts[0].trim().split(" ")[parts[0].trim().split(" ").length - 1];
                currentPassengers = Integer.parseInt(num);
            }
        } catch (Exception e) {
            // Keep default
        }

        Spinner<Integer> passengersSpinner = new Spinner<>(1, 9, currentPassengers);
        passengersSpinner.setEditable(true);
        passengersSpinner.setPrefWidth(Double.MAX_VALUE);

        // Special requests (editable)
        Label requestsLabel = new Label("Special Requests:");
        requestsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        TextArea requestsArea = new TextArea();
        requestsArea.setPromptText("Wheelchair assistance, meal preferences, etc.");
        requestsArea.setPrefRowCount(3);
        requestsArea.setWrapText(true);

        // Price info
        Label priceInfo = new Label(String.format("Current Total: $%.2f", booking.price));
        priceInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");

        content.getChildren().addAll(
            flightInfo, new Separator(),
            passengersLabel, passengersSpinner,
            requestsLabel, requestsArea,
            new Separator(), priceInfo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        saveButton.setText("Save Changes");
        saveButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Update booking in database
                boolean success = updateFlightBooking(
                    booking.id,
                    passengersSpinner.getValue(),
                    requestsArea.getText()
                );

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Booking Updated",
                             "Your booking has been updated successfully!");
                    loadAllBookings();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Update Failed",
                             "Could not update booking. Please try again.");
                }
            }
        });
    }

    private boolean updateFlightBooking(int bookingId, int passengers, String specialRequests) {
        try {
            Connection conn = DataSource.getInstance().getConnection();

            if (conn == null || conn.isClosed()) {
                System.err.println("❌ Database connection is closed");
                return false;
            }

            // Get current booking
            String selectQuery = "SELECT nombre_passagers, prix_total FROM reservations_vol WHERE reservation_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setInt(1, bookingId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int oldPassengers = rs.getInt("nombre_passagers");
                double oldTotal = rs.getDouble("prix_total");

                // Calculate new price (proportional to passengers)
                double pricePerPassenger = oldPassengers > 0 ? oldTotal / oldPassengers : oldTotal;
                double newTotal = pricePerPassenger * passengers;

                // Update booking - simpler query without demandes_speciales if it causes issues
                String updateQuery = "UPDATE reservations_vol SET nombre_passagers = ?, prix_total = ? WHERE reservation_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, passengers);
                updateStmt.setDouble(2, newTotal);
                updateStmt.setInt(3, bookingId);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("✅ Booking updated: ID=" + bookingId + ", Passengers=" + passengers + ", Total=$" + newTotal);
                    return true;
                } else {
                    System.err.println("❌ No rows affected for booking ID: " + bookingId);
                }
            } else {
                System.err.println("❌ Booking not found: " + bookingId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating booking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case "CONFIRMEE": return "✓ Confirmed";
            case "EN_ATTENTE": return "⏳ Pending";
            case "ANNULEE": return "✕ Cancelled";
            case "TERMINEE": return "✓ Completed";
            default: return status;
        }
    }

    private String getStatusStyle(String status) {
        String baseStyle = "-fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: 5 12; -fx-background-radius: 15;";
        if (status == null) return baseStyle + " -fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        switch (status) {
            case "CONFIRMEE": return baseStyle + " -fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
            case "EN_ATTENTE": return baseStyle + " -fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case "ANNULEE": return baseStyle + " -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            case "TERMINEE": return baseStyle + " -fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            default: return baseStyle + " -fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        }
    }

    private void viewBookingDetails(BookingInfo booking) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Booking Details");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: white;");

        // Header with icon and title
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(booking.icon);
        iconLabel.setStyle("-fx-font-size: 40px;");

        VBox titleBox = new VBox(3);
        Label titleLabel = new Label(booking.title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label subtitleLabel = new Label(booking.subtitle);
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        header.getChildren().addAll(iconLabel, titleBox);

        // Status badge
        Label statusLabel = new Label(getStatusDisplay(booking.status));
        statusLabel.setStyle(getStatusStyle(booking.status));

        // Details grid
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: #f9fafb; -fx-padding: 15; -fx-background-radius: 8;");

        int row = 0;

        // Confirmation
        grid.add(new Label("🎫 Confirmation:"), 0, row);
        Label confValue = new Label(booking.confirmationNumber != null ? booking.confirmationNumber : "N/A");
        confValue.setStyle("-fx-font-weight: bold;");
        grid.add(confValue, 1, row++);

        // Type
        grid.add(new Label("📦 Type:"), 0, row);
        grid.add(new Label(booking.type), 1, row++);

        // Date
        grid.add(new Label("📅 Date:"), 0, row);
        String dateStr = booking.tripDate.format(DATE_FORMATTER);
        if (booking.endDate != null) {
            dateStr += " - " + booking.endDate.format(DATE_FORMATTER);
        }
        grid.add(new Label(dateStr), 1, row++);

        // Details
        grid.add(new Label("📋 Details:"), 0, row);
        grid.add(new Label(booking.details), 1, row++);

        // Booked on
        grid.add(new Label("🗓️ Booked On:"), 0, row);
        grid.add(new Label(booking.bookingDate.format(DATE_FORMATTER)), 1, row++);

        // Price
        grid.add(new Label("💰 Total Price:"), 0, row);
        Label priceValue = new Label(String.format("$%.2f", booking.price));
        priceValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");
        grid.add(priceValue, 1, row++);

        // Separator
        Separator sep = new Separator();

        content.getChildren().addAll(header, statusLabel, sep, grid);

        dialog.getDialogPane().setContent(content);

        // Create custom button types
        ButtonType exportPdfType = new ButtonType("📄 Export PDF", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(exportPdfType, ButtonType.CLOSE);

        Button exportBtn = (Button) dialog.getDialogPane().lookupButton(exportPdfType);
        exportBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == exportPdfType) {
                exportBookingToPDF(booking);
            }
        });
    }

    /**
     * Export booking details to PDF
     */
    private void exportBookingToPDF(BookingInfo booking) {
        try {
            // Create booking details object
            PDFExportService.BookingDetails details = new PDFExportService.BookingDetails();
            details.type = booking.type;
            details.title = booking.title;
            details.subtitle = booking.subtitle;
            details.confirmationNumber = booking.confirmationNumber;
            details.status = getStatusDisplay(booking.status);
            details.tripDate = booking.tripDate.format(DATE_FORMATTER);
            details.bookingDate = booking.bookingDate.format(DATE_FORMATTER);
            details.details = booking.details;
            details.price = String.format("$%.2f", booking.price);

            // Export to PDF
            java.io.File pdfFile = PDFExportService.exportBookingToPDF(details);

            // Show success dialog with option to open
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Export Successful");
            successAlert.setHeaderText("✅ Booking Exported Successfully!");
            successAlert.setContentText("Your booking details have been exported to:\n\n" +
                                       pdfFile.getAbsolutePath() + "\n\n" +
                                       "📄 The file will open in your browser.\n" +
                                       "💡 Tip: Press Ctrl+P to print or save as PDF!");

            ButtonType openType = new ButtonType("Open File");
            ButtonType closeType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            successAlert.getButtonTypes().setAll(openType, closeType);

            successAlert.showAndWait().ifPresent(response -> {
                if (response == openType) {
                    try {
                        // Open PDF with default application
                        java.awt.Desktop.getDesktop().open(pdfFile);
                    } catch (Exception ex) {
                        System.err.println("Could not open PDF: " + ex.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Failed",
                     "Could not export booking to PDF.\n\nError: " + e.getMessage());
        }
    }
    private void cancelBooking(BookingInfo booking) {
        // Create confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cancel Booking");
        dialog.setHeaderText("⚠️ Cancel Booking?");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        Label warningLabel = new Label("Are you sure you want to cancel this booking?");
        warningLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        VBox detailsBox = new VBox(5);
        detailsBox.setStyle("-fx-background-color: #fef2f2; -fx-padding: 15; -fx-background-radius: 8;");

        Label typeLabel = new Label(booking.icon + " " + booking.type);
        typeLabel.setStyle("-fx-font-weight: bold;");
        Label titleLabel = new Label(booking.title);
        Label detailsLabel = new Label(booking.details);
        detailsLabel.setStyle("-fx-text-fill: #6b7280;");
        Label priceLabel = new Label(String.format("Total: $%.2f", booking.price));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");

        detailsBox.getChildren().addAll(typeLabel, titleLabel, detailsLabel, priceLabel);

        Label refundLabel = new Label("💰 Refund will be processed within 5-7 business days.");
        refundLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        content.getChildren().addAll(warningLabel, detailsBox, refundLabel);

        dialog.getDialogPane().setContent(content);

        ButtonType cancelType = new ButtonType("Yes, Cancel Booking", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelType, ButtonType.NO);

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(cancelType);
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == cancelType) {
                boolean success = false;
                if (booking.type.equals("Flight")) {
                    success = flightBookingService.cancelBooking(booking.id);
                } else if (booking.type.equals("Hotel")) {
                    success = hotelBookingService.cancelBooking(booking.id);
                }

                if (success) {
                    // Send cancellation email
                    sendCancellationEmail(booking);

                    showAlert(Alert.AlertType.INFORMATION, "Booking Cancelled",
                             "Your booking has been cancelled successfully.\n\n" +
                             "Confirmation: " + booking.confirmationNumber + "\n" +
                             "A confirmation email has been sent.");
                    loadAllBookings();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Cancellation Failed",
                             "Could not cancel booking. Please try again.");
                }
            }
        });
    }

    private void sendCancellationEmail(BookingInfo booking) {
        try {
            EmailNotificationService emailService = new EmailNotificationService();

            String subject = "❌ TripWise - Booking Cancelled " + booking.confirmationNumber;

            StringBuilder content = new StringBuilder();
            content.append("========================================\n");
            content.append("       BOOKING CANCELLED\n");
            content.append("========================================\n\n");
            content.append("Your booking has been cancelled.\n\n");
            content.append("Confirmation: ").append(booking.confirmationNumber).append("\n");
            content.append("Type: ").append(booking.type).append("\n");
            content.append("Details: ").append(booking.title).append("\n");
            content.append("Amount: $").append(String.format("%.2f", booking.price)).append("\n\n");
            content.append("💰 Refund will be processed within 5-7 business days.\n\n");
            content.append("If you did not request this cancellation, please contact support.\n\n");
            content.append("Best regards,\n");
            content.append("TripWise Travel Agency\n");
            content.append("========================================\n");

            emailService.sendEmailDirect("eya.khemirii@gmail.com", subject, content.toString());
            System.out.println("📧 Cancellation email sent");
        } catch (Exception e) {
            System.err.println("⚠️ Could not send cancellation email: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private int getTravelerIdForUser(int userId) {
        try {
            Connection conn = DataSource.getInstance().getConnection();

            // First, try to get existing voyageur
            String query = "SELECT voyageur_id FROM voyageurs WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("voyageur_id");
            }

            // If not found, create a new voyageur profile
            System.out.println("Creating new traveler profile for user: " + userId);
            String insertQuery = "INSERT INTO voyageurs (user_id, points_fidelite, created_at) VALUES (?, 0, NOW())";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery, java.sql.Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, userId);
            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newVoyageurId = generatedKeys.getInt(1);
                    System.out.println("✅ Created new traveler profile with ID: " + newVoyageurId);
                    return newVoyageurId;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting/creating traveler ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Open seat selection dialog for a flight booking
     */
    private void openSeatSelection(BookingInfo booking) {
        try {
            Connection conn = DataSource.getInstance().getConnection();

            // Get flight details for this booking
            String query = """
                SELECT rv.reservation_id, rv.vol_id, rv.nombre_passagers, cv.type_classe as classe, 
                       rv.prix_total, rv.sieges_attribues,
                       v.numero_vol, v.capacite_totale, ad.ville as ville_depart, aa.ville as ville_arrivee
                FROM reservations_vol rv
                JOIN vols v ON rv.vol_id = v.vol_id
                JOIN classes_vol cv ON rv.classe_id = cv.classe_id
                JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id
                JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id
                WHERE rv.reservation_id = ?
                """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, booking.id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int volId = rs.getInt("vol_id");
                int reservationId = rs.getInt("reservation_id");
                int passengers = rs.getInt("nombre_passagers");
                String currentSeats = rs.getString("sieges_attribues");
                String flightNumber = rs.getString("numero_vol");
                String route = rs.getString("ville_depart") + " → " + rs.getString("ville_arrivee");
                int capacity = rs.getInt("capacite_totale");

                // Show seat selection dialog
                showSeatSelectionDialog(reservationId, volId, passengers, currentSeats, flightNumber, route, capacity);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find flight details for this booking.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening seat selection: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open seat selection.");
        }
    }

    /**
     * Show seat selection dialog
     */
    private void showSeatSelectionDialog(int reservationId, int volId, int passengers,
                                         String currentSeats, String flightNumber, String route, int capacity) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Seats - " + flightNumber);
        dialog.setHeaderText("Choose Your Seats");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(520);
        content.setPrefHeight(500);

        // Flight info
        Label flightInfo = new Label("✈️ " + flightNumber + " | " + route);
        flightInfo.setStyle("-fx-font-size: 14px; -fx-background-color: #eff6ff; -fx-padding: 10; -fx-background-radius: 8;");

        // Current seats
        Label currentLabel = new Label("Current seats: " + (currentSeats != null && !currentSeats.isEmpty() ? currentSeats : "None selected"));
        currentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        // Legend
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
            new Label("🟢 Available"),
            new Label("🔴 Occupied"),
            new Label("🔵 Selected")
        );

        // Seat grid
        javafx.scene.layout.GridPane seatGrid = new javafx.scene.layout.GridPane();
        seatGrid.setHgap(6);
        seatGrid.setVgap(6);
        seatGrid.setAlignment(Pos.CENTER);
        seatGrid.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Column headers
        String[] columns = {"A", "B", "C", "", "D", "E", "F"};
        for (int col = 0; col < columns.length; col++) {
            Label colLabel = new Label(columns[col]);
            colLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-min-width: 32; -fx-alignment: center;");
            colLabel.setAlignment(Pos.CENTER);
            seatGrid.add(colLabel, col, 0);
        }

        // Track selected seats
        java.util.List<String> selectedSeats = new java.util.ArrayList<>();
        if (currentSeats != null && !currentSeats.isEmpty()) {
            for (String seat : currentSeats.split(",")) {
                selectedSeats.add(seat.trim());
            }
        }

        // Generate seat rows
        int maxRows = Math.min(12, (int) Math.ceil(capacity / 6.0));
        java.util.Random random = new java.util.Random(volId);

        for (int row = 1; row <= maxRows; row++) {
            // Row number
            Label rowLabel = new Label(String.valueOf(row));
            rowLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            seatGrid.add(rowLabel, 7, row);

            for (int col = 0; col < columns.length; col++) {
                if (columns[col].isEmpty()) {
                    seatGrid.add(new Label("  "), col, row);
                    continue;
                }

                String seatNumber = row + columns[col];
                boolean isCurrentlySelected = selectedSeats.contains(seatNumber);
                boolean isOccupied = !isCurrentlySelected && random.nextDouble() < 0.25;

                Button seatBtn = new Button(seatNumber);
                seatBtn.setPrefSize(32, 28);
                seatBtn.setStyle(getSeatStyle(isOccupied, isCurrentlySelected));

                if (!isOccupied) {
                    final String seat = seatNumber;
                    seatBtn.setOnAction(e -> {
                        if (selectedSeats.contains(seat)) {
                            selectedSeats.remove(seat);
                            seatBtn.setStyle(getSeatStyle(false, false));
                        } else if (selectedSeats.size() < passengers) {
                            selectedSeats.add(seat);
                            seatBtn.setStyle(getSeatStyle(false, true));
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Limit Reached",
                                "You can only select " + passengers + " seat(s).");
                        }
                    });
                } else {
                    seatBtn.setDisable(true);
                }

                seatGrid.add(seatBtn, col, row);
            }
        }

        // Scroll pane
        ScrollPane scrollPane = new ScrollPane(seatGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Selection info
        Label selectionInfo = new Label("Select " + passengers + " seat(s)");
        selectionInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        content.getChildren().addAll(flightInfo, currentLabel, legend, scrollPane, selectionInfo);

        dialog.getDialogPane().setContent(content);

        ButtonType saveType = new ButtonType("Save Seats", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveType) {
                if (selectedSeats.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "No Seats", "Please select at least one seat.");
                    return;
                }

                String newSeats = String.join(", ", selectedSeats);
                boolean success = updateBookingSeats(reservationId, newSeats);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Seats Updated",
                        "Your seats have been updated to: " + newSeats);
                    loadAllBookings();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Update Failed",
                        "Could not update seats. Please try again.");
                }
            }
        });
    }

    private String getSeatStyle(boolean occupied, boolean selected) {
        if (occupied) {
            return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 9px; -fx-background-radius: 4;";
        } else if (selected) {
            return "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 9px; -fx-background-radius: 4; -fx-cursor: hand;";
        } else {
            return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 9px; -fx-background-radius: 4; -fx-cursor: hand;";
        }
    }

    private boolean updateBookingSeats(int reservationId, String seats) {
        try {
            Connection conn = DataSource.getInstance().getConnection();
            String query = "UPDATE reservations_vol SET sieges_attribues = ? WHERE reservation_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, seats);
            stmt.setInt(2, reservationId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Seats updated for reservation " + reservationId + ": " + seats);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating seats: " + e.getMessage());
        }
        return false;
    }

    // Inner class for booking info
    private static class BookingInfo {
        int id;
        String type;
        String icon;
        String title;
        String subtitle;
        String confirmationNumber;
        String status;
        double price;
        LocalDateTime bookingDate;
        LocalDateTime tripDate;
        LocalDate endDate;
        String details;
    }
}
