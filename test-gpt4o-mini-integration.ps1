# Test GPT-4o-mini Integration with Backend
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing GPT-4o-mini LLM Integration" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test Case 1: High Risk Fraud (Location Mismatch)
Write-Host "Test 1: High Risk Fraud Detection" -ForegroundColor Yellow
Write-Host "Description: Fraudulent transaction with location mismatch" -ForegroundColor Gray
$body1 = @{
    amount = 5000
    description = "This is a fraudulent transaction. Card was stolen and used without my authorization."
    userCurrentLocation = "INDIA"
    transactionLocation = "USA"
} | ConvertTo-Json

try {
    Write-Host "Sending request to backend..." -ForegroundColor Gray
    $response1 = Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body1 `
        -TimeoutSec 180
    
    Write-Host "✓ Response received!" -ForegroundColor Green
    Write-Host "Decision: $($response1.decision)" -ForegroundColor Cyan
    Write-Host "Risk Score: $($response1.riskScore)" -ForegroundColor Cyan
    Write-Host "Refund Amount: $($response1.refundAmount)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Yellow
    Write-Host $response1.reviewReason -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test Case 2: Same Location - AI Scrutiny
Write-Host "Test 2: Same Location AI Scrutiny" -ForegroundColor Yellow
Write-Host "Description: Ambiguous case requiring AI analysis" -ForegroundColor Gray
$body2 = @{
    amount = 2000
    description = "I don't recognize this charge from yesterday. It appears on my statement but I didn't make it."
    userCurrentLocation = "INDIA"
    transactionLocation = "INDIA"
} | ConvertTo-Json

try {
    Write-Host "Sending request to backend..." -ForegroundColor Gray
    $response2 = Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body2 `
        -TimeoutSec 180
    
    Write-Host "✓ Response received!" -ForegroundColor Green
    Write-Host "Decision: $($response2.decision)" -ForegroundColor Cyan
    Write-Host "Risk Score: $($response2.riskScore)" -ForegroundColor Cyan
    Write-Host "Refund Amount: $($response2.refundAmount)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Yellow
    Write-Host $response2.reviewReason -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test Case 3: Contradictory Statement
Write-Host "Test 3: Contradictory Statement Detection" -ForegroundColor Yellow
Write-Host "Description: User claims not fraud but wants refund" -ForegroundColor Gray
$body3 = @{
    amount = 1500
    description = "This is not fraud but I want my money back because the seller did not dispatch the item."
    userCurrentLocation = "INDIA"
    transactionLocation = "INDIA"
} | ConvertTo-Json

try {
    Write-Host "Sending request to backend..." -ForegroundColor Gray
    $response3 = Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body3 `
        -TimeoutSec 180
    
    Write-Host "✓ Response received!" -ForegroundColor Green
    Write-Host "Decision: $($response3.decision)" -ForegroundColor Cyan
    Write-Host "Risk Score: $($response3.riskScore)" -ForegroundColor Cyan
    Write-Host "Refund Amount: $($response3.refundAmount)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Yellow
    Write-Host $response3.reviewReason -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob
