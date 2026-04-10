# Read the markdown file
$markdown = Get-Content -Path "STEP_BY_STEP_IMPLEMENTATION_GUIDE.md" -Raw -Encoding UTF8

# Simple markdown to HTML conversion
$html = $markdown

# Convert headers
$html = $html -replace '(?m)^# (.+)$', '<h1>$1</h1>'
$html = $html -replace '(?m)^## (.+)$', '<h2>$1</h2>'
$html = $html -replace '(?m)^### (.+)$', '<h3>$1</h3>'
$html = $html -replace '(?m)^#### (.+)$', '<h4>$1</h4>'

# Convert bold and italic
$html = $html -replace '\*\*(.+?)\*\*', '<strong>$1</strong>'
$html = $html -replace '\*(.+?)\*', '<em>$1</em>'

# Convert inline code
$html = $html -replace '`([^`]+)`', '<code>$1</code>'

# Convert code blocks
$html = $html -replace '(?s)```(\w+)?\r?\n(.+?)\r?\n```', '<pre><code>$2</code></pre>'

# Convert lists
$html = $html -replace '(?m)^- (.+)$', '<li>$1</li>'
$html = $html -replace '(?m)^(\d+)\. (.+)$', '<li>$2</li>'

# Wrap consecutive list items
$html = $html -replace '(<li>.*?</li>\r?\n)+', '<ul>$0</ul>'

# Convert line breaks
$html = $html -replace '\r?\n\r?\n', '<br><br>'
$html = $html -replace '\r?\n', '<br>'

# Create full HTML document
$fullHtml = @"
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Step by Step Implementation Guide - AI Dispute Management System</title>
    <style>
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            max-width: 1200px;
            margin: 40px auto;
            padding: 20px;
            line-height: 1.8;
            background: #f5f5f5;
        }
        .container {
            background: white;
            padding: 50px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #2c3e50;
            border-bottom: 4px solid #3498db;
            padding-bottom: 15px;
            margin-top: 30px;
            margin-bottom: 20px;
            font-size: 2.5em;
            font-weight: bold;
        }
        h2 {
            color: #34495e;
            margin-top: 40px;
            margin-bottom: 15px;
            border-bottom: 3px solid #95a5a6;
            padding-bottom: 10px;
            font-size: 2em;
            font-weight: bold;
        }
        h3 {
            color: #7f8c8d;
            margin-top: 30px;
            margin-bottom: 12px;
            font-size: 1.5em;
            font-weight: bold;
        }
        h4 {
            color: #95a5a6;
            margin-top: 20px;
            margin-bottom: 10px;
            font-size: 1.2em;
            font-weight: bold;
        }
        code {
            background: #f4f4f4;
            padding: 3px 8px;
            border-radius: 4px;
            font-family: 'Consolas', 'Monaco', monospace;
            font-size: 0.9em;
            color: #e74c3c;
        }
        pre {
            background: #2c3e50;
            color: #ecf0f1;
            padding: 20px;
            border-radius: 6px;
            overflow-x: auto;
            border-left: 4px solid #3498db;
            margin: 20px 0;
        }
        pre code {
            background: none;
            color: #ecf0f1;
            padding: 0;
        }
        ul, ol {
            margin: 15px 0;
            padding-left: 40px;
        }
        li {
            margin: 8px 0;
            line-height: 1.6;
        }
        strong {
            color: #2c3e50;
            font-weight: bold;
        }
        em {
            font-style: italic;
        }
        br {
            line-height: 1.8;
        }
        @media print {
            body {
                background: white;
            }
            .container {
                box-shadow: none;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        $html
    </div>
</body>
</html>
"@

# Write to file
$fullHtml | Out-File -FilePath "STEP_BY_STEP_IMPLEMENTATION_GUIDE.html" -Encoding UTF8

Write-Host "✅ HTML file created successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📄 To open in Microsoft Word:" -ForegroundColor Cyan
Write-Host "   1. Right-click 'STEP_BY_STEP_IMPLEMENTATION_GUIDE.html'" -ForegroundColor Yellow
Write-Host "   2. Select 'Open with' → 'Microsoft Word'" -ForegroundColor Yellow
Write-Host "   3. Word will open with all formatting preserved" -ForegroundColor Yellow
Write-Host "   4. In Word: File → Save As → Choose '.docx' format" -ForegroundColor Yellow
Write-Host ""
Write-Host "Alternative: Double-click the HTML file to open in browser, then:" -ForegroundColor Cyan
Write-Host "   - Press Ctrl+A to select all" -ForegroundColor Yellow
Write-Host "   - Press Ctrl+C to copy" -ForegroundColor Yellow
Write-Host "   - Open Word and press Ctrl+V to paste" -ForegroundColor Yellow
Write-Host ""
Write-Host "File location: $PWD\STEP_BY_STEP_IMPLEMENTATION_GUIDE.html" -ForegroundColor Green

# Made with Bob
