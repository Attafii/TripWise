package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.model.ReimbursementRequest;
import ui.util.ReimbursementService;

public class ApprovalsController {

    @FXML private TableView<ReimbursementRequest> tableRequests;
    @FXML private TableColumn<ReimbursementRequest, String> colEmployee;
    @FXML private TableColumn<ReimbursementRequest, String> colDate;
    @FXML private TableColumn<ReimbursementRequest, Double> colAmount;
    @FXML private TableColumn<ReimbursementRequest, String> colStatus;
    @FXML private TableColumn<ReimbursementRequest, String> colReference;
    @FXML private TableColumn<ReimbursementRequest, Void> colAction;

    // pending badge (in approvals.fxml top HBox)
    @FXML private Label pendingCountLabel;

    private final ReimbursementService service = new ReimbursementService();

    @FXML
    public void initialize() {
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));

        addActionButton();
        refresh();
    }

    private void refresh() {
        try {
            var items = service.loadRequests();
            tableRequests.setItems(items);

            // update badge
            if (pendingCountLabel != null) {
                int count = (items != null) ? items.size() : 0;
                pendingCountLabel.setText(count > 0 ? ("En attente: " + count) : "");
                pendingCountLabel.setVisible(count > 0);
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void addActionButton() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Voir");
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                btn.setOnAction(event -> {
                    ReimbursementRequest clicked = getTableView().getItems().get(getIndex());
                    openDetails(clicked);
                });
                setGraphic(btn);
            }
        });
    }

    private void openDetails(ReimbursementRequest r) {
        try {
            java.net.URL fxml = getClass().getResource("/ui/approval-details.fxml");
            if (fxml == null) {
                throw new IllegalStateException("approval-details.fxml not found at /ui/approval-details.fxml (check src/main/resources/ui/)");
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxml);
            javafx.scene.Parent root = loader.load();

            ApprovalDetailsController ctrl = loader.getController();
            ctrl.setData(r, this::refresh);

            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle("Détails - " + r.getReference());
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            String msg = (cause != null) ? cause.toString() : e.toString();
            new Alert(Alert.AlertType.ERROR, "Cannot open details: " + msg).showAndWait();
        }
    }
}