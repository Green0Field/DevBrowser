package com.example.ui.pages.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.AutoUpdateCheckFrequency
import com.example.model.UpdateCheckStatus
import com.example.ui.components.MarkdownText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutoUpdateSettingsSection(
    isEnabled: Boolean,
    apiKey: String,
    checkFrequency: AutoUpdateCheckFrequency,
    startupCount: Int,
    lastCheckMillis: Long,
    updateStatus: UpdateCheckStatus,
    isIncludePrereleases: Boolean,
    onSetEnabled: (Boolean) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSelectFrequency: (AutoUpdateCheckFrequency) -> Unit,
    onSetIncludePrereleases: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onDownloadApk: (String) -> Unit,
    onOpenReleaseUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    var inputKey by remember(apiKey) { mutableStateOf(apiKey) }
    var isKeyHidden by remember { mutableStateOf(true) }
    var keySaveFeedback by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Master Enable Switch
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, cs.outlineVariant),
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
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) cs.primaryContainer else cs.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Auto Update",
                            tint = if (isEnabled) cs.onPrimaryContainer else cs.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Automatic Updates",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = if (isEnabled) "Checks GitHub Releases on startup" else "Automatic checking is disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onSetEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = cs.primary,
                        checkedTrackColor = cs.primaryContainer
                    ),
                    modifier = Modifier.testTag("switch_auto_update_enabled")
                )
            }
        }

        // Card: Include Pre-releases Toggle
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, cs.outlineVariant),
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
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isIncludePrereleases) cs.primaryContainer else cs.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Pre-releases",
                            tint = if (isIncludePrereleases) cs.onPrimaryContainer else cs.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Also Download Pre-releases",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = if (isIncludePrereleases) "Include pre-releases when checking for updates" else "Only stable releases will be checked",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isIncludePrereleases,
                    onCheckedChange = onSetIncludePrereleases,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = cs.primary,
                        checkedTrackColor = cs.primaryContainer
                    ),
                    modifier = Modifier.testTag("switch_include_prereleases")
                )
            }
        }

        // Card 2: Status & Manual Check Banner
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "DevBrowser Version",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = cs.onSurface
                        )
                        Text(
                            text = "Current: v${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.primary
                        )
                    }

                    Surface(
                        color = cs.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lastCheckMillis > 0) {
                                val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                                "Checked " + sdf.format(Date(lastCheckMillis))
                            } else "Never checked",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Status Message & Outcome Card
                when (updateStatus) {
                    is UpdateCheckStatus.Checking -> {
                        Surface(
                            color = cs.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = cs.primary
                                )
                                Text(
                                    text = "Checking GitHub Releases for updates…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onPrimaryContainer
                                )
                            }
                        }
                    }

                    is UpdateCheckStatus.UpdateAvailable -> {
                        val info = updateStatus.info
                        Surface(
                            color = cs.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, cs.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = cs.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "New Update Available: v${info.latestVersionName}!",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = cs.onPrimaryContainer
                                    )
                                }

                                if (info.releaseTitle.isNotBlank() && info.releaseTitle != info.latestVersionName) {
                                    MarkdownText(
                                        text = info.releaseTitle,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = cs.onPrimaryContainer
                                    )
                                }

                                if (info.releaseNotes.isNotBlank()) {
                                    Surface(
                                        color = cs.surface.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(modifier = Modifier.padding(10.dp)) {
                                            MarkdownText(
                                                text = info.releaseNotes,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp,
                                                color = cs.onSurface
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onDownloadApk(info.downloadUrl) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = cs.primary,
                                            contentColor = cs.onPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_download_update_apk")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Download APK${if (info.apkSizeFormatted.isNotBlank()) " (${info.apkSizeFormatted})" else ""}")
                                    }

                                    OutlinedButton(
                                        onClick = { onOpenReleaseUrl(info.htmlUrl) },
                                        modifier = Modifier.testTag("btn_view_release_github")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("GitHub")
                                    }
                                }
                            }
                        }
                    }

                    is UpdateCheckStatus.UpToDate -> {
                        Surface(
                            color = cs.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = cs.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "You are using the latest version of DevBrowser (v${updateStatus.currentVersion}).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is UpdateCheckStatus.Error -> {
                        Surface(
                            color = cs.errorContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = cs.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = updateStatus.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    UpdateCheckStatus.Idle -> {
                        // In idle state, display a concise tip
                        Text(
                            text = "DevBrowser queries the GitHub Releases API (Green0Field/DevBrowser) to detect newer tagged builds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onCheckForUpdates,
                    enabled = updateStatus !is UpdateCheckStatus.Checking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_check_for_updates_now")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check for updates now")
                }
            }
        }

        // Card 3: When to check for updates (Frequency)
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cs.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = cs.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "When to Check for Updates",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = if (isEnabled) {
                                if (checkFrequency == AutoUpdateCheckFrequency.MANUAL_ONLY) {
                                    "Manual checks only"
                                } else {
                                    "Startup counter: session $startupCount of ${checkFrequency.startupThreshold}"
                                }
                            } else "Requires Automatic Updates to be enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AutoUpdateCheckFrequency.entries.forEach { freq ->
                        val isSelected = checkFrequency == freq
                        Surface(
                            color = if (isSelected) cs.primaryContainer else cs.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(1.5.dp, cs.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = isEnabled) { onSelectFrequency(freq) }
                                .testTag("frequency_option_${freq.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = freq.displayName,
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

        // Card 4: Optional GitHub API Key
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, cs.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cs.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = cs.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "GitHub API Key (Optional)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = "Increases GitHub API limit from 60 to 5,000 requests/hr",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "A key is not required for normal usage. DevBrowser uses anonymous requests by default. If you frequently encounter rate limits, paste a GitHub Personal Access Token (classic or fine-grained) with public repo read access below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        keySaveFeedback = null
                    },
                    label = { Text("GitHub Personal Access Token") },
                    placeholder = { Text("ghp_... or github_pat_...") },
                    singleLine = true,
                    visualTransformation = if (isKeyHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (inputKey.isNotEmpty()) {
                                IconButton(onClick = {
                                    inputKey = ""
                                    onSaveApiKey("")
                                    keySaveFeedback = "API Key cleared"
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Token"
                                    )
                                }
                            }
                            IconButton(onClick = { isKeyHidden = !isKeyHidden }) {
                                Icon(
                                    imageVector = if (isKeyHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isKeyHidden) "Show Key" else "Hide Key"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_github_api_key")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (keySaveFeedback != null) {
                        Text(
                            text = keySaveFeedback ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            onSaveApiKey(inputKey.trim())
                            keySaveFeedback = if (inputKey.isBlank()) "Cleared token" else "Token saved successfully"
                        },
                        modifier = Modifier.testTag("btn_save_github_api_key")
                    ) {
                        Text("Save Token")
                    }
                }
            }
        }
    }
}
