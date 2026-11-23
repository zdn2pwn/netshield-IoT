package com.mistxyt.netshield_iot.presentation.traffic

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.presentation.components.DnsEventRow
import com.mistxyt.netshield_iot.presentation.components.EmptyStateView
import com.mistxyt.netshield_iot.ui.theme.StatusAllowedGreen
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed
import com.mistxyt.netshield_iot.ui.theme.StatusWarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficScreen(
    viewModel: TrafficViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("All", "Malware", "Phishing", "Gambling", "Adult Content", "Ads & Trackers", "Development", "General")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.traffic_screen_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val dotColor = if (!uiState.isConnected) {
                                StatusBlockedRed
                            } else if (uiState.isPaused) {
                                StatusWarningAmber
                            } else {
                                StatusAllowedGreen
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(dotColor)
                            )
                            val statusLabel = if (!uiState.isConnected) {
                                stringResource(R.string.traffic_ws_disconnected)
                            } else if (uiState.isPaused) {
                                stringResource(R.string.traffic_paused_label)
                            } else {
                                stringResource(R.string.traffic_live_label)
                            }
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Pause / Resume Stream
                    IconButton(onClick = { viewModel.togglePause() }) {
                        Icon(
                            imageVector = if (uiState.isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                            contentDescription = if (uiState.isPaused) stringResource(R.string.traffic_resume) else stringResource(R.string.traffic_pause),
                            tint = if (uiState.isPaused) StatusWarningAmber else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Refresh Historical
                    IconButton(onClick = { viewModel.refreshHistorical() }) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.action_refresh)
                            )
                        }
                    }

                    // Clear stream
                    IconButton(onClick = { viewModel.clearEvents() }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = stringResource(R.string.action_delete)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text(stringResource(R.string.traffic_search_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Filter Chips (All, Allowed, Blocked)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.actionFilter == TrafficFilterAction.ALL,
                        onClick = { viewModel.setActionFilter(TrafficFilterAction.ALL) },
                        label = { Text(stringResource(R.string.traffic_filter_all)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = uiState.actionFilter == TrafficFilterAction.ALLOWED,
                        onClick = { viewModel.setActionFilter(TrafficFilterAction.ALLOWED) },
                        label = { Text(stringResource(R.string.traffic_filter_allowed)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = uiState.actionFilter == TrafficFilterAction.BLOCKED,
                        onClick = { viewModel.setActionFilter(TrafficFilterAction.BLOCKED) },
                        label = { Text(stringResource(R.string.traffic_filter_blocked)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Horizontal Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = uiState.categoryFilter == category,
                            onClick = { viewModel.setCategoryFilter(category) },
                            label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Live Events List
            val filtered = uiState.filteredEvents
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.SearchOff,
                        message = stringResource(R.string.traffic_empty_state)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.traffic_events_count, filtered.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(
                        items = filtered,
                        key = { it.id }
                    ) { event ->
                        DnsEventRow(event = event)
                    }
                }
            }
        }
    }
}
