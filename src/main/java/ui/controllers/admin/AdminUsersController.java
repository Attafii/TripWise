package ui.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.admin.model.Role;
import ui.admin.model.User;
import ui.admin.repository.UserRepository;
import ui.admin.repository.impl.MySqlUserRepository;
import ui.admin.service.UserService;
import ui.util.AdminFX;

public class AdminUsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colId, colEmail, colFullName, colRole;
    @FXML private TableColumn<User, Boolean> colActive;

    // Switch to MySQL repo here
    private final UserRepository repo = new MySqlUserRepository();
    private final UserService userService = new UserService(repo);
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Lambdas binding
        colId.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().getId()));
        colEmail.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().getEmail()));
        colFullName.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().getFullName()));
        colRole.setCellValueFactory(c -> AdminFX.readOnlyString(c.getValue().getRole().name()));
        colActive.setCellValueFactory(c -> AdminFX.readOnlyBoolean(c.getValue().isActive()));

        refresh(); // async (see below)
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
            if (repo instanceof MySqlUserRepository mysql) {
                mysql.deleteByEmail(selected.getEmail());
            } else {
                userService.delete(selected.getId());
            }
            refresh();
        }
    }

    private void refresh() {
        // Load users on a background thread so the view always appears
        Task<java.util.List<User>> task = new Task<>() {
            @Override protected java.util.List<User> call() { return userService.all(); }
        };
        task.setOnSucceeded(e -> {
            users.setAll(task.getValue());
            usersTable.setItems(users);
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            AdminFX.warn("Users",
                    "Could not load users.\n\n" +
                            AdminFX.fullCauseMessage(task.getException()));
            users.clear();
            usersTable.setItems(users);
        });
        new Thread(task, "load-users").start();
    }
}