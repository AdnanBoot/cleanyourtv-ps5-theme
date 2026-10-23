#!/usr/bin/env bash
# Copyright (c) 2026 Ilham
# All Rights Reserved.
# This from project cleanyourtv by Gathin32 custom by Ilham Asyari
# ==============================================================================
# CleanYourTV - Rollback & Uninstaller Script
# Restores the TV back to its factory default stock launcher.
# ==============================================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "${CYAN}${BOLD}"
echo "=========================================================="
echo "   🔄 CleanYourTV - Launcher Rollback & Uninstaller"
echo "=========================================================="
echo -e "${NC}"

# 1. Check for ADB
if ! command -v adb &> /dev/null; then
    if [ -f "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
        export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
    elif [ -f "$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" ]; then
        export PATH="$LOCALAPPDATA/Android/Sdk/platform-tools:$PATH"
    else
        echo -e "${RED}[ERROR] ADB is not in PATH.${NC}"
        exit 1
    fi
fi

# 2. Check connected devices or ask for IP
CONNECTED_DEVICES=$(adb devices | grep -E "device$" | awk '{print $1}' || true)
TARGET_DEVICE=""

if [ -n "$CONNECTED_DEVICES" ]; then
    TARGET_DEVICE=$(echo "$CONNECTED_DEVICES" | head -n 1)
    echo -e "${GREEN}✓ Using connected device: $TARGET_DEVICE${NC}"
else
    read -p "Enter your TV IP address (e.g. 192.168.1.150): " TV_IP_INPUT
    TV_IP=$(echo "$TV_IP_INPUT" | sed 's/:5555//')
    adb connect "$TV_IP:5555"
    TARGET_DEVICE="$TV_IP:5555"
fi

echo -e "\n${BLUE}Re-enabling known stock TV launchers...${NC}"
# Re-enable standard Google TV / Android TV / OEM launchers if disabled
STOCK_LAUNCHERS=(
    "com.google.android.apps.tv.launcherx"
    "com.google.android.tvlauncher"
    "com.tcl.tvmodels"
    "com.tcl.home"
    "com.mitv.tvhome"
    "com.amazon.tv.launcher"
)

for pkg in "${STOCK_LAUNCHERS[@]}"; do
    adb -s "$TARGET_DEVICE" shell pm enable "$pkg" 2>/dev/null || true
done

echo -e "${BLUE}Restoring default screensaver...${NC}"
adb -s "$TARGET_DEVICE" shell settings put secure screensaver_components com.google.android.backdrop/.Backdrop 2>/dev/null || true

echo -e "${BLUE}Uninstalling CleanYourTV launcher (com.gathin.tvhome)...${NC}"
adb -s "$TARGET_DEVICE" uninstall com.gathin.tvhome || true

echo -e "${BLUE}Sending HOME key to activate default launcher...${NC}"
adb -s "$TARGET_DEVICE" shell input keyevent KEYCODE_HOME

echo -e "\n${GREEN}${BOLD}=========================================================="
echo "   ✓ Rollback Complete!"
echo "   Your TV has been restored to its original stock launcher."
echo "==========================================================${NC}"
