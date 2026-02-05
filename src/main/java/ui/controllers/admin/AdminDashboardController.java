package ui.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import ui.util.SceneManager;

public class AdminDashboardController {

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent p = loader.load();
            contentArea.getChildren().setAll(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}