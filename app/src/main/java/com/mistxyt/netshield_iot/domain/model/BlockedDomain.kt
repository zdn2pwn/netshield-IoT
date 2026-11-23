package com.mistxyt.netshield_iot.domain.model

data class BlockedDomain(
    val id: String,
    val domain: String,
    val category: String = "Custom",
    val source: String = "Manual Entry",
    val createdAt: Long = System.currentTimeMillis(),
    val enabled: Boolean = true
)
