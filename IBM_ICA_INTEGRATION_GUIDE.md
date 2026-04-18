# IBM ICA Integration Guide

## Overview

This guide explains how to integrate IBM ICA (IBM Consulting Advantage) API for **faster cloud-based AI analysis** instead of running models locally with Ollama.

## Why IBM ICA?

### Performance Comparison

| Feature | Ollama (Local) | IBM ICA (Cloud) |
|---------|---------------|-----------------|
| **Response Time** | 1-2 minutes | **5-10 seconds** ⚡ |
| **Setup** | Install models locally | API key only |
| **Resources** | High CPU/RAM usage | No local resources |
| **Scalability** | Limited by hardware | Unlimited |
| **Model Access** | Manual downloads | Instant access |
| **Cost** | Free (hardware cost) | Pay per use |

### Key Benefits

✅ **10-20x Faster**: Responses in seconds vs minutes
✅ **No Local Setup**: No need to download large models
✅ **Always Available**: Cloud-based, no local resource constraints
✅ **Latest Models**: Access to Claude Sonnet 4.5 and other advanced models
✅ **Scalable**: Handle multiple requests simultaneously

## Setup Instructions

### Step 1: Get Your IBM ICA API Key

1. Go to [IBM Consulting Advantage](https://servicesessentials.ibm.com)
2. Navigate to **Settings** → **API Keys**
3. Click **Generate API Key**
4. Copy your API key (it will look like: `sk-proj-...`)

### Step 2: Configure the Application

Edit `backend/src/main/resources/application.properties`:

```properties
# IBM ICA Configuration
ibm.ica.enabled=true
ibm.ica.api-key=YOUR_API_KEY_HERE
ibm.ica.base-url=https://servicesessentials.ibm.com/api/v1
ibm.ica.model=anthropic.claude-sonnet-4-5-20250514
ibm.ica.timeout=30

# Set IBM ICA as the primary LLM provider
llm.provider=ibm-ica
```

**Important**: Replace `YOUR_API_KEY_HERE` with your actual API key.

### Step 3: Set Environment Variable (Recommended)

For security, use an environment variable instead of hardcoding the API key:

**Windows (PowerShell)**:
```powershell
$env:IBM_ICA_API_KEY="your-api-key-here"
```

**Windows (Command Prompt)**:
```cmd
set IBM_ICA_API_KEY=your-api-key-here
```

**Linux/Mac**:
```bash
export IBM_ICA_API_KEY="your-api-key-here"
```

Then in `application.properties`:
```properties
ibm.ica.api-key=${IBM_ICA_API_KEY}
```

### Step 4: Rebuild and Restart

```bash
cd backend
mvnw.cmd clean package -DskipTests
java -jar target/dispute-ai-1.0.0.jar
```

Or use the restart script:
```bash
restart-backend.bat
```

## Configuration Options

### Available Models

```properties
# Claude Sonnet 4.5 (Recommended - Best balance)
ibm.ica.model=anthropic.claude-sonnet-4-5-20250514

# Claude Opus (Most capable, slower)
ibm.ica.model=anthropic.claude-opus-4-20250514

# Claude Haiku (Fastest, less capable)
ibm.ica.model=anthropic.claude-haiku-4-20250514
```

### Timeout Settings

```properties
# Default: 30 seconds (usually completes in 5-10 seconds)
ibm.ica.timeout=30

# For complex analysis, increase if needed
ibm.ica.timeout=60
```

### Provider Selection

```properties
# Use IBM ICA (Cloud - Fast)
llm.provider=ibm-ica

# Use Ollama (Local - Slower)
llm.provider=ollama
```

## Testing the Integration

### Test Script

Create `test-ibm-ica.ps1`:

```powershell
Write-Host "Testing IBM ICA Integration..." -ForegroundColor Cyan

$body = @{
    amount = 5000
    description = "Fraudulent transaction. Card was stolen and used without authorization."
    userCurrentLocation = "INDIA"
    transactionLocation = "USA"
} | ConvertTo-Json

try {
    $startTime = Get-Date
    $response = Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 60
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    
    Write-Host "✓ Success! Response time: $duration seconds" -ForegroundColor Green
    Write-Host "Decision: $($response.decision)" -ForegroundColor Cyan
    Write-Host "Risk Score: $($response.riskScore)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Review Reason:" -ForegroundColor Yellow
    Write-Host $response.reviewReason
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}
```

Run the test:
```powershell
powershell -ExecutionPolicy Bypass -File test-ibm-ica.ps1
```

### Expected Response

```json
{
  "intent": "FRAUD",
  "riskScore": 85,
  "decision": "AUTO_REFUND & BLOCK_CARD",
  "refundAmount": 5000.0,
  "reviewReason": "⚠️ HIGH RISK FRAUD DETECTED (Risk Score: 85)...
  
  🤖 IBM ICA Analysis (anthropic.claude-sonnet-4-5-20250514):
  • Fraud Assessment: YES
  • Confidence: HIGH
  • Key Reasons: [Detailed analysis in 5-10 seconds]
  • Red Flags: [Specific concerns]
  • Recommendations: [Next steps]"
}
```

## Fallback Mechanism

If IBM ICA is unavailable, the system automatically falls back to:
1. Ollama (if enabled)
2. Rule-based analysis

```properties
# Enable both for redundancy
ibm.ica.enabled=true
ollama.enabled=true
llm.provider=ibm-ica
```

## Cost Considerations

### IBM ICA Pricing

- **Pay per token**: Charged based on input/output tokens
- **Typical cost per dispute**: $0.01 - $0.05
- **Monthly estimate** (1000 disputes): $10 - $50

### Cost vs Value

| Metric | Local (Ollama) | Cloud (IBM ICA) |
|--------|---------------|-----------------|
| Hardware Cost | $1000+ (GPU) | $0 |
| Electricity | $50/month | $0 |
| API Cost | $0 | $10-50/month |
| Response Time | 1-2 min | 5-10 sec |
| **Total Cost** | **High upfront** | **Low monthly** |

## Troubleshooting

### Error: "Failed to call IBM ICA API"

**Cause**: Invalid API key or network issue

**Solution**:
1. Verify API key is correct
2. Check internet connection
3. Ensure `ibm.ica.base-url` is correct

### Error: "IBM ICA analysis unavailable"

**Cause**: Service timeout or rate limit

**Solution**:
1. Increase timeout: `ibm.ica.timeout=60`
2. Check API rate limits
3. System will fallback to rule-based analysis

### Slow Response Times

**Cause**: Network latency or complex prompts

**Solution**:
1. Check internet speed
2. Use faster model (Claude Haiku)
3. Reduce prompt complexity

## Security Best Practices

### 1. Never Commit API Keys

Add to `.gitignore`:
```
application.properties
*.env
```

### 2. Use Environment Variables

```properties
ibm.ica.api-key=${IBM_ICA_API_KEY}
```

### 3. Rotate Keys Regularly

- Generate new API key monthly
- Revoke old keys immediately

### 4. Monitor Usage

- Check IBM ICA dashboard for usage
- Set up billing alerts
- Monitor for unusual activity

## Migration from Ollama

### Quick Switch

1. **Keep Ollama as fallback**:
```properties
llm.provider=ibm-ica
ibm.ica.enabled=true
ollama.enabled=true
```

2. **Test both providers**:
```properties
# Test IBM ICA
llm.provider=ibm-ica

# Test Ollama
llm.provider=ollama
```

3. **Compare results** and choose based on:
   - Response time
   - Analysis quality
   - Cost
   - Reliability

## Monitoring & Logging

### Enable Detailed Logging

```properties
logging.level.com.app.service.IbmIcaService=DEBUG
```

### Check Logs

Look for:
- `IBM ICA analysis failed` - API errors
- Response times in logs
- Token usage statistics

## Support

### IBM ICA Support
- Documentation: https://servicesessentials.ibm.com/docs
- Support: Contact IBM Consulting Advantage team

### Application Support
- Check logs in backend console
- Review `IBM_ICA_INTEGRATION_GUIDE.md`
- Contact development team

---

## Summary

✅ **Setup**: Add API key to configuration
✅ **Performance**: 10-20x faster than local models
✅ **Cost**: Low monthly cost vs high hardware cost
✅ **Reliability**: Cloud-based with automatic fallback
✅ **Security**: Use environment variables for API keys

**Recommendation**: Use IBM ICA for production deployments where speed and scalability are critical.

---
**Last Updated**: 2026-04-16
**Made with Bob**