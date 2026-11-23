package com.mistxyt.netshield_iot.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTraffic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.action_refresh),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // A. Demo Mode Banner (if active)
            if (uiState.systemStatus.isDemoMode) {
                item {
                    DemoModeBanner()
                }
            }

            // B. System Status Card
            item {
                SystemStatusCard(status = uiState.systemStatus)
            }

            // C. Statistics Cards Grid
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
                    action = {
                        TextButton(onClick = onNavigateToTraffic) {
                            Text(
                                text = stringResource(R.string.action_view_all),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                )
            }

            if (uiState.recentEvents.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.History,
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
}
