package com.mistxyt.netshield_iot.data.demo

import com.mistxyt.netshield_iot.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

object DemoDataProvider {

    fun getInitialBlockedDomains(): List<BlockedDomain> {
        val now = System.currentTimeMillis()
        return listOf(
            BlockedDomain(
                id = "blk-1",
                domain = "tracker.adservice.google.com",
                category = "Ads & Trackers",
                source = "Threat Intel Feed",
                createdAt = now - 86400000L * 5,
                enabled = true
            ),
            BlockedDomain(
                id = "blk-2",
                domain = "crypto-miner-pool.xyz",
                category = "Malware",
                source = "Threat Intel Feed",
                createdAt = now - 86400000L * 4,
                enabled = true
            ),
            BlockedDomain(
                id = "blk-3",
                domain = "slot-gacor-online88.top",
                category = "Gambling",
                source = "Manual Entry",
                createdAt = now - 86400000L * 3,
                enabled = true
            ),
            BlockedDomain(
                id = "blk-4",
                domain = "secure-login-bank-verify.cc",
                category = "Phishing",
                source = "Threat Intel Feed",
                createdAt = now - 86400000L * 2,
                enabled = true
            ),
            BlockedDomain(
                id = "blk-5",
                domain = "adult-cam-network.xxx",
                category = "Adult Content",
                source = "Manual Entry",
                createdAt = now - 86400000L,
                enabled = true
            ),
            BlockedDomain(
                id = "blk-6",
                domain = "telemetry.smart-tv-ads.com",
                category = "Ads & Trackers",
                source = "Heuristic",
                createdAt = now - 43200000L,
                enabled = true
            ),
            BlockedDomain(
                id = "blk-7",
                domain = "trojan-dropper-c2.ru",
                category = "Malware",
                source = "Threat Intel Feed",
                createdAt = now - 21600000L,
                enabled = true
            ),
            BlockedDomain(
                id = "blk-8",
                domain = "poker-live-jackpot99.vip",
                category = "Gambling",
                source = "Manual Entry",
                createdAt = now - 10800000L,
                enabled = true
            )
        )
    }

    fun getInitialDevices(): List<Device> {
        val now = System.currentTimeMillis()
        return listOf(
            Device(
                id = "dev-101",
                ipAddress = "192.168.1.101",
                macAddress = "E4:5F:01:2D:8A:10",
                requestCount = 8420,
                blockedCount = 1340,
                lastActivity = now - 12000L
            ),
            Device(
                id = "dev-105",
                ipAddress = "192.168.1.105",
                macAddress = "3C:28:6D:71:B2:44",
                requestCount = 6890,
                blockedCount = 1820,
                lastActivity = now - 5000L
            ),
            Device(
                id = "dev-112",
                ipAddress = "192.168.1.112",
                macAddress = "70:2C:1F:B4:9C:08",
                requestCount = 5120,
                blockedCount = 780,
                lastActivity = now - 45000L
            ),
            Device(
                id = "dev-120",
                ipAddress = "192.168.1.120",
                macAddress = "84:CC:A8:8D:E1:92",
                requestCount = 2100,
                blockedCount = 40,
                lastActivity = now - 180000L
            ),
            Device(
                id = "dev-145",
                ipAddress = "192.168.1.145",
                macAddress = "DC:A6:32:04:77:2E",
                requestCount = 1430,
                blockedCount = 95,
                lastActivity = now - 320000L
            ),
            Device(
                id = "dev-160",
                ipAddress = "192.168.1.160",
                macAddress = "B0:4E:26:93:10:FB",
                requestCount = 890,
                blockedCount = 45,
                lastActivity = now - 600000L
            )
        )
    }

    fun getInitialRecentEvents(): List<DnsEvent> {
        val now = System.currentTimeMillis()
        return listOf(
            DnsEvent(
                id = "demo-evt-1",
                timestamp = now - 4000L,
                clientIp = "192.168.1.105",
                domain = "tracker.adservice.google.com",
                queryType = "A",
                action = DnsAction.BLOCK,
                category = "Ads & Trackers",
                source = "Threat Intel Feed"
            ),
            DnsEvent(
                id = "demo-evt-2",
                timestamp = now - 18000L,
                clientIp = "192.168.1.101",
                domain = "api.github.com",
                queryType = "HTTPS",
                action = DnsAction.ALLOW,
                category = "Development",
                source = "Upstream DNS"
            ),
            DnsEvent(
                id = "demo-evt-3",
                timestamp = now - 35000L,
                clientIp = "192.168.1.105",
                domain = "slot-gacor-online88.top",
                queryType = "A",
                action = DnsAction.BLOCK,
                category = "Gambling",
                source = "Manual Entry"
            ),
            DnsEvent(
                id = "demo-evt-4",
                timestamp = now - 62000L,
                clientIp = "192.168.1.112",
                domain = "netflix.com",
                queryType = "A",
                action = DnsAction.ALLOW,
                category = "Streaming",
                source = "Upstream DNS"
            ),
            DnsEvent(
                id = "demo-evt-5",
                timestamp = now - 95000L,
                clientIp = "192.168.1.101",
                domain = "crypto-miner-pool.xyz",
                queryType = "A",
                action = DnsAction.BLOCK,
                category = "Malware",
                source = "Threat Intel Feed"
            ),
            DnsEvent(
                id = "demo-evt-6",
                timestamp = now - 140000L,
                clientIp = "192.168.1.145",
                domain = "cloudflare-dns.com",
                queryType = "AAAA",
                action = DnsAction.ALLOW,
                category = "Infrastructure",
                source = "Upstream DNS"
            ),
            DnsEvent(
                id = "demo-evt-7",
                timestamp = now - 190000L,
                clientIp = "192.168.1.105",
                domain = "secure-login-bank-verify.cc",
                queryType = "A",
                action = DnsAction.BLOCK,
                category = "Phishing",
                source = "Threat Intel Feed"
            ),
            DnsEvent(
                id = "demo-evt-8",
                timestamp = now - 250000L,
                clientIp = "192.168.1.120",
                domain = "mqtt.broker.home",
                queryType = "A",
                action = DnsAction.ALLOW,
                category = "Local IoT",
                source = "Local Resolver"
            )
        )
    }

