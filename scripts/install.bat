@echo off
title TV Detox Launcher - Windows Installer
echo ==============================================
echo     TV Detox Launcher - Auto Installer (Windows)
echo ==============================================
echo.

set /p TV_IP="Enter your Android TV IP address (e.g. 10.60.180.251): "

if "%TV_IP%"=="" (
    echo Error: IP address cannot be empty.
    pause
    exit /b 1
)

echo.
echo [1/6] Connecting to TV at %TV_IP%:5555...
adb connect %TV_IP%:5555
timeout /t 2 >nul

echo.
echo [2/6] Verifying connected devices...
adb devices

set APK_PATH=..\app\build\outputs\apk\debug\app-debug.apk
if not exist "%APK_PATH%" set APK_PATH=tv-detox-launcher.apk
if not exist "%APK_PATH%" set APK_PATH=..\tv-detox-launcher.apk

echo.
echo [3/6] Installing TV Detox Launcher from %APK_PATH%...
adb install -r "%APK_PATH%"

echo.
echo [4/6] Setting TV Detox as default Home Launcher...
adb shell am start -n com.detox.tvlauncher/.MainActivity
adb shell cmd package set-home-activity com.detox.tvlauncher/.MainActivity
adb shell input keyevent KEYCODE_HOME

echo.
echo [5/6] Disabling heavy stock launchers (Google Launcher ^& PatchWall)...
adb shell pm disable-user --user 0 com.google.android.tvlauncher
adb shell pm disable-user --user 0 com.mitv.tvhome.atv

echo.
echo [6/6] Tuning responsiveness and sleep timeout...
rem Setting 0.5 provides smooth 60fps card focus without robotic lag
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5
rem 5 minute screen sleep
adb shell settings put system screen_off_timeout 300000
adb shell settings put secure screensaver_enabled 0

echo.
echo ==============================================
echo   Success! TV Detox Launcher is now active!
echo ==============================================
pause
