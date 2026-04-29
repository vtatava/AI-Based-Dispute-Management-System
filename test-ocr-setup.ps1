# Test OCR Setup Script
# This script verifies Tesseract OCR installation and configuration

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Tesseract OCR Setup Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if Tesseract is installed
Write-Host "Test 1: Checking Tesseract installation..." -ForegroundColor Yellow
$tesseractPath = "C:\Program Files\Tesseract-OCR\tesseract.exe"

if (Test-Path $tesseractPath) {
    Write-Host "✓ Tesseract found at: $tesseractPath" -ForegroundColor Green
    
    # Get version
    try {
        $version = & $tesseractPath --version 2>&1 | Select-Object -First 1
        Write-Host "  Version: $version" -ForegroundColor Green
    } catch {
        Write-Host "  Warning: Could not get version" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ Tesseract NOT found at: $tesseractPath" -ForegroundColor Red
    Write-Host "  Please install Tesseract from: https://github.com/UB-Mannheim/tesseract/wiki" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test 2: Check tessdata folder
Write-Host "Test 2: Checking tessdata folder..." -ForegroundColor Yellow
$tessdataPath = "C:\Program Files\Tesseract-OCR\tessdata"

if (Test-Path $tessdataPath) {
    Write-Host "✓ Tessdata folder found at: $tessdataPath" -ForegroundColor Green
    
    # Check for English language data
    $engData = Join-Path $tessdataPath "eng.traineddata"
    if (Test-Path $engData) {
        Write-Host "✓ English language data found (eng.traineddata)" -ForegroundColor Green
    } else {
        Write-Host "✗ English language data NOT found" -ForegroundColor Red
        Write-Host "  Please download from: https://github.com/tesseract-ocr/tessdata" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ Tessdata folder NOT found" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 3: Check application.properties configuration
Write-Host "Test 3: Checking application.properties..." -ForegroundColor Yellow
$propsFile = "backend\src\main\resources\application.properties"

if (Test-Path $propsFile) {
    $content = Get-Content $propsFile -Raw
    
    if ($content -match "ocr\.enabled=true") {
        Write-Host "✓ OCR is ENABLED in configuration" -ForegroundColor Green
    } else {
        Write-Host "✗ OCR is DISABLED in configuration" -ForegroundColor Red
        Write-Host "  Set ocr.enabled=true in application.properties" -ForegroundColor Yellow
    }
    
    if ($content -match "tesseract\.datapath=C:/Program Files/Tesseract-OCR/tessdata") {
        Write-Host "✓ Tesseract datapath is configured correctly" -ForegroundColor Green
    } else {
        Write-Host "⚠ Tesseract datapath may need adjustment" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ application.properties not found" -ForegroundColor Red
}

Write-Host ""

# Test 4: Check if backend is running
Write-Host "Test 4: Checking backend status..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090/api/dispute/validate-id" -Method POST -ErrorAction SilentlyContinue
    Write-Host "⚠ Backend is running but endpoint needs proper request" -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✓ Backend is running (endpoint accessible)" -ForegroundColor Green
    } else {
        Write-Host "✗ Backend is NOT running" -ForegroundColor Red
        Write-Host "  Start backend with: cd backend && mvnw spring-boot:run" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Setup Verification Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. If backend is not running, start it with:" -ForegroundColor White
Write-Host "   cd backend" -ForegroundColor Gray
Write-Host "   .\mvnw.cmd spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Open the frontend at: http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "3. Upload an ID image with text like:" -ForegroundColor White
Write-Host "   - AADHAAR: 123456789" -ForegroundColor Gray
Write-Host "   - Name: Rajesh Kumar" -ForegroundColor Gray
Write-Host ""
Write-Host "4. The system will automatically extract and validate the ID!" -ForegroundColor White
Write-Host ""

# Made with Bob
