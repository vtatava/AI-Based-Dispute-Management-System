# Backend Port Change Notice

## Important Update

The backend server port has been changed from **8080** to **9090**.

## What Changed

### Backend Configuration
- **File:** `backend/src/main/resources/application.properties`
- **Old Port:** 8080
- **New Port:** 9090
- **Backend URL:** http://localhost:9090

### Frontend Configuration
- **File:** `frontend/src/App.js`
- **API Endpoint Updated:** http://localhost:9090/api/dispute/raise

### Test Scripts Updated
- `test-backend-ollama.ps1` - Now uses port 9090

## How to Access

### Backend API
- Base URL: http://localhost:9090
- API Endpoint: http://localhost:9090/api/dispute/raise
- Health Check: http://localhost:9090/actuator/health (if enabled)

### Frontend
- Frontend still runs on: http://localhost:3000
- Frontend automatically connects to backend on port 9090

## Starting the Application

### Option 1: Use Start Scripts
```bash
# Start both frontend and backend
.\start-all.bat

# Or start individually
.\start-backend.bat
.\start-frontend.bat
```

### Option 2: Manual Start
```bash
# Backend (Terminal 1)
cd backend
.\mvnw.cmd spring-boot:run

# Frontend (Terminal 2)
cd frontend
npm start
```

## Testing the Integration

After starting both servers, test the Ollama integration:
```bash
powershell -ExecutionPolicy Bypass -File test-backend-ollama.ps1
```

## Troubleshooting

### Port Already in Use
If port 9090 is already in use:
1. Find the process: `netstat -ano | findstr :9090`
2. Kill the process: `taskkill /PID <process_id> /F`
3. Or change the port in `application.properties`

### Cannot Connect to Backend
- Ensure backend is running on port 9090
- Check firewall settings
- Verify no other service is using port 9090

## Additional Changes Made

Along with the port change, the following fixes were also applied:

### Ollama Integration Fix
- Fixed WebClient configuration in `OllamaService.java`
- Added proper base URL configuration
- Changed API calls to use relative paths

These fixes resolve the "⚠️ LLM analysis unavailable" issue.

---
**Note:** After these changes, you must restart the backend server for the changes to take effect.