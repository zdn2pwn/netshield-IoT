package com.mistxyt.netshield_iot.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.Tune
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
        selectedIcon = Icons.Rounded.SpaceDashboard,
        unselectedIcon = Icons.Outlined.SpaceDashboard
    )

    object Traffic : Screen(
        route = "traffic",
        titleRes = R.string.nav_traffic,
        selectedIcon = Icons.Rounded.Insights,
        unselectedIcon = Icons.Outlined.Insights
    )

    object Blocklist : Screen(
        route = "blocklist",
        titleRes = R.string.nav_blocklist,
        selectedIcon = Icons.Rounded.Shield,
        unselectedIcon = Icons.Outlined.Shield
    )

    object Devices : Screen(
        route = "devices",
        titleRes = R.string.nav_devices,
        selectedIcon = Icons.Rounded.Hub,
        unselectedIcon = Icons.Outlined.Hub
    )

    object Settings : Screen(
        route = "settings",
        titleRes = R.string.nav_settings,
        selectedIcon = Icons.Rounded.Tune,
        unselectedIcon = Icons.Outlined.Tune
    )

    companion object {
        val navItems: List<Screen>
            get() = listOf(Dashboard, Traffic, Blocklist, Devices, Settings)

        // Backward compatibility
        val bottomNavItems: List<Screen>
            get() = navItems
    }
}
