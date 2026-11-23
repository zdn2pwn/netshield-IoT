package com.mistxyt.netshield_iot.presentation.traffic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mistxyt.netshield_iot.domain.model.DnsAction
import com.mistxyt.netshield_iot.domain.model.DnsEvent
import com.mistxyt.netshield_iot.domain.repository.DnsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TrafficFilterAction {
    ALL,
    ALLOWED,
    BLOCKED
}

data class TrafficUiState(
    val events: List<DnsEvent> = emptyList(),
    val searchQuery: String = "",
    val actionFilter: TrafficFilterAction = TrafficFilterAction.ALL,
    val categoryFilter: String = "All",
    val isPaused: Boolean = false,
    val isConnected: Boolean = true,
    val isRefreshing: Boolean = false
) {
    val filteredEvents: List<DnsEvent>
        get() = events.filter { event ->
            val matchesSearch = searchQuery.isBlank() ||
                    event.domain.contains(searchQuery, ignoreCase = true) ||
                    event.clientIp.contains(searchQuery, ignoreCase = true)

            val matchesAction = when (actionFilter) {
                TrafficFilterAction.ALL -> true
                TrafficFilterAction.ALLOWED -> event.action == DnsAction.ALLOW
                TrafficFilterAction.BLOCKED -> event.action == DnsAction.BLOCK
            }

            val matchesCategory = categoryFilter == "All" ||
                    event.category.equals(categoryFilter, ignoreCase = true)

            matchesSearch && matchesAction && matchesCategory
        }
}

private data class TrafficFilters(
    val query: String,
    val action: TrafficFilterAction,
    val category: String
)

private data class TrafficStatus(
    val isPaused: Boolean,
    val isConnected: Boolean,
    val isRefreshing: Boolean
)

class TrafficViewModel(
    private val dnsRepository: DnsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _actionFilter = MutableStateFlow(TrafficFilterAction.ALL)
    private val _categoryFilter = MutableStateFlow("All")
    private val _isPaused = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _eventsList = MutableStateFlow<List<DnsEvent>>(emptyList())

    private val filtersFlow: Flow<TrafficFilters> = combine(
        _searchQuery,
        _actionFilter,
        _categoryFilter
    ) { q, a, c ->
        TrafficFilters(q, a, c)
    }

    private val statusFlow: Flow<TrafficStatus> = combine(
        _isPaused,
        dnsRepository.getWebSocketConnectionState(),
        _isRefreshing
    ) { p, c, r ->
        TrafficStatus(p, c, r)
    }

    val uiState: StateFlow<TrafficUiState> = combine(
        _eventsList,
        filtersFlow,
        statusFlow
    ) { events, filters, status ->
        TrafficUiState(
            events = events,
            searchQuery = filters.query,
            actionFilter = filters.action,
            categoryFilter = filters.category,
            isPaused = status.isPaused,
            isConnected = status.isConnected,
            isRefreshing = status.isRefreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrafficUiState()
    )

    init {
        // Seed from DB recent events
        viewModelScope.launch {
            dnsRepository.getRecentEvents(100).first().let { initial ->
                _eventsList.value = initial
            }
        }

        // Stream live events
        viewModelScope.launch {
            dnsRepository.observeLiveTraffic().collect { event ->
                if (!_isPaused.value) {
                    val current = _eventsList.value.toMutableList()
                    current.add(0, event)
                    // Keep up to 200 items in memory
                    if (current.size > 200) {
                        _eventsList.value = current.take(200)
                    } else {
                        _eventsList.value = current
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setActionFilter(filter: TrafficFilterAction) {
        _actionFilter.value = filter
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun clearEvents() {
        viewModelScope.launch {
            _eventsList.value = emptyList()
            dnsRepository.clearEventHistory()
        }
    }

    fun refreshHistorical() {
        viewModelScope.launch {
            _isRefreshing.value = true
            dnsRepository.refreshAll()
            dnsRepository.getRecentEvents(100).first().let { fresh ->
                _eventsList.value = fresh
            }
            _isRefreshing.value = false
        }
    }

    class Factory(private val dnsRepository: DnsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrafficViewModel(dnsRepository) as T
        }
    }
}
