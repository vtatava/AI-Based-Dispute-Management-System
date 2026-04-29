@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Starting Dispute AI Application
echo  Backend + Frontend
echo ========================================
echo.

REM Set Node.js path
set "NODE_PATH=C:\Program Files\nodejs"

REM Check prerequisites
echo Checking prerequisites...
echo.

REM Check Java
echo [1/4] Checking Java...
java -version 2>&1 | findstr /i "version" >nul
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 17 or higher
    echo Download from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo [OK] Java found
java -version 2>&1 | findstr /i "version"
echo.

REM Check Maven wrapper
echo [2/4] Checking Maven wrapper...
if not exist "%~dp0backend\mvnw.cmd" (
    echo [ERROR] Maven wrapper (mvnw.cmd) not found in backend directory
    echo Expected location: %~dp0backend\mvnw.cmd
    echo.
    pause
    exit /b 1
)
echo [OK] Maven wrapper found
echo.

REM Check Node.js
echo [3/4] Checking Node.js...
"%NODE_PATH%\node.exe" -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js not found at: %NODE_PATH%
    echo Please install Node.js 16 or higher
    echo Download from: https://nodejs.org/
    echo.
    pause
    exit /b 1
)
echo [OK] Node.js found:
"%NODE_PATH%\node.exe" -v
echo.

REM Check npm
echo [4/4] Checking npm...
"%NODE_PATH%\npm.cmd" -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] npm is not installed or not in PATH
    echo.
    pause
    exit /b 1
)
echo [OK] npm found:
"%NODE_PATH%\npm.cmd" -v
echo.

echo ========================================
echo  All prerequisites satisfied!
echo ========================================
echo.

echo ========================================
echo  Starting Backend Server...
echo ========================================
echo.

REM Start backend in a new window
start "Dispute AI - Backend Server" cmd /k "%~dp0start-backend.bat"

echo [OK] Backend starting in separate window...
echo Waiting 15 seconds for backend to initialize...
echo.

timeout /t 15 /nobreak

echo ========================================
echo  Starting Frontend Server...
echo ========================================
echo.

REM Start frontend in a new window
start "Dispute AI - Frontend Server" cmd /k "%~dp0start-frontend.bat"

echo [OK] Frontend starting in separate window...
echo.

echo ========================================
echo  Application Started Successfully!
echo ========================================
echo.
echo Backend:  http://localhost:9090
echo Frontend: http://localhost:3000
echo.
echo Two command windows have been opened:
echo   1. Backend Server (Spring Boot) - Port 9090
echo   2. Frontend Server (React) - Port 3000
echo.
echo IMPORTANT:
echo - Keep both windows open while using the application
echo - Close those windows to stop the servers
echo - Frontend will open automatically in your browser
echo.
echo Press any key to exit this launcher...
pause >nul

@REM Made with Bob
