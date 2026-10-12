package com.example.model

data class BrowserTab(
    val id: String,
    val title: String = "Home",
    val url: String = URL_HOME,
    val isLoading: Boolean = false,
    val progress: Int = 100,
    val webViewCanGoBack: Boolean = false,
    val webViewCanGoForward: Boolean = false,
    val backStack: List<String> = emptyList(),
    val forwardStack: List<String> = emptyList(),
    val isDesktopSite: Boolean = false,
    val devicePreset: DeviceViewportPreset = DeviceViewportPreset.RESPONSIVE
) {
    val canGoBack: Boolean
        get() = backStack.isNotEmpty() || webViewCanGoBack

    val canGoForward: Boolean
        get() = forwardStack.isNotEmpty() || webViewCanGoForward

    val isInternalPage: Boolean
        get() = url.startsWith("devbrowser:", ignoreCase = true)

    companion object {
        const val URL_HOME = "devbrowser:home"
        const val URL_TEST = "devbrowser:test"
        const val URL_BOOKMARKS = "devbrowser:bookmarks"
        const val URL_HISTORY = "devbrowser:history"
        const val URL_SETTINGS = "devbrowser:settings"
        const val URL_SETTINGS_THEME = "devbrowser:settings/theme"
        const val URL_SETTINGS_SEARCH = "devbrowser:settings/search"
        const val URL_SETTINGS_DATA = "devbrowser:settings/data"
        const val URL_SETTINGS_VIRUSTOTAL = "devbrowser:settings/virustotal"
        const val URL_SETTINGS_ABOUT = "devbrowser:settings/about"
        const val INTERNAL_TEST_BASE_URL = "https://devbrowser.internal/test"
    }
}

