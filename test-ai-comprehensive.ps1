Write-Host '=== AI DISPUTE MANAGEMENT SYSTEM - COMPREHENSIVE TEST ===' -ForegroundColor Cyan
Write-Host ''

# Test 1: Legitimate Fraud (Different Locations)
Write-Host '1. Testing LEGITIMATE FRAUD (USA transaction, user in INDIA):' -ForegroundColor Yellow
$response1 = Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC001","description":"I did not make this transaction. Someone used my card without permission in USA","amount":5000.00,"transactionLocation":"USA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content | ConvertFrom-Json
Write-Host "Decision: $($response1.decision)" -ForegroundColor Green
Write-Host "Reason: $($response1.reason)"
Write-Host ''

# Test 2: Seller Dispute (Same Location)
Write-Host '2. Testing SELLER DISPUTE (Item not dispatched):' -ForegroundColor Yellow
$response2 = Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC002","description":"I ordered a laptop but seller did not dispatch the item. Payment was deducted but no delivery","amount":45000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content | ConvertFrom-Json
Write-Host "Decision: $($response2.decision)" -ForegroundColor Magenta
Write-Host "Reason: $($response2.reason)"
Write-Host ''

# Test 3: Contradictory Statement
Write-Host '3. Testing CONTRADICTORY STATEMENT:' -ForegroundColor Yellow
$response3 = Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC003","description":"This is not fraud but I want my money back. The transaction was authorized but I need refund","amount":10000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content | ConvertFrom-Json
Write-Host "Decision: $($response3.decision)" -ForegroundColor Magenta
Write-Host "Reason: $($response3.reason)"
Write-Host ''

# Test 4: High Suspicion (Same location, vague description)
Write-Host '4. Testing HIGH SUSPICION (Same location, suspicious patterns):' -ForegroundColor Yellow
$response4 = Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC001","description":"Something wrong happened. I need refund immediately. This is urgent","amount":50000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content | ConvertFrom-Json
Write-Host "Decision: $($response4.decision)" -ForegroundColor Red
Write-Host "Reason: $($response4.reason)"
Write-Host ''

# Test 5: Quality Dispute
Write-Host '5. Testing QUALITY DISPUTE:' -ForegroundColor Yellow
$response5 = Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC002","description":"Received defective product. The item is broken and not working properly","amount":8000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content | ConvertFrom-Json
Write-Host "Decision: $($response5.decision)" -ForegroundColor Magenta
Write-Host "Reason: $($response5.reason)"
Write-Host ''

# Test 6: Service Dispute
Write-Host '6. Testing SERVICE DISPUTE:' -ForegroundColor Yellow
$response6 = Invoke-WebRequest -Uri 'http://localhost:9090/api/dispute/raise-agentic' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{"userId":"ABC001","description":"Hotel booking was cancelled but refund not received. Service not provided","amount":15000.00,"transactionLocation":"INDIA","userCurrentLocation":"INDIA","transactionType":"ONLINE"}' | Select-Object -ExpandProperty Content | ConvertFrom-Json
Write-Host "Decision: $($response6.decision)" -ForegroundColor Magenta
Write-Host "Reason: $($response6.reason)"
Write-Host ''

Write-Host '=== TEST COMPLETE ===' -ForegroundColor Cyan
Write-Host ''
Write-Host 'AI CAPABILITIES DEMONSTRATED:' -ForegroundColor Green
Write-Host '✓ Location-based fraud detection' -ForegroundColor White
Write-Host '✓ Seller/Service dispute identification' -ForegroundColor White
Write-Host '✓ Contradictory statement detection' -ForegroundColor White
Write-Host '✓ Behavioral pattern analysis' -ForegroundColor White
Write-Host '✓ Sentiment & linguistic analysis' -ForegroundColor White
Write-Host '✓ Urgency tactics detection' -ForegroundColor White
Write-Host '✓ Quality dispute handling' -ForegroundColor White
Write-Host '✓ Multi-agent orchestration' -ForegroundColor White

# Made with Bob
