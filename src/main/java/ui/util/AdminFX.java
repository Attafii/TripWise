package ui.util;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ui.admin.model.Role;
import ui.admin.model.User;

import java.util.Optional;

public class AdminFX {
    public static ReadOnlyStringWrapper readOnlyString(String value) {
        return new ReadOnlyStringWrapper(value);
    }
    public static ReadOnlyDoubleWrapper readOnlyNumber(double value) {
        return new ReadOnlyDoubleWrapper(value);
    }
    public static ReadOnlyBooleanWrapper readOnlyBoolean(boolean value) {
        return new ReadOnlyBooleanWrapper(value);
    }

    public static boolean confirm(String title, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        a.setTitle(title);
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.YES;
    }

    public static void info(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    public static void warn(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    /**
     * Minimal create/edit stub (no FXML).
     * For demo purposes: on create -> default values; on edit -> toggle active and cycle role.
     */
    public static User userDialog(User existing) {
        if (existing == null) {
            User u = new User();
            u.setEmail("new.user@tripwise.com");
            u.setFullName("New User");
            u.setRole(Role.USER);
            u.setActive(true);
            return u;
        } else {
            existing.setActive(!existing.isActive());
            switch (existing.getRole()) {
                case USER -> existing.setRole(Role.MANAGER);
                case MANAGER -> existing.setRole(Role.ADMIN);
                default -> existing.setRole(Role.USER);
            }
            return existing;
        }
    }
}