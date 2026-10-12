package com.example.ui.pages.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.model.SearchEngineItem
import com.example.ui.pages.SearchEngineLogo

@Composable
fun SearchSettingsSection(
    searchEngines: List<SearchEngineItem>,
    searchEngine: SearchEngineItem,
    javaScriptEnabled: Boolean,
    onSelectSearchEngine: (SearchEngineItem) -> Unit,
    onAddCustomSearchEngine: (String, String) -> Unit,
    onRemoveCustomSearchEngine: (String) -> Unit,
    onToggleJavaScript: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    var showAddEngineForm by remember { mutableStateOf(false) }
    var customEngineName by remember { mutableStateOf("") }
    var customEngineUrl by remember { mutableStateOf("https://") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Default Search Engine",
                                style = MaterialTheme.typography.titleMedium,
                                color = cs.onSurface
                            )
                            Text(
                                text = "Select your preferred search provider or tap + to add your own",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    // '+' Button to Add Custom Search Engine
                    Surface(
                        color = cs.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.5.dp, cs.primary, CircleShape)
                            .clip(CircleShape)
                            .clickable { showAddEngineForm = !showAddEngineForm }
                            .testTag("add_search_engine_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Custom Search Engine",
                                tint = cs.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    searchEngines.forEach { engine ->
                        val selected = searchEngine.id == engine.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) cs.primaryContainer else cs.surfaceVariant)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) cs.primary else cs.outlineVariant,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { onSelectSearchEngine(engine) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                                .testTag("search_engine_chip_${engine.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SearchEngineLogo(engine = engine, size = 20.dp)
                                Text(
                                    text = engine.displayName,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selected) cs.primary else cs.onSurface
                                )
                                if (engine.isCustom) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove ${engine.displayName}",
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clickable { onRemoveCustomSearchEngine(engine.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showAddEngineForm) {
                    Surface(
                        color = cs.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cs.primary, RoundedCornerShape(20.dp))
                            .testTag("custom_search_engine_form")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Add Custom Search Engine",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = cs.primary
                            )
                            OutlinedTextField(
                                value = customEngineName,
                                onValueChange = { customEngineName = it },
                                label = { Text("Search Engine Name (e.g. Startpage, GitHub)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_search_engine_name_input")
                            )
                            OutlinedTextField(
                                value = customEngineUrl,
                                onValueChange = { customEngineUrl = it },
                                label = { Text("Query URL with %s (e.g. https://www.startpage.com/sp/search?query=%s)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_search_engine_url_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(cs.primary)
                                        .clickable {
                                            if (customEngineName.isNotBlank() && customEngineUrl.isNotBlank()) {
                                                onAddCustomSearchEngine(
                                                    customEngineName.trim(),
                                                    customEngineUrl.trim()
                                                )
                                                customEngineName = ""
                                                customEngineUrl = "https://"
                                                showAddEngineForm = false
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 9.dp)
                                        .testTag("save_custom_search_engine_button")
                                ) {
                                    Text(
                                        text = "Add Search Engine",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = cs.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = cs.outlineVariant)

                // JavaScript Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Enable JavaScript",
                                style = MaterialTheme.typography.titleSmall,
                                color = cs.onSurface
                            )
                            Text(
                                text = "Required for interactive search results, autocomplete, and Web APIs",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = javaScriptEnabled,
                        onCheckedChange = onToggleJavaScript,
                        modifier = Modifier.testTag("javascript_toggle_switch")
                    )
                }
            }
        }
    }
}
