package ui.service;

import ui.model.Flight;
import ui.model.FlightBooking;
import ui.util.DataSource;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * FlightReportService - Generates CSV and Excel reports for flights
 * Uses Apache POI for Excel generation when available
 */
public class FlightReportService {

    private final Connection connection;
    private final FlightService flightService;
    private final FlightBookingService bookingService;
    private boolean poiAvailable = false;

    public FlightReportService() {
        this.connection = DataSource.getInstance().getConnection();
        this.flightService = new FlightService();
        this.bookingService = new FlightBookingService();

        // Check if Apache POI is available
        try {
            Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
            poiAvailable = true;
            System.out.println("✅ Apache POI available - Excel export enabled");
        } catch (ClassNotFoundException e) {
            poiAvailable = false;
            System.out.println("ℹ️ Apache POI not available - using CSV export");
        }
    }

    /**
     * Check if Excel export is available
     */
    public boolean isExcelExportAvailable() {
        return poiAvailable;
    }

    /**
     * Generate Excel report for all flights using Apache POI
     */
    public String generateFlightsExcelReportPOI(String outputPath) throws IOException {
        if (!poiAvailable) {
            return generateFlightsExcelReport(outputPath); // Fallback to CSV
        }

        List<Flight> flights = flightService.getAll();
        String filePath = outputPath + "/flights_report_" + LocalDate.now() + ".xlsx";

        try {
            // Use reflection to avoid compile-time dependency
            Object workbook = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook")
                .getDeclaredConstructor().newInstance();

            Object sheet = workbook.getClass().getMethod("createSheet", String.class)
                .invoke(workbook, "Flights Report");

            // Create header style
            Object headerStyle = createHeaderStyle(workbook);

            // Create header row
            Object headerRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, 0);
            String[] headers = {"Flight No", "Airline", "From", "To", "Departure", "Arrival",
                               "Aircraft", "Capacity", "Available", "Status", "Price"};

            for (int i = 0; i < headers.length; i++) {
                Object cell = headerRow.getClass().getMethod("createCell", int.class).invoke(headerRow, i);
                cell.getClass().getMethod("setCellValue", String.class).invoke(cell, headers[i]);
                cell.getClass().getMethod("setCellStyle", Class.forName("org.apache.poi.ss.usermodel.CellStyle"))
                    .invoke(cell, headerStyle);
            }

            // Create data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;

            for (Flight flight : flights) {
                Object row = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, rowNum++);

                createCell(row, 0, flight.getNumeroVol());
                createCell(row, 1, flight.getCompagnieName());
                createCell(row, 2, flight.getVilleDepart());
                createCell(row, 3, flight.getVilleArrivee());
                createCell(row, 4, flight.getDateDepart() != null ? flight.getDateDepart().format(formatter) : "");
                createCell(row, 5, flight.getDateArrivee() != null ? flight.getDateArrivee().format(formatter) : "");
                createCell(row, 6, flight.getTypeAvion());
                createCellNumeric(row, 7, flight.getCapaciteTotale());
                createCellNumeric(row, 8, flight.getPlacesDisponibles());
                createCell(row, 9, flight.getStatutVol() != null ? flight.getStatutVol().name() : "");
                createCellNumeric(row, 10, flight.getMinPrice());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.getClass().getMethod("autoSizeColumn", int.class).invoke(sheet, i);
            }

