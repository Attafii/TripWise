@echo off
echo ========================================
echo TripWise - Project Verification Script
echo ========================================
echo.

echo Checking project structure...
echo.

echo [1/6] Checking src/main/java/ui/app/Main.java...
if exist "src\main\java\ui\app\Main.java" (
    echo    ✓ Main.java found
) else (
    echo    ✗ Main.java NOT FOUND!
)

echo [2/6] Checking src/main/resources/ui/login.fxml...
if exist "src\main\resources\ui\login.fxml" (
    echo    ✓ login.fxml found
) else (
    echo    ✗ login.fxml NOT FOUND!
)

echo [3/6] Checking src/main/resources/ui/dashboard.fxml...
if exist "src\main\resources\ui\dashboard.fxml" (
    echo    ✓ dashboard.fxml found
) else (
    echo    ✗ dashboard.fxml NOT FOUND!
)

echo [4/6] Checking src/main/resources/ui/style.css...
if exist "src\main\resources\ui\style.css" (
    echo    ✓ style.css found
) else (
    echo    ✗ style.css NOT FOUND!
)

echo [5/6] Checking pom.xml...
if exist "pom.xml" (
    echo    ✓ pom.xml found
) else (
    echo    ✗ pom.xml NOT FOUND!
)

echo [6/6] Checking if resources are in target/classes...
if exist "target\classes\ui\login.fxml" (
    echo    ✓ Resources compiled to target/classes
) else (
    echo    ✗ Resources NOT in target/classes - Run: mvn clean compile
)

echo.
echo ========================================
echo Checking Java and Maven...
echo ========================================
echo.

echo Java version:
java -version 2>&1 | findstr "version"

echo.
echo Maven version:
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    mvn -version 2>&1 | findstr "Apache Maven"
) else (
    echo Maven NOT FOUND in PATH!
    echo Please install Maven or use IDE's built-in Maven
)

echo.
echo ========================================
echo Recommendations:
echo ========================================
echo.
echo 1. If resources NOT in target/classes, run:
echo    mvn clean compile
echo.
echo 2. To run the application:
echo    mvn javafx:run
echo.
echo 3. If using IntelliJ IDEA:
echo    - Mark src/main/resources as Resources Root
echo    - Build → Rebuild Project
echo    - Run Main.java
echo.
echo ========================================
pause
