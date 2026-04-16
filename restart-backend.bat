@echo off
echo Restarting Backend Server...
echo.
echo Stopping existing backend process...
taskkill /F /FI "WINDOWTITLE eq *mvnw.cmd spring-boot:run*" 2>nul
timeout /t 2 /nobreak >nul

echo.
echo Starting backend server...
cd backend
start "Backend Server" cmd /k ".\mvnw.cmd spring-boot:run"
echo.
echo Backend server is starting...
echo Wait for "Started DisputeAiApplication" message in the backend window
echo.
pause

@REM Made with Bob
