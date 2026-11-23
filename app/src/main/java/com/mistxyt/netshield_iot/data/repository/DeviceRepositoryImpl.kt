package com.mistxyt.netshield_iot.data.repository

import com.mistxyt.netshield_iot.core.util.DateTimeHelper
import com.mistxyt.netshield_iot.data.local.NetShieldDatabase
import com.mistxyt.netshield_iot.data.local.entity.DeviceEntity
import com.mistxyt.netshield_iot.data.local.preferences.NetShieldPreferences
import com.mistxyt.netshield_iot.data.remote.NetworkModule
import com.mistxyt.netshield_iot.domain.model.Device
import com.mistxyt.netshield_iot.domain.repository.AppSettings
import com.mistxyt.netshield_iot.domain.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceRepositoryImpl(
    private val database: NetShieldDatabase,
    private val preferences: NetShieldPreferences,
    private val scope: CoroutineScope
) : DeviceRepository {

    private val deviceDao = database.deviceDao()
    private var currentSettings = AppSettings()

    init {
        scope.launch {
            preferences.appSettingsFlow.collect { settings ->
                currentSettings = settings
                if (!settings.isDemoMode) {
                    refreshDevices()
                }
            }
        }
    }

    override fun getDevices(): Flow<List<Device>> {
        return deviceDao.getAllDevices().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshDevices(): Result<List<Device>> = withContext(Dispatchers.IO) {
        if (currentSettings.isDemoMode) {
            val list = deviceDao.getAllDevices().first().map { it.toDomain() }
            return@withContext Result.success(list)
        }

        try {
            val api = NetworkModule.getApiService(currentSettings.esp32Ip, currentSettings.restPort)
            val response = api.getDevices()
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { dto ->
                    val ip = dto.ipAddress ?: "192.168.1.1"
                    val existing = deviceDao.getDeviceByIp(ip)
                    DeviceEntity(
                        id = dto.id ?: existing?.id ?: "dev-${ip.replace('.', '-')}",
                        ipAddress = ip,
                        macAddress = dto.macAddress ?: existing?.macAddress ?: "Active Client",
                        requestCount = dto.requestCount ?: existing?.requestCount ?: 0,
                        blockedCount = dto.blockedCount ?: existing?.blockedCount ?: 0,
                        lastActivity = DateTimeHelper.resolveValidEpochMs(dto.lastActivity),
                        customName = existing?.customName,
                        deviceType = existing?.deviceType ?: "SMARTPHONE",
                        detectedModel = existing?.detectedModel ?: (if (ip.endsWith(".1")) "Hotspot Gateway / Router" else null),
                        isInternetPaused = existing?.isInternetPaused ?: false
                    )
                }
                deviceDao.insertDevices(entities)
                Result.success(entities.map { it.toDomain() })
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDeviceDetails(
        id: String,
        customName: String,
        deviceType: com.mistxyt.netshield_iot.domain.model.DeviceType
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            deviceDao.updateDeviceAlias(id, customName, deviceType.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleInternetPause(id: String, isPaused: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            deviceDao.updateInternetPaused(id, isPaused)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearDevices() {
        withContext(Dispatchers.IO) {
            deviceDao.clearAll()
        }
    }
}
