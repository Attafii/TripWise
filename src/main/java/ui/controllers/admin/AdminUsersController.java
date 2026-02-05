package ui.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.admin.model.Role;
import ui.admin.model.User;
import ui.admin.repository.impl.InMemoryUserRepository;
import ui.admin.service.UserService;
import ui.util.AdminFX;

public class AdminUsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colId, colEmail, colFullName, colRole;
    @FXML private TableColumn<User, Boolean> colActive;

    private final UserService userService = new UserService(new InMemoryUserRepository());
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().getRole().name()));
        colActive.setCellValueFactory(c -> AdminFX.readOnlyBoolean(c.getValue().isActive()));
        refresh();
    }

    @FXML
    private void onCreate() {
        User u = AdminFX.userDialog(null);
        if (u != null) {
            if (u.getRole() == null) u.setRole(Role.USER);
            userService.create(u);
            refresh();
        }
    }

    @FXML
    private void onEdit() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        User updated = AdminFX.userDialog(selected);
        if (updated != null) {
            userService.update(updated);
            refresh();
        }
    }

    @FXML
    private void onDelete() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (AdminFX.confirm("Delete user", "Are you sure you want to delete this user?")) {
            userService.delete(selected.getId());
            refresh();
        }
    }

    private void refresh() {
        users.setAll(userService.all());
        usersTable.setItems(users);
    }
}