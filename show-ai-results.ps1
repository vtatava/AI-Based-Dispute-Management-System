Write-Host '=== AI DISPUTE MANAGEMENT SYSTEM - TEST RESULTS ===' -ForegroundColor Cyan
Write-Host ''

# Test 1: Legitimate Fraud
Write-Host '1. LEGITIMATE FRAUD (Different Locations):' -ForegroundColor Yellow
Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC001","description":"I did not make this transaction. Someone used my card without permission in USA","amount":5000.00,"transactionLocation":"USA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content
Write-Host ''
Write-Host '---' -ForegroundColor Gray
Write-Host ''

# Test 2: Seller Dispute
Write-Host '2. SELLER DISPUTE (Same Location):' -ForegroundColor Yellow
Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC002","description":"Seller did not dispatch my order. Payment deducted but no delivery","amount":45000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content
Write-Host ''
Write-Host '---' -ForegroundColor Gray
Write-Host ''

# Test 3: Contradictory Statement
Write-Host '3. CONTRADICTORY STATEMENT:' -ForegroundColor Yellow
Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC003","description":"This is not fraud but I want my money back immediately","amount":10000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content
Write-Host ''
Write-Host '---' -ForegroundColor Gray
Write-Host ''

# Test 4: High Suspicion
Write-Host '4. HIGH SUSPICION (Vague + Urgent):' -ForegroundColor Yellow
Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC001","description":"Something wrong. Need refund urgent immediately","amount":50000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content
Write-Host ''

Write-Host '=== AI ANALYSIS COMPLETE ===' -ForegroundColor Cyan

# Made with Bob
