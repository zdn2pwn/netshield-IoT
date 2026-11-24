package com.mistxyt.netshield_iot.data.repository

import com.mistxyt.netshield_iot.data.local.NetShieldDatabase
import com.mistxyt.netshield_iot.data.local.entity.BlockedDomainEntity
import com.mistxyt.netshield_iot.data.local.preferences.NetShieldPreferences
import com.mistxyt.netshield_iot.data.remote.NetworkModule
import com.mistxyt.netshield_iot.data.remote.dto.AddBlocklistRequestDto
import com.mistxyt.netshield_iot.domain.model.BlockedDomain
import com.mistxyt.netshield_iot.domain.repository.AppSettings
import com.mistxyt.netshield_iot.domain.repository.BlocklistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class BlocklistRepositoryImpl(
    private val database: NetShieldDatabase,
    private val preferences: NetShieldPreferences,
    private val scope: CoroutineScope
) : BlocklistRepository {

    private val blockedDomainDao = database.blockedDomainDao()
    private var currentSettings = AppSettings()

    init {
        scope.launch {
            preferences.appSettingsFlow.collect { settings ->
                currentSettings = settings
                if (!settings.isDemoMode) {
                    refreshBlocklist()
                }
            }
        }
    }

    override fun getBlockedDomains(): Flow<List<BlockedDomain>> {
        return blockedDomainDao.getAllBlockedDomains().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addBlockedDomain(
        domain: String,
        category: String,
        source: String
    ): Result<BlockedDomain> = withContext(Dispatchers.IO) {
        val cleanDomain = domain.trim().lowercase()
        val now = System.currentTimeMillis()

        if (currentSettings.isDemoMode) {
            val newDomain = BlockedDomain(
                id = "blk-${UUID.randomUUID().toString().take(8)}",
                domain = cleanDomain,
                category = category,
                source = source,
                createdAt = now,
                enabled = true
            )
            blockedDomainDao.insertDomain(BlockedDomainEntity.fromDomain(newDomain))
            return@withContext Result.success(newDomain)
        }

        try {
            val api = NetworkModule.getApiService(currentSettings.esp32Ip, currentSettings.restPort)
            val response = api.addBlockedDomain(
                AddBlocklistRequestDto(
                    domain = cleanDomain,
                    category = category,
                    source = source
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val created = BlockedDomain(
                    id = dto.id ?: "blk-${UUID.randomUUID().toString().take(8)}",
                    domain = dto.domain ?: cleanDomain,
                    category = dto.category ?: category,
                    source = dto.source ?: source,
                    createdAt = dto.createdAt ?: now,
                    enabled = dto.enabled ?: true
                )
                blockedDomainDao.insertDomain(BlockedDomainEntity.fromDomain(created))
                Result.success(created)
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBlockedDomain(id: String, domain: String, category: String): Result<BlockedDomain> = withContext(Dispatchers.IO) {
        val cleanDomain = domain.trim().lowercase()
        val now = System.currentTimeMillis()

        if (currentSettings.isDemoMode) {
            val existing = blockedDomainDao.getAllBlockedDomains().map { it.toDomain() }.firstOrNull { it.id == id }
            val updated = (existing ?: BlockedDomain(id = id, domain = cleanDomain, category = category, source = "Manual Entry", createdAt = now, enabled = true)).copy(
                domain = cleanDomain,
                category = category
            )
            blockedDomainDao.insertDomain(BlockedDomainEntity.fromDomain(updated))
            return@withContext Result.success(updated)
        }

        try {
            val api = NetworkModule.getApiService(currentSettings.esp32Ip, currentSettings.restPort)
            val response = api.updateBlockedDomain(
                id,
                AddBlocklistRequestDto(
                    domain = cleanDomain,
                    category = category,
                    source = "Manual Entry"
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val updated = BlockedDomain(
                    id = dto.id ?: id,
                    domain = dto.domain ?: cleanDomain,
                    category = dto.category ?: category,
                    source = dto.source ?: "Manual Entry",
                    createdAt = dto.createdAt ?: now,
                    enabled = dto.enabled ?: true
                )
                blockedDomainDao.insertDomain(BlockedDomainEntity.fromDomain(updated))
                Result.success(updated)
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeBlockedDomain(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (currentSettings.isDemoMode) {
            blockedDomainDao.deleteById(id)
            return@withContext Result.success(Unit)
        }

        try {
            val api = NetworkModule.getApiService(currentSettings.esp32Ip, currentSettings.restPort)
            val response = api.deleteBlockedDomain(id)
            if (response.isSuccessful) {
                blockedDomainDao.deleteById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithEsp32(): Result<Int> = withContext(Dispatchers.IO) {
        if (currentSettings.isDemoMode) {
            return@withContext Result.success(0)
        }

        try {
            val api = NetworkModule.getApiService(currentSettings.esp32Ip, currentSettings.restPort)
            val response = api.syncBlocklist()
            if (response.isSuccessful && response.body() != null) {
                val syncedCount = response.body()!!.syncedRulesCount ?: 0
                refreshBlocklist()
                Result.success(syncedCount)
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshBlocklist(): Result<Unit> = withContext(Dispatchers.IO) {
        if (currentSettings.isDemoMode) {
            return@withContext Result.success(Unit)
        }

        try {
            val api = NetworkModule.getApiService(currentSettings.esp32Ip, currentSettings.restPort)
            val response = api.getBlocklist()
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { dto ->
                    BlockedDomainEntity(
                        id = dto.id ?: "blk-${UUID.randomUUID().toString().take(8)}",
                        domain = dto.domain ?: "",
                        category = dto.category ?: "Custom",
                        source = dto.source ?: "ESP32",
                        createdAt = dto.createdAt ?: System.currentTimeMillis(),
                        enabled = dto.enabled ?: true
                    )
                }.filter { it.domain.isNotBlank() }
                blockedDomainDao.clearAll()
                blockedDomainDao.insertDomains(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
