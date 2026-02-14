package ui.model;

import java.math.BigDecimal;

/**
 * FlightClass Entity - represents flight class pricing
 * Maps to 'classes_vol' table
 */
public class FlightClass {

    private int classeVolId;
    private int volId;
    private ClasseType classe;
    private BigDecimal prix;
    private int placesDisponibles;
    private String avantages;

    public enum ClasseType {
        ECONOMIQUE, PREMIUM, BUSINESS, PREMIERE
    }

    public FlightClass() {}

    public FlightClass(int volId, ClasseType classe, BigDecimal prix, int placesDisponibles) {
        this.volId = volId;
        this.classe = classe;
        this.prix = prix;
        this.placesDisponibles = placesDisponibles;
    }

    // Getters and Setters
    public int getClasseVolId() { return classeVolId; }
    public void setClasseVolId(int classeVolId) { this.classeVolId = classeVolId; }

    public int getVolId() { return volId; }
    public void setVolId(int volId) { this.volId = volId; }

    public ClasseType getClasse() { return classe; }
    public void setClasse(ClasseType classe) { this.classe = classe; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }

    public String getAvantages() { return avantages; }
    public void setAvantages(String avantages) { this.avantages = avantages; }

    public double getPriceAsDouble() {
        return prix != null ? prix.doubleValue() : 0.0;
    }

    public String getDisplayName() {
        switch (classe) {
            case ECONOMIQUE: return "Economy";
            case PREMIUM: return "Premium";
            case BUSINESS: return "Business";
            case PREMIERE: return "First Class";
            default: return classe.name();
        }
    }

    @Override
    public String toString() {
        return getDisplayName() + " - $" + String.format("%.0f", getPriceAsDouble());
    }
}
