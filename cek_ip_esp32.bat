@echo off
chcp 65001 > nul
title NETSHIELD IoT - ESP32 IP Finder ^& Live Monitor

setlocal

set "SCRIPT_PATH=%~dp0scripts\cek_ip.ps1"
if not exist "%SCRIPT_PATH%" (
    set "SCRIPT_PATH=%~dp0cek_ip_esp32.ps1"
)

echo.
echo ============================================================
echo   NETSHIELD IoT - ESP32 IP Finder ^& Live Monitor
echo ============================================================
echo.
echo Sebelum memulai, apakah Anda ingin mengganti kredensial Wi-Fi
echo (SSID dan Password) yang akan digunakan ESP32?
echo.
set /p CHANGE_WIFI="Ganti kredensial Wi-Fi? (y/N): "
if /I "%CHANGE_WIFI%"=="y" (
    set /p WIFI_SSID="Masukkan SSID Wi-Fi baru: "
    set /p WIFI_PASS="Masukkan Password Wi-Fi baru: "
) else (
    set "WIFI_SSID="
    set "WIFI_PASS="
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_PATH%" -WifiSsid "%WIFI_SSID%" -WifiPassword "%WIFI_PASS%"

if errorlevel 1 (
    echo.
    echo Tekan sembarang tombol untuk keluar...
    pause > nul
)

endlocal
