package ui.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ui.model.Payment;
import ui.db.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class PaymentApiServer {
    private final HttpServer server;

    public PaymentApiServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/api/payments/process", new ProcessPaymentHandler());
        this.server.createContext("/api/payments/process/", new ProcessPaymentHandler());
        this.server.createContext("/", new RootHandler());
        this.server.createContext("/api/bookings/hotels", new BookedHotelsHandler());
        this.server.createContext("/api/bookings/hotels/", new BookedHotelsHandler());
        this.server.createContext("/api/rentals/cars", new BookedCarsHandler());
        this.server.createContext("/api/rentals/cars/", new BookedCarsHandler());
        this.server.createContext("/api/payments", new PaymentsListHandler());
        this.server.createContext("/api/payments/", new PaymentsListHandler());
        this.server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    public void start() {
        server.start();
    }

    static class ProcessPaymentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] bytes = new byte[0];
                exchange.sendResponseHeaders(204, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            Map<String, String> form = new HashMap<>();
            if (contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
                form = parseForm(bodyString(exchange.getRequestBody()));
            } else if (contentType != null && contentType.toLowerCase().contains("application/json")) {
                String raw = bodyString(exchange.getRequestBody());
                form = parseSimpleJson(raw);
            } else {
                form = parseForm(bodyString(exchange.getRequestBody()));
            }

            String cardNumber = form.getOrDefault("cardNumber", "");
            String expiryDate = form.getOrDefault("expiryDate", "");
            String cvv = form.getOrDefault("cvv", "");
            Payment payment = new Payment(cardNumber, expiryDate, cvv);
            boolean ok = payment.processPayment();
            if (ok) {
                String txId = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
                try (var conn = Database.get();
                     var ps = conn.prepareStatement("INSERT INTO PAYMENTS(id, last4) VALUES(?,?)")) {
                    ps.setString(1, txId);
                    String last4 = cardNumber != null && cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : "";
                    ps.setString(2, last4);
                    ps.executeUpdate();
                } catch (Exception ignored) {}
                writeJson(exchange, 200, "{\"success\":true,\"transactionId\":\"" + txId + "\"}");
            } else {
                writeJson(exchange, 400, "{\"success\":false,\"message\":\"Invalid payment details\"}");
            }
        }

        private static void addCors(HttpExchange exchange) {
            Headers h = exchange.getResponseHeaders();
            h.add("Access-Control-Allow-Origin", "*");
            h.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            h.add("Access-Control-Allow-Headers", "Content-Type");
        }

        private static String bodyString(InputStream is) throws IOException {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        private static Map<String, String> parseForm(String body) {
            Map<String, String> map = new HashMap<>();
            if (body == null || body.isEmpty()) return map;
            String[] parts = body.split("&");
            for (String p : parts) {
                int i = p.indexOf('=');
                if (i > 0) {
                    String k = URLDecoder.decode(p.substring(0, i), StandardCharsets.UTF_8);
                    String v = URLDecoder.decode(p.substring(i + 1), StandardCharsets.UTF_8);
                    map.put(k, v);
                }
            }
            return map;
        }

        private static Map<String, String> parseSimpleJson(String json) {
            Map<String, String> map = new HashMap<>();
            if (json == null) return map;
            String s = json.trim();
            if (s.startsWith("{") && s.endsWith("}")) {
                s = s.substring(1, s.length() - 1).trim();
                if (!s.isEmpty()) {
                    String[] pairs = s.split(",");
                    for (String pair : pairs) {
                        String[] kv = pair.split(":", 2);
                        if (kv.length == 2) {
                            String key = stripQuotes(kv[0].trim());
                            String val = stripQuotes(kv[1].trim());
                            map.put(key, val);
                        }
                    }
                }
            }
            return map;
        }

        private static String stripQuotes(String s) {
            if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }

        private static void writeJson(HttpExchange exchange, int status, String json) throws IOException {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            Headers h = exchange.getResponseHeaders();
            h.set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers h = exchange.getResponseHeaders();
            h.set("Content-Type", "application/json; charset=UTF-8");
            String json = "{\"endpoints\":[\"POST /api/payments/process\",\"GET /api/payments\",\"GET /api/bookings/hotels\",\"GET /api/rentals/cars\"]}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class BookedHotelsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.set("Content-Type", "application/json; charset=UTF-8");
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] err = "{\"error\":\"Method Not Allowed\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(405, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            try (var conn = Database.get();
                 var ps = conn.prepareStatement(
                         "SELECT h.name, h.city, COUNT(b.id) AS bookings " +
                         "FROM BOOKINGS b JOIN HOTELS h ON b.hotel_name=h.name AND b.hotel_city=h.city " +
                         "WHERE UPPER(b.status)='CONFIRMED' " +
                         "GROUP BY h.name, h.city ORDER BY bookings DESC, h.city, h.name")) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) sb.append(",");
                        first = false;
                        String name = rs.getString("name");
                        String city = rs.getString("city");
                        int bookings = rs.getInt("bookings");
                        sb.append("{\"name\":\"")
                          .append(escape(name))
                          .append("\",\"city\":\"")
                          .append(escape(city))
                          .append("\",\"bookings\":")
                          .append(bookings)
                          .append("}");
                    }
                }
            } catch (Exception e) {
                byte[] err = "{\"error\":\"Failed to load data\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }
            sb.append("]");
            byte[] out = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, out.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(out); }
        }

        private static String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    static class BookedCarsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.set("Content-Type", "application/json; charset=UTF-8");
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] err = "{\"error\":\"Method Not Allowed\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(405, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            try (var conn = Database.get();
                 var ps = conn.prepareStatement(
                         "SELECT brand, model, COUNT(id) AS bookings " +
                         "FROM CAR_RENTALS WHERE UPPER(status)='CONFIRMED' " +
                         "GROUP BY brand, model ORDER BY bookings DESC, brand, model")) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) sb.append(",");
                        first = false;
                        String brand = rs.getString("brand");
                        String model = rs.getString("model");
                        int bookings = rs.getInt("bookings");
                        sb.append("{\"brand\":\"")
                          .append(escape(brand))
                          .append("\",\"model\":\"")
                          .append(escape(model))
                          .append("\",\"bookings\":")
                          .append(bookings)
                          .append("}");
                    }
                }
            } catch (Exception e) {
                byte[] err = "{\"error\":\"Failed to load data\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }
            sb.append("]");
            byte[] out = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, out.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(out); }
        }

        private static String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    static class PaymentsListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.set("Content-Type", "application/json; charset=UTF-8");
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                byte[] err = "{\"error\":\"Method Not Allowed\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(405, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            try (var conn = Database.get();
                 var ps = conn.prepareStatement(
                         "SELECT id, last4, created_at FROM PAYMENTS ORDER BY created_at DESC LIMIT 100")) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) sb.append(",");
                        first = false;
                        String id = rs.getString("id");
                        String last4 = rs.getString("last4");
                        String created = String.valueOf(rs.getTimestamp("created_at"));
                        sb.append("{\"id\":\"")
                          .append(escape(id))
                          .append("\",\"last4\":\"")
                          .append(escape(last4))
                          .append("\",\"createdAt\":\"")
                          .append(escape(created))
                          .append("\"}");
                    }
                }
            } catch (Exception e) {
                byte[] err = "{\"error\":\"Failed to load data\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }
            sb.append("]");
            byte[] out = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, out.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(out); }
        }

        private static String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
