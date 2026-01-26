package ui.model;

import java.math.BigDecimal;

/**
 * Car (Vehicule) Entity - Maps to 'vehicules' table
 */
public class Car {
    private int vehiculeId;
    private int compagnieId;
    private String marque;
    private String modele;
    private Integer annee;
    private Categorie categorie;
    private Transmission transmission;
    private Carburant carburant;
    private int nombrePlaces;
    private Integer nombrePortes;
    private boolean climatisation;
    private boolean gps;
    private String imageUrl;
    private BigDecimal prixJour;
    private BigDecimal caution;
    private boolean kilometrageIllimite;
    private boolean isAvailable;

    // Display field from join
    private String compagnieName;

    public enum Categorie {
        ECONOMIQUE, COMPACTE, INTERMEDIAIRE, SUV, LUXE, MONOSPACE
    }

    public enum Transmission {
        MANUELLE, AUTOMATIQUE
    }

    public enum Carburant {
        ESSENCE, DIESEL, ELECTRIQUE, HYBRIDE
    }

    public Car() {
        this.climatisation = true;
        this.kilometrageIllimite = true;
        this.isAvailable = true;
    }

    public Car(String model, String type, double pricePerDay) {
        this();
        this.modele = model;
        this.prixJour = BigDecimal.valueOf(pricePerDay);
        // Parse type to categorie
        try {
            this.categorie = Categorie.valueOf(type.toUpperCase());
        } catch (Exception e) {
            this.categorie = Categorie.ECONOMIQUE;
        }
    }

    // Getters and Setters
    public int getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(int vehiculeId) { this.vehiculeId = vehiculeId; }

    public int getCompagnieId() { return compagnieId; }
    public void setCompagnieId(int compagnieId) { this.compagnieId = compagnieId; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Transmission getTransmission() { return transmission; }
    public void setTransmission(Transmission transmission) { this.transmission = transmission; }

    public Carburant getCarburant() { return carburant; }
    public void setCarburant(Carburant carburant) { this.carburant = carburant; }

    public int getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(int nombrePlaces) { this.nombrePlaces = nombrePlaces; }

    public Integer getNombrePortes() { return nombrePortes; }
    public void setNombrePortes(Integer nombrePortes) { this.nombrePortes = nombrePortes; }

    public boolean isClimatisation() { return climatisation; }
    public void setClimatisation(boolean climatisation) { this.climatisation = climatisation; }

    public boolean isGps() { return gps; }
    public void setGps(boolean gps) { this.gps = gps; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getPrixJour() { return prixJour; }
    public void setPrixJour(BigDecimal prixJour) { this.prixJour = prixJour; }

    public BigDecimal getCaution() { return caution; }
    public void setCaution(BigDecimal caution) { this.caution = caution; }

    public boolean isKilometrageIllimite() { return kilometrageIllimite; }
    public void setKilometrageIllimite(boolean kilometrageIllimite) { this.kilometrageIllimite = kilometrageIllimite; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getCompagnieName() { return compagnieName; }
    public void setCompagnieName(String compagnieName) { this.compagnieName = compagnieName; }

    // Backward compatibility
    public String getModel() {
        return marque != null && modele != null ? marque + " " + modele : modele;
    }
    public String getType() {
        return categorie != null ? categorie.name() : "ECONOMIQUE";
    }
    public double getPricePerDay() {
        return prixJour != null ? prixJour.doubleValue() : 0.0;
    }

    @Override
    public String toString() {
        return "Car{" +
                "vehiculeId=" + vehiculeId +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", categorie=" + categorie +
                ", prixJour=" + prixJour +
                '}';
    }
}

