@echo off
echo ========================================
echo Restarting Backend with AI ID Extraction
echo ========================================
echo.

echo Step 1: Stopping current backend...
taskkill /F /FI "WINDOWTITLE eq Administrator:  backend" 2>nul
timeout /t 2 /nobreak >nul

echo Step 2: Cleaning old build...
cd backend
call mvn clean -q

echo Step 3: Building with new AI ID extraction service...
call mvn package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo Step 4: Starting backend with AI-powered ID validation...
echo.
echo ========================================
echo Backend Starting on Port 9090
echo AI ID Extraction: ENABLED
echo ========================================
echo.
start "backend" cmd /k "java -jar target/dispute-ai-1.0.0.jar"

echo.
echo ========================================
echo Backend restarted successfully!
echo.
echo Features enabled:
echo - AI-Powered ID Extraction
echo - Fuzzy ID Matching (90%% threshold)
echo - Intelligent Name Validation
echo - Confidence Scoring
echo.
echo Test with: Sample_Images/Amit_Patel.png
echo ========================================
echo.
pause

@REM Made with Bob
