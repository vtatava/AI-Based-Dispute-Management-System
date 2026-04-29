# Simple Receipt Validation Test
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Testing Receipt Validation Feature" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9090/api/dispute"
$receiptFile = "test-receipt.txt"
$idFile = "Sample_Images/Amit_Patel.png"

# Check if files exist
if (-not (Test-Path $receiptFile)) {
    Write-Host "Error: Receipt file not found: $receiptFile" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $idFile)) {
    Write-Host "Error: ID file not found: $idFile" -ForegroundColor Red
    exit 1
}

Write-Host "Test Files:" -ForegroundColor Yellow
Write-Host "  Receipt: $receiptFile" -ForegroundColor White
Write-Host "  ID Document: $idFile" -ForegroundColor White
Write-Host ""

# Test 1: Valid Receipt - All data matches
Write-Host "========================================" -ForegroundColor Green
Write-Host "TEST 1: Valid Receipt (All Data Matches)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Sending request with matching data..." -ForegroundColor Yellow

$form = @{
    amount = "5000"
    transactionLocation = "USA"
    userCurrentLocation = "INDIA"
    description = "Unauthorized transaction detected"
    userId = "ABC003"
    transactionType = "ONLINE"
    transactionDateTime = "2026-04-28T00:00:00"
    useAgenticMode = "false"
    transactionReceipt = Get-Item -Path $receiptFile
    userIdDocument = Get-Item -Path $idFile
}

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/raise-with-files" -Method Post -Form $form -ContentType "multipart/form-data"
    
    Write-Host "Response Received:" -ForegroundColor Green
    Write-Host "  Intent: $($response.intent)" -ForegroundColor White
    Write-Host "  Decision: $($response.decision)" -ForegroundColor White
    Write-Host "  Risk Score: $($response.riskScore)" -ForegroundColor White
    Write-Host "  Refund Amount: $($response.refundAmount)" -ForegroundColor White
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Cyan
    Write-Host $response.reviewReason -ForegroundColor White
    Write-Host ""
    
    if ($response.decision -eq "AUTO_REFUND") {
        Write-Host "✓ TEST 1 PASSED: Auto refund approved!" -ForegroundColor Green
    } else {
        Write-Host "✗ TEST 1 FAILED: Expected AUTO_REFUND, got $($response.decision)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ TEST 1 FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob