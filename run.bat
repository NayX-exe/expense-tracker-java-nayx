@echo off
REM ─── Expense Tracker — Build & Run Script (Windows) ─────────────────────────
echo Building Expense Tracker...

set SRC_DIR=src\main\java
set OUT_DIR=out
set MAIN_CLASS=com.expensetracker.App

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM Find and compile all Java files
echo Compiling...
for /R "%SRC_DIR%" %%f in (*.java) do (
    javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" "%%f" 2>nul
)

REM Better: compile all at once
dir /S /B "%SRC_DIR%\*.java" > sources.txt
javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" @sources.txt
del sources.txt

echo Build successful!
echo Launching Expense Tracker...
java -cp "%OUT_DIR%" %MAIN_CLASS%
pause