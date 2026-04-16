# Test Backend Ollama Integration
Write-Host "Testing Backend Ollama Integration..." -ForegroundColor Cyan

$testRequest = @{
    description = "This is a fraudulent transaction. Card was stolen and used without my authorization."
    amount = 5000
    transactionLocation = "USA"
    userCurrentLocation = "INDIA"
} | ConvertTo-Json

Write-Host "`nSending test dispute request to backend..."
Write-Host "Request: $testRequest" -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" -Method Post -Body $testRequest -ContentType "application/json" -TimeoutSec 120
    Write-Host "`nBackend Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10
    
    # Check if LLM analysis is present
    if ($response.reviewReason -like "*LLM Analysis*") {
        Write-Host "`n[SUCCESS] LLM Analysis is working!" -ForegroundColor Green
    } elseif ($response.reviewReason -like "*LLM analysis unavailable*") {
        Write-Host "`n[FAILED] LLM Analysis failed - using fallback" -ForegroundColor Red
    } else {
        Write-Host "`n[UNKNOWN] LLM status unclear" -ForegroundColor Yellow
    }
} catch {
    Write-Host "`nError calling backend:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Made with Bob
