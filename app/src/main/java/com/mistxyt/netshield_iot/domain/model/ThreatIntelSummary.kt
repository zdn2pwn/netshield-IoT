package com.mistxyt.netshield_iot.domain.model

data class ThreatIntelSummary(
    val activeThreatSources: Int = 3,
    val totalBlockedDomains: Int = 14250,
    val lastFeedUpdate: Long = System.currentTimeMillis() - 3600000L,
    val engineVersion: String = "v1.4.2-ESP32-CORE"
)
