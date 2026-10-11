package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.model.DeviceViewportPreset
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun BrowserTopBar(
    activeTab: BrowserTab,
    tabCount: Int,
    isCurrentPageBookmarked: Boolean,
    isElementPickerActive: Boolean,
    isDeviceToolbarOpen: Boolean,
    isViewportLandscape: Boolean,
    isFindInPageVisible: Boolean,
    findInPageQuery: String,
    isDevToolsOpen: Boolean,
    errorCount: Int,
    warnCount: Int,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onToggleElementPicker: () -> Unit,
    onToggleDeviceToolbar: () -> Unit,
    onSelectViewportPreset: (DeviceViewportPreset) -> Unit,
    onToggleViewportOrientation: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onToggleBookmarkCurrentPage: () -> Unit,
    onToggleFindInPage: (Boolean) -> Unit,
    onFindInPageQueryChange: (String) -> Unit,
    onFindNextInPage: (Boolean) -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onToggleDevTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember(activeTab.url) { mutableStateOf(activeTab.url) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(activeTab.url) {
        urlInput = activeTab.url
    }

    Surface(
        color = cs.surface,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Home quick button
                IconButton(
                    onClick = { onNavigate(BrowserTab.URL_HOME) },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("browser_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home (devbrowser:home)",
                        tint = if (activeTab.url == BrowserTab.URL_HOME) cs.primary else cs.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Spacious Address Bar / Omnibox
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(cs.background)
                        .border(1.dp, cs.outlineVariant, RoundedCornerShape(19.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isInternal = activeTab.url.startsWith("devbrowser:", ignoreCase = true)
                    val isSecure = activeTab.url.startsWith("https://")
                    Icon(
                        imageVector = if (isInternal) Icons.Default.Science else Icons.Default.Lock,
                        contentDescription = "Address security indicator",
                        tint = when {
                            isInternal -> cs.primary
                            isSecure -> DevEmerald
                            else -> DevAmber
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = cs.onSurface
                        ),
                        cursorBrush = SolidColor(cs.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                onNavigate(urlInput)
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("url_omnibox_input")
                    )
                    if (urlInput.isNotEmpty() && urlInput != activeTab.url) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear input",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { urlInput = "" }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Page",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                focusManager.clearFocus()
                                onReload()
                            }
                            .testTag("browser_reload_button")
                    )
                }

                // Element Picker ("Inspect Element") Quick Toggle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isElementPickerActive) cs.primaryContainer else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isElementPickerActive) cs.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleElementPicker() }
                        .testTag("element_picker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdsClick,
                        contentDescription = "Inspect Element on Page",
                        tint = if (isElementPickerActive) cs.primary else cs.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tab Switcher Counter Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(cs.surfaceVariant)
                        .border(1.5.dp, cs.outline, RoundedCornerShape(8.dp))
                        .clickable { onOpenTabSwitcher() }
                        .testTag("tab_switcher_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabCount.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                }

                // Chrome DevTools Toggle Button with Error/Warning Badge
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDevToolsOpen) cs.primary else cs.surfaceVariant)
                        .clickable { onToggleDevTools() }
                        .padding(horizontal = 9.dp)
                        .testTag("toggle_devtools_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Toggle Chrome DevTools",
                            tint = if (isDevToolsOpen) cs.onPrimary else cs.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        if (errorCount > 0 || warnCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (errorCount > 0) DevRose else DevAmber)
                                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = if (errorCount > 0) "$errorCount" else "$warnCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Browser Overflow Menu (⋮)
                Box {
                    IconButton(
                        onClick = { showOverflowMenu = true },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("browser_overflow_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Browser Menu",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false },
                        modifier = Modifier.background(cs.surface)
                    ) {
                        // Top quick action row: Back / Forward / Star / Reload
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = {
                                    showOverflowMenu = false
                                    onBack()
                                },
                                enabled = activeTab.canGoBack
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (activeTab.canGoBack) cs.onSurface else cs.outline
                                )
                            }
                            IconButton(
                                onClick = {
                                    showOverflowMenu = false
                                    onForward()
                                },
                                enabled = activeTab.canGoForward
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward",
                                    tint = if (activeTab.canGoForward) cs.onSurface else cs.outline
                                )
                            }
                            IconButton(
                                onClick = {
                                    onToggleBookmarkCurrentPage()
                                    showOverflowMenu = false
                                }
                            ) {
                                Icon(
                                    imageVector = if (isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark current page",
                                    tint = if (isCurrentPageBookmarked) cs.primary else cs.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    showOverflowMenu = false
                                    onReload()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = cs.onSurface
                                )
                            }
                        }

                        HorizontalDivider(color = cs.outlineVariant)

                        // Add / Remove current page to Bookmarks
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isCurrentPageBookmarked) "Remove from Bookmarks" else "Add to Bookmarks",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    tint = cs.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showOverflowMenu = false
                                onToggleBookmarkCurrentPage()
                            },
                            modifier = Modifier.testTag("menu_add_bookmark")
                        )

                        // Bookmarks page (devbrowser:bookmarks)
                        DropdownMenuItem(
                            text = { Text("Bookmarks", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.Bookmarks, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showOverflowMenu = false
                                onNavigate(BrowserTab.URL_BOOKMARKS)
                            },
                            modifier = Modifier.testTag("menu_open_bookmarks")
                        )

                        // History page (devbrowser:history)
                        DropdownMenuItem(
                            text = { Text("History", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.History, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showOverflowMenu = false
                                onNavigate(BrowserTab.URL_HISTORY)
                            },
                            modifier = Modifier.testTag("menu_open_history")
                        )

                        // Test page (devbrowser:test)
                        DropdownMenuItem(
                            text = { Text("DevTools Test Page", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.Science, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showOverflowMenu = false
                                onNavigate(BrowserTab.URL_TEST)
                            },
                            modifier = Modifier.testTag("menu_open_test_page")
                        )

                        HorizontalDivider(color = cs.outlineVariant)

                        DropdownMenuItem(
                            text = { Text("Find in Page…", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.FindInPage, contentDescription = null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showOverflowMenu = false
                                onToggleFindInPage(true)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Device Viewport Toolbar", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = if (isDeviceToolbarOpen) cs.primary else cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showOverflowMenu = false
                                onToggleDeviceToolbar()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Desktop Site", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.DesktopWindows, contentDescription = null, tint = if (activeTab.isDesktopSite) cs.primary else cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                Checkbox(
                                    checked = activeTab.isDesktopSite,
                                    onCheckedChange = null
                                )
                            },
                            onClick = {
                                showOverflowMenu = false
                                onToggleDesktopSite()
                            }
                        )

                        HorizontalDivider(color = cs.outlineVariant)

                        // Settings page (devbrowser:settings)
                        DropdownMenuItem(
                            text = { Text("Settings", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showOverflowMenu = false
                                onNavigate(BrowserTab.URL_SETTINGS)
                            },
                            modifier = Modifier.testTag("menu_open_settings")
                        )
                    }
                }
            }

            // Collapsible Find in Page Bar
            AnimatedVisibility(visible = isFindInPageVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FindInPage,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    BasicTextField(
                        value = findInPageQuery,
                        onValueChange = onFindInPageQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = cs.onSurface
                        ),
                        cursorBrush = SolidColor(cs.primary),
                        decorationBox = { inner ->
                            if (findInPageQuery.isEmpty()) {
                                Text("Find in page…", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            }
                            inner()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(cs.background)
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                    IconButton(
                        onClick = { onFindNextInPage(false) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match", tint = cs.onSurface, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { onFindNextInPage(true) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match", tint = cs.onSurface, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { onToggleFindInPage(false) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Find in Page", tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Compact Element Picker Indicator Strip
            AnimatedVisibility(visible = isElementPickerActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Inspect Mode: Tap any element on the page to select it",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onToggleElementPicker() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Collapsible Device Viewport Emulation Bar
            AnimatedVisibility(visible = isDeviceToolbarOpen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.background)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DeviceViewportPreset.entries.forEach { preset ->
                        val selected = activeTab.devicePreset == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) cs.primaryContainer else cs.surfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (selected) cs.primary else cs.outlineVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectViewportPreset(preset) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("viewport_preset_${preset.name.lowercase()}")
                        ) {
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) cs.primary else cs.onSurfaceVariant
                            )
                        }
                    }
                    if (activeTab.devicePreset.widthDp != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(cs.surfaceVariant)
                                .border(1.dp, cs.outlineVariant, RoundedCornerShape(6.dp))
                                .clickable { onToggleViewportOrientation() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate viewport",
                                    tint = cs.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isViewportLandscape) "Landscape" else "Portrait",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.primary
                                )
                            }
                        }
                    }
                }
            }

            if (activeTab.isLoading) {
                LinearProgressIndicator(
                    progress = { (activeTab.progress / 100f).coerceIn(0.05f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = cs.primary,
                    trackColor = cs.surface
                )
            } else {
                HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)
            }
        }
    }
}
