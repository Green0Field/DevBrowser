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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BrowserTab
import com.example.model.UserScript
import com.example.model.UserScriptUpdateInterval
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import kotlinx.coroutines.launch

@Composable
fun UserscriptsSettingsSection(
    userScripts: List<UserScript>,
    updateInterval: UserScriptUpdateInterval,
    onNavigate: (String) -> Unit,
    onDeleteScript: (String) -> Unit,
    onToggleScriptEnabled: (String, Boolean) -> Unit,
    onSelectUpdateInterval: (UserScriptUpdateInterval) -> Unit,
    onImportFromUrl: (String, (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var showAddChoiceDialog by remember { mutableStateOf(false) }
    var showImportUrlDialog by remember { mutableStateOf(false) }
    var importUrlInput by remember { mutableStateOf("https://") }
    var isImporting by remember { mutableStateOf(false) }
    var importFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Userscripts Header & Add Button
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
                            .background(cs.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Userscripts",
                            tint = cs.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Userscripts Support",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = "${userScripts.size} scripts configured (${userScripts.count { it.enabled }} active)",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = cs.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.5.dp, cs.primary, CircleShape)
                        .clip(CircleShape)
                        .clickable { showAddChoiceDialog = true }
                        .testTag("add_userscript_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Userscript",
                            tint = cs.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Card 2: Auto-Update Frequency
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
                        text = "Script Auto-Update Frequency",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                }

                Text(
                    text = "If a script defines an @updateURL, check for updates periodically at this interval.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    UserScriptUpdateInterval.entries.forEach { interval ->
                        val isSelected = interval == updateInterval
                        Surface(
                            color = if (isSelected) cs.primaryContainer else cs.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) cs.primary else cs.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectUpdateInterval(interval) }
                                .testTag("userscript_interval_${interval.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = interval.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) cs.onPrimaryContainer else cs.onSurface
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(cs.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 3: List of Userscripts
        if (userScripts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Installed Scripts",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                userScripts.forEach { script ->
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(18.dp),
                        tonalElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("userscript_item_${script.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = script.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = cs.onSurface
                                )
                                Text(
                                    text = "Matches: ${script.matchPatterns.joinToString(", ")}${if (script.updateUrl.isNotBlank()) " • Auto-update enabled" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = {
                                    onNavigate("${BrowserTab.URL_SETTINGS_USERSCRIPTS}/edit?file=${script.id}")
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Script", tint = cs.primary, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = { onDeleteScript(script.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Script", tint = DevRose, modifier = Modifier.size(20.dp))
                                }

                                Switch(
                                    checked = script.enabled,
                                    onCheckedChange = { onToggleScriptEnabled(script.id, it) },
                                    modifier = Modifier.testTag("userscript_switch_${script.id}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: Add Choice Dialog (Create New vs Paste Link)
    if (showAddChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showAddChoiceDialog = false },
            title = { Text("Add Userscript") },
            text = { Text("Choose how you would like to add a new userscript:") },
            confirmButton = {
                Button(
                    onClick = {
                        showAddChoiceDialog = false
                        onNavigate("${BrowserTab.URL_SETTINGS_USERSCRIPTS}/edit")
                    },
                    modifier = Modifier.testTag("dialog_create_new_button")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create New Script")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddChoiceDialog = false
                        showImportUrlDialog = true
                    },
                    modifier = Modifier.testTag("dialog_paste_link_button")
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Paste Link / Import")
                }
            }
        )
    }

    // Dialog 2: Paste Link / Import from URL Dialog
    if (showImportUrlDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isImporting) showImportUrlDialog = false
            },
            title = { Text("Import Userscript from URL") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste the direct link to a userscript file (.user.js):", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = importUrlInput,
                        onValueChange = { importUrlInput = it },
                        label = { Text("Userscript URL") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_url_input")
                    )
                    if (importFeedback != null) {
                        val (success, msg) = importFeedback!!
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (success) DevEmerald else DevRose
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isImporting = true
                        importFeedback = null
                        onImportFromUrl(importUrlInput) { success, msg ->
                            isImporting = false
                            importFeedback = Pair(success, msg)
                            if (success) {
                                scope.launch {
                                    kotlinx.coroutines.delay(800)
                                    showImportUrlDialog = false
                                    importUrlInput = "https://"
                                    importFeedback = null
                                }
                            }
                        }
                    },
                    enabled = importUrlInput.isNotBlank() && !isImporting,
                    modifier = Modifier.testTag("import_confirm_button")
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Download & Import")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isImporting) showImportUrlDialog = false
                    },
                    enabled = !isImporting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
