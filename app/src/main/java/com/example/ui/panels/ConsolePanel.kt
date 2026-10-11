package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsoleLogEntry
import com.example.model.ConsoleLogLevel
import com.example.model.JsSnippet
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevRose
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun ConsolePanel(
    logs: List<ConsoleLogEntry>,
    selectedFilter: ConsoleLogLevel?,
    searchQuery: String,
    commandHistory: List<String>,
    snippets: List<JsSnippet>,
    onSelectFilter: (ConsoleLogLevel?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearLogs: () -> Unit,
    onExecuteJs: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var jsCommand by remember { mutableStateOf("") }
    var historyCursor by remember { mutableIntStateOf(-1) }
    var showSnippetsMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val cs = MaterialTheme.colorScheme

    val filteredLogs = remember(logs, selectedFilter, searchQuery) {
        logs.filter { entry ->
            val levelMatches = when (selectedFilter) {
                null -> true
                ConsoleLogLevel.LOG -> entry.level == ConsoleLogLevel.LOG || entry.level == ConsoleLogLevel.COMMAND || entry.level == ConsoleLogLevel.RESULT
                else -> entry.level == selectedFilter
            }
            val queryMatches = searchQuery.isBlank() ||
                entry.message.contains(searchQuery, ignoreCase = true) ||
                entry.source.contains(searchQuery, ignoreCase = true)
            levelMatches && queryMatches
        }
    }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Single Clean Console Toolbar: Clear + Level Filters + Snippets Dropdown + Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onClearLogs,
                modifier = Modifier
                    .size(26.dp)
                    .testTag("clear_console_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear console logs",
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterItems = listOf(
                    null to "All",
                    ConsoleLogLevel.ERROR to "Errors (${logs.count { it.level == ConsoleLogLevel.ERROR }})",
                    ConsoleLogLevel.WARN to "Warn (${logs.count { it.level == ConsoleLogLevel.WARN }})",
                    ConsoleLogLevel.INFO to "Info"
                )
                filterItems.forEach { (level, label) ->
                    val active = selectedFilter == level
                    val chipAccent = when (level) {
                        ConsoleLogLevel.ERROR -> DevRose
                        ConsoleLogLevel.WARN -> DevAmber
                        else -> cs.primary
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) chipAccent.copy(alpha = 0.2f) else cs.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (active) chipAccent else cs.outlineVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectFilter(level) }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (active) chipAccent else cs.onSurfaceVariant
                        )
                    }
                }
            }

            // Compact Snippets Dropdown Button
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(cs.surfaceVariant)
                        .border(1.dp, cs.outline, RoundedCornerShape(6.dp))
                        .clickable { showSnippetsMenu = true }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                        .testTag("console_snippets_menu_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "JS Snippets",
                            tint = DevAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Snippets",
                            style = MaterialTheme.typography.labelSmall,
                            color = DevAmber
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSnippetsMenu,
                    onDismissRequest = { showSnippetsMenu = false },
                    modifier = Modifier.background(cs.surface)
                ) {
                    snippets.forEach { snippet ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(snippet.title, style = MaterialTheme.typography.labelMedium, color = cs.onSurface)
                                    Text(snippet.description, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                                }
                            },
                            onClick = {
                                showSnippetsMenu = false
                                onExecuteJs(snippet.code)
                            }
                        )
                    }
                }
            }

            // Compact filter input
            Row(
                modifier = Modifier
                    .width(96.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(cs.background)
                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = cs.onSurface
                    ),
                    cursorBrush = SolidColor(cs.primary),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text("Filter…", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        }
                        inner()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

        // Console Log Stream
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(cs.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Console ready — use \$0 for selected element or \$_ for last result",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(cs.background)
                    .testTag("console_log_list")
            ) {
                items(filteredLogs, key = { it.id }) { item ->
                    ConsoleLogRow(
                        entry = item,
                        onClickLog = { jsCommand = item.message }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = cs.outlineVariant)
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

        // Interactive JS REPL Input Bar with Command History (↑ / ↓) and $0 / $_ shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surface)
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(16.dp)
            )

            // Quick $0 (selected DOM node) chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(cs.surfaceVariant)
                    .clickable { jsCommand += "\$0" }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text("\$0", style = MaterialTheme.typography.labelSmall, color = DevAmber)
            }

            // Quick $_ (last evaluated result) chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(cs.surfaceVariant)
                    .clickable { jsCommand += "\$_" }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text("\$_", style = MaterialTheme.typography.labelSmall, color = cs.primary)
            }

            BasicTextField(
                value = jsCommand,
                onValueChange = {
                    jsCommand = it
                    historyCursor = -1
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    color = cs.onSurface
                ),
                cursorBrush = SolidColor(cs.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (jsCommand.isNotBlank()) {
                            onExecuteJs(jsCommand)
                            jsCommand = ""
                            historyCursor = -1
                        }
                    }
                ),
                decorationBox = { inner ->
                    if (jsCommand.isEmpty()) {
                        Text(
                            text = "Evaluate JS (e.g. \$0.style, document.title)…",
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cs.background)
                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 7.dp)
                    .testTag("console_repl_input")
            )

            // Command History Up / Down
            IconButton(
                onClick = {
                    if (commandHistory.isNotEmpty()) {
                        val nextIdx = (historyCursor + 1).coerceAtMost(commandHistory.lastIndex)
                        historyCursor = nextIdx
                        jsCommand = commandHistory[nextIdx]
                    }
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Previous command",
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = {
                    if (historyCursor > 0) {
                        historyCursor -= 1
                        jsCommand = commandHistory[historyCursor]
                    } else {
                        historyCursor = -1
                        jsCommand = ""
                    }
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Next command",
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cs.primary)
                    .clickable {
                        if (jsCommand.isNotBlank()) {
                            onExecuteJs(jsCommand)
                            jsCommand = ""
                            historyCursor = -1
                        }
                    }
                    .testTag("console_repl_run_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute JavaScript",
                    tint = cs.onPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun ConsoleLogRow(
    entry: ConsoleLogEntry,
    onClickLog: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val rowBg = when (entry.level) {
        ConsoleLogLevel.ERROR -> DevRose.copy(alpha = 0.14f)
        ConsoleLogLevel.WARN -> DevAmber.copy(alpha = 0.12f)
        ConsoleLogLevel.COMMAND -> cs.primary.copy(alpha = 0.08f)
        ConsoleLogLevel.RESULT -> DevEmerald.copy(alpha = 0.08f)
        else -> Color.Transparent
    }
    val textColor = when (entry.level) {
        ConsoleLogLevel.ERROR -> DevRose
        ConsoleLogLevel.WARN -> DevAmber
        ConsoleLogLevel.INFO -> cs.primary
        ConsoleLogLevel.COMMAND -> cs.primary
        ConsoleLogLevel.RESULT -> DevEmerald
        ConsoleLogLevel.LOG -> cs.onSurface
    }
    val badgeColor = when (entry.level) {
        ConsoleLogLevel.ERROR -> DevRose
        ConsoleLogLevel.WARN -> DevAmber
        ConsoleLogLevel.INFO -> cs.primary
        ConsoleLogLevel.COMMAND -> DevPurple
        ConsoleLogLevel.RESULT -> DevEmerald
        ConsoleLogLevel.LOG -> cs.outline
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable {
                if (entry.level == ConsoleLogLevel.COMMAND) onClickLog()
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(badgeColor.copy(alpha = 0.25f))
                .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = entry.level.badge,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                ),
                color = badgeColor
            )
        }

        if (entry.count > 1) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeColor)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "${entry.count}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = Color.White
                )
            }
        }

        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = entry.timestamp.substringBeforeLast("."),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = cs.onSurfaceVariant
        )
    }
}
