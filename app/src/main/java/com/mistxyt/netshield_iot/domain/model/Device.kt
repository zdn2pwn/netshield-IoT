package com.mistxyt.netshield_iot.domain.model

data class Device(
    val id: String,
    val ipAddress: String,
    val macAddress: String? = null,
    val requestCount: Int = 0,
    val blockedCount: Int = 0,
    val lastActivity: Long = System.currentTimeMillis()
) {
    val allowedCount: Int
        get() = (requestCount - blockedCount).coerceAtLeast(0)

    val blockRatioPercentage: Int
        get() = if (requestCount > 0) ((blockedCount.toDouble() / requestCount) * 100).toInt() else 0
}
