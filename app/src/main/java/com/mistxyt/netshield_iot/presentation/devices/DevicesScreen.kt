package com.mistxyt.netshield_iot.presentation.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.core.util.DateTimeHelper
import com.mistxyt.netshield_iot.domain.model.Device
import com.mistxyt.netshield_iot.domain.model.DeviceType
import com.mistxyt.netshield_iot.domain.model.DnsAction
import com.mistxyt.netshield_iot.domain.model.DnsEvent
import com.mistxyt.netshield_iot.presentation.components.ActionBadge
import com.mistxyt.netshield_iot.presentation.components.EmptyStateView
import com.mistxyt.netshield_iot.presentation.components.categoryDisplayName
import com.mistxyt.netshield_iot.ui.theme.StatusAllowedGreen
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed
import java.text.NumberFormat
import java.util.Locale

/** Returns the natural rounded icon for each device category. */
fun deviceTypeIcon(type: DeviceType): ImageVector = when (type) {
    DeviceType.SMARTPHONE -> Icons.Rounded.Smartphone
    DeviceType.TABLET     -> Icons.Rounded.TabletAndroid
    DeviceType.LAPTOP     -> Icons.Rounded.Laptop
    DeviceType.ROUTER     -> Icons.Rounded.Router
    DeviceType.IOT        -> Icons.Rounded.Hub
    DeviceType.UNKNOWN    -> Icons.Rounded.Devices
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val selectedQueries by viewModel.selectedDeviceQueries.collectAsState()
    val editingDevice by viewModel.editingDevice.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ergonomic Device Header & Filter Bar
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
                    Column {
                        Text(
                            text = stringResource(R.string.devices_connected_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.devices_detected_on_wifi, uiState.devices.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Sort Button & Dropdown
                    Box {
                        FilledTonalButton(
                            onClick = { sortMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.devices_sort_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Rounded.Numbers, contentDescription = null) },
                                text = { Text(stringResource(R.string.devices_sort_requests)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.MOST_REQUESTS)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Rounded.Shield, contentDescription = null) },
                                text = { Text(stringResource(R.string.devices_sort_blocked)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.MOST_BLOCKED)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                                text = { Text(stringResource(R.string.devices_sort_recent)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.MOST_RECENT)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Rounded.Dns, contentDescription = null) },
                                text = { Text(stringResource(R.string.devices_sort_ip)) },
                                onClick = {
                                    viewModel.onSortOptionChange(DeviceSortOption.IP_ADDRESS)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Modern Search Bar
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = {
                        Text(
                            stringResource(R.string.devices_search_hint),
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
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        val sorted = uiState.sortedFilteredDevices
        if (sorted.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Rounded.Hub,
                    message = stringResource(R.string.devices_empty)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = sorted,
                    key = { it.id }
                ) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { viewModel.selectDevice(device) }
                    )
                }
            }
        }
    }

    // Bottom sheet for device detail
    selectedDevice?.let { dev ->
        val latestDev = uiState.devices.find { it.id == dev.id } ?: dev
        DeviceDetailBottomSheet(
            device = latestDev,
            queries = selectedQueries,
            onDismiss = { viewModel.dismissDeviceDetails() },
            onEditAlias = { viewModel.showEditDialog(latestDev) },
            onTogglePause = { viewModel.toggleInternetPause(latestDev) }
        )
    }

    // Edit alias/type dialog
    editingDevice?.let { dev ->
        EditDeviceDialog(
            device = dev,
            onConfirm = { name, type ->
                viewModel.updateDeviceAlias(dev, name, type)
                viewModel.hideEditDialog()
            },
            onDismiss = { viewModel.hideEditDialog() }
        )
    }
}

// ---------------------------------------------------------------------------
// Device Card
// ---------------------------------------------------------------------------

@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    val isPaused = device.isInternetPaused

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaused)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            if (isPaused) StatusBlockedRed.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: icon, name, badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPaused) StatusBlockedRed.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = deviceTypeIcon(device.deviceType),
                            contentDescription = null,
                            tint = if (isPaused) StatusBlockedRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitle = buildString {
                            append(device.ipAddress)
                            if (!device.detectedModel.isNullOrBlank() &&
                                device.detectedModel != device.displayName) {
                                append(" • ${device.detectedModel}")
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPaused) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(StatusBlockedRed.copy(alpha = 0.12f))
                                .border(0.6.dp, StatusBlockedRed.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.device_paused_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusBlockedRed
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (device.blockedCount > 0)
                                        StatusBlockedRed.copy(alpha = 0.1f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    0.5.dp,
                                    if (device.blockedCount > 0) StatusBlockedRed.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(999.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${device.blockRatioPercentage}% Blokir",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (device.blockedCount > 0) StatusBlockedRed
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.action_details),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.8.dp
            )

            // Metrics row: 3 balanced tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(label = stringResource(R.string.devices_total_queries), value = formatter.format(device.requestCount))
                MetricColumn(label = stringResource(R.string.devices_blocked_queries), value = formatter.format(device.blockedCount), valueColor = StatusBlockedRed)
                MetricColumn(label = stringResource(R.string.devices_last_seen), value = DateTimeHelper.formatWibTime(device.lastActivity))
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

// ---------------------------------------------------------------------------
// Device Detail Bottom Sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailBottomSheet(
    device: Device,
    queries: List<DnsEvent>,
    onDismiss: () -> Unit,
    onEditAlias: () -> Unit,
    onTogglePause: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    var filterBlockedOnly by remember { mutableStateOf(false) }

    val displayedQueries = remember(queries, filterBlockedOnly) {
        if (filterBlockedOnly) queries.filter { it.action == DnsAction.BLOCK }
        else queries
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = deviceTypeIcon(device.deviceType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = device.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!device.detectedModel.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.device_model_label, device.detectedModel ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${device.ipAddress} • MAC: ${device.macAddress ?: "Dynamic (DHCP)"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (device.blockedCount > 0)
                                StatusBlockedRed.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${device.blockRatioPercentage}% Blokir",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (device.blockedCount > 0) StatusBlockedRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action buttons: Edit Alias & Internet Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onEditAlias,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.device_edit_alias))
                }

                val pauseColor = if (device.isInternetPaused) StatusBlockedRed else StatusAllowedGreen
                Button(
                    onClick = onTogglePause,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (device.isInternetPaused) StatusBlockedRed else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (device.isInternetPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (device.isInternetPaused) stringResource(R.string.device_resume_internet) else stringResource(R.string.device_pause_internet))
                }
            }

            // Internet Pause warning if paused
            if (device.isInternetPaused) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = StatusBlockedRed.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, StatusBlockedRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = StatusBlockedRed, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.device_paused_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusBlockedRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Metric Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCardTile(stringResource(R.string.devices_total_queries), formatter.format(device.requestCount), Modifier.weight(1f))
                StatCardTile(stringResource(R.string.traffic_filter_allowed), formatter.format(device.allowedCount), Modifier.weight(1f), StatusAllowedGreen)
                StatCardTile(stringResource(R.string.traffic_filter_blocked), formatter.format(device.blockedCount), Modifier.weight(1f), StatusBlockedRed)
            }

            // Last active
            Text(
                text = stringResource(R.string.device_last_activity_label, DateTimeHelper.formatWibFullDateTime(device.lastActivity)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            // Query Log Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.device_query_history, displayedQueries.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilterChip(
                    selected = filterBlockedOnly,
                    onClick = { filterBlockedOnly = !filterBlockedOnly },
                    label = { Text(stringResource(R.string.device_blocked_only), style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (filterBlockedOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                )
            }

            if (displayedQueries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filterBlockedOnly) stringResource(R.string.device_no_blocked)
                               else stringResource(R.string.device_no_queries),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = displayedQueries,
                        key = { it.id }
                    ) { q ->
                        DeviceQueryRow(event = q)
                    }
                }
            }

            // Bottom action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { clipboardManager.setText(AnnotatedString(device.ipAddress)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_copy_ip))
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}

@Composable
private fun StatCardTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

// ---------------------------------------------------------------------------
// Edit Device Alias/Type Dialog
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeviceDialog(
    device: Device,
    onConfirm: (name: String, type: DeviceType) -> Unit,
    onDismiss: () -> Unit
) {
    var nameText by remember { mutableStateOf(device.customName ?: "") }
    var selectedType by remember { mutableStateOf(device.deviceType) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val deviceTypeLabels = mapOf(
        DeviceType.SMARTPHONE to stringResource(R.string.device_type_smartphone),
        DeviceType.TABLET      to stringResource(R.string.device_type_tablet),
        DeviceType.LAPTOP     to stringResource(R.string.device_type_laptop),
        DeviceType.ROUTER     to stringResource(R.string.device_type_router),
        DeviceType.IOT         to stringResource(R.string.device_type_iot),
        DeviceType.UNKNOWN    to stringResource(R.string.device_type_other)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.device_edit_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (!device.detectedModel.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = stringResource(R.string.device_detected_model, device.detectedModel ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text(stringResource(R.string.device_alias_label)) },
                    placeholder = { Text(device.displayName) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = deviceTypeLabels[selectedType] ?: stringResource(R.string.device_type_other),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.device_type_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        DeviceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = deviceTypeIcon(type),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(deviceTypeLabels[type] ?: stringResource(R.string.device_type_other))
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameText, selectedType) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ---------------------------------------------------------------------------
// Query Row
// ---------------------------------------------------------------------------

@Composable
fun DeviceQueryRow(
    event: DnsEvent,
    modifier: Modifier = Modifier
) {
    val isBlocked = event.action == DnsAction.BLOCK
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = event.domain,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBlocked) StatusBlockedRed else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${categoryDisplayName(event.category)} • ${DateTimeHelper.formatWibTime(event.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ActionBadge(isBlocked = isBlocked)
        }
    }
}



