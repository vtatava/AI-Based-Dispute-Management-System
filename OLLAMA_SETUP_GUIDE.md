# Ollama Integration Setup Guide

## 🚀 Complete Guide to Integrate Ollama with Llama3.2/Mistral

This guide will help you set up and use Ollama with your AI-Based Dispute Management System.

---

## 📋 Prerequisites

- Windows 11 (or Windows 10)
- At least 8GB RAM (16GB recommended for Llama3.2)
- Internet connection for initial model download
- Admin privileges for installation

---

## 🔧 Step 1: Install Ollama

### Option A: Download and Install (Recommended)

1. **Download Ollama for Windows**
   - Visit: https://ollama.com/download
   - Click "Download for Windows"
   - Run the installer (`OllamaSetup.exe`)

2. **Install Ollama**
   - Follow the installation wizard
   - Default installation path: `C:\Users\<YourUsername>\AppData\Local\Programs\Ollama`
   - Ollama will start automatically after installation

3. **Verify Installation**
   ```powershell
   ollama --version
   ```
   You should see the version number (e.g., `ollama version 0.1.x`)

---

## 📦 Step 2: Pull AI Models

### Option 1: Llama 3.2 (Recommended)

**Llama 3.2** is Meta's latest model with excellent reasoning capabilities.

```powershell
# Pull Llama 3.2 (3B parameters - faster, less memory)
ollama pull llama3.2

# OR pull Llama 3.2 (7B parameters - more accurate, needs more memory)
ollama pull llama3.2:7b
```

**Model Sizes:**
- `llama3.2` (3B): ~2GB download, ~4GB RAM required
- `llama3.2:7b` (7B): ~4GB download, ~8GB RAM required

### Option 2: Mistral (Alternative)

**Mistral** is known for fast inference and good performance.

```powershell
# Pull Mistral 7B
ollama pull mistral

# OR pull Mistral 7B Instruct (optimized for instructions)
ollama pull mistral:instruct
```

**Model Sizes:**
- `mistral`: ~4GB download, ~8GB RAM required
- `mistral:instruct`: ~4GB download, ~8GB RAM required

### Verify Model Installation

```powershell
# List all installed models
ollama list
```

You should see your downloaded model(s) listed.

---

## ⚙️ Step 3: Configure Your Application

### Update `application.properties`

The configuration is already set in `backend/src/main/resources/application.properties`:

```properties
# Ollama Configuration
ollama.base-url=http://localhost:11434
ollama.model=llama3.2
ollama.timeout=60
ollama.enabled=true
```

### Change Model (if needed)

To use **Mistral** instead of Llama3.2:

```properties
ollama.model=mistral
```

To use **Llama3.2 7B**:

```properties
ollama.model=llama3.2:7b
```

### Disable Ollama (fallback to rule-based AI)

```properties
ollama.enabled=false
```

---

## 🧪 Step 4: Test Ollama

### Test 1: Verify Ollama is Running

```powershell
# Check if Ollama service is running
curl http://localhost:11434/api/tags
```

You should see a JSON response with your installed models.

### Test 2: Test Model Directly

```powershell
# Test Llama3.2
ollama run llama3.2 "Analyze this transaction: A user claims fraud on a $5000 purchase in USA while they are in India. Is this suspicious?"
```

The model should provide a detailed analysis.

### Test 3: Test via API

Create a test file `test-ollama.ps1`:

```powershell
$body = @{
    model = "llama3.2"
    prompt = "Is a transaction in USA suspicious if the user is currently in India? Answer briefly."
    stream = $false
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:11434/api/generate" -Method Post -Body $body -ContentType "application/json"
```

Run it:
```powershell
.\test-ollama.ps1
```

---

## 🏃 Step 5: Run Your Application

### 1. Start Ollama (if not running)

Ollama usually starts automatically. If not:

```powershell
# Start Ollama service
ollama serve
```

Keep this terminal open.

### 2. Build Backend (if needed)

```powershell
cd backend
.\mvnw.cmd clean install
```

### 3. Start Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Or use the batch file:
```powershell
.\start-backend.bat
```

### 4. Start Frontend

```powershell
cd frontend
npm start
```

Or use the batch file:
```powershell
.\start-frontend.bat
```

---

## 🎯 Step 6: Test the Integration

### Test Case 1: High-Risk Fraud

**Input:**
- Amount: $25,000
- Transaction Location: USA
- User Current Location: INDIA
- Description: "This transaction was not done by me. My card was stolen."

**Expected Result:**
- Risk Score: 80-100
- Decision: AUTO_REFUND & BLOCK_CARD
- LLM Analysis: Detailed fraud assessment with reasons

### Test Case 2: Medium-Risk Case

**Input:**
- Amount: $5,000
- Transaction Location: INDIA
- User Current Location: INDIA
- Description: "I don't recognize this charge. It might be unauthorized."

**Expected Result:**
- Risk Score: 40-79
- Decision: HUMAN_REVIEW
- LLM Analysis: Recommendations for investigation

### Test Case 3: Low-Risk Case

**Input:**
- Amount: $500
- Transaction Location: INDIA
- User Current Location: INDIA
- Description: "I want to verify this transaction. It seems legitimate but I want to confirm."

**Expected Result:**
- Risk Score: 0-39
- Decision: HUMAN_REVIEW
- LLM Analysis: Low-risk assessment with suggestions

---

## 🔍 How It Works

