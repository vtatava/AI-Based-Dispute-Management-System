# Final Test for Location Fraud Detection
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Location Fraud Detection - Final Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9090/api/dispute/raise-agentic"

# Test Case 1: Fraudulent Claim - User claims India but DB shows USA
Write-Host "Test 1: FRAUDULENT CLAIM" -ForegroundColor Yellow
Write-Host "User: ABC002 (Priya Sharma)" -ForegroundColor Gray
Write-Host "Database Current Location: USA" -ForegroundColor Gray
Write-Host "User Claims Location: India" -ForegroundColor Gray
Write-Host "Expected: Location Fraud Detected with High Risk Score" -ForegroundColor Gray
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
    Write-Host "Response Received" -ForegroundColor Green
    Write-Host ""
    Write-Host "=== RESULTS ===" -ForegroundColor Cyan
    Write-Host "Decision: $($response1.decision)" -ForegroundColor White
    Write-Host "Risk Score: $($response1.riskScore)/100" -ForegroundColor White
    Write-Host "Intent: $($response1.intent)" -ForegroundColor White
    Write-Host ""
    
    Write-Host "=== AGENT FLOW ===" -ForegroundColor Cyan
    if ($response1.agentFlow) {
        Write-Host "`nIntent Agent:" -ForegroundColor Yellow
        Write-Host $response1.agentFlow.intentResult
        
        Write-Host "`nContext Agent:" -ForegroundColor Yellow
        Write-Host $response1.agentFlow.contextResult
        
        Write-Host "`nDecision Agent:" -ForegroundColor Yellow
        Write-Host $response1.agentFlow.decisionResult
    }
    
    Write-Host ""
    $contextResult = $response1.agentFlow.contextResult
    if ($contextResult -match "LOCATION FRAUD") {
        Write-Host "SUCCESS: Location Fraud Detected!" -ForegroundColor Green
    } else {
        Write-Host "FAILED: Location Fraud NOT Detected" -ForegroundColor Red
    }
}
catch {
    Write-Host "Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================`n" -ForegroundColor Cyan

# Test Case 2: Valid Claim - Location matches
Write-Host "Test 2: VALID CLAIM" -ForegroundColor Yellow
Write-Host "User: ABC001 (Rajesh Kumar)" -ForegroundColor Gray
Write-Host "Database Current Location: India" -ForegroundColor Gray
Write-Host "User Claims Location: India" -ForegroundColor Gray
Write-Host "Expected: No Location Fraud, Normal Processing" -ForegroundColor Gray
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
    Write-Host "Response Received" -ForegroundColor Green
    Write-Host ""
    Write-Host "=== RESULTS ===" -ForegroundColor Cyan
    Write-Host "Decision: $($response2.decision)" -ForegroundColor White
    Write-Host "Risk Score: $($response2.riskScore)/100" -ForegroundColor White
    Write-Host "Intent: $($response2.intent)" -ForegroundColor White
    Write-Host ""
    
    Write-Host "=== AGENT FLOW ===" -ForegroundColor Cyan
    if ($response2.agentFlow) {
        Write-Host "`nIntent Agent:" -ForegroundColor Yellow
        Write-Host $response2.agentFlow.intentResult
        
        Write-Host "`nContext Agent:" -ForegroundColor Yellow
        Write-Host $response2.agentFlow.contextResult
        
        Write-Host "`nDecision Agent:" -ForegroundColor Yellow
        Write-Host $response2.agentFlow.decisionResult
    }
    
    Write-Host ""
    $contextResult2 = $response2.agentFlow.contextResult
    if ($contextResult2 -match "LOCATION FRAUD") {
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
