package ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int paiementId;
    private Integer reservationHotelId;
    private Integer reservationVolId;
    private Integer reservationVehiculeId;
    private BigDecimal montant;
    private MethodePaiement methodePaiement;
    private StatutPaiement statutPaiement;
    private String transactionId;
    private LocalDateTime datePaiement;
    private LocalDateTime createdAt;

    public enum MethodePaiement {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, CASH, BANK_TRANSFER
    }

    public enum StatutPaiement {
        PENDING, COMPLETE, FAILED, REFUNDED
    }

    public Payment() {
        this.statutPaiement = StatutPaiement.PENDING;
        this.datePaiement = LocalDateTime.now();
    }

    public Payment(BigDecimal montant, MethodePaiement methodePaiement) {
        this();
        this.montant = montant;
        this.methodePaiement = methodePaiement;
    }

    public int getPaiementId() { return paiementId; }
    public void setPaiementId(int paiementId) { this.paiementId = paiementId; }

    public Integer getReservationHotelId() { return reservationHotelId; }
    public void setReservationHotelId(Integer reservationHotelId) { this.reservationHotelId = reservationHotelId; }

    public Integer getReservationVolId() { return reservationVolId; }
    public void setReservationVolId(Integer reservationVolId) { this.reservationVolId = reservationVolId; }

    public Integer getReservationVehiculeId() { return reservationVehiculeId; }
    public void setReservationVehiculeId(Integer reservationVehiculeId) { this.reservationVehiculeId = reservationVehiculeId; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public MethodePaiement getMethodePaiement() { return methodePaiement; }
    public void setMethodePaiement(MethodePaiement methodePaiement) { this.methodePaiement = methodePaiement; }

    public StatutPaiement getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(StatutPaiement statutPaiement) { this.statutPaiement = statutPaiement; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Payment{" +
                "paiementId=" + paiementId +
                ", montant=" + montant +
                ", methodePaiement=" + methodePaiement +
                ", statutPaiement=" + statutPaiement +
                ", transactionId='" + transactionId + '\'' +
                ", datePaiement=" + datePaiement +
                '}';
    }
}
