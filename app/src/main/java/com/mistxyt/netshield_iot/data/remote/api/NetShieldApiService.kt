package com.mistxyt.netshield_iot.data.remote.api

import com.mistxyt.netshield_iot.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface NetShieldApiService {

    @GET("api/v1/health")
    suspend fun getHealth(): Response<HealthDto>

    @GET("api/v1/status")
    suspend fun getSystemStatus(): Response<SystemStatusDto>

    @GET("api/v1/stats")
    suspend fun getStats(): Response<DashboardStatsDto>

    @GET("api/v1/traffic")
    suspend fun getTrafficAnalytics(): Response<List<TrafficPointDto>>

    @GET("api/v1/traffic/recent")
    suspend fun getRecentTraffic(
        @Query("limit") limit: Int = 20
    ): Response<List<DnsEventDto>>

    @GET("api/v1/blocklist")
    suspend fun getBlocklist(): Response<List<BlockedDomainDto>>

    @POST("api/v1/blocklist")
    suspend fun addBlockedDomain(
        @Body request: AddBlocklistRequestDto
    ): Response<BlockedDomainDto>

    @DELETE("api/v1/blocklist/{id}")
    suspend fun deleteBlockedDomain(
        @Path("id") id: String
    ): Response<DeleteResponseDto>

    @POST("api/v1/blocklist/sync")
    suspend fun syncBlocklist(): Response<SyncResponseDto>

    @GET("api/v1/devices")
    suspend fun getDevices(): Response<List<DeviceDto>>
}
