package ui.controllers.traveler;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import ui.model.HotelBooking;
import ui.model.HotelBooking.StatutReservation;
import ui.service.BookingManagementService;
import ui.util.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TravelerBookingsController - Manages traveler's booking view
 * Features: View all bookings, filter, modify dates, cancel, download PDF
 */
public class TravelerBookingsController {

    // FXML Components - Filters
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TextField searchField;

    // FXML Components - Statistics
    @FXML private Text totalBookingsLabel;
    @FXML private Text upcomingBookingsLabel;
    @FXML private Text pendingBookingsLabel;
    @FXML private Text totalSpentLabel;

    // FXML Components - View Toggle
    @FXML private RadioButton timelineViewRadio;
    @FXML private RadioButton listViewRadio;

    // FXML Components - Timeline View
    @FXML private ScrollPane timelineScrollPane;
    @FXML private VBox timelineContainer;

    // FXML Components - Table View
    @FXML private TableView<HotelBooking> bookingsTableView;
    @FXML private TableColumn<HotelBooking, String> bookingIdColumn;
    @FXML private TableColumn<HotelBooking, String> typeColumn;
    @FXML private TableColumn<HotelBooking, String> hotelNameColumn;
    @FXML private TableColumn<HotelBooking, String> confirmationColumn;
    @FXML private TableColumn<HotelBooking, String> checkinColumn;
    @FXML private TableColumn<HotelBooking, String> checkoutColumn;
    @FXML private TableColumn<HotelBooking, String> guestsColumn;
    @FXML private TableColumn<HotelBooking, String> nightsColumn;
    @FXML private TableColumn<HotelBooking, String> priceColumn;
    @FXML private TableColumn<HotelBooking, String> statusColumn;
    @FXML private TableColumn<HotelBooking, Void> actionsColumn;

    // FXML Components - Empty/Loading States
    @FXML private VBox emptyStateContainer;
    @FXML private VBox loadingContainer;

    // FXML Components - Pagination
    @FXML private HBox paginationContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    // Services
    private BookingManagementService bookingService;
    
    // Data
    private ObservableList<HotelBooking> allBookings;
    private ObservableList<HotelBooking> filteredBookings;
    private int currentTravelerId;
    
    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    // Date formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        bookingService = new BookingManagementService();
        allBookings = FXCollections.observableArrayList();
        filteredBookings = FXCollections.observableArrayList();

        // Get current traveler ID from session
        currentTravelerId = SessionManager.getInstance().getCurrentUser().getUserId();

        // Initialize table columns
        setupTableColumns();

        // Setup view toggle listeners
        setupViewToggle();

        // Populate ComboBoxes
        typeComboBox.getItems().addAll("All Types", "Hotels", "Flights", "Cars");
        statusComboBox.getItems().addAll("All Statuses", "Pending", "Confirmed", "Cancelled", "Completed");

        // Set default filter values
        typeComboBox.setValue("All Types");
        statusComboBox.setValue("All Statuses");

