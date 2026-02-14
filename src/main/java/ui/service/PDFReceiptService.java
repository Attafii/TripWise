package ui.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDFReceiptService - Generates PDF receipts and reports
 * Uses iText 7 library for PDF generation
 */
public class PDFReceiptService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(52, 152, 219); // #3498db
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(39, 174, 96); // #27ae60

    /**
     * Generate booking receipt PDF
     */
    public static File generateBookingReceipt(String outputPath, String bookingId, 
                                             String userName, String userEmail,
                                             String hotelName, String hotelAddress,
                                             String checkIn, String checkOut,
                                             String roomType, int nights,
                                             double pricePerNight, double totalPrice,
                                             String paymentMethod, String bookingDate) {
        try {
            File file = new File(outputPath);
            file.getParentFile().mkdirs();

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Header
            Paragraph header = new Paragraph("TRIPWISE")
                    .setFontSize(32)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Hotel Booking Receipt")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subHeader);

            // Booking Information
            document.add(new Paragraph("Booking Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginTop(10));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(infoTable, "Booking ID:", bookingId, true);
            addTableRow(infoTable, "Booking Date:", bookingDate, false);
            addTableRow(infoTable, "Status:", "CONFIRMED", false);

            document.add(infoTable);

            // Customer Information
            document.add(new Paragraph("Customer Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginTop(20));

            Table customerTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            customerTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(customerTable, "Name:", userName, false);
            addTableRow(customerTable, "Email:", userEmail, false);

            document.add(customerTable);

            // Hotel Information
            document.add(new Paragraph("Hotel Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginTop(20));

            Table hotelTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            hotelTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(hotelTable, "Hotel Name:", hotelName, false);
            addTableRow(hotelTable, "Address:", hotelAddress, false);
            addTableRow(hotelTable, "Room Type:", roomType, false);
            addTableRow(hotelTable, "Check-in:", checkIn, false);
            addTableRow(hotelTable, "Check-out:", checkOut, false);
            addTableRow(hotelTable, "Number of Nights:", String.valueOf(nights), false);

            document.add(hotelTable);

            // Payment Information
            document.add(new Paragraph("Payment Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginTop(20));

            Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            paymentTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(paymentTable, "Price per Night:", "$" + String.format("%.2f", pricePerNight), false);
            addTableRow(paymentTable, "Number of Nights:", String.valueOf(nights), false);
            addTableRow(paymentTable, "Subtotal:", "$" + String.format("%.2f", pricePerNight * nights), false);
            addTableRow(paymentTable, "Taxes & Fees:", "$" + String.format("%.2f", totalPrice - (pricePerNight * nights)), false);

            document.add(paymentTable);

            // Total Amount (highlighted)
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            totalTable.setWidth(UnitValue.createPercentValue(100));
            totalTable.setMarginTop(10);

            Cell totalLabelCell = new Cell().add(new Paragraph("TOTAL AMOUNT:").setBold().setFontSize(14))
                    .setBackgroundColor(new DeviceRgb(236, 240, 241))
                    .setPadding(10);
            Cell totalValueCell = new Cell().add(new Paragraph("$" + String.format("%.2f", totalPrice))
                    .setBold().setFontSize(16).setFontColor(SUCCESS_COLOR))
                    .setBackgroundColor(new DeviceRgb(236, 240, 241))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(10);

            totalTable.addCell(totalLabelCell);
            totalTable.addCell(totalValueCell);
            document.add(totalTable);

            // Payment Method
            document.add(new Paragraph("Payment Method: " + paymentMethod)
                    .setMarginTop(10)
                    .setItalic());

            // Footer
            document.add(new Paragraph("\n\nThank you for choosing TripWise!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setMarginTop(30));

            document.add(new Paragraph("For support, contact us at support@tripwise.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(formatter))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(10));

            document.close();
            System.out.println("✅ PDF receipt generated: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            System.err.println("❌ Failed to generate PDF receipt: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate flight booking receipt PDF
     */
    public static File generateFlightReceipt(String outputPath, String bookingId,
                                            String userName, String userEmail,
                                            String flightNumber, String airline,
                                            String departure, String arrival,
                                            String departureTime, String arrivalTime,
                                            String seatClass, int passengers,
                                            double pricePerTicket, double totalPrice,
                                            String paymentMethod, String bookingDate) {
        try {
            File file = new File(outputPath);
            file.getParentFile().mkdirs();

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Header
            Paragraph header = new Paragraph("TRIPWISE")
                    .setFontSize(32)
                    .setBold()
                    .setFontColor(new DeviceRgb(230, 126, 34)) // Orange for flights
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Flight Booking Receipt")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subHeader);

            // Booking Information
            document.add(new Paragraph("Booking Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(new DeviceRgb(230, 126, 34))
                    .setMarginTop(10));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(infoTable, "Booking ID:", bookingId, true);
            addTableRow(infoTable, "Booking Date:", bookingDate, false);
            addTableRow(infoTable, "Status:", "CONFIRMED", false);

            document.add(infoTable);

            // Passenger Information
            document.add(new Paragraph("Passenger Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(new DeviceRgb(230, 126, 34))
                    .setMarginTop(20));

            Table passengerTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            passengerTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(passengerTable, "Name:", userName, false);
            addTableRow(passengerTable, "Email:", userEmail, false);
            addTableRow(passengerTable, "Passengers:", String.valueOf(passengers), false);

            document.add(passengerTable);

            // Flight Information
            document.add(new Paragraph("Flight Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(new DeviceRgb(230, 126, 34))
                    .setMarginTop(20));

            Table flightTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            flightTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(flightTable, "Flight Number:", flightNumber, false);
            addTableRow(flightTable, "Airline:", airline, false);
            addTableRow(flightTable, "Class:", seatClass, false);
            addTableRow(flightTable, "Departure:", departure + " at " + departureTime, false);
            addTableRow(flightTable, "Arrival:", arrival + " at " + arrivalTime, false);

            document.add(flightTable);

            // Important Notice
            Table noticeTable = new Table(UnitValue.createPercentArray(new float[]{100}));
            noticeTable.setWidth(UnitValue.createPercentValue(100));
            noticeTable.setMarginTop(15);
            noticeTable.setBackgroundColor(new DeviceRgb(255, 243, 205));

            Cell noticeCell = new Cell().add(new Paragraph("⚠️ IMPORTANT: Please arrive at the airport at least 2 hours before departure. Bring a valid photo ID and this confirmation.")
                    .setFontSize(10))
                    .setPadding(10);
            noticeTable.addCell(noticeCell);
            document.add(noticeTable);

            // Payment Information
            document.add(new Paragraph("Payment Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(new DeviceRgb(230, 126, 34))
                    .setMarginTop(20));

            Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            paymentTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(paymentTable, "Price per Ticket:", "$" + String.format("%.2f", pricePerTicket), false);
            addTableRow(paymentTable, "Number of Passengers:", String.valueOf(passengers), false);
            addTableRow(paymentTable, "Subtotal:", "$" + String.format("%.2f", pricePerTicket * passengers), false);
            addTableRow(paymentTable, "Taxes & Fees:", "$" + String.format("%.2f", totalPrice - (pricePerTicket * passengers)), false);

            document.add(paymentTable);

            // Total Amount
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            totalTable.setWidth(UnitValue.createPercentValue(100));
            totalTable.setMarginTop(10);

            Cell totalLabelCell = new Cell().add(new Paragraph("TOTAL AMOUNT:").setBold().setFontSize(14))
                    .setBackgroundColor(new DeviceRgb(236, 240, 241))
                    .setPadding(10);
            Cell totalValueCell = new Cell().add(new Paragraph("$" + String.format("%.2f", totalPrice))
                    .setBold().setFontSize(16).setFontColor(SUCCESS_COLOR))
                    .setBackgroundColor(new DeviceRgb(236, 240, 241))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(10);

            totalTable.addCell(totalLabelCell);
            totalTable.addCell(totalValueCell);
            document.add(totalTable);

            // Payment Method
            document.add(new Paragraph("Payment Method: " + paymentMethod)
                    .setMarginTop(10)
                    .setItalic());

            // Footer
            document.add(new Paragraph("\n\nHave a safe flight!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setMarginTop(30));

            document.add(new Paragraph("For support, contact us at support@tripwise.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(formatter))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(10));

            document.close();
            System.out.println("✅ PDF flight receipt generated: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            System.err.println("❌ Failed to generate PDF flight receipt: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate user report PDF (for admin)
     */
    public static File generateUserReport(String outputPath, java.util.List<String[]> userData,
                                         int totalUsers, int activeUsers, int inactiveUsers) {
        try {
            File file = new File(outputPath);
            file.getParentFile().mkdirs();

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Header
            Paragraph header = new Paragraph("TRIPWISE")
                    .setFontSize(32)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("User Report")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subHeader);

            // Summary Statistics
            document.add(new Paragraph("Summary")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginTop(10));

            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            summaryTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(summaryTable, "Total Users:", String.valueOf(totalUsers), false);
            addTableRow(summaryTable, "Active Users:", String.valueOf(activeUsers), false);
            addTableRow(summaryTable, "Inactive Users:", String.valueOf(inactiveUsers), false);

            document.add(summaryTable);

            // User List
            document.add(new Paragraph("User List")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginTop(20));

            Table userTable = new Table(UnitValue.createPercentArray(new float[]{10, 25, 30, 20, 15}));
            userTable.setWidth(UnitValue.createPercentValue(100));

            // Headers
            userTable.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()).setBackgroundColor(PRIMARY_COLOR).setFontColor(ColorConstants.WHITE));
            userTable.addHeaderCell(new Cell().add(new Paragraph("Name").setBold()).setBackgroundColor(PRIMARY_COLOR).setFontColor(ColorConstants.WHITE));
            userTable.addHeaderCell(new Cell().add(new Paragraph("Email").setBold()).setBackgroundColor(PRIMARY_COLOR).setFontColor(ColorConstants.WHITE));
            userTable.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()).setBackgroundColor(PRIMARY_COLOR).setFontColor(ColorConstants.WHITE));
            userTable.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()).setBackgroundColor(PRIMARY_COLOR).setFontColor(ColorConstants.WHITE));

            // Data rows
            for (String[] user : userData) {
                userTable.addCell(new Cell().add(new Paragraph(user[0])));
                userTable.addCell(new Cell().add(new Paragraph(user[1])));
                userTable.addCell(new Cell().add(new Paragraph(user[2])));
                userTable.addCell(new Cell().add(new Paragraph(user[3])));
                userTable.addCell(new Cell().add(new Paragraph(user[4])));
            }

            document.add(userTable);

            // Footer
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(formatter))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(30));

            document.close();
            System.out.println("✅ PDF user report generated: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            System.err.println("❌ Failed to generate PDF user report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to add a row to a table
     */
    private static void addTableRow(Table table, String label, String value, boolean bold) {
        Cell labelCell = new Cell().add(new Paragraph(label).setBold())
                .setPadding(5);
        Cell valueCell = new Cell().add(new Paragraph(value))
                .setPadding(5);
        
        if (bold) {
            valueCell.setBold();
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
