package com.example.ui.pages.debug

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.net.URLEncoder
import com.example.model.AppUpdateInfo
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.model.DeviceViewportPreset
import com.example.model.SearchEngineItem
import com.example.model.ThemeAccentColor
import com.example.model.WebPageError
import com.example.model.WebPageErrorType
import com.example.ui.pages.error.WebErrorPage
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import java.net.URLDecoder
import java.util.Locale

/**
 * Parses query params from internal debug URLs:
 * e.g. devbrowser:debug/error/unexpected?site=abcdefg.com&err=ERROR_-999
 */
fun parseDebugQueryParams(fullUrl: String): Pair<String, Map<String, String>> {
    val rawPath = fullUrl.substringBefore("?")
    val queryString = fullUrl.substringAfter("?", "")
    if (queryString.isEmpty()) {
        return Pair(rawPath, emptyMap())
    }

    val params = mutableMapOf<String, String>()
    val pairs = queryString.split("&")
    for (pair in pairs) {
        if (pair.isBlank()) continue
        val rawKey = pair.substringBefore("=").trim()
        val rawVal = if (pair.contains("=")) pair.substringAfter("=").trim() else ""
        if (rawKey.isNotEmpty()) {
            val decodedKey = try { URLDecoder.decode(rawKey, "UTF-8") } catch (_: Exception) { rawKey }
            val decodedVal = try { URLDecoder.decode(rawVal, "UTF-8") } catch (_: Exception) { rawVal }
            params[decodedKey] = decodedVal
        }
    }
    return Pair(rawPath, params)
}

enum class DebugCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val path: String,
    val icon: ImageVector
) {
    ERROR(
        id = "error",
        title = "Error Pages",
        subtitle = "Custom error page previews with query parameters (?site=...&err=...)",
        path = "devbrowser:debug/error",
        icon = Icons.Default.ErrorOutline
    ),
    VIEWPORT(
        id = "viewport",
        title = "Device Viewports",
        subtitle = "Preview screen dimensions, user agents, and device emulation presets",
        path = "devbrowser:debug/viewport",
        icon = Icons.Default.Devices
    ),
    THEME(
        id = "theme",
        title = "Theme & Colors",
        subtitle = "Inspect active M3 tokens, neon dark/light modes, and accent colors",
        path = "devbrowser:debug/theme",
        icon = Icons.Default.Palette
    ),
    RUNTIME(
        id = "runtime",
        title = "Runtime & State",
        subtitle = "Active tabs, cache stats, Webview engine, and memory diagnostics",
        path = "devbrowser:debug/runtime",
        icon = Icons.Default.BugReport
    ),
    AUTO_UPDATE(
        id = "autoupdate",
        title = "Auto-Update Simulator",
        subtitle = "Simulate update popup with custom version, description, and changelog parameters",
        path = "devbrowser:debug/autoupdate",
        icon = Icons.Default.SystemUpdate
    );

    companion object {
        fun fromPath(path: String): DebugCategory? {
            val lower = path.lowercase(Locale.US).substringBefore("?")
            return entries.firstOrNull { lower == it.path || lower.startsWith("${it.path}/") }
        }
    }
}

data class DebugErrorPreset(
    val slug: String,
    val title: String,
    val description: String,
    val errorType: WebPageErrorType,
    val defaultErrorCode: Int,
    val defaultErrorName: String
)

val DEBUG_ERROR_PRESETS = listOf(
    DebugErrorPreset(
        slug = "dns",
        title = "Website Doesn't Exist",
        description = "DNS lookup failure / server name not resolved (ERROR_HOST_LOOKUP)",
        errorType = WebPageErrorType.WEBSITE_DOES_NOT_EXIST,
        defaultErrorCode = -2,
        defaultErrorName = "ERROR_HOST_LOOKUP"
    ),
    DebugErrorPreset(
        slug = "no-internet",
        title = "No Internet Connection",
        description = "Device network offline / internet connection unavailable",
        errorType = WebPageErrorType.NO_INTERNET,
        defaultErrorCode = -2,
        defaultErrorName = "ERR_INTERNET_DISCONNECTED"
    ),
    DebugErrorPreset(
        slug = "timeout",
        title = "Connection Timed Out",
        description = "Remote server took too long to reply (ERROR_TIMEOUT)",
        errorType = WebPageErrorType.CONNECTION_TIMED_OUT,
        defaultErrorCode = -8,
        defaultErrorName = "ERROR_TIMEOUT"
    ),
    DebugErrorPreset(
        slug = "cannot-connect",
        title = "Cannot Connect to Server",
        description = "Connection refused by server or unreachable host (ERROR_CONNECT)",
        errorType = WebPageErrorType.CANNOT_CONNECT,
        defaultErrorCode = -6,
        defaultErrorName = "ERROR_CONNECT"
    ),
    DebugErrorPreset(
        slug = "ssl",
        title = "Secure Connection Failed",
        description = "SSL certificate validation or TLS handshake failed (ERROR_FAILED_SSL_HANDSHAKE)",
        errorType = WebPageErrorType.SECURITY_SSL_ERROR,
        defaultErrorCode = -11,
        defaultErrorName = "ERROR_FAILED_SSL_HANDSHAKE"
    ),
    DebugErrorPreset(
        slug = "invalid-address",
        title = "Invalid Web Address",
        description = "Malformed address or unsupported URI scheme (ERROR_BAD_URL)",
        errorType = WebPageErrorType.INVALID_ADDRESS,
        defaultErrorCode = -12,
        defaultErrorName = "ERROR_BAD_URL"
    ),
    DebugErrorPreset(
        slug = "unexpected",
        title = "Unexpected Error",
        description = "Unknown or unhandled WebView error with customizable code",
        errorType = WebPageErrorType.UNEXPECTED_ERROR,
        defaultErrorCode = -9999,
        defaultErrorName = "ERROR_DEBUG"
    )
)

@Composable
fun DevBrowserDebugPage(
    currentUrl: String,
    searchEngine: SearchEngineItem,
    tabs: List<BrowserTab>,
    activeTab: BrowserTab,
    isDarkTheme: Boolean,
    isNeonMode: Boolean,
    accentColor: ThemeAccentColor,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onTriggerAutoUpdate: (version: String?, description: String?, changelog: String?) -> Unit = { _, _, _ -> },
    pendingUpdateNotice: AppUpdateInfo? = null,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val (rawPath, queryParams) = remember(currentUrl) { parseDebugQueryParams(currentUrl) }
    val lowerPath = rawPath.lowercase(Locale.US)

    // Only intercept system back gesture on subpages (e.g. devbrowser:debug/error) to return to main debug menu
    val isSubPage = lowerPath != BrowserTab.URL_DEBUG &&
        lowerPath != "devbrowser://debug" &&
        lowerPath != "about:debug" &&
        lowerPath != "debug"
    if (isSubPage) {
        BackHandler {
            onNavigateBack()
        }
    }

    // Check if user is on an individual error subpage: devbrowser:debug/error/<slug>
    if (lowerPath.startsWith("devbrowser:debug/error/")) {
        val slug = lowerPath.removePrefix("devbrowser:debug/error/").substringBefore("/")
        val preset = DEBUG_ERROR_PRESETS.firstOrNull { it.slug == slug } ?: DEBUG_ERROR_PRESETS.last()

        val defaultUrl = "devbrowser:debug/error/${preset.slug}"
        val targetSite = queryParams["site"]?.ifBlank { null } ?: defaultUrl
        val customErrParam = queryParams["err"]?.ifBlank { null }

        val errorName = customErrParam ?: preset.defaultErrorName
        val errorCode = if (customErrParam != null) {
            if (customErrParam.startsWith("ERROR_")) {
                customErrParam.removePrefix("ERROR_").toIntOrNull() ?: -999
            } else {
                customErrParam.toIntOrNull() ?: -999
            }
        } else {
            preset.defaultErrorCode
        }

        val simulatedError = WebPageError(
            url = targetSite,
            errorCode = errorCode,
            errorName = errorName,
            description = "Simulated error generated by DevBrowser debug tools (URL: $currentUrl)",
            errorType = preset.errorType
        )

        WebErrorPage(
            error = simulatedError,
            searchEngine = searchEngine,
            canGoBack = true,
            onRetry = { onNavigate(currentUrl) },
            onGoBack = { onNavigate(BrowserTab.URL_DEBUG_ERROR) },
            onGoHome = { onNavigate(BrowserTab.URL_HOME) },
            onSearchWeb = { query -> onNavigate(searchEngine.formatSearchUrl(query)) },
            modifier = modifier
        )
        return
    }

    // Otherwise render standard debug navigation view
    val activeCategory = DebugCategory.fromPath(lowerPath)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(cs.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = cs.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (activeCategory == null) "Debug Center" else activeCategory.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DevAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "INTERNAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = DevAmber
                            )
                        }
                    }

                    Text(
                        text = if (activeCategory == null) "devbrowser:debug" else activeCategory.path,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = cs.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))

            // Body Content based on active Category
            when (activeCategory) {
                null -> DebugMainCategoriesView(
                    onNavigate = onNavigate
                )
                DebugCategory.ERROR -> DebugErrorCategoryView(
                    onNavigate = onNavigate
                )
                DebugCategory.VIEWPORT -> DebugViewportCategoryView(
                    activePreset = activeTab.devicePreset
                )
                DebugCategory.THEME -> DebugThemeCategoryView(
                    isDarkTheme = isDarkTheme,
                    isNeonMode = isNeonMode,
                    accentColor = accentColor
                )
                DebugCategory.RUNTIME -> DebugRuntimeCategoryView(
                    tabs = tabs,
                    activeTab = activeTab
                )
                DebugCategory.AUTO_UPDATE -> DebugAutoUpdateCategoryView(
                    currentUrl = currentUrl,
                    queryParams = queryParams,
                    onNavigate = onNavigate,
                    onTriggerAutoUpdate = onTriggerAutoUpdate
                )
            }
        }
    }
}

/**
 * Main debug page (devbrowser:debug) listing all debug categories
 */
@Composable
private fun DebugMainCategoriesView(
    onNavigate: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = cs.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Secret Developer Debug Pages",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                    }
                    Text(
                        text = "These diagnostic tools are strictly accessible via direct URL navigation in the address bar. Select a category below or type the full path into the URL input.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "DEBUG CATEGORIES",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        items(DebugCategory.entries.size) { index ->
            val cat = DebugCategory.entries[index]
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(cat.path) }
                    .testTag("debug_cat_${cat.id}")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(cs.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(cat.icon, contentDescription = null, tint = cs.primary, modifier = Modifier.size(24.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cat.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = cat.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                        Text(
                            text = cat.path,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = cs.primary
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Category page for Error Pages: devbrowser:debug/error
 * Features links to all preset error pages, plus interactive generator testing ?site=...&err=...
 */
@Composable
private fun DebugErrorCategoryView(
    onNavigate: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedPreset by remember { mutableStateOf(DEBUG_ERROR_PRESETS.last()) }
    var inputSite by remember { mutableStateOf("thisdosentexsist.com") }
    var inputErrCode by remember { mutableStateOf("ERROR_-999") }

    val computedQueryUrl = remember(selectedPreset, inputSite, inputErrCode) {
        val base = "devbrowser:debug/error/${selectedPreset.slug}"
        val queryParts = mutableListOf<String>()
        if (inputSite.isNotBlank()) {
            queryParts.add("site=${inputSite.trim()}")
        }
        if (selectedPreset.slug == "unexpected" && inputErrCode.isNotBlank()) {
            queryParts.add("err=${inputErrCode.trim()}")
        }
        if (queryParts.isNotEmpty()) "$base?${queryParts.joinToString("&")}" else base
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Interactive URL Generator Card
        item {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("card_error_generator")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = cs.primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Custom Error URL Generator & Tester",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                    }

                    Text(
                        text = "Customize the site parameter (?site=...) and error code (?err=...) to test the custom error pages in real-time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )

                    // Error Type Selector (Chips/Buttons)
                    Text(
                        text = "Select Error Page Type:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(cs.surfaceVariant.copy(alpha = 0.35f))
                            .border(1.dp, cs.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        DEBUG_ERROR_PRESETS.forEachIndexed { idx, p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPreset = p }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedPreset == p,
                                    onClick = { selectedPreset = p },
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (selectedPreset == p) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selectedPreset == p) cs.primary else cs.onSurface
                                    )
                                    Text(
                                        text = p.slug,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                        color = cs.onSurfaceVariant
                                    )
                                }
                            }
                            if (idx < DEBUG_ERROR_PRESETS.size - 1) {
                                HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.25f))
                            }
                        }
                    }

                    // Input for ?site=
                    OutlinedTextField(
                        value = inputSite,
                        onValueChange = { inputSite = it },
                        label = { Text("Site Parameter (?site=...)") },
                        placeholder = { Text("e.g. thisdosentexsist.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_debug_site")
                    )

                    // Input for ?err= (shown especially for unexpected error)
                    if (selectedPreset.slug == "unexpected") {
                        OutlinedTextField(
                            value = inputErrCode,
                            onValueChange = { inputErrCode = it },
                            label = { Text("Error Code Parameter (?err=...)") },
                            placeholder = { Text("e.g. ERROR_-999") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_debug_err")
                        )
                    }

                    // Generated URL preview box
                    Surface(
                        color = cs.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = computedQueryUrl,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                ),
                                color = cs.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(computedQueryUrl))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Launch Preview Button
                    Button(
                        onClick = { onNavigate(computedQueryUrl) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("button_launch_debug_error")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Launch Error Page Preview")
                    }
                }
            }
        }

        // List of all Error Debug Page Links
        item {
            Text(
                text = "INDIVIDUAL ERROR PAGES",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }

        items(DEBUG_ERROR_PRESETS.size) { idx ->
            val preset = DEBUG_ERROR_PRESETS[idx]
            val defaultPath = "devbrowser:debug/error/${preset.slug}"
            val withSiteExample = "devbrowser:debug/error/${preset.slug}?site=thisdosentexsist.com"
            val unexpectedFullExample = "devbrowser:debug/error/unexpected?site=abcdefg.com&err=ERROR_-999"

            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = cs.onSurface
                            )
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                            Text(
                                text = defaultPath,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = cs.primary
                            )
                        }

                        Button(
                            onClick = { onNavigate(defaultPath) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_open_${preset.slug}")
                        ) {
                            Text("Open")
                        }
                    }

                    // Parameterized Quick Links
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate(withSiteExample) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("With ?site=...", fontSize = 11.sp)
                        }

                        if (preset.slug == "unexpected") {
                            OutlinedButton(
                                onClick = { onNavigate(unexpectedFullExample) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("?site=...&err=...", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category page for Viewport & Devices: devbrowser:debug/viewport
 */
@Composable
private fun DebugViewportCategoryView(
    activePreset: DeviceViewportPreset
) {
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "DEVICE VIEWPORT PRESETS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = cs.onSurfaceVariant
            )
        }

        items(DeviceViewportPreset.entries.size) { idx ->
            val preset = DeviceViewportPreset.entries[idx]
            val isCurrent = preset == activePreset

            Surface(
                color = if (isCurrent) cs.primary.copy(alpha = 0.08f) else cs.surface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrent) cs.primary else cs.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isCurrent) cs.primary else cs.onSurface
                        )
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cs.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = cs.primary
                                )
                            }
                        }
                    }
                    val dimen = if (preset.widthDp != null && preset.heightDp != null) "${preset.widthDp} × ${preset.heightDp} dp" else "Fluid (Responsive)"
                    Text(text = "Dimensions: $dimen", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    Text(
                        text = "User-Agent: ${preset.userAgent}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                        color = cs.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Category page for Theme: devbrowser:debug/theme
 */
@Composable
private fun DebugThemeCategoryView(
    isDarkTheme: Boolean,
    isNeonMode: Boolean,
    accentColor: ThemeAccentColor
) {
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Active Theme Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Text(
                        text = "Mode: ${if (isDarkTheme) "Dark" else "Light"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        text = "Neon Glow: ${if (isNeonMode) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        text = "Accent Color: ${accentColor.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "AVAILABLE ACCENT PRESETS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = cs.onSurfaceVariant
            )
        }

        items(ThemeAccentColor.entries.size) { idx ->
            val acc = ThemeAccentColor.entries[idx]
            val isCurrent = acc == accentColor
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) cs.primary else cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(acc.darkHex))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = acc.displayName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "Dark: 0x${acc.darkHex.toString(16).uppercase()} | Light: 0x${acc.lightHex.toString(16).uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Category page for Runtime: devbrowser:debug/runtime
 */
@Composable
private fun DebugRuntimeCategoryView(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab
) {
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Browser Runtime Diagnostics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Text(text = "Open Tabs Count: ${tabs.size}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Active Tab ID: ${activeTab.id}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Active Tab URL: ${activeTab.url}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Back Stack Count: ${activeTab.backStack.size}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Forward Stack Count: ${activeTab.forwardStack.size}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Active Tab Error State: ${activeTab.errorState?.errorType?.name ?: "None"}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

fun buildAutoUpdateDebugUrl(version: String, description: String, changelog: String): String {
    val params = mutableListOf<String>()
    if (version.isNotBlank()) params.add("version=" + URLEncoder.encode(version.trim(), "UTF-8"))
    if (description.isNotBlank()) params.add("description=" + URLEncoder.encode(description.trim(), "UTF-8"))
    if (changelog.isNotBlank()) params.add("changelog=" + URLEncoder.encode(changelog.trim(), "UTF-8"))
    return if (params.isEmpty()) BrowserTab.URL_DEBUG_AUTO_UPDATE else "${BrowserTab.URL_DEBUG_AUTO_UPDATE}?" + params.joinToString("&")
}

/**
 * Category page for Auto-Update simulator: devbrowser:debug/autoupdate
 */
@Composable
private fun DebugAutoUpdateCategoryView(
    currentUrl: String,
    queryParams: Map<String, String>,
    onNavigate: (String) -> Unit,
    onTriggerAutoUpdate: (version: String?, description: String?, changelog: String?) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val clipboardManager = LocalClipboardManager.current

    val paramVersion = queryParams["version"] ?: queryParams["v"]
    val paramDesc = queryParams["description"] ?: queryParams["desc"] ?: queryParams["short_description"]
    val paramChangelog = queryParams["changelog"] ?: queryParams["notes"] ?: queryParams["release_notes"]

    // Automatically trigger update popup when navigating to this debug page
    LaunchedEffect(currentUrl) {
        onTriggerAutoUpdate(paramVersion, paramDesc, paramChangelog)
    }

    var versionInput by remember(paramVersion) { mutableStateOf(paramVersion ?: "") }
    var descInput by remember(paramDesc) { mutableStateOf(paramDesc ?: "") }
    var changelogInput by remember(paramChangelog) { mutableStateOf(paramChangelog ?: "") }

    val cleanPreviewVersion = versionInput.trim().removePrefix("v").removePrefix("V").ifEmpty { "12.3" }
    val computedDownloadUrl = "https://github.com/Green0Field/DevBrowser/releases/download/v$cleanPreviewVersion/DevBrowser-$cleanPreviewVersion-release.apk"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Card
        item {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(cs.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = cs.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Update Debug Simulator",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = cs.onSurface
                            )
                            Text(
                                text = "Simulate update popup on demand even when no update is published",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))

                    Text(
                        text = "The update popup triggers automatically upon opening this page. You can customize the version, short description, and changelog below, or launch the popup anytime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            onTriggerAutoUpdate(
                                versionInput.ifBlank { null },
                                descInput.ifBlank { null },
                                changelogInput.ifBlank { null }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_trigger_debug_update")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show Update Popup Now")
                    }
                }
            }
        }

        // Live Parameter Inputs
        item {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customize Simulation Parameters",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Text(
                        text = "If any field is left empty, the value is automatically fetched from the newest GitHub release.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = versionInput,
                        onValueChange = { versionInput = it },
                        label = { Text("Version Number (?version=...)") },
                        placeholder = { Text("e.g. 12.3 or 1.0.0") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debug_autoupdate_version")
                    )

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Short Description (?description=...)") },
                        placeholder = { Text("e.g. cool new version") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debug_autoupdate_description")
                    )

                    OutlinedTextField(
                        value = changelogInput,
                        onValueChange = { changelogInput = it },
                        label = { Text("Changelog (?changelog=...)") },
                        placeholder = { Text("e.g. - Added new feature\\n - added another feature") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debug_autoupdate_changelog")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val targetUrl = buildAutoUpdateDebugUrl(versionInput, descInput, changelogInput)
                                onNavigate(targetUrl)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply & Update URL")
                        }

                        OutlinedButton(
                            onClick = {
                                versionInput = ""
                                descInput = ""
                                changelogInput = ""
                                onNavigate(BrowserTab.URL_DEBUG_AUTO_UPDATE)
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        // Quick Presets
        item {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )

                    // Preset 1: user specified example
                    OutlinedButton(
                        onClick = {
                            val presetUrl = "devbrowser:debug/autoupdate?version=12.3&description=cool new version&changelog=- Added new feature\\n- added another feature"
                            onNavigate(presetUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_preset_v12_3")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text("Preset: v12.3 with Custom Description & Changelog", fontWeight = FontWeight.SemiBold)
                            Text("?version=12.3&description=cool new version&changelog=...", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        }
                    }

                    // Preset 2: version only (tests GitHub fallback)
                    OutlinedButton(
                        onClick = {
                            val presetUrl = "devbrowser:debug/autoupdate?version=99.0"
                            onNavigate(presetUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_preset_github_fallback")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text("Preset: Version 99.0 only (GitHub Fallback for notes)", fontWeight = FontWeight.SemiBold)
                            Text("?version=99.0 (Description and notes retrieved from GitHub)", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        }
                    }

                    // Preset 3: v0.2 Minor Update
                    OutlinedButton(
                        onClick = {
                            val presetUrl = "devbrowser:debug/autoupdate?version=0.2&description=Bug Fixes & Improvements&changelog=- Fixed tab switching\\n- Improved JavaScript engine"
                            onNavigate(presetUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_preset_v0_2")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text("Preset: v0.2 Minor Update", fontWeight = FontWeight.SemiBold)
                            Text("?version=0.2&description=Bug Fixes...&changelog=...", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        }
                    }

                    // Preset 4: Pure GitHub latest
                    OutlinedButton(
                        onClick = {
                            onNavigate(BrowserTab.URL_DEBUG_AUTO_UPDATE)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_preset_github_latest")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text("Preset: Pure GitHub Latest (No Query Params)", fontWeight = FontWeight.SemiBold)
                            Text("devbrowser:debug/autoupdate", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Automatic Download Link Card
        item {
            Surface(
                color = cs.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Automated Download URL Resolution",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Text(
                        text = "A URL download parameter is not needed because DevBrowser computes the APK binary URL directly from the version number:",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = computedDownloadUrl,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = cs.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Copyable Test URL Card
        item {
            val fullUrl = buildAutoUpdateDebugUrl(versionInput, descInput, changelogInput)
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current Simulator URL",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.onSurface
                    )
                    Text(
                        text = fullUrl,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = cs.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fullUrl))
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy URL")
                    }
                }
            }
        }
    }
}

