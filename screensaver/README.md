# Screensaver Photos Directory

Drop your personal photos here to display them as a cinematic TV screensaver slideshow.

### Supported formats:
- Apple iPhone photos: `.HEIC`
- Standard images: `.JPG`, `.JPEG`, `.PNG`, `.WEBP`

### How it works:
- **100% Privacy**: Personal photos placed in this directory are gitignored and will never be committed to Git or pushed to GitHub.
- **Automatic Sync**: When you run `./install.sh`, it will detect any photos in this folder, optimize their resolution to 1080p, and transfer them to `/sdcard/Screensaver/` on your TV.
- **Default Screensaver Fallback**: If you don't add any photos (or leave this folder empty), CleanYourTV will automatically display its default screensaver (a floating clock and date drifting over an ambient color-shifting glow).
