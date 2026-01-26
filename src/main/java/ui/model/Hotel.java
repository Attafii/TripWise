package ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Hotel Entity - Maps to 'hotels' table
 */
public class Hotel {
    private int hotelId;
    private String nomHotel;
    private String adresse;
    private String ville;
    private String pays;
    private String codePostal;
    private BigDecimal etoiles;
    private String phoneNumber;
    private String email;
    private String siteWeb;
    private String description;
    private String equipements;
    private String politiqueAnnulation;
    private LocalTime heureCheckin;
    private LocalTime heureCheckout;
    private String imageUrl;
    private boolean isActive;
    private LocalDateTime createdAt;

    // For backward compatibility
    private double pricePerNight; // This would come from chambres table
    private double rating;

    public Hotel() {
        this.isActive = true;
        this.heureCheckin = LocalTime.of(14, 0);
        this.heureCheckout = LocalTime.of(11, 0);
    }

    public Hotel(String name, String city, double pricePerNight, double rating) {
        this();
        this.nomHotel = name;
        this.ville = city;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
    }

    // Getters and Setters
    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public String getNomHotel() { return nomHotel; }
    public void setNomHotel(String nomHotel) { this.nomHotel = nomHotel; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

    public BigDecimal getEtoiles() { return etoiles; }
    public void setEtoiles(BigDecimal etoiles) { this.etoiles = etoiles; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEquipements() { return equipements; }
    public void setEquipements(String equipements) { this.equipements = equipements; }

    public String getPolitiqueAnnulation() { return politiqueAnnulation; }
    public void setPolitiqueAnnulation(String politiqueAnnulation) { this.politiqueAnnulation = politiqueAnnulation; }

    public LocalTime getHeureCheckin() { return heureCheckin; }
    public void setHeureCheckin(LocalTime heureCheckin) { this.heureCheckin = heureCheckin; }

    public LocalTime getHeureCheckout() { return heureCheckout; }
    public void setHeureCheckout(LocalTime heureCheckout) { this.heureCheckout = heureCheckout; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Backward compatibility
    public String getName() { return nomHotel; }
    public String getCity() { return ville; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public double getRating() {
        return etoiles != null ? etoiles.doubleValue() : rating;
    }
    public void setRating(double rating) { this.rating = rating; }

    @Override
    public String toString() {
        return "Hotel{" +
                "hotelId=" + hotelId +
                ", nomHotel='" + nomHotel + '\'' +
                ", ville='" + ville + '\'' +
                ", etoiles=" + etoiles +
                '}';
    }
}

