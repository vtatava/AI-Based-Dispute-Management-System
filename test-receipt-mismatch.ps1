# Test script to verify receipt validation catches mismatches
Write-Host "=== Testing Receipt Validation with Mismatched Data ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Form Data: Amount=250000, Date=23-04-2026" -ForegroundColor Yellow
Write-Host "Receipt Data: Amount=2500, Date=28-Apr-2026" -ForegroundColor Yellow
Write-Host ""

$url = "http://localhost:9090/api/dispute/raise-with-files"

# Using curl for multipart form data
$curlCommand = @"
curl -X POST "$url" \
  -F "userId=ABC003" \
  -F "amount=250000" \
  -F "transactionDateTime=23-04-2026 23:21" \
  -F "transactionLocation=USA" \
  -F "userCurrentLocation=INDIA" \
  -F "transactionType=ATM" \
  -F "description=not done by me" \
  -F "transactionReceipt=@test-receipt-atm.txt"
"@

Write-Host "Sending request..." -ForegroundColor Cyan
Write-Host ""

# Execute curl command
$response = Invoke-Expression $curlCommand | ConvertFrom-Json

Write-Host "=== RESPONSE ===" -ForegroundColor Green
Write-Host "Decision: $($response.decision)" -ForegroundColor $(if ($response.decision -eq "REJECTED") { "Red" } else { "Yellow" })
Write-Host "Risk Score: $($response.riskScore)"
Write-Host ""
Write-Host "Review Reason:" -ForegroundColor Yellow
Write-Host $response.reviewReason
Write-Host ""

if ($response.decision -eq "REJECTED") {
    Write-Host "SUCCESS: Validation correctly REJECTED the mismatched data" -ForegroundColor Green
} else {
    Write-Host "FAILED: Validation should have REJECTED but got: $($response.decision)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Check Terminal 1 for detailed validation logs" -ForegroundColor Cyan

# Made with Bob
