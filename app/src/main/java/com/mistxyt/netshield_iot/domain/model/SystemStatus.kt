package com.mistxyt.netshield_iot.domain.model

data class SystemStatus(
    val online: Boolean = false,
    val ipAddress: String = "192.168.1.10",
    val dnsEngineStatus: String = "ACTIVE",
    val websocketConnected: Boolean = false,
    val uptimeSeconds: Long = 0L,
    val lastSyncTimestamp: Long = 0L,
    val freeHeapBytes: Long? = null,
    val isDemoMode: Boolean = false
) {
    val formattedUptime: String
        get() {
            val hours = uptimeSeconds / 3600
            val minutes = (uptimeSeconds % 3600) / 60
            val seconds = uptimeSeconds % 60
            return "${hours}h ${minutes}m ${seconds}s"
        }
}
