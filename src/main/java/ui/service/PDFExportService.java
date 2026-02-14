package ui.service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDFExportService - Export booking details to HTML/PDF
 *
 * Creates an HTML file that can be:
 * 1. Opened in browser and printed to PDF
 * 2. Viewed directly
 */
public class PDFExportService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    /**
     * Export flight booking details to HTML file (can be printed as PDF)
     */
    public static File exportBookingToPDF(BookingDetails details) throws IOException {

        String htmlContent = generateHTML(details);

        // Generate filename
        String fileName = "TripWise_Booking_" +
                         (details.confirmationNumber != null ? details.confirmationNumber : "Unknown") +
                         "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                         ".html";

        // Save to user's Downloads folder
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (!downloadsDir.exists()) {
            downloadsDir = new File(userHome);
        }

        File htmlFile = new File(downloadsDir, fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile))) {
            writer.print(htmlContent);
        }

        System.out.println("✅ Booking exported to: " + htmlFile.getAbsolutePath());
        return htmlFile;
    }

    /**
     * Generate beautiful HTML content for the booking
     */
    private static String generateHTML(BookingDetails details) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>TripWise - Booking Confirmation</title>\n");
        html.append("    <style>\n");
        html.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f3f4f6; padding: 20px; }\n");
        html.append("        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }\n");
        html.append("        .header { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; padding: 30px; text-align: center; }\n");
        html.append("        .header h1 { font-size: 28px; margin-bottom: 5px; }\n");
        html.append("        .header p { opacity: 0.9; font-size: 14px; }\n");
        html.append("        .confirmation-box { background: #10b981; color: white; margin: 20px; padding: 20px; border-radius: 8px; text-align: center; }\n");
        html.append("        .confirmation-box h2 { font-size: 18px; margin-bottom: 10px; }\n");
        html.append("        .confirmation-box .number { font-size: 32px; font-weight: bold; letter-spacing: 2px; }\n");
        html.append("        .status { text-align: center; padding: 10px; margin: 0 20px; border-radius: 20px; font-weight: bold; }\n");
        html.append("        .status.confirmed { background: #d1fae5; color: #065f46; }\n");
        html.append("        .status.pending { background: #fef3c7; color: #92400e; }\n");
        html.append("        .status.cancelled { background: #fee2e2; color: #991b1b; }\n");
        html.append("        .details { padding: 20px; }\n");
        html.append("        .details h3 { color: #374151; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 2px solid #e5e7eb; }\n");
        html.append("        .detail-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f3f4f6; }\n");
        html.append("        .detail-row:last-child { border-bottom: none; }\n");
        html.append("        .detail-label { color: #6b7280; }\n");
        html.append("        .detail-value { color: #111827; font-weight: 500; }\n");
        html.append("        .price-box { background: #eff6ff; margin: 20px; padding: 20px; border-radius: 8px; border: 2px solid #3b82f6; text-align: center; }\n");
        html.append("        .price-box .label { color: #6b7280; font-size: 14px; }\n");
        html.append("        .price-box .amount { color: #3b82f6; font-size: 36px; font-weight: bold; }\n");
        html.append("        .footer { background: #f9fafb; padding: 20px; text-align: center; color: #6b7280; font-size: 12px; }\n");
        html.append("        .footer p { margin: 5px 0; }\n");
        html.append("        @media print { body { background: white; padding: 0; } .container { box-shadow: none; } }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");

        // Header
        html.append("        <div class=\"header\">\n");
        html.append("            <h1>✈️ TripWise</h1>\n");
        html.append("            <p>Your Journey, Our Priority</p>\n");
        html.append("        </div>\n");

        // Confirmation box
        html.append("        <div class=\"confirmation-box\">\n");
        html.append("            <h2>BOOKING CONFIRMATION</h2>\n");
        html.append("            <div class=\"number\">").append(details.confirmationNumber != null ? details.confirmationNumber : "N/A").append("</div>\n");
        html.append("        </div>\n");

        // Status
        String statusClass = details.status.toLowerCase().contains("confirmed") ? "confirmed" :
                            details.status.toLowerCase().contains("pending") ? "pending" : "cancelled";
        html.append("        <div class=\"status ").append(statusClass).append("\">").append(details.status).append("</div>\n");

        // Details
        html.append("        <div class=\"details\">\n");
        html.append("            <h3>").append(details.type).append(" Details</h3>\n");

        addDetailRow(html, "Route / Title", details.title);
        addDetailRow(html, "Details", details.subtitle);
        addDetailRow(html, "Trip Date", details.tripDate);
        addDetailRow(html, "Additional Info", details.details);
        addDetailRow(html, "Booking Date", details.bookingDate);

        html.append("        </div>\n");

        // Price box
        html.append("        <div class=\"price-box\">\n");
        html.append("            <div class=\"label\">Total Price</div>\n");
        html.append("            <div class=\"amount\">").append(details.price).append("</div>\n");
        html.append("        </div>\n");

        // Footer
        html.append("        <div class=\"footer\">\n");
        html.append("            <p>Generated on: ").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("</p>\n");
        html.append("            <p>TripWise Travel Agency | www.tripwise.com | support@tripwise.com</p>\n");
        html.append("            <p>Thank you for choosing TripWise! Have a great trip! ✨</p>\n");
        html.append("        </div>\n");

        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private static void addDetailRow(StringBuilder html, String label, String value) {
        html.append("            <div class=\"detail-row\">\n");
        html.append("                <span class=\"detail-label\">").append(label).append("</span>\n");
        html.append("                <span class=\"detail-value\">").append(value != null ? value : "N/A").append("</span>\n");
        html.append("            </div>\n");
    }

    /**
     * Data class for booking details
     */
    public static class BookingDetails {
        public String type;
        public String title;
        public String subtitle;
        public String confirmationNumber;
        public String status;
        public String tripDate;
        public String bookingDate;
        public String details;
        public String price;

        public BookingDetails() {}
    }
}
