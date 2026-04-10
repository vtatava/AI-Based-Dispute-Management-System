@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Starting Dispute AI Frontend (React)
echo ========================================
echo.

REM Set Node.js path
set "NODE_PATH=C:\Program Files\nodejs"
set "PATH=%NODE_PATH%;%PATH%"

REM Check if Node.js is installed
echo Checking Node.js installation...
"%NODE_PATH%\node.exe" -v >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Node.js not found at: %NODE_PATH%
    echo Please install Node.js 16 or higher
    echo.
    echo Download from: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

echo [OK] Node.js found:
"%NODE_PATH%\node.exe" -v
echo.

REM Check if npm is installed
echo Checking npm installation...
"%NODE_PATH%\npm.cmd" -v >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] npm is not installed or not in PATH
    echo.
    pause
    exit /b 1
)

echo [OK] npm found:
"%NODE_PATH%\npm.cmd" -v
echo.

REM Navigate to frontend directory
echo Navigating to frontend directory...
cd /d "%~dp0frontend"

if %errorlevel% neq 0 (
    echo [ERROR] Failed to navigate to frontend directory
    echo Current directory: %CD%
    pause
    exit /b 1
)

echo [OK] Current directory: %CD%
echo.

REM Check if package.json exists
if not exist "package.json" (
    echo [ERROR] package.json not found
    echo Expected location: %CD%\package.json
    echo.
    pause
    exit /b 1
)

echo [OK] package.json found
echo.

REM Check if node_modules exists
if not exist "node_modules\" (
    echo ========================================
    echo  Installing dependencies...
    echo  This may take a few minutes...
    echo ========================================
    echo.
    call "%NODE_PATH%\npm.cmd" install
    
    if %errorlevel% neq 0 (
        echo.
        echo [ERROR] npm install failed
        echo Check the error messages above
        echo.
        pause
        exit /b 1
    )
    echo.
    echo [SUCCESS] Dependencies installed successfully!
    echo.
) else (
    echo [OK] Dependencies already installed
    echo.
)

echo ========================================
echo  Starting React Development Server
echo ========================================
echo.
echo Frontend will be available at: http://localhost:3000
echo Browser will open automatically
echo.
echo Press Ctrl+C to stop the server
echo.
echo NOTE: Make sure backend is running on port 8080
echo.

call "%NODE_PATH%\npm.cmd" start

REM If we reach here, the server has stopped
echo.
echo [INFO] React development server has stopped
echo.
pause

@REM Made with Bob
