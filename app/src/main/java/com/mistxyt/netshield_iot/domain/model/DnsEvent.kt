package com.mistxyt.netshield_iot.domain.model

enum class DnsAction {
    ALLOW,
    BLOCK;

    companion object {
        fun fromString(value: String?): DnsAction {
            return when (value?.trim()?.uppercase()) {
                "BLOCK", "BLOCKED", "DENY" -> BLOCK
                else -> ALLOW
            }
        }
    }
}

data class DnsEvent(
    val id: String,
    val timestamp: Long,
    val clientIp: String,
    val domain: String,
    val queryType: String = "A",
    val action: DnsAction,
    val category: String = "General",
    val source: String = "ESP32 Core"
)
