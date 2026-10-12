package com.example.ui.pages.settings

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ClearDataTimeRange
import com.example.model.HistoryItem
import com.example.model.SiteDataItem

@Composable
fun DataSettingsSection(
    history: List<HistoryItem>,
    siteVisitCounts: Map<String, Int>,
    onClearBrowsingData: (
        targetDomain: String?,
        timeRange: ClearDataTimeRange,
        deleteHistory: Boolean,
        deleteCookies: Boolean,
        deleteCache: Boolean,
        deleteStorage: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var targetSiteForDialog by remember { mutableStateOf<SiteDataItem?>(null) }
    var statusFeedback by remember { mutableStateOf<String?>(null) }

    // Derive list of visited websites from history and siteVisitCounts
    val websiteItems = remember(history, siteVisitCounts) {
        val domainMap = mutableMapOf<String, MutableList<HistoryItem>>()
        history.forEach { item ->
            val host = extractDomain(item.url)
            if (host.isNotBlank() && !host.startsWith("devbrowser") && host != "about:blank") {
                domainMap.getOrPut(host) { mutableListOf() }.add(item)
            }
        }
        siteVisitCounts.keys.forEach { url ->
            val host = extractDomain(url)
            if (host.isNotBlank() && !host.startsWith("devbrowser") && host != "about:blank") {
                domainMap.getOrPut(host) { mutableListOf() }
            }
        }
        domainMap.map { (domain, items) ->
            val totalVisits = siteVisitCounts.filter { it.key.contains(domain, ignoreCase = true) }
                .values.sum().coerceAtLeast(items.size)
            SiteDataItem(
                domain = domain,
                historyCount = items.size,
                visitCount = totalVisits,
                lastVisited = items.firstOrNull()?.timestamp ?: "Recent"
            )
        }.sortedByDescending { it.visitCount }
    }

    val filteredWebsites = remember(websiteItems, searchQuery) {
        if (searchQuery.isBlank()) websiteItems
        else websiteItems.filter { it.domain.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feedback Banner
        AnimatedVisibility(visible = statusFeedback != null) {
            Surface(
                color = cs.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cs.primary, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = statusFeedback ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.onPrimaryContainer
                    )
                }
            }
        }

        // 1. Site Data Overview
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Browser Site Data & Storage",
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface
                        )
                        Text(
                            text = "Manage cookies, caches, browsing history, and saved web storage",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                // Metric Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sites Metric
                    Surface(
                        color = cs.surfaceVariant,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Websites",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                            Text(
                                text = "${websiteItems.size}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = cs.primary
                            )
                            Text(
                                text = "stored sites",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    // History Metric
                    Surface(
                        color = cs.surfaceVariant,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                            Text(
                                text = "${history.size}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = cs.primary
                            )
                            Text(
                                text = "page visits",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    // Storage Metric
                    Surface(
                        color = cs.surfaceVariant,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Storage",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = cs.secondary
                            )
                            Text(
                                text = "cookies & cache",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = cs.outlineVariant)

                // 2. Delete All Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(cs.error.copy(alpha = 0.14f))
                        .border(1.5.dp, cs.error, RoundedCornerShape(50))
                        .clickable {
                            targetSiteForDialog = null
                            showDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .testTag("delete_all_site_data_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Delete All Data",
                            tint = cs.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Delete All Browsing Data",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = cs.error
                        )
                    }
                }
            }
        }

        // 3. Websites List (Individual Websites Data)
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Websites with Saved Data",
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface
                        )
                        Text(
                            text = "Tap any website to delete its individual cookies, cache, or history",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                // Filter / Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Filter websites…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_websites_input")
                )

                if (filteredWebsites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (websiteItems.isEmpty()) "No site data recorded yet" else "No matching websites found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                            Text(
                                text = "Browse websites to manage their individual data here",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredWebsites.forEach { site ->
                            WebsiteDataItemRow(
                                site = site,
                                onClick = {
                                    targetSiteForDialog = site
                                    showDialog = true
                                },
                                onDelete = {
                                    targetSiteForDialog = site
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 4. Popup Dialog for Selection of Data and Time Range
    if (showDialog) {
        ClearDataPopupDialog(
            targetSite = targetSiteForDialog,
            onDismiss = { showDialog = false },
            onConfirm = { timeRange, deleteHist, deleteCookies, deleteCache, deleteStorage ->
                onClearBrowsingData(
                    targetSiteForDialog?.domain,
                    timeRange,
                    deleteHist,
                    deleteCookies,
                    deleteCache,
                    deleteStorage
                )
                statusFeedback = if (targetSiteForDialog != null) {
                    "Data for ${targetSiteForDialog?.domain} cleared (${timeRange.displayName})!"
                } else {
                    "Browsing data cleared (${timeRange.displayName})!"
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun WebsiteDataItemRow(
    site: SiteDataItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Surface(
        color = cs.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("site_data_item_${site.domain.replace('.', '_')}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Domain avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(cs.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = site.domain.firstOrNull()?.uppercase() ?: "W",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cs.primary
                    )
                }

                Column {
                    Text(
                        text = site.domain,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = cs.onSurface
                    )
                    Text(
                        text = "${site.visitCount} visits • ${site.historyCount} history items • Cookies & Cache",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_site_data_${site.domain.replace('.', '_')}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete data for ${site.domain}",
                    tint = cs.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ClearDataPopupDialog(
    targetSite: SiteDataItem?,
    onDismiss: () -> Unit,
    onConfirm: (
        timeRange: ClearDataTimeRange,
        deleteHistory: Boolean,
        deleteCookies: Boolean,
        deleteCache: Boolean,
        deleteStorage: Boolean
    ) -> Unit
) {
    val cs = MaterialTheme.colorScheme

    var selectedTimeRange by remember { mutableStateOf(ClearDataTimeRange.ALL_TIME) }
    var deleteHistory by remember { mutableStateOf(true) }
    var deleteCookies by remember { mutableStateOf(true) }
    var deleteCache by remember { mutableStateOf(true) }
    var deleteStorage by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (targetSite != null) "Clear Data for ${targetSite.domain}" else "Clear Browsing Data",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (targetSite != null) "Choose what data to delete for ${targetSite.domain}" else "Choose what data to clear from DevBrowser",
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Time Range Section
                Text(
                    text = "Time Range",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = cs.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClearDataTimeRange.entries.forEach { range ->
                        val isSelected = selectedTimeRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) cs.primary else cs.surfaceVariant)
                                .clickable { selectedTimeRange = range }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("time_range_${range.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = range.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) cs.onPrimary else cs.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(color = cs.outlineVariant)

                // What to Delete Checkboxes
                Text(
                    text = "What to Delete",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = cs.primary
                )

                // Browsing History
                ClearDataCheckboxRow(
                    checked = deleteHistory,
                    onCheckedChange = { deleteHistory = it },
                    title = "Browsing History",
                    description = "Deletes visited pages and search entries",
                    icon = Icons.Default.History,
                    testTag = "checkbox_delete_history"
                )

                // Cookies & Site Data
                ClearDataCheckboxRow(
                    checked = deleteCookies,
                    onCheckedChange = { deleteCookies = it },
                    title = "Cookies & Site Data",
                    description = "Signs you out of sites and removes tracking cookies",
                    icon = Icons.Default.Cookie,
                    testTag = "checkbox_delete_cookies"
                )

                // Cached Images & Files
                ClearDataCheckboxRow(
                    checked = deleteCache,
                    onCheckedChange = { deleteCache = it },
                    title = "Cached Images & Files",
                    description = "Frees up local WebView cache memory",
                    icon = Icons.Default.Public,
                    testTag = "checkbox_delete_cache"
                )

                // Site Storage
                ClearDataCheckboxRow(
                    checked = deleteStorage,
                    onCheckedChange = { deleteStorage = it },
                    title = "Site Storage (LocalStorage & Session)",
                    description = "Clears offline saved web application storage",
                    icon = Icons.Default.Storage,
                    testTag = "checkbox_delete_storage"
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(cs.error)
                    .clickable {
                        onConfirm(
                            selectedTimeRange,
                            deleteHistory,
                            deleteCookies,
                            deleteCache,
                            deleteStorage
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("confirm_clear_data_button")
            ) {
                Text(
                    text = "Clear Data",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = cs.onError
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clear_data_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ClearDataCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = cs.primary),
            modifier = Modifier.testTag(testTag)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) cs.primary else cs.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = cs.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant
            )
        }
    }
}

private fun extractDomain(rawUrl: String): String {
    return try {
        val uri = Uri.parse(rawUrl)
        val host = uri.host
        if (!host.isNullOrBlank()) {
            host.removePrefix("www.")
        } else {
            rawUrl.removePrefix("https://").removePrefix("http://").substringBefore("/")
        }
    } catch (e: Exception) {
        rawUrl.removePrefix("https://").removePrefix("http://").substringBefore("/")
    }
}
