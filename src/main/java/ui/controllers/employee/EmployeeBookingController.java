package ui.controllers.employee;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ui.model.HotelBooking;
import ui.model.HotelBooking.StatutReservation;
import ui.service.BookingManagementService;
import ui.util.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Employee Booking Management Dashboard
 * Complete booking approval/rejection workflow with bulk operations
 */
public class EmployeeBookingController {

    @FXML private StackPane rootPane;
    @FXML private TableView<HotelBooking> bookingTable;
    @FXML private TableColumn<HotelBooking, String> colBookingId;
    @FXML private TableColumn<HotelBooking, String> colGuestName;
    @FXML private TableColumn<HotelBooking, String> colHotel;
    @FXML private TableColumn<HotelBooking, String> colCheckin;
    @FXML private TableColumn<HotelBooking, String> colCheckout;
    @FXML private TableColumn<HotelBooking, Integer> colNights;
    @FXML private TableColumn<HotelBooking, Double> colTotal;
    @FXML private TableColumn<HotelBooking, String> colStatus;
    
    // Filters
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private Button btnFilter;
    @FXML private Button btnClearFilter;
    
    // Action buttons
    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnModify;
    @FXML private Button btnRefresh;
    @FXML private Button btnBulkApprove;
    @FXML private Button btnBulkReject;
    
    // Statistics cards
    @FXML private Label lblTotalBookings;
    @FXML private Label lblPendingBookings;
    @FXML private Label lblConfirmedBookings;
    @FXML private Label lblRevenue;
    
    // Details panel
    @FXML private TextArea txtBookingDetails;
    @FXML private DatePicker dpNewCheckin;
    @FXML private DatePicker dpNewCheckout;
    @FXML private TextArea txtRejectionReason;
    
    private final BookingManagementService bookingService;
    private ObservableList<HotelBooking> bookingList;
    private HotelBooking selectedBooking;
    
