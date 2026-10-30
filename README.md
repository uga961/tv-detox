# TV Detox Launcher 📺⚡

> A clean, ultra-lightweight, and lightning-fast minimalist launcher built specifically for Android TV and memory-constrained devices (such as 1GB RAM Mi TVs, Xiaomi TV Sticks, Chromecast with Google TV, and generic ATV boxes).

![TV Detox Banner](https://raw.githubusercontent.com/uga961/tv-detox/main/preview.png)

---

## ✨ Features & Interface

- **Minimalist Aesthetic:** Deep obsidian background with a subtle, ambient center-glow.
- **Top Bar Information:**
  - **Dynamic Greeting:** Greets you with `GOOD MORNING`, `GOOD AFTERNOON`, `GOOD EVENING`, or `GOOD NIGHT` based on local time.
  - **Clock & Date:** Large, high-visibility digital clock with date format (`1:13 PM | SATURDAY 17 OCTOBER`).
- **Quick Action Pills:**
  - **Inputs:** Instant access to TV input sources (HDMI 1/2/3, AV).
  - **Wi-Fi:** Displays connected Wi-Fi network name (SSID) and opens network settings on click.
  - **Settings:** One-click shortcut to TV system settings.
- **Dynamic App Title:** Displays the currently focused application name in large, bold typography.
- **Dynamic App Discovery & Real-Time Auto-Refresh:**
  - Automatically queries the Android system for **ALL** installed TV and sideloaded applications.
  - Newly installed apps from the Google Play Store appear instantly without needing a TV restart.
  - Dedicated custom card for **Google Play Store** (`com.android.vending`) so you can download apps with one click.
- **Instant-Response App Carousel:**
  - High-resolution, rounded cards for your essential daily apps (**Netflix**, **Amazon Prime Video**, **YouTube**, **Disney+ Hotstar**, **FanCode**, **Google Play Store**).
  - Automatically displays official 16:9 Leanback banners for any other installed app (e.g., Spotify, VLC, JioCinema, SonyLIV, Zee5, etc.).
  - **All Applications (+):** Built-in launcher grid to access any other installed TV app without cluttering the main screen.
- **Ultra-Low Memory Footprint:**
  - **APK Size:** Only ~3.2 MB.
  - **RAM Overhead:** ~10–14 MB (compared to 80–150 MB for stock Google TV Launcher and Xiaomi PatchWall).
  - **0% Idle CPU Usage:** No background tracking, no telemetry, and zero ads.

---

## 🔍 Understanding the "Lag" Issue (Animation Scale Fix)

If your TV felt unresponsive or jerky after running `settings put global animator_duration_scale 0`:

> **Why it happens:** Setting `animator_duration_scale 0` causes Android to skip all property animators. On Android TV, remote control navigation relies on focus animations (card scaling, glow transitions) to provide immediate visual feedback. With animators turned off, the remote presses feel robotic and choppy.
>
> **The Solution:** Setting animations to **`0.5x`** provides the best of both worlds — instantaneous navigation with smooth 60 FPS card scaling.

```bash
# Recommended optimal animation settings:
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5
```

---

## 🚀 Step-by-Step Installation & Setup

### Step 1: Enable Developer Options on Your TV
1. Turn on your Android TV and open **Settings** (gear icon).
2. Go to **Device Preferences** (or **System**) → **About**.
3. Scroll down to **Build** (or Android TV OS build) and click it **7 times** until you see:
   > *"You are now a developer!"*
4. Go back to **Device Preferences** → open the new **Developer options** menu.
5. Turn on **USB Debugging** (on Xiaomi/Mi TV, ensure **ADB Debugging** / **Network Debugging** is enabled).

---

### Step 2: Connect to Your TV over Wi-Fi
Make sure your computer is on the same Wi-Fi network as your TV.

1. Find your TV's IP address in **Settings → Network & Internet → [Your Wi-Fi]** (e.g. `10.60.180.251`).
2. Open Command Prompt / Terminal on your computer and run:
   ```bash
   adb connect <YOUR_TV_IP>:5555
   ```
3. A prompt will appear on your TV: **"Allow USB debugging?"**.
   - Check the box **"Always allow from this computer"** and click **OK**.
4. Confirm connection:
   ```bash
   adb devices
   # Should output: <YOUR_TV_IP>:5555    device
   ```

---

### Step 3: Install TV Detox Launcher
Download `tv-detox-launcher.apk` from the root of this repository or your clone:

```bash
# From the repository root:
adb install -r tv-detox-launcher.apk
```

---

### Step 4: Set TV Detox as Default Launcher
Start the launcher and set it as the primary home activity:

```bash
adb shell am start -n com.detox.tvlauncher/.MainActivity
adb shell cmd package set-home-activity com.detox.tvlauncher/.MainActivity
adb shell input keyevent KEYCODE_HOME
```

---

### Step 5: Disable Stock Cluttered Launchers
To prevent Google TV Launcher and Xiaomi PatchWall from running in the background and consuming memory:

```bash
# Disable Google TV Launcher
adb shell pm disable-user --user 0 com.google.android.tvlauncher

# Disable Xiaomi PatchWall
adb shell pm disable-user --user 0 com.mitv.tvhome.atv
```
*(Pressing the Home or PatchWall buttons on your remote will now instantly direct to TV Detox).*

---

### Step 6: Speed Up System & Sleep Timeout
Apply optimal animation scales (snappy 0.5x) and set 5-minute screen sleep:

```bash
# Smooth 0.5x animations
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5

# Sleep after 5 minutes (300,000 ms) and disable screensaver
adb shell settings put system screen_off_timeout 300000
adb shell settings put secure screensaver_enabled 0
```

---

### Step 7: Safe Bloatware Debloating (Tested on Mi TV / Android TV)

Run this one-line command to disable background telemetry, analytics, and unused system bloatware:

**Windows (Command Prompt):**
```cmd
for %p in (com.miui.tv.analytics com.xiaomi.statistic com.google.android.feedback com.google.android.tv.bugreportsender com.xiaomi.mitv.handbook com.google.android.youtube.tvmusic com.google.android.videos com.google.android.play.games com.xiaomi.mimusic2 com.mitv.gallery com.android.gallery3d com.android.camera2 com.android.printspooler com.google.android.syncadapters.calendar com.android.smspush com.xiaomi.mitv.smartshare com.xiaomi.wfdsinkhelperservice com.google.android.marvin.talkback com.xm.webcontent com.xiaomo.tv.milegal com.google.android.tvrecommendations com.google.android.leanbacklauncher.recommendations com.google.android.backdrop com.android.wallpapercropper com.android.wallpaperbackup com.mitv.dream com.android.dreams.basic com.xiaomi.floatingframe android.autoinstalls.config.xioami.dangal) do adb shell pm disable-user --user 0 %p
```

**Linux / macOS:**
```bash
for p in com.miui.tv.analytics com.xiaomi.statistic com.google.android.feedback com.google.android.tv.bugreportsender com.xiaomi.mitv.handbook com.google.android.youtube.tvmusic com.google.android.videos com.google.android.play.games com.xiaomi.mimusic2 com.mitv.gallery com.android.gallery3d com.android.camera2 com.android.printspooler com.google.android.syncadapters.calendar com.android.smspush com.xiaomi.mitv.smartshare com.xiaomi.wfdsinkhelperservice com.google.android.marvin.talkback com.xm.webcontent com.xiaomo.tv.milegal com.google.android.tvrecommendations com.google.android.leanbacklauncher.recommendations com.google.android.backdrop com.android.wallpapercropper com.android.wallpaperbackup com.mitv.dream com.android.dreams.basic com.xiaomi.floatingframe android.autoinstalls.config.xioami.dangal; do adb shell pm disable-user --user 0 $p; done
```

---

### Step 8: Set Default Input on Boot (e.g. HDMI 3)
To ensure your TV turns on directly to HDMI 3 (or your last used console/set-top box input):
1. On your TV, navigate to **Settings → Inputs** (or **Device Preferences → Inputs**).
2. Select **"Default Input on Boot"** or **"Stay on last used input"**.
3. Choose **HDMI 3**.

---

### Step 9: Final Reboot & Memory Verification
Reboot the TV once to clear cached processes:

```bash
adb reboot
```

After 30 seconds, reconnect and verify your free memory:
```bash
adb connect <YOUR_TV_IP>:5555
adb shell cat /proc/meminfo | findstr /i "MemTotal MemFree MemAvailable"
```
*Expected Usable RAM Increase: **+40% to +55%** free available memory!*

---

## 🛠️ Automated Scripts

Inside the `scripts/` folder, you will find ready-to-run automation scripts:
- **`install.bat` / `install.sh`**: Automatically prompts for your TV IP, connects, installs the APK, and sets up TV Detox as default launcher.
- **`debloat.bat`**: One-click disabling of all background telemetry and adware.
- **`restore.bat`**: Full rollback script to restore default factory settings, re-enable Google Launcher/PatchWall, and reset animation speeds.

---

## 🔄 Reversion & Undo Guide

If you ever wish to restore any original system component, run the corresponding command:

| Component | Restore Command |
|---|---|
| **Google TV Launcher** | `adb shell pm enable com.google.android.tvlauncher` |
| **Xiaomi PatchWall** | `adb shell pm enable com.mitv.tvhome.atv` |
| **Any Disabled App** | `adb shell pm enable <package_name>` |
| **Factory Animations (1x)** | `adb shell settings put global window_animation_scale 1`<br>`adb shell settings put global transition_animation_scale 1`<br>`adb shell settings put global animator_duration_scale 1` |
| **Screen Sleep (30 min)** | `adb shell settings put system screen_off_timeout 1800000`<br>`adb shell settings put secure screensaver_enabled 1` |
| **Uninstall TV Detox** | `adb uninstall com.detox.tvlauncher` |

---

## 🏗️ Building from Source

To compile the APK yourself using Android SDK / Gradle:

```bash
# Clone the repository
git clone https://github.com/uga961/tv-detox.git
cd tv-detox

# Build debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
MIT License. Free and open source for all Android TV enthusiasts.
