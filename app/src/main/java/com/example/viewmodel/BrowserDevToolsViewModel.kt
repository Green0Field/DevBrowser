package com.example.viewmodel

import android.content.res.Configuration
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devtools.DevToolsScripts
import com.example.model.BrowserTab
import com.example.model.ConsoleLogEntry
import com.example.model.ConsoleLogLevel
import com.example.model.DevToolsPanelTab
import com.example.model.DeviceViewportPreset
import com.example.model.DomNodeItem
import com.example.model.HistoryItem
import com.example.model.HomeShortcut
import com.example.model.JsSnippet
import com.example.model.NetworkRequestEntry
import com.example.model.NetworkResourceType
import com.example.model.PerformanceMetrics
import com.example.model.QuickBookmark
import com.example.model.SearchEngineItem
import com.example.model.ShortcutSource
import com.example.model.StorageEntry
import com.example.model.StorageType
import com.example.model.ThemeAccentColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class BrowserUiState(
    val tabs: List<BrowserTab> = listOf(BrowserTab(id = "tab_1", title = "Home", url = BrowserTab.URL_HOME)),
    val activeTabId: String = "tab_1",
    val showTabSwitcher: Boolean = false,
    val isFindInPageVisible: Boolean = false,
    val findInPageQuery: String = "",
    val isDevToolsOpen: Boolean = false,
    val activeDevToolsTab: DevToolsPanelTab = DevToolsPanelTab.ELEMENTS,
    val dockHeightFraction: Float = 0.48f,
    val isElementPickerActive: Boolean = false,
    val isDeviceToolbarOpen: Boolean = false,
    val isViewportLandscape: Boolean = false,
    val isDarkTheme: Boolean = true,
    val isNeonMode: Boolean = true,
    val accentColor: ThemeAccentColor = ThemeAccentColor.BLUE,
    val searchEngines: List<SearchEngineItem> = SearchEngineItem.DEFAULT_ENGINES,
    val searchEngine: SearchEngineItem = SearchEngineItem.DUCKDUCKGO,
    val javaScriptEnabled: Boolean = true,
    val domNodes: List<DomNodeItem> = emptyList(),
    val selectedDomNode: DomNodeItem? = null,
    val domSearchQuery: String = "",
    val consoleLogs: List<ConsoleLogEntry> = emptyList(),
    val consoleLevelFilter: ConsoleLogLevel? = null,
    val consoleSearchQuery: String = "",
    val commandHistory: List<String> = listOf(
        "document.title",
        "\$0",
        "performance.now()",
        "Object.keys(localStorage)"
    ),
    val networkRequests: List<NetworkRequestEntry> = emptyList(),
    val networkTypeFilter: NetworkResourceType = NetworkResourceType.ALL,
    val networkSearchQuery: String = "",
    val selectedNetworkRequest: NetworkRequestEntry? = null,
    val preserveNetworkLog: Boolean = false,
    val isOfflineThrottled: Boolean = false,
    val storageEntries: List<StorageEntry> = emptyList(),
    val selectedStorageType: StorageType = StorageType.LOCAL_STORAGE,
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics(),
    val pageSourceHtml: String = "",
    val sourceSearchQuery: String = "",
    val bookmarks: List<QuickBookmark> = DevToolsScripts.DEFAULT_BOOKMARKS,
    val pinnedShortcuts: List<HomeShortcut> = DevToolsScripts.DEFAULT_PINNED_SHORTCUTS,
    val dismissedAutoShortcutUrls: Set<String> = emptySet(),
    val siteVisitCounts: Map<String, Int> = mapOf(
        "https://news.ycombinator.com" to 6,
        "https://example.com" to 3
    ),
    val bookmarkClickCounts: Map<String, Int> = mapOf(
        "https://en.m.wikipedia.org" to 4,
        "https://httpbin.org/html" to 2
    ),
    val siteTitles: Map<String, String> = mapOf(
        "https://news.ycombinator.com" to "Hacker News",
        "https://example.com" to "Example.com",
        "https://en.m.wikipedia.org" to "Wikipedia",
        "https://httpbin.org/html" to "HTTPBin HTML"
    ),
    val history: List<HistoryItem> = listOf(
        HistoryItem(1L, "Home", BrowserTab.URL_HOME, "Just now")
    ),
    val snippets: List<JsSnippet> = DevToolsScripts.DEFAULT_SNIPPETS
) {
    val activeTab: BrowserTab
        get() = tabs.find { it.id == activeTabId } ?: tabs.first()

    val errorCount: Int
        get() = consoleLogs.count { it.level == ConsoleLogLevel.ERROR }

    val warnCount: Int
        get() = consoleLogs.count { it.level == ConsoleLogLevel.WARN }

    val isCurrentPageBookmarked: Boolean
        get() = bookmarks.any { it.url.equals(activeTab.url, ignoreCase = true) }

    val homeShortcuts: List<HomeShortcut>
        get() {
            val pinnedUrls = pinnedShortcuts.map { it.url.lowercase(Locale.US) }.toSet()
            val excludedInternal = setOf(
                BrowserTab.URL_HOME.lowercase(Locale.US),
                BrowserTab.URL_BOOKMARKS.lowercase(Locale.US),
                BrowserTab.URL_HISTORY.lowercase(Locale.US),
                BrowserTab.URL_SETTINGS.lowercase(Locale.US)
            )
            val dismissedLower = dismissedAutoShortcutUrls.map { it.lowercase(Locale.US) }.toSet()

            val autoVisited = siteVisitCounts.entries
                .sortedByDescending { it.value }
                .filter { (url, _) ->
                    val lower = url.lowercase(Locale.US)
                    lower !in pinnedUrls && lower !in excludedInternal && lower !in dismissedLower
                }
                .take(4)
                .map { (url, visits) ->
                    val title = siteTitles[url]
                        ?: bookmarks.find { it.url.equals(url, ignoreCase = true) }?.title
                        ?: url.removePrefix("https://").removePrefix("http://").take(24)
                    HomeShortcut(
                        id = "auto_visit_${url.hashCode()}",
                        title = title,
                        url = url,
                        isPinned = false,
                        source = ShortcutSource.MOST_VISITED,
                        count = visits
                    )
                }

            val usedUrls = pinnedUrls + autoVisited.map { it.url.lowercase(Locale.US) }.toSet()

            val autoBookmarks = bookmarkClickCounts.entries
                .sortedByDescending { it.value }
                .filter { (url, _) ->
                    val lower = url.lowercase(Locale.US)
                    lower !in usedUrls && lower !in excludedInternal && lower !in dismissedLower
                }
                .take(4)
                .map { (url, clicks) ->
                    val title = bookmarks.find { it.url.equals(url, ignoreCase = true) }?.title
                        ?: siteTitles[url]
                        ?: url.removePrefix("https://").removePrefix("http://").take(24)
                    HomeShortcut(
                        id = "auto_bm_${url.hashCode()}",
                        title = title,
                        url = url,
                        isPinned = false,
                        source = ShortcutSource.TOP_BOOKMARK,
                        count = clicks
                    )
                }

            return (pinnedShortcuts + autoVisited + autoBookmarks).take(12)
        }
}

class BrowserDevToolsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val logIdGenerator = AtomicLong(1L)
    private val historyIdGenerator = AtomicLong(2L)
    private val pageLoadTimestampMs = AtomicLong(System.currentTimeMillis())
    private var webViewRef: WebView? = null

    fun attachWebView(webView: WebView?) {
        webViewRef = webView
        if (webView != null) {
            syncWebViewThemePreference()
        }
    }

    fun syncWebViewThemePreference() {
        val state = _uiState.value
        val isDark = state.isDarkTheme
        val isNeon = state.isNeonMode
        val accent = state.accentColor
        viewModelScope.launch(Dispatchers.Main) {
            val wv = webViewRef ?: return@launch
            try {
                val nightBits = if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                val config = wv.resources.configuration
                config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightBits
                wv.dispatchConfigurationChanged(config)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    wv.settings.isAlgorithmicDarkeningAllowed = isDark
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    @Suppress("DEPRECATION")
                    wv.settings.forceDark = if (isDark) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
                }
            } catch (_: Exception) {
            }

            val script = DevToolsScripts.buildColorSchemeSyncScript(
                isDark = isDark,
                accentHex = accent.cssHex(isDark, isNeon),
                bgHex = accent.cssBgHex(isDark, isNeon),
                surfaceHex = accent.cssSurfaceHex(isDark, isNeon)
            )
            wv.evaluateJavascript(script, null)
        }
    }

    private fun runJs(script: String, onResult: ((String) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.Main) {
            webViewRef?.evaluateJavascript(script) { res ->
                onResult?.invoke(res ?: "undefined")
            }
        }
    }

    private fun currentTimeFormatted(): String {
        return SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    }

    private fun mapRawUrlToDisplayUrl(rawUrl: String): String {
        return if (rawUrl.startsWith(BrowserTab.INTERNAL_TEST_BASE_URL) || rawUrl.contains("devbrowser.internal/test")) {
            BrowserTab.URL_TEST
        } else {
            rawUrl
        }
    }

    private fun titleForInternalUrl(url: String): String {
        return when (url.lowercase(Locale.US)) {
            BrowserTab.URL_HOME -> "Home"
            BrowserTab.URL_BOOKMARKS -> "Bookmarks"
            BrowserTab.URL_HISTORY -> "History"
            BrowserTab.URL_SETTINGS -> "Settings"
            BrowserTab.URL_TEST -> "DevBrowser Test Page"
            else -> url
        }
    }

    private fun recordHistoryEntry(title: String, url: String) {
        _uiState.update { state ->
            val isTrackableSite = !url.startsWith("devbrowser:", ignoreCase = true) || url.equals(BrowserTab.URL_TEST, ignoreCase = true)
            val updatedVisits = if (isTrackableSite) {
                state.siteVisitCounts + (url to ((state.siteVisitCounts[url] ?: 0) + 1))
            } else {
                state.siteVisitCounts
            }
            val updatedTitles = if (isTrackableSite && title.isNotBlank() && title != url) {
                state.siteTitles + (url to title)
            } else {
                state.siteTitles
            }
            val updatedHistory = if (state.history.firstOrNull()?.url == url) {
                state.history
            } else {
                val entry = HistoryItem(
                    id = historyIdGenerator.getAndIncrement(),
                    title = title,
                    url = url,
                    timestamp = SimpleDateFormat("HH:mm", Locale.US).format(Date())
                )
                (listOf(entry) + state.history).take(100)
            }
            state.copy(
                history = updatedHistory,
                siteVisitCounts = updatedVisits,
                siteTitles = updatedTitles
            )
        }
    }

    fun onPageStarted(rawUrl: String) {
        val displayUrl = mapRawUrlToDisplayUrl(rawUrl)
        pageLoadTimestampMs.set(System.currentTimeMillis())
        syncWebViewThemePreference()
        _uiState.update { state ->
            val currentTab = state.activeTab
            // Only overwrite tab URL if it's not currently showing a Compose internal page (home/bookmarks/history/settings)
            val isComposeInternal = currentTab.url in listOf(
                BrowserTab.URL_HOME,
                BrowserTab.URL_BOOKMARKS,
                BrowserTab.URL_HISTORY,
                BrowserTab.URL_SETTINGS
            )
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    if (isComposeInternal && displayUrl == BrowserTab.URL_TEST) {
                        tab
                    } else {
                        tab.copy(url = displayUrl, isLoading = true, progress = 15)
                    }
                } else tab
            }
            val clearedNet = if (state.preserveNetworkLog) state.networkRequests else emptyList()
            state.copy(
                tabs = updatedTabs,
                networkRequests = clearedNet,
                selectedNetworkRequest = if (state.preserveNetworkLog) state.selectedNetworkRequest else null
            )
        }
    }

    fun onPageProgressChanged(progress: Int) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(progress = progress, isLoading = progress < 100)
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
    }

    fun onPageFinished(rawUrl: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
        val displayUrl = mapRawUrlToDisplayUrl(rawUrl)
        val cleanTitle = when {
            displayUrl == BrowserTab.URL_TEST -> "DevBrowser Test Page"
            displayUrl.startsWith("devbrowser:") -> titleForInternalUrl(displayUrl)
            !title.isNullOrBlank() -> title
            else -> displayUrl.removePrefix("https://").removePrefix("http://").take(32)
        }

        _uiState.update { state ->
            val currentTab = state.activeTab
            val isComposeInternal = currentTab.url in listOf(
                BrowserTab.URL_HOME,
                BrowserTab.URL_BOOKMARKS,
                BrowserTab.URL_HISTORY,
                BrowserTab.URL_SETTINGS
            )
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    if (isComposeInternal && displayUrl == BrowserTab.URL_TEST) {
                        tab.copy(isLoading = false, progress = 100)
                    } else {
                        tab.copy(
                            url = displayUrl,
                            title = cleanTitle,
                            isLoading = false,
                            progress = 100,
                            webViewCanGoBack = canGoBack,
                            webViewCanGoForward = canGoForward
                        )
                    }
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }

        val activeUrl = _uiState.value.activeTab.url
        if (activeUrl == BrowserTab.URL_TEST || !activeUrl.startsWith("devbrowser:")) {
            recordHistoryEntry(cleanTitle, displayUrl)
        }

        syncWebViewThemePreference()
        runJs(DevToolsScripts.DEVTOOLS_AGENT_JS) {
            if (_uiState.value.isElementPickerActive) {
                runJs("if(window.__devScopeSetInspectMode) window.__devScopeSetInspectMode(true);")
            }
        }
    }

    private fun resolveInputToUrl(rawInput: String): String {
        val trimmed = rawInput.trim()
        val lower = trimmed.lowercase(Locale.US)
        return when {
            lower == "devbrowser:home" || lower == "about:home" -> BrowserTab.URL_HOME
            lower == "devbrowser:bookmarks" -> BrowserTab.URL_BOOKMARKS
            lower == "devbrowser:history" -> BrowserTab.URL_HISTORY
            lower == "devbrowser:settings" -> BrowserTab.URL_SETTINGS
            lower == "devbrowser:test" || lower == "demo" || lower == "sandbox" ||
                lower.contains("devscope.local") || lower.contains("devbrowser.internal") -> BrowserTab.URL_TEST
            lower.startsWith("devbrowser:") -> lower
            trimmed.startsWith("https://") || trimmed.startsWith("http://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> {
                _uiState.value.searchEngine.formatSearchUrl(trimmed)
            }
        }
    }

    fun navigateToInput(rawInput: String, pushToBackStack: Boolean = true) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return

        val finalUrl = resolveInputToUrl(trimmed)
        val currentTab = _uiState.value.activeTab
        val newBackStack = if (pushToBackStack && currentTab.url != finalUrl) {
            (currentTab.backStack + currentTab.url).takeLast(40)
        } else {
            currentTab.backStack
        }

        val isInternalCompose = finalUrl in listOf(
            BrowserTab.URL_HOME,
            BrowserTab.URL_BOOKMARKS,
            BrowserTab.URL_HISTORY,
            BrowserTab.URL_SETTINGS
        )

        val newTitle = if (finalUrl.startsWith("devbrowser:")) {
            titleForInternalUrl(finalUrl)
        } else {
            finalUrl
        }

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(
                        url = finalUrl,
                        title = newTitle,
                        isLoading = !isInternalCompose,
                        progress = if (isInternalCompose) 100 else 15,
                        backStack = newBackStack,
                        forwardStack = if (pushToBackStack) emptyList() else tab.forwardStack
                    )
                } else tab
            }
            state.copy(
                tabs = updatedTabs,
                isDevToolsOpen = if (finalUrl == BrowserTab.URL_TEST) true else state.isDevToolsOpen
            )
        }

        recordHistoryEntry(newTitle, finalUrl)

        if (!isInternalCompose) {
            viewModelScope.launch(Dispatchers.Main) {
                val wv = webViewRef ?: return@launch
                if (finalUrl == BrowserTab.URL_TEST) {
                    wv.loadDataWithBaseURL(
                        BrowserTab.INTERNAL_TEST_BASE_URL,
                        DevToolsScripts.PLAYGROUND_HTML,
                        "text/html",
                        "UTF-8",
                        null
                    )
                } else {
                    wv.loadUrl(finalUrl)
                }
            }
        }
    }

    fun reloadActivePage() {
        val currentUrl = _uiState.value.activeTab.url
        if (currentUrl in listOf(
                BrowserTab.URL_HOME,
                BrowserTab.URL_BOOKMARKS,
                BrowserTab.URL_HISTORY,
                BrowserTab.URL_SETTINGS
            )
        ) {
            return
        }
        viewModelScope.launch(Dispatchers.Main) {
            val wv = webViewRef ?: return@launch
            if (currentUrl == BrowserTab.URL_TEST) {
                wv.loadDataWithBaseURL(
                    BrowserTab.INTERNAL_TEST_BASE_URL,
                    DevToolsScripts.PLAYGROUND_HTML,
                    "text/html",
                    "UTF-8",
                    null
                )
            } else {
                wv.reload()
            }
        }
    }

    fun goBack() {
        val tab = _uiState.value.activeTab
        if (tab.backStack.isNotEmpty()) {
            val prevUrl = tab.backStack.last()
            val remainingBack = tab.backStack.dropLast(1)
            val updatedForward = listOf(tab.url) + tab.forwardStack
            _uiState.update { state ->
                val updatedTabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) {
                        t.copy(backStack = remainingBack, forwardStack = updatedForward)
                    } else t
                }
                state.copy(tabs = updatedTabs)
            }
            navigateToInput(prevUrl, pushToBackStack = false)
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                val wv = webViewRef ?: return@launch
                if (wv.canGoBack()) wv.goBack()
            }
        }
    }

    fun goForward() {
        val tab = _uiState.value.activeTab
        if (tab.forwardStack.isNotEmpty()) {
            val nextUrl = tab.forwardStack.first()
            val remainingForward = tab.forwardStack.drop(1)
            val updatedBack = tab.backStack + tab.url
            _uiState.update { state ->
                val updatedTabs = state.tabs.map { t ->
                    if (t.id == state.activeTabId) {
                        t.copy(backStack = updatedBack, forwardStack = remainingForward)
                    } else t
                }
                state.copy(tabs = updatedTabs)
            }
            navigateToInput(nextUrl, pushToBackStack = false)
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                val wv = webViewRef ?: return@launch
                if (wv.canGoForward()) wv.goForward()
            }
        }
    }

    // Theme & Browser Settings
    fun setDarkTheme(isDark: Boolean) {
        _uiState.update { it.copy(isDarkTheme = isDark) }
        syncWebViewThemePreference()
    }

    fun setNeonMode(isNeon: Boolean) {
        _uiState.update { it.copy(isNeonMode = isNeon) }
        syncWebViewThemePreference()
    }

    fun setThemeAccentColor(color: ThemeAccentColor) {
        _uiState.update { it.copy(accentColor = color) }
        syncWebViewThemePreference()
    }

    fun setSearchEngine(engine: SearchEngineItem) {
        _uiState.update { it.copy(searchEngine = engine) }
    }

    fun addCustomSearchEngine(name: String, rawQueryUrl: String) {
        val cleanName = name.trim()
        val rawTemplate = rawQueryUrl.trim()
        if (cleanName.isEmpty() || rawTemplate.isEmpty()) return
        val normalizedUrl = when {
            rawTemplate.startsWith("https://") || rawTemplate.startsWith("http://") -> rawTemplate
            else -> "https://$rawTemplate"
        }
        val finalTemplate = if (normalizedUrl.contains("%s")) {
            normalizedUrl
        } else if (normalizedUrl.contains("?")) {
            "$normalizedUrl%s"
        } else {
            "${normalizedUrl.trimEnd('/')}/search?q=%s"
        }
        val customEngine = SearchEngineItem(
            id = "custom_${System.currentTimeMillis()}",
            displayName = cleanName,
            queryTemplate = finalTemplate,
            isCustom = true
        )
        _uiState.update { state ->
            state.copy(
                searchEngines = state.searchEngines + customEngine,
                searchEngine = customEngine
            )
        }
    }

    fun removeCustomSearchEngine(engineId: String) {
        _uiState.update { state ->
            val updated = state.searchEngines.filterNot { it.id == engineId && it.isCustom }
            val active = if (state.searchEngine.id == engineId) {
                updated.firstOrNull() ?: SearchEngineItem.DUCKDUCKGO
            } else {
                state.searchEngine
            }
            state.copy(searchEngines = updated, searchEngine = active)
        }
    }

    fun setJavaScriptEnabled(enabled: Boolean) {
        _uiState.update { it.copy(javaScriptEnabled = enabled) }
        viewModelScope.launch(Dispatchers.Main) {
            webViewRef?.settings?.javaScriptEnabled = enabled
            reloadActivePage()
        }
    }

    // Find in Page
    fun toggleFindInPage(show: Boolean) {
        _uiState.update { it.copy(isFindInPageVisible = show, findInPageQuery = if (!show) "" else it.findInPageQuery) }
        if (!show) {
            viewModelScope.launch(Dispatchers.Main) {
                webViewRef?.clearMatches()
            }
        }
    }

    fun updateFindInPageQuery(query: String) {
        _uiState.update { it.copy(findInPageQuery = query) }
        viewModelScope.launch(Dispatchers.Main) {
            val wv = webViewRef ?: return@launch
            if (query.isBlank()) wv.clearMatches()
            else wv.findAllAsync(query)
        }
    }

    fun findNextInPage(forward: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            webViewRef?.findNext(forward)
        }
    }

    // Desktop Site & Viewport Controls
    fun toggleDesktopSite() {
        val currentTab = _uiState.value.activeTab
        val nextDesktop = !currentTab.isDesktopSite
        val nextPreset = if (nextDesktop) DeviceViewportPreset.DESKTOP_CHROME else DeviceViewportPreset.RESPONSIVE
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(isDesktopSite = nextDesktop, devicePreset = nextPreset)
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
        viewModelScope.launch(Dispatchers.Main) {
            webViewRef?.settings?.userAgentString = nextPreset.userAgent
            reloadActivePage()
        }
    }

    // Bookmarks & History Operations
    fun toggleBookmarkCurrentPage() {
        val active = _uiState.value.activeTab
        _uiState.update { state ->
            val exists = state.bookmarks.any { it.url.equals(active.url, ignoreCase = true) }
            val updated = if (exists) {
                state.bookmarks.filterNot { it.url.equals(active.url, ignoreCase = true) }
            } else {
                listOf(QuickBookmark(active.title.ifBlank { active.url }, active.url, "Saved")) + state.bookmarks
            }
            state.copy(bookmarks = updated)
        }
    }

    fun addCustomBookmark(title: String, rawUrl: String) {
        val finalUrl = resolveInputToUrl(rawUrl)
        _uiState.update { state ->
            val filtered = state.bookmarks.filterNot { it.url.equals(finalUrl, ignoreCase = true) }
            state.copy(bookmarks = listOf(QuickBookmark(title, finalUrl, "Saved")) + filtered)
        }
    }

    fun removeBookmark(url: String) {
        _uiState.update { state ->
            state.copy(bookmarks = state.bookmarks.filterNot { it.url == url })
        }
    }

    fun openBookmark(bookmark: QuickBookmark) {
        _uiState.update { state ->
            val currentClicks = state.bookmarkClickCounts[bookmark.url] ?: 0
            state.copy(
                bookmarkClickCounts = state.bookmarkClickCounts + (bookmark.url to (currentClicks + 1)),
                siteTitles = state.siteTitles + (bookmark.url to bookmark.title)
            )
        }
        navigateToInput(bookmark.url)
    }

    // Home Page Shortcuts Operations (Separate from Bookmarks)
    fun pinShortcut(shortcut: HomeShortcut) {
        _uiState.update { state ->
            val alreadyPinned = state.pinnedShortcuts.any { it.url.equals(shortcut.url, ignoreCase = true) }
            if (alreadyPinned) {
                state
            } else {
                val pinned = shortcut.copy(
                    id = "pinned_${System.currentTimeMillis()}",
                    isPinned = true,
                    source = ShortcutSource.PINNED
                )
                state.copy(
                    pinnedShortcuts = state.pinnedShortcuts + pinned,
                    dismissedAutoShortcutUrls = state.dismissedAutoShortcutUrls - shortcut.url
                )
            }
        }
    }

    fun addCustomShortcut(title: String, rawUrl: String) {
        val cleanTitle = title.trim()
        val cleanUrl = rawUrl.trim()
        if (cleanTitle.isEmpty() || cleanUrl.isEmpty()) return
        val finalUrl = resolveInputToUrl(cleanUrl)
        _uiState.update { state ->
            val filtered = state.pinnedShortcuts.filterNot { it.url.equals(finalUrl, ignoreCase = true) }
            val newShortcut = HomeShortcut(
                id = "pinned_${System.currentTimeMillis()}",
                title = cleanTitle,
                url = finalUrl,
                isPinned = true,
                source = ShortcutSource.PINNED,
                count = 1
            )
            state.copy(
                pinnedShortcuts = filtered + newShortcut,
                dismissedAutoShortcutUrls = state.dismissedAutoShortcutUrls - finalUrl,
                siteTitles = state.siteTitles + (finalUrl to cleanTitle)
            )
        }
    }

    fun unpinShortcut(shortcut: HomeShortcut) {
        _uiState.update { state ->
            state.copy(
                pinnedShortcuts = state.pinnedShortcuts.filterNot { it.url.equals(shortcut.url, ignoreCase = true) }
            )
        }
    }

    fun removeShortcut(shortcut: HomeShortcut) {
        _uiState.update { state ->
            state.copy(
                pinnedShortcuts = state.pinnedShortcuts.filterNot { it.url.equals(shortcut.url, ignoreCase = true) },
                dismissedAutoShortcutUrls = state.dismissedAutoShortcutUrls + shortcut.url
            )
        }
    }

    fun deleteHistoryItem(id: Long) {
        _uiState.update { state ->
            state.copy(history = state.history.filterNot { it.id == id })
        }
    }

    fun clearBrowsingHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun clearAllBrowserSiteData() {
        clearStorageByType(StorageType.LOCAL_STORAGE)
        clearStorageByType(StorageType.SESSION_STORAGE)
        clearStorageByType(StorageType.COOKIES)
        viewModelScope.launch(Dispatchers.Main) {
            webViewRef?.clearCache(true)
        }
    }

    // Multi-Tab Actions
    fun toggleTabSwitcher(show: Boolean) {
        _uiState.update { it.copy(showTabSwitcher = show) }
    }

    fun createNewTab(initialUrl: String = BrowserTab.URL_HOME) {
        val newId = "tab_${System.currentTimeMillis()}"
        val newTab = BrowserTab(
            id = newId,
            title = titleForInternalUrl(initialUrl),
            url = initialUrl
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newId,
                showTabSwitcher = false
            )
        }
        navigateToInput(initialUrl, pushToBackStack = false)
    }

    fun switchTab(tabId: String) {
        val target = _uiState.value.tabs.find { it.id == tabId } ?: return
        _uiState.update { it.copy(activeTabId = tabId, showTabSwitcher = false) }
        navigateToInput(target.url, pushToBackStack = false)
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            navigateToInput(BrowserTab.URL_HOME, pushToBackStack = false)
            _uiState.update { it.copy(showTabSwitcher = false) }
            return
        }
        val remaining = currentTabs.filterNot { it.id == tabId }
        val nextActiveId = if (_uiState.value.activeTabId == tabId) {
            remaining.last().id
        } else {
            _uiState.value.activeTabId
        }
        _uiState.update { it.copy(tabs = remaining, activeTabId = nextActiveId) }
        val activeUrl = remaining.find { it.id == nextActiveId }?.url ?: BrowserTab.URL_HOME
        navigateToInput(activeUrl, pushToBackStack = false)
    }

    // DevTools Drawer Controls
    fun toggleDevTools() {
        _uiState.update { it.copy(isDevToolsOpen = !it.isDevToolsOpen) }
        if (_uiState.value.isDevToolsOpen) {
            refreshAllDevToolsData()
        }
    }

    fun selectDevToolsTab(tab: DevToolsPanelTab) {
        _uiState.update { it.copy(activeDevToolsTab = tab, isDevToolsOpen = true) }
        when (tab) {
            DevToolsPanelTab.ELEMENTS -> runJs("if(window.__devScopeRefreshDom) window.__devScopeRefreshDom('${_uiState.value.domSearchQuery}');")
            DevToolsPanelTab.APPLICATION -> runJs("if(window.__devScopeRefreshStorage) window.__devScopeRefreshStorage();")
            DevToolsPanelTab.PERFORMANCE -> runJs("if(window.__devScopeCollectPerformance) window.__devScopeCollectPerformance();")
            DevToolsPanelTab.SOURCES -> runJs("if(window.__devScopeCaptureSources) window.__devScopeCaptureSources();")
            else -> {}
        }
    }

    fun adjustDockHeightByDelta(deltaFraction: Float) {
        _uiState.update { state ->
            val next = (state.dockHeightFraction + deltaFraction).coerceIn(0.25f, 0.80f)
            state.copy(dockHeightFraction = next)
        }
    }

    fun snapDockHeightPreset() {
        _uiState.update { state ->
            val next = when {
                state.dockHeightFraction < 0.40f -> 0.50f
                state.dockHeightFraction < 0.62f -> 0.74f
                else -> 0.34f
            }
            state.copy(dockHeightFraction = next)
        }
    }

    fun toggleElementPicker() {
        val currentUrl = _uiState.value.activeTab.url
        if (currentUrl in listOf(
                BrowserTab.URL_HOME,
                BrowserTab.URL_BOOKMARKS,
                BrowserTab.URL_HISTORY,
                BrowserTab.URL_SETTINGS
            )
        ) {
            // Switch to devbrowser:test so the user has an HTML DOM page to pick elements on
            navigateToInput(BrowserTab.URL_TEST)
        }
        val next = !_uiState.value.isElementPickerActive
        _uiState.update {
            it.copy(
                isElementPickerActive = next,
                isDevToolsOpen = true,
                activeDevToolsTab = if (next) DevToolsPanelTab.ELEMENTS else it.activeDevToolsTab
            )
        }
        runJs("if(window.__devScopeSetInspectMode) window.__devScopeSetInspectMode($next);")
    }

    fun toggleDeviceToolbar() {
        _uiState.update { it.copy(isDeviceToolbarOpen = !it.isDeviceToolbarOpen) }
    }

    fun selectViewportPreset(preset: DeviceViewportPreset) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(
                        devicePreset = preset,
                        isDesktopSite = preset == DeviceViewportPreset.DESKTOP_CHROME
                    )
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
        viewModelScope.launch(Dispatchers.Main) {
            webViewRef?.settings?.userAgentString = preset.userAgent
            reloadActivePage()
        }
    }

    fun toggleViewportOrientation() {
        _uiState.update { it.copy(isViewportLandscape = !it.isViewportLandscape) }
    }

    fun refreshAllDevToolsData() {
        runJs("if(window.__devScopeRefreshAll) window.__devScopeRefreshAll();")
    }

    // Callbacks from DevScopeJsBridge
    fun onBridgeConsoleLog(level: ConsoleLogLevel, message: String, source: String) {
        _uiState.update { state ->
            val last = state.consoleLogs.lastOrNull()
            val updatedLogs = if (last != null && last.level == level && last.message == message) {
                state.consoleLogs.dropLast(1) + last.copy(
                    count = last.count + 1,
                    timestamp = currentTimeFormatted()
                )
            } else {
                (state.consoleLogs + ConsoleLogEntry(
                    id = logIdGenerator.getAndIncrement(),
                    level = level,
                    message = message,
                    source = source,
                    timestamp = currentTimeFormatted()
                )).takeLast(250)
            }
            state.copy(consoleLogs = updatedLogs)
        }
    }

    fun onBridgeDomTreeUpdated(nodes: List<DomNodeItem>) {
        _uiState.update { state ->
            val currentSelectedPath = state.selectedDomNode?.path
            val refreshedSelection = if (currentSelectedPath != null) {
                nodes.find { it.path == currentSelectedPath } ?: state.selectedDomNode
            } else {
                nodes.find { it.idAttr == "customizable-box" }
                    ?: nodes.find { it.tag == "body" }
                    ?: nodes.firstOrNull()
            }
            state.copy(
                domNodes = nodes,
                selectedDomNode = refreshedSelection
            )
        }
    }

    fun onBridgeNodeSelected(node: DomNodeItem, pickedFromPage: Boolean) {
        _uiState.update { state ->
            state.copy(
                selectedDomNode = node,
                isDevToolsOpen = if (pickedFromPage) true else state.isDevToolsOpen,
                activeDevToolsTab = if (pickedFromPage) DevToolsPanelTab.ELEMENTS else state.activeDevToolsTab,
                isElementPickerActive = if (pickedFromPage) false else state.isElementPickerActive
            )
        }
        if (pickedFromPage) {
            runJs("if(window.__devScopeSetInspectMode) window.__devScopeSetInspectMode(false);")
        }
    }

    fun onBridgeNetworkRequest(entry: NetworkRequestEntry) {
        _uiState.update { state ->
            val updated = (state.networkRequests + entry).takeLast(160)
            val selected = state.selectedNetworkRequest ?: entry
            state.copy(
                networkRequests = updated,
                selectedNetworkRequest = selected
            )
        }
    }

    fun onWebViewResourceLoaded(rawUrl: String) {
        val url = mapRawUrlToDisplayUrl(rawUrl)
        val lower = url.lowercase(Locale.US)
        if (lower.startsWith("javascript:") || lower.startsWith("data:")) return
        val resType = when {
            lower.endsWith(".js") || lower.contains(".js?") -> NetworkResourceType.JS
            lower.endsWith(".css") || lower.contains(".css?") || lower.contains("fonts.googleapis") -> NetworkResourceType.CSS
            lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".webp") || lower.endsWith(".svg") || lower.endsWith(".gif") ||
                lower.endsWith(".ico") -> NetworkResourceType.IMG
            lower == _uiState.value.activeTab.url.lowercase(Locale.US) ||
                lower.endsWith(".html") || lower == BrowserTab.URL_TEST -> NetworkResourceType.DOC
            else -> NetworkResourceType.OTHER
        }
        val mime = when (resType) {
            NetworkResourceType.JS -> "application/javascript"
            NetworkResourceType.CSS -> "text/css"
            NetworkResourceType.IMG -> "image/webp"
            NetworkResourceType.DOC -> "text/html"
            else -> "application/octet-stream"
        }
        val offset = (System.currentTimeMillis() - pageLoadTimestampMs.get()).coerceAtLeast(4L)
        val dur = (18L..95L).random()
        val entry = NetworkRequestEntry(
            id = "res_${System.currentTimeMillis()}_${(100..999).random()}",
            method = "GET",
            url = url,
            status = 200,
            statusText = "OK",
            resourceType = resType,
            mimeType = mime,
            startTimeOffsetMs = offset,
            durationMs = dur,
            sizeBytes = (1200..28400).random(),
            requestHeaders = mapOf(
                "Accept" to "*/*",
                "User-Agent" to _uiState.value.activeTab.devicePreset.userAgent
            ),
            responseHeaders = mapOf(
                "Content-Type" to mime,
                "Cache-Control" to "max-age=3600"
            ),
            responsePreview = "/* Resource loaded via WebView pipeline: $url */"
        )
        onBridgeNetworkRequest(entry)
    }

    fun onBridgeStorageUpdated(entries: List<StorageEntry>) {
        viewModelScope.launch(Dispatchers.Main) {
            val cookieUrl = _uiState.value.activeTab.url
            val nativeCookies = mutableListOf<StorageEntry>()
            try {
                val rawCookie = CookieManager.getInstance().getCookie(cookieUrl)
                if (!rawCookie.isNullOrBlank()) {
                    rawCookie.split(";").forEach { part ->
                        val eq = part.indexOf('=')
                        if (eq > 0) {
                            val k = part.substring(0, eq).trim()
                            val v = part.substring(eq + 1).trim()
                            if (k.isNotEmpty() && entries.none { it.type == StorageType.COOKIES && it.key == k }) {
                                nativeCookies.add(StorageEntry(k, v, StorageType.COOKIES))
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
            _uiState.update { it.copy(storageEntries = entries + nativeCookies) }
        }
    }

    fun onBridgePerformanceUpdated(metrics: PerformanceMetrics) {
        _uiState.update { it.copy(performanceMetrics = metrics) }
    }

    fun onBridgeSourcesUpdated(html: String) {
        _uiState.update { it.copy(pageSourceHtml = html) }
    }

    // Elements Panel Interactive Operations
    fun selectDomNodeFromTree(node: DomNodeItem) {
        _uiState.update { it.copy(selectedDomNode = node) }
        runJs("if(window.__devScopeHighlightNode) window.__devScopeHighlightNode('${node.path}', true);")
    }

    fun updateDomSearchQuery(query: String) {
        _uiState.update { it.copy(domSearchQuery = query) }
        val escaped = JSONObject.quote(query)
        runJs("if(window.__devScopeRefreshDom) window.__devScopeRefreshDom($escaped);")
    }

    fun applyInlineStyleToSelected(nodePath: String, newStyleText: String) {
        _uiState.update { state ->
            val updatedNodes = state.domNodes.map {
                if (it.path == nodePath) it.copy(inlineStyle = newStyleText) else it
            }
            val updatedSelected = if (state.selectedDomNode?.path == nodePath) {
                state.selectedDomNode.copy(inlineStyle = newStyleText)
            } else state.selectedDomNode
            state.copy(domNodes = updatedNodes, selectedDomNode = updatedSelected)
        }
        val escapedPath = JSONObject.quote(nodePath)
        val escapedStyle = JSONObject.quote(newStyleText)
        runJs("if(window.__devScopeApplyInlineStyle) window.__devScopeApplyInlineStyle($escapedPath, $escapedStyle);")
    }

    fun hideDomNode(nodePath: String) {
        _uiState.update { state ->
            fun appendHideStyle(existing: String): String {
                val base = existing.trim().trimEnd(';')
                return if (base.isEmpty()) {
                    "display: none !important;"
                } else {
                    "$base; display: none !important;"
                }
            }
            val updatedNodes = state.domNodes.map {
                if (it.path == nodePath) {
                    it.copy(
                        inlineStyle = appendHideStyle(it.inlineStyle),
                        computedStyles = it.computedStyles + ("display" to "none")
                    )
                } else it
            }
            val updatedSelected = if (state.selectedDomNode?.path == nodePath) {
                state.selectedDomNode.copy(
                    inlineStyle = appendHideStyle(state.selectedDomNode.inlineStyle),
                    computedStyles = state.selectedDomNode.computedStyles + ("display" to "none")
                )
            } else state.selectedDomNode
            state.copy(domNodes = updatedNodes, selectedDomNode = updatedSelected)
        }
        val escapedPath = JSONObject.quote(nodePath)
        runJs("if(window.__devScopeHideElement) window.__devScopeHideElement($escapedPath);")
    }

    fun editDomNodeText(nodePath: String, newText: String) {
        _uiState.update { state ->
            val updatedNodes = state.domNodes.map {
                if (it.path == nodePath) it.copy(textPreview = newText) else it
            }
            val updatedSelected = if (state.selectedDomNode?.path == nodePath) {
                state.selectedDomNode.copy(textPreview = newText)
            } else state.selectedDomNode
            state.copy(domNodes = updatedNodes, selectedDomNode = updatedSelected)
        }
        val escapedPath = JSONObject.quote(nodePath)
        val escapedText = JSONObject.quote(newText)
        runJs("if(window.__devScopeEditNodeText) window.__devScopeEditNodeText($escapedPath, $escapedText);")
    }

    fun setDomNodeAttribute(nodePath: String, attrName: String, attrValue: String) {
        _uiState.update { state ->
            val updatedNodes = state.domNodes.map {
                if (it.path == nodePath) {
                    val nextAttrs = if (attrValue.isEmpty()) it.attributes - attrName else it.attributes + (attrName to attrValue)
                    it.copy(attributes = nextAttrs)
                } else it
            }
            val updatedSelected = if (state.selectedDomNode?.path == nodePath) {
                val nextAttrs = if (attrValue.isEmpty()) state.selectedDomNode.attributes - attrName else state.selectedDomNode.attributes + (attrName to attrValue)
                state.selectedDomNode.copy(attributes = nextAttrs)
            } else state.selectedDomNode
            state.copy(domNodes = updatedNodes, selectedDomNode = updatedSelected)
        }
        val escapedPath = JSONObject.quote(nodePath)
        val escapedName = JSONObject.quote(attrName)
        val escapedVal = JSONObject.quote(attrValue)
        runJs("if(window.__devScopeSetAttribute) window.__devScopeSetAttribute($escapedPath, $escapedName, $escapedVal);")
    }

    fun removeDomNode(nodePath: String) {
        val escapedPath = JSONObject.quote(nodePath)
        runJs("if(window.__devScopeRemoveNode) window.__devScopeRemoveNode($escapedPath);")
    }

    fun appendChildElementToNode(nodePath: String, tag: String, textContent: String) {
        val escapedPath = JSONObject.quote(nodePath)
        val escapedTag = JSONObject.quote(tag.trim().ifEmpty { "div" })
        val escapedText = JSONObject.quote(textContent)
        val script = """
            (function() {
              var parent = document.querySelector('[data-devscope-id=' + $escapedPath + ']');
              if (!parent) return;
              var child = document.createElement($escapedTag);
              child.innerText = $escapedText;
              parent.appendChild(child);
              if (window.__devScopeRefreshDom) window.__devScopeRefreshDom('');
            })();
        """.trimIndent()
        runJs(script)
    }

    // Console Panel Operations
    fun setConsoleLevelFilter(level: ConsoleLogLevel?) {
        _uiState.update { it.copy(consoleLevelFilter = level) }
    }

    fun setConsoleSearchQuery(query: String) {
        _uiState.update { it.copy(consoleSearchQuery = query) }
    }

    fun clearConsoleLogs() {
        _uiState.update { it.copy(consoleLogs = emptyList()) }
    }

    fun evaluateJavaScriptInConsole(rawCode: String) {
        val code = rawCode.trim()
        if (code.isEmpty()) return

        _uiState.update { state ->
            val updatedHistory = (listOf(code) + state.commandHistory.filterNot { it == code }).take(30)
            state.copy(
                commandHistory = updatedHistory,
                consoleLogs = state.consoleLogs + ConsoleLogEntry(
                    id = logIdGenerator.getAndIncrement(),
                    level = ConsoleLogLevel.COMMAND,
                    message = code,
                    source = "REPL",
                    timestamp = currentTimeFormatted()
                )
            )
        }

        val wrappedCode = """
            (function() {
              try {
                var __res = eval(${JSONObject.quote(code)});
                window['${'$'}_'] = __res;
                if (__res === undefined) return 'undefined';
                if (__res === null) return 'null';
                if (__res instanceof Element) return '<' + __res.tagName.toLowerCase() + (__res.id ? '#' + __res.id : '') + '>';
                if (typeof __res === 'object') {
                  try { return JSON.stringify(__res, null, 2); } catch(e) { return String(__res); }
                }
                return String(__res);
              } catch (err) {
                return 'ERR::' + (err.stack || err.message || String(err));
              }
            })();
        """.trimIndent()

        runJs(wrappedCode) { rawResult ->
            val unquoted = try {
                if (rawResult.startsWith("\"") && rawResult.endsWith("\"")) {
                    JSONObject("{\"v\":$rawResult}").getString("v")
                } else rawResult
            } catch (_: Exception) {
                rawResult
            }
            val isErr = unquoted.startsWith("ERR::")
            val cleanMsg = if (isErr) unquoted.removePrefix("ERR::") else unquoted
            _uiState.update { state ->
                state.copy(
                    consoleLogs = state.consoleLogs + ConsoleLogEntry(
                        id = logIdGenerator.getAndIncrement(),
                        level = if (isErr) ConsoleLogLevel.ERROR else ConsoleLogLevel.RESULT,
                        message = cleanMsg,
                        source = "REPL",
                        timestamp = currentTimeFormatted()
                    )
                )
            }
            refreshAllDevToolsData()
        }
    }

    // Network Panel Operations
    fun setNetworkTypeFilter(filter: NetworkResourceType) {
        _uiState.update { it.copy(networkTypeFilter = filter) }
    }

    fun selectNetworkRequest(entry: NetworkRequestEntry?) {
        _uiState.update { it.copy(selectedNetworkRequest = entry) }
    }

    fun togglePreserveNetworkLog() {
        _uiState.update { it.copy(preserveNetworkLog = !it.preserveNetworkLog) }
    }

    fun toggleOfflineThrottling() {
        val next = !_uiState.value.isOfflineThrottled
        _uiState.update { it.copy(isOfflineThrottled = next) }
        viewModelScope.launch(Dispatchers.Main) {
            webViewRef?.settings?.blockNetworkLoads = next
        }
    }

    fun clearNetworkRequests() {
        _uiState.update { it.copy(networkRequests = emptyList(), selectedNetworkRequest = null) }
    }

    fun triggerSyntheticApiRequest(method: String, path: String, status: Int) {
        runJs("if(window.__devScopeRecordSyntheticFetch) window.__devScopeRecordSyntheticFetch('$method', 'devbrowser:test$path', $status);")
    }

    fun replayNetworkRequest(entry: NetworkRequestEntry) {
        if (entry.url.startsWith("devbrowser:test") || entry.url.contains("devbrowser.internal")) {
            val path = "/" + entry.url.substringAfter("devbrowser:test/", "api/v1/replay")
            triggerSyntheticApiRequest(entry.method, path, entry.status.takeIf { it > 0 } ?: 200)
        } else {
            val escapedUrl = JSONObject.quote(entry.url)
            val escapedMethod = JSONObject.quote(entry.method)
            runJs("fetch($escapedUrl, { method: $escapedMethod }).catch(function(){});")
        }
    }

    fun buildCurlCommand(entry: NetworkRequestEntry): String {
        return buildString {
            append("curl -X ${entry.method} '${entry.url}'")
            entry.requestHeaders.forEach { (k, v) ->
                append(" \\\n  -H '$k: $v'")
            }
            if (entry.requestBody.isNotBlank()) {
                val escapedBody = entry.requestBody.replace("'", "'\\''")
                append(" \\\n  --data-raw '$escapedBody'")
            }
        }
    }

    // Storage Panel Operations
    fun selectStorageType(type: StorageType) {
        _uiState.update { it.copy(selectedStorageType = type) }
        runJs("if(window.__devScopeRefreshStorage) window.__devScopeRefreshStorage();")
    }

    fun setStorageKeyValue(type: StorageType, key: String, value: String) {
        if (key.isBlank()) return
        val t = JSONObject.quote(type.name)
        val k = JSONObject.quote(key.trim())
        val v = JSONObject.quote(value)
        runJs("if(window.__devScopeSetStorageItem) window.__devScopeSetStorageItem($t, $k, $v);")
    }

    fun deleteStorageKey(type: StorageType, key: String) {
        val t = JSONObject.quote(type.name)
        val k = JSONObject.quote(key)
        runJs("if(window.__devScopeDeleteStorageItem) window.__devScopeDeleteStorageItem($t, $k);")
    }

    fun clearStorageByType(type: StorageType) {
        when (type) {
            StorageType.LOCAL_STORAGE -> runJs("localStorage.clear(); if(window.__devScopeRefreshStorage) window.__devScopeRefreshStorage();")
            StorageType.SESSION_STORAGE -> runJs("sessionStorage.clear(); if(window.__devScopeRefreshStorage) window.__devScopeRefreshStorage();")
            StorageType.COOKIES -> {
                CookieManager.getInstance().removeAllCookies(null)
                runJs("document.cookie.split(';').forEach(function(c) { document.cookie = c.replace(/^ +/, '').replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/'); }); if(window.__devScopeRefreshStorage) window.__devScopeRefreshStorage();")
            }
        }
    }

    // Performance / Sources Operations
    fun runLighthouseAudit() {
        runJs("if(window.__devScopeCollectPerformance) window.__devScopeCollectPerformance();")
    }

    fun updateSourceSearchQuery(query: String) {
        _uiState.update { it.copy(sourceSearchQuery = query) }
    }
}
