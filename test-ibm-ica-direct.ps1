# Test IBM ICA API Direct Connection
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing IBM ICA API Direct Connection" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$apiKey = "538a014b-c4c4-4882-8b95-30f567bae92f"
$baseUrl = "https://servicesessentials.ibm.com/api/v1"

# Test 1: Check if base URL is reachable
Write-Host "Test 1: Checking base URL connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method Get -TimeoutSec 10 -ErrorAction Stop
    Write-Host "✓ Base URL is reachable (Status: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "✗ Base URL not reachable: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Try to call chat completions endpoint
Write-Host "Test 2: Testing chat completions endpoint..." -ForegroundColor Yellow

$headers = @{
    "Authorization" = "Bearer $apiKey"
    "Content-Type" = "application/json"
}

$body = @{
    model = "anthropic.claude-sonnet-4-5-20250514"
    messages = @(
        @{
            role = "user"
            content = "Say hello"
        }
    )
    max_tokens = 100
} | ConvertTo-Json -Depth 10

try {
    Write-Host "Sending request to: $baseUrl/chat/completions" -ForegroundColor Gray
    $response = Invoke-RestMethod -Uri "$baseUrl/chat/completions" `
        -Method Post `
        -Headers $headers `
        -Body $body `
        -TimeoutSec 30
    
    Write-Host "✓ IBM ICA API is working!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host ($response | ConvertTo-Json -Depth 5)
    
} catch {
    Write-Host "✗ IBM ICA API call failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
        Write-Host "Status Description: $($_.Exception.Response.StatusDescription)" -ForegroundColor Yellow
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body:" -ForegroundColor Yellow
            Write-Host $responseBody -ForegroundColor White
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob
