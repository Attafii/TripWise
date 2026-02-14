package ui.service;

import ui.model.Payment;
import ui.util.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaymentService implements IService<Payment> {

    private final Connection connection;

    public PaymentService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(Payment payment) {
        String query = "INSERT INTO paiements (reservation_hotel_id, reservation_vol_id, reservation_vehicule_id, " +
                      "montant, methode_paiement, statut_paiement, transaction_id, date_paiement, created_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, payment.getReservationHotelId());
            stmt.setObject(2, payment.getReservationVolId());
            stmt.setObject(3, payment.getReservationVehiculeId());
            stmt.setBigDecimal(4, payment.getMontant());
            stmt.setString(5, payment.getMethodePaiement() != null ? payment.getMethodePaiement().name() : "CREDIT_CARD");
            stmt.setString(6, payment.getStatutPaiement() != null ? payment.getStatutPaiement().name() : "PENDING");
            
            String transactionId = generateTransactionId();
            payment.setTransactionId(transactionId);
            stmt.setString(7, transactionId);
            
            stmt.setTimestamp(8, payment.getDatePaiement() != null ? Timestamp.valueOf(payment.getDatePaiement()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    payment.setPaiementId(rs.getInt(1));
                }
                System.out.println("✅ Payment added successfully: " + transactionId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding payment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Payment payment) {
        String query = "UPDATE paiements SET montant=?, methode_paiement=?, statut_paiement=?, date_paiement=? " +
                      "WHERE paiement_id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBigDecimal(1, payment.getMontant());
            stmt.setString(2, payment.getMethodePaiement() != null ? payment.getMethodePaiement().name() : "CREDIT_CARD");
            stmt.setString(3, payment.getStatutPaiement() != null ? payment.getStatutPaiement().name() : "PENDING");
            stmt.setTimestamp(4, payment.getDatePaiement() != null ? Timestamp.valueOf(payment.getDatePaiement()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, payment.getPaiementId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Payment updated successfully (ID: " + payment.getPaiementId() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating payment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        System.err.println("❌ Cannot delete payments - financial records are immutable");
        return false;
    }

    @Override
    public Payment getById(int id) {
        String query = "SELECT * FROM paiements WHERE paiement_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractPaymentFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting payment by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Payment> getAll() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM paiements ORDER BY date_paiement DESC LIMIT 100";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + payments.size() + " payments from database");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all payments: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    public List<Payment> getPaymentsByUser(int userId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT p.* FROM paiements p " +
                      "LEFT JOIN reservations_hotel rh ON p.reservation_hotel_id = rh.reservation_id " +
                      "LEFT JOIN reservations_vol rv ON p.reservation_vol_id = rv.reservation_id " +
                      "LEFT JOIN reservations_vehicule rvh ON p.reservation_vehicule_id = rvh.reservation_id " +
                      "WHERE rh.voyageur_id = ? OR rv.voyageur_id = ? OR rvh.voyageur_id = ? " +
                      "ORDER BY p.date_paiement DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting payments by user: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        Payment payment = new Payment();

        payment.setPaiementId(rs.getInt("paiement_id"));
        
        Integer hotelId = rs.getInt("reservation_hotel_id");
        payment.setReservationHotelId(rs.wasNull() ? null : hotelId);
        
        Integer volId = rs.getInt("reservation_vol_id");
        payment.setReservationVolId(rs.wasNull() ? null : volId);
        
        Integer vehiculeId = rs.getInt("reservation_vehicule_id");
        payment.setReservationVehiculeId(rs.wasNull() ? null : vehiculeId);
        
        payment.setMontant(rs.getBigDecimal("montant"));
        
        String methode = rs.getString("methode_paiement");
        if (methode != null) {
            payment.setMethodePaiement(Payment.MethodePaiement.valueOf(methode));
        }
        
        String statut = rs.getString("statut_paiement");
        if (statut != null) {
            payment.setStatutPaiement(Payment.StatutPaiement.valueOf(statut));
        }
        
        payment.setTransactionId(rs.getString("transaction_id"));
        
        Timestamp datePaiement = rs.getTimestamp("date_paiement");
        if (datePaiement != null) {
            payment.setDatePaiement(datePaiement.toLocalDateTime());
        }
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            payment.setCreatedAt(created.toLocalDateTime());
        }

        return payment;
    }

    private String generateTransactionId() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(10000);
        return String.format("TXN-%d-%04d", timestamp, random);
    }
}
