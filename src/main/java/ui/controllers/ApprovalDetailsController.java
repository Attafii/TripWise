package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.model.ReimbursementRequest;
import ui.util.ReimbursementService;

public class ApprovalDetailsController {

    @FXML private Label empLabel;
    @FXML private Label refLabel;
    @FXML private Label amountLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;

    @FXML private TextArea rejectComment;
    @FXML private Button approveBtn;
    @FXML private Button rejectBtn;

    private final ReimbursementService service = new ReimbursementService();
    private ReimbursementRequest request;
    private Runnable onDone;

    /** Called by the parent controller to pass data */
    public void setData(ReimbursementRequest r, Runnable onDone) {
        this.request = r;
        this.onDone = onDone;

        empLabel.setText(r.getEmployeeName());
        refLabel.setText(r.getReference());
        amountLabel.setText(String.format("%.2f", r.getAmount()));
        dateLabel.setText(r.getDate() != null ? r.getDate().toString() : "-");
        statusLabel.setText(r.getStatus());
    }

    @FXML
    private void onApprove() {
        try {
            service.approve(request.getId(), 1); // TODO: replace 1 with real approverId
            closeWithRefresh();
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML
    private void onReject() {
        String comment = (rejectComment.getText() == null) ? "" : rejectComment.getText().trim();
        if (comment.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Commentaire obligatoire pour le rejet.").showAndWait();
            return;
        }
        try {
            service.reject(request.getId(), 1, comment); // TODO: replace 1 with real approverId
            closeWithRefresh();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void closeWithRefresh() {
        if (onDone != null) onDone.run();
        ((javafx.stage.Stage) approveBtn.getScene().getWindow()).close();
    }

    private void showError(Exception e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
    }
}