# Replicate This Apple TV 4K Style Launcher

This guide allows anyone who clones this repository to replicate and install this Apple TV 4K style TV launcher with custom dock apps, cinematic wallpapers, Ken Burns animation, and stats.

You can install it in multiple ways:
1. **Interactive Terminal Prompt (`./install.sh`)** – Zero AI required, guided CLI wizard in your terminal.
2. **Single Command Prompt / APK Sideload** – 1-line ADB command or USB flash drive install.
3. **Single AI Prompt** – Using an AI assistant (Claude Code, Antigravity, Cursor, ChatGPT) to customize apps and wallpapers automatically.

---

## ⚡ Option 1: Interactive Terminal Prompt (Zero AI Required)

You don't need any AI to install and run this launcher. Just open your Terminal (macOS/Linux) or Command Prompt / Git Bash (Windows) in the repository folder and run:

```bash
./install.sh
```

The script will interactively guide you:
1. Finds ADB automatically.
2. Detects or asks for your TV's IP address (e.g. `192.168.1.150`).
3. Installs the pre-built APK (or gives you the option to recompile from source).
4. Automatically grants `GET_USAGE_STATS` permission for screen time.
5. Launches CleanYourTV immediately on your screen!

---

## 📦 Option 2: Pre-built APK (Single Command Prompt or USB)

If you already have the APK or want to install with a single command prompt:

### Via ADB:
```bash
adb connect <YOUR_TV_IP>:5555
adb install -r launcher/app/build/outputs/apk/debug/app-deblouting-ps5.apk
adb shell am start -n com.gathin.tvhome/.MainActivity
```

### Via USB / TV File Manager (Zero PC Required):
1. Copy `launcher/app/build/outputs/apk/debug/app-deblouting-ps5.apk` to a USB flash drive.
2. Plug the USB drive into your TV.
3. Open any TV file manager (like **X-plore** or **FX File Explorer**) or use the **Downloader** app to open and install the APK.

---

## 🤖 Option 3: Replicate with a Single AI Prompt (For Custom Dock Apps)

Copy and paste the prompt below into your AI coding assistant (on a computer connected to the same Wi-Fi as your TV):

```markdown
I want to replicate and customize the CleanYourTV Apple TV 4K style launcher on my Android TV / Google TV.

Before making changes, please ask me:
1. "Which apps would you like pinned to your bottom dock, in order? (e.g., YouTube, Netflix, Disney+, Prime Video, Vidio, Apple TV, HBO Max, Plex, Spotify, etc.)"
2. "Do you have custom full-screen wallpapers for these apps (saved in wallpapers/), or would you like matching cinematic key art wallpapers mapped?"
3. "Would you like pure single-page mode (dock + top action buttons only) or keep the top tabs (Home & App list)?"

Once I provide my list of apps:
1. Connect to the TV over ADB (port 5555) and verify installed packages (`pm list packages`).
2. Update `launcher/app/src/main/res/values/config.xml`:
   - Set `<string-array name="dock_apps">` with the exact package names of my chosen apps in order.
   - Set `<integer name="columns">` to match the number of dock apps.
3. Configure the visual assets for each dock app:
   - **Unfocused icon**: A clean, borderless square logo in `launcher/app/src/main/assets/icons/<name>-small-icons.png` (50×50dp, 14dp rounded corners).
   - **Focused banner**: The Leanback banner (92×52dp) or high-res show card.
   - **Cinematic wallpaper**: A full 1080p backdrop in `launcher/app/src/main/res/drawable-nodpi/wallpaper_<pkg_or_name>.jpg` and `wallpapers/` that smoothly cross-fades when the dock item is focused.
4. Maintain performance optimizations:
   - Instant, crisp dock switching (no bouncy spring physics or laggy alpha cross-fade delays).
   - Subtle bottom-right stats card displaying "Weekly Screen Time" and "Storage Usage".
   - Deterministic D-pad navigation: Pressing UP from the dock jumps directly to top action icons (Inputs, Wi-Fi, Settings), and pressing DOWN returns to the dock.
5. Build the debug APK (`./gradlew assembleDebug`), install over ADB, grant `GET_USAGE_STATS` permission, and launch `com.gathin.tvhome/.MainActivity`.
6. Take a screenshot from the TV (`screencap -p`) and pull it to verify that everything looks pixel-perfect.
```

---

## 🛠 Option 4: Step-by-Step Manual Replication

If you prefer customizing the launcher manually, follow these steps:

### 1. Configure Your Dock Apps
Open [`launcher/app/src/main/res/values/config.xml`](file:///launcher/app/src/main/res/values/config.xml) and edit `dock_apps` and `columns`:

```xml
<string-array name="dock_apps">
    <item>com.google.android.youtube.tv</item>
    <item>com.apple.atve.androidtv.appletv</item>
    <item>com.netflix.ninja</item>
    <item>com.wbd.stream</item>
    <item>com.amazon.amazonvideo.livingroom</item>
    <item>com.disney.disneyplus</item>
    <item>com.vidio.android.tv</item>
</string-array>

<!-- Number of columns matching the number of dock items -->
<integer name="columns">7</integer>
```

### 2. Add App Assets & Wallpapers
For each app on your dock:
1. **Unfocused Small Icon** (Optional but recommended):
   Place a transparent or dark square logo (50×50dp) in `launcher/app/src/main/assets/icons/<app>-small-icons.png`. The launcher automatically rounds corners by 14dp.
2. **Focused Show Wallpaper**:
   Place a high-resolution 1920×1080 JPEG in:
   - `launcher/app/src/main/res/drawable-nodpi/wallpaper_<app>.jpg`
   - and `wallpapers/<app>.jpg`
   The launcher maps wallpapers by package name (e.g. `wallpaper_youtube.jpg`, `wallpaper_netflix.jpg`, `wallpaper_vidio.jpg`).

### 3. Build & Install
Run the deploy script or execute via terminal:

```bash
# 1. Connect to your TV over ADB
adb connect 192.168.1.xxx:5555

# 2. Build and install the APK
cd launcher
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-deblouting-ps5.apk

# 3. Grant Usage Stats permission for screen time
adb shell appops set com.gathin.tvhome GET_USAGE_STATS allow

# 4. Start the launcher
adb shell am start -n com.gathin.tvhome/.MainActivity
```

### 4. Verify on Screen
Capture a screenshot to confirm layout and wallpaper alignment:

```bash
adb shell screencap -p /sdcard/home_screen.png
adb pull /sdcard/home_screen.png .
```

---

## 🔄 How to Rollback / Restore Your Stock Launcher

Rolling back is completely safe and takes less than 30 seconds.

### If you installed via .APK or `./install.sh`:
- **Directly from your TV (No PC required)**:
  1. Open your TV **Settings ⚙️** -> **Apps** -> **See all apps**.
  2. Select **CleanYourTV** (or `com.gathin.tvhome`).
  3. Click **Uninstall**.
  4. Press the **HOME** button on your TV remote. Your TV will automatically return to its original stock launcher (Google TV / Android TV).
- **Via 1-Click Script**:
  Run `./uninstall.sh` in your terminal. It will re-enable any disabled stock launcher and uninstall CleanYourTV automatically.
- **Via ADB Command**:
  ```bash
  adb uninstall com.gathin.tvhome
  ```

### If you installed using the AI Prompt Method:
- **On the TV**:
  Simply prompt your AI assistant:
  > *"Please uninstall CleanYourTV from my TV, re-enable the stock launcher, and restore the TV home screen."*
  The AI will run the rollback commands automatically over ADB.
- **On the Local Codebase**:
  To reset the code back to default and discard custom app configurations:
  ```bash
  git restore .
  # or switch back to the original branch:
  git checkout main
  ```

---

## 🌟 Key Features in this Replicated Launcher

- **Apple TV 4K Style Glass Dock**: Sleek translucent glass highlight that snaps instantly between apps without lag or frame drops.
- **Cinematic Show Art**: Full-bleed 1080p show backdrops that cross-fade when focusing on each streaming service.
- **Clean Bottom-Right Stats**: Displays Weekly Screen Time and Storage Usage for the focused application.
- **Pure Single-Page Experience**: Home-first design with direct navigation between dock apps and top quick controls (Inputs / HDMI, Wi-Fi, Settings).
- **Lightweight & Battery/CPU Friendly**: Zero heavy physics simulations or background battery drains on Android TV hardware.
