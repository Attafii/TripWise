package ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Room {
    private int chambreId;
    private int hotelId;
    private String numeroChambre;
    private TypeChambre typeChambre;
    private BigDecimal prixNuit;
    private int capaciteAdultes;
    private int capaciteEnfants;
    private Double superficie;
    private String equipements;
    private boolean isAvailable;
    private LocalDateTime createdAt;

    private String hotelName;

    public enum TypeChambre {
        STANDARD, DELUXE, SUITE, PRESIDENTIAL
    }

    public Room() {
        this.isAvailable = true;
        this.capaciteAdultes = 2;
        this.capaciteEnfants = 0;
    }

    public Room(int hotelId, String numeroChambre, TypeChambre typeChambre, BigDecimal prixNuit) {
        this();
        this.hotelId = hotelId;
        this.numeroChambre = numeroChambre;
        this.typeChambre = typeChambre;
        this.prixNuit = prixNuit;
    }

    public int getChambreId() { return chambreId; }
    public void setChambreId(int chambreId) { this.chambreId = chambreId; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public String getNumeroChambre() { return numeroChambre; }
    public void setNumeroChambre(String numeroChambre) { this.numeroChambre = numeroChambre; }

    public TypeChambre getTypeChambre() { return typeChambre; }
    public void setTypeChambre(TypeChambre typeChambre) { this.typeChambre = typeChambre; }

    public BigDecimal getPrixNuit() { return prixNuit; }
    public void setPrixNuit(BigDecimal prixNuit) { this.prixNuit = prixNuit; }

    public int getCapaciteAdultes() { return capaciteAdultes; }
    public void setCapaciteAdultes(int capaciteAdultes) { this.capaciteAdultes = capaciteAdultes; }

    public int getCapaciteEnfants() { return capaciteEnfants; }
    public void setCapaciteEnfants(int capaciteEnfants) { this.capaciteEnfants = capaciteEnfants; }

    public Double getSuperficie() { return superficie; }
    public void setSuperficie(Double superficie) { this.superficie = superficie; }

    public String getEquipements() { return equipements; }
    public void setEquipements(String equipements) { this.equipements = equipements; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    @Override
    public String toString() {
        return "Room{" +
                "chambreId=" + chambreId +
                ", numeroChambre='" + numeroChambre + '\'' +
                ", typeChambre=" + typeChambre +
                ", prixNuit=" + prixNuit +
                ", capaciteAdultes=" + capaciteAdultes +
                '}';
    }
}
