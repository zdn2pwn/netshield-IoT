package com.mistxyt.netshield_iot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HealthDto(
    @SerializedName("status") val status: String?,
    @SerializedName("timestamp") val timestamp: Long?
)

data class SystemStatusDto(
    @SerializedName("online") val online: Boolean?,
    @SerializedName("ipAddress") val ipAddress: String?,
    @SerializedName("dnsEngineStatus") val dnsEngineStatus: String?,
    @SerializedName("uptime") val uptime: Long?,
    @SerializedName("lastSync") val lastSync: Long?,
    @SerializedName("freeHeap") val freeHeap: Long?
)

data class DashboardStatsDto(
    @SerializedName("totalRequests") val totalRequests: Long?,
    @SerializedName("blockedRequests") val blockedRequests: Long?,
    @SerializedName("allowedRequests") val allowedRequests: Long?,
    @SerializedName("uniqueDevices") val uniqueDevices: Int?
)

data class TrafficPointDto(
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("label") val label: String?,
    @SerializedName("totalQueries") val totalQueries: Int?,
    @SerializedName("blockedQueries") val blockedQueries: Int?
)

data class BlockedDomainDto(
    @SerializedName("id") val id: String?,
    @SerializedName("domain") val domain: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("createdAt") val createdAt: Long?,
    @SerializedName("enabled") val enabled: Boolean?
)

data class AddBlocklistRequestDto(
    @SerializedName("domain") val domain: String,
    @SerializedName("category") val category: String,
    @SerializedName("source") val source: String = "Manual Entry"
)

data class SyncResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("syncedRulesCount") val syncedRulesCount: Int?,
    @SerializedName("timestamp") val timestamp: Long?
)

data class DeleteResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?
)

data class DeviceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("ipAddress") val ipAddress: String?,
    @SerializedName("macAddress") val macAddress: String?,
    @SerializedName("requestCount") val requestCount: Int?,
    @SerializedName("blockedCount") val blockedCount: Int?,
    @SerializedName("lastActivity") val lastActivity: Long?
)

data class DnsEventDto(
    @SerializedName("id") val id: String?,
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("clientIp") val clientIp: String?,
    @SerializedName("domain") val domain: String?,
    @SerializedName("queryType") val queryType: String?,
    @SerializedName("action") val action: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("source") val source: String?
)
