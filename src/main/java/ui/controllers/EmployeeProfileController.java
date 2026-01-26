package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ui.model.User;
import ui.util.SceneManager;
import ui.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeProfileController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label departmentLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private Label hireDateLabel;

    @FXML
    private Label totalBookingsLabel;

    @FXML
    private Label pendingApprovalsLabel;

    @FXML
    private Label approvedTodayLabel;

    @FXML
    private void initialize() {
        loadEmployeeProfile();
        loadEmployeeStats();
    }

    private void loadEmployeeProfile() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                System.err.println("❌ No user logged in");
                return;
            }

            Connection conn = ui.util.DataSource.getInstance().getConnection();
            String query = "SELECT u.first_name, u.last_name, u.email, e.department, e.position, e.hire_date " +
                          "FROM users u " +
                          "JOIN employes e ON u.user_id = e.user_id " +
                          "WHERE u.user_id = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                nameLabel.setText(fullName);
                emailLabel.setText(rs.getString("email"));
                departmentLabel.setText("Department: " + rs.getString("department"));
                positionLabel.setText("Position: " + rs.getString("position"));
                hireDateLabel.setText("Hire Date: " + rs.getDate("hire_date").toString());
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading employee profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEmployeeStats() {
        try {
            Connection conn = ui.util.DataSource.getInstance().getConnection();

            // Total bookings managed
            String totalQuery = "SELECT COUNT(*) as total FROM reservations_hotel WHERE statut_reservation != 'EN_ATTENTE'";
            PreparedStatement stmt = conn.prepareStatement(totalQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalBookingsLabel.setText(String.valueOf(rs.getInt("total")));
            }

            // Pending approvals
            String pendingQuery = "SELECT COUNT(*) as pending FROM reservations_hotel WHERE statut_reservation = 'EN_ATTENTE'";
            stmt = conn.prepareStatement(pendingQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                pendingApprovalsLabel.setText(String.valueOf(rs.getInt("pending")));
            }

            // Approved today
            String approvedQuery = "SELECT COUNT(*) as approved FROM reservations_hotel " +
                                  "WHERE statut_reservation = 'CONFIRMEE' AND DATE(date_reservation) = CURDATE()";
            stmt = conn.prepareStatement(approvedQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                approvedTodayLabel.setText(String.valueOf(rs.getInt("approved")));
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading employee stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageBookings() {
        try {
            SceneManager.switchScene("/ui/employee-booking-management.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error navigating to booking management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewCustomers() {
        try {
            SceneManager.switchScene("/ui/employee-customer-management.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error navigating to customer management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHotelOverview() {
        try {
            SceneManager.switchScene("/ui/book-hotel-new.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error navigating to hotel overview: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
