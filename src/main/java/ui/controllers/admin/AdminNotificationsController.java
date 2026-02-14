package ui.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.model.Notification;
import ui.admin.repository.NotificationRepository;
import ui.admin.repository.impl.MySqlNotificationRepository;
import ui.service.NotificationService;
import ui.util.AdminFX;

public class AdminNotificationsController {

    @FXML private TextField titleField;
    @FXML private TextArea bodyArea;
    @FXML private Label lastSentLabel;

    @FXML private TableView<Notification> historyTable;
    @FXML private TableColumn<Notification, String> colTime;
    @FXML private TableColumn<Notification, String> colTitle;
    @FXML private TableColumn<Notification, String> colBody;

    @FXML private Label lblPageInfo;

    // Repo + service
    private final MySqlNotificationRepository mysqlRepo = new MySqlNotificationRepository();
    private final NotificationRepository repo = mysqlRepo;
    // NotificationService uses a no-arg constructor; repository handles DB paging
    private final NotificationService service = new NotificationService();

    private final ObservableList<Notification> history = FXCollections.observableArrayList();

    // Paging
    private int pageIndex = 0;
    private final int pageSize = 20;
    private int totalRows = 0;

    @FXML
    private void initialize() {
        colTime.setCellValueFactory(c -> AdminFX.formatDateTime(c.getValue().getSentAt()));
        // Model provides backward-compatible getters: getTitle/getBody
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBody.setCellValueFactory(new PropertyValueFactory<>("body"));

        historyTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        updateCountsAndLoad();
    }

    @FXML
    private void onPreview() {
        AdminFX.info(
                "Preview",
                (titleField.getText() == null ? "" : titleField.getText()) + "\n\n" +
                        (bodyArea.getText() == null ? "" : bodyArea.getText())
        );
    }

    @FXML
    private void onSendToAll() {
        String title = titleField.getText();
        String body  = bodyArea.getText();
        if (title == null || title.isBlank() || body == null || body.isBlank()) {
            AdminFX.warn("Validation", "Please fill in both title and message.");
            return;
        }
        // Build a Notification using backward-compatible setters and save via repo
        Notification n = new Notification();
        n.setTitle(title);
        n.setBody(body);
        // Save using the repository which writes UUID id and sent_at
        repo.save(n);

        lastSentLabel.setText("Just now");
        titleField.clear();
        bodyArea.clear();
        pageIndex = 0;           // show newest from first page
        updateCountsAndLoad();
    }

    @FXML
    private void onDelete() {
        Notification sel = historyTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AdminFX.warn("Delete", "Select a notification in the table first.");
            return;
        }
        if (!AdminFX.confirm("Delete notification", "Are you sure you want to delete this notification?")) {
            return;
        }
        if (sel.getId() == null || sel.getId().isBlank()) {
            AdminFX.warn("Delete", "Selected row has no ID; cannot delete.");
            return;
        }
        mysqlRepo.deleteById(sel.getId());
        if (history.size() - 1 <= 0 && pageIndex > 0) pageIndex--; // go back a page if emptied
        updateCountsAndLoad();
    }

    @FXML
    private void onRefresh() {
        updateCountsAndLoad();
    }

    @FXML
    private void onPrevPage() {
        if (pageIndex > 0) {
            pageIndex--;
            loadPage();
        }
    }

    @FXML
    private void onNextPage() {
        if ((pageIndex + 1) * pageSize < totalRows) {
            pageIndex++;
            loadPage();
        }
    }

    private void updateCountsAndLoad() {
        totalRows = mysqlRepo.countHistory();
        int maxPageIndex = totalRows == 0 ? 0 : (totalRows - 1) / pageSize;
        if (pageIndex > maxPageIndex) pageIndex = maxPageIndex;
        loadPage();
    }

    private void loadPage() {
        int offset = pageIndex * pageSize;
        history.setAll(mysqlRepo.historyPage(pageSize, offset));
        historyTable.setItems(history);
        int totalPages = Math.max(1, (totalRows + pageSize - 1) / pageSize);
        lblPageInfo.setText("Page " + (pageIndex + 1) + " / " + totalPages);
    }
}