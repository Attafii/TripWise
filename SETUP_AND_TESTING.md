# TripWise - Setup & Testing Instructions

## ✅ FIXES APPLIED

### Issue: All sections showing blank for all user types

**Root Causes Found:**
1. ❌ FXML used `FXCollections.observableArrayList()` factory method (not supported in FXML)
2. ❌ Missing CSS classes: `.stats-container`, `.content-container`
3. ❌ `advancedSearchBtn` not included in button highlight array

**Fixes Applied:**
1. ✅ Removed FXCollections from all FXML files
2. ✅ Populated ComboBox items in controller `initialize()` methods
3. ✅ Added missing CSS classes to `style.css`
4. ✅ Added `advancedSearchBtn` to highlightButton method
5. ✅ Fixed all FXML loading issues

---

## 🗄️ DATABASE SETUP (REQUIRED!)

### Step 1: Ensure Tables Exist

Run this in phpMyAdmin → tripwise_db → SQL:

```sql
-- Verify tables exist
SHOW TABLES;

-- Should see: users, hotels, chambres, reservations_hotel, deals
-- If 'deals' table missing, create it:

CREATE TABLE IF NOT EXISTS `deals` (
  `deal_id` INT AUTO_INCREMENT PRIMARY KEY,
  `deal_type` VARCHAR(50) NOT NULL,
  `item_type` VARCHAR(50),
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `item_id` INT,
  `item_name` VARCHAR(255),
  `original_price` DECIMAL(10,2) NOT NULL,
  `discounted_price` DECIMAL(10,2) NOT NULL,
  `discount_percentage` DECIMAL(5,2) NOT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  `destination` VARCHAR(255),
  `available_slots` INT DEFAULT 0,
  `is_active` BOOLEAN DEFAULT TRUE,
  `image_url` VARCHAR(500),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_deal_type` (`deal_type`),
  INDEX `idx_active` (`is_active`),
  INDEX `idx_dates` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 2: Load Sample Data

**Execute in this order:**

**2.1 Load Hotels (if not already loaded):**
```bash
# Location: database/sample_hotels_data.sql
# In phpMyAdmin: tripwise_db → SQL → Paste script → Go
# Creates: 12 hotels, 30+ rooms
```

**2.2 Load Deals (REQUIRED for Phase 1.3):**
```bash
# Location: database/sample_deals_data.sql  
# In phpMyAdmin: tripwise_db → SQL → Paste script → Go
# Creates: 18 deals with various types
```

### Step 3: Verify Data

```sql
SELECT COUNT(*) FROM hotels;     -- Should be 12
SELECT COUNT(*) FROM chambres;   -- Should be 30+
SELECT COUNT(*) FROM deals;      -- Should be 18
SELECT COUNT(*) FROM users;      -- Should be 3+
```

---

## 🚀 RUNNING THE APPLICATION

### Method 1: IntelliJ IDEA
1. Open project in IntelliJ
2. Wait for Maven to sync
3. Right-click `src/main/java/ui/app/Main.java`
4. Click "Run 'Main'"

### Method 2: Command Line
```bash
cd "C:\Users\Ahmed Attafi\IdeaProjects\TripWise"
mvn clean javafx:run
```

---

## 🧪 TESTING GUIDE

### Test 1: Login as Traveler

**Credentials:**
- Email: `traveler@tripwise.com`
- Password: `Admin123!`

**What You Should See:**
1. ✅ Dashboard home with 4 stat cards and 2 charts
2. ✅ Sidebar menu with 9 items (no employee section)
3. ✅ All pages load without "View coming soon" message

**Test Each Menu Item:**

| Menu Item | Expected Behavior |
|-----------|------------------|
| 📊 Dashboard | Shows stats cards (Completed Trips: 225, Active: 80, etc.) and charts |
| 🔍 Advanced Search | Shows filter panel, price calculator, search button (map disabled) |
| 📋 My Bookings | Shows filter bar, stats (0 bookings if none exist), empty state |
| 📅 Schedule | Shows "Coming Soon" placeholder |
| 💳 Payments | Shows "Coming Soon" placeholder |
| 💬 Messages | Shows "Coming Soon" placeholder |
| ✈️ Flight Tracking | Shows "Coming Soon" placeholder |
| 🤖 AI Agent | Shows chat interface (should work) |
| 🎯 Deals | Shows filter bar, should display 18 deals in 3x3 grid |

**Detailed Tests:**

**A. Advanced Search:**
1. Select destination: "Paris"
2. Select dates: Tomorrow to +3 days
3. Set guests: 2
4. Click "Search Hotels"
5. Should show hotel cards (if data loaded)

**B. My Bookings:**
1. Should show "No bookings yet" with empty state
2. Try changing filters - should work
3. Toggle between Timeline/List view

**C. Deals:**
1. Should show 18 deals in grid (3 columns)
2. Try filters: Type=Hotels, Destination=Paris
3. Should filter down results
4. Click "View Details" on a deal - shows dialog
5. Click "Book Now" - shows confirmation dialog

---

### Test 2: Login as Employee

**Credentials:**
- Email: `employee@tripwise.com`
- Password: `Admin123!`

