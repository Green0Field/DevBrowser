package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.devtools.DevToolsScripts
import com.example.model.BrowserTab
import com.example.model.ConsoleLogLevel
import com.example.model.DevToolsPanelTab
import com.example.model.ThemeAccentColor
import com.example.viewmodel.BrowserDevToolsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
}
