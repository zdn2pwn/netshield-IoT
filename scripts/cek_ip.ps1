# ======================================================================
#       NETSHIELD IoT - ESP32-C3 SERIAL MONITOR & IP FINDER
# ======================================================================

$Host.UI.RawUI.WindowTitle = 'NETSHIELD IoT - ESP32 IP Finder & Live Monitor'

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          NETSHIELD IoT - ESP32-C3 SERIAL MONITOR & IP FINDER         " -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Sedang mendeteksi port COM ESP32-C3..." -ForegroundColor White

function Get-TargetPort {
    # 1. Cek port COM yang aktif
    $ports = [System.IO.Ports.SerialPort]::GetPortNames()
    if ($ports -contains 'COM3') { return 'COM3' }
    if ($ports.Count -gt 0) { return $ports[0] }

    # 2. Cek lewat PnP Device jika belum ketemu
    $dev = Get-PnpDevice -Class Ports -ErrorAction SilentlyContinue | Where-Object { 
        $_.FriendlyName -match '\((COM\d+)\)' -and $_.Status -eq 'OK' 
    }
    if ($dev) {
        if ($dev.FriendlyName -match '\((COM\d+)\)') { return $matches[1] }
    }
    return $null
}

$comPort = Get-TargetPort
while (-not $comPort) {
    Write-Host "[!] ESP32 belum terdeteksi. Silakan tancapkan kabel USB ESP32 ke laptop..." -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    $comPort = Get-TargetPort
}

Write-Host "[OK] ESP32 terdeteksi pada port: $comPort" -ForegroundColor Green
Write-Host "----------------------------------------------------------------------" -ForegroundColor Gray
Write-Host "Membuka Serial Monitor (115200 baud)..." -ForegroundColor Gray
Write-Host "Tekan CTRL+C di terminal ini kapan saja untuk keluar." -ForegroundColor DarkGray
Write-Host "----------------------------------------------------------------------" -ForegroundColor Gray
Write-Host ""

$serial = $null
try {
    $serial = New-Object System.IO.Ports.SerialPort $comPort, 115200
    $serial.DtrEnable = $true
    $serial.RtsEnable = $false
    $serial.ReadTimeout = 1000
    $serial.Open()

    # Kirim newline agar ESP32 langsung merespons dengan IP tanpa menunggu
    Start-Sleep -Milliseconds 200
    try { $serial.WriteLine("") } catch {}

    $ipFound = $false

    while ($true) {
        try {
            $line = $serial.ReadLine()
            if ($line) {
                $line = $line.Trim()

                if ($line -match "\[IP ESP32\]\s*->\s*([\d\.]+)") {
                    $ip = $matches[1]
                    Write-Host ""
                    Write-Host "====================================================" -ForegroundColor Green
                    Write-Host "   >>> ALAMAT IP ESP32: $ip <<<" -ForegroundColor Yellow
                    Write-Host "   Masukkan IP di atas pada Pengaturan Aplikasi Android" -ForegroundColor Green
                    Write-Host "   Port REST: 80  |  Port WebSocket: 81" -ForegroundColor Cyan
                    Write-Host "====================================================" -ForegroundColor Green
                    Write-Host ""
                    $ipFound = $true
                } elseif ($line.Contains("[BLOCKED]")) {
                    Write-Host " [BLOKIR]  $line" -ForegroundColor Red
                } elseif ($line.Contains("[ALLOWED]")) {
                    Write-Host " [IZINKAN] $line" -ForegroundColor Green
                } elseif ($line.Contains("[OK]")) {
                    Write-Host " [SISTEM]  $line" -ForegroundColor Cyan
                } elseif ($line.Length -gt 0) {
                    Write-Host "           $line" -ForegroundColor White
                }
            }
        } catch [System.TimeoutException] {
            # Setiap timeout 1 detik jika IP belum muncul, picu kembali dengan newline
            if (-not $ipFound) {
                try { $serial.WriteLine("") } catch {}
            }
        }
        Start-Sleep -Milliseconds 10
    }
} catch {
    Write-Host ""
    Write-Host "[ERROR] Gagal mengakses port $comPort : $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Tips Penyelesaian:" -ForegroundColor Yellow
    Write-Host " 1. Pastikan Serial Monitor di Arduino IDE sudah DITUTUP." -ForegroundColor Yellow
    Write-Host " 2. Cabut lalu colokkan kembali kabel USB ESP32 jika port terkunci." -ForegroundColor Yellow
    Write-Host ""
} finally {
    if ($serial -and $serial.IsOpen) {
        $serial.Close()
        Write-Host "[INFO] Port $comPort ditutup dengan aman." -ForegroundColor Gray
    }
}
