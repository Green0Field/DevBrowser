package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.devtools.DevToolsScripts
import com.example.model.BrowserTab
import com.example.model.ConsoleLogLevel
import com.example.model.DevToolsPanelTab
import com.example.model.DomNodeItem
import com.example.model.ShortcutSource
import com.example.model.ThemeAccentColor
import com.example.model.SettingsCategory
import com.example.model.VirusTotalRescanInterval
import com.example.model.VirusTotalScanResult
import com.example.model.VirusTotalThreatLevel
import com.example.model.AiThreatCategory
import com.example.model.AiProtectionScanResult
import com.example.viewmodel.BrowserDevToolsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DevBrowser", appName)
    }

    @Test
    fun `default tab opens devbrowser home and routes internal pages`() {
        val viewModel = BrowserDevToolsViewModel()
        assertEquals(BrowserTab.URL_HOME, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_TEST)
        assertEquals(BrowserTab.URL_TEST, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_BOOKMARKS)
        assertEquals(BrowserTab.URL_BOOKMARKS, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_HISTORY)
        assertEquals(BrowserTab.URL_HISTORY, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_SETTINGS)
        assertEquals(BrowserTab.URL_SETTINGS, viewModel.uiState.value.activeTab.url)
    }

    @Test
    fun `viewmodel switches themes and records console logs`() {
        val viewModel = BrowserDevToolsViewModel()
        assertTrue(viewModel.uiState.value.isDarkTheme)
        assertTrue(viewModel.uiState.value.isNeonMode)
        assertEquals(ThemeAccentColor.BLUE, viewModel.uiState.value.accentColor)

        viewModel.setDarkTheme(false)
        viewModel.setNeonMode(false)
        viewModel.setThemeAccentColor(ThemeAccentColor.PURPLE)
        assertFalse(viewModel.uiState.value.isDarkTheme)
        assertFalse(viewModel.uiState.value.isNeonMode)
        assertEquals(ThemeAccentColor.PURPLE, viewModel.uiState.value.accentColor)
        assertNotEquals(ThemeAccentColor.BLUE.darkBgHex, ThemeAccentColor.PURPLE.darkBgHex)
        assertNotEquals(ThemeAccentColor.BLUE.lightBgHex, ThemeAccentColor.PURPLE.lightBgHex)
        assertNotEquals(
            ThemeAccentColor.PURPLE.activePrimaryHex(isDark = true, isNeon = true),
            ThemeAccentColor.PURPLE.activePrimaryHex(isDark = true, isNeon = false)
        )
        assertNotEquals(
            ThemeAccentColor.PURPLE.activeBgHex(isDark = true, isNeon = true),
            ThemeAccentColor.PURPLE.activeBgHex(isDark = true, isNeon = false)
        )

        val lightScript = DevToolsScripts.buildColorSchemeSyncScript(
            isDark = false,
            accentHex = ThemeAccentColor.PURPLE.cssHex(false),
            bgHex = ThemeAccentColor.PURPLE.cssBgHex(false),
            surfaceHex = ThemeAccentColor.PURPLE.cssSurfaceHex(false)
        )
        assertTrue(lightScript.contains("var scheme = 'light'"))
        assertTrue(lightScript.contains("prefers-color-scheme"))

        viewModel.selectDevToolsTab(DevToolsPanelTab.CONSOLE)
        assertEquals(DevToolsPanelTab.CONSOLE, viewModel.uiState.value.activeDevToolsTab)

        viewModel.onBridgeConsoleLog(ConsoleLogLevel.ERROR, "SyntaxError: Unexpected token", "test.js")
        assertTrue(viewModel.uiState.value.consoleLogs.isNotEmpty())
        assertEquals(1, viewModel.uiState.value.errorCount)
    }

    @Test
    fun `custom search engines and shortcuts and hide dom node work properly`() {
        val viewModel = BrowserDevToolsViewModel()

        // 1. Test page HTML has no header nav bar
        assertFalse(DevToolsScripts.PLAYGROUND_HTML.contains("<header id=\"site-header\""))

        // 2. Custom search engine addition
        viewModel.addCustomSearchEngine("Startpage", "https://www.startpage.com/sp/search?query=%s")
        assertEquals("Startpage", viewModel.uiState.value.searchEngine.displayName)
        assertTrue(viewModel.uiState.value.searchEngine.isCustom)
        assertEquals(
            "https://www.startpage.com/sp/search?query=kotlin+compose",
            viewModel.uiState.value.searchEngine.formatSearchUrl("kotlin compose")
        )

        // 3. Shortcuts include pinned, most visited, and top bookmarks, and auto shortcuts can be pinned
        val initialShortcuts = viewModel.uiState.value.homeShortcuts
        assertTrue(initialShortcuts.any { it.isPinned && it.source == ShortcutSource.PINNED })
        val autoVisited = initialShortcuts.first { !it.isPinned && it.source == ShortcutSource.MOST_VISITED }
        viewModel.pinShortcut(autoVisited)
        assertTrue(viewModel.uiState.value.pinnedShortcuts.any { it.url == autoVisited.url && it.isPinned })

        // Add custom shortcut
        viewModel.addCustomShortcut("GitHub", "https://github.com")
        assertTrue(viewModel.uiState.value.homeShortcuts.any { it.title == "GitHub" && it.isPinned })

        // 4. Hide DOM element applies display: none !important;
        val sampleNode = DomNodeItem(
            path = "node_1",
            cssSelector = "section#customizable-box.card",
            tag = "section",
            idAttr = "customizable-box",
            inlineStyle = "padding: 18px;"
        )
        viewModel.onBridgeDomTreeUpdated(listOf(sampleNode))
        viewModel.hideDomNode("node_1")
        val updatedNode = viewModel.uiState.value.selectedDomNode!!
        assertTrue(updatedNode.inlineStyle.contains("display: none !important;"))
        assertEquals("none", updatedNode.computedStyles["display"])
    }

    @Test
    fun `theme preferences persist across closing and reopening app`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("devbrowser_user_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        // First session: user changes Dark Mode -> Light Mode, Neon Mode -> Off, Color -> GREEN
        val firstSessionVm = BrowserDevToolsViewModel(context)
        assertTrue(firstSessionVm.uiState.value.isDarkTheme)
        assertTrue(firstSessionVm.uiState.value.isNeonMode)
        assertEquals(ThemeAccentColor.BLUE, firstSessionVm.uiState.value.accentColor)

        firstSessionVm.setDarkTheme(false)
        firstSessionVm.setNeonMode(false)
        firstSessionVm.setThemeAccentColor(ThemeAccentColor.GREEN)
        firstSessionVm.addCustomSearchEngine("Kagi", "https://kagi.com/search?q=%s")

        // Simulate closing and reopening the app with a brand new ViewModel instance
        val reopenedSessionVm = BrowserDevToolsViewModel(context)
        assertFalse(reopenedSessionVm.uiState.value.isDarkTheme)
        assertFalse(reopenedSessionVm.uiState.value.isNeonMode)
        assertEquals(ThemeAccentColor.GREEN, reopenedSessionVm.uiState.value.accentColor)
        assertEquals("Kagi", reopenedSessionVm.uiState.value.searchEngine.displayName)
    }

    @Test
    fun `settings categories routing and data clearing by website and timerange`() {
        val viewModel = BrowserDevToolsViewModel()

        // Test Category URL routing
        viewModel.navigateToInput(BrowserTab.URL_SETTINGS)
        assertEquals(BrowserTab.URL_SETTINGS, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_SETTINGS_THEME)
        assertEquals(BrowserTab.URL_SETTINGS_THEME, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_SETTINGS_SEARCH)
        assertEquals(BrowserTab.URL_SETTINGS_SEARCH, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_SETTINGS_DATA)
        assertEquals(BrowserTab.URL_SETTINGS_DATA, viewModel.uiState.value.activeTab.url)

        viewModel.navigateToInput(BrowserTab.URL_SETTINGS_ABOUT)
        assertEquals(BrowserTab.URL_SETTINGS_ABOUT, viewModel.uiState.value.activeTab.url)

        // Verify SettingsCategory enum helper
        assertEquals(com.example.model.SettingsCategory.THEME, com.example.model.SettingsCategory.fromUrl("devbrowser:settings/theme"))
        assertEquals(com.example.model.SettingsCategory.SEARCH, com.example.model.SettingsCategory.fromUrl("devbrowser:settings/search"))
        assertEquals(com.example.model.SettingsCategory.DATA, com.example.model.SettingsCategory.fromUrl("devbrowser:settings/data"))
        assertEquals(com.example.model.SettingsCategory.ABOUT, com.example.model.SettingsCategory.fromUrl("devbrowser:settings/about"))
        assertEquals(com.example.model.SettingsCategory.USERSCRIPTS, com.example.model.SettingsCategory.fromUrl("devbrowser:settings/userscripts"))
        assertEquals(com.example.model.SettingsCategory.USERSCRIPTS, com.example.model.SettingsCategory.fromUrl("devbrowser:settings/userscripts/edit?file=123"))
        assertEquals(null, com.example.model.SettingsCategory.fromUrl("devbrowser:settings"))

        // Add history visits for multiple domains
        viewModel.navigateToInput("https://github.com/Green0Field/DevBrowser")
        viewModel.onPageFinished("https://github.com/Green0Field/DevBrowser", "DevBrowser Repo", false, false)

        viewModel.navigateToInput("https://google.com")
        viewModel.onPageFinished("https://google.com", "Google", false, false)

        val initialHistory = viewModel.uiState.value.history
        assertTrue(initialHistory.any { it.url.contains("github.com") })
        assertTrue(initialHistory.any { it.url.contains("google.com") })

        // Clear data ONLY for google.com
        viewModel.clearBrowsingData(
            targetDomain = "google.com",
            timeRange = com.example.model.ClearDataTimeRange.ALL_TIME,
            deleteHistory = true,
            deleteCookies = true,
            deleteCache = false,
            deleteStorage = true
        )

        val updatedHistory = viewModel.uiState.value.history
        assertFalse(updatedHistory.any { it.url.contains("google.com") })
        assertTrue(updatedHistory.any { it.url.contains("github.com") })

        // Clear ALL data
        viewModel.clearAllBrowserSiteData()
        assertTrue(viewModel.uiState.value.history.isEmpty())
    }

    @Test
    fun `virustotal categorization, persistence and warning interception`() {
        // Test categorization rules
        assertEquals(VirusTotalThreatLevel.SAFE, VirusTotalScanResult.calculateThreatLevel(0))
        assertEquals(VirusTotalThreatLevel.SAFE, VirusTotalScanResult.calculateThreatLevel(3))
        assertEquals(VirusTotalThreatLevel.CAREFUL, VirusTotalScanResult.calculateThreatLevel(4))
        assertEquals(VirusTotalThreatLevel.CAREFUL, VirusTotalScanResult.calculateThreatLevel(8))
        assertEquals(VirusTotalThreatLevel.DANGER, VirusTotalScanResult.calculateThreatLevel(9))
        assertEquals(VirusTotalThreatLevel.DANGER, VirusTotalScanResult.calculateThreatLevel(25))

        // Test settings category routing
        assertEquals(SettingsCategory.VIRUSTOTAL, SettingsCategory.fromUrl("devbrowser:settings/virustotal"))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = BrowserDevToolsViewModel(context)
        viewModel.attachPreferencesIfNeeded(context)

        // Configure VirusTotal settings
        viewModel.setVirusTotalEnabled(true)
        viewModel.setVirusTotalApiKey("test-fake-vt-api-key")
        viewModel.setVirusTotalRescanInterval(VirusTotalRescanInterval.HOURS_6)

        assertTrue(viewModel.uiState.value.isVirusTotalEnabled)
        assertEquals("test-fake-vt-api-key", viewModel.uiState.value.virusTotalApiKey)
        assertEquals(VirusTotalRescanInterval.HOURS_6, viewModel.uiState.value.virusTotalRescanInterval)

        // Simulate a cached scan for a dangerous site (12 flags)
        val dangerScan = VirusTotalScanResult(
            url = "https://malicious-test.example.com",
            domain = "malicious-test.example.com",
            maliciousCount = 12,
            suspiciousCount = 3,
            harmlessCount = 2,
            undetectedCount = 50,
            totalEngines = 67,
            scanTimestamp = System.currentTimeMillis()
        )
        viewModel.saveDirectScanResult(dangerScan)

        // Navigating to the dangerous site should trigger a warning
        viewModel.navigateToInput("https://malicious-test.example.com")
        assertNotNull(viewModel.uiState.value.activeWarningState)
        assertEquals(VirusTotalThreatLevel.DANGER, viewModel.uiState.value.activeWarningState?.threatLevel)

        // User bypasses warning
        viewModel.bypassWarning("https://malicious-test.example.com")
        assertNull(viewModel.uiState.value.activeWarningState)
        assertTrue(viewModel.uiState.value.bypassedWarningUrls.contains("malicious-test.example.com"))

        // Simulate a cached scan for a careful site (5 flags)
        val carefulScan = VirusTotalScanResult(
            url = "https://suspicious-test.example.com",
            domain = "suspicious-test.example.com",
            maliciousCount = 5,
            totalEngines = 70,
            scanTimestamp = System.currentTimeMillis()
        )
        viewModel.saveDirectScanResult(carefulScan)

        viewModel.navigateToInput("https://suspicious-test.example.com")
        assertNotNull(viewModel.uiState.value.activeWarningState)
        assertEquals(VirusTotalThreatLevel.CAREFUL, viewModel.uiState.value.activeWarningState?.threatLevel)

        // Simulate safe site (1 flag)
        val safeScan = VirusTotalScanResult(
            url = "https://safe-test.example.com",
            domain = "safe-test.example.com",
            maliciousCount = 1,
            totalEngines = 72,
            scanTimestamp = System.currentTimeMillis()
        )
        viewModel.saveDirectScanResult(safeScan)
        viewModel.navigateToInput("https://safe-test.example.com")
        assertNull(viewModel.uiState.value.activeWarningState)
    }

    @Test
    fun `ai features master toggle, key config, ai protection, and workflows`() {
        // 1. SettingsCategory and URL routing
        assertEquals("devbrowser:settings/ai", BrowserTab.URL_SETTINGS_AI)
        assertEquals(SettingsCategory.AI_FEATURES, SettingsCategory.fromUrl("devbrowser:settings/ai"))
        assertEquals(SettingsCategory.AI_FEATURES, SettingsCategory.fromUrl("devbrowser:settings/ai/"))

        // 2. Threat category mapping
        assertEquals(AiThreatCategory.SAFE, AiThreatCategory.fromString("SAFE"))
        assertEquals(AiThreatCategory.CAREFUL, AiThreatCategory.fromString("CAREFUL"))
        assertEquals(AiThreatCategory.DANGER, AiThreatCategory.fromString("DANGER"))
        assertEquals(AiThreatCategory.DANGER, AiThreatCategory.fromString("MALICIOUS"))
        assertEquals(VirusTotalThreatLevel.SAFE, AiThreatCategory.SAFE.vtEquivalent)
        assertEquals(VirusTotalThreatLevel.CAREFUL, AiThreatCategory.CAREFUL.vtEquivalent)
        assertEquals(VirusTotalThreatLevel.DANGER, AiThreatCategory.DANGER.vtEquivalent)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = BrowserDevToolsViewModel(context)
        viewModel.attachPreferencesIfNeeded(context)

        // 3. Default state: AI features disabled by default
        assertFalse(viewModel.uiState.value.isAiMasterEnabled)
        assertFalse(viewModel.uiState.value.isAiProtectionActive)

        // Sub-feature toggle saved even when master toggle is off
        viewModel.setAiProtectionEnabled(true)
        assertTrue(viewModel.uiState.value.isAiProtectionEnabled)
        assertFalse("AI Protection must NOT be active while master switch is off", viewModel.uiState.value.isAiProtectionActive)

        // Configure Gemini API key
        viewModel.setGeminiApiKey("AIzaSy-Test-Gemini-Key")
        assertEquals("AIzaSy-Test-Gemini-Key", viewModel.uiState.value.geminiApiKey)
        assertEquals("AIzaSy-Test-Gemini-Key", viewModel.uiState.value.effectiveGeminiApiKey)

        // Turn on master switch with VirusTotal off: AI Protection becomes active
        viewModel.setVirusTotalEnabled(false)
        viewModel.setAiMasterEnabled(true)
        assertTrue(viewModel.uiState.value.isAiMasterEnabled)
        assertTrue(viewModel.uiState.value.isAiProtectionActive)

        // Grayed out / inactive when VirusTotal is enabled
        viewModel.setVirusTotalEnabled(true)
        assertFalse("AI Protection must be grayed out/inactive when VirusTotal is enabled", viewModel.uiState.value.isAiProtectionActive)
        viewModel.setVirusTotalEnabled(false)
        assertTrue(viewModel.uiState.value.isAiProtectionActive)

        // 4. AI Protection navigation interception with cached scans
        val dangerAiScan = AiProtectionScanResult(
            url = "https://ai-phish-danger.example.com",
            domain = "ai-phish-danger.example.com",
            category = AiThreatCategory.DANGER,
            reasoning = "Gemini identified active credential harvesting and spoofing patterns."
        )
        viewModel.saveDirectAiScanResult(dangerAiScan)

        viewModel.navigateToInput("https://ai-phish-danger.example.com")
        val warning = viewModel.uiState.value.activeWarningState
        assertNotNull("AI Protection must intercept danger site", warning)
        assertEquals(VirusTotalThreatLevel.DANGER, warning?.threatLevel)
        assertTrue(warning?.isAiProtection == true)
        assertEquals("Gemini identified active credential harvesting and spoofing patterns.", warning?.aiReasoning)

        // Safe AI site should not trigger warning
        val safeAiScan = AiProtectionScanResult(
            url = "https://ai-safe.example.com",
            domain = "ai-safe.example.com",
            category = AiThreatCategory.SAFE,
            reasoning = "Legitimate verified technology platform."
        )
        viewModel.saveDirectAiScanResult(safeAiScan)
        viewModel.navigateToInput("https://ai-safe.example.com")
        assertNull("Safe AI site should not trigger warning", viewModel.uiState.value.activeWarningState)

        // 5. Workflows: AI Chat and Translate actions
        viewModel.setAiMasterEnabled(false)
        viewModel.triggerAiChat()
        assertNotNull(viewModel.uiState.value.activeAiChatState)
        assertTrue(viewModel.uiState.value.activeAiChatState?.errorMessage?.contains("disabled", ignoreCase = true) == true)
        viewModel.dismissAiChat()
        assertNull(viewModel.uiState.value.activeAiChatState)

        viewModel.triggerTranslate()
        assertNotNull(viewModel.uiState.value.activeTranslationState)
        assertTrue(viewModel.uiState.value.activeTranslationState?.errorMessage?.contains("disabled", ignoreCase = true) == true)
        viewModel.dismissTranslation()
        assertNull(viewModel.uiState.value.activeTranslationState)
    }
}
