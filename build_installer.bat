@echo off
title Spydy Reminder - Build Installer
cd /d "%~dp0"

echo ============================================
echo   Spydy Reminder - Building Windows .exe
echo ============================================
echo.

if not exist "node_modules" (
    echo Installing dependencies (first time only)...
    call npm install
    if errorlevel 1 (
        echo.
        echo FAILED to install dependencies. Make sure Node.js is installed:
        echo https://nodejs.org
        pause
        exit /b 1
    )
)

echo.
echo Building installer .exe with electron-builder...
call npm run dist

if errorlevel 1 (
    echo.
    echo Build failed. See the error above.
    pause
    exit /b 1
)

echo.
echo ============================================
echo   DONE! Your .exe files are in the "dist" folder:
echo   - Spydy Reminder Setup ^<version^>.exe   (installer)
echo   - Spydy Reminder ^<version^>.exe         (portable, no install)
echo ============================================
pause