            // Add summary section
            int summaryRow = rowNum + 2;
            Object summaryHeaderRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow);
            createCell(summaryHeaderRow, 0, "SUMMARY");

            Object totalRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow + 1);
            createCell(totalRow, 0, "Total Flights:");
            createCellNumeric(totalRow, 1, flights.size());

            int totalCapacity = flights.stream().mapToInt(Flight::getCapaciteTotale).sum();
            int totalAvailable = flights.stream().mapToInt(Flight::getPlacesDisponibles).sum();

            Object capacityRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow + 2);
            createCell(capacityRow, 0, "Total Capacity:");
            createCellNumeric(capacityRow, 1, totalCapacity);

            Object availableRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow + 3);
            createCell(availableRow, 0, "Available Seats:");
            createCellNumeric(availableRow, 1, totalAvailable);

            if (totalCapacity > 0) {
                Object occupancyRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow + 4);
                createCell(occupancyRow, 0, "Occupancy Rate:");
                createCell(occupancyRow, 1, String.format("%.1f%%", (1 - (double) totalAvailable / totalCapacity) * 100));
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.getClass().getMethod("write", java.io.OutputStream.class).invoke(workbook, fileOut);
            }

            workbook.getClass().getMethod("close").invoke(workbook);
            System.out.println("✅ Excel report generated: " + filePath);
            return filePath;

        } catch (Exception e) {
            System.err.println("❌ Error generating Excel report: " + e.getMessage());
            e.printStackTrace();
            // Fallback to CSV
            return generateFlightsExcelReport(outputPath);
        }
    }

    /**
     * Generate Excel report for bookings using Apache POI
     */
    public String generateBookingsExcelReportPOI(String outputPath) throws IOException {
        if (!poiAvailable) {
            return generateBookingsExcelReport(outputPath);
        }

        List<FlightBooking> bookings = bookingService.getAll();
        String filePath = outputPath + "/bookings_report_" + LocalDate.now() + ".xlsx";

        try {
            Object workbook = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook")
                .getDeclaredConstructor().newInstance();

            Object sheet = workbook.getClass().getMethod("createSheet", String.class)
                .invoke(workbook, "Bookings Report");

            Object headerStyle = createHeaderStyle(workbook);

            // Header row
            Object headerRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, 0);
            String[] headers = {"Booking ID", "Confirmation", "Passenger", "Email", "Flight",
                               "Route", "Date", "Class", "Passengers", "Seats", "Price", "Status"};

            for (int i = 0; i < headers.length; i++) {
                Object cell = headerRow.getClass().getMethod("createCell", int.class).invoke(headerRow, i);
                cell.getClass().getMethod("setCellValue", String.class).invoke(cell, headers[i]);
                cell.getClass().getMethod("setCellStyle", Class.forName("org.apache.poi.ss.usermodel.CellStyle"))
                    .invoke(cell, headerStyle);
            }

            // Data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;

            for (FlightBooking booking : bookings) {
                Object row = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, rowNum++);

                createCell(row, 0, booking.getBookingId());
                createCell(row, 1, booking.getNumeroConfirmation());
                createCell(row, 2, booking.getPassengerName());
                createCell(row, 3, booking.getPassengerEmail());
                createCell(row, 4, booking.getNumeroVol());
                createCell(row, 5, booking.getRoute());
                createCell(row, 6, booking.getDateDepart() != null ? booking.getDateDepart().format(formatter) : "");
                createCell(row, 7, booking.getClasseNom());
                createCellNumeric(row, 8, booking.getNombrePassagers());
                createCell(row, 9, booking.getSiegesAttribues() != null ? booking.getSiegesAttribues() : "N/A");
                createCellNumeric(row, 10, booking.getPriceAsDouble());
                createCell(row, 11, booking.getStatusDisplay());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.getClass().getMethod("autoSizeColumn", int.class).invoke(sheet, i);
            }

            // Summary
            int summaryRow = rowNum + 2;
            Object summaryHeaderRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow);
            createCell(summaryHeaderRow, 0, "SUMMARY");

            double totalRevenue = bookings.stream().mapToDouble(FlightBooking::getPriceAsDouble).sum();
            long confirmed = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.CONFIRMEE).count();
            long pending = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.EN_ATTENTE).count();
            long cancelled = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.ANNULEE).count();

            Object totalBookingsRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow + 1);
            createCell(totalBookingsRow, 0, "Total Bookings:");
            createCellNumeric(totalBookingsRow, 1, bookings.size());

            Object revenueRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow + 2);
            createCell(revenueRow, 0, "Total Revenue:");
            createCell(revenueRow, 1, String.format("$%.2f", totalRevenue));

            Object statusRow = sheet.getClass().getMethod("createRow", int.class).invoke(sheet, summaryRow + 3);
            createCell(statusRow, 0, "Status Breakdown:");
            createCell(statusRow, 1, String.format("Confirmed: %d | Pending: %d | Cancelled: %d", confirmed, pending, cancelled));

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.getClass().getMethod("write", java.io.OutputStream.class).invoke(workbook, fileOut);
            }

            workbook.getClass().getMethod("close").invoke(workbook);
            System.out.println("✅ Bookings Excel report generated: " + filePath);
            return filePath;

        } catch (Exception e) {
            System.err.println("❌ Error generating bookings Excel report: " + e.getMessage());
            return generateBookingsExcelReport(outputPath);
        }
    }

    /**
     * Generate CSV report for all flights (can be opened in Excel)
     */
    public String generateFlightsExcelReport(String outputPath) throws IOException {
        List<Flight> flights = flightService.getAll();
        String filePath = outputPath + "/flights_report_" + LocalDate.now() + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Flight No,Airline,From,To,Departure,Arrival,Aircraft,Capacity,Available,Status,Min Price");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Flight flight : flights) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%d,%d,%s,$%.2f%n",
                    escape(flight.getNumeroVol()),
                    escape(flight.getCompagnieName()),
                    escape(flight.getVilleDepart()),
                    escape(flight.getVilleArrivee()),
                    flight.getDateDepart() != null ? flight.getDateDepart().format(formatter) : "",
                    flight.getDateArrivee() != null ? flight.getDateArrivee().format(formatter) : "",
                    escape(flight.getTypeAvion()),
                    flight.getCapaciteTotale(),
                    flight.getPlacesDisponibles(),
                    flight.getStatutVol().name(),
                    flight.getMinPrice()
                );
            }

            writer.println();
            writer.println("SUMMARY");
            writer.println("Total Flights," + flights.size());
            int totalCapacity = flights.stream().mapToInt(Flight::getCapaciteTotale).sum();
            int totalAvailable = flights.stream().mapToInt(Flight::getPlacesDisponibles).sum();
            writer.println("Total Capacity," + totalCapacity);
            writer.println("Available Seats," + totalAvailable);
            if (totalCapacity > 0) {
                writer.printf("Occupancy Rate,%.1f%%%n", (1 - (double) totalAvailable / totalCapacity) * 100);
            }
        }

        System.out.println("CSV report generated: " + filePath);
        return filePath;
    }

    /**
     * Generate CSV report for bookings
     */
    public String generateBookingsExcelReport(String outputPath) throws IOException {
        List<FlightBooking> bookings = bookingService.getAll();
        String filePath = outputPath + "/bookings_report_" + LocalDate.now() + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Booking ID,Confirmation,Passenger,Email,Flight,Route,Date,Class,Passengers,Seats,Price,Status");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (FlightBooking booking : bookings) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%d,%s,$%.2f,%s%n",
                    escape(booking.getBookingId()),
                    escape(booking.getNumeroConfirmation()),
                    escape(booking.getPassengerName()),
                    escape(booking.getPassengerEmail()),
                    escape(booking.getNumeroVol()),
                    escape(booking.getRoute()),
                    booking.getDateDepart() != null ? booking.getDateDepart().format(formatter) : "",
                    escape(booking.getClasseNom()),
                    booking.getNombrePassagers(),
                    booking.getSiegesAttribues() != null ? escape(booking.getSiegesAttribues()) : "N/A",
                    booking.getPriceAsDouble(),
                    escape(booking.getStatusDisplay())
                );
            }

            writer.println();
            writer.println("SUMMARY");
            writer.println("Total Bookings," + bookings.size());
            double totalRevenue = bookings.stream().mapToDouble(FlightBooking::getPriceAsDouble).sum();
            writer.printf("Total Revenue,$%.2f%n", totalRevenue);

            long confirmed = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.CONFIRMEE).count();
            long pending = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.EN_ATTENTE).count();
            long cancelled = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.ANNULEE).count();

            writer.println("Confirmed," + confirmed);
            writer.println("Pending," + pending);
            writer.println("Cancelled," + cancelled);
        }

        System.out.println("Bookings CSV report generated: " + filePath);
        return filePath;
    }

    /**
     * Generate capacity report CSV
     */
    public String generateCapacityReportExcel(String outputPath) throws IOException {
        String filePath = outputPath + "/capacity_report_" + LocalDate.now() + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Flight,Route,Departure,Total Seats,Booked,Available,Occupancy %,First Class,Business,Premium,Economy");

            List<FlightCapacity> capacities = getFlightCapacities();

            for (FlightCapacity cap : capacities) {
                writer.printf("%s,%s,%s,%d,%d,%d,%.1f%%,%d,%d,%d,%d%n",
                    escape(cap.flightNumber),
                    escape(cap.route),
                    escape(cap.departure),
                    cap.totalSeats,
                    cap.bookedSeats,
                    cap.availableSeats,
                    cap.occupancyRate,
                    cap.firstClassAvailable,
                    cap.businessAvailable,
                    cap.premiumAvailable,
                    cap.economyAvailable
                );
            }
        }

        System.out.println("Capacity CSV report generated: " + filePath);
        return filePath;
    }

    /**
     * Generate text report for flights
     */
    public String generateFlightsPdfReport(String outputPath) throws IOException {
        List<Flight> flights = flightService.getAll();
        String filePath = outputPath + "/flights_report_" + LocalDate.now() + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("===============================================================");
            writer.println("                    TRIPWISE - FLIGHTS REPORT");
            writer.println("===============================================================");
            writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
            writer.println();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            writer.printf("%-10s %-15s %-12s %-12s %-16s %-8s %-8s %-10s%n",
                "Flight", "Airline", "From", "To", "Departure", "Capacity", "Avail", "Status");
            writer.println("-------------------------------------------------------------------------------");

            for (Flight flight : flights) {
                writer.printf("%-10s %-15s %-12s %-12s %-16s %-8d %-8d %-10s%n",
                    flight.getNumeroVol(),
                    truncate(flight.getCompagnieName(), 15),
                    truncate(flight.getVilleDepart(), 12),
                    truncate(flight.getVilleArrivee(), 12),
                    flight.getDateDepart() != null ? flight.getDateDepart().format(formatter) : "-",
                    flight.getCapaciteTotale(),
                    flight.getPlacesDisponibles(),
                    flight.getStatutVol().name()
                );
            }

            writer.println();
            writer.println("===============================================================");
            writer.println("SUMMARY");
            writer.println("---------------------------------------------------------------");
            writer.println("Total Flights: " + flights.size());

            int totalCapacity = flights.stream().mapToInt(Flight::getCapaciteTotale).sum();
            int totalAvailable = flights.stream().mapToInt(Flight::getPlacesDisponibles).sum();
            writer.println("Total Capacity: " + totalCapacity + " seats");
            writer.println("Available Seats: " + totalAvailable + " seats");
            if (totalCapacity > 0) {
                writer.printf("Occupancy Rate: %.1f%%%n", (1 - (double) totalAvailable / totalCapacity) * 100);
            }

            writer.println();
            writer.println("(c) 2026 TripWise Travel Agency");
        }

        System.out.println("Text report generated: " + filePath);
        return filePath;
    }

    /**
     * Generate text report for bookings
     */
    public String generateBookingsPdfReport(String outputPath) throws IOException {
        List<FlightBooking> bookings = bookingService.getAll();
        String filePath = outputPath + "/bookings_report_" + LocalDate.now() + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("===============================================================");
            writer.println("                   TRIPWISE - BOOKINGS REPORT");
            writer.println("===============================================================");
            writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
            writer.println();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");

            writer.printf("%-8s %-15s %-10s %-18s %-10s %-10s %-10s %-10s%n",
                "ID", "Passenger", "Flight", "Route", "Date", "Class", "Price", "Status");
            writer.println("------------------------------------------------------------------------------------------");

            for (FlightBooking booking : bookings) {
                writer.printf("%-8s %-15s %-10s %-18s %-10s %-10s $%-9.0f %-10s%n",
                    truncate(booking.getBookingId(), 8),
                    truncate(booking.getPassengerName(), 15),
                    booking.getNumeroVol(),
                    truncate(booking.getRoute(), 18),
                    booking.getDateDepart() != null ? booking.getDateDepart().format(formatter) : "-",
                    truncate(booking.getClasseNom(), 10),
                    booking.getPriceAsDouble(),
                    booking.getStatusDisplay()
                );
            }

            writer.println();
            writer.println("===============================================================");
            writer.println("STATISTICS");
            writer.println("---------------------------------------------------------------");

            double totalRevenue = bookings.stream().mapToDouble(FlightBooking::getPriceAsDouble).sum();
            long confirmed = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.CONFIRMEE).count();
            long pending = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.EN_ATTENTE).count();
            long cancelled = bookings.stream().filter(b -> b.getStatutReservation() == FlightBooking.StatutReservation.ANNULEE).count();

            writer.println("Total Bookings: " + bookings.size());
            writer.printf("Total Revenue: $%.2f%n", totalRevenue);
            writer.println("Confirmed: " + confirmed + " | Pending: " + pending + " | Cancelled: " + cancelled);

            writer.println();
            writer.println("(c) 2026 TripWise Travel Agency");
        }

        System.out.println("Text report generated: " + filePath);
        return filePath;
    }

    // ==================== HELPER METHODS FOR APACHE POI ====================

    /**
     * Create header style for Excel cells using reflection
     */
    private Object createHeaderStyle(Object workbook) throws Exception {
        Object style = workbook.getClass().getMethod("createCellStyle").invoke(workbook);
        Object font = workbook.getClass().getMethod("createFont").invoke(workbook);

        font.getClass().getMethod("setBold", boolean.class).invoke(font, true);
        style.getClass().getMethod("setFont", Class.forName("org.apache.poi.ss.usermodel.Font")).invoke(style, font);

        return style;
    }

    /**
     * Create a cell with string value using reflection
     */
    private void createCell(Object row, int column, String value) throws Exception {
        Object cell = row.getClass().getMethod("createCell", int.class).invoke(row, column);
        cell.getClass().getMethod("setCellValue", String.class).invoke(cell, value != null ? value : "");
    }

    /**
     * Create a cell with numeric value (double) using reflection
     */
    private void createCellNumeric(Object row, int column, double value) throws Exception {
        Object cell = row.getClass().getMethod("createCell", int.class).invoke(row, column);
        cell.getClass().getMethod("setCellValue", double.class).invoke(cell, value);
    }

    /**
     * Create a cell with numeric value (int) using reflection
     */
    private void createCellNumeric(Object row, int column, int value) throws Exception {
        Object cell = row.getClass().getMethod("createCell", int.class).invoke(row, column);
        cell.getClass().getMethod("setCellValue", double.class).invoke(cell, (double) value);
    }

    // ==================== CSV HELPER METHODS ====================

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() > maxLength ? value.substring(0, maxLength - 2) + ".." : value;
    }

    private List<FlightCapacity> getFlightCapacities() {
        List<FlightCapacity> capacities = new ArrayList<>();

        String query = "SELECT v.numero_vol, v.capacite_totale, v.places_disponibles, " +
                      "ad.ville as depart, aa.ville as arrivee, v.date_depart " +
                      "FROM vols v " +
                      "JOIN aeroports ad ON v.aeroport_depart_id = ad.aeroport_id " +
                      "JOIN aeroports aa ON v.aeroport_arrivee_id = aa.aeroport_id " +
                      "WHERE v.is_active = 1 " +
                      "ORDER BY v.date_depart";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                FlightCapacity cap = new FlightCapacity();
                cap.flightNumber = rs.getString("numero_vol");
                cap.route = rs.getString("depart") + " - " + rs.getString("arrivee");

                Timestamp ts = rs.getTimestamp("date_depart");
                cap.departure = ts != null ? ts.toLocalDateTime().format(formatter) : "-";

                cap.totalSeats = rs.getInt("capacite_totale");
                cap.availableSeats = rs.getInt("places_disponibles");
                cap.bookedSeats = cap.totalSeats - cap.availableSeats;
                cap.occupancyRate = cap.totalSeats > 0 ? (double) cap.bookedSeats / cap.totalSeats * 100 : 0;

                cap.firstClassAvailable = 0;
                cap.businessAvailable = 0;
                cap.premiumAvailable = 0;
                cap.economyAvailable = cap.availableSeats;

                capacities.add(cap);
            }
        } catch (SQLException e) {
            System.err.println("Error getting capacity data: " + e.getMessage());
        }

        return capacities;
    }

    public static class FlightCapacity {
        public String flightNumber;
        public String route;
        public String departure;
        public int totalSeats;
        public int bookedSeats;
        public int availableSeats;
        public double occupancyRate;
        public int firstClassAvailable;
        public int businessAvailable;
        public int premiumAvailable;
        public int economyAvailable;
    }
}
