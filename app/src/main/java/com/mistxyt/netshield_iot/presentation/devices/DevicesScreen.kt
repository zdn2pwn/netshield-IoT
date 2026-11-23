package com.mistxyt.netshield_iot.presentation.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.domain.model.Device
import com.mistxyt.netshield_iot.presentation.components.EmptyStateView
import com.mistxyt.netshield_iot.ui.theme.StatusAllowedGreen
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.devices_screen_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.devices.size} devices observed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Sort Menu
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Sort,
                                contentDescription = stringResource(R.string.devices_sort_label),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.devices_sort_requests)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.MOST_REQUESTS)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.devices_sort_blocked)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.MOST_BLOCKED)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.devices_sort_recent)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.MOST_RECENT)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.devices_sort_ip)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.IP_ADDRESS)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }

                    // Refresh
                    IconButton(onClick = { viewModel.refresh() }) {
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
            // Search Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text(stringResource(R.string.devices_search_hint)) },
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
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            val sorted = uiState.sortedFilteredDevices
            if (sorted.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.DevicesOther,
                        message = stringResource(R.string.devices_empty)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Honest DNS disclaimer note
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.devices_disclaimer),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(
                        items = sorted,
                        key = { it.id }
                    ) { device ->
                        DeviceCard(device = device)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Device IP and MAC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Computer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = device.ipAddress,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (device.macAddress != null) {
                            Text(
                                text = device.macAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Block ratio badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (device.blockedCount > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${device.blockRatioPercentage}% blocked",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (device.blockedCount > 0) StatusBlockedRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                thickness = 0.8.dp
            )

            // Metrics row: Total, Blocked, Last Active
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.devices_total_queries),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatter.format(device.requestCount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.devices_blocked_queries),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatter.format(device.blockedCount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusBlockedRed
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.devices_last_seen),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val dateFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(device.lastActivity))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
