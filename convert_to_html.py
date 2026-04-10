import markdown

# Read the markdown file
with open('STEP_BY_STEP_IMPLEMENTATION_GUIDE.md', 'r', encoding='utf-8') as f:
    content = f.read()

# Convert to HTML with extensions
html_content = markdown.markdown(content, extensions=['fenced_code', 'tables', 'nl2br'])

# Create styled HTML document
styled_html = f'''<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Step by Step Implementation Guide - AI Dispute Management System</title>
    <style>
        body {{
            font-family: 'Segoe UI', Arial, sans-serif;
            max-width: 1200px;
            margin: 40px auto;
            padding: 20px;
            line-height: 1.6;
            background: #f5f5f5;
        }}
        .container {{
            background: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }}
        h1 {{
            color: #2c3e50;
            border-bottom: 4px solid #3498db;
            padding-bottom: 15px;
            margin-top: 0;
            font-size: 2.5em;
        }}
        h2 {{
            color: #34495e;
            margin-top: 40px;
            border-bottom: 3px solid #95a5a6;
            padding-bottom: 10px;
            font-size: 2em;
        }}
        h3 {{
            color: #7f8c8d;
            margin-top: 30px;
            font-size: 1.5em;
        }}
        h4 {{
            color: #95a5a6;
            margin-top: 20px;
            font-size: 1.2em;
        }}
        code {{
            background: #f4f4f4;
            padding: 3px 8px;
            border-radius: 4px;
            font-family: 'Consolas', 'Monaco', monospace;
            font-size: 0.9em;
            color: #e74c3c;
        }}
        pre {{
            background: #2c3e50;
            color: #ecf0f1;
            padding: 20px;
            border-radius: 6px;
            overflow-x: auto;
            border-left: 4px solid #3498db;
        }}
        pre code {{
            background: none;
            color: #ecf0f1;
            padding: 0;
        }}
        table {{
            border-collapse: collapse;
            width: 100%;
            margin: 20px 0;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }}
        th, td {{
            border: 1px solid #ddd;
            padding: 12px 15px;
            text-align: left;
        }}
        th {{
            background: #3498db;
            color: white;
            font-weight: bold;
        }}
        tr:nth-child(even) {{
            background: #f9f9f9;
        }}
        tr:hover {{
            background: #f1f1f1;
        }}
        ul, ol {{
            margin: 15px 0;
            padding-left: 40px;
        }}
        li {{
            margin: 8px 0;
        }}
        blockquote {{
            border-left: 4px solid #3498db;
            padding-left: 20px;
            margin: 20px 0;
            color: #7f8c8d;
            font-style: italic;
        }}
        strong {{
            color: #2c3e50;
        }}
        a {{
            color: #3498db;
            text-decoration: none;
        }}
        a:hover {{
            text-decoration: underline;
        }}
        .note {{
            background: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 15px;
            margin: 20px 0;
            border-radius: 4px;
        }}
        @media print {{
            body {{
                background: white;
            }}
            .container {{
                box-shadow: none;
            }}
        }}
    </style>
</head>
<body>
    <div class="container">
        {html_content}
    </div>
</body>
</html>'''

# Write to HTML file
with open('STEP_BY_STEP_IMPLEMENTATION_GUIDE.html', 'w', encoding='utf-8') as f:
    f.write(styled_html)

print("✅ HTML file created successfully: STEP_BY_STEP_IMPLEMENTATION_GUIDE.html")
print("📄 You can now open this file in Microsoft Word:")
print("   1. Right-click the HTML file")
print("   2. Select 'Open with' → 'Microsoft Word'")
print("   3. Word will preserve all formatting and headings")
print("   4. Save as .docx from Word if needed")

# Made with Bob
