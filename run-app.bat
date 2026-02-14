@echo off
cd /d "C:\Users\Utilisateur\Desktop\Java's Project\TripWise"
echo ========================================
echo        TripWise - Build and Run
echo ========================================
echo.
echo === Cleaning and compiling... ===
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo === Build failed! ===
    pause
    exit /b 1
)
echo === Build successful! ===
echo.
echo === Starting application... ===
call mvn javafx:run
echo.
echo === Application closed ===
pause
