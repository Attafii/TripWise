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

    private final ReimbursementService service = new ReimbursementService();

    @FXML
    public void initialize() {

        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));

        addActionButton();
        tableRequests.setItems(service.loadRequests());
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
                    System.out.println("Open details: " + clicked.getId());
                });

                setGraphic(btn);
            }
        });
    }
}