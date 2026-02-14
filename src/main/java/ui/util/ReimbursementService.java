package ui.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ui.model.ReimbursementRequest;

import java.time.LocalDate;

public class ReimbursementService {

    public ObservableList<ReimbursementRequest> loadRequests() {
        return FXCollections.observableArrayList(
                new ReimbursementRequest(1, "John Doe", LocalDate.now(), 120.5, "Pending", "REF2024A"),
                new ReimbursementRequest(2, "Sarah Miller", LocalDate.now().minusDays(2), 87.2, "Pending", "REF2024B")
        );
    }

    public void approve(int id) {
        System.out.println("Approved: " + id);
    }

    public void reject(int id, String reason) {
        System.out.println("Rejected: " + id + " - " + reason);
    }
}