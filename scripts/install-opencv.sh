#!/usr/bin/env bash
set -euo pipefail

# Install system OpenCV libraries and configure JVM binding.
# For Debian/Ubuntu.

sudo apt-get update
sudo apt-get install -y \
  libopencv-dev \
  libgtk2.0-0 \
  libglib2.0-0 \
  libsm6 \
  libxrender1 \
  libxext6

if ! grep -q "JAVA_TOOL_OPTIONS.*java.library.path" "$HOME/.bashrc"; then
  echo 'export JAVA_TOOL_OPTIONS="-Djava.library.path=/usr/lib/x86_64-linux-gnu"' >> "$HOME/.bashrc"
fi

echo "OpenCV installed. Reopen terminal or run: source ~/.bashrc"
