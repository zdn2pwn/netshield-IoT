package com.mistxyt.netshield_iot.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mistxyt.netshield_iot.presentation.blocklist.BlocklistScreen
import com.mistxyt.netshield_iot.presentation.blocklist.BlocklistViewModel
import com.mistxyt.netshield_iot.presentation.dashboard.DashboardScreen
import com.mistxyt.netshield_iot.presentation.dashboard.DashboardViewModel
import com.mistxyt.netshield_iot.presentation.devices.DevicesScreen
import com.mistxyt.netshield_iot.presentation.devices.DevicesViewModel
import com.mistxyt.netshield_iot.presentation.settings.SettingsScreen
import com.mistxyt.netshield_iot.presentation.settings.SettingsViewModel
import com.mistxyt.netshield_iot.presentation.traffic.TrafficScreen
import com.mistxyt.netshield_iot.presentation.traffic.TrafficViewModel

@Composable
fun NetShieldMainApp(
    dashboardViewModel: DashboardViewModel,
    trafficViewModel: TrafficViewModel,
    blocklistViewModel: BlocklistViewModel,
    devicesViewModel: DevicesViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = stringResource(screen.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(screen.titleRes),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToTraffic = {
                        navController.navigate(Screen.Traffic.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Traffic.route) {
                TrafficScreen(viewModel = trafficViewModel)
            }

            composable(Screen.Blocklist.route) {
                BlocklistScreen(viewModel = blocklistViewModel)
            }

            composable(Screen.Devices.route) {
                DevicesScreen(viewModel = devicesViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
