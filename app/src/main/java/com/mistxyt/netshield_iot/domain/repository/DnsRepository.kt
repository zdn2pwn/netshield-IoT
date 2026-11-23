package com.mistxyt.netshield_iot.domain.repository

import com.mistxyt.netshield_iot.domain.model.DashboardStats
import com.mistxyt.netshield_iot.domain.model.DnsEvent
import com.mistxyt.netshield_iot.domain.model.SystemStatus
import com.mistxyt.netshield_iot.domain.model.ThreatIntelSummary
import com.mistxyt.netshield_iot.domain.model.TrafficPoint
import kotlinx.coroutines.flow.Flow

interface DnsRepository {
    fun getStats(): Flow<DashboardStats>
    fun getSystemStatus(): Flow<SystemStatus>
    fun getThreatIntel(): Flow<ThreatIntelSummary>
    fun getTrafficAnalytics(): Flow<List<TrafficPoint>>
    fun getRecentEvents(limit: Int = 20): Flow<List<DnsEvent>>
    fun getEventsByClientIp(clientIp: String, limit: Int = 100): Flow<List<DnsEvent>>
    fun observeLiveTraffic(): Flow<DnsEvent>
    fun getWebSocketConnectionState(): Flow<Boolean>
    suspend fun refreshAll(): Result<Unit>
    suspend fun clearEventHistory()
}