enum class ThemeAccentColor(
    val displayName: String,
    val neonDarkHex: Long,
    val neonLightHex: Long,
    val darkHex: Long,
    val lightHex: Long,
    val neonDarkBgHex: Long,
    val neonDarkSurfaceHex: Long,
    val neonDarkSurfaceVariantHex: Long,
    val darkBgHex: Long,
    val darkSurfaceHex: Long,
    val darkSurfaceVariantHex: Long,
    val darkOutlineHex: Long,
    val darkOutlineVariantHex: Long,
    val neonLightBgHex: Long,
    val neonLightSurfaceHex: Long,
    val neonLightSurfaceVariantHex: Long,
    val lightBgHex: Long,
    val lightSurfaceHex: Long,
    val lightSurfaceVariantHex: Long,
    val lightOutlineHex: Long,
    val lightOutlineVariantHex: Long
) {
    BLUE(
        displayName = "Blue",
        neonDarkHex = 0xFF00D2FF,
        neonLightHex = 0xFF0077FF,
        darkHex = 0xFF3B82F6,
        lightHex = 0xFF2563EB,
        neonDarkBgHex = 0xFF040D1F,
        neonDarkSurfaceHex = 0xFF081A38,
        neonDarkSurfaceVariantHex = 0xFF0E2956,
        darkBgHex = 0xFF0C1220,
        darkSurfaceHex = 0xFF141E30,
        darkSurfaceVariantHex = 0xFF1E2C44,
        darkOutlineHex = 0xFF33496E,
        darkOutlineVariantHex = 0xFF253652,
        neonLightBgHex = 0xFFE0F4FF,
        neonLightSurfaceHex = 0xFFF0FAFF,
        neonLightSurfaceVariantHex = 0xFFC2EAFF,
        lightBgHex = 0xFFEEF5FF,
        lightSurfaceHex = 0xFFF7FAFF,
        lightSurfaceVariantHex = 0xFFDCEAFE,
        lightOutlineHex = 0xFF93C5FD,
        lightOutlineVariantHex = 0xFFBFDBFE
    ),
    RED(
        displayName = "Red",
        neonDarkHex = 0xFFFF1E56,
        neonLightHex = 0xFFFF003C,
        darkHex = 0xFFEF4444,
        lightHex = 0xFFDC2626,
        neonDarkBgHex = 0xFF1F040B,
        neonDarkSurfaceHex = 0xFF360814,
        neonDarkSurfaceVariantHex = 0xFF520D20,
        darkBgHex = 0xFF1B0C0E,
        darkSurfaceHex = 0xFF2B1418,
        darkSurfaceVariantHex = 0xFF3D1D23,
        darkOutlineHex = 0xFF6B323C,
        darkOutlineVariantHex = 0xFF4F242C,
        neonLightBgHex = 0xFFFFE3EA,
        neonLightSurfaceHex = 0xFFFFF2F5,
        neonLightSurfaceVariantHex = 0xFFFFC7D5,
        lightBgHex = 0xFFFFF0F1,
        lightSurfaceHex = 0xFFFFF8F8,
        lightSurfaceVariantHex = 0xFFFFE0E3,
        lightOutlineHex = 0xFFFCA5A5,
        lightOutlineVariantHex = 0xFFFECDD3
    ),
    ORANGE(
        displayName = "Orange",
        neonDarkHex = 0xFFFF6B00,
        neonLightHex = 0xFFFF5500,
        darkHex = 0xFFF97316,
        lightHex = 0xFFEA580C,
        neonDarkBgHex = 0xFF1F0B02,
        neonDarkSurfaceHex = 0xFF361405,
        neonDarkSurfaceVariantHex = 0xFF522009,
        darkBgHex = 0xFF1C1009,
        darkSurfaceHex = 0xFF2C1A10,
        darkSurfaceVariantHex = 0xFF3E2618,
        darkOutlineHex = 0xFF6E432A,
        darkOutlineVariantHex = 0xFF52311F,
        neonLightBgHex = 0xFFFFECE0,
        neonLightSurfaceHex = 0xFFFFF6F0,
        neonLightSurfaceVariantHex = 0xFFFFD6BD,
        lightBgHex = 0xFFFFF5EB,
        lightSurfaceHex = 0xFFFFFAF5,
        lightSurfaceVariantHex = 0xFFFFE6CC,
        lightOutlineHex = 0xFFFDBA74,
        lightOutlineVariantHex = 0xFFFED7AA
    ),
    YELLOW(
        displayName = "Yellow",
        neonDarkHex = 0xFFFFEA00,
        neonLightHex = 0xFFD9B300,
        darkHex = 0xFFEAB308,
        lightHex = 0xFFCA8A04,
        neonDarkBgHex = 0xFF1C1802,
        neonDarkSurfaceHex = 0xFF332B05,
        neonDarkSurfaceVariantHex = 0xFF4D4109,
        darkBgHex = 0xFF191608,
        darkSurfaceHex = 0xFF29230E,
        darkSurfaceVariantHex = 0xFF3B3215,
        darkOutlineHex = 0xFF665824,
        darkOutlineVariantHex = 0xFF4D421B,
        neonLightBgHex = 0xFFFFFDE0,
        neonLightSurfaceHex = 0xFFFFFFF0,
        neonLightSurfaceVariantHex = 0xFFFFF8B8,
        lightBgHex = 0xFFFEFBE8,
        lightSurfaceHex = 0xFFFFFEF5,
        lightSurfaceVariantHex = 0xFFFEF3C7,
        lightOutlineHex = 0xFFFDE047,
        lightOutlineVariantHex = 0xFFFEF08A
    ),
    GREEN(
        displayName = "Green",
        neonDarkHex = 0xFF00FF87,
        neonLightHex = 0xFF00B85C,
        darkHex = 0xFF10B981,
        lightHex = 0xFF059669,
        neonDarkBgHex = 0xFF021C10,
        neonDarkSurfaceHex = 0xFF06331E,
        neonDarkSurfaceVariantHex = 0xFF0B4D2E,
        darkBgHex = 0xFF081913,
        darkSurfaceHex = 0xFF10281F,
        darkSurfaceVariantHex = 0xFF18382C,
        darkOutlineHex = 0xFF285E4A,
        darkOutlineVariantHex = 0xFF1D4738,
        neonLightBgHex = 0xFFDCFFEE,
        neonLightSurfaceHex = 0xFFF0FFF7,
        neonLightSurfaceVariantHex = 0xFFB8FFDD,
        lightBgHex = 0xFFEBFEF5,
        lightSurfaceHex = 0xFFF5FFF9,
        lightSurfaceVariantHex = 0xFFD1FAE5,
        lightOutlineHex = 0xFF6EE7B7,
        lightOutlineVariantHex = 0xFFA7F3D0
    ),
    PURPLE(
        displayName = "Purple",
        neonDarkHex = 0xFFD500F9,
        neonLightHex = 0xFFAA00FF,
        darkHex = 0xFFA855F7,
        lightHex = 0xFF7C3AED,
        neonDarkBgHex = 0xFF160322,
        neonDarkSurfaceHex = 0xFF28073D,
        neonDarkSurfaceVariantHex = 0xFF3D0C5C,
        darkBgHex = 0xFF140C1F,
        darkSurfaceHex = 0xFF221433,
        darkSurfaceVariantHex = 0xFF301D47,
        darkOutlineHex = 0xFF523278,
        darkOutlineVariantHex = 0xFF3D2559,
        neonLightBgHex = 0xFFF5E3FF,
        neonLightSurfaceHex = 0xFFFAF2FF,
        neonLightSurfaceVariantHex = 0xFFE9C7FF,
        lightBgHex = 0xFFF6F0FF,
        lightSurfaceHex = 0xFFFAF7FF,
        lightSurfaceVariantHex = 0xFFEDE0FF,
        lightOutlineHex = 0xFFC4B5FD,
        lightOutlineVariantHex = 0xFFDDD6FE
    ),
    CYAN(
        displayName = "Cyan",
        neonDarkHex = 0xFF00F5D4,
        neonLightHex = 0xFF00ACC1,
        darkHex = 0xFF06B6D4,
        lightHex = 0xFF0891B2,
        neonDarkBgHex = 0xFF021A1F,
        neonDarkSurfaceHex = 0xFF062E36,
        neonDarkSurfaceVariantHex = 0xFF0B4652,
        darkBgHex = 0xFF07171C,
        darkSurfaceHex = 0xFF0E252D,
        darkSurfaceVariantHex = 0xFF153540,
        darkOutlineHex = 0xFF245A6B,
        darkOutlineVariantHex = 0xFF1A4350,
        neonLightBgHex = 0xFFDCFEFF,
        neonLightSurfaceHex = 0xFFF0FEFF,
        neonLightSurfaceVariantHex = 0xFFB8FAFF,
        lightBgHex = 0xFFEBFEFF,
        lightSurfaceHex = 0xFFF5FEFF,
        lightSurfaceVariantHex = 0xFFCFFAFE,
        lightOutlineHex = 0xFF67E8F9,
        lightOutlineVariantHex = 0xFFA5F3FC
    ),
    PINK(
        displayName = "Pink",
        neonDarkHex = 0xFFFF00A0,
        neonLightHex = 0xFFD60087,
        darkHex = 0xFFEC4899,
        lightHex = 0xFFDB2777,
        neonDarkBgHex = 0xFF210315,
        neonDarkSurfaceHex = 0xFF380724,
        neonDarkSurfaceVariantHex = 0xFF540C37,
        darkBgHex = 0xFF1C0B14,
        darkSurfaceHex = 0xFF2C1220,
        darkSurfaceVariantHex = 0xFF3E1A2E,
        darkOutlineHex = 0xFF6B2D50,
        darkOutlineVariantHex = 0xFF50213B,
        neonLightBgHex = 0xFFFFE3F3,
        neonLightSurfaceHex = 0xFFFFF2FA,
        neonLightSurfaceVariantHex = 0xFFFFC7E8,
        lightBgHex = 0xFFFFF0F7,
        lightSurfaceHex = 0xFFFFF7FB,
        lightSurfaceVariantHex = 0xFFFCE7F3,
        lightOutlineHex = 0xFFF9A8D4,
        lightOutlineVariantHex = 0xFFFBCFE8
    );

    fun activePrimaryHex(isDark: Boolean, isNeon: Boolean): Long {
        return when {
            isDark && isNeon -> neonDarkHex
            isDark && !isNeon -> darkHex
            !isDark && isNeon -> neonLightHex
            else -> lightHex
        }
    }

    fun activeBgHex(isDark: Boolean, isNeon: Boolean): Long {
        return when {
            isDark && isNeon -> neonDarkBgHex
            isDark && !isNeon -> darkBgHex
            !isDark && isNeon -> neonLightBgHex
            else -> lightBgHex
        }
    }

    fun activeSurfaceHex(isDark: Boolean, isNeon: Boolean): Long {
        return when {
            isDark && isNeon -> neonDarkSurfaceHex
            isDark && !isNeon -> darkSurfaceHex
            !isDark && isNeon -> neonLightSurfaceHex
            else -> lightSurfaceHex
        }
    }

    fun activeSurfaceVariantHex(isDark: Boolean, isNeon: Boolean): Long {
        return when {
            isDark && isNeon -> neonDarkSurfaceVariantHex
            isDark && !isNeon -> darkSurfaceVariantHex
            !isDark && isNeon -> neonLightSurfaceVariantHex
            else -> lightSurfaceVariantHex
        }
    }

    fun cssHex(isDark: Boolean, isNeon: Boolean = true): String {
        val raw = activePrimaryHex(isDark, isNeon) and 0xFFFFFF
        return String.format(java.util.Locale.US, "#%06x", raw)
    }

    fun cssBgHex(isDark: Boolean, isNeon: Boolean = true): String {
        val raw = activeBgHex(isDark, isNeon) and 0xFFFFFF
        return String.format(java.util.Locale.US, "#%06x", raw)
    }

    fun cssSurfaceHex(isDark: Boolean, isNeon: Boolean = true): String {
        val raw = activeSurfaceHex(isDark, isNeon) and 0xFFFFFF
        return String.format(java.util.Locale.US, "#%06x", raw)
    }
}

