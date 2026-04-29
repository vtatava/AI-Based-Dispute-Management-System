# Simple Test for Location Fraud Detection
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Location Fraud Detection Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9090/api/dispute/raise-agentic"

# Test Case 1: Fraudulent Claim - User claims India but DB shows USA
Write-Host "Test 1: FRAUDULENT CLAIM" -ForegroundColor Yellow
Write-Host "User: ABC002 (Priya Sharma) - DB shows currently in USA" -ForegroundColor Gray
Write-Host "User claims to be in: India" -ForegroundColor Gray
Write-Host ""

$test1 = @{
    userId = "ABC002"
    userName = "Priya Sharma"
    userDob = "1990-08-22"
    amount = 25000
    transactionLocation = "Mumbai"
    userCurrentLocation = "India"
    description = "I was shopping in Mumbai when my card was charged without my authorization"
    transactionType = "MERCHANT"
    transactionDateTime = "2024-04-18T10:00:00"
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $test1 -ContentType "application/json"
    Write-Host "Response received:" -ForegroundColor Green
    Write-Host "Decision: $($response1.decision)"
    Write-Host "Risk Score: $($response1.riskScore)/100"
    Write-Host "Intent: $($response1.intent)"
    Write-Host ""
    Write-Host "Explanation:" -ForegroundColor Cyan
    Write-Host $response1.explanation
    Write-Host ""
    
    if ($response1.explanation -match "LOCATION FRAUD") {
        Write-Host "SUCCESS: Location Fraud Detected!" -ForegroundColor Green
    } else {
        Write-Host "FAILED: Location Fraud NOT Detected" -ForegroundColor Red
    }
}
catch {
    Write-Host "Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================`n" -ForegroundColor Cyan

# Test Case 2: Valid Claim
Write-Host "Test 2: VALID CLAIM" -ForegroundColor Yellow
Write-Host "User: ABC001 (Rajesh Kumar) - DB shows currently in India" -ForegroundColor Gray
Write-Host "User claims to be in: India" -ForegroundColor Gray
Write-Host ""

$test2 = @{
    userId = "ABC001"
    userName = "Rajesh Kumar"
    userDob = "1985-05-15"
    amount = 15000
    transactionLocation = "Delhi"
    userCurrentLocation = "India"
    description = "Unauthorized transaction on my card in Delhi"
    transactionType = "ATM"
    transactionDateTime = "2024-04-18T09:00:00"
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $test2 -ContentType "application/json"
    Write-Host "Response received:" -ForegroundColor Green
    Write-Host "Decision: $($response2.decision)"
    Write-Host "Risk Score: $($response2.riskScore)/100"
    Write-Host "Intent: $($response2.intent)"
    Write-Host ""
    Write-Host "Explanation:" -ForegroundColor Cyan
    Write-Host $response2.explanation
    Write-Host ""
    
    if ($response2.explanation -match "LOCATION FRAUD") {
        Write-Host "FAILED: Location Fraud Detected (should not be)" -ForegroundColor Red
    } else {
        Write-Host "SUCCESS: No Location Fraud - Valid Claim!" -ForegroundColor Green
    }
}
catch {
    Write-Host "Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Suite Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob
