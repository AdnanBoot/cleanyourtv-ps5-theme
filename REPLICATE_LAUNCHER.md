# Replicate This Apple TV 4K Style Launcher

This guide allows anyone who clones this repository to replicate this Apple TV 4K style TV launcher with custom dock apps, show wallpapers, and stats in **one single prompt** using an AI assistant (Claude Code, Antigravity, Cursor, Codex, Gemini CLI), or manually via Gradle and ADB.

---

## ⚡ Option 1: Replicate with a Single AI Prompt (Recommended)

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

## 🛠 Option 2: Step-by-Step Manual Replication

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
adb install -r app/build/outputs/apk/debug/app-debug.apk

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

## 🌟 Key Features in this Replicated Launcher

- **Apple TV 4K Style Glass Dock**: Sleek translucent glass highlight that snaps instantly between apps without lag or frame drops.
- **Cinematic Show Art**: Full-bleed 1080p show backdrops that cross-fade when focusing on each streaming service.
- **Clean Bottom-Right Stats**: Displays Weekly Screen Time and Storage Usage for the focused application.
- **Pure Single-Page Experience**: Home-first design with direct navigation between dock apps and top quick controls (Inputs / HDMI, Wi-Fi, Settings).
- **Lightweight & Battery/CPU Friendly**: Zero heavy physics simulations or background battery drains on Android TV hardware.
