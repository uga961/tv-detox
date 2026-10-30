#!/usr/bin/env bash
set -e

echo "=============================================="
echo "    TV Detox Launcher - Auto Installer        "
echo "=============================================="
echo ""

read -p "Enter your Android TV IP address (e.g. 10.60.180.251): " TV_IP

if [ -z "$TV_IP" ]; then
    echo "Error: IP address cannot be empty."
    exit 1
fi

echo "[1/6] Connecting to TV at $TV_IP:5555..."
adb connect "$TV_IP:5555"
sleep 2

echo "[2/6] Verifying device connection..."
adb devices

APK_PATH="../app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    APK_PATH="./tv-detox-launcher.apk"
fi

if [ ! -f "$APK_PATH" ]; then
    APK_PATH="../tv-detox-launcher.apk"
fi

echo "[3/6] Installing TV Detox Launcher APK from $APK_PATH..."
adb install -r "$APK_PATH"

echo "[4/6] Registering TV Detox Launcher as default Home activity..."
adb shell am start -n com.detox.tvlauncher/.MainActivity
adb shell cmd package set-home-activity com.detox.tvlauncher/.MainActivity
adb shell input keyevent KEYCODE_HOME

echo "[5/6] Disabling bloated stock launchers (Google TV Launcher & Xiaomi PatchWall)..."
adb shell pm disable-user --user 0 com.google.android.tvlauncher 2>/dev/null || true
adb shell pm disable-user --user 0 com.mitv.tvhome.atv 2>/dev/null || true

echo "[6/6] Tuning responsiveness and sleep timer..."
# 0.5x scale provides buttery smooth remote navigation without sluggishness or choppy snaps
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5
# 5 minutes sleep timeout
adb shell settings put system screen_off_timeout 300000
adb shell settings put secure screensaver_enabled 0

echo ""
echo "=============================================="
echo "  Success! TV Detox Launcher is now active!   "
echo "=============================================="
