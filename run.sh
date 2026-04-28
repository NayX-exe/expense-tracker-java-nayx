#!/bin/bash
# ─── Expense Tracker — Build & Run Script ─────────────────────────────────────
set -e
echo "Building Expense Tracker..."

SRC_DIR="src/main/java"
OUT_DIR="out"
MAIN_CLASS="com.expensetracker.App"

# Auto-detect javac (handles macOS, Ubuntu, Windows WSL)
if command -v javac &>/dev/null; then
    JAVAC="javac"
    JAVA="java"
elif [ -n "$JAVA_HOME" ]; then
    JAVAC="$JAVA_HOME/bin/javac"
    JAVA="$JAVA_HOME/bin/java"
else
    # Try common JDK locations
    for d in /usr/lib/jvm/java-*/bin /usr/local/opt/openjdk/bin; do
        if [ -x "$d/javac" ]; then JAVAC="$d/javac"; JAVA="$d/java"; break; fi
    done
fi

if [ -z "$JAVAC" ]; then
    echo "ERROR: javac not found. Please install JDK 11+ and set JAVA_HOME."
    exit 1
fi

echo "Using: $JAVAC"
mkdir -p "$OUT_DIR"

# Compile all .java files
find "$SRC_DIR" -name "*.java" > /tmp/sources.txt
"$JAVAC" -d "$OUT_DIR" -sourcepath "$SRC_DIR" @/tmp/sources.txt

echo "Build successful!"
echo "Launching Expense Tracker..."
"$JAVA" -cp "$OUT_DIR" "$MAIN_CLASS"