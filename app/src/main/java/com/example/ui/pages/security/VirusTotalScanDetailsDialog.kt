package com.example.ui.pages.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.VirusTotalRescanInterval
import com.example.model.VirusTotalScanResult
import com.example.model.VirusTotalThreatLevel
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose

@Composable
fun VirusTotalScanDetailsDialog(
    currentUrl: String,
    scanResult: VirusTotalScanResult?,
    isVirusTotalEnabled: Boolean,
    hasApiKey: Boolean,
    isScanning: Boolean,
    rescanInterval: VirusTotalRescanInterval,
    onRescan: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenExternalUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val threat = scanResult?.threatLevel ?: VirusTotalThreatLevel.NOT_SCANNED

    val badgeColor = when {
        !isVirusTotalEnabled || !hasApiKey -> cs.outline
        isScanning -> cs.primary
        threat == VirusTotalThreatLevel.DANGER -> DevRose
        threat == VirusTotalThreatLevel.CAREFUL -> DevAmber
        threat == VirusTotalThreatLevel.SAFE -> DevEmerald
        else -> cs.outline
    }

    val statusTitle = when {
        !isVirusTotalEnabled -> "Scanner Disabled"
        !hasApiKey -> "API Key Missing"
        isScanning -> "Scanning Site..."
        threat == VirusTotalThreatLevel.DANGER -> "Danger! High Risk"
        threat == VirusTotalThreatLevel.CAREFUL -> "Careful: Suspicious"
        threat == VirusTotalThreatLevel.SAFE -> "Safe Site"
        else -> "Not Scanned"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = cs.surface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("virustotal_details_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Shield Icon & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(1.5.dp, badgeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = cs.primary
                                )
                            } else {
                                Icon(
                                    imageVector = when (threat) {
                                        VirusTotalThreatLevel.DANGER -> Icons.Default.Dangerous
                                        VirusTotalThreatLevel.CAREFUL -> Icons.Default.WarningAmber
                                        VirusTotalThreatLevel.SAFE -> Icons.Default.Shield
                                        else -> Icons.Default.Security
                                    },
                                    contentDescription = statusTitle,
                                    tint = badgeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = cs.onSurface
                            )
                            Text(
                                text = if (isVirusTotalEnabled) "VirusTotal Security" else "DevBrowser Shield",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Done")
                    }
                }

                // URL pill
                Surface(
                    color = cs.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentUrl,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        color = cs.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))

                if (!isVirusTotalEnabled || !hasApiKey) {
                    // Disabled / No Key State
                    Surface(
                        color = cs.primaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (!isVirusTotalEnabled) "Enable VirusTotal in Settings" else "Add your VirusTotal API key",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = cs.onSurface
                            )
                            Text(
                                text = "Scan every website automatically with 70+ security vendors. Protect against malware and phishing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenSettings()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open VirusTotal Settings")
                            }
                        }
                    }
                } else if (scanResult != null && scanResult.errorMessage == null && scanResult.threatLevel != VirusTotalThreatLevel.NOT_SCANNED) {
                    // Active Scan Results Breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detection Score",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = cs.onSurface
                            )
                            Text(
                                text = "${scanResult.maliciousCount} / ${scanResult.totalEngines.coerceAtLeast(1)} Malicious",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = badgeColor
                            )
                        }

                        // Statistics Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(
                                label = "Malicious",
                                count = scanResult.maliciousCount.toString(),
                                color = if (scanResult.maliciousCount > 0) DevRose else cs.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = "Suspicious",
                                count = scanResult.suspiciousCount.toString(),
                                color = if (scanResult.suspiciousCount > 0) DevAmber else cs.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = "Harmless",
                                count = scanResult.harmlessCount.toString(),
                                color = DevEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = "Undetected",
                                count = scanResult.undetectedCount.toString(),
                                color = cs.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Date & Expiration info
                        Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Scanned: ${scanResult.scanDateFormatted}",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                            Text(
                                text = "Rescan schedule: ${rescanInterval.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                } else if (scanResult?.errorMessage != null) {
                    // Error message
                    Surface(
                        color = DevAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = scanResult.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = DevAmber,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Actions: Rescan and Settings
                if (isVirusTotalEnabled && hasApiKey) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onRescan()
                            },
                            enabled = !isScanning,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rescan")
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onOpenSettings()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Settings")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        color = cs.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = cs.onSurfaceVariant
            )
        }
    }
}
