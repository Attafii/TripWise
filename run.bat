@echo off
echo ========================================
echo TripWise - Build and Run Script
echo ========================================
echo.

echo Step 1: Cleaning previous build...
call mvn clean
if %errorlevel% neq 0 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Compiling and copying resources...
call mvn compile
if %errorlevel% neq 0 (
    echo ERROR: Maven compile failed!
    pause
    exit /b 1
)

echo.
echo Step 3: Running application...
call mvn javafx:run

pause
