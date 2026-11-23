package com.mistxyt.netshield_iot.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mistxyt.netshield_iot.domain.repository.AppLanguage
import com.mistxyt.netshield_iot.domain.repository.AppSettings
import com.mistxyt.netshield_iot.domain.repository.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netshield_settings")

class NetShieldPreferences(private val context: Context) {

    private object PreferencesKeys {
        val ESP32_IP = stringPreferencesKey("esp32_ip")
        val REST_PORT = intPreferencesKey("rest_port")
        val WS_PORT = intPreferencesKey("ws_port")
        val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val esp32Ip = preferences[PreferencesKeys.ESP32_IP] ?: "192.168.43.85"
            val restPort = preferences[PreferencesKeys.REST_PORT] ?: 80
            val wsPort = preferences[PreferencesKeys.WS_PORT] ?: 81
            val isDemoMode = preferences[PreferencesKeys.IS_DEMO_MODE] ?: false
            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val langStr = preferences[PreferencesKeys.LANGUAGE] ?: AppLanguage.SYSTEM.name

            AppSettings(
                esp32Ip = esp32Ip,
                restPort = restPort,
                wsPort = wsPort,
                isDemoMode = isDemoMode,
                themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM),
                language = runCatching { AppLanguage.valueOf(langStr) }.getOrDefault(AppLanguage.SYSTEM)
            )
        }

    suspend fun setEsp32Ip(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ESP32_IP] = ip.trim()
        }
    }

    suspend fun setRestPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REST_PORT] = port
        }
    }

    suspend fun setWsPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WS_PORT] = port
        }
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DEMO_MODE] = enabled
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.name
        }
    }
}
