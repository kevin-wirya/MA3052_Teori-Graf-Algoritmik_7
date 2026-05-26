#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"

if [ ! -d "$DIR/out" ] || [ -z "$(ls -A "$DIR/out" 2>/dev/null)" ]; then
  echo "Build not found. Compiling..."
  bash "$DIR/build.sh"
  if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Build failed. Aborting."
    exit 1
  fi
fi

cd "$DIR/.."

java --module-path "$DIR/lib/javafx-sdk-21.0.2/lib" \
  --add-modules javafx.controls \
  -cp "$DIR/out" \
  com.grafapp.Main
