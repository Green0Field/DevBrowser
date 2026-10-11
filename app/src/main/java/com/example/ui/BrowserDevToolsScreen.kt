package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devtools.DevScopeJsBridge
import com.example.devtools.DevToolsScripts
import com.example.model.BrowserTab
import com.example.ui.components.BrowserTopBar
import com.example.ui.components.DevToolsDock
import com.example.ui.components.TabSwitcherOverlay
import com.example.ui.pages.DevBrowserBookmarksPage
import com.example.ui.pages.DevBrowserHistoryPage
import com.example.ui.pages.DevBrowserHomePage
import com.example.ui.pages.DevBrowserSettingsPage
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserDevToolsViewModel
import com.example.viewmodel.BrowserUiState
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserDevToolsScreen(
    viewModel: BrowserDevToolsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = uiState.activeTab.canGoBack) {
        viewModel.goBack()
    }

    val jsBridge = remember(viewModel) {
        DevScopeJsBridge(
            onConsoleLogReceived = viewModel::onBridgeConsoleLog,
            onDomTreeReceived = viewModel::onBridgeDomTreeUpdated,
            onNodeSelectedReceived = viewModel::onBridgeNodeSelected,
            onNetworkRequestReceived = viewModel::onBridgeNetworkRequest,
            onStorageReceived = viewModel::onBridgeStorageUpdated,
            onPerformanceReceived = viewModel::onBridgePerformanceUpdated,
            onSourcesReceived = viewModel::onBridgeSourcesUpdated
        )
    }

    val webViewInstance = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            WebView.setWebContentsDebuggingEnabled(true)
            CookieManager.getInstance().setAcceptCookie(true)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadsImagesAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                userAgentString = uiState.activeTab.devicePreset.userAgent
            }
            addJavascriptInterface(jsBridge, "DevScopeBridge")

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (url != null) {
                        viewModel.onPageStarted(url)
                    }
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if (url != null) {
                        viewModel.onWebViewResourceLoaded(url)
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (view != null && url != null) {
                        viewModel.onPageFinished(
                            rawUrl = url,
                            title = view.title,
                            canGoBack = view.canGoBack(),
                            canGoForward = view.canGoForward()
                        )
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val reqUrl = request?.url?.toString() ?: return false
                    if (reqUrl.startsWith("devbrowser:", ignoreCase = true)) {
                        viewModel.navigateToInput(reqUrl)
                        return true
                    }
                    return false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    viewModel.onPageProgressChanged(newProgress)
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    return true
                }
            }

            // Preload devbrowser:test HTML in the background so DevTools is always ready
            loadDataWithBaseURL(
                BrowserTab.INTERNAL_TEST_BASE_URL,
                DevToolsScripts.PLAYGROUND_HTML,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    DisposableEffect(webViewInstance) {
        viewModel.attachWebView(webViewInstance)
        onDispose {
            viewModel.attachWebView(null)
            webViewInstance.destroy()
        }
    }

    MyApplicationTheme(
        darkTheme = uiState.isDarkTheme,
        isNeon = uiState.isNeonMode,
        accentColor = uiState.accentColor
    ) {
        val cs = MaterialTheme.colorScheme
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cs.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Streamlined Top Browser Bar + Overflow Menu + Find in Page
                BrowserTopBar(
                    activeTab = uiState.activeTab,
                    tabCount = uiState.tabs.size,
                    isCurrentPageBookmarked = uiState.isCurrentPageBookmarked,
                    isElementPickerActive = uiState.isElementPickerActive,
                    isDeviceToolbarOpen = uiState.isDeviceToolbarOpen,
                    isViewportLandscape = uiState.isViewportLandscape,
                    isFindInPageVisible = uiState.isFindInPageVisible,
                    findInPageQuery = uiState.findInPageQuery,
                    isDevToolsOpen = uiState.isDevToolsOpen,
                    errorCount = uiState.errorCount,
                    warnCount = uiState.warnCount,
                    onNavigate = { viewModel.navigateToInput(it) },
                    onBack = viewModel::goBack,
                    onForward = viewModel::goForward,
                    onReload = viewModel::reloadActivePage,
                    onToggleElementPicker = viewModel::toggleElementPicker,
                    onToggleDeviceToolbar = viewModel::toggleDeviceToolbar,
                    onSelectViewportPreset = viewModel::selectViewportPreset,
                    onToggleViewportOrientation = viewModel::toggleViewportOrientation,
                    onToggleDesktopSite = viewModel::toggleDesktopSite,
                    onToggleBookmarkCurrentPage = viewModel::toggleBookmarkCurrentPage,
                    onToggleFindInPage = viewModel::toggleFindInPage,
                    onFindInPageQueryChange = viewModel::updateFindInPageQuery,
                    onFindNextInPage = viewModel::findNextInPage,
                    onOpenTabSwitcher = { viewModel.toggleTabSwitcher(true) },
                    onToggleDevTools = viewModel::toggleDevTools
                )

                // Main Content Area: Adaptive Split between Browser Viewport and Chrome DevTools Dock
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val isWideScreen = maxWidth >= 720.dp
                    val density = LocalDensity.current
                    val totalHeightPx = with(density) { maxHeight.toPx() }

                    if (isWideScreen && uiState.isDevToolsOpen) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            BrowserViewportContainer(
                                webView = webViewInstance,
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxHeight()
                            )
                            VerticalDivider(thickness = 2.dp, color = cs.primary.copy(alpha = 0.5f))
                            DevToolsDock(
                                uiState = uiState,
                                totalHeightPx = totalHeightPx,
                                onDragDockDelta = viewModel::adjustDockHeightByDelta,
                                onSelectTab = viewModel::selectDevToolsTab,
                                onToggleElementPicker = viewModel::toggleElementPicker,
                                onSnapDockHeight = viewModel::snapDockHeightPreset,
                                onRefreshDevTools = viewModel::refreshAllDevToolsData,
                                onCloseDevTools = viewModel::toggleDevTools,
                                onDomSearchChange = viewModel::updateDomSearchQuery,
                                onSelectDomNode = viewModel::selectDomNodeFromTree,
                                onApplyInlineStyle = viewModel::applyInlineStyleToSelected,
                                onHideNode = viewModel::hideDomNode,
                                onEditNodeText = viewModel::editDomNodeText,
                                onSetAttribute = viewModel::setDomNodeAttribute,
                                onRemoveNode = viewModel::removeDomNode,
                                onAppendChildNode = viewModel::appendChildElementToNode,
                                onConsoleFilterChange = viewModel::setConsoleLevelFilter,
                                onConsoleSearchChange = viewModel::setConsoleSearchQuery,
                                onClearConsole = viewModel::clearConsoleLogs,
                                onExecuteJs = viewModel::evaluateJavaScriptInConsole,
                                onNetworkFilterChange = viewModel::setNetworkTypeFilter,
                                onSelectNetworkRequest = viewModel::selectNetworkRequest,
                                onTogglePreserveLog = viewModel::togglePreserveNetworkLog,
                                onToggleOffline = viewModel::toggleOfflineThrottling,
                                onClearNetwork = viewModel::clearNetworkRequests,
                                onTriggerSyntheticFetch = viewModel::triggerSyntheticApiRequest,
                                onReplayRequest = viewModel::replayNetworkRequest,
                                onBuildCurl = viewModel::buildCurlCommand,
                                onSelectStorageType = viewModel::selectStorageType,
                                onUpsertStorage = viewModel::setStorageKeyValue,
                                onDeleteStorage = viewModel::deleteStorageKey,
                                onClearStorageType = viewModel::clearStorageByType,
                                onRunAudit = viewModel::runLighthouseAudit,
                                onSourceSearchChange = viewModel::updateSourceSearchQuery,
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            val dockFraction = if (uiState.isDevToolsOpen) uiState.dockHeightFraction else 0f
                            val webViewFraction = (1f - dockFraction).coerceIn(0.20f, 1f)

                            BrowserViewportContainer(
                                webView = webViewInstance,
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(webViewFraction)
                            )

                            if (uiState.isDevToolsOpen) {
                                HorizontalDivider(thickness = 2.dp, color = cs.primary.copy(alpha = 0.55f))
                                DevToolsDock(
                                    uiState = uiState,
                                    totalHeightPx = totalHeightPx,
                                    onDragDockDelta = viewModel::adjustDockHeightByDelta,
                                    onSelectTab = viewModel::selectDevToolsTab,
                                    onToggleElementPicker = viewModel::toggleElementPicker,
                                    onSnapDockHeight = viewModel::snapDockHeightPreset,
                                    onRefreshDevTools = viewModel::refreshAllDevToolsData,
                                    onCloseDevTools = viewModel::toggleDevTools,
                                    onDomSearchChange = viewModel::updateDomSearchQuery,
                                    onSelectDomNode = viewModel::selectDomNodeFromTree,
                                    onApplyInlineStyle = viewModel::applyInlineStyleToSelected,
                                    onHideNode = viewModel::hideDomNode,
                                    onEditNodeText = viewModel::editDomNodeText,
                                    onSetAttribute = viewModel::setDomNodeAttribute,
                                    onRemoveNode = viewModel::removeDomNode,
                                    onAppendChildNode = viewModel::appendChildElementToNode,
                                    onConsoleFilterChange = viewModel::setConsoleLevelFilter,
                                    onConsoleSearchChange = viewModel::setConsoleSearchQuery,
                                    onClearConsole = viewModel::clearConsoleLogs,
                                    onExecuteJs = viewModel::evaluateJavaScriptInConsole,
                                    onNetworkFilterChange = viewModel::setNetworkTypeFilter,
                                    onSelectNetworkRequest = viewModel::selectNetworkRequest,
                                    onTogglePreserveLog = viewModel::togglePreserveNetworkLog,
                                    onToggleOffline = viewModel::toggleOfflineThrottling,
                                    onClearNetwork = viewModel::clearNetworkRequests,
                                    onTriggerSyntheticFetch = viewModel::triggerSyntheticApiRequest,
                                    onReplayRequest = viewModel::replayNetworkRequest,
                                    onBuildCurl = viewModel::buildCurlCommand,
                                    onSelectStorageType = viewModel::selectStorageType,
                                    onUpsertStorage = viewModel::setStorageKeyValue,
                                    onDeleteStorage = viewModel::deleteStorageKey,
                                    onClearStorageType = viewModel::clearStorageByType,
                                    onRunAudit = viewModel::runLighthouseAudit,
                                    onSourceSearchChange = viewModel::updateSourceSearchQuery,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(dockFraction)
                                )
                            }
                        }
                    }
                }
            }

            // Multi-Tab Switcher Modal Overlay
            if (uiState.showTabSwitcher) {
                TabSwitcherOverlay(
                    tabs = uiState.tabs,
                    activeTabId = uiState.activeTabId,
                    onSelectTab = viewModel::switchTab,
                    onCloseTab = viewModel::closeTab,
                    onNewTab = { viewModel.createNewTab() },
                    onDismiss = { viewModel.toggleTabSwitcher(false) }
                )
            }
        }
    }
}

