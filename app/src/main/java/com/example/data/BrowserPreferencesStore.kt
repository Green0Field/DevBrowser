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
import com.example.model.VirusTotalRescanInterval
import com.example.model.VirusTotalScanResult
import com.example.model.VirusTotalThreatLevel
import com.example.model.UserScript
import com.example.model.UserScriptUpdateInterval
import com.example.model.AiProtectionScanResult
import com.example.model.AiThreatCategory
import com.example.model.GeminiModelChoice
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

        val vtEnabled = prefs.getBoolean(KEY_VIRUSTOTAL_ENABLED, baseState.isVirusTotalEnabled)
        val vtApiKey = prefs.getString(KEY_VIRUSTOTAL_API_KEY, baseState.virusTotalApiKey) ?: ""
        val vtIntervalName = prefs.getString(KEY_VIRUSTOTAL_RESCAN_INTERVAL, baseState.virusTotalRescanInterval.name)
        val vtInterval = runCatching { VirusTotalRescanInterval.valueOf(vtIntervalName ?: "") }.getOrDefault(VirusTotalRescanInterval.HOURS_24)
        val vtScans = loadVirusTotalScanResults()

        val userScripts = loadUserScripts() ?: baseState.userScripts
        val scriptIntervalName = prefs.getString(KEY_USER_SCRIPT_UPDATE_INTERVAL, baseState.userScriptUpdateInterval.name)
        val scriptInterval = runCatching { UserScriptUpdateInterval.valueOf(scriptIntervalName ?: "") }.getOrDefault(UserScriptUpdateInterval.HOURS_24)

        val aiMasterEnabled = prefs.getBoolean(KEY_AI_MASTER_ENABLED, baseState.isAiMasterEnabled)
        val geminiKey = prefs.getString(KEY_GEMINI_API_KEY, baseState.geminiApiKey) ?: ""
        val geminiModelId = prefs.getString(KEY_GEMINI_MODEL, baseState.selectedGeminiModel.id)
        val selectedModel = GeminiModelChoice.fromId(geminiModelId)
        val aiProtectionEnabled = prefs.getBoolean(KEY_AI_PROTECTION_ENABLED, baseState.isAiProtectionEnabled)
        val aiSummaryEnabled = prefs.getBoolean(KEY_AI_SUMMARY_ENABLED, baseState.isAiSummaryEnabled)
        val aiTranslateEnabled = prefs.getBoolean(KEY_AI_TRANSLATE_ENABLED, baseState.isAiTranslateEnabled)
        val aiScans = loadAiScanResults()

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
            history = history,
            isVirusTotalEnabled = vtEnabled,
            virusTotalApiKey = vtApiKey,
            virusTotalRescanInterval = vtInterval,
            virusTotalScanCache = vtScans,
            userScripts = userScripts,
            userScriptUpdateInterval = scriptInterval,
            isAiMasterEnabled = aiMasterEnabled,
            geminiApiKey = geminiKey,
            selectedGeminiModel = selectedModel,
            isAiProtectionEnabled = aiProtectionEnabled,
            isAiSummaryEnabled = aiSummaryEnabled,
            isAiTranslateEnabled = aiTranslateEnabled,
            aiScanCache = aiScans
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
                    put("createdAtMillis", item.createdAtMillis)
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
                    val createdAt = obj.optLong("createdAtMillis", System.currentTimeMillis())
                    add(HistoryItem(id = id, title = title, url = url, timestamp = timestamp, createdAtMillis = createdAt))
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

    fun saveVirusTotalEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIRUSTOTAL_ENABLED, enabled).commit()
    }

    fun saveVirusTotalApiKey(apiKey: String) {
        prefs.edit().putString(KEY_VIRUSTOTAL_API_KEY, apiKey).commit()
    }

    fun saveVirusTotalRescanInterval(interval: VirusTotalRescanInterval) {
        prefs.edit().putString(KEY_VIRUSTOTAL_RESCAN_INTERVAL, interval.name).commit()
    }

    fun saveVirusTotalScanResults(results: Map<String, VirusTotalScanResult>) {
        val root = JSONObject()
        results.forEach { (key, scan) ->
            val obj = JSONObject().apply {
                put("url", scan.url)
                put("domain", scan.domain)
                put("malicious", scan.maliciousCount)
                put("suspicious", scan.suspiciousCount)
                put("harmless", scan.harmlessCount)
                put("undetected", scan.undetectedCount)
                put("totalEngines", scan.totalEngines)
                put("timestamp", scan.scanTimestamp)
                put("threatLevel", scan.threatLevel.name)
                put("permalink", scan.permalink ?: "")
                put("dateFormatted", scan.scanDateFormatted)
                put("errorMessage", scan.errorMessage ?: "")
            }
            root.put(key, obj)
        }
        prefs.edit().putString(KEY_VIRUSTOTAL_SCANS_JSON, root.toString()).commit()
    }

    fun loadVirusTotalScanResults(): Map<String, VirusTotalScanResult> {
        val raw = prefs.getString(KEY_VIRUSTOTAL_SCANS_JSON, null) ?: return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            val map = mutableMapOf<String, VirusTotalScanResult>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = root.optJSONObject(key) ?: continue
                val threatLevelName = obj.optString("threatLevel", VirusTotalThreatLevel.SAFE.name)
                val threatLevel = runCatching { VirusTotalThreatLevel.valueOf(threatLevelName) }.getOrDefault(VirusTotalThreatLevel.SAFE)
                val permalink = obj.optString("permalink", "").takeIf { it.isNotEmpty() }
                val errorMsg = obj.optString("errorMessage", "").takeIf { it.isNotEmpty() }

                map[key] = VirusTotalScanResult(
                    url = obj.optString("url", ""),
                    domain = obj.optString("domain", ""),
                    maliciousCount = obj.optInt("malicious", 0),
                    suspiciousCount = obj.optInt("suspicious", 0),
                    harmlessCount = obj.optInt("harmless", 0),
                    undetectedCount = obj.optInt("undetected", 0),
                    totalEngines = obj.optInt("totalEngines", 0),
                    scanTimestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    threatLevel = threatLevel,
                    permalink = permalink,
                    scanDateFormatted = obj.optString("dateFormatted", ""),
                    errorMessage = errorMsg
                )
            }
            map
        }.getOrDefault(emptyMap())
    }

    fun clearVirusTotalScanResults() {
        prefs.edit().remove(KEY_VIRUSTOTAL_SCANS_JSON).commit()
    }

    fun saveUserScripts(scripts: List<UserScript>) {
        val array = JSONArray()
        scripts.forEach { script ->
            val obj = JSONObject().apply {
                put("id", script.id)
                put("name", script.name)
                put("code", script.code)
                put("enabled", script.enabled)
                put("updateUrl", script.updateUrl)
                put("lastUpdated", script.lastUpdated)
                val matches = JSONArray()
                script.matchPatterns.forEach { matches.put(it) }
                put("matchPatterns", matches)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_USER_SCRIPTS_JSON, array.toString()).commit()
    }

    fun loadUserScripts(): List<UserScript>? {
        val raw = prefs.getString(KEY_USER_SCRIPTS_JSON, null) ?: return null
        return runCatching {
            val array = JSONArray(raw)
            val list = mutableListOf<UserScript>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val matches = mutableListOf<String>()
                val matchesArr = obj.optJSONArray("matchPatterns")
                if (matchesArr != null) {
                    for (j in 0 until matchesArr.length()) {
                        matches.add(matchesArr.optString(j, "*"))
                    }
                }
                if (matches.isEmpty()) matches.add("*")

                list.add(
                    UserScript(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", "Unnamed Script"),
                        code = obj.optString("code", ""),
                        enabled = obj.optBoolean("enabled", true),
                        updateUrl = obj.optString("updateUrl", ""),
                        lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis()),
                        matchPatterns = matches
                    )
                )
            }
            list
        }.getOrNull()
    }

    fun saveUserScriptUpdateInterval(interval: UserScriptUpdateInterval) {
        prefs.edit().putString(KEY_USER_SCRIPT_UPDATE_INTERVAL, interval.name).commit()
    }

    fun saveAiMasterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_MASTER_ENABLED, enabled).commit()
    }

    fun saveGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, key.trim()).commit()
    }

    fun saveAiProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_PROTECTION_ENABLED, enabled).commit()
    }

    fun saveAiSummaryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_SUMMARY_ENABLED, enabled).commit()
    }

    fun saveAiTranslateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_TRANSLATE_ENABLED, enabled).commit()
    }

    fun saveGeminiModel(model: GeminiModelChoice) {
        prefs.edit().putString(KEY_GEMINI_MODEL, model.id).commit()
    }

    fun saveAiScanResults(scans: Map<String, AiProtectionScanResult>) {
        val root = JSONObject()
        scans.forEach { (key, scan) ->
            val obj = JSONObject().apply {
                put("url", scan.url)
                put("domain", scan.domain)
                put("category", scan.category.name)
                put("reasoning", scan.reasoning)
                scan.modelUsed?.let { put("modelUsed", it) }
                put("timestamp", scan.timestamp)
            }
            root.put(key, obj)
        }
        prefs.edit().putString(KEY_AI_SCANS_JSON, root.toString()).commit()
    }

    fun loadAiScanResults(): Map<String, AiProtectionScanResult> {
        val raw = prefs.getString(KEY_AI_SCANS_JSON, null) ?: return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            val map = mutableMapOf<String, AiProtectionScanResult>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = root.optJSONObject(key) ?: continue
                val catName = obj.optString("category", AiThreatCategory.SAFE.name)
                val cat = runCatching { AiThreatCategory.valueOf(catName) }.getOrDefault(AiThreatCategory.SAFE)
                map[key] = AiProtectionScanResult(
                    url = obj.optString("url", key),
                    domain = obj.optString("domain", ""),
                    category = cat,
                    reasoning = obj.optString("reasoning", ""),
                    modelUsed = if (obj.has("modelUsed")) obj.optString("modelUsed") else null,
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            }
            map
        }.getOrDefault(emptyMap())
    }

    fun clearAiScanResults() {
        prefs.edit().remove(KEY_AI_SCANS_JSON).commit()
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
        private const val KEY_VIRUSTOTAL_ENABLED = "virustotal_enabled"
        private const val KEY_VIRUSTOTAL_API_KEY = "virustotal_api_key"
        private const val KEY_VIRUSTOTAL_RESCAN_INTERVAL = "virustotal_rescan_interval"
        private const val KEY_VIRUSTOTAL_SCANS_JSON = "virustotal_scans_json"
        private const val KEY_USER_SCRIPTS_JSON = "user_scripts_json"
        private const val KEY_USER_SCRIPT_UPDATE_INTERVAL = "user_script_update_interval"
        private const val KEY_AI_MASTER_ENABLED = "ai_master_enabled"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_AI_PROTECTION_ENABLED = "ai_protection_enabled"
        private const val KEY_AI_SUMMARY_ENABLED = "ai_summary_enabled"
        private const val KEY_AI_TRANSLATE_ENABLED = "ai_translate_enabled"
        private const val KEY_AI_SCANS_JSON = "ai_scans_json"
    }
}
