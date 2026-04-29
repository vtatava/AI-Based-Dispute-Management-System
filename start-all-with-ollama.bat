@echo off
echo ========================================
echo Starting AI Dispute Management System
echo WITH Ollama LLM Support
echo ========================================
echo.

REM Start Ollama service in a new window
echo [1/3] Starting Ollama Service...
start "Ollama Service" cmd /k "C:\Users\0022SM744\AppData\Local\Programs\Ollama\ollama.exe serve"
timeout /t 3 /nobreak >nul

REM Start Backend in a new window
echo [2/3] Starting Backend (Spring Boot)...
start "Backend Server" cmd /k "cd backend && mvnw.cmd spring-boot:run"
timeout /t 5 /nobreak >nul

REM Start Frontend in a new window
echo [3/3] Starting Frontend (React)...
start "Frontend Server" cmd /k "cd frontend && npm start"

echo.
echo ========================================
echo All services are starting!
echo ========================================
echo.
echo Three windows will open:
echo 1. Ollama Service (http://localhost:11434)
echo 2. Backend Server (http://localhost:9090)
echo 3. Frontend (http://localhost:3000)
echo.
echo Wait for all services to start (30-60 seconds)
echo Then open: http://localhost:3000
echo.
echo To stop: Close all three windows
echo ========================================
pause

@REM Made with Bob
