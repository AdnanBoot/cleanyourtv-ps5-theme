#!/usr/bin/env bash
# Copyright (c) 2026 Ilham
# All Rights Reserved.
# This from project cleanyourtv by Gathin32 custom by Ilham Asyari
# ==============================================================================
# CleanYourTV - Interactive Installer & Setup Wizard
# Works on macOS, Linux, and Windows (Git Bash / WSL)
# No Agentic AI required - run directly from your Terminal / Command Prompt!
# ==============================================================================

set -e

# Terminal Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

echo -e "${CYAN}${BOLD}"
echo "=========================================================="
echo "   📺 CleanYourTV - PS5 Launcher custom by Ilham Asyari"
echo "=========================================================="
echo -e "${NC}"

# 1. Check for ADB
if ! command -v adb &> /dev/null; then
    # Check default macOS / Android SDK locations
    if [ -f "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
        export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
    elif [ -f "$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" ]; then
        export PATH="$LOCALAPPDATA/Android/Sdk/platform-tools:$PATH"
    else
        echo -e "${RED}[ERROR] ADB (Android Debug Bridge) is not installed or not in PATH.${NC}"
        echo -e "Please install platform-tools: https://developer.android.com/tools/releases/platform-tools"
        exit 1
    fi
fi

echo -e "${GREEN}✓ ADB found: $(which adb)${NC}\n"

# 2. Check for connected TV devices
CONNECTED_DEVICES=$(adb devices | grep -E "device$" | awk '{print $1}' || true)

if [ -n "$CONNECTED_DEVICES" ]; then
    echo -e "${YELLOW}Detected currently connected device(s):${NC}"
    echo "$CONNECTED_DEVICES"
    read -p "Use currently connected device? (Y/n): " USE_EXISTING
    USE_EXISTING=${USE_EXISTING:-Y}
    if [[ "$USE_EXISTING" =~ ^[Yy]$ ]]; then
        TARGET_DEVICE=$(echo "$CONNECTED_DEVICES" | head -n 1)
    fi
fi

# If no target device yet, ask for IP address
if [ -z "$TARGET_DEVICE" ]; then
    echo -e "${BOLD}Connect to your Android TV / Google TV over Wi-Fi:${NC}"
    echo "Make sure Developer Options & USB / Network Debugging are enabled on your TV."
    read -p "Enter your TV IP address (e.g. 192.168.1.150): " TV_IP_INPUT

    # Strip port if user typed it
    TV_IP=$(echo "$TV_IP_INPUT" | sed 's/:5555//')

    if [ -z "$TV_IP" ]; then
        echo -e "${RED}[ERROR] No IP address entered.${NC}"
        exit 1
    fi

    echo -e "\n${BLUE}Connecting to $TV_IP:5555...${NC}"
    adb connect "$TV_IP:5555"
    sleep 1

    if ! adb devices | grep -q "$TV_IP:5555.*device"; then
        echo -e "${RED}[ERROR] Could not connect to $TV_IP:5555.${NC}"
        echo "Tips:"
        echo "1. Verify your TV is turned ON and on the same Wi-Fi network."
        echo "2. Go to TV Settings -> Device Preferences -> Developer Options -> Network Debugging -> Turn ON."
        echo "3. Accept the 'Always allow from this computer' prompt on your TV screen if it appears."
        exit 1
    fi
    TARGET_DEVICE="$TV_IP:5555"
fi

echo -e "${GREEN}✓ Connected to TV: $TARGET_DEVICE${NC}\n"

# 3. Choose APK: Pre-built or Build from source
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PREBUILT_APK="$SCRIPT_DIR/launcher/app/build/outputs/apk/debug/app-deblouting-ps5.apk"
if [ ! -f "$PREBUILT_APK" ] && [ -f "$SCRIPT_DIR/launcher/app/build/outputs/apk/release/app-deblouting-ps5.apk" ]; then
    PREBUILT_APK="$SCRIPT_DIR/launcher/app/build/outputs/apk/release/app-deblouting-ps5.apk"
fi

APK_PATH=""
if [ -f "$PREBUILT_APK" ]; then
    echo -e "${BOLD}How would you like to install?${NC}"
    echo "1) Fast Install: Use existing pre-built APK ($(du -h "$PREBUILT_APK" | cut -f1)) [Default]"
    echo "2) Build from Source: Recompile launcher with Gradle"
    read -p "Enter choice [1 or 2]: " BUILD_CHOICE
    BUILD_CHOICE=${BUILD_CHOICE:-1}

    if [ "$BUILD_CHOICE" == "2" ]; then
        echo -e "\n${BLUE}Building APK with Gradle...${NC}"
        cd "$SCRIPT_DIR/launcher"
        ./gradlew assembleDebug
        cd "$SCRIPT_DIR"
        APK_PATH="$PREBUILT_APK"
    else
        APK_PATH="$PREBUILT_APK"
    fi
else
    echo -e "${BLUE}No pre-built APK found. Building from source with Gradle...${NC}"
    cd "$SCRIPT_DIR/launcher"
    ./gradlew assembleDebug
    cd "$SCRIPT_DIR"
    APK_PATH="$PREBUILT_APK"
fi

# 4. Install onto TV
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}[ERROR] APK file not found at $APK_PATH${NC}"
    exit 1
fi

echo -e "\n${BLUE}Installing CleanYourTV onto $TARGET_DEVICE...${NC}"
adb -s "$TARGET_DEVICE" install -r "$APK_PATH"

# 5. Grant Permissions & Launch
echo -e "${BLUE}Granting Usage Stats permission for screen time...${NC}"
adb -s "$TARGET_DEVICE" shell appops set com.gathin.tvhome GET_USAGE_STATS allow || true

# 6. Screensaver Setup
HAS_PHOTOS=$(find "$SCRIPT_DIR/screensaver" -maxdepth 1 -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.heic" -o -iname "*.webp" \) 2>/dev/null | head -n 1 || true)

if [ -n "$HAS_PHOTOS" ]; then
    echo -e "\n${YELLOW}Detected personal photos in screensaver/ folder.${NC}"
    read -p "Sync screensaver photos to TV (/sdcard/Screensaver/)? (Y/n): " SYNC_SCREENSAVER
    SYNC_SCREENSAVER=${SYNC_SCREENSAVER:-Y}
    if [[ "$SYNC_SCREENSAVER" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Optimizing and syncing screensaver photos for TV...${NC}"
        TV_H=$(echo "$TARGET_DEVICE" | cut -d: -f1)
        TV_P=$(echo "$TARGET_DEVICE" | cut -d: -f2)
        TV_HOST="$TV_H" TV_PORT="${TV_P:-5555}" python3 "$SCRIPT_DIR/tools/sync-screensaver.py" || true
    fi
else
    echo -e "\n${BLUE}ℹ No personal photos found in screensaver/ folder.${NC}"
    echo -e "CleanYourTV will use its default screensaver (drifting clock over ambient glow)."
fi

echo -e "\n${BLUE}Starting CleanYourTV Launcher on your TV...${NC}"
adb -s "$TARGET_DEVICE" shell am start -n com.gathin.tvhome/.MainActivity

echo -e "\n${GREEN}${BOLD}=========================================================="
echo "   🎉 Installation Complete!"
echo "   CleanYourTV is now running on your television."
echo "==========================================================${NC}"
echo -e "Press HOME on your TV remote to select CleanYourTV as your default launcher."
