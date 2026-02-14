package ui.controllers.admin;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ui.model.User;
import ui.model.User.UserType;
import ui.service.UserService;
import ui.util.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Admin User Management Controller
 * Complete CRUD operations with real-time validation
 */
public class AdminUserController {

    @FXML private StackPane rootPane;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colType;
    @FXML private TableColumn<User, String> colStatus;
    
    // Form fields
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<UserType> cbUserType;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private TextField txtNationality;
    @FXML private TextField txtPassport;
    @FXML private TextArea txtAddress;
    @FXML private CheckBox chkActive;
    
    // Search & filter
    @FXML private TextField txtSearch;
    @FXML private ComboBox<UserType> cbFilterType;
    @FXML private ComboBox<String> cbFilterStatus;
    
    // Buttons
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnExportPDF;
    @FXML private Button btnExportExcel;
    
    // Validation labels
    @FXML private Label lblEmailError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblPasswordError;
    
    private final UserService userService;
    private ObservableList<User> userList;
    private User selectedUser;
    
    public AdminUserController() {
        this.userService = new UserService();
    }
    
    @FXML
    public void initialize() {
        setupTable();
        setupFormValidation();
        setupFilters();
        loadUsers();
        playEntranceAnimation();
    }
    
    /**
     * Setup table columns
     */
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colType.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserType().toString()));
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
        
        // Row selection
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                populateForm(newSelection);
            }
        });
        
        // Apply alternating row colors
        userTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f9fafb;");
                    } else {
                        setStyle("-fx-background-color: white;");
                    }
                }
            }
        });
    }
    
    /**
     * Setup real-time form validation
     */
    private void setupFormValidation() {
        // Email validation
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            String error = ValidationUtil.getEmailError(newVal);
            if (error != null) {
                lblEmailError.setText(error);
                lblEmailError.setVisible(true);
                txtEmail.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            } else {
                lblEmailError.setVisible(false);
                txtEmail.setStyle("-fx-border-color: #10b981; -fx-border-width: 2px;");
            }
        });
        
        // Phone validation
        txtPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                String error = ValidationUtil.getPhoneError(newVal);
                if (error != null) {
                    lblPhoneError.setText(error);
                    lblPhoneError.setVisible(true);
                    txtPhone.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
                } else {
                    lblPhoneError.setVisible(false);
                    txtPhone.setStyle("-fx-border-color: #10b981; -fx-border-width: 2px;");
                }
            }
        });
        
        // Password validation
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                String error = ValidationUtil.getPasswordError(newVal);
                if (error != null) {
                    lblPasswordError.setText(error);
                    lblPasswordError.setVisible(true);
                    txtPassword.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
                } else {
                    lblPasswordError.setVisible(false);
                    txtPassword.setStyle("-fx-border-color: #10b981; -fx-border-width: 2px;");
                }
            }
        });
    }
    
    /**
     * Setup filters
     */
    private void setupFilters() {
        // User type filter
        cbUserType.setItems(FXCollections.observableArrayList(UserType.values()));
        cbFilterType.setItems(FXCollections.observableArrayList(UserType.values()));
        
        // Status filter
        cbFilterStatus.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        cbFilterStatus.setValue("All");
        
        // Search listener
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());
        cbFilterType.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers());
        cbFilterStatus.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers());
    }
    
    /**
     * Load all users
     */
    private void loadUsers() {
        userList = FXCollections.observableArrayList(userService.getAll());
        userTable.setItems(userList);
    }
    
    /**
     * Filter users based on search and filters
     */
    private void filterUsers() {
        String searchText = txtSearch.getText().toLowerCase();
        UserType filterType = cbFilterType.getValue();
        String filterStatus = cbFilterStatus.getValue();
        
        ObservableList<User> filteredList = FXCollections.observableArrayList();
        
        for (User user : userList) {
            boolean matchesSearch = searchText.isEmpty() || 
                user.getFullName().toLowerCase().contains(searchText) ||
                user.getEmail().toLowerCase().contains(searchText);
            
            boolean matchesType = filterType == null || user.getUserType() == filterType;
            
            boolean matchesStatus = "All".equals(filterStatus) ||
                ("Active".equals(filterStatus) && user.isActive()) ||
                ("Inactive".equals(filterStatus) && !user.isActive());
            
            if (matchesSearch && matchesType && matchesStatus) {
                filteredList.add(user);
            }
        }
        
        userTable.setItems(filteredList);
    }
    
    /**
     * Add new user
     */
    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }
        
        User newUser = new User();
        populateUserFromForm(newUser);
        
        if (userService.add(newUser)) {
            NotificationUtil.showSuccess("User added successfully!", rootPane);
            loadUsers();
            clearForm();
        } else {
            NotificationUtil.showError("Failed to add user", rootPane);
        }
    }
    
    /**
     * Update existing user
     */
    @FXML
    private void handleUpdate() {
        if (selectedUser == null) {
            NotificationUtil.showWarning("Please select a user to update", rootPane);
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        if (DialogUtil.showConfirmation("Update User", "Are you sure you want to update this user?")) {
            populateUserFromForm(selectedUser);
            
            if (userService.update(selectedUser)) {
                NotificationUtil.showSuccess("User updated successfully!", rootPane);
                loadUsers();
                clearForm();
            } else {
                NotificationUtil.showError("Failed to update user", rootPane);
            }
        }
    }
    
    /**
     * Delete user
     */
    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            NotificationUtil.showWarning("Please select a user to delete", rootPane);
            return;
        }
        
        if (DialogUtil.showDeleteConfirmation(selectedUser.getFullName())) {
            if (userService.delete(selectedUser.getUserId())) {
                NotificationUtil.showSuccess("User deleted successfully!", rootPane);
                loadUsers();
                clearForm();
            } else {
                NotificationUtil.showError("Failed to delete user", rootPane);
            }
        }
    }
    
    /**
     * Export to PDF
     */
    @FXML
    private void handleExportPDF() {
        if (ExportUtil.exportToPDF(userTable, "User_Management")) {
            NotificationUtil.showSuccess("PDF exported successfully!", rootPane);
        } else {
            NotificationUtil.showError("Failed to export PDF", rootPane);
        }
    }
    
    /**
     * Export to Excel
     */
    @FXML
    private void handleExportExcel() {
        if (ExcelExportUtil.exportToExcel(userTable, "User Management")) {
            NotificationUtil.showSuccess("Excel exported successfully!", rootPane);
        } else {
            NotificationUtil.showError("Failed to export Excel", rootPane);
        }
    }
    
    /**
     * Clear form
     */
    @FXML
    private void handleClear() {
        clearForm();
    }
    
    /**
     * Validate form fields
     */
    private boolean validateForm() {
        if (!ValidationUtil.isRequired(txtFirstName.getText())) {
            NotificationUtil.showError("First name is required", rootPane);
            return false;
        }
        
        if (!ValidationUtil.isRequired(txtLastName.getText())) {
            NotificationUtil.showError("Last name is required", rootPane);
            return false;
        }
        
        if (!ValidationUtil.isValidEmail(txtEmail.getText())) {
            NotificationUtil.showError("Valid email is required", rootPane);
            return false;
        }
        
        if (selectedUser == null && !ValidationUtil.isValidPassword(txtPassword.getText())) {
            NotificationUtil.showError("Valid password is required (min 8 chars, letter + number)", rootPane);
            return false;
        }
        
        if (cbUserType.getValue() == null) {
            NotificationUtil.showError("User type is required", rootPane);
            return false;
        }
        
        return true;
    }
    
    /**
     * Populate form from user
     */
    private void populateForm(User user) {
        txtFirstName.setText(user.getFirstName());
        txtLastName.setText(user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhoneNumber());
        txtPassword.clear(); // Don't show password
        cbUserType.setValue(user.getUserType());
        dpDateOfBirth.setValue(user.getDateOfBirth());
        txtNationality.setText(user.getNationality());
        txtPassport.setText(user.getPassportNumber());
        txtAddress.setText(user.getAddress());
        chkActive.setSelected(user.isActive());
    }
    
    /**
     * Populate user from form
     */
    private void populateUserFromForm(User user) {
        user.setFirstName(txtFirstName.getText());
        user.setLastName(txtLastName.getText());
        user.setEmail(txtEmail.getText());
        user.setPhoneNumber(txtPhone.getText());
        
        // Password will be hashed automatically in UserService.add() method
        if (!txtPassword.getText().isEmpty()) {
            user.setPasswordHash(txtPassword.getText());
        }
        
        user.setUserType(cbUserType.getValue());
        user.setDateOfBirth(dpDateOfBirth.getValue());
        user.setNationality(txtNationality.getText());
        user.setPassportNumber(txtPassport.getText());
        user.setAddress(txtAddress.getText());
        user.setActive(chkActive.isSelected());
    }
    
    /**
     * Clear form fields
     */
    private void clearForm() {
        selectedUser = null;
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtPassword.clear();
        cbUserType.setValue(null);
        dpDateOfBirth.setValue(null);
        txtNationality.clear();
        txtPassport.clear();
        txtAddress.clear();
        chkActive.setSelected(true);
        
        lblEmailError.setVisible(false);
        lblPhoneError.setVisible(false);
        lblPasswordError.setVisible(false);
        
        txtEmail.setStyle("");
        txtPhone.setStyle("");
        txtPassword.setStyle("");
        
        userTable.getSelectionModel().clearSelection();
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
