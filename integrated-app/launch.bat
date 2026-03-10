@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   Menjalankan Graf Algoritmik
echo ========================================
echo.

REM Run from parent directory so data/ folder is accessible
cd /d "%~dp0.."

REM Run the app
java --module-path "integrated-app\lib\javafx-sdk-21.0.2\lib" ^
    --add-modules javafx.controls ^
    -cp "integrated-app\out" ^
    com.grafapp.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Aplikasi error!
    pause
)
