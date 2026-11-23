package com.mistxyt.netshield_iot.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mistxyt.netshield_iot.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TestConnectionState {
    object Idle : TestConnectionState()
    object Testing : TestConnectionState()
    object Success : TestConnectionState()
    data class Failed(val error: String) : TestConnectionState()
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val testState: TestConnectionState = TestConnectionState.Idle,
    val feedbackMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dnsRepository: DnsRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _testState = MutableStateFlow<TestConnectionState>(TestConnectionState.Idle)
    private val _feedbackMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.getSettings(),
        _testState,
        _feedbackMessage
    ) { settings, testState, feedback ->
        SettingsUiState(
            settings = settings,
            testState = testState,
            feedbackMessage = feedback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateEsp32Ip(ip: String) {
        viewModelScope.launch {
            settingsRepository.setEsp32Ip(ip)
        }
    }

    fun updateRestPort(port: Int) {
        viewModelScope.launch {
            settingsRepository.setRestPort(port)
        }
    }

    fun updateWsPort(port: Int) {
        viewModelScope.launch {
            settingsRepository.setWsPort(port)
        }
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDemoMode(enabled)
            _feedbackMessage.value = if (enabled) "Switched to Demo Mode" else "Switched to Live ESP32 Mode"
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun testConnection(customIp: String? = null, customPort: Int? = null) {
        viewModelScope.launch {
            _testState.value = TestConnectionState.Testing
            customIp?.let { settingsRepository.setEsp32Ip(it) }
            customPort?.let { settingsRepository.setRestPort(it) }

            val result = settingsRepository.testConnection(customIp, customPort)
            if (result.isSuccess) {
                _testState.value = TestConnectionState.Success
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Connection timed out"
                _testState.value = TestConnectionState.Failed(err)
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            dnsRepository.clearEventHistory()
            _feedbackMessage.value = "Local DNS event history cleared."
        }
    }

    fun resetFeedback() {
        _feedbackMessage.value = null
        _testState.value = TestConnectionState.Idle
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val dnsRepository: DnsRepository,
        private val deviceRepository: DeviceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, dnsRepository, deviceRepository) as T
        }
    }
}
