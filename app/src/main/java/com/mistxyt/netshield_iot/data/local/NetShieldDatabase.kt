package com.mistxyt.netshield_iot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mistxyt.netshield_iot.data.local.dao.BlockedDomainDao
import com.mistxyt.netshield_iot.data.local.dao.DeviceDao
import com.mistxyt.netshield_iot.data.local.dao.DnsEventDao
import com.mistxyt.netshield_iot.data.local.entity.BlockedDomainEntity
import com.mistxyt.netshield_iot.data.local.entity.DeviceEntity
import com.mistxyt.netshield_iot.data.local.entity.DnsEventEntity

@Database(
    entities = [
        DnsEventEntity::class,
        BlockedDomainEntity::class,
        DeviceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NetShieldDatabase : RoomDatabase() {
    abstract fun dnsEventDao(): DnsEventDao
    abstract fun blockedDomainDao(): BlockedDomainDao
    abstract fun deviceDao(): DeviceDao

    companion object {
        @Volatile
        private var INSTANCE: NetShieldDatabase? = null

        fun getInstance(context: Context): NetShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetShieldDatabase::class.java,
                    "netshield_iot.db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
