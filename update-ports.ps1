# Update all documentation files from port 8080 to 9090
$files = @(
    'HOW_TO_START.md',
    'SETUP_INSTRUCTIONS.md',
    'FINAL_STATUS.md',
    'PROJECT_SUMMARY.md',
    'NODEJS_INSTALLATION.md',
    'OLLAMA_INTEGRATION_README.md',
    'AI_ML_INTEGRATION_PLAN.md',
    'STEP_BY_STEP_IMPLEMENTATION_GUIDE.md'
)

foreach($file in $files) {
    if(Test-Path $file) {
        Write-Host "Updating $file..." -ForegroundColor Cyan
        (Get-Content $file) -replace '8080', '9090' | Set-Content $file
        Write-Host "  Updated $file" -ForegroundColor Green
    } else {
        Write-Host "  File not found: $file" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "All documentation files updated successfully!" -ForegroundColor Green

# Made with Bob
