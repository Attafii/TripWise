package ui.service;

import ui.util.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AIQueryGeneratorService - Converts natural language queries to SQL
 * and executes them safely with validation and formatting
 */
public class AIQueryGeneratorService {

    private final NVIDIAChatService aiService;
    private final DataSource dataSource;
    
    // Database schema information for AI context
    private static final String DATABASE_SCHEMA = """
        TripWise Database Schema:
        
        TABLES:
        1. users (user_id, email, first_name, last_name, phone_number, user_type)
        2. hotels (hotel_id, nom_hotel, ville, pays, etoiles, prix_nuit, adresse, is_active)
        3. chambres (chambre_id, hotel_id, type_chambre, capacite, prix_nuit, is_available)
        4. reservations_hotel (reservation_id, voyageur_id, hotel_id, chambre_id, date_checkin, date_checkout, prix_total, statut_reservation, numero_confirmation)
        5. vols (vol_id, numero_vol, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, places_disponibles, statut_vol, is_active)
        6. compagnies_aeriennes (compagnie_id, nom_compagnie, code_iata, pays_origine)
        7. aeroports (aeroport_id, nom_aeroport, ville, pays, code_iata)
        8. classes_vol (classe_id, vol_id, type_classe, prix, places_disponibles)
        9. reservations_vol (reservation_id, voyageur_id, vol_id, classe_id, prix_total, statut_reservation)
        10. vehicules (vehicule_id, marque, modele, categorie, prix_jour, is_available, ville_location)
        11. reservations_vehicule (reservation_id, voyageur_id, vehicule_id, date_debut, date_fin, prix_total, statut_reservation)
        12. deals (deal_id, deal_type, title, description, original_price, discounted_price, discount_percentage, start_date, end_date, destination, is_active)
        13. voyageurs (voyageur_id, user_id, date_naissance, passeport_numero, nationalite)
        
        COMMON STATUSES:
        - Reservations: 'EN_ATTENTE', 'CONFIRMEE', 'ANNULEE', 'TERMINEE'
        - Flights: 'PLANIFIE', 'EN_COURS', 'ATTERRI', 'ANNULE', 'RETARDE'
        
        RELATIONSHIPS:
        - reservations_hotel.voyageur_id → voyageurs.voyageur_id → users.user_id
        - chambres.hotel_id → hotels.hotel_id
        - vols.compagnie_id → compagnies_aeriennes.compagnie_id
        - vols.aeroport_depart_id/aeroport_arrivee_id → aeroports.aeroport_id
        """;
    
