package ui.model;

import java.time.LocalDateTime;

/**
 * Flight Entity - Maps to 'vols' table
 */
public class Flight {
    private int volId;
    private String numeroVol;
    private int compagnieId;
    private int aeroportDepartId;
    private int aeroportArriveeId;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArrivee;
    private Integer dureeVol; // in minutes
    private String typeAvion;
    private int capaciteTotale;
    private int placesDisponibles;
    private StatutVol statutVol;
    private String porteEmbarquement;
    private String terminal;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Additional display fields (from joins)
    private String compagnieName;
    private String aeroportDepartName;
    private String aeroportArriveeName;
    private String villeDepart;
    private String villeArrivee;

    public enum StatutVol {
        PROGRAMME, EN_COURS, ATTERRI, ANNULE, RETARDE
    }

    // Constructors
    public Flight() {
        this.isActive = true;
        this.statutVol = StatutVol.PROGRAMME;
    }

    public Flight(String numeroVol, LocalDateTime dateDepart, LocalDateTime dateArrivee, int placesDisponibles) {
        this();
        this.numeroVol = numeroVol;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.placesDisponibles = placesDisponibles;
    }

    // Getters and Setters
    public int getVolId() { return volId; }
    public void setVolId(int volId) { this.volId = volId; }

    public String getNumeroVol() { return numeroVol; }
    public void setNumeroVol(String numeroVol) { this.numeroVol = numeroVol; }

    public int getCompagnieId() { return compagnieId; }
    public void setCompagnieId(int compagnieId) { this.compagnieId = compagnieId; }

    public int getAeroportDepartId() { return aeroportDepartId; }
    public void setAeroportDepartId(int aeroportDepartId) { this.aeroportDepartId = aeroportDepartId; }

    public int getAeroportArriveeId() { return aeroportArriveeId; }
    public void setAeroportArriveeId(int aeroportArriveeId) { this.aeroportArriveeId = aeroportArriveeId; }

    public LocalDateTime getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDateTime dateDepart) { this.dateDepart = dateDepart; }

    public LocalDateTime getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDateTime dateArrivee) { this.dateArrivee = dateArrivee; }

    public Integer getDureeVol() { return dureeVol; }
    public void setDureeVol(Integer dureeVol) { this.dureeVol = dureeVol; }

    public String getTypeAvion() { return typeAvion; }
    public void setTypeAvion(String typeAvion) { this.typeAvion = typeAvion; }

    public int getCapaciteTotale() { return capaciteTotale; }
    public void setCapaciteTotale(int capaciteTotale) { this.capaciteTotale = capaciteTotale; }

    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }

    public StatutVol getStatutVol() { return statutVol; }
    public void setStatutVol(StatutVol statutVol) { this.statutVol = statutVol; }

    public String getPorteEmbarquement() { return porteEmbarquement; }
    public void setPorteEmbarquement(String porteEmbarquement) { this.porteEmbarquement = porteEmbarquement; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCompagnieName() { return compagnieName; }
    public void setCompagnieName(String compagnieName) { this.compagnieName = compagnieName; }

    public String getAeroportDepartName() { return aeroportDepartName; }
    public void setAeroportDepartName(String aeroportDepartName) { this.aeroportDepartName = aeroportDepartName; }

    public String getAeroportArriveeName() { return aeroportArriveeName; }
    public void setAeroportArriveeName(String aeroportArriveeName) { this.aeroportArriveeName = aeroportArriveeName; }

    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }

    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String villeArrivee) { this.villeArrivee = villeArrivee; }

    public String getRoute() {
        return (villeDepart != null && villeArrivee != null)
            ? villeDepart + " → " + villeArrivee
            : numeroVol;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "numeroVol='" + numeroVol + '\'' +
                ", route='" + getRoute() + '\'' +
                ", dateDepart=" + dateDepart +
                ", places=" + placesDisponibles +
                '}';
    }
}

