package com.mistxyt.netshield_iot.presentation.blocklist

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.domain.model.BlockedDomain
import com.mistxyt.netshield_iot.presentation.components.AddDomainDialog
import com.mistxyt.netshield_iot.presentation.components.ConfirmDeleteDialog
import com.mistxyt.netshield_iot.presentation.components.EmptyStateView
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedBgLight
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistScreen(
    viewModel: BlocklistViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var domainToDelete by remember { mutableStateOf<BlockedDomain?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val categories = listOf("All", "Malware", "Phishing", "Gambling", "Adult Content", "Ads & Trackers", "Custom")

    LaunchedEffect(uiState.feedbackMessage, uiState.errorMessage) {
        val msg = uiState.feedbackMessage ?: uiState.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.blocklist_screen_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.domains.size} active rules",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Sync with ESP32
                    IconButton(
                        onClick = { viewModel.syncWithEsp32() },
                        enabled = !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = stringResource(R.string.blocklist_sync_action),
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.blocklist_add_domain)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter Chips
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
                    placeholder = { Text(stringResource(R.string.blocklist_search_hint)) },
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

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.onCategorySelect(category) },
                            label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            val filtered = uiState.filteredDomains
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.Shield,
                        message = stringResource(R.string.blocklist_empty)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filtered,
                        key = { it.id }
                    ) { domainItem ->
                        BlockedDomainCard(
                            domain = domainItem,
                            onDelete = { domainToDelete = domainItem }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDomainDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { domain, category ->
                viewModel.addDomain(domain, category)
                showAddDialog = false
            }
        )
    }

    if (domainToDelete != null) {
        ConfirmDeleteDialog(
            domainName = domainToDelete!!.domain,
            onDismiss = { domainToDelete = null },
            onConfirm = {
                viewModel.removeDomain(domainToDelete!!.id, domainToDelete!!.domain)
                domainToDelete = null
            }
        )
    }
}

@Composable
fun BlockedDomainCard(
    domain: BlockedDomain,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = domain.domain,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusBlockedBgLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = domain.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = StatusBlockedRed
                        )
                    }

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Text(
                        text = domain.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(domain.createdAt))
                    Text(
                        text = "• $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
