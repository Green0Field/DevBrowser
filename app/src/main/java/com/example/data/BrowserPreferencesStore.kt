package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.devtools.DevToolsScripts
import com.example.model.BrowserTab
import com.example.model.HistoryItem
import com.example.model.HomeShortcut
import com.example.model.QuickBookmark
import com.example.model.SearchEngineItem
import com.example.model.ShortcutSource
import com.example.model.ThemeAccentColor
import com.example.viewmodel.BrowserUiState
import org.json.JSONArray
import org.json.JSONObject

class BrowserPreferencesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadInitialState(baseState: BrowserUiState = BrowserUiState()): BrowserUiState {
        val isDark = prefs.getBoolean(KEY_IS_DARK_THEME, baseState.isDarkTheme)
        val isNeon = prefs.getBoolean(KEY_IS_NEON_MODE, baseState.isNeonMode)
        val accentName = prefs.getString(KEY_ACCENT_COLOR, baseState.accentColor.name)
        val accent = ThemeAccentColor.entries.find { it.name == accentName } ?: baseState.accentColor
        val jsEnabled = prefs.getBoolean(KEY_JAVASCRIPT_ENABLED, baseState.javaScriptEnabled)

        val customEngines = loadCustomSearchEngines()
        val allEngines = SearchEngineItem.DEFAULT_ENGINES + customEngines
        val activeEngineId = prefs.getString(KEY_ACTIVE_SEARCH_ENGINE_ID, baseState.searchEngine.id)
        val activeEngine = allEngines.find { it.id == activeEngineId } ?: SearchEngineItem.DUCKDUCKGO

        val bookmarks = loadBookmarks() ?: baseState.bookmarks
        val pinnedShortcuts = loadPinnedShortcuts() ?: baseState.pinnedShortcuts
        val dismissedUrls = prefs.getStringSet(KEY_DISMISSED_SHORTCUTS, null)?.toSet()
            ?: baseState.dismissedAutoShortcutUrls
        val visitCounts = loadIntMap(KEY_SITE_VISIT_COUNTS) ?: baseState.siteVisitCounts
        val clickCounts = loadIntMap(KEY_BOOKMARK_CLICK_COUNTS) ?: baseState.bookmarkClickCounts
        val siteTitles = loadStringMap(KEY_SITE_TITLES) ?: baseState.siteTitles
        val history = loadHistory() ?: baseState.history

