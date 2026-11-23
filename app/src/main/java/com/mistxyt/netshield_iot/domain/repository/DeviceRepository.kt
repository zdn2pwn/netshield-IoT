package com.mistxyt.netshield_iot.domain.repository

import com.mistxyt.netshield_iot.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDevices(): Flow<List<Device>>
    suspend fun refreshDevices(): Result<List<Device>>
    suspend fun updateDeviceDetails(id: String, customName: String, deviceType: com.mistxyt.netshield_iot.domain.model.DeviceType): Result<Unit>
    suspend fun toggleInternetPause(id: String, isPaused: Boolean): Result<Unit>
    suspend fun clearDevices()
}
