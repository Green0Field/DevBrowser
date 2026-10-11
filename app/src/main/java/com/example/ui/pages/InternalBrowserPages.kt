package com.example.ui.pages

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BrowserTab
import com.example.model.HistoryItem
import com.example.model.QuickBookmark
import com.example.model.SearchEngineOption
import com.example.model.ThemeAccentColor

@Composable
fun DevBrowserHomePage(
    bookmarks: List<QuickBookmark>,
    searchEngine: SearchEngineOption,
    onNavigate: (String) -> Unit,
    onOpenDevTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_devbrowser_home"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Header
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cs.primaryContainer)
                                .border(1.dp, cs.primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = cs.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "DevBrowser",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Web Browser with Integrated Chrome DevTools",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            // Home Search / URL Bar
            item {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = {
                        Text(
                            text = "Search ${searchEngine.displayName} or type a URL…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = cs.primary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (searchInput.isNotBlank()) {
                                focusManager.clearFocus()
                                onNavigate(searchInput)
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }

            // Internal Pages Quick Strip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    InternalLinkChip(
                        icon = Icons.Default.Bookmarks,
                        label = "Bookmarks",
                        onClick = { onNavigate(BrowserTab.URL_BOOKMARKS) }
                    )
                    InternalLinkChip(
                        icon = Icons.Default.History,
                        label = "History",
                        onClick = { onNavigate(BrowserTab.URL_HISTORY) }
                    )
                    InternalLinkChip(
                        icon = Icons.Default.Science,
                        label = "Test Page",
                        onClick = { onNavigate(BrowserTab.URL_TEST) }
                    )
                    InternalLinkChip(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = { onNavigate(BrowserTab.URL_SETTINGS) }
                    )
                }
            }

            // Pinned Bookmarks Grid
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = cs.onBackground
                        )
                        Text(
                            text = "Manage →",
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigate(BrowserTab.URL_BOOKMARKS) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bookmarks.chunked(2).forEach { rowBookmarks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowBookmarks.forEach { bm ->
                                    Surface(
                                        color = cs.surface,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onNavigate(bm.url) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(cs.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (bm.url == BrowserTab.URL_TEST) Icons.Default.Science else Icons.Default.Language,
                                                    contentDescription = null,
                                                    tint = cs.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = bm.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = cs.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = bm.url.removePrefix("https://"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = cs.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowBookmarks.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InternalLinkChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        color = cs.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurface
            )
        }
    }
}

@Composable
fun DevBrowserBookmarksPage(
    bookmarks: List<QuickBookmark>,
    onSelectUrl: (String) -> Unit,
    onAddBookmark: (String, String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("https://") }
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_devbrowser_bookmarks"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onBackground
                        )
                        Text(
                            text = "${bookmarks.size} saved sites • devbrowser:bookmarks",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(cs.primary)
                            .clickable { showAddForm = !showAddForm }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("add_bookmark_form_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = cs.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Add Bookmark",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = cs.onPrimary
                            )
                        }
                    }
                }
            }

            if (showAddForm) {
                item {
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cs.primary, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "New Bookmark",
                                style = MaterialTheme.typography.titleSmall,
                                color = cs.primary
                            )
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newUrl,
                                onValueChange = { newUrl = it },
                                label = { Text("URL (e.g. https://github.com or devbrowser:test)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cs.primary)
                                        .clickable {
                                            if (newUrl.isNotBlank()) {
                                                onAddBookmark(
                                                    newTitle.ifBlank { newUrl },
                                                    newUrl.trim()
                                                )
                                                newTitle = ""
                                                newUrl = "https://"
                                                showAddForm = false
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Save Bookmark",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = cs.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (bookmarks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No bookmarks saved yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(bookmarks, key = { it.url }) { bm ->
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectUrl(bm.url) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = cs.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = bm.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = bm.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onRemoveBookmark(bm.url) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete bookmark ${bm.title}",
                                    tint = cs.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DevBrowserHistoryPage(
    history: List<HistoryItem>,
    onSelectUrl: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_devbrowser_history"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onBackground
                        )
                        Text(
                            text = "${history.size} visited pages • devbrowser:history",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }

                    if (history.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(cs.error.copy(alpha = 0.16f))
                                .border(1.dp, cs.error, RoundedCornerShape(10.dp))
                                .clickable { onClearHistory() }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("clear_history_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = cs.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = cs.error
                                )
                            }
                        }
                    }
                }
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Browsing history is empty.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(history, key = { it.id }) { item ->
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectUrl(item.url) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = cs.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { onDeleteHistoryItem(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove history item",
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DevBrowserSettingsPage(
    isDarkTheme: Boolean,
    isNeonMode: Boolean,
    accentColor: ThemeAccentColor,
    searchEngine: SearchEngineOption,
    javaScriptEnabled: Boolean,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetNeonMode: (Boolean) -> Unit,
    onSelectAccentColor: (ThemeAccentColor) -> Unit,
    onSelectSearchEngine: (SearchEngineOption) -> Unit,
    onToggleJavaScript: (Boolean) -> Unit,
    onClearSiteData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    var clearedDataFeedback by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_devbrowser_settings"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = cs.onBackground
                    )
                    Text(
                        text = "Customize themes, colors, and browser behavior • devbrowser:settings",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            // 1. Theme Mode (Dark Mode Default vs Light Mode)
            item {
                Surface(
                    color = cs.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cs.outlineVariant, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = cs.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Appearance Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = cs.onSurface
                                )
                                Text(
                                    text = "Sends prefers-color-scheme: ${if (isDarkTheme) "dark" else "light"} to websites so sites auto-switch theme",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Dark Mode Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDarkTheme) cs.primaryContainer else cs.surfaceVariant)
                                    .border(
                                        width = if (isDarkTheme) 2.dp else 1.dp,
                                        color = if (isDarkTheme) cs.primary else cs.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSetDarkTheme(true) }
                                    .padding(12.dp)
                                    .testTag("theme_mode_dark"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (isDarkTheme) cs.primary else cs.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Dark (Default)",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isDarkTheme) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = cs.onSurface
                                    )
                                }
                            }

                            // Light Mode Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isDarkTheme) cs.primaryContainer else cs.surfaceVariant)
                                    .border(
                                        width = if (!isDarkTheme) 2.dp else 1.dp,
                                        color = if (!isDarkTheme) cs.primary else cs.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSetDarkTheme(false) }
                                    .padding(12.dp)
                                    .testTag("theme_mode_light"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (!isDarkTheme) cs.primary else cs.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Light Mode",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (!isDarkTheme) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = cs.onSurface
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = cs.outlineVariant)

                        // Neon vs No-Neon Toggle (Default: Neon ON)
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
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = cs.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Neon Mode",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = cs.onSurface
                                    )
                                    Text(
                                        text = if (isNeonMode) "Vibrant neon glow & saturated backgrounds (Default: On)"
                                        else "Standard muted tonal palette (No Neon)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cs.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isNeonMode,
                                onCheckedChange = onSetNeonMode,
                                modifier = Modifier.testTag("neon_mode_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Neon On Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isNeonMode) cs.primaryContainer else cs.surfaceVariant)
                                    .border(
                                        width = if (isNeonMode) 2.dp else 1.dp,
                                        color = if (isNeonMode) cs.primary else cs.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSetNeonMode(true) }
                                    .padding(12.dp)
                                    .testTag("theme_neon_on"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isNeonMode) cs.primary else cs.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Neon (Default)",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isNeonMode) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = cs.onSurface
                                    )
                                }
                            }

                            // No Neon Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isNeonMode) cs.primaryContainer else cs.surfaceVariant)
                                    .border(
                                        width = if (!isNeonMode) 2.dp else 1.dp,
                                        color = if (!isNeonMode) cs.primary else cs.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSetNeonMode(false) }
                                    .padding(12.dp)
                                    .testTag("theme_neon_off"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = if (!isNeonMode) cs.primary else cs.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "No Neon",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (!isNeonMode) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = cs.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Accent Color Picker (Blue Default, Red, Orange, Yellow, Green, Purple, Cyan, Pink)
            item {
                Surface(
                    color = cs.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cs.outlineVariant, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = cs.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Theme Accent Color",
                                style = MaterialTheme.typography.titleMedium,
                                color = cs.onSurface
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeAccentColor.entries.chunked(4).forEach { rowColors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowColors.forEach { colorOption ->
                                        val isSelected = accentColor == colorOption
                                        val swatchColor = Color(colorOption.activePrimaryHex(isDarkTheme, isNeonMode))
                                        val tileBgColor = if (isSelected) {
                                            swatchColor.copy(alpha = if (isNeonMode) 0.34f else 0.26f)
                                        } else {
                                            Color(colorOption.activeSurfaceVariantHex(isDarkTheme, isNeonMode))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(tileBgColor)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) swatchColor else cs.outlineVariant,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { onSelectAccentColor(colorOption) }
                                                .padding(vertical = 10.dp, horizontal = 6.dp)
                                                .testTag("theme_color_${colorOption.name.lowercase()}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(swatchColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = if (colorOption == ThemeAccentColor.YELLOW) Color.Black else Color.White,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = colorOption.displayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = cs.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Search Engine & WebView Settings
            item {
                Surface(
                    color = cs.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cs.outlineVariant, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Default Search Engine",
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SearchEngineOption.entries.forEach { engine ->
                                val selected = searchEngine == engine
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) cs.primaryContainer else cs.surfaceVariant)
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) cs.primary else cs.outlineVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectSearchEngine(engine) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = engine.displayName,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) cs.primary else cs.onSurface
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = cs.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable JavaScript",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = cs.onSurface
                                )
                                Text(
                                    text = "Required for interactive websites and Chrome DevTools inspection",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = javaScriptEnabled,
                                onCheckedChange = onToggleJavaScript
                            )
                        }

                        HorizontalDivider(color = cs.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Clear Site Storage & Cache",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = cs.onSurface
                                )
                                Text(
                                    text = if (clearedDataFeedback) "Cleared all LocalStorage, SessionStorage, Cookies & Cache!"
                                    else "Wipe LocalStorage, SessionStorage, Cookies, and WebView cache",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (clearedDataFeedback) cs.primary else cs.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cs.error.copy(alpha = 0.16f))
                                    .border(1.dp, cs.error, RoundedCornerShape(8.dp))
                                    .clickable {
                                        onClearSiteData()
                                        clearedDataFeedback = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "Clear Data",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = cs.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
