@echo off
echo Restarting frontend with Tailwind CSS...
cd frontend
echo Killing existing npm processes...
taskkill /F /IM node.exe /T 2>nul
timeout /t 2 /nobreak >nul
echo Starting frontend...
start cmd /k "npm start"
echo Frontend restarted! Check http://localhost:3000

@REM Made with Bob
