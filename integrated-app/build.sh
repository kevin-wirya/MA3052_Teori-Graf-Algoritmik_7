#!/bin/bash
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
DIR="$(cd "$(dirname "$0")" && pwd)"
JFX_LIB="$DIR/lib/javafx-sdk-21.0.2/lib"
SRC="$DIR/src"
OUT="$DIR/out"

echo ""
echo "Compiling Graf Algoritmik..."
echo ""

mkdir -p "$OUT"

"$JAVA_HOME/bin/javac" -d "$OUT" -sourcepath "$SRC" \
  --module-path "$JFX_LIB" \
  --add-modules javafx.controls \
  "$SRC/com/grafapp/Main.java" \
  "$SRC/com/grafapp/model/"*.java \
  "$SRC/com/grafapp/algorithm/"*.java \
  "$SRC/com/grafapp/algorithm/impl/"*.java \
  "$SRC/com/grafapp/layout/"*.java \
  "$SRC/com/grafapp/visualization/"*.java \
  "$SRC/com/grafapp/ui/"*.java \
  "$SRC/com/grafapp/util/"*.java

if [ $? -ne 0 ]; then
  echo ""
  echo "[ERROR] Compilation FAILED!"
  exit 1
fi

echo ""
echo "[OK] Compilation successful!"
echo "Run with: ./launch.sh"
