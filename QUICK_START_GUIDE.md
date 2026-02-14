# 🚀 TripWise - Quick Start Guide

## ⚡ Quick Setup (5 Minutes)

### 1. Database Setup
```sql
mysql -u root -p
CREATE DATABASE tripwise_db;
USE tripwise_db;
SOURCE database_schema.sql;
SOURCE database_schema_admin_features.sql;
```

### 2. Run Application
```bash
cd C:\Users\Ahmed Attafi\IdeaProjects\TripWise
mvn clean compile
mvn javafx:run
```

### 3. Login
```
Admin: admin@tripwise.com / Admin123!
Employee: employee@tripwise.com / Admin123!
Traveler: traveler@tripwise.com / Admin123!
```

---

## 📝 Common Tasks

### Send Booking Confirmation Email
```java
EmailService emailService = new EmailService();
emailService.sendBookingConfirmation(
    userEmail, userName, bookingId,
    hotelName, checkIn, checkOut, totalPrice
);
```

### Generate PDF Receipt
```java
PDFReceiptService.generateBookingReceipt(
    outputPath, bookingId, userName, userEmail,
    hotelName, hotelAddress, checkIn, checkOut,
    roomType, nights, pricePerNight, totalPrice,
    paymentMethod, bookingDate
);
```

### Show Notification
```java
NotificationManager.showSuccess("Booking confirmed!", rootPane);
NotificationManager.showError("Failed to book", rootPane);
NotificationManager.showWarning("Check your dates", rootPane);
NotificationManager.showInfo("Processing...", rootPane);
```

### Enhanced Booking with Auto-Email & PDF
```java
EnhancedBookingService service = new EnhancedBookingService();
BookingResult result = service.createHotelBooking(booking, user);

if (result.isSuccess()) {
    // Email sent + PDF generated automatically
    System.out.println("Booking ID: " + result.getBookingId());
    System.out.println("PDF: " + result.getPdfPath());
}
```

### Log Activity
```java
AdminActivityLogsController.logActivity(
    userId,
    "CREATE",           // Action
    "Booking",          // Entity
    "Created booking #12345",
    "127.0.0.1",        // IP
    "INFO"              // Severity
);
```

---

## 🗂️ Key File Locations

### Controllers
```
Admin:    src/main/java/ui/controllers/admin/
Employee: src/main/java/ui/controllers/employee/
General:  src/main/java/ui/controllers/
```

### Views
```
Admin:    src/main/resources/ui/admin/
Employee: src/main/resources/ui/employee/
General:  src/main/resources/ui/
```

### Services
```
src/main/java/ui/service/
  ├── EmailService.java
  ├── PDFReceiptService.java
  ├── EnhancedBookingService.java
  ├── UserService.java
  ├── HotelBookingService.java
  └── ...
```

---

## 🔧 Configuration

### Email (SMTP)
```sql
UPDATE system_settings SET setting_value = 'true' WHERE setting_key = 'email_enabled';
UPDATE system_settings SET setting_value = 'smtp.gmail.com' WHERE setting_key = 'smtp_host';
UPDATE system_settings SET setting_value = '587' WHERE setting_key = 'smtp_port';
UPDATE system_settings SET setting_value = 'your-email@gmail.com' WHERE setting_key = 'smtp_username';
UPDATE system_settings SET setting_value = 'your-app-password' WHERE setting_key = 'smtp_password';
```

### Database Connection
```java
// src/main/java/ui/util/DataSource.java
private static final String URL = "jdbc:mysql://localhost:3306/tripwise_db";
private static final String USER = "root";
private static final String PASSWORD = "";
```

---

## 🐛 Troubleshooting

### Maven Build Failed
```bash
# Clear cache and rebuild
mvn clean
mvn compile -U
```

### Database Connection Error
```bash
# Check MySQL is running (XAMPP)
# Verify database exists
mysql -u root -p -e "SHOW DATABASES LIKE 'tripwise_db';"
```

### Email Not Sending
```sql
-- Check email is enabled
SELECT * FROM system_settings WHERE setting_key LIKE 'email%';

-- Test with simple email first
-- Check SMTP credentials are correct
-- For Gmail, use App Password (not regular password)
```

### PDF Not Generating
```java
// Check output directory exists
File dir = new File("C:/Users/[Username]/TripWise_Receipts");
if (!dir.exists()) dir.mkdirs();

// Verify iText dependencies in pom.xml
```

---

## 📚 Documentation

- **Admin Panel:** `ADMIN_PANEL_README.md`
- **Advanced Features:** `PHASE_5_ADVANCED_FEATURES.md`
- **Complete Summary:** `PROJECT_SUMMARY.md`
- **This Guide:** `QUICK_START_GUIDE.md`

---

## 🎯 Key User Roles

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access, all features |
| **RESPONSABLE** | Admin panel access, employee features |
| **EMPLOYE** | Booking management, customer service |
| **VOYAGEUR** | Book hotels/flights, view bookings |
| **VISITEUR** | Browse only, limited access |

---

## 📊 Database Quick Reference

### Main Tables
```sql
users              -- User accounts
hotels             -- Hotel catalog
chambres           -- Hotel rooms
vols               -- Flights
vehicules          -- Rental vehicles
reservations_hotel -- Hotel bookings
activity_logs      -- Audit trail
system_settings    -- Configuration
```

### Common Queries
```sql
-- All confirmed bookings
SELECT * FROM reservations_hotel WHERE statut_reservation = 'CONFIRMEE';

-- User's bookings
SELECT * FROM reservations_hotel WHERE voyageur_id = ?;

-- Today's activity
SELECT * FROM activity_logs WHERE DATE(created_at) = CURDATE();

-- System setting
SELECT setting_value FROM system_settings WHERE setting_key = 'email_enabled';
```

---

## 💻 Development Commands

```bash
# Clean build
mvn clean

# Compile only
mvn compile

# Run application
mvn javafx:run

# Package JAR
mvn package

# Skip tests
mvn clean install -DskipTests

# Update dependencies
mvn clean install -U
```

---

## 🎨 UI Color Reference

```css
Primary:   #3498db  /* Blue */
Success:   #27ae60  /* Green */
Warning:   #f39c12  /* Orange */
Danger:    #e74c3c  /* Red */
Info:      #9b59b6  /* Purple */
Dark:      #2c3e50  /* Dark Gray */
Light:     #ecf0f1  /* Light Gray */
```

---

## 🔐 Security Checklist

- [x] Passwords hashed with BCrypt
- [x] SQL injection prevention (PreparedStatements)
- [x] Role-based access control
- [x] Session timeout configured
- [x] Input validation on all forms
- [x] Activity logging enabled
- [x] Sensitive data encrypted

---

## 📞 Quick Links

- **Project Root:** `C:\Users\Ahmed Attafi\IdeaProjects\TripWise`
- **Receipts Folder:** `C:\Users\[Username]\TripWise_Receipts`
- **Backups Folder:** `C:\Users\[Username]\TripWise_Backups`
- **Database:** `localhost:3306/tripwise_db`

---

## ✅ Pre-Launch Checklist

- [ ] Database schema imported
- [ ] Test accounts working
- [ ] Email configuration tested
- [ ] PDF generation tested
- [ ] All admin features accessible
- [ ] Backup system functional
- [ ] Activity logs recording
- [ ] Notifications displaying
- [ ] Charts rendering
- [ ] Search functionality working

---

**Version:** 1.5.0  
**Last Updated:** Feb 4, 2026  
**Status:** Production Ready ✅
