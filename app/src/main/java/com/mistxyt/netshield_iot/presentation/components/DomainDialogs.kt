package com.mistxyt.netshield_iot.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.core.util.DateTimeHelper
import com.mistxyt.netshield_iot.domain.model.DnsAction
import com.mistxyt.netshield_iot.domain.model.DnsEvent
import com.mistxyt.netshield_iot.ui.theme.StatusAllowedGreen
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed

private val DOMAIN_REGEX = Regex("^[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)+$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDomainDialog(
    onDismiss: () -> Unit,
    onConfirm: (domain: String, category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var domainText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Malware") }
    var isError by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Malware",
        "Phishing",
        "Gambling",
        "Adult Content",
        "Ads & Trackers",
        "Custom"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.blocklist_add_domain),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = domainText,
                    onValueChange = {
                        domainText = it.trim().lowercase()
                        isError = false
                    },
                    label = { Text(stringResource(R.string.blocklist_domain_label)) },
                    placeholder = { Text(stringResource(R.string.blocklist_domain_hint)) },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = stringResource(R.string.blocklist_domain_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.blocklist_category_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = domainText.trim().lowercase()
                    if (trimmed.isNotBlank() && DOMAIN_REGEX.matches(trimmed)) {
                        onConfirm(trimmed, selectedCategory)
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    )
}

@Composable
fun ConfirmDeleteDialog(
    domainName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.blocklist_delete_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.blocklist_delete_dialog_message, domainName),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.action_remove), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    )
}

@Composable
fun DnsEventDetailDialog(
    event: DnsEvent,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onFilterClientIp: ((String) -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val isBlocked = event.action == DnsAction.BLOCK

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detail Kueri DNS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                ActionBadge(isBlocked = isBlocked)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Domain Tujuan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = event.domain,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isBlocked) StatusBlockedRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                DetailItemRow(label = "Status Tindakan", value = if (isBlocked) "DIBLOKIR (Proteksi Aktif)" else "DIIZINKAN")
                DetailItemRow(label = "Kategori", value = event.category)
                DetailItemRow(label = "IP Klien", value = event.clientIp)
                DetailItemRow(label = "Tipe Kueri", value = event.queryType)
                DetailItemRow(label = "Waktu Akses", value = DateTimeHelper.formatWibFullDateTime(event.timestamp))
                DetailItemRow(label = "Mesin Filter", value = event.source)

                if (isBlocked) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = StatusBlockedRed.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, StatusBlockedRed.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Akses dicegat oleh NetShield IoT untuk menjaga keamanan jaringan lokal dari situs terlarang/berbahaya.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusBlockedRed,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(event.domain))
                        copied = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (copied) "Disalin!" else "Salin Domain")
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup")
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    )
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
