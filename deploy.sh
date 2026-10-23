#!/bin/bash
# Copyright (c) 2026 Ilham
# All Rights Reserved.
# This from project cleanyourtv by Gathin32 custom by Ilham Asyari
TV_IP="192.168.1.237:5555"
APK="launcher/app/build/outputs/apk/debug/app-deblouting-ps5.apk"

echo "Connecting to TV at $TV_IP..."
adb connect $TV_IP

if adb devices | grep -q "$TV_IP.*device"; then
    echo "Installing $APK..."
    adb install -r "$APK"
    adb shell appops set com.gathin.tvhome GET_USAGE_STATS allow
    echo "Starting launcher..."
    adb shell am start -n com.gathin.tvhome/.MainActivity
    echo "Done! Launcher successfully updated on TV."
else
    echo "TV is not reachable over ADB right now ($TV_IP). Please ensure the TV screen is on and connected to Wi-Fi."
fi
