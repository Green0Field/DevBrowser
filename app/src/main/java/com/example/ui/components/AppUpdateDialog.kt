package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.model.AppUpdateInfo

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDownloadApk: (String) -> Unit,
    onOpenReleaseUrl: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 8.dp,
            border = BorderStroke(1.5.dp, cs.primary),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("dialog_app_update")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(cs.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = cs.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Update Available",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = cs.onSurface
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME} → v${updateInfo.latestVersionName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = cs.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = cs.onSurfaceVariant
                        )
                    }
                }

                // Release Title & Short Description (Markdown supported)
                if (updateInfo.releaseTitle.isNotBlank()) {
                    MarkdownText(
                        text = updateInfo.releaseTitle,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("text_dialog_release_title")
                    )
                }

                // Changelog / Release Notes (Markdown supported with scroll)
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Surface(
                        color = cs.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            MarkdownText(
                                text = updateInfo.releaseNotes,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = cs.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("text_dialog_release_notes")
                            )
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onDownloadApk(updateInfo.downloadUrl)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_dialog_download_apk")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download APK${if (updateInfo.apkSizeFormatted.isNotBlank()) " (${updateInfo.apkSizeFormatted})" else ""}")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onOpenReleaseUrl(updateInfo.htmlUrl)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_dialog_view_github")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Notes")
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Later")
                        }
                    }
                }
            }
        }
    }
}
