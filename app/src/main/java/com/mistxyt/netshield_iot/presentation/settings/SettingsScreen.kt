package com.mistxyt.netshield_iot.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.domain.repository.AppLanguage
import com.mistxyt.netshield_iot.domain.repository.AppThemeMode
import com.mistxyt.netshield_iot.ui.theme.StatusAllowedGreen
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var ipInput by rememberSaveable { mutableStateOf(uiState.settings.esp32Ip) }
    var restPortInput by rememberSaveable { mutableStateOf(uiState.settings.restPort.toString()) }
    var wsPortInput by rememberSaveable { mutableStateOf(uiState.settings.wsPort.toString()) }

    // Sync input once settings are loaded if field was empty or at initial state
    LaunchedEffect(uiState.settings.esp32Ip) {
        if (ipInput.isBlank() || ipInput == "192.168.1.10" || ipInput == "192.168.43.1") {
            ipInput = uiState.settings.esp32Ip
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Operating Mode Section
            SettingsSection(
                title = stringResource(R.string.settings_section_mode),
                icon = Icons.Outlined.Tune
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = stringResource(R.string.settings_demo_mode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.settings_demo_mode_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.settings.isDemoMode,
                                onCheckedChange = { viewModel.setDemoMode(it) }
                            )
                        }

                        if (!uiState.settings.isDemoMode) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Sensors,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.settings_live_mode_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hardware Connection Section
            SettingsSection(
                title = stringResource(R.string.settings_section_hardware),
                icon = Icons.Outlined.Router
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { ipInput = it },
                            label = { Text(stringResource(R.string.settings_esp32_ip)) },
                            placeholder = { Text(stringResource(R.string.settings_esp32_ip_hint)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = restPortInput,
                                onValueChange = { restPortInput = it },
                                label = { Text(stringResource(R.string.settings_esp32_http_port)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = wsPortInput,
                                onValueChange = { wsPortInput = it },
                                label = { Text(stringResource(R.string.settings_esp32_ws_port)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Test Connection Button & Result
                        Button(
                            onClick = {
                                viewModel.updateEsp32Ip(ipInput)
                                restPortInput.toIntOrNull()?.let { p -> viewModel.updateRestPort(p) }
                                wsPortInput.toIntOrNull()?.let { p -> viewModel.updateWsPort(p) }
                                viewModel.testConnection(ipInput, restPortInput.toIntOrNull() ?: 80)
                            },
                            enabled = uiState.testState !is TestConnectionState.Testing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (uiState.testState is TestConnectionState.Testing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.settings_testing))
                            } else {
                                Icon(imageVector = Icons.Outlined.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.settings_test_connection))
                            }
                        }

                        when (val state = uiState.testState) {
                            is TestConnectionState.Success -> {
                                Text(
                                    text = stringResource(R.string.settings_test_success),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusAllowedGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            is TestConnectionState.Failed -> {
                                Text(
                                    text = "${stringResource(R.string.settings_test_failed)} (${state.error})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusBlockedRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            // 3. Appearance Section
            SettingsSection(
                title = stringResource(R.string.settings_section_appearance),
                icon = Icons.Outlined.Palette
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeOptionRow(
                            title = stringResource(R.string.settings_theme_system),
                            selected = uiState.settings.themeMode == AppThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
                        )
                        ThemeOptionRow(
                            title = stringResource(R.string.settings_theme_light),
                            selected = uiState.settings.themeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) }
                        )
                        ThemeOptionRow(
                            title = stringResource(R.string.settings_theme_dark),
                            selected = uiState.settings.themeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) }
                        )
                    }
                }
            }

            // 4. Language Section
            SettingsSection(
                title = stringResource(R.string.settings_section_language),
                icon = Icons.Outlined.Translate
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeOptionRow(
                            title = stringResource(R.string.settings_lang_system),
                            selected = uiState.settings.language == AppLanguage.SYSTEM,
                            onClick = { viewModel.setLanguage(AppLanguage.SYSTEM) }
                        )
                        ThemeOptionRow(
                            title = stringResource(R.string.settings_lang_en),
                            selected = uiState.settings.language == AppLanguage.EN,
                            onClick = { viewModel.setLanguage(AppLanguage.EN) }
                        )
                        ThemeOptionRow(
                            title = stringResource(R.string.settings_lang_id),
                            selected = uiState.settings.language == AppLanguage.ID,
                            onClick = { viewModel.setLanguage(AppLanguage.ID) }
                        )
                    }
                }
            }

            // 5. Data & Local Persistence
            SettingsSection(
                title = stringResource(R.string.settings_section_storage),
                icon = Icons.Outlined.Storage
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.settings_clear_cache_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { viewModel.clearCache() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_clear_cache))
                        }
                    }
                }
            }

            // 6. About NetShield
            SettingsSection(
                title = stringResource(R.string.settings_section_about),
                icon = Icons.Outlined.Info
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_about_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        AboutRow(
                            label = stringResource(R.string.settings_version_label),
                            value = stringResource(R.string.settings_version_value)
                        )
                        AboutRow(
                            label = stringResource(R.string.settings_architecture_label),
                            value = stringResource(R.string.settings_architecture_value)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_security_note),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
private fun AboutRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
