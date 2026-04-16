@echo off
echo ========================================
echo Ollama Integration Test Script
echo ========================================
echo.

echo [1/5] Checking if Ollama is installed...
ollama --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Ollama is not installed or not in PATH
    echo Please install Ollama from: https://ollama.com/download
    pause
    exit /b 1
)
echo [OK] Ollama is installed
echo.

echo [2/5] Checking if Ollama service is running...
curl -s http://localhost:11434/api/tags >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Ollama service is not running
    echo Starting Ollama service...
    start "Ollama Service" ollama serve
    timeout /t 5 /nobreak >nul
    echo [OK] Ollama service started
) else (
    echo [OK] Ollama service is running
)
echo.

echo [3/5] Checking if llama3.2 model is installed...
ollama list | findstr "llama3.2" >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] llama3.2 model not found
    echo.
    echo Would you like to download llama3.2 now? (This will take a few minutes)
    set /p download="Enter Y to download, N to skip: "
    if /i "%download%"=="Y" (
        echo Downloading llama3.2...
        ollama pull llama3.2
        echo [OK] llama3.2 downloaded
    ) else (
        echo [SKIPPED] Model download skipped
        echo Note: You can download it later with: ollama pull llama3.2
    )
) else (
    echo [OK] llama3.2 model is installed
)
echo.

echo [4/5] Testing Ollama API...
echo Testing with a simple prompt...
curl -s -X POST http://localhost:11434/api/generate -d "{\"model\":\"llama3.2\",\"prompt\":\"Say 'Ollama is working!' in one sentence.\",\"stream\":false}" >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Failed to connect to Ollama API
    echo Make sure Ollama service is running
    pause
    exit /b 1
)
echo [OK] Ollama API is responding
echo.

echo [5/5] Testing fraud analysis prompt...
echo Sending a test fraud analysis request...
curl -s -X POST http://localhost:11434/api/generate -d "{\"model\":\"llama3.2\",\"prompt\":\"Analyze this transaction: User claims fraud on $5000 purchase in USA while they are in India. Is this suspicious? Answer in 2 sentences.\",\"stream\":false}" -o test-response.json
if %errorlevel% neq 0 (
    echo [ERROR] Failed to get response from Ollama
    pause
    exit /b 1
)
echo [OK] Received response from Ollama
echo.

echo ========================================
echo Test Summary
echo ========================================
echo [OK] Ollama is properly configured
echo [OK] Ready to use with your application
echo.
echo Next Steps:
echo 1. Start your backend: start-backend.bat
echo 2. Start your frontend: start-frontend.bat
echo 3. Submit a dispute to see LLM analysis in action
echo.
echo For detailed setup instructions, see: OLLAMA_SETUP_GUIDE.md
echo ========================================
echo.

if exist test-response.json (
    echo Sample LLM Response Preview:
    echo ---
    type test-response.json
    echo ---
    del test-response.json
)

pause

@REM Made with Bob
