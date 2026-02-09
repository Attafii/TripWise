package ui.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ui.model.ReimbursementRequest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReimbursementService {

    // ====== TABLE & COLUMN NAMES (match your MySQL) ======
    private static final String T_REQ = "reimbursement_requests";
    private static final String T_EMP = "employees";

    // Columns in reimbursement_requests
    private static final String C_ID = "id";
    private static final String C_EMP_ID = "employee_id";
    private static final String C_DATE = "date_submitted";
    private static final String C_AMOUNT = "amount";
    private static final String C_STATUS = "status";
    private static final String C_REF = "reference";
    private static final String C_MANAGER_COMMENT = "manager_comment";
    private static final String C_UPDATED_AT = "updated_at";

    // Columns in employees
    private static final String C_EMP_FULLNAME = "full_name";

    /**
     * Load PENDING reimbursement requests with employee full name.
     * Maps DB columns to UI model via SQL aliases:
     *   full_name          -> employeeName
     *   date_submitted     -> date
     *   amount             -> amount
     *   status             -> status
     *   reference          -> reference
     */
    public ObservableList<ReimbursementRequest> loadRequests() {
        ObservableList<ReimbursementRequest> list = FXCollections.observableArrayList();

        String sql = """
            SELECT
                r.%s AS id,
                e.%s AS employeeName,
                r.%s AS date,
                r.%s AS amount,
                r.%s AS status,
                r.%s AS reference
            FROM %s r
            JOIN %s e ON e.id = r.%s
            WHERE LOWER(r.%s) = 'pending'
            ORDER BY r.%s DESC
            """.formatted(
                C_ID, C_EMP_FULLNAME, C_DATE, C_AMOUNT, C_STATUS, C_REF,
                T_REQ, T_EMP, C_EMP_ID, C_STATUS, C_DATE
        );

        System.out.println("[ReimbursementService] SQL =>\n" + sql);

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int rows = 0;
            while (rs.next()) {
                rows++;

                int id = rs.getInt("id");
                String employeeName = rs.getString("employeeName");

                Date sqlDate = rs.getDate("date");
                LocalDate date = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                double amount = rs.getDouble("amount");
                String status = rs.getString("status");
                String reference = rs.getString("reference");

                list.add(new ReimbursementRequest(id, employeeName, date, amount, status, reference));
            }
            System.out.println("[ReimbursementService] Loaded rows: " + rows);
            if (rows == 0) {
                System.out.println("[ReimbursementService] No PENDING rows found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DB loadRequests() failed: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Approve request: set status='APPROVED', updated_at=NOW(), optionally clear/add manager_comment.
     * Use a real approverId if you have one in your auth; not needed by DB here.
     */
    public void approve(int requestId, int approverId) throws Exception {
        String sql = """
            UPDATE %s
            SET %s='APPROVED', %s=NOW()
            WHERE %s=?
            """.formatted(T_REQ, C_STATUS, C_UPDATED_AT, C_ID);

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            int updated = ps.executeUpdate();
            System.out.println("[ReimbursementService] Approve updated rows: " + updated);
        }
    }

    /**
     * Reject request: set status='REJECTED', store manager_comment, updated_at=NOW().
     */
    public void reject(int requestId, int approverId, String managerComment) throws Exception {
        if (managerComment == null || managerComment.trim().isEmpty()) {
            throw new IllegalArgumentException("Commentaire obligatoire pour le rejet.");
        }

        String sql = """
            UPDATE %s
            SET %s='REJECTED', %s=?, %s=NOW()
            WHERE %s=?
            """.formatted(T_REQ, C_STATUS, C_MANAGER_COMMENT, C_UPDATED_AT, C_ID);

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, managerComment);
            ps.setInt(2, requestId);
            int updated = ps.executeUpdate();
            System.out.println("[ReimbursementService] Reject updated rows: " + updated);
        }
    }

    /**
     * Count pending (for a badge if needed).
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM %s WHERE LOWER(%s)='pending'".formatted(T_REQ, C_STATUS);
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int count = rs.getInt(1);
            System.out.println("[ReimbursementService] Pending count: " + count);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}