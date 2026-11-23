package com.mistxyt.netshield_iot

import android.app.Application
import com.mistxyt.netshield_iot.data.local.NetShieldDatabase
import com.mistxyt.netshield_iot.data.local.preferences.NetShieldPreferences
import com.mistxyt.netshield_iot.data.remote.NetworkModule
import com.mistxyt.netshield_iot.data.remote.websocket.NetShieldWebSocketClient
import com.mistxyt.netshield_iot.data.repository.BlocklistRepositoryImpl
import com.mistxyt.netshield_iot.data.repository.DeviceRepositoryImpl
import com.mistxyt.netshield_iot.data.repository.DnsRepositoryImpl
import com.mistxyt.netshield_iot.data.repository.SettingsRepositoryImpl
import com.mistxyt.netshield_iot.domain.repository.BlocklistRepository
import com.mistxyt.netshield_iot.domain.repository.DeviceRepository
import com.mistxyt.netshield_iot.domain.repository.DnsRepository
import com.mistxyt.netshield_iot.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NetShieldApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: NetShieldDatabase
        private set
    lateinit var preferences: NetShieldPreferences
        private set
    lateinit var webSocketClient: NetShieldWebSocketClient
        private set

    lateinit var dnsRepository: DnsRepository
        private set
    lateinit var blocklistRepository: BlocklistRepository
        private set
    lateinit var deviceRepository: DeviceRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        NetworkModule.initialize(this)
        database = NetShieldDatabase.getInstance(this)
        preferences = NetShieldPreferences(this)
        webSocketClient = NetShieldWebSocketClient(NetworkModule.okHttpClient, NetworkModule.gson)

        settingsRepository = SettingsRepositoryImpl(preferences)
        dnsRepository = DnsRepositoryImpl(this, database, preferences, webSocketClient, applicationScope)
        blocklistRepository = BlocklistRepositoryImpl(database, preferences, applicationScope)
        deviceRepository = DeviceRepositoryImpl(database, preferences, applicationScope)
    }
}