    // Regex patterns for SQL injection prevention
    private static final Pattern[] DANGEROUS_PATTERNS = {
        Pattern.compile(".*;\\s*DROP\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*;\\s*DELETE\\s+FROM\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*;\\s*TRUNCATE\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*;\\s*UPDATE\\s+.*SET\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*;\\s*INSERT\\s+INTO\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("--;", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/\\*.*\\*/", Pattern.CASE_INSENSITIVE),
        Pattern.compile("EXEC\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("EXECUTE\\s*\\(", Pattern.CASE_INSENSITIVE)
    };
    
    public AIQueryGeneratorService() {
        this.aiService = new NVIDIAChatService();
        this.dataSource = DataSource.getInstance();
    }
    
    /**
     * Process natural language query: generate SQL, execute, and format results
     */
    public String processNaturalLanguageQuery(String naturalQuery) {
        try {
            // Step 1: Generate SQL from natural language
            String sqlQuery = generateSQLFromNaturalLanguage(naturalQuery);
            
            if (sqlQuery == null || sqlQuery.isEmpty()) {
                return "❌ Could not generate a valid SQL query from your request.";
            }
            
            // Step 2: Validate SQL for safety
            if (!isSafeQuery(sqlQuery)) {
                return "❌ Query validation failed: Only SELECT queries are allowed for safety.";
            }
            
            // Step 3: Execute query
            List<Map<String, Object>> results = executeSafeQuery(sqlQuery);
            
            // Step 4: Format results for chat display
            return formatResultsForChat(results, naturalQuery, sqlQuery);
            
        } catch (Exception e) {
            System.err.println("❌ Error processing natural language query: " + e.getMessage());
            e.printStackTrace();
            return "❌ Error processing your query: " + e.getMessage();
        }
    }
    
    /**
     * Generate SQL query from natural language using AI
     */
    public String generateSQLFromNaturalLanguage(String userQuery) {
        try {
            String systemPrompt = """
                You are a SQL query generator for the TripWise travel booking system.
                
                """ + DATABASE_SCHEMA + """
                
                STRICT RULES:
                1. Generate ONLY SELECT queries (no INSERT, UPDATE, DELETE, DROP, TRUNCATE)
                2. Use proper JOINs for related data
                3. Always use LIMIT to prevent large result sets (default: LIMIT 10, max: LIMIT 50)
                4. Return ONLY the SQL query, nothing else
                5. Use proper table aliases for readability
                6. Include relevant columns that answer the user's question
                7. Use proper WHERE clauses for filtering
                8. Use ORDER BY when ranking/sorting is implied
                
                EXAMPLES:
                
                User: "Show me all hotels in Paris"
                SQL: SELECT hotel_id, nom_hotel, ville, etoiles, prix_nuit FROM hotels WHERE ville = 'Paris' AND is_active = 1 LIMIT 10
                
                User: "Find 5-star hotels"
                SQL: SELECT hotel_id, nom_hotel, ville, etoiles, prix_nuit FROM hotels WHERE etoiles >= 5 AND is_active = 1 ORDER BY prix_nuit ASC LIMIT 10
                
                User: "Show me pending hotel bookings"
                SQL: SELECT r.reservation_id, r.numero_confirmation, CONCAT(u.first_name, ' ', u.last_name) as customer_name, h.nom_hotel, r.date_checkin, r.date_checkout, r.prix_total FROM reservations_hotel r JOIN voyageurs v ON r.voyageur_id = v.voyageur_id JOIN users u ON v.user_id = u.user_id JOIN hotels h ON r.hotel_id = h.hotel_id WHERE r.statut_reservation = 'EN_ATTENTE' LIMIT 10
                
                User: "Find cheapest flights from Paris to New York"
                SQL: SELECT v.numero_vol, ca.nom_compagnie, ad.ville as depart, aa.ville as arrivee, v.date_depart, MIN(cv.prix) as min_price FROM vols v JOIN compagnies_aeriennes ca ON v.compagnie_id = ca.compagnie_id JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id JOIN classes_vol cv ON v.vol_id = cv.vol_id WHERE ad.ville = 'Paris' AND aa.ville = 'New York' AND v.is_active = 1 GROUP BY v.vol_id ORDER BY min_price ASC LIMIT 10
                
                User: "Show me available cars in Dubai"
                SQL: SELECT vehicule_id, marque, modele, categorie, prix_jour, ville_location FROM vehicules WHERE ville_location = 'Dubai' AND is_available = 1 ORDER BY prix_jour ASC LIMIT 10
                
                User: "What are the best deals right now?"
                SQL: SELECT deal_id, title, description, original_price, discounted_price, discount_percentage, destination FROM deals WHERE is_active = 1 AND NOW() BETWEEN start_date AND end_date ORDER BY discount_percentage DESC LIMIT 10
                
                Now generate a SQL query for the following user request.
                Return ONLY the SQL query, no explanation, no markdown, no extra text.
                """;
            
            // Create a temporary chat service with SQL generation context
            NVIDIAChatService sqlGenerator = new NVIDIAChatService();
            sqlGenerator.clearHistory();
            
            // Send system prompt + user query
            String response = sqlGenerator.sendMessage(systemPrompt + "\n\nUser request: " + userQuery);
            
            // Extract SQL query from response (remove markdown if present)
            String sql = extractSQLFromResponse(response);
            
            System.out.println("🔍 Generated SQL: " + sql);
            
            return sql;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating SQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extract SQL query from AI response (handles markdown and extra text)
     */
    private String extractSQLFromResponse(String response) {
        // Remove markdown code blocks
        String sql = response.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "");
        
        // Remove leading/trailing whitespace
        sql = sql.trim();
        
        // If response contains multiple lines, try to find the SELECT statement
        if (sql.contains("\n")) {
            String[] lines = sql.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.toUpperCase().startsWith("SELECT")) {
                    return line;
                }
            }
        }
        
        return sql;
    }
    
    /**
     * Validate that SQL query is safe (SELECT only, no dangerous operations)
     */
    public boolean isSafeQuery(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }
        
        // Must be a SELECT query
        String upperSQL = sql.trim().toUpperCase();
        if (!upperSQL.startsWith("SELECT")) {
            System.err.println("❌ Query must start with SELECT");
            return false;
        }
        
        // Check for dangerous patterns
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(sql).find()) {
                System.err.println("❌ Query contains dangerous pattern: " + pattern.pattern());
                return false;
            }
        }
        
        // Additional checks
        if (upperSQL.contains("DROP ") || upperSQL.contains("DELETE ") || 
            upperSQL.contains("TRUNCATE ") || upperSQL.contains("ALTER ") ||
            upperSQL.contains("CREATE ") || upperSQL.contains("GRANT ") ||
            upperSQL.contains("REVOKE ")) {
            System.err.println("❌ Query contains forbidden keywords");
            return false;
        }
        
        return true;
    }
    
    /**
     * Execute safe SQL query and return results
     */
    public List<Map<String, Object>> executeSafeQuery(String sql) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Get column names
            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnLabel(i));
            }
            
            // Fetch results
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = columnNames.get(i - 1);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error executing query: " + e.getMessage());
            throw e;
        }
        
        return results;
    }
    
    /**
     * Format query results for chat display
     */
    public String formatResultsForChat(List<Map<String, Object>> results, String originalQuery, String sqlQuery) {
        if (results == null || results.isEmpty()) {
            return "🔍 Query executed successfully, but no results found.\n\n" +
                   "📊 Query: " + originalQuery + "\n" +
                   "💾 SQL: " + sqlQuery;
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("🔍 Query Results for: \"").append(originalQuery).append("\"\n\n");
        formatted.append("📊 Found ").append(results.size()).append(" result(s)\n");
        formatted.append("─".repeat(50)).append("\n\n");
        
        // Format each row
        int count = 0;
        for (Map<String, Object> row : results) {
            count++;
            formatted.append(String.format("Result #%d:\n", count));
            
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Format key (make it readable)
                String formattedKey = formatColumnName(key);
                
                // Format value
                String formattedValue = value != null ? value.toString() : "N/A";
                
                formatted.append(String.format("  %s: %s\n", formattedKey, formattedValue));
            }
            formatted.append("\n");
        }
        
        // Add SQL query for transparency
        formatted.append("─".repeat(50)).append("\n");
        formatted.append("💾 SQL Query: ").append(sqlQuery);
        
        return formatted.toString();
    }
    
    /**
     * Format column name for display (convert snake_case to Title Case)
     */
    private String formatColumnName(String columnName) {
        // Replace underscores with spaces
        String formatted = columnName.replace("_", " ");
        
        // Capitalize first letter of each word
        String[] words = formatted.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Get suggested queries based on user context
     */
    public List<String> getSuggestedQueries(String userType) {
        List<String> suggestions = new ArrayList<>();
        
        if ("EMPLOYE".equals(userType) || "ADMIN".equals(userType)) {
            suggestions.add("Show me all pending bookings");
            suggestions.add("What are the top 5 hotels by bookings?");
            suggestions.add("Show me today's check-ins");
            suggestions.add("Find all cancelled reservations this month");
            suggestions.add("Show me available cars in all locations");
        } else {
            suggestions.add("Show me 5-star hotels in Paris");
            suggestions.add("Find cheapest available rooms");
            suggestions.add("What are the best deals right now?");
            suggestions.add("Show me my bookings");
            suggestions.add("Find flights to New York");
        }
        
        return suggestions;
    }
}