        // Add search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 2) {
                applyFilters();
            } else if (newVal == null || newVal.isEmpty()) {
                applyFilters();
            }
        });

        // Load bookings
        loadBookings();
    }

    /**
     * Setup table columns with cell value factories
     */
    private void setupTableColumns() {
        bookingIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBookingId()));
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("Hotel")); // TODO: Expand when flights/cars added
        
        hotelNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getHotelName()));
        
        confirmationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNumeroConfirmation() != null ? 
                cellData.getValue().getNumeroConfirmation() : "Pending"));
        
        checkinColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateCheckin() != null ? 
                cellData.getValue().getDateCheckin().format(DATE_FORMATTER) : "N/A"));
        
        checkoutColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateCheckout() != null ? 
                cellData.getValue().getDateCheckout().format(DATE_FORMATTER) : "N/A"));
        
        guestsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(
                cellData.getValue().getNombreAdultes() + cellData.getValue().getNombreEnfants())));
        
        nightsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getNombreNuits())));
        
        priceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getPrixTotal())));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatusDisplay()));

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button modifyButton = new Button("Modify");
            private final Button cancelButton = new Button("Cancel");
            private final HBox container = new HBox(4, viewButton, modifyButton, cancelButton);

            {
                viewButton.getStyleClass().add("btn-sm");
                modifyButton.getStyleClass().add("btn-sm");
                cancelButton.getStyleClass().addAll("btn-sm", "btn-danger");
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HotelBooking booking = getTableView().getItems().get(getIndex());
                    
                    // Disable modify/cancel for completed or cancelled bookings
                    boolean isCancellable = booking.getStatutReservation() != StatutReservation.ANNULEE &&
                                           booking.getStatutReservation() != StatutReservation.TERMINEE;
                    modifyButton.setDisable(!isCancellable);
                    cancelButton.setDisable(!isCancellable);
                    
                    viewButton.setOnAction(e -> handleViewBooking(booking));
                    modifyButton.setOnAction(e -> handleModifyBooking(booking));
                    cancelButton.setOnAction(e -> handleCancelBooking(booking));
                    
                    setGraphic(container);
                }
            }
        });
    }

    /**
     * Setup view toggle (Timeline vs List)
     */
    private void setupViewToggle() {
        timelineViewRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                timelineScrollPane.setVisible(true);
                timelineScrollPane.setManaged(true);
                bookingsTableView.setVisible(false);
                bookingsTableView.setManaged(false);
                paginationContainer.setVisible(false);
                
                refreshTimelineView();
            }
        });

        listViewRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                timelineScrollPane.setVisible(false);
                timelineScrollPane.setManaged(false);
                bookingsTableView.setVisible(true);
                bookingsTableView.setManaged(true);
                paginationContainer.setVisible(true);
                
                refreshTableView();
            }
        });
    }

    /**
     * Load bookings from database
     */
    private void loadBookings() {
        showLoading(true);

        // Use a background thread for database query
        new Thread(() -> {
            try {
                List<HotelBooking> bookings = bookingService.getBookingsByTraveler(currentTravelerId);
                
                javafx.application.Platform.runLater(() -> {
                    allBookings.clear();
                    allBookings.addAll(bookings);
                    applyFilters();
                    updateStatistics();
                    showLoading(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showLoading(false);
                    showError("Failed to load bookings: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Apply filters and refresh view
     */
    @FXML
    private void handleApplyFilters() {
        applyFilters();
    }

    /**
     * Reset all filters
     */
    @FXML
    private void handleResetFilters() {
        typeComboBox.setValue("All Types");
        statusComboBox.setValue("All Statuses");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        searchField.clear();
        applyFilters();
    }

    /**
     * Apply current filters to bookings list
     */
    private void applyFilters() {
        String typeFilter = typeComboBox.getValue();
        String statusFilter = statusComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String searchText = searchField.getText();

        filteredBookings.clear();
        
        List<HotelBooking> filtered = allBookings.stream()
            .filter(booking -> {
                // Type filter (currently only hotels)
                if (!"All Types".equals(typeFilter) && !"Hotels".equals(typeFilter)) {
                    return false;
                }
                
                // Status filter
                if (!"All Statuses".equals(statusFilter)) {
                    String bookingStatus = booking.getStatusDisplay();
                    if (!statusFilter.equals(bookingStatus)) {
                        return false;
                    }
                }
                
                // Date range filter
                if (fromDate != null && booking.getDateCheckin().isBefore(fromDate)) {
                    return false;
                }
                if (toDate != null && booking.getDateCheckout().isAfter(toDate)) {
                    return false;
                }
                
                // Search text filter
                if (searchText != null && !searchText.isEmpty()) {
                    String searchLower = searchText.toLowerCase();
                    return (booking.getHotelName() != null && booking.getHotelName().toLowerCase().contains(searchLower)) ||
                           (booking.getNumeroConfirmation() != null && booking.getNumeroConfirmation().toLowerCase().contains(searchLower)) ||
                           booking.getBookingId().toLowerCase().contains(searchLower);
                }
                
                return true;
            })
            .collect(Collectors.toList());

        filteredBookings.addAll(filtered);
        
        // Show empty state if no results
        if (filteredBookings.isEmpty() && !allBookings.isEmpty()) {
            // Filters applied but no results
            showEmptyState(false);
        } else if (allBookings.isEmpty()) {
            // No bookings at all
            showEmptyState(true);
        } else {
            hideEmptyState();
        }

        // Refresh current view
        if (timelineViewRadio.isSelected()) {
            refreshTimelineView();
        } else {
            refreshTableView();
        }
    }

    /**
     * Refresh timeline view with filtered bookings
     */
    private void refreshTimelineView() {
        timelineContainer.getChildren().clear();

        if (filteredBookings.isEmpty()) {
            return;
        }

        // Group bookings by date (upcoming first, then past)
        LocalDate today = LocalDate.now();
        
        List<HotelBooking> upcomingBookings = filteredBookings.stream()
            .filter(b -> b.getDateCheckin().isAfter(today) || b.getDateCheckin().isEqual(today))
            .sorted((b1, b2) -> b1.getDateCheckin().compareTo(b2.getDateCheckin()))
            .collect(Collectors.toList());
        
        List<HotelBooking> pastBookings = filteredBookings.stream()
            .filter(b -> b.getDateCheckin().isBefore(today))
            .sorted((b1, b2) -> b2.getDateCheckin().compareTo(b1.getDateCheckin()))
            .collect(Collectors.toList());

        // Add upcoming bookings section
        if (!upcomingBookings.isEmpty()) {
            Text upcomingHeader = new Text("Upcoming Trips");
            upcomingHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");
            timelineContainer.getChildren().add(upcomingHeader);

            for (HotelBooking booking : upcomingBookings) {
                timelineContainer.getChildren().add(createBookingCard(booking));
            }
        }

        // Add past bookings section
        if (!pastBookings.isEmpty()) {
            Text pastHeader = new Text("Past Trips");
            pastHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-fill: #64748b;");
            VBox.setMargin(pastHeader, new Insets(24, 0, 0, 0));
            timelineContainer.getChildren().add(pastHeader);

            for (HotelBooking booking : pastBookings) {
                timelineContainer.getChildren().add(createBookingCard(booking));
            }
        }
    }

    /**
     * Create a booking card for timeline view
     */
    private VBox createBookingCard(HotelBooking booking) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));

        // Header row: Hotel name + Status badge
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Text hotelName = new Text(booking.getHotelName());
        hotelName.setStyle("-fx-font-size: 16px; -fx-font-weight: 600;");
        HBox.setHgrow(hotelName, javafx.scene.layout.Priority.ALWAYS);
        
        Label statusBadge = new Label(booking.getStatusDisplay());
        statusBadge.getStyleClass().add("badge");
        statusBadge.getStyleClass().add(getStatusBadgeClass(booking.getStatutReservation()));
        
        headerRow.getChildren().addAll(hotelName, statusBadge);

        // Booking details
        VBox detailsBox = new VBox(4);
        detailsBox.getChildren().addAll(
            createDetailRow("📅", "Check-in: " + booking.getDateCheckin().format(DATE_FORMATTER)),
            createDetailRow("📅", "Check-out: " + booking.getDateCheckout().format(DATE_FORMATTER)),
            createDetailRow("🛏️", booking.getChambreType() + " • " + booking.getNombreNuits() + " nights"),
            createDetailRow("👥", booking.getNombreAdultes() + " adults, " + booking.getNombreEnfants() + " children"),
            createDetailRow("💰", String.format("$%.2f", booking.getPrixTotal()))
        );

        if (booking.getNumeroConfirmation() != null) {
            detailsBox.getChildren().add(
                createDetailRow("✅", "Confirmation: " + booking.getNumeroConfirmation())
            );
        }

        // Action buttons
        HBox actionRow = new HBox(8);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        
        Button viewButton = new Button("View Details");
        viewButton.getStyleClass().add("btn-secondary");
        viewButton.setOnAction(e -> handleViewBooking(booking));
        
        Button modifyButton = new Button("Modify Dates");
        modifyButton.getStyleClass().add("btn-secondary");
        modifyButton.setOnAction(e -> handleModifyBooking(booking));
        
        Button cancelButton = new Button("Cancel Booking");
        cancelButton.getStyleClass().addAll("btn-secondary", "btn-danger");
        cancelButton.setOnAction(e -> handleCancelBooking(booking));
        
        Button downloadButton = new Button("Download PDF");
        downloadButton.getStyleClass().add("btn-secondary");
        downloadButton.setOnAction(e -> handleDownloadPDF(booking));

        // Disable modify/cancel for completed or cancelled bookings
        boolean isCancellable = booking.getStatutReservation() != StatutReservation.ANNULEE &&
                               booking.getStatutReservation() != StatutReservation.TERMINEE;
        modifyButton.setDisable(!isCancellable);
        cancelButton.setDisable(!isCancellable);

        actionRow.getChildren().addAll(viewButton, modifyButton, cancelButton, downloadButton);

        card.getChildren().addAll(headerRow, detailsBox, actionRow);
        return card;
    }

    /**
     * Create a detail row with icon and text
     */
    private HBox createDetailRow(String icon, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Text iconText = new Text(icon);
        Text detailText = new Text(text);
        detailText.setStyle("-fx-fill: #64748b;");
        
        row.getChildren().addAll(iconText, detailText);
        return row;
    }

    /**
     * Get CSS class for status badge
     */
    private String getStatusBadgeClass(StatutReservation status) {
        switch (status) {
            case CONFIRMEE: return "badge-success";
            case EN_ATTENTE: return "badge-warning";
            case ANNULEE: return "badge-danger";
            case TERMINEE: return "badge-secondary";
            default: return "badge-secondary";
        }
    }

    /**
     * Refresh table view with pagination
     */
    private void refreshTableView() {
        totalPages = (int) Math.ceil((double) filteredBookings.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredBookings.size());

        ObservableList<HotelBooking> pageData = FXCollections.observableArrayList(
            filteredBookings.subList(fromIndex, toIndex)
        );
        
        bookingsTableView.setItems(pageData);
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);
    }

    /**
     * Update statistics display
     */
    private void updateStatistics() {
        int total = allBookings.size();
        long upcoming = allBookings.stream()
            .filter(b -> b.getDateCheckin().isAfter(LocalDate.now()) || b.getDateCheckin().isEqual(LocalDate.now()))
            .count();
        long pending = allBookings.stream()
            .filter(b -> b.getStatutReservation() == StatutReservation.EN_ATTENTE)
            .count();
        double totalSpent = allBookings.stream()
            .filter(b -> b.getStatutReservation() == StatutReservation.CONFIRMEE || 
                        b.getStatutReservation() == StatutReservation.TERMINEE)
            .mapToDouble(HotelBooking::getPrixTotal)
            .sum();

        totalBookingsLabel.setText(String.valueOf(total));
        upcomingBookingsLabel.setText(String.valueOf(upcoming));
        pendingBookingsLabel.setText(String.valueOf(pending));
        totalSpentLabel.setText(String.format("$%.2f", totalSpent));
    }

    /**
     * Handle view booking details
     */
    private void handleViewBooking(HotelBooking booking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText(booking.getHotelName());
        
        StringBuilder content = new StringBuilder();
        content.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        content.append("Confirmation: ").append(booking.getNumeroConfirmation() != null ? 
            booking.getNumeroConfirmation() : "Pending").append("\n\n");
        content.append("Check-in: ").append(booking.getDateCheckin().format(DATE_FORMATTER)).append("\n");
        content.append("Check-out: ").append(booking.getDateCheckout().format(DATE_FORMATTER)).append("\n");
        content.append("Nights: ").append(booking.getNombreNuits()).append("\n\n");
        content.append("Room: ").append(booking.getChambreType()).append("\n");
        content.append("Guests: ").append(booking.getNombreAdultes()).append(" adults, ")
               .append(booking.getNombreEnfants()).append(" children\n\n");
        content.append("Total Price: $").append(String.format("%.2f", booking.getPrixTotal())).append("\n");
        content.append("Status: ").append(booking.getStatusDisplay()).append("\n");
        
        if (booking.getDemandesSpeciales() != null && !booking.getDemandesSpeciales().isEmpty()) {
            content.append("\nSpecial Requests:\n").append(booking.getDemandesSpeciales());
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Handle modify booking dates
     */
    private void handleModifyBooking(HotelBooking booking) {
        // Create a dialog with date pickers
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modify Booking Dates");
        dialog.setHeaderText("Change check-in and check-out dates for " + booking.getHotelName());

        // Create form
        VBox form = new VBox(12);
        form.setPadding(new Insets(16));
        
        DatePicker newCheckinPicker = new DatePicker(booking.getDateCheckin());
        DatePicker newCheckoutPicker = new DatePicker(booking.getDateCheckout());
        
        form.getChildren().addAll(
            new Label("New Check-in Date:"),
            newCheckinPicker,
            new Label("New Check-out Date:"),
            newCheckoutPicker
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            LocalDate newCheckin = newCheckinPicker.getValue();
            LocalDate newCheckout = newCheckoutPicker.getValue();

            if (newCheckin != null && newCheckout != null && newCheckin.isBefore(newCheckout)) {
                if (bookingService.modifyBooking(booking.getReservationId(), newCheckin, newCheckout)) {
                    showSuccess("Booking dates updated successfully!");
                    loadBookings(); // Reload to reflect changes
                } else {
                    showError("Failed to update booking dates. Please try again.");
                }
            } else {
                showError("Invalid dates. Check-in must be before check-out.");
            }
        }
    }

    /**
     * Handle cancel booking
     */
    private void handleCancelBooking(HotelBooking booking) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Booking");
        confirmAlert.setHeaderText("Are you sure you want to cancel this booking?");
        confirmAlert.setContentText(booking.getHotelName() + " - " + 
            booking.getDateCheckin().format(DATE_FORMATTER) + " to " + 
            booking.getDateCheckout().format(DATE_FORMATTER));

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (bookingService.rejectBooking(booking.getReservationId(), currentTravelerId, "Cancelled by traveler")) {
                showSuccess("Booking cancelled successfully.");
                loadBookings(); // Reload to reflect changes
            } else {
                showError("Failed to cancel booking. Please contact support.");
            }
        }
    }

    /**
     * Handle download PDF
     */
    private void handleDownloadPDF(HotelBooking booking) {
        // TODO: Implement PDF generation using iText
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Download PDF");
        alert.setHeaderText("PDF Download");
        alert.setContentText("PDF download feature will be implemented in Phase 7.\n\n" +
            "Booking confirmation for " + booking.getHotelName() + " would be downloaded here.");
        alert.showAndWait();
    }

    /**
     * Navigate to search page
     */
    @FXML
    private void handleGoToSearch() {
        // TODO: Navigate to advanced search page
        showInfo("Navigation to search page will be implemented after menu integration.");
    }

    /**
     * Handle pagination - previous page
     */
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            refreshTableView();
        }
    }

    /**
     * Handle pagination - next page
     */
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            refreshTableView();
        }
    }

    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        loadingContainer.setVisible(show);
        loadingContainer.setManaged(show);
        timelineScrollPane.setVisible(!show);
        bookingsTableView.setVisible(!show);
    }

    /**
     * Show/hide empty state
     */
    private void showEmptyState(boolean noBookingsAtAll) {
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        timelineScrollPane.setVisible(false);
        timelineScrollPane.setManaged(false);
        bookingsTableView.setVisible(false);
        bookingsTableView.setManaged(false);
    }

    /**
     * Hide empty state
     */
    private void hideEmptyState() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show info message
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