@Composable
private fun BrowserViewportContainer(
    webView: WebView,
    uiState: BrowserUiState,
    viewModel: BrowserDevToolsViewModel,
    modifier: Modifier = Modifier
) {
    val activeUrl = uiState.activeTab.url.lowercase(Locale.US)
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .background(cs.background)
            .testTag("browser_webview_container"),
        contentAlignment = Alignment.Center
    ) {
        when (activeUrl) {
            BrowserTab.URL_HOME -> DevBrowserHomePage(
                shortcuts = uiState.homeShortcuts,
                searchEngine = uiState.searchEngine,
                onNavigate = { viewModel.navigateToInput(it) },
                onPinShortcut = viewModel::pinShortcut,
                onAddShortcut = viewModel::addCustomShortcut,
                onUnpinShortcut = viewModel::unpinShortcut,
                onRemoveShortcut = viewModel::removeShortcut
            )

            BrowserTab.URL_BOOKMARKS -> DevBrowserBookmarksPage(
                bookmarks = uiState.bookmarks,
                onOpenBookmark = viewModel::openBookmark,
                onSelectUrl = { viewModel.navigateToInput(it) },
                onAddBookmark = viewModel::addCustomBookmark,
                onRemoveBookmark = viewModel::removeBookmark
            )

            BrowserTab.URL_HISTORY -> DevBrowserHistoryPage(
                history = uiState.history,
                onSelectUrl = { viewModel.navigateToInput(it) },
                onDeleteHistoryItem = viewModel::deleteHistoryItem,
                onClearHistory = viewModel::clearBrowsingHistory
            )

            BrowserTab.URL_SETTINGS -> DevBrowserSettingsPage(
                isDarkTheme = uiState.isDarkTheme,
                isNeonMode = uiState.isNeonMode,
                accentColor = uiState.accentColor,
                searchEngines = uiState.searchEngines,
                searchEngine = uiState.searchEngine,
                javaScriptEnabled = uiState.javaScriptEnabled,
                onSetDarkTheme = viewModel::setDarkTheme,
                onSetNeonMode = viewModel::setNeonMode,
                onSelectAccentColor = viewModel::setThemeAccentColor,
                onSelectSearchEngine = viewModel::setSearchEngine,
                onAddCustomSearchEngine = viewModel::addCustomSearchEngine,
                onRemoveCustomSearchEngine = viewModel::removeCustomSearchEngine,
                onToggleJavaScript = viewModel::setJavaScriptEnabled,
                onClearSiteData = viewModel::clearAllBrowserSiteData
            )

            else -> {
                val preset = uiState.activeTab.devicePreset
                val hasCustomFrame = preset.widthDp != null && preset.heightDp != null
                if (hasCustomFrame) {
                    val targetW = if (uiState.isViewportLandscape) preset.heightDp!! else preset.widthDp!!
                    val targetH = if (uiState.isViewportLandscape) preset.widthDp!! else preset.heightDp!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(cs.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.primary
                            )
                            Text(
                                text = "$targetW × $targetH dp",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(targetW.dp)
                                .height(targetH.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, cs.outline, RoundedCornerShape(12.dp))
                        ) {
                            AndroidView(
                                factory = { webView },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    AndroidView(
                        factory = { webView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
