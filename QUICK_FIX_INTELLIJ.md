# 🚀 Quick Fix for "FXML resource not found" in IntelliJ IDEA

## The Problem
```
Error: FXML resource not found: /ui/login.fxml
```

This happens because IntelliJ IDEA is not copying FXML files from `src/main/resources` to `target/classes`.

## ✅ SOLUTION (3 Simple Steps)

### Step 1: Mark Resources Directory
1. In Project view, find `src/main/resources` folder
2. **Right-click** on it
3. Select **"Mark Directory as"** → **"Resources Root"**
4. The folder icon should turn **purple/blue** (resources folder icon)

### Step 2: Rebuild Project
1. Go to menu: **Build** → **Rebuild Project**
2. Wait for build to complete
3. Check the Build output window for errors

### Step 3: Verify and Run
1. In Project view, expand: `target/classes/ui/`
2. You should now see:
   - `login.fxml`
   - `dashboard.fxml`
   - `style.css`
   - All other FXML files
3. **Run** the application (Shift+F10 or click green Run button)

## 🎯 Alternative: Use Run Configuration

If still not working, configure the Run Configuration:

1. Click **Run** → **Edit Configurations...**
2. Click **+** → **Application**
3. Set these values:
   - **Name:** TripWise
   - **Main class:** `ui.app.Main`
   - **VM options:** (optional, to suppress warnings)
     ```
     --add-opens javafx.graphics/com.sun.glass.utils=ALL-UNNAMED
     ```
   - **Working directory:** `$PROJECT_DIR$`
   - **Use classpath of module:** TripWise
4. Click **Apply** → **OK**
5. Run this configuration

## 🔧 If Still Not Working

### Option A: Invalidate Caches
1. **File** → **Invalidate Caches...**
2. Check **"Clear file system cache and Local History"**
3. Click **Invalidate and Restart**

### Option B: Reimport Maven Project
1. Open **Maven** tool window (right sidebar)
2. Click **Refresh** icon (circular arrows)
3. Wait for dependencies to download
4. **Build** → **Rebuild Project**

### Option C: Clean and Rebuild via Maven
1. Open **Maven** tool window
2. Expand **Lifecycle**
3. Double-click **clean**
4. Double-click **compile**
5. Run the application

## ✅ Verify Success

After these steps, run the application. You should see:
```
Switching to scene: /ui/login.fxml
[No error - login screen appears]
```

## 📸 Expected Project Structure in IntelliJ

```
TripWise
├── 📦 src/main/java
│   └── 📦 ui
│       ├── 📦 app
│       │   └── Main.java
│       ├── 📦 controllers
│       ├── 📦 model
│       ├── 📦 service
│       └── 📦 util
├── 📁 src/main/resources  ← Should be PURPLE (Resources Root)
│   └── 📁 ui
│       ├── login.fxml
│       ├── dashboard.fxml
│       ├── style.css
│       └── ...
└── 📁 target/classes  ← Should contain all compiled files + resources
    └── 📁 ui
        ├── login.fxml  ← Must be here!
        ├── dashboard.fxml
        ├── style.css
        └── ...
```

## 🎓 Why This Happens

IntelliJ IDEA needs to know which folders contain resources that should be copied to the output directory. When you mark `src/main/resources` as "Resources Root", IntelliJ:

1. Copies all files from this folder to `target/classes` during build
2. Adds this folder to the classpath
3. Makes resources accessible via `getResource()` calls

Without this configuration, FXML files stay in `src/main/resources` and are NOT available at runtime.

---

**✅ After following these steps, your application should launch successfully!**

**Still having issues?** Check `BUILD_INSTRUCTIONS.md` for more detailed troubleshooting.
