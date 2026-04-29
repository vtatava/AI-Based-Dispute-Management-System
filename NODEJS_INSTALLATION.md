# 📦 Node.js Installation Guide

## ⚠️ Node.js Not Found

Your system does not have Node.js/npm installed, which is required to run the React frontend.

## Installing Node.js on Windows

### Option 1: Official Installer (Recommended)

1. **Download Node.js**
   - Visit: https://nodejs.org/
   - Download the **LTS version** (Long Term Support)
   - Current LTS: Node.js 20.x or 18.x

2. **Run the Installer**
   - Double-click the downloaded `.msi` file
   - Follow the installation wizard
   - ✅ Check "Automatically install necessary tools"
   - ✅ Add to PATH (default option)

3. **Verify Installation**
   Open a NEW PowerShell window and run:
   ```powershell
   node -version
   npm -version
   ```

   Expected output:
   ```
   v20.x.x
   10.x.x
   ```

### Option 2: Using Chocolatey

```powershell
# Install Node.js LTS
choco install nodejs-lts

# Verify
node -version
npm -version
```

### Option 3: Using Scoop

```powershell
# Install Node.js
scoop install nodejs-lts

# Verify
node -version
npm -version
```

### Option 4: Using NVM (Node Version Manager)

```powershell
# Install NVM for Windows
# Download from: https://github.com/coreybutler/nvm-windows/releases

# After installation:
nvm install lts
nvm use lts

# Verify
node -version
npm -version
```

---

## After Installing Node.js

### 1. Verify Installation
```powershell
node -version
npm -version
```

### 2. Install Frontend Dependencies
```powershell
cd frontend
npm install
```

This will install:
- React 18.2.0
- React DOM 18.2.0
- Axios 1.6.0
- react-scripts 5.0.1

### 3. Start Frontend
```powershell
npm start
```

The frontend will:
- Start on http://localhost:3000
- Automatically open in your browser
- Hot-reload on file changes

---

## Current System Status

✅ **Installed:**
- Java 17 (OpenJDK 17.0.16)
- Backend is RUNNING on port 9090

❌ **Need to Install:**
- Node.js 18+ or 20+ (LTS recommended)
- npm (comes with Node.js)

---

## Troubleshooting

### npm command not found after installation
1. Close ALL terminal windows
2. Open a NEW PowerShell window
3. Try again: `npm -version`

### Permission errors during npm install
```powershell
# Run PowerShell as Administrator
npm install
```

### Slow npm install
```powershell
# Use a faster registry
npm config set registry https://registry.npmjs.org/
npm install
```

### Port 3000 already in use
- The system will prompt you to use a different port
- Press 'Y' to accept
- Or kill the process using port 3000:
```powershell
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

---

## Alternative: Test Backend Without Frontend

You can test the backend API without the frontend using:

### PowerShell
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" -Method POST -ContentType "application/json" -Body '{"amount":25000,"location":"USA","description":"not done by me"}'
```

### cURL (if installed)
```bash
curl -X POST http://localhost:9090/api/dispute/raise \
  -H "Content-Type: application/json" \
  -d '{"amount":25000,"location":"USA","description":"not done by me"}'
```

### Postman
1. Method: POST
2. URL: http://localhost:9090/api/dispute/raise
3. Headers: Content-Type: application/json
4. Body (raw JSON):
```json
{
  "amount": 25000,
  "location": "USA",
  "description": "not done by me"
}
```

---

## Quick Start After Node.js Installation

1. **Verify Node.js**
   ```powershell
   node -version
   npm -version
   ```

2. **Install Dependencies**
   ```powershell
   cd frontend
   npm install
   ```

3. **Start Frontend**
   ```powershell
   npm start
   ```

4. **Access Application**
   - Frontend: http://localhost:3000
   - Backend: http://localhost:9090

---

## System Requirements Summary

| Component | Status | Version |
|-----------|--------|---------|
| Java | ✅ Installed | 17.0.16 |
| Maven | ✅ Wrapper Included | 3.9.5 |
| Backend | ✅ Running | Port 9090 |
| Node.js | ❌ Not Installed | Need 18+ or 20+ |
| npm | ❌ Not Installed | Comes with Node.js |
| Frontend | ⏳ Waiting | Need Node.js |

---

## Next Steps

1. ✅ Backend is running successfully
2. ❌ Install Node.js (see instructions above)
3. ⏳ Install frontend dependencies (`npm install`)
4. ⏳ Start frontend (`npm start`)
5. ⏳ Test the complete application

---

**Download Node.js**: https://nodejs.org/
**Choose**: LTS (Long Term Support) version
**After Installation**: Close and reopen terminal, then run `npm install` in frontend directory
