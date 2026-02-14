package ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FlightBooking Entity - represents a flight booking/reservation
 * Maps to 'reservations_vol' table
 */
public class FlightBooking {

    private int reservationId;
    private int voyageurId;
    private int volId;
    private int classeVolId;
    private LocalDateTime dateReservation;
    private int nombrePassagers;
    private BigDecimal prixTotal;
    private StatutReservation statutReservation;
    private String numeroConfirmation;
    private String siegesAttribues;
    private String bagage;
    private String repas;
    private String demandesSpeciales;

    // Display fields from joins
    private String numeroVol;
    private String compagnieName;
    private String villeDepart;
    private String villeArrivee;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArrivee;
    private String classeNom;
    private String passengerName;
    private String passengerEmail;

    public enum StatutReservation {
        EN_ATTENTE, CONFIRMEE, ANNULEE, EMBARQUEE, TERMINEE
    }

    public FlightBooking() {
        this.statutReservation = StatutReservation.EN_ATTENTE;
        this.dateReservation = LocalDateTime.now();
    }

    // Getters and Setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getVoyageurId() { return voyageurId; }
    public void setVoyageurId(int voyageurId) { this.voyageurId = voyageurId; }

    public int getVolId() { return volId; }
    public void setVolId(int volId) { this.volId = volId; }

    public int getClasseVolId() { return classeVolId; }
    public void setClasseVolId(int classeVolId) { this.classeVolId = classeVolId; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public int getNombrePassagers() { return nombrePassagers; }
    public void setNombrePassagers(int nombrePassagers) { this.nombrePassagers = nombrePassagers; }

    public BigDecimal getPrixTotal() { return prixTotal; }
    public void setPrixTotal(BigDecimal prixTotal) { this.prixTotal = prixTotal; }

    public StatutReservation getStatutReservation() { return statutReservation; }
    public void setStatutReservation(StatutReservation statutReservation) { this.statutReservation = statutReservation; }

    public String getNumeroConfirmation() { return numeroConfirmation; }
    public void setNumeroConfirmation(String numeroConfirmation) { this.numeroConfirmation = numeroConfirmation; }

    public String getSiegesAttribues() { return siegesAttribues; }
    public void setSiegesAttribues(String siegesAttribues) { this.siegesAttribues = siegesAttribues; }

    public String getBagage() { return bagage; }
    public void setBagage(String bagage) { this.bagage = bagage; }

    public String getRepas() { return repas; }
    public void setRepas(String repas) { this.repas = repas; }

    public String getDemandesSpeciales() { return demandesSpeciales; }
    public void setDemandesSpeciales(String demandesSpeciales) { this.demandesSpeciales = demandesSpeciales; }

    // Display fields
    public String getNumeroVol() { return numeroVol; }
    public void setNumeroVol(String numeroVol) { this.numeroVol = numeroVol; }

    public String getCompagnieName() { return compagnieName; }
    public void setCompagnieName(String compagnieName) { this.compagnieName = compagnieName; }

    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }

    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String villeArrivee) { this.villeArrivee = villeArrivee; }

    public LocalDateTime getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDateTime dateDepart) { this.dateDepart = dateDepart; }

    public LocalDateTime getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDateTime dateArrivee) { this.dateArrivee = dateArrivee; }

    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }

    public String getRoute() {
        return villeDepart + " → " + villeArrivee;
    }

    public String getBookingId() {
        return "FL" + String.format("%04d", reservationId);
    }

    public String getStatusDisplay() {
        switch (statutReservation) {
            case CONFIRMEE: return "Confirmed";
            case EN_ATTENTE: return "Pending";
            case ANNULEE: return "Cancelled";
            case EMBARQUEE: return "Boarded";
            case TERMINEE: return "Completed";
            default: return statutReservation.name();
        }
    }

    public double getPriceAsDouble() {
        return prixTotal != null ? prixTotal.doubleValue() : 0.0;
    }

    @Override
    public String toString() {
        return "FlightBooking{" +
                "reservationId=" + reservationId +
                ", flight='" + numeroVol + '\'' +
                ", route='" + getRoute() + '\'' +
                ", status=" + statutReservation +
                '}';
    }
}
