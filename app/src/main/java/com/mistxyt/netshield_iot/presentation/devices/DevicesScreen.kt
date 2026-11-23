package com.mistxyt.netshield_iot.presentation.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.*
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
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.core.util.DateTimeHelper
import com.mistxyt.netshield_iot.domain.model.Device
import com.mistxyt.netshield_iot.domain.model.DeviceType
import com.mistxyt.netshield_iot.domain.model.DnsAction
import com.mistxyt.netshield_iot.domain.model.DnsEvent
import com.mistxyt.netshield_iot.presentation.components.ActionBadge
import com.mistxyt.netshield_iot.presentation.components.EmptyStateView
import com.mistxyt.netshield_iot.ui.theme.StatusAllowedGreen
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed
import java.text.NumberFormat
import java.util.Locale

/** Returns the Material icon that best represents a device type. */
fun deviceTypeIcon(type: DeviceType): ImageVector = when (type) {
    DeviceType.SMARTPHONE -> Icons.Outlined.Smartphone
    DeviceType.TABLET     -> Icons.Outlined.TabletAndroid
    DeviceType.LAPTOP     -> Icons.Outlined.Laptop
    DeviceType.ROUTER     -> Icons.Outlined.Router
    DeviceType.IOT        -> Icons.Outlined.DevicesOther
    DeviceType.UNKNOWN    -> Icons.Outlined.DeviceUnknown
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
            // Always reflect latest data from the live list
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isInternetPaused)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (device.isInternetPaused) StatusBlockedRed.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: icon, name, badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Device type icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = deviceTypeIcon(device.deviceType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        // Display name (alias > detected model > IP fallback)
                        Text(
                            text = device.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Show IP as subtitle (and detected model if different from displayName)
                        val subtitle = buildString {
                            append(device.ipAddress)
                            if (!device.detectedModel.isNullOrBlank() &&
                                device.detectedModel != device.displayName) {
                                append("  •  ${device.detectedModel}")
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
                    // Paused badge
                    if (device.isInternetPaused) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusBlockedRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusBlockedRed
                            )
                        }
                    } else {
                        // Block ratio badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (device.blockedCount > 0)
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${device.blockRatioPercentage}% blocked",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (device.blockedCount > 0) StatusBlockedRed
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Detail",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                thickness = 0.8.dp
            )

            // Metrics row
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
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
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
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = deviceTypeIcon(device.deviceType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
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
                        // Show detected OS/model
                        if (!device.detectedModel.isNullOrBlank()) {
                            Text(
                                text = device.detectedModel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${device.ipAddress}  •  MAC: ${device.macAddress ?: "Dynamic (DHCP)"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (device.blockedCount > 0)
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${device.blockRatioPercentage}% Blokir",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (device.blockedCount > 0) StatusBlockedRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Action buttons: Edit Alias & Internet Pause ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditAlias,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Edit Alias")
                }

                // Internet Pause toggle button
                val pauseColor = if (device.isInternetPaused) StatusBlockedRed else StatusAllowedGreen
                Button(
                    onClick = onTogglePause,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pauseColor.copy(alpha = 0.15f),
                        contentColor = pauseColor
                    )
                ) {
                    Icon(
                        imageVector = if (device.isInternetPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (device.isInternetPaused) "Resume" else "Pause Internet")
                }
            }

            // Internet Pause warning
            if (device.isInternetPaused) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusBlockedRed.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, StatusBlockedRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = StatusBlockedRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Internet diblokir total — semua kueri DNS dikembalikan 0.0.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusBlockedRed
                        )
                    }
                }
            }

            // ── Metric Summary Cards ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Total Kueri", formatter.format(device.requestCount), Modifier.weight(1f))
                StatCard("Diizinkan", formatter.format(device.allowedCount), Modifier.weight(1f), StatusAllowedGreen)
                StatCard("Diblokir", formatter.format(device.blockedCount), Modifier.weight(1f), StatusBlockedRed)
            }

            // Last active
            Text(
                text = "Aktivitas Terakhir: ${DateTimeHelper.formatWibFullDateTime(device.lastActivity)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            // ── Query Log ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Akses Domain (${displayedQueries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FilterChip(
                    selected = filterBlockedOnly,
                    onClick = { filterBlockedOnly = !filterBlockedOnly },
                    label = { Text("Hanya Diblokir", style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(6.dp)
                )
            }

            if (displayedQueries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filterBlockedOnly) "Tidak ada domain diblokir untuk perangkat ini."
                               else "Belum ada domain yang tercatat untuk perangkat ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = displayedQueries,
                        key = { it.id }
                    ) { q ->
                        DeviceQueryRow(event = q)
                    }
                }
            }

            // ── Bottom action buttons ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { clipboardManager.setText(AnnotatedString(device.ipAddress)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin IP")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
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
        DeviceType.SMARTPHONE to "Smartphone",
        DeviceType.TABLET      to "Tablet",
        DeviceType.LAPTOP      to "Laptop / PC",
        DeviceType.ROUTER      to "Router / Gateway",
        DeviceType.IOT         to "IoT / Smart Device",
        DeviceType.UNKNOWN     to "Unknown"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Perangkat", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Show detected model as hint
                if (!device.detectedModel.isNullOrBlank()) {
                    Text(
                        text = "Terdeteksi sebagai: ${device.detectedModel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nama / Alias") },
                    placeholder = { Text(device.displayName) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Device type dropdown
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = deviceTypeLabels[selectedType] ?: "Unknown",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipe Perangkat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        shape = RoundedCornerShape(8.dp),
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = deviceTypeIcon(type),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(deviceTypeLabels[type] ?: type.name)
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
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(16.dp)
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
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
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBlocked) StatusBlockedRed else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.category} • ${DateTimeHelper.formatWibTime(event.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ActionBadge(isBlocked = isBlocked)
        }
    }
}
