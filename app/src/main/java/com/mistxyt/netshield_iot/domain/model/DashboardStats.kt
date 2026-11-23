package com.mistxyt.netshield_iot.domain.model

data class DashboardStats(
    val totalRequests: Long = 0L,
    val blockedRequests: Long = 0L,
    val allowedRequests: Long = 0L,
    val uniqueDevices: Int = 0
) {
    val blockedPercentage: Float
        get() = if (totalRequests > 0) (blockedRequests.toFloat() / totalRequests.toFloat()) * 100f else 0f

    val allowedPercentage: Float
        get() = if (totalRequests > 0) (allowedRequests.toFloat() / totalRequests.toFloat()) * 100f else 0f
}
