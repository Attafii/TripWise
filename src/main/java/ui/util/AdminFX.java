package ui.util;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ui.admin.model.Role;
import ui.admin.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AdminFX {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static ReadOnlyStringWrapper readOnlyString(String value) {
        return new ReadOnlyStringWrapper(value);
    }
    public static ReadOnlyDoubleWrapper readOnlyNumber(double value) {
        return new ReadOnlyDoubleWrapper(value);
    }
    public static ReadOnlyBooleanWrapper readOnlyBoolean(boolean value) {
        return new ReadOnlyBooleanWrapper(value);
    }
    public static ReadOnlyStringWrapper formatDateTime(LocalDateTime dt) {
        String s = (dt == null) ? "" : dt.format(DT_FMT);
        return new ReadOnlyStringWrapper(s);
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

    // Minimal create/edit stub for demo
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

    // ui/util/AdminFX.java
    public static String fullCauseMessage(Throwable t) {
        if (t == null) return "";
        StringBuilder sb = new StringBuilder();
        Throwable cur = t;
        int depth = 0;
        while (cur != null && depth < 8) { // cap to avoid loops
            sb.append(cur.getClass().getName())
                    .append(": ")
                    .append(cur.getMessage() == null ? "" : cur.getMessage())
                    .append("\n");
            cur = cur.getCause();
            depth++;
        }
        return sb.toString();
    }
}