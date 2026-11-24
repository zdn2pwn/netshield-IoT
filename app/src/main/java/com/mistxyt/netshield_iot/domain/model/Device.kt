package com.mistxyt.netshield_iot.domain.model

enum class DeviceType {
    SMARTPHONE,
    TABLET,
    LAPTOP,
    ROUTER,
    IOT,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): DeviceType {
            return when (value?.trim()?.uppercase()) {
                "SMARTPHONE", "PHONE" -> SMARTPHONE
                "TABLET" -> TABLET
                "LAPTOP", "PC", "COMPUTER" -> LAPTOP
                "ROUTER", "GATEWAY" -> ROUTER
                "IOT" -> IOT
                else -> SMARTPHONE
            }
        }
    }
}

data class Device(
    val id: String,
    val ipAddress: String,
    val macAddress: String? = null,
    val requestCount: Int = 0,
    val blockedCount: Int = 0,
    val lastActivity: Long = System.currentTimeMillis(),
    val customName: String? = null,
    val deviceType: DeviceType = DeviceType.SMARTPHONE,
    val detectedModel: String? = null,
    val isInternetPaused: Boolean = false
) {
    val displayName: String
        get() = customName?.takeIf { it.isNotBlank() }
            ?: detectedModel?.takeIf { it.isNotBlank() }
            ?: (if (ipAddress.endsWith(".1")) "Host Router / Hotspot" else "Wi-Fi Device ($ipAddress)")

    val allowedCount: Int
        get() = (requestCount - blockedCount).coerceAtLeast(0)

    val blockRatioPercentage: Int
        get() = if (requestCount > 0) ((blockedCount.toDouble() / requestCount) * 100).toInt() else 0
}

