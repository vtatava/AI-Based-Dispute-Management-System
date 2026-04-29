# Test Script for Location Fraud Detection Feature
# Tests the travel history validation and location fraud detection

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Location Fraud Detection Test Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api/disputes/submit"

# Test Case 1: Fraudulent Claim - User claims India but DB shows USA
Write-Host "Test 1: FRAUDULENT CLAIM - Location Mismatch (No Travel History)" -ForegroundColor Yellow
Write-Host "User: ABC002 (Priya Sharma)" -ForegroundColor Gray
Write-Host "Claimed Location: India" -ForegroundColor Gray
Write-Host "Database Location: USA" -ForegroundColor Gray
Write-Host "Expected: Location Fraud Detected, High Risk Score" -ForegroundColor Gray
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
    Write-Host "✓ Response received" -ForegroundColor Green
    Write-Host "Decision: $($response1.decision)" -ForegroundColor $(if($response1.decision -eq "HUMAN_REVIEW"){"Yellow"}else{"Red"})
    Write-Host "Risk Score: $($response1.riskScore)/100" -ForegroundColor $(if($response1.riskScore -gt 70){"Red"}elseif($response1.riskScore -gt 40){"Yellow"}else{"Green"})
    Write-Host "Intent: $($response1.intent)" -ForegroundColor Cyan
    
    if ($response1.explanation -match "LOCATION FRAUD") {
        Write-Host "✓ Location Fraud Detected!" -ForegroundColor Green
    } else {
        Write-Host "✗ Location Fraud NOT Detected (Expected)" -ForegroundColor Red
    }
    
    Write-Host "`nExplanation:" -ForegroundColor Cyan
    Write-Host $response1.explanation
} catch {
    Write-Host "✗ Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================`n" -ForegroundColor Cyan

# Test Case 2: Valid Claim - User claims India and DB shows India
Write-Host "Test 2: VALID CLAIM - Location Match" -ForegroundColor Yellow
Write-Host "User: ABC001 (Rajesh Kumar)" -ForegroundColor Gray
Write-Host "Claimed Location: India" -ForegroundColor Gray
Write-Host "Database Location: India" -ForegroundColor Gray
Write-Host "Expected: No Location Fraud, Normal Risk Processing" -ForegroundColor Gray
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
    Write-Host "✓ Response received" -ForegroundColor Green
    Write-Host "Decision: $($response2.decision)" -ForegroundColor $(if($response2.decision -eq "AUTO_REFUND"){"Green"}else{"Yellow"})
    Write-Host "Risk Score: $($response2.riskScore)/100" -ForegroundColor $(if($response2.riskScore -gt 70){"Red"}elseif($response2.riskScore -gt 40){"Yellow"}else{"Green"})
    Write-Host "Intent: $($response2.intent)" -ForegroundColor Cyan
    
    if ($response2.explanation -match "LOCATION FRAUD") {
        Write-Host "✗ Location Fraud Detected (Not Expected)" -ForegroundColor Red
    } else {
        Write-Host "✓ No Location Fraud - Valid Claim!" -ForegroundColor Green
    }
    
    Write-Host "`nExplanation:" -ForegroundColor Cyan
    Write-Host $response2.explanation
} catch {
    Write-Host "✗ Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================`n" -ForegroundColor Cyan

# Test Case 3: Suspicious Claim - User in India but transaction in USA
Write-Host "Test 3: SUSPICIOUS CLAIM - Transaction Location Mismatch" -ForegroundColor Yellow
Write-Host "User: ABC003 (Amit Patel)" -ForegroundColor Gray
Write-Host "User Location: India (matches DB)" -ForegroundColor Gray
Write-Host "Transaction Location: USA" -ForegroundColor Gray
Write-Host "Expected: Location Mismatch Warning, Medium Risk" -ForegroundColor Gray
Write-Host ""

$test3 = @{
    userId = "ABC003"
    userName = "Amit Patel"
    userDob = "1988-03-10"
    amount = 50000
    transactionLocation = "New York"
    userCurrentLocation = "India"
    description = "I never traveled to USA but my card was charged there"
    transactionType = "ONLINE"
    transactionDateTime = "2024-04-18T08:00:00"
    websiteUrl = "fake-amazon-deals.com"
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $test3 -ContentType "application/json"
    Write-Host "✓ Response received" -ForegroundColor Green
    Write-Host "Decision: $($response3.decision)" -ForegroundColor $(if($response3.decision -eq "AUTO_REFUND"){"Green"}else{"Yellow"})
    Write-Host "Risk Score: $($response3.riskScore)/100" -ForegroundColor $(if($response3.riskScore -gt 70){"Red"}elseif($response3.riskScore -gt 40){"Yellow"}else{"Green"})
    Write-Host "Intent: $($response3.intent)" -ForegroundColor Cyan
    
    if ($response3.explanation -match "Location mismatch") {
        Write-Host "✓ Location Mismatch Detected!" -ForegroundColor Green
    }
    
    Write-Host "`nExplanation:" -ForegroundColor Cyan
    Write-Host $response3.explanation
} catch {
    Write-Host "✗ Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Suite Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob
