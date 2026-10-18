package com.example.ui.pages.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.model.BrowserTab
import com.example.model.ClearDataTimeRange
import com.example.model.HistoryItem
import com.example.model.SearchEngineItem
import com.example.model.SettingsCategory
import com.example.model.ThemeAccentColor
import com.example.model.VirusTotalRescanInterval
import com.example.model.UserScript
import com.example.model.UserScriptUpdateInterval
import com.example.model.GeminiModelChoice
import com.example.model.AutoUpdateCheckFrequency
import com.example.model.UpdateCheckStatus

@Composable
fun DevBrowserSettingsPage(
    currentUrl: String,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    isNeonMode: Boolean,
    accentColor: ThemeAccentColor,
    searchEngines: List<SearchEngineItem>,
    searchEngine: SearchEngineItem,
    javaScriptEnabled: Boolean,
    history: List<HistoryItem>,
    siteVisitCounts: Map<String, Int>,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetNeonMode: (Boolean) -> Unit,
    onSelectAccentColor: (ThemeAccentColor) -> Unit,
    onSelectSearchEngine: (SearchEngineItem) -> Unit,
    onAddCustomSearchEngine: (String, String) -> Unit,
    onRemoveCustomSearchEngine: (String) -> Unit,
    onToggleJavaScript: (Boolean) -> Unit,
    onClearBrowsingData: (
        targetDomain: String?,
        timeRange: ClearDataTimeRange,
        deleteHistory: Boolean,
        deleteCookies: Boolean,
        deleteCache: Boolean,
        deleteStorage: Boolean
    ) -> Unit,
    isVirusTotalEnabled: Boolean = false,
    virusTotalApiKey: String = "",
    virusTotalRescanInterval: VirusTotalRescanInterval = VirusTotalRescanInterval.HOURS_24,
    virusTotalCachedScansCount: Int = 0,
    onSetVirusTotalEnabled: (Boolean) -> Unit = {},
    onSaveVirusTotalApiKey: (String) -> Unit = {},
    onSelectVirusTotalRescanInterval: (VirusTotalRescanInterval) -> Unit = {},
    onClearVirusTotalScanCache: () -> Unit = {},
    userScripts: List<UserScript> = emptyList(),
    userScriptUpdateInterval: UserScriptUpdateInterval = UserScriptUpdateInterval.HOURS_24,
    onAddUserScript: (UserScript) -> Unit = {},
    onUpdateUserScript: (UserScript) -> Unit = {},
    onDeleteUserScript: (String) -> Unit = {},
    onToggleUserScriptEnabled: (String, Boolean) -> Unit = { _, _ -> },
    onSelectUserScriptUpdateInterval: (UserScriptUpdateInterval) -> Unit = {},
    onImportUserScriptFromUrl: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    isAiMasterEnabled: Boolean = false,
    geminiApiKey: String = "",
    effectiveGeminiApiKey: String = "",
    isAiProtectionEnabled: Boolean = false,
    isAiSummaryEnabled: Boolean = true,
    isAiTranslateEnabled: Boolean = true,
    aiCachedScansCount: Int = 0,
    onSetAiMasterEnabled: (Boolean) -> Unit = {},
    onSaveGeminiApiKey: (String) -> Unit = {},
    onSetAiProtectionEnabled: (Boolean) -> Unit = {},
    onSetAiSummaryEnabled: (Boolean) -> Unit = {},
    onSetAiTranslateEnabled: (Boolean) -> Unit = {},
    onClearAiScanCache: () -> Unit = {},
    selectedGeminiModel: GeminiModelChoice = GeminiModelChoice.AUTO,
    onSelectGeminiModel: (GeminiModelChoice) -> Unit = {},
    isAutoUpdateEnabled: Boolean = true,
    githubApiKey: String = "",
    autoUpdateFrequency: AutoUpdateCheckFrequency = AutoUpdateCheckFrequency.EACH_STARTUP,
    autoUpdateStartupCount: Int = 0,
    lastUpdateCheckMillis: Long = 0L,
    updateCheckStatus: UpdateCheckStatus = UpdateCheckStatus.Idle,
    isIncludePrereleases: Boolean = false,
    onSetAutoUpdateEnabled: (Boolean) -> Unit = {},
    onSaveGithubApiKey: (String) -> Unit = {},
    onSelectAutoUpdateFrequency: (AutoUpdateCheckFrequency) -> Unit = {},
    onSetIncludePrereleases: (Boolean) -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onDownloadUpdateApk: (String) -> Unit = {},
    onOpenReleaseUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val activeCategory = SettingsCategory.fromUrl(currentUrl)

    // Handle system back gesture
    if (activeCategory != null) {
        BackHandler {
            onNavigate(BrowserTab.URL_SETTINGS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_devbrowser_settings"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                if (activeCategory != null) {
                    // Category Sub-Page Header with Back Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = cs.surfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable { onNavigate(BrowserTab.URL_SETTINGS) }
                                .testTag("settings_back_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Settings Categories",
                                    tint = cs.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeCategory.displayName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = cs.onBackground
                            )
                            Text(
                                text = activeCategory.url,
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.primary
                            )
                        }
                    }
                } else {
                    // Main Categories Overview Header
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onBackground
                        )
                        Text(
                            text = "Customize themes, search engines, privacy data, and app info • devbrowser:settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            }

            // Main Content: Dispatch based on Category or show Category List
            if (activeCategory == null) {
                // 1. Theme Category Card
                item {
                    SettingsCategoryCard(
                        category = SettingsCategory.THEME,
                        icon = Icons.Default.Palette,
                        statusPill = "${if (isDarkTheme) "Dark" else "Light"} • ${accentColor.displayName}",
                        onClick = { onNavigate(SettingsCategory.THEME.url) },
                        testTag = "settings_category_card_theme"
                    )
                }

                // 2. Search Category Card
                item {
                    SettingsCategoryCard(
                        category = SettingsCategory.SEARCH,
                        icon = Icons.Default.Search,
                        statusPill = searchEngine.displayName,
                        onClick = { onNavigate(SettingsCategory.SEARCH.url) },
                        testTag = "settings_category_card_search"
                    )
                }

                // 3. Data Category Card
                item {
                    SettingsCategoryCard(
                        category = SettingsCategory.DATA,
                        icon = Icons.Default.DeleteSweep,
                        statusPill = "${history.size} items • ${siteVisitCounts.size} sites",
                        onClick = { onNavigate(SettingsCategory.DATA.url) },
                        testTag = "settings_category_card_data"
                    )
                }

                // 4. VirusTotal Category Card
                item {
                    val vtStatus = when {
                        !isVirusTotalEnabled -> "Disabled"
                        virusTotalApiKey.isBlank() -> "Key Required"
                        else -> "Active • ${virusTotalRescanInterval.displayName}"
                    }
                    SettingsCategoryCard(
                        category = SettingsCategory.VIRUSTOTAL,
                        icon = Icons.Default.Shield,
                        statusPill = vtStatus,
                        onClick = { onNavigate(SettingsCategory.VIRUSTOTAL.url) },
                        testTag = "settings_category_card_virustotal"
                    )
                }

                // 5. AI Features Category Card
                item {
                    val aiStatus = when {
                        !isAiMasterEnabled -> "Disabled"
                        geminiApiKey.isBlank() && effectiveGeminiApiKey.isBlank() -> "Key Required"
                        else -> "Active"
                    }
                    SettingsCategoryCard(
                        category = SettingsCategory.AI_FEATURES,
                        icon = Icons.Default.AutoAwesome,
                        statusPill = aiStatus,
                        onClick = { onNavigate(SettingsCategory.AI_FEATURES.url) },
                        testTag = "settings_category_card_ai"
                    )
                }

                // 6. Userscripts Category Card
                item {
                    val activeCount = userScripts.count { it.enabled }
                    SettingsCategoryCard(
                        category = SettingsCategory.USERSCRIPTS,
                        icon = Icons.Default.Code,
                        statusPill = "$activeCount active • ${userScripts.size} total",
                        onClick = { onNavigate(SettingsCategory.USERSCRIPTS.url) },
                        testTag = "settings_category_card_userscripts"
                    )
                }

                // 7. Auto Update Category Card (above About)
                item {
                    val updateStatusPill = when (val s = updateCheckStatus) {
                        is UpdateCheckStatus.Checking -> "Checking…"
                        is UpdateCheckStatus.UpdateAvailable -> "Update: v${s.info.latestVersionName}"
                        is UpdateCheckStatus.UpToDate -> "Up to date"
                        is UpdateCheckStatus.Error -> "Check failed"
                        is UpdateCheckStatus.Idle -> if (isAutoUpdateEnabled) autoUpdateFrequency.displayName else "Disabled"
                    }
                    SettingsCategoryCard(
                        category = SettingsCategory.AUTO_UPDATE,
                        icon = Icons.Default.SystemUpdate,
                        statusPill = updateStatusPill,
                        onClick = { onNavigate(SettingsCategory.AUTO_UPDATE.url) },
                        testTag = "settings_category_card_auto_update"
                    )
                }

                // 8. About Category Card
                item {
                    SettingsCategoryCard(
                        category = SettingsCategory.ABOUT,
                        icon = Icons.Default.Info,
                        statusPill = "v${BuildConfig.VERSION_NAME}",
                        onClick = { onNavigate(SettingsCategory.ABOUT.url) },
                        testTag = "settings_category_card_about"
                    )
                }
            } else {
                // Render the Specific Category Sub-Page
                item {
                    when (activeCategory) {
                        SettingsCategory.THEME -> ThemeSettingsSection(
                            isDarkTheme = isDarkTheme,
                            isNeonMode = isNeonMode,
                            accentColor = accentColor,
                            onSetDarkTheme = onSetDarkTheme,
                            onSetNeonMode = onSetNeonMode,
                            onSelectAccentColor = onSelectAccentColor
                        )

                        SettingsCategory.SEARCH -> SearchSettingsSection(
                            searchEngines = searchEngines,
                            searchEngine = searchEngine,
                            javaScriptEnabled = javaScriptEnabled,
                            onSelectSearchEngine = onSelectSearchEngine,
                            onAddCustomSearchEngine = onAddCustomSearchEngine,
                            onRemoveCustomSearchEngine = onRemoveCustomSearchEngine,
                            onToggleJavaScript = onToggleJavaScript
                        )

                        SettingsCategory.DATA -> DataSettingsSection(
                            history = history,
                            siteVisitCounts = siteVisitCounts,
                            onClearBrowsingData = onClearBrowsingData
                        )

                        SettingsCategory.VIRUSTOTAL -> VirusTotalSettingsSection(
                            isEnabled = isVirusTotalEnabled,
                            apiKey = virusTotalApiKey,
                            rescanInterval = virusTotalRescanInterval,
                            cachedScansCount = virusTotalCachedScansCount,
                            onSetEnabled = onSetVirusTotalEnabled,
                            onSaveApiKey = onSaveVirusTotalApiKey,
                            onSelectRescanInterval = onSelectVirusTotalRescanInterval,
                            onClearScanCache = onClearVirusTotalScanCache
                        )

                        SettingsCategory.AI_FEATURES -> AiSettingsSection(
                            isAiMasterEnabled = isAiMasterEnabled,
                            geminiApiKey = geminiApiKey,
                            effectiveApiKey = effectiveGeminiApiKey,
                            isAiProtectionEnabled = isAiProtectionEnabled,
                            isAiSummaryEnabled = isAiSummaryEnabled,
                            isAiTranslateEnabled = isAiTranslateEnabled,
                            isVirusTotalEnabled = isVirusTotalEnabled,
                            cachedScansCount = aiCachedScansCount,
                            onSetAiMasterEnabled = onSetAiMasterEnabled,
                            onSaveGeminiApiKey = onSaveGeminiApiKey,
                            onSetAiProtectionEnabled = onSetAiProtectionEnabled,
                            onSetAiSummaryEnabled = onSetAiSummaryEnabled,
                            onSetAiTranslateEnabled = onSetAiTranslateEnabled,
                            onClearAiScanCache = onClearAiScanCache,
                            selectedGeminiModel = selectedGeminiModel,
                            onSelectGeminiModel = onSelectGeminiModel
                        )

                        SettingsCategory.USERSCRIPTS -> UserscriptsSettingsSection(
                            userScripts = userScripts,
                            updateInterval = userScriptUpdateInterval,
                            onNavigate = onNavigate,
                            onDeleteScript = onDeleteUserScript,
                            onToggleScriptEnabled = onToggleUserScriptEnabled,
                            onSelectUpdateInterval = onSelectUserScriptUpdateInterval,
                            onImportFromUrl = onImportUserScriptFromUrl
                        )

                        SettingsCategory.AUTO_UPDATE -> AutoUpdateSettingsSection(
                            isEnabled = isAutoUpdateEnabled,
                            apiKey = githubApiKey,
                            checkFrequency = autoUpdateFrequency,
                            startupCount = autoUpdateStartupCount,
                            lastCheckMillis = lastUpdateCheckMillis,
                            updateStatus = updateCheckStatus,
                            isIncludePrereleases = isIncludePrereleases,
                            onSetEnabled = onSetAutoUpdateEnabled,
                            onSaveApiKey = onSaveGithubApiKey,
                            onSelectFrequency = onSelectAutoUpdateFrequency,
                            onSetIncludePrereleases = onSetIncludePrereleases,
                            onCheckForUpdates = onCheckForUpdates,
                            onDownloadApk = onDownloadUpdateApk,
                            onOpenReleaseUrl = onOpenReleaseUrl
                        )

                        SettingsCategory.ABOUT -> AboutSettingsSection(
                            onNavigate = onNavigate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    category: SettingsCategory,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusPill: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        color = cs.surface,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(cs.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.displayName,
                        tint = cs.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Text(
                        text = category.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(cs.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusPill,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = cs.primary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
