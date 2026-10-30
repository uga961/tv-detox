@echo off
title Android TV Bloatware Remover
echo ==============================================
echo     Safe Android TV Bloatware Remover
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
echo Disabling telemetry, ads, and bloatware packages...
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
    com.mitv.tvhome.atv
) do (
    echo Disabling %%p...
    adb shell pm disable-user --user 0 %%p
)

echo.
echo ==============================================
echo   Debloat Complete! Checking RAM savings:
echo ==============================================
adb shell cat /proc/meminfo | findstr /i "MemTotal MemFree MemAvailable"
echo.
pause
