package ui.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class PaymentClient {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE = "http://localhost:9090";

    public static Result processPayment(String cardNumber, String expiryDate, String cvv) {
        try {
            String json = "{\"cardNumber\":\"" + escape(cardNumber) + "\"," +
                          "\"expiryDate\":\"" + escape(expiryDate) + "\"," +
                          "\"cvv\":\"" + escape(cvv) + "\"}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/api/payments/process"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                String body = res.body();
                boolean success = body.contains("\"success\":true");
                String txId = extract(body, "transactionId");
                return new Result(success, txId, null);
            } else {
                return new Result(false, null, "HTTP " + res.statusCode());
            }
        } catch (Exception e) {
            return new Result(false, null, e.getMessage());
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extract(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int i = json.indexOf(pattern);
        if (i < 0) return null;
        int start = i + pattern.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    public static class Result {
        public final boolean success;
        public final String transactionId;
        public final String error;
        public Result(boolean success, String transactionId, String error) {
            this.success = success;
            this.transactionId = transactionId;
            this.error = error;
        }
    }
}