### 1. **User Submits Dispute**
   - Frontend sends dispute details to backend

### 2. **Rule-Based AI Analysis**
   - System performs initial analysis (location check, pattern detection, etc.)
   - Calculates risk score (0-100)

### 3. **Ollama LLM Analysis** (NEW!)
   - System sends comprehensive prompt to Ollama
   - Prompt includes:
     - Dispute description
     - Transaction amount
     - Locations (transaction vs. user)
     - Current risk score
     - Current system decision
   
### 4. **LLM Provides Insights**
   - **Fraud Assessment**: YES/NO/UNCERTAIN
   - **Confidence Level**: HIGH/MEDIUM/LOW
   - **Recommended Decision**: AUTO_REFUND or HUMAN_REVIEW
   - **Key Reasons**: Specific factors supporting the decision
   - **Red Flags**: Suspicious patterns identified
   - **Recommendations**: Suggested next steps

### 5. **Final Decision**
   - If LLM confidence is HIGH and agrees with rule-based analysis → Use LLM recommendation
   - If LLM confidence is LOW or unavailable → Use rule-based decision
   - System combines both analyses for comprehensive reasoning

### 6. **Response to User**
   - Decision (AUTO_REFUND, HUMAN_REVIEW, etc.)
   - Risk score
   - Detailed reasoning including LLM insights
   - Contact information if needed

---

## 🛠️ Troubleshooting

### Issue 1: "Ollama not found"

**Solution:**
```powershell
# Add Ollama to PATH (if not done automatically)
$env:Path += ";C:\Users\$env:USERNAME\AppData\Local\Programs\Ollama"

# Verify
ollama --version
```

### Issue 2: "Connection refused to localhost:11434"

**Solution:**
```powershell
# Start Ollama service
ollama serve
```

### Issue 3: "Model not found"

**Solution:**
```powershell
# Pull the model again
ollama pull llama3.2

# Verify it's installed
ollama list
```

### Issue 4: "Timeout waiting for Ollama response"

**Solution:**
- Increase timeout in `application.properties`:
  ```properties
  ollama.timeout=120
  ```
- Or use a smaller/faster model:
  ```properties
  ollama.model=llama3.2
  ```

### Issue 5: "Out of memory"

**Solution:**
- Use a smaller model:
  ```powershell
  ollama pull llama3.2  # 3B instead of 7B
  ```
- Close other applications
- Increase system RAM if possible

### Issue 6: Backend fails to start

**Solution:**
```powershell
# Clean and rebuild
cd backend
.\mvnw.cmd clean install -DskipTests

# Start again
.\mvnw.cmd spring-boot:run
```

---

## 📊 Performance Tips

### 1. **Choose the Right Model**

| Model | Size | Speed | Accuracy | RAM Required |
|-------|------|-------|----------|--------------|
| llama3.2 (3B) | 2GB | Fast | Good | 4GB |
| llama3.2:7b | 4GB | Medium | Excellent | 8GB |
| mistral | 4GB | Fast | Very Good | 8GB |
| mistral:instruct | 4GB | Fast | Very Good | 8GB |

### 2. **Optimize Timeout**

- For fast responses: `ollama.timeout=30`
- For detailed analysis: `ollama.timeout=60`
- For complex cases: `ollama.timeout=120`

### 3. **Enable/Disable as Needed**

- Development/Testing: `ollama.enabled=true`
- Production (if Ollama unavailable): `ollama.enabled=false`

---

## 🔐 Security Considerations

1. **Local Deployment**: Ollama runs locally (localhost:11434) - no data sent to external servers
2. **Data Privacy**: All dispute data stays on your machine
3. **Firewall**: Ensure port 11434 is not exposed to external networks
4. **Model Updates**: Regularly update models for latest improvements

---

## 📚 Additional Resources

- **Ollama Documentation**: https://github.com/ollama/ollama
- **Llama 3.2 Info**: https://ollama.com/library/llama3.2
- **Mistral Info**: https://ollama.com/library/mistral
- **Model Library**: https://ollama.com/library

---

## 🎉 Success Indicators

You'll know the integration is working when:

✅ Ollama service is running (`ollama list` shows your model)  
✅ Backend starts without errors  
✅ Dispute submissions show "🤖 LLM Analysis" in the response  
✅ Detailed reasoning includes fraud assessment, confidence, and recommendations  
✅ Response time is reasonable (5-30 seconds depending on model)  

---

## 💡 Example LLM Output

When you submit a dispute, you'll see output like:

```
🤖 LLM Analysis (llama3.2):
• Fraud Assessment: YES
• Confidence: HIGH
• Key Reasons: 
  1. Geographical impossibility - transaction in USA while user in India
  2. High transaction amount ($25,000) increases fraud risk
  3. User explicitly claims unauthorized transaction
  4. No legitimate reason for location mismatch
• Red Flags: 
  - Location mismatch is a critical fraud indicator
  - Amount is significantly high
  - User denies authorization
• Recommendations: 
  - Immediate card blocking recommended
  - Full refund should be processed
  - Investigate merchant for potential compromise
```

---

## 🆘 Need Help?

If you encounter issues:

1. Check Ollama logs: `ollama logs`
2. Check backend logs in the terminal
3. Verify model is installed: `ollama list`
4. Test Ollama directly: `ollama run llama3.2 "test"`
5. Disable Ollama temporarily: Set `ollama.enabled=false`

---

**Made with ❤️ using Ollama + Llama3.2/Mistral**