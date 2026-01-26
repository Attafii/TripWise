package ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import ui.util.SceneManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeCustomerManagementController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Customer> customersTable;

    @FXML
    private TableColumn<Customer, String> customerIdCol;

    @FXML
    private TableColumn<Customer, String> nameCol;

    @FXML
    private TableColumn<Customer, String> emailCol;

    @FXML
    private TableColumn<Customer, String> phoneCol;

    @FXML
    private TableColumn<Customer, String> nationalityCol;

    @FXML
    private TableColumn<Customer, String> totalBookingsCol;

    @FXML
    private TableColumn<Customer, String> loyaltyStatusCol;

    @FXML
    private TableColumn<Customer, Void> actionsCol;

    private ObservableList<Customer> allCustomers = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTableColumns();
        loadCustomers();
        setupSearchFilter();
    }

    private void setupTableColumns() {
        customerIdCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().userId)));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().email));
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().phone));
        nationalityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nationality));
        totalBookingsCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().totalBookings)));
        loyaltyStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().loyaltyStatus));

        // Actions column
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁 View Profile");
            private final HBox pane = new HBox(viewBtn);

            {
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");

                viewBtn.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleViewProfile(customer);
                });

                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadCustomers() {
        allCustomers.clear();
        try {
            Connection conn = ui.util.DataSource.getInstance().getConnection();
            String query = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone_number, u.nationality, " +
                          "v.loyalty_status, " +
                          "(SELECT COUNT(*) FROM reservations_hotel rh WHERE rh.voyageur_id = v.voyageur_id) as total_bookings " +
                          "FROM users u " +
                          "JOIN voyageurs v ON u.user_id = v.user_id " +
                          "WHERE u.user_type = 'VOYAGEUR' AND u.is_active = 1";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer customer = new Customer();
                customer.userId = rs.getInt("user_id");
                customer.name = rs.getString("first_name") + " " + rs.getString("last_name");
                customer.email = rs.getString("email");
                customer.phone = rs.getString("phone_number") != null ? rs.getString("phone_number") : "N/A";
                customer.nationality = rs.getString("nationality") != null ? rs.getString("nationality") : "N/A";
                customer.loyaltyStatus = rs.getString("loyalty_status");
                customer.totalBookings = rs.getInt("total_bookings");

                allCustomers.add(customer);
            }

            customersTable.setItems(allCustomers);

        } catch (Exception e) {
            System.err.println("❌ Error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                customersTable.setItems(allCustomers);
                return;
            }

            String search = newValue.toLowerCase();
            ObservableList<Customer> filtered = allCustomers.filtered(c ->
                c.name.toLowerCase().contains(search) ||
                c.email.toLowerCase().contains(search) ||
                String.valueOf(c.userId).contains(search)
            );
            customersTable.setItems(filtered);
        });
    }

    private void handleViewProfile(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Profile");
        alert.setHeaderText(customer.name);
        alert.setContentText(
            "Customer ID: " + customer.userId + "\n" +
            "Email: " + customer.email + "\n" +
            "Phone: " + customer.phone + "\n" +
            "Nationality: " + customer.nationality + "\n" +
            "Loyalty Status: " + customer.loyaltyStatus + "\n" +
            "Total Bookings: " + customer.totalBookings
        );
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchScene("/ui/employee-profile.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Inner class for customer data
    public static class Customer {
        int userId;
        String name;
        String email;
        String phone;
        String nationality;
        String loyaltyStatus;
        int totalBookings;
    }
}
