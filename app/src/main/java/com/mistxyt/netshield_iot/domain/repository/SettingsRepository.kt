package com.mistxyt.netshield_iot.domain.repository

import kotlinx.coroutines.flow.Flow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage {
    SYSTEM,
    EN,
    ID
}

data class AppSettings(
    val esp32Ip: String = "192.168.43.85",
    val restPort: Int = 80,
    val wsPort: Int = 81,
    val isDemoMode: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM
)

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun setEsp32Ip(ip: String)
    suspend fun setRestPort(port: Int)
    suspend fun setWsPort(port: Int)
    suspend fun setDemoMode(enabled: Boolean)
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun testConnection(customIp: String? = null, customPort: Int? = null): Result<Boolean>
}
