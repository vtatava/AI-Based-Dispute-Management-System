Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IBM ICA Test Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if test script is still running
$testProcess = Get-Process powershell -ErrorAction SilentlyContinue | Where-Object { 
    $_.MainWindowTitle -like "*test-ibm-ica-direct*" 
}

if ($testProcess) {
    Write-Host "Test script is still running" -ForegroundColor Yellow
    Write-Host "  Process ID: $($testProcess.Id)" -ForegroundColor Gray
    Write-Host "  Start Time: $($testProcess.StartTime)" -ForegroundColor Gray
    $runtime = (Get-Date) - $testProcess.StartTime
    Write-Host "  Running for: $([math]::Round($runtime.TotalMinutes, 1)) minutes" -ForegroundColor Gray
} else {
    Write-Host "Test script is not running" -ForegroundColor Red
}

Write-Host ""
Write-Host "Recommendation:" -ForegroundColor Cyan
Write-Host "The IBM ICA test has been running for a while without output." -ForegroundColor White
Write-Host "This suggests the API endpoint may not be accessible or responding." -ForegroundColor White
Write-Host ""
Write-Host "Options:" -ForegroundColor Yellow
Write-Host "1. Wait a bit longer (API might be slow)" -ForegroundColor White
Write-Host "2. Stop the test and use Ollama (already working)" -ForegroundColor White
Write-Host "3. Verify IBM ICA API details with IBM support" -ForegroundColor White
Write-Host ""
Write-Host "To use the working Ollama integration:" -ForegroundColor Green
Write-Host "  1. Edit backend/src/main/resources/application.properties" -ForegroundColor Gray
Write-Host "  2. Change: llm.provider=ollama" -ForegroundColor Gray
Write-Host "  3. Run: .\restart-backend.bat" -ForegroundColor Gray
Write-Host "  4. Test with: test-simple.ps1" -ForegroundColor Gray
Write-Host ""

# Made with Bob
