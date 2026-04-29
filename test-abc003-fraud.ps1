# Test ABC003 Location Fraud Detection
# User ABC003 claims to be in India, but DB shows USA

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ABC003 Location Fraud Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Scenario:" -ForegroundColor Yellow
Write-Host "User: ABC003 (Amit Patel)" -ForegroundColor Gray
Write-Host "Database Current Location: USA" -ForegroundColor Gray
Write-Host "User Claims Location: India" -ForegroundColor Gray
Write-Host "Expected: LOCATION FRAUD DETECTED" -ForegroundColor Red
Write-Host ""

$baseUrl = "http://localhost:9090/api/dispute/raise-agentic"

$testRequest = @{
    userId = "ABC003"
    userName = "Amit Patel"
    userDob = "1988-03-10"
    amount = 30000
    transactionLocation = "Mumbai"
    userCurrentLocation = "India"
    description = "Unauthorized transaction on my card. I am currently in India and did not make this purchase."
    transactionType = "ONLINE"
    transactionDateTime = "2024-04-18T10:00:00"
    websiteUrl = "suspicious-store.com"
} | ConvertTo-Json

Write-Host "Submitting dispute..." -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $testRequest -ContentType "application/json"
    
    Write-Host "=== RESPONSE RECEIVED ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "Decision: $($response.decision)" -ForegroundColor White
    Write-Host "Risk Score: $($response.riskScore)/100" -ForegroundColor $(if($response.riskScore -gt 70){"Red"}else{"Yellow"})
    Write-Host "Intent: $($response.intent)" -ForegroundColor White
    Write-Host ""
    
    Write-Host "=== AGENT FLOW DETAILS ===" -ForegroundColor Cyan
    Write-Host ""
    
    if ($response.agentFlow) {
        Write-Host "1. Intent Agent:" -ForegroundColor Yellow
        Write-Host "   $($response.agentFlow.intentResult)" -ForegroundColor Gray
        Write-Host ""
        
        Write-Host "2. Context Agent:" -ForegroundColor Yellow
        Write-Host "   $($response.agentFlow.contextResult)" -ForegroundColor Gray
        Write-Host ""
        
        Write-Host "3. Decision Agent:" -ForegroundColor Yellow
        Write-Host "   $($response.agentFlow.decisionResult)" -ForegroundColor Gray
        Write-Host ""
    }
    
    Write-Host "=== FRAUD DETECTION RESULT ===" -ForegroundColor Cyan
    Write-Host ""
    
    $contextResult = $response.agentFlow.contextResult
    if ($contextResult -match "LOCATION FRAUD") {
        Write-Host "SUCCESS: Location Fraud Detected!" -ForegroundColor Green
        Write-Host ""
        Write-Host "The system correctly identified that:" -ForegroundColor Green
        Write-Host "- User claims to be in India" -ForegroundColor Green
        Write-Host "- Database shows user is in USA" -ForegroundColor Green
        Write-Host "- This is a fraudulent claim!" -ForegroundColor Green
    } else {
        Write-Host "FAILED: Location Fraud NOT Detected" -ForegroundColor Red
        Write-Host "The system should have detected the location mismatch!" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "Risk Score Analysis:" -ForegroundColor Cyan
    if ($response.riskScore -gt 100) {
        Write-Host "- Very High Risk (>100): Likely fraudulent claim" -ForegroundColor Red
    } elseif ($response.riskScore -gt 70) {
        Write-Host "- High Risk (70-100): Suspicious activity detected" -ForegroundColor Yellow
    } else {
        Write-Host "- Medium/Low Risk (<70): May need review" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "ERROR: Test Failed" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Ensure backend is running on port 9090" -ForegroundColor Gray
    Write-Host "2. Check H2 Console to verify ABC003 current_location = 'USA'" -ForegroundColor Gray
    Write-Host "3. SQL to update: UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC003';" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To update ABC003 location in database:" -ForegroundColor Yellow
Write-Host "1. Open H2 Console: http://localhost:9090/h2-console" -ForegroundColor Gray
Write-Host "2. Execute: UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC003';" -ForegroundColor Gray
Write-Host "3. Run this test again (no restart needed)" -ForegroundColor Gray

# Made with Bob