data class SearchEngineItem(
    val id: String,
    val displayName: String,
    val queryTemplate: String,
    val isCustom: Boolean = false
) {
    fun formatSearchUrl(rawQuery: String): String {
        val encoded = java.net.URLEncoder.encode(rawQuery.trim(), "UTF-8")
        return if (queryTemplate.contains("%s")) {
            queryTemplate.replace("%s", encoded)
        } else {
            queryTemplate + encoded
        }
    }

    companion object {
        val DUCKDUCKGO = SearchEngineItem("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=%s")
        val GOOGLE = SearchEngineItem("google", "Google", "https://www.google.com/search?q=%s")
        val BING = SearchEngineItem("bing", "Bing", "https://www.bing.com/search?q=%s")
        val BRAVE = SearchEngineItem("brave", "Brave Search", "https://search.brave.com/search?q=%s")

        val DEFAULT_ENGINES = listOf(DUCKDUCKGO, GOOGLE, BING, BRAVE)
    }
}

enum class ShortcutSource(val badgeLabel: String) {
    PINNED("Pinned"),
    MOST_VISITED("Most Visited"),
    TOP_BOOKMARK("Top Bookmark")
}

data class HomeShortcut(
    val id: String,
    val title: String,
    val url: String,
    val isPinned: Boolean = true,
    val source: ShortcutSource = ShortcutSource.PINNED,
    val count: Int = 1
)

