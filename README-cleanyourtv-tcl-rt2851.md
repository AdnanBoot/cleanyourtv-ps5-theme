# CleanYourTV — TCL BeyondTV RT2851 Setup Report & Documentation

## Device Specifications
- **Device Model**: TCL BeyondTV (Client Type: TCL-AP-RT2851-S1)
- **Board**: RT2851
- **OS Version**: Android TV 9 (API level 28)
- **TV+ OS Version**: V5.2.0
- **Software Version**: V8-R851T02-LF1V634.018640
- **Kernel**: Linux 4.14.76 (armv7l, 32-bit)
- **Display Resolution**: 1920×1080 @ 320 dpi

---

## 1. Debloat Summary

All modifications were done non-destructively using `pm disable-user --user 0`. No system packages were uninstalled.

### Debloated Batches:
1. **Telemetry & Analytics**:
   - `com.tcl.bi` (TCL Business Intelligence / Telemetry)
   - `com.google.android.feedback` (Google Feedback Bug Sender)
   - `com.google.android.tv.bugreportsender`
   - `com.google.android.partnersetup`
   - `com.google.android.onetimeinitializer`
   - `android.autoinstalls.config.tcl.device`
2. **Content Recommendations & Promotions**:
   - `com.google.android.tvrecommendations` (Android TV recommendation channel service)
   - `com.google.android.leanbacklauncher.recommendations`
   - `com.tcl.partnercustomizer`
   - `com.tcl.messagebox`
   - `com.tcl.esticker`
   - `com.tcl.bootadservice`
3. **Unused Preloads & Regional Apps**:
   - `com.google.android.videos` (Google Play Movies & TV)
   - `com.tcl.ui_mediaCenter`
   - `com.tcl.useragreement`
   - `com.tcl.android.webview`
   - `com.tcl.keyhelp`
   - Regional Freeview/Stan/LineTV stub packages
4. **Wireless Duplicates & Updaters**:
   - `com.tcl.miracast`
   - `com.tcl.versionUpdateApp` (Firmware Updater)
   - `com.tcl.MultiScreenInteraction_TV` (T-Cast duplicate)
5. **Manufacturer Store & Cleaner**:
   - `com.tcl.appmarket2`
   - `com.tcl.tvweishi` (TCL TV Guard / Cleaner)
6. **Voice Assistant**:
   - `com.google.android.katniss` (Google Assistant)
   - `com.tcl.assistant`
   - `com.tcl.micmanager`
   - `com.google.android.play.games`
7. **TCL Home & TCL Channel**:
   - `com.tcl.tv.tclhome_master` (TCL Home Master)
   - `com.tcl.tv.tclhome_passive` (TCL Home Passive)
   - `com.tcl.waterfall.overseas` (TCL Channel / Live Stream Feed)
8. **Reminder & Duplicate Hotstar**:
   - `com.tcl.notereminder` (TCL Reminder)
   - `in.startv.hotstar.dplus.tv` (Disney+ Hotstar stub/preinstall)

### Preserved Essential Apps:
- **Streaming**: Netflix, YouTube, Prime Video, Apple TV+, Disney+, HBO Max, Vidio, OTT Navigator
- **Core TV Services**: Live TV (`com.tcl.tv`), Source Manager (`com.tcl.sourcemananger`), Google Play Store, Play Services, Chromecast Built-in (`com.google.android.apps.mediashell`), Bluetooth, Wi-Fi.

---

## 2. Apple TV 4K Style Custom Launcher

A native, lightweight Android TV launcher (`com.gathin.tvhome`) was built and installed with custom enhancements:

