package com.mistxyt.netshield_iot.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.unit.sp
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernStatCard(
                title = stringResource(R.string.stat_total_requests),
                value = formatter.format(stats.totalRequests),
                icon = Icons.Rounded.QueryStats,
                accentColor = MaterialTheme.colorScheme.primary,
                subtext = stringResource(R.string.stat_subtext_all_queries),
                badgeText = null,
                modifier = Modifier.weight(1f)
            )
            val blockedPct = String.format(Locale.US, "%.1f%%", stats.blockedPercentage)
            ModernStatCard(
                title = stringResource(R.string.stat_blocked_requests),
                value = formatter.format(stats.blockedRequests),
                icon = Icons.Rounded.Shield,
                accentColor = StatusBlockedRed,
                subtext = stringResource(R.string.stat_subtext_threats_prevented),
                badgeText = stringResource(R.string.stat_blocked_badge, blockedPct),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val allowedPct = String.format(Locale.US, "%.1f%%", stats.allowedPercentage)
            ModernStatCard(
                title = stringResource(R.string.stat_allowed_requests),
                value = formatter.format(stats.allowedRequests),
                icon = Icons.Rounded.CheckCircle,
                accentColor = StatusAllowedGreen,
                subtext = stringResource(R.string.stat_subtext_clean_traffic),
                badgeText = stringResource(R.string.stat_safe_badge, allowedPct),
                modifier = Modifier.weight(1f)
            )
            ModernStatCard(
                title = stringResource(R.string.stat_unique_devices),
                value = formatter.format(stats.uniqueDevices),
                icon = Icons.Rounded.Hub,
                accentColor = StatusInfoBlue,
                subtext = stringResource(R.string.stat_subtext_clients_connected),
                badgeText = null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    subtext: String,
    badgeText: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
