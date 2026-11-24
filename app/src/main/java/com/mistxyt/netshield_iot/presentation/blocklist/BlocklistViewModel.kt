package com.mistxyt.netshield_iot.presentation.blocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mistxyt.netshield_iot.domain.model.BlockedDomain
import com.mistxyt.netshield_iot.domain.repository.BlocklistRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BlocklistUiState(
    val domains: List<BlockedDomain> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredDomains: List<BlockedDomain>
        get() = domains.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.domain.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" ||
                    item.category.equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesCategory
        }
}

private data class BlocklistFilter(
    val query: String,
    val category: String
)

private data class BlocklistStatus(
    val isLoading: Boolean,
    val isSyncing: Boolean,
    val feedbackMessage: String?,
    val errorMessage: String?
)

class BlocklistViewModel(
    private val blocklistRepository: BlocklistRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _isLoading = MutableStateFlow(false)
    private val _isSyncing = MutableStateFlow(false)
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val filterFlow: Flow<BlocklistFilter> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, cat ->
        BlocklistFilter(query, cat)
    }

    private val statusFlow: Flow<BlocklistStatus> = combine(
        _isLoading,
        _isSyncing,
        _feedbackMessage,
        _errorMessage
    ) { loading, syncing, feedback, error ->
        BlocklistStatus(loading, syncing, feedback, error)
    }

    val uiState: StateFlow<BlocklistUiState> = combine(
        blocklistRepository.getBlockedDomains(),
        filterFlow,
        statusFlow
    ) { domains, filter, status ->
        BlocklistUiState(
            domains = domains,
            searchQuery = filter.query,
            selectedCategory = filter.category,
            isLoading = status.isLoading,
            isSyncing = status.isSyncing,
            feedbackMessage = status.feedbackMessage,
            errorMessage = status.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BlocklistUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun addDomain(domain: String, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = blocklistRepository.addBlockedDomain(domain, category, "Manual Entry")
            if (result.isSuccess) {
                _feedbackMessage.value = "Domain $domain added"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to add domain"
            }
            _isLoading.value = false
        }
    }

    fun editDomain(id: String, domain: String, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = blocklistRepository.updateBlockedDomain(id, domain, category)
            if (result.isSuccess) {
                _feedbackMessage.value = "Domain $domain updated"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update domain"
            }
            _isLoading.value = false
        }
    }

    fun removeDomain(id: String, domainName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = blocklistRepository.removeBlockedDomain(id)
            if (result.isSuccess) {
                _feedbackMessage.value = "Domain $domainName removed"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to remove domain"
            }
            _isLoading.value = false
        }
    }

    fun syncWithEsp32() {
        viewModelScope.launch {
            _isSyncing.value = true
            _errorMessage.value = null
            val result = blocklistRepository.syncWithEsp32()
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _feedbackMessage.value = "Synced $count rules to ESP32"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Sync failed"
            }
            _isSyncing.value = false
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
        _errorMessage.value = null
    }

    class Factory(private val blocklistRepository: BlocklistRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BlocklistViewModel(blocklistRepository) as T
        }
    }
}
