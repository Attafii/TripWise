package tracking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class GPSReceiver {

    // Dernières coordonnées reçues (lisibles par ton contrôleur JavaFX)
    public static volatile double lastLat = 0.0;
    public static volatile double lastLng = 0.0;

    private static HttpServer server;

    public static void start() throws IOException {
        if (server != null) return; // déjà démarré

        // "0.0.0.0" = écoute sur toutes les interfaces (permet à Wokwi d'atteindre ta machine)
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8081), 0);

        server.createContext("/update", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                    return;
                }

                // Lire le JSON brut
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Parsing JSON ultra simple (sans dépendance) : {"lat": 36.8625, "lng": 10.1956}
                // (Si tu préfères une vraie lib JSON, je te mets plus bas l'option org.json)
                double lat = extractNumber(body, "\"lat\"");
                double lng = extractNumber(body, "\"lng\"");

                if (!Double.isNaN(lat) && !Double.isNaN(lng)) {
                    lastLat = lat;
                    lastLng = lng;
                    System.out.println("Received coords: " + lastLat + ", " + lastLng);
                    writeResponse(exchange, 200, "OK");
                } else {
                    writeResponse(exchange, 400, "Bad JSON: " + body);
                }
            }
        });

        server.start();
        System.out.println("GPS Server listening on http://0.0.0.0:8080/update");
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private static void writeResponse(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Petit parseur minimaliste pour éviter une dépendance externe
    private static double extractNumber(String json, String key) {
        try {
            int i = json.indexOf(key);
            if (i < 0) return Double.NaN;
            int colon = json.indexOf(':', i);
            if (colon < 0) return Double.NaN;

            int j = colon + 1;
            // sauter espaces
            while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
            // lire jusqu'à fin du nombre
            int k = j;
            while (k < json.length()) {
                char c = json.charAt(k);
                if (!(Character.isDigit(c) || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E')) break;
                k++;
            }
            String num = json.substring(j, k);
            return Double.parseDouble(num);
        } catch (Exception e) {
            return Double.NaN;
        }
    }
}