@echo off
title Android TV - Full Restore to Factory Stock
echo ==============================================
echo     Android TV - Factory Settings & Launcher Restore
echo ==============================================
echo.

set /p TV_IP="Enter your Android TV IP address (e.g. 10.60.180.251): "
if "%TV_IP%"=="" (
    echo Error: IP address cannot be empty.
    pause
    exit /b 1
)

echo Connecting to %TV_IP%:5555...
adb connect %TV_IP%:5555
timeout /t 2 >nul

echo.
echo [1/4] Re-enabling official Google TV Launcher & PatchWall...
adb shell pm enable com.google.android.tvlauncher
adb shell pm enable com.mitv.tvhome.atv

echo.
echo [2/4] Restoring original animation speeds and sleep timeouts...
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1
adb shell settings put system screen_off_timeout 1800000
adb shell settings put secure screensaver_enabled 1

echo.
echo [3/4] Re-enabling all previously disabled system packages...
for %%p in (
    com.miui.tv.analytics
    com.xiaomi.statistic
    com.google.android.feedback
    com.google.android.tv.bugreportsender
    com.xiaomi.mitv.handbook
    com.google.android.youtube.tvmusic
    com.google.android.videos
    com.google.android.play.games
    com.xiaomi.mimusic2
    com.mitv.gallery
    com.android.gallery3d
    com.android.camera2
    com.android.printspooler
    com.google.android.syncadapters.calendar
    com.android.smspush
    com.xiaomi.mitv.smartshare
    com.xiaomi.wfdsinkhelperservice
    com.google.android.marvin.talkback
    com.xm.webcontent
    com.xiaomo.tv.milegal
    com.google.android.tvrecommendations
    com.google.android.leanbacklauncher.recommendations
    com.google.android.backdrop
    com.android.wallpapercropper
    com.android.wallpaperbackup
    com.mitv.dream
    com.android.dreams.basic
    com.xiaomi.floatingframe
    android.autoinstalls.config.xioami.dangal
) do (
    adb shell pm enable %%p
)

echo.
echo [4/4] Uninstalling TV Detox Launcher (optional)...
adb uninstall com.detox.tvlauncher

echo.
echo ==============================================
echo   All original settings and apps restored!
echo ==============================================
pause
