package com.example.ui.panels

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StorageEntry
import com.example.model.StorageType
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun StoragePanel(
    entries: List<StorageEntry>,
    selectedType: StorageType,
    onSelectType: (StorageType) -> Unit,
    onUpsertEntry: (StorageType, String, String) -> Unit,
    onDeleteEntry: (StorageType, String) -> Unit,
    onClearType: (StorageType) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputKey by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme

    val filteredEntries = remember(entries, selectedType) {
        entries.filter { it.type == selectedType }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Storage Type Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StorageType.entries.forEach { type ->
                val count = entries.count { it.type == type }
                val active = selectedType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (active) cs.primaryContainer else cs.surfaceVariant)
                        .border(1.dp, if (active) cs.primary else cs.outlineVariant, RoundedCornerShape(6.dp))
                        .clickable { onSelectType(type) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("storage_tab_${type.name.lowercase()}")
                ) {
                    Text(
                        text = "${type.label} ($count)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (active) cs.primary else cs.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { onClearType(selectedType) },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("clear_storage_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear all ${selectedType.label}",
                    tint = DevRose,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

        // Add / Edit Key-Value Form
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = inputKey,
                onValueChange = { inputKey = it },
                label = { Text("Key", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                modifier = Modifier
                    .weight(0.38f)
                    .testTag("storage_key_input")
            )
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Value", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                modifier = Modifier
                    .weight(0.45f)
                    .testTag("storage_value_input")
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DevEmerald)
                    .clickable {
                        if (inputKey.isNotBlank()) {
                            onUpsertEntry(selectedType, inputKey, inputValue)
                            inputKey = ""
                            inputValue = ""
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .testTag("storage_save_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Set",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

        // Key-Value Table
        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ${selectedType.label} entries found for this origin. Add a key above!",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.background)
                    .testTag("storage_entries_list")
            ) {
                items(filteredEntries, key = { "${it.type}_${it.key}" }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                inputKey = entry.key
                                inputValue = entry.value
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = entry.value,
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteEntry(entry.type, entry.key) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete key ${entry.key}",
                                tint = DevAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = cs.outlineVariant)
                }
            }
        }
    }
}
