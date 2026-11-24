package com.mistxyt.netshield_iot.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.presentation.components.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTraffic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Demo Mode Banner (if active)
        if (uiState.systemStatus.isDemoMode) {
            item {
                DemoModeBanner()
            }
        }

        // B. System Status Card (Hardware Hub)
        item {
            SystemStatusCard(status = uiState.systemStatus)
        }

        // C. Statistics Cards Grid (2x2)
        item {
            StatsGrid(stats = uiState.stats)
        }

        // D. Traffic Analytics Chart
        item {
            TrafficAnalyticsChart(points = uiState.trafficPoints)
        }

        // E. Recent DNS Activity
        item {
            SectionHeader(
                title = stringResource(R.string.recent_activity_title),
                subtitle = "Aktivitas domain yang baru saja diakses",
                action = {
                    TextButton(onClick = onNavigateToTraffic) {
                        Text(
                            text = stringResource(R.string.action_view_all),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }

        if (uiState.recentEvents.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Rounded.History,
                    message = stringResource(R.string.recent_activity_empty)
                )
            }
        } else {
            items(
                items = uiState.recentEvents.take(6),
                key = { it.id }
            ) { event ->
                DnsEventRow(event = event)
            }
        }

        // F. Threat Intelligence Summary
        item {
            ThreatIntelCard(intel = uiState.threatIntel)
        }
    }
}
