@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Starting Dispute AI Backend Server
echo ========================================
echo.

REM Check if Java is installed
echo Checking Java installation...
java -version 2>&1 | findstr /i "version" >nul
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 17 or higher
    echo.
    echo Download from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo [OK] Java found!
java -version 2>&1 | findstr /i "version"
echo.

REM Navigate to backend directory
echo Navigating to backend directory...
cd /d "%~dp0backend"

if %errorlevel% neq 0 (
    echo [ERROR] Failed to navigate to backend directory
    echo Current directory: %CD%
    pause
    exit /b 1
)

echo [OK] Current directory: %CD%
echo.

REM Check if Maven wrapper exists
if not exist "mvnw.cmd" (
    echo [ERROR] Maven wrapper (mvnw.cmd) not found
    echo Expected location: %CD%\mvnw.cmd
    echo.
    dir mvnw.*
    echo.
    pause
    exit /b 1
)

echo [OK] Maven wrapper found
echo.

echo ========================================
echo  Building the project...
echo  This may take a few minutes...
echo ========================================
echo.

call mvnw.cmd clean install -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Maven build failed
    echo Check the error messages above
    echo.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Build completed successfully!
echo.
echo ========================================
echo  Starting Spring Boot Application
echo ========================================
echo.
echo Backend will be available at: http://localhost:9090
echo.
echo Press Ctrl+C to stop the server
echo.

call mvnw.cmd spring-boot:run

REM If we reach here, the server has stopped
echo.
echo [INFO] Spring Boot application has stopped
echo.
pause

@REM Made with Bob
