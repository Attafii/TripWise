package ui.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Booking Entity - represents a hotel booking
 * Maps to 'reservations_hotel' table
 */
public class HotelBooking {

    private int reservationId;
    private int voyageurId;
    private int hotelId;
    private int chambreId;
    private LocalDate dateCheckin;
    private LocalDate dateCheckout;
    private int nombreNuits;
    private int nombreAdultes;
    private int nombreEnfants;
    private double prixTotal;
    private StatutReservation statutReservation;
    private String demandesSpeciales;
    private String numeroConfirmation;
    private LocalDateTime dateReservation;

    // Additional display fields (from joins)
    private String hotelName;
    private String chambreType;
    private String guestName;
    private String guestEmail;

    public enum StatutReservation {
        EN_ATTENTE, CONFIRMEE, ANNULEE, TERMINEE
    }

    // Constructors
    public HotelBooking() {
        this.statutReservation = StatutReservation.EN_ATTENTE;
        this.dateReservation = LocalDateTime.now();
    }

    // Getters and Setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getVoyageurId() { return voyageurId; }
    public void setVoyageurId(int voyageurId) { this.voyageurId = voyageurId; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public int getChambreId() { return chambreId; }
    public void setChambreId(int chambreId) { this.chambreId = chambreId; }

    public LocalDate getDateCheckin() { return dateCheckin; }
    public void setDateCheckin(LocalDate dateCheckin) { this.dateCheckin = dateCheckin; }

    public LocalDate getDateCheckout() { return dateCheckout; }
    public void setDateCheckout(LocalDate dateCheckout) { this.dateCheckout = dateCheckout; }

    public int getNombreNuits() { return nombreNuits; }
    public void setNombreNuits(int nombreNuits) { this.nombreNuits = nombreNuits; }

    public int getNombreAdultes() { return nombreAdultes; }
    public void setNombreAdultes(int nombreAdultes) { this.nombreAdultes = nombreAdultes; }

    public int getNombreEnfants() { return nombreEnfants; }
    public void setNombreEnfants(int nombreEnfants) { this.nombreEnfants = nombreEnfants; }

    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }

    public StatutReservation getStatutReservation() { return statutReservation; }
    public void setStatutReservation(StatutReservation statutReservation) { this.statutReservation = statutReservation; }

    public String getDemandesSpeciales() { return demandesSpeciales; }
    public void setDemandesSpeciales(String demandesSpeciales) { this.demandesSpeciales = demandesSpeciales; }

    public String getNumeroConfirmation() { return numeroConfirmation; }
    public void setNumeroConfirmation(String numeroConfirmation) { this.numeroConfirmation = numeroConfirmation; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    // Display fields
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getChambreType() { return chambreType; }
    public void setChambreType(String chambreType) { this.chambreType = chambreType; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getBookingId() {
        return "BK" + String.format("%04d", reservationId);
    }

    public String getStatusDisplay() {
        switch (statutReservation) {
            case CONFIRMEE: return "Confirmed";
            case EN_ATTENTE: return "Pending";
            case ANNULEE: return "Cancelled";
            case TERMINEE: return "Completed";
            default: return statutReservation.name();
        }
    }

    @Override
    public String toString() {
        return "HotelBooking{" +
                "reservationId=" + reservationId +
                ", hotel='" + hotelName + '\'' +
                ", checkin=" + dateCheckin +
                ", checkout=" + dateCheckout +
                ", status=" + statutReservation +
                '}';
    }
}
