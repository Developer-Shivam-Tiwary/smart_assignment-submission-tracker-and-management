@echo off
title SmartAssignment Backend Server compiler and launcher
echo ==================================================
echo  Compiling SmartAssignment Backend...
echo ==================================================

if not exist bin mkdir bin

REM Find all java files and compile them
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    del sources.txt
    pause
    exit /b %errorlevel%
)
del sources.txt
echo [SUCCESS] Compilation complete.

REM Copy resources
copy /y src\main\resources\database.properties bin\ >nul

echo ==================================================
echo  Starting AppServer on port 8080...
echo ==================================================
java -cp "bin;lib/*" com.smartassignment.server.AppServer
pause
