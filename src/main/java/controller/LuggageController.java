package controller;

import dao.LuggageDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import model.Luggage;
import tracking.GPSReceiver;
import javafx.concurrent.Worker;

public class LuggageController {

    @FXML private TableView<Luggage> luggageTable;
    @FXML private TableColumn<Luggage, Integer> colId;
    @FXML private TableColumn<Luggage, Integer> colBooking;
    @FXML private TableColumn<Luggage, Double> colWeight;
    @FXML private TableColumn<Luggage, String> colStatus;

    @FXML private TextField tfBookingId;
    @FXML private TextField tfWeight;
    @FXML private TextField tfStatus;

    // --- Carte ---
    @FXML private WebView mapView;
    private WebEngine engine;
    private Timeline mapUpdater;

    private final LuggageDAO luggageDAO = new LuggageDAO();
    private final ObservableList<Luggage> data = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        // Colonnes de la table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBooking.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Carte : charge un HTML Leaflet en mémoire et démarre la mise à jour périodique
        if (mapView != null) {
            engine = mapView.getEngine();
            loadLeafletMapInline();  // pas besoin de map.html
            startPollingMap();       // met à jour le marqueur toutes les 2 secondes
        }
    }

    @FXML
    private void loadByBooking(){
        if(tfBookingId.getText().isBlank()) return;
        int bookingId = Integer.parseInt(tfBookingId.getText().trim());
        data.setAll(luggageDAO.getLuggageByBooking(bookingId));
        luggageTable.setItems(data);
    }

    @FXML
    private void addLuggage(){
        if(tfBookingId.getText().isBlank() || tfWeight.getText().isBlank() || tfStatus.getText().isBlank()) return;
        int bookingId = Integer.parseInt(tfBookingId.getText().trim());
        double weight = Double.parseDouble(tfWeight.getText().trim());
        String status = tfStatus.getText().trim();

        luggageDAO.addLuggage(new Luggage(bookingId, weight, status));
        loadByBooking();
        tfWeight.clear();
        tfStatus.clear();
    }

    /* =======================
          Carte (Leaflet)
       ======================= */



    private void loadLeafletMapInline() {
        String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">

          <!-- Leaflet CSS/JS en HTTPS -->
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

          <style>
            html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; }
            /* (Optionnel) améliore l’anti-crénelage sur certaines configs */
            body { -webkit-font-smoothing: antialiased; }
          </style>
        </head>
        <body>
          <div id="map"></div>

          <script>
            // Crée la carte et couche OSM (HTTPS pour éviter mixed-content)
            var map = L.map('map').setView([0, 0], 2);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);

            // Marqueur initial
            var marker = L.marker([0,0]).addTo(map);

            // Appelée depuis Java : engine.executeScript("updatePosition(lat, lng)");
            window.updatePosition = function(lat, lng) {
              marker.setLatLng([lat, lng]);
              map.setView([lat, lng], 15);
            }
          </script>
        </body>
        </html>
        """;

        engine.loadContent(html, "text/html");

        // ⚠️ Très important : n’appeler updatePosition/Timeline qu’après le chargement
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                startPollingMap(); // ta Timeline qui lit lastLat/lastLng et appelle updatePosition(...)
            }
        });
    }

    /** Lit les dernières coordonnées reçues par GPSReceiver et déplace le marqueur */
    private void startPollingMap() {
        mapUpdater = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            double lat = GPSReceiver.lastLat;
            double lng = GPSReceiver.lastLng;

            // Si aucune donnée reçue, on ne fait rien
            if (Double.compare(lat, 0.0) == 0 && Double.compare(lng, 0.0) == 0) return;

            String js = String.format("updatePosition(%f, %f);", lat, lng);
            Platform.runLater(() -> {
                try {
                    engine.executeScript(js);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }));
        mapUpdater.setCycleCount(Timeline.INDEFINITE);
        mapUpdater.play();
    }
}