    public EmployeeBookingController() {
        this.bookingService = new BookingManagementService();
    }
    
    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadPendingBookings();
        loadStatistics();
        playEntranceAnimation();
    }
    
    /**
     * Setup table columns with custom cell factories
     */
    private void setupTable() {
        colBookingId.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBookingId()));
        colGuestName.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colHotel.setCellValueFactory(new PropertyValueFactory<>("hotelName"));
        
        colCheckin.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateCheckin();
            return new javafx.beans.property.SimpleStringProperty(
                date != null ? date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : ""
            );
        });
        
        colCheckout.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateCheckout();
            return new javafx.beans.property.SimpleStringProperty(
                date != null ? date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : ""
            );
        });
        
        colNights.setCellValueFactory(new PropertyValueFactory<>("nombreNuits"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplay()));
        
        // Enable multiple selection
        bookingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Row selection listener
        bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedBooking = newSelection;
                displayBookingDetails(newSelection);
            }
        });
        
        // Color-coded status rows
        bookingTable.setRowFactory(tv -> new TableRow<HotelBooking>() {
            @Override
            protected void updateItem(HotelBooking booking, boolean empty) {
                super.updateItem(booking, empty);
                if (empty || booking == null) {
                    setStyle("");
                } else {
                    String baseStyle = getIndex() % 2 == 0 ? "-fx-background-color: #f9fafb;" : "-fx-background-color: white;";
                    
                    switch (booking.getStatutReservation()) {
                        case EN_ATTENTE:
                            setStyle(baseStyle + " -fx-border-color: #f59e0b; -fx-border-width: 0 0 0 4px;");
                            break;
                        case CONFIRMEE:
                            setStyle(baseStyle + " -fx-border-color: #10b981; -fx-border-width: 0 0 0 4px;");
                            break;
                        case ANNULEE:
                            setStyle(baseStyle + " -fx-border-color: #ef4444; -fx-border-width: 0 0 0 4px;");
                            break;
                        default:
                            setStyle(baseStyle);
                    }
                }
            }
        });
    }
    
    /**
     * Setup filters
     */
    private void setupFilters() {
        cbStatusFilter.setItems(FXCollections.observableArrayList(
            "All", "Pending", "Confirmed", "Cancelled", "Completed"
        ));
        cbStatusFilter.setValue("Pending");
        
        // Set default date range (last 30 days)
        dpFromDate.setValue(LocalDate.now().minusDays(30));
        dpToDate.setValue(LocalDate.now().plusDays(30));
        
        // Search on type
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 2) {
                handleSearch();
            } else if (newVal == null || newVal.isEmpty()) {
                loadPendingBookings();
            }
        });
    }
    
    /**
     * Load pending bookings (default view)
     */
    private void loadPendingBookings() {
        bookingList = FXCollections.observableArrayList(bookingService.getPendingBookings());
        bookingTable.setItems(bookingList);
        updateActionButtons();
    }
    
    /**
     * Load bookings with filters
     */
    @FXML
    private void handleFilter() {
        StatutReservation status = getStatusFromFilter();
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();
        
        bookingList = FXCollections.observableArrayList(
            bookingService.getAllBookings(status, fromDate, toDate)
        );
        bookingTable.setItems(bookingList);
        updateActionButtons();
    }
    
    /**
     * Clear filters
     */
    @FXML
    private void handleClearFilter() {
        cbStatusFilter.setValue("Pending");
        dpFromDate.setValue(LocalDate.now().minusDays(30));
        dpToDate.setValue(LocalDate.now().plusDays(30));
        txtSearch.clear();
        loadPendingBookings();
    }
    
    /**
     * Search bookings
     */
    @FXML
    private void handleSearch() {
        String searchTerm = txtSearch.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            bookingList = FXCollections.observableArrayList(
                bookingService.searchBookings(searchTerm)
            );
            bookingTable.setItems(bookingList);
        }
    }
    
    /**
     * Approve selected booking
     */
    @FXML
    private void handleApprove() {
        if (selectedBooking == null) {
            NotificationUtil.showWarning("Please select a booking to approve", rootPane);
            return;
        }
        
        if (selectedBooking.getStatutReservation() != StatutReservation.EN_ATTENTE) {
            NotificationUtil.showWarning("Only pending bookings can be approved", rootPane);
            return;
        }
        
        if (DialogUtil.showConfirmation("Approve Booking", 
            "Approve booking " + selectedBooking.getBookingId() + " for " + selectedBooking.getGuestName() + "?")) {
            
            int employeeId = getCurrentEmployeeId();
            if (bookingService.approveBooking(selectedBooking.getReservationId(), employeeId)) {
                NotificationUtil.showSuccess("Booking approved successfully!", rootPane);
                handleFilter();
                loadStatistics();
            } else {
                NotificationUtil.showError("Failed to approve booking", rootPane);
            }
        }
    }
    
    /**
     * Reject selected booking
     */
    @FXML
    private void handleReject() {
        if (selectedBooking == null) {
            NotificationUtil.showWarning("Please select a booking to reject", rootPane);
            return;
        }
        
        if (selectedBooking.getStatutReservation() != StatutReservation.EN_ATTENTE) {
            NotificationUtil.showWarning("Only pending bookings can be rejected", rootPane);
            return;
        }
        
        String reason = txtRejectionReason.getText();
        if (reason == null || reason.trim().isEmpty()) {
            NotificationUtil.showWarning("Please provide a rejection reason", rootPane);
            return;
        }
        
        if (DialogUtil.showConfirmation("Reject Booking", 
            "Reject booking " + selectedBooking.getBookingId() + "?\nThis action cannot be undone.")) {
            
            int employeeId = getCurrentEmployeeId();
            if (bookingService.rejectBooking(selectedBooking.getReservationId(), employeeId, reason)) {
                NotificationUtil.showSuccess("Booking rejected", rootPane);
                txtRejectionReason.clear();
                handleFilter();
                loadStatistics();
            } else {
                NotificationUtil.showError("Failed to reject booking", rootPane);
            }
        }
    }
    
    /**
     * Modify booking dates
     */
    @FXML
    private void handleModify() {
        if (selectedBooking == null) {
            NotificationUtil.showWarning("Please select a booking to modify", rootPane);
            return;
        }
        
        LocalDate newCheckin = dpNewCheckin.getValue();
        LocalDate newCheckout = dpNewCheckout.getValue();
        
        if (newCheckin == null || newCheckout == null) {
            NotificationUtil.showWarning("Please select new check-in and check-out dates", rootPane);
            return;
        }
        
        if (newCheckout.isBefore(newCheckin) || newCheckout.isEqual(newCheckin)) {
            NotificationUtil.showError("Check-out date must be after check-in date", rootPane);
            return;
        }
        
        if (DialogUtil.showConfirmation("Modify Booking", 
            "Update booking dates?\nNew check-in: " + newCheckin + "\nNew check-out: " + newCheckout)) {
            
            if (bookingService.modifyBooking(selectedBooking.getReservationId(), newCheckin, newCheckout)) {
                NotificationUtil.showSuccess("Booking dates updated successfully!", rootPane);
                dpNewCheckin.setValue(null);
                dpNewCheckout.setValue(null);
                handleFilter();
            } else {
                NotificationUtil.showError("Failed to modify booking", rootPane);
            }
        }
    }
    
    /**
     * Bulk approve selected bookings
     */
    @FXML
    private void handleBulkApprove() {
        ObservableList<HotelBooking> selectedBookings = bookingTable.getSelectionModel().getSelectedItems();
        
        if (selectedBookings.isEmpty()) {
            NotificationUtil.showWarning("Please select bookings to approve", rootPane);
            return;
        }
        
        List<Integer> bookingIds = new ArrayList<>();
        for (HotelBooking booking : selectedBookings) {
            if (booking.getStatutReservation() == StatutReservation.EN_ATTENTE) {
                bookingIds.add(booking.getReservationId());
            }
        }
        
        if (bookingIds.isEmpty()) {
            NotificationUtil.showWarning("No pending bookings selected", rootPane);
            return;
        }
        
        if (DialogUtil.showConfirmation("Bulk Approve", 
            "Approve " + bookingIds.size() + " bookings?")) {
            
            int employeeId = getCurrentEmployeeId();
            int successCount = bookingService.bulkApproveBookings(bookingIds, employeeId);
            NotificationUtil.showSuccess("Approved " + successCount + " bookings!", rootPane);
            handleFilter();
            loadStatistics();
        }
    }
    
    /**
     * Bulk reject selected bookings
     */
    @FXML
    private void handleBulkReject() {
        ObservableList<HotelBooking> selectedBookings = bookingTable.getSelectionModel().getSelectedItems();
        
        if (selectedBookings.isEmpty()) {
            NotificationUtil.showWarning("Please select bookings to reject", rootPane);
            return;
        }
        
        String reason = txtRejectionReason.getText();
        if (reason == null || reason.trim().isEmpty()) {
            NotificationUtil.showWarning("Please provide a rejection reason", rootPane);
            return;
        }
        
        List<Integer> bookingIds = new ArrayList<>();
        for (HotelBooking booking : selectedBookings) {
            if (booking.getStatutReservation() == StatutReservation.EN_ATTENTE) {
                bookingIds.add(booking.getReservationId());
            }
        }
        
        if (bookingIds.isEmpty()) {
            NotificationUtil.showWarning("No pending bookings selected", rootPane);
            return;
        }
        
        if (DialogUtil.showConfirmation("Bulk Reject", 
            "Reject " + bookingIds.size() + " bookings?\nThis action cannot be undone.")) {
            
            int employeeId = getCurrentEmployeeId();
            int successCount = bookingService.bulkRejectBookings(bookingIds, employeeId, reason);
            NotificationUtil.showSuccess("Rejected " + successCount + " bookings", rootPane);
            txtRejectionReason.clear();
            handleFilter();
            loadStatistics();
        }
    }
    
    /**
     * Refresh bookings
     */
    @FXML
    private void handleRefresh() {
        handleFilter();
        loadStatistics();
        NotificationUtil.showInfo("Bookings refreshed", rootPane);
    }
    
    /**
     * Display booking details in details panel
     */
    private void displayBookingDetails(HotelBooking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        
        StringBuilder details = new StringBuilder();
        details.append("BOOKING DETAILS\n");
        details.append("═══════════════════════════════════════\n\n");
        details.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        details.append("Confirmation: ").append(booking.getNumeroConfirmation() != null ? booking.getNumeroConfirmation() : "Pending").append("\n");
        details.append("Status: ").append(booking.getStatusDisplay()).append("\n\n");
        
        details.append("GUEST INFORMATION\n");
        details.append("───────────────────────────────────────\n");
        details.append("Name: ").append(booking.getGuestName()).append("\n");
        details.append("Email: ").append(booking.getGuestEmail()).append("\n\n");
        
        details.append("HOTEL INFORMATION\n");
        details.append("───────────────────────────────────────\n");
        details.append("Hotel: ").append(booking.getHotelName()).append("\n");
        details.append("Room Type: ").append(booking.getChambreType()).append("\n\n");
        
        details.append("STAY DETAILS\n");
        details.append("───────────────────────────────────────\n");
        details.append("Check-in: ").append(booking.getDateCheckin().format(dateFormatter)).append("\n");
        details.append("Check-out: ").append(booking.getDateCheckout().format(dateFormatter)).append("\n");
        details.append("Nights: ").append(booking.getNombreNuits()).append("\n");
        details.append("Guests: ").append(booking.getNombreAdultes()).append(" adults");
        if (booking.getNombreEnfants() > 0) {
            details.append(", ").append(booking.getNombreEnfants()).append(" children");
        }
        details.append("\n\n");
        
        details.append("PRICING\n");
        details.append("───────────────────────────────────────\n");
        details.append("Total: $").append(String.format("%.2f", booking.getPrixTotal())).append("\n\n");
        
        if (booking.getDemandesSpeciales() != null && !booking.getDemandesSpeciales().isEmpty()) {
            details.append("SPECIAL REQUESTS\n");
            details.append("───────────────────────────────────────\n");
            details.append(booking.getDemandesSpeciales()).append("\n\n");
        }
        
        details.append("BOOKING DATE\n");
        details.append("───────────────────────────────────────\n");
        details.append(booking.getDateReservation().format(timeFormatter)).append("\n");
        
        txtBookingDetails.setText(details.toString());
        
        // Pre-fill modification dates
        dpNewCheckin.setValue(booking.getDateCheckin());
        dpNewCheckout.setValue(booking.getDateCheckout());
    }
    
    /**
     * Load booking statistics
     */
    private void loadStatistics() {
        LocalDate fromDate = LocalDate.now().minusDays(30);
        LocalDate toDate = LocalDate.now().plusDays(30);
        
        BookingManagementService.BookingStats stats = bookingService.getBookingStats(fromDate, toDate);
        
        lblTotalBookings.setText(String.valueOf(stats.totalBookings));
        lblPendingBookings.setText(String.valueOf(stats.pendingBookings));
        lblConfirmedBookings.setText(String.valueOf(stats.confirmedBookings));
        lblRevenue.setText("$" + String.format("%,.2f", stats.totalRevenue));
    }
    
    /**
     * Update action button states
     */
    private void updateActionButtons() {
        boolean hasPending = false;
        for (HotelBooking booking : bookingList) {
            if (booking.getStatutReservation() == StatutReservation.EN_ATTENTE) {
                hasPending = true;
                break;
            }
        }
        
        btnBulkApprove.setDisable(!hasPending);
        btnBulkReject.setDisable(!hasPending);
    }
    
    /**
     * Get status enum from filter
     */
    private StatutReservation getStatusFromFilter() {
        String filter = cbStatusFilter.getValue();
        if (filter == null || filter.equals("All")) return null;
        
        switch (filter) {
            case "Pending": return StatutReservation.EN_ATTENTE;
            case "Confirmed": return StatutReservation.CONFIRMEE;
            case "Cancelled": return StatutReservation.ANNULEE;
            case "Completed": return StatutReservation.TERMINEE;
            default: return null;
        }
    }
    
    /**
     * Get current employee ID from session
     */
    private int getCurrentEmployeeId() {
        // Get from SessionManager
        return ui.util.SessionManager.getInstance().getCurrentUser().getUserId();
    }
    
    /**
     * Play entrance animation
     */
    private void playEntranceAnimation() {
        if (rootPane != null) {
            rootPane.setOpacity(0.0);
            FadeTransition fade = new FadeTransition(Duration.millis(400), rootPane);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }
}
