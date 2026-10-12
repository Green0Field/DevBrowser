package com.example.ui.pages.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VirusTotalThreatLevel
import com.example.model.VirusTotalWarningState
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevRose

@Composable
fun VirusTotalWarningPage(
    warningState: VirusTotalWarningState,
    onGoBack: () -> Unit,
    onContinueAnyway: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDanger = warningState.threatLevel == VirusTotalThreatLevel.DANGER
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Intercept back button to return safely
    BackHandler {
        onGoBack()
    }

    val primaryColor = if (isDanger) DevRose else DevAmber
    val containerBg = if (isDanger) Color(0xFF1E0B0F) else Color(0xFF1C1405)
    val cardBorder = if (isDanger) DevRose.copy(alpha = 0.6f) else DevAmber.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(containerBg)
            .testTag(if (isDanger) "virustotal_warning_danger" else "virustotal_warning_careful"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Big Warning Icon Badge
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .shadow(16.dp, shape = CircleShape, ambientColor = primaryColor, spotColor = primaryColor)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.15f))
                        )
                    )
                    .border(2.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDanger) Icons.Default.Dangerous else Icons.Default.WarningAmber,
                    contentDescription = if (isDanger) "Danger Warning" else "Careful Warning",
                    tint = primaryColor,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = if (isDanger) "Danger! Malicious Website" else "Careful: Suspicious Website",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp
                ),
                color = primaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = if (isDanger) {
                    "VirusTotal detected 9 or more security engines flagging this website as malicious. Visiting this page could compromise your security."
                } else {
                    "VirusTotal flagged this website with 4 to 8 security vendor warnings. Proceed with caution."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scan Breakdown Card
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VirusTotal Scan Score",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(primaryColor.copy(alpha = 0.2f))
                                .border(1.dp, primaryColor, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${warningState.scanResult.maliciousCount} / ${warningState.scanResult.totalEngines.coerceAtLeast(1)} Malicious",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = primaryColor
                            )
                        }
                    }

                    // Destination URL preview
                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = warningState.targetUrl,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatPill("Malicious", warningState.scanResult.maliciousCount.toString(), primaryColor)
                        StatPill("Suspicious", warningState.scanResult.suspiciousCount.toString(), DevAmber)
                        StatPill("Harmless", warningState.scanResult.harmlessCount.toString(), Color(0xFF10B981))
                        StatPill("Undetected", warningState.scanResult.undetectedCount.toString(), Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Safe Choice: Go Back
                Button(
                    onClick = onGoBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDanger) DevRose else DevAmber,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("warning_go_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Go Back to Safety (Recommended)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Secondary Unsafe Choice: Continue
                OutlinedButton(
                    onClick = {
                        if (isDanger) {
                            showConfirmDialog = true
                        } else {
                            onContinueAnyway(warningState.targetUrl)
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.75f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("warning_continue_anyway_button")
                ) {
                    Text(
                        text = if (isDanger) "Continue anyway" else "Continue to site",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }

    // Modal Confirmation Dialog for Danger! sites ("Are you sure?")
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dangerous,
                    contentDescription = "Are you sure?",
                    tint = DevRose,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Are you sure?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "This website has ${warningState.scanResult.maliciousCount} security engine detections indicating malicious activity. Browsing this site may expose your device to malware or security breaches.\n\nDo you still want to proceed?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onContinueAnyway(warningState.targetUrl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevRose, contentColor = Color.White),
                    modifier = Modifier.testTag("warning_confirm_proceed_button")
                ) {
                    Text("Proceed Anyway")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    modifier = Modifier.testTag("warning_cancel_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatPill(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}
