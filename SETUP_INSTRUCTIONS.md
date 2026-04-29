# 🔧 Setup Instructions

## ⚠️ Important: Maven Installation Required

Your system has **Java 17** installed ✅, but **Maven is not installed** ❌.

## Installing Maven on Windows

### Option 1: Using Chocolatey (Recommended)
```powershell
# Install Chocolatey if not already installed
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Maven
choco install maven
```

### Option 2: Manual Installation
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to System PATH:
   - Open System Properties → Environment Variables
   - Add to PATH: `C:\Program Files\Apache\maven\bin`
4. Verify: Open new terminal and run `mvn -version`

### Option 3: Using Scoop
```powershell
# Install Scoop if not already installed
iwr -useb get.scoop.sh | iex

# Install Maven
scoop install maven
```

## Verify Installation

After installing Maven, verify with:
```powershell
mvn -version
```

Expected output:
```
Apache Maven 3.x.x
Maven home: C:\...
Java version: 17.0.16
```

## Running the Application

### Backend
```powershell
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend
```powershell
cd frontend
npm install
npm start
```

## Alternative: Run Without Maven (Using Maven Wrapper)

If you don't want to install Maven globally, you can use Maven Wrapper:

1. Download Maven Wrapper files to backend directory:
```powershell
cd backend
# Download mvnw and mvnw.cmd from Spring Initializr or copy from another project
```

2. Run using wrapper:
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

## Quick Test After Setup

Once Maven is installed and backend is running:

1. Backend should show:
```
Started DisputeAiApplication in X.XXX seconds
Tomcat started on port(s): 9090
```

2. Test API with PowerShell:
```powershell
Invoke-RestMethod -Uri "http://localhost:9090/api/dispute/raise" -Method POST -ContentType "application/json" -Body '{"amount":25000,"location":"USA","description":"not done by me"}'
```

Expected response:
```json
{
  "intent": "FRAUD",
  "riskScore": 90,
  "decision": "AUTO_REFUND & BLOCK_CARD"
}
```

## System Requirements Summary

✅ **Installed:**
- Java 17 (OpenJDK 17.0.16)

❌ **Need to Install:**
- Maven 3.6+ (see instructions above)
- Node.js 16+ (for frontend)
- npm (comes with Node.js)

## Next Steps

1. Install Maven using one of the methods above
2. Open a NEW terminal (to refresh PATH)
3. Verify: `mvn -version`
4. Follow QUICKSTART.md to run the application

## Troubleshooting

### Maven command not found after installation
- Close and reopen terminal
- Verify PATH includes Maven bin directory
- Run: `$env:Path` to check PATH in PowerShell

### Java version mismatch
- Ensure JAVA_HOME points to Java 17
- Set in Environment Variables if needed

### Port already in use
```powershell
# Check what's using port 9090
netstat -ano | findstr :9090

# Kill the process
taskkill /PID <PID> /F
```

---

**Once Maven is installed, refer to QUICKSTART.md for running the application!**
