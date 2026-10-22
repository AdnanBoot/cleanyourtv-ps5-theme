# Small Icons for PS5-style Launcher

Place your small app icons in this folder (`icons/`), then run:

```bash
bash tools/sync-icons.sh
```

This will automatically push the icons to `/sdcard/Icons/` on your TV.

### Supported File Formats:
- `.png` (recommended, transparent PNG)
- `.webp`
- `.jpg` / `.jpeg`

### Supported File Naming:
You can name the icon file using the app name or package name:
- `youtube.png` (or `com.google.android.youtube.tv.png`)
- `appletv.png` or `apple.png` (or `com.apple.atve.tcl.appletv.androidos.playstore.png`)
- `netflix.png` (or `com.netflix.ninja.png`)
- `hbomax.png` or `hbo.png` or `max.png` (or `com.wbd.stream.png`)
- `primevideo.png` or `prime.png` (or `com.amazon.amazonvideo.livingroom.png`)
- `disney.png` or `disneyplus.png` (or `com.disney.plus.png`)
- `vidio.png` (or `com.vidio.android.tv.png`)
- `playstore.png` or `googleplay.png` (or `com.android.vending.png`)
- `ott.png` or `ottnavigator.png` (or `appnovatica.tv.png`)

When an app is **unfocused**, it will shrink down and show this small icon.
When an app is **focused**, it will zoom in and show the full existing logo/banner.
