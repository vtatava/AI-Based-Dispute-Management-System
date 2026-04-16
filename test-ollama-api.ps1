# Test Ollama API
$body = @{
    model = "llama3.2"
    prompt = "Say hello in one word"
    stream = $false
} | ConvertTo-Json

Write-Host "Testing Ollama API..."
Write-Host "Request body: $body"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:11434/api/generate" -Method Post -Body $body -ContentType "application/json" -TimeoutSec 60
    Write-Host "`nSuccess! Response:"
    $response | ConvertTo-Json -Depth 5
} catch {
    Write-Host "`nError: $_"
    Write-Host "Error Details: $($_.Exception.Message)"
}

# Made with Bob
