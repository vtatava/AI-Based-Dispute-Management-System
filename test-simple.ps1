# Simple IBM ICA Test
Write-Host "Testing IBM ICA Integration..." -ForegroundColor Cyan

$body = @{
    amount = 5000
    description = "Fraudulent transaction. Card was stolen."
    userCurrentLocation = "INDIA"
    transactionLocation = "USA"
} | ConvertTo-Json

Write-Host "Sending request..." -ForegroundColor Gray
$startTime = Get-Date

try {
    $response = Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 60
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    
    Write-Host ""
    Write-Host "SUCCESS!" -ForegroundColor Green
    Write-Host "Response Time: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan
    Write-Host "Decision: $($response.decision)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Yellow
    Write-Host $response.reviewReason
    
} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

# Made with Bob
