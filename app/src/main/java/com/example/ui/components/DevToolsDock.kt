package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DevToolsPanelTab
import com.example.ui.panels.ConsolePanel
import com.example.ui.panels.ElementsPanel
import com.example.ui.panels.NetworkPanel
import com.example.ui.panels.PerformancePanel
import com.example.ui.panels.SourcesPanel
import com.example.ui.panels.StoragePanel
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevRose
import com.example.viewmodel.BrowserUiState
import kotlin.math.roundToInt

@Composable
fun DevToolsDock(
    uiState: BrowserUiState,
    totalHeightPx: Float,
    onDragDockDelta: (Float) -> Unit,
    onSelectTab: (DevToolsPanelTab) -> Unit,
    onToggleElementPicker: () -> Unit,
    onSnapDockHeight: () -> Unit,
    onRefreshDevTools: () -> Unit,
    onCloseDevTools: () -> Unit,
    // Elements callbacks
    onDomSearchChange: (String) -> Unit,
    onSelectDomNode: (com.example.model.DomNodeItem) -> Unit,
    onApplyInlineStyle: (String, String) -> Unit,
    onHideNode: (String) -> Unit,
    onEditNodeText: (String, String) -> Unit,
    onSetAttribute: (String, String, String) -> Unit,
    onRemoveNode: (String) -> Unit,
    onAppendChildNode: (String, String, String) -> Unit,
    // Console callbacks
    onConsoleFilterChange: (com.example.model.ConsoleLogLevel?) -> Unit,
    onConsoleSearchChange: (String) -> Unit,
    onClearConsole: () -> Unit,
    onExecuteJs: (String) -> Unit,
    // Network callbacks
    onNetworkFilterChange: (com.example.model.NetworkResourceType) -> Unit,
    onSelectNetworkRequest: (com.example.model.NetworkRequestEntry?) -> Unit,
    onTogglePreserveLog: () -> Unit,
    onToggleOffline: () -> Unit,
    onClearNetwork: () -> Unit,
    onTriggerSyntheticFetch: (String, String, Int) -> Unit,
    onReplayRequest: (com.example.model.NetworkRequestEntry) -> Unit,
    onBuildCurl: (com.example.model.NetworkRequestEntry) -> String,
    // Storage callbacks
    onSelectStorageType: (com.example.model.StorageType) -> Unit,
    onUpsertStorage: (com.example.model.StorageType, String, String) -> Unit,
    onDeleteStorage: (com.example.model.StorageType, String) -> Unit,
    onClearStorageType: (com.example.model.StorageType) -> Unit,
    // Performance & Sources callbacks
    onRunAudit: () -> Unit,
    onSourceSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        color = cs.surface,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Draggable Split Handle + Top Chrome DevTools Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.surfaceVariant)
                    .pointerInput(totalHeightPx) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            if (totalHeightPx > 100f) {
                                onDragDockDelta(-dragAmount / totalHeightPx)
                            }
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Element Picker Toggle (Top-left icon in Chrome DevTools)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (uiState.isElementPickerActive) cs.primaryContainer else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (uiState.isElementPickerActive) cs.primary else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggleElementPicker() }
                        .testTag("devtools_dock_picker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdsClick,
                        contentDescription = "Select an element in the page to inspect it",
                        tint = if (uiState.isElementPickerActive) cs.primary else cs.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Scrollable DevTools Panel Tabs
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    DevToolsPanelTab.entries.forEach { tab ->
                        val active = uiState.activeDevToolsTab == tab
                        val icon = when (tab) {
                            DevToolsPanelTab.ELEMENTS -> Icons.Default.Code
                            DevToolsPanelTab.CONSOLE -> Icons.Default.Terminal
                            DevToolsPanelTab.NETWORK -> Icons.Default.SwapVert
                            DevToolsPanelTab.APPLICATION -> Icons.Default.Storage
                            DevToolsPanelTab.PERFORMANCE -> Icons.Default.Speed
                            DevToolsPanelTab.SOURCES -> Icons.Default.Description
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (active) cs.primaryContainer else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (active) cs.primary else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { onSelectTab(tab) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("devtools_tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) cs.primary else cs.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (active) cs.primary else cs.onSurfaceVariant
                                )

                                if (tab == DevToolsPanelTab.CONSOLE && uiState.errorCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(DevRose)
                                            .padding(horizontal = 4.dp, vertical = 0.5.dp)
                                    ) {
                                        Text(
                                            text = "${uiState.errorCount}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                } else if (tab == DevToolsPanelTab.NETWORK && uiState.networkRequests.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(cs.primaryContainer)
                                            .padding(horizontal = 4.dp, vertical = 0.5.dp)
                                    ) {
                                        Text(
                                            text = "${uiState.networkRequests.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            color = cs.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dock Height Snap / Drag Readout
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(cs.surface)
                        .border(1.dp, cs.outline, RoundedCornerShape(6.dp))
                        .clickable { onSnapDockHeight() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("devtools_resize_dock_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = "Resize DevTools Dock",
                            tint = DevAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${(uiState.dockHeightFraction * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = DevAmber
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshDevTools,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync DOM & Storage",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onCloseDevTools,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("close_devtools_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close DevTools",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

            // Active DevTools Panel Body
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState.activeDevToolsTab) {
                    DevToolsPanelTab.ELEMENTS -> ElementsPanel(
                        domNodes = uiState.domNodes,
                        selectedNode = uiState.selectedDomNode,
                        searchQuery = uiState.domSearchQuery,
                        onSearchQueryChange = onDomSearchChange,
                        onSelectNode = onSelectDomNode,
                        onApplyInlineStyle = onApplyInlineStyle,
                        onHideNode = onHideNode,
                        onEditNodeText = onEditNodeText,
                        onSetAttribute = onSetAttribute,
                        onRemoveNode = onRemoveNode,
                        onAppendChildNode = onAppendChildNode
                    )

                    DevToolsPanelTab.CONSOLE -> ConsolePanel(
                        logs = uiState.consoleLogs,
                        selectedFilter = uiState.consoleLevelFilter,
                        searchQuery = uiState.consoleSearchQuery,
                        commandHistory = uiState.commandHistory,
                        snippets = uiState.snippets,
                        onSelectFilter = onConsoleFilterChange,
                        onSearchQueryChange = onConsoleSearchChange,
                        onClearLogs = onClearConsole,
                        onExecuteJs = onExecuteJs
                    )

                    DevToolsPanelTab.NETWORK -> NetworkPanel(
                        requests = uiState.networkRequests,
                        selectedFilter = uiState.networkTypeFilter,
                        selectedRequest = uiState.selectedNetworkRequest,
                        preserveLog = uiState.preserveNetworkLog,
                        isOfflineThrottled = uiState.isOfflineThrottled,
                        onSelectFilter = onNetworkFilterChange,
                        onSelectRequest = onSelectNetworkRequest,
                        onTogglePreserveLog = onTogglePreserveLog,
                        onToggleOffline = onToggleOffline,
                        onClearRequests = onClearNetwork,
                        onTriggerSyntheticFetch = onTriggerSyntheticFetch,
                        onReplayRequest = onReplayRequest,
                        onBuildCurl = onBuildCurl
                    )

                    DevToolsPanelTab.APPLICATION -> StoragePanel(
                        entries = uiState.storageEntries,
                        selectedType = uiState.selectedStorageType,
                        onSelectType = onSelectStorageType,
                        onUpsertEntry = onUpsertStorage,
                        onDeleteEntry = onDeleteStorage,
                        onClearType = onClearStorageType
                    )

                    DevToolsPanelTab.PERFORMANCE -> PerformancePanel(
                        metrics = uiState.performanceMetrics,
                        onRunAudit = onRunAudit
                    )

                    DevToolsPanelTab.SOURCES -> SourcesPanel(
                        htmlSource = uiState.pageSourceHtml,
                        searchQuery = uiState.sourceSearchQuery,
                        onSearchQueryChange = onSourceSearchChange
                    )
                }
            }
        }
    }
}
