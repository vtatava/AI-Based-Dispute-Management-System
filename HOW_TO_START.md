# 🚀 How to Start the Application

## Quick Start (Recommended)

### Option 1: Start Everything at Once
1. Double-click `start-all.bat`
2. Wait for two command windows to open:
   - **Backend Server** (Spring Boot) - Port 9090
   - **Frontend Server** (React) - Port 3000
3. Browser will automatically open to http://localhost:3000
4. **Keep both windows open** while using the application

### Option 2: Start Individually
1. Double-click `start-backend.bat` first
2. Wait for "Started DisputeAiApplication" message
3. Then double-click `start-frontend.bat`
4. Browser will open automatically

---

## ⚠️ Important Notes

### Windows Behavior
- ✅ **Windows staying open is NORMAL** - This means servers are running
- ❌ **Do NOT close the windows** - Closing them stops the servers
- ✅ The windows will show logs and status messages
- ✅ You can minimize them if needed

### Stopping the Application
To stop the servers:
1. Go to each command window
2. Press `Ctrl+C`
3. Confirm with `Y` if prompted
4. Or simply close the windows

---

## 📋 Prerequisites

Before running the batch files, ensure you have:
- ✅ **Java 17 or higher** - [Download](https://adoptium.net/)
- ✅ **Node.js 16 or higher** - [Download](https://nodejs.org/)
- ✅ **npm** (comes with Node.js)

The batch files will check these automatically and show clear error messages if anything is missing.

---

## 🔍 What Each File Does

### start-backend.bat
- Checks Java installation
- Navigates to backend directory
- Builds the project using Maven wrapper
- Starts Spring Boot server on port 9090
- **Window must stay open**

### start-frontend.bat
- Checks Node.js and npm installation
- Navigates to frontend directory
- Installs dependencies (first run only)
- Starts React development server on port 3000
- Opens browser automatically
- **Window must stay open**

### start-all.bat
- Validates all prerequisites
- Launches backend in separate window
- Waits 15 seconds for backend to initialize
- Launches frontend in separate window
- Shows status and URLs

---

## 🐛 Troubleshooting

### "Port already in use" Error

**Backend (Port 9090):**
```cmd
netstat -ano | findstr :9090
taskkill /PID <PID_NUMBER> /F
```

**Frontend (Port 3000):**
- React will ask if you want to use a different port
- Type `Y` and press Enter

### Build Errors
If Maven build fails:
1. Close all windows
2. Delete `backend\target` folder
3. Run `start-backend.bat` again

If npm install fails:
1. Close all windows
2. Delete `frontend\node_modules` folder
3. Run `start-frontend.bat` again

### Java/Node.js Not Found
- Make sure they are installed
- Restart your computer after installation
- Check they are in your system PATH

---

## ✅ Success Indicators

### Backend Running Successfully:
```
Started DisputeAiApplication in X.XXX seconds
Tomcat started on port(s): 9090
```

### Frontend Running Successfully:
```
Compiled successfully!
webpack compiled with 0 warnings
Local: http://localhost:3000
```

### Both Working Together:
- ✅ No errors in either window
- ✅ Browser opens to http://localhost:3000
- ✅ Form is visible and styled
- ✅ Can submit disputes and see results

---

## 🎯 Testing the Application

Once both servers are running, try these test cases:

### Test 1: High Risk Fraud
- Amount: `25000`
- Location: `USA`
- Description: `not done by me`
- Expected: Risk Score 90, AUTO_REFUND

### Test 2: Low Risk
- Amount: `5000`
- Location: `INDIA`
- Description: `Product issue`
- Expected: Risk Score 0, REJECT

### Test 3: Medium Risk
- Amount: `15000`
- Location: `USA`
- Description: `Item not received`
- Expected: Risk Score 50, HUMAN_REVIEW

---

## 📞 Need Help?

If you encounter issues:
1. Check the error messages in the command windows
2. Verify prerequisites are installed
3. Try restarting your computer
4. Check firewall/antivirus settings

---

**Remember: Keep the command windows open while using the application!** 🎉
