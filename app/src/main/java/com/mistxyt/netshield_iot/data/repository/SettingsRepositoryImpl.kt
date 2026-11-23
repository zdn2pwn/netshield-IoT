package com.mistxyt.netshield_iot.data.repository

import com.mistxyt.netshield_iot.data.local.preferences.NetShieldPreferences
import com.mistxyt.netshield_iot.data.remote.NetworkModule
import com.mistxyt.netshield_iot.domain.repository.AppLanguage
import com.mistxyt.netshield_iot.domain.repository.AppSettings
import com.mistxyt.netshield_iot.domain.repository.AppThemeMode
import com.mistxyt.netshield_iot.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val preferences: NetShieldPreferences
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return preferences.appSettingsFlow
    }

    override suspend fun setEsp32Ip(ip: String) {
        preferences.setEsp32Ip(ip)
    }

    override suspend fun setRestPort(port: Int) {
        preferences.setRestPort(port)
    }

    override suspend fun setWsPort(port: Int) {
        preferences.setWsPort(port)
    }

    override suspend fun setDemoMode(enabled: Boolean) {
        preferences.setDemoMode(enabled)
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        preferences.setThemeMode(mode)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        preferences.setLanguage(language)
    }

    override suspend fun testConnection(customIp: String?, customPort: Int?): Result<Boolean> = withContext(Dispatchers.IO) {
        val settings = preferences.appSettingsFlow.first()
        if (settings.isDemoMode) {
            return@withContext Result.success(true)
        }

        val rawIp = customIp ?: settings.esp32Ip
        val cleanIp = NetworkModule.sanitizeIp(rawIp).ifBlank { "127.0.0.1" }
        val targetPort = if (customPort != null && customPort in 1..65535) customPort else settings.restPort

        // 1. Direct TCP Socket & HTTP probe (Ultra-reliable, zero library overhead)
        try {
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(cleanIp, targetPort), 2500)
                socket.soTimeout = 2500
                val writer = java.io.PrintWriter(socket.getOutputStream(), true)
                writer.print("GET /api/v1/health HTTP/1.1\r\nHost: $cleanIp\r\nConnection: close\r\n\r\n")
                writer.flush()

                val reader = java.io.BufferedReader(java.io.InputStreamReader(socket.getInputStream()))
                val statusLine = reader.readLine()
                if (statusLine != null && (statusLine.contains("200") || statusLine.contains("OK") || statusLine.contains("ok"))) {
                    return@withContext Result.success(true)
                }
            }
        } catch (_: Exception) {
            // Fall through to Retrofit/OkHttp probe
        }

        // 2. Secondary probe via Retrofit / OkHttp
        try {
            val api = NetworkModule.getApiService(cleanIp, targetPort)
            val response = api.getHealth()
            if (response.isSuccessful) {
                return@withContext Result.success(true)
            }
            val statusResp = api.getSystemStatus()
            if (statusResp.isSuccessful) {
                return@withContext Result.success(true)
            }
            Result.failure(Exception("HTTP Error: ${response.code()}"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("IP $cleanIp tidak merespons (Timeout). Pastikan ESP32 & HP tersambung ke hotspot yang sama."))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Koneksi ditolak pada port $targetPort. Pastikan port 80 aktif."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung"))
        }
    }
}