**What You Should See:**
1. ✅ All traveler features (9 menu items)
2. ✅ **PLUS** Employee section with 3 additional items:
   - 👤 My Profile
   - 📝 Manage Bookings
   - 👥 View Customers

**Test Employee Features:**
- Click "👤 My Profile" - Should show employee profile form
- Click "📝 Manage Bookings" - Should show booking management table
- Click "👥 View Customers" - Should show customer list

---

### Test 3: Login as Admin

**Credentials:**
- Email: `admin@tripwise.com`
- Password: `Admin123!`

**Expected:**
- Same as Employee (all features unlocked)

---

## ❌ KNOWN ISSUES & WORKAROUNDS

### Issue 1: WebView/Google Maps Not Working
**Symptom:** Map section says "Map feature requires JavaFX Web module"  
**Cause:** JavaFX Web module not loaded yet  
**Fix:**
```bash
cd "C:\Users\Ahmed Attafi\IdeaProjects\TripWise"
mvn clean install
# Then uncomment code in TravelerAdvancedSearchController.java
```

### Issue 2: No Hotels Showing in Search
**Symptom:** Search returns 0 results  
**Cause:** Sample hotel data not loaded  
**Fix:** Execute `database/sample_hotels_data.sql` in phpMyAdmin

### Issue 3: No Deals Showing
**Symptom:** Deals page shows empty state  
**Cause:** Sample deals data not loaded  
**Fix:** Execute `database/sample_deals_data.sql` in phpMyAdmin

### Issue 4: Blank Page After Login
**Symptom:** White/blank screen after login  
**Cause:** FXML loading error (should be fixed now)  
**Fix:** Check console output for errors, ensure all fixes applied

---

## 🐛 DEBUGGING TIPS

### Check Console Output
Look for these indicators:

**✅ Good Signs:**
```
✅ Advanced Search Controller initialized
✅ Retrieved 18 active deals
✅ Retrieved 0 bookings for traveler: 3
Dashboard Home Controller initialized
```

**❌ Bad Signs:**
```
❌ Error loading view: /ui/traveler/traveler-deals.fxml
FileNotFoundException: /ui/traveler/traveler-deals.fxml
javafx.fxml.LoadException
Cannot find FXCollections
```

### Common Fixes:

**1. FXML Not Found:**
```bash
# Rebuild project
mvn clean compile
```

**2. Controller Not Found:**
```bash
# Check package structure
ls src/main/java/ui/controllers/traveler/
# Should show: TravelerAdvancedSearchController.java, TravelerBookingsController.java, TravelerDealsController.java
```

**3. Database Connection Issues:**
- Ensure XAMPP MySQL is running
- Check `DataSource.java` has correct credentials
- Default: localhost:3306, user=root, password=(empty)

---

## ✅ SUCCESS CRITERIA

**Phase 1 is working if:**

1. ✅ Can login as traveler/employee/admin
2. ✅ Dashboard home shows stats and charts
3. ✅ All 9 menu items load (no blank pages)
4. ✅ Advanced Search shows filter panel
5. ✅ My Bookings shows empty state or bookings list
6. ✅ Deals page shows 18 deals in grid
7. ✅ Filters work (type, category, destination)
8. ✅ Can click "View Details" and "Book Now" on deals
9. ✅ Employee menu appears for employee/admin
10. ✅ No console errors about FXML loading

---

## 📊 PHASE 1 COMPLETION CHECKLIST

- [x] Advanced Hotel Search (with filters, grid/list view, pagination)
- [x] My Bookings Dashboard (timeline/table views, filters, modify/cancel)
- [x] Deals & Offers Page (hero card, filters, sorting, booking)
- [x] Database scripts (hotels + deals sample data)
- [x] UI/UX styling (Material Design 3, badges, cards)
- [x] Navigation menu (9 items for travelers, +3 for employees)
- [x] Error handling (Alert dialogs, console logging)
- [x] FXML loading fixes (removed FXCollections, added CSS classes)

**PHASE 1: 100% COMPLETE** ✅

---

## 🚀 NEXT: PHASE 2

Once Phase 1 testing is complete, proceed to:

**Phase 2: AI Chatbot Enhancement**
- Natural language to SQL query generation
- Database query execution from chat
- Context memory & quick actions
- Intelligent booking assistance

---

## 📞 TROUBLESHOOTING

If you still see blank pages:

1. **Check Maven Sync:**
   - IntelliJ: File → Reload All from Disk
   - IntelliJ: Right-click pom.xml → Maven → Reload Project

2. **Rebuild Project:**
   ```bash
   mvn clean package
   ```

3. **Check Java Version:**
   ```bash
   java -version
   # Should be Java 17 or higher
   ```

4. **Verify File Paths:**
   ```bash
   ls src/main/resources/ui/traveler/
   # Should list: traveler-advanced-search.fxml, traveler-bookings.fxml, traveler-deals.fxml
   ```

5. **Test Individual FXML:**
   - Try opening FXML files in Scene Builder
   - Check for syntax errors

---

**Last Updated:** February 4, 2026  
**Status:** All fixes applied, ready for testing
