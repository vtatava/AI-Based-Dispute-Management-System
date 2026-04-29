Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IBM ICA API Connection Test (Updated)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$apiKey = "17968c16-328f-493c-b815-f3dc5ede5fda"
$baseUrl = "https://servicesessentials.ibm.com/curatorai/api/v1"

Write-Host "Testing IBM ICA API Connection..." -ForegroundColor Yellow
Write-Host "API Key: $($apiKey.Substring(0,8))..." -ForegroundColor Gray
Write-Host "Base URL: $baseUrl" -ForegroundColor Gray
Write-Host ""

# Test 1: Check base URL accessibility
Write-Host "Test 1: Checking base URL accessibility..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method Get -TimeoutSec 10 -ErrorAction Stop
    Write-Host "✓ Base URL is accessible" -ForegroundColor Green
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Base URL check failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Test chat completions endpoint with authentication
Write-Host "Test 2: Testing chat completions endpoint..." -ForegroundColor Cyan

$headers = @{
    "Authorization" = "Bearer $apiKey"
    "Content-Type" = "application/json"
}

$body = @{
    model = "anthropic.claude-sonnet-4-5-20250514"
    messages = @(
        @{
            role = "user"
            content = "Say hello in one word"
        }
    )
    max_tokens = 50
} | ConvertTo-Json -Depth 10

try {
    Write-Host "Sending request to: $baseUrl/chat/completions" -ForegroundColor Gray
    Write-Host "Request body:" -ForegroundColor Gray
    Write-Host $body -ForegroundColor DarkGray
    Write-Host ""
    
    $response = Invoke-RestMethod -Uri "$baseUrl/chat/completions" `
        -Method Post `
        -Headers $headers `
        -Body $body `
        -TimeoutSec 60
    
    Write-Host "✓ IBM ICA API is working!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host ($response | ConvertTo-Json -Depth 5) -ForegroundColor White
    Write-Host ""
    Write-Host "SUCCESS! IBM ICA is now connected and working!" -ForegroundColor Green
    
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
    
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Verify API key is active in IBM ICA portal" -ForegroundColor White
    Write-Host "2. Check if model name is correct" -ForegroundColor White
    Write-Host "3. Ensure network/firewall allows access to servicesessentials.ibm.com" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Made with Bob
