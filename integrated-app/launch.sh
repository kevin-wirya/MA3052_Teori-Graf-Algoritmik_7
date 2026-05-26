#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"

cd "$DIR/.."

java --module-path "$DIR/lib/javafx-sdk-21.0.2/lib" \
  --add-modules javafx.controls \
  -cp "$DIR/out" \
  com.grafapp.Main
