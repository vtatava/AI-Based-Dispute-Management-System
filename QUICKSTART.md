# 🚀 Quick Start Guide

## Prerequisites Check
Before starting, ensure you have:
- ✅ Java 17 or higher (`java -version`)
- ✅ Maven 3.6+ (`mvn -version`)
- ✅ Node.js 16+ (`node -version`)
- ✅ npm (`npm -version`)

## Step-by-Step Setup

### 1️⃣ Start Backend (First Terminal)

```bash
# Navigate to backend directory
cd backend

# Install dependencies and run
mvn clean install
mvn spring-boot:run
```

**Expected Output:**
```
Started DisputeAiApplication in X.XXX seconds
Tomcat started on port(s): 9090
```

✅ Backend is ready at: http://localhost:9090

---

### 2️⃣ Start Frontend (Second Terminal)

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

**Expected Output:**
```
Compiled successfully!
Local: http://localhost:3000
```

✅ Frontend will automatically open at: http://localhost:3000

---

## 🧪 Test the Application

### Test Case 1: High Risk Fraud
**Input:**
- Amount: `25000`
- Location: `USA`
- Description: `not done by me`

**Expected Result:**
- Intent: `FRAUD`
- Risk Score: `90`
- Decision: `AUTO_REFUND & BLOCK_CARD`

---

### Test Case 2: Low Risk
**Input:**
- Amount: `5000`
- Location: `INDIA`
- Description: `Product issue`

**Expected Result:**
- Intent: `NORMAL`
- Risk Score: `0`
- Decision: `REJECT`

---

### Test Case 3: Medium Risk
**Input:**
- Amount: `15000`
- Location: `USA`
- Description: `Item not received`

**Expected Result:**
- Intent: `NORMAL`
- Risk Score: `50`
- Decision: `HUMAN_REVIEW`

---

## 🔧 Troubleshooting

### Backend Issues

**Problem:** Port 9090 already in use
```bash
# Windows
netstat -ano | findstr :9090
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:9090 | xargs kill -9
```

**Problem:** Maven build fails
```bash
# Clean and rebuild
mvn clean
mvn install -U
```

---

### Frontend Issues

**Problem:** Port 3000 already in use
- Choose a different port when prompted (Y)
- Or kill the process using port 3000

**Problem:** npm install fails
```bash
# Clear cache and reinstall
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

**Problem:** CORS error
- Ensure backend is running on port 9090
- Check browser console for exact error
- Verify CORS is enabled in DisputeController.java

---

## 📊 API Testing (Optional)

### Using cURL:
```bash
curl -X POST http://localhost:9090/api/dispute/raise \
  -H "Content-Type: application/json" \
  -d "{\"amount\":25000,\"location\":\"USA\",\"description\":\"not done by me\"}"
```

### Using Postman:
1. Method: `POST`
2. URL: `http://localhost:9090/api/dispute/raise`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "amount": 25000,
  "location": "USA",
  "description": "not done by me"
}
```

---

## ✅ Success Indicators

### Backend Running Successfully:
- ✅ No errors in console
- ✅ "Started DisputeAiApplication" message appears
- ✅ Port 9090 is listening

### Frontend Running Successfully:
- ✅ Browser opens automatically
- ✅ Form is visible and styled
- ✅ No console errors in browser DevTools

### Integration Working:
- ✅ Form submission shows loading spinner
- ✅ Results appear after submission
- ✅ Risk score bar animates
- ✅ Colors match risk levels

---

## 🎯 Next Steps

1. ✅ Test all three scenarios
2. ✅ Try different combinations
3. ✅ Check browser console for any errors
4. ✅ Review the code structure
5. ✅ Customize the UI if needed

---

## 📞 Need Help?

Check the main README.md for:
- Detailed architecture
- API documentation
- Development guidelines
- Feature list

---

**Happy Testing! 🎉**
