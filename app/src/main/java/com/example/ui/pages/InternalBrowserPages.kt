package com.example.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BrowserTab
import com.example.model.HistoryItem
import com.example.model.HomeShortcut
import com.example.model.QuickBookmark
import com.example.model.SearchEngineItem
import com.example.model.ShortcutSource
import com.example.model.ThemeAccentColor
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevBrowserHomePage(
    shortcuts: List<HomeShortcut>,
    searchEngine: SearchEngineItem,
    onNavigate: (String) -> Unit,
    onPinShortcut: (HomeShortcut) -> Unit,
    onAddShortcut: (String, String) -> Unit,
    onUnpinShortcut: (HomeShortcut) -> Unit,
    onRemoveShortcut: (HomeShortcut) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    var showAddShortcutForm by remember { mutableStateOf(false) }
    var newShortcutTitle by remember { mutableStateOf("") }
    var newShortcutUrl by remember { mutableStateOf("https://") }
    var statusBannerText by remember { mutableStateOf<String?>(null) }
    var selectedPinnedShortcutMenu by remember { mutableStateOf<HomeShortcut?>(null) }

    val focusManager = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        cs.primary.copy(alpha = 0.10f),
                        cs.background,
                        cs.background
                    )
                )
            )
            .testTag("page_devbrowser_home"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Expressive Brand Header with Exact App Launcher Icon
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF0F172A),
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .size(52.dp)
                                .border(2.dp, cs.primary, RoundedCornerShape(18.dp))
                                .testTag("home_app_logo")
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_launcher_icon_1791732376975),
                                contentDescription = "DevBrowser App Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }
                        Column {
                            Text(
                                text = "DevBrowser",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                                color = cs.onBackground
                            )
                            Text(
                                text = "Expressive Web Browser & Chrome DevTools",
                                style = MaterialTheme.typography.labelMedium,
                                color = cs.primary
                            )
                        }
                    }
                }
            }

            // Expressive Pill Search / URL Bar with Active Search Engine Logo
            item {
                Surface(
                    color = cs.surface,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, cs.outline, RoundedCornerShape(50))
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = {
                            Text(
                                text = "Search ${searchEngine.displayName} or type a URL…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier.padding(start = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SearchEngineLogo(engine = searchEngine, size = 24.dp)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                if (searchInput.isNotBlank()) {
                                    focusManager.clearFocus()
                                    onNavigate(searchInput)
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }
            }

            // Internal Pages Expressive Pill Strip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    InternalLinkChip(
                        icon = Icons.Default.Bookmarks,
                        label = "Bookmarks",
                        onClick = { onNavigate(BrowserTab.URL_BOOKMARKS) }
                    )
                    InternalLinkChip(
                        icon = Icons.Default.History,
                        label = "History",
                        onClick = { onNavigate(BrowserTab.URL_HISTORY) }
                    )
                    InternalLinkChip(
                        icon = Icons.Default.Science,
                        label = "Test Page",
                        onClick = { onNavigate(BrowserTab.URL_TEST) }
                    )
                    InternalLinkChip(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = { onNavigate(BrowserTab.URL_SETTINGS) }
                    )
                }
            }

            // Shortcuts Section (Separate from Bookmarks; auto-added from Most Visited & Top Bookmarks; hold to pin)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_shortcuts_section"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shortcuts",
                                style = MaterialTheme.typography.titleLarge,
                                color = cs.onBackground
                            )
                            Text(
                                text = "Auto-added from most visited & bookmarks • Hold to pin",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = cs.primaryContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(1.dp, cs.primary, RoundedCornerShape(50))
                                .clip(RoundedCornerShape(50))
                                .clickable { showAddShortcutForm = !showAddShortcutForm }
                                .testTag("add_shortcut_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Shortcut",
                                    tint = cs.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Add Shortcut",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = cs.primary
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = statusBannerText != null) {
                        statusBannerText?.let { msg ->
                            Surface(
                                color = cs.primaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, cs.primary, RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = cs.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = cs.primary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { statusBannerText = null }
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showAddShortcutForm) {
                        Surface(
                            color = cs.surface,
                            shape = RoundedCornerShape(22.dp),
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, cs.primary, RoundedCornerShape(22.dp))
                                .testTag("add_shortcut_form")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "New Pinned Shortcut",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = cs.primary
                                )
                                OutlinedTextField(
                                    value = newShortcutTitle,
                                    onValueChange = { newShortcutTitle = it },
                                    label = { Text("Shortcut Name") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("shortcut_title_input")
                                )
                                OutlinedTextField(
                                    value = newShortcutUrl,
                                    onValueChange = { newShortcutUrl = it },
                                    label = { Text("URL (e.g. https://github.com)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("shortcut_url_input")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(cs.primary)
                                            .clickable {
                                                if (newShortcutUrl.isNotBlank()) {
                                                    val cleanTitle = newShortcutTitle.ifBlank {
                                                        newShortcutUrl.removePrefix("https://").removePrefix("http://")
                                                    }
                                                    onAddShortcut(cleanTitle, newShortcutUrl.trim())
                                                    statusBannerText = "Added & pinned '$cleanTitle' to Shortcuts"
                                                    newShortcutTitle = ""
                                                    newShortcutUrl = "https://"
                                                    showAddShortcutForm = false
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 9.dp)
                                            .testTag("save_shortcut_button")
                                    ) {
                                        Text(
                                            text = "Pin Shortcut",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = cs.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action banner when long-pressing an already-pinned shortcut
                    AnimatedVisibility(visible = selectedPinnedShortcutMenu != null) {
                        selectedPinnedShortcutMenu?.let { target ->
                            Surface(
                                color = cs.surfaceVariant,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, cs.primary, RoundedCornerShape(18.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = target.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = cs.onSurface
                                        )
                                        Text(
                                            text = "Pinned shortcut options",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cs.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(cs.surface)
                                                .border(1.dp, cs.outline, RoundedCornerShape(50))
                                                .clickable {
                                                    onUnpinShortcut(target)
                                                    selectedPinnedShortcutMenu = null
                                                    statusBannerText = "Unpinned '${target.title}'"
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Unpin", style = MaterialTheme.typography.labelSmall, color = cs.primary)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(cs.error.copy(alpha = 0.16f))
                                                .border(1.dp, cs.error, RoundedCornerShape(50))
                                                .clickable {
                                                    onRemoveShortcut(target)
                                                    selectedPinnedShortcutMenu = null
                                                    statusBannerText = "Removed '${target.title}'"
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Remove", style = MaterialTheme.typography.labelSmall, color = cs.error)
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancel",
                                            tint = cs.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.CenterVertically)
                                                .clickable { selectedPinnedShortcutMenu = null }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Expressive 2-column Grid of Shortcuts
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        shortcuts.chunked(2).forEach { rowShortcuts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowShortcuts.forEach { sc ->
                                    Surface(
                                        color = if (sc.isPinned) cs.surface else cs.surface.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(22.dp),
                                        tonalElevation = if (sc.isPinned) 4.dp else 1.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                width = if (sc.isPinned) 1.5.dp else 1.dp,
                                                color = if (sc.isPinned) cs.primary.copy(alpha = 0.65f) else cs.outlineVariant,
                                                shape = RoundedCornerShape(22.dp)
                                            )
                                            .clip(RoundedCornerShape(22.dp))
                                            .combinedClickable(
                                                onClick = { onNavigate(sc.url) },
                                                onLongClick = {
                                                    if (!sc.isPinned) {
                                                        onPinShortcut(sc)
                                                        statusBannerText = "Pinned '${sc.title}' to Shortcuts!"
                                                    } else {
                                                        selectedPinnedShortcutMenu = sc
                                                    }
                                                }
                                            )
                                            .testTag("shortcut_card_${sc.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(cs.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            sc.url == BrowserTab.URL_TEST -> Icons.Default.Science
                                                            sc.source == ShortcutSource.TOP_BOOKMARK -> Icons.Default.Bookmark
                                                            sc.source == ShortcutSource.MOST_VISITED -> Icons.Default.TrendingUp
                                                            else -> Icons.Default.Language
                                                        },
                                                        contentDescription = null,
                                                        tint = cs.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                // Pinned vs Auto-Added Badge
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(
                                                            if (sc.isPinned) cs.primaryContainer
                                                            else cs.surfaceVariant
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        if (sc.isPinned) {
                                                            Icon(
                                                                imageVector = Icons.Default.PushPin,
                                                                contentDescription = "Pinned",
                                                                tint = cs.primary,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = if (sc.isPinned) "Pinned" else sc.source.badgeLabel,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                            color = if (sc.isPinned) cs.primary else cs.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = sc.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = cs.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (sc.isPinned) {
                                                        sc.url.removePrefix("https://")
                                                    } else {
                                                        "Hold to pin • ${sc.url.removePrefix("https://")}"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = cs.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowShortcuts.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchEngineLogo(
    engine: SearchEngineItem,
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    val lowerId = engine.id.lowercase(Locale.US)
    val lowerName = engine.displayName.lowercase(Locale.US)
    val lowerUrl = engine.queryTemplate.lowercase(Locale.US)

    Canvas(
        modifier = modifier
            .size(size)
            .testTag("search_engine_logo_${engine.id}")
    ) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w.coerceAtMost(h) / 2f

        when {
            lowerId == "google" || lowerName.contains("google") || lowerUrl.contains("google.") -> {
                // White badge background
                drawCircle(color = Color.White, radius = radius, center = center)
                val arcStroke = Stroke(width = w * 0.20f, cap = StrokeCap.Butt)
                val inset = w * 0.22f
                val arcSize = Size(w - inset * 2, h - inset * 2)
                val topLeft = Offset(inset, inset)
                // Red top arc
                drawArc(
                    color = Color(0xFFEA4335),
                    startAngle = -145f,
                    sweepAngle = 105f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = arcStroke
                )
                // Yellow left arc
                drawArc(
                    color = Color(0xFFFBBC05),
                    startAngle = 140f,
                    sweepAngle = 75f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = arcStroke
                )
                // Green bottom arc
                drawArc(
                    color = Color(0xFF34A853),
                    startAngle = 45f,
                    sweepAngle = 95f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = arcStroke
                )
                // Blue right arc + crossbar
                drawArc(
                    color = Color(0xFF4285F4),
                    startAngle = -12f,
                    sweepAngle = 57f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = arcStroke
                )
                drawLine(
                    color = Color(0xFF4285F4),
                    start = Offset(w * 0.50f, h * 0.50f),
                    end = Offset(w * 0.84f, h * 0.50f),
                    strokeWidth = w * 0.19f,
                    cap = StrokeCap.Butt
                )
            }

            lowerId == "duckduckgo" || lowerName.contains("duck") || lowerUrl.contains("duckduckgo") -> {
                // DuckDuckGo Orange Circle
                drawCircle(color = Color(0xFFDE5833), radius = radius, center = center)
                // White Duck Head & Neck silhouette
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.42f,
                    center = Offset(w * 0.44f, h * 0.43f)
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(w * 0.32f, h * 0.45f),
                    size = Size(w * 0.25f, h * 0.36f)
                )
                // Yellow-Orange Duck Bill
                val billPath = Path().apply {
                    moveTo(w * 0.56f, h * 0.37f)
                    lineTo(w * 0.84f, h * 0.44f)
                    lineTo(w * 0.56f, h * 0.53f)
                    close()
                }
                drawPath(path = billPath, color = Color(0xFFFFCC33))
                // Green Bow Tie
                drawCircle(
                    color = Color(0xFF4DAA57),
                    radius = radius * 0.15f,
                    center = Offset(w * 0.45f, h * 0.76f)
                )
            }

            lowerId == "bing" || lowerName.contains("bing") || lowerUrl.contains("bing.") -> {
                // Microsoft Bing Teal-Blue Circle + Geometric 'b' Ribbon
                drawCircle(color = Color(0xFF008373), radius = radius, center = center)
                val bPath = Path().apply {
                    moveTo(w * 0.34f, h * 0.20f)
                    lineTo(w * 0.48f, h * 0.26f)
                    lineTo(w * 0.48f, h * 0.58f)
                    lineTo(w * 0.72f, h * 0.46f)
                    lineTo(w * 0.64f, h * 0.76f)
                    lineTo(w * 0.34f, h * 0.66f)
                    close()
                }
                drawPath(path = bPath, color = Color(0xFF00E5FF))
            }

            lowerId == "brave" || lowerName.contains("brave") || lowerUrl.contains("brave.") -> {
                // Brave Lion Orange-Crimson Shield
                drawCircle(color = Color(0xFFFB542B), radius = radius, center = center)
                val shield = Path().apply {
                    moveTo(w * 0.50f, h * 0.18f)
                    lineTo(w * 0.78f, h * 0.30f)
                    lineTo(w * 0.68f, h * 0.72f)
                    lineTo(w * 0.50f, h * 0.85f)
                    lineTo(w * 0.32f, h * 0.72f)
                    lineTo(w * 0.22f, h * 0.30f)
                    close()
                }
                drawPath(path = shield, color = Color.White)
                drawCircle(
                    color = Color(0xFFFB542B),
                    radius = radius * 0.22f,
                    center = Offset(w * 0.50f, h * 0.48f)
                )
            }

            else -> {
                // Custom Search Engine Emblem (Vibrant Indigo/Cyan Circle + Search Magnifier Ring)
                drawCircle(color = Color(0xFF6366F1), radius = radius, center = center)
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.36f,
                    center = Offset(w * 0.44f, h * 0.44f),
                    style = Stroke(width = w * 0.12f)
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.58f, h * 0.58f),
                    end = Offset(w * 0.78f, h * 0.78f),
                    strokeWidth = w * 0.13f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun InternalLinkChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        color = cs.surface,
        shape = RoundedCornerShape(50),
        tonalElevation = 3.dp,
        modifier = Modifier
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = cs.onSurface
            )
        }
    }
}

@Composable
fun DevBrowserBookmarksPage(
    bookmarks: List<QuickBookmark>,
    onOpenBookmark: (QuickBookmark) -> Unit,
    onSelectUrl: (String) -> Unit,
    onAddBookmark: (String, String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("https://") }
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_devbrowser_bookmarks"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onBackground
                        )
                        Text(
                            text = "${bookmarks.size} saved sites • Most clicked auto-appear in Shortcuts",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(cs.primary)
                            .clickable { showAddForm = !showAddForm }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                            .testTag("add_bookmark_form_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = cs.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Add Bookmark",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = cs.onPrimary
                            )
                        }
                    }
                }
            }

            if (showAddForm) {
                item {
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(22.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, cs.primary, RoundedCornerShape(22.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "New Bookmark",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = cs.primary
                            )
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Title") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newUrl,
                                onValueChange = { newUrl = it },
                                label = { Text("URL (e.g. https://github.com or devbrowser:test)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(cs.primary)
                                        .clickable {
                                            if (newUrl.isNotBlank()) {
                                                onAddBookmark(
                                                    newTitle.ifBlank { newUrl },
                                                    newUrl.trim()
                                                )
                                                newTitle = ""
                                                newUrl = "https://"
                                                showAddForm = false
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        text = "Save Bookmark",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = cs.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (bookmarks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No bookmarks saved yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(bookmarks, key = { it.url }) { bm ->
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenBookmark(bm) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cs.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = cs.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = bm.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = bm.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onRemoveBookmark(bm.url) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete bookmark ${bm.title}",
                                    tint = cs.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DevBrowserHistoryPage(
    history: List<HistoryItem>,
    onSelectUrl: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_devbrowser_history"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onBackground
                        )
                        Text(
                            text = "${history.size} visited pages • devbrowser:history",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }

                    if (history.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(cs.error.copy(alpha = 0.16f))
                                .border(1.dp, cs.error, RoundedCornerShape(50))
                                .clickable { onClearHistory() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("clear_history_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = cs.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = cs.error
                                )
                            }
                        }
                    }
                }
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Browsing history is empty.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(history, key = { it.id }) { item ->
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSelectUrl(item.url) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = cs.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { onDeleteHistoryItem(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove history item",
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