    fun getDashboardStats(): DashboardStats {
        return DashboardStats(
            totalRequests = 24850,
            blockedRequests = 4120,
            allowedRequests = 20730,
            uniqueDevices = 6
        )
    }

    fun getTrafficPoints(): List<TrafficPoint> {
        val now = System.currentTimeMillis()
        val hourMs = 3600000L
        return listOf(
            TrafficPoint(now - 20 * hourMs, "04:00", 280, 24),
            TrafficPoint(now - 16 * hourMs, "08:00", 1450, 190),
            TrafficPoint(now - 12 * hourMs, "12:00", 2890, 480),
            TrafficPoint(now - 8 * hourMs, "16:00", 3120, 560),
            TrafficPoint(now - 4 * hourMs, "20:00", 4450, 890),
            TrafficPoint(now, "24:00", 3980, 680)
        )
    }

    fun getSystemStatus(): SystemStatus {
        return SystemStatus(
            online = true,
            ipAddress = "192.168.1.10 (Demo)",
            dnsEngineStatus = "ACTIVE",
            websocketConnected = true,
            uptimeSeconds = 142850L,
            lastSyncTimestamp = System.currentTimeMillis() - 120000L,
            freeHeapBytes = 196608L,
            isDemoMode = true
        )
    }

    fun getThreatIntel(): ThreatIntelSummary {
        return ThreatIntelSummary(
            activeThreatSources = 4,
            totalBlockedDomains = 14250,
            lastFeedUpdate = System.currentTimeMillis() - 3600000L,
            engineVersion = "v1.4.2-ESP32-DEMO"
        )
    }

    private val demoDomainsPool = listOf(
        Pair("telemetry.microsoft.com", Pair(DnsAction.BLOCK, "Ads & Trackers")),
        Pair("api.twitter.com", Pair(DnsAction.ALLOW, "Social Media")),
        Pair("phish-update-account.tk", Pair(DnsAction.BLOCK, "Phishing")),
        Pair("cdn.jsdelivr.net", Pair(DnsAction.ALLOW, "Content Delivery")),
        Pair("trojan.download-source.ru", Pair(DnsAction.BLOCK, "Malware")),
        Pair("graph.facebook.com", Pair(DnsAction.ALLOW, "Social Media")),
        Pair("ads.doubleclick.net", Pair(DnsAction.BLOCK, "Ads & Trackers")),
        Pair("poker-win-realmoney.club", Pair(DnsAction.BLOCK, "Gambling")),
        Pair("upload.wikimedia.org", Pair(DnsAction.ALLOW, "General")),
        Pair("iot.espressif.cn", Pair(DnsAction.ALLOW, "IoT System"))
    )

    private val demoIps = listOf(
        "192.168.1.101", "192.168.1.105", "192.168.1.112",
        "192.168.1.120", "192.168.1.145", "192.168.1.160"
    )

    /**
     * Emits a simulated stream of realistic events every 2-4 seconds when in Demo mode.
     */
    fun createSimulatedTrafficFlow(): Flow<DnsEvent> = flow {
        while (true) {
            delay((2000L..4500L).random())
            val sample = demoDomainsPool.random()
            val clientIp = demoIps.random()
            val event = DnsEvent(
                id = "sim-${UUID.randomUUID().toString().take(8)}",
                timestamp = System.currentTimeMillis(),
                clientIp = clientIp,
                domain = sample.first,
                queryType = if (sample.second.first == DnsAction.BLOCK) "A" else listOf("A", "AAAA", "HTTPS").random(),
                action = sample.second.first,
                category = sample.second.second,
                source = if (sample.second.first == DnsAction.BLOCK) "Threat Intel Feed" else "Upstream DNS"
            )
            emit(event)
        }
    }
}
