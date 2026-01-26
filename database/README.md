# 🗄️ Database Setup Guide - TripWise

## 📋 Prerequisites
- **XAMPP** installed (with MySQL/MariaDB)
- **Java 17** or higher
- **Maven** configured

---

## 🚀 Step-by-Step Setup

### **Step 1: Start XAMPP MySQL**

1. Open **XAMPP Control Panel**
2. Click **Start** for **Apache** (optional)
3. Click **Start** for **MySQL**
4. Verify MySQL is running (green indicator)

![XAMPP Running](https://i.imgur.com/xampp-example.png)

---

### **Step 2: Create Database**

#### **Option A: Using phpMyAdmin (GUI)**

1. Open your browser and go to: `http://localhost/phpmyadmin`
2. Click **"New"** in the left sidebar
3. Database name: `tripwise_db`
4. Collation: `utf8mb4_unicode_ci`
5. Click **"Create"**

#### **Option B: Using MySQL Command Line**

```bash
# Open XAMPP Shell or MySQL Command Line
mysql -u root -p

# Create database
CREATE DATABASE tripwise_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

---

### **Step 3: Import Database Schema**

#### **Option A: Using phpMyAdmin**

1. Go to `http://localhost/phpmyadmin`
2. Select `tripwise_db` from the left sidebar
3. Click **"Import"** tab
4. Click **"Choose File"**
5. Navigate to: `C:\Users\Ahmed Attafi\IdeaProjects\TripWise\database\setup.sql`
6. Click **"Go"**
7. ✅ You should see: "Import has been successfully finished"

#### **Option B: Using Command Line**

```bash
# Navigate to project directory
cd "C:\Users\Ahmed Attafi\IdeaProjects\TripWise"

# Import SQL file
mysql -u root -p tripwise_db < database\setup.sql
```

---

### **Step 4: Verify Database Setup**

1. Go to phpMyAdmin: `http://localhost/phpmyadmin`
2. Click on `tripwise_db`
3. You should see these tables:
   - ✅ `users`
   - ✅ `flights`
   - ✅ `hotels`
   - ✅ `cars`
   - ✅ `flight_bookings`
   - ✅ `hotel_bookings`
   - ✅ `car_rentals`

4. Click on `users` table → Browse
5. You should see 3 sample users

---

### **Step 5: Update Database Credentials (if needed)**

If your MySQL has a password or different configuration:

**Edit:** `src/main/java/ui/util/DataSource.java`

```java
private static final String URL = "jdbc:mysql://localhost:3306/tripwise_db";
private static final String USERNAME = "root";
private static final String PASSWORD = ""; // Change if you set a password
```

---

### **Step 6: Add MySQL Dependency & Test Connection**

1. **Maven will download MySQL Connector automatically**
   ```bash
   mvn clean install
   ```

2. **Test database connection** (optional test class):

Create a test file to verify connection works.

---

## 🔍 Database Schema Overview

### **Users Table**
```sql
- id (Primary Key)
- first_name
- last_name
- email (Unique)
- password
- phone
- address
- created_at
- updated_at
```

### **Flights Table**
```sql
- id (Primary Key)
- airline
- flight_number
- departure_city
- arrival_city
- departure_time
- arrival_time
- price
- available_seats
- status
```

### **Hotels Table**
```sql
- id (Primary Key)
- name
- city
- address
- price_per_night
- rating
- available_rooms
- amenities
- description
```

### **Cars Table**
```sql
- id (Primary Key)
- model
- brand
- type
- year
- price_per_day
- available
- location
- features
```

### **Booking Tables**
- `flight_bookings` - Links users to flights
- `hotel_bookings` - Links users to hotels
- `car_rentals` - Links users to cars

All booking tables have foreign keys to `users` table.

---

## 🧪 Sample Login Credentials

After importing the database, you can login with:

| Email | Password |
|-------|----------|
| `john.doe@example.com` | `password123` |
| `jane.smith@example.com` | `password123` |
| `ahmed.attafi@example.com` | `password123` |

⚠️ **Important:** These are test credentials. Change them in production!

---

## 🛠️ Troubleshooting

### **Problem: Connection Refused**
```
Solution: Make sure MySQL is running in XAMPP Control Panel
```

### **Problem: Access Denied for user 'root'**
```
Solution: Check if you've set a password for MySQL root user
Update DataSource.java with correct password
```

### **Problem: Unknown database 'tripwise_db'**
```
Solution: Create the database first (Step 2)
Then import the schema (Step 3)
```

### **Problem: Table doesn't exist**
```
Solution: Import setup.sql file again
Make sure import completed without errors
```

---

## ✅ Verification Checklist

- [ ] XAMPP MySQL is running
- [ ] Database `tripwise_db` exists
- [ ] All 7 tables created successfully
- [ ] Sample data imported (3 users, flights, hotels, cars)
- [ ] Maven dependencies downloaded
- [ ] Application can connect to database

---

## 🔐 Security Notes

1. **Password Hashing**: Currently passwords are stored in plain text. 
   - TODO: Implement password hashing (BCrypt recommended)

2. **SQL Injection**: Using PreparedStatements prevents SQL injection
   - ✅ Already implemented in UserService

3. **Production Setup**:
   - Change default passwords
   - Use environment variables for credentials
   - Enable MySQL authentication
   - Use SSL for database connections

---

## 📚 Next Steps

After database setup:
1. Test user authentication in LoginController
2. Implement flight/hotel/car services
3. Create booking functionality
4. Add profile management
5. Implement booking history in dashboard

---

**Need Help?** Check logs in IntelliJ console for detailed error messages.
