@echo off
chcp 65001 > nul
title NETSHIELD IoT - ESP32 IP Finder ^& Live Monitor

set SCRIPT_PATH=%~dp0scripts\cek_ip.ps1
if not exist "%SCRIPT_PATH%" (
    set SCRIPT_PATH=%~dp0cek_ip_esp32.ps1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_PATH%"

if errorlevel 1 (
    echo.
    echo Tekan sembarang tombol untuk keluar...
    pause > nul
)
