@echo off
if not exist bin mkdir bin
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
set err=%errorlevel%
del sources.txt
exit /b %err%
