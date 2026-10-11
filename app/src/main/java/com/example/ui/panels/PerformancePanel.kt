package com.example.ui.panels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PerformanceMetrics
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose

@Composable
fun PerformancePanel(
    metrics: PerformanceMetrics,
    onRunAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + Re-run Audit Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lighthouse & Runtime Vitals",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.onBackground
                    )
                    Text(
                        text = "Live page telemetry from Navigation & Paint Timing APIs",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(cs.primaryContainer)
                        .border(1.dp, cs.primary, RoundedCornerShape(8.dp))
                        .clickable { onRunAudit() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("rerun_lighthouse_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Run Audit",
                            tint = cs.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Re-Audit",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = cs.primary
                        )
                    }
                }
            }
        }

        // 4 Lighthouse Score Gauges
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cs.surface)
                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LighthouseGauge("Performance", metrics.performanceScore)
                LighthouseGauge("Accessibility", metrics.accessibilityScore)
                LighthouseGauge("Best Practices", metrics.bestPracticesScore)
                LighthouseGauge("SEO", metrics.seoScore)
            }
        }

        // Core Timing & DOM Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricTile(
                    label = "First Contentful Paint",
                    value = "${metrics.firstContentfulPaintMs} ms",
                    accent = DevEmerald,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "DOMContentLoaded",
                    value = "${metrics.domContentLoadedMs} ms",
                    accent = cs.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "DOM Elements",
                    value = "${metrics.domNodesCount} (d:${metrics.maxDomDepth})",
                    accent = DevAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "JS Heap Used",
                    value = "${metrics.jsHeapUsedMb} MB",
                    accent = cs.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Diagnostics & Audit Checks (${metrics.audits.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = cs.primary
            )
        }

        items(metrics.audits, key = { it.title }) { audit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.surface)
                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (audit.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (audit.passed) DevEmerald else DevAmber,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audit.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = cs.onSurface
                    )
                    Text(
                        text = audit.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
                if (audit.metricValue.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(cs.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = audit.metricValue,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (audit.passed) DevEmerald else DevAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LighthouseGauge(label: String, score: Int) {
    val cs = MaterialTheme.colorScheme
    val gaugeColor = when {
        score >= 90 -> DevEmerald
        score >= 50 -> DevAmber
        else -> DevRose
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                drawArc(
                    color = gaugeColor.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = gaugeColor,
                    startAngle = -90f,
                    sweepAngle = (score / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$score",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = gaugeColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant,
            maxLines = 1
        )
    }
}
