#!/usr/bin/env bash
# Copyright (c) 2026 Ilham
# All Rights Reserved.
# This from project cleanyourtv by Gathin32 custom by Ilham Asyari
# Pushes small icons from local icons/ folder to /sdcard/Icons/ on the TV.
set -e

TV_HOST="${TV_HOST:-192.168.1.237}"
TV_PORT="${TV_PORT:-5555}"
DEVICE="${TV_HOST}:${TV_PORT}"

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ICONS_DIR="$DIR/icons"

if [ ! -d "$ICONS_DIR" ]; then
    mkdir -p "$ICONS_DIR"
fi

echo "==> Syncing small icons from $ICONS_DIR to TV ($DEVICE:/sdcard/Icons)..."
adb -s "$DEVICE" shell mkdir -p /sdcard/Icons
adb -s "$DEVICE" push "$ICONS_DIR/." /sdcard/Icons/

echo "==> Done! Files currently on TV /sdcard/Icons:"
adb -s "$DEVICE" shell ls -la /sdcard/Icons
