package com.mistxyt.netshield_iot.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.mistxyt.netshield_iot.R

sealed class Screen(
    val route: String,
    @param:StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : Screen(
        route = "dashboard",
        titleRes = R.string.nav_dashboard,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    object Traffic : Screen(
        route = "traffic",
        titleRes = R.string.nav_traffic,
        selectedIcon = Icons.Filled.Sensors,
        unselectedIcon = Icons.Outlined.Sensors
    )

    object Blocklist : Screen(
        route = "blocklist",
        titleRes = R.string.nav_blocklist,
        selectedIcon = Icons.Filled.Block,
        unselectedIcon = Icons.Outlined.Block
    )

    object Devices : Screen(
        route = "devices",
        titleRes = R.string.nav_devices,
        selectedIcon = Icons.Filled.Devices,
        unselectedIcon = Icons.Outlined.Devices
    )

    object Settings : Screen(
        route = "settings",
        titleRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val bottomNavItems = listOf(Dashboard, Traffic, Blocklist, Devices, Settings)
    }
}
