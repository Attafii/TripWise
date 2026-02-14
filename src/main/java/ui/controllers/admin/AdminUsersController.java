package ui.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.model.Role;
import ui.model.User;
import ui.admin.repository.UserRepository;
import ui.admin.repository.impl.MySqlUserRepository;
import ui.service.UserService;
import ui.util.AdminFX;

public class AdminUsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String>  colId, colEmail, colFullName, colRole;
    @FXML private TableColumn<User, Boolean> colActive;

    // Search box (already added in your FXML)
    @FXML private TextField searchField;

    // Repo/service
    private final UserRepository repo = new MySqlUserRepository();
    // UserService uses a no-arg constructor and provides add/getAll/delete(int)
    private final UserService userService = new UserService();

    // Data + wrappers
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private FilteredList<User> filtered;
    private SortedList<User> sorted;

    @FXML
    private void initialize() {
        // ----- Column bindings (null-safe) -----
        colId.setCellValueFactory(c -> AdminFX.readOnlyString(nullToEmpty(c.getValue().getId())));
        colEmail.setCellValueFactory(c -> AdminFX.readOnlyString(nullToEmpty(c.getValue().getEmail())));
        colFullName.setCellValueFactory(c -> AdminFX.readOnlyString(nullToEmpty(c.getValue().getFullName())));
        colActive.setCellValueFactory(c -> AdminFX.readOnlyBoolean(c.getValue().isActive()));

        // Value factory for role (kept for proper sorting)
        colRole.setCellValueFactory(c -> AdminFX.readOnlyString(
                c.getValue().getRole() == null ? "" : c.getValue().getRole().name()));

        // ----- ROLE CHIP RENDERER -----
        // This adds a colored "chip" per role (ADMIN, MANAGER, USER) using CSS classes you already added.
        colRole.setCellFactory(col -> new TableCell<>() {
            private final Label chip = new Label();
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null || role.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                String r = role.toUpperCase();
                chip.setText(r);
                chip.getStyleClass().setAll("chip"); // base chip style
                switch (r) {
                    case "ADMIN"   -> chip.getStyleClass().add("chip-role-admin");
                    case "MANAGER" -> chip.getStyleClass().add("chip-role-manager");
                    default        -> chip.getStyleClass().add("chip-role-user");
                }
                setGraphic(chip);
                setText(null);
            }
        });

        // ----- Filtering + sorting -----
        filtered = new FilteredList<>(users, u -> true);
        sorted   = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sorted);

        // Live search (email/full name; case-insensitive)
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> {
                final String q = (val == null ? "" : val.trim().toLowerCase());
                filtered.setPredicate(u -> {
                    if (q.isEmpty()) return true;
                    String email = safeLower(u.getEmail());
                    String name  = safeLower(u.getFullName());
                    return email.contains(q) || name.contains(q);
                });
            });
        }

        // Initial load
        refresh();
    }

    @FXML
    private void onCreate() {
        User u = AdminFX.userDialog(null);
        if (u != null) {
            if (u.getRole() == null) u.setRole(Role.USER);
            // UserService provides add(User)
            userService.add(u);
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
                // delete expects an int id
                userService.delete(selected.getUserId());
            }
            refresh();
        }
    }

    // Manual refresh button in the header
    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        // Load users on a background thread so the view always appears
        Task<java.util.List<User>> task = new Task<>() {
            @Override protected java.util.List<User> call() { return userService.getAll(); }
        };
        task.setOnSucceeded(e -> users.setAll(task.getValue()));
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            AdminFX.warn("Users",
                    "Could not load users.\n\n" + AdminFX.fullCauseMessage(task.getException()));
            users.clear();
        });
        new Thread(task, "load-users").start();
    }

    // ---- helpers ----
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static String safeLower(String s)    { return s == null ? "" : s.toLowerCase(); }
}