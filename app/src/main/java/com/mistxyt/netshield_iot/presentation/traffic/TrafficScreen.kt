package com.mistxyt.netshield_iot.presentation.traffic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.unit.sp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.presentation.components.DnsEventDetailDialog
import com.mistxyt.netshield_iot.presentation.components.DnsEventRow
import com.mistxyt.netshield_iot.presentation.components.categoryDisplayName
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ergonomic Stream Status & Quick Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Status Pill
                    val dotColor = if (!uiState.isConnected) {
                        StatusBlockedRed
                    } else if (uiState.isPaused) {
                        StatusWarningAmber
                    } else {
                        StatusAllowedGreen
                    }
                    val statusLabel = if (!uiState.isConnected) {
                        stringResource(R.string.traffic_ws_disconnected)
                    } else if (uiState.isPaused) {
                        stringResource(R.string.traffic_paused_label)
                    } else {
                        stringResource(R.string.traffic_live_label)
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = dotColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, dotColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(dotColor)
                            )
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = dotColor
                            )
                        }
                    }

                    // Action buttons: Pause / Resume & Clear
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = { viewModel.togglePause() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isPaused) stringResource(R.string.traffic_resume) else stringResource(R.string.traffic_pause),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { viewModel.clearEvents() }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteSweep,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Search Bar
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = {
                        Text(
                            stringResource(R.string.traffic_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.action_delete),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                )

                // Segmented Action Filter Pills (Diblokir Saja, Semua, Diizinkan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.actionFilter == TrafficFilterAction.BLOCKED,
                        onClick = { viewModel.setActionFilter(TrafficFilterAction.BLOCKED) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (uiState.actionFilter == TrafficFilterAction.BLOCKED) StatusBlockedRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(stringResource(R.string.traffic_filter_blocked_only), fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(999.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.actionFilter == TrafficFilterAction.BLOCKED,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            selectedBorderColor = StatusBlockedRed,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = StatusBlockedRed.copy(alpha = 0.15f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedLabelColor = StatusBlockedRed
                        )
                    )
                    FilterChip(
                        selected = uiState.actionFilter == TrafficFilterAction.ALL,
                        onClick = { viewModel.setActionFilter(TrafficFilterAction.ALL) },
                        label = { Text(stringResource(R.string.traffic_filter_all), fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(999.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.actionFilter == TrafficFilterAction.ALL,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = uiState.actionFilter == TrafficFilterAction.ALLOWED,
                        onClick = { viewModel.setActionFilter(TrafficFilterAction.ALLOWED) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (uiState.actionFilter == TrafficFilterAction.ALLOWED) StatusAllowedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(stringResource(R.string.traffic_filter_allowed), fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(999.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.actionFilter == TrafficFilterAction.ALLOWED,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            selectedBorderColor = StatusAllowedGreen,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = StatusAllowedGreen.copy(alpha = 0.15f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedLabelColor = StatusAllowedGreen
                        )
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
                        val isSelected = uiState.categoryFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCategoryFilter(category) },
                            label = { Text(categoryDisplayName(category), style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(999.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                    icon = Icons.Rounded.Insights,
                    message = stringResource(R.string.traffic_empty_state)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.traffic_events_observed, filtered.size),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.actionFilter == TrafficFilterAction.BLOCKED) {
                            Text(
                                text = stringResource(R.string.traffic_normal_logs_hidden),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                items(
                    items = filtered,
                    key = { it.id }
                ) { event ->
                    DnsEventRow(
                        event = event,
                        onClick = { viewModel.selectEvent(event) }
                    )
                }
            }
        }

        // DNS Event Detail Modal Dialog
        uiState.selectedEvent?.let { selected ->
            DnsEventDetailDialog(
                event = selected,
                onDismiss = { viewModel.selectEvent(null) }
            )
        }
    }
}

