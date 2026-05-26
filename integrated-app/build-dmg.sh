#!/bin/bash
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
DIR="$(cd "$(dirname "$0")" && pwd)"
BASE="$(cd "$DIR/.." && pwd)"
JFX="$DIR/lib/javafx-sdk-21.0.2/lib"
SRC="$DIR/src"
OUT="$DIR/out"
STAGING="$DIR/staging"
RELEASE="$DIR/release"
JAR="$DIR/grafapp.jar"

set -e

echo "========================================"
echo "  Building Graf Algoritmik DMG"
echo "========================================"
echo ""

# Step 1: Compile with JDK 21
echo "[1/4] Compiling source files..."
rm -rf "$OUT"
mkdir -p "$OUT"
"$JAVA_HOME/bin/javac" -d "$OUT" -sourcepath "$SRC" \
  --module-path "$JFX" \
  --add-modules javafx.controls \
  "$SRC/com/grafapp/Main.java" \
  "$SRC/com/grafapp/model/"*.java \
  "$SRC/com/grafapp/algorithm/"*.java \
  "$SRC/com/grafapp/algorithm/impl/"*.java \
  "$SRC/com/grafapp/layout/"*.java \
  "$SRC/com/grafapp/visualization/"*.java \
  "$SRC/com/grafapp/ui/"*.java \
  "$SRC/com/grafapp/util/"*.java
echo "  Done."

# Step 2: Create JAR (using JDK 21 jar for consistency)
echo "[2/4] Creating JAR..."
cd "$OUT"
"$JAVA_HOME/bin/jar" cf "$JAR" .
echo "  Done."

# Step 3: Stage resources
echo "[3/4] Staging resources..."
mkdir -p "$STAGING"
cp "$JAR" "$STAGING/"
cp -r "$DIR/styles" "$STAGING/"
cp -r "$BASE/data" "$STAGING/"
echo "  Done."

# Step 4: Build DMG with jpackage
echo "[4/4] Running jpackage to create DMG..."
rm -rf "$RELEASE"
mkdir -p "$RELEASE"
"$JAVA_HOME/bin/jpackage" \
  --name "Graf Algoritmik" \
  --input "$STAGING" \
  --main-jar grafapp.jar \
  --main-class com.grafapp.Main \
  --module-path "$JFX" \
  --add-modules javafx.controls \
  --type dmg \
  --dest "$RELEASE" \
  --mac-package-identifier com.grafapp.main \
  --java-options "-Xmx1G"
echo "  Done."

# Cleanup
rm -rf "$STAGING" "$JAR"

echo ""
echo "========================================"
echo "  DMG created: $RELEASE"
ls -lh "$RELEASE"/*.dmg
echo "========================================"
