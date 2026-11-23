package com.mistxyt.netshield_iot.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R

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
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Domain Input
                OutlinedTextField(
                    value = domainText,
                    onValueChange = {
                        domainText = it.trim().lowercase()
                        if (isError) isError = false
                    },
                    label = { Text(stringResource(R.string.blocklist_domain_label)) },
                    placeholder = { Text(stringResource(R.string.blocklist_domain_hint)) },
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text(stringResource(R.string.blocklist_domain_error), color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category Selection ExposedDropdown
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
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
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
                    val clean = domainText.trim().lowercase()
                    if (clean.isNotBlank() && DOMAIN_REGEX.matches(clean)) {
                        onConfirm(clean, selectedCategory)
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
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
                fontWeight = FontWeight.SemiBold
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
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.action_remove))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        modifier = modifier
    )
}
