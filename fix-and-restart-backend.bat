@echo off
echo ========================================
echo Fixing Ollama LLM Integration Issue
echo ========================================
echo.

echo Step 1: Stopping current backend...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :9090 ^| findstr LISTENING') do (
    echo Killing process %%a
    taskkill /F /PID %%a 2>nul
)
timeout /t 2 /nobreak >nul

echo.
echo Step 2: Starting backend with updated configuration...
cd backend
start "Dispute AI Backend" java -jar target/dispute-ai-1.0.0.jar

echo.
echo ========================================
echo Backend is starting...
echo Please wait 30 seconds for initialization
echo ========================================
echo.
echo The backend will be available at: http://localhost:9090
echo.
echo To test the Ollama integration, run:
echo   test-backend-ollama.ps1
echo.
echo Press any key to exit...
pause >nul

@REM Made with Bob
