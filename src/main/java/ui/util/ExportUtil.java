package ui.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExportUtil - Export data to PDF (HTML format) and CSV
 * Lightweight implementation without external PDF libraries
 */
public class ExportUtil {

    /**
     * Export TableView to HTML (can be saved as PDF from browser)
     * @param tableView The TableView to export
     * @param filePath Output HTML file path
     * @param title Document title
     * @return true if export successful
     */
    public static <T> boolean exportToPDF(TableView<T> tableView, String filePath, String title) {
        try {
            // Create HTML file (user can print to PDF from browser)
            PrintWriter writer = new PrintWriter(new FileWriter(filePath.replace(".pdf", ".html")));
            
            // Write HTML header with Material Design 3 styling
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<title>" + title + "</title>");
            writer.println("<style>");
            writer.println("body { font-family: 'Segoe UI', Roboto, sans-serif; margin: 40px; background: #f8f9fa; }");
            writer.println(".container { background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }");
            writer.println("h1 { color: #1e40af; margin-bottom: 10px; }");
            writer.println(".date { color: #6b7280; font-size: 14px; margin-bottom: 30px; }");
            writer.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
            writer.println("th { background: #eff6ff; color: #1e40af; padding: 12px; text-align: left; font-weight: 600; }");
            writer.println("td { padding: 10px; border-bottom: 1px solid #e5e7eb; }");
            writer.println("tr:nth-child(even) { background: #f9fafb; }");
            writer.println("tr:hover { background: #eff6ff; }");
            writer.println(".footer { text-align: right; color: #6b7280; font-size: 12px; margin-top: 20px; }");
            writer.println("@media print { body { margin: 0; } .container { box-shadow: none; } }");
            writer.println("</style>");
            writer.println("</head><body>");
            writer.println("<div class='container'>");
            
            // Title
            writer.println("<h1>" + title + "</h1>");
            
            // Date
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.println("<div class='date'>Generated: " + date + "</div>");
            
            // Table
            writer.println("<table>");
            
            // Headers
            writer.println("<thead><tr>");
            ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
            for (TableColumn<T, ?> column : columns) {
                writer.println("<th>" + escapeHtml(column.getText()) + "</th>");
            }
            writer.println("</tr></thead>");
            
            // Rows
            writer.println("<tbody>");
            ObservableList<T> items = tableView.getItems();
            for (T item : items) {
                writer.println("<tr>");
                for (TableColumn<T, ?> column : columns) {
                    Object cellData = column.getCellData(item);
                    String cellValue = cellData != null ? escapeHtml(cellData.toString()) : "";
                    writer.println("<td>" + cellValue + "</td>");
                }
                writer.println("</tr>");
            }
            writer.println("</tbody>");
            writer.println("</table>");
            
            // Footer
            writer.println("<div class='footer'>Total Records: " + items.size() + "</div>");
            writer.println("</div>");
            writer.println("</body></html>");
            
            writer.close();
            
            System.out.println("✅ HTML exported successfully: " + filePath.replace(".pdf", ".html"));
            System.out.println("💡 Open the HTML file in a browser and use Print > Save as PDF");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error exporting to HTML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export TableView to CSV
     * @param tableView The TableView to export
     * @param filePath Output CSV file path
     * @param title Document title (used for filename)
     * @return true if export successful
     */
    public static <T> boolean exportToCSV(TableView<T> tableView, String filePath, String title) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filePath));
            
            // Write title as comment
            writer.println("# " + title);
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.println("# Generated: " + date);
            writer.println();
            
            // Write headers
            ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                writer.print(escapeCsv(columns.get(i).getText()));
                if (i < columns.size() - 1) {
                    writer.print(",");
                }
            }
            writer.println();
            
            // Write data rows
            ObservableList<T> items = tableView.getItems();
            for (T item : items) {
                for (int i = 0; i < columns.size(); i++) {
                    Object cellData = columns.get(i).getCellData(item);
                    String cellValue = cellData != null ? escapeCsv(cellData.toString()) : "";
                    writer.print(cellValue);
                    if (i < columns.size() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
            
            writer.close();
            
            System.out.println("✅ CSV exported successfully: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error exporting to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export TableView to PDF with auto-generated filename
     */
    public static <T> boolean exportToPDF(TableView<T> tableView, String title) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = title.replaceAll("\\s+", "_") + "_" + timestamp + ".pdf";
        String filePath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + filename;
        
        return exportToPDF(tableView, filePath, title);
    }
    
    /**
     * Get default export directory
     */
    public static String getDefaultExportDir() {
        return System.getProperty("user.home") + File.separator + "Downloads";
    }
    
    /**
     * Generate filename with timestamp
     */
    public static String generateFilename(String prefix, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + "." + extension;
    }
    
    /**
     * Escape HTML special characters
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Escape CSV special characters
     */
    private static String escapeCsv(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
