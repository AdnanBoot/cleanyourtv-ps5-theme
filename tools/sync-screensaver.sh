#!/usr/bin/env bash
# Wrapper to run the Python screensaver optimizer & sync tool
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
python3 "$DIR/sync-screensaver.py"
