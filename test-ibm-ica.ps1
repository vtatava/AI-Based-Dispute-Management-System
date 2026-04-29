# Test IBM ICA Integration
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing IBM ICA Integration" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test Case: High Risk Fraud Detection
Write-Host "Test: High Risk Fraud Detection" -ForegroundColor Yellow
Write-Host "Description: Fraudulent transaction with location mismatch" -ForegroundColor Gray
Write-Host ""

$body = @{
    amount = 5000
    description = "This is a fraudulent transaction. Card was stolen and used without my authorization."
    userCurrentLocation = "INDIA"
    transactionLocation = "USA"
} | ConvertTo-Json

try {
    Write-Host "Sending request to backend..." -ForegroundColor Gray
    $startTime = Get-Date
    
    $response = Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 60
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ SUCCESS!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response Time: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan
    Write-Host "Decision: $($response.decision)" -ForegroundColor Cyan
    Write-Host "Risk Score: $($response.riskScore)" -ForegroundColor Cyan
    Write-Host "Refund Amount: `$$($response.refundAmount)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Yellow
    Write-Host $response.reviewReason -ForegroundColor White
    Write-Host ""
    
    # Check if IBM ICA was used
    if ($response.reviewReason -like "*IBM ICA Analysis*") {
        Write-Host "✓ IBM ICA API was used successfully!" -ForegroundColor Green
    } elseif ($response.reviewReason -like "*LLM Analysis*") {
        Write-Host "⚠ Ollama was used (IBM ICA may not be configured)" -ForegroundColor Yellow
    } else {
        Write-Host "⚠ Rule-based analysis only (LLM not used)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Performance Analysis:" -ForegroundColor Cyan
    if ($duration -lt 15) {
        Write-Host "⚡ Excellent! Response time under 15 seconds (IBM ICA)" -ForegroundColor Green
    } elseif ($duration -lt 60) {
        Write-Host "⚠ Moderate response time (may be using Ollama)" -ForegroundColor Yellow
    } else {
        Write-Host "⚠ Slow response time (check configuration)" -ForegroundColor Red
    }
    
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "✗ ERROR" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Check if backend is running on port 9090" -ForegroundColor White
    Write-Host "2. Verify IBM ICA API key is configured" -ForegroundColor White
    Write-Host "3. Check internet connection" -ForegroundColor White
    Write-Host "4. Review backend logs for errors" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob
