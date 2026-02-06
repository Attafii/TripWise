package ui.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.admin.model.Notification;
import ui.admin.repository.impl.InMemoryNotificationRepository;
import ui.admin.service.NotificationService;
import ui.util.AdminFX;

public class AdminNotificationsController {

    @FXML private TextField titleField;
    @FXML private TextArea bodyArea;
    @FXML private Label lastSentLabel;
    @FXML private TableView<Notification> historyTable;

    @FXML private TableColumn<Notification, String> colTime;
    @FXML private TableColumn<Notification, String> colTitle;
    @FXML private TableColumn<Notification, String> colBody;

    private final NotificationService service = new NotificationService(new InMemoryNotificationRepository());
    private final ObservableList<Notification> history = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Format LocalDateTime to String
        colTime.setCellValueFactory(c -> AdminFX.formatDateTime(c.getValue().getSentAt()));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBody.setCellValueFactory(new PropertyValueFactory<>("body"));
        refreshHistory();
    }

    @FXML
    private void onPreview() {
        AdminFX.info("Preview",
                (titleField.getText() == null ? "" : titleField.getText()) +
                        "\n\n" + (bodyArea.getText() == null ? "" : bodyArea.getText()));
    }

    @FXML
    private void onSendToAll() {
        String title = titleField.getText();
        String body = bodyArea.getText();
        if (title == null || title.isBlank() || body == null || body.isBlank()) {
            AdminFX.warn("Validation", "Please fill in both title and message.");
            return;
        }
        service.broadcast(title, body);
        lastSentLabel.setText("Just now");
        titleField.clear();
        bodyArea.clear();
        refreshHistory();
    }

    private void refreshHistory() {
        history.setAll(service.history());
        historyTable.setItems(history);
    }
}