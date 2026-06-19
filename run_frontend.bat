@echo off
title SmartAssignment Frontend Server
echo ==================================================
echo  Starting Local Web Server for React Frontend...
echo  Open http://localhost:3000 in your browser.
echo ==================================================
cd frontend
python -m http.server 3000
pause
