# 🔧 QUICK FIX: LLM Not Available Issue

## ✅ Problem Solved!

**Root Cause:** Ollama service is not running

**Your System Status:**
- ✅ Ollama installed: YES (version 0.20.7)
- ✅ Models available: YES (mistral + llama3.2)
- ❌ Service running: NO ← **This is the issue!**

---

## 🚀 Quick Solution (3 Steps)

### Step 1: Start Ollama Service

**Option A: Use the batch file (Easiest)**
```
Double-click: start-ollama.bat
```

**Option B: Manual command**
```powershell
ollama serve
```

Keep this window open!

---

### Step 2: Start Your Application

**Option A: Start everything at once**
```
Double-click: start-all-with-ollama.bat
```

**Option B: Start manually**
```powershell
# Terminal 1: Ollama (already running from Step 1)

# Terminal 2: Backend
.\start-backend.bat

# Terminal 3: Frontend
.\start-frontend.bat
```

---

### Step 3: Test It!

1. Open http://localhost:3000
2. Submit a test dispute:
   - Amount: `25000`
   - Transaction Location: `USA`
   - User Location: `INDIA`
   - Description: `This transaction was not done by me`

3. Look for this in the response:
   ```
   🤖 LLM Analysis (mistral):
   • Fraud Assessment: YES
   • Confidence: HIGH
   • Key Reasons: ...
   ```

**If you see this, it's working!** ✅

---

## 📋 What Changed?

### Before (Not Working):
```
⚠️ LLM analysis unavailable. Using rule-based decision: PENDING
• Recommendations: Review manually or retry LLM analysis
```

### After (Working):
```
🤖 LLM Analysis (mistral):
• Fraud Assessment: YES
• Confidence: HIGH
• Key Reasons:
  1. Geographical impossibility - transaction in USA while user in India
  2. High transaction amount ($25,000) increases fraud risk
  3. User explicitly claims unauthorized transaction
• Red Flags:
  - Location mismatch is a critical fraud indicator
  - Amount is significantly high
• Recommendations:
  - Immediate card blocking recommended
  - Full refund should be processed
```

---

## 🎯 Files Created for You

1. **start-ollama.bat** - Start Ollama service only
2. **start-all-with-ollama.bat** - Start everything (Ollama + Backend + Frontend)
3. **add-ollama-to-path.ps1** - Add Ollama to system PATH (already done)

---

## 🔍 Verify Everything is Working

### Check 1: Ollama Service
```powershell
curl http://localhost:11434/api/tags
```
**Expected:** JSON response with your models

### Check 2: Backend Logs
Look for this in backend terminal:
```
Ollama integration enabled
Model: mistral
Base URL: http://localhost:11434
```

### Check 3: Test Dispute
Submit a dispute and check for "🤖 LLM Analysis" in response

---

## 🛠️ Troubleshooting

### Issue: "Connection refused to localhost:11434"
**Solution:** Ollama service is not running
```powershell
.\start-ollama.bat
```

### Issue: Still showing "LLM unavailable"
**Solution:** Restart backend after starting Ollama
```powershell
# Stop backend (Ctrl+C)
# Start Ollama
.\start-ollama.bat
# Start backend again
.\start-backend.bat
```

### Issue: Slow responses
**Solution:** This is normal! LLM analysis takes 5-30 seconds
- Llama3.2: 5-15 seconds
- Mistral: 10-30 seconds

### Issue: Want to use different model
**Solution:** Edit `backend/src/main/resources/application.properties`
```properties
# Change from mistral to llama3.2
ollama.model=llama3.2
```
Then restart backend.

---

## 📊 Your Models

You have both models installed:

| Model | Size | Speed | Accuracy | Current |
|-------|------|-------|----------|---------|
| **mistral** | 4.4 GB | Fast | Excellent | ✅ Active |
| **llama3.2** | 2.0 GB | Faster | Good | Available |

To switch to llama3.2 (faster):
1. Edit `application.properties`: `ollama.model=llama3.2`
2. Restart backend

---

## 🎉 Success Checklist

- [ ] Ollama service running (start-ollama.bat)
- [ ] Backend running (start-backend.bat)
- [ ] Frontend running (start-frontend.bat)
- [ ] Can access http://localhost:3000
- [ ] Dispute submission shows "🤖 LLM Analysis"
- [ ] Detailed AI reasoning appears in response

---

## 💡 Pro Tips

1. **Always start Ollama first** before backend
2. **Keep Ollama window open** while using the app
3. **Use start-all-with-ollama.bat** for convenience
4. **First LLM call is slower** (model loading), subsequent calls are faster
5. **Check backend logs** if issues persist

---

## 🚀 Quick Start Command

```powershell
# One command to start everything:
.\start-all-with-ollama.bat
```

This will open 3 windows:
1. Ollama Service
2. Backend Server
3. Frontend

Wait 30-60 seconds, then open http://localhost:3000

---

## 📞 Need More Help?

- Check backend logs for errors
- Verify Ollama is running: `curl http://localhost:11434/api/tags`
- Test Ollama directly: `ollama run mistral "test"`
- Review: `OLLAMA_SETUP_GUIDE.md` for detailed setup
- Review: `OLLAMA_INTEGRATION_README.md` for how it works

---

**Made with ❤️ by Bob**

*Your LLM is ready to detect fraud! 🎯*