### Custom 9-App Dock Configuration:
The bottom dock row contains the exact requested 9 apps in order:
1. **YouTube** (`com.google.android.youtube.tv`) — focused wallpaper: `youtube.jpg`
2. **Apple TV** (`com.apple.atve.androidtv.appletv`) — focused wallpaper: *Silo* (`appletv.jpg`)
3. **Netflix** (`com.netflix.ninja`) — focused wallpaper: *Stranger Things* (`netflix.jpg`)
4. **HBO Max** (`com.wbd.stream`) — focused wallpaper: *The Last of Us* (`hbomax.jpg`)
5. **Prime Video** (`com.amazon.amazonvideo.livingroom`) — focused wallpaper: *The Boys* (`prime.jpg`)
6. **Disney+** (`com.disney.disneyplus`) — focused wallpaper: `disney.jpg`
7. **Vidio** (`com.vidio.android.tv`) — focused wallpaper: `vidio.jpg`
8. **Google Play Store** (`com.android.vending`) — focused wallpaper: `googleplay.jpg`
9. **OTT Navigator** (`appnovatica.tv`) — focused wallpaper: `ottnavigator.png`

### Focused Show Wallpapers & Grid Motion:
When focusing any dock item, the full screen smoothly cross-fades into the cinematic key art wallpaper for that show (rendered in pixel-perfect unscaled 1080p Full HD):
- Every dock app has its own custom key art wallpaper mapped to its package.
- **Navigating Down to Grid**: The wallpaper smoothly fades out to pitch black so the app grid can be navigated with zero visual clutter.
- **Navigating Up to Dock**: The wallpaper of the active dock app fades back in.

---

## 3. Dynamic Photo Screensaver (Daydream & Idle)

A custom dynamic photo screensaver is built into the launcher (`com.gathin.tvhome/.ClockDream` & `ClockActivity`):
- **Clean Full-Screen Slideshow**: Pure photos with no phone mockup frame, no clocks, and no dates.
- **Aspect Ratio Preservation**:
  - **Portrait Photos (Phone format)**: Displayed centered with full 1080 vertical height (`fitCenter`), uncropped and unstretched, with subtle ambient lighting softly filling the background edges.
  - **Landscape Photos**: Displayed full screen in aspect-fit without cropping or stretching.
- **Sync & Optimization Tool**: Run `./tools/sync-screensaver.sh` to automatically convert `.HEIC` files to JPEG, downsample to native 1080p, and sync to `/sdcard/Screensaver/` on the TV.

---

## 4. Ad-Blocking Private DNS (AdGuard)

Configured system-wide Private DNS via AdGuard:
```bash
adb shell settings put global private_dns_specifier dns.adguard-dns.com
adb shell settings put global private_dns_mode hostname
```

> **Note**: Private DNS blocks in-app banner ads and telemetry trackers across apps and browsers. It does not block YouTube video ads (which are served from the same streams as video content).

### How to Undo AdGuard DNS:
To switch back to standard opportunistic DNS:
```bash
adb shell settings put global private_dns_mode opportunistic
```

---

## 5. Rollback & Restore Guides

### A. Rollback to Saved Working Apple TV Launcher
If you customize the launcher again in the future and something breaks, run this single script to instantly rollback to this exact verified build:
```bash
bash backup/rollback-launcher.sh
```
This automatically reinstalls the saved working APK (`backup/saved_build/launcher-working.apk`), restores permissions, sets it as default Home, and launches it.

### B. Re-enable All Debloated Packages
To re-enable all 51 debloated manufacturer packages:
```bash
TV_HOST=192.168.1.237 bash backup/restore.sh
```

### C. Revert to Stock Factory Google TV Launcher
To return to the stock Google TV launcher:
```bash
adb -s 192.168.1.237:5555 shell pm enable --user 0 com.google.android.tvlauncher
adb -s 192.168.1.237:5555 shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity
```

---

## 6. Security Step: Closing Network ADB

Leaving network ADB open on port 5555 allows any device on your local network to open a root/shell connection to your TV.

To close it securely:
1. On your TV remote, go to **Settings > Device Preferences > Developer options**.
2. Turn **USB debugging** OFF.
3. Turn **Network debugging** / **ADB over network** OFF (if present).
4. Select **Revoke USB debugging authorizations**.
5. Reboot your TV.
6. Verify from your terminal that port 5555 is closed:
   ```bash
   python3 tools/adb-closed.py 192.168.1.237
   ```
