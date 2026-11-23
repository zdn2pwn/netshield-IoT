package com.mistxyt.netshield_iot.core.util

data class DeviceFingerprint(
    val detectedModel: String,
    val deviceType: String
)

object DeviceFingerprintHelper {

    fun detect(clientIp: String, domain: String): DeviceFingerprint? {
        val cleanIp = clientIp.trim()
        if (cleanIp.endsWith(".1")) {
            return DeviceFingerprint(
                detectedModel = "Hotspot Gateway / Router",
                deviceType = "ROUTER"
            )
        }

        val d = domain.lowercase().trim()

        // Apple Ecosystem (iOS / iPadOS / macOS)
        if (d.contains("apple.com") || d.contains("icloud.com") || 
            d.contains("aaplimg.com") || d.contains("mzstatic.com") || 
            d.contains("itunes.apple.com") || d.contains("smoot.apple.com") ||
            d.contains("xp.apple.com") || d.contains("mask.icloud.com")) {
            return DeviceFingerprint(
                detectedModel = "Apple iPhone / iOS",
                deviceType = "SMARTPHONE"
            )
        }

        // Windows Ecosystem (PC / Laptop)
        if (d.contains("windowsupdate.com") || d.contains("msftncsi.com") || 
            d.contains("microsoft.com") || d.contains("live.com") || 
            d.contains("bing.com") || d.contains("office.com")) {
            return DeviceFingerprint(
                detectedModel = "Windows PC / Laptop",
                deviceType = "LAPTOP"
            )
        }

        // Android Ecosystem
        if (d.contains("connectivitycheck.android.com") || d.contains("play.googleapis.com") || 
            d.contains("gvt1.com") || d.contains("android.clients.google.com")) {
            return DeviceFingerprint(
                detectedModel = "Android Device",
                deviceType = "SMARTPHONE"
            )
        }

        return null
    }
}