enum class DevToolsPanelTab(val label: String) {
    ELEMENTS("Elements"),
    CONSOLE("Console"),
    NETWORK("Network"),
    APPLICATION("Storage"),
    PERFORMANCE("Audit"),
    SOURCES("Sources")
}

data class BoxModelMetrics(
    val width: Int = 0,
    val height: Int = 0,
    val marginTop: Int = 0,
    val marginRight: Int = 0,
    val marginBottom: Int = 0,
    val marginLeft: Int = 0,
    val borderTop: Int = 0,
    val borderRight: Int = 0,
    val borderBottom: Int = 0,
    val borderLeft: Int = 0,
    val paddingTop: Int = 0,
    val paddingRight: Int = 0,
    val paddingBottom: Int = 0,
    val paddingLeft: Int = 0
)

data class DomNodeItem(
    val path: String,
    val cssSelector: String = "",
    val tag: String,
    val idAttr: String = "",
    val classAttr: String = "",
    val attributes: Map<String, String> = emptyMap(),
    val textPreview: String = "",
    val childCount: Int = 0,
    val depth: Int = 0,
    val inlineStyle: String = "",
    val computedStyles: Map<String, String> = emptyMap(),
    val boxModel: BoxModelMetrics = BoxModelMetrics(),
    val outerHtmlPreview: String = ""
)

enum class ConsoleLogLevel(val badge: String) {
    LOG("LOG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERR"),
    COMMAND(">"),
    RESULT("<")
}

data class ConsoleLogEntry(
    val id: Long,
    val level: ConsoleLogLevel,
    val message: String,
    val source: String = "console",
    val timestamp: String,
    val count: Int = 1
)

enum class NetworkResourceType(val label: String) {
    ALL("All"),
    FETCH_XHR("XHR"),
    DOC("Doc"),
    JS("JS"),
    CSS("CSS"),
    IMG("Img"),
    OTHER("Other")
}

data class NetworkRequestEntry(
    val id: String,
    val method: String,
    val url: String,
    val status: Int,
    val statusText: String,
    val resourceType: NetworkResourceType,
    val mimeType: String = "text/plain",
    val startTimeOffsetMs: Long = 0L,
    val durationMs: Long = 0L,
    val sizeBytes: Int = 0,
    val requestHeaders: Map<String, String> = emptyMap(),
    val responseHeaders: Map<String, String> = emptyMap(),
    val requestBody: String = "",
    val responsePreview: String = "",
    val isError: Boolean = false
)

enum class StorageType(val label: String) {
    LOCAL_STORAGE("Local"),
    SESSION_STORAGE("Session"),
    COOKIES("Cookies")
}

data class StorageEntry(
    val key: String,
    val value: String,
    val type: StorageType
)

