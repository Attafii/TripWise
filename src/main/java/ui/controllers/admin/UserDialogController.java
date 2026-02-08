package ui.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.model.Role;
import ui.model.User;

public class UserDialogController {
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private CheckBox activeCheck;

    private User model;

    public void setModel(User user) {
        this.model = user;
        if (user != null) {
            emailField.setText(user.getEmail());
            fullNameField.setText(user.getFullName());
            roleCombo.getSelectionModel().select(user.getRole());
            activeCheck.setSelected(user.isActive());
        }
    }

    @FXML
    private void initialize() {
        roleCombo.getItems().setAll(Role.ADMIN, Role.MANAGER, Role.USER);
        if (roleCombo.getSelectionModel().isEmpty()) {
            roleCombo.getSelectionModel().select(Role.USER);
        }
    }

    public User buildResult() {
        if (model == null) model = new User();
        model.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
        model.setFullName(fullNameField.getText() == null ? "" : fullNameField.getText().trim());
        model.setRole(roleCombo.getValue());
        model.setActive(activeCheck.isSelected());
        return model;
    }
}