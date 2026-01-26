@echo off
echo ========================================
echo  TripWise - Running Database Test
echo ========================================
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set
    echo Please set JAVA_HOME to your JDK installation
    pause
    exit /b 1
)

echo Using Java: %JAVA_HOME%
echo.

cd /d "%~dp0"

REM Compile Java files
echo Compiling Java files...
"%JAVA_HOME%\bin\javac" -d target\classes -cp "target\classes;%USERPROFILE%\.m2\repository\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar" src\main\java\ui\test\DatabaseConnectionTest.java src\main\java\ui\model\User.java src\main\java\ui\service\*.java src\main\java\ui\util\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running Database Connection Test...
echo.
"%JAVA_HOME%\bin\java" -cp "target\classes;%USERPROFILE%\.m2\repository\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar" ui.test.DatabaseConnectionTest

echo.
echo ========================================
pause
