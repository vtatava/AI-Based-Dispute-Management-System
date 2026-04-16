# 🤖 Ollama LLM Integration - AI-Powered Dispute Analysis

## Overview

This project now includes **Ollama integration** with **Llama3.2** or **Mistral** models to provide advanced AI-powered dispute analysis. The LLM analyzes transaction disputes and provides intelligent recommendations for AUTO_REFUND or HUMAN_REVIEW decisions with detailed reasoning.

---

## 🎯 What Does Ollama Do?

### Before Ollama (Rule-Based AI Only):
- ✅ Location mismatch detection
- ✅ Pattern recognition
- ✅ Risk score calculation
- ❌ Limited contextual understanding
- ❌ No natural language reasoning

### After Ollama Integration:
- ✅ **All previous features PLUS:**
- ✅ **Deep semantic understanding** of dispute descriptions
- ✅ **Contextual fraud assessment** with confidence levels
- ✅ **Natural language explanations** for decisions
- ✅ **Intelligent recommendations** based on comprehensive analysis
- ✅ **Red flag identification** with specific reasoning
- ✅ **Actionable suggestions** for next steps

---

## 🚀 Quick Start

### 1. Install Ollama

```powershell
# Download from https://ollama.com/download
# Or use winget (if available)
winget install Ollama.Ollama
```

### 2. Pull a Model

```powershell
# Option 1: Llama 3.2 (Recommended - 3B parameters)
ollama pull llama3.2

# Option 2: Mistral (Alternative - 7B parameters)
ollama pull mistral
```

### 3. Test Ollama

```powershell
# Run the test script
.\test-ollama-integration.bat
```

### 4. Start Your Application

```powershell
# Start backend
.\start-backend.bat

# Start frontend (in another terminal)
.\start-frontend.bat
```

---

## 📊 How It Works

### Architecture Flow

```
User Submits Dispute
        ↓
[Frontend] → [Backend API]
                  ↓
        [Rule-Based AI Analysis]
        - Location check
        - Pattern detection
        - Risk scoring
                  ↓
        [Ollama LLM Analysis] ← NEW!
        - Semantic understanding
        - Fraud assessment
        - Confidence evaluation
        - Detailed reasoning
                  ↓
        [Decision Engine]
        - Combines both analyses
        - Makes final decision
        - Generates explanation
                  ↓
        [Response to User]
        - Decision (AUTO_REFUND/HUMAN_REVIEW)
        - Risk score
        - LLM insights
        - Recommendations
```

### Example Analysis

**Input:**
```json
{
  "amount": 25000,
  "transactionLocation": "USA",
  "userCurrentLocation": "INDIA",
  "description": "This transaction was not done by me. My card was stolen."
}
```

**Output (with Ollama):**
```
Risk Score: 90/100
Decision: AUTO_REFUND & BLOCK_CARD

⚠️ HIGH RISK FRAUD DETECTED (Risk Score: 90)
CRITICAL: Transaction location (USA) does NOT match user's current location (INDIA).
Clear fraud indicators detected. Full refund approved automatically.
Card will be BLOCKED immediately for security.

🤖 LLM Analysis (llama3.2):
• Fraud Assessment: YES
• Confidence: HIGH
• Key Reasons:
  1. Geographical impossibility - user cannot be in two countries simultaneously
  2. Explicit claim of card theft indicates unauthorized access
  3. High transaction amount ($25,000) increases fraud severity
  4. User denies authorization completely
• Red Flags:
  - Critical location mismatch (USA vs INDIA)
  - High-value transaction
  - Direct fraud claim
  - No legitimate explanation possible
• Recommendations:
  - Immediate card blocking (already initiated)
  - Full refund processing
  - Investigate merchant for potential data breach
  - Review recent transaction history for other unauthorized charges
```

---

## ⚙️ Configuration

### Application Properties

Located in `backend/src/main/resources/application.properties`:

```properties
# Ollama Configuration
ollama.base-url=http://localhost:11434
ollama.model=llama3.2
ollama.timeout=60
ollama.enabled=true
```

### Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `ollama.base-url` | `http://localhost:11434` | Ollama API endpoint |
| `ollama.model` | `llama3.2` | Model to use (llama3.2, mistral, etc.) |
| `ollama.timeout` | `60` | Timeout in seconds for LLM response |
| `ollama.enabled` | `true` | Enable/disable Ollama integration |

### Switching Models

**To use Mistral:**
```properties
ollama.model=mistral
```

**To use Llama 3.2 7B (more accurate, slower):**
```properties
ollama.model=llama3.2:7b
```

**To disable Ollama (fallback to rule-based only):**
```properties
ollama.enabled=false
```

---

## 🧪 Testing

### Test Script

Run the automated test:
```powershell
.\test-ollama-integration.bat
```

This will:
1. ✅ Check if Ollama is installed
2. ✅ Verify Ollama service is running
3. ✅ Check if model is downloaded
4. ✅ Test Ollama API connectivity
5. ✅ Send a sample fraud analysis request

### Manual Testing

**Test 1: High-Risk Fraud**
```json
POST http://localhost:9090/api/dispute/raise
{
  "amount": 25000,
  "transactionLocation": "USA",
  "userCurrentLocation": "INDIA",
  "description": "This transaction was not done by me. My card was stolen."
}
```