data class AuditItem(
    val category: String,
    val title: String,
    val description: String,
    val passed: Boolean,
    val metricValue: String = ""
)

data class PerformanceMetrics(
    val domNodesCount: Int = 0,
    val maxDomDepth: Int = 0,
    val scriptsCount: Int = 0,
    val stylesheetsCount: Int = 0,
    val imagesCount: Int = 0,
    val missingAltImagesCount: Int = 0,
    val domContentLoadedMs: Long = 0L,
    val loadEventMs: Long = 0L,
    val firstPaintMs: Long = 0L,
    val firstContentfulPaintMs: Long = 0L,
    val jsHeapUsedMb: Float = 0f,
    val jsHeapTotalMb: Float = 0f,
    val performanceScore: Int = 96,
    val accessibilityScore: Int = 92,
    val bestPracticesScore: Int = 95,
    val seoScore: Int = 94,
    val audits: List<AuditItem> = emptyList()
)

data class JsSnippet(
    val id: String,
    val title: String,
    val description: String,
    val code: String
)

enum class DeviceViewportPreset(
    val displayName: String,
    val widthDp: Int?,
    val heightDp: Int?,
    val userAgent: String,
    val badge: String
) {
    RESPONSIVE(
        displayName = "Fit Screen",
        widthDp = null,
        heightDp = null,
        userAgent = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        badge = "Fit"
    ),
    PIXEL_9(
        displayName = "Pixel 9 (360×740)",
        widthDp = 360,
        heightDp = 740,
        userAgent = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        badge = "360×740"
    ),
    IPHONE_16(
        displayName = "iPhone 16 (340×700)",
        widthDp = 340,
        heightDp = 700,
        userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1",
        badge = "340×700"
    ),
    IPAD_MINI(
        displayName = "Tablet (520×680)",
        widthDp = 520,
        heightDp = 680,
        userAgent = "Mozilla/5.0 (iPad; CPU OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1",
        badge = "520×680"
    ),
    DESKTOP_CHROME(
        displayName = "Desktop UA",
        widthDp = null,
        heightDp = null,
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        badge = "Desktop"
    )
}

data class QuickBookmark(
    val title: String,
    val url: String,
    val category: String
)

data class HistoryItem(
    val id: Long,
    val title: String,
    val url: String,
    val timestamp: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class ClearDataTimeRange(val displayName: String, val durationMillis: Long) {
    LAST_HOUR("Last hour", 60 * 60 * 1000L),
    LAST_24_HOURS("Last 24 hours", 24 * 60 * 60 * 1000L),
    LAST_7_DAYS("Last 7 days", 7 * 24 * 60 * 60 * 1000L),
    LAST_4_WEEKS("Last 4 weeks", 28L * 24 * 60 * 60 * 1000L),
    ALL_TIME("All time", Long.MAX_VALUE)
}

data class SiteDataItem(
    val domain: String,
    val historyCount: Int,
    val visitCount: Int,
    val lastVisited: String
)

enum class SettingsCategory(
    val categoryId: String,
    val displayName: String,
    val subtitle: String,
    val url: String
) {
    THEME(
        categoryId = "theme",
        displayName = "Theme & Colors",
        subtitle = "Light and dark mode, neon accents, color palettes",
        url = BrowserTab.URL_SETTINGS_THEME
    ),
    SEARCH(
        categoryId = "search",
        displayName = "Search Engine",
        subtitle = "Default search provider, query templates, custom engines",
        url = BrowserTab.URL_SETTINGS_SEARCH
    ),
    DATA(
        categoryId = "data",
        displayName = "Site Data & Privacy",
        subtitle = "Cookies, cache, browsing history, and per-site data",
        url = BrowserTab.URL_SETTINGS_DATA
    ),
    VIRUSTOTAL(
        categoryId = "virustotal",
        displayName = "VirusTotal Security",
        subtitle = "Malware & phishing scan, API key, shield status",
        url = BrowserTab.URL_SETTINGS_VIRUSTOTAL
    ),
    ABOUT(
        categoryId = "about",
        displayName = "About DevBrowser",
        subtitle = "Version v0.1, GitHub repository, browser engine",
        url = BrowserTab.URL_SETTINGS_ABOUT
    );

    companion object {
        fun fromUrl(rawUrl: String): SettingsCategory? {
            val lower = rawUrl.lowercase(java.util.Locale.US).trim()
            return entries.firstOrNull {
                lower == it.url || lower == "devbrowser:settings/${it.categoryId}" || lower.endsWith("/${it.categoryId}")
            }
        }
    }
}
