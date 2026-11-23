package com.mistxyt.netshield_iot.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mistxyt.netshield_iot.domain.model.BlockedDomain
import com.mistxyt.netshield_iot.domain.model.Device
import com.mistxyt.netshield_iot.domain.model.DeviceType
import com.mistxyt.netshield_iot.domain.model.DnsAction
import com.mistxyt.netshield_iot.domain.model.DnsEvent

@Entity(
    tableName = "dns_events",
    indices = [Index(value = ["timestamp"]), Index(value = ["domain"])]
)
data class DnsEventEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val clientIp: String,
    val domain: String,
    val queryType: String,
    val action: String,
    val category: String,
    val source: String
) {
    fun toDomain(): DnsEvent = DnsEvent(
        id = id,
        timestamp = timestamp,
        clientIp = clientIp,
        domain = domain,
        queryType = queryType,
        action = DnsAction.fromString(action),
        category = category,
        source = source
    )

    companion object {
        fun fromDomain(event: DnsEvent): DnsEventEntity = DnsEventEntity(
            id = event.id,
            timestamp = event.timestamp,
            clientIp = event.clientIp,
            domain = event.domain,
            queryType = event.queryType,
            action = event.action.name,
            category = event.category,
            source = event.source
        )
    }
}

@Entity(
    tableName = "blocked_domains",
    indices = [Index(value = ["domain"], unique = true)]
)
data class BlockedDomainEntity(
    @PrimaryKey val id: String,
    val domain: String,
    val category: String,
    val source: String,
    val createdAt: Long,
    val enabled: Boolean
) {
    fun toDomain(): BlockedDomain = BlockedDomain(
        id = id,
        domain = domain,
        category = category,
        source = source,
        createdAt = createdAt,
        enabled = enabled
    )

    companion object {
        fun fromDomain(domain: BlockedDomain): BlockedDomainEntity = BlockedDomainEntity(
            id = domain.id,
            domain = domain.domain,
            category = domain.category,
            source = domain.source,
            createdAt = domain.createdAt,
            enabled = domain.enabled
        )
    }
}

@Entity(
    tableName = "devices",
    indices = [Index(value = ["ipAddress"], unique = true)]
)
data class DeviceEntity(
    @PrimaryKey val id: String,
    val ipAddress: String,
    val macAddress: String?,
    val requestCount: Int,
    val blockedCount: Int,
    val lastActivity: Long,
    val customName: String? = null,
    val deviceType: String? = "SMARTPHONE",
    val detectedModel: String? = null,
    val isInternetPaused: Boolean = false
) {
    fun toDomain(): Device = Device(
        id = id,
        ipAddress = ipAddress,
        macAddress = macAddress,
        requestCount = requestCount,
        blockedCount = blockedCount,
        lastActivity = lastActivity,
        customName = customName,
        deviceType = DeviceType.fromString(deviceType),
        detectedModel = detectedModel,
        isInternetPaused = isInternetPaused
    )

    companion object {
        fun fromDomain(device: Device): DeviceEntity = DeviceEntity(
            id = device.id,
            ipAddress = device.ipAddress,
            macAddress = device.macAddress,
            requestCount = device.requestCount,
            blockedCount = device.blockedCount,
            lastActivity = device.lastActivity,
            customName = device.customName,
            deviceType = device.deviceType.name,
            detectedModel = device.detectedModel,
            isInternetPaused = device.isInternetPaused
        )
    }
}