**Expected:** AUTO_REFUND with detailed LLM analysis

**Test 2: Ambiguous Case**
```json
POST http://localhost:9090/api/dispute/raise
{
  "amount": 5000,
  "transactionLocation": "INDIA",
  "userCurrentLocation": "INDIA",
  "description": "I don't recognize this charge. It might be unauthorized."
}
```

**Expected:** HUMAN_REVIEW with LLM recommendations

---

## 📈 Performance

### Model Comparison

| Model | Size | Speed | Accuracy | RAM | Best For |
|-------|------|-------|----------|-----|----------|
| **llama3.2** (3B) | 2GB | ⚡ Fast | ⭐⭐⭐⭐ Good | 4GB | General use, fast responses |
| **llama3.2:7b** | 4GB | 🐢 Slower | ⭐⭐⭐⭐⭐ Excellent | 8GB | High accuracy, detailed analysis |
| **mistral** | 4GB | ⚡ Fast | ⭐⭐⭐⭐ Very Good | 8GB | Fast inference, good balance |

### Response Times

- **Rule-Based AI Only**: < 1 second
- **With Ollama (llama3.2)**: 5-15 seconds
- **With Ollama (llama3.2:7b)**: 10-30 seconds
- **With Ollama (mistral)**: 5-20 seconds

*Times vary based on hardware and prompt complexity*

---

## 🔧 Troubleshooting

### Common Issues

#### 1. "Ollama not found"
```powershell
# Solution: Install Ollama
# Download from: https://ollama.com/download
```

#### 2. "Connection refused to localhost:11434"
```powershell
# Solution: Start Ollama service
ollama serve
```

#### 3. "Model not found"
```powershell
# Solution: Pull the model
ollama pull llama3.2
```

#### 4. "Timeout waiting for response"
```properties
# Solution: Increase timeout in application.properties
ollama.timeout=120
```

#### 5. "Out of memory"
```powershell
# Solution: Use smaller model
ollama pull llama3.2  # Instead of llama3.2:7b
```

### Fallback Behavior

If Ollama fails or is unavailable:
- ✅ System automatically falls back to rule-based AI
- ✅ Dispute processing continues normally
- ✅ Decision is made using traditional algorithms
- ⚠️ LLM insights will not be available

---

## 🎓 Understanding LLM Output

### Fraud Assessment
- **YES**: Clear fraud indicators detected
- **NO**: No fraud indicators found
- **UNCERTAIN**: Ambiguous case requiring investigation

### Confidence Level
- **HIGH**: LLM is very confident in its assessment
- **MEDIUM**: Some uncertainty exists
- **LOW**: Insufficient information for confident assessment

### Recommended Decision
- **AUTO_REFUND**: Clear fraud, immediate refund recommended
- **HUMAN_REVIEW**: Requires human judgment and investigation

---

## 🔐 Security & Privacy

### Data Privacy
- ✅ **All processing is LOCAL** - Ollama runs on your machine
- ✅ **No data sent to external servers** - Complete privacy
- ✅ **No internet required** (after model download)
- ✅ **Full control** over your data

### Security Best Practices
1. Keep Ollama updated: `ollama update`
2. Don't expose port 11434 externally
3. Use firewall rules to restrict access
4. Regularly update models for improvements

---

## 📚 Additional Resources

- **Ollama Documentation**: https://github.com/ollama/ollama
- **Llama 3.2 Model Card**: https://ollama.com/library/llama3.2
- **Mistral Model Card**: https://ollama.com/library/mistral
- **Full Setup Guide**: See `OLLAMA_SETUP_GUIDE.md`

---

## 🎯 Benefits of Ollama Integration

### For Fraud Detection
- ✅ **Better accuracy** through semantic understanding
- ✅ **Contextual analysis** of dispute descriptions
- ✅ **Nuanced decision-making** beyond simple rules
- ✅ **Explainable AI** with detailed reasoning

### For Operations
- ✅ **Reduced false positives** through intelligent analysis
- ✅ **Better customer experience** with clear explanations
- ✅ **Faster resolution** for clear-cut cases
- ✅ **Improved audit trail** with detailed reasoning

### For Compliance
- ✅ **Transparent decisions** with full explanation
- ✅ **Audit-ready** with complete reasoning chain
- ✅ **Consistent analysis** across all cases
- ✅ **Regulatory compliance** through explainability

---

## 🚀 Future Enhancements

Planned improvements:
- [ ] Multi-model ensemble (combine multiple LLMs)
- [ ] Fine-tuning on historical dispute data
- [ ] Real-time model switching based on case complexity
- [ ] Automated model performance monitoring
- [ ] Integration with external fraud databases

---

## 📞 Support

For issues or questions:
- Check `OLLAMA_SETUP_GUIDE.md` for detailed setup
- Run `test-ollama-integration.bat` for diagnostics
- Review backend logs for error messages
- Disable Ollama temporarily: `ollama.enabled=false`

---

**Made with ❤️ using Ollama + Llama3.2/Mistral**

*Bringing the power of Large Language Models to fraud detection and dispute management*