        return baseState.copy(
            isDarkTheme = isDark,
            isNeonMode = isNeon,
            accentColor = accent,
            javaScriptEnabled = jsEnabled,
            searchEngines = allEngines,
            searchEngine = activeEngine,
            bookmarks = bookmarks,
            pinnedShortcuts = pinnedShortcuts,
            dismissedAutoShortcutUrls = dismissedUrls,
            siteVisitCounts = visitCounts,
            bookmarkClickCounts = clickCounts,
            siteTitles = siteTitles,
            history = history
        )
    }

    fun saveDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_THEME, isDark).commit()
    }

    fun saveNeonMode(isNeon: Boolean) {
        prefs.edit().putBoolean(KEY_IS_NEON_MODE, isNeon).commit()
    }

    fun saveAccentColor(color: ThemeAccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, color.name).commit()
    }

    fun saveJavaScriptEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_JAVASCRIPT_ENABLED, enabled).commit()
    }

    fun saveSearchEngines(allEngines: List<SearchEngineItem>, activeEngine: SearchEngineItem) {
        val customArray = JSONArray()
        allEngines.filter { it.isCustom }.forEach { engine ->
            customArray.put(
                JSONObject().apply {
                    put("id", engine.id)
                    put("displayName", engine.displayName)
                    put("queryTemplate", engine.queryTemplate)
                }
            )
        }
        prefs.edit()
            .putString(KEY_CUSTOM_SEARCH_ENGINES_JSON, customArray.toString())
            .putString(KEY_ACTIVE_SEARCH_ENGINE_ID, activeEngine.id)
            .commit()
    }

    fun saveBookmarks(bookmarks: List<QuickBookmark>) {
        val arr = JSONArray()
        bookmarks.forEach { bm ->
            arr.put(
                JSONObject().apply {
                    put("title", bm.title)
                    put("url", bm.url)
                    put("category", bm.category)
                }
            )
        }
        prefs.edit().putString(KEY_BOOKMARKS_JSON, arr.toString()).apply()
    }

    fun saveShortcutsState(
        pinnedShortcuts: List<HomeShortcut>,
        dismissedUrls: Set<String>,
        visitCounts: Map<String, Int>,
        clickCounts: Map<String, Int>,
        siteTitles: Map<String, String>
    ) {
        val pinnedArr = JSONArray()
        pinnedShortcuts.forEach { item ->
            pinnedArr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("url", item.url)
                    put("count", item.count)
                }
            )
        }
        prefs.edit()
            .putString(KEY_PINNED_SHORTCUTS_JSON, pinnedArr.toString())
            .putStringSet(KEY_DISMISSED_SHORTCUTS, HashSet(dismissedUrls))
            .putString(KEY_SITE_VISIT_COUNTS, encodeIntMap(visitCounts))
            .putString(KEY_BOOKMARK_CLICK_COUNTS, encodeIntMap(clickCounts))
            .putString(KEY_SITE_TITLES, encodeStringMap(siteTitles))
            .apply()
    }

    fun saveHistory(history: List<HistoryItem>) {
        val arr = JSONArray()
        history.take(80).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("url", item.url)
                    put("timestamp", item.timestamp)
                }
            )
        }
        prefs.edit().putString(KEY_HISTORY_JSON, arr.toString()).apply()
    }

    private fun loadCustomSearchEngines(): List<SearchEngineItem> {
        val raw = prefs.getString(KEY_CUSTOM_SEARCH_ENGINES_JSON, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    val name = obj.optString("displayName")
                    val template = obj.optString("queryTemplate")
                    if (id.isNotBlank() && name.isNotBlank() && template.isNotBlank()) {
                        add(SearchEngineItem(id = id, displayName = name, queryTemplate = template, isCustom = true))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun loadBookmarks(): List<QuickBookmark>? {
        val raw = prefs.getString(KEY_BOOKMARKS_JSON, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val title = obj.optString("title")
                    val url = obj.optString("url")
                    val category = obj.optString("category", "Saved")
                    if (url.isNotBlank()) {
                        add(QuickBookmark(title = title.ifBlank { url }, url = url, category = category))
                    }
                }
            }
        }.getOrNull()
    }

    private fun loadPinnedShortcuts(): List<HomeShortcut>? {
        val raw = prefs.getString(KEY_PINNED_SHORTCUTS_JSON, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id", "pinned_$i")
                    val title = obj.optString("title")
                    val url = obj.optString("url")
                    val count = obj.optInt("count", 1)
                    if (url.isNotBlank()) {
                        add(
                            HomeShortcut(
                                id = id,
                                title = title.ifBlank { url },
                                url = url,
                                isPinned = true,
                                source = ShortcutSource.PINNED,
                                count = count
                            )
                        )
                    }
                }
            }
        }.getOrNull()
    }

    private fun loadHistory(): List<HistoryItem>? {
        val raw = prefs.getString(KEY_HISTORY_JSON, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optLong("id", (i + 1).toLong())
                    val title = obj.optString("title", "Home")
                    val url = obj.optString("url", BrowserTab.URL_HOME)
                    val timestamp = obj.optString("timestamp", "")
                    add(HistoryItem(id = id, title = title, url = url, timestamp = timestamp))
                }
            }
        }.getOrNull()
    }

    private fun encodeIntMap(map: Map<String, Int>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    private fun loadIntMap(key: String): Map<String, Int>? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching {
            val obj = JSONObject(raw)
            val result = mutableMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = obj.optInt(k, 0)
            }
            result
        }.getOrNull()
    }

    private fun encodeStringMap(map: Map<String, String>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    private fun loadStringMap(key: String): Map<String, String>? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching {
            val obj = JSONObject(raw)
            val result = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = obj.optString(k, "")
            }
            result
        }.getOrNull()
    }

    companion object {
        private const val PREFS_NAME = "devbrowser_user_preferences"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_IS_NEON_MODE = "is_neon_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_JAVASCRIPT_ENABLED = "javascript_enabled"
        private const val KEY_ACTIVE_SEARCH_ENGINE_ID = "active_search_engine_id"
        private const val KEY_CUSTOM_SEARCH_ENGINES_JSON = "custom_search_engines_json"
        private const val KEY_BOOKMARKS_JSON = "bookmarks_json"
        private const val KEY_PINNED_SHORTCUTS_JSON = "pinned_shortcuts_json"
        private const val KEY_DISMISSED_SHORTCUTS = "dismissed_auto_shortcuts"
        private const val KEY_SITE_VISIT_COUNTS = "site_visit_counts_json"
        private const val KEY_BOOKMARK_CLICK_COUNTS = "bookmark_click_counts_json"
        private const val KEY_SITE_TITLES = "site_titles_json"
        private const val KEY_HISTORY_JSON = "history_json"
    }
}
