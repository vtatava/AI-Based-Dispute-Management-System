# Add Ollama to System PATH
Write-Host "Adding Ollama to System PATH..." -ForegroundColor Cyan

$ollamaPath = "C:\Users\0022SM744\AppData\Local\Programs\Ollama"

if (Test-Path "$ollamaPath\ollama.exe") {
    Write-Host "Ollama found at: $ollamaPath" -ForegroundColor Green
    
    # Get current user PATH
    $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
    
    # Check if already in PATH
    if ($currentPath -like "*$ollamaPath*") {
        Write-Host "Ollama is already in your PATH" -ForegroundColor Yellow
    } else {
        Write-Host "Adding Ollama to User PATH..." -ForegroundColor Cyan
        $newPath = $currentPath + ";" + $ollamaPath
        [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
        Write-Host "Ollama added to PATH successfully!" -ForegroundColor Green
        Write-Host "IMPORTANT: Restart PowerShell for changes to take effect" -ForegroundColor Yellow
    }
    
    # Add to current session
    $env:Path += ";$ollamaPath"
    Write-Host "Ollama added to current session" -ForegroundColor Green
    
    # Test Ollama
    Write-Host "`nTesting Ollama..." -ForegroundColor Cyan
    & "$ollamaPath\ollama.exe" --version
    
    Write-Host "`nChecking installed models..." -ForegroundColor Cyan
    & "$ollamaPath\ollama.exe" list
    
    Write-Host "`nChecking if Ollama service is running..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:11434/api/tags" -Method Get -TimeoutSec 2 -ErrorAction Stop
        Write-Host "Ollama service is running!" -ForegroundColor Green
    } catch {
        Write-Host "Ollama service is NOT running" -ForegroundColor Yellow
        Write-Host "To start: ollama serve" -ForegroundColor White
    }
    
    Write-Host "`n=== Next Steps ===" -ForegroundColor Cyan
    Write-Host "1. Restart PowerShell or VS Code terminal"
    Write-Host "2. Test: ollama --version"
    Write-Host "3. Check models: ollama list"
    Write-Host "4. If no models: ollama pull mistral"
    Write-Host "5. Start service: ollama serve"
    Write-Host "6. Restart backend: .\start-backend.bat"
    
} else {
    Write-Host "Ollama not found at: $ollamaPath" -ForegroundColor Red
    Write-Host "Install from: https://ollama.com/download" -ForegroundColor Yellow
}

# Made with Bob
