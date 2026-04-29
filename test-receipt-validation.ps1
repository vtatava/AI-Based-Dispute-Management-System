# Test Receipt Validation Feature
# This script tests the new transaction receipt validation functionality

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
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "TEST 2: Invalid Receipt (Wrong Amount)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

Write-Host "Sending request with mismatched amount..." -ForegroundColor Yellow

$form2 = @{
    amount = "3000"  # Different from receipt (5000)
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
    $response2 = Invoke-RestMethod -Uri "$baseUrl/raise-with-files" -Method Post -Form $form2 -ContentType "multipart/form-data"
    
    Write-Host "Response Received:" -ForegroundColor Green
    Write-Host "  Intent: $($response2.intent)" -ForegroundColor White
    Write-Host "  Decision: $($response2.decision)" -ForegroundColor White
    Write-Host "  Risk Score: $($response2.riskScore)" -ForegroundColor White
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Cyan
    Write-Host $response2.reviewReason -ForegroundColor White
    Write-Host ""
    
    if ($response2.decision -eq "REJECTED") {
        Write-Host "✓ TEST 2 PASSED: Correctly rejected mismatched data!" -ForegroundColor Green
    } else {
        Write-Host "✗ TEST 2 FAILED: Expected REJECTED, got $($response2.decision)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ TEST 2 FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "TEST 3: Invalid Receipt (Wrong UserID)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

Write-Host "Sending request with mismatched UserID..." -ForegroundColor Yellow

$form3 = @{
    amount = "5000"
    transactionLocation = "USA"
    userCurrentLocation = "INDIA"
    description = "Unauthorized transaction detected"
    userId = "ABC999"  # Different from receipt (ABC003)
    transactionType = "ONLINE"
    transactionDateTime = "2026-04-28T00:00:00"
    useAgenticMode = "false"
    transactionReceipt = Get-Item -Path $receiptFile
    userIdDocument = Get-Item -Path $idFile
}

try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/raise-with-files" -Method Post -Form $form3 -ContentType "multipart/form-data"
    
    Write-Host "Response Received:" -ForegroundColor Green
    Write-Host "  Intent: $($response3.intent)" -ForegroundColor White
    Write-Host "  Decision: $($response3.decision)" -ForegroundColor White
    Write-Host "  Risk Score: $($response3.riskScore)" -ForegroundColor White
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Cyan
    Write-Host $response3.reviewReason -ForegroundColor White
    Write-Host ""
    
    if ($response3.decision -eq "REJECTED") {
        Write-Host "✓ TEST 3 PASSED: Correctly rejected mismatched UserID!" -ForegroundColor Green
    } else {
        Write-Host "✗ TEST 3 FAILED: Expected REJECTED, got $($response3.decision)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ TEST 3 FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "All tests completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Expected Results:" -ForegroundColor Yellow
Write-Host "  TEST 1: AUTO_REFUND (matching data)" -ForegroundColor White
Write-Host "  TEST 2: REJECTED (amount mismatch)" -ForegroundColor White
Write-Host "  TEST 3: REJECTED (UserID mismatch)" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')

# Made with Bob
