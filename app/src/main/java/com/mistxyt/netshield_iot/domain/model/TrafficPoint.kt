package com.mistxyt.netshield_iot.domain.model

data class TrafficPoint(
    val timestamp: Long,
    val label: String,
    val totalQueries: Int,
    val blockedQueries: Int
) {
    val allowedQueries: Int
        get() = (totalQueries - blockedQueries).coerceAtLeast(0)
}
