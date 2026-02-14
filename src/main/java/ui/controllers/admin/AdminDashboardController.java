package ui.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import ui.util.SceneManager;

public class AdminDashboardController {

    // Keep ONE declaration only; fx:id="contentArea" must match admin-dashboard.fxml
    @FXML private StackPane contentArea;

    @FXML
    private void initialize() {
        openUsers(); // default tab on open
    }

    @FXML
    private void openUsers() { load("/ui/admin/admin-users.fxml"); }

    @FXML
    private void openReservations() { load("/ui/admin/admin-reservations.fxml"); }

    @FXML
    private void openNotifications() { load("/ui/admin/admin-notifications.fxml"); }

    @FXML
    private void openReports() { load("/ui/admin/admin-reports.fxml"); }

    @FXML
    private void backToApp() {
        SceneManager.switchScene("/ui/dashboard.fxml");
    }

    private void load(String fxml) {
        try {
            System.out.println("[NAV] Loading: " + fxml);

            var url = getClass().getResource(fxml);
            if (url == null) {
                showError("View not found",
                        "FXML resource not on classpath:\n" + fxml,
                        null);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load(); // IMPORTANT: explicit type

            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            showError("Failed to open view",
                    "An error occurred while opening:\n" + fxml,
                    ex);
        }
    }

    private void showError(String title, String msg, Throwable t) {
        if (t != null) t.printStackTrace();
        var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg + (t != null ? ("\n\n" + ui.util.AdminFX.fullCauseMessage(t)) : ""));
        a.show();
    }
}