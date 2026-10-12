package com.example.ui.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VirusTotalApiClient
import com.example.model.VirusTotalRescanInterval
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import kotlinx.coroutines.launch

@Composable
fun VirusTotalSettingsSection(
    isEnabled: Boolean,
    apiKey: String,
    rescanInterval: VirusTotalRescanInterval,
    cachedScansCount: Int,
    onSetEnabled: (Boolean) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSelectRescanInterval: (VirusTotalRescanInterval) -> Unit,
    onClearScanCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var inputKey by remember(apiKey) { mutableStateOf(apiKey) }
    var isKeyHidden by remember { mutableStateOf(true) }
    var keySaveFeedback by remember { mutableStateOf<String?>(null) }
    var isTestingKey by remember { mutableStateOf(false) }
    var testResultFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Master Enable Switch
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) cs.primaryContainer else cs.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "VirusTotal Scanner",
                            tint = if (isEnabled) cs.primary else cs.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "VirusTotal Site Scanner",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = if (isEnabled) "Real-time scanning enabled" else "Website scanning is off",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onSetEnabled(it) },
                    modifier = Modifier.testTag("virustotal_enable_switch")
                )
            }
        }

        // Card 2: API Key Input & Verification
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = cs.primary, modifier = Modifier.size(22.dp))
                    Text(
                        text = "VirusTotal API Key",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                }

                Text(
                    text = "A free VirusTotal API key allows scanning URLs against 70+ antivirus engines. Get a free key at virustotal.com.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        keySaveFeedback = null
                        testResultFeedback = null
                    },
                    label = { Text("API Key") },
                    placeholder = { Text("Paste your VirusTotal API key...") },
                    singleLine = true,
                    visualTransformation = if (isKeyHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { isKeyHidden = !isKeyHidden }) {
                            Icon(
                                imageVector = if (isKeyHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isKeyHidden) "Show Key" else "Hide Key"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("virustotal_api_key_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveApiKey(inputKey)
                            keySaveFeedback = "API Key Saved"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("virustotal_save_key_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Key")
                    }

                    OutlinedButton(
                        onClick = {
                            isTestingKey = true
                            testResultFeedback = null
                            scope.launch {
                                val result = VirusTotalApiClient.testApiKey(inputKey)
                                isTestingKey = false
                                testResultFeedback = if (result.isSuccess) {
                                    Pair(true, "API Key is valid and working!")
                                } else {
                                    Pair(false, result.exceptionOrNull()?.message ?: "Verification failed")
                                }
                            }
                        },
                        enabled = inputKey.isNotBlank() && !isTestingKey,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("virustotal_test_key_button")
                    ) {
                        if (isTestingKey) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Test Key")
                        }
                    }
                }

                // Feedback notices
                if (keySaveFeedback != null) {
                    Text(
                        text = keySaveFeedback!!,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DevEmerald
                    )
                }

                if (testResultFeedback != null) {
                    val (success, msg) = testResultFeedback!!
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (success) DevEmerald else DevRose
                    )
                }
            }
        }

        // Card 3: Rescan Interval Picker
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = cs.primary, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Rescan Frequency",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                }

                Text(
                    text = "After scanning and storing website results on device, wait this duration before checking VirusTotal again on subsequent visits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VirusTotalRescanInterval.entries.forEach { interval ->
                        val isSelected = interval == rescanInterval
                        Surface(
                            color = if (isSelected) cs.primaryContainer else cs.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, cs.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectRescanInterval(interval) }
                                .testTag("rescan_interval_${interval.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = interval.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) cs.onPrimaryContainer else cs.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = cs.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 4: Local Scan Cache Management
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Stored Scan Results",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(cs.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$cachedScansCount scanned sites",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = cs.primary
                        )
                    }
                }

                Text(
                    text = "Scan results are stored locally on your device to keep browsing fast and conserve VirusTotal API quota.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onClearScanCache,
                    enabled = cachedScansCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DevRose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("virustotal_clear_cache_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Stored Scan Cache")
                }
            }
        }

        // Card 5: Threat Category Guide
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Threat Categories",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = cs.onSurface
                )

                ThreatGuideRow(
                    label = "Safe (0–3 flags)",
                    description = "Site loads normally without interruptions. Results stored locally.",
                    color = DevEmerald
                )
                HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))
                ThreatGuideRow(
                    label = "Careful (4–8 flags)",
                    description = "Displays a caution warning page before loading the website.",
                    color = DevAmber
                )
                HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))
                ThreatGuideRow(
                    label = "Danger! (9+ flags)",
                    description = "Blocks access with a critical hazard page and an 'Are you sure?' confirmation.",
                    color = DevRose
                )
            }
        }
    }
}

@Composable
private fun ThreatGuideRow(label: String, description: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
