package com.mistxyt.netshield_iot.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mistxyt.netshield_iot.domain.model.*
import com.mistxyt.netshield_iot.domain.repository.DnsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val systemStatus: SystemStatus = SystemStatus(),
    val stats: DashboardStats = DashboardStats(),
    val trafficPoints: List<TrafficPoint> = emptyList(),
    val recentEvents: List<DnsEvent> = emptyList(),
    val threatIntel: ThreatIntelSummary = ThreatIntelSummary(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

private data class DashboardCoreData(
    val status: SystemStatus,
    val stats: DashboardStats,
    val traffic: List<TrafficPoint>,
    val recent: List<DnsEvent>,
    val threat: ThreatIntelSummary
)

class DashboardViewModel(
    private val dnsRepository: DnsRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val coreDataFlow: Flow<DashboardCoreData> = combine(
        dnsRepository.getSystemStatus(),
        dnsRepository.getStats(),
        dnsRepository.getTrafficAnalytics(),
        dnsRepository.getRecentEvents(15),
        dnsRepository.getThreatIntel()
    ) { status, stats, traffic, recent, threat ->
        DashboardCoreData(status, stats, traffic, recent, threat)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        coreDataFlow,
        _isRefreshing,
        _errorMessage
    ) { core, refreshing, error ->
        DashboardUiState(
            systemStatus = core.status,
            stats = core.stats,
            trafficPoints = core.traffic,
            recentEvents = core.recent,
            threatIntel = core.threat,
            isRefreshing = refreshing,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            val result = dnsRepository.refreshAll()
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage
            }
            _isRefreshing.value = false
        }
    }

    class Factory(private val dnsRepository: DnsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(dnsRepository) as T
        }
    }
}
