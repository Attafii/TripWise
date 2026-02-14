# TripWise - Build and Run Instructions

## 🔧 Fix "FXML resource not found" Error

### Option 1: Using Maven (Recommended)

1. Open terminal in project root directory
2. Run the build script:
   ```bash
   run.bat
   ```
   
   Or manually:
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

### Option 2: IntelliJ IDEA Setup

If running from IntelliJ IDEA:

1. **Mark Resources Directory**
   - Right-click `src/main/resources` folder
   - Select "Mark Directory as" → "Resources Root"
   - The folder icon should turn to a resources folder (purple)

2. **Rebuild Project**
   - Go to `Build` → `Rebuild Project`
   - This copies FXML files to `target/classes`

3. **Configure Run Configuration**
   - Go to `Run` → `Edit Configurations`
   - Make sure "Main class" is set to `ui.app.Main`
   - VM options (if needed):
     ```
     --add-opens javafx.graphics/com.sun.glass.utils=ALL-UNNAMED
     ```

4. **Run the Application**
   - Click the green Run button or press Shift+F10

### Option 3: Eclipse Setup

If running from Eclipse:

1. **Right-click project** → `Properties`
2. Go to `Java Build Path` → `Source` tab
3. Make sure `src/main/resources` is listed
4. If not, click `Add Folder` and add it
5. Clean and rebuild: `Project` → `Clean...`
6. Run: Right-click `Main.java` → `Run As` → `Java Application`

## ✅ Verify Resources Are Copied

After building, check if FXML files exist in:
```
target/classes/ui/login.fxml
target/classes/ui/dashboard.fxml
target/classes/ui/style.css
```

If files are missing, the resources are not being copied correctly.

## 🚨 Common Issues and Solutions

### Issue 1: "WARNING: Restricted methods will be blocked"
**Solution:** This is a Java warning, not an error. To suppress it, add this VM option:
```
--add-opens javafx.graphics/com.sun.glass.utils=ALL-UNNAMED
```

### Issue 2: "FXML resource not found: /ui/login.fxml"
**Solution:** 
1. Ensure `src/main/resources/ui/login.fxml` exists ✓
2. Run `mvn clean compile` to copy resources
3. Check `target/classes/ui/login.fxml` exists
4. If using IDE, mark `src/main/resources` as Resources Root

### Issue 3: Maven not found (mvn command not recognized)
**Solution:**
1. Install Maven from https://maven.apache.org/download.cgi
2. Add Maven to PATH environment variable
3. Or use IDE's built-in Maven (IntelliJ has it)

### Issue 4: MySQL Connection Error
**Solution:**
1. Start MySQL service
2. Create database: `CREATE DATABASE tripwise_db;`
3. Update `DataSource.java` with correct credentials
4. Run database schema (if provided)

## 🎯 Quick Start (After Fix)

1. **Build project:**
   ```bash
   mvn clean install
   ```

2. **Run application:**
   ```bash
   mvn javafx:run
   ```

3. **Login with test user:**
   - Email: admin@tripwise.com (or create one in database)
   - Password: (from database)

## 📁 Project Structure

```
TripWise/
├── src/main/java/ui/          # Java source files
│   ├── app/Main.java           # Application entry point
│   ├── controllers/            # UI controllers
│   ├── model/                  # Data models
│   ├── service/                # Business logic
│   └── util/                   # Utilities
├── src/main/resources/ui/      # FXML and resources
│   ├── login.fxml             # Login screen
│   ├── dashboard.fxml         # Main dashboard
│   ├── style.css              # Material Design CSS
│   └── ...                    # Other FXML files
├── target/classes/             # Compiled output (after build)
├── pom.xml                    # Maven configuration
└── run.bat                    # Build and run script
```

## 🔍 Debug Checklist

- [ ] Maven installed and in PATH
- [ ] MySQL server running
- [ ] Database `tripwise_db` exists
- [ ] `src/main/resources` marked as Resources Root (IDE)
- [ ] Project rebuilt (Build → Rebuild Project)
- [ ] FXML files exist in `target/classes/ui/`
- [ ] JavaFX version 20.0.2 dependencies downloaded
- [ ] No compilation errors in code

## 💡 Additional Tips

### Run with VM Options (suppress warnings):
```bash
mvn javafx:run -Djavafx.args="--add-opens javafx.graphics/com.sun.glass.utils=ALL-UNNAMED"
```

### Check if resources are in classpath:
```bash
dir target\classes\ui
# Should show: login.fxml, dashboard.fxml, style.css, etc.
```

### Force rebuild all:
```bash
mvn clean install -U
```

## 📞 Still Having Issues?

1. Check console output for exact error message
2. Verify file paths are correct (Windows uses backslashes)
3. Ensure Java 17+ is installed: `java -version`
4. Make sure JavaFX modules are in Maven repository
5. Try running from command line instead of IDE

---

**✅ After following these steps, the application should launch successfully!**
