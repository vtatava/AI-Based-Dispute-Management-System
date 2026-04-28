# Simple test to check IBM ICA AI response
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing IBM ICA AI Response" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test data
$testData = @{
    userId = "ABC001"
    description = "I did not make this transaction. Someone used my card without permission in USA"
    amount = 5000.00
    transactionLocation = "USA"
    userCurrentLocation = "INDIA"
    transactionType = "ONLINE"
}

Write-Host "Test Request:" -ForegroundColor Yellow
Write-Host ($testData | ConvertTo-Json) -ForegroundColor White
Write-Host ""

try {
    Write-Host "Sending request to backend..." -ForegroundColor Yellow
    
    $response = Invoke-WebRequest -Uri "http://localhost:9090/api/dispute/raise-agentic" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body ($testData | ConvertTo-Json) `
        -TimeoutSec 60
    
    Write-Host "✓ Request successful!" -ForegroundColor Green
    Write-Host ""
    
    # Parse response
    $responseData = $response.Content | ConvertFrom-Json
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "AI RESPONSE ANALYSIS" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    # Display key fields
    Write-Host "Dispute ID: " -NoNewline -ForegroundColor Yellow
    Write-Host $responseData.disputeId -ForegroundColor White
    
    Write-Host "Status: " -NoNewline -ForegroundColor Yellow
    Write-Host $responseData.status -ForegroundColor White
    
    Write-Host "Decision: " -NoNewline -ForegroundColor Yellow
    Write-Host $responseData.decision -ForegroundColor White
    
    Write-Host "Confidence Score: " -NoNewline -ForegroundColor Yellow
    Write-Host $responseData.confidenceScore -ForegroundColor White
    
    Write-Host ""
    Write-Host "AI Analysis:" -ForegroundColor Yellow
    Write-Host $responseData.aiAnalysis -ForegroundColor White
    
    Write-Host ""
    Write-Host "Reasoning:" -ForegroundColor Yellow
    Write-Host $responseData.reasoning -ForegroundColor White
    
    # Check if IBM ICA was used
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "IBM ICA INTEGRATION CHECK" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    if ($responseData.aiAnalysis -match "IBM|ICA|watsonx") {
        Write-Host "✓ IBM ICA appears to be integrated" -ForegroundColor Green
    } else {
        Write-Host "⚠ IBM ICA integration not detected in response" -ForegroundColor Yellow
    }
    
    # Save full response
    $responseData | ConvertTo-Json -Depth 10 | Out-File -FilePath "test-ica-response.json" -Encoding UTF8
    Write-Host ""
    Write-Host "Full response saved to: test-ica-response.json" -ForegroundColor Cyan
}
catch {
    Write-Host "✗ Error occurred:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host ""
        Write-Host "Response Body:" -ForegroundColor Yellow
        Write-Host $responseBody -ForegroundColor White
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob
