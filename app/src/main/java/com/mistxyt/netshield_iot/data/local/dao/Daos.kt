package com.mistxyt.netshield_iot.data.local.dao

import androidx.room.*
import com.mistxyt.netshield_iot.data.local.entity.BlockedDomainEntity
import com.mistxyt.netshield_iot.data.local.entity.DeviceEntity
import com.mistxyt.netshield_iot.data.local.entity.DnsEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DnsEventDao {
    @Query("SELECT * FROM dns_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<DnsEventEntity>>

    @Query("SELECT * FROM dns_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<DnsEventEntity>>

    @Query("SELECT * FROM dns_events WHERE clientIp = :clientIp ORDER BY timestamp DESC LIMIT :limit")
    fun getEventsByClientIp(clientIp: String, limit: Int = 100): Flow<List<DnsEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: DnsEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvents(events: List<DnsEventEntity>)

    @Query("DELETE FROM dns_events")
    fun clearEvents()
}

@Dao
interface BlockedDomainDao {
    @Query("SELECT * FROM blocked_domains ORDER BY createdAt DESC")
    fun getAllBlockedDomains(): Flow<List<BlockedDomainEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDomain(domain: BlockedDomainEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDomains(domains: List<BlockedDomainEntity>)

    @Query("DELETE FROM blocked_domains WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM blocked_domains")
    fun clearAll()
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY lastActivity DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE ipAddress = :ipAddress LIMIT 1")
    fun getDeviceByIp(ipAddress: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDevices(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices")
    fun clearAll()
}
