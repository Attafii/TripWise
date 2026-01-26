package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.Hotel;
import ui.service.HotelService;
import ui.util.SceneManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * HotelDetailController - Shows detailed information about a specific hotel
 */
public class HotelDetailController {

    @FXML
    private Label hotelNameLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label starsLabel;

    @FXML
    private Label ratingLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private FlowPane amenitiesContainer;

    @FXML
    private Label checkinLabel;

    @FXML
    private Label checkoutLabel;

    @FXML
    private Label cancellationLabel;

    @FXML
    private VBox roomsContainer;

    private Hotel hotel;
    private HotelService hotelService;

    // Static reference to current hotel (for navigation)
    private static Hotel currentHotel;

    public static void setCurrentHotel(Hotel hotel) {
        currentHotel = hotel;
    }

    @FXML
    private void initialize() {
        hotelService = new HotelService();
        
        if (currentHotel != null) {
            loadHotelDetails(currentHotel);
        } else {
            System.err.println("❌ No hotel selected!");
        }
    }

    private void loadHotelDetails(Hotel hotel) {
        this.hotel = hotel;

        // Hotel Name
        hotelNameLabel.setText(hotel.getNomHotel());

        // Location
        locationLabel.setText(hotel.getVille() + ", " + hotel.getPays());

        // Stars
        if (hotel.getEtoiles() != null) {
            int stars = hotel.getEtoiles().intValue();
            starsLabel.setText("⭐".repeat(stars));
            ratingLabel.setText(String.format("%.1f", hotel.getEtoiles()));
        }

        // Contact
        phoneLabel.setText(hotel.getPhoneNumber() != null ? hotel.getPhoneNumber() : "N/A");
        emailLabel.setText(hotel.getEmail() != null ? hotel.getEmail() : "N/A");

        // Price
        priceLabel.setText("$" + String.format("%.0f", hotel.getPricePerNight()));

        // Description
        descriptionLabel.setText(hotel.getDescription() != null ? 
            hotel.getDescription() : "Luxury hotel with excellent amenities and service.");

        // Amenities
        if (hotel.getEquipements() != null) {
            String[] amenities = hotel.getEquipements().split(",");
            for (String amenity : amenities) {
                Label amenityLabel = new Label(getAmenityIcon(amenity.trim()) + " " + amenity.trim());
                amenityLabel.setStyle("-fx-background-color: #f3f4f6; -fx-padding: 8 12; -fx-background-radius: 8; -fx-font-size: 13px; -fx-text-fill: #374151;");
                amenitiesContainer.getChildren().add(amenityLabel);
            }
        }

        // Check-in/out times
        if (hotel.getHeureCheckin() != null) {
            checkinLabel.setText(hotel.getHeureCheckin().toString());
        }
        if (hotel.getHeureCheckout() != null) {
            checkoutLabel.setText(hotel.getHeureCheckout().toString());
        }

        // Cancellation policy
        cancellationLabel.setText(hotel.getPolitiqueAnnulation() != null ? 
            hotel.getPolitiqueAnnulation() : "Free cancellation up to 24 hours before check-in");

        // Load rooms
        loadRooms(hotel.getHotelId());
    }

    private void loadRooms(int hotelId) {
        List<Room> rooms = getRoomsForHotel(hotelId);

        if (rooms.isEmpty()) {
            Label noRooms = new Label("No rooms available at the moment.");
            noRooms.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20;");
            roomsContainer.getChildren().add(noRooms);
            return;
        }

        for (Room room : rooms) {
            VBox roomCard = createRoomCard(room);
            roomsContainer.getChildren().add(roomCard);
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Room Type
        Label roomType = new Label(room.typeChambre);
        roomType.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // Room details
        HBox details = new HBox(15);
        details.setStyle("-fx-alignment: center-left;");

        Label capacityLabel = new Label("👥 " + room.capacite + " guests");
        capacityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Label sizeLabel = new Label("📐 " + room.superficie + " m²");
        sizeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        details.getChildren().addAll(capacityLabel, sizeLabel);

        // Description
        if (room.description != null && !room.description.isEmpty()) {
            Label desc = new Label(room.description);
            desc.setWrapText(true);
            desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");
            card.getChildren().add(desc);
        }

        // Equipements
        if (room.equipements != null && !room.equipements.isEmpty()) {
            FlowPane equip = new FlowPane(8, 8);
            String[] items = room.equipements.split(",");
            for (String item : items) {
                Label equipLabel = new Label("✓ " + item.trim());
                equipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-background-color: #d1fae5; -fx-padding: 4 8; -fx-background-radius: 4;");
                equip.getChildren().add(equipLabel);
            }
            card.getChildren().add(equip);
        }

        // Price and Book button
        HBox footer = new HBox(15);
        footer.setStyle("-fx-alignment: center-left;");

        VBox priceBox = new VBox(2);
        Label pricePerNight = new Label("$" + String.format("%.0f", room.prixNuit));
        pricePerNight.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");
        Label perNightLabel = new Label("per night");
        perNightLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        priceBox.getChildren().addAll(pricePerNight, perNightLabel);

        Button bookBtn = new Button("Select Room");
        bookBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: 600; -fx-cursor: hand;");
        bookBtn.setOnAction(e -> handleBookRoom(room));

        footer.getChildren().addAll(priceBox, new javafx.scene.layout.Region(), bookBtn);
        HBox.setHgrow(footer.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().addAll(roomType, details, footer);

        return card;
    }

    private List<Room> getRoomsForHotel(int hotelId) {
        List<Room> rooms = new ArrayList<>();

        try {
            Connection conn = ui.util.DataSource.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            
            String query = "SELECT * FROM chambres WHERE hotel_id = " + hotelId + " AND is_available = 1";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Room room = new Room();
                room.chambreId = rs.getInt("chambre_id");
                room.typeChambre = rs.getString("type_chambre");
                room.capacite = rs.getInt("capacite");
                room.superficie = rs.getInt("superficie");
                room.prixNuit = rs.getDouble("prix_nuit");
                room.description = rs.getString("description");
                room.equipements = rs.getString("equipements");
                rooms.add(room);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading rooms: " + e.getMessage());
            e.printStackTrace();
        }

        return rooms;
    }

    private void handleBookRoom(Room room) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Room Booking");
        alert.setHeaderText("Book " + room.typeChambre);
        alert.setContentText(String.format(
            "You selected:\n%s\nCapacity: %d guests\nPrice: $%.2f/night\n\nBooking feature coming soon!",
            room.typeChambre, room.capacite, room.prixNuit
        ));
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchScene("/ui/book-hotel-new.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBookNow() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Book Hotel");
        alert.setHeaderText("Book " + hotel.getNomHotel());
        alert.setContentText("Please select a room from the available rooms list on the right.");
        alert.showAndWait();
    }

    private String getAmenityIcon(String amenity) {
        String lower = amenity.toLowerCase();
        if (lower.contains("wifi")) return "📶";
        if (lower.contains("pool")) return "🏊";
        if (lower.contains("spa")) return "💆";
        if (lower.contains("restaurant")) return "🍽️";
        if (lower.contains("gym")) return "💪";
        if (lower.contains("parking")) return "🅿️";
        if (lower.contains("bar")) return "🍸";
        if (lower.contains("beach")) return "🏖️";
        if (lower.contains("room service")) return "🛎️";
        if (lower.contains("concierge")) return "🎩";
        return "✓";
    }

    // Inner class for Room data
    private static class Room {
        int chambreId;
        String typeChambre;
        int capacite;
        int superficie;
        double prixNuit;
        String description;
        String equipements;
    }
}
