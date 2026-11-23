package com.mistxyt.netshield_iot.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mistxyt.netshield_iot.R
import com.mistxyt.netshield_iot.domain.model.TrafficPoint
import com.mistxyt.netshield_iot.ui.theme.StatusBlockedRed

@Composable
fun TrafficAnalyticsChart(
    points: List<TrafficPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.traffic_analytics_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.traffic_analytics_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = stringResource(R.string.stat_total_requests)
                    )
                    LegendItem(
                        color = StatusBlockedRed,
                        label = stringResource(R.string.stat_blocked_requests)
                    )
                }
            }

            if (points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.traffic_chart_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 24.dp.toPx()
                    val chartHeight = height - bottomPadding

                    val maxVal = points.maxOfOrNull { it.totalQueries }?.toFloat()?.coerceAtLeast(10f) ?: 100f
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    // Draw 3 horizontal guideline dashed lines
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 1..3) {
                        val y = chartHeight * (i / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = dashEffect
                        )
                    }

                    // Total queries curve & gradient area
                    val totalPath = Path()
                    val totalFillPath = Path()
                    val blockedPath = Path()

                    points.forEachIndexed { index, point ->
                        val x = index * stepX
                        val yTotal = chartHeight - (point.totalQueries / maxVal) * chartHeight
                        val yBlocked = chartHeight - (point.blockedQueries / maxVal) * chartHeight

                        if (index == 0) {
                            totalPath.moveTo(x, yTotal)
                            totalFillPath.moveTo(x, chartHeight)
                            totalFillPath.lineTo(x, yTotal)
                            blockedPath.moveTo(x, yBlocked)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevYTotal = chartHeight - (points[index - 1].totalQueries / maxVal) * chartHeight
                            val prevYBlocked = chartHeight - (points[index - 1].blockedQueries / maxVal) * chartHeight

                            val controlX1 = prevX + (x - prevX) / 2
                            totalPath.cubicTo(controlX1, prevYTotal, controlX1, yTotal, x, yTotal)
                            totalFillPath.cubicTo(controlX1, prevYTotal, controlX1, yTotal, x, yTotal)

                            blockedPath.cubicTo(controlX1, prevYBlocked, controlX1, yBlocked, x, yBlocked)
                        }
                    }

                    // Complete totalFillPath
                    totalFillPath.lineTo(width, chartHeight)
                    totalFillPath.close()

                    // Draw fill gradient
                    drawPath(
                        path = totalFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.25f),
                                primaryColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw line paths
                    drawPath(
                        path = totalPath,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = blockedPath,
                        color = StatusBlockedRed,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw points on total line
                    points.forEachIndexed { index, point ->
                        val x = index * stepX
                        val yTotal = chartHeight - (point.totalQueries / maxVal) * chartHeight
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, yTotal)
                        )
                    }
                }

                // Time axis labels below the chart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEach { point ->
                        Text(
                            text = point.label,
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
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
