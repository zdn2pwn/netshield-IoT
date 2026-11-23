package com.mistxyt.netshield_iot.data.repository

import com.mistxyt.netshield_iot.data.demo.DemoDataProvider
import com.mistxyt.netshield_iot.data.local.NetShieldDatabase
import com.mistxyt.netshield_iot.data.local.entity.DnsEventEntity
import com.mistxyt.netshield_iot.data.local.preferences.NetShieldPreferences
import com.mistxyt.netshield_iot.data.remote.NetworkModule
import com.mistxyt.netshield_iot.data.remote.websocket.NetShieldWebSocketClient
import com.mistxyt.netshield_iot.data.remote.websocket.WebSocketState
import com.mistxyt.netshield_iot.domain.model.*
import com.mistxyt.netshield_iot.domain.repository.AppSettings
import com.mistxyt.netshield_iot.domain.repository.DnsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DnsRepositoryImpl(
    private val database: NetShieldDatabase,
    private val preferences: NetShieldPreferences,
    private val webSocketClient: NetShieldWebSocketClient,
    private val scope: CoroutineScope
) : DnsRepository {

    private val dnsEventDao = database.dnsEventDao()

    private val _liveStats = MutableStateFlow(DashboardStats())
    private val _systemStatus = MutableStateFlow(
        SystemStatus(
            online = false,
            dnsEngineStatus = "STANDBY",
            websocketConnected = false,
            uptimeSeconds = 0L,
            lastSyncTimestamp = 0L,
            isDemoMode = false
        )
    )
    private val _threatIntel = MutableStateFlow(
        ThreatIntelSummary(
            activeThreatSources = 0,
            totalBlockedDomains = 0,
            lastFeedUpdate = 0L,
            engineVersion = "NetShield ESP32 Core"
        )
    )
    private val _trafficAnalytics = MutableStateFlow<List<TrafficPoint>>(emptyList())

    private var currentSettings: AppSettings = AppSettings()

    init {
        scope.launch {
            preferences.appSettingsFlow.collect { settings ->
                currentSettings = settings
                if (settings.isDemoMode) {
                    webSocketClient.disconnect()
                    _systemStatus.value = DemoDataProvider.getSystemStatus()
                    _liveStats.value = DemoDataProvider.getDashboardStats()
                    _threatIntel.value = DemoDataProvider.getThreatIntel()
                    _trafficAnalytics.value = DemoDataProvider.getTrafficPoints()
                } else {
                    webSocketClient.connect(settings.esp32Ip, settings.wsPort)
                    refreshAll()
                }
            }
        }

        // Listen to incoming live WebSocket events and save to local DB safely on IO dispatcher
        scope.launch(Dispatchers.IO) {
            webSocketClient.incomingEvents.collect { event ->
                dnsEventDao.insertEvent(DnsEventEntity.fromDomain(event))
                // Increment dynamic stats
                val cur = _liveStats.value
                val isBlocked = event.action == DnsAction.BLOCK
                _liveStats.value = cur.copy(
                    totalRequests = cur.totalRequests + 1,
                    blockedRequests = if (isBlocked) cur.blockedRequests + 1 else cur.blockedRequests,
                    allowedRequests = if (!isBlocked) cur.allowedRequests + 1 else cur.allowedRequests
                )
            }
        }

        // Track WebSocket connection state in SystemStatus
        scope.launch {
            webSocketClient.connectionState.collect { state ->
                val isConnected = state == WebSocketState.CONNECTED
                val cur = _systemStatus.value
                if (!currentSettings.isDemoMode) {
                    _systemStatus.value = cur.copy(
                        websocketConnected = isConnected,
                        online = isConnected,
                        dnsEngineStatus = if (isConnected) "ACTIVE" else "DISCONNECTED"
                    )
                }
            }
        }
    }

    override fun getStats(): Flow<DashboardStats> = _liveStats.asStateFlow()

    override fun getSystemStatus(): Flow<SystemStatus> = _systemStatus.asStateFlow()

    override fun getThreatIntel(): Flow<ThreatIntelSummary> = _threatIntel.asStateFlow()

    override fun getTrafficAnalytics(): Flow<List<TrafficPoint>> = _trafficAnalytics.asStateFlow()

    override fun getRecentEvents(limit: Int): Flow<List<DnsEvent>> {
        return dnsEventDao.getRecentEvents(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeLiveTraffic(): Flow<DnsEvent> {
        return if (currentSettings.isDemoMode) {
            DemoDataProvider.createSimulatedTrafficFlow().onEach { event ->
                withContext(Dispatchers.IO) {
                    dnsEventDao.insertEvent(DnsEventEntity.fromDomain(event))
                }
            }
        } else {
            webSocketClient.incomingEvents
        }
    }

    override fun getWebSocketConnectionState(): Flow<Boolean> {
        return if (currentSettings.isDemoMode) {
            flowOf(true)
        } else {
            webSocketClient.connectionState.map { it == WebSocketState.CONNECTED }
        }
    }

    override suspend fun refreshAll(): Result<Unit> = withContext(Dispatchers.IO) {
        if (currentSettings.isDemoMode) {
            _systemStatus.value = DemoDataProvider.getSystemStatus()
            _liveStats.value = DemoDataProvider.getDashboardStats()
            _trafficAnalytics.value = DemoDataProvider.getTrafficPoints()
            return@withContext Result.success(Unit)
        }

        try {
            val api = NetworkModule.getApiService(currentSettings.esp32Ip, currentSettings.restPort)
            
            // 1. Fetch Status
            val statusResp = api.getSystemStatus()
            if (statusResp.isSuccessful && statusResp.body() != null) {
                val dto = statusResp.body()!!
                _systemStatus.value = SystemStatus(
                    online = dto.online ?: true,
                    ipAddress = dto.ipAddress ?: currentSettings.esp32Ip,
                    dnsEngineStatus = dto.dnsEngineStatus ?: "ACTIVE",
                    websocketConnected = webSocketClient.connectionState.value == WebSocketState.CONNECTED,
                    uptimeSeconds = dto.uptime ?: 0L,
                    lastSyncTimestamp = dto.lastSync ?: System.currentTimeMillis(),
                    freeHeapBytes = dto.freeHeap,
                    isDemoMode = false
                )
            }

            // 2. Fetch Stats
            val statsResp = api.getStats()
            if (statsResp.isSuccessful && statsResp.body() != null) {
                val dto = statsResp.body()!!
                _liveStats.value = DashboardStats(
                    totalRequests = dto.totalRequests ?: 0L,
                    blockedRequests = dto.blockedRequests ?: 0L,
                    allowedRequests = dto.allowedRequests ?: 0L,
                    uniqueDevices = dto.uniqueDevices ?: 0
                )
            }

            // 3. Fetch Traffic Analytics
            val trafficResp = api.getTrafficAnalytics()
            if (trafficResp.isSuccessful && trafficResp.body() != null) {
                val list = trafficResp.body()!!.map { pointDto ->
                    TrafficPoint(
                        timestamp = pointDto.timestamp ?: System.currentTimeMillis(),
                        label = pointDto.label ?: "--:--",
                        totalQueries = pointDto.totalQueries ?: 0,
                        blockedQueries = pointDto.blockedQueries ?: 0
                    )
                }
                _trafficAnalytics.value = list
            }

            // 4. Fetch Recent Traffic into DB
            val recentResp = api.getRecentTraffic(30)
            if (recentResp.isSuccessful && recentResp.body() != null) {
                val entities = recentResp.body()!!.map { evtDto ->
                    DnsEventEntity(
                        id = evtDto.id ?: "evt-${System.currentTimeMillis()}-${(100..999).random()}",
                        timestamp = evtDto.timestamp ?: System.currentTimeMillis(),
                        clientIp = evtDto.clientIp ?: "192.168.1.1",
                        domain = evtDto.domain ?: "",
                        queryType = evtDto.queryType ?: "A",
                        action = evtDto.action ?: "ALLOW",
                        category = evtDto.category ?: "General",
                        source = evtDto.source ?: "ESP32"
                    )
                }.filter { it.domain.isNotBlank() }
                dnsEventDao.insertEvents(entities)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            _systemStatus.value = _systemStatus.value.copy(
                online = false,
                websocketConnected = false,
                dnsEngineStatus = "OFFLINE",
                isDemoMode = false
            )
            Result.failure(e)
        }
    }

    override suspend fun clearEventHistory() {
        withContext(Dispatchers.IO) {
            dnsEventDao.clearEvents()
        }
    }
}
