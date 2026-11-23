package com.mistxyt.netshield_iot.domain.repository

import com.mistxyt.netshield_iot.domain.model.BlockedDomain
import kotlinx.coroutines.flow.Flow

interface BlocklistRepository {
    fun getBlockedDomains(): Flow<List<BlockedDomain>>
    suspend fun addBlockedDomain(domain: String, category: String, source: String): Result<BlockedDomain>
    suspend fun removeBlockedDomain(id: String): Result<Unit>
    suspend fun syncWithEsp32(): Result<Int>
    suspend fun refreshBlocklist(): Result<Unit>
}
