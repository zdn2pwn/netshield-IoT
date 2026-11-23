package com.mistxyt.netshield_iot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.mistxyt.netshield_iot.core.util.LocaleHelper
import com.mistxyt.netshield_iot.domain.repository.AppThemeMode
import com.mistxyt.netshield_iot.presentation.blocklist.BlocklistViewModel
import com.mistxyt.netshield_iot.presentation.dashboard.DashboardViewModel
import com.mistxyt.netshield_iot.presentation.devices.DevicesViewModel
import com.mistxyt.netshield_iot.presentation.navigation.NetShieldMainApp
import com.mistxyt.netshield_iot.presentation.settings.SettingsViewModel
import com.mistxyt.netshield_iot.presentation.traffic.TrafficViewModel
import com.mistxyt.netshield_iot.ui.theme.NetshieldiotTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NetShieldApplication

        val dashboardViewModel: DashboardViewModel by viewModels {
            DashboardViewModel.Factory(app.dnsRepository)
        }
        val trafficViewModel: TrafficViewModel by viewModels {
            TrafficViewModel.Factory(app.dnsRepository)
        }
        val blocklistViewModel: BlocklistViewModel by viewModels {
            BlocklistViewModel.Factory(app.blocklistRepository)
        }
        val devicesViewModel: DevicesViewModel by viewModels {
            DevicesViewModel.Factory(app.deviceRepository)
        }
        val settingsViewModel: SettingsViewModel by viewModels {
            SettingsViewModel.Factory(app.settingsRepository, app.dnsRepository, app.deviceRepository)
        }

        setContent {
            val settingsState by app.settingsRepository.getSettings().collectAsState(initial = com.mistxyt.netshield_iot.domain.repository.AppSettings())

            val isDarkTheme = when (settingsState.themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val currentContext = LocalContext.current
            val localizedContext = LocaleHelper.getLocalizedContext(currentContext, settingsState.language)

            CompositionLocalProvider(LocalContext provides localizedContext) {
                NetshieldiotTheme(darkTheme = isDarkTheme) {
                    NetShieldMainApp(
                        dashboardViewModel = dashboardViewModel,
                        trafficViewModel = trafficViewModel,
                        blocklistViewModel = blocklistViewModel,
                        devicesViewModel = devicesViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
