package com.mistxyt.netshield_iot.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.domain.model.DashboardStats
import com.mistxyt.netshield_iot.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatsGrid(
    stats: DashboardStats,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = stringResource(R.string.stat_total_requests),
                value = formatter.format(stats.totalRequests),
                icon = Icons.Outlined.QueryStats,
                accentColor = MaterialTheme.colorScheme.primary,
                subtext = null,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_unique_devices),
                value = formatter.format(stats.uniqueDevices),
                icon = Icons.Outlined.Devices,
                accentColor = MaterialTheme.colorScheme.tertiary,
                subtext = null,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val blockedPct = String.format(Locale.US, "%.1f%%", stats.blockedPercentage)
            StatCard(
                title = stringResource(R.string.stat_blocked_requests),
                value = formatter.format(stats.blockedRequests),
                icon = Icons.Outlined.Block,
                accentColor = StatusBlockedRed,
                subtext = blockedPct,
                modifier = Modifier.weight(1f)
            )
            val allowedPct = String.format(Locale.US, "%.1f%%", stats.allowedPercentage)
            StatCard(
                title = stringResource(R.string.stat_allowed_requests),
                value = formatter.format(stats.allowedRequests),
                icon = Icons.Outlined.CheckCircle,
                accentColor = StatusAllowedGreen,
                subtext = allowedPct,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    subtext: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}
