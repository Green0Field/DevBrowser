package com.example.viewmodel

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BrowserPreferencesStore
import com.example.devtools.DevToolsScripts
import com.example.model.BrowserTab
import com.example.model.ClearDataTimeRange
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
import com.example.data.VirusTotalApiClient
import com.example.model.VirusTotalRescanInterval
import com.example.model.VirusTotalScanResult
import com.example.model.VirusTotalThreatLevel
import com.example.model.VirusTotalWarningState
import com.example.model.UserScript
import com.example.model.UserScriptUpdateInterval
import com.example.model.AiProtectionScanResult
import com.example.model.AiThreatCategory
import com.example.model.AiChatDialogState
import com.example.model.ChatMessage
import com.example.data.GitHubUpdateApiClient
import com.example.model.AutoUpdateCheckFrequency
import com.example.model.AppUpdateInfo
import com.example.model.UpdateCheckStatus
import com.example.model.GeminiModelChoice
import com.example.model.WebpageTranslationState
import com.example.model.WebPageError
import com.example.model.WebPageErrorType
import com.example.ui.pages.error.resolveWebPageError
import com.example.data.GeminiApiClient
import com.example.BuildConfig
import com.example.ui.pages.debug.parseDebugQueryParams
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
    val domNodes: List<DomNodeItem> = DevToolsScripts.DEFAULT_DOM_NODES,
    val selectedDomNode: DomNodeItem? = DevToolsScripts.DEFAULT_DOM_NODES.find { it.idAttr == "customizable-box" },
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
    val storageEntries: List<StorageEntry> = DevToolsScripts.DEFAULT_STORAGE_ENTRIES,
    val selectedStorageType: StorageType = StorageType.LOCAL_STORAGE,
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics(),
    val pageSourceHtml: String = DevToolsScripts.PLAYGROUND_HTML,
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
    val snippets: List<JsSnippet> = DevToolsScripts.DEFAULT_SNIPPETS,
    val isVirusTotalEnabled: Boolean = false,
    val virusTotalApiKey: String = "",
    val virusTotalRescanInterval: VirusTotalRescanInterval = VirusTotalRescanInterval.HOURS_24,
    val virusTotalScanCache: Map<String, VirusTotalScanResult> = emptyMap(),
    val isScanningVirusTotal: Boolean = false,
    val activeWarningState: VirusTotalWarningState? = null,
    val bypassedWarningUrls: Set<String> = emptySet(),
    val isSecurityPopupVisible: Boolean = false,
    val userScripts: List<UserScript> = listOf(UserScript.createDefaultTemplate()),
    val userScriptUpdateInterval: UserScriptUpdateInterval = UserScriptUpdateInterval.HOURS_24,
    val isAiMasterEnabled: Boolean = false,
    val geminiApiKey: String = "",
    val isAiProtectionEnabled: Boolean = false,
    val isAiSummaryEnabled: Boolean = true,
    val isAiTranslateEnabled: Boolean = true,
    val selectedGeminiModel: GeminiModelChoice = GeminiModelChoice.AUTO,
    val aiScanCache: Map<String, AiProtectionScanResult> = emptyMap(),
    val isScanningAiProtection: Boolean = false,
    val activeAiChatState: AiChatDialogState? = null,
    val activeTranslationState: WebpageTranslationState? = null,
    val isAutoUpdateEnabled: Boolean = true,
    val githubApiKey: String = "",
    val autoUpdateFrequency: AutoUpdateCheckFrequency = AutoUpdateCheckFrequency.EACH_STARTUP,
    val autoUpdateStartupCount: Int = 0,
    val lastUpdateCheckMillis: Long = 0L,
    val updateCheckStatus: UpdateCheckStatus = UpdateCheckStatus.Idle,
    val pendingUpdateNotice: AppUpdateInfo? = null,
    val isIncludePrereleases: Boolean = false
) {
    val activeTab: BrowserTab
        get() = tabs.find { it.id == activeTabId } ?: tabs.first()

    val effectiveGeminiApiKey: String
        get() = if (geminiApiKey.isNotBlank()) {
            geminiApiKey.trim()
        } else {
            runCatching {
                BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            }.getOrNull() ?: ""
        }

    val isAiProtectionActive: Boolean
        get() = isAiMasterEnabled && isAiProtectionEnabled && !isVirusTotalEnabled && effectiveGeminiApiKey.isNotBlank()

    val currentScanResult: VirusTotalScanResult?
        get() {
            val url = activeTab.url
            if (url.startsWith("devbrowser:", ignoreCase = true)) return null
            val domain = VirusTotalApiClient.extractDomain(url)
            return virusTotalScanCache[url] ?: virusTotalScanCache[domain]
        }

    val currentAiScanResult: AiProtectionScanResult?
        get() {
            val url = activeTab.url
            if (url.startsWith("devbrowser:", ignoreCase = true)) return null
            val domain = VirusTotalApiClient.extractDomain(url)
            return aiScanCache[url] ?: aiScanCache[domain]
        }

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
                    lower !in pinnedUrls && !lower.startsWith("devbrowser:") && lower !in dismissedLower
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

class BrowserDevToolsViewModel(
    private var preferencesStore: BrowserPreferencesStore? = null
) : ViewModel() {

    constructor(context: Context) : this(BrowserPreferencesStore(context.applicationContext))

    private val _uiState = MutableStateFlow(
        preferencesStore?.loadInitialState(BrowserUiState()) ?: BrowserUiState()
    )
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val logIdGenerator = AtomicLong(1L)
    private val historyIdGenerator = AtomicLong(
        (_uiState.value.history.maxOfOrNull { it.id } ?: 1L) + 1L
    )
    private val pageLoadTimestampMs = AtomicLong(System.currentTimeMillis())
    private var webViewRef: WebView? = null

    fun attachPreferencesIfNeeded(context: Context) {
        if (preferencesStore == null) {
            val store = BrowserPreferencesStore(context.applicationContext)
            preferencesStore = store
            _uiState.update { current -> store.loadInitialState(current) }
            val maxHistId = _uiState.value.history.maxOfOrNull { it.id } ?: 1L
            if (historyIdGenerator.get() <= maxHistId) {
                historyIdGenerator.set(maxHistId + 1L)
            }
            syncWebViewThemePreference()
            checkAutoUpdateOnStartupIfNeeded()
        }
    }

    private fun persistShortcutsAndHistory() {
        val state = _uiState.value
        preferencesStore?.saveShortcutsState(
            pinnedShortcuts = state.pinnedShortcuts,
            dismissedUrls = state.dismissedAutoShortcutUrls,
            visitCounts = state.siteVisitCounts,
            clickCounts = state.bookmarkClickCounts,
            siteTitles = state.siteTitles
        )
        preferencesStore?.saveHistory(state.history)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BrowserDevToolsViewModel(BrowserPreferencesStore(appContext)) as T
                }
            }
        }
    }

    fun attachWebView(webView: WebView?) {
        val isNewAttachment = webView != null && webViewRef !== webView
        webViewRef = webView
        if (webView != null) {
            syncWebViewThemePreference()
            if (isNewAttachment) {
                val activeUrl = _uiState.value.activeTab.url
                val isInternalCompose = isComposeInternalUrl(activeUrl)
                if (!isInternalCompose) {
                    if (activeUrl == BrowserTab.URL_TEST) {
                        webView.loadDataWithBaseURL(
                            BrowserTab.INTERNAL_TEST_BASE_URL,
                            DevToolsScripts.PLAYGROUND_HTML,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    } else {
                        webView.loadUrl(activeUrl)
                    }
                }
            }
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

    fun isComposeInternalUrl(url: String): Boolean {
        val lower = url.lowercase(Locale.US).trim()
        return lower == BrowserTab.URL_HOME ||
            lower == "about:home" ||
            lower == "home" ||
            lower == BrowserTab.URL_BOOKMARKS ||
            lower == "about:bookmarks" ||
            lower == "bookmarks" ||
            lower == BrowserTab.URL_HISTORY ||
            lower == "about:history" ||
            lower == "history" ||
            lower == BrowserTab.URL_SETTINGS ||
            lower == "about:settings" ||
            lower == "settings" ||
            lower.startsWith("devbrowser:settings") ||
            lower.startsWith("about:settings") ||
            lower == BrowserTab.URL_DEBUG ||
            lower == "about:debug" ||
            lower == "debug" ||
            lower == "devbrowser://debug" ||
            lower.startsWith("devbrowser:debug") ||
            lower.startsWith("devbrowser://debug") ||
            lower.startsWith("about:debug") ||
            lower.startsWith("debug/")
    }

    private fun titleForInternalUrl(url: String): String {
        val lower = url.lowercase(Locale.US).trim()
        return when {
            lower == BrowserTab.URL_HOME -> "Home"
            lower == BrowserTab.URL_BOOKMARKS -> "Bookmarks"
            lower == BrowserTab.URL_HISTORY -> "History"
            lower == BrowserTab.URL_SETTINGS -> "Settings"
            lower == BrowserTab.URL_SETTINGS_THEME -> "Settings - Theme"
            lower == BrowserTab.URL_SETTINGS_SEARCH -> "Settings - Search"
            lower == BrowserTab.URL_SETTINGS_DATA -> "Settings - Data"
            lower == BrowserTab.URL_SETTINGS_VIRUSTOTAL -> "Settings - VirusTotal"
            lower == BrowserTab.URL_SETTINGS_AI -> "Settings - Ai Features"
            lower == BrowserTab.URL_SETTINGS_AUTO_UPDATE -> "Settings - Auto update"
            lower == BrowserTab.URL_SETTINGS_ABOUT -> "Settings - About"
            lower.startsWith("devbrowser:settings/") -> "Settings - " + lower.removePrefix("devbrowser:settings/").replaceFirstChar { it.uppercase() }
            lower == BrowserTab.URL_DEBUG -> "DevBrowser Debug"
            lower == BrowserTab.URL_DEBUG_ERROR -> "Debug - Error Pages"
            lower == BrowserTab.URL_DEBUG_AUTO_UPDATE || lower.startsWith("devbrowser:debug/autoupdate") -> "Debug - Auto-Update Simulator"
            lower.startsWith("devbrowser:debug/error/") -> "Debug - " + lower.removePrefix("devbrowser:debug/error/").substringBefore("?").replaceFirstChar { it.uppercase() }
            lower.startsWith("devbrowser:debug/") -> "Debug - " + lower.removePrefix("devbrowser:debug/").substringBefore("?").replaceFirstChar { it.uppercase() }
            lower == BrowserTab.URL_TEST -> "DevBrowser Test Page"
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
                    timestamp = SimpleDateFormat("HH:mm", Locale.US).format(Date()),
                    createdAtMillis = System.currentTimeMillis()
                )
                (listOf(entry) + state.history).take(100)
            }
            state.copy(
                history = updatedHistory,
                siteVisitCounts = updatedVisits,
                siteTitles = updatedTitles
            )
        }
        persistShortcutsAndHistory()
    }

    fun onPageStarted(rawUrl: String) {
        val displayUrl = mapRawUrlToDisplayUrl(rawUrl)
        pageLoadTimestampMs.set(System.currentTimeMillis())
        syncWebViewThemePreference()
        _uiState.update { state ->
            val currentTab = state.activeTab
            // Only overwrite tab URL if it's not currently showing a Compose internal page (home/bookmarks/history/settings)
            val isComposeInternal = isComposeInternalUrl(currentTab.url)
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    if (isComposeInternal) {
                        tab
                    } else {
                        tab.copy(url = displayUrl, isLoading = true, progress = 15, errorState = null)
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
            val isComposeInternal = isComposeInternalUrl(currentTab.url)
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    if (isComposeInternal) {
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
            if (activeUrl != BrowserTab.URL_TEST) {
                scanUrlIfNeeded(activeUrl)
                injectUserScriptsIfNeeded(activeUrl)
                checkForUserscriptUpdates()
            }
        }

        syncWebViewThemePreference()
        runJs(DevToolsScripts.DEVTOOLS_AGENT_JS) {
            if (_uiState.value.isElementPickerActive) {
                runJs("if(window.__devScopeSetInspectMode) window.__devScopeSetInspectMode(true);")
            }
        }
    }

    fun onWebPageError(failingUrl: String, errorCode: Int, description: String, context: Context) {
        val currentTab = _uiState.value.activeTab
        if (isComposeInternalUrl(currentTab.url)) {
            return
        }
        val error = resolveWebPageError(context, failingUrl, errorCode, description)
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(
                        isLoading = false,
                        progress = 100,
                        errorState = error,
                        title = error.errorType.title
                    )
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
    }

    fun clearWebPageError() {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(errorState = null)
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
    }

    fun retryAfterError() {
        val tab = _uiState.value.activeTab
        val targetUrl = tab.errorState?.url ?: tab.url
        clearWebPageError()
        if (targetUrl.isNotBlank() && !targetUrl.startsWith("devbrowser:")) {
            viewModelScope.launch(Dispatchers.Main) {
                val wv = webViewRef ?: return@launch
                wv.loadUrl(targetUrl)
            }
        } else {
            reloadActivePage()
        }
    }

    fun searchWeb(query: String) {
        val engine = _uiState.value.searchEngine
        val searchUrl = engine.formatSearchUrl(query)
        navigateToInput(searchUrl)
    }

    private fun resolveInputToUrl(rawInput: String): String {
        val trimmed = rawInput.trim()
        val lower = trimmed.lowercase(Locale.US)
        return when {
            lower == "devbrowser:home" || lower == "about:home" -> BrowserTab.URL_HOME
            lower == "devbrowser:bookmarks" -> BrowserTab.URL_BOOKMARKS
            lower == "devbrowser:history" -> BrowserTab.URL_HISTORY
            lower == "devbrowser:settings" -> BrowserTab.URL_SETTINGS
            lower == "devbrowser:settings/theme" -> BrowserTab.URL_SETTINGS_THEME
            lower == "devbrowser:settings/search" -> BrowserTab.URL_SETTINGS_SEARCH
            lower == "devbrowser:settings/data" -> BrowserTab.URL_SETTINGS_DATA
            lower == "devbrowser:settings/virustotal" -> BrowserTab.URL_SETTINGS_VIRUSTOTAL
            lower == "devbrowser:settings/about" -> BrowserTab.URL_SETTINGS_ABOUT
            lower.startsWith("devbrowser:settings/") -> lower
            // Debug URL mappings - support devbrowser:debug, devbrowser://debug, about:debug, debug
            lower == "devbrowser:debug" || lower == "about:debug" || lower == "debug" || lower == "devbrowser://debug" -> BrowserTab.URL_DEBUG
            lower == "devbrowser:debug/error" || lower == "about:debug/error" || lower == "debug/error" || lower == "devbrowser://debug/error" -> BrowserTab.URL_DEBUG_ERROR
            lower == "devbrowser:debug/autoupdate" || lower == "about:debug/autoupdate" || lower == "debug/autoupdate" || lower == "devbrowser://debug/autoupdate" -> BrowserTab.URL_DEBUG_AUTO_UPDATE
            trimmed.startsWith("devbrowser:debug", ignoreCase = true) ||
            trimmed.startsWith("devbrowser://debug", ignoreCase = true) ||
            trimmed.startsWith("about:debug", ignoreCase = true) ||
            trimmed.startsWith("debug/", ignoreCase = true) -> {
                val suffix = when {
                    trimmed.startsWith("devbrowser://debug", ignoreCase = true) -> trimmed.substring("devbrowser://debug".length)
                    trimmed.startsWith("devbrowser:debug", ignoreCase = true) -> trimmed.substring("devbrowser:debug".length)
                    trimmed.startsWith("about:debug", ignoreCase = true) -> trimmed.substring("about:debug".length)
                    trimmed.startsWith("debug/", ignoreCase = true) -> "/" + trimmed.substring("debug/".length)
                    else -> ""
                }
                "devbrowser:debug$suffix"
            }
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

        val isInternalCompose = isComposeInternalUrl(finalUrl)

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
                        forwardStack = if (pushToBackStack) emptyList() else tab.forwardStack,
                        errorState = null
                    )
                } else tab
            }
            state.copy(
                tabs = updatedTabs,
                isDevToolsOpen = if (finalUrl == BrowserTab.URL_TEST) true else state.isDevToolsOpen,
                activeWarningState = null
            )
        }

        recordHistoryEntry(newTitle, finalUrl)

        if (finalUrl.startsWith("devbrowser:debug/autoupdate", ignoreCase = true)) {
            val (_, params) = parseDebugQueryParams(finalUrl)
            val ver = params["version"] ?: params["v"]
            val desc = params["description"] ?: params["desc"] ?: params["short_description"]
            val changelog = params["changelog"] ?: params["notes"] ?: params["release_notes"]
            triggerDebugAutoUpdate(ver, desc, changelog)
        }

        if (!isInternalCompose) {
            if (!finalUrl.startsWith("devbrowser:", ignoreCase = true)) {
                scanUrlIfNeeded(finalUrl)
            }
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
        if (isComposeInternalUrl(currentUrl)) {
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
        preferencesStore?.saveDarkTheme(isDark)
        syncWebViewThemePreference()
    }

    fun setNeonMode(isNeon: Boolean) {
        _uiState.update { it.copy(isNeonMode = isNeon) }
        preferencesStore?.saveNeonMode(isNeon)
        syncWebViewThemePreference()
    }

    fun setThemeAccentColor(color: ThemeAccentColor) {
        _uiState.update { it.copy(accentColor = color) }
        preferencesStore?.saveAccentColor(color)
        syncWebViewThemePreference()
    }

    fun setSearchEngine(engine: SearchEngineItem) {
        _uiState.update { it.copy(searchEngine = engine) }
        val state = _uiState.value
        preferencesStore?.saveSearchEngines(state.searchEngines, state.searchEngine)
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
        val state = _uiState.value
        preferencesStore?.saveSearchEngines(state.searchEngines, state.searchEngine)
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
        val state = _uiState.value
        preferencesStore?.saveSearchEngines(state.searchEngines, state.searchEngine)
    }

    fun setJavaScriptEnabled(enabled: Boolean) {
        _uiState.update { it.copy(javaScriptEnabled = enabled) }
        preferencesStore?.saveJavaScriptEnabled(enabled)
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
        preferencesStore?.saveBookmarks(_uiState.value.bookmarks)
    }

    fun addCustomBookmark(title: String, rawUrl: String) {
        val finalUrl = resolveInputToUrl(rawUrl)
        _uiState.update { state ->
            val filtered = state.bookmarks.filterNot { it.url.equals(finalUrl, ignoreCase = true) }
            state.copy(bookmarks = listOf(QuickBookmark(title, finalUrl, "Saved")) + filtered)
        }
        preferencesStore?.saveBookmarks(_uiState.value.bookmarks)
    }

    fun removeBookmark(url: String) {
        _uiState.update { state ->
            state.copy(bookmarks = state.bookmarks.filterNot { it.url == url })
        }
        preferencesStore?.saveBookmarks(_uiState.value.bookmarks)
    }

    fun openBookmark(bookmark: QuickBookmark) {
        _uiState.update { state ->
            val currentClicks = state.bookmarkClickCounts[bookmark.url] ?: 0
            state.copy(
                bookmarkClickCounts = state.bookmarkClickCounts + (bookmark.url to (currentClicks + 1)),
                siteTitles = state.siteTitles + (bookmark.url to bookmark.title)
            )
        }
        persistShortcutsAndHistory()
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
        persistShortcutsAndHistory()
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
        persistShortcutsAndHistory()
    }

    fun unpinShortcut(shortcut: HomeShortcut) {
        _uiState.update { state ->
            state.copy(
                pinnedShortcuts = state.pinnedShortcuts.filterNot { it.url.equals(shortcut.url, ignoreCase = true) }
            )
        }
        persistShortcutsAndHistory()
    }

    fun removeShortcut(shortcut: HomeShortcut) {
        _uiState.update { state ->
            state.copy(
                pinnedShortcuts = state.pinnedShortcuts.filterNot { it.url.equals(shortcut.url, ignoreCase = true) },
                dismissedAutoShortcutUrls = state.dismissedAutoShortcutUrls + shortcut.url
            )
        }
        persistShortcutsAndHistory()
    }

    fun deleteHistoryItem(id: Long) {
        _uiState.update { state ->
            state.copy(history = state.history.filterNot { it.id == id })
        }
        persistShortcutsAndHistory()
    }

    fun clearBrowsingHistory() {
        _uiState.update { it.copy(history = emptyList()) }
        persistShortcutsAndHistory()
    }

    fun clearBrowsingData(
        targetDomain: String? = null,
        timeRange: ClearDataTimeRange = ClearDataTimeRange.ALL_TIME,
        deleteHistory: Boolean = true,
        deleteCookies: Boolean = true,
        deleteCache: Boolean = true,
        deleteStorage: Boolean = true
    ) {
        val now = System.currentTimeMillis()
        val cutoffTime = if (timeRange == ClearDataTimeRange.ALL_TIME) 0L else now - timeRange.durationMillis

        if (deleteHistory) {
            _uiState.update { state ->
                val updatedHistory = state.history.filterNot { item ->
                    val matchesTime = item.createdAtMillis >= cutoffTime
                    val matchesDomain = targetDomain.isNullOrBlank() || item.url.contains(targetDomain, ignoreCase = true)
                    matchesTime && matchesDomain
                }
                val updatedVisits = if (targetDomain.isNullOrBlank() && timeRange == ClearDataTimeRange.ALL_TIME) {
                    emptyMap()
                } else if (!targetDomain.isNullOrBlank()) {
                    state.siteVisitCounts.filterNot { it.key.contains(targetDomain, ignoreCase = true) }
                } else {
                    state.siteVisitCounts
                }
                state.copy(history = updatedHistory, siteVisitCounts = updatedVisits)
            }
            persistShortcutsAndHistory()
        }

        if (deleteCookies) {
            try {
                val cookieManager = CookieManager.getInstance()
                if (targetDomain.isNullOrBlank()) {
                    cookieManager.removeAllCookies(null)
                    cookieManager.flush()
                } else {
                    val urlHttps = "https://$targetDomain"
                    val urlHttp = "http://$targetDomain"
                    val cookies = cookieManager.getCookie(urlHttps) ?: cookieManager.getCookie(urlHttp)
                    if (cookies != null) {
                        cookies.split(";").forEach { c ->
                            val name = c.substringBefore("=").trim()
                            if (name.isNotEmpty()) {
                                cookieManager.setCookie(urlHttps, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/; Domain=.$targetDomain")
                                cookieManager.setCookie(urlHttps, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/")
                            }
                        }
                    }
                    cookieManager.flush()
                }
            } catch (e: Exception) {
                Log.e("DevBrowser", "Error clearing cookies", e)
            }
        }

        if (deleteStorage) {
            try {
                if (targetDomain.isNullOrBlank()) {
                    WebStorage.getInstance().deleteAllData()
                    clearStorageByType(StorageType.LOCAL_STORAGE)
                    clearStorageByType(StorageType.SESSION_STORAGE)
                } else {
                    WebStorage.getInstance().deleteOrigin("https://$targetDomain")
                    WebStorage.getInstance().deleteOrigin("http://$targetDomain")
                    runJs("if(location.hostname.includes('$targetDomain')) { try { localStorage.clear(); sessionStorage.clear(); } catch(e){} }")
                }
            } catch (e: Exception) {
                Log.e("DevBrowser", "Error clearing storage", e)
            }
        }

        if (deleteCache) {
            viewModelScope.launch(Dispatchers.Main) {
                webViewRef?.clearCache(true)
            }
        }
    }

    fun clearAllBrowserSiteData() {
        clearBrowsingData(
            targetDomain = null,
            timeRange = ClearDataTimeRange.ALL_TIME,
            deleteHistory = true,
            deleteCookies = true,
            deleteCache = true,
            deleteStorage = true
        )
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
        if (isComposeInternalUrl(currentUrl)) {
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
            val refreshedSelection = (if (currentSelectedPath != null) {
                nodes.find { it.path == currentSelectedPath }
            } else {
                null
            }) ?: nodes.find { it.idAttr == "customizable-box" }
                ?: nodes.find { it.tag == "body" }
                ?: nodes.firstOrNull()
                ?: state.selectedDomNode
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

    // VirusTotal Security Operations
    fun setVirusTotalEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isVirusTotalEnabled = enabled) }
        preferencesStore?.saveVirusTotalEnabled(enabled)
        if (enabled) {
            val currentUrl = _uiState.value.activeTab.url
            if (!currentUrl.startsWith("devbrowser:", ignoreCase = true)) {
                scanUrlIfNeeded(currentUrl)
            }
        }
    }

    fun setVirusTotalApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        _uiState.update { it.copy(virusTotalApiKey = trimmed) }
        preferencesStore?.saveVirusTotalApiKey(trimmed)
    }

    fun setVirusTotalRescanInterval(interval: VirusTotalRescanInterval) {
        _uiState.update { it.copy(virusTotalRescanInterval = interval) }
        preferencesStore?.saveVirusTotalRescanInterval(interval)
    }

    fun clearVirusTotalScanCache() {
        _uiState.update { it.copy(virusTotalScanCache = emptyMap(), activeWarningState = null) }
        preferencesStore?.clearVirusTotalScanResults()
    }

    fun setSecurityPopupVisible(visible: Boolean) {
        _uiState.update { it.copy(isSecurityPopupVisible = visible) }
    }

    fun bypassWarning(url: String) {
        val domain = VirusTotalApiClient.extractDomain(url)
        _uiState.update { state ->
            state.copy(
                bypassedWarningUrls = state.bypassedWarningUrls + url + domain,
                activeWarningState = null
            )
        }
    }

    fun dismissWarning() {
        _uiState.update { it.copy(activeWarningState = null) }
    }

    fun setWarningAreYouSureDialog(show: Boolean) {
        _uiState.update { state ->
            val cur = state.activeWarningState ?: return@update state
            state.copy(activeWarningState = cur.copy(showAreYouSureDialog = show))
        }
    }

    fun scanUrlIfNeeded(targetUrl: String, forceRescan: Boolean = false) {
        val state = _uiState.value
        if (targetUrl.startsWith("devbrowser:", ignoreCase = true)) return

        if (state.isVirusTotalEnabled && state.virusTotalApiKey.isNotBlank()) {
            scanUrlWithVirusTotal(targetUrl, forceRescan)
        } else if (state.isAiProtectionActive) {
            scanUrlWithAiProtection(targetUrl, forceRescan)
        }
    }

    private fun scanUrlWithVirusTotal(targetUrl: String, forceRescan: Boolean = false) {
        val state = _uiState.value
        val domain = VirusTotalApiClient.extractDomain(targetUrl)
        val cached = state.virusTotalScanCache[targetUrl] ?: state.virusTotalScanCache[domain]
        val intervalMillis = state.virusTotalRescanInterval.durationMillis
        val now = System.currentTimeMillis()
        val isFresh = cached != null && (intervalMillis > 0L && (now - cached.scanTimestamp) < intervalMillis)

        if (!forceRescan && isFresh && cached != null) {
            if ((cached.threatLevel == VirusTotalThreatLevel.CAREFUL || cached.threatLevel == VirusTotalThreatLevel.DANGER) &&
                targetUrl !in state.bypassedWarningUrls && domain !in state.bypassedWarningUrls
            ) {
                _uiState.update {
                    it.copy(
                        activeWarningState = VirusTotalWarningState(
                            targetUrl = targetUrl,
                            domain = domain,
                            scanResult = cached,
                            threatLevel = cached.threatLevel
                        )
                    )
                }
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isScanningVirusTotal = true) }
            val scanResult = VirusTotalApiClient.scanUrl(state.virusTotalApiKey, targetUrl)

            _uiState.update { s ->
                val newCache = s.virusTotalScanCache + (targetUrl to scanResult) + (domain to scanResult)
                val isStillOnUrl = s.activeTab.url.equals(targetUrl, ignoreCase = true)
                val shouldWarn = isStillOnUrl &&
                    (scanResult.threatLevel == VirusTotalThreatLevel.CAREFUL || scanResult.threatLevel == VirusTotalThreatLevel.DANGER) &&
                    targetUrl !in s.bypassedWarningUrls && domain !in s.bypassedWarningUrls

                s.copy(
                    virusTotalScanCache = newCache,
                    isScanningVirusTotal = false,
                    activeWarningState = if (shouldWarn) {
                        VirusTotalWarningState(
                            targetUrl = targetUrl,
                            domain = domain,
                            scanResult = scanResult,
                            threatLevel = scanResult.threatLevel
                        )
                    } else s.activeWarningState
                )
            }
            preferencesStore?.saveVirusTotalScanResults(_uiState.value.virusTotalScanCache)
        }
    }

    fun scanUrlWithAiProtection(targetUrl: String, forceRescan: Boolean = false) {
        val state = _uiState.value
        if (!state.isAiProtectionActive) return
        if (targetUrl.startsWith("devbrowser:", ignoreCase = true)) return

        val domain = VirusTotalApiClient.extractDomain(targetUrl)
        val cached = state.aiScanCache[targetUrl] ?: state.aiScanCache[domain]

        if (!forceRescan && cached != null) {
            if ((cached.category == AiThreatCategory.CAREFUL || cached.category == AiThreatCategory.DANGER) &&
                targetUrl !in state.bypassedWarningUrls && domain !in state.bypassedWarningUrls
            ) {
                _uiState.update {
                    it.copy(
                        activeWarningState = VirusTotalWarningState(
                            targetUrl = targetUrl,
                            domain = domain,
                            scanResult = VirusTotalScanResult(
                                url = targetUrl,
                                domain = domain,
                                maliciousCount = if (cached.category == AiThreatCategory.DANGER) 10 else 5,
                                totalEngines = 10,
                                threatLevel = cached.category.vtEquivalent,
                                errorMessage = cached.reasoning
                            ),
                            threatLevel = cached.category.vtEquivalent,
                            isAiProtection = true,
                            aiReasoning = cached.reasoning
                        )
                    )
                }
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isScanningAiProtection = true) }
            val scanResult = GeminiApiClient.analyzeUrlSecurity(
                apiKey = state.effectiveGeminiApiKey,
                targetUrl = targetUrl,
                domain = domain,
                modelChoice = state.selectedGeminiModel
            )

            _uiState.update { s ->
                val newCache = s.aiScanCache + (targetUrl to scanResult) + (domain to scanResult)
                val isStillOnUrl = s.activeTab.url.equals(targetUrl, ignoreCase = true)
                val shouldWarn = isStillOnUrl &&
                    (scanResult.category == AiThreatCategory.CAREFUL || scanResult.category == AiThreatCategory.DANGER) &&
                    targetUrl !in s.bypassedWarningUrls && domain !in s.bypassedWarningUrls

                s.copy(
                    aiScanCache = newCache,
                    isScanningAiProtection = false,
                    activeWarningState = if (shouldWarn) {
                        VirusTotalWarningState(
                            targetUrl = targetUrl,
                            domain = domain,
                            scanResult = VirusTotalScanResult(
                                url = targetUrl,
                                domain = domain,
                                maliciousCount = if (scanResult.category == AiThreatCategory.DANGER) 10 else 5,
                                totalEngines = 10,
                                threatLevel = scanResult.category.vtEquivalent,
                                errorMessage = scanResult.reasoning
                            ),
                            threatLevel = scanResult.category.vtEquivalent,
                            isAiProtection = true,
                            aiReasoning = scanResult.reasoning
                        )
                    } else s.activeWarningState
                )
            }
            preferencesStore?.saveAiScanResults(_uiState.value.aiScanCache)
        }
    }

    fun rescanCurrentUrl() {
        val activeUrl = _uiState.value.activeTab.url
        if (!activeUrl.startsWith("devbrowser:", ignoreCase = true)) {
            scanUrlIfNeeded(activeUrl, forceRescan = true)
        }
    }

    fun saveDirectScanResult(result: VirusTotalScanResult) {
        _uiState.update { state ->
            val newCache = state.virusTotalScanCache + (result.url to result) + (result.domain to result)
            state.copy(virusTotalScanCache = newCache)
        }
        preferencesStore?.saveVirusTotalScanResults(_uiState.value.virusTotalScanCache)
    }

    fun saveDirectAiScanResult(result: AiProtectionScanResult) {
        _uiState.update { state ->
            val newCache = state.aiScanCache + (result.url to result) + (result.domain to result)
            state.copy(aiScanCache = newCache)
        }
        preferencesStore?.saveAiScanResults(_uiState.value.aiScanCache)
    }

    // AI Operations & Preferences
    fun setAiMasterEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAiMasterEnabled = enabled) }
        preferencesStore?.saveAiMasterEnabled(enabled)
        val activeUrl = _uiState.value.activeTab.url
        if (enabled && !activeUrl.startsWith("devbrowser:", ignoreCase = true)) {
            scanUrlIfNeeded(activeUrl)
        }
    }

    fun setGeminiApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        _uiState.update { it.copy(geminiApiKey = trimmed) }
        preferencesStore?.saveGeminiApiKey(trimmed)
    }

    fun setAiProtectionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAiProtectionEnabled = enabled) }
        preferencesStore?.saveAiProtectionEnabled(enabled)
        val activeUrl = _uiState.value.activeTab.url
        if (enabled && !activeUrl.startsWith("devbrowser:", ignoreCase = true)) {
            scanUrlIfNeeded(activeUrl)
        }
    }

    fun setAiSummaryEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAiSummaryEnabled = enabled) }
        preferencesStore?.saveAiSummaryEnabled(enabled)
    }

    fun setAiTranslateEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAiTranslateEnabled = enabled) }
        preferencesStore?.saveAiTranslateEnabled(enabled)
    }

    fun setGeminiModel(model: GeminiModelChoice) {
        _uiState.update { it.copy(selectedGeminiModel = model) }
        preferencesStore?.saveGeminiModel(model)
    }

    fun clearAiScanCache() {
        _uiState.update { it.copy(aiScanCache = emptyMap(), activeWarningState = null) }
        preferencesStore?.clearAiScanResults()
    }

    // AI Chat Workflow
    fun triggerAiChat() {
        val state = _uiState.value
        val activeTab = state.activeTab
        val apiKey = state.effectiveGeminiApiKey

        if (!state.isAiMasterEnabled) {
            _uiState.update {
                it.copy(
                    activeAiChatState = AiChatDialogState(
                        url = activeTab.url,
                        title = activeTab.title,
                        errorMessage = "AI Features are currently disabled.\nEnable the master toggle in Settings > Ai Features to use AI Chat."
                    )
                )
            }
            return
        }

        if (apiKey.isBlank()) {
            _uiState.update {
                it.copy(
                    activeAiChatState = AiChatDialogState(
                        url = activeTab.url,
                        title = activeTab.title,
                        errorMessage = "Gemini API key is required.\nPlease enter your Gemini API key in Settings > Ai Features."
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                activeAiChatState = AiChatDialogState(
                    url = activeTab.url,
                    title = activeTab.title,
                    isGeneratingSuggestions = true
                )
            )
        }

        val extractScript = """
            (function() {
                try {
                    var title = document.title || '';
                    var bodyText = document.body ? (document.body.innerText || document.body.textContent || '') : '';
                    return (title + '\n\n' + bodyText).slice(0, 15000);
                } catch(e) {
                    return document.title || '';
                }
            })();
        """.trimIndent()

        runJs(extractScript) { rawExtracted ->
            val cleanText = unescapeJsString(rawExtracted).ifBlank { activeTab.title }
            viewModelScope.launch(Dispatchers.IO) {
                val suggestionsResult = GeminiApiClient.generateChatSuggestions(
                    apiKey = apiKey,
                    title = activeTab.title,
                    url = activeTab.url,
                    webpageContent = cleanText,
                    modelChoice = state.selectedGeminiModel
                )
                val suggestions = suggestionsResult.getOrDefault(listOf("Summarize this webpage", "What is the main topic?", "Extract key takeaways"))

                _uiState.update { s ->
                    val cur = s.activeAiChatState ?: return@update s
                    cur.copy(
                        webpageContent = cleanText,
                        suggestions = suggestions,
                        isGeneratingSuggestions = false
                    ).let { updated -> s.copy(activeAiChatState = updated) }
                }
            }
        }
    }

    fun updateAiChatQuery(query: String) {
        _uiState.update { s ->
            val cur = s.activeAiChatState ?: return@update s
            s.copy(activeAiChatState = cur.copy(inputQuery = query))
        }
    }

    fun sendAiChatMessage(query: String) {
        val state = _uiState.value
        val chatState = state.activeAiChatState ?: return
        val apiKey = state.effectiveGeminiApiKey

        val userMsg = ChatMessage(text = query, isUser = true)
        val newMessages = chatState.messages + userMsg

        _uiState.update { s ->
            s.copy(
                activeAiChatState = chatState.copy(
                    messages = newMessages,
                    inputQuery = "",
                    isLoadingAnswer = true,
                    errorMessage = null
                )
            )
        }

        val historyPairs = chatState.messages.map { Pair(it.text, it.isUser) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = GeminiApiClient.askAiChat(
                apiKey = apiKey,
                title = chatState.title,
                url = chatState.url,
                webpageContent = chatState.webpageContent,
                chatHistory = historyPairs,
                userQuery = query,
                modelChoice = state.selectedGeminiModel
            )

            _uiState.update { s ->
                val cur = s.activeAiChatState ?: return@update s
                result.fold(
                    onSuccess = { (answer, modelUsed) ->
                        val modelMsg = ChatMessage(text = answer, isUser = false)
                        cur.copy(
                            messages = cur.messages + modelMsg,
                            modelUsed = modelUsed,
                            isLoadingAnswer = false,
                            errorMessage = null
                        )
                    },
                    onFailure = { err ->
                        cur.copy(
                            isLoadingAnswer = false,
                            errorMessage = err.message ?: "Failed to get AI response"
                        )
                    }
                ).let { updated -> s.copy(activeAiChatState = updated) }
            }
        }
    }

    fun dismissAiChat() {
        _uiState.update { it.copy(activeAiChatState = null) }
    }

    // AI Translation Workflow
    fun triggerTranslate() {
        val state = _uiState.value
        val activeTab = state.activeTab
        val apiKey = state.effectiveGeminiApiKey

        if (!state.isAiMasterEnabled) {
            _uiState.update {
                it.copy(
                    activeTranslationState = WebpageTranslationState(
                        url = activeTab.url,
                        title = activeTab.title,
                        showResultDialog = true,
                        errorMessage = "AI Features are currently disabled.\nEnable the master toggle in Settings > Ai Features to translate webpages."
                    )
                )
            }
            return
        }

        if (apiKey.isBlank()) {
            _uiState.update {
                it.copy(
                    activeTranslationState = WebpageTranslationState(
                        url = activeTab.url,
                        title = activeTab.title,
                        showResultDialog = true,
                        errorMessage = "Gemini API key is required.\nPlease enter your Gemini API key in Settings > Ai Features."
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                activeTranslationState = WebpageTranslationState(
                    url = activeTab.url,
                    title = activeTab.title,
                    showLanguageSelector = true,
                    isLoading = true
                )
            )
        }

        val snippetScript = """
            (function() {
                try {
                    var lang = document.documentElement.lang || document.querySelector('meta[http-equiv="content-language"]')?.content || '';
                    var body = document.body ? (document.body.innerText || document.body.textContent || '').slice(0, 500) : '';
                    return JSON.stringify({ lang: lang, text: body });
                } catch(e) {
                    return JSON.stringify({ lang: '', text: '' });
                }
            })();
        """.trimIndent()

        runJs(snippetScript) { rawSnippet ->
            var detected = "English"
            var sampleText = ""
            try {
                val unescaped = unescapeJsString(rawSnippet)
                val obj = org.json.JSONObject(unescaped)
                val rawLang = obj.optString("lang", "").trim()
                sampleText = obj.optString("text", "").trim()
                if (rawLang.isNotBlank()) {
                    val loc = Locale.forLanguageTag(rawLang)
                    if (loc.displayLanguage.isNotBlank()) {
                        detected = loc.displayLanguage
                    }
                }
            } catch (_: Exception) {}

            viewModelScope.launch(Dispatchers.IO) {
                if (sampleText.isNotBlank()) {
                    val aiDetect = GeminiApiClient.detectLanguage(
                        apiKey = apiKey,
                        sampleText = sampleText,
                        modelChoice = state.selectedGeminiModel
                    )
                    if (aiDetect.isSuccess && aiDetect.getOrNull()?.isNotBlank() == true) {
                        detected = aiDetect.getOrNull()!!
                    }
                }

                _uiState.update { s ->
                    val cur = s.activeTranslationState ?: return@update s
                    val target = if (detected.equals("English", ignoreCase = true)) "Spanish" else "English"
                    s.copy(
                        activeTranslationState = cur.copy(
                            sourceLanguage = detected,
                            targetLanguage = target,
                            isLoading = false
                        )
                    )
                }
            }
        }
    }

    fun confirmTranslation(sourceLang: String, targetLang: String) {
        val state = _uiState.value
        val activeTab = state.activeTab
        val apiKey = state.effectiveGeminiApiKey

        _uiState.update { s ->
            s.copy(
                activeTranslationState = WebpageTranslationState(
                    url = activeTab.url,
                    title = activeTab.title,
                    sourceLanguage = sourceLang,
                    targetLanguage = targetLang,
                    showLanguageSelector = false,
                    showResultDialog = true,
                    isLoading = true,
                    errorMessage = null
                )
            )
        }

        val extractScript = """
            (function() {
                try {
                    var title = document.title || '';
                    var bodyText = document.body ? (document.body.innerText || document.body.textContent || '') : '';
                    return (title + '\n\n' + bodyText).slice(0, 15000);
                } catch(e) {
                    return document.title || '';
                }
            })();
        """.trimIndent()

        runJs(extractScript) { rawExtracted ->
            val cleanText = unescapeJsString(rawExtracted).ifBlank { activeTab.title }
            viewModelScope.launch(Dispatchers.IO) {
                val result = GeminiApiClient.translateContent(
                    apiKey = apiKey,
                    content = cleanText,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    modelChoice = state.selectedGeminiModel
                )
                _uiState.update { s ->
                    val cur = s.activeTranslationState ?: return@update s
                    result.fold(
                        onSuccess = { (translated, modelUsed) ->
                            cur.copy(
                                originalText = cleanText,
                                translatedText = translated,
                                modelUsed = modelUsed,
                                isLoading = false,
                                errorMessage = null
                            )
                        },
                        onFailure = { err ->
                            cur.copy(
                                originalText = cleanText,
                                errorMessage = err.message ?: "Failed to translate webpage",
                                isLoading = false
                            )
                        }
                    ).let { updated -> s.copy(activeTranslationState = updated) }
                }
            }
        }
    }

    fun dismissTranslation() {
        _uiState.update { it.copy(activeTranslationState = null) }
    }

    private fun unescapeJsString(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed == "null" || trimmed == "undefined") return ""
        return try {
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                org.json.JSONTokener(trimmed).nextValue().toString()
            } else {
                trimmed
            }
        } catch (_: Exception) {
            trimmed.removeSurrounding("\"")
        }
    }

    // Userscripts Operations
    fun addUserScript(script: UserScript) {
        _uiState.update { state ->
            val updated = state.userScripts + script
            preferencesStore?.saveUserScripts(updated)
            state.copy(userScripts = updated)
        }
    }

    fun updateUserScript(script: UserScript) {
        _uiState.update { state ->
            val updated = state.userScripts.map { if (it.id == script.id) script else it }
            preferencesStore?.saveUserScripts(updated)
            state.copy(userScripts = updated)
        }
    }

    fun deleteUserScript(id: String) {
        _uiState.update { state ->
            val updated = state.userScripts.filter { it.id != id }
            preferencesStore?.saveUserScripts(updated)
            state.copy(userScripts = updated)
        }
    }

    fun toggleUserScriptEnabled(id: String, enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.userScripts.map { if (it.id == id) it.copy(enabled = enabled) else it }
            preferencesStore?.saveUserScripts(updated)
            state.copy(userScripts = updated)
        }
    }

    fun setUserScriptUpdateInterval(interval: UserScriptUpdateInterval) {
        _uiState.update { it.copy(userScriptUpdateInterval = interval) }
        preferencesStore?.saveUserScriptUpdateInterval(interval)
    }

    fun importUserScriptFromUrl(urlString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val url = java.net.URL(urlString)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "DevBrowser-UserscriptClient/1.0")
                }
                val code = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val fallbackName = url.path.substringAfterLast('/').ifBlank { "Imported Script" }.removeSuffix(".user.js")
                val parsed = UserScript.parseMetadata(code, fallbackName)
                parsed.copy(updateUrl = if (parsed.updateUrl.isBlank()) urlString else parsed.updateUrl)
            }
            viewModelScope.launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    val script = result.getOrThrow()
                    addUserScript(script)
                    onResult(true, "Successfully imported '${script.name}'")
                } else {
                    onResult(false, result.exceptionOrNull()?.message ?: "Failed to download userscript")
                }
            }
        }
    }

    fun injectUserScriptsIfNeeded(url: String) {
        val state = _uiState.value
        if (!state.javaScriptEnabled) return
        val enabledScripts = state.userScripts.filter { it.enabled && it.code.isNotBlank() }
        if (enabledScripts.isEmpty()) return

        viewModelScope.launch(Dispatchers.Main) {
            val wv = webViewRef ?: return@launch
            enabledScripts.forEach { script ->
                val matches = script.matchPatterns.any { pattern ->
                    pattern == "*" || pattern == "*://*/*" || url.contains(pattern.removePrefix("*").removeSuffix("*"), ignoreCase = true)
                }
                if (matches) {
                    wv.evaluateJavascript(script.code, null)
                }
            }
        }
    }

    fun checkForUserscriptUpdates() {
        val state = _uiState.value
        val intervalHours = state.userScriptUpdateInterval.hours
        if (intervalHours <= 0) return

        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val intervalMs = intervalHours * 3600_000L
            val scripts = state.userScripts
            var updated = false
            val newScripts = scripts.map { script ->
                if (script.updateUrl.isNotBlank() && (now - script.lastUpdated > intervalMs)) {
                    val fetchResult = runCatching {
                        val url = java.net.URL(script.updateUrl)
                        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                            connectTimeout = 8000
                            readTimeout = 8000
                            requestMethod = "GET"
                        }
                        conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    }
                    if (fetchResult.isSuccess) {
                        val newCode = fetchResult.getOrThrow()
                        val parsed = UserScript.parseMetadata(newCode, script.name)
                        updated = true
                        script.copy(
                            code = newCode,
                            name = parsed.name.ifBlank { script.name },
                            lastUpdated = now,
                            matchPatterns = parsed.matchPatterns
                        )
                    } else script
                } else script
            }
            if (updated) {
                viewModelScope.launch(Dispatchers.Main) {
                    _uiState.update { st ->
                        preferencesStore?.saveUserScripts(newScripts)
                        st.copy(userScripts = newScripts)
                    }
                }
            }
        }
    }

    // Auto-Update Management
    fun setAutoUpdateEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAutoUpdateEnabled = enabled) }
        preferencesStore?.saveAutoUpdateEnabled(enabled)
    }

    fun setGithubApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        _uiState.update { it.copy(githubApiKey = trimmed) }
        preferencesStore?.saveGithubApiKey(trimmed)
    }

    fun setAutoUpdateFrequency(frequency: AutoUpdateCheckFrequency) {
        _uiState.update { it.copy(autoUpdateFrequency = frequency) }
        preferencesStore?.saveAutoUpdateFrequency(frequency)
    }

    fun setIncludePrereleases(include: Boolean) {
        _uiState.update { it.copy(isIncludePrereleases = include) }
        preferencesStore?.saveIncludePrereleases(include)
    }

    fun dismissUpdateNotice() {
        _uiState.update { it.copy(pendingUpdateNotice = null) }
    }

    fun triggerDebugAutoUpdate(
        versionParam: String? = null,
        descriptionParam: String? = null,
        changelogParam: String? = null
    ) {
        val state = _uiState.value
        _uiState.update { it.copy(updateCheckStatus = UpdateCheckStatus.Checking) }

        viewModelScope.launch(Dispatchers.IO) {
            val needsGithubFetch = versionParam.isNullOrBlank() ||
                descriptionParam.isNullOrBlank() ||
                changelogParam.isNullOrBlank()

            val githubReleaseInfo = if (needsGithubFetch) {
                when (val result = GitHubUpdateApiClient.checkForUpdates(
                    apiKey = state.githubApiKey,
                    currentVersionName = com.example.BuildConfig.VERSION_NAME,
                    includePrereleases = state.isIncludePrereleases
                )) {
                    is GitHubUpdateApiClient.Result.Success -> result.updateInfo
                    is GitHubUpdateApiClient.Result.Error -> null
                }
            } else null

            val fallbackVersion = com.example.BuildConfig.VERSION_NAME
            val resolvedVersion = when {
                !versionParam.isNullOrBlank() -> versionParam.trim()
                githubReleaseInfo != null && githubReleaseInfo.latestVersionName.isNotBlank() -> githubReleaseInfo.latestVersionName
                else -> fallbackVersion
            }

            val cleanVer = resolvedVersion.removePrefix("v").removePrefix("V")

            val resolvedTitle = when {
                !descriptionParam.isNullOrBlank() -> descriptionParam.trim()
                githubReleaseInfo != null && githubReleaseInfo.releaseTitle.isNotBlank() -> githubReleaseInfo.releaseTitle
                else -> "DevBrowser v$cleanVer"
            }

            val resolvedNotes = when {
                !changelogParam.isNullOrBlank() -> changelogParam.trim().replace("\\n", "\n")
                githubReleaseInfo != null && githubReleaseInfo.releaseNotes.isNotBlank() -> githubReleaseInfo.releaseNotes
                else -> "- Performance improvements and bug fixes\n- Updated WebView engine\n- Developer tools enhancements"
            }

            val resolvedDownloadUrl = if (githubReleaseInfo != null &&
                cleanVer.equals(githubReleaseInfo.latestVersionName.removePrefix("v").removePrefix("V"), ignoreCase = true) &&
                githubReleaseInfo.downloadUrl.isNotBlank()
            ) {
                githubReleaseInfo.downloadUrl
            } else {
                "https://github.com/Green0Field/DevBrowser/releases/download/v$cleanVer/DevBrowser-$cleanVer-release.apk"
            }

            val resolvedHtmlUrl = if (githubReleaseInfo != null &&
                cleanVer.equals(githubReleaseInfo.latestVersionName.removePrefix("v").removePrefix("V"), ignoreCase = true) &&
                githubReleaseInfo.htmlUrl.isNotBlank()
            ) {
                githubReleaseInfo.htmlUrl
            } else {
                "https://github.com/Green0Field/DevBrowser/releases/tag/v$cleanVer"
            }

            val simulatedUpdateInfo = AppUpdateInfo(
                latestVersionName = cleanVer,
                releaseTitle = resolvedTitle,
                releaseNotes = resolvedNotes,
                publishedAt = githubReleaseInfo?.publishedAt?.ifBlank { null } ?: "Just now",
                downloadUrl = resolvedDownloadUrl,
                htmlUrl = resolvedHtmlUrl,
                apkFileName = "DevBrowser-$cleanVer-release.apk",
                apkSizeFormatted = if (githubReleaseInfo != null && cleanVer.equals(githubReleaseInfo.latestVersionName, ignoreCase = true) && githubReleaseInfo.apkSizeFormatted.isNotBlank()) {
                    githubReleaseInfo.apkSizeFormatted
                } else {
                    "14.2 MB"
                },
                isUpdateAvailable = true
            )

            _uiState.update { current ->
                current.copy(
                    pendingUpdateNotice = simulatedUpdateInfo,
                    updateCheckStatus = UpdateCheckStatus.UpdateAvailable(simulatedUpdateInfo)
                )
            }
        }
    }

    fun checkForAppUpdates(isManual: Boolean = true) {
        val state = _uiState.value
        _uiState.update { it.copy(updateCheckStatus = UpdateCheckStatus.Checking) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = GitHubUpdateApiClient.checkForUpdates(
                apiKey = state.githubApiKey,
                currentVersionName = com.example.BuildConfig.VERSION_NAME,
                includePrereleases = state.isIncludePrereleases
            )
            val now = System.currentTimeMillis()
            preferencesStore?.saveLastUpdateCheckMillis(now)

            _uiState.update { current ->
                when (result) {
                    is GitHubUpdateApiClient.Result.Success -> {
                        val info = result.updateInfo
                        if (info.isUpdateAvailable) {
                            current.copy(
                                lastUpdateCheckMillis = now,
                                updateCheckStatus = UpdateCheckStatus.UpdateAvailable(info),
                                pendingUpdateNotice = info
                            )
                        } else {
                            current.copy(
                                lastUpdateCheckMillis = now,
                                updateCheckStatus = UpdateCheckStatus.UpToDate(
                                    currentVersion = com.example.BuildConfig.VERSION_NAME,
                                    lastCheckedMillis = now
                                )
                            )
                        }
                    }
                    is GitHubUpdateApiClient.Result.Error -> {
                        current.copy(
                            lastUpdateCheckMillis = now,
                            updateCheckStatus = UpdateCheckStatus.Error(result.message)
                        )
                    }
                }
            }
        }
    }

    private fun checkAutoUpdateOnStartupIfNeeded() {
        val state = _uiState.value
        val newStartupCount = state.autoUpdateStartupCount + 1
        preferencesStore?.saveAutoUpdateStartupCount(newStartupCount)

        if (state.isAutoUpdateEnabled && state.autoUpdateFrequency != AutoUpdateCheckFrequency.MANUAL_ONLY) {
            val threshold = state.autoUpdateFrequency.startupThreshold
            if (newStartupCount >= threshold) {
                preferencesStore?.saveAutoUpdateStartupCount(0)
                _uiState.update { it.copy(autoUpdateStartupCount = 0) }
                checkForAppUpdates(isManual = false)
            } else {
                _uiState.update { it.copy(autoUpdateStartupCount = newStartupCount) }
            }
        } else {
            _uiState.update { it.copy(autoUpdateStartupCount = newStartupCount) }
        }
    }
}
