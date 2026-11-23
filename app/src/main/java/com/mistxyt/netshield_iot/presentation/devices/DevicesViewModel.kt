package com.mistxyt.netshield_iot.presentation.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mistxyt.netshield_iot.domain.model.Device
import com.mistxyt.netshield_iot.domain.model.DeviceType
import com.mistxyt.netshield_iot.domain.model.DnsEvent
import com.mistxyt.netshield_iot.domain.repository.DeviceRepository
import com.mistxyt.netshield_iot.domain.repository.DnsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DeviceSortOption {
    MOST_REQUESTS,
    MOST_BLOCKED,
    MOST_RECENT,
    IP_ADDRESS
}

data class DevicesUiState(
    val devices: List<Device> = emptyList(),
    val searchQuery: String = "",
    val sortOption: DeviceSortOption = DeviceSortOption.MOST_REQUESTS,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) {
    val sortedFilteredDevices: List<Device>
        get() {
            val filtered = devices.filter { device ->
                searchQuery.isBlank() ||
                        device.ipAddress.contains(searchQuery, ignoreCase = true) ||
                        device.displayName.contains(searchQuery, ignoreCase = true) ||
                        (device.macAddress?.contains(searchQuery, ignoreCase = true) == true) ||
                        (device.detectedModel?.contains(searchQuery, ignoreCase = true) == true)
            }
            return when (sortOption) {
                DeviceSortOption.MOST_REQUESTS -> filtered.sortedByDescending { it.requestCount }
                DeviceSortOption.MOST_BLOCKED -> filtered.sortedByDescending { it.blockedCount }
                DeviceSortOption.MOST_RECENT -> filtered.sortedByDescending { it.lastActivity }
                DeviceSortOption.IP_ADDRESS -> filtered.sortedBy { it.ipAddress }
            }
        }
}

class DevicesViewModel(
    private val deviceRepository: DeviceRepository,
    private val dnsRepository: DnsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(DeviceSortOption.MOST_REQUESTS)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _selectedDevice = MutableStateFlow<Device?>(null)
    private val _editingDevice = MutableStateFlow<Device?>(null)

    val selectedDevice: StateFlow<Device?> = _selectedDevice.asStateFlow()
    val editingDevice: StateFlow<Device?> = _editingDevice.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDeviceQueries: StateFlow<List<DnsEvent>> = _selectedDevice
        .flatMapLatest { dev ->
            if (dev == null) flowOf(emptyList())
            else dnsRepository.getEventsByClientIp(dev.ipAddress, 100)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<DevicesUiState> = combine(
        deviceRepository.getDevices(),
        _searchQuery,
        _sortOption,
        _isRefreshing,
        _errorMessage
    ) { devices, query, sort, refreshing, error ->
        DevicesUiState(
            devices = devices,
            searchQuery = query,
            sortOption = sort,
            isRefreshing = refreshing,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DevicesUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOptionChange(sort: DeviceSortOption) {
        _sortOption.value = sort
    }

    fun selectDevice(device: Device?) {
        _selectedDevice.value = device
    }

    fun dismissDeviceDetails() {
        _selectedDevice.value = null
    }

    fun showEditDialog(device: Device) {
        _editingDevice.value = device
    }

    fun hideEditDialog() {
        _editingDevice.value = null
    }

    fun updateDeviceAlias(device: Device, newName: String, newType: DeviceType) {
        viewModelScope.launch {
            deviceRepository.updateDeviceDetails(device.id, newName.trim(), newType)
        }
    }

    fun toggleInternetPause(device: Device) {
        viewModelScope.launch {
            deviceRepository.toggleInternetPause(device.id, !device.isInternetPaused)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            val result = deviceRepository.refreshDevices()
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage
            }
            _isRefreshing.value = false
        }
    }

    class Factory(
        private val deviceRepository: DeviceRepository,
        private val dnsRepository: DnsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DevicesViewModel(deviceRepository, dnsRepository) as T
        }
    }
